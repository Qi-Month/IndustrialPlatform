package top.qm.industrialplatform.block.state.properties;

import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;

public class PlatformProperties {
    public static final EnumProperty<PlatformMode> PLATFORM_MODE;
    public static final BooleanProperty FLOATING = BooleanProperty.create("floating");

    public PlatformProperties() {
    }

    static {
        PLATFORM_MODE = EnumProperty.create("type", PlatformMode.class);
    }
}
