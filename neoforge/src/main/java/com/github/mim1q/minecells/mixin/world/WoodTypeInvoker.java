package com.github.mim1q.minecells.mixin.world;

import net.minecraft.block.WoodType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WoodType.class)
public interface WoodTypeInvoker {
  @Invoker("register")
  static WoodType minecells$register(WoodType type) {
    throw new AssertionError();
  }
}
