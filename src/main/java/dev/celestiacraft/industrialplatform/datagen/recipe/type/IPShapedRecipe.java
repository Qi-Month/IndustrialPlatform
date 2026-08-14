package dev.celestiacraft.industrialplatform.datagen.recipe.type;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.IPTags;
import dev.celestiacraft.industrialplatform.block.IPBlocks;
import dev.celestiacraft.industrialplatform.datagen.recipe.IPRecipeProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;

import java.util.function.Consumer;

public class IPShapedRecipe extends IPRecipeProvider {
	public IPShapedRecipe(PackOutput output) {
		super(output);
	}

	public static void register(Consumer<FinishedRecipe> consumer) {
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, IPBlocks.INDUSTRIAL_PLATFORM.get())
				.pattern("ABA")
				.pattern("CCC")
				.pattern("CCC")
				.define('A', Tags.Items.DYES)
				.define('B', Tags.Items.STONE)
				.define('C', IPTags.Items.DEEPSLATE)
				.unlockedBy("stone", has(Tags.Items.STONE))
				.save(consumer, IndustrialPlatform.loadResource("platform"));

		ConditionalRecipe.builder()
				.addCondition(new ModLoadedCondition("create"))
				.addRecipe((recipe) -> {
					ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, IPBlocks.FLUID_POOL.get())
							.pattern("A A")
							.pattern("B B")
							.pattern("BBB")
							.define('A', Tags.Items.DYES)
							.define('C', IPTags.Items.DEEPSLATE)
							.define('D', Tags.Items.STONE)
							.unlockedBy("stone", has(Tags.Items.STONE))
							.save(recipe, IndustrialPlatform.loadResource("pool"));
				})
				.build(consumer, IndustrialPlatform.loadResource("pool"));
	}
}