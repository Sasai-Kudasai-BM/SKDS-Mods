package net.skds.wpo.chunk;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;
import net.skds.lib.storage.PalettedData;
import net.skds.lib.storage.PalettedStorage;
import net.skds.lib.utils.Holders;
import net.skds.skdscore.world.ChunkSectionData;
import net.skds.skdscore.world.SectionDataHolder;
import net.skds.wpo.fluid.DirectFluidSupl;
import net.skds.wpo.world.CustomWorldTicks;
import net.skds.wpo.world.FluidTickTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FluidSectionData implements ChunkSectionData {

	private static final DirectFluidSupl supl = DirectFluidSupl.INSTANCE;
	private static final FluidState defaultValue = Fluids.EMPTY.defaultFluidState();

	private final PalettedStorage<FluidState> storage;
	private final SectionDataHolder holder;
	private List<FluidTickTask> ticks;

	public FluidSectionData(SectionDataHolder holder) {
		this.holder = holder;
		this.storage = new PalettedStorage<>(4096, defaultValue, supl);
		//this.ticks = new Short2IntOpenHashMap(0, .9f);
	}

	@Override
	public void onLoad(LevelChunk chunk, SectionPos sectionPos) {
		if (ticks != null && !ticks.isEmpty() && chunk.getLevel() instanceof ServerLevel w) {
			var tickTasks = ((CustomWorldTicks) w.getFluidTicks());
			for (FluidTickTask task : ticks) {
				tickTasks.addTickTask(task);
			}
			ticks = null;
		}
	}

	@Override
	public void onUnload(LevelChunk chunk, SectionPos sectionPos) {
		if (chunk.getLevel() instanceof ServerLevel w) {
			var tickTasks = ((CustomWorldTicks) w.getFluidTicks()).tickTasks;
			this.ticks = new ArrayList<>();
			tickTasks.stream()
					.filter(t -> SectionPos.asLong(t.pos) == sectionPos.asLong())
					.forEach(t -> {
						ticks.add(t);
						tickTasks.remove(t);
					});
		}
	}

	public FluidState getFluidState(int x, int y, int z) {
		return holder.getSection().getBlockState(x, y, z).getFluidState();
	}

	@Override
	public int getDataSize() {
		return storage.getDataSize();
	}

	@Override
	public void read(FriendlyByteBuf buf) {
		byte bits = buf.readByte();
		if (bits == 0) {
			int id = buf.readVarInt();
			storage.setDefaultValue(supl.get(id));
		} else {
			final int len = buf.readVarInt();
			long[] data = new long[len];
			for (int i = 0; i < len; i++) {
				data[i] = buf.readLong();
			}
			storage.setData(new PalettedData(bits, 4096, data));
		}
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		if (storage.isSingle()) {
			buf.writeByte(0); // Bits Per Entry
			buf.writeVarInt(supl.getIndex(defaultValue)); // Palette
			//buf.writeVarInt(0); //Data Array Length
			// empty //Data Array
		} else {
			buf.writeByte(storage.bits); // Bits Per Entry
			buf.writeVarInt(storage.getData().words.length); //Data Array Length
			for (int i = 0; i < storage.getData().words.length; i++) {
				buf.writeLong(storage.getData().words[i]);
			}
		}
	}

	@Override
	public CompoundTag serializeNBT() {
		final CompoundTag nbt = new CompoundTag();
		if (ticks != null && !ticks.isEmpty()) {
			ListTag list = new ListTag();
			for (FluidTickTask t : this.ticks) {
				CompoundTag taskTag = new CompoundTag();
				taskTag.putString("id", ForgeRegistries.FLUIDS.getKey(t.fluid).toString());
				taskTag.putLong("pos", t.pos.asLong());
				taskTag.putInt("time", t.startTick);
			}
			nbt.put("ticks", list);
		}
		final ListTag states = new ListTag();
		if (storage.isSingle()) {
			states.add(NbtUtils.writeFluidState(storage.getDefaultValue()));
		} else {
			final PalettedData remappedData = getRemappedData(states);
			nbt.put("data", new LongArrayTag(remappedData.words));
		}
		nbt.put("states", states);
		return nbt;
	}

	private PalettedData getRemappedData(ListTag states) {
		final Int2IntMap values = new Int2IntOpenHashMap(4);
		final Holders.IntHolder c = new Holders.IntHolder(-1);
		final PalettedData data = storage.getData();
		for (int i = 0; i < 4096; i++) {
			int v = data.getValue(i);
			values.computeIfAbsent(v, v2 -> {
				states.add(NbtUtils.writeFluidState(supl.get(v)));
				return c.increment(1);
			});
		}
		final PalettedData remappedData = new PalettedData(PalettedStorage.calcBits(values.size()), 4096);
		for (int i = 0; i < 4096; i++) {
			int v = data.getValue(i);
			remappedData.setValue(i, values.get(v));
		}
		return remappedData;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		final ListTag tickList = nbt.getList("ticks", 10);
		if (!tickList.isEmpty()) {
			this.ticks = new ArrayList<>(tickList.size());
			for (int i = 0; i < tickList.size(); i++) {
				CompoundTag tickTag = tickList.getCompound(i);
				Fluid f = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(tickTag.getString("id")));
				if (f == null) {
					continue;
				}
				BlockPos pos = BlockPos.of(tickTag.getLong("pos"));
				int time = tickTag.getInt("time");
				this.ticks.add(new FluidTickTask(pos, f, time));
			}
		}

		ListTag statesNbt = nbt.getList("states", 10);
		List<FluidState> states = new ArrayList<>();
		for (int i = 0; i < statesNbt.size(); i++) {
			states.add(readFluidState(statesNbt.getCompound(i)));
		}
		if (states.size() == 1) {
			storage.setDefaultValue(states.get(0));
		} else {
			long[] data = nbt.getLongArray("data");
			final PalettedData remappedData = new PalettedData(PalettedStorage.calcBits(states.size()), 4096, data);
			final PalettedData myData = storage.getData();
			for (int i = 0; i < 4096; i++) {
				int v = remappedData.getValue(i);
				int v2 = supl.getIndex(states.get(v));
				myData.setValue(i, v2);
			}
		}
	}

	private FluidState readFluidState(CompoundTag tag) {
		if (!tag.contains("Name", 8)) {
			return defaultValue;
		} else {
			ResourceLocation resourcelocation = new ResourceLocation(tag.getString("Name"));
			Fluid fluid = ForgeRegistries.FLUIDS.getValue(resourcelocation);
			if (fluid == null) {
				return defaultValue;
			} else {
				FluidState fluidState = fluid.defaultFluidState();
				if (tag.contains("Properties", 10)) {
					CompoundTag compoundtag = tag.getCompound("Properties");
					StateDefinition<Fluid, FluidState> statedefinition = fluid.getStateDefinition();
					for (String s : compoundtag.getAllKeys()) {
						Property<?> property = statedefinition.getProperty(s);
						if (property != null) {
							fluidState = setValueHelper(fluidState, property, s, compoundtag);
						}
					}
				}

				return fluidState;
			}
		}
	}

	private static <S extends StateHolder<?, S>, T extends Comparable<T>> S setValueHelper(S state, Property<T> property, String name, CompoundTag tag) {
		Optional<T> optional = property.getValue(tag.getString(name));
		//LOGGER.warn("Unable to read property: {} with value: {} for blockstate: {}", name, tag.getString(name), p_129209_);
		return optional.map(t -> state.setValue(property, t)).orElse(state);
	}
}
