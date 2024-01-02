package net.skds.wpo;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.skds.skdscore.utils.WorldSide;
import net.skds.skdscore.world.ChunkSectionData;
import net.skds.skdscore.world.SectionDataRegistry;
import net.skds.skdscore.world.SectionDataRegistryEntry;
import net.skds.wpo.chunk.FluidSectionData;
import net.skds.wpo.fluid.DirectFluidSupl;


@Mod(WPO.MOD_ID)
public class WPO {

	public static final String MOD_ID = "wpo";

	public static final SectionDataRegistryEntry<ChunkSectionData> REGISTRY_ENTRY = SectionDataRegistry.regSectionData(FluidSectionData::new, WorldSide.BOTH, "fluidData");

	public WPO() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::commonSetup);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, WPOConfig.SPEC);

		MinecraftForge.EVENT_BUS.register(new Events());
	}


	public void commonSetup(FMLCommonSetupEvent e) {
		DirectFluidSupl.INSTANCE.init();
	}

}
