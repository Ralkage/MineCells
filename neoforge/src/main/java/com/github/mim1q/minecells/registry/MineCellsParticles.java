package com.github.mim1q.minecells.registry;

import com.github.mim1q.minecells.MineCells;
import com.github.mim1q.minecells.particle.colored.ColoredParticleType;
import com.github.mim1q.minecells.particle.electric.ElectricParticleType;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class MineCellsParticles {

  public static final SimpleParticleType AURA = new SimpleParticleType(false) { };
  public static final SimpleParticleType EXPLOSION = new SimpleParticleType(false) { };
  public static final SimpleParticleType PROTECTOR = new SimpleParticleType(false) { };
  public static final SimpleParticleType CHARGE = new SimpleParticleType(false) { };
  public static final SimpleParticleType FLY = new SimpleParticleType(false) { };
  public static final ColoredParticleType SPECKLE = ColoredParticleType.create();
  public static final ColoredParticleType FALLING_LEAF = ColoredParticleType.create();
  public static final ElectricParticleType ELECTRICITY = ElectricParticleType.create();
  public static final ColoredParticleType DROP = ColoredParticleType.create();
  public static final ColoredParticleType SMALL_DROP = ColoredParticleType.create();
  public static final ColoredParticleType RISING_BUBBLE = ColoredParticleType.create();

  public static void init() {
    Registry.register(Registries.PARTICLE_TYPE, MineCells.createId("aura"), AURA);
    Registry.register(Registries.PARTICLE_TYPE, MineCells.createId("explosion"), EXPLOSION);
    Registry.register(Registries.PARTICLE_TYPE, MineCells.createId("protector"), PROTECTOR);
    Registry.register(Registries.PARTICLE_TYPE, MineCells.createId("charge"), CHARGE);
    Registry.register(Registries.PARTICLE_TYPE, MineCells.createId("fly"), FLY);
    Registry.register(Registries.PARTICLE_TYPE, MineCells.createId("speckle"), SPECKLE);
    Registry.register(Registries.PARTICLE_TYPE, MineCells.createId("falling_leaf"), FALLING_LEAF);
    Registry.register(Registries.PARTICLE_TYPE, MineCells.createId("electricity"), ELECTRICITY);
    Registry.register(Registries.PARTICLE_TYPE, MineCells.createId("drop"), DROP);
    Registry.register(Registries.PARTICLE_TYPE, MineCells.createId("small_drop"), SMALL_DROP);
    Registry.register(Registries.PARTICLE_TYPE, MineCells.createId("rising_bubble"), RISING_BUBBLE);
  }

}
