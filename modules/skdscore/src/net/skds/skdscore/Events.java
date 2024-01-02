package net.skds.skdscore;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.skds.skdscore.mixinglue.ChunkSectionGlue;
import net.skds.skdscore.utils.ServerInfo;
import net.skds.skdscore.world.SectionDataHolder;

import java.util.function.BooleanSupplier;

public class Events {

	@SubscribeEvent
	public void configEvent(ModConfigEvent e) {
		SKDSCoreConfig.onLoad();
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void serverTick(TickEvent.ServerTickEvent e) {
		if (e.phase == TickEvent.Phase.START) {
			ServerInfo.setTickStart(Util.getNanos());
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void serverStart(ServerStartingEvent e) {
		ServerInfo.setServer(e.getServer());
	}

	@SubscribeEvent
	public void loadChunk(ChunkEvent.Load e) {
		ChunkAccess c = e.getChunk();
		if (c.getClass() != LevelChunk.class) {
			return;
		}
		final LevelChunkSection[] sections = c.getSections();
		for (int i = 0; i < sections.length; i++) {
			((ChunkSectionGlue) sections[i]).getDataHolder().onLoad((LevelChunk) c);
		}
	}

	@SubscribeEvent
	public void saveChunk(ChunkDataEvent.Save e) {
		ChunkAccess ca = e.getChunk();
		if (ca.getStatus() != ChunkStatus.FULL) {
			return;
		}
		ListTag list = new ListTag();
		var sections = ca.getSections();
		final int max = ca.getMaxSection();
		for (int i = ca.getMinSection(); i < max; i++) {
			int j = ca.getSectionIndexFromSectionY(i);
			LevelChunkSection sec = sections[j];
			if (sec == null) {
				continue;
			}
			SectionDataHolder holder = ((ChunkSectionGlue) sec).getDataHolder();
			CompoundTag sectionTag = holder.serializeNBT();
			if (sectionTag != null && !sectionTag.isEmpty()) {
				sectionTag.putByte("Y", (byte) i);
				list.add(sectionTag);
			}
		}
		CompoundTag nbt = e.getData();
		nbt.put("skdsSectionData", list);
	}

	@SubscribeEvent
	public void loadChunk(ChunkDataEvent.Load e) {
		if (e.getStatus() != ChunkStatus.ChunkType.LEVELCHUNK) {
			return;
		}
		CompoundTag nbt = e.getData();
		if (!nbt.contains("skdsSectionData")) {
			return;
		}
		ListTag list = nbt.getList("skdsSectionData", 10);
		if (list.isEmpty()) {
			return;
		}
		ChunkAccess ca = e.getChunk();
		final int max = list.size();
		var sections = ca.getSections();
		for (int i = ca.getMinSection(); i < max; i++) {
			CompoundTag tag = list.getCompound(i);
			int j = ca.getSectionIndexFromSectionY(tag.getInt("Y"));
			LevelChunkSection sec = sections[j];
			if (sec == null) {
				continue;
			}
			SectionDataHolder holder = ((ChunkSectionGlue) sec).getDataHolder();
			holder.deserializeNBT(tag);
		}
	}

	public static void serverTickEnd(MinecraftServer server, BooleanSupplier bs) {

	}
}
