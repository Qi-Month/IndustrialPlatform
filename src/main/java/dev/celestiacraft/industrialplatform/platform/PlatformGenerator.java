package dev.celestiacraft.industrialplatform.platform;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 平台底板生成器.
 * <p>
 * 图案规则(和原来的结构 NBT 完全一致, 所以 1x1 区块的内置平台外观不变):
 * <ul>
 *     <li>最外一圈: 条纹, (x + z) 为偶数用主色, 奇数用副色</li>
 *     <li>边框内侧: 每隔 {@link #LAMP_SPACING} 格一盏灯, 四个内角一定有</li>
 *     <li>棋盘样式: (x + z) 为奇数用底色, 偶数用副色; 工业样式: 整片底色</li>
 * </ul>
 */
public class PlatformGenerator {
	/**
	 * 边框宽度(格)
	 */
	public static final int BORDER_WIDTH = 1;
	/**
	 * 灯距离外圈的内缩格数
	 */
	public static final int LAMP_INSET = 1;
	/**
	 * 灯沿边框的间隔(格)
	 */
	public static final int LAMP_SPACING = 15;

	/**
	 * 放置一个平台方块时的回调, 坐标是相对控制器所在区块角落的局部坐标
	 */
	public interface DeckConsumer {
		void accept(int localX, int localZ, BlockState state);
	}

	public static void forEachDeck(PlatformStyle style, PlatformPalette palette, PlatformLayout layout, DeckConsumer consumer) {
		int width = layout.width();
		int depth = layout.depth();

		for (int z = 0; z < depth; z++) {
			for (int x = 0; x < width; x++) {
				consumer.accept(x, z, deckStateAt(style, palette, layout, x, z));
			}
		}
	}

	/**
	 * 某个局部坐标上应该放什么
	 */
	public static BlockState deckStateAt(PlatformStyle style, PlatformPalette palette, PlatformLayout layout, int localX, int localZ) {
		int width = layout.width();
		int depth = layout.depth();

		boolean border = localX < BORDER_WIDTH
				|| localZ < BORDER_WIDTH
				|| localX >= width - BORDER_WIDTH
				|| localZ >= depth - BORDER_WIDTH;

		if (border) {
			return (localX + localZ) % 2 == 0 ? palette.borderPrimary() : palette.borderSecondary();
		}

		if (isLampPosition(width, depth, localX, localZ)) {
			return palette.lamp();
		}

		if (style == PlatformStyle.CHECKERBOARD) {
			return (localX + localZ) % 2 == 1 ? palette.base() : palette.checker();
		}

		return palette.base();
	}

	/**
	 * 灯只在边框内侧那一圈上, 并且沿圈每隔 LAMP_SPACING 格一盏
	 */
	public static boolean isLampPosition(int width, int depth, int localX, int localZ) {
		int minX = LAMP_INSET;
		int minZ = LAMP_INSET;
		int maxX = width - 1 - LAMP_INSET;
		int maxZ = depth - 1 - LAMP_INSET;

		if (localX < minX || localX > maxX || localZ < minZ || localZ > maxZ) {
			return false;
		}

		if (localZ == minZ || localZ == maxZ) {
			return isLampAlong(localX, minX, maxX);
		}

		if (localX == minX || localX == maxX) {
			return isLampAlong(localZ, minZ, maxZ);
		}

		return false;
	}

	private static boolean isLampAlong(int value, int min, int max) {
		return value == min || value == max || (value - min) % LAMP_SPACING == 0;
	}

	/**
	 * 底板 + 向下填充的垫底方块, 一共要消耗多少物品
	 */
	public static Map<Item, Integer> countMaterials(PlatformStyle style, PlatformPalette palette, PlatformLayout layout, int downFill, BlockState filler) {
		Map<Item, Integer> materials = new LinkedHashMap<>();

		forEachDeck(style, palette, layout, (localX, localZ, state) -> {
			add(materials, state, 1);
		});

		int layers = Math.max(0, downFill);
		if (layers > 0 && filler != null && !filler.isAir()) {
			add(materials, filler, layout.area() * layers);
		}

		return materials;
	}

	private static void add(Map<Item, Integer> materials, BlockState state, int amount) {
		if (state == null || state.isAir() || amount <= 0) {
			return;
		}

		Item item = state.getBlock().asItem();
		if (item == Blocks.AIR.asItem()) {
			return;
		}

		materials.merge(item, amount, Integer::sum);
	}
}