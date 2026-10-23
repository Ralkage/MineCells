"""Generate the Clock Tower route's 16-block structure tiles.

Run with ``PYTHONPATH=<path to nbtlib> python3 tools/generate_clock_tower.py``.
The rooms use a fixed 16x16 grid so the two staircases and raised wings always
meet at the same height, including across chunk boundaries.
"""

from __future__ import annotations

import json
import gzip
import io
import math
from pathlib import Path

from nbtlib import File
from nbtlib.tag import Byte, Compound, Double, Int, List, String


ROOT = Path(__file__).resolve().parents[1]
TEMPLATES = ROOT / "src/main/resources/data/minecells/structure/clock_tower"
POOLS = ROOT / "src/main/resources/data/minecells/worldgen/template_pool/clock_tower"

AIR = "minecraft:air"
BRICK = "minecells:prison_bricks"
SMALL_BRICK = "minecells:small_prison_bricks"
FLOOR = "minecraft:polished_deepslate"
COPPER = "minecraft:cut_copper"
OXIDIZED = "minecraft:oxidized_cut_copper"
GOLD = "minecraft:gold_block"
LAMP = "minecraft:sea_lantern"
IRON = "minecraft:iron_bars"
DEEPSLATE = "minecraft:deepslate_bricks"
GRATE = "minecraft:copper_grate"
WAXED = "minecraft:waxed_cut_copper"


class Canvas:
    def __init__(self, name: str):
        self.name = name
        self.blocks: dict[tuple[int, int, int], tuple[str, dict[str, str], dict | None]] = {}
        self.entities: list[Compound] = []

    def put(self, x: int, y: int, z: int, name: str, properties: dict[str, str] | None = None, nbt: dict | None = None):
        assert 0 <= x < 16 and 0 <= y < 16 and 0 <= z < 16, (self.name, x, y, z)
        self.blocks[x, y, z] = (name, properties or {}, nbt)

    def fill(self, x0: int, y0: int, z0: int, x1: int, y1: int, z1: int, name: str):
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1):
                for z in range(z0, z1 + 1):
                    self.put(x, y, z, name)

    def erase(self, x0: int, y0: int, z0: int, x1: int, y1: int, z1: int):
        self.fill(x0, y0, z0, x1, y1, z1, AIR)

    def entity(self, x: float, y: float, z: float, entity_id: str, **data: str):
        self.entities.append(Compound({
            "blockPos": List[Int]([int(x), int(y), int(z)]),
            "pos": List[Double]([x, y, z]),
            "nbt": Compound({"id": String(entity_id), **{key: String(value) for key, value in data.items()}}),
        }))

    def save(self):
        palette: list[Compound] = []
        indices: dict[tuple[str, tuple[tuple[str, str], ...]], int] = {}
        records: list[Compound] = []
        for (x, y, z), (name, properties, nbt) in sorted(self.blocks.items(), key=lambda e: (e[0][1], e[0][2], e[0][0])):
            key = (name, tuple(sorted(properties.items())))
            if key not in indices:
                state = Compound({"Name": String(name)})
                if properties:
                    state["Properties"] = Compound({k: String(v) for k, v in properties.items()})
                indices[key] = len(palette)
                palette.append(state)
            entry = Compound({"pos": List[Int]([x, y, z]), "state": Int(indices[key])})
            if nbt is not None:
                entry["nbt"] = Compound({key: (Byte(value) if isinstance(value, bool) else String(value)) for key, value in nbt.items()})
            records.append(entry)
        file = File({
            "DataVersion": Int(3955),
            "size": List[Int]([16, 16, 16]),
            "palette": List[Compound](palette),
            "blocks": List[Compound](records),
            "entities": List[Compound](self.entities),
        })
        TEMPLATES.mkdir(parents=True, exist_ok=True)
        buffer = io.BytesIO()
        file.write(buffer)
        (TEMPLATES / f"{self.name}.nbt").write_bytes(gzip.compress(buffer.getvalue(), mtime=0))


def tile(name: str) -> Canvas:
    c = Canvas(name)
    c.fill(0, 0, 0, 15, 15, 15, AIR)
    return c


def arch(c: Canvas, side: str, lo: int = 1, hi: int = 5):
    if side == "west":
        c.erase(0, lo, 6, 0, hi, 9)
    elif side == "east":
        c.erase(15, lo, 6, 15, hi, 9)
    elif side == "north":
        c.erase(6, lo, 0, 9, hi, 0)
    elif side == "south":
        c.erase(6, lo, 15, 9, hi, 15)


