package com.github.ysbbbbbb.kaleidoscopetavern.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

public final class PotionBottleClient {
    public static void refreshChunk(BlockPos pos) {
        Minecraft.getInstance().levelRenderer.setSectionDirty(
                SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getY()),
                SectionPos.blockToSectionCoord(pos.getZ()));
    }
}
