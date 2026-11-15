package com.github.ysbbbbbb.kaleidoscopetavern.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class IncenseParticle extends SingleQuadParticle {
    protected final float baseAlpha;

    protected IncenseParticle(ClientLevel level, SpriteSet sprites, double x, double y, double z,
                              double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, sprites.get(level.getRandom()));
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.lifetime = 40 + level.getRandom().nextInt(20);
        this.gravity = -0.002F;
        this.quadSize = 0.15F + level.getRandom().nextFloat() * 0.05F;
        this.baseAlpha = 0.8F;
        this.alpha = this.baseAlpha;
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

        this.xd += (this.random.nextDouble() - 0.5) * 0.001;
        this.zd += (this.random.nextDouble() - 0.5) * 0.001;

        float progress = (float) this.age / this.lifetime;
        if (progress > 0.75F) {
            this.alpha = this.baseAlpha * (1.0F - (progress - 0.75F) * 4.0F);
        }

        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.95;
        this.zd *= 0.95;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public Provider(SpriteSet sprites) { this.sprites = sprites; }
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed, net.minecraft.util.RandomSource random) {
            return new IncenseParticle(level, sprites, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }
}