def wall_shell(c: Canvas, exits: tuple[str, ...], height: int = 8):
    for x in range(16):
        for z in range(16):
            border = x in (0, 15) or z in (0, 15)
            c.put(x, 0, z, COPPER if border else (FLOOR if (x + z) % 5 else SMALL_BRICK))
            if border:
                for y in range(1, height + 1):
                    c.put(x, y, z, COPPER if y in (2, height) else (DEEPSLATE if y % 5 == 0 else BRICK))
    # The open roof leaves the sky and distant tower rooms visible. Copper
    # cornices, buttresses and paired windows give each tile a distinct height.
    for x in range(1, 15):
        for z in (0, 15):
            c.put(x, height + 1, z, COPPER if x % 4 else GOLD)
            if x in (4, 11):
                for y in (4, 5):
                    c.put(x, y, z, IRON)
    for z in range(1, 15):
        for x in (0, 15):
            c.put(x, height + 1, z, COPPER if z % 4 else GOLD)
            if z in (4, 11):
                for y in (4, 5):
                    c.put(x, y, z, IRON)
    for x, z in ((2, 2), (2, 13), (13, 2), (13, 13)):
        c.fill(x, 1, z, x, height + 1, z, OXIDIZED)
        c.put(x, height + 2, z, GOLD)
    for side in exits:
        arch(c, side)
    for x, z in ((1, 1), (1, 14), (14, 1), (14, 14)):
        c.fill(x, 1, z, x, height + 2, z, OXIDIZED)
        c.put(x, height + 3, z, GOLD)
    for x, z in ((4, 4), (11, 4), (4, 11), (11, 11)):
        c.put(x, 8, z, "minecraft:chain")
        c.put(x, 7, z, "minecraft:lantern")


def brass_inlay(c: Canvas, cx: float = 7.5, cz: float = 7.5, inner: float = 3.3, outer: float = 4.6):
    for x in range(2, 14):
        for z in range(2, 14):
            d = math.hypot(x - cx, z - cz)
            if inner < d < outer:
                c.put(x, 0, z, GOLD if x in (3, 7, 8, 12) and z in (3, 7, 8, 12) else COPPER)


def clockwork(c: Canvas, x: int, y: int, z: int, radius: int = 3):
    # A raised gear motif, set on the wall; all parts remain outside the path.
    for dx in range(-radius, radius + 1):
        for dy in range(-radius, radius + 1):
            d = math.hypot(dx, dy)
            if radius - 1.15 < d <= radius + .2:
                c.put(x + dx, y + dy, z, GOLD if dx == 0 or dy == 0 else COPPER)
    c.put(x, y, z, OXIDIZED)


def flag(c: Canvas, x: int, y: int, z: int):
    # Side mounted flag is visible and reachable from the entry aisle.
    c.put(x, y, z, "minecells:clock_tower_flag", {"facing": "north", "waving": "false", "placement": "side"},
          {"id": "minecells:biome_banner"})


def clock_face(c: Canvas, z: int = 1):
    # The copper circle and gold cardinal marks make the facade legible from the room.
    for x in range(3, 13):
        for y in range(3, 13):
            d = math.hypot(x - 7.5, y - 7.5)
            if d <= 4.5:
                c.put(x, y, z, OXIDIZED)
            if 3.7 < d <= 4.5:
                c.put(x, y, z, GOLD if (x in (7, 8) or y in (7, 8)) else COPPER)
    for x, y in ((7, 5), (7, 6), (7, 7), (8, 7), (9, 7), (10, 7)):
        c.put(x, y, z, GOLD)


def loot_chest(c: Canvas, x: int, z: int, rare: bool = False):
    table = "minecells:chests/clock_tower/rare" if rare else "minecells:chests/clock_tower/common"
    c.put(x, 1, z, "minecraft:chest", {"facing": "north", "type": "single", "waterlogged": "false"},
          {"id": "minecraft:chest", "LootTable": table})


def spawner(c: Canvas, x: int, z: int, rune: str = "minecells:clock_tower/gears"):
    c.put(x, 1, z, "minecells:spawner_rune", nbt={"id": "minecells:spawner_rune", "dataId": rune})
    for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        c.put(x + dx, 0, z + dz, COPPER)


