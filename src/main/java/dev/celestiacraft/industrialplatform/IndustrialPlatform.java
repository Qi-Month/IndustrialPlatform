package dev.celestiacraft.industrialplatform;

import dev.celestiacraft.industrialplatform.common.register.IPBlocks;
import dev.celestiacraft.industrialplatform.common.register.IPItems;
import dev.celestiacraft.industrialplatform.common.register.IPMenus;
import dev.celestiacraft.industrialplatform.config.CommonConfig;
import dev.celestiacraft.industrialplatform.network.IPNetwork;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(IndustrialPlatform.MODID)
public class IndustrialPlatform {
	public static final String MODID = "industrial_platform";
	public static final String NAME = "Industrial Platform";
	public static final Logger LOGGER = LogManager.getLogger(NAME);

	public static ResourceLocation loadResource(String path) {
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}

	public IndustrialPlatform(IEventBus bus, ModContainer container) {
		container.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC, "nebula/" + MODID + "/common.toml");

		bus.addListener(IPNetwork::register);

		IPBlocks.register(bus);
		IPItems.register(bus);
		IPMenus.register(bus);
	}
}