package dev.celestiacraft.industrialplatform.client;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoundaryRenderData {
	private static final List<BoundaryEntry> entries = new ArrayList<>();
	private static long lastUpdateTick = 0;

	public record BoundaryEntry(BlockPos pos, boolean extended, boolean floating) {}

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
}