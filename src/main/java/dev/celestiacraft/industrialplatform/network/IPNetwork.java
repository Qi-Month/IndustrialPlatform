package dev.celestiacraft.industrialplatform.network;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.network.packet.BlueprintListPacket;
import dev.celestiacraft.industrialplatform.network.packet.BuilderSelectPacket;
import dev.celestiacraft.industrialplatform.network.packet.FillAdjustPacket;
import dev.celestiacraft.industrialplatform.network.packet.PlatformBuildPacket;
import dev.celestiacraft.industrialplatform.network.packet.PlatformSettingsClearPacket;
import dev.celestiacraft.industrialplatform.network.packet.PlatformSettingsPacket;
import dev.celestiacraft.industrialplatform.network.packet.PlatformSettingsSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class IPNetwork {
	private static final String PROTOCOL_VERSION = "1";

	public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
			.named(IndustrialPlatform.loadResource("network"))
			.clientAcceptedVersions(PROTOCOL_VERSION::equals)
			.serverAcceptedVersions(PROTOCOL_VERSION::equals)
			.networkProtocolVersion(() -> PROTOCOL_VERSION)
			.simpleChannel();

	public static void register() {
		int index = 0;

		CHANNEL.messageBuilder(PlatformSettingsPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
				.encoder(PlatformSettingsPacket::encode)
				.decoder(PlatformSettingsPacket::decode)
				.consumerMainThread(PlatformSettingsPacket::handle)
				.add();

		CHANNEL.messageBuilder(PlatformBuildPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
				.encoder(PlatformBuildPacket::encode)
				.decoder(PlatformBuildPacket::decode)
				.consumerMainThread(PlatformBuildPacket::handle)
				.add();

		CHANNEL.messageBuilder(BuilderSelectPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
				.encoder(BuilderSelectPacket::encode)
				.decoder(BuilderSelectPacket::decode)
				.consumerMainThread(BuilderSelectPacket::handle)
				.add();

		CHANNEL.messageBuilder(FillAdjustPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
				.encoder(FillAdjustPacket::encode)
				.decoder(FillAdjustPacket::decode)
				.consumerMainThread(FillAdjustPacket::handle)
				.add();

		CHANNEL.messageBuilder(PlatformSettingsSyncPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(PlatformSettingsSyncPacket::encode)
				.decoder(PlatformSettingsSyncPacket::decode)
				.consumerMainThread(PlatformSettingsSyncPacket::handle)
				.add();

		CHANNEL.messageBuilder(BlueprintListPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(BlueprintListPacket::encode)
				.decoder(BlueprintListPacket::decode)
				.consumerMainThread(BlueprintListPacket::handle)
				.add();

		CHANNEL.messageBuilder(PlatformSettingsClearPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(PlatformSettingsClearPacket::encode)
				.decoder(PlatformSettingsClearPacket::decode)
				.consumerMainThread(PlatformSettingsClearPacket::handle)
				.add();
	}

	public static void sendToServer(Object packet) {
		CHANNEL.sendToServer(packet);
	}

	public static void sendToPlayer(ServerPlayer player, Object packet) {
		CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
	}

	public static void sendToTracking(ServerLevel level, BlockPos pos, Object packet) {
		CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), packet);
	}
}