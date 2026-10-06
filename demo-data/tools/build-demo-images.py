#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Claw Pet 演示图片生成工具
=========================

把「原始宠物照片」转换成本项目实际使用的演示图。

两个方向，分别对应两个子命令：

  --import <目录>   把外部的原始照片（PNG 等）转成仓库内的素材源
                    输出 → demo-data/images/originals/pet-XX.jpg
  (默认)            从素材源生成真正会被前端加载的演示图
                    输出 → demo-data/images/pet/demo-XX.jpg
                           demo-data/images/pet/shelter.jpg

为什么要分两步？
  因为「素材源」和「运行时用的图」职责不同：
  - originals/ 是给人和脚本看的原始素材，质量优先，只在需要重新生成时才用
  - pet/       是真正被 <img> 加载的，体积优先（首屏要快）

依赖：Pillow（pip install Pillow）
  只有 --manifest 不需要它 —— 那个模式纯读文件、纯写 JSON。

用法：
  python demo-data/tools/build-demo-images.py --import "D:/32326/Pictures/Camera Roll/宠物"
  python demo-data/tools/build-demo-images.py
  python demo-data/tools/build-demo-images.py --manifest   # 只重建 manifest.json
"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

# 延迟导入：--manifest 模式不碰图片，就不该强制装 Pillow。
# 其它模式在开跑前会调 require_pillow()，失败时给出明确提示而不是 ImportError 堆栈。
Image = None


def require_pillow():
    """按需导入 Pillow，并把 Image 挂到模块级（save_jpeg 里要用 Image.LANCZOS）。"""
    global Image
    if Image is None:
        try:
            from PIL import Image as _Image
        except ImportError:
            sys.exit("缺少 Pillow，请先执行：pip install Pillow")
        Image = _Image
    return Image

# ---------------------------------------------------------------- 路径

TOOLS_DIR = Path(__file__).resolve().parent
DEMO_DATA = TOOLS_DIR.parent
ORIGINALS_DIR = DEMO_DATA / "images" / "originals"
PET_DIR = DEMO_DATA / "images" / "pet"
MANIFEST = DEMO_DATA / "images" / "manifest.json"

# ---------------------------------------------------------------- 参数

# 素材源转 JPEG 时的质量。这个值要「肉眼无损」，因为它是源头
#
# 为什么不是 100/无损？实测过：19 张原图 11.27 MB(PNG) → 1.47 MB(q92)。
# 而 PNG 那个「无损」是假的 —— 它带了个全 255 的 alpha 通道，纯属白占体积。
IMPORT_QUALITY = 92

# 演示图转 JPEG 时的质量与最大宽度。
# 演示图会被宠物列表、详情页、首页宠物墙加载，所以要控制体积
#
# 注意：演示图走的是「原图 → q92 素材源 → q82 演示图」两次压缩，
# 比直接 PNG→q82 多一代损失。实测（墨宝/团团/憨憨/雪宝 4 张）：
#   单次 317.8 KB，PSNR 35.13 / 37.97 / 40.77 / 46.03 dB
#   两次 312.1 KB，PSNR 34.42 / 37.35 / 40.00 / 45.12 dB
# 即只差 0.6~0.9 dB、体积几乎不变 —— 肉眼不可辨，所以这个两步流程是划算的
# （换来的是仓库自带素材源，不依赖外部的原始照片目录）。
DEMO_QUALITY = 82
DEMO_MAX_WIDTH = 1200

# ---------------------------------------------------------------- 数据映射
#
# pet_id 必须和 demo-data/sql/02-demo-pets.sql 里的 pet_info.id 一一对应。
# src_name 是原始照片的文件名（就是当初堆在 Pictures 目录里的那个）。
#
# ⚠️ 改动这里之前先确认 demo-pets.sql 里的 id 没有变，否则图和宠物会对错人。

PETS = [
    # pet_id, 名字,   品种,            原图文件名
    (1,  "奶糖", "布偶猫",       "奶糖.png"),
    (2,  "年糕", "橘猫",         "年糕.png"),
    (3,  "雪宝", "英短银渐层",   "雪宝.png"),
    (4,  "墨宝", "黑猫",         "墨宝.png"),
    (5,  "铁柱", "中华田园犬",   "铁柱.png"),
    (6,  "团团", "柴犬",         "团团.png"),
    (7,  "咖啡", "拉布拉多",     "咖啡.png"),
    (8,  "豆花", "边牧",         "豆花.png"),
    (9,  "跳跳", "荷兰垂耳兔",   "跳跳.png"),
    (10, "布丁", "荷兰猪",       "布丁.png"),
    (11, "绒绒", "金丝熊仓鼠",   "绒绒.png"),
    (12, "彩虹", "虎皮鹦鹉",     "虎皮鹦鹉1.png"),
    (13, "咕咕", "玄凤鹦鹉",     "玄凤鹦鹉.png"),
    (14, "闪电", "蜜袋鼯",       "蜜袋鼯1.png"),
    (15, "憨憨", "法斗",         "憨憨.png"),
]

