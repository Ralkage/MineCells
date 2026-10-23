package com.github.mim1q.minecells.mixin.world;

import net.minecraft.block.BlockSetType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockSetType.class)
public interface BlockSetTypeInvoker {
  @Invoker("register")
  static BlockSetType minecells$register(BlockSetType type) {
    throw new AssertionError();
  }
}
