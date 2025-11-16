package top.qm.industrialplatform.compat.jade;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import top.qm.industrialplatform.block.custom.platform.IndustrialPlatformBlock;

@WailaPlugin
public class IPJadePlugin implements IWailaPlugin {
	@Override
	public void register(IWailaCommonRegistration registration) {
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerBlockComponent(IPComponentProvider.INSTANCE, IndustrialPlatformBlock.class);
		System.out.println("Jade Plugin registered");
	}
}