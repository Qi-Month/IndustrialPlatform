# Industrial Platform 迁移至 Minecraft 26.3 / NeoForge 26.3.0.103

## 目标与边界

以用户提供的 [26.3 模板版本配置](../examplemod-template-26.3/gradle.properties#L7-L9)为准，迁移当前工程的全部 85 个生产 Java 文件及其资源，而非仅修改版本号。保留模组 ID、版本 2.0.1、现有平台/建造站/流体池/调节器功能及之前的同步、预览和掉落修复。

实施在当前目录进行，新建 `26.3-NeoForge` 迁移分支，保留现有 `26.1.2-NeoForge` 分支与提交；不改目录名，不覆盖参考模板或旧源工程 `F:\CloneProject\IndustrialPlatform`，不提交或推送。沿用用户之前的明确要求：主代理亲自执行，不使用子代理，不运行 Gradle。编译验证使用 Java 25 直接完成，Gradle 构建与游戏内最终验收仍由用户执行。

## 已核实的迁移依据

- 目标：Minecraft `26.3`，范围 `[26.3]`，NeoForge `26.3.0.103`，NeoForm `26.3-1`，FML `12.0.12`。当前与参考模板已同为 Java 25、ModDevGradle `2.0.148`、Gradle wrapper `9.2.1`、Foojay `1.0.0`，这些无需无意义升级。
- 已读取 `D:\GradleHome` 中的 26.3 / NeoForge 26.3.0.101 完整源码，并通过现有 10808 代理读取官方 26.3.0.103 sources/userdev。关键输入、几何提交、方块轮廓、PiP 注册、网络与模型法线接口和 0.101 一致，但 0.103 的 Entity、ItemStack、BlockEntity 补丁有变化；最终验证必须使用精确 0.103 构件，不能把 0.101 当成目标版本。
- 当前没有直接 GLFW 调用，但 SDL 鼠标左键值已是 `1`，现有蓝图点击判断仍写 `0`。世界滚轮事件在原版累计出非零整数后才分发，并提供 `getAccumulatedScrollY()`；GUI 滚轮仍接收小数偏移。
- 26.3 删除 `MultiBufferSource` 和 `OutputTarget`，管线类型迁到 `com.mojang.renderpearl.api.pipeline.RenderPipeline`。PiP 的 `renderToTexture` 改为接收 `SubmitNodeCollector`，注册工厂改为无参 `Supplier`；方块轮廓回调也改用提交器，不再接收缓冲区或旧透明阶段参数。
- `Minecraft.screen/setScreen` 改由 `Minecraft.gui.screen()/setScreen()` 管理；FML 删除 `ModConfig.Type.COMMON`，其不自动同步的两端本地配置用途对应 `LOCAL`。原版投影扰动参数现位于 `LevelRenderState.playerRenderState`，旧 GameRenderer 两个 spinning 字段不存在。
- Jade 正式版 `26.3.5`：`curse.maven:jade-324717:9042207`；JEI 正式版 `31.14.0.72`：`curse.maven:jei-238222:9207610`。两者 CurseMaven jar 的 SHA-1 均已与官方 Modrinth 发布文件一致。JEI jar 已内嵌满足要求的 MezzConfig `0.6.6`，不另加重复配置模组，也不为追新换成 beta。

## 实施步骤

### 1. 工作队列与构建版本

1. 获批后清理 Dynamic Spec 的旧普通完成任务，创建版本/基础 API、SDL 输入、渲染、验证这几项有限任务，随进度更新。
2. 再核对工作区是否有新改动，创建迁移分支；不覆盖用户更改或既有分支。
3. 更新 [gradle.properties](gradle.properties#L13-L21) 的 Minecraft、NeoForge、Jade、JEI 版本及文件 ID，保留代理、模组身份和构建参数。
4. 保留 [build.gradle](build.gradle) 的 CurseMaven、`compileOnly + localRuntime` 和客户端/服务端数据生成入口；按 [26.3 元数据模板](../examplemod-template-26.3/src/main/templates/META-INF/neoforge.mods.toml)清理旧 loader 属性及对应替换项，保持 Jade/JEI 可选依赖。
5. 在 [IndustrialPlatform.java](src/main/java/dev/celestiacraft/industrialplatform/IndustrialPlatform.java#L26-L33) 将配置注册改为 `LOCAL`，保留原 `nebula/industrial_platform/common.toml` 文件位置和已有配置键；不引入新的自动同步语义。

### 2. 基础 API、数据和兼容插件

- 将客户端读取/切换界面的旧访问统一迁到 `Minecraft.gui`，包括滚轮处理、全息目标、HUD 与界面退出逻辑；保持客户端隔离。
- 将 [IPItemTagsProvider.java](src/main/java/dev/celestiacraft/industrialplatform/datagen/tags/IPItemTagsProvider.java) 从已删除的 `IntrinsicHolderTagsProvider` 迁到 `TagsProvider<Item>` / `TagAppender<Item>`，按真实资源键 API 添加物品。
- 对全量编译发现的注册、菜单、网络、存储、结构放置、掉落和数据生成签名变化逐项查阅目标源码后修复，不猜测替代方法。不改七种网络包的业务格式和服务端权威校验。
- 保留 [DataGenerators.java](src/main/java/dev/celestiacraft/industrialplatform/datagen/DataGenerators.java#L20-L40) 两个入口均注册完整六个 provider 的修复，防止语言生成再次清理配方/标签/掉落表。
- 用精确 Jade/JEI 版本的公开 API 核对现有插件，保留工具提示、物品说明及预览面板额外区域；无兼容模组时不得加载其客户端插件类。

### 3. SDL 输入与滚轮

- [PlatformBuilderScreen.java](src/main/java/dev/celestiacraft/industrialplatform/client/screen/PlatformBuilderScreen.java#L407-L419) 的鼠标判断改用 `InputConstants.MOUSE_BUTTON_LEFT`，排查其他原生输入数字；菜单内部按钮 ID 不做机械替换。
- [FillAdjusterHandler.java](src/main/java/dev/celestiacraft/industrialplatform/client/FillAdjusterHandler.java#L25-L62) 使用 NeoForge 已累计的纵向滚轮量，保留潜行调节、Ctrl 十倍、Alt/站立放行、边界限制和服务端同步。只有实际用于调节的输入才拦截，不自行注册 SDL 原生回调。
- 两个建造界面的蓝图/材料列表和填充控件复用原版 `ScrollWheelHandler` 处理小数滚动与方向反转；按悬停操作区域管理残余量，避免上/下填充或不同列表之间串用。重复逻辑集中于小型客户端共用方法或工具，不在每次输入创建新累加器。

### 4. 渲染迁移——核心工作

1. **管线与透明度**：[HologramRenderTypes.java](src/main/java/dev/celestiacraft/industrialplatform/client/hologram/HologramRenderTypes.java) 和 [HologramOverlay.java](src/main/java/dev/celestiacraft/industrialplatform/client/hologram/HologramOverlay.java) 使用 RenderPearl 管线类型；移除旧 OutputTarget。参照 26.3 原版线框/半透明管线配置倒转深度、混合、OIT 和层级，保留颜色、透明度、线宽及遮挡。复用原版片段/OIT 管线，避免直接 OpenGL 调用。
2. **PiP 模型**：[PlatformPreviewRenderer.java](src/main/java/dev/celestiacraft/industrialplatform/client/preview/PlatformPreviewRenderer.java#L138-L206) 的 PictureRenderer 使用无参工厂和新三参数 `renderToTexture`；[PlatformPreviewModel.java](src/main/java/dev/celestiacraft/industrialplatform/client/preview/PlatformPreviewModel.java) 将缓存的分层 quad 快照按 RenderType 提交自定义几何，删除 `getBuffer/endBatch`。模型、清空区域面片与线框均由提交器和 FeatureRenderDispatcher 绘制，离屏纹理由原版 PiP 生命周期管理。
3. **世界几何**：[BoundaryRenderer.java](src/main/java/dev/celestiacraft/industrialplatform/client/BoundaryRenderer.java) 保留 extraction → 不可变状态 → submission；[HologramWorldRenderer.java](src/main/java/dev/celestiacraft/industrialplatform/client/hologram/HologramWorldRenderer.java) 的轮廓回调适配新四参数接口，通过提交器绘制括角，保持锁定时抑制原版轮廓的行为。
4. **投影与 Mixin**：从当前帧 `playerRenderState` 取得 portal/nausea 强度及 `spinningEffectAngle`，与原版投影矩阵相同顺序组合。保留已核实仍存在的 bobHurt/bobView Invoker，删除 [GameRendererAccessor.java](src/main/java/dev/celestiacraft/industrialplatform/mixin/client/GameRendererAccessor.java#L10-L22) 中两个失效 spinning Accessor，不新增不必要注入。
5. **GUI 与资源生命周期**：适配 [ClientSetup.java](src/main/java/dev/celestiacraft/industrialplatform/client/ClientSetup.java) 注册契约及实际需要变化的 GUI extraction 方法，保留缩放、裁剪、旋转、引线/扫光、资源重载失效和断线/关闭清理。模型仍仅在参数或资源变化时重建；回调只消费快照，不读取可变世界，不在顶点循环分配对象。

### 5. 可观察验证与交付

- 用 `D:\GradleHome` 中的 Java 25 和 NeoFormRuntime 准备精确 NeoForge 26.3.0.103 patched Minecraft、依赖及兼容模组 classpath；仅缺失构件从官方/已核验来源下载，不运行 Gradle，不混用旧版 jar。
- 直接编译全部生产 Java 至零错误；验证 Mixin 目标方法及描述符，检查已删除 API、错误导包、未使用 import 和客户端/服务端加载边界。辅助验证产物仅放忽略的 build 目录，不新增 README 或项目文档。
- 执行有明确输入输出的回归：SDL 左键/滚轮整数与小数/反向/修饰键/边界；PiP 与轮廓提交契约及非空顶点；投影正常/晃动/受伤/传送门/反胃/背后目标；四种平台模式与 fill/蓝图同步；流体池原掉落合堆与容器保护。
- 检查并按需要重新生成现有资源，验证四个配方及关联 advancement、三个掉落表、七个标签、语言和六个结构资源；测试两个数据生成入口交替执行不会丢数据。Create 条件配方继续保留原条件，普通配方仍可由 JEI 查看。
- 运行 `git diff --check`，报告改动、目标版本、直接编译/回归结果及未完成的运行时检查。提供用户游戏内验收要点：两屏和 HUD 同时显示、GUI scale/窗口调整、资源重载、多平台边界、SDL 滚轮、多人同步、专用服务端加载。未运行真实游戏/GPU 时，不声称视觉渲染、FPS 或网络时延实测通过。

## 特别值得注意的点

- 这是渲染提交架构迁移，不是 GLFW 名称替换；旧缓冲区、轮廓回调和 spinning Accessor 即使改版本也会编译或启动失败。
- 保留当前 26.1.2 分支和业务行为；26.3 新分支上的文件仍位于当前目录。未经另行要求不提交、推送或改写历史。
- 保留用户要求的 CurseMaven 和代理。JEI 稳定版已包含 MezzConfig，避免误加重复/不兼容运行依赖。
- 用户继续负责 Gradle 构建和真实游戏内最终确认；本次不运行 Gradle，不将直接 Java 验证冒称为 Gradle 或实际游戏测试。
