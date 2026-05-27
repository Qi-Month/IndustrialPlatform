package dev.celestiacraft.industrialplatform.api;

import dev.celestiacraft.industrialplatform.block.BlockRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import dev.celestiacraft.industrialplatform.IndustrialPlatform;

import javax.annotation.Nonnull;
import java.util.Optional;

public class IPLogic {
	public static void placeStructure(ServerLevel level, int x, int y, int z, String structureId) {
		StructureTemplateManager manager = level.getStructureManager();
		ResourceLocation structureName = IndustrialPlatform.loadResource(structureId);
		Optional<StructureTemplate> template = manager.get(structureName);
		template.ifPresent((temp) -> {
			temp.placeInWorld(
					level,
					new BlockPos(x, y, z),
					new BlockPos(x, y, z),
					createSafePlaceSettings(level),
					level.random,
					3
			);
		});
	}

	private static StructurePlaceSettings createSafePlaceSettings(ServerLevel level) {
		return new StructurePlaceSettings()
				.setRotation(Rotation.NONE)
				.setMirror(Mirror.NONE)
				.setIgnoreEntities(false)
				.addProcessor(new DropBeforePlaceProcessor(level));
	}

	public static void fillArea(ServerLevel level, int x0, int y0, int z0, int x1, int y1, int z1) {
		for (int x = x0; x <= x1; x++) {
			for (int y = y0; y <= y1; y++) {
				for (int z = z0; z <= z1; z++) {
					destroyIfBreakable(level, new BlockPos(x, y, z));
				}
			}
		}
	}

	public static void fillAreaConditional(ServerLevel level, int x0, int y0, int z0, int x1, int y1, int z1) {
		for (int x = x0; x <= x1; x++) {
			for (int y = y0; y <= y1; y++) {
				for (int z = z0; z <= z1; z++) {
					BlockPos pos = new BlockPos(x, y, z);
					fillStoneIfOpen(level, pos);
				}
			}
		}
	}

	public static void consumeItem(Player player, ItemStack stack, InteractionHand hand) {
		player.swing(hand);

		if (!player.isCreative()) {
			stack.shrink(1);
		}
	}

	private static boolean isUnbreakable(ServerLevel level, BlockPos pos, BlockState state) {
		return state.getDestroySpeed(level, pos) < 0.0F;
	}

	private static boolean destroyIfBreakable(ServerLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.isAir() || isUnbreakable(level, pos, state)) {
			return false;
		}

		level.destroyBlock(pos, shouldDrop(state));
		return true;
	}

	private static boolean fillStoneIfOpen(ServerLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (!state.isAir() && !(state.getBlock() instanceof LiquidBlock)) {
			return false;
		}

		level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
		return true;
	}

	private static void clearForTemplateAir(ServerLevel level, BlockPos pos, BlockState state) {
		if (state.isAir() || isUnbreakable(level, pos, state)) {
			return;
		}

		level.destroyBlock(pos, shouldDrop(state));
	}

	private static boolean shouldDrop(BlockState state) {
		return !state.is(BlockRegister.INDUSTRIAL_PLATFORM.get())
				&& !state.is(BlockRegister.FLUID_POOL.get())
				&& !state.is(IPTags.Blocks.NO_DROP_BLOCKS);
	}

	private static class DropBeforePlaceProcessor extends StructureProcessor {
		private final ServerLevel level;

		private DropBeforePlaceProcessor(ServerLevel level) {
			this.level = level;
		}

		@Override
		public StructureTemplate.StructureBlockInfo processBlock(
				@Nonnull LevelReader levelReader,
				@Nonnull BlockPos offset,
				@Nonnull BlockPos pos,
				@Nonnull StructureTemplate.StructureBlockInfo originalBlockInfo,
				StructureTemplate.StructureBlockInfo currentBlockInfo,
				@Nonnull StructurePlaceSettings settings
		) {
			BlockPos targetPos = currentBlockInfo.pos();
			BlockState targetState = level.getBlockState(targetPos);

			if (isUnbreakable(level, targetPos, targetState)) {
				return null;
			}

			if (currentBlockInfo.state().isAir()) {
				clearForTemplateAir(level, targetPos, targetState);
				return currentBlockInfo;
			}

			if (!targetState.isAir()) {
				level.destroyBlock(targetPos, shouldDrop(targetState));
			}

			return currentBlockInfo;
		}

		@Override
		protected @Nonnull StructureProcessorType<?> getType() {
			return StructureProcessorType.NOP;
		}
	}
}