package net.p3pp3rf1y.sophisticatedcore.util;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RecipeHelperTest {
	static {
		TestBootstrap.initialize();
	}

	private static Level regularOrderRecipesLevel;
	private static Level reverseOrderRecipesLevel;

	private static List<RecipeHolder<CraftingRecipe>> getCraftingRecipes() {
		List<RecipeHolder<CraftingRecipe>> craftingRecipes = new ArrayList<>();
		//stones
		craftingRecipes.add(recipe("granite_to_diorite", shaped(Items.GRANITE, Items.DIORITE)));
		craftingRecipes.add(recipe("granite_from_diorite", shapeless(Items.DIORITE, Items.GRANITE, 9)));
		craftingRecipes.add(recipe("stone_to_granite", shaped(Items.STONE, Items.GRANITE)));
		craftingRecipes.add(recipe("stone_from_granite", shapeless(Items.GRANITE, Items.STONE, 9)));

		//gold
		craftingRecipes.add(recipe("gold_ingot_to_gold_block", shaped(Items.GOLD_INGOT, Items.GOLD_BLOCK)));
		craftingRecipes.add(recipe("gold_ingot_from_gold_block", shapeless(Items.GOLD_BLOCK, Items.GOLD_INGOT, 9)));
		craftingRecipes.add(recipe("gold_nugget_to_gold_ingot", shaped(Items.GOLD_NUGGET, Items.GOLD_INGOT)));
		craftingRecipes.add(recipe("gold_nugget_from_gold_ingot", shapeless(Items.GOLD_INGOT, Items.GOLD_NUGGET, 9)));


		//confusion recipes
		craftingRecipes.add(recipe("gold_nugget_to_diorite", shaped(Items.GOLD_NUGGET, Items.DIORITE)));
		craftingRecipes.add(recipe("granite_to_gold_block", shaped(Items.GRANITE, Items.GOLD_BLOCK)));
		craftingRecipes.add(recipe("gold_nugget_from_granite", shapeless(Items.GRANITE, Items.GOLD_NUGGET, 9)));
		craftingRecipes.add(recipe("granite_from_diamond", shapeless(Items.DIAMOND, Items.GRANITE, 9)));
		craftingRecipes.add(recipe("iron_nugget_from_granite", shapeless(Items.GRANITE, Items.IRON_NUGGET, 9)));
		craftingRecipes.add(recipe("stone_from_gold_ingot", shapeless(Items.GOLD_INGOT, Items.STONE, 9)));
		craftingRecipes.add(recipe("torches_from_gold_block", shapeless(Items.GOLD_BLOCK, Items.TORCH, 9)));

		return craftingRecipes;
	}

	static Stream<Level> classParams() {
		return Stream.of(regularOrderRecipesLevel, reverseOrderRecipesLevel);
	}

	static Stream<Arguments> withClassParams(List<Arguments> methodParams) {
		return classParams().flatMap(classParam -> methodParams.stream().map(arguments -> new CombinedArguments(classParam, arguments)));
	}

	private static class CombinedArguments implements Arguments {
		private final Object[] arguments;

		public CombinedArguments(Level level, Arguments methodArguments) {
			arguments = new Object[methodArguments.get().length + 1];
			arguments[0] = level;
			System.arraycopy(methodArguments.get(), 0, arguments, 1, methodArguments.get().length);
		}
		@Override
		public Object[] get() {
			return arguments;
		}
	}

	@BeforeAll
	public static void setup() {
		regularOrderRecipesLevel = getLevelWithRecipeManagerFor(getCraftingRecipes());

		List<RecipeHolder<CraftingRecipe>> reverseOrderRecipes = getCraftingRecipes();
		Collections.reverse(reverseOrderRecipes);
		reverseOrderRecipesLevel = getLevelWithRecipeManagerFor(reverseOrderRecipes);
	}

	private static Level getLevelWithRecipeManagerFor(List<RecipeHolder<CraftingRecipe>> craftingRecipes) {
		RecipeManager mockRecipeManager = mock(RecipeManager.class);
		when(mockRecipeManager.getAllOfType(RecipeType.CRAFTING)).thenReturn(craftingRecipes);

		Level level = mock(Level.class);
		MinecraftServer server = mock(MinecraftServer.class);
		when(server.getRecipeManager()).thenReturn(mockRecipeManager);
		when(level.getServer()).thenReturn(server);
		return level;
	}

	private static RecipeHolder<CraftingRecipe> recipe(String id, CraftingRecipe recipe) {
		return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, Identifier.parse(id)), recipe);
	}

	private static ShapedRecipe shaped(Item ingredient, Item result) {
		return new ShapedRecipe(commonInfo(), bookInfo(), new ShapedRecipePattern(3, 3,
				Collections.nCopies(9, Optional.of(Ingredient.of(ingredient))), Optional.empty()), new ItemStackTemplate(result));
	}

	private static ShapelessRecipe shapeless(Item ingredient, Item result, int count) {
		return new ShapelessRecipe(commonInfo(), bookInfo(), new ItemStackTemplate(result, count), List.of(Ingredient.of(ingredient)));
	}

	private static Recipe.CommonInfo commonInfo() {
		return new Recipe.CommonInfo(true);
	}

	private static CraftingRecipe.CraftingBookInfo bookInfo() {
		return new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, "");
	}

	@AfterEach
	void clearCache() {
		RecipeHelper.onRecipesUpdated();
	}

	@ParameterizedTest
	@MethodSource
	void testGetCompatingResult(Level level, Item item, RecipeHelper.CompactingResult expectedResult) {
		RecipeHelper.setLevel(level);

		RecipeHelper.CompactingResult actualResult = RecipeHelper.getCompactingResult(new ItemStack(item), RecipeHelper.CompactingShape.THREE_BY_THREE_UNCRAFTABLE);

		assertCompactingResultEquals(expectedResult, actualResult, "getCompactingResult returned wrong result");
	}

	static Stream<Arguments> testGetCompatingResult() {
		return withClassParams(
				List.of(
						Arguments.of(Items.GOLD_INGOT, new RecipeHelper.CompactingResult(new ItemStack(Items.GOLD_BLOCK), Collections.emptyList())),
						Arguments.of(Items.GOLD_NUGGET, new RecipeHelper.CompactingResult(new ItemStack(Items.GOLD_INGOT), Collections.emptyList())),
						Arguments.of(Items.GRANITE, new RecipeHelper.CompactingResult(new ItemStack(Items.DIORITE), Collections.emptyList())),
						Arguments.of(Items.STONE, new RecipeHelper.CompactingResult(new ItemStack(Items.GRANITE), Collections.emptyList()))
				)
		);
	}


	@ParameterizedTest
	@MethodSource
	void testGetUncompactingResult(Level level, Item item, RecipeHelper.UncompactingResult expectedResult) {
		RecipeHelper.setLevel(level);

		RecipeHelper.UncompactingResult actualResult = RecipeHelper.getUncompactingResult(new ItemStack(item));

		assertUncompactingResultEquals(expectedResult, actualResult, "getUncompactingResult returned wrong result");
	}

	static Stream<Arguments> testGetUncompactingResult() {
		return withClassParams(
				List.of(
						Arguments.of(Items.GOLD_BLOCK, new RecipeHelper.UncompactingResult(new ItemStack(Items.GOLD_INGOT), RecipeHelper.CompactingShape.THREE_BY_THREE_UNCRAFTABLE)),
						Arguments.of(Items.GOLD_INGOT, new RecipeHelper.UncompactingResult(new ItemStack(Items.GOLD_NUGGET), RecipeHelper.CompactingShape.THREE_BY_THREE_UNCRAFTABLE)),
						Arguments.of(Items.DIORITE, new RecipeHelper.UncompactingResult(new ItemStack(Items.GRANITE), RecipeHelper.CompactingShape.THREE_BY_THREE_UNCRAFTABLE)),
						Arguments.of(Items.GRANITE, new RecipeHelper.UncompactingResult(new ItemStack(Items.STONE), RecipeHelper.CompactingShape.THREE_BY_THREE_UNCRAFTABLE))
				)
		);
	}

	@ParameterizedTest
	@MethodSource
	void testGetItemCompactingShapes(Level level, Item item, Set<RecipeHelper.CompactingShape> shapes) {
		RecipeHelper.setLevel(level);

		Set<RecipeHelper.CompactingShape> actualShapes = RecipeHelper.getItemCompactingShapes(new ItemStack(item));

		if (!Objects.equals(shapes, actualShapes)) {
			assertionFailure().message("getItemCompactingShapes returned wrong result")
					.expected(shapes)
					.actual(actualShapes)
					.buildAndThrow();
		}
	}

	static Stream<Arguments> testGetItemCompactingShapes() {
		return withClassParams(
				List.of(
						Arguments.of(Items.GOLD_INGOT, Set.of(RecipeHelper.CompactingShape.THREE_BY_THREE_UNCRAFTABLE)),
						Arguments.of(Items.GOLD_NUGGET, Set.of(RecipeHelper.CompactingShape.THREE_BY_THREE_UNCRAFTABLE)),
						Arguments.of(Items.GRANITE, Set.of(RecipeHelper.CompactingShape.THREE_BY_THREE_UNCRAFTABLE)),
						Arguments.of(Items.STONE, Set.of(RecipeHelper.CompactingShape.THREE_BY_THREE_UNCRAFTABLE))
				)
		);
	}

	private static void assertCompactingResultEquals(RecipeHelper.CompactingResult expected, RecipeHelper.CompactingResult actual, Object message) {
		if (ItemStack.matches(expected.getResult(), actual.getResult())
				&& areItemListsEqual(expected.getRemainingItems(), actual.getRemainingItems())) {
			return;
		}

		assertionFailure().message(message)
				.expected(expected.getResult() + ":" + expected.getRemainingItems())
				.actual(actual.getResult() + ":" + actual.getRemainingItems())
				.buildAndThrow();
	}


	private static boolean areItemListsEqual(List<ItemStack> expected, List<ItemStack> actual) {
		if (expected.size() != actual.size()) {
			return false;
		}
		for (int i = 0; i < expected.size(); i++) {
			if (!ItemStack.matches(expected.get(i), actual.get(i))) {
				return false;
			}
		}
		return true;
	}
	private static void assertUncompactingResultEquals(RecipeHelper.UncompactingResult expected, RecipeHelper.UncompactingResult actual, Object message) {
		if (ItemStack.isSameItemSameComponents(expected.getResult(), actual.getResult()) && expected.getCompactUsingShape() == actual.getCompactUsingShape()) {
			return;
		}
		assertionFailure().message(message)
				.expected(expected.getResult() + ":" + expected.getCompactUsingShape())
				.actual(actual.getResult() + ":" + actual.getCompactUsingShape())
				.buildAndThrow();
	}
}
