package top.qm.industrialplatform.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import top.qm.industrialplatform.IndustrialPlatform;

@SuppressWarnings("ALL")
@Mod.EventBusSubscriber(modid = IndustrialPlatform.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class IndustrialPlatformBlock extends Block {
    public IndustrialPlatformBlock() {
        super(Properties.copy(Blocks.DEEPSLATE_BRICKS).noOcclusion());
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Block.box(0, 0, 0, 16, 12, 16);
    }
}