def portal(c: Canvas, x: int, z: int, facing: str, block: str, target: str):
    # A complete unbreakable frame surrounds the 1-block portal plane.
    offset = 1 if facing == "east" else -1
    for side, dz in (("left", -1), ("right", 1)):
        for y in (1, 2):
            c.put(x, y, z + dz, "minecells:unbreakable_doorway_frame", {"facing": facing, "type": side})
        c.put(x, 3, z + dz, "minecells:unbreakable_doorway_frame", {"facing": facing, "type": "top_" + side})
    c.put(x, 1, z, "minecells:unbreakable_doorway_frame", {"facing": facing, "type": "middle"})
    c.put(x, 3, z, "minecells:unbreakable_doorway_frame", {"facing": facing, "type": "top"})
    c.put(x, 2, z, block, {"facing": facing, "closed": "false"},
          {"id": "minecells:doorway", "special_point_target": target, "only_owner_can_enter": False})
    # Floor trim points into the room, making the active portal easy to find.
    c.put(x + offset, 0, z, GOLD)


def start() -> Canvas:
    c = tile("start")
    wall_shell(c, ("east",), 9)
    clock_face(c)
    portal(c, 1, 8, "east", "minecells:black_bridge_doorway", "minecells:entrance")
    for x in range(4, 13):
        c.put(x, 0, 8, GOLD if x in (4, 8, 12) else COPPER)
    for x, z in ((5, 4), (10, 4), (5, 12), (10, 12)):
        c.put(x, 1, z, "minecraft:chiseled_copper")
    for x in (5, 10):
        c.fill(x, 1, 12, x, 3, 12, DEEPSLATE)
        c.put(x, 4, 12, LAMP)
    flag(c, 4, 3, 14)
    return c


def hall(name: str = "hall", lower: bool = False) -> Canvas:
    c = tile(name)
    wall_shell(c, ("west", "east", "south") if lower else ("west", "east"), 8)
    for x in (3, 7, 11):
        for z in (3, 12):
            c.put(x, 0, z, GOLD)
            c.put(x, 1, z, "minecraft:chiseled_copper")
    for x in (4, 8, 12):
        c.put(x, 0, 7, COPPER)
        c.put(x, 0, 8, COPPER)
    for x in (4, 11):
        c.put(x, 1, 4, "minecraft:copper_grate")
        c.put(x, 1, 11, "minecraft:copper_grate")
    return c


def forge() -> Canvas:
    c = tile("forge")
    wall_shell(c, ("north", "east"), 9)
    # The lower service loop connects the first hall to the gear chamber's
    # treasure room, rewarding exploration without forcing a dead end.
    for x in (3, 12):
        for z in (3, 12):
            c.fill(x, 1, z, x, 3, z, DEEPSLATE)
            c.put(x, 4, z, "minecraft:chiseled_copper")
            c.put(x, 5, z, LAMP)
    for x in range(5, 11):
        for z in range(5, 11):
            c.put(x, 0, z, WAXED if (x + z) % 2 else OXIDIZED)
    for x, z in ((5, 6), (10, 6), (5, 10), (10, 10)):
        c.put(x, 1, z, "minecraft:heavy_weighted_pressure_plate")
    loot_chest(c, 3, 8)
    # The lower-loop banner is the second step of the tower's ordered trial.
    flag(c, 4, 3, 14)
    return c


def gear_room() -> Canvas:
    c = tile("gear_room")
    wall_shell(c, ("west", "east", "south"), 10)
    clock_face(c)
    spawner(c, 5, 5)
    spawner(c, 11, 11, "minecells:clock_tower/gears")
    loot_chest(c, 12, 3)
    for x in range(3, 13):
        for z in range(3, 13):
            d = math.hypot(x - 7.5, z - 7.5)
            if 3.5 < d < 5.0:
                c.put(x, 0, z, COPPER)
    for x, z in ((4, 12), (12, 4)):
        c.fill(x, 1, z, x, 4, z, OXIDIZED)
        c.put(x, 5, z, LAMP)
    clockwork(c, 8, 7, 13, 3)
    return c


def treasure_room() -> Canvas:
    c = tile("treasure_room")
    wall_shell(c, ("north", "west"), 8)
    for x, z in ((4, 5), (11, 5), (4, 11), (11, 11)):
        c.fill(x, 1, z, x, 3, z, BRICK)
        c.put(x, 4, z, GOLD)
    loot_chest(c, 8, 9, True)
    loot_chest(c, 6, 9)
    c.put(8, 0, 8, GOLD)
    brass_inlay(c, inner=4.5, outer=5.2)
    return c


