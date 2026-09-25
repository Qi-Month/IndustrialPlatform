package dev.celestiacraft.industrialplatform.common.block.platform;

import dev.celestiacraft.industrialplatform.api.IPLogic;
import dev.celestiacraft.industrialplatform.api.IPTags;
import dev.celestiacraft.industrialplatform.api.ItemMatcher;
import dev.celestiacraft.industrialplatform.api.PlatformSettings;
import dev.celestiacraft.industrialplatform.common.block.IPlatformController;
import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformMode;
import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformProperties;
import dev.celestiacraft.industrialplatform.config.CommonConfig;
import dev.celestiacraft.industrialplatform.data.PlatformSettingsStorage;
import dev.celestiacraft.industrialplatform.event.PreviewPlayerTickHandler;
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

public class PlatformBlock extends Block implements SimpleWaterloggedBlock, IPlatformController {
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final EnumProperty<PlatformMode> PLATFORM_MODE = PlatformProperties.PLATFORM_MODE;

	public PlatformBlock() {
		super(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_BRICKS).noOcclusion());
		registerDefaultState(stateDefinition.any()
				.setValue(WATERLOGGED, false)
				.setValue(PLATFORM_MODE, PlatformMode.INDUSTRIAL_LIGHT));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(PLATFORM_MODE, WATERLOGGED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());

		return defaultBlockState()
				.setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER)
				.setValue(PLATFORM_MODE, PlatformMode.INDUSTRIAL_LIGHT);
	}

	@Override
	public @NotNull FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED)
				? Fluids.WATER.getSource(false)
				: super.getFluidState(state);
	}

	@Override
	public @NotNull BlockState updateShape(BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
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
	 * 右键平台方块:
	 * <ul>
	 *     <li>潜行 + 空手: 打开搭建界面</li>
	 *     <li>站立 + 手持调节器(扳手 / 填充调节器): 切换平台类型</li>
	 *     <li>手持材料: 直接展开一次平台</li>
	 * </ul>
	 */
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
		// 只认主手, 不然主手拿着东西时会轮到副手把动作顶走
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResult.PASS;
		}

		ItemStack held = player.getItemInHand(hand);

		if (player.isShiftKeyDown()) {
			// 潜行 + 空手: 打开搭建界面
			if (!held.isEmpty()) {
				// 手持调节器时潜行右键归物品自己(切换上下填充目标), 这里不接管
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

		// 站立 + 调节器: 切换平台类型(原版那套按枚举声明顺序循环)
		if (IPlatformController.isAdjuster(held)) {
			if (level.isClientSide()) {
				return InteractionResult.SUCCESS;
			}

			level.setBlock(pos, state.cycle(PLATFORM_MODE), 3);
			player.swing(InteractionHand.MAIN_HAND, true);

			return InteractionResult.SUCCESS;
		}

		// 手持材料: 直接展开
		if (!isMaterial(held)) {
			return InteractionResult.PASS;
		}

		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.PASS;
		}

		// 用这个方块自己保存的填充距离(调节器同步过来的 / 界面里设过的); 一次都没设过才回落到配置默认值,
		// 不然展开出来的层数会和预览对不上
		PlatformSettings stored = PlatformSettingsStorage.get(serverLevel).get(pos).orElse(null);
		int upFill = stored == null ? CommonConfig.TOP_FILLING_DISTANCE.get() : stored.upFill();
		int downFill = stored == null ? CommonConfig.BOTTOM_FILLING_DISTANCE.get() : stored.downFill();

		// 第一次展开时把这次真正用到的值记下来, 之后预览/界面读到的就是真建出来的这个数
		if (stored == null) {
			PlatformSettingsStorage.get(serverLevel).put(pos, PlatformSettings.defaults(state.getValue(PLATFORM_MODE)).withFill(upFill, downFill));
		}

		buildPlatform(serverLevel, pos, state.getValue(PLATFORM_MODE), upFill, downFill);
		player.displayClientMessage(Component.translatable("message.industrial_platform.done").withStyle(ChatFormatting.GREEN), true);
		IPLogic.consumeItem(player, held, hand);

		return InteractionResult.SUCCESS;
	}

	public static boolean isMaterial(ItemStack stack) {
		return ItemMatcher.matches(stack, CommonConfig.PLATFORM_MATERIAL) || stack.is(IPTags.Items.PLATFORM_MATERIAL);
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
					.setValue(PLATFORM_MODE, mode), 3);
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
	public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
		// 控制器被结构覆盖/挖掉时, 顺手清掉它保存的搭建设置
		if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel) {
			PlatformSettingsStorage.get(serverLevel).remove(pos);
			PreviewPlayerTickHandler.onControllerRemoved(serverLevel, pos);
		}

		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public boolean isPathfindable(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull PathComputationType type) {
		return false;
	}
}