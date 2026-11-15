package com.github.ysbbbbbb.kaleidoscopetavern.compat.rei.category;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.compat.rei.ReiUtil;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.ShakerRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import com.github.ysbbbbbb.kaleidoscopetavern.compat.rei.TavernRecipeDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ReiShakerRecipeCategory implements DisplayCategory<TavernRecipeDisplay> {
    public static final CategoryIdentifier<TavernRecipeDisplay> ID = CategoryIdentifier.of(KaleidoscopeTavern.MOD_ID, "plugin/shaker");

    private static final MutableComponent TITLE = Component.translatable("block.kaleidoscope_tavern.shaker");
    private static final Identifier BG = KaleidoscopeTavern.modLoc("textures/gui/jei/shaker.png");

    public static final int WIDTH = 150;
    public static final int HEIGHT = 80;

    @Override
    public CategoryIdentifier<? extends TavernRecipeDisplay> getCategoryIdentifier() {
        return ID;
    }

    @Override
    public List<Widget> setupDisplay(TavernRecipeDisplay display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        int startX = bounds.x;
        int startY = bounds.y;

        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createTexturedWidget(BG, startX, startY, 0, 0, WIDTH, HEIGHT));

        var inputs = display.getInputEntries();
        int offsetY = 0;
        for (EntryIngredient input : inputs) {
            widgets.add(Widgets.createSlot(new Point(startX + 52, startY + 14 + offsetY))
                    .entries(input)
                    .markInput());
            offsetY += 18;
        }

        widgets.add(Widgets.createSlot(new Point(startX + 112, startY + 36))
                .entries(display.getOutputEntries().get(0))
                .backgroundEnabled(false)
                .markOutput());

        // 绘制颜色指示块
        display.getOptionalRecipe().ifPresent(holder -> {
            if (holder.value() instanceof ShakerRecipe shakerRecipe) {
                shakerRecipe.ingredientColors().forEach((index, color) -> {
                    int x = startX + 69;
                    int y = startY + 14 + 18 * index;
                    int rgba = 0xFF000000 | color.getColor();
                    widgets.add(Widgets.createDrawableWidget((guiGraphics, mouseX, mouseY, v) ->
                            guiGraphics.fill(x, y, x + 5, y + 16, rgba)));
                });
            }
        });

        return widgets;
    }

    @Override
    public int getDisplayWidth(TavernRecipeDisplay display) {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return HEIGHT;
    }

    @Override
    public Component getTitle() {
        return TITLE;
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(ModItems.SHAKER.get());
    }

    public static void registerCategories(CategoryRegistry registry) {
        registry.add(new ReiShakerRecipeCategory());
        registry.addWorkstations(ReiShakerRecipeCategory.ID, ReiUtil.ofItem(ModItems.SHAKER.get()));
    }

    public static void registerDisplays(DisplayRegistry registry) {
        com.github.ysbbbbbb.kaleidoscopetavern.client.init.ClientRecipeEvent.SHAKER_RECIPES.forEach(holder -> {
            ShakerRecipe recipe = holder.value();
            List<EntryIngredient> inputs = new ArrayList<>();
            for (var ingredient : recipe.getIngredients()) {
                if (!ingredient.isEmpty()) {
                    inputs.add(ReiUtil.ofIngredient(ingredient));
                }
            }

            List<EntryIngredient> outputs = List.of(ReiUtil.ofItemStack(recipe.result().create()));

            registry.add(new TavernRecipeDisplay(holder, inputs, outputs) {
                @Override
                public CategoryIdentifier<?> getCategoryIdentifier() {
                    return ReiShakerRecipeCategory.ID;
                }
            });
        });
    }
}
