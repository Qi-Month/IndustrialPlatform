package dev.celestiacraft.industrialplatform.datagen.recipe.type;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.IPTags;
import dev.celestiacraft.industrialplatform.block.BlockRegister;
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
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, BlockRegister.INDUSTRIAL_PLATFORM.get())
				.pattern("ACB")
				.pattern("DDD")
				.pattern("DDD")
				.define('A', Tags.Items.DYES_YELLOW)
				.define('B', Tags.Items.DYES_BLACK)
				.define('C', IPTags.Items.DEEPSLATE)
				.define('D', Tags.Items.STONES)
				.unlockedBy("stone", has(Tags.Items.STONES))
				.save(output, IndustrialPlatform.loadResource("platform"));

		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, BlockRegister.INDUSTRIAL_PLATFORM.get())
				.pattern("ACB")
				.pattern("DDD")
				.pattern("DDD")
				.define('A', Tags.Items.DYES_BLACK)
				.define('B', Tags.Items.DYES_YELLOW)
				.define('C', IPTags.Items.DEEPSLATE)
				.define('D', Tags.Items.STONES)
				.unlockedBy("stone", has(Tags.Items.STONES))
				.save(output, IndustrialPlatform.loadResource("platform_2"));

		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, BlockRegister.FLUID_POOL.get())
				.pattern("A B")
				.pattern("D D")
				.pattern("DCD")
				.define('A', Tags.Items.DYES_BLACK)
				.define('B', Tags.Items.DYES_YELLOW)
				.define('C', IPTags.Items.DEEPSLATE)
				.define('D', Tags.Items.STONES)
				.unlockedBy("stone", has(Tags.Items.STONES))
				.save(
						output.withConditions(new ModLoadedCondition("create")),
						IndustrialPlatform.loadResource("pool")
				);

		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, BlockRegister.FLUID_POOL.get())
				.pattern("A B")
				.pattern("D D")
				.pattern("DCD")
				.define('A', Tags.Items.DYES_BLACK)
				.define('B', Tags.Items.DYES_YELLOW)
				.define('C', IPTags.Items.DEEPSLATE)
				.define('D', Tags.Items.STONES)
				.unlockedBy("stone", has(Tags.Items.STONES))
				.save(
						output.withConditions(new ModLoadedCondition("create")),
						IndustrialPlatform.loadResource("pool2")
				);
	}
}