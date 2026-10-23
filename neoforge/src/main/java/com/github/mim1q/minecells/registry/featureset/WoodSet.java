package com.github.mim1q.minecells.registry.featureset;

import com.github.mim1q.minecells.MineCells;
import com.github.mim1q.minecells.mixin.world.BlockSetTypeInvoker;
import com.github.mim1q.minecells.mixin.world.WoodTypeInvoker;
import net.minecraft.block.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SignItem;
import net.minecraft.util.Identifier;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class WoodSet extends FeatureSet {
  private static final Map<Block, Block> STRIPPED_BLOCKS = new IdentityHashMap<>();

  public final BlockSetType blockSetType = BlockSetTypeInvoker.minecells$register(new BlockSetType(MineCells.createId(name).toString()));
  public final WoodType woodType = WoodTypeInvoker.minecells$register(new WoodType(MineCells.createId(name).toString(), blockSetType));

  public final Block planks = registerBlockWithItem(name + "_planks", new Block(defaultBlockSettings()));
  public final PillarBlock log = registerBlockWithItem(name + "_log", new PillarBlock(defaultBlockSettings()));
  public final PillarBlock strippedLog = registerBlockWithItem("stripped_" + name + "_log", new PillarBlock(defaultBlockSettings()));
  public final PillarBlock wood = registerBlockWithItem(name + "_wood", new PillarBlock(defaultBlockSettings()));
  public final PillarBlock strippedWood = registerBlockWithItem("stripped_" + name + "_wood", new PillarBlock(defaultBlockSettings()));
  public final StairsBlock stairs = registerBlockWithItem(name + "_stairs", new StairsBlock(planks.getDefaultState(), defaultBlockSettings()));
  public final SlabBlock slab = registerBlockWithItem(name + "_slab", new SlabBlock(defaultBlockSettings()));
  public final DoorBlock door = registerBlockWithItem(name + "_door", new DoorBlock(blockSetType, defaultBlockSettings().nonOpaque()));
  public final TrapdoorBlock trapdoor = registerBlockWithItem(name + "_trapdoor", new TrapdoorBlock(blockSetType, defaultBlockSettings().nonOpaque()));
  public final FenceBlock fence = registerBlockWithItem(name + "_fence", new FenceBlock(defaultBlockSettings()));
  public final FenceGateBlock fenceGate = registerBlockWithItem(name + "_fence_gate", new FenceGateBlock(woodType, defaultBlockSettings()));
  public final ButtonBlock button = registerBlockWithItem(name + "_button", new ButtonBlock(blockSetType, 20, defaultBlockSettings().noCollision()));
  public final PressurePlateBlock pressurePlate = registerBlockWithItem(name + "_pressure_plate", new PressurePlateBlock(blockSetType, defaultBlockSettings().noCollision()));
  public final SignBlock sign = registerBlock(name + "_sign", new SignBlock(woodType, defaultBlockSettings().nonOpaque().noCollision()));
  public final WallSignBlock wallSign = registerBlock(name + "_wall_sign", new WallSignBlock(woodType, defaultBlockSettings().nonOpaque().noCollision()));
  public final SignItem signItem = registerItem(name + "_sign", new SignItem(defaultItemSettings().maxCount(16), sign, wallSign));

  public WoodSet(
    Identifier identifier,
    Supplier<Item.Settings> defaultItemSettings,
    Supplier<AbstractBlock.Settings> defaultBlockSettings
  ) {
    super(identifier, defaultItemSettings, defaultBlockSettings);
    STRIPPED_BLOCKS.put(log, strippedLog);
    STRIPPED_BLOCKS.put(wood, strippedWood);
  }

  public static void onBlockToolModification(BlockEvent.BlockToolModificationEvent event) {
    if (event.getItemAbility() != ItemAbilities.AXE_STRIP) return;
    var stripped = STRIPPED_BLOCKS.get(event.getState().getBlock());
    if (stripped != null) {
      event.setFinalState(stripped.getDefaultState().with(PillarBlock.AXIS, event.getState().get(PillarBlock.AXIS)));
    }
  }

  public List<Block> getBlocks() {
    return List.of(planks, log, strippedLog, wood, strippedWood, stairs, slab, door, trapdoor, fence, fenceGate, button, pressurePlate, sign, wallSign);
  }

  @Override
  public List<ItemStack> getStacks() {
    return Stream.of(
      planks, log, strippedLog, wood, strippedWood, stairs, slab, door, trapdoor, fence, fenceGate, button, pressurePlate, signItem
    ).map(b -> b.asItem().getDefaultStack()).toList();
  }
}
