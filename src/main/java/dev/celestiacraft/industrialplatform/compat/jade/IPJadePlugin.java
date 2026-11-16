package dev.celestiacraft.industrialplatform.compat.jade;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.common.block.platform.PlatformBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class IPJadePlugin implements IWailaPlugin {
	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerBlockComponent(IPComponentProvider.INSTANCE, PlatformBlock.class);
		IndustrialPlatform.LOGGER.info("Jade Plugin is registered!");
	}
}