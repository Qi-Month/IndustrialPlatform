package dev.celestiacraft.industrialplatform.network.packet;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.common.menu.IPlatformBuilderMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端调整搭建界面设置: 向上填充 / 向下填充 / 模式
 */
public record PlatformSettingsPacket(int upFill, int downFill, int mode) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<PlatformSettingsPacket> TYPE = new CustomPacketPayload.Type<>(IndustrialPlatform.loadResource("platform_settings"));

	public static final StreamCodec<RegistryFriendlyByteBuf, PlatformSettingsPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, PlatformSettingsPacket::upFill,
			ByteBufCodecs.VAR_INT, PlatformSettingsPacket::downFill,
			ByteBufCodecs.VAR_INT, PlatformSettingsPacket::mode,
			PlatformSettingsPacket::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(PlatformSettingsPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof IPlatformBuilderMenu menu) {
				menu.applySettings(packet.upFill(), packet.downFill(), packet.mode());
			}
		});
	}
}