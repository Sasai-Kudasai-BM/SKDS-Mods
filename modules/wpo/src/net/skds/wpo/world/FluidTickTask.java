package net.skds.wpo.world;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluid;
import net.skds.skdscore.world.AsyncWorldViewer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class FluidTickTask implements Comparable<FluidTickTask>, Runnable {

	private ServerLevel world;
	private AsyncWorldViewer viewer;
	@Getter
	private int nextTick;
	private Consumer<FluidTickTask> postProcessTasks;
	public final BlockPos pos;
	public final Fluid fluid;
	public final int startTick;

	public boolean analyzed = false;

	public FluidTickTask(BlockPos pos, Fluid fluid, int startTick) {
		this.pos = pos;
		this.fluid = fluid;
		this.startTick = startTick;
	}

	public void setWorld(AsyncWorldViewer viewer, Consumer<FluidTickTask> postProcessor) {
		this.viewer = viewer;
		this.world = viewer.world;
		this.postProcessTasks = postProcessor;
		this.nextTick = startTick + Math.max(fluid.getTickDelay(world) / 3, 1);
	}


	private void analyzeTick() {
		System.out.println("analyze " + pos);
		postProcessTasks.accept(this);
	}

	private void finalTick() {

		System.out.println("post process " + pos);
	}

	@Override
	public void run() {
		if (analyzed) finalTick();
		else {
			analyzeTick();
			analyzed = true;
		}
	}

	@Override
	public int compareTo(@NotNull FluidTickTask o) {
		if (nextTick == o.nextTick) {
			return pos.compareTo(o.pos);
		}
		return nextTick - o.nextTick;
	}

}
