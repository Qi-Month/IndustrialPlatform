package dev.celestiacraft.industrialplatform.compat.jade;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.PlatformPreviewSettings;
import dev.celestiacraft.industrialplatform.api.PlatformSettings;
import dev.celestiacraft.industrialplatform.common.block.platform.PlatformBlock;
import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformMode;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum IPComponentProvider implements IComponentProvider<BlockAccessor> {
	INSTANCE;

	private static final Component INDUSTRIAL = Component.translatable(tooltipKey("industrial"));
	private static final Component CHECKERBOARD = Component.translatable(tooltipKey("checkerboard"));
	private static final Component LIGHT = Component.translatable(tooltipKey("light"));
	private static final Component HEAVY = Component.translatable(tooltipKey("heavy"));
	private static final String FILL_UP_KEY = tooltipKey("fill_up");
	private static final String FILL_DOWN_KEY = tooltipKey("fill_down");
	private PlatformSettings displayedSettings;
	private Component fillUpTooltip;
	private Component fillDownTooltip;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor block, IPluginConfig config) {
		PlatformMode mode = block.getBlockState().getValue(PlatformBlock.PLATFORM_MODE);
		tooltip.add(mode.isCheckerboard() ? CHECKERBOARD : INDUSTRIAL);
		tooltip.add(mode.isHeavy() ? HEAVY : LIGHT);

		PlatformSettings settings = PlatformPreviewSettings.get(block.getPosition());
		if (settings == null) {
			return;
		}
		if (!settings.equals(displayedSettings)) {
			displayedSettings = settings;
			fillUpTooltip = Component.translatable(FILL_UP_KEY, settings.upFill());
			fillDownTooltip = Component.translatable(FILL_DOWN_KEY, settings.downFill());
		}
		tooltip.add(fillUpTooltip);
		tooltip.add(fillDownTooltip);
	}

	private static String tooltipKey(String key) {
		return "tooltip.jade." + IndustrialPlatform.MODID + "." + key;
	}

	@Override
	public Identifier getUid() {
		return IPType.COMMON;
	}
}