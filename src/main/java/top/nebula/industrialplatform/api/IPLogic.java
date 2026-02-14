package top.nebula.industrialplatform.api;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import top.nebula.industrialplatform.IndustrialPlatform;

import java.util.Optional;

public class IPLogic {
	public static void placeStructure(ServerLevel level, int x, int y, int z, String structureId) {
		StructureTemplateManager manager = level.getStructureManager();
		ResourceLocation structureName = IndustrialPlatform.loadResource("industrial_platform/" + structureId);
		Optional<StructureTemplate> template = manager.get(structureName);
		template.ifPresent((temp) -> {
			temp.placeInWorld(
					level,
					new BlockPos(x, y, z),
					new BlockPos(x, y, z),
					new StructurePlaceSettings()
							.setRotation(Rotation.NONE)
							.setMirror(Mirror.NONE)
							.setIgnoreEntities(false),
					level.random,
					3
			);
		});
	}

	public static void placeExtendedStructure(ServerLevel level, int x, int y, int z, String structureId) {
		StructureTemplateManager manager = level.getStructureManager();
		ResourceLocation structureName = IndustrialPlatform.loadResource("industrial_platform/" + structureId);
		Optional<StructureTemplate> template = manager.get(structureName);
		for (int i = x - 16; i <= x + 16; i = i + 16) {
			for (int j = z - 16; j <= z + 16; j = j + 16) {
				int finalI = i;
				int finalJ = j;
				template.ifPresent((temp) -> {
					temp.placeInWorld(
							level,
							new BlockPos(finalI, y, finalJ),
							new BlockPos(finalI, y, finalJ),
							new StructurePlaceSettings()
									.setRotation(Rotation.NONE)
									.setMirror(Mirror.NONE)
									.setIgnoreEntities(false),
							level.random,
							3
					);
				});
			}
		}
	}

	public static void fillArea(ServerLevel level, int x0, int y0, int z0, int x1, int y1, int z1) {
		for (int x = x0; x <= x1; x++) {
			for (int y = y0; y <= y1; y++) {
				for (int z = z0; z <= z1; z++) {
					level.setBlockAndUpdate(
							new BlockPos(x, y, z),
							Blocks.AIR.defaultBlockState()
					);
				}
			}
		}
	}

	public static void fillAreaConditional(ServerLevel level, int x0, int y0, int z0, int x1, int y1, int z1) {
		for (int x = x0; x <= x1; x++) {
			for (int y = y0; y <= y1; y++) {
				for (int z = z0; z <= z1; z++) {
					BlockPos pos = new BlockPos(x, y, z);
					BlockState state = level.getBlockState(pos);
					if (state.isAir() || !state.getFluidState().isEmpty()) {
						level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
					}
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
}