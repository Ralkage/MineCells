package com.github.mim1q.minecells.registry;

import com.github.mim1q.minecells.particle.*;
import com.github.mim1q.minecells.particle.colored.ColoredParticle;
import com.github.mim1q.minecells.particle.electric.ElectricParticle;
import net.minecraft.client.particle.FlameParticle;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/** Client particle factories are kept out of the common particle registry class. */
public final class MineCellsParticleClient {
  private MineCellsParticleClient() { }

  public static void onRegisterProviders(RegisterParticleProvidersEvent event) {
    event.registerSpriteSet(MineCellsParticles.AURA, FlameParticle.Factory::new);
    event.registerSpriteSet(MineCellsParticles.EXPLOSION, ExplosionParticle.Factory::new);
    event.registerSpriteSet(MineCellsParticles.PROTECTOR, ProtectorParticle.Factory::new);
    event.registerSpriteSet(MineCellsParticles.CHARGE, ChargeParticle.Factory::new);
    event.registerSpriteSet(MineCellsParticles.FLY, FlyParticle.Factory::new);
    event.registerSpriteSet(MineCellsParticles.SPECKLE, sprite -> new ColoredParticle.Factory(sprite, SpeckleParticle::new));
    event.registerSpriteSet(MineCellsParticles.FALLING_LEAF, sprite -> new ColoredParticle.Factory(sprite, FallingLeafParticle::new));
    event.registerSpriteSet(MineCellsParticles.ELECTRICITY, ElectricParticle.Factory::new);
    event.registerSpriteSet(MineCellsParticles.DROP, sprite -> new ColoredParticle.Factory(sprite, DropParticle::new));
    event.registerSpriteSet(MineCellsParticles.SMALL_DROP, sprite -> new ColoredParticle.Factory(sprite, SmallDropParticle::new));
    event.registerSpriteSet(MineCellsParticles.RISING_BUBBLE, sprite -> new ColoredParticle.Factory(sprite, RisingBubbleParticle::new));
  }
}
