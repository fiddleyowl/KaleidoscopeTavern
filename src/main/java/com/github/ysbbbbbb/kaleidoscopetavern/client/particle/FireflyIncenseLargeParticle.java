package com.github.ysbbbbbb.kaleidoscopetavern.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class FireflyIncenseLargeParticle extends SingleQuadParticle {
    private final float baseAlpha;
    private final float flickerSpeed;

    protected FireflyIncenseLargeParticle(ClientLevel level, SpriteSet sprites, double x, double y, double z,
                                          double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, sprites.get(level.getRandom()));
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.lifetime = 60 + level.getRandom().nextInt(40);
        this.gravity = -0.001F;
        this.quadSize = 0.08F + level.getRandom().nextFloat() * 0.04F;
        this.baseAlpha = 0.9F;
        this.alpha = this.baseAlpha;
        this.flickerSpeed = 0.3F + level.getRandom().nextFloat() * 0.4F;
        this.hasPhysics = false;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.xd += (this.random.nextDouble() - 0.5) * 0.002;
        this.zd += (this.random.nextDouble() - 0.5) * 0.002;
        this.yd += (this.random.nextDouble() - 0.5) * 0.0005;

        float flicker = (float) (Math.sin(this.age * this.flickerSpeed) * 0.4 + 0.6);
        float progress = (float) this.age / this.lifetime;
        float fadeAlpha = progress > 0.8F ? this.baseAlpha * (1.0F - (progress - 0.8F) * 5.0F) : this.baseAlpha;
        this.alpha = fadeAlpha * flicker;

        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.96;
        this.zd *= 0.96;
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return 0xF000F0;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public Provider(SpriteSet sprites) { this.sprites = sprites; }
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed, net.minecraft.util.RandomSource random) {
            return new FireflyIncenseLargeParticle(level, sprites, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }
}
