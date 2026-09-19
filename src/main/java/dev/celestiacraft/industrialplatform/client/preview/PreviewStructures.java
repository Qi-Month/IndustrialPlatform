package dev.celestiacraft.industrialplatform.client.preview;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformMode;
import dev.celestiacraft.industrialplatform.platform.PlatformGenerator;
import dev.celestiacraft.industrialplatform.platform.PlatformLayout;
import dev.celestiacraft.industrialplatform.platform.PlatformPalette;
import dev.celestiacraft.industrialplatform.platform.PlatformStyle;
import dev.celestiacraft.industrialplatform.platform.blueprint.PlatformBlueprint;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.neoforged.fml.ModList;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PreviewStructures {
	private static final String DATA_FOLDER = "data";
	private static final String STRUCTURE_FOLDER = "structure";
	private static final String EXTENSION = ".nbt";
	private static final int GENERATED_HEIGHT = 1;

	private static final Map<PlatformMode, PlatformBlueprint> CACHE = new EnumMap<>(PlatformMode.class);

	public static PlatformBlueprint get(PlatformMode mode) {
		return CACHE.computeIfAbsent(mode, PreviewStructures::load);
	}

	private static PlatformBlueprint load(PlatformMode mode) {
		String id = mode.structureId();
		Path path = ModList.get().getModFileById(IndustrialPlatform.MODID).getFile()
				.findResource(DATA_FOLDER, IndustrialPlatform.MODID, STRUCTURE_FOLDER, id + EXTENSION);

		try (InputStream stream = Files.newInputStream(path)) {
			CompoundTag tag = NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap());
			return PlatformBlueprint.parse(id, tag, BuiltInRegistries.BLOCK.asLookup());
		} catch (Exception exception) {
			IndustrialPlatform.LOGGER.warn("Failed to read built-in structure {}, previewing the generated layout instead", id, exception);
			return generate(mode);
		}
	}

	private static PlatformBlueprint generate(PlatformMode mode) {
		PlatformStyle style = PlatformStyle.of(mode);
		PlatformLayout layout = PlatformLayout.of(mode.chunkSize(), mode.chunkSize());
		PlatformPalette palette = PlatformPalette.defaults(style);
		List<PlatformBlueprint.PlacedBlock> blocks = new ArrayList<>(layout.area());

		PlatformGenerator.forEachDeck(style, palette, layout, (localX, localZ, state) ->
				blocks.add(new PlatformBlueprint.PlacedBlock(new BlockPos(localX, 0, localZ), state)));

		Vec3i size = new Vec3i(layout.width(), GENERATED_HEIGHT, layout.depth());
		return new PlatformBlueprint(mode.structureId(), size, blocks, Map.of(), List.of());
	}
}
