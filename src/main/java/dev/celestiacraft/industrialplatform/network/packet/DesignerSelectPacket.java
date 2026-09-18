package dev.celestiacraft.industrialplatform.network.packet;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.common.menu.PlatformDesignerMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端 -> 服务端: 在设计台界面里选了哪份蓝图
 */
public record DesignerSelectPacket(String blueprintId) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<DesignerSelectPacket> TYPE = new CustomPacketPayload.Type<>(IndustrialPlatform.loadResource("designer_select"));

	public static final StreamCodec<RegistryFriendlyByteBuf, DesignerSelectPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, DesignerSelectPacket::blueprintId,
			DesignerSelectPacket::new
	);

	public DesignerSelectPacket(String blueprintId) {
		this.blueprintId = blueprintId == null ? "" : blueprintId;
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(DesignerSelectPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof PlatformDesignerMenu menu) {
				menu.selectBlueprint(packet.blueprintId());
			}
		});
	}
}