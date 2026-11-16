package dev.celestiacraft.industrialplatform.api;

import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformMode;
import dev.celestiacraft.industrialplatform.common.menu.PlatformBuildMenu;
import dev.celestiacraft.industrialplatform.config.CommonConfig;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

/**
 * 一个平台控制器上的搭建设置: 模式 + 向上/向下填充格数 + (设计台用的)蓝图 id
 */
public record PlatformSettings(PlatformMode mode, int upFill, int downFill, @Nullable String blueprintId) {
	public static PlatformSettings defaults(PlatformMode mode) {
		return new PlatformSettings(
				mode,
				PlatformBuildMenu.clampFill(CommonConfig.TOP_FILLING_DISTANCE.get()),
				PlatformBuildMenu.clampFill(CommonConfig.BOTTOM_FILLING_DISTANCE.get()),
				null
		);
	}

	public static PlatformSettings fromNBT(CompoundTag tag) {
		String blueprint = tag.getStringOr("Blueprint", "");
		return new PlatformSettings(
				PlatformMode.byIndex(tag.getIntOr("Mode", 0)),
				tag.getIntOr("UpFill", 0),
				tag.getIntOr("DownFill", 0),
				blueprint.isEmpty() ? null : blueprint
		);
	}

	public CompoundTag toNBT() {
		CompoundTag tag = new CompoundTag();
		tag.putInt("Mode", mode.ordinal());
		tag.putInt("UpFill", upFill);
		tag.putInt("DownFill", downFill);
		tag.putString("Blueprint", blueprintId == null ? "" : blueprintId);
		return tag;
	}

	public boolean hasBlueprint() {
		return blueprintId != null && !blueprintId.isEmpty();
	}

	public PlatformSettings withBlueprint(@Nullable String id) {
		return new PlatformSettings(mode, upFill, downFill, id);
	}

	public PlatformSettings withMode(PlatformMode mode) {
		return this.mode == mode ? this : new PlatformSettings(mode, upFill, downFill, blueprintId);
	}

	public PlatformSettings withFill(int upFill, int downFill) {
		return new PlatformSettings(mode, upFill, downFill, blueprintId);
	}

	public static PlatformSettings resolve(@Nullable PlatformSettings stored, PlatformMode mode) {
		return stored == null ? defaults(mode) : stored.withMode(mode);
	}
}