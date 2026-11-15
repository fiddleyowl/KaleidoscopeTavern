package com.github.ysbbbbbb.kaleidoscopetavern.datagen.model;

import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * Keeps upstream artwork as resource inputs and generates native 26.1 item definitions.
 */
public final class ModModelProvider implements DataProvider {
    private final PackOutput.PathProvider items;

    public ModModelProvider(PackOutput output) {
        items = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
    }

    private static JsonObject model(String path) {
        var model = new JsonObject();
        model.addProperty("type", "minecraft:model");
        model.addProperty("model", "kaleidoscope_tavern:item/" + path);
        return model;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        var futures = new ArrayList<CompletableFuture<?>>();
        for (var entry : ModItems.ITEMS.getEntries()) {
            var id = entry.getId();
            String path = id.getPath();
            JsonObject definition = model(path);
            if (path.equals("shaker")) {
                definition = new JsonObject();
                definition.addProperty("type", "minecraft:select");
                definition.addProperty("property", "minecraft:display_context");
                var cases = new JsonArray();
                var entryCase = new JsonObject();
                var contexts = new JsonArray();
                contexts.add("gui");
                contexts.add("fixed");
                entryCase.add("when", contexts);
                entryCase.add("model", model(path));
                cases.add(entryCase);
                definition.add("cases", cases);
                definition.add("fallback", model("shaker_3d"));
            } else if (path.equals("signature_cocktail")) {
                var tints = new JsonArray();
                var white = new JsonObject();
                white.addProperty("type", "minecraft:constant");
                white.addProperty("value", -1);
                var color = new JsonObject();
                color.addProperty("type", "kaleidoscope_tavern:signature_cocktail");
                tints.add(white);
                tints.add(color);
                definition.add("tints", tints);
            }
            var root = new JsonObject();
            root.add("model", definition);
            futures.add(DataProvider.saveStable(cache, root, items.json(id)));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Kaleidoscope Tavern item definitions";
    }
}
