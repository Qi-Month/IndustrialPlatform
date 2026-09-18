package dev.celestiacraft.industrialplatform;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import dev.celestiacraft.industrialplatform.common.register.IPBlocks;
import dev.celestiacraft.industrialplatform.config.CommonConfig;
import dev.celestiacraft.industrialplatform.common.register.IPItems;
import dev.celestiacraft.industrialplatform.common.register.IPMenus;
import dev.celestiacraft.industrialplatform.network.IPNetwork;

@Mod(IndustrialPlatform.MODID)
public class IndustrialPlatform {
	public static final String MODID = "industrial_platform";
	public static final String NAME = "Industrial Platform";
	public static final Logger LOGGER = LogManager.getLogger(NAME);

	public static ResourceLocation loadResource(String path) {
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}

	public IndustrialPlatform(FMLJavaModLoadingContext context) {
		IEventBus bus = context.getModEventBus();

		context.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC, "nebula/" + MODID + "/common.toml");

		IPBlocks.register(bus);
		IPItems.register(bus);
		IPMenus.register(bus);

		IPNetwork.register();
	}
}