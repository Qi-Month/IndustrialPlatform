package dev.celestiacraft.industrialplatform.block.state.properties.platform;

import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class PlatformProperties {
	/**
	 * 搭建界面里向上/向下填充格数的范围
	 */
	public static final int MIN_FILL_DISTANCE = 0;
	public static final int MAX_FILL_DISTANCE = 64;

	/**
	 * 悬浮: 上下都不进行填充与清理
	 */
	public static final BooleanProperty FLOATING = BooleanProperty.create("floating");

	public static final EnumProperty<PlatformMode> PLATFORM_MODE = EnumProperty.create("type", PlatformMode.class);
}