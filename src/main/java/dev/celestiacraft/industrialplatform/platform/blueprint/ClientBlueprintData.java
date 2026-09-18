package dev.celestiacraft.industrialplatform.platform.blueprint;

import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Map;

/**
 * 客户端手里的蓝图清单(由服务端发过来), 设计台界面用它画列表和材料
 */
public class ClientBlueprintData {
	/**
	 * @param materials 材料 -> 需要数量
	 */
	public record Entry(String id, int sizeX, int sizeY, int sizeZ, int blockCount, Map<Item, Integer> materials) {
		public String describeSize() {
			return sizeX + "x" + sizeY + "x" + sizeZ;
		}
	}

	private static volatile List<Entry> entries = List.of();
	private static volatile String selectedId = "";

	public static void set(List<Entry> newEntries) {
		entries = List.copyOf(newEntries);
	}

	public static void setSelected(String id) {
		selectedId = id == null ? "" : id;
	}

	public static String getSelected() {
		return selectedId;
	}

	public static List<Entry> list() {
		return entries;
	}

	public static Entry get(String id) {
		if (id == null || id.isEmpty()) {
			return null;
		}

		for (Entry entry : entries) {
			if (entry.id().equals(id)) {
				return entry;
			}
		}

		return null;
	}

	public static boolean isEmpty() {
		return entries.isEmpty();
	}

	public static void clear() {
		entries = List.of();
		selectedId = "";
	}
}