package dev.celestiacraft.industrialplatform.platform;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 平台调色板: 玩家在搭建界面里选的几种方块
 *
 * @param borderPrimary   外圈条纹主色
 * @param borderSecondary 外圈条纹副色
 * @param base            底板底色(工业样式整片底板 / 棋盘样式的主色格)
 * @param checker         棋盘样式的副色格
 * @param lamp            灯饰(边框内侧每隔若干格一盏)
 */
public record PlatformPalette(BlockState borderPrimary, BlockState borderSecondary, BlockState base, BlockState checker, BlockState lamp) {
	/**
	 * 与内置平台一致: 黄/黑混凝土条纹边框, 深板岩瓦或雪块+白混凝土棋盘, 海晶灯
	 */
	public static PlatformPalette defaults(PlatformStyle style) {
		BlockState borderPrimary = Blocks.YELLOW_CONCRETE.defaultBlockState();
		BlockState borderSecondary = Blocks.BLACK_CONCRETE.defaultBlockState();
		BlockState lamp = Blocks.SEA_LANTERN.defaultBlockState();

		if (style == PlatformStyle.CHECKERBOARD) {
			return new PlatformPalette(
					borderPrimary,
					borderSecondary,
					Blocks.SNOW_BLOCK.defaultBlockState(),
					Blocks.WHITE_CONCRETE.defaultBlockState(),
					lamp
			);
		}

		BlockState base = Blocks.DEEPSLATE_TILES.defaultBlockState();
		return new PlatformPalette(borderPrimary, borderSecondary, base, base, lamp);
	}

	/**
	 * 空的槽位用兜底调色板补上(整合包里方块被移除时)
	 */
	public PlatformPalette withFallback(PlatformPalette fallback) {
		return new PlatformPalette(
				borderPrimary != null ? borderPrimary : fallback.borderPrimary(),
				borderSecondary != null ? borderSecondary : fallback.borderSecondary(),
				base != null ? base : fallback.base(),
				checker != null ? checker : fallback.checker(),
				lamp != null ? lamp : fallback.lamp()
		);
	}
}