package dev.celestiacraft.industrialplatform.config;

import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformMode;
import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformProperties;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class CommonConfig {
	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

	/**
	 * 可以放进搭建界面材料槽的物品
	 */
	public static final ModConfigSpec.ConfigValue<List<? extends String>> PLATFORM_MATERIAL;
	/**
	 * 额外的"平台调节器"物品: 手持后右键平台方块切换平台类型, 并显示区块边界预览
	 * <p>
	 * 填充调节器(industrial_platform:fill_adjuster)已经在代码里硬绑, 不需要写进这里
	 */
	public static final ModConfigSpec.ConfigValue<List<? extends String>> ADJUSTER;

	/**
	 * 搭建界面里向上 / 向下填充格数的默认值
	 */
	public static final ModConfigSpec.IntValue TOP_FILLING_DISTANCE;
	public static final ModConfigSpec.IntValue BOTTOM_FILLING_DISTANCE;

	public static final ModConfigSpec.IntValue MIN_PLATFORM_CHUNKS;
	public static final ModConfigSpec.IntValue MAX_PLATFORM_CHUNKS;

	/**
	 * 搭建一次平台消耗的材料数量
	 */
	public static final ModConfigSpec.IntValue LIGHT_PLATFORM_COST;
	public static final ModConfigSpec.IntValue HEAVY_PLATFORM_COST;
	public static final ModConfigSpec.IntValue FILLING_LAYER_COST;

	static {
		BUILDER.comment("All settings below will only take effect after restarting the server or client.")
				.push("general");

		PLATFORM_MATERIAL = BUILDER
				.comment("Items that can be put into the material slot of the platform builder screen.")
				.comment("Use #namespace:path for tags, namespace:path for item IDs.")
				.comment("The item tag industrial_platform:platform_material is accepted as well.")
				.comment("Examples: #c:stones, minecraft:cobblestone")
				.defineListAllowEmpty(
						"platform_material",
						List.of("#c:stones", "minecraft:cobblestone"),
						() -> "",
						CommonConfig::validateString
				);

		ADJUSTER = BUILDER
				.comment("Extra items treated as a platform adjuster: right-click a platform block to cycle its type,")
				.comment("hold one to display the block boundary preview.")
				.comment("The fill adjuster (industrial_platform:fill_adjuster) is built in and needs no entry here.")
				.comment("Use #namespace:path for tags, namespace:path for item IDs.")
				.comment("Examples: #c:tools/wrench, minecraft:stick")
				.defineListAllowEmpty(
						"adjuster",
						List.of("#c:tools/wrench"),
						() -> "",
						CommonConfig::validateString
				);

		TOP_FILLING_DISTANCE = BUILDER
				.comment("Default value of the fill-up field shown in the platform builder screen.")
				.comment("The player can still change it in the screen, range: " + PlatformProperties.MIN_FILL_DISTANCE + " ~ " + PlatformProperties.MAX_FILL_DISTANCE)
				.comment("type: int")
				.comment("default: 5")
				.defineInRange("top_filling_distance", 5, PlatformProperties.MIN_FILL_DISTANCE, PlatformProperties.MAX_FILL_DISTANCE);

		BOTTOM_FILLING_DISTANCE = BUILDER
				.comment("Default value of the fill-down field shown in the platform builder screen.")
				.comment("The player can still change it in the screen, range: " + PlatformProperties.MIN_FILL_DISTANCE + " ~ " + PlatformProperties.MAX_FILL_DISTANCE)
				.comment("type: int")
				.comment("default: 5")
				.defineInRange("bottom_filling_distance", 5, PlatformProperties.MIN_FILL_DISTANCE, PlatformProperties.MAX_FILL_DISTANCE);

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

		BUILDER.comment("How many items a single platform build consumes.").push("cost");

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

	public static final ModConfigSpec SPEC = BUILDER.build();

	private static boolean validateString(Object object) {
		return object instanceof String;
	}
}