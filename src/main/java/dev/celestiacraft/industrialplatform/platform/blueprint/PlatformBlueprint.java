package dev.celestiacraft.industrialplatform.platform.blueprint;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 从外部导入的平台蓝图(原版结构 NBT 格式).
 * <p>
 * 结构文件和结构方块导出的格式一样: {@code size / palette(s) / blocks},
 * 所以玩家可以用结构方块、WorldEdit、Create 蓝图工具等导出来直接丢进 schematics/platform.
 */
public class PlatformBlueprint {
	private final String id;
	private final Vec3i size;
	private final List<PlacedBlock> blocks;
	private final Map<Item, Integer> materials;
	private final List<String> missingBlocks;

	public PlatformBlueprint(String id, Vec3i size, List<PlacedBlock> blocks, Map<Item, Integer> materials, List<String> missingBlocks) {
		this.id = id;
		this.size = size;
		this.blocks = blocks;
		this.materials = materials;
		this.missingBlocks = missingBlocks;
	}

	/**
	 * 蓝图里的一个方块, 坐标是相对蓝图原点的局部坐标
	 */
	public record PlacedBlock(BlockPos pos, BlockState state) {
	}

	/**
	 * 解析结构 NBT. 不认识的方块会被记进 missingBlocks 并跳过, 不会让整份蓝图读不出来
	 */
	public static PlatformBlueprint parse(String id, CompoundTag tag, HolderGetter<Block> blockLookup) {
		Vec3i size = readVector(requireList(tag, "size"), "size");
		if (size.getX() <= 0 || size.getY() <= 0 || size.getZ() <= 0) {
			throw new IllegalArgumentException("Blueprint size must be positive");
		}

		List<BlockState> palette = new ArrayList<>();
		List<String> missing = new ArrayList<>();
		ListTag paletteTag = readPalette(tag);
		for (Tag element : paletteTag) {
			CompoundTag entry = normalizeBlockState(requireCompound(element, "palette"));
			String name = entry.getStringOr("id", "");
			Identifier blockId = Identifier.tryParse(name);
			if (blockId == null) {
				throw new IllegalArgumentException("Invalid palette block identifier: " + name);
			}
			if (blockLookup.get(ResourceKey.create(Registries.BLOCK, blockId)).isEmpty()) {
				missing.add(name);
			}
			palette.add(NbtUtils.readBlockState(blockLookup, entry));
		}

		List<PlacedBlock> blocks = new ArrayList<>();
		Map<Item, Integer> materials = new LinkedHashMap<>();
		ListTag blockTags = requireList(tag, "blocks");
		for (Tag element : blockTags) {
			CompoundTag entry = requireCompound(element, "blocks");
			Vec3i coordinates = readVector(requireList(entry, "pos"), "pos");
			if (!(entry.get("state") instanceof IntTag stateTag)) {
				throw new IllegalArgumentException("Block state must be an integer");
			}
			int stateIndex = stateTag.intValue();
			if (stateIndex < 0 || stateIndex >= palette.size()) {
				continue;
			}

			BlockState state = palette.get(stateIndex);
			if (state.isAir() || state.is(Blocks.STRUCTURE_VOID)) {
				continue;
			}
			BlockPos pos = new BlockPos(coordinates.getX(), coordinates.getY(), coordinates.getZ());
			blocks.add(new PlacedBlock(pos, state));
			addMaterial(materials, state);
		}

		return new PlatformBlueprint(id, size, blocks, materials, missing);
	}

	private static CompoundTag normalizeBlockState(CompoundTag entry) {
		if (entry.contains("id")) {
			return entry;
		}
		CompoundTag state = entry.copy();
		state.putString("id", state.getStringOr("Name", ""));
		state.remove("Name");
		Tag properties = state.get("Properties");
		if (properties != null) {
			state.put("properties", properties);
			state.remove("Properties");
		}
		return state;
	}

	private static ListTag readPalette(CompoundTag tag) {
		if (tag.contains("palettes")) {
			ListTag palettes = requireList(tag, "palettes");
			if (!palettes.isEmpty()) {
				return palettes.getList(0).orElseThrow(() -> new IllegalArgumentException("Palette variant must be a list"));
			}
		}
		return requireList(tag, "palette");
	}

	private static ListTag requireList(CompoundTag tag, String key) {
		return tag.getList(key).orElseThrow(() -> new IllegalArgumentException("Missing or invalid list: " + key));
	}

	private static CompoundTag requireCompound(Tag tag, String key) {
		if (tag instanceof CompoundTag compound) {
			return compound;
		}
		throw new IllegalArgumentException(key + " entry must be a compound");
	}

	private static Vec3i readVector(ListTag tag, String key) {
		if (tag.size() != 3 || !(tag.get(0) instanceof IntTag x) || !(tag.get(1) instanceof IntTag y) || !(tag.get(2) instanceof IntTag z)) {
			throw new IllegalArgumentException(key + " must contain three integers");
		}
		return new Vec3i(x.intValue(), y.intValue(), z.intValue());
	}

	private static void addMaterial(Map<Item, Integer> materials, BlockState state) {
		Item item = state.getBlock().asItem();
		if (item == Items.AIR) {
			return;
		}

		materials.merge(item, 1, Integer::sum);
	}

	public String getId() {
		return id;
	}

	public Vec3i getSize() {
		return size;
	}

	public int sizeX() {
		return size.getX();
	}

	public int sizeY() {
		return size.getY();
	}

	public int sizeZ() {
		return size.getZ();
	}

	public List<PlacedBlock> getBlocks() {
		return blocks;
	}

	public Map<Item, Integer> getMaterials() {
		return materials;
	}

	public List<String> getMissingBlocks() {
		return missingBlocks;
	}

	public boolean hasMissingBlocks() {
		return !missingBlocks.isEmpty();
	}

	public int getBlockCount() {
		return blocks.size();
	}

	public String describe() {
		return id + " (" + sizeX() + "x" + sizeY() + "x" + sizeZ() + ", " + getBlockCount() + " blocks)";
	}
}