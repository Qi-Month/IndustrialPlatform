package dev.celestiacraft.industrialplatform.network.packet;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.common.item.FillAdjusterItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端 -> 服务端: 填充调节器滚轮改过数值了
 */
public record FillAdjustPacket(int upFill, int downFill) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<FillAdjustPacket> TYPE = new CustomPacketPayload.Type<>(IndustrialPlatform.loadResource("fill_adjust"));

	public static final StreamCodec<RegistryFriendlyByteBuf, FillAdjustPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, FillAdjustPacket::upFill,
			ByteBufCodecs.VAR_INT, FillAdjustPacket::downFill,
			FillAdjustPacket::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(FillAdjustPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (!(context.player() instanceof ServerPlayer player)) {
				return;
			}

			ItemStack stack = player.getMainHandItem();
			if (stack.getItem() instanceof FillAdjusterItem) {
				FillAdjusterItem.setFills(stack, packet.upFill(), packet.downFill());
			}
		});
	}
}