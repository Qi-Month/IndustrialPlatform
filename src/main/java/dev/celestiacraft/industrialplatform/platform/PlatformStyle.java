package dev.celestiacraft.industrialplatform.platform;

import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformMode;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * 平台底板的图案样式
 */
public enum PlatformStyle implements StringRepresentable {
	/**
	 * 工业: 底色铺满 + 警示条纹边框
	 */
	INDUSTRIAL("industrial"),
	/**
	 * 棋盘: 底色与副色棋盘 + 警示条纹边框
	 */
	CHECKERBOARD("checkerboard");

	private final String name;

	PlatformStyle(String name) {
		this.name = name;
	}

	public static PlatformStyle byIndex(int index) {
		PlatformStyle[] values = values();
		return values[net.minecraft.util.Mth.clamp(index, 0, values.length - 1)];
	}

	public static PlatformStyle of(PlatformMode mode) {
		return mode.isCheckerboard() ? CHECKERBOARD : INDUSTRIAL;
	}

	@Override
	public @NotNull String getSerializedName() {
		return name;
	}
}