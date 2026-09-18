package dev.celestiacraft.industrialplatform.network.packet;

import dev.celestiacraft.industrialplatform.common.menu.IPlatformBuilderMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 客户端调整搭建界面设置: 向上填充 / 向下填充 / 模式
 */
public class PlatformSettingsPacket {
	private final int upFill;
	private final int downFill;
	private final int mode;

	public PlatformSettingsPacket(int upFill, int downFill, int mode) {
		this.upFill = upFill;
		this.downFill = downFill;
		this.mode = mode;
	}

	public static void encode(PlatformSettingsPacket packet, FriendlyByteBuf buf) {
		buf.writeVarInt(packet.upFill);
		buf.writeVarInt(packet.downFill);
		buf.writeVarInt(packet.mode);
	}

	public static PlatformSettingsPacket decode(FriendlyByteBuf buf) {
		return new PlatformSettingsPacket(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
	}

	public static void handle(PlatformSettingsPacket packet, Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		ServerPlayer player = context.getSender();

		if (player != null && player.containerMenu instanceof IPlatformBuilderMenu menu) {
			menu.applySettings(packet.upFill, packet.downFill, packet.mode);
		}

		context.setPacketHandled(true);
	}
}