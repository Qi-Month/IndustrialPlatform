package top.nebula.industrialplatform.compat.jade;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import top.nebula.industrialplatform.IndustrialPlatform;
import top.nebula.industrialplatform.block.custom.platform.PlatformBlock;

@WailaPlugin
public class IPJadePlugin implements IWailaPlugin {
	@Override
	public void register(IWailaCommonRegistration registration) {
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerBlockComponent(IPComponentProvider.INSTANCE, PlatformBlock.class);
		IndustrialPlatform.LOGGER.info("Jade Plugin is registered!");
	}
}