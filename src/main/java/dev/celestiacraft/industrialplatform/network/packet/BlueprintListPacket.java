package dev.celestiacraft.industrialplatform.network.packet;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.platform.blueprint.ClientBlueprintData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务端 -> 客户端: 可用的蓝图清单(名字/尺寸/材料), 以及当前选中的那份
 */
public record BlueprintListPacket(List<ClientBlueprintData.Entry> entries, String selectedId, Map<String, String> unavailable) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<BlueprintListPacket> TYPE = new CustomPacketPayload.Type<>(IndustrialPlatform.loadResource("blueprint_list"));

	public static final StreamCodec<RegistryFriendlyByteBuf, BlueprintListPacket> STREAM_CODEC = StreamCodec.of(
			BlueprintListPacket::encode,
			BlueprintListPacket::decode
	);

	public BlueprintListPacket(List<ClientBlueprintData.Entry> entries, String selectedId, Map<String, String> unavailable) {
		this.entries = entries;
		this.selectedId = selectedId == null ? "" : selectedId;
		this.unavailable = unavailable;
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	private static void encode(RegistryFriendlyByteBuf buf, BlueprintListPacket packet) {
		buf.writeVarInt(packet.entries().size());

		for (ClientBlueprintData.Entry entry : packet.entries()) {
			buf.writeUtf(entry.id());
			buf.writeVarInt(entry.sizeX());
			buf.writeVarInt(entry.sizeY());
			buf.writeVarInt(entry.sizeZ());
			buf.writeVarInt(entry.blockCount());
			buf.writeVarInt(entry.materials().size());

			entry.materials().forEach((item, count) -> {
				buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(item));
				buf.writeVarInt(count);
			});
		}

		buf.writeUtf(packet.selectedId());
		buf.writeVarInt(packet.unavailable().size());
		packet.unavailable().forEach((id, reason) -> {
			buf.writeUtf(id);
			buf.writeUtf(reason);
		});
	}

	private static BlueprintListPacket decode(RegistryFriendlyByteBuf buf) {
		int count = buf.readVarInt();
		List<ClientBlueprintData.Entry> entries = new ArrayList<>(count);

		for (int index = 0; index < count; index++) {
			String id = buf.readUtf();
			int sizeX = buf.readVarInt();
			int sizeY = buf.readVarInt();
			int sizeZ = buf.readVarInt();
			int blockCount = buf.readVarInt();
			int materialCount = buf.readVarInt();
			Map<Item, Integer> materials = new LinkedHashMap<>();

			for (int material = 0; material < materialCount; material++) {
				ResourceLocation itemId = buf.readResourceLocation();
				int amount = buf.readVarInt();
				materials.put(BuiltInRegistries.ITEM.get(itemId), amount);
			}

			entries.add(new ClientBlueprintData.Entry(id, sizeX, sizeY, sizeZ, blockCount, materials));
		}

		String selectedId = buf.readUtf();
		int unavailableCount = buf.readVarInt();
		Map<String, String> unavailable = new LinkedHashMap<>();

		for (int index = 0; index < unavailableCount; index++) {
			unavailable.put(buf.readUtf(), buf.readUtf());
		}

		return new BlueprintListPacket(entries, selectedId, unavailable);
	}

	public static void handle(BlueprintListPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			ClientBlueprintData.set(packet.entries());
			ClientBlueprintData.setSelected(packet.selectedId());
		});
	}
}