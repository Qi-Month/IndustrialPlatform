package dev.celestiacraft.industrialplatform.block.state.properties.platform;

import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum PlatformMode implements StringRepresentable {
	INDUSTRIAL_LIGHT("industrial_light"),
	INDUSTRIAL_HEAVY("industrial_heavy"),
	CHECKERBOARD_LIGHT("checkerboard_light"),
	CHECKERBOARD_HEAVY("checkerboard_heavy");

	private static final PlatformMode[] VALUES = values();

	private final String name;

	PlatformMode(String name) {
		this.name = name;
	}

	/**
	 * 棋盘格样式
	 */
	public boolean isCheckerboard() {
		return this == CHECKERBOARD_LIGHT || this == CHECKERBOARD_HEAVY;
	}

	/**
	 * 重型平台, 占用 3x3 区块
	 */
	public boolean isHeavy() {
		return this == INDUSTRIAL_HEAVY || this == CHECKERBOARD_HEAVY;
	}

	/**
	 * 平台占用的区块边长
	 */
	public int chunkSize() {
		return this.isHeavy() ? 3 : 1;
	}

	public static PlatformMode of(boolean checkerboard, boolean heavy) {
		if (checkerboard) {
			return heavy ? CHECKERBOARD_HEAVY : CHECKERBOARD_LIGHT;
		}
		return heavy ? INDUSTRIAL_HEAVY : INDUSTRIAL_LIGHT;
	}

	/**
	 * 网络传输 / 数据槽读取时的安全取值
	 */
	public static PlatformMode byIndex(int index) {
		return VALUES[Mth.clamp(index, 0, VALUES.length - 1)];
	}

	@Override
	public @NotNull String getSerializedName() {
		return this.name;
	}
}