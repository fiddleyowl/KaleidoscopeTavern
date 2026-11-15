package com.github.ysbbbbbb.kaleidoscopetavern.item;

import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;


public class GrapevineItem extends BlockItem {
    public GrapevineItem(Identifier id) {
        super(ModBlocks.WILD_GRAPEVINE.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.kaleidoscope_tavern.grapevine.1").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.kaleidoscope_tavern.grapevine.2").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.kaleidoscope_tavern.grapevine.3").withStyle(ChatFormatting.GRAY));
    }
}
