package dev.celestiacraft.industrialplatform.network.packet;

import dev.celestiacraft.industrialplatform.common.item.FillAdjusterItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 客户端 -> 服务端: 填充调节器滚轮改过数值了
 */
public class FillAdjustPacket {
	private final int upFill;
	private final int downFill;

	public FillAdjustPacket(int upFill, int downFill) {
		this.upFill = upFill;
		this.downFill = downFill;
	}

	public static void encode(FillAdjustPacket packet, FriendlyByteBuf buf) {
		buf.writeVarInt(packet.upFill);
		buf.writeVarInt(packet.downFill);
	}

	public static FillAdjustPacket decode(FriendlyByteBuf buf) {
		return new FillAdjustPacket(buf.readVarInt(), buf.readVarInt());
	}

	public static void handle(FillAdjustPacket packet, Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		ServerPlayer player = context.getSender();

		if (player != null) {
			ItemStack stack = player.getMainHandItem();
			if (stack.getItem() instanceof FillAdjusterItem) {
				FillAdjusterItem.setFills(stack, packet.upFill, packet.downFill);
			}
		}

		context.setPacketHandled(true);
	}
}