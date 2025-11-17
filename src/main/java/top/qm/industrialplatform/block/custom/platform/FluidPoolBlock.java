package top.qm.industrialplatform.block.custom.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import top.qm.industrialplatform.block.custom.BasePlatformBlock;

public class FluidPoolBlock extends BasePlatformBlock {
	public FluidPoolBlock() {
		super(Properties.copy(Blocks.DEEPSLATE_BRICKS));
	}

	@Override
	public void generateFloating(ServerLevel level, BlockPos pos, Player player) {
		int x = Math.floorDiv(pos.getX(), 16) * 16;
		int z = Math.floorDiv(pos.getZ(), 16) * 16;
		int y = pos.getY();

		if (y <= 0) {
			player.displayClientMessage(
					Component.translatable("message.industrial_platform.too_low"),
					true
			);
			return;
		}

		placeStructure(level, x, y - 31, z, "pool_top");
		placeStructure(level, x, y - 63, z, "pool_bottom");
	}

	@Override
	public void generateGround(ServerLevel level, BlockPos pos, Player player) {
		int x = Math.floorDiv(pos.getX(), 16) * 16;
		int z = Math.floorDiv(pos.getZ(), 16) * 16;
		int y = pos.getY();

		fillArea(level, x, y + 1, z, x + 15, y + 11, z + 15);
		generateFloating(level, pos, player);
	}
}