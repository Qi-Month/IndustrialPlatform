package dev.celestiacraft.industrialplatform.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.PlatformSettings;
import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformMode;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.SavedDataStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 把每个平台方块上次选好的搭建设置存进存档, 退出界面/重进游戏后仍然保留
 */
public class PlatformSettingsStorage extends SavedData {
	private static final String DATA_NAME = "industrial_platform_settings";
	private static final String ENTRY_KEY = "Settings";
	private static final String POS_KEY = "Pos";
	public static final Codec<PlatformSettingsStorage> CODEC = CompoundTag.CODEC.comapFlatMap(PlatformSettingsStorage::decode, PlatformSettingsStorage::toNBT);
	public static final SavedDataType<PlatformSettingsStorage> TYPE = new SavedDataType<>(
			Identifier.fromNamespaceAndPath(IndustrialPlatform.MODID, DATA_NAME), PlatformSettingsStorage::new, CODEC
	);

	private final Map<BlockPos, PlatformSettings> settings = new HashMap<>();

	public static PlatformSettingsStorage get(ServerLevel level) {
		SavedDataStorage storage = level.getDataStorage();
		PlatformSettingsStorage loaded = storage.get(TYPE);
		if (loaded != null) {
			return loaded;
		}

		Path root = level.getServer().getWorldPath(LevelResource.ROOT);
		Path dataFolder = DimensionType.getStorageFolder(level.dimension(), root).resolve("data");
		Path currentFile = TYPE.id().withSuffix(".dat").resolveAgainst(dataFolder);
		if (!Files.notExists(currentFile)) {
			throw new IllegalStateException("Cannot load existing platform settings: " + currentFile);
		}

		for (Path legacyFile : legacyFiles(level, root, dataFolder)) {
			if (Files.notExists(legacyFile)) {
				continue;
			}

			try {
				CompoundTag rootTag = storage.readTagFromDisk(legacyFile, null, SharedConstants.getCurrentVersion().dataVersion().version());
				CompoundTag dataTag = rootTag.getCompound("data").orElseThrow(() -> new IllegalArgumentException("Missing compound data tag"));
				PlatformSettingsStorage migrated = CODEC.parse(NbtOps.INSTANCE, dataTag).getOrThrow();
				if (!Files.notExists(currentFile)) {
					throw new IllegalStateException("Platform settings appeared during migration: " + currentFile);
				}
				storage.set(TYPE, migrated);
				return migrated;
			} catch (IOException | RuntimeException exception) {
				throw new IllegalStateException("Cannot migrate platform settings: " + legacyFile, exception);
			}
		}

		return storage.computeIfAbsent(TYPE);
	}

	private static Set<Path> legacyFiles(ServerLevel level, Path root, Path dataFolder) {
		Set<Path> files = new LinkedHashSet<>();
		files.add(dataFolder.resolve(DATA_NAME + ".dat"));
		Path oldDimensionFolder;
		if (level.dimension().equals(Level.OVERWORLD)) {
			oldDimensionFolder = root;
		} else if (level.dimension().equals(Level.NETHER)) {
			oldDimensionFolder = root.resolve("DIM-1");
		} else if (level.dimension().equals(Level.END)) {
			oldDimensionFolder = root.resolve("DIM1");
		} else {
			oldDimensionFolder = DimensionType.getStorageFolder(level.dimension(), root);
		}
		files.add(oldDimensionFolder.resolve("data").resolve(DATA_NAME + ".dat"));
		return files;
	}

	public Optional<PlatformSettings> get(BlockPos pos) {
		return Optional.ofNullable(settings.get(pos));
	}

	public PlatformSettings resolve(BlockPos pos, PlatformMode mode) {
		PlatformSettings stored = settings.get(pos);
		PlatformSettings resolved = PlatformSettings.resolve(stored, mode);
		if (stored != null && resolved != stored) {
			put(pos, resolved);
		}
		return resolved;
	}

	public boolean put(BlockPos pos, PlatformSettings value) {
		BlockPos immutable = pos.immutable();
		if (value.equals(settings.put(immutable, value))) {
			return false;
		}
		setDirty();
		return true;
	}

	public void remove(BlockPos pos) {
		if (settings.remove(pos) != null) {
			setDirty();
		}
	}

	public CompoundTag toNBT() {
		CompoundTag tag = new CompoundTag();
		ListTag list = new ListTag();
		settings.forEach((pos, value) -> {
			CompoundTag entry = value.toNBT();
			entry.putIntArray(POS_KEY, new int[] {pos.getX(), pos.getY(), pos.getZ()});
			list.add(entry);
		});
		tag.put(ENTRY_KEY, list);
		return tag;
	}

	public static DataResult<PlatformSettingsStorage> decode(CompoundTag tag) {
		try {
			ListTag list = tag.getList(ENTRY_KEY).orElseThrow(() -> new IllegalArgumentException("Missing Settings list"));
			PlatformSettingsStorage data = new PlatformSettingsStorage();
			for (Tag element : list) {
				if (!(element instanceof CompoundTag entry)) {
					throw new IllegalArgumentException("Settings entry must be a compound");
				}
				int[] pos = entry.getIntArray(POS_KEY).orElseThrow(() -> new IllegalArgumentException("Settings Pos must be an int array"));
				if (pos.length != 3) {
					throw new IllegalArgumentException("Settings Pos must contain three integers");
				}
				Tag mode = entry.get("Mode");
				Tag upFill = entry.get("UpFill");
				Tag downFill = entry.get("DownFill");
				Tag blueprint = entry.get("Blueprint");
				if (mode != null && !(mode instanceof IntTag) || upFill != null && !(upFill instanceof IntTag)
						|| downFill != null && !(downFill instanceof IntTag) || blueprint != null && !(blueprint instanceof StringTag)) {
					throw new IllegalArgumentException("Invalid platform settings fields");
				}
				data.settings.put(new BlockPos(pos[0], pos[1], pos[2]), PlatformSettings.fromNBT(entry));
			}
			return DataResult.success(data);
		} catch (RuntimeException exception) {
			return DataResult.error(() -> "Invalid platform settings: " + exception.getMessage());
		}
	}
}
