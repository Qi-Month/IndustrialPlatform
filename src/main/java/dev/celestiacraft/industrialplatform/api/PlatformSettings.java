package dev.celestiacraft.industrialplatform.api;

import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformMode;
import org.jetbrains.annotations.Nullable;

/**
 * 一个平台控制器上的搭建设置: 模式 + 向上/向下填充格数 + (设计台用的)蓝图 id
 */
public record PlatformSettings(PlatformMode mode, int upFill, int downFill, @Nullable String blueprintId) {
	/**
	 * 上下都不填充 => 悬浮
	 */
	public boolean isFloating() {
		return upFill == 0 && downFill == 0;
	}

	public boolean hasBlueprint() {
		return blueprintId != null && !blueprintId.isEmpty();
	}

	public PlatformSettings withBlueprint(@Nullable String id) {
		return new PlatformSettings(mode, upFill, downFill, id);
	}

	public PlatformSettings withFill(int upFill, int downFill) {
		return new PlatformSettings(mode, upFill, downFill, blueprintId);
	}
}