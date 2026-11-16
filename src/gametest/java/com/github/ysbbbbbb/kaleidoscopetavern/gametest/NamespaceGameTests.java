package com.github.ysbbbbbb.kaleidoscopetavern.gametest;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.block.deco.PaintingBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.block.deco.SandwichBoardBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopetavern.init.tag.TagCommon;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.GrapevineItem;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/** Public names must resolve after registration, including the compatibility entry points. */
final class NamespaceGameTests {
    static void publicNames(GameTestHelper h) {
        var stream = NamespaceGameTests.class.getClassLoader().getResourceAsStream("assets/kaleidoscope_tavern/lang/en_us.json");
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            var translations = JsonParser.parseReader(reader).getAsJsonObject();
            for (var entry : ModItems.ITEMS.getEntries()) {
                var item = entry.get();
                String expected = item instanceof BlockItem blockItem && !(item instanceof GrapevineItem)
                        ? originalBlockName(blockItem.getBlock())
                        : "item.kaleidoscope_tavern." + entry.getId().getPath();
                h.assertTrue(translations.has(expected), "Missing original item translation: " + entry.getId());
                h.assertValueEqual(entry.get().getDescriptionId(), expected, "Item description ID: " + entry.getId());
            }
            for (var entry : ModBlocks.BLOCKS.getEntries()) {
                String expected = originalBlockName(entry.get());
                h.assertValueEqual(entry.get().getDescriptionId(), expected, "Block description ID: " + entry.getId());
            }
        } catch (java.io.IOException exception) {
            throw new IllegalStateException(exception);
        }

        var level = h.getLevel();
        var originalRecipe = ResourceKey.create(Registries.RECIPE, KaleidoscopeTavern.modLoc("grass_sandwich_board_from_grass"));
        h.assertTrue(level.recipeAccess().byKey(originalRecipe).isPresent(), "Missing upstream grass board recipe ID");
        var renamedRecipe = ResourceKey.create(Registries.RECIPE, KaleidoscopeTavern.modLoc("grass_sandwich_board_from_short_grass"));
        h.assertTrue(level.recipeAccess().byKey(renamedRecipe).isEmpty(), "Unexpected renamed grass board recipe ID");
        h.assertTrue(ModItems.GRAPE.get().builtInRegistryHolder().is(TagCommon.LEGACY_FRUITS_GRAPES), "Missing forge grape tag");
        h.assertTrue(ModItems.GRAPE.get().builtInRegistryHolder().is(TagCommon.FRUITS_GRAPES), "Missing c grape tag");
        h.assertTrue(!BottleBlockItem.isValidForShaker(((BottleBlockItem) ModItems.WINE.get()).getFilledStack(3)), "Shaker accepted level 3 wine");
        h.assertTrue(BottleBlockItem.isValidForShaker(((BottleBlockItem) ModItems.WINE.get()).getFilledStack(4)), "Shaker rejected level 4 wine");

        var lines = new ArrayList<Component>();
        var wine = ((BottleBlockItem) ModItems.WINE.get()).getFilledStack(4);
        wine.getItem().appendHoverText(wine, Item.TooltipContext.of(level), TooltipDisplay.DEFAULT, lines::add, TooltipFlag.NORMAL);
        h.assertTrue(lines.stream().anyMatch(line -> line.getContents() instanceof TranslatableContents content
                && content.getKey().equals("color.kaleidoscope_tavern.prefix")), "Original wine color label missing");
        h.succeed();
    }

    private static String originalBlockName(Block block) {
        String name = block instanceof PaintingBlock ? "painting"
                : block instanceof SandwichBoardBlock ? "sandwich_board"
                : BuiltInRegistries.BLOCK.getKey(block).getPath();
        return "block.kaleidoscope_tavern." + name;
    }
}
