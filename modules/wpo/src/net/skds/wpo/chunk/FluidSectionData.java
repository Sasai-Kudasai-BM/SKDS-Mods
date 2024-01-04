package net.skds.wpo.chunk;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.shorts.Short2IntMap;
import it.unimi.dsi.fastutil.shorts.Short2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FluidSectionData implements ChunkSectionData {

	private static final DirectFluidSupl supl = DirectFluidSupl.INSTANCE;
	private static final FluidState defaultValue = Fluids.EMPTY.defaultFluidState();

	private final PalettedStorage<FluidState> storage;
	private final SectionDataHolder holder;
	private final Short2IntMap ticks;

	private boolean tickable;

	public FluidSectionData(SectionDataHolder holder) {
		this.holder = holder;
		this.storage = new PalettedStorage<>(4096, defaultValue, supl);
		this.ticks = new Short2IntOpenHashMap(0, .9f);
		this.ticks.defaultReturnValue(-1);
	}

	@Override
	public void onLoad(LevelChunk chunk, long sectionPos) {
		if (isTickable() && chunk.getLevel() instanceof ServerLevel w) {
			((CustomWorldTicks<Fluid>) w.getFluidTicks()).tickSections.put(sectionPos, this);
		}
	}

	public boolean isTickable() {
		return tickable = !ticks.isEmpty();
	}

	public boolean addTick(BlockPos pos, int time) {
		return addTick(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15, time);
	}

	public boolean addTick(int x, int y, int z, int time) {
		int position = SectionDataHolder.getIndex(x, y, z);
		if (ticks.put((short) position, time) == -1) {
			boolean last = tickable;
			tickable = true;
			return !last;
		}
		return false;
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
		if (!ticks.isEmpty()) {
			final IntArrayList tickList = new IntArrayList();
			for (Short2IntMap.Entry e : this.ticks.short2IntEntrySet()) {
				tickList.add(e.getShortKey());
				tickList.add(e.getIntValue());
			}
			nbt.put("ticks", new IntArrayTag(tickList));
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
		final int[] tickArray = nbt.getIntArray("ticks");
		for (int i = 0; i < tickArray.length; ) {
			this.ticks.put((short) tickArray[i++], tickArray[i++]);
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
