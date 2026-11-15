package com.github.ysbbbbbb.kaleidoscopetavern.client.gui.overlay;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.mixology.ShakerBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopetavern.util.ColorUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.Objects;

public class ShakerOverlay implements GuiLayer {
    private static final Identifier IMG = KaleidoscopeTavern.modLoc("textures/gui/shaker.png");
    private static final Identifier ICON = KaleidoscopeTavern.modLoc("gui/rhombus");

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameMode == null || minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            return;
        }
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        // 如果指向雪克杯，提示内容
        this.renderShakerBlockTips(guiGraphics, screenWidth, screenHeight, minecraft, player);
        this.renderShakerProgress(guiGraphics, screenWidth, screenHeight, player, partialTick);
    }

    private void renderShakerProgress(GuiGraphicsExtractor guiGraphics, int screenWidth, int screenHeight, LocalPlayer player, float partialTick) {
        // 如果手持雪克杯
        int remainingTicks = player.getUseItemRemainingTicks();
        if (remainingTicks > 0 && player.getUseItem().is(ModItems.SHAKER.get())) {
            guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, IMG, screenWidth / 2 - 91, screenHeight / 2 + 32, 0, 0, 181, 17, 256, 256);

            // 图标移动
            int offsetX = (int) Math.round((player.getTicksUsingItem() + partialTick) * 1.5);
            guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, IMG, screenWidth / 2 - 91 + offsetX, screenHeight / 2 + 26, 181, 0, 11, 13, 256, 256);
        }
    }

    private void renderShakerBlockTips(GuiGraphicsExtractor guiGraphics, int screenWidth, int screenHeight, Minecraft minecraft, LocalPlayer player) {
        HitResult hitResult = minecraft.hitResult;
        if (!(hitResult instanceof BlockHitResult result)) {
            return;
        }
        Level level = player.level();
        BlockPos blockPos = result.getBlockPos();
        BlockState blockState = player.level().getBlockState(blockPos);
        if (!blockState.is(ModBlocks.SHAKER.get())) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof ShakerBlockEntity shaker)) {
            return;
        }

        Font font = Minecraft.getInstance().font;
        int x = screenWidth / 2 - 28;
        int y = screenHeight / 2 + 26;

        ItemStackHandler storage = shaker.getStorage();
        for (int i = 0; i < storage.getSlots(); i++) {
            ItemStack stack = storage.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            ChatFormatting chatFormatting = ColorUtils.ITEM_COLOR_CACHE.apply(stack.getItem());
            // 如果没有颜色匹配，渲染物品
            if (chatFormatting == ChatFormatting.RESET) {
                guiGraphics.fakeItem(stack, x, y);
                guiGraphics.itemDecorations(font, stack, x, y);
            } else {
                int color = Objects.requireNonNull(chatFormatting.getColor()) | 0xFF000000;
                renderIcon(guiGraphics, x, y + 6, color);
            }
            x = x + 20;
        }
    }

    @SuppressWarnings("deprecation")
    private void renderIcon(GuiGraphicsExtractor pGuiGraphicsExtractor, int x, int y, int color) {
        pGuiGraphicsExtractor.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
                KaleidoscopeTavern.modLoc("textures/gui/rhombus.png"), x, y, 0, 0, 16, 16, 16, 16, color);
    }
}