# 采集了但最终没用上的备选照片。
# 保留它们是因为「素材齐全」比「省这点体积」更重要 —— 以后想换某只宠物的照片时有得选。
UNUSED = [
    ("gou3.png",  "狗狗备选 3"),
    ("mao4.png",  "猫咪备选 4"),
    ("mao6.png",  "猫咪备选 6"),
    ("tuzi3.png", "兔子备选 3"),
]

# 收容所图片（首页 HeroBanner 用），来自数据库 shelter_info.image
SHELTER = ("shelter.jpg", "收容所图片（首页大图）")


# ---------------------------------------------------------------- 工具函数

def flatten_to_rgb(im: Image.Image, background=(255, 255, 255)) -> Image.Image:
    """
    把带 alpha 的图拍平成 RGB。

    JPEG 不支持透明通道，直接 convert('RGB') 会把透明区域变成黑色，
    所以要先铺一层背景色再合成。

    （本项目这 19 张原图实测 alpha 全是 255，走不到这个分支；
      但外部导入的图未必，所以保留这条路径。）
    """
    if im.mode in ("RGBA", "LA") or (im.mode == "P" and "transparency" in im.info):
        rgba = im.convert("RGBA")
        bg = Image.new("RGB", rgba.size, background)
        bg.paste(rgba, mask=rgba.split()[-1])
        return bg
    return im.convert("RGB")


def save_jpeg(im: Image.Image, dst: Path, quality: int, max_width: int = None) -> None:
    """按需缩放后存成渐进式 JPEG"""
    if max_width and im.width > max_width:
        h = round(im.height * max_width / im.width)
        im = im.resize((max_width, h), Image.LANCZOS)
    dst.parent.mkdir(parents=True, exist_ok=True)
    im.save(dst, "JPEG", quality=quality, optimize=True, progressive=True)


def kb(path: Path) -> float:
    return path.stat().st_size / 1024


# ---------------------------------------------------------------- 子命令

def do_import(src_dir: Path, shelter_src: Path = None) -> int:
    """
    把外部原始照片转成仓库内的素材源（originals/）

    宠物照从 src_dir 里按名字找；收容所图不在那个目录里（它原本是上传到
    upload/pet/ 的），所以要单独用 --shelter-src 指过来。
    """
    if not src_dir.is_dir():
        sys.exit(f"目录不存在：{src_dir}")

    print(f"从 {src_dir} 导入原始照片 → {ORIGINALS_DIR.relative_to(DEMO_DATA.parent)}")
    print()
    missing, done = [], 0

    jobs = [(f"pet-{pid:02d}.jpg", src_dir / name) for pid, _, _, name in PETS]
    jobs += [(f"unused-{i:02d}.jpg", src_dir / name)
             for i, (name, _) in enumerate(UNUSED, 1)]

    for out_name, src in jobs:
        if not src.exists():
            missing.append(src.name)
            continue
        dst = ORIGINALS_DIR / out_name
        save_jpeg(flatten_to_rgb(Image.open(src)), dst, IMPORT_QUALITY)
        done += 1
        print(f"  {src.name:22s} → {out_name:18s} {kb(src):8.1f} KB → {kb(dst):7.1f} KB")

    # 收容所图：可选，找不到不算「缺失」（很多人的 Pictures 目录里本来就没有它）
    if shelter_src and shelter_src.exists():
        dst = ORIGINALS_DIR / SHELTER[0]
        save_jpeg(flatten_to_rgb(Image.open(shelter_src)), dst, IMPORT_QUALITY)
        done += 1
        print(f"  {shelter_src.name:22s} → {SHELTER[0]:18s} "
              f"{kb(shelter_src):8.1f} KB → {kb(dst):7.1f} KB")
    elif shelter_src:
        print(f"  ⚠️  收容所图源文件不存在，已跳过：{shelter_src}")
    else:
        print("  （未指定 --shelter-src，跳过收容所图）")

    print()
    print(f"完成 {done} 张，输出目录 {ORIGINALS_DIR}")
    if missing:
        print(f"⚠️  源目录里缺少 {len(missing)} 个文件：{', '.join(missing)}")
        return 1
    return 0


