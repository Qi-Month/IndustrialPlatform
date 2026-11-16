package dev.celestiacraft.industrialplatform.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.client.hologram.HologramColors;
import dev.celestiacraft.industrialplatform.client.hologram.HologramRenderTypes;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = IndustrialPlatform.MODID, value = Dist.CLIENT)
public class BoundaryRenderer {
	public static final float PREVIEW_R = 0.4F;
	public static final float PREVIEW_G = 0.65F;
	public static final float PREVIEW_B = 1.0F;
	private static final ContextKey<State> STATE = new ContextKey<>(IndustrialPlatform.loadResource("boundary_state"));
	private static final int DECK_COLOR = HologramColors.withAlpha(HologramColors.rgb(PREVIEW_R * 0.55F, PREVIEW_G * 0.55F, PREVIEW_B * 0.55F), 0.22F);

	private record Box(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float deckY) {
	}

	private record State(List<Box> boxes, int lineColor, float lineWidth) {
	}

	@SubscribeEvent
	public static void onExtractLevel(ExtractLevelRenderStateEvent event) {
		List<Box> boxes = new ArrayList<>();
		BoundaryRenderData.BoundaryEntry editing = BoundaryRenderData.getOverride();
		Vec3 camera = event.getRenderState().cameraRenderState.pos;
		for (BoundaryRenderData.BoundaryEntry entry : BoundaryRenderData.getEntries()) {
			if (editing == null || !editing.pos().equals(entry.pos())) {
				boxes.add(extractBox(entry, camera));
			}
		}
		if (editing != null) {
			boxes.add(extractBox(editing, camera));
		}
		float partialTick = event.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		float alpha = 0.75F + 0.15F * Mth.sin((event.getRenderState().gameTime + partialTick) * 0.06F);
		event.getRenderState().setRenderData(STATE, new State(List.copyOf(boxes), HologramColors.withAlpha(HologramColors.ACCENT, alpha), HologramRenderTypes.lineWidth()));
	}

	private static Box extractBox(BoundaryRenderData.BoundaryEntry entry, Vec3 camera) {
		AABB box = boxOf(entry);
		return new Box((float) (box.minX - camera.x), (float) (box.minY - camera.y), (float) (box.minZ - camera.z),
				(float) (box.maxX - camera.x), (float) (box.maxY - camera.y), (float) (box.maxZ - camera.z), (float) (entry.pos().getY() + 1.01 - camera.y));
	}

	@SubscribeEvent
	public static void onSubmitGeometry(SubmitCustomGeometryEvent event) {
		LevelRenderState levelState = event.getLevelRenderState();
		State state = levelState.getRenderData(STATE);
		if (state == null || state.boxes().isEmpty()) {
			return;
		}
		event.getSubmitNodeCollector().submitCustomGeometry(event.getPoseStack(), HologramRenderTypes.FACES, (pose, consumer) -> drawFaces(pose, consumer, state));
		event.getSubmitNodeCollector().submitCustomGeometry(event.getPoseStack(), HologramRenderTypes.BOUNDARY_LINES, (pose, consumer) -> {
			for (Box box : state.boxes()) {
				HologramRenderTypes.box(pose, consumer, box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ(), state.lineColor(), state.lineWidth());
			}
		});
	}

	private static void drawFaces(PoseStack.Pose pose, VertexConsumer consumer, State state) {
		for (Box box : state.boxes()) {
			HologramRenderTypes.vertex(pose.pose(), consumer, box.minX(), box.deckY(), box.minZ(), DECK_COLOR);
			HologramRenderTypes.vertex(pose.pose(), consumer, box.minX(), box.deckY(), box.maxZ(), DECK_COLOR);
			HologramRenderTypes.vertex(pose.pose(), consumer, box.maxX(), box.deckY(), box.maxZ(), DECK_COLOR);
			HologramRenderTypes.vertex(pose.pose(), consumer, box.maxX(), box.deckY(), box.minZ(), DECK_COLOR);
		}
	}

	public static AABB boxOf(BoundaryRenderData.BoundaryEntry entry) {
		int chunkX = Math.floorDiv(entry.pos().getX(), 16) * 16;
		int chunkZ = Math.floorDiv(entry.pos().getZ(), 16) * 16;
		double originX = chunkX - (entry.chunksX() - 1) * 8.0;
		double originZ = chunkZ - (entry.chunksZ() - 1) * 8.0;
		int posY = entry.pos().getY();
		return new AABB(originX, posY - Math.max(0, entry.downFill()), originZ,
				originX + Math.max(1, entry.sizeX()), posY + Math.max(0, entry.upFill()) + 1, originZ + Math.max(1, entry.sizeZ()));
	}
}
