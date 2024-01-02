package net.skds.wpo;

import lombok.Getter;
import net.minecraftforge.common.ForgeConfigSpec;

public class WPOConfig {

	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

	private static final ForgeConfigSpec.BooleanValue DEBUG = BUILDER
			.comment("Enables debug")
			.define("debug", false);

	static final ForgeConfigSpec SPEC = BUILDER.build();

	@Getter
	private static boolean debug;


	static void onLoad() {
		debug = DEBUG.get();
	}
}
