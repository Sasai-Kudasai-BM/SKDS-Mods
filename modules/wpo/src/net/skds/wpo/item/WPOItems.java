package net.skds.wpo.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.skds.wpo.WPO;

public class WPOItems {

	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WPO.MOD_ID);
	private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, WPO.MOD_ID);

	public static final RegistryObject<Item> DEBUG_STICK = ITEMS.register("debug_stick", () -> new DebugStickItem(new Item.Properties()));

	public static final RegistryObject<CreativeModeTab> TAB = CREATIVE_TABS.register("main_tab", () -> CreativeModeTab.builder()
			.title(Component.literal("WPO"))
			.icon(Items.WATER_BUCKET::getDefaultInstance)
			.displayItems((parameters, output) -> {
				output.accept(DEBUG_STICK.get());
			}).build());

	public static void init() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ITEMS.register(modEventBus);
		CREATIVE_TABS.register(modEventBus);
	}
}
