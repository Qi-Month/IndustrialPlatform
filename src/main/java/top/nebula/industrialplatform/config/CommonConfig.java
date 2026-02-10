package top.nebula.industrialplatform.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class CommonConfig {
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> TRIGGER_BLOCK;
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ADJUSTER;

	static {
		BUILDER.comment("All settings below will only take effect after restarting the server or client.")
				.push("general");

		TRIGGER_BLOCK = BUILDER
				.comment("Items that can trigger platform placement (right-click).")
				.comment("Use \"#namespace:path\" for tags, \"namespace:path\" for item IDs.")
				.comment("Examples: \"#forge:stone\", \"minecraft:cobblestone\"")
				.defineListAllowEmpty(
						"trigger_block",
						List.of("#forge:stones"),
						CommonConfig::validateString
				);

		ADJUSTER = BUILDER
				.comment("Items that can adjust platform mode (right-click) and show boundary preview (hold).")
				.comment("Use \"#namespace:path\" for tags, \"namespace:path\" for item IDs.")
				.comment("Examples: \"#forge:tools/wrench\", \"minecraft:stick\"")
				.defineListAllowEmpty(
						"adjuster",
						List.of("#forge:tools/wrench", "minecraft:stick"),
						CommonConfig::validateString
				);

		BUILDER.pop();
	}

	private static boolean validateString(Object object) {
		return object instanceof String;
	}

	public static final ForgeConfigSpec SPEC = BUILDER.build();
}