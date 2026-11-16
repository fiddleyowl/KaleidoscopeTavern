package com.github.ysbbbbbb.kaleidoscopetavern.gametest;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.PressingTubBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.container.SimpleInput;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModEffects;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.DrinkBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.IHasContainer;
import com.github.ysbbbbbb.kaleidoscopetavern.item.JuiceBucketItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.ShakerItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.StringLightsBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;

/** Exercises vanilla interaction paths that registry and serialization checks do not cover. */
final class BehaviorGameTests {
    static void ingredientReturnOverflow(GameTestHelper h) {
        var level = h.getLevel();
        var pos = h.absolutePos(new BlockPos(1, 1, 1));
        level.setBlockAndUpdate(pos, ModBlocks.PRESSING_TUB.get().defaultBlockState());
        var tub = (PressingTubBlockEntity) level.getBlockEntity(pos);
        for (int available : new int[]{8, 4, 0}) {
            var player = h.makeMockPlayer(GameType.SURVIVAL);
            player.setPos(h.absolutePos(new BlockPos(2, 1, 2)).getBottomCenter());
            for (int slot = 0; slot < player.getInventory().getNonEquipmentItems().size(); slot++) {
                player.getInventory().setItem(slot, new ItemStack(Items.STONE, 64));
            }
            if (available == 8) player.getInventory().setItem(10, ItemStack.EMPTY);
            if (available == 4) player.getInventory().setItem(10, new ItemStack(ModItems.GRAPE.get(), 60));
            int before = player.getInventory().countItem(ModItems.GRAPE.get());
            h.assertTrue(tub.addIngredient(new ItemStack(ModItems.GRAPE.get(), 8)), "Could not add grapes for return test");
            h.assertTrue(tub.removeIngredient(player, 8), "Could not return grapes");
            int stored = player.getInventory().countItem(ModItems.GRAPE.get()) - before;
            var drops = level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(2),
                    entity -> entity.getItem().is(ModItems.GRAPE));
            int dropped = drops.stream().mapToInt(entity -> entity.getItem().getCount()).sum();
            h.assertTrue(stored == available && dropped == 8 - available,
                    "Returned grapes lost: space=" + available + ", stored=" + stored + ", dropped=" + dropped);
            drops.forEach(ItemEntity::discard);
        }
        h.succeed();
    }

    static void fuelAndComposting(GameTestHelper h) {
        var fuel = new ItemStack(ModItems.GRAPEVINE.get());
        h.assertTrue(fuel.getBurnTime(RecipeType.SMELTING, h.getLevel().fuelValues()) == 200,
                "Grapevine furnace fuel duration differs from upstream");
        for (var entry : java.util.Map.of(ModItems.GRAPEVINE, 0.25F, ModItems.GRAPE, 0.5F,
                ModItems.ICE_GRAPE, 0.5F, ModItems.GOLD_GRAPE, 0.5F, ModItems.GREEN_GRAPE, 0.5F).entrySet()) {
            float actual = net.minecraft.world.level.block.ComposterBlock.getValue(new ItemStack(entry.getKey().get()));
            h.assertTrue(Math.abs(actual - entry.getValue()) < 0.0001F,
                    "Composting chance differs from upstream: " + entry.getKey().getId() + " / " + actual);
        }
        h.succeed();
    }

    static void stringLightsEquipment(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        for (var entry : ModItems.ITEMS.getEntries()) {
            if (!(entry.get() instanceof StringLightsBlockItem)) continue;
            ItemStack stack = new ItemStack(entry.get());
            h.assertValueEqual(player.getEquipmentSlotForItem(stack), EquipmentSlot.CHEST, "Lights equipment slot");
            boolean accepted = player.inventoryMenu.getSlot(6).mayPlace(stack);
            h.assertTrue(accepted, "Chest slot rejected string lights: " + entry.getId());
            h.assertTrue(!player.inventoryMenu.getSlot(5).mayPlace(stack), "Head slot accepted string lights");
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            h.assertTrue(!stack.use(h.getLevel(), player, InteractionHand.MAIN_HAND).consumesAction(),
                    "Lights unexpectedly auto-equipped on right click");
        }
        h.succeed();
    }

    static void pressingTubCapabilities(GameTestHelper h) {
        var level = h.getLevel();
        var pos = h.absolutePos(new BlockPos(1, 1, 1));
        level.setBlockAndUpdate(pos, ModBlocks.PRESSING_TUB.get().defaultBlockState());
        var unsided = level.getCapability(Capabilities.Item.BLOCK, pos, null);
        h.assertTrue(unsided != null, "Pressing tub rejected unsided item capability lookup");
        h.assertTrue(level.getCapability(Capabilities.Item.BLOCK, pos, Direction.NORTH) != null, "Missing side access");
        h.assertTrue(level.getCapability(Capabilities.Item.BLOCK, pos, Direction.UP) == null, "Unexpected top access");
        h.assertTrue(level.getCapability(Capabilities.Item.BLOCK, pos, Direction.DOWN) == null, "Unexpected bottom access");
        h.assertTrue(level.getCapability(Capabilities.Fluid.BLOCK, pos, null) != null, "Missing fluid access");

        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var tub = (PressingTubBlockEntity) level.getBlockEntity(pos);
        ItemStack grapes = new ItemStack(ModItems.GRAPE.get(), 8);
        h.assertTrue(tub.addIngredient(grapes), "Pressing tub rejected grapes");
        for (int i = 0; i < 8; i++) h.assertTrue(tub.press(player, 1), "Grape pressing failed at " + i);
        h.assertValueEqual(tub.getFluidAmount(), 1000, "Pressed fluid amount");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
        h.assertTrue(tub.getResult(player, ItemAccess.forPlayerInteraction(player, InteractionHand.MAIN_HAND)), "Bucket filling failed");
        h.assertTrue(player.getMainHandItem().is(ModItems.GRAPE_BUCKET), "Wrong filled bucket");
        h.assertValueEqual(tub.getFluidAmount(), 0, "Fluid duplicated while filling bucket");
        h.succeed();
    }

    static void drinkContainers(GameTestHelper h) {
        for (var entry : ModItems.ITEMS.getEntries()) {
            var item = entry.get();
            if (!(item instanceof DrinkBlockItem) && !(item instanceof JuiceBucketItem)) continue;
            var container = ((IHasContainer) item).getContainerItem();
            for (int count : new int[]{1, 2}) {
                var player = h.makeMockPlayer(GameType.SURVIVAL);
                ItemStack stack = item instanceof DrinkBlockItem
                        ? ((BottleBlockItem) item).getFilledStack(3) : new ItemStack(item);
                stack.setCount(count);
                player.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
                player.setItemInHand(InteractionHand.MAIN_HAND, stack);
                stack.use(h.getLevel(), player, InteractionHand.MAIN_HAND);
                for (int tick = 0; tick < 32; tick++) player.tick();
                int returned = player.getInventory().countItem(container);
                h.assertValueEqual(returned, 1, "Returned container count: " + entry.getId() + " x" + count);
                h.assertValueEqual(player.getInventory().countItem(item), count - 1, "Consumed drink count: " + entry.getId());
                if (item instanceof JuiceBucketItem) h.assertTrue(!player.hasEffect(MobEffects.POISON), "Juice did not cure poison");
            }
        }
        h.succeed();
    }

    static void shakerTiming(GameTestHelper h) {
        var level = h.getLevel();
        var recipe = level.recipeAccess().recipeMap().byType(ModRecipes.SHAKER_RECIPE.get()).stream()
                .filter(holder -> holder.value().assemble(new SimpleInput(java.util.List.of())).is(ModItems.ALLIUM_GARDEN))
                .findFirst().orElseThrow();
        ItemStackHandler ingredients = new ItemStackHandler(3);
        for (int i = 0; i < 3; i++) {
            var ingredient = recipe.value().ingredients().get(i).items().findFirst().orElseThrow().value();
            ItemStack stack = ingredient instanceof BottleBlockItem bottle ? bottle.getFilledStack(3) : new ItemStack(ingredient);
            ingredients.setStackInSlot(i, stack);
        }
        for (int ticks : new int[]{18, 19, 68, 69, 88, 89, 98, 99, 111}) {
            var player = h.makeMockPlayer(GameType.SURVIVAL);
            player.setNoGravity(true);
            player.setPos(h.absolutePos(new BlockPos(2, 1, 2)).getBottomCenter());
            ItemStack stack = new ItemStack(ModItems.SHAKER.get());
            ShakerItem.setStorage(stack, ingredients);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            stack.use(level, player, InteractionHand.MAIN_HAND);
            for (int tick = 0; tick < ticks; tick++) player.tick();
            player.releaseUsingItem();
            ItemStack result = ShakerItem.getResult(stack);
            if (ticks < 19) h.assertTrue(result.isEmpty(), "Early shaker result");
            else {
                var expected = ticks < 69 || ticks >= 99 ? ModItems.MYSTERY_COCKTAIL
                        : ticks < 89 ? ModItems.SIGNATURE_COCKTAIL : ModItems.ALLIUM_GARDEN;
                h.assertTrue(result.is(expected), "Wrong shaker result at " + ticks + " ticks: " + result);
            }
        }
        h.succeed();
    }

    static void bottleInteraction(GameTestHelper h) {
        var level = h.getLevel();
        var relativePos = new BlockPos(1, 1, 1);
        var pos = h.absolutePos(relativePos);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        level.setBlockAndUpdate(pos, ModBlocks.EMPTY_BOTTLE.get().defaultBlockState());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE, 2));
        h.useBlock(relativePos, player, hit);
        h.assertTrue(level.getBlockState(pos).is(ModBlocks.EMPTY_BOTTLE), "Held block picked up the bottle");
        h.assertTrue(level.getBlockState(pos.above()).is(Blocks.STONE), "Held block was not placed");

        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        h.useBlock(relativePos, player, hit);
        h.assertTrue(level.getBlockState(pos).isAir(), "Empty-hand bottle pickup failed");
        h.assertValueEqual(player.getInventory().countItem(ModItems.EMPTY_BOTTLE.get()), 1, "Picked-up bottle count");

        level.setBlockAndUpdate(pos, ModBlocks.EMPTY_BOTTLE.get().defaultBlockState());
        var adventurePlayer = h.makeMockPlayer(GameType.ADVENTURE);
        GameType.ADVENTURE.updatePlayerAbilities(adventurePlayer.getAbilities());
        h.useBlock(relativePos, adventurePlayer, hit);
        h.assertTrue(level.getBlockState(pos).is(ModBlocks.EMPTY_BOTTLE), "Adventure mode picked up the bottle");
        h.succeed();
    }

    static void effectAttributes(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        double blockRange = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        double entityRange = player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
        double stepHeight = player.getAttributeValue(Attributes.STEP_HEIGHT);
        player.addEffect(new MobEffectInstance(ModEffects.LONG_REACH, 100));
        player.addEffect(new MobEffectInstance(ModEffects.HIGH_HEELS, 100));
        h.assertTrue(Math.abs(player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) - blockRange - 3) < 0.0001, "Block range modifier");
        h.assertTrue(Math.abs(player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE) - entityRange - 3) < 0.0001, "Entity range modifier");
        h.assertTrue(Math.abs(player.getAttributeValue(Attributes.STEP_HEIGHT) - stepHeight - 0.5) < 0.0001, "Step height modifier");
        player.removeEffect(ModEffects.LONG_REACH);
        player.removeEffect(ModEffects.HIGH_HEELS);
        h.assertTrue(Math.abs(player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) - blockRange) < 0.0001, "Block range not restored");
        h.assertTrue(Math.abs(player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE) - entityRange) < 0.0001, "Entity range not restored");
        h.assertTrue(Math.abs(player.getAttributeValue(Attributes.STEP_HEIGHT) - stepHeight) < 0.0001, "Step height not restored");
        h.succeed();
    }
}
