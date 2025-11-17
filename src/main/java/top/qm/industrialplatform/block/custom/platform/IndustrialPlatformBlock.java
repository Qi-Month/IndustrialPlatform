package top.qm.industrialplatform.block.custom.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import top.qm.industrialplatform.block.custom.BasePlatformBlock;

import java.util.*;

public class IndustrialPlatformBlock extends BasePlatformBlock {
	public IndustrialPlatformBlock() {
		super(Properties.copy(Blocks.DEEPSLATE_BRICKS));
	}

	@Override
	public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos,
	                                    @NotNull CollisionContext context) {
		return Block.box(0, 0, 0, 16, 12, 16);
	}

	@Override
	public void generateFloating(ServerLevel level, BlockPos pos, Player player) {
		int x = Math.floorDiv(pos.getX(), 16) * 16;
		int z = Math.floorDiv(pos.getZ(), 16) * 16;
		int y = pos.getY();

		BasePlatformBlock.placeStructure(level, x, y, z, "industrial");
	}

	@Override
	public void generateGround(ServerLevel level, BlockPos pos, Player player) {
		int x = Math.floorDiv(pos.getX(), 16) * 16;
		int z = Math.floorDiv(pos.getZ(), 16) * 16;
		int y = pos.getY();

		BasePlatformBlock.fillArea(level, x, y + 1, z, x + 15, y + 11, z + 15);
		BasePlatformBlock.placeStructure(level, x, y, z, "industrial");
	}
}