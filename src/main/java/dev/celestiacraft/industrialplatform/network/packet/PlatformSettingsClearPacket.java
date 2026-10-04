package dev.celestiacraft.industrialplatform.network.packet;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.PlatformPreviewSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlatformSettingsClearPacket(BlockPos pos) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<PlatformSettingsClearPacket> TYPE = new CustomPacketPayload.Type<>(IndustrialPlatform.loadResource("platform_settings_clear"));

	public static final StreamCodec<RegistryFriendlyByteBuf, PlatformSettingsClearPacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, PlatformSettingsClearPacket::pos,
			PlatformSettingsClearPacket::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(PlatformSettingsClearPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> PlatformPreviewSettings.remove(packet.pos()));
	}
}