# 测试数据（demo-data）

这个目录**只放数据，不放代码**。项目跑起来需要的全部测试数据都在这里，
从建表脚本到宠物照片，一个目录打包齐。

## 为什么单独拎出来

之前这些东西是散着的：

| 原来在哪 | 问题 |
|---|---|
| `claw-pet-server/sql/` | 建表脚本和演示数据混在后端代码里 |
| `claw-pet-server/sql/demo-images/` | 演示图又嵌在 sql 里面一层 |
| 作者的 `Pictures/Camera Roll/宠物/` | **原始照片根本不在仓库里**，别人拿不到 |

现在统一到 `demo-data/`，好处是：

1. **找得到** —— 想换宠物照片、想改演示数据，只需要看这一个目录
2. **拿得走** —— 从原始照片到最终演示图的完整链路都在仓库里，可复现
3. **删得掉** —— 只是个独立目录。不想要测试数据，删掉 `demo-data/` 即可，
   不影响 `claw-pet-server/` 和 `claw-pet-web/` 的代码

> 唯一要留意的是容器部署：`docker-compose.yml` 把 `demo-data/sql/` 挂给了 MySQL 做首次初始化。
> 删了这个目录，容器建库时就没有脚本可执行了（会建出一个空库）。
> 这时改用「本地部署 + 手动导库」就行。

---

## 目录结构

```
demo-data/
├── README.md                      ← 你正在看的这个
├── sql/
│   ├── 01-init.sql                # 建库：15 张表 + 默认管理员 + 收容所信息 + 领养须知
│   └── 02-demo-pets.sql           # 演示数据：5 个分类 + 15 只宠物 + 宠物图片记录
├── images/
│   ├── manifest.json              # 机器可读的对应关系（图片 ↔ 宠物），由脚本生成
│   ├── pet/                       # ⭐ 真正被前端加载的演示图（16 张 / 1.0 MB）
│   │   ├── demo-01.jpg … demo-15.jpg   # 15 只宠物的封面（对应 pet_image.url）
│   │   └── shelter.jpg                 # 收容所图片，首页 HeroBanner 用（对应 shelter_info.image）
│   └── originals/                 # 素材源（20 张 / 1.9 MB），只在需要重新生成时才用
│       ├── pet-01.jpg … pet-15.jpg     # 15 只宠物的原图
│       ├── shelter.jpg                 # 收容所图原图
│       └── unused-01.jpg … unused-04.jpg  # 采集了但没用上的备选照片
└── tools/
    └── build-demo-images.py       # 从原图生成演示图的脚本（也可用来从外部照片导入）
```

### `images/pet/` 和 `images/originals/` 有什么区别

两个目录放的是同一批宠物的照片，但**职责不同**：

| | `originals/` | `pet/` |
|---|---|---|
| 干什么用 | 素材源头，给人看、给脚本读 | **实际被 `<img>` 加载的图** |
| 优先级 | 质量优先 | 体积优先（首屏要快） |
| 参数 | JPEG q92，不缩放 | JPEG q82，限宽 1200 |
| 会进 upload 吗 | ❌ 不会 | ✅ 启动脚本会拷到 `claw-pet-server/upload/pet/` |

**为什么要分两份？** 因为「保留原始素材」和「让页面加载快」是两个互相冲突的目标。
如果只留原图，首屏要下 11 MB；如果只留压缩图，以后想换质量就再也回不去了。
各留一份就都解决了 —— 而且压缩图只花 1.1 MB，很便宜。

---

## 怎么用

### 方式一：一键（推荐）

Windows 在**仓库根目录**双击 `start.bat`（macOS/Linux 跑 `./scripts/start.sh`）—— 它会自动把
`images/pet/*.jpg` 拷到 `claw-pet-server/upload/pet/`。

但**建库导数据这一步脚本不做**（因为要先知道你的 MySQL 账号密码），按下面手动来一次：

```bash
mysql -u root -p -e "CREATE DATABASE claw_pet DEFAULT CHARSET utf8mb4;"
mysql -u root -p claw_pet < demo-data/sql/01-init.sql
mysql -u root -p claw_pet < demo-data/sql/02-demo-pets.sql
```

### 方式二：手动

```bash
# 1. 建库导数据（同上）

# 2. 把演示图拷到上传目录
#    Windows PowerShell:
Copy-Item demo-data\images\pet\*.jpg claw-pet-server\upload\pet\
#    macOS / Linux:
cp demo-data/images/pet/*.jpg claw-pet-server/upload/pet/

# 3. 启动
./scripts/start.sh
```

> 顺序无所谓：先拷图还是先导数据都行，只是要两边都做完才看得到图。

---

## 数据 ↔ 宠物对应表

`02-demo-pets.sql` 里的 `pet_image.url` 指向的就是 `images/pet/demo-XX.jpg`。
这个对应关系**不能随便改** —— 改了 id 就得同步改图名，否则图和宠物会对错人。

