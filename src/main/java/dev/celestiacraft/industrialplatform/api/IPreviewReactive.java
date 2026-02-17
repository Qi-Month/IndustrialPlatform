package dev.celestiacraft.industrialplatform.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public interface IPreviewReactive {
	/**
	 * 当玩家手持 wrench 或 平台触发方块 靠近时调用
	 *
	 * @param level
	 * @param pos
	 * @param state
	 */
	void onPreviewHover(ServerLevel level, BlockPos pos, BlockState state);
}