package dev.celestiacraft.industrialplatform.config;

import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformMode;
import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformProperties;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class CommonConfig {
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

	/**
	 * 可以放进搭建界面材料槽的物品
	 */
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> PLATFORM_MATERIAL;
	/**
	 * 手持后右键平台方块可以打开搭建界面的物品
	 */
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ADJUSTER;

	/**
	 * 搭建界面里向上 / 向下填充格数的默认值
	 */
	public static final ForgeConfigSpec.IntValue TOP_FILLING_DISTANCE;
	public static final ForgeConfigSpec.IntValue BOTTOM_FILLING_DISTANCE;

	public static final ForgeConfigSpec.IntValue MIN_PLATFORM_CHUNKS;
	public static final ForgeConfigSpec.IntValue MAX_PLATFORM_CHUNKS;

	/**
	 * 搭建一次平台消耗的材料数量(旧版固定消耗, 正在被材料统计替换)
	 */
	public static final ForgeConfigSpec.IntValue LIGHT_PLATFORM_COST;
	public static final ForgeConfigSpec.IntValue HEAVY_PLATFORM_COST;
	public static final ForgeConfigSpec.IntValue FILLING_LAYER_COST;

	static {
		BUILDER.comment("All settings below will only take effect after restarting the server or client.")
				.push("general");

		PLATFORM_MATERIAL = BUILDER
				.comment("Items that can be put into the material slot of the platform builder screen.")
				.comment("Use #namespace:path for tags, namespace:path for item IDs.")
				.comment("The item tag industrial_platform:platform_material is accepted as well.")
				.comment("Examples: #forge:stone, minecraft:cobblestone")
				.defineListAllowEmpty(
						"platform_material",
						List.of("#forge:stone", "minecraft:cobblestone"),
						CommonConfig::validateString
				);

		ADJUSTER = BUILDER
				.comment("Extra items treated as a platform adjuster: standing right click a platform block to cycle")
				.comment("its type, and holding one displays the block boundary preview.")
				.comment("The Fill Adjuster item is always treated as one and does not need to be listed here.")
				.comment("Use #namespace:path for tags, namespace:path for item IDs.")
				.comment("Examples: #forge:tools/wrench")
				.defineListAllowEmpty(
						"adjuster",
						List.of("#forge:tools/wrench"),
						CommonConfig::validateString
				);

		TOP_FILLING_DISTANCE = BUILDER
				.comment("Default value of the fill-up field shown in the platform builder screen.")
				.comment("The player can still change it in the screen, range: " + PlatformProperties.MIN_FILL_DISTANCE + " ~ " + PlatformProperties.MAX_FILL_DISTANCE)
				.comment("type: int")
				.comment("default: 5")
				.defineInRange("default_top_filling_distance", 5, PlatformProperties.MIN_FILL_DISTANCE, PlatformProperties.MAX_FILL_DISTANCE);

		BOTTOM_FILLING_DISTANCE = BUILDER
				.comment("Default value of the fill-down field shown in the platform builder screen.")
				.comment("The player can still change it in the screen, range: " + PlatformProperties.MIN_FILL_DISTANCE + " ~ " + PlatformProperties.MAX_FILL_DISTANCE)
				.comment("type: int")
				.comment("default: 5")
				.defineInRange("default_bottom_filling_distance", 5, PlatformProperties.MIN_FILL_DISTANCE, PlatformProperties.MAX_FILL_DISTANCE);

		BUILDER.pop();

		BUILDER.comment("Platform footprint limits, measured in chunks per side.").push("platform");

		MIN_PLATFORM_CHUNKS = BUILDER
				.comment("Smallest platform footprint, in chunks per side.")
				.comment("default: 1")
				.defineInRange("min_platform_chunks", 1, 1, 8);

		MAX_PLATFORM_CHUNKS = BUILDER
				.comment("Largest platform footprint, in chunks per side.")
				.comment("default: 4 (64x64 blocks)")
				.defineInRange("max_platform_chunks", 4, 1, 8);

		BUILDER.pop();

		BUILDER.comment("Legacy cost settings, kept while the material system is being rewritten.").push("cost");

		LIGHT_PLATFORM_COST = BUILDER
				.comment("Cost of a standard platform (1x1 chunk).")
				.comment("default: 1")
				.defineInRange("light_platform_cost", 1, 0, 4096);

		HEAVY_PLATFORM_COST = BUILDER
				.comment("Cost of a heavy platform (3x3 chunks).")
				.comment("default: 1, raise it if a heavy platform should cost more")
				.defineInRange("heavy_platform_cost", 1, 0, 4096);

		FILLING_LAYER_COST = BUILDER
				.comment("Extra cost for every layer filled up / down.")
				.comment("default: 0")
				.defineInRange("filling_layer_cost", 0, 0, 4096);

		BUILDER.pop();
	}

	/**
	 * 一次搭建需要消耗的材料数量
	 */
	public static int getBuildCost(PlatformMode mode, int upFill, int downFill) {
		int base = mode.isHeavy() ? HEAVY_PLATFORM_COST.get() : LIGHT_PLATFORM_COST.get();
		int perLayer = FILLING_LAYER_COST.get();

		return Math.max(0, base + perLayer * (Math.max(0, upFill) + Math.max(0, downFill)));
	}

	private static boolean validateString(Object object) {
		return object instanceof String;
	}

	public static final ForgeConfigSpec SPEC = BUILDER.build();
}