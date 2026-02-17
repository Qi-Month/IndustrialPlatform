package dev.celestiacraft.industrialplatform.block.state.properties.platform;

import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class PlatformProperties {
	public static final BooleanProperty FLOATING = BooleanProperty.create("floating");

	public PlatformProperties() {
	}

	public static final EnumProperty<PlatformMode> PLATFORM_MODE;

	static {
		PLATFORM_MODE = EnumProperty.create("type", PlatformMode.class);
	}
}