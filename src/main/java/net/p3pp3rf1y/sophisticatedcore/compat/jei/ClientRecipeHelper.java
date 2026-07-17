package net.p3pp3rf1y.sophisticatedcore.compat.jei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.p3pp3rf1y.sophisticatedcore.crafting.IWrapperRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ClientRecipeHelper {
	private ClientRecipeHelper() {}

	public static <T extends Recipe<?>> Optional<RecipeHolder<T>> getCraftingRecipeByKey(RecipeType<T> type, Identifier recipeKey) {
		Minecraft minecraft = Minecraft.getInstance();
		ClientLevel world = minecraft.level;
		if (world == null) {
			return Optional.empty();
		}

		RecipeManager recipeManager = getRecipeManager(minecraft);
		if (recipeManager == null) {
			return Optional.empty();
		}
		RecipeHolder<?> recipeHolder = recipeManager.byKey(ResourceKey.create(Registries.RECIPE, recipeKey)).orElse(null);
		return recipeHolder != null && recipeHolder.value().getType().equals(type) ? Optional.of((RecipeHolder<T>) recipeHolder) : Optional.empty();
	}

	public static <I extends RecipeInput, T extends Recipe<I>, U extends Recipe<?>> List<RecipeHolder<T>> transformAllRecipesOfType(RecipeType<T> recipeType, Class<U> filterRecipeClass, Function<U, T> transformRecipe) {
		Minecraft minecraft = Minecraft.getInstance();
		ClientLevel level = minecraft.level;
		if (level == null) {
			return Collections.emptyList();
		}

		RecipeManager recipeManager = getRecipeManager(minecraft);
		if (recipeManager == null) {
			return Collections.emptyList();
		}

		return recipeManager.getRecipes()
				.stream()
				.filter(r -> r.value().getType().equals(recipeType))
				.filter(r -> filterRecipeClass.isInstance(r.value()))
				.map(r -> new RecipeHolder<>(r.id(), transformRecipe.apply(filterRecipeClass.cast(r.value()))))
				.toList();
	}

	public static <I extends RecipeInput, T extends Recipe<I>, U extends Recipe<?>> List<RecipeHolder<T>> transformAllRecipesOfTypeIntoMultiple(RecipeType<T> recipeType, Class<U> filterRecipeClass, Function<U, List<RecipeHolder<T>>> transformRecipe) {
		Minecraft minecraft = Minecraft.getInstance();
		ClientLevel level = minecraft.level;
		if (level == null) {
			return Collections.emptyList();
		}

		RecipeManager recipeManager = getRecipeManager(minecraft);
		if (recipeManager == null) {
			return Collections.emptyList();
		}

		return recipeManager.getRecipes()
				.stream()
				.filter(r -> r.value().getType().equals(recipeType))
				.filter(r -> filterRecipeClass.isInstance(r.value()))
				.map(r -> transformRecipe.apply(filterRecipeClass.cast(r.value())))
				.collect(ArrayList::new, List::addAll, List::addAll);
	}

	public static CraftingRecipe copyShapedRecipe(ShapedRecipe recipe) {
		return new ShapedRecipe(new Recipe.CommonInfo(recipe.showNotification()), new CraftingRecipe.CraftingBookInfo(recipe.category(), recipe.group()), recipe.pattern, ItemStackTemplate.fromNonEmptyStack(getResultItem(recipe)));
	}

	public static CraftingRecipe copyShapelessRecipe(ShapelessRecipe recipe) {
		return new ShapelessRecipe(new Recipe.CommonInfo(recipe.showNotification()), new CraftingRecipe.CraftingBookInfo(recipe.category(), recipe.group()), ItemStackTemplate.fromNonEmptyStack(getResultItem(recipe)), recipe.placementInfo().ingredients());
	}

	public static ItemStack getResultItem(Recipe<?> recipe) {
		if (recipe instanceof IWrapperRecipe<?> wrapperRecipe) {
			return getResultItem(wrapperRecipe.getCompose());
		}
		if (recipe instanceof ShapedRecipe shapedRecipe) {
			return shapedRecipe.result.create();
		}
		if (recipe instanceof ShapelessRecipe shapelessRecipe) {
			return shapelessRecipe.result.create();
		}
		return ItemStack.EMPTY;
	}

	public static <I extends RecipeInput> ItemStack assemble(Recipe<I> recipe, I container) {
		return recipe.assemble(container);
	}

	private static RecipeManager getRecipeManager(Minecraft minecraft) {
		MinecraftServer server = minecraft.getSingleplayerServer();
		return server == null ? null : server.getRecipeManager();
	}
}
