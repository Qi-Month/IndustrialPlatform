package dev.celestiacraft.industrialplatform.datagen.recipe;

import dev.celestiacraft.industrialplatform.datagen.recipe.type.IPShapedRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class IPRecipeProvider extends RecipeProvider {
	public IPRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> future) {
		super(output, future);
	}

	@Override
	protected void buildRecipes(@NotNull RecipeOutput output) {
		shaped(output);
	}

	private void shaped(RecipeOutput output) {
		IPShapedRecipe.register(output);
	}
}