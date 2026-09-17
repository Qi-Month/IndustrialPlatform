package dev.celestiacraft.industrialplatform.event;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.IPreviewReactive;
import dev.celestiacraft.industrialplatform.api.ItemMatcher;
import dev.celestiacraft.industrialplatform.api.PlatformSettings;
import dev.celestiacraft.industrialplatform.block.IPlatformController;
import dev.celestiacraft.industrialplatform.config.CommonConfig;
import dev.celestiacraft.industrialplatform.data.PlatformSettingsStorage;
import dev.celestiacraft.industrialplatform.network.IPNetwork;
import dev.celestiacraft.industrialplatform.network.packet.PlatformSettingsSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = IndustrialPlatform.MODID)
public class PreviewPlayerTickHandler {
	// 每个玩家已经同步过的搭建设置, 只在变化时才发包
	private static final Map<UUID, Map<BlockPos, PlatformSettings>> SYNCED = new ConcurrentHashMap<>();

	private static boolean isPreviewTrigger(ItemStack stack) {
		return ItemMatcher.matches(stack, CommonConfig.ADJUSTER);
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
		if (!isPreviewTrigger(player.getMainHandItem()) && !isPreviewTrigger(player.getOffhandItem())) {
			return;
		}
		if (player.tickCount % 5 != 0) {
			return;
		}

		BlockPos center = player.blockPosition();
		Map<BlockPos, PlatformSettings> synced = SYNCED.computeIfAbsent(player.getUUID(), key -> new HashMap<>());

		for (BlockPos pos : getFirstAndSeconPos(center)) {
			BlockState state = serverLevel.getBlockState(pos);
			Block block = state.getBlock();

			if (block instanceof IPreviewReactive reactive) {
				reactive.onPreviewHover(serverLevel, pos, state);
			}

			// 把附近平台保存的搭建设置推给客户端, 这样界面外的预览也知道上下填充格数
			if (block instanceof IPlatformController && player instanceof ServerPlayer serverPlayer) {
				syncSettings(serverLevel, serverPlayer, pos, synced);
			}
		}
	}

	private static void syncSettings(ServerLevel level, ServerPlayer player, BlockPos pos, Map<BlockPos, PlatformSettings> synced) {
		PlatformSettings settings = PlatformSettingsStorage.get(level).get(pos).orElse(null);
		if (settings == null) {
			return;
		}

		BlockPos immutable = pos.immutable();
		if (settings.equals(synced.get(immutable))) {
			return;
		}

		synced.put(immutable, settings);
		IPNetwork.sendToPlayer(player, new PlatformSettingsSyncPacket(immutable, settings.mode(), settings.upFill(), settings.downFill(), settings.blueprintId()));
	}

	@SubscribeEvent
	public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		SYNCED.remove(event.getEntity().getUUID());
	}

	private static Iterable<BlockPos> getFirstAndSeconPos(BlockPos center) {
		return BlockPos.betweenClosed(
				center.offset(-15, -15, -15),
				center.offset(15, 15, 15)
		);
	}
}