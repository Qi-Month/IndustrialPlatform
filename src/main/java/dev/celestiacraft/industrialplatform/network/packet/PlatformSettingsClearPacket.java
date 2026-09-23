package dev.celestiacraft.industrialplatform.network.packet;

import dev.celestiacraft.industrialplatform.api.PlatformPreviewSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlatformSettingsClearPacket {
	private final BlockPos pos;

	public PlatformSettingsClearPacket(BlockPos pos) {
		this.pos = pos;
	}

	public static void encode(PlatformSettingsClearPacket packet, FriendlyByteBuf buf) {
		buf.writeBlockPos(packet.pos);
	}

	public static PlatformSettingsClearPacket decode(FriendlyByteBuf buf) {
		return new PlatformSettingsClearPacket(buf.readBlockPos());
	}

	public static void handle(PlatformSettingsClearPacket packet, Supplier<NetworkEvent.Context> supplier) {
		PlatformPreviewSettings.remove(packet.pos);
		supplier.get().setPacketHandled(true);
	}
}
