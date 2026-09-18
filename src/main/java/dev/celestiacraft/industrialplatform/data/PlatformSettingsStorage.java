package dev.celestiacraft.industrialplatform.data;

import dev.celestiacraft.industrialplatform.api.PlatformSettings;
import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 把每个平台方块上次选好的搭建设置存进存档, 退出界面/重进游戏后仍然保留
 */
public class PlatformSettingsStorage extends SavedData {
	private static final String DATA_NAME = "industrial_platform_settings";
	private static final String ENTRY_KEY = "Settings";
	private static final String POS_KEY = "Pos";
	private static final String MODE_KEY = "Mode";
	private static final String UP_KEY = "UpFill";
	private static final String DOWN_KEY = "DownFill";
	private static final String BLUEPRINT_KEY = "Blueprint";

	private final Map<BlockPos, PlatformSettings> settings = new HashMap<>();

	public static PlatformSettingsStorage get(ServerLevel level) {
		DimensionDataStorage storage = level.getDataStorage();

		return storage.computeIfAbsent(
				new SavedData.Factory<>(PlatformSettingsStorage::new, PlatformSettingsStorage::load),
				DATA_NAME
		);
	}

	public Optional<PlatformSettings> get(BlockPos pos) {
		return Optional.ofNullable(settings.get(pos));
	}

	public void put(BlockPos pos, PlatformSettings value) {
		settings.put(pos.immutable(), value);
		setDirty();
	}

	public void remove(BlockPos pos) {
		if (settings.remove(pos) != null) {
			setDirty();
		}
	}

	@Override
	public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
		ListTag list = new ListTag();

		settings.forEach((pos, value) -> {
			CompoundTag entry = new CompoundTag();
			entry.putIntArray(POS_KEY, new int[] {pos.getX(), pos.getY(), pos.getZ()});
			entry.putInt(MODE_KEY, value.mode().ordinal());
			entry.putInt(UP_KEY, value.upFill());
			entry.putInt(DOWN_KEY, value.downFill());
			entry.putString(BLUEPRINT_KEY, value.blueprintId() == null ? "" : value.blueprintId());
			list.add(entry);
		});

		tag.put(ENTRY_KEY, list);
		return tag;
	}

	private static PlatformSettingsStorage load(CompoundTag tag, HolderLookup.Provider provider) {
		PlatformSettingsStorage data = new PlatformSettingsStorage();
		ListTag list = tag.getList(ENTRY_KEY, Tag.TAG_COMPOUND);

		for (int index = 0; index < list.size(); index++) {
			CompoundTag entry = list.getCompound(index);
			int[] pos = entry.getIntArray(POS_KEY);
			if (pos.length != 3) {
				continue;
			}

			data.settings.put(
					new BlockPos(pos[0], pos[1], pos[2]),
					new PlatformSettings(
							PlatformMode.byIndex(entry.getInt(MODE_KEY)),
							entry.getInt(UP_KEY),
							entry.getInt(DOWN_KEY),
							entry.getString(BLUEPRINT_KEY)
					)
			);
		}

		return data;
	}
}