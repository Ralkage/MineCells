"""Put a Crypt trial banner beside the boss exit rift.

Run with Python 3 and nbtlib 2.x from the repository root. The operation is
idempotent and leaves the original exit rift and boss encounter in place.
"""

from copy import deepcopy
from pathlib import Path

import nbtlib


ROOT = Path(__file__).resolve().parents[1] / "src/main/resources/data/minecells/structure/insufferable_crypt"
SOURCE = ROOT / "spawn.nbt"
DESTINATION = ROOT / "boss_room.nbt"
POSITION = (1, 3, 5)


def main():
    source = nbtlib.load(SOURCE)
    destination = nbtlib.load(DESTINATION)
    source_palette = source["palette"]
    banner = next(
        block for block in source["blocks"]
        if str(source_palette[int(block["state"])]["Name"]) == "minecells:insufferable_crypt_flag"
    )

    blocks = destination["blocks"]
    palette = destination["palette"]
    positions = {tuple(map(int, block["pos"])): block for block in blocks}
    x, y, z = POSITION
    assert str(palette[int(positions[(x, y - 2, z)]["state"])]["Name"]) != "minecraft:air"
    assert str(palette[int(positions[(x - 1, y, z)]["state"])]["Name"]) != "minecraft:air"
    existing = positions.get(POSITION)
    if existing is not None:
        assert str(palette[int(existing["state"])]["Name"]) in (
            "minecraft:air", "minecells:insufferable_crypt_flag"
        )

    state = deepcopy(source_palette[int(banner["state"])])
    state["Properties"]["facing"] = nbtlib.String("east")
    if state in palette:
        state_index = palette.index(state)
    else:
        state_index = len(palette)
        palette.append(state)

    blocks[:] = [block for block in blocks if tuple(map(int, block["pos"])) != POSITION]
    placed = deepcopy(banner)
    placed["pos"] = nbtlib.List[nbtlib.Int](POSITION)
    placed["state"] = nbtlib.Int(state_index)
    blocks.append(placed)
    destination.save()
    print(f"Added Crypt exit trial banner at {POSITION}")


if __name__ == "__main__":
    main()
