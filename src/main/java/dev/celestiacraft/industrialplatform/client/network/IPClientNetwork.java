package dev.celestiacraft.industrialplatform.client.network;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.PlatformPreviewSettings;
import dev.celestiacraft.industrialplatform.api.PlatformSettings;
import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformMode;
import dev.celestiacraft.industrialplatform.network.packet.BlueprintListPacket;
import dev.celestiacraft.industrialplatform.network.packet.PlatformSettingsClearPacket;
import dev.celestiacraft.industrialplatform.network.packet.PlatformSettingsSyncPacket;
import dev.celestiacraft.industrialplatform.platform.blueprint.ClientBlueprintData;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@EventBusSubscriber(modid = IndustrialPlatform.MODID, value = Dist.CLIENT)
public class IPClientNetwork {
	public static void sendToServer(CustomPacketPayload payload) {
		ClientPacketDistributor.sendToServer(payload);
	}

	@SubscribeEvent
	public static void register(RegisterClientPayloadHandlersEvent event) {
		event.register(PlatformSettingsSyncPacket.TYPE, IPClientNetwork::handleSettingsSync);
		event.register(BlueprintListPacket.TYPE, IPClientNetwork::handleBlueprintList);
		event.register(PlatformSettingsClearPacket.TYPE, IPClientNetwork::handleSettingsClear);
	}

	private static void handleSettingsSync(PlatformSettingsSyncPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> PlatformPreviewSettings.put(
				packet.pos(),
				new PlatformSettings(PlatformMode.byIndex(packet.mode()), packet.upFill(), packet.downFill(), packet.blueprintId())
		));
	}

	private static void handleBlueprintList(BlueprintListPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			ClientBlueprintData.set(packet.entries());
			ClientBlueprintData.setSelected(packet.selectedId());
		});
	}

	private static void handleSettingsClear(PlatformSettingsClearPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> PlatformPreviewSettings.remove(packet.pos()));
	}
}
