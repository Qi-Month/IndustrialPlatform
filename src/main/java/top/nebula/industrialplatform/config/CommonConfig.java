package top.nebula.industrialplatform.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CommonConfig {
	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

	static {
		BUILDER.comment("All settings below will only take effect after restarting the server or client.")
				.push("general");

		BUILDER.pop();
	}

	private static boolean validateString(Object obj) {
		return obj instanceof String;
	}

	public static final ModConfigSpec SPEC = BUILDER.build();
}