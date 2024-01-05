package net.skds.skdscore.events;

import lombok.AllArgsConstructor;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.eventbus.api.Event;

@AllArgsConstructor
public class WorldAfterTickEvent extends Event {
	public final ServerLevel world;
}
