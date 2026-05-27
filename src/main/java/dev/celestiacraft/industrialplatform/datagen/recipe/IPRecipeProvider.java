package dev.celestiacraft.industrialplatform.datagen.recipe;

import dev.celestiacraft.industrialplatform.datagen.recipe.type.IPShapedRecipe;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class IPRecipeProvider extends RecipeProvider {
	public IPRecipeProvider(PackOutput output) {
		super(output);
	}

	@Override
	protected void buildRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
		shaped(consumer);
	}

	private void shaped(Consumer<FinishedRecipe> consumer) {
		IPShapedRecipe.register(consumer);
	}
}