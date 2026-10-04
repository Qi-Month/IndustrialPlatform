package dev.celestiacraft.industrialplatform.datagen.recipe.type;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.IPTags;
import dev.celestiacraft.industrialplatform.common.register.IPBlocks;
import dev.celestiacraft.industrialplatform.common.register.IPItems;
import dev.celestiacraft.industrialplatform.datagen.recipe.IPRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

import java.util.concurrent.CompletableFuture;

public class IPShapedRecipe extends IPRecipeProvider {
	public IPShapedRecipe(PackOutput output, CompletableFuture<HolderLookup.Provider> future) {
		super(output, future);
	}

	public static void register(RecipeOutput output) {
		shaped(output);
	}

	private static void shaped(RecipeOutput output) {
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, IPBlocks.INDUSTRIAL_PLATFORM.get())
				.pattern("ABA")
				.pattern("CCC")
				.pattern("CCC")
				.define('A', Tags.Items.DYES)
				.define('B', Tags.Items.STONES)
				.define('C', IPTags.Items.DEEPSLATE)
				.unlockedBy("stone", has(Tags.Items.STONES))
				.save(output, IndustrialPlatform.loadResource("platform"));

		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, IPBlocks.FLUID_POOL.get())
				.pattern("A A")
				.pattern("B B")
				.pattern("BCB")
				.define('A', Tags.Items.DYES)
				.define('B', IPTags.Items.DEEPSLATE)
				.define('C', Tags.Items.STONES)
				.unlockedBy("stone", has(Tags.Items.STONES))
				.save(
						output.withConditions(new ModLoadedCondition("create")),
						IndustrialPlatform.loadResource("pool2")
				);

		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, IPBlocks.PLATFORM_BUILDER.get())
				.pattern("ABA")
				.pattern("BCB")
				.pattern("DDD")
				.define('A', Tags.Items.DYES)
				.define('B', IPTags.Items.DEEPSLATE)
				.define('C', IPBlocks.INDUSTRIAL_PLATFORM.get())
				.define('D', Tags.Items.STONES)
				.unlockedBy("platform", has(IPBlocks.INDUSTRIAL_PLATFORM.get()))
				.save(output, IndustrialPlatform.loadResource("platform_builder"));

		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, IPItems.FILL_ADJUSTER.get())
				.pattern(" A")
				.pattern("B ")
				.define('A', IPTags.Items.STONE)
				.define('B', Tags.Items.RODS_WOODEN)
				.unlockedBy("stick", has(Tags.Items.RODS_WOODEN))
				.save(output, IndustrialPlatform.loadResource("fill_adjuster"));
	}
}