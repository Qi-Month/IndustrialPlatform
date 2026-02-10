package top.nebula.industrialplatform.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public interface IWrenchReactive {
	/**
	 * 当玩家手持 wrench 靠近时调用
	 *
	 * @param level
	 * @param pos
	 * @param state
	 */
	void onWrenchHover(ServerLevel level, BlockPos pos, BlockState state);
}