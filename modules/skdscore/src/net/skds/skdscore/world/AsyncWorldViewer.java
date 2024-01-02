package net.skds.skdscore.world;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.FluidState;
import net.skds.skdscore.mixins.ChunkSourceInvoker;

public class AsyncWorldViewer {

	public final ServerLevel world;
	public final BlockState voidState = Blocks.VOID_AIR.defaultBlockState();
	public final FluidState voidFluidState = voidState.getFluidState();


	public AsyncWorldViewer(ServerLevel world) {
		this.world = world;
	}

	public BlockState getBlockState(int x, int y, int z) {
		if (world.isOutsideBuildHeight(y)) {
			return voidState;
		}
		LevelChunkSection section = getSection(x >> 4, world.getSectionIndex(y), z >> 4);
		if (section == null) {
			return null;
		}
		return section.getBlockState(x & 15, y & 15, z & 15);
	}

	public FluidState getFluidState(int x, int y, int z) {
		if (world.isOutsideBuildHeight(y)) {
			return voidFluidState;
		}
		LevelChunkSection section = getSection(x >> 4, world.getSectionIndex(y), z >> 4);
		if (section == null) {
			return null;
		}
		return section.getFluidState(x & 15, y & 15, z & 15);
	}

	public LevelChunkSection getSection(int cx, int cy, int cz) {
		LevelChunk chunk = getChunk(cx, cz);
		if (chunk == null) {
			return null;
		}
		if (cy < 0) {
			return null;
		}
		LevelChunkSection[] sections = chunk.getSections();
		if (sections.length <= cy) {
			return null;
		}
		return sections[cy];
	}

	public LevelChunk getChunk(int cx, int cz) {
		long pos = ChunkPos.asLong(cx, cz);
		ChunkHolder holder = ((ChunkSourceInvoker) world.getChunkSource()).getLoadedChunkAsync(pos);
		return holder.getFullChunk();
	}
}
