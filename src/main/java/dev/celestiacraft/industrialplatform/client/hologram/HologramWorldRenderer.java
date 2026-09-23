package dev.celestiacraft.industrialplatform.client.hologram;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.client.BoundaryRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector4f;

@Mod.EventBusSubscriber(modid = IndustrialPlatform.MODID, value = Dist.CLIENT)
public class HologramWorldRenderer {
	private static final int CORNER_COUNT = 8;
	private static final int EAST_BIT = 1;
	private static final int TOP_BIT = 2;
	private static final int SOUTH_BIT = 4;

	private static final float ARM_RATIO = 0.25F;
	private static final float BRACKET_GAP = 0.02F;
	private static final float LOCK_ON_SPREAD = 0.35F;
	private static final float BRACKET_ALPHA = 0.85F;
	private static final float BRACKET_PULSE = 0.15F;
	private static final float PULSE_SPEED = 0.25F;

	private static final Vector4f anchor = new Vector4f();
	private static boolean anchorVisible;
	private static float anchorNdcX;
	private static float anchorNdcY;

	@SubscribeEvent
	public static void onRenderHighlight(RenderHighlightEvent.Block event) {
		BlockPos pos = event.getTarget().getBlockPos();
		if (!HologramTarget.isLockedOn(pos)) {
			return;
		}

		event.setCanceled(true);

		float partialTick = event.getPartialTick();
		float spread = BRACKET_GAP + LOCK_ON_SPREAD * (1.0F - HologramTarget.getProgress(partialTick));
		float time = event.getCamera().getEntity().tickCount + partialTick;
		float alpha = BRACKET_ALPHA + BRACKET_PULSE * Mth.sin(time * PULSE_SPEED);

		AABB bounds = HologramTarget.getBounds();
		Vec3 camera = event.getCamera().getPosition();
		float minX = (float) (pos.getX() + bounds.minX - camera.x) - spread;
		float minY = (float) (pos.getY() + bounds.minY - camera.y) - spread;
		float minZ = (float) (pos.getZ() + bounds.minZ - camera.z) - spread;
		float maxX = (float) (pos.getX() + bounds.maxX - camera.x) + spread;
		float maxY = (float) (pos.getY() + bounds.maxY - camera.y) + spread;
		float maxZ = (float) (pos.getZ() + bounds.maxZ - camera.z) + spread;

		VertexConsumer consumer = event.getMultiBufferSource().getBuffer(HologramRenderTypes.BRACKET_LINES);
		drawBrackets(event.getPoseStack().last(), consumer, minX, minY, minZ, maxX, maxY, maxZ, alpha);
	}

	private static void drawBrackets(PoseStack.Pose pose, VertexConsumer consumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float alpha) {
		float armX = (maxX - minX) * ARM_RATIO;
		float armY = (maxY - minY) * ARM_RATIO;
		float armZ = (maxZ - minZ) * ARM_RATIO;

		for (int corner = 0; corner < CORNER_COUNT; corner++) {
			boolean east = (corner & EAST_BIT) != 0;
			boolean top = (corner & TOP_BIT) != 0;
			boolean south = (corner & SOUTH_BIT) != 0;
			float x = east ? maxX : minX;
			float y = top ? maxY : minY;
			float z = south ? maxZ : minZ;

			drawArm(pose, consumer, x, y, z, east ? -armX : armX, 0.0F, 0.0F, alpha);
			drawArm(pose, consumer, x, y, z, 0.0F, top ? -armY : armY, 0.0F, alpha);
			drawArm(pose, consumer, x, y, z, 0.0F, 0.0F, south ? -armZ : armZ, alpha);
		}
	}

	private static void drawArm(PoseStack.Pose pose, VertexConsumer consumer, float x, float y, float z, float dx, float dy, float dz, float alpha) {
		float normalX = Math.signum(dx);
		float normalY = Math.signum(dy);
		float normalZ = Math.signum(dz);

		consumer.vertex(pose.pose(), x, y, z)
				.color(BoundaryRenderer.PREVIEW_R, BoundaryRenderer.PREVIEW_G, BoundaryRenderer.PREVIEW_B, alpha)
				.normal(pose.normal(), normalX, normalY, normalZ)
				.endVertex();
		consumer.vertex(pose.pose(), x + dx, y + dy, z + dz)
				.color(BoundaryRenderer.PREVIEW_R, BoundaryRenderer.PREVIEW_G, BoundaryRenderer.PREVIEW_B, alpha)
				.normal(pose.normal(), normalX, normalY, normalZ)
				.endVertex();
	}

	@SubscribeEvent
	public static void onRenderLevelStage(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
			return;
		}

		anchorVisible = false;
		BlockPos pos = HologramTarget.getPos();
		if (pos == null) {
			return;
		}

		AABB bounds = HologramTarget.getBounds();
		Vec3 camera = event.getCamera().getPosition();
		anchor.set(
				(float) (pos.getX() + (bounds.minX + bounds.maxX) * 0.5 - camera.x),
				(float) (pos.getY() + bounds.maxY - camera.y),
				(float) (pos.getZ() + (bounds.minZ + bounds.maxZ) * 0.5 - camera.z),
				1.0F
		);
		anchor.mul(event.getPoseStack().last().pose()).mul(event.getProjectionMatrix());

		if (anchor.w <= 0.0F) {
			return;
		}

		anchorNdcX = anchor.x / anchor.w;
		anchorNdcY = anchor.y / anchor.w;
		anchorVisible = true;
	}

	public static boolean isAnchorVisible() {
		return anchorVisible;
	}

	public static float getAnchorX(int screenWidth) {
		return (anchorNdcX + 1.0F) * 0.5F * screenWidth;
	}

	public static float getAnchorY(int screenHeight) {
		return (1.0F - anchorNdcY) * 0.5F * screenHeight;
	}
}
