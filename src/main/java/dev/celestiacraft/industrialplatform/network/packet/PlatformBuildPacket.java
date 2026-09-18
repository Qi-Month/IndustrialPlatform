package dev.celestiacraft.industrialplatform.network.packet;

import dev.celestiacraft.industrialplatform.common.menu.IPlatformBuilderMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 客户端点击搭建按钮
 */
public class PlatformBuildPacket {
	public static void encode(PlatformBuildPacket packet, FriendlyByteBuf buf) {
	}

	public static PlatformBuildPacket decode(FriendlyByteBuf buf) {
		return new PlatformBuildPacket();
	}

	public static void handle(PlatformBuildPacket packet, Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		ServerPlayer player = context.getSender();

		if (player != null && player.containerMenu instanceof IPlatformBuilderMenu menu) {
			menu.build(player);
		}

		context.setPacketHandled(true);
	}
}