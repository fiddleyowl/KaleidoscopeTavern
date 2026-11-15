package com.github.ysbbbbbb.kaleidoscopetavern.item;

import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.resources.Identifier;
import com.github.ysbbbbbb.kaleidoscopetavern.datamap.data.DrinkEffectData;
import com.github.ysbbbbbb.kaleidoscopetavern.datamap.DrinkEffectResolver;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.google.common.collect.Lists;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class CocktailBlockItem extends GlasswareBlockItem implements IHasContainer {
    public CocktailBlockItem(Identifier id, Block block) {
        super(id, block);
    }

    public CocktailBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.DRINK;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();

        // 只有潜行时才放置
        if (player == null || player.isShiftKeyDown()) {
            return this.place(new BlockPlaceContext(context));
        }

        // 否则尝试喝下去
        Level level = context.getLevel();
        InteractionResult result = this.use(level, player, context.getHand());
        return result;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
            serverPlayer.awardStat(Stats.ITEM_USED.get(this));
        }
        this.addDrinkEffect(stack, level, entity);
        if (entity instanceof Player player && !player.isCreative()) {
            stack.shrink(1);
        }
        return returnContainerToEntity(stack, level, entity);
    }

    protected void addDrinkEffect(ItemStack drink, Level level, LivingEntity entity) {
        var effects = DrinkEffectResolver.entriesFor(level.registryAccess(), drink, 0);
        for (DrinkEffectData.Entry entry : effects) {
            if (!level.isClientSide() && level.getRandom().nextFloat() < entry.probability()) {
                MobEffect effect = entry.effect().value();
                int amplifier = entry.amplifier();
                if (effect.isInstantenous()) {
                    // 瞬时效果直接触发，不通过 addEffect
                    effect.applyInstantenousEffect((net.minecraft.server.level.ServerLevel) level, entity, entity, entity, amplifier, 1.0);
                } else {
                    // json 里的持续时间是秒，但是内部游戏是 tick，需要转化
                    int duration = entry.duration() * 20;
                    MobEffectInstance instance = new MobEffectInstance(entry.effect(), duration, amplifier);
                    entity.addEffect(instance);
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        if (context.registries() == null) return;
        var effectData = DrinkEffectResolver.entriesFor(context.registries(), stack, 0);
        List<MobEffectInstance> effectsShow = Lists.newArrayList();
        for (DrinkEffectData.Entry entry : effectData) {
            if (entry.probability() >= 1.0F) {
                int duration = entry.duration() * 20;
                int amplifier = entry.amplifier();
                effectsShow.add(new MobEffectInstance(entry.effect(), duration, amplifier));
            }
        }

        if (!effectsShow.isEmpty()) {
            tooltip.accept(CommonComponents.space());
            PotionContents.addPotionTooltip(effectsShow, tooltip, 1.0F, context.tickRate());
        }
    }

    @Override
    public Item getContainerItem() {
        return ModItems.EMPTY_GLASSWARE.get();
    }
}
