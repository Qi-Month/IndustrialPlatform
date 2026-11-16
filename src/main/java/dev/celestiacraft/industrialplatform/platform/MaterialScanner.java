package dev.celestiacraft.industrialplatform.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 材料来源: 玩家主背包 + 控制器周围一小圈容器.
 * <p>
 * 容器范围: 水平 ±{@link #CONTAINER_RADIUS} 格, 上下各 {@link #CONTAINER_UP} / {@link #CONTAINER_DOWN} 格
 */
public class MaterialScanner {
	public static final int CONTAINER_RADIUS = 4;
	public static final int CONTAINER_UP = 1;
	public static final int CONTAINER_DOWN = 1;

	public record Receipt(Map<ItemResource, Integer> resources) {
		public Receipt {
			resources = Collections.unmodifiableMap(new LinkedHashMap<>(resources));
			resources.forEach((resource, amount) -> {
				if (resource.isEmpty() || amount <= 0) {
					throw new IllegalArgumentException("Receipt must contain non-empty resources with positive amounts");
				}
			});
		}
	}

	public static List<ResourceHandler<ItemResource>> sources(ServerLevel level, BlockPos controllerPos, Player player) {
		List<ResourceHandler<ItemResource>> sources = new ArrayList<>();
		ResourceHandler<ItemResource> inventory = PlayerInventoryWrapper.of(player).getMainSlots();
		sources.add(inventory);
		Set<ResourceHandler<ItemResource>> handlers = Collections.newSetFromMap(new IdentityHashMap<>());
		handlers.add(inventory);
		Set<BlockEntity> blockEntities = Collections.newSetFromMap(new IdentityHashMap<>());
		Set<BlockPos> visited = new HashSet<>();

		BlockPos min = controllerPos.offset(-CONTAINER_RADIUS, -CONTAINER_DOWN, -CONTAINER_RADIUS);
		BlockPos max = controllerPos.offset(CONTAINER_RADIUS, CONTAINER_UP, CONTAINER_RADIUS);
		for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
			if (pos.equals(controllerPos) || visited.contains(pos) || !level.isLoaded(pos)) {
				continue;
			}
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity == null || !blockEntities.add(blockEntity)) {
				continue;
			}
			BlockState state = blockEntity.getBlockState();
			BlockPos otherChestPos = state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE
					? pos.relative(ChestBlock.getConnectedDirection(state)) : null;
			if (otherChestPos != null && !level.isLoaded(otherChestPos)) {
				continue;
			}
			ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pos, null);
			if (handler != null && handlers.add(handler)) {
				sources.add(handler);
				if (otherChestPos != null && handler instanceof CombinedResourceHandler<?>) {
					visited.add(otherChestPos);
				}
			}
		}
		return sources;
	}

	/**
	 * 统计每种材料现有多少
	 */
	public static Map<Item, Integer> count(ServerLevel level, BlockPos controllerPos, Player player, Map<Item, Integer> needed) {
		Map<Item, Integer> available = new LinkedHashMap<>();
		List<ResourceHandler<ItemResource>> sources = sources(level, controllerPos, player);
		needed.keySet().forEach(item -> available.put(item, countItem(sources, item, Integer.MAX_VALUE)));
		return available;
	}

	public static boolean canAfford(ServerLevel level, BlockPos controllerPos, Player player, Map<Item, Integer> needed) {
		List<ResourceHandler<ItemResource>> sources = sources(level, controllerPos, player);
		for (Map.Entry<Item, Integer> entry : needed.entrySet()) {
			if (entry.getValue() < 0 || countItem(sources, entry.getKey(), entry.getValue()) < entry.getValue()) {
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

	public static @Nullable Receipt consume(ServerLevel level, BlockPos controllerPos, Player player, Map<Item, Integer> needed) {
		return consume(sources(level, controllerPos, player), needed);
	}

	public static @Nullable Receipt consume(List<ResourceHandler<ItemResource>> sources, Map<Item, Integer> needed) {
		Map<ItemResource, Integer> taken = new LinkedHashMap<>();
		try (Transaction transaction = Transaction.openRoot()) {
			for (Map.Entry<Item, Integer> entry : needed.entrySet()) {
				int remaining = entry.getValue();
				if (remaining < 0) {
					return null;
				}
				for (ResourceHandler<ItemResource> handler : sources) {
					remaining -= extract(handler, entry.getKey(), remaining, taken, transaction);
					if (remaining == 0) {
						break;
					}
				}
				if (remaining > 0) {
					return null;
				}
			}
			Receipt receipt = new Receipt(taken);
			transaction.commit();
			return receipt;
		}
	}

	/**
	 * 把材料还回去(放置失败时用)
	 */
	public static void give(ServerLevel level, BlockPos controllerPos, Player player, Receipt receipt) {
		List<ResourceHandler<ItemResource>> sources = sources(level, controllerPos, player);
		PlayerInventoryWrapper inventory = PlayerInventoryWrapper.of(player);
		try (Transaction transaction = Transaction.openRoot()) {
			for (Map.Entry<ItemResource, Integer> entry : receipt.resources().entrySet()) {
				int remaining = entry.getValue();
				for (ResourceHandler<ItemResource> handler : sources) {
					remaining -= ResourceHandlerUtil.insertStacking(handler, entry.getKey(), remaining, transaction);
					if (remaining == 0) {
						break;
					}
				}
				if (remaining > 0) {
					inventory.placeItemBackInInventory(entry.getKey(), remaining, transaction);
				}
			}
			transaction.commit();
		}
	}

	private static int countItem(List<ResourceHandler<ItemResource>> sources, Item item, int limit) {
		int total = 0;
		for (ResourceHandler<ItemResource> handler : sources) {
			int size = handler.size();
			for (int slot = 0; slot < size && total < limit; slot++) {
				if (handler.getResource(slot).is(item)) {
					total += Math.min(handler.getAmountAsInt(slot), limit - total);
				}
			}
			if (total >= limit) {
				break;
			}
		}
		return total;
	}

	private static int extract(ResourceHandler<ItemResource> handler, Item item, int amount, Map<ItemResource, Integer> taken, TransactionContext transaction) {
		int extracted = 0;
		int size = handler.size();
		for (int slot = 0; slot < size && extracted < amount; slot++) {
			ItemResource resource = handler.getResource(slot);
			if (resource.isEmpty() || !resource.is(item)) {
				continue;
			}
			int count = handler.extract(slot, resource, amount - extracted, transaction);
			if (count > 0) {
				taken.merge(resource, count, Integer::sum);
				extracted += count;
			}
		}
		return extracted;
	}
}
