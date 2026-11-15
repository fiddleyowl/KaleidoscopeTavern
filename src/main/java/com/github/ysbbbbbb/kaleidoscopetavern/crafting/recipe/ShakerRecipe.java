package com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe;

import com.github.ysbbbbbb.kaleidoscopetavern.crafting.container.SimpleInput;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopetavern.util.ColorUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;

import java.util.List;

import net.minecraft.world.item.crafting.RecipeBookCategory;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.serializer.ShakerRecipeSerializer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.RecipeMatcher;
import org.jetbrains.annotations.Nullable;

public final class ShakerRecipe implements Recipe<SimpleInput> {
    public static final RecipeSerializer<ShakerRecipe> SERIALIZER = new RecipeSerializer<>(ShakerRecipeSerializer.CODEC, ShakerRecipeSerializer.STREAM_CODEC);
    private final List<Ingredient> ingredients;
    private final net.minecraft.world.item.ItemStackTemplate result;

    private Int2ObjectMap<ChatFormatting> ingredientColors;

    public ShakerRecipe(
            List<Ingredient> ingredients,
            net.minecraft.world.item.ItemStackTemplate result,
            Int2ObjectMap<ChatFormatting> ingredientColors
    ) {
        this.ingredients = ingredients;
        this.result = result;
        this.ingredientColors = ingredientColors;
    }

    @Override
    public boolean matches(SimpleInput input, Level level) {
        return RecipeMatcher.findMatches(input.getInputs(), ingredients) != null;
    }

    @Override
    public ItemStack assemble(SimpleInput input) {
        return this.result.create();
    }

    public List<Ingredient> getIngredients() {
        return this.ingredients;
    }

    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result.create();
    }

    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return false;
    }

    @Override
    public RecipeSerializer<ShakerRecipe> getSerializer() {
        return ModRecipes.SHAKER_SERIALIZER.get();
    }

    @Override
    public RecipeType<ShakerRecipe> getType() {
        return ModRecipes.SHAKER_RECIPE.get();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipes.SHAKER_RECIPE_CATEGORY.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public net.minecraft.world.item.crafting.PlacementInfo placementInfo() {
        return net.minecraft.world.item.crafting.PlacementInfo.NOT_PLACEABLE;
    }

    public List<Ingredient> ingredients() {
        return ingredients;
    }

    public net.minecraft.world.item.ItemStackTemplate result() {
        return result;
    }

    public Int2ObjectMap<ChatFormatting> ingredientColors() {
        if (this.ingredientColors.equals(Int2ObjectMaps.emptyMap())) {
            this.ingredientColors = getIngredientColors(this.ingredients);
        }
        return ingredientColors;
    }

    private static Int2ObjectMap<ChatFormatting> getIngredientColors(List<Ingredient> ingredients) {
        Int2ObjectMap<ChatFormatting> ingredientColors = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < ingredients.size(); i++) {
            ChatFormatting formatting = getColor(ingredients.get(i));
            if (formatting != null) {
                ingredientColors.put(i, formatting);
            }
        }
        return ingredientColors;
    }

    @Nullable
    private static ChatFormatting getColor(Ingredient ingredient) {
        for (var stack : ingredient.items().toList()) {
            for (var entry : ColorUtils.COCKTAIL_INGREDIENT_COLORS.entrySet()) {
                if (stack.is(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }
}
