package top.nebula.industrialplatform.compat.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import top.nebula.industrialplatform.block.custom.platform.PlatformBlock;
import top.nebula.industrialplatform.block.state.properties.platform.PlatformMode;

public enum IPComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        BlockState state = accessor.getBlockState();
        if (state.getValue(PlatformBlock.PLATFORM_MODE) == PlatformMode.INDUSTRIAL_LIGHT || state.getValue(PlatformBlock.PLATFORM_MODE) == PlatformMode.CHECKERBOARD_LIGHT) {
            tooltip.add(Component.translatable("tooltip.jade.industrialplatform.light"));
        } else {
            tooltip.add(Component.translatable("tooltip.jade.industrialplatform.heavy"));
        }
        if (state.getValue(PlatformBlock.FLOATING)) {
            tooltip.add(Component.translatable("tooltip.jade.industrialplatform.filling"));
        } else {
            tooltip.add(Component.translatable("tooltip.jade.industrialplatform.floating"));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return IPType.COMMON;
    }
}