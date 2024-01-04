package net.skds.skdscore.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.util.INBTSerializable;

public interface ChunkSectionData extends INBTSerializable<CompoundTag> {

	int getDataSize();

	void read(FriendlyByteBuf buf);

	void write(FriendlyByteBuf buf);

	void onLoad(LevelChunk chunk, long sectionPos);
}
