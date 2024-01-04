package net.skds.wpo.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.material.FluidState;
import net.skds.skdscore.world.AsyncWorldViewer;

public class DebugStickItem extends Item {

	public DebugStickItem(Properties p) {
		super(p);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (!(context.getLevel() instanceof ServerLevel sl)) {
			return InteractionResult.PASS;
		}
		AsyncWorldViewer viewer = new AsyncWorldViewer(sl);
		BlockPos pos = context.getClickedPos().offset(context.getClickedFace().getNormal());
		FluidState fs = viewer.getFluidState(pos);

		context.getPlayer().sendSystemMessage(Component.literal("use " + fs));

		return InteractionResult.PASS;
	}
}
