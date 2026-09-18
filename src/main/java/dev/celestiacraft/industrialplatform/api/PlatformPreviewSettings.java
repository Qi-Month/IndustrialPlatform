package dev.celestiacraft.industrialplatform.api;

import net.minecraft.core.BlockPos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端缓存: 记录每个平台方块当前的搭建设置.
 * 界面打开时由数据槽驱动, 界面关闭后服务端会通过同步包补上,
 * 这样界面外的区块边界预览也能显示模式与上下填充格数.
 */
public class PlatformPreviewSettings {
	private static final Map<BlockPos, PlatformSettings> CACHE = new ConcurrentHashMap<>();

	public static void put(BlockPos pos, PlatformSettings settings) {
		CACHE.put(pos.immutable(), settings);
	}

	public static PlatformSettings get(BlockPos pos) {
		return CACHE.get(pos);
	}

	public static void remove(BlockPos pos) {
		CACHE.remove(pos);
	}

	public static void clear() {
		CACHE.clear();
	}
}