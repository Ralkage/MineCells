"""Place optional elite obelisks in guaranteed Mine Cells route rooms.

Run with Python 3 and nbtlib 2.x from the repository root. The operation is
idempotent; it replaces an obelisk with the same encounter ID if present.
"""

from copy import deepcopy
from pathlib import Path

import nbtlib


ROOT = Path(__file__).resolve().parents[1] / "src/main/resources/data/minecells/structure"
PROTOTYPE = ROOT / "promenade/special/vine_rune.nbt"
PLACEMENTS = {
    "better_prison/promenade.nbt": ("minecells:elite/prison_gaoler", (8, 1, 7)),
    "promenade/path/crossroads.nbt": ("minecells:elite/promenade_briar_warden", (8, 1, 8)),
    "ramparts/spawn.nbt": ("minecells:elite/ramparts_storm_herald", (8, 4, 4)),
    "black_bridge/bottom2.nbt": ("minecells:elite/black_bridge_sentinel", (28, 37, 29)),
    "insufferable_crypt/spawn.nbt": ("minecells:elite/crypt_sentry", (5, 2, 5)),
}


def add_obelisk(path: Path, encounter: str, position: tuple[int, int, int], prototype):
    structure = nbtlib.load(path)
    palette = structure["palette"]
    block_names = {
        tuple(map(int, block["pos"])): str(palette[int(block["state"])]["Name"])
        for block in structure["blocks"]
    }
    x, y, z = position
    size = tuple(map(int, structure["size"]))
    assert all(0 <= value < bound for value, bound in zip(position, size)), (path, position, size)

    open_blocks = {None, "minecraft:air", "minecraft:cave_air", "minecraft:void_air"}
    below = block_names.get((x, y - 1, z))
    assert below not in open_blocks and "barrier" not in below and "water" not in below, (path, position, below)
    for offset in range(3):
        assert block_names.get((x, y + offset, z)) in open_blocks, (path, position, offset)

    entities = [
        entity for entity in structure["entities"]
        if str(entity["nbt"].get("spawnerRuneDataId", "")) != encounter
    ]
    entity = deepcopy(prototype)
    entity["blockPos"] = nbtlib.List[nbtlib.Int]([x, y, z])
    entity["pos"] = nbtlib.List[nbtlib.Double]([x + 0.5, y, z + 0.5])
    entity["nbt"]["Pos"] = nbtlib.List[nbtlib.Double]([x + 0.5, y, z + 0.5])
    entity["nbt"]["spawnerRuneDataId"] = nbtlib.String(encounter)
    entity["nbt"].pop("UUID", None)
    entities.append(entity)
    structure["entities"] = nbtlib.List[nbtlib.Compound](entities)
    structure.save()
    print(f"{path.relative_to(ROOT)}: {encounter} at {position}")


def main():
    prototype_file = nbtlib.load(PROTOTYPE)
    prototype = next(
        entity for entity in prototype_file["entities"]
        if str(entity["nbt"].get("id")) == "minecells:elite_obelisk"
    )
    for relative_path, (encounter, position) in PLACEMENTS.items():
        add_obelisk(ROOT / relative_path, encounter, position, prototype)


if __name__ == "__main__":
    main()
