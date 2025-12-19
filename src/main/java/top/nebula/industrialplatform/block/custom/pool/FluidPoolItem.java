package top.nebula.industrialplatform.block.custom.pool;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class FluidPoolItem extends BlockItem {
    public FluidPoolItem(Block block, Properties properties) {
        super(block, properties);
    }

    // 添加Tooltip
    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        String translated = Component.translatable("tooltip.industrial_platform.fluid_pool").getString();
        for (String line : translated.split("\n")) {
            tooltip.add(Component.literal(line));
        }
    }

}
