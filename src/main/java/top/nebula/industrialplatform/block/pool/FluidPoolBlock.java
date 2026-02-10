package top.nebula.industrialplatform.block.pool;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.nebula.industrialplatform.IndustrialPlatform;
import top.nebula.industrialplatform.config.CommonConfig;
import top.nebula.industrialplatform.utils.ItemMatcher;

import static top.nebula.industrialplatform.utils.IPLogic.*;

@SuppressWarnings("ALL")
@Mod.EventBusSubscriber(modid = IndustrialPlatform.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FluidPoolBlock extends Block {
	public FluidPoolBlock() {
		super(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_BRICKS).noOcclusion());
		this.registerDefaultState(this.stateDefinition.any());
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		Level level = event.getLevel();
		BlockPos blockPos = event.getPos();
		Player player = event.getEntity();
		InteractionHand hand = event.getHand();
		ItemStack item = player.getItemInHand(hand);
		BlockState state = level.getBlockState(blockPos);

		if (level.isClientSide() || !(state.getBlock() instanceof FluidPoolBlock)) {
			return;
		}

		// 判断是否为石头
		boolean isStone = ItemMatcher.matches(item, CommonConfig.TRIGGER_BLOCK);

		ServerLevel serverLevel = (ServerLevel) level;

		if (isStone && hand == InteractionHand.MAIN_HAND) {
			// 石头右键：生成结构
			int posX = blockPos.getX();
			int posY = blockPos.getY();
			int posZ = blockPos.getZ();
			int finX = (int) Math.floor(posX / 16.0) * 16;
			int finZ = (int) Math.floor(posZ / 16.0) * 16;

			if (posY <= 0) {
				MutableComponent failKey = Component.translatable("message.industrial_platform.too_low")
						.withStyle(ChatFormatting.RED);
				player.displayClientMessage(failKey, true);
				return;
			}
			placeStructure(serverLevel, finX, posY - 31, finZ, "pool_top");
			placeStructure(serverLevel, finX, posY - 63, finZ, "pool_bottom");

			MutableComponent successfulKey = Component.translatable("message.industrial_platform.pool_done")
					.withStyle(ChatFormatting.GREEN);
			player.displayClientMessage(successfulKey, true);

			consumeItem(player, item, hand);
			event.setCanceled(true);
		}
	}

	public FluidPoolBlock(Properties properties) {
		super(properties);
	}
}