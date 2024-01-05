package net.skds.wpo;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.skds.wpo.world.CustomWorldTicks;

public class Events {

	@SubscribeEvent
	public void unloadWorld(LevelEvent.Unload e) {
		if (e.getLevel() instanceof ServerLevel w) {
			((CustomWorldTicks) w.getFluidTicks()).unload();
		}
	}
}
