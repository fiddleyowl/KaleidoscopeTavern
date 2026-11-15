package com.github.ysbbbbbb.kaleidoscopetavern.item;

import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;


public class TooltipItem extends Item {
    private final String[] tooltipKeys;

    public TooltipItem(Properties properties, String... tooltipKeys) {
        super(properties);
        this.tooltipKeys = tooltipKeys;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        for (String tooltipKey : this.tooltipKeys) {
            tooltip.accept(Component.translatable(tooltipKey).withStyle(ChatFormatting.GRAY));
        }
    }
}
