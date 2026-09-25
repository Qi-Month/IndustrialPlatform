package dev.celestiacraft.industrialplatform.network.packet;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.common.menu.PlatformBuilderMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端 -> 服务端: 在建造站界面里选了哪份蓝图
 */
public record BuilderSelectPacket(String blueprintId) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<BuilderSelectPacket> TYPE = new CustomPacketPayload.Type<>(IndustrialPlatform.loadResource("builder_select"));

	public static final StreamCodec<RegistryFriendlyByteBuf, BuilderSelectPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, BuilderSelectPacket::blueprintId,
			BuilderSelectPacket::new
	);

	public BuilderSelectPacket(String blueprintId) {
		this.blueprintId = blueprintId == null ? "" : blueprintId;
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(BuilderSelectPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof PlatformBuilderMenu menu) {
				menu.selectBlueprint(packet.blueprintId());
			}
		});
	}
}