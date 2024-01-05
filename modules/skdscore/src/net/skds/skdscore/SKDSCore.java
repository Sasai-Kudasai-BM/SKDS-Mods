package net.skds.skdscore;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.skds.skdscore.world.SectionDataRegistry;


@Mod(SKDSCore.MOD_ID)
public class SKDSCore {

	public static final String MOD_ID = "skdscore";
	public static final String MOD_NAME = "SKDS Core";
	public static final String MOD_VERSION = "1.0.0";

	public static final IEventBus EVENT_BUS = BusBuilder.builder().useModLauncher().build();

	public SKDSCore() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::commonSetup);

		MinecraftForge.EVENT_BUS.register(new Events());
		if (FMLEnvironment.dist == Dist.CLIENT) {
			initClient();
		} else {
			initDedicatedServer();
		}


		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SKDSCoreConfig.SPEC);
	}

	public void commonSetup(FMLCommonSetupEvent e) {
		SectionDataRegistry.finish();
	}

	@OnlyIn(Dist.CLIENT)
	private void initClient() {
		MinecraftForge.EVENT_BUS.register(new ClientEvents());
	}

	@OnlyIn(Dist.DEDICATED_SERVER)
	private void initDedicatedServer() {
		MinecraftForge.EVENT_BUS.register(new ServerEvents());
	}
}
