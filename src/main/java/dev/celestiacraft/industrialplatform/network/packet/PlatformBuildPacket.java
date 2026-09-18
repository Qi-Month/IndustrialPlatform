package dev.celestiacraft.industrialplatform.network.packet;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.common.menu.IPlatformBuilderMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端点击搭建按钮
 */
public record PlatformBuildPacket() implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<PlatformBuildPacket> TYPE = new CustomPacketPayload.Type<>(IndustrialPlatform.loadResource("platform_build"));

	public static final StreamCodec<FriendlyByteBuf, PlatformBuildPacket> STREAM_CODEC = StreamCodec.unit(new PlatformBuildPacket());

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(PlatformBuildPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof IPlatformBuilderMenu menu) {
				menu.build(player);
			}
		});
	}
}