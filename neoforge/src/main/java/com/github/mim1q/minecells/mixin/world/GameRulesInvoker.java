package com.github.mim1q.minecells.mixin.world;

import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRules.class)
public interface GameRulesInvoker {
  @Invoker("register")
  static <T extends GameRules.Rule<T>> GameRules.Key<T> minecells$register(
    String name, GameRules.Category category, GameRules.Type<T> type
  ) {
    throw new AssertionError();
  }
}
