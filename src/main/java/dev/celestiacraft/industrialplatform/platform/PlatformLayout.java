package dev.celestiacraft.industrialplatform.platform;

import dev.celestiacraft.industrialplatform.config.CommonConfig;
import net.minecraft.util.Mth;

/**
 * 平台底板占地: 以控制器所在区块为锚点的区块数
 *
 * @param chunksX 东西方向的区块数
 * @param chunksZ 南北方向的区块数
 */
public record PlatformLayout(int chunksX, int chunksZ) {
	public static final int BLOCKS_PER_CHUNK = 16;

	public PlatformLayout {
		int min = CommonConfig.MIN_PLATFORM_CHUNKS.get();
		int max = Math.max(min, CommonConfig.MAX_PLATFORM_CHUNKS.get());

		chunksX = Mth.clamp(chunksX, min, max);
		chunksZ = Mth.clamp(chunksZ, min, max);
	}

	public static PlatformLayout of(int chunksX, int chunksZ) {
		return new PlatformLayout(chunksX, chunksZ);
	}

	/**
	 * 方块数换算成需要的区块数(向上取整)
	 */
	public static int chunksOf(int blocks) {
		return Math.max(1, (blocks + BLOCKS_PER_CHUNK - 1) / BLOCKS_PER_CHUNK);
	}

	/**
	 * 东西方向长度(方块)
	 */
	public int width() {
		return this.chunksX * BLOCKS_PER_CHUNK;
	}

	/**
	 * 南北方向长度(方块)
	 */
	public int depth() {
		return this.chunksZ * BLOCKS_PER_CHUNK;
	}

	public int area() {
		return this.width() * this.depth();
	}

	public String describe() {
		return this.chunksX + "x" + this.chunksZ;
	}
}