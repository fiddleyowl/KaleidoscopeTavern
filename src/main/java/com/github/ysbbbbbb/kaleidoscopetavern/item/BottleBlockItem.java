package com.github.ysbbbbbb.kaleidoscopetavern.item;

import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.IBarrel;
import com.github.ysbbbbbb.kaleidoscopetavern.datamap.DrinkEffectResolver;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModDataComponents;
import com.github.ysbbbbbb.kaleidoscopetavern.util.ColorUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;
import java.util.function.Consumer;

public class BottleBlockItem extends BlockItem {
    public BottleBlockItem(Identifier id, Block block) {
        this(block, new Properties().useBlockDescriptionPrefix()
                .stacksTo(16)
                .setId(ResourceKey.create(Registries.ITEM, id)));
    }

    public BottleBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static ItemStack getMaxLevelDrink(DeferredItem<Item> item) {
        ItemStack stack = item.get().getDefaultInstance();
        setBrewLevel(stack, IBarrel.BREWING_FINISHED);
        return stack;
    }

    public static void setBrewLevel(ItemStack stack, int brewLevel) {
        stack.set(ModDataComponents.BREW_LEVEL, clampBrewLevel(brewLevel));
    }

    public static int getBrewLevel(ItemStack stack) {
        int brewLevel = stack.getOrDefault(ModDataComponents.BREW_LEVEL, 0);
        int clampedBrewLevel = clampBrewLevel(brewLevel);
        if (brewLevel != clampedBrewLevel) {
            stack.set(ModDataComponents.BREW_LEVEL, clampedBrewLevel);
        }
        return clampedBrewLevel;
    }

    public static int clampBrewLevel(int brewLevel) {
        return Math.clamp(brewLevel, IBarrel.BREWING_NOT_STARTED, IBarrel.BREWING_FINISHED);
    }

    /**
     * 判断酒瓶物品是否满足调酒品质要求（brewLevel >= {@link #MIN_BREW_LEVEL_FOR_SHAKER}）。
     * 非 BottleBlockItem 的物品直接返回 true（药水等其他原料不受此限制）。
     */
    public static final int MIN_BREW_LEVEL_FOR_SHAKER = 4;

    public static boolean isValidForShaker(ItemStack stack) {
        if (!(stack.getItem() instanceof BottleBlockItem)) {
            return true;
        }
        return getBrewLevel(stack) >= MIN_BREW_LEVEL_FOR_SHAKER;
    }

    public ItemStack getFilledStack(int brewLevel) {
        ItemStack stack = new ItemStack(this);
        setBrewLevel(stack, brewLevel);
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        ChatFormatting applied = ColorUtils.ITEM_COLOR_CACHE.apply(stack.getItem());
        if (applied != ChatFormatting.RESET) {
            builder.accept(Component.translatable("color.kaleidoscope_tavern.prefix")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable("color.kaleidoscope_tavern.%s".formatted(applied.getName())).withStyle(applied)));
        }
        int brewLevel = getBrewLevel(stack);
        if (0 < brewLevel) {
            Component brewLevelText = Component.translatable("message.kaleidoscope_tavern.barrel.brew_level.%d".formatted(brewLevel));
            builder.accept(Component.translatable("tooltip.kaleidoscope_tavern.bottle_block.brew_level", brewLevelText).withStyle(ChatFormatting.GRAY));

            List<MobEffectInstance> effectsShow = DrinkEffectResolver.guaranteedInstances(
                    DrinkEffectResolver.entriesFor(context.registries(), stack, brewLevel)
            );

            if (!effectsShow.isEmpty()) {
                PotionContents.addPotionTooltip(effectsShow, builder, 1.0F, context.tickRate());
            }
        }
    }
}
