package dev.celestiacraft.industrialplatform.network.packet;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.PlatformPreviewSettings;
import dev.celestiacraft.industrialplatform.api.PlatformSettings;
import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 服务端 -> 客户端: 告知某个平台控制器的搭建设置, 界面外的预览要用
 */
public record PlatformSettingsSyncPacket(BlockPos pos, int mode, int upFill, int downFill, String blueprintId) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<PlatformSettingsSyncPacket> TYPE = new CustomPacketPayload.Type<>(IndustrialPlatform.loadResource("platform_settings_sync"));

	public static final StreamCodec<RegistryFriendlyByteBuf, PlatformSettingsSyncPacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, PlatformSettingsSyncPacket::pos,
			ByteBufCodecs.VAR_INT, PlatformSettingsSyncPacket::mode,
			ByteBufCodecs.VAR_INT, PlatformSettingsSyncPacket::upFill,
			ByteBufCodecs.VAR_INT, PlatformSettingsSyncPacket::downFill,
			ByteBufCodecs.STRING_UTF8, PlatformSettingsSyncPacket::blueprintId,
			PlatformSettingsSyncPacket::new
	);

	public PlatformSettingsSyncPacket(BlockPos pos, PlatformMode mode, int upFill, int downFill, String blueprintId) {
		this(pos, mode.ordinal(), upFill, downFill, blueprintId == null ? "" : blueprintId);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(PlatformSettingsSyncPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> PlatformPreviewSettings.put(
				packet.pos(),
				new PlatformSettings(PlatformMode.byIndex(packet.mode()), packet.upFill(), packet.downFill(), packet.blueprintId())
		));
	}
}