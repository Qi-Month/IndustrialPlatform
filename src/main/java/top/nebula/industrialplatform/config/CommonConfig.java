package top.nebula.industrialplatform.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class CommonConfig {
	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

	public static final ModConfigSpec.ConfigValue<List<? extends String>> TRIGGER_BLOCK;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> ADJUSTER;
	public static final ModConfigSpec.IntValue TOP_FILLING_DISTANCE;
	public static final ModConfigSpec.IntValue BOTTOM_FILLING_DISTANCE;

	static {
		BUILDER.comment("All settings below will only take effect after restarting the server or client.")
				.push("general");

		TRIGGER_BLOCK = BUILDER
				.comment("Items that can trigger platform placement (right-click).")
				.comment("Use \"#namespace:path\" for tags, \"namespace:path\" for item IDs.")
				.comment("Examples: \"#c:stones\", \"minecraft:cobblestone\"")
				.defineListAllowEmpty(
						"trigger_block",
						List.of("#c:stones"),
						() -> "",
						CommonConfig::validateString
				);

		ADJUSTER = BUILDER
				.comment("Items that can adjust platform mode (right-click) and show boundary preview (hold).")
				.comment("Use \"#namespace:path\" for tags, \"namespace:path\" for item IDs.")
				.comment("Examples: \"#c:tools/wrench\", \"minecraft:stick\"")
				.defineListAllowEmpty(
						"adjuster",
						List.of("#c:tools/wrench", "minecraft:stick"),
						() -> "",
						CommonConfig::validateString
				);

		TOP_FILLING_DISTANCE = BUILDER
				.comment("The distance that the platform fills on its top")
				.comment("type: int")
				.comment("default: 5")
				.defineInRange("top_filling_distance", 5, 0, 64);

		BOTTOM_FILLING_DISTANCE = BUILDER
				.comment("The distance that the platform fills on its bottom")
				.comment("type: int")
				.comment("default: 5")
				.defineInRange("bottom_filling_distance", 5, 0, 64);

		BUILDER.pop();
	}

	private static boolean validateString(Object obj) {
		return obj instanceof String;
	}

	public static final ModConfigSpec SPEC = BUILDER.build();
}
