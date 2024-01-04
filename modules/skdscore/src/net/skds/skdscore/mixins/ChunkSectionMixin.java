package net.skds.skdscore.mixins;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.skds.skdscore.mixinglue.ChunkSectionGlue;
import net.skds.skdscore.world.SectionDataHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunkSection.class)
public abstract class ChunkSectionMixin implements ChunkSectionGlue {

	SectionDataHolder dataHolder = new SectionDataHolder((LevelChunkSection) (Object) this);

	@Override
	public SectionDataHolder getDataHolder() {
		return dataHolder;
	}

	@Inject(method = "read", at = @At("TAIL"))
	void read(FriendlyByteBuf buf, CallbackInfo ci) {
		dataHolder.read(buf);
	}

	@Inject(method = "write", at = @At("TAIL"))
	void write(FriendlyByteBuf buf, CallbackInfo ci) {
		dataHolder.write(buf);
	}

	@Inject(method = "getSerializedSize", at = @At("RETURN"), cancellable = true)
	void getSerializedSize(CallbackInfoReturnable<Integer> ci) {
		int value = ci.getReturnValueI();
		ci.setReturnValue(value + dataHolder.getExtraSize());
	}
}
