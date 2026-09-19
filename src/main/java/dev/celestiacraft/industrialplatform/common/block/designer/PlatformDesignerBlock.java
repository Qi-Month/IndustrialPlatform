package dev.celestiacraft.industrialplatform.common.block.designer;

import dev.celestiacraft.industrialplatform.api.ItemMatcher;
import dev.celestiacraft.industrialplatform.common.block.IPlatformController;
import dev.celestiacraft.industrialplatform.config.CommonConfig;
import dev.celestiacraft.industrialplatform.common.menu.PlatformDesignerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

public class PlatformDesignerBlock extends Block implements IPlatformController {
	public PlatformDesignerBlock() {
		super(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_TILES));
	}

	@Override
	public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return Block.box(0, 0, 0, 16, 12, 16);
	}

	@Override
	public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
		if (!canOpen(player.getItemInHand(hand), hand)) {
			return InteractionResult.PASS;
		}

		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		if (player instanceof ServerPlayer serverPlayer) {
			PlatformDesignerMenu.open(serverPlayer, pos);
		}

		return InteractionResult.SUCCESS;
	}

	/**
	 * 空手或手持调节器都能打开设计台
	 * <p>
	 * 空手只认主手, 不然主手拿着东西时会轮到副手的空手把界面顶开
	 */
	private static boolean canOpen(ItemStack held, InteractionHand hand) {
		if (ItemMatcher.matches(held, CommonConfig.ADJUSTER)) {
			return true;
		}

		return held.isEmpty() && hand == InteractionHand.MAIN_HAND;
	}
}