package dev.celestiacraft.industrialplatform.api;

import dev.celestiacraft.industrialplatform.block.state.properties.platform.PlatformMode;
import org.jetbrains.annotations.Nullable;

/**
 * 一个平台控制器上的搭建设置: 模式 + 向上/向下填充格数 + (设计台用的)蓝图 id
 */
public record PlatformSettings(PlatformMode mode, int upFill, int downFill, @Nullable String blueprintId) {
	/**
	 * 上下都不填充 => 悬浮
	 */
	public boolean isFloating() {
		return this.upFill == 0 && this.downFill == 0;
	}

	public boolean hasBlueprint() {
		return this.blueprintId != null && !this.blueprintId.isEmpty();
	}

	public PlatformSettings withBlueprint(@Nullable String id) {
		return new PlatformSettings(this.mode, this.upFill, this.downFill, id);
	}

	public PlatformSettings withFill(int upFill, int downFill) {
		return new PlatformSettings(this.mode, upFill, downFill, this.blueprintId);
	}
}