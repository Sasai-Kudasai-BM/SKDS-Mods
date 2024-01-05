package net.skds.wpo.fluid;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.registries.ForgeRegistries;
import net.skds.lib.storage.PalettedStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DirectFluidSupl implements PalettedStorage.DirectSupplier<FluidState> {

	public static final DirectFluidSupl INSTANCE = new DirectFluidSupl();

	private FluidState[] fluidArray;
	private Reference2IntMap<FluidState> stateMap;


	public void init() {

		Reference2IntMap<FluidState> stateIds = new Reference2IntOpenHashMap<>();

		Collection<Fluid> fluids = ForgeRegistries.FLUIDS.getValues();
		final List<FluidState> list = new ArrayList<>();
		for (Fluid f : fluids) {
			for (FluidState state : f.getStateDefinition().getPossibleStates()) {
				int i = list.size();
				list.add(state);
				stateIds.put(state, i);
				//System.out.println(state);
			}
		}
		fluidArray = list.toArray(new FluidState[0]);
		stateMap = stateIds;
	}

	@Override
	public FluidState get(int index) {
		return fluidArray[index];
	}

	@Override
	public int getIndex(FluidState value) {
		return stateMap.getOrDefault(value, 0);
	}

	@Override
	public int size() {
		return fluidArray.length;
	}

	@Override
	public int bitThreshold() {
		return 0;
	}

	@Override
	public int minBits() {
		return 0;
	}
}
