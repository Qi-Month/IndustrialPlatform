package dev.celestiacraft.industrialplatform.datagen.recipe;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.datagen.recipe.type.IPShapedRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.concurrent.CompletableFuture;

public class IPRecipeProvider extends RecipeProvider.Runner {
	public IPRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> future) {
		super(output, future);
	}

	@Override
	protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
		return new IPShapedRecipe(registries, output);
	}

	@Override
	public String getName() {
		return IndustrialPlatform.NAME + " Recipes";
	}
}