def do_generate() -> int:
    """从素材源生成真正被前端加载的演示图（pet/）"""
    if not ORIGINALS_DIR.is_dir():
        sys.exit(f"素材源目录不存在：{ORIGINALS_DIR}\n先执行 --import 导入原始照片")

    print(f"从 {ORIGINALS_DIR.relative_to(DEMO_DATA.parent)} 生成演示图 → "
          f"{PET_DIR.relative_to(DEMO_DATA.parent)}")
    print()
    total_before = total_after = 0
    missing = []

    for pid, name, _, _ in PETS:
        src = ORIGINALS_DIR / f"pet-{pid:02d}.jpg"
        if not src.exists():
            missing.append(src.name)
            continue
        dst = PET_DIR / f"demo-{pid:02d}.jpg"
        save_jpeg(Image.open(src), dst, DEMO_QUALITY, DEMO_MAX_WIDTH)
        total_before += src.stat().st_size
        total_after += dst.stat().st_size
        print(f"  pet-{pid:02d}.jpg → demo-{pid:02d}.jpg   {kb(src):7.1f} KB → {kb(dst):6.1f} KB"
              f"   （{name}）")

    # 收容所图
    src = ORIGINALS_DIR / SHELTER[0]
    if src.exists():
        dst = PET_DIR / SHELTER[0]
        save_jpeg(Image.open(src), dst, DEMO_QUALITY, 1600)
        total_before += src.stat().st_size
        total_after += dst.stat().st_size
        print(f"  {SHELTER[0]:22s} → {SHELTER[0]:18s} {kb(src):7.1f} KB → {kb(dst):6.1f} KB"
              f"   （{SHELTER[1]}）")
    else:
        missing.append(SHELTER[0])

    print()
    if total_before:
        # 用十进制 MB（÷1000²）和文档里的数字口径一致；这里只统计会被 <img> 加载的图，
        # 不含 originals/ 里那几张没用上的备选，所以数值会比「originals 总体积」小
        print(f"合计 {total_before/1_000_000:.2f} MB → {total_after/1_000_000:.2f} MB"
              f"（压到 {total_after/total_before*100:.0f}%）")
    if missing:
        print(f"⚠️  缺少素材源 {len(missing)} 个：{', '.join(missing)}")
        return 1
    return 0


def do_manifest() -> int:
    """重建 manifest.json（机器可读的对应关系）"""
    entries = []
    for pid, name, breed, src_name in PETS:
        orig = ORIGINALS_DIR / f"pet-{pid:02d}.jpg"
        demo = PET_DIR / f"demo-{pid:02d}.jpg"
        entries.append({
            "pet_id": pid,
            "pet_name": name,
            "breed": breed,
            "original_name": src_name,
            "original": f"images/originals/pet-{pid:02d}.jpg",
            "demo": f"demo-{pid:02d}.jpg",
            "db_url": f"/profile/pet/demo-{pid:02d}.jpg",
            "size_original": orig.stat().st_size if orig.exists() else None,
            "size_demo": demo.stat().st_size if demo.exists() else None,
        })

    shelter_orig = ORIGINALS_DIR / SHELTER[0]
    shelter_demo = PET_DIR / SHELTER[0]
    manifest = {
        "_comment": "由 demo-data/tools/build-demo-images.py 生成，请勿手工编辑",
        "shelter": {
            "description": SHELTER[1],
            "db_column": "shelter_info.image",
            "original": f"images/originals/{SHELTER[0]}",
            "demo": SHELTER[0],
            "db_url": f"/profile/pet/{SHELTER[0]}",
            "size_original": shelter_orig.stat().st_size if shelter_orig.exists() else None,
            "size_demo": shelter_demo.stat().st_size if shelter_demo.exists() else None,
        },
        "unused": [
            {
                "original_name": src_name,
                "note": note,
                "original": f"images/originals/unused-{i:02d}.jpg",
            }
            for i, (src_name, note) in enumerate(UNUSED, 1)
        ],
        "pets": entries,
    }

    # 显式 newline="\n"：Windows 上默认会写成 CRLF，而 .gitattributes 规定仓库里存 LF，
    # 不固定的话每跑一次脚本这个文件都会显示成"已修改"。
    with open(MANIFEST, "w", encoding="utf-8", newline="\n") as f:
        f.write(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n")
    print(f"已写入 {MANIFEST}（{len(entries)} 只宠物 + 1 张收容所图）")
    return 0


# ---------------------------------------------------------------- 入口

def main() -> int:
    ap = argparse.ArgumentParser(
        description="Claw Pet 演示图片生成工具",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    ap.add_argument("--import", dest="import_dir", metavar="目录",
                    help="从外部照片目录导入素材源（转 JPEG 存到 images/originals/）")
    ap.add_argument("--shelter-src", metavar="文件",
                    help="收容所图的原始文件（不在宠物照片目录里时单独指过来）")
    ap.add_argument("--manifest", action="store_true",
                    help="只重建 manifest.json")
    args = ap.parse_args()

    if args.import_dir:
        require_pillow()
        rc = do_import(Path(args.import_dir),
                       Path(args.shelter_src) if args.shelter_src else None)
        if rc:
            return rc
        do_generate()
        return do_manifest()

    if args.manifest:
        return do_manifest()

    require_pillow()
    rc = do_generate()
    if rc:
        return rc
    return do_manifest()


if __name__ == "__main__":
    sys.exit(main())