def stair_ascent() -> Canvas:
    c = tile("stair_ascent")
    for x in range(16):
        height = x // 2
        for z in range(16):
            c.put(x, 0, z, BRICK)
        for z in range(5, 11):
            c.fill(x, 0, z, x, height, z, BRICK)
            if x % 2:
                c.put(x, height + 1, z, "minecraft:polished_deepslate_slab",
                      {"type": "bottom", "waterlogged": "false"})
            else:
                c.put(x, height, z, FLOOR)
        for z in (4, 11):
            c.fill(x, 1, z, x, height + 2, z, BRICK)
            c.put(x, height + 3, z, IRON)
            if x % 2 == 0:
                c.put(x, height + 2, z, COPPER)
        if x % 4 == 0:
            for z in (3, 12):
                c.fill(x, 1, z, x, height + 4, z, OXIDIZED)
                c.put(x, height + 5, z, LAMP)
        if x in (4, 8, 12):
            for z in (1, 14):
                c.fill(x, 1, z, x, height + 5, z, BRICK)
                c.put(x, height + 6, z, GOLD)
    for x in (2, 6, 10, 14):
        for z in (2, 13):
            for y in range(2, 5):
                c.put(x, y, z, IRON)
    for x in (0, 15):
        for z in range(16):
            if 5 <= z <= 10:
                continue
            c.fill(x, 1, z, x, 10, z, BRICK)
    return c


def pendulum_room() -> Canvas:
    c = tile("pendulum_room")
    wall_shell(c, ("west", "east", "south"), 11)
    spawner(c, 6, 4, "minecells:clock_tower/pendulums")
    spawner(c, 10, 12, "minecells:clock_tower/pendulums")
    loot_chest(c, 4, 12, True)
    # Chains and copper weights swing visually above the center of the room.
    for x, z in ((5, 8), (8, 8), (11, 8)):
        for y in range(8, 13):
            c.put(x, y, z, "minecraft:chain")
        c.put(x, 7, z, GOLD)
        c.put(x, 6, z, COPPER)
        c.put(x, 5, z, OXIDIZED)
    brass_inlay(c, inner=4.7, outer=5.5)
    # Timed-ascent shrine at the upper landing, 16 blocks above the entry flag.
    flag(c, 4, 3, 14)
    return c


def workshop() -> Canvas:
    c = tile("workshop")
    wall_shell(c, ("north", "west"), 10)
    for x in (3, 7, 11):
        for z in (3, 12):
            c.fill(x, 1, z, x, 3, z, DEEPSLATE)
            c.put(x, 4, z, "minecraft:chiseled_copper")
    for z in range(4, 12):
        c.put(8, 0, z, GRATE)
    for x in range(4, 12):
        c.put(x, 0, 8, GRATE)
    spawner(c, 11, 8, "minecells:clock_tower/workshop")
    loot_chest(c, 4, 4, True)
    return c


def observatory() -> Canvas:
    c = tile("observatory")
    wall_shell(c, ("east",), 10)
    # A high glassless oculus and measured brass floor mark the optional wing.
    brass_inlay(c, inner=5.0, outer=6.0)
    clock_face(c, 1)
    for x in (3, 12):
        for z in (3, 12):
            c.fill(x, 1, z, x, 5, z, OXIDIZED)
            c.put(x, 6, z, LAMP)
    loot_chest(c, 8, 11, True)
    return c


def bell_chamber() -> Canvas:
    c = tile("bell_chamber")
    wall_shell(c, ("west", "east"), 12)
    brass_inlay(c, inner=4.8, outer=5.8)
    # Four tall bells and an open central dais frame the elite encounter.
    for x, z in ((3, 3), (12, 3), (3, 12), (12, 12)):
        c.fill(x, 1, z, x, 4, z, BRICK)
        c.put(x, 5, z, LAMP)
        for y in range(8, 12):
            c.put(x, y, z, "minecraft:chain")
        c.put(x, 7, z, GOLD)
        c.put(x, 6, z, COPPER)
    for x in range(5, 11):
        for z in range(5, 11):
            c.put(x, 0, z, WAXED if (x + z) % 2 else OXIDIZED)
    c.entity(8.5, 1.0, 8.5, "minecells:elite_obelisk",
             spawnerRuneDataId="minecells:elite/clock_tower_chronoguard")
    return c


