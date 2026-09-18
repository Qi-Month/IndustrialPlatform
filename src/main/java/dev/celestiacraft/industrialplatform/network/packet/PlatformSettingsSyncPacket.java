package dev.celestiacraft.industrialplatform.network.packet;

import dev.celestiacraft.industrialplatform.api.PlatformPreviewSettings;
import dev.celestiacraft.industrialplatform.api.PlatformSettings;
import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 服务端 -> 客户端: 告知某个平台控制器的搭建设置, 界面外的预览要用
 */
public class PlatformSettingsSyncPacket {
	private final BlockPos pos;
	private final int mode;
	private final int upFill;
	private final int downFill;
	private final String blueprintId;

	public PlatformSettingsSyncPacket(BlockPos pos, PlatformMode mode, int upFill, int downFill, String blueprintId) {
		this.pos = pos;
		this.mode = mode.ordinal();
		this.upFill = upFill;
		this.downFill = downFill;
		this.blueprintId = blueprintId == null ? "" : blueprintId;
	}

	public static void encode(PlatformSettingsSyncPacket packet, FriendlyByteBuf buf) {
		buf.writeBlockPos(packet.pos);
		buf.writeVarInt(packet.mode);
		buf.writeVarInt(packet.upFill);
		buf.writeVarInt(packet.downFill);
		buf.writeUtf(packet.blueprintId);
	}

	public static PlatformSettingsSyncPacket decode(FriendlyByteBuf buf) {
		return new PlatformSettingsSyncPacket(
				buf.readBlockPos(),
				PlatformMode.byIndex(buf.readVarInt()),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readUtf()
		);
	}

	public static void handle(PlatformSettingsSyncPacket packet, Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();

		PlatformPreviewSettings.put(
				packet.pos,
				new PlatformSettings(PlatformMode.byIndex(packet.mode), packet.upFill, packet.downFill, packet.blueprintId)
		);

		context.setPacketHandled(true);
	}
}