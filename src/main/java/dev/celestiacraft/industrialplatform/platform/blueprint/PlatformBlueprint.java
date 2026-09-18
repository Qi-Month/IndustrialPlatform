package dev.celestiacraft.industrialplatform.platform.blueprint;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
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
		ListTag sizeTag = tag.getList("size", Tag.TAG_INT);
		Vec3i size = new Vec3i(sizeTag.getInt(0), sizeTag.getInt(1), sizeTag.getInt(2));

		List<BlockState> palette = new ArrayList<>();
		List<String> missing = new ArrayList<>();
		ListTag paletteTag = readPalette(tag);

		for (int index = 0; index < paletteTag.size(); index++) {
			CompoundTag entry = paletteTag.getCompound(index);
			String name = entry.getString("Name");
			BlockState state = NbtUtils.readBlockState(blockLookup, entry);

			if (state.isAir() && !name.isEmpty() && !name.equals("minecraft:air")) {
				missing.add(name);
			}

			palette.add(state);
		}

		List<PlacedBlock> blocks = new ArrayList<>();
		Map<Item, Integer> materials = new LinkedHashMap<>();
		ListTag blockTags = tag.getList("blocks", Tag.TAG_COMPOUND);

		for (int index = 0; index < blockTags.size(); index++) {
			CompoundTag entry = blockTags.getCompound(index);
			int stateIndex = entry.getInt("state");

			if (stateIndex < 0 || stateIndex >= palette.size()) {
				continue;
			}

			BlockState state = palette.get(stateIndex);
			if (state.isAir() || state.is(Blocks.STRUCTURE_VOID)) {
				continue;
			}

			ListTag posTag = entry.getList("pos", Tag.TAG_INT);
			BlockPos pos = new BlockPos(posTag.getInt(0), posTag.getInt(1), posTag.getInt(2));

			blocks.add(new PlacedBlock(pos, state));
			addMaterial(materials, state);
		}

		return new PlatformBlueprint(id, size, blocks, materials, missing);
	}

	private static ListTag readPalette(CompoundTag tag) {
		if (tag.contains("palettes", Tag.TAG_LIST)) {
			ListTag palettes = tag.getList("palettes", Tag.TAG_LIST);
			if (!palettes.isEmpty()) {
				// 多调色板是原版的随机变体, 这里取第一套
				return palettes.getList(0);
			}
		}

		return tag.getList("palette", Tag.TAG_COMPOUND);
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