def support(height: int) -> Canvas:
    c = Canvas(f"support_{height}")
    # This sparse template occupies only the vertical columns below a raised tile;
    # it never writes air over the playable level above it.
    for x, z in ((2, 2), (2, 13), (13, 2), (13, 13)):
        for y in range(height):
            c.put(x, y, z, COPPER if y % 4 == 0 else OXIDIZED)
        for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            c.put(x + dx, height - 1, z + dz, BRICK)
    return c


def arena(quadrant: str) -> Canvas:
    c = tile("arena_" + quadrant)
    is_west = quadrant.endswith("w")
    is_north = quadrant.startswith("n")
    gx_base = 0 if is_west else 16
    gz_base = 0 if is_north else 16
    for x in range(16):
        for z in range(16):
            gx, gz = gx_base + x, gz_base + z
            radius = math.hypot(gx - 15.5, gz - 15.5)
            floor = FLOOR
            if 10.8 < radius < 12.2:
                floor = COPPER
            if abs(gx - 15.5) < 1 and gz < 15 or abs(gz - 15.5) < 1 and gx > 15:
                floor = GOLD
            for tick in range(12):
                a = tick * math.pi / 6
                if abs(gx - round(15.5 + 13 * math.cos(a))) < 1 and abs(gz - round(15.5 + 13 * math.sin(a))) < 1:
                    floor = GOLD
            c.put(x, 0, z, floor)
            outer = (is_west and x == 0) or (not is_west and x == 15) or (is_north and z == 0) or (not is_north and z == 15)
            if outer:
                for y in range(1, 8):
                    c.put(x, y, z, BRICK if y not in (2, 7) else COPPER)
                c.put(x, 8, z, COPPER)
                if (x + z) % 4 == 0:
                    c.put(x, 9, z, GOLD)
    if quadrant == "nw":
        arch(c, "west", 1, 5)
        # A single setup block spawns Time Keeper when this room becomes active.
        c.put(15, 1, 15, "minecells:time_keeper_box", nbt={"id": "minecells:setup_block_entity"})
    if quadrant == "ne":
        portal(c, 15, 8, "west", "minecells:overworld_doorway", "minecells:entrance")
        for z in range(7, 10):
            for y in range(1, 4):
                controller = (z == 8 and y == 2)
                name = "minecells:boss_barrier_controller" if controller else "minecells:conditional_barrier"
                nbt = {"id": "minecells:barrier_controller"} if controller else None
                c.put(14, y, z, name, {"facing": "west", "open": "false"}, nbt)
    # One pillar per outer corner keeps the center completely open for the
    # Time Keeper's dash while showing the four quadrants as one arena.
    corner_x = 1 if is_west else 14
    corner_z = 1 if is_north else 14
    c.fill(corner_x, 1, corner_z, corner_x, 11, corner_z, OXIDIZED)
    c.put(corner_x, 12, corner_z, GOLD)
    c.put(corner_x, 8, corner_z, "minecraft:lantern")
    # A copper walkway far overhead lends the arena a tower silhouette but
    # does not change boss movement on the floor.
    for p in range(2, 14):
        c.put(p, 12, 2 if is_north else 13, COPPER if p % 3 else GOLD)
        c.put(2 if is_west else 13, 12, p, COPPER if p % 3 else GOLD)
    return c


def save_pool(name: str):
    POOLS.mkdir(parents=True, exist_ok=True)
    (POOLS / f"{name}.json").write_text(json.dumps({
        "name": f"minecells:clock_tower/{name}",
        "fallback": "minecraft:empty",
        "elements": [{
            "weight": 1,
            "element": {
                "element_type": "minecraft:single_pool_element",
                "location": f"minecells:clock_tower/{name}",
                "projection": "rigid",
                "processors": "minecraft:empty",
            },
        }],
    }, indent=2) + "\n")


def main():
    canvases = [start(), hall(), hall("hall_junction", lower=True), forge(), gear_room(), stair_ascent(), pendulum_room(),
                workshop(), observatory(), bell_chamber(), treasure_room()]
    canvases += [support(8), support(16)]
    canvases += [arena(q) for q in ("nw", "ne", "sw", "se")]
    for canvas in canvases:
        canvas.save()
        save_pool(canvas.name)
        print(canvas.name, len(canvas.blocks))


if __name__ == "__main__":
    main()
