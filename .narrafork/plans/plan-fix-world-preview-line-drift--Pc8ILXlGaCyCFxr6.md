# 修复世界预览线框的视角相关漂移

## 问题与依据

用户报告：世界预览线框在特定斜视角度上下移动。检查目标 26.3.0.103 源码后，边界坐标本身只取决于平台位置和填充参数，没有角度相关动画；当前线条却使用了会改变位置的视图缩放。

- [HologramRenderTypes.java](src/main/java/dev/celestiacraft/industrialplatform/client/hologram/HologramRenderTypes.java#L22-L30) 为边界线及括角使用 `VIEW_OFFSET_Z_LAYERING`；透视模式会再缩放相机相关坐标。
- 原版 `rendertype_lines.vsh` 内置 `VIEW_SCALE = 1 - 1/256`，在投影前再次收缩位置。与上一项叠加后，线框等效位置成为 `camera + scale × (world - camera)`，并非原始世界坐标。斜视、近距离及相机位置变化时，深度/遮挡错位更明显；投影中带有视图晃动的平移时也不再严格保持线中心位置。
- 26.3 世界绘制开始会设置正确的透视投影，不能把问题简单归咎于 PiP 泄漏正交投影。此次修复针对已确认的相机相关位置偏移；尚未运行真实 GPU 复现，不声称已排除所有设备端问题。

## 实施

1. **改用不移动顶点的线条顶点着色器**
   - 在模组 shader 资源中新增一个最小线条 `.vsh`，基于已读取的 26.3 原版接口，保持位置/颜色/法线/线宽属性及 fog、globals、dynamictransforms、projection 布局。
   - 移除 `VIEW_SCALE`，直接用 `ProjMat × ModelViewMat × Position` 投影。
   - 保留屏幕像素线宽展开；对投影辅助点 `w=0` 和投影线方向为零的情况避免除零/零向量归一化，退化为零面积而非产生无效顶点。

2. **以栅格深度偏移替代视图层变换**
   - 在现有 [HologramRenderTypes.java](src/main/java/dev/celestiacraft/industrialplatform/client/hologram/HologramRenderTypes.java) 建立缓存的边界线/括角管线，复用原版线条片段着色器与布局，统一使用新增顶点着色器。
   - 删除两处 `VIEW_OFFSET_Z_LAYERING`，不移动、缩放世界几何或修改平台边界参数。
   - 采用原版 `LINES_DEPTH_BIAS` 同方向的深度偏移值。遵守 26.3 反向深度 `GREATER_THAN_OR_EQUAL`；边界线仍不写深度，括角保持现有写深度与透明混合行为，不改成穿墙显示。
   - 为边界创建对应的自定义 OIT 三阶段管线；三个阶段使用相同顶点着色器及相同只读深度偏移，保留原透明渲染规则。
   - 不复制新工具类、不增加 tick/每帧几何重建、不新增无意义数值字段。着色器和管线均一次注册、复用。

3. **补全客户端注册与兼容检查**
   - 更新 [ClientSetup.java](src/main/java/dev/celestiacraft/industrialplatform/client/ClientSetup.java)，通过已核实的 `registerPipeline`、`registerOitPipelineSet` 注册新管线。
   - 检查共享线条管线在 GUI/PiP 清空区域框中仍遵守原尺寸、线宽和裁剪；不改动其控制器逻辑、世界扫描和网络同步。

## 有限验证

- 数值复现旧缩放导致的等效世界坐标偏移；覆盖相机俯仰/偏航、近中远距离与包含平移的视图晃动，核验新实现中心线投影与原始世界顶点一致。
- 检查两种 RenderType 都不再含位置层变换；普通管线与全部 OIT 阶段引用同一自定义 shader，深度方向/写入策略与透明混合符合预期。
- 验证零投影方向、辅助点 `w=0` 不产生 NaN/无限值；保留既有 PiP、世界边界、括角顶点数量及状态快照回归。
- 使用已缓存的精确 26.3.0.103/Java25 classpath 直接编译全部生产 Java，并运行相关离线回归和 `git diff --check`。不运行 Gradle，不使用子代理，不提交或推送。
- 最终明确说明 CPU/源码验证范围；真实 GPU、游戏内多角度结果仍须用户构建后确认。

## 注意事项

- 仅涉及渲染类型、客户端管线注册及必要 shader 资源，不改世界坐标、填充数值、方块生成/掉落逻辑或现有相机 Mixin。
- 保留现有 26.3 分支和所有用户改动，尤其是生成资源的已暂存删除及新的未跟踪生成文件；不对 Git 暂存区做操作。
