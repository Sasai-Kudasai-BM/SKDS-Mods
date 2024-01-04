package net.skds.wpo.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;
import net.skds.skdscore.world.AsyncWorldViewer;
import net.skds.wpo.WPO;
import net.skds.wpo.chunk.FluidSectionData;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.BiConsumer;
import java.util.function.LongPredicate;
import java.util.function.Supplier;

@SuppressWarnings("NullableProblems")
public class CustomWorldTicks<T> extends LevelTicks<T> {

	public final ServerLevel world;
	public final AsyncWorldViewer viewer;

	public final ConcurrentSkipListMap<Long, FluidSectionData> tickSections = new ConcurrentSkipListMap<>();

	public CustomWorldTicks(LongPredicate p, Supplier<ProfilerFiller> s, ServerLevel world) {
		super(p, s);
		this.world = world;
		this.viewer = new AsyncWorldViewer(world);
	}

	@Override
	public void addContainer(ChunkPos p_193232_, LevelChunkTicks<T> p_193233_) {
	}

	@Override
	public void removeContainer(ChunkPos p_193230_) {
	}

	@Override
	public void schedule(ScheduledTick<T> st) {
		System.out.println(st);
		FluidSectionData fd = viewer.getSectionDataForBlock(WPO.FLUID_SECTION_DATA, st.pos());
		if (fd == null) {
			return;
		}
		if (fd.addTick(st.pos(), (int) world.getLevelData().getGameTime())) {
			long secPos = SectionPos.asLong(st.pos());
			tickSections.put(secPos, fd);
		}
	}

	@Override
	public void tick(long p_193226_, int p_193227_, BiConsumer<BlockPos, T> p_193228_) {
	}

	@Override
	public boolean hasScheduledTick(BlockPos p_193254_, T p_193255_) {
		return false;
	}

	@Override
	public boolean willTickThisTick(BlockPos p_193282_, T p_193283_) {
		return false;
	}

	@Override
	public void clearArea(BoundingBox p_193235_) {
	}

	@Override
	public void copyArea(BoundingBox p_193243_, Vec3i p_193244_) {
	}

	@Override
	public void copyAreaFrom(LevelTicks<T> p_265554_, BoundingBox p_265172_, Vec3i p_265318_) {
	}

	@Override
	public int count() {
		return 0;
	}

}
