package com.github.ysbbbbbb.kaleidoscopetavern.gametest;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.BaseBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarCabinetBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.DrinkBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.deco.StorageBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.deco.TextBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.mixology.ShakerBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModParticles;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.ShakerItem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** Exercises the real chunk, entity-layer and particle rendering paths in a throwaway world. */
public final class ClientSceneSetup {
    public static void populate(Minecraft client) {
        var clientPlayer = client.player;
        for (var entry : ModParticles.PARTICLES.getEntries()) {
            if (entry.get() instanceof SimpleParticleType particle) {
                client.level.addParticle(particle, clientPlayer.getX(), clientPlayer.getY() + 1, clientPlayer.getZ() + 2,
                        0.01, 0.01, 0.01);
            }
        }
        var server = client.getSingleplayerServer();
        server.execute(() -> {
            var player = server.getPlayerList().getPlayers().getFirst();
            var level = player.level();
            var origin = player.blockPosition().above();
            ItemStack wine = ((BottleBlockItem) ModItems.WINE.get()).getFilledStack(3);
            int index = 0;
            for (var entry : ModBlocks.BLOCKS.getEntries()) {
                var pos = origin.offset(index % 12 - 6, 0, index / 12 + 4);
                level.setBlock(pos, entry.get().defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                var entity = level.getBlockEntity(pos);
                if (entity instanceof StorageBlockEntity storage) storage.getItems().setStackInSlot(0, wine.copy());
                else if (entity instanceof BarCabinetBlockEntity cabinet) {
                    cabinet.setLeftItem(wine.copy());
                    cabinet.setSingle(true);
                } else if (entity instanceof DrinkBlockEntity drink) drink.addItem(wine.copy());
                else if (entity instanceof ShakerBlockEntity shaker) shaker.getStorage().setStackInSlot(0, wine.copy());
                else if (entity instanceof TextBlockEntity text) text.setText("26.1.2");
                if (entity instanceof BaseBlockEntity base) base.refresh();
                index++;
            }
            ItemStack shaker = new ItemStack(ModItems.SHAKER.get());
            var storage = ShakerItem.getStorage(shaker);
            for (int i = 0; i < 3; i++) storage.setStackInSlot(i, wine.copy());
            ShakerItem.setStorage(shaker, storage);
            player.setItemInHand(InteractionHand.MAIN_HAND, shaker);
            player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.STRING_LIGHTS_RED.get()));
            shaker.use(level, player, InteractionHand.MAIN_HAND);
            KaleidoscopeTavern.LOGGER.info("CLIENT_SCENE_READY: {} blocks placed; shaker, lights and particles active", index);
        });
    }
}
