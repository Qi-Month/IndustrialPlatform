package dev.celestiacraft.industrialplatform.client.hologram;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.PlatformSettings;
import dev.celestiacraft.industrialplatform.mixin.client.GameRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.OptionsRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

@EventBusSubscriber(modid = IndustrialPlatform.MODID, value = Dist.CLIENT)
public class HologramWorldRenderer {
	private static final ContextKey<Frame> FRAME = new ContextKey<>(IndustrialPlatform.loadResource("hologram_frame"));
	private static final Matrix4f projection = new Matrix4f();
	private static final PoseStack bobStack = new PoseStack();
	private static final Vector3f spinningAxis = new Vector3f(0.0F, Mth.SQRT_OF_TWO / 2.0F, Mth.SQRT_OF_TWO / 2.0F);
	private static final Vector4f anchor = new Vector4f();
	private static Frame frame;

	public record Frame(BlockPos pos, PlatformSettings settings, float progress, float ndcX, float ndcY) {
		public float screenX(int width) {
			return (ndcX + 1.0F) * 0.5F * width;
		}

		public float screenY(int height) {
			return (1.0F - ndcY) * 0.5F * height;
		}
	}

	private record Brackets(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int color, float width) {
	}

	@SubscribeEvent
	public static void onExtractOutline(ExtractBlockOutlineRenderStateEvent event) {
		BlockPos pos = event.getBlockPos();
		if (!HologramTarget.isLockedOn(pos)) {
			return;
		}
		float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
		float spread = 0.02F + 0.35F * (1.0F - HologramTarget.getProgress(partialTick));
		float alpha = 0.85F + 0.15F * Mth.sin((event.getLevelRenderState().gameTime + partialTick) * 0.25F);
		AABB bounds = HologramTarget.getBounds();
		Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
		Brackets brackets = new Brackets((float) (pos.getX() + bounds.minX - camera.x) - spread,
				(float) (pos.getY() + bounds.minY - camera.y) - spread, (float) (pos.getZ() + bounds.minZ - camera.z) - spread,
				(float) (pos.getX() + bounds.maxX - camera.x) + spread, (float) (pos.getY() + bounds.maxY - camera.y) + spread,
				(float) (pos.getZ() + bounds.maxZ - camera.z) + spread, HologramColors.withAlpha(HologramColors.ACCENT, alpha), HologramRenderTypes.lineWidth() * 2.0F);
		event.addCustomRenderer((state, buffers, pose, translucentPass, levelState) -> {
			if (state.isTranslucent() == translucentPass) {
				drawBrackets(pose.last(), buffers.getBuffer(HologramRenderTypes.BRACKET_LINES), brackets);
			}
			return true;
		});
	}

	private static void drawBrackets(PoseStack.Pose pose, VertexConsumer consumer, Brackets brackets) {
		float armX = (brackets.maxX() - brackets.minX()) * 0.25F;
		float armY = (brackets.maxY() - brackets.minY()) * 0.25F;
		float armZ = (brackets.maxZ() - brackets.minZ()) * 0.25F;
		for (int corner = 0; corner < 8; corner++) {
			boolean east = (corner & 1) != 0;
			boolean top = (corner & 2) != 0;
			boolean south = (corner & 4) != 0;
			float x = east ? brackets.maxX() : brackets.minX();
			float y = top ? brackets.maxY() : brackets.minY();
			float z = south ? brackets.maxZ() : brackets.minZ();
			HologramRenderTypes.line(pose, consumer, x, y, z, x + (east ? -armX : armX), y, z, brackets.color(), brackets.width());
			HologramRenderTypes.line(pose, consumer, x, y, z, x, y + (top ? -armY : armY), z, brackets.color(), brackets.width());
			HologramRenderTypes.line(pose, consumer, x, y, z, x, y, z + (south ? -armZ : armZ), brackets.color(), brackets.width());
		}
	}

	@SubscribeEvent
	public static void onExtractLevel(ExtractLevelRenderStateEvent event) {
		frame = null;
		BlockPos pos = HologramTarget.getPos();
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (pos == null || player == null) {
			return;
		}
		CameraRenderState camera = event.getRenderState().cameraRenderState;
		OptionsRenderState options = minecraft.gameRenderer.getGameRenderState().optionsRenderState;
		GameRendererAccessor renderer = (GameRendererAccessor) minecraft.gameRenderer;
		float partialTick = event.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		projection.set(camera.projectionMatrix);
		bobStack.pushPose();
		renderer.industrialPlatform$bobHurt(camera, bobStack);
		if (options.bobView) {
			renderer.industrialPlatform$bobView(camera, bobStack);
		}
		projection.mul(bobStack.last().pose());
		bobStack.popPose();
		float portalIntensity = Mth.lerp(partialTick, player.oPortalEffectIntensity, player.portalEffectIntensity);
		float nauseaIntensity = player.getEffectBlendFactor(MobEffects.NAUSEA, partialTick);
		float intensity = Math.max(portalIntensity, nauseaIntensity) * (options.screenEffectScale * options.screenEffectScale);
		if (intensity > 0.0F) {
			float skew = 5.0F / (intensity * intensity + 5.0F) - intensity * 0.04F;
			skew *= skew;
			float angle = (renderer.industrialPlatform$spinningEffectTime() + partialTick * renderer.industrialPlatform$spinningEffectSpeed()) * Mth.DEG_TO_RAD;
			projection.rotate(angle, spinningAxis).scale(1.0F / skew, 1.0F, 1.0F).rotate(-angle, spinningAxis);
		}
		AABB bounds = HologramTarget.getBounds();
		anchor.set((float) (pos.getX() + (bounds.minX + bounds.maxX) * 0.5 - camera.pos.x),
				(float) (pos.getY() + bounds.maxY - camera.pos.y), (float) (pos.getZ() + (bounds.minZ + bounds.maxZ) * 0.5 - camera.pos.z), 1.0F)
				.mul(camera.viewRotationMatrix).mul(projection);
		if (anchor.w <= 0.0F || !Float.isFinite(anchor.w)) {
			return;
		}
		frame = new Frame(pos, HologramTarget.getSettings(), HologramTarget.getProgress(partialTick), anchor.x / anchor.w, anchor.y / anchor.w);
		event.getRenderState().setRenderData(FRAME, frame);
	}

	public static Frame getFrame() {
		return frame;
	}

	public static void clear() {
		frame = null;
	}

	public static boolean isAnchorVisible() {
		return frame != null;
	}

	public static float getAnchorX(int screenWidth) {
		return frame == null ? 0.0F : frame.screenX(screenWidth);
	}

	public static float getAnchorY(int screenHeight) {
		return frame == null ? 0.0F : frame.screenY(screenHeight);
	}
}
