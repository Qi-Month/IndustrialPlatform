package dev.celestiacraft.industrialplatform.network;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.network.packet.BlueprintListPacket;
import dev.celestiacraft.industrialplatform.network.packet.DesignerSelectPacket;
import dev.celestiacraft.industrialplatform.network.packet.FillAdjustPacket;
import dev.celestiacraft.industrialplatform.network.packet.PlatformBuildPacket;
import dev.celestiacraft.industrialplatform.network.packet.PlatformSettingsClearPacket;
import dev.celestiacraft.industrialplatform.network.packet.PlatformSettingsPacket;
import dev.celestiacraft.industrialplatform.network.packet.PlatformSettingsSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 网络层: NeoForge 的 payload 注册, 每个包自己带 Type 与 StreamCodec
 */
public class IPNetwork {
	private static final String PROTOCOL_VERSION = "1";

	public static void register(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

		registrar.playToServer(PlatformSettingsPacket.TYPE, PlatformSettingsPacket.STREAM_CODEC, PlatformSettingsPacket::handle);
		registrar.playToServer(PlatformBuildPacket.TYPE, PlatformBuildPacket.STREAM_CODEC, PlatformBuildPacket::handle);
		registrar.playToServer(DesignerSelectPacket.TYPE, DesignerSelectPacket.STREAM_CODEC, DesignerSelectPacket::handle);
		registrar.playToServer(FillAdjustPacket.TYPE, FillAdjustPacket.STREAM_CODEC, FillAdjustPacket::handle);

		registrar.playToClient(PlatformSettingsSyncPacket.TYPE, PlatformSettingsSyncPacket.STREAM_CODEC, PlatformSettingsSyncPacket::handle);
		registrar.playToClient(BlueprintListPacket.TYPE, BlueprintListPacket.STREAM_CODEC, BlueprintListPacket::handle);
		registrar.playToClient(PlatformSettingsClearPacket.TYPE, PlatformSettingsClearPacket.STREAM_CODEC, PlatformSettingsClearPacket::handle);
	}

	public static void sendToServer(CustomPacketPayload payload) {
		PacketDistributor.sendToServer(payload);
	}

	public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
		PacketDistributor.sendToPlayer(player, payload);
	}

	public static void sendToTracking(ServerLevel level, BlockPos pos, CustomPacketPayload payload) {
		PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(pos), payload);
	}
}