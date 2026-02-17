package dev.celestiacraft.industrialplatform.compat.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.block.platform.PlatformBlock;
import dev.celestiacraft.industrialplatform.block.state.properties.platform.PlatformMode;

public enum IPComponentProvider implements IComponentProvider<BlockAccessor> {
	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor block, IPluginConfig config) {
		BlockState state = block.getBlockState();

		boolean isLight = state.getValue(PlatformBlock.PLATFORM_MODE) == PlatformMode.INDUSTRIAL_LIGHT || state.getValue(PlatformBlock.PLATFORM_MODE) == PlatformMode.CHECKERBOARD_LIGHT;
		boolean isIndustrial = state.getValue(PlatformBlock.PLATFORM_MODE) == PlatformMode.INDUSTRIAL_LIGHT || state.getValue(PlatformBlock.PLATFORM_MODE) == PlatformMode.INDUSTRIAL_HEAVY;
		boolean isFloating = state.getValue(PlatformBlock.FLOATING);

		if (isIndustrial) {
			tooltip.add(Component.translatable(addTranKey("tooltip.jade.%s.industrial")));
		} else {
			tooltip.add(Component.translatable(addTranKey("tooltip.jade.%s.checkerboard")));
		}
		if (isLight) {
			tooltip.add(Component.translatable(addTranKey("tooltip.jade.%s.light")));
		} else {
			tooltip.add(Component.translatable(addTranKey("tooltip.jade.%s.heavy")));
		}
		if (isFloating) {
			tooltip.add(Component.translatable(addTranKey("tooltip.jade.%s.filling")));
		} else {
			tooltip.add(Component.translatable(addTranKey("tooltip.jade.%s.floating")));
		}
	}

	String addTranKey(String key) {
		return String.format(key, IndustrialPlatform.MODID);
	}

	@Override
	public ResourceLocation getUid() {
		return IPType.COMMON;
	}
}