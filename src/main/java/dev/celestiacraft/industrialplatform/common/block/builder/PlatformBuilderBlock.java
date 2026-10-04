package dev.celestiacraft.industrialplatform.common.block.builder;

import dev.celestiacraft.industrialplatform.common.block.IPlatformController;
import dev.celestiacraft.industrialplatform.common.menu.PlatformBuilderMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class PlatformBuilderBlock extends Block implements IPlatformController {
	public PlatformBuilderBlock() {
		super(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_TILES));
	}

	/**
	 * 站立右键直接打开建造界面, 和原版工作台一个路子, 只看方块本身不挑手里的东西
	 */
	@Override
	protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
		return openBuilder(level, pos, player);
	}

	@Override
	protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack held, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
		openBuilder(level, pos, player);
		return ItemInteractionResult.SUCCESS;
	}

	private static InteractionResult openBuilder(Level level, BlockPos pos, Player player) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		if (player instanceof ServerPlayer serverPlayer) {
			PlatformBuilderMenu.open(serverPlayer, pos);
		}

		return InteractionResult.SUCCESS;
	}
}