package dev.celestiacraft.industrialplatform.api;

import com.mojang.serialization.MapCodec;
import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.common.register.IPBlocks;
import dev.celestiacraft.industrialplatform.platform.PoolDrops;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.NopProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class IPLogic {
	public static final BlockState FILL_BLOCK = Blocks.STONE.defaultBlockState();

	public static void appendShiftTooltip(TooltipFlag flag, Consumer<Component> tooltip, String expandedKey, String collapsedKey) {
		if (!flag.hasShiftDown()) {
			tooltip.accept(Component.translatable(collapsedKey));
			return;
		}
		String translated = Component.translatable(expandedKey).getString();
		for (String line : translated.split("\n")) {
			tooltip.accept(Component.literal(line));
		}
	}

	/**
	 * 结构文件是否存在, 用于在破坏地形之前做检查
	 */
	public static boolean hasStructure(ServerLevel level, String structureId) {
		return getStructure(level, structureId).isPresent();
	}

	public static boolean placeStructure(ServerLevel level, int x, int y, int z, String structureId) {
		return getStructure(level, structureId).map(template -> placeStructure(level, x, y, z, template)).orElse(false);
	}

	/**
	 * 放一份已经读好的结构, 免得同一个结构被反复从数据包资源里读
	 */
	public static boolean placeStructure(ServerLevel level, int x, int y, int z, StructureTemplate template) {
		return template.placeInWorld(
				level,
				new BlockPos(x, y, z),
				new BlockPos(x, y, z),
				createSafePlaceSettings(level),
				level.getRandom(),
				3
		);
	}

	/**
	 * 读取 data/&lt;命名空间&gt;/structures/&lt;id&gt;.nbt
	 * <p>
	 * 数据包放同名文件就能覆盖内置的平台外观
	 */
	public static Optional<StructureTemplate> getStructure(ServerLevel level, String structureId) {
		StructureTemplateManager manager = level.getStructureTemplateManager();
		Identifier structureName = IndustrialPlatform.loadResource(structureId);
		return manager.get(structureName);
	}

	private static StructurePlaceSettings createSafePlaceSettings(ServerLevel level) {
		return new StructurePlaceSettings()
				.setRotation(Rotation.NONE)
				.setMirror(Mirror.NONE)
				.setIgnoreEntities(false)
				.addProcessor(new DropBeforePlaceProcessor(level));
	}

	/**
	 * 向上清理: 不管原来是什么(草/树/水/石头), 一律顶成空气, 不可破坏的方块跳过
	 */
	public static void fillArea(ServerLevel level, int x0, int y0, int z0, int x1, int y1, int z1) {
		for (int x = x0; x <= x1; x++) {
			for (int y = y0; y <= y1; y++) {
				for (int z = z0; z <= z1; z++) {
					destroyIfBreakable(level, new BlockPos(x, y, z));
				}
			}
		}
	}

	public static void fillAreaConditional(ServerLevel level, int x0, int y0, int z0, int x1, int y1, int z1) {
		for (int x = x0; x <= x1; x++) {
			for (int y = y0; y <= y1; y++) {
				for (int z = z0; z <= z1; z++) {
					BlockPos pos = new BlockPos(x, y, z);
					fillStone(level, pos);
				}
			}
		}
	}

	public static void consumeItem(Player player, ItemStack stack, InteractionHand hand) {
		player.swing(hand, SwingAnimation.DEFAULT, false);

		if (!player.isCreative()) {
			stack.shrink(1);
		}
	}

	/**
	 * 把生成出来的方块放下去: 不可破坏的方块跳过, 其余按规则替换
	 *
	 * @return 是否真的放上去了
	 */
	public static boolean placeGenerated(ServerLevel level, BlockPos pos, BlockState state) {
		BlockState existing = level.getBlockState(pos);

		if (isUnbreakable(level, pos, existing)) {
			return false;
		}

		if (existing.equals(state)) {
			return true;
		}

		if (!existing.isAir()) {
			level.destroyBlock(pos, shouldDrop(existing));
		}

		return level.setBlock(pos, state, 3);
	}

	private static boolean isUnbreakable(ServerLevel level, BlockPos pos, BlockState state) {
		return state.getDestroySpeed(level, pos) < 0.0F;
	}

	private static boolean destroyIfBreakable(ServerLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.isAir() || isUnbreakable(level, pos, state)) {
			return false;
		}

		level.destroyBlock(pos, shouldDrop(state));
		return true;
	}

	/**
	 * 向下填充: 不管原来是什么, 一律顶成垫底方块(不可破坏的方块跳过)
	 */
	private static boolean fillStone(ServerLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (isUnbreakable(level, pos, state) || state.is(FILL_BLOCK.getBlock())) {
			return false;
		}

		level.setBlockAndUpdate(pos, FILL_BLOCK);
		return true;
	}

	private static boolean clearForTemplateAir(ServerLevel level, BlockPos pos, BlockState state) {
		if (state.isAir() || isUnbreakable(level, pos, state)) {
			return false;
		}

		level.destroyBlock(pos, shouldDrop(state));
		return true;
	}

	/**
	 * 被顶掉的方块掉不掉落, 默认不掉(配置里可开)
	 */
	private static boolean shouldDrop(BlockState state) {
		return !state.is(IPBlocks.INDUSTRIAL_PLATFORM.get())
				&& !state.is(IPBlocks.FLUID_POOL.get())
				&& !state.is(IPTags.Blocks.NO_DROP_BLOCKS);
	}

	public static boolean fitsBuildHeight(LevelHeightAccessor level, int originY, int height) {
		return height > 0 && originY >= level.getMinY() && (long) originY + height - 1 <= level.getMaxY();
	}

	public static boolean canReplaceForPool(BlockGetter level, BlockPos pos, BlockState existing, BlockState replacement) {
		return !level.isOutsideBuildHeight(pos) && !existing.hasBlockEntity()
				&& existing != replacement && !(existing.isAir() && replacement.isAir())
				&& existing.getDestroySpeed(level, pos) >= 0.0F;
	}

	public static boolean placePoolStructure(ServerLevel level, BlockPos origin, StructureTemplate template, PoolDrops drops) {
		StructurePlaceSettings settings = new StructurePlaceSettings()
				.setIgnoreEntities(true)
				.addProcessor(new PoolPlaceProcessor(level, drops));
		try {
			return template.placeInWorld(level, origin, origin, settings, level.getRandom(), Block.UPDATE_ALL);
		} finally {
			drops.completePlacement();
		}
	}

	private static class PoolPlaceProcessor implements StructureProcessor {
		private final ServerLevel level;
		private final PoolDrops drops;

		private PoolPlaceProcessor(ServerLevel level, PoolDrops drops) {
			this.level = level;
			this.drops = drops;
		}

		@Override
		public StructureTemplate.StructureBlockInfo process(LevelReader reader, BlockPos offset, BlockPos pos,
				StructureTemplate.StructureBlockInfo original, StructureTemplate.StructureBlockInfo current,
				StructurePlaceSettings settings, @Nullable StructureTemplate template) {
			BlockPos target = current.pos();
			BlockState existing = level.getBlockState(target);
			if (!canReplaceForPool(level, target, existing, current.state())) {
				return null;
			}
			drops.dropResources(target, existing);
			return current;
		}

		@Override
		public MapCodec<? extends StructureProcessor> codec() {
			return NopProcessor.MAP_CODEC;
		}
	}

	private static class DropBeforePlaceProcessor implements StructureProcessor {
		private final ServerLevel level;

		private DropBeforePlaceProcessor(ServerLevel level) {
			this.level = level;
		}

		/**
		 * 1.21 的 NeoForge 把 processBlock 换成了多一个 template 参数的 process
		 */
		@Override
		public StructureTemplate.StructureBlockInfo process(
				@NotNull LevelReader levelReader,
				@NotNull BlockPos offset,
				@NotNull BlockPos pos,
				StructureTemplate.@NotNull StructureBlockInfo originalBlockInfo,
				StructureTemplate.StructureBlockInfo currentBlockInfo,
				@NotNull StructurePlaceSettings settings,
				@Nullable StructureTemplate template
		) {
			BlockPos targetPos = currentBlockInfo.pos();
			BlockState targetState = level.getBlockState(targetPos);

			if (isUnbreakable(level, targetPos, targetState)) {
				return null;
			}

			if (currentBlockInfo.state().isAir()) {
				clearForTemplateAir(level, targetPos, targetState);
				return currentBlockInfo;
			}

			if (!targetState.isAir()) {
				level.destroyBlock(targetPos, shouldDrop(targetState));
			}

			return currentBlockInfo;
		}

		@Override
		public @NotNull MapCodec<? extends StructureProcessor> codec() {
			return NopProcessor.MAP_CODEC;
		}
	}
}