package com.github.mim1q.minecells.registry;

import com.github.mim1q.minecells.MineCells;
import net.minecraft.util.Identifier;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/** Client textures for the two NeoForge fluid types. */
public final class MineCellsFluidClient {
  private MineCellsFluidClient() { }

  public static void registerExtensions(RegisterClientExtensionsEvent event) {
    event.registerFluidType(new IClientFluidTypeExtensions() {
      @Override
      public Identifier getStillTexture() {
        return MineCells.createId("block/fluid/toxic_sewage");
      }

      @Override
      public Identifier getFlowingTexture() {
        return MineCells.createId("block/fluid/toxic_sewage_flowing");
      }
    }, MineCellsFluids.SEWAGE_TYPE);

    event.registerFluidType(new IClientFluidTypeExtensions() {
      @Override
      public Identifier getStillTexture() {
        return MineCells.createId("block/fluid/ancient_sewage");
      }

      @Override
      public Identifier getFlowingTexture() {
        return MineCells.createId("block/fluid/ancient_sewage_flowing");
      }
    }, MineCellsFluids.ANCIENT_SEWAGE_TYPE);
  }
}
