package com.github.ysbbbbbb.kaleidoscopetavern.gametest;

import com.github.ysbbbbbb.kaleidoscopetavern.crafting.serializer.BarrelRecipeSerializer;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.serializer.PressingTubRecipeSerializer;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.serializer.ShakerRecipeSerializer;
import com.github.ysbbbbbb.kaleidoscopetavern.datamap.DrinkEffectResolver;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.ShakerItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.SignatureCocktailBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.network.message.ClearShakerC2SMessage;
import com.github.ysbbbbbb.kaleidoscopetavern.network.message.TextOpenS2CMessage;
import com.github.ysbbbbbb.kaleidoscopetavern.network.message.TextUpdateC2SMessage;
import com.github.ysbbbbbb.kaleidoscopetavern.util.TextAlignment;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.List;
import java.util.stream.Collectors;

final class NetworkGameTests {
    static void roundTrips(GameTestHelper h) {
        ItemStack wine = ((BottleBlockItem) ModItems.WINE.get()).getFilledStack(4);
        ItemStack cocktail = new ItemStack(ModItems.SIGNATURE_COCKTAIL.get());
        SignatureCocktailBlockItem.setColor(cocktail, 0x42C1AB);
        SignatureCocktailBlockItem.setEffects(cocktail,
                DrinkEffectResolver.entriesFor(h.getLevel().registryAccess(), ModItems.BLOODY_MARY.get(), 0));
        ItemStack shaker = new ItemStack(ModItems.SHAKER.get());
        ItemStackHandler storage = new ItemStackHandler(3);
        storage.setStackInSlot(0, wine);
        ShakerItem.setStorage(shaker, storage);
        ShakerItem.setResult(shaker, cocktail);
        var ops = RegistryOps.create(JsonOps.INSTANCE, h.getLevel().registryAccess());
        for (var stack : List.of(wine, cocktail, shaker)) {
            var decoded = roundTrip(h, ItemStack.STREAM_CODEC, stack);
            // 上游 Result 包装 ItemStack，默认 equals 比较对象身份；这里核对完整组件数据。
            h.assertTrue(ItemStack.CODEC.encodeStart(ops, stack).getOrThrow()
                            .equals(ItemStack.CODEC.encodeStart(ops, decoded).getOrThrow()),
                    "Item components changed over the network: " + stack);
        }

        var recipes = h.getLevel().recipeAccess().recipeMap();
        for (var holder : recipes.byType(ModRecipes.BARREL_RECIPE.get())) {
            var before = holder.value();
            var after = roundTrip(h, BarrelRecipeSerializer.STREAM_CODEC, before);
            ingredients(h, before.ingredients(), after.ingredients());
            ingredients(h, List.of(before.carrier()), List.of(after.carrier()));
            h.assertTrue(before.fluid() == after.fluid() && before.unitTime() == after.unitTime()
                    && ItemStack.matches(before.result().create(), after.result().create()), "Barrel network data: " + holder.id());
        }
        for (var holder : recipes.byType(ModRecipes.PRESSING_TUB_RECIPE.get())) {
            var before = holder.value();
            var after = roundTrip(h, PressingTubRecipeSerializer.STREAM_CODEC, before);
            ingredients(h, List.of(before.input()), List.of(after.input()));
            h.assertTrue(before.getFluid() == after.getFluid() && before.getFluidAmount() == after.getFluidAmount(),
                    "Pressing tub network data: " + holder.id());
        }
        for (var holder : recipes.byType(ModRecipes.SHAKER_RECIPE.get())) {
            var before = holder.value();
            var after = roundTrip(h, ShakerRecipeSerializer.STREAM_CODEC, before);
            ingredients(h, before.getIngredients(), after.getIngredients());
            h.assertTrue(before.ingredientColors().equals(after.ingredientColors())
                    && ItemStack.matches(before.result().create(), after.result().create()), "Shaker network data: " + holder.id());
        }

        var pos = new BlockPos(12, 64, -8);
        var update = new TextUpdateC2SMessage(pos, "酒馆测试\nTavern test", TextAlignment.RIGHT);
        h.assertTrue(update.equals(roundTrip(h, TextUpdateC2SMessage.STREAM_CODEC, update)), "Text update network data");
        var open = new TextOpenS2CMessage(pos);
        h.assertTrue(open.equals(roundTrip(h, TextOpenS2CMessage.STREAM_CODEC, open)), "Text open network data");
        var clear = new ClearShakerC2SMessage();
        h.assertTrue(clear.equals(roundTrip(h, ClearShakerC2SMessage.STREAM_CODEC, clear)), "Clear shaker network data");
        h.succeed();
    }

    private static void ingredients(GameTestHelper h, List<Ingredient> before, List<Ingredient> after) {
        h.assertTrue(before.size() == after.size(), "Network ingredient count changed");
        for (int i = 0; i < before.size(); i++) {
            var expected = before.get(i).items().map(holder -> holder.value()).collect(Collectors.toSet());
            var actual = after.get(i).items().map(holder -> holder.value()).collect(Collectors.toSet());
            h.assertTrue(expected.equals(actual), "Network ingredient candidates changed at " + i);
        }
    }

    private static <T> T roundTrip(GameTestHelper h, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, T value) {
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), h.getLevel().registryAccess());
        try {
            codec.encode(buffer, value);
            T decoded = codec.decode(buffer);
            h.assertTrue(buffer.readableBytes() == 0, "Unread network payload bytes");
            return decoded;
        } finally {
            buffer.release();
        }
    }
}
