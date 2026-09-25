package dev.celestiacraft.industrialplatform.common.block.builder;

import dev.celestiacraft.industrialplatform.common.block.IPlatformController;
import dev.celestiacraft.industrialplatform.common.menu.PlatformBuilderMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class PlatformBuilderBlock extends Block implements IPlatformController {
	public PlatformBuilderBlock() {
		super(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_TILES));
	}

	@Override
	public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return Block.box(0, 0, 0, 16, 12, 16);
	}

	/**
	 * 站立右键直接打开设计界面, 和原版工作台一个路子, 只看方块本身不挑手里的东西
	 */
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		if (player instanceof ServerPlayer serverPlayer) {
			PlatformBuilderMenu.open(serverPlayer, pos);
		}

		return InteractionResult.SUCCESS;
	}
}