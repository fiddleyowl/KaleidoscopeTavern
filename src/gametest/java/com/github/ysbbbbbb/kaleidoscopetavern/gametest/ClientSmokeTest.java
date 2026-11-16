package com.github.ysbbbbbb.kaleidoscopetavern.gametest;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModEffects;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModEntities;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModFluids;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModCreativeTabs;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Opt-in client loading check. It closes only the development game it starts.
 */
@EventBusSubscriber(modid = KaleidoscopeTavern.MOD_ID, value = Dist.CLIENT)
public final class ClientSmokeTest {
    private static int titleTicks;
    private static boolean complete;
    private static boolean worldStarted;
    private static int worldTicks;
    private static int totalTicks;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("kaleidoscope_tavern.client_smoke") || complete) return;
        Minecraft client = Minecraft.getInstance();
        if (++totalTicks > 1200)
            throw new IllegalStateException("Client smoke timed out: screen=" + client.screen + ", overlay=" + client.getOverlay() + ", worldStarted=" + worldStarted);
        if (totalTicks % 100 == 0)
            KaleidoscopeTavern.LOGGER.info("CLIENT_SMOKE_WAIT: screen={}, overlay={}, worldStarted={}",
                    client.screen == null ? "null" : client.screen.getClass().getName(), client.getOverlay(), worldStarted);
        if (client.screen instanceof net.neoforged.neoforge.client.gui.LoadingErrorScreen warningScreen) {
            if (net.neoforged.fml.ModLoader.hasErrors()) {
                throw new IllegalStateException("Client mod loading errors: " + net.neoforged.fml.ModLoader.getLoadingErrors());
            }
            String continueLabel = net.neoforged.fml.i18n.FMLTranslations.parseMessage("fml.button.continue.launch");
            for (var child : warningScreen.children()) {
                if (child instanceof net.minecraft.client.gui.components.AbstractButton button
                        && button.getMessage().getString().equals(continueLabel)) {
                    KaleidoscopeTavern.LOGGER.warn("CLIENT_SMOKE: continuing past dependency warnings: {}", net.neoforged.fml.ModLoader.getLoadingWarnings());
                    button.onPress(new net.minecraft.client.input.KeyEvent(257, 0, 0));
                    return;
                }
            }
        }
        if (!worldStarted) {
            if (!(client.screen instanceof TitleScreen) || client.getOverlay() != null || ++titleTicks < 40) return;
            worldStarted = true;
            client.createWorldOpenFlows().createFreshLevel(
                    "_kaleidoscope_port_smoke_" + System.currentTimeMillis(),
                    new net.minecraft.world.level.LevelSettings("Port validation", net.minecraft.world.level.GameType.CREATIVE,
                            new net.minecraft.world.level.LevelSettings.DifficultySettings(net.minecraft.world.Difficulty.PEACEFUL, false, false),
                            true, net.minecraft.world.level.WorldDataConfiguration.DEFAULT),
                    new net.minecraft.world.level.levelgen.WorldOptions(26L, false, false),
                    net.minecraft.world.level.levelgen.presets.WorldPresets::createFlatWorldDimensions,
                    client.screen);
            return;
        }
        if (client.level != null && client.player != null && worldTicks == 20) ClientSceneSetup.populate(client);
        if (client.level == null || client.player == null || client.getOverlay() != null || ++worldTicks < 100) return;
        if (com.github.ysbbbbbb.kaleidoscopetavern.client.init.ClientRecipeEvent.SHAKER_RECIPES.size() != 12) {
            throw new IllegalStateException("Client did not receive all 12 shaker recipes");
        }
        if (net.neoforged.fml.ModList.get().isLoaded("jei") && !JeiSmokeObserver.ready()) return;
        if (net.neoforged.fml.ModList.get().isLoaded("roughlyenoughitems") && !ReiSmokeObserver.ready()) return;
        int count = 0;
        for (var item : ModItems.ITEMS.getEntries()) {
            String key = item.get().getDescriptionId();
            if (!net.minecraft.client.resources.language.I18n.exists(key)
                    || !new ItemStack(item.get()).getHoverName().getString().equals(net.minecraft.client.resources.language.I18n.get(key))) {
                throw new IllegalStateException("Unresolved item display name: " + item.getId() + " / " + key);
            }
            checkTooltip(client, new ItemStack(item.get()));
            if (item.get() instanceof BottleBlockItem bottle) {
                for (int brewLevel = 1; brewLevel <= 6; brewLevel++) checkTooltip(client, bottle.getFilledStack(brewLevel));
            }
            for (var context : ItemDisplayContext.values()) {
                if (context == ItemDisplayContext.NONE) continue;
                var state = new ItemStackRenderState();
                client.getItemModelResolver().updateForTopItem(state, new ItemStack(item.get()), context, client.level, client.player, 0);
                if (state.isEmpty())
                    throw new IllegalStateException("Empty item model: " + item.getId() + " / " + context);
            }
            count++;
        }
        ModEffects.EFFECTS.getEntries().forEach(entry -> assertTranslated(Component.translatable(entry.get().getDescriptionId()), entry.getId().toString()));
        ModEntities.ENTITY_TYPES.getEntries().forEach(entry -> assertTranslated(Component.translatable(entry.get().getDescriptionId()), entry.getId().toString()));
        ModFluids.FLUID_TYPES.getEntries().forEach(entry -> assertTranslated(Component.translatable(entry.get().getDescriptionId()), entry.getId().toString()));
        ModCreativeTabs.TABS.getEntries().forEach(entry -> assertTranslated(entry.get().getDisplayName(), entry.getId().toString()));
        complete = true;
        KaleidoscopeTavern.LOGGER.info("CLIENT_TRANSLATIONS_PASS: item tooltips, brew levels 1-6, effects, entities, fluids and creative tabs");
        KaleidoscopeTavern.LOGGER.info("CLIENT_SMOKE_PASS: {} registered items resolved in every display context", count);
        client.stop();
    }

    private static void checkTooltip(Minecraft client, ItemStack stack) {
        for (Component line : stack.getTooltipLines(Item.TooltipContext.of(client.level), client.player, TooltipFlag.NORMAL)) {
            assertTranslated(line, stack.toString());
        }
    }

    static void assertTranslated(Component component, String context) {
        if (component.getContents() instanceof TranslatableContents translation) {
            String key = translation.getKey();
            if (key.contains(KaleidoscopeTavern.MOD_ID) && !I18n.exists(key)) {
                throw new IllegalStateException("Unresolved translation: " + context + " / " + key);
            }
            for (Object argument : translation.getArgs()) {
                if (argument instanceof Component nested) assertTranslated(nested, context);
            }
        }
        component.getSiblings().forEach(sibling -> assertTranslated(sibling, context));
    }
}
