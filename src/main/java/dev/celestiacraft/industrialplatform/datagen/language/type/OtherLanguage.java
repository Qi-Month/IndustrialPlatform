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
				"§aRight click with a wrench or a stick to cycle the platform type\n§aSneak + right click toggles floating\n§aRight click with a material to deploy the platform\n§aThe Fill Adjuster sets how far it fills (scroll wheel)\n§aWith enable_builder_ui = true, right click with an empty hand to open the builder screen",
				"§a手持扳手或木棍右键切换平台类型\n§a潜行右键切换是否悬浮\n§a手持材料右键即可展开平台\n§a填充调节器(滚轮)决定上下填充格数\n§a启用搭建界面后, 空手右键即可打开搭建界面"
		);

		addCustomLang(
				"tooltip.industrial_platform.platform_designer.off",
				"§eHold Shift for details",
				"§e按住Shift查看详情"
		);

		addCustomLang(
				"tooltip.industrial_platform.platform_designer",
				"§aRight click with an empty hand to open the platform designer\n§aDesign custom platforms: size, style and the blocks to build with\n§aBlueprint files can be dropped into schematics/platform",
				"§a空手右键即可打开平台设计界面\n§a自定义平台: 尺寸, 样式与用哪些方块搭建\n§a蓝图文件放进 schematics/platform 目录即可被识别"
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
				"message.industrial_platform.no_material",
				"Put a material into the slot first!",
				"请先在槽位中放入材料!"
		);

		addCustomLang(
				"message.industrial_platform.not_enough_material",
				"Not enough material, %s required!",
				"材料不足, 需要 %s 个!"
		);

		addCustomLang(
				"message.industrial_platform.build_failed",
				"Build failed: the platform structure is missing!",
				"搭建失败: 缺少平台结构文件!"
		);

		addCustomLang(
				"menu.industrial_platform.platform_build",
				"Platform Builder",
				"平台搭建"
		);

		addCustomLang(
				"menu.industrial_platform.platform_designer",
				"Platform Designer",
				"平台设计台"
		);

		addCustomLang(
				"message.industrial_platform.no_blueprint",
				"No blueprint selected! Put .nbt files into schematics/platform",
				"没有选中蓝图! 把 .nbt 文件放进 schematics/platform 目录"
		);

		addCustomLang(
				"message.industrial_platform.missing_materials",
				"Missing materials: %s",
				"材料不足: %s"
		);

		addCustomLang(
				"gui.industrial_platform.blueprint_empty",
				"No blueprint found",
				"没有找到蓝图"
		);

		addCustomLang(
				"gui.industrial_platform.blueprint_material_tip",
				"%s x%s",
				"%s ×%s"
		);

		addCustomLang(
				"gui.industrial_platform.blueprint_materials",
				"Materials",
				"材料"
		);

		addCustomLang(
				"tooltip.industrial_platform.fill_adjuster.up",
				"Fill up: %s",
				"向上填充: %s 格"
		);

		addCustomLang(
				"tooltip.industrial_platform.fill_adjuster.down",
				"Fill down: %s",
				"向下填充: %s 格"
		);

		addCustomLang(
				"tooltip.industrial_platform.fill_adjuster.usage",
				"Scroll wheel: adjust (sneak = fill down, Ctrl = 10 at a time)",
				"滚轮: 站着调向上填充, 潜行调向下填充, 按住 Ctrl 一次 10 格"
		);

		addCustomLang(
				"tooltip.industrial_platform.fill_adjuster.alt",
				"Hold Alt while scrolling to switch hotbar slots as usual",
				"按住 Alt 滚动即可正常切换快捷栏"
		);

		addCustomLang(
				"tooltip.industrial_platform.fill_adjuster.deploy",
				"Right click a platform block to deploy it",
				"对准平台方块右键即可展开"
		);

		addCustomLang(
				"message.industrial_platform.fill_values",
				"Fill up: %s / Fill down: %s",
				"向上填充: %s / 向下填充: %s"
		);

		addCustomLang(
				"gui.industrial_platform.fill_up",
				"Fill Up",
				"向上填充"
		);

		addCustomLang(
				"gui.industrial_platform.fill_down",
				"Fill Down",
				"向下填充"
		);

		addCustomLang(
				"gui.industrial_platform.fill_range",
				"Range: %s ~ %s",
				"范围: %s ~ %s"
		);

		addCustomLang(
				"gui.industrial_platform.style",
				"Style",
				"平台样式"
		);

		addCustomLang(
				"gui.industrial_platform.style.industrial",
				"Industrial",
				"工业"
		);

		addCustomLang(
				"gui.industrial_platform.style.industrial.tooltip",
				"Industrial style platform",
				"工业风格的平台"
		);

		addCustomLang(
				"gui.industrial_platform.style.checkerboard",
				"Checkerboard",
				"棋盘格"
		);

		addCustomLang(
				"gui.industrial_platform.style.checkerboard.tooltip",
				"Checkerboard style platform",
				"棋盘格风格的平台"
		);

		addCustomLang(
				"gui.industrial_platform.size",
				"Size",
				"平台尺寸"
		);

		addCustomLang(
				"gui.industrial_platform.size.light",
				"1x1 chunk",
				"1×1 区块"
		);

		addCustomLang(
				"gui.industrial_platform.size.light.tooltip",
				"Standard platform, covers 1x1 chunk",
				"普通平台, 覆盖 1×1 区块"
		);

		addCustomLang(
				"gui.industrial_platform.size.heavy",
				"3x3 chunks",
				"3×3 区块"
		);

		addCustomLang(
				"gui.industrial_platform.size.heavy.tooltip",
				"Heavy platform, covers 3x3 chunks",
				"重型平台, 覆盖 3×3 区块"
		);

		addCustomLang(
				"gui.industrial_platform.material",
				"Material",
				"材料"
		);

		addCustomLang(
				"gui.industrial_platform.preview",
				"Preview",
				"预览"
		);

		addCustomLang(
				"gui.industrial_platform.preview.info",
				"%sx%s  ↑%s ↓%s",
				"%s×%s  ↑%s ↓%s"
		);

		addCustomLang(
				"gui.industrial_platform.build_plain",
				"Build",
				"搭建"
		);

		addCustomLang(
				"gui.industrial_platform.not_enough_materials",
				"Not enough materials",
				"材料不足"
		);

		addCustomLang(
				"gui.industrial_platform.build",
				"Build (x%s)",
				"搭建 (×%s)"
		);

		addCustomLang(
				"gui.industrial_platform.build.tooltip",
				"%s %s\nFill up: %s\nFill down: %s",
				"%s%s\n向上填充: %s 格\n向下填充: %s 格"
		);

		addCustomLang(
				"gui.industrial_platform.build.missing_material",
				"Put a material into the slot first",
				"请先在槽位中放入材料"
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
				"平台尺寸: 1×1 区块"
		);

		addCustomLang(
				"tooltip.jade.industrial_platform.heavy",
				"Platform scale: 3x3 chunks",
				"平台尺寸: 3×3 区块"
		);

		addCustomLang(
				"tooltip.jade.industrial_platform.floating",
				"Placement: floating (nothing filled)",
				"放置方式: 悬浮 (不填充)"
		);

		addCustomLang(
				"tooltip.jade.industrial_platform.filling",
				"Placement: filled",
				"放置方式: 已填充"
		);

		addCustomLang(
				"jei.info.industrial_platform.platform",
				"Right click a platform block with an empty hand to open the platform builder\nA wrench or a stick works just as well\nAdjust the fill height and choose the style / size in the screen\nPut a material into the slot, then press Build to deploy the platform",
				"空手右键平台方块即可打开搭建界面\n手持扳手或木棍右键也一样\n在界面中调整上下填充格数并选择样式与尺寸\n放入材料后点击搭建按钮即可展开平台"
		);

		addCustomLang(
				"jei.info.industrial_platform.fluid_pool",
				"Right click with any stone to deploy the infinite pool\nStand besides the block with a wrench or a stick in hand to display preview\nFluid Pools will not remove blocks above\nThe pool is over 10,000 blocks, which supports infinite fluid in Create",
				"以任意石头右键展开流体池\n手持调节器靠近流体池方块以显示展开预览\n流体池不会清除上方方块\n展开的流体池容量超过1万格, 足够支持机械动力的无限流体"
		);
	}
}