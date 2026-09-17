"""生成两个示例平台蓝图(原版结构 NBT 格式), 供测试导入功能.

用法:
    python tools/make_sample_blueprints.py

会写到 run/schematics/platform 与 run-data/schematics/platform 两处
(前者是游戏运行目录, 后者是数据生成目录), 丢进游戏目录/schematics/platform 即可被识别.
"""

import gzip
import os
import struct

TAG_END = 0
TAG_INT = 3
TAG_STRING = 8
TAG_LIST = 9
TAG_COMPOUND = 10

DATA_VERSION = 3465  # 1.20.1


def _string(value):
    data = value.encode("utf-8")
    return struct.pack(">H", len(data)) + data


def _named(tag_type, name, payload):
    return bytes([tag_type]) + _string(name) + payload


def _int(name, value):
    return _named(TAG_INT, name, struct.pack(">i", value))


def _string_tag(name, value):
    return _named(TAG_STRING, name, _string(value))


def _int_list(name, values):
    payload = struct.pack(">i", len(values)) + b"".join(struct.pack(">i", v) for v in values)
    return _named(TAG_LIST, name, bytes([TAG_INT]) + payload)


def _compound_list(name, items):
    payload = struct.pack(">i", len(items)) + b"".join(items)
    return _named(TAG_LIST, name, bytes([TAG_COMPOUND]) + payload)


def _compound_payload(items):
    """compound 的 payload: 列表元素用它(元素不带名字)"""
    return b"".join(items) + bytes([TAG_END])


def build_structure(size_x, size_y, size_z, block_at):
    """block_at(x, y, z) -> 方块 id 或者 None(空气)"""
    palette = []
    palette_index = {}
    blocks = []

    for y in range(size_y):
        for z in range(size_z):
            for x in range(size_x):
                name = block_at(x, y, z)
                if not name:
                    continue

                if name not in palette_index:
                    palette_index[name] = len(palette)
                    palette.append(_compound_payload([_string_tag("Name", name)]))

                blocks.append(_compound_payload([
                    _int_list("pos", [x, y, z]),
                    _int("state", palette_index[name]),
                ]))

    return _named(TAG_COMPOUND, "", _compound_payload([
        _int("DataVersion", DATA_VERSION),
        _int_list("size", [size_x, size_y, size_z]),
        _compound_list("palette", palette),
        _compound_list("blocks", blocks),
        _compound_list("entities", []),
    ]))


def write(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with gzip.open(path, "wb") as handle:
        handle.write(data)
    print("written", path, len(data), "bytes")


def industrial_deck_32(x, y, z):
    """32x32 单层: 磨制安山岩/铁块交替边框, 平滑石头底板, 内圈每 15 格一盏海晶灯"""
    if y != 0:
        return None

    border = x in (0, 31) or z in (0, 31)
    if border:
        return "minecraft:iron_block" if (x + z) % 2 == 0 else "minecraft:polished_andesite"

    def along(value):
        return value in (1, 31) or (value - 1) % 15 == 0

    if (z in (1, 30) and along(x)) or (x in (1, 30) and along(z)):
        return "minecraft:sea_lantern"

    return "minecraft:smooth_stone"


def railed_deck_16(x, y, z):
    """16x16 带一圈护栏: 0 层石砖底板, 1 层四周围一圈铁栏杆(演示多层蓝图)"""
    if y == 0:
        if x in (0, 15) or z in (0, 15):
            return "minecraft:chiseled_stone_bricks"
        return "minecraft:stone_bricks" if (x + z) % 2 == 0 else "minecraft:smooth_stone"

    if y == 1 and (x in (0, 15) or z in (0, 15)):
        if x in (0, 15) and z in (0, 15):
            return "minecraft:sea_lantern"
        return "minecraft:iron_bars"

    return None


if __name__ == "__main__":
    samples = {
        "industrial_deck_32": build_structure(32, 1, 32, industrial_deck_32),
        "railed_deck_16": build_structure(16, 2, 16, railed_deck_16),
    }

    for folder in ("run", "run-data"):
        for name, data in samples.items():
            write(os.path.join(folder, "schematics", "platform", name + ".nbt"), data)