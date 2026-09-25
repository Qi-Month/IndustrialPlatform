package dev.celestiacraft.industrialplatform.network.packet;

import dev.celestiacraft.industrialplatform.common.menu.PlatformBuilderMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 客户端 -> 服务端: 在设计台界面里选了哪份蓝图
 */
public class BuilderSelectPacket {
	private final String blueprintId;

	public BuilderSelectPacket(String blueprintId) {
		this.blueprintId = blueprintId == null ? "" : blueprintId;
	}

	public static void encode(BuilderSelectPacket packet, FriendlyByteBuf buf) {
		buf.writeUtf(packet.blueprintId);
	}

	public static BuilderSelectPacket decode(FriendlyByteBuf buf) {
		return new BuilderSelectPacket(buf.readUtf());
	}

	public static void handle(BuilderSelectPacket packet, Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		ServerPlayer player = context.getSender();

		if (player != null && player.containerMenu instanceof PlatformBuilderMenu menu) {
			menu.selectBlueprint(packet.blueprintId);
		}

		context.setPacketHandled(true);
	}
}