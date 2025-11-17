package top.qm.industrialplatform.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class BasePlatformBlock extends Block implements SimpleWaterloggedBlock {

	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public BasePlatformBlock(Properties props) {
		super(props.noOcclusion());
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(WATERLOGGED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED);
	}

	@Override
	public @NotNull FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED)
				? Fluids.WATER.getSource(false)
				: super.getFluidState(state);
	}

	@Override
	public @NotNull BlockState updateShape(BlockState state, @NotNull Direction dir, @NotNull BlockState neighborState,
	                                       @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
		if (state.getValue(WATERLOGGED)) {
			level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		}
		return super.updateShape(state, dir, neighborState, level, pos, neighborPos);
	}

	public static void placeStructure(ServerLevel level, int x, int y, int z, String id) {
		StructureTemplateManager manager = level.getStructureManager();
		ResourceLocation name = ResourceLocation.parse("industrial_platform:industrial_platform/" + id);
		Optional<StructureTemplate> template = manager.get(name);

		template.ifPresent((temp) -> {
			temp.placeInWorld(level,
					new BlockPos(x, y, z),
					new BlockPos(x, y, z),
					new StructurePlaceSettings(),
					level.random,
					3
			);
		});
	}

	protected void placeExtendedStructure(ServerLevel level, int x, int y, int z, String id) {
		StructureTemplateManager manager = level.getStructureManager();
		ResourceLocation name = ResourceLocation.parse("industrial_platform:industrial_platform/" + id);
		Optional<StructureTemplate> template = manager.get(name);

		for (int px = x - 16; px <= x + 16; px += 16) {
			for (int pz = z - 16; pz <= z + 16; pz += 16) {
				int finalPx = px;
				int finalPz = pz;
				template.ifPresent((temp) -> {
					temp.placeInWorld(
							level,
							new BlockPos(finalPx, y, finalPz),
							new BlockPos(finalPx, y, finalPz),
							new StructurePlaceSettings(),
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
					level.setBlockAndUpdate(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
				}
			}
		}
	}

	protected void fillAreaConditional(ServerLevel level, int x0, int y0, int z0, int x1, int y1, int z1) {
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

	public abstract void generateFloating(ServerLevel level, BlockPos pos, Player player);

	public abstract void generateGround(ServerLevel level, BlockPos pos, Player player);
}