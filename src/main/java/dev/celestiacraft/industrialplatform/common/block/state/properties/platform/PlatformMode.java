package dev.celestiacraft.industrialplatform.common.block.state.properties.platform;

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
		return isHeavy() ? 3 : 1;
	}

	/**
	 * 内置结构文件名: data/industrial_platform/structures/&lt;id&gt;.nbt
	 * <p>
	 * 数据包放同名文件即可覆盖, 缺失时由 PlatformGenerator 程序化生成兜底
	 */
	public String structureId() {
		return switch (this) {
			case INDUSTRIAL_LIGHT -> "industrial";
			case INDUSTRIAL_HEAVY -> "industrial_h";
			case CHECKERBOARD_LIGHT -> "checkerboard";
			case CHECKERBOARD_HEAVY -> "checkerboard_h";
		};
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
		return name;
	}
}