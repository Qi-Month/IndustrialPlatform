package dev.celestiacraft.industrialplatform.datagen;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.datagen.language.LanguageGenerate;
import dev.celestiacraft.industrialplatform.datagen.language.locale.Chinese;
import dev.celestiacraft.industrialplatform.datagen.language.locale.English;
import dev.celestiacraft.industrialplatform.datagen.loot.IPLootTableProvider;
import dev.celestiacraft.industrialplatform.datagen.recipe.IPRecipeProvider;
import dev.celestiacraft.industrialplatform.datagen.tags.IPBlockTagsProvider;
import dev.celestiacraft.industrialplatform.datagen.tags.IPItemTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = IndustrialPlatform.MODID)
public class DataGenerators {
	@SubscribeEvent
	public static void onClientDatagen(GatherDataEvent.Client event) {
		addProviders(event);
	}

	@SubscribeEvent
	public static void onServerDatagen(GatherDataEvent.Server event) {
		addProviders(event);
	}

	private static void addProviders(GatherDataEvent event) {
		PackOutput output = event.getGenerator().getPackOutput();
		CompletableFuture<HolderLookup.Provider> provider = event.getLookupProvider();
		LanguageGenerate.register();
		event.addProvider(new English(output));
		event.addProvider(new Chinese(output));
		event.addProvider(new IPBlockTagsProvider(output, provider));
		event.addProvider(new IPItemTagsProvider(output, provider));
		event.addProvider(new IPRecipeProvider(output, provider));
		event.addProvider(new IPLootTableProvider(output, provider));
	}
}
