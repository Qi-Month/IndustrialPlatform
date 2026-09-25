package dev.celestiacraft.industrialplatform.event;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.common.block.platform.PlatformBlock;
import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * 搭建界面的打开条件是"潜行 + 空手 + 右键"。
 * <p>
 * 但原版潜行时只要还有一只手拿着东西(副手盾牌、火把之类), 就会直接跳过方块交互
 * ({@code doesSneakBypassUse} 默认 false), 方块自己的 use 根本不会被调用, 界面就永远打不开。
 * 所以这里在"潜行 + 主手空着"时把方块交互放行, 让 {@code PlatformBlock#useWithoutItem} 有机会跑;
 * 最终开不开仍然由方块自己那套条件判断, 这里只是别让原版把交互提前吃掉。
 * <p>
 * 只针对平台方块: 建造站走原版那套"站立右键直接打开", 不需要放行。
 * <p>
 * NeoForge 起事件结果改用 {@link TriState}: {@code TRUE} 即原来的 {@code Event.Result.ALLOW}。
 */
@EventBusSubscriber(modid = IndustrialPlatform.MODID)
public class BuilderUiInteractHandler {
	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		// 只放行主手那一次, 副手再走一遍没意义
		if (event.getHand() != InteractionHand.MAIN_HAND) {
			return;
		}

		if (!event.getEntity().isShiftKeyDown() || !event.getEntity().getMainHandItem().isEmpty()) {
			return;
		}

		if (!(event.getLevel().getBlockState(event.getPos()).getBlock() instanceof PlatformBlock)) {
			return;
		}

		event.setUseBlock(TriState.TRUE);
	}
}
