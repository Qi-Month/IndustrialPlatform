package dev.celestiacraft.industrialplatform.platform;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.common.register.IPBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@EventBusSubscriber(modid = IndustrialPlatform.MODID)
public class PoolDrops implements AutoCloseable {
	private static final ThreadLocal<Capture> CURRENT = new ThreadLocal<>();
	private static final ProblemReporter MERGE_REPORTER = new ProblemReporter() {
		@Override
		public ProblemReporter forChild(PathElement path) {
			return this;
		}

		@Override
		public void report(Problem problem) {
			throw new IllegalArgumentException("Item metadata cannot be safely merged: " + problem);
		}
	};

	private final ServerLevel level;
	private final Player player;
	private final BlockPos controller;
	private final Vec3 releasePosition;
	private final Deque<Harvest> pending = new ArrayDeque<>();
	private final List<ItemEntity> separate = new ArrayList<>();
	private final StackBatch batch = new StackBatch();
	private boolean closed;

	public PoolDrops(ServerLevel level, Player player, BlockPos controller, Vec3 releasePosition) {
		this.level = level;
		this.player = player;
		this.controller = controller.immutable();
		this.releasePosition = releasePosition;
	}

	public void dropResources(BlockPos pos, BlockState state) {
		if (closed) {
			throw new IllegalStateException("Pool drop collection is already closed");
		}
		if (state.isAir() || pos.equals(controller) && state.is(IPBlocks.FLUID_POOL.get())) {
			return;
		}
		Capture capture = new Capture(level, player, pos.immutable(), state);
		capture.run(() -> Block.dropResources(state, level, pos, null, player, ItemStack.EMPTY));
		List<ItemEntity> drops = capture.acceptedDrops();
		if (!drops.isEmpty()) {
			pending.addLast(new Harvest(capture.pos, state, drops));
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBlockDrops(BlockDropsEvent event) {
		Capture capture = CURRENT.get();
		if (capture == null || capture.event != null || event.isCanceled() || capture.level != event.getLevel()
				|| capture.player != event.getBreaker() || capture.state != event.getState() || !capture.pos.equals(event.getPos())) {
			return;
		}
		capture.event = event;
		capture.drops.addAll(event.getDrops());
		event.getDrops().clear();
	}

	public void completePlacement() {
		while (!pending.isEmpty()) {
			Harvest harvest = pending.removeFirst();
			if (level.getBlockState(harvest.pos) == harvest.state) {
				continue;
			}
			for (ItemEntity entity : harvest.drops) {
				if (entity.isRemoved() || entity.isAddedToLevel() || entity.getItem().isEmpty()) {
					continue;
				}
				if (entity.getClass() != ItemEntity.class || entity.hasAttachments() || entity.isPassenger() || entity.isVehicle()) {
					separate.add(entity);
					continue;
				}
				CompoundTag metadata;
				try {
					metadata = metadata(entity, level.registryAccess());
				} catch (RuntimeException exception) {
					separate.add(entity);
					continue;
				}
				batch.add(entity, metadata);
			}
		}
	}

	private static CompoundTag metadata(ItemEntity entity, HolderLookup.Provider registries) {
		TagValueOutput output = TagValueOutput.createWithContext(MERGE_REPORTER, registries);
		entity.saveWithoutId(output);
		CompoundTag data = output.buildResult();
		data.remove("UUID");
		data.remove("Pos");
		data.remove("Motion");
		data.remove("Rotation");
		data.remove("Item");
		return data;
	}

	private void release(Group group, ItemStack stack) {
		level.addFreshEntity(group.createEntity(level, level.registryAccess(), releasePosition, stack));
	}

	@Override
	public void close() {
		if (closed) {
			return;
		}
		closed = true;
		completePlacement();
		batch.drain(this::release);
		for (ItemEntity entity : separate) {
			level.addFreshEntity(entity);
		}
		separate.clear();
	}

	private record Harvest(BlockPos pos, BlockState state, List<ItemEntity> drops) {
	}

	private record Key(ItemResource item, CompoundTag metadata) {
	}

	private static final class Group {
		private final ItemEntity prototype;
		private final ItemResource item;
		private final CompoundTag metadata;
		private long amount;
		private boolean prototypeUsed;

		private Group(ItemEntity prototype, ItemResource item, CompoundTag metadata) {
			this.prototype = prototype;
			this.item = item;
			this.metadata = metadata;
		}

		private ItemEntity createEntity(ServerLevel level, HolderLookup.Provider registries, Vec3 position, ItemStack stack) {
			ItemEntity entity = prototype;
			if (prototypeUsed) {
				entity = new ItemEntity(level, position.x, position.y, position.z, stack);
				TagValueOutput output = TagValueOutput.createWithContext(MERGE_REPORTER, registries);
				output.store(metadata);
				output.store("Item", ItemStack.CODEC, stack);
				entity.load(TagValueInput.create(MERGE_REPORTER, registries, output.buildResult()));
			} else {
				prototypeUsed = true;
				entity.setItem(stack);
			}
			entity.setPos(position);
			return entity;
		}
	}

	private static final class StackBatch {
		private final Map<Key, Group> groups = new LinkedHashMap<>();

		private void add(ItemEntity entity, CompoundTag metadata) {
			ItemStack stack = entity.getItem();
			if (stack.isEmpty()) {
				return;
			}
			ItemResource item = ItemResource.of(stack);
			Key key = new Key(item, metadata);
			Group group = groups.get(key);
			if (group == null) {
				CompoundTag saved = metadata.copy();
				group = new Group(entity, item, saved);
				groups.put(new Key(item, saved), group);
			}
			group.amount = Math.addExact(group.amount, stack.getCount());
		}

		private void drain(BiConsumer<Group, ItemStack> consumer) {
			for (Group group : groups.values()) {
				int limit = Math.max(1, group.item.getMaxStackSize());
				while (group.amount > 0) {
					int count = (int) Math.min(group.amount, limit);
					consumer.accept(group, group.item.toStack(count));
					group.amount -= count;
				}
			}
			groups.clear();
		}
	}

	private static final class Capture {
		private final ServerLevel level;
		private final Player player;
		private final BlockPos pos;
		private final BlockState state;
		private final List<ItemEntity> drops = new ArrayList<>();
		private BlockDropsEvent event;

		private Capture(ServerLevel level, Player player, BlockPos pos, BlockState state) {
			this.level = level;
			this.player = player;
			this.pos = pos;
			this.state = state;
		}

		private List<ItemEntity> acceptedDrops() {
			return event == null || event.isCanceled() ? List.of() : drops;
		}

		private void run(Runnable action) {
			Capture previous = CURRENT.get();
			CURRENT.set(this);
			try {
				action.run();
			} finally {
				if (previous == null) {
					CURRENT.remove();
				} else {
					CURRENT.set(previous);
				}
			}
		}
	}
}
