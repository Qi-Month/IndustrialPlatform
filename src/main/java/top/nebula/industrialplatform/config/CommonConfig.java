package top.nebula.industrialplatform.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfig {
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

	static {
		BUILDER.comment("All settings below will only take effect after restarting the server or client.")
				.push("general");
	}

	private static boolean validateString(Object obj) {
		return obj instanceof String;
	}

	public static final ForgeConfigSpec SPEC = BUILDER.build();
}