package top.nebula.industrialplatform.compat.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import top.nebula.industrialplatform.IndustrialPlatform;
import top.nebula.industrialplatform.block.custom.platform.PlatformBlock;
import top.nebula.industrialplatform.block.state.properties.platform.PlatformMode;

public enum IPComponentProvider implements IBlockComponentProvider {
	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor block, IPluginConfig config) {
		BlockState state = block.getBlockState();

		boolean isIndustrialLight = state.getValue(PlatformBlock.PLATFORM_MODE) == PlatformMode.INDUSTRIAL_LIGHT;
		boolean isCheckerboardLight = state.getValue(PlatformBlock.PLATFORM_MODE) == PlatformMode.CHECKERBOARD_LIGHT;
		boolean isFloating = state.getValue(PlatformBlock.FLOATING);

		if (isIndustrialLight || isCheckerboardLight) {
			tooltip.add(Component.translatable("tooltip.jade." + IndustrialPlatform.MODID + ".light"));
		} else {
			tooltip.add(Component.translatable("tooltip.jade." + IndustrialPlatform.MODID + ".heavy"));
		}
		if (isFloating) {
			tooltip.add(Component.translatable("tooltip.jade." + IndustrialPlatform.MODID + ".filling"));
		} else {
			tooltip.add(Component.translatable("tooltip.jade." + IndustrialPlatform.MODID + ".floating"));
		}
	}

	@Override
	public ResourceLocation getUid() {
		return IPType.COMMON;
	}
}