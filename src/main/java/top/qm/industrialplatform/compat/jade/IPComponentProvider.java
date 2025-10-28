package top.qm.industrialplatform.compat.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import top.qm.industrialplatform.IndustrialPlatform;

public class IPComponentProvider implements IBlockComponentProvider {
    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        tooltip.add(Component.translatable("aaaa"));
    }

    @Override
    public ResourceLocation getUid() {
        return IndustrialPlatform.loadResource("industrial_platform");
    }
}