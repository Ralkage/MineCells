package com.github.mim1q.minecells.structure;

import com.github.mim1q.minecells.MineCells;
import com.github.mim1q.minecells.structure.grid.GridBasedStructure;
import com.github.mim1q.minecells.structure.grid.GridPiece;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class MineCellsStructures {
  // Structures
  public static StructureType<MineCellsBigJigsawStructure> BIG_JIGSAW;

  // Prisoners' Quarters
  public static StructureType<GridBasedStructure> PRISON;

  // Promenade of the Condemned
  public static StructureType<GridBasedStructure> PROMENADE;
  public static StructureType<GridBasedStructure> PROMENADE_WALL_X;
  public static StructureType<GridBasedStructure> PROMENADE_WALL_Z;

  // Ramparts
  public static StructureType<GridBasedStructure> RAMPARTS;

  // Black Bridge
  public static StructureType<GridBasedStructure> BLACK_BRIDGE;

  // Clock Tower
  public static StructureType<GridBasedStructure> CLOCK_TOWER;

  // Structure Pieces
  public static final StructurePieceType GRID_PIECE = GridPiece::new;

  private static <S extends Structure> StructureType<S> registerStructure(String id, MapCodec<S> codec) {
    return Registry.register(Registries.STRUCTURE_TYPE, MineCells.createId(id), () -> codec);
  }

  public static void registerTypes() {
    BIG_JIGSAW = registerStructure("big_jigsaw", MineCellsBigJigsawStructure.CODEC);
    PRISON = registerStructure("prison", GridBasedStructure.PRISON_CODEC);
    PROMENADE = registerStructure("promenade", GridBasedStructure.PROMENADE_CODEC);
    PROMENADE_WALL_X = registerStructure("promenade_wall_x", GridBasedStructure.PROMENADE_WALL_X_CODEC);
    PROMENADE_WALL_Z = registerStructure("promenade_wall_z", GridBasedStructure.PROMENADE_WALL_Z_CODEC);
    RAMPARTS = registerStructure("ramparts", GridBasedStructure.RAMPARTS_CODEC);
    BLACK_BRIDGE = registerStructure("black_bridge", GridBasedStructure.BLACK_BRIDGE_CODEC);
    CLOCK_TOWER = registerStructure("clock_tower", GridBasedStructure.CLOCK_TOWER_CODEC);
  }

  public static void registerPieces() {
    // Preserve the upstream ID used by already-generated structure pieces.
    Registry.register(Registries.STRUCTURE_PIECE, "grid_generator_piece", GRID_PIECE);
  }
}
