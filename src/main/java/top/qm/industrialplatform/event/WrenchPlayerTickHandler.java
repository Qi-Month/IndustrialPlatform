package top.qm.industrialplatform.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.qm.industrialplatform.IndustrialPlatform;
import top.qm.industrialplatform.utils.IPTags;

@Mod.EventBusSubscriber(modid = IndustrialPlatform.MODID)
public class WrenchPlayerTickHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        Level level = player.level();

        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!player.getMainHandItem().is(IPTags.Items.WRENCH)) return;
        if (player.tickCount % 5 != 0) return;

        BlockPos center = player.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-3, -3, -3),
                center.offset(3, 3, 3))) {

            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();

            if (block instanceof IWrenchReactive reactive) {
                reactive.onWrenchHover(serverLevel, pos, state);
            }
        }
    }
}
