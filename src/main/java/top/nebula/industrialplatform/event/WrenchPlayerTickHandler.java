package top.nebula.industrialplatform.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.nebula.industrialplatform.IndustrialPlatform;
import top.nebula.industrialplatform.utils.IPTags;

@Mod.EventBusSubscriber(modid = IndustrialPlatform.MODID)
public class WrenchPlayerTickHandler {

	private static boolean isAdjuster(ItemStack stack) {
		return stack.is(IPTags.Items.WRENCH) || stack.is(Items.STICK);
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) {
			return;
		}

		Player player = event.player;
		Level level = player.level();

		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}
		if (!isAdjuster(player.getMainHandItem())) {
			return;
		}
		if (player.tickCount % 5 != 0) {
			return;
		}

		BlockPos center = player.blockPosition();

		for (BlockPos pos : BlockPos.betweenClosed(
				center.offset(-3, -3, -3),
				center.offset(3, 3, 3)
		)) {
			BlockState state = level.getBlockState(pos);
			Block block = state.getBlock();

			if (block instanceof IWrenchReactive reactive) {
				reactive.onWrenchHover(serverLevel, pos, state);
			}
		}
	}
}
