package com.github.ysbbbbbb.kaleidoscopetavern.datagen.recipe;

import com.github.ysbbbbbb.kaleidoscopetavern.datagen.builder.ShakerBuilder;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopetavern.init.tag.TagMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;


public class ShakerRecipeProvider extends ModRecipeProvider {
    public ShakerRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        // 血腥玛丽
        ShakerBuilder.builder()
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_RED))
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_RED))
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_RED))
                .setResult(ModItems.BLOODY_MARY.get())
                .save(this.output);

        // 翡翠
        ShakerBuilder.builder()
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_GREEN))
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_GREEN))
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_GREEN))
                .setResult(ModItems.EMERALD.get())
                .save(this.output);

        // 绿色蚱蜢
        ShakerBuilder.builder()
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_GREEN))
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_GREEN))
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_WHITE))
                .setResult(ModItems.GRASSHOPPER.get())
                .save(this.output);

        // 绒球葱花园
        ShakerBuilder.builder()
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_LIGHT_PURPLE))
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_LIGHT_PURPLE))
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_LIGHT_PURPLE))
                .setResult(ModItems.ALLIUM_GARDEN.get())
                .save(this.output);

        // 深水炸弹
        ShakerBuilder.builder()
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_BLUE))
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_LIGHT_PURPLE))
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_WHITE))
                .setResult(ModItems.DEPTH_CHARGE.get())
                .save(this.output);

        // 螺丝起子
        ShakerBuilder.builder()
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_YELLOW))
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_YELLOW))
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_GOLD))
                .setResult(ModItems.SCREWDRIVER.get())
                .save(this.output);

        // 教父
        ShakerBuilder.builder()
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_RED))
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_RED))
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_GOLD))
                .setResult(ModItems.GODFATHER.get())
                .save(this.output);

        // 白色佳人
        ShakerBuilder.builder()
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_WHITE))
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_GREEN))
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_YELLOW))
                .setResult(ModItems.WHITE_LADY.get())
                .save(this.output);

        // 莫吉托
        ShakerBuilder.builder()
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_WHITE))
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_WHITE))
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_GREEN))
                .setResult(ModItems.MOJITO.get())
                .save(this.output);

        // 黄铜心脏
        ShakerBuilder.builder()
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_GOLD))
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_GOLD))
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_GOLD))
                .setResult(ModItems.BRASS_HEART.get())
                .save(this.output);

        // 下界特调
        ShakerBuilder.builder()
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_RED))
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_GREEN))
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_BLUE))
                .setResult(ModItems.NETHER_SPECIAL.get())
                .save(this.output);

        // 幽匿特调
        ShakerBuilder.builder()
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_BLUE))
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_BLUE))
                .addIngredient(this.items.getOrThrow(TagMod.COCKTAIL_INGREDIENT_LIGHT_PURPLE))
                .setResult(ModItems.SCULK_SPECIAL.get())
                .save(this.output);
    }
}
