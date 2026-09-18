package dev.celestiacraft.industrialplatform.platform.blueprint;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 扫描 {@code 游戏目录/schematics/platform} 下的结构 NBT 当作平台蓝图.
 * <p>
 * 缓存按 文件修改时间 + 大小 失效, 所以玩家把文件丢进去/覆盖掉, 下次打开界面就是新的.
 */
public class BlueprintLibrary {
	private static final String FOLDER = "schematics";
	private static final String SUB_FOLDER = "platform";
	private static final String EXTENSION = ".nbt";

	private static final Map<String, Cached> CACHE = new LinkedHashMap<>();
	private static final Map<String, String> FAILED = new LinkedHashMap<>();

	private record Cached(PlatformBlueprint blueprint, long modified, long size) {
	}

	public static Path directory() {
		return FMLPaths.GAMEDIR.get().resolve(FOLDER).resolve(SUB_FOLDER);
	}

	/**
	 * 可用的蓝图. 引用了当前环境里没有的方块(整合包没装那个 Mod)的蓝图不会出现在列表里
	 */
	public static synchronized List<PlatformBlueprint> list() {
		refresh();

		List<PlatformBlueprint> blueprints = new ArrayList<>();
		CACHE.values().forEach(cached -> {
			if (!cached.blueprint().hasMissingBlocks()) {
				blueprints.add(cached.blueprint());
			}
		});
		return blueprints;
	}

	/**
	 * 按 id 取蓝图, 缺方块的同样拿不到(和列表保持一致)
	 */
	public static synchronized Optional<PlatformBlueprint> get(String id) {
		refresh();

		Cached cached = CACHE.get(id);
		if (cached == null || cached.blueprint().hasMissingBlocks()) {
			return Optional.empty();
		}

		return Optional.of(cached.blueprint());
	}

	/**
	 * 不可用的蓝图: 读不了的坏文件 + 缺方块的, 界面里可以提示玩家
	 */
	public static synchronized Map<String, String> unavailable() {
		refresh();

		Map<String, String> unavailable = new LinkedHashMap<>(FAILED);
		CACHE.forEach((id, cached) -> {
			if (cached.blueprint().hasMissingBlocks()) {
				unavailable.put(id, "missing blocks: " + cached.blueprint().getMissingBlocks());
			}
		});
		return unavailable;
	}

	public static synchronized void invalidate() {
		CACHE.clear();
		FAILED.clear();
	}

	private static void refresh() {
		Path dir = directory();

		Map<String, Path> files = new LinkedHashMap<>();
		try {
			Files.createDirectories(dir);
			try (Stream<Path> stream = Files.list(dir)) {
				stream.filter(Files::isRegularFile)
						.filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(EXTENSION))
						.sorted()
						.forEach(path -> files.put(stripExtension(path.getFileName().toString()), path));
			}
		} catch (IOException exception) {
			IndustrialPlatform.LOGGER.warn("Failed to scan blueprint folder {}", dir, exception);
			return;
		}

		CACHE.keySet().removeIf(id -> !files.containsKey(id));
		FAILED.keySet().removeIf(id -> !files.containsKey(id));

		files.forEach((id, path) -> {
			try {
				long modified = Files.getLastModifiedTime(path).toMillis();
				long size = Files.size(path);

				Cached cached = CACHE.get(id);
				if (cached != null && cached.modified() == modified && cached.size() == size) {
					return;
				}

				CompoundTag tag = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
				PlatformBlueprint blueprint = PlatformBlueprint.parse(id, tag, BuiltInRegistries.BLOCK.asLookup());

				CACHE.put(id, new Cached(blueprint, modified, size));
				FAILED.remove(id);

				if (blueprint.hasMissingBlocks()) {
					IndustrialPlatform.LOGGER.warn("Blueprint {} references blocks that are not installed: {}", id, blueprint.getMissingBlocks());
				}
			} catch (Exception exception) {
				CACHE.remove(id);
				FAILED.put(id, String.valueOf(exception.getMessage()));
				IndustrialPlatform.LOGGER.warn("Failed to read blueprint {}", path, exception);
			}
		});
	}

	private static String stripExtension(String fileName) {
		return fileName.substring(0, fileName.length() - EXTENSION.length());
	}
}