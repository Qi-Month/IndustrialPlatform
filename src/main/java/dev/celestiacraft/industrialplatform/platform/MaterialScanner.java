package dev.celestiacraft.industrialplatform.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 材料来源: 玩家主背包 + 控制器周围一小圈容器.
 * <p>
 * 容器范围: 水平 ±{@link #CONTAINER_RADIUS} 格, 上下各 {@link #CONTAINER_UP} / {@link #CONTAINER_DOWN} 格
 */
public class MaterialScanner {
	public static final int CONTAINER_RADIUS = 4;
	public static final int CONTAINER_UP = 1;
	public static final int CONTAINER_DOWN = 1;
	/**
	 * 只动主背包与快捷栏
	 */
	private static final int PLAYER_SLOTS = 36;

	public static List<IItemHandler> sources(ServerLevel level, BlockPos controllerPos, Player player) {
		List<IItemHandler> sources = new ArrayList<>();
		sources.add(new RangedWrapper(new InvWrapper(player.getInventory()), 0, PLAYER_SLOTS));

		BlockPos min = controllerPos.offset(-CONTAINER_RADIUS, -CONTAINER_DOWN, -CONTAINER_RADIUS);
		BlockPos max = controllerPos.offset(CONTAINER_RADIUS, CONTAINER_UP, CONTAINER_RADIUS);

		for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity == null || pos.equals(controllerPos)) {
				continue;
			}

			IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
			if (handler != null) {
				sources.add(handler);
			}
		}

		return sources;
	}

	/**
	 * 统计每种材料现有多少
	 */
	public static Map<Item, Integer> count(ServerLevel level, BlockPos controllerPos, Player player, Map<Item, Integer> needed) {
		Map<Item, Integer> available = new LinkedHashMap<>();
		List<IItemHandler> sources = sources(level, controllerPos, player);

		needed.keySet().forEach((item) -> {
			int total = 0;
			for (IItemHandler handler : sources) {
				total += countItem(handler, item);
			}
			available.put(item, total);
		});

		return available;
	}

	public static boolean canAfford(ServerLevel level, BlockPos controllerPos, Player player, Map<Item, Integer> needed) {
		List<IItemHandler> sources = sources(level, controllerPos, player);

		for (Map.Entry<Item, Integer> entry : needed.entrySet()) {
			int total = 0;

			for (IItemHandler handler : sources) {
				total += countItem(handler, entry.getKey());
				if (total >= entry.getValue()) {
					break;
				}
			}

			if (total < entry.getValue()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 还缺哪几种材料(给玩家提示用)
	 */
	public static Map<Item, Integer> missing(ServerLevel level, BlockPos controllerPos, Player player, Map<Item, Integer> needed) {
		Map<Item, Integer> available = count(level, controllerPos, player, needed);
		Map<Item, Integer> missing = new LinkedHashMap<>();

		needed.forEach((item, amount) -> {
			int have = available.getOrDefault(item, 0);
			if (have < amount) {
				missing.put(item, amount - have);
			}
		});

		return missing;
	}

	public static boolean consume(ServerLevel level, BlockPos controllerPos, Player player, Map<Item, Integer> needed) {
		List<IItemHandler> sources = sources(level, controllerPos, player);
		Map<Item, Integer> taken = new LinkedHashMap<>();

		for (Map.Entry<Item, Integer> entry : needed.entrySet()) {
			int remaining = entry.getValue();

			for (IItemHandler handler : sources) {
				remaining -= extract(handler, entry.getKey(), remaining);
				if (remaining <= 0) {
					break;
				}
			}

			if (remaining > 0) {
				taken.put(entry.getKey(), entry.getValue() - remaining);
				refund(sources, taken);
				return false;
			}

			taken.put(entry.getKey(), entry.getValue());
		}

		return true;
	}

	/**
	 * 把材料还回去(放置失败时用)
	 */
	public static void give(ServerLevel level, BlockPos controllerPos, Player player, Map<Item, Integer> items) {
		refund(sources(level, controllerPos, player), items);
	}

	private static int countItem(IItemHandler handler, Item item) {
		int total = 0;

		for (int slot = 0; slot < handler.getSlots(); slot++) {
			ItemStack stack = handler.getStackInSlot(slot);
			if (stack.is(item)) {
				total += stack.getCount();
			}
		}

		return total;
	}

	private static int extract(IItemHandler handler, Item item, int amount) {
		int taken = 0;

		for (int slot = 0; slot < handler.getSlots() && taken < amount; slot++) {
			ItemStack stack = handler.getStackInSlot(slot);
			if (!stack.is(item)) {
				continue;
			}

			ItemStack extracted = handler.extractItem(slot, amount - taken, false);
			taken += extracted.getCount();
		}

		return taken;
	}

	private static void refund(List<IItemHandler> sources, Map<Item, Integer> taken) {
		taken.forEach((item, amount) -> {
			int remaining = amount;

			for (IItemHandler handler : sources) {
				if (remaining <= 0) {
					return;
				}

				ItemStack leftover = ItemHandlerHelper.insertItemStacked(handler, new ItemStack(item, remaining), false);
				remaining = leftover.getCount();
			}
		});
	}
}