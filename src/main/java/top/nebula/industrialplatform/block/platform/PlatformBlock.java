package top.nebula.industrialplatform.block.platform;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import top.nebula.industrialplatform.config.CommonConfig;
import top.nebula.industrialplatform.IndustrialPlatform;
import top.nebula.industrialplatform.block.state.properties.platform.PlatformMode;
import top.nebula.industrialplatform.block.state.properties.platform.PlatformProperties;
import top.nebula.industrialplatform.utils.ItemMatcher;

import static top.nebula.industrialplatform.utils.IPLogic.*;

@SuppressWarnings("ALL")
@Mod.EventBusSubscriber(modid = IndustrialPlatform.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlatformBlock extends Block {
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final EnumProperty PLATFORM_MODE = PlatformProperties.PLATFORM_MODE;
	public static final BooleanProperty FLOATING = PlatformProperties.FLOATING;
	public static final BooleanProperty DISPLAYPREVIEW = PlatformProperties.DISPLAY_PREVIEW;

	public PlatformBlock() {
		super(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_BRICKS).noOcclusion());
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(WATERLOGGED, false)
				.setValue(FLOATING, false)
				.setValue(DISPLAYPREVIEW, false)
				.setValue(PLATFORM_MODE, PlatformMode.INDUSTRIAL_LIGHT));
	}

	@Override
	public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(PLATFORM_MODE);
		builder.add(FLOATING);
		builder.add(DISPLAYPREVIEW);
		builder.add(WATERLOGGED);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
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

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		Level level = event.getLevel();
		BlockPos blockPos = event.getPos();
		Player player = event.getEntity();
		InteractionHand hand = event.getHand();
		ItemStack item = player.getItemInHand(hand);
		BlockState state = level.getBlockState(blockPos);

		if (level.isClientSide() || !(state.getBlock() instanceof PlatformBlock)) {
			return;
		}

		// 判断是否为调整器或放置物品
		boolean isStickAndWench = ItemMatcher.matches(item, CommonConfig.ADJUSTER);
		boolean isStone = ItemMatcher.matches(item, CommonConfig.TRIGGER_BLOCK);

		ServerLevel serverLevel = (ServerLevel) level;

		if (isStickAndWench && hand == InteractionHand.MAIN_HAND && player.isCrouching()) {
			// 调整器蹲下：切换 FLOATING
			serverLevel.setBlock(blockPos, state.cycle(FLOATING), 3);
			player.swing(InteractionHand.MAIN_HAND, true);
		} else if (isStickAndWench && hand == InteractionHand.MAIN_HAND) {
			// 调整器右键：切换 PLATFORM_MODE
			serverLevel.setBlock(blockPos, state.cycle(PLATFORM_MODE), 3);
			player.swing(InteractionHand.MAIN_HAND, true);
		} else if (isStone && hand == InteractionHand.MAIN_HAND) {
			// 石头右键：生成结构
			int posX = blockPos.getX();
			int posY = blockPos.getY();
			int posZ = blockPos.getZ();
			int finX = (int) Math.floor(posX / 16.0) * 16;
			int finZ = (int) Math.floor(posZ / 16.0) * 16;
			int topFilling = CommonConfig.TOP_FILLING_DISTANCE.get();
			int bottomFilling = CommonConfig.BOTTOM_FILLING_DISTANCE.get();


			if (state.getValue(FLOATING)) {
				if (state.getValue(PLATFORM_MODE) == PlatformMode.INDUSTRIAL_LIGHT) {
					placeStructure(serverLevel, finX, posY, finZ, "industrial");
				} else if (state.getValue(PLATFORM_MODE) == PlatformMode.INDUSTRIAL_HEAVY) {
					placeExtendedStructure(serverLevel, finX, posY, finZ, "industrial");
				} else if (state.getValue(PLATFORM_MODE) == PlatformMode.CHECKERBOARD_LIGHT) {
					placeStructure(serverLevel, finX, posY, finZ, "checkerboard");
				} else if (state.getValue(PLATFORM_MODE) == PlatformMode.CHECKERBOARD_HEAVY) {
					placeExtendedStructure(serverLevel, finX, posY, finZ, "checkerboard");
				}
			} else {
				if (state.getValue(PLATFORM_MODE) == PlatformMode.INDUSTRIAL_LIGHT) {
					fillArea(serverLevel, finX, posY + 1, finZ, finX + 15, posY + topFilling, finZ + 15);
					fillAreaConditional(serverLevel, finX, posY - bottomFilling, finZ, finX + 15, posY - 1, finZ + 15);
					placeStructure(serverLevel, finX, posY, finZ, "industrial");
				} else if (state.getValue(PLATFORM_MODE) == PlatformMode.INDUSTRIAL_HEAVY) {
					fillArea(serverLevel, finX - 16, posY + 1, finZ - 16, finX + 31, posY + topFilling, finZ + 31);
					fillAreaConditional(serverLevel, finX - 16, posY - bottomFilling, finZ - 16, finX + 31, posY - 1, finZ + 31);
					placeExtendedStructure(serverLevel, finX, posY, finZ, "industrial");
				} else if (state.getValue(PLATFORM_MODE) == PlatformMode.CHECKERBOARD_LIGHT) {
					fillArea(serverLevel, finX, posY + 1, finZ, finX + 15, posY + topFilling, finZ + 15);
					fillAreaConditional(serverLevel, finX, posY - bottomFilling, finZ, finX + 15, posY - 1, finZ + 15);
					placeStructure(serverLevel, finX, posY, finZ, "checkerboard");
				} else if (state.getValue(PLATFORM_MODE) == PlatformMode.CHECKERBOARD_HEAVY) {
					fillArea(serverLevel, finX - 16, posY + 1, finZ - 16, finX + 31, posY + topFilling, finZ + 31);
					fillAreaConditional(serverLevel, finX - 16, posY - bottomFilling, finZ - 16, finX + 31, posY - 1, finZ + 31);
					placeExtendedStructure(serverLevel, finX, posY, finZ, "checkerboard");
				}
			}

			MutableComponent successfulKey = Component.translatable("message.industrial_platform.done")
					.withStyle(ChatFormatting.GREEN);
			player.displayClientMessage(successfulKey, true);
			consumeItem(player, item, hand);
			event.setCanceled(true);
		}

		event.setCancellationResult(InteractionResult.SUCCESS);
	}

	public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos blockPos, PathComputationType type) {
		return false;
	}
}