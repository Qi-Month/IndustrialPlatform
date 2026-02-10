package top.nebula.industrialplatform.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.nebula.industrialplatform.IndustrialPlatform;
import top.nebula.industrialplatform.block.platform.PlatformBlock;
import top.nebula.industrialplatform.block.pool.FluidPoolBlock;
import top.nebula.industrialplatform.block.state.properties.platform.PlatformMode;
import top.nebula.industrialplatform.block.state.properties.platform.PlatformProperties;
import top.nebula.industrialplatform.utils.IPTags;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = IndustrialPlatform.MODID, value = Dist.CLIENT)
public class ClientWrenchHandler {
	private static boolean isAdjuster(ItemStack stack) {
		return stack.is(IPTags.Items.WRENCH) || stack.is(Items.STICK);
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		Level level = mc.level;

		if (player == null || level == null) {
			return;
		}

		long tick = level.getGameTime();

		if (!isAdjuster(player.getMainHandItem())) {
			if (!BoundaryRenderData.getEntries().isEmpty()) {
				BoundaryRenderData.clear();
			}
			return;
		}

		if (tick % 5 != 0) {
			if (BoundaryRenderData.isExpired(tick)) {
				BoundaryRenderData.clear();
			}
			return;
		}

		BlockPos center = player.blockPosition();
		List<BoundaryRenderData.BoundaryEntry> newEntries = new ArrayList<>();

		for (BlockPos pos : BlockPos.betweenClosed(
				center.offset(-3, -3, -3),
				center.offset(3, 3, 3)
		)) {
			BlockState state = level.getBlockState(pos);
			Block block = state.getBlock();

			if (block instanceof PlatformBlock) {
				boolean floating = state.getValue(PlatformProperties.FLOATING);
				PlatformMode mode = state.getValue(PlatformProperties.PLATFORM_MODE);
				boolean extended = mode == PlatformMode.INDUSTRIAL_HEAVY || mode == PlatformMode.CHECKERBOARD_HEAVY;
				newEntries.add(new BoundaryRenderData.BoundaryEntry(pos.immutable(), extended, floating));
			} else if (block instanceof FluidPoolBlock) {
				newEntries.add(new BoundaryRenderData.BoundaryEntry(pos.immutable(), false, true));
			}
		}

		BoundaryRenderData.update(newEntries, tick);
	}
}