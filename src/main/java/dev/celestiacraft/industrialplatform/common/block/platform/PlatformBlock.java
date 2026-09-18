package dev.celestiacraft.industrialplatform.common.block.platform;

import dev.celestiacraft.industrialplatform.api.IPLogic;
import dev.celestiacraft.industrialplatform.api.IPTags;
import dev.celestiacraft.industrialplatform.api.ItemMatcher;
import dev.celestiacraft.industrialplatform.common.block.IPlatformController;
import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformMode;
import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformProperties;
import dev.celestiacraft.industrialplatform.config.CommonConfig;
import dev.celestiacraft.industrialplatform.data.PlatformSettingsStorage;
import dev.celestiacraft.industrialplatform.common.item.FillAdjusterItem;
import dev.celestiacraft.industrialplatform.platform.PlatformGenerator;
import dev.celestiacraft.industrialplatform.platform.PlatformLayout;
import dev.celestiacraft.industrialplatform.platform.PlatformPalette;
import dev.celestiacraft.industrialplatform.platform.PlatformStyle;
import dev.celestiacraft.industrialplatform.platform.blueprint.PlatformBlueprint;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ALL")
public class PlatformBlock extends Block implements SimpleWaterloggedBlock, IPlatformController {
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final EnumProperty<PlatformMode> PLATFORM_MODE = PlatformProperties.PLATFORM_MODE;
	public static final BooleanProperty FLOATING = PlatformProperties.FLOATING;

	public PlatformBlock() {
		super(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_BRICKS).noOcclusion());
		registerDefaultState(stateDefinition.any()
				.setValue(WATERLOGGED, false)
				.setValue(FLOATING, false)
				.setValue(PLATFORM_MODE, PlatformMode.INDUSTRIAL_LIGHT));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(PLATFORM_MODE, FLOATING, WATERLOGGED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());

