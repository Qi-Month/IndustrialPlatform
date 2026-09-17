package dev.celestiacraft.industrialplatform.network.packet;

import dev.celestiacraft.industrialplatform.platform.blueprint.ClientBlueprintData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 服务端 -> 客户端: 可用的蓝图清单(名字/尺寸/材料), 以及当前选中的那份
 */
public class BlueprintListPacket {
	private final List<ClientBlueprintData.Entry> entries;
	private final String selectedId;
	private final Map<String, String> unavailable;

	public BlueprintListPacket(List<ClientBlueprintData.Entry> entries, String selectedId, Map<String, String> unavailable) {
		this.entries = entries;
		this.selectedId = selectedId == null ? "" : selectedId;
		this.unavailable = unavailable;
	}

	public static void encode(BlueprintListPacket packet, FriendlyByteBuf buf) {
		buf.writeVarInt(packet.entries.size());

		for (ClientBlueprintData.Entry entry : packet.entries) {
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

		buf.writeUtf(packet.selectedId);
		buf.writeVarInt(packet.unavailable.size());
		packet.unavailable.forEach((id, reason) -> {
			buf.writeUtf(id);
			buf.writeUtf(reason);
		});
	}

	public static BlueprintListPacket decode(FriendlyByteBuf buf) {
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
				Item item = BuiltInRegistries.ITEM.get(itemId);
				materials.put(item, amount);
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

	public static void handle(BlueprintListPacket packet, Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();

		ClientBlueprintData.set(packet.entries);
		ClientBlueprintData.setSelected(packet.selectedId);

		context.setPacketHandled(true);
	}
}