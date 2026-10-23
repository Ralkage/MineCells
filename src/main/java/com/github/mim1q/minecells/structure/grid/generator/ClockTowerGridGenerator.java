package com.github.mim1q.minecells.structure.grid.generator;

import com.github.mim1q.minecells.MineCells;
import com.github.mim1q.minecells.structure.grid.GridPiecesGenerator.RoomGridGenerator;
import com.github.mim1q.minecells.structure.grid.SpecialPointIds;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;

/**
 * A fixed route with a lower workshop loop, two half-floor ascents,
 * an upper wing, an elite bell chamber and a two-by-two boss arena.
 * The first walkable floor starts at Y=63 after the rigid pool's vertical offset.
 * Structure placement is anchored to each 1024-block run center.
 */
public class ClockTowerGridGenerator extends RoomGridGenerator {
  private static final Identifier START = MineCells.createId("clock_tower/start");
  private static final Identifier HALL = MineCells.createId("clock_tower/hall");
  private static final Identifier HALL_JUNCTION = MineCells.createId("clock_tower/hall_junction");
  private static final Identifier GEAR_ROOM = MineCells.createId("clock_tower/gear_room");
  private static final Identifier FORGE = MineCells.createId("clock_tower/forge");
  private static final Identifier STAIR_ASCENT = MineCells.createId("clock_tower/stair_ascent");
  private static final Identifier SUPPORT_8 = MineCells.createId("clock_tower/support_8");
  private static final Identifier SUPPORT_16 = MineCells.createId("clock_tower/support_16");
  private static final Identifier PENDULUM_ROOM = MineCells.createId("clock_tower/pendulum_room");
  private static final Identifier WORKSHOP = MineCells.createId("clock_tower/workshop");
  private static final Identifier OBSERVATORY = MineCells.createId("clock_tower/observatory");
  private static final Identifier BELL_CHAMBER = MineCells.createId("clock_tower/bell_chamber");
  private static final Identifier TREASURE_ROOM = MineCells.createId("clock_tower/treasure_room");
  private static final Identifier ARENA_NW = MineCells.createId("clock_tower/arena_nw");
  private static final Identifier ARENA_NE = MineCells.createId("clock_tower/arena_ne");
  private static final Identifier ARENA_SW = MineCells.createId("clock_tower/arena_sw");
  private static final Identifier ARENA_SE = MineCells.createId("clock_tower/arena_se");

  @Override
  protected void addRooms(Random random) {
    addRoom(room(new Vec3i(0, 4, 0), START)
      .specialPoint(SpecialPointIds.ENTRANCE, new Vec3i(4, 0, 8), BlockRotation.CLOCKWISE_90));
    addRoom(room(new Vec3i(1, 4, 0), HALL_JUNCTION));
    addRoom(room(new Vec3i(2, 4, 0), GEAR_ROOM));
    addRoom(room(new Vec3i(1, 4, 1), FORGE));
    addRoom(room(new Vec3i(2, 4, 1), TREASURE_ROOM));

    addRoom(room(new Vec3i(3, 4, 0), STAIR_ASCENT));
    addRoom(room(new Vec3i(4, 4, 0), SUPPORT_8));
    addRoom(room(new Vec3i(4, 4, 0), HALL).offset(0, 8, 0));
    addRoom(room(new Vec3i(5, 4, 0), SUPPORT_8));
    addRoom(room(new Vec3i(5, 4, 0), STAIR_ASCENT).offset(0, 8, 0));
    addRoom(room(new Vec3i(6, 4, 0), SUPPORT_16));
    addRoom(room(new Vec3i(6, 5, 0), PENDULUM_ROOM));

    addRoom(room(new Vec3i(6, 4, 1), SUPPORT_16));
    addRoom(room(new Vec3i(6, 5, 1), WORKSHOP));
    addRoom(room(new Vec3i(5, 4, 1), SUPPORT_16));
    addRoom(room(new Vec3i(5, 5, 1), OBSERVATORY));

    addRoom(room(new Vec3i(7, 4, 0), SUPPORT_16));
    addRoom(room(new Vec3i(7, 5, 0), BELL_CHAMBER));
    addRoom(room(new Vec3i(8, 4, 0), SUPPORT_16));
    addRoom(room(new Vec3i(8, 5, 0), ARENA_NW));
    addRoom(room(new Vec3i(9, 4, 0), SUPPORT_16));
    addRoom(room(new Vec3i(9, 5, 0), ARENA_NE)
      .specialPoint(SpecialPointIds.EXIT, new Vec3i(13, 0, 8), BlockRotation.CLOCKWISE_90));
    addRoom(room(new Vec3i(8, 4, 1), SUPPORT_16));
    addRoom(room(new Vec3i(8, 5, 1), ARENA_SW));
    addRoom(room(new Vec3i(9, 4, 1), SUPPORT_16));
    addRoom(room(new Vec3i(9, 5, 1), ARENA_SE));
  }

  @Override
  public int getVersion() {
    return 2;
  }
}