		return defaultBlockState()
				.setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER)
				.setValue(FLOATING, false)
				.setValue(PLATFORM_MODE, PlatformMode.INDUSTRIAL_LIGHT);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED)
				? Fluids.WATER.getSource(false)
				: super.getFluidState(state);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
		if (state.getValue(WATERLOGGED)) {
			level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		}
		return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
	}

	@Override
	public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return Block.box(0, 0, 0, 16, 12, 16);
	}

	/**
	 * 空手或手持调节器右键打开搭建界面, 不再直接展开平台
	 */
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
		ItemStack held = player.getItemInHand(hand);

		// 界面开着就打开搭建界面, 关着就是以前那套玩法
		if (CommonConfig.ENABLE_BUILDER_UI.get()) {
			if (!canOpenBuilder(held, hand)) {
				return InteractionResult.PASS;
			}

			if (level.isClientSide()) {
				return InteractionResult.SUCCESS;
			}

			if (player instanceof ServerPlayer serverPlayer) {
				IPlatformController.openBuilder(serverPlayer, pos);
			}

			return InteractionResult.SUCCESS;
		}

		if (!handlesInteraction(held)) {
			return InteractionResult.PASS;
		}

		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		return classicUse(state, level, pos, player, hand, held);
	}

	/**
	 * 空手或手持调节器都能打开搭建界面
	 * <p>
	 * 空手只认主手, 不然主手拿着东西时会轮到副手的空手把界面顶开
	 */
	private static boolean canOpenBuilder(ItemStack held, InteractionHand hand) {
		if (ItemMatcher.matches(held, CommonConfig.ADJUSTER)) {
			return true;
		}

		return held.isEmpty() && hand == InteractionHand.MAIN_HAND;
	}

	/**
	 * 老玩法里, 手里拿的东西会不会被这个交互吃掉(客户端也要用同一套判断, 不然两边动作对不上)
	 */
	private static boolean handlesInteraction(ItemStack held) {
		if (ItemMatcher.matches(held, CommonConfig.ADJUSTER)) {
			return true;
		}

		return held.getItem() instanceof FillAdjusterItem || isMaterial(held);
	}

	public static boolean isMaterial(ItemStack stack) {
		return ItemMatcher.matches(stack, CommonConfig.PLATFORM_MATERIAL) || stack.is(IPTags.Items.PLATFORM_MATERIAL);
	}

	/**
	 * 界面关掉时的老玩法: 调节器切模式/悬浮, 材料或填充调节器右键直接展开
	 */
	private static InteractionResult classicUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack held) {
		if (ItemMatcher.matches(held, CommonConfig.ADJUSTER)) {
			if (hand != InteractionHand.MAIN_HAND) {
				return InteractionResult.PASS;
			}

			// 站着切平台类型, 潜行切悬浮
			level.setBlock(pos, player.isCrouching() ? state.cycle(FLOATING) : state.cycle(PLATFORM_MODE), 3);
			player.swing(InteractionHand.MAIN_HAND, true);
			return InteractionResult.SUCCESS;
		}

		int upFill;
		int downFill;
		boolean consume = false;

		if (held.getItem() instanceof FillAdjusterItem) {
			upFill = FillAdjusterItem.getUpFill(held);
			downFill = FillAdjusterItem.getDownFill(held);
		} else if (hand == InteractionHand.MAIN_HAND && isMaterial(held)) {
			upFill = CommonConfig.TOP_FILLING_DISTANCE.get();
			downFill = CommonConfig.BOTTOM_FILLING_DISTANCE.get();
			consume = true;
		} else {
			return InteractionResult.PASS;
		}

		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.SUCCESS;
		}

		PlatformMode mode = state.getValue(PLATFORM_MODE);
		buildPlatform(serverLevel, pos, mode, upFill, downFill);

		player.displayClientMessage(Component.translatable("message.industrial_platform.done").withStyle(ChatFormatting.GREEN), true);

		if (consume) {
			IPLogic.consumeItem(player, held, hand);
		}

		return InteractionResult.SUCCESS;
	}

	/**
	 * 老方块(内置款式)的入口: 直接放 data/industrial_platform/structures 里的结构文件,
	 * 数据包放同名文件就能覆盖外观; 结构文件缺失时回退到 {@link PlatformGenerator} 程序化生成
	 *
	 * @param upFill   向上清理的格数, 0 表示保留上方方块
	 * @param downFill 向下填充垫底方块的格数
	 */
	public static boolean buildPlatform(ServerLevel level, BlockPos controllerPos, PlatformMode mode, int upFill, int downFill) {
		StructureTemplate template = IPLogic.getStructure(level, mode.structureId()).orElse(null);
		PlatformStyle style = PlatformStyle.of(mode);

		if (template == null) {
			int chunks = mode.chunkSize();
			return buildPlatform(level, controllerPos, style, PlatformLayout.of(chunks, chunks), PlatformPalette.defaults(style), upFill, downFill);
		}

		int width = template.getSize().getX();
		int depth = template.getSize().getZ();
		int originX = originOf(controllerPos.getX(), width);
		int originZ = originOf(controllerPos.getZ(), depth);
		int originY = controllerPos.getY();

		int up = getUpFill(upFill);
		int down = getDownFill(downFill);

		if (up > 0) {
			int clearFrom = originY + template.getSize().getY();
			IPLogic.fillArea(level, originX, clearFrom, originZ, originX + width - 1, clearFrom + up - 1, originZ + depth - 1);
		}

		if (down > 0) {
			IPLogic.fillAreaConditional(level, originX, originY - down, originZ, originX + width - 1, originY - 1, originZ + depth - 1);
		}

		IPLogic.placeStructure(level, originX, originY, originZ, template);

		// 控制器方块还有可能留在原地, 把这次搭建的模式与悬浮状态写回去
		BlockState state = level.getBlockState(controllerPos);
		if (state.getBlock() instanceof PlatformBlock) {
			level.setBlock(controllerPos, state
					.setValue(PLATFORM_MODE, mode)
					.setValue(FLOATING, up == 0 && down == 0), 3);
		}

		return true;
	}

	/**
	 * 控制器所在区块尽量落在平台中间: 向上取整的区块数决定往左/北偏几个区块
	 * <p>
	 * 16 格 => 不偏, 48 格 => 偏一个区块, 和老结构 NBT 年代的摆法一致
	 */
	private static int originOf(int coordinate, int size) {
		int chunkSize = PlatformLayout.BLOCKS_PER_CHUNK;
		int chunks = Math.max(1, (size + chunkSize - 1) / chunkSize);

		return Math.floorDiv(coordinate, chunkSize) * chunkSize - ((chunks - 1) / 2) * chunkSize;
	}

	/**
	 * 按 样式 + 尺寸 + 调色板 铺平台, 底板由 {@link PlatformGenerator} 算出来, 不再读结构文件
	 *
	 * @param upFill   向上清理的格数, 0 表示保留上方方块
	 * @param downFill 向下填充垫底方块的格数
	 */
	public static boolean buildPlatform(ServerLevel level, BlockPos controllerPos, PlatformStyle style, PlatformLayout layout, PlatformPalette palette, int upFill, int downFill) {
		int chunks = PlatformLayout.BLOCKS_PER_CHUNK;
		int chunkX = Math.floorDiv(controllerPos.getX(), chunks) * chunks;
		int chunkZ = Math.floorDiv(controllerPos.getZ(), chunks) * chunks;

		// 控制器所在区块尽量落在平台中间: 1x1 与 3x3 的结果和改造前完全一致
		int originX = chunkX - ((layout.chunksX() - 1) / 2) * chunks;
		int originZ = chunkZ - ((layout.chunksZ() - 1) / 2) * chunks;
		int originY = controllerPos.getY();

		int width = layout.width();
		int depth = layout.depth();
		int up = getUpFill(upFill);
		int down = getDownFill(downFill);

		if (up > 0) {
			IPLogic.fillArea(level, originX, originY + 1, originZ, originX + width - 1, originY + up, originZ + depth - 1);
		}
		if (down > 0) {
			IPLogic.fillAreaConditional(level, originX, originY - down, originZ, originX + width - 1, originY - 1, originZ + depth - 1);
		}

		PlatformGenerator.forEachDeck(style, palette, layout, (localX, localZ, state) ->
				IPLogic.placeGenerated(level, new BlockPos(originX + localX, originY, originZ + localZ), state));

		BlockState state = level.getBlockState(controllerPos);
		if (state.getBlock() instanceof PlatformBlock) {
			level.setBlock(controllerPos, state.setValue(FLOATING, up == 0 && down == 0), 3);
		}

		return true;
	}

	/**
	 * 照着蓝图铺: 底板/层数/方块全部来自蓝图 NBT
	 *
	 * @param upFill   蓝图上方再清理多少格
	 * @param downFill 蓝图下方垫多少格
	 */
	public static boolean buildBlueprint(ServerLevel level, BlockPos controllerPos, PlatformBlueprint blueprint, int upFill, int downFill) {
		int chunkSize = PlatformLayout.BLOCKS_PER_CHUNK;
		int chunkX = Math.floorDiv(controllerPos.getX(), chunkSize) * chunkSize;
		int chunkZ = Math.floorDiv(controllerPos.getZ(), chunkSize) * chunkSize;

		int sizeX = Math.max(1, blueprint.sizeX());
		int sizeY = Math.max(1, blueprint.sizeY());
		int sizeZ = Math.max(1, blueprint.sizeZ());

		int originX = chunkX - ((PlatformLayout.chunksOf(sizeX) - 1) / 2) * chunkSize;
		int originZ = chunkZ - ((PlatformLayout.chunksOf(sizeZ) - 1) / 2) * chunkSize;
		int originY = controllerPos.getY();

		int up = getUpFill(upFill);
		int down = getDownFill(downFill);

		if (up > 0) {
			int clearFrom = originY + sizeY;
			IPLogic.fillArea(level, originX, clearFrom, originZ, originX + sizeX - 1, clearFrom + up - 1, originZ + sizeZ - 1);
		}

		if (down > 0) {
			IPLogic.fillAreaConditional(level, originX, originY - down, originZ, originX + sizeX - 1, originY - 1, originZ + sizeZ - 1);
		}

		for (PlatformBlueprint.PlacedBlock placed : blueprint.getBlocks()) {
			BlockPos target = new BlockPos(
					originX + placed.pos().getX(),
					originY + placed.pos().getY(),
					originZ + placed.pos().getZ()
			);

			IPLogic.placeGenerated(level, target, placed.state());
		}

		return true;
	}

	private static int getUpFill(int upFill) {
		return Mth.clamp(upFill, PlatformProperties.MIN_FILL_DISTANCE, PlatformProperties.MAX_FILL_DISTANCE);
	}

	private static int getDownFill(int downFill) {
		return Mth.clamp(downFill, PlatformProperties.MIN_FILL_DISTANCE, PlatformProperties.MAX_FILL_DISTANCE);
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		// 控制器被结构覆盖/挖掉时, 顺手清掉它保存的搭建设置
		if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel) {
			PlatformSettingsStorage.get(serverLevel).remove(pos);
		}

		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
		return false;
	}
}