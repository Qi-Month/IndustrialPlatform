package top.nebula.industrialplatform;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.nebula.industrialplatform.block.BlockRegister;
import top.nebula.industrialplatform.config.CommonConfig;

@Mod(IndustrialPlatform.MODID)
public class IndustrialPlatform {
	public static final String MODID = "industrial_platform";
	public static final String NAME = "Industrial Platform";
	public static final Logger LOGGER = LogManager.getLogger(NAME);

	public static ResourceLocation loadResource(String path) {
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}

	public IndustrialPlatform(IEventBus bus, ModContainer container) {
		container.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC, "nebula/industrial_platform/common.toml");

		BlockRegister.register(bus);
	}
}