package net.skds.wpo.mixins;

import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.FluidState;
import net.skds.skdscore.mixinglue.ChunkSectionGlue;
import net.skds.wpo.WPO;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunkSection.class)
public abstract class ChunkSectionMixin implements ChunkSectionGlue {

	@Inject(method = "getFluidState", at = @At("HEAD"), cancellable = true)
	void getFluidState(int x, int y, int z, CallbackInfoReturnable<FluidState> ci) {
		FluidState state = getDataHolder().getData(WPO.FLUID_SECTION_DATA).getFluidState(x, y, z);
		ci.setReturnValue(state);
	}

}
