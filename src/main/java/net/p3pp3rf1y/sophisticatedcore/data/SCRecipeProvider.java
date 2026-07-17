package net.p3pp3rf1y.sophisticatedcore.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.crafting.UpgradeClearRecipe;

import java.util.concurrent.CompletableFuture;

public class SCRecipeProvider extends FabricRecipeProvider {
	public SCRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput recipeOutput) {
		return new RecipeProvider(registries, recipeOutput) {
			@Override
			public void buildRecipes() {
				SpecialRecipeBuilder.special(UpgradeClearRecipe::new).save(recipeOutput, SophisticatedCore.getRegistryName("upgrade_clear"));
			}
		};
	}

	@Override
	public String getName() {
		return "Sophisticated Core Recipes";
	}
}
