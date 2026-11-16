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
				"§aStanding right click with a wrench or a fill adjuster cycles the platform type\n§aRight click with a material deploys the platform\n§aSneak + right click with an empty hand opens the builder screen\n§aThe Fill Adjuster sets how far it fills (sneak + scroll wheel)",
				"§a站立时手持扳手或填充调节器右键切换平台类型\n§a手持材料右键即可展开平台\n§a潜行+空手右键即可打开搭建界面\n§a填充调节器(潜行+滚轮)决定上下填充格数"
		);

		addCustomLang(
				"tooltip.industrial_platform.platform_builder.off",
				"§eHold Shift for details",
				"§e按住Shift查看详情"
		);

		addCustomLang(
				"tooltip.industrial_platform.platform_builder",
				"§aRight click to open the Platform Building Station\n§aDesign custom platforms: size, style and the blocks to build with\n§aBlueprint files can be dropped into schematics/platform",
				"§a右键即可打开平台建造站\n§a自定义平台: 尺寸, 样式与用哪些方块搭建\n§a蓝图文件放进 schematics/platform 目录即可被识别"
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
				"menu.industrial_platform.platform_builder",
				"Platform Building Station",
				"平台建造站"
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
				"tooltip.industrial_platform.fill_adjuster.target",
				"Adjusting: %s",
				"当前调节: %s"
		);

		addCustomLang(
				"tooltip.industrial_platform.fill_adjuster.switch",
				"Sneak + right click: switch between fill up / fill down",
				"潜行右键: 切换向上填充 / 向下填充"
		);

		addCustomLang(
				"tooltip.industrial_platform.fill_adjuster.usage",
				"Sneak + scroll: adjust the selected value (Ctrl = 10 at a time)",
				"潜行滚轮: 调节当前选中的数值 (按住 Ctrl 一次 10 格)"
		);

		addCustomLang(
				"tooltip.industrial_platform.fill_adjuster.alt",
				"Hold Alt while sneaking to switch hotbar slots instead",
				"潜行时按住 Alt 滚动则改为切换快捷栏"
		);

		addCustomLang(
				"tooltip.industrial_platform.fill_adjuster.deploy",
				"Standing right click a platform block cycles the platform type",
				"站立时右键平台方块切换平台类型"
		);

		addCustomLang(
				"message.industrial_platform.fill_values",
				"Adjusting %s: %s",
				"当前调节 %s: %s 格"
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
				"tooltip.jade.industrial_platform.fill_up",
				"Fill up: %s",
				"向上填充: %s 格"
		);

		addCustomLang(
				"tooltip.jade.industrial_platform.fill_down",
				"Fill down: %s",
				"向下填充: %s 格"
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
				"Sneak + right click a platform block with an empty hand to open the platform builder\nHold a wrench or the fill adjuster to display the block boundary preview\nAdjust the fill height and choose the style / size in the screen\nPut a material into the slot, then press Build to deploy the platform",
				"潜行+空手右键平台方块即可打开搭建界面\n手持扳手或填充调节器可显示区块边界预览\n在界面中调整上下填充格数并选择样式与尺寸\n放入材料后点击搭建按钮即可展开平台"
		);

		addCustomLang(
				"jei.info.industrial_platform.fluid_pool",
				"Right click with any stone to deploy the infinite pool\nStand besides the block with a wrench or the fill adjuster in hand to display preview\nFluid Pools will not remove blocks above\nThe pool is over 10,000 blocks, which supports infinite fluid in Create",
				"以任意石头右键展开流体池\n手持填充调节器靠近流体池方块以显示展开预览\n流体池不会清除上方方块\n展开的流体池容量超过1万格, 足够支持机械动力的无限流体"
		);

		addCustomLang(
				"industrial_platform.configuration.title",
				"Industrial Platform Configuration",
				"工业平台配置"
		);

		addCustomLang(
				"industrial_platform.configuration.section.nebula.industrial.platform.common.toml",
				"Common Settings",
				"通用设置"
		);

		addCustomLang(
				"industrial_platform.configuration.section.nebula.industrial.platform.common.toml.title",
				"Industrial Platform — Common Settings",
				"工业平台 — 通用设置"
		);

		addCustomLang(
				"industrial_platform.configuration.general",
				"General",
				"常规"
		);

		addCustomLang(
				"industrial_platform.configuration.general.tooltip",
				"Materials, additional adjusters and default fill distances. Restart the server or client after changing these settings.",
				"材料、额外调节器与默认填充距离。修改这些设置后请重启服务端或客户端。"
		);

		addCustomLang(
				"industrial_platform.configuration.platform",
				"Platform Size",
				"平台尺寸"
		);

		addCustomLang(
				"industrial_platform.configuration.platform.tooltip",
				"Minimum and maximum platform size, in chunks per side.",
				"平台每边的最小与最大区块数。"
		);

		addCustomLang(
				"industrial_platform.configuration.cost",
				"Material Costs",
				"材料消耗"
		);

		addCustomLang(
				"industrial_platform.configuration.cost.tooltip",
				"Base material costs for standard and heavy platforms, plus the extra cost per filled layer.",
				"普通与重型平台的基础材料消耗，以及每层填充的额外消耗。"
		);

		addCustomLang(
				"industrial_platform.configuration.platform_material",
				"Platform Materials",
				"平台材料"
		);

		addCustomLang(
				"industrial_platform.configuration.platform_material.tooltip",
				"Items accepted by the builder material slot. Use #namespace:path for tags or namespace:path for item IDs. The industrial_platform:platform_material item tag is also accepted. Defaults: #c:stones, minecraft:cobblestone.",
				"搭建界面材料槽接受的物品。标签使用 #命名空间:路径，物品 ID 使用 命名空间:路径。industrial_platform:platform_material 物品标签也会被接受。默认：#c:stones、minecraft:cobblestone。"
		);

		addCustomLang(
				"industrial_platform.configuration.adjuster",
				"Additional Platform Adjusters",
				"额外平台调节器"
		);

		addCustomLang(
				"industrial_platform.configuration.adjuster.tooltip",
				"Extra items that cycle the platform type on right-click and show the block boundary preview while held. Use #namespace:path for tags or namespace:path for item IDs. The Fill Adjuster is built in. Default: #c:tools/wrench.",
				"额外的调节器物品：右键切换平台类型，手持时显示区块边界预览。标签使用 #命名空间:路径，物品 ID 使用 命名空间:路径。填充调节器已内置，无需添加。默认：#c:tools/wrench。"
		);

		addCustomLang(
				"industrial_platform.configuration.top_filling_distance",
				"Default Upward Fill Distance",
				"默认向上填充距离"
		);

		addCustomLang(
				"industrial_platform.configuration.top_filling_distance.tooltip",
				"Initial upward fill distance in the builder screen. Players can change it in the screen. Default: 5 blocks.",
				"搭建界面中向上填充距离的初始值，玩家仍可在界面中调整。默认：5 格。"
		);

		addCustomLang(
				"industrial_platform.configuration.bottom_filling_distance",
				"Default Downward Fill Distance",
				"默认向下填充距离"
		);

		addCustomLang(
				"industrial_platform.configuration.bottom_filling_distance.tooltip",
				"Initial downward fill distance in the builder screen. Players can change it in the screen. Default: 5 blocks.",
				"搭建界面中向下填充距离的初始值，玩家仍可在界面中调整。默认：5 格。"
		);

		addCustomLang(
				"industrial_platform.configuration.min_platform_chunks",
				"Minimum Platform Size",
				"最小平台尺寸"
		);

		addCustomLang(
				"industrial_platform.configuration.min_platform_chunks.tooltip",
				"Smallest allowed platform footprint, in chunks per side. Default: 1.",
				"允许的最小平台尺寸，以每边区块数计。默认：1。"
		);

		addCustomLang(
				"industrial_platform.configuration.max_platform_chunks",
				"Maximum Platform Size",
				"最大平台尺寸"
		);

		addCustomLang(
				"industrial_platform.configuration.max_platform_chunks.tooltip",
				"Largest allowed platform footprint, in chunks per side. Default: 4 (64 × 64 blocks).",
				"允许的最大平台尺寸，以每边区块数计。默认：4（64 × 64 格）。"
		);

		addCustomLang(
				"industrial_platform.configuration.light_platform_cost",
				"Standard Platform Cost",
				"普通平台消耗"
		);

		addCustomLang(
				"industrial_platform.configuration.light_platform_cost.tooltip",
				"Base number of material items consumed by a standard platform build. Default: 1.",
				"每次搭建普通平台消耗的基础材料数量。默认：1。"
		);

		addCustomLang(
				"industrial_platform.configuration.heavy_platform_cost",
				"Heavy Platform Cost",
				"重型平台消耗"
		);

		addCustomLang(
				"industrial_platform.configuration.heavy_platform_cost.tooltip",
				"Base number of material items consumed by a heavy platform build. Increase it to make heavy platforms more expensive. Default: 1.",
				"每次搭建重型平台消耗的基础材料数量。调高此值可提高重型平台的消耗。默认：1。"
		);

		addCustomLang(
				"industrial_platform.configuration.filling_layer_cost",
				"Cost per Filled Layer",
				"每层填充消耗"
		);

		addCustomLang(
				"industrial_platform.configuration.filling_layer_cost.tooltip",
				"Extra material items consumed for each layer filled upward or downward. Added to the base platform cost. Default: 0.",
				"每向上或向下填充一层额外消耗的材料数量，计入平台基础消耗之外。默认：0。"
		);
	}
}