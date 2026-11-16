package dev.celestiacraft.industrialplatform.datagen.recipe.type;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.IPTags;
import dev.celestiacraft.industrialplatform.common.register.IPBlocks;
import dev.celestiacraft.industrialplatform.common.register.IPItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

public class IPShapedRecipe extends RecipeProvider {
	public IPShapedRecipe(HolderLookup.Provider registries, RecipeOutput output) {
		super(registries, output);
	}

	@Override
	protected void buildRecipes() {
		shaped(RecipeCategory.TOOLS, IPBlocks.INDUSTRIAL_PLATFORM.get())
				.pattern("ABA")
				.pattern("CCC")
				.pattern("CCC")
				.define('A', Tags.Items.DYES)
				.define('B', Tags.Items.STONES)
				.define('C', IPTags.Items.DEEPSLATE)
				.unlockedBy("stone", has(Tags.Items.STONES))
				.save(output, recipeKey("platform"));

		shaped(RecipeCategory.TOOLS, IPBlocks.FLUID_POOL.get())
				.pattern("A A")
				.pattern("B B")
				.pattern("BCB")
				.define('A', Tags.Items.DYES)
				.define('B', IPTags.Items.DEEPSLATE)
				.define('C', Tags.Items.STONES)
				.unlockedBy("stone", has(Tags.Items.STONES))
				.save(output.withConditions(new ModLoadedCondition("create")), recipeKey("pool2"));

		shaped(RecipeCategory.DECORATIONS, IPBlocks.PLATFORM_BUILDER.get())
				.pattern("ABA")
				.pattern("BCB")
				.pattern("DDD")
				.define('A', Tags.Items.DYES)
				.define('B', IPTags.Items.DEEPSLATE)
				.define('C', IPBlocks.INDUSTRIAL_PLATFORM.get())
				.define('D', Tags.Items.STONES)
				.unlockedBy("platform", has(IPBlocks.INDUSTRIAL_PLATFORM.get()))
				.save(output, recipeKey("platform_builder"));

		shaped(RecipeCategory.TOOLS, IPItems.FILL_ADJUSTER.get())
				.pattern(" A")
				.pattern("B ")
				.define('A', IPTags.Items.STONE)
				.define('B', Tags.Items.RODS_WOODEN)
				.unlockedBy("stick", has(Tags.Items.RODS_WOODEN))
				.save(output, recipeKey("fill_adjuster"));
	}

	private static ResourceKey<Recipe<?>> recipeKey(String name) {
		return ResourceKey.create(Registries.RECIPE, IndustrialPlatform.loadResource(name));
	}
}