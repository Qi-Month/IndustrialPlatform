package top.nebula.industrialplatform.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public interface IWrenchReactive {

    /**
     * 当玩家手持 wrench 靠近时调用
     */
    void onWrenchHover(ServerLevel level, BlockPos pos, BlockState state);
}