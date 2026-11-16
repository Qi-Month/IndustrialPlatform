package dev.celestiacraft.industrialplatform.datagen.recipe;

import dev.celestiacraft.industrialplatform.datagen.recipe.type.IPShapedRecipe;
import net.minecraft.core.registries.MultiRegistryBootstrap;
import net.minecraft.data.recipes.RecipeProvider;

public class IPRecipeProvider {
	public static final MultiRegistryBootstrap BOOTSTRAP = RecipeProvider.asBootstrap(IPShapedRecipe::new);
}