| demo 图 | pet_id | 名字 | 品种 | 原图文件名 |
|:---:|:---:|---|---|---|
| `demo-01.jpg` | 1 | 奶糖 | 布偶猫 | 奶糖.png |
| `demo-02.jpg` | 2 | 年糕 | 橘猫 | 年糕.png |
| `demo-03.jpg` | 3 | 雪宝 | 英短银渐层 | 雪宝.png |
| `demo-04.jpg` | 4 | 墨宝 | 黑猫 | 墨宝.png |
| `demo-05.jpg` | 5 | 铁柱 | 中华田园犬 | 铁柱.png |
| `demo-06.jpg` | 6 | 团团 | 柴犬 | 团团.png |
| `demo-07.jpg` | 7 | 咖啡 | 拉布拉多 | 咖啡.png |
| `demo-08.jpg` | 8 | 豆花 | 边牧 | 豆花.png |
| `demo-09.jpg` | 9 | 跳跳 | 荷兰垂耳兔 | 跳跳.png |
| `demo-10.jpg` | 10 | 布丁 | 荷兰猪 | 布丁.png |
| `demo-11.jpg` | 11 | 绒绒 | 金丝熊仓鼠 | 绒绒.png |
| `demo-12.jpg` | 12 | 彩虹 | 虎皮鹦鹉 | 虎皮鹦鹉1.png |
| `demo-13.jpg` | 13 | 咕咕 | 玄凤鹦鹉 | 玄凤鹦鹉.png |
| `demo-14.jpg` | 14 | 闪电 | 蜜袋鼯 | 蜜袋鼯1.png |
| `demo-15.jpg` | 15 | 憨憨 | 法斗 | 憨憨.png |
| `shelter.jpg` | — | 收容所 | — | （原来是上传的 UUID 文件） |

另外 4 张 `unused-0X.jpg` 是当初采集但没用上的备选，保留下来以便以后换图：
`unused-01`(狗) / `unused-02`(猫) / `unused-03`(猫) / `unused-04`(兔)。
完整信息见 `images/manifest.json`。

---

## 重新生成演示图

需要 [Pillow](https://pypi.org/project/Pillow/)：`pip install Pillow`

### 从外部原始照片重新导入

```bash
python demo-data/tools/build-demo-images.py \
    --import "<你的照片目录>" \
    --shelter-src "<收容所图源文件>"
```

它会做三件事：把照片转成素材源存进 `originals/` → 生成 `pet/` 里的演示图 → 重建 `manifest.json`。

> `--shelter-src` 是可选的。收容所图不在宠物照片目录里（它原本是上传到
> `upload/pet/` 的 UUID 文件），所以单独指过来；不给就跳过。

### 只从仓库内的素材源重新生成

```bash
python demo-data/tools/build-demo-images.py
```

这个不需要外部照片，纯离线可跑。改完压缩参数（`DEMO_QUALITY` / `DEMO_MAX_WIDTH`）后
跑一次即可。**每条命令都是确定性的**：输入不变则输出不变，反复跑不会越压越糊。

---

## 关于体积

| | 原始（PNG） | 本仓库（JPEG） |
|---|---|---|
| 19 张原图 | 11.27 MB | **1.9 MB**（q92） |
| 16 张演示图 | — | **1.0 MB**（q82） |
| **合计** | 11.27 MB | **3.0 MB** |

原图之所以这么大，是因为它们是 **RGBA PNG** —— 而实测这个 alpha 通道**全是 255**，
也就是完全不透明，纯属白占体积。转成 JPEG 后视觉上没有区别。

> 照片类素材不要用 PNG 存：PNG 是无损压缩，对照片几乎压不动。

演示图走的是「原图 → q92 素材源 → q82 演示图」两次压缩，比直接转多一代损失。
实测过，只差 **0.6~0.9 dB PSNR**、体积几乎不变，肉眼不可辨，所以这个两步流程是划算的。

---

## ⚠️ 改这里之前请先读

### 1. 新增「存图片路径的列」时，要同步改两处

- 把图放进 `images/pet/`（否则 clone 的人看不到图）
- 把这一列加进 `claw-pet-server` 的 `FileCleanupTask.collectReferencedUrls()`
  （否则它引用的图会在宽限期后被定时任务当孤儿删掉）

第二条是真踩过的坑：`shelter_info.image` 一度不在清理任务的保护名单里，
1.4 MB 的收容所图差一点被当成垃圾删掉。

### 2. `01-init.sql` 与真实数据库有一点小脱节

`adoption_guide.id` / `pet_tip.id` 在脚本里是 `BIGINT`，实际库里是 `int`。
不影响使用（都是整数），但导入时如果报类型警告可忽略。

### 3. 改了 `pet_info.id` 就要改图名

见上面「数据 ↔ 宠物对应表」。对应关系是硬编码在 SQL 和文件名里的。
