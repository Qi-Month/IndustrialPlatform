package dev.celestiacraft.industrialplatform.datagen.language.type;

import dev.celestiacraft.industrialplatform.datagen.language.LanguageGenerate;

public class OtherLanguage extends LanguageGenerate {
	public static void addLang() {
		addCustomLang(
				"tooltip.industrial_platform.industrial_platform.off",
				"§eHold Shift for details",
				"§e按住Shift查看详情"
		);

		addCustomLang(
				"tooltip.industrial_platform.industrial_platform",
				"§aRight click with a wrench or stick to adjust Types of Platform\n§aRight click while sneaking to choose fill the space or not\n§aRight click with any stone to deploy the platform",
				"§a右键调节平台类型\n§a潜行右键以切换是否向上下填充\n§a以任意石头右键展开平台"
		);

		addCustomLang(
				"tooltip.industrial_platform.fluid_pool",
				"§aRight Click with any stone to deploy the infinite pool",
				"§a以任意石头右键展开流体池"
		);

		addCustomLang(
				"config.jade.plugin_industrial_platform.common",
				"Industrial Platform Tooltip",
				"工业平台: 物品提示"
		);

		addCustomLang(
				"message.industrial_platform.done",
				"Platform deployed successfully!",
				"平台放置成功!"
		);

		addCustomLang(
				"message.industrial_platform.pool_done",
				"Pool deployed successfully!",
				"流体池已展开!"
		);

		addCustomLang(
				"message.industrial_platform.too_low",
				"Too low to deploy an Infinity Pool!",
				"太低了! 流体池无法展开!"
		);

		addCustomLang(
				"tooltip.jade.industrial_platform.industrial",
				"Platform type: Industrial",
				"平台类型: 工业平台"
		);

		addCustomLang(
				"tooltip.jade.industrial_platform.checkerboard",
				"Platform type: Checkerboard",
				"平台类型: 棋盘格平台"
		);

		addCustomLang(
				"tooltip.jade.industrial_platform.light",
				"Platform scale: 1x1 chunk",
				"平台尺寸: 1x1区块"
		);

		addCustomLang(
				"tooltip.jade.industrial_platform.heavy",
				"Platform scale: 3x3 chunks",
				"平台尺寸: 3x3区块"
		);

		addCustomLang(
				"tooltip.jade.industrial_platform.floating",
				"Blocks above the platform will be removed",
				"平台上方的方块将被移除"
		);

		addCustomLang(
				"tooltip.jade.industrial_platform.filling",
				"Blocks above the platform will not be removed",
				"平台上方的方块将会保留"
		);

		addCustomLang(
				"jei.info.industrial_platform.platform",
				"A Wrench or a stick is required for adjustment\nStand besides the block with a wrench or a stick in hand to display preview\nRight click to adjust Types of Platform\nRight click while sneaking to choose fill the space or not\nRight click with any stone to deploy the platform",
				"使用木棍或扳手进行调节\n手持调节器靠近平台方块以显示展开预览\n站立右键调节平台类型\n潜行右键以切换是否向上下填充\n以任意石头右键展开平台"
		);

		addCustomLang(
				"jei.info.industrial_platform.fluid_pool",
				"Right click with any stone to deploy the infinite pool\nStand besides the block with a wrench or a stick in hand to display preview\nFluid Pools will not remove blocks above\nThe pool is over 10,000 blocks, which supports infinite fluid in Create",
				"以任意石头右键展开流体池\n手持调节器靠近流体池方块以显示展开预览\n流体池不会清除上方方块\n展开的流体池容量超过1万格, 足够支持机械动力的无限流体"
		);
	}
}