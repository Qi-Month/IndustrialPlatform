package top.qm.industrialplatform.block.state.properties.platform;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum PlatformMode implements StringRepresentable {
    INDUSTRIAL_LIGHT("industrial_light"),
    INDUSTRIAL_HEAVY("industrial_heavy"),
    CHECKERBOARD_LIGHT("checkerboard_light"),
    CHECKERBOARD_HEAVY("checkerboard_heavy"),
    POOL("pool");

    private final String name;

    PlatformMode(String name) {
        this.name = name;
    }

    public @NotNull String getSerializedName() {
        return this.name;
    }
}