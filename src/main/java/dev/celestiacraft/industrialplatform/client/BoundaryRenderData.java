package dev.celestiacraft.industrialplatform.client;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoundaryRenderData {
	private static final List<BoundaryEntry> entries = new ArrayList<>();
	/**
	 * 界面里正在编辑的那个平台, 优先于扫描结果绘制
	 */
	private static volatile @Nullable BoundaryEntry override;
	private static long lastUpdateTick = 0;

	/**
	 * @param sizeX    东西方向长度(方块数)
	 * @param sizeZ    南北方向长度(方块数)
	 * @param upFill   向上填充格数, -1 表示未知(只有方块状态信息)
	 * @param downFill 向下填充格数, -1 表示未知
	 */
	public record BoundaryEntry(BlockPos pos, int sizeX, int sizeZ, boolean floating, int upFill, int downFill) {
		public boolean hasFillInfo() {
			return upFill >= 0 && downFill >= 0;
		}

		public int chunksX() {
			return Math.max(1, (sizeX + 15) / 16);
		}

		public int chunksZ() {
			return Math.max(1, (sizeZ + 15) / 16);
		}
	}

	public static void update(List<BoundaryEntry> newEntries, long tick) {
		entries.clear();
		entries.addAll(newEntries);
		lastUpdateTick = tick;
	}

	public static List<BoundaryEntry> getEntries() {
		return Collections.unmodifiableList(entries);
	}

	public static boolean isExpired(long currentTick) {
		return currentTick - lastUpdateTick > 10;
	}

	public static void clear() {
		entries.clear();
	}

	public static void setOverride(@Nullable BoundaryEntry entry) {
		override = entry;
	}

	public static @Nullable BoundaryEntry getOverride() {
		return override;
	}

	public static void clearOverride() {
		override = null;
	}
}