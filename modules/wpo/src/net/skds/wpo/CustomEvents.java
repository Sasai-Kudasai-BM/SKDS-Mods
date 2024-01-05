package net.skds.wpo;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.skds.skdscore.events.WorldAfterTickEvent;
import net.skds.skdscore.utils.ServerInfo;
import net.skds.wpo.world.CustomWorldTicks;

public class CustomEvents {

	@SubscribeEvent
	public void onServerWorldTick(WorldAfterTickEvent e) {
		((CustomWorldTicks) e.world.getFluidTicks()).onTickEnd(() -> ServerInfo.remainTimeMillis() > 5);
	}
}
