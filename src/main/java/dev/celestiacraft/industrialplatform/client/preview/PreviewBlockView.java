package dev.celestiacraft.industrialplatform.client.preview;

import dev.celestiacraft.industrialplatform.api.IPLogic;
import dev.celestiacraft.industrialplatform.platform.blueprint.PlatformBlueprint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * 预览用的假世界: y = 0 起是结构底板, 底板下方 downFill 层是垫底方块, 其余全是空气, 光照恒为最亮
 */
public class PreviewBlockView implements BlockAndTintGetter {
	private static final BlockState AIR = Blocks.AIR.defaultBlockState();
	private static final int FULL_LIGHT = 15;
	private static final int NO_TINT = -1;

	private static final float SHADE_UP = 1.0F;
	private static final float SHADE_DOWN = 0.5F;
	private static final float SHADE_NORTH_SOUTH = 0.8F;
	private static final float SHADE_EAST_WEST = 0.6F;

	private final int sizeX;
	private final int sizeY;
	private final int sizeZ;
	private final int downFill;
	private final BlockState[] deck;
	private final BlockPos tintAnchor;

	/**
	 * @param tintAnchor 平台控制器的世界坐标, 草/树叶这类需要群系染色的方块按这个位置取色
	 */
	public PreviewBlockView(PlatformBlueprint structure, int downFill, BlockPos tintAnchor) {
		sizeX = Math.max(1, structure.sizeX());
		sizeY = Math.max(1, structure.sizeY());
		sizeZ = Math.max(1, structure.sizeZ());
		this.downFill = Math.max(0, downFill);
		this.tintAnchor = tintAnchor;
		deck = new BlockState[sizeX * sizeY * sizeZ];

		for (PlatformBlueprint.PlacedBlock placed : structure.getBlocks()) {
			BlockPos pos = placed.pos();
			if (isInDeck(pos.getX(), pos.getY(), pos.getZ())) {
				deck[deckIndex(pos.getX(), pos.getY(), pos.getZ())] = placed.state();
			}
		}
	}

	/**
	 * 只遍历可能被看见的方块: 底板全部 + 垫底区域的四面外墙, 垫底内部全被挡住, 不用送去渲染
	 */
	public void forEachVisibleBlock(BiConsumer<BlockPos, BlockState> consumer) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		for (int y = 0; y < sizeY; y++) {
			for (int z = 0; z < sizeZ; z++) {
				for (int x = 0; x < sizeX; x++) {
					BlockState state = deck[deckIndex(x, y, z)];
					if (state != null && !state.isAir()) {
						consumer.accept(pos.set(x, y, z), state);
					}
				}
			}
		}

		for (int y = -downFill; y < 0; y++) {
			for (int z = 0; z < sizeZ; z++) {
				boolean edgeRow = z == 0 || z == sizeZ - 1;
				int step = edgeRow ? 1 : Math.max(1, sizeX - 1);

				for (int x = 0; x < sizeX; x += step) {
					consumer.accept(pos.set(x, y, z), IPLogic.FILL_BLOCK);
				}
			}
		}
	}

	private boolean isInDeck(int x, int y, int z) {
		return x >= 0 && x < sizeX && y >= 0 && y < sizeY && z >= 0 && z < sizeZ;
	}

	private boolean isInFill(int x, int y, int z) {
		return y < 0 && y >= -downFill && x >= 0 && x < sizeX && z >= 0 && z < sizeZ;
	}

	private int deckIndex(int x, int y, int z) {
		return (y * sizeZ + z) * sizeX + x;
	}

	@Override
	public @NotNull BlockState getBlockState(@NotNull BlockPos pos) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		if (isInDeck(x, y, z)) {
			BlockState state = deck[deckIndex(x, y, z)];
			return state != null ? state : AIR;
		}

		return isInFill(x, y, z) ? IPLogic.FILL_BLOCK : AIR;
	}

	@Override
	public @NotNull FluidState getFluidState(@NotNull BlockPos pos) {
		return Fluids.EMPTY.defaultFluidState();
	}

	@Override
	public @Nullable BlockEntity getBlockEntity(@NotNull BlockPos pos) {
		return null;
	}

	@Override
	public float getShade(@NotNull Direction direction, boolean shade) {
		if (!shade) {
			return SHADE_UP;
		}

		return switch (direction) {
			case DOWN -> SHADE_DOWN;
			case UP -> SHADE_UP;
			case NORTH, SOUTH -> SHADE_NORTH_SOUTH;
			case WEST, EAST -> SHADE_EAST_WEST;
		};
	}

	/**
	 * 亮度直接由下面几个方法给出, 用不到光照引擎
	 */
	@Override
	public @Nullable LevelLightEngine getLightEngine() {
		return null;
	}

	@Override
	public int getBrightness(@NotNull LightLayer layer, @NotNull BlockPos pos) {
		return FULL_LIGHT;
	}

	@Override
	public int getRawBrightness(@NotNull BlockPos pos, int amount) {
		return FULL_LIGHT;
	}

	@Override
	public boolean canSeeSky(@NotNull BlockPos pos) {
		return true;
	}

	@Override
	public int getBlockTint(@NotNull BlockPos pos, @NotNull ColorResolver resolver) {
		ClientLevel level = Minecraft.getInstance().level;
		return level != null ? level.getBlockTint(tintAnchor, resolver) : NO_TINT;
	}

	@Override
	public int getHeight() {
		return downFill + sizeY;
	}

	@Override
	public int getMinBuildHeight() {
		return -downFill;
	}
}
