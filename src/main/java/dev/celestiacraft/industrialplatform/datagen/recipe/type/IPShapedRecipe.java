package dev.celestiacraft.industrialplatform.datagen.recipe.type;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.IPTags;
import dev.celestiacraft.industrialplatform.common.register.IPBlocks;
import dev.celestiacraft.industrialplatform.common.register.IPItems;
import dev.celestiacraft.industrialplatform.datagen.recipe.IPRecipeProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
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
							.pattern("BCB")
							.define('A', Tags.Items.DYES)
							.define('B', IPTags.Items.DEEPSLATE)
							.define('C', Tags.Items.STONE)
							.unlockedBy("stone", has(Tags.Items.STONE))
							.save(recipe, IndustrialPlatform.loadResource("pool"));
				})
				.build(consumer, IndustrialPlatform.loadResource("pool"));

		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, IPBlocks.PLATFORM_BUILDER.get())
				.pattern("ABA")
				.pattern("BCB")
				.pattern("DDD")
				.define('A', Tags.Items.DYES)
				.define('B', IPTags.Items.DEEPSLATE)
				.define('C', IPBlocks.INDUSTRIAL_PLATFORM.get())
				.define('D', IPTags.Items.STONE)
				.unlockedBy("platform", has(IPBlocks.INDUSTRIAL_PLATFORM.get()))
				.save(consumer, IndustrialPlatform.loadResource("platform_builder"));

		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, IPItems.FILL_ADJUSTER.get())
				.pattern(" A")
				.pattern("B ")
				.define('A', IPTags.Items.STONE)
				.define('B', Tags.Items.RODS_WOODEN)
				.unlockedBy("stick", has(Tags.Items.RODS_WOODEN))
				.save(consumer, IndustrialPlatform.loadResource("fill_adjuster"));
	}
}