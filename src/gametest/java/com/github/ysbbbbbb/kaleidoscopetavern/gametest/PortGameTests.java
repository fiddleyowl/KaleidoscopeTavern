package com.github.ysbbbbbb.kaleidoscopetavern.gametest;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.PotionBottleBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.mixology.ShakerBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.container.SimpleInput;
import com.github.ysbbbbbb.kaleidoscopetavern.datamap.DrinkEffectResolver;
import com.github.ysbbbbbb.kaleidoscopetavern.init.*;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.CocktailBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.ShakerItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.SignatureCocktailBlockItem;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Comparator;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Development-only server regression tests; this source set is excluded from published jars.
 */
@EventBusSubscriber(modid = KaleidoscopeTavern.MOD_ID)
public final class PortGameTests {
    private static final Map<String, Consumer<GameTestHelper>> TESTS = Map.of(
            "content_and_blockstates", PortGameTests::content,
            "shaker_and_component_roundtrip", PortGameTests::shaker,
            "cocktail_drinking_sound", PortGameTests::cocktailDrinkingSound,
            "barrel_item_carrier", h -> barrel(h, false),
            "barrel_placed_carrier", h -> barrel(h, true));

    @SubscribeEvent
    public static void registerFunctions(net.neoforged.neoforge.registries.RegisterEvent event) {
        if (!Boolean.getBoolean("kaleidoscope_tavern.port_tests")) return;
        event.register(Registries.TEST_FUNCTION, helper -> TESTS.forEach((name, fn) -> helper.register(id(name), fn)));
    }

    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event) {
        if (!Boolean.getBoolean("kaleidoscope_tavern.port_tests")) return;
        var environment = event.registerEnvironment(id("port_tests"));
        TESTS.forEach((name, fn) -> event.registerTest(id(name), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, id(name)),
                new TestData<>(environment, id("port_test"), 40, 0, true))));
    }

    private static Identifier id(String path) {
        return KaleidoscopeTavern.modLoc(path);
    }

    private static long count(Registry<?> registry) {
        return registry.keySet().stream().filter(id -> id.getNamespace().equals(KaleidoscopeTavern.MOD_ID)).count();
    }

    private static void content(GameTestHelper h) {
        h.assertValueEqual(count(BuiltInRegistries.BLOCK), 159L, "block count");
        h.assertValueEqual(count(BuiltInRegistries.ITEM), 157L, "item count");
        h.assertValueEqual(count(BuiltInRegistries.MOB_EFFECT), 12L, "effect count");
        h.assertValueEqual(h.getLevel().registryAccess().lookupOrThrow(ModDatapackRegistries.DRINK_EFFECT).listElements().count(), 37L, "drink effect definitions");
        for (var entry : ModBlocks.BLOCKS.getEntries()) {
            Block block = entry.get();
            String resource = "assets/kaleidoscope_tavern/blockstates/" + entry.getId().getPath() + ".json";
            try (var input = PortGameTests.class.getClassLoader().getResourceAsStream(resource)) {
                h.assertTrue(input != null, "Missing blockstate: " + resource);
                var json = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
                if (json.has("variants")) {
                    for (String variant : json.getAsJsonObject("variants").keySet()) {
                        if (variant.isEmpty()) continue;
                        for (String condition : variant.split(",")) {
                            String[] pair = condition.split("=");
                            var property = block.getStateDefinition().getProperty(pair[0]);
                            h.assertTrue(property != null, entry.getId() + " has no property " + pair[0]);
                            h.assertTrue(property.getValue(pair[1]).isPresent(), entry.getId() + " invalid value " + condition);
                        }
                    }
                }
            } catch (Exception e) {
                throw new IllegalStateException(resource, e);
            }
            // Every registered entity block must survive the new ValueInput/ValueOutput format.
            if (block instanceof net.minecraft.world.level.block.EntityBlock entityBlock) {
                var state = block.defaultBlockState();
                BlockEntity entity = entityBlock.newBlockEntity(BlockPos.ZERO, state);
                if (entity != null) {
                    ItemStack wine = ((BottleBlockItem) ModItems.WINE.get()).getFilledStack(3);
                    if (entity instanceof com.github.ysbbbbbb.kaleidoscopetavern.blockentity.deco.StorageBlockEntity storage) {
                        storage.getItems().setStackInSlot(0, wine);
                    } else if (entity instanceof com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarCabinetBlockEntity cabinet) {
                        cabinet.setLeftItem(wine);
                        cabinet.setSingle(true);
                    } else if (entity instanceof PotionBottleBlockEntity bottle) {
                        bottle.setPotionStack(new ItemStack(Items.POTION));
                    } else if (entity instanceof ShakerBlockEntity shaker) {
                        shaker.getStorage().setStackInSlot(0, wine);
                    } else if (entity instanceof com.github.ysbbbbbb.kaleidoscopetavern.blockentity.mixology.SignatureCocktailBlockEntity cocktail) {
                        cocktail.setColor(0x42C1AB);
                        cocktail.setEffects(DrinkEffectResolver.entriesFor(h.getLevel().registryAccess(), ModItems.BLOODY_MARY.get(), 0));
                    }
                    var problems = new ProblemReporter.Collector();
                    var output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(problems, h.getLevel().registryAccess());
                    entity.saveWithFullMetadata(output);
                    h.assertTrue(problems.isEmpty(), entry.getId() + " serialization errors: " + problems.getReport());
                    CompoundTag saved = output.buildResult();
                    var restored = BlockEntity.loadStatic(BlockPos.ZERO, state, saved, h.getLevel().registryAccess());
                    h.assertTrue(restored != null, "Block entity failed to reload: " + entry.getId());
                    h.assertValueEqual(restored.saveWithFullMetadata(h.getLevel().registryAccess()), saved,
                            "Stored block entity data: " + entry.getId());
                }
            }
        }
        h.succeed();
    }

    private static void shaker(GameTestHelper h) {
        var level = h.getLevel();
        var recipes = level.recipeAccess().recipeMap().byType(ModRecipes.SHAKER_RECIPE.get());
        h.assertValueEqual(recipes.size(), 12, "shaker recipes");
        for (var holder : recipes) {
            var ingredients = holder.value().ingredients().stream()
                    .map(i -> new ItemStack(i.items().findFirst().orElseThrow())).toList();
            var input = new SimpleInput(ingredients);
            h.assertTrue(holder.value().matches(input, level), "Shaker recipe does not match its inputs: " + holder.id());
            h.assertTrue(!holder.value().assemble(input).isEmpty(), "Shaker result missing: " + holder.id());
        }
        var effects = DrinkEffectResolver.entriesFor(level.registryAccess(), ModItems.BLOODY_MARY.get(), 0);
        h.assertTrue(!effects.isEmpty(), "Cocktail effects unavailable");
        ItemStack cocktail = new ItemStack(ModItems.SIGNATURE_COCKTAIL.get());
        SignatureCocktailBlockItem.setColor(cocktail, 0x42C1AB);
        SignatureCocktailBlockItem.setEffects(cocktail, effects);
        var ops = RegistryOps.create(JsonOps.INSTANCE, level.registryAccess());
        var encoded = ItemStack.CODEC.encodeStart(ops, cocktail).getOrThrow();
        var decoded = ItemStack.CODEC.parse(ops, encoded).getOrThrow();
        h.assertValueEqual(SignatureCocktailBlockItem.getColor(decoded), 0x42C1AB, "cocktail color");
        h.assertValueEqual(SignatureCocktailBlockItem.getEffects(decoded), effects, "cocktail effects");
        ItemStack tool = new ItemStack(ModItems.SHAKER.get());
        var storage = ShakerItem.getStorage(tool);
        storage.setStackInSlot(0, ((BottleBlockItem) ModItems.WINE.get()).getFilledStack(3));
        ShakerItem.setStorage(tool, storage);
        ShakerItem.setResult(tool, cocktail);
        decoded = ItemStack.CODEC.parse(ops, ItemStack.CODEC.encodeStart(ops, tool).getOrThrow()).getOrThrow();
        h.assertTrue(ShakerItem.getStorage(decoded).getStackInSlot(0).is(ModItems.WINE), "Shaker storage lost");
        h.assertTrue(ShakerItem.getResult(decoded).is(ModItems.SIGNATURE_COCKTAIL), "Shaker result lost");
        h.succeed();
    }

    private static void barrel(GameTestHelper h, boolean placed) {
        var level = h.getLevel();
        BlockPos barrelPos = h.absolutePos(new BlockPos(1, 1, 1));
        BlockPos tapPos = h.absolutePos(new BlockPos(2, 2, 1));
        BlockPos below = tapPos.below();
        level.setBlockAndUpdate(barrelPos, ModBlocks.BARREL.get().defaultBlockState());
        var barrel = (BarrelBlockEntity) level.getBlockEntity(barrelPos);
        var data = barrel.saveCustomOnly(level.registryAccess());
        data.putInt("brew_level", 3);
        data.putString("recipe_id", "kaleidoscope_tavern_test:" + (placed ? "placed_output" : "item_output"));
        barrel.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), data));
        barrel.getOutput().set(0, ItemResource.of(Items.DIAMOND), 1);
        ItemEntity carrier = null;
        if (placed) {
            level.setBlockAndUpdate(below, Blocks.DIRT.defaultBlockState());
        } else {
            level.setBlockAndUpdate(below, Blocks.AIR.defaultBlockState());
            carrier = new ItemEntity(level, below.getX() + 0.5, below.getY() + 0.5, below.getZ() + 0.5, new ItemStack(Items.STICK, 2));
            level.addFreshEntity(carrier);
        }
        h.assertTrue(barrel.canTapExtract(level, tapPos, null), "Barrel rejected a valid carrier");
        barrel.doTapExtract(level, tapPos);
        if (placed) h.assertTrue(level.getBlockState(below).isAir(), "Placed carrier was not consumed");
        else h.assertValueEqual(carrier.getItem().getCount(), 1, "item carrier consumption");
        var drops = level.getEntitiesOfClass(ItemEntity.class, new net.minecraft.world.phys.AABB(below).inflate(0.1),
                item -> item.getItem().is(Items.DIAMOND));
        h.assertValueEqual(drops.size(), 1, "non-block output drops");
        h.assertTrue(!barrel.isBrewing(), "Empty barrel did not reset");
        h.succeed();
    }

    private static void cocktailDrinkingSound(GameTestHelper h) {
        class DrinkingPlayer extends Player {
            int drinkSounds;

            DrinkingPlayer() {
                super(h.getLevel(), new com.mojang.authlib.GameProfile(UUID.randomUUID(), "drink-test"));
            }

            @Override public GameType gameMode() { return GameType.SURVIVAL; }
            @Override public boolean isClientAuthoritative() { return false; }

            @Override
            public void playSound(SoundEvent sound, float volume, float pitch) {
                if (sound == SoundEvents.GENERIC_DRINK.value()) drinkSounds++;
            }
        }
        for (var entry : ModItems.ITEMS.getEntries().stream()
                .sorted(Comparator.comparing(item -> item.getId().toString())).toList()) {
            if (!(entry.get() instanceof CocktailBlockItem)) continue;
            var player = new DrinkingPlayer();
            player.setPos(h.absolutePos(new BlockPos(1, 1, 1)).getBottomCenter());
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(entry.get()));
            entry.get().use(h.getLevel(), player, InteractionHand.MAIN_HAND);
            h.assertTrue(player.isUsingItem(), "Cocktail did not start drinking: " + entry.getId());
            for (int tick = 0; tick < 31; tick++) player.tick();
            h.assertTrue(player.drinkSounds > 0, "No sound while drinking: " + entry.getId());
            int drinkingSounds = player.drinkSounds;
            player.tick();
            h.assertValueEqual(player.drinkSounds, drinkingSounds + 1, "Final drinking sound: " + entry.getId());
            h.assertTrue(player.getMainHandItem().is(ModItems.EMPTY_GLASSWARE), "Cocktail did not return its glass: " + entry.getId());
            h.assertTrue(!player.isUsingItem(), "Cocktail did not finish after 32 ticks: " + entry.getId());
        }
        h.succeed();
    }
}
