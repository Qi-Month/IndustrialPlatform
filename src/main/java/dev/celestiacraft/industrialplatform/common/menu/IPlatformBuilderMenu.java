package dev.celestiacraft.industrialplatform.common.menu;

import net.minecraft.server.level.ServerPlayer;

/**
 * 搭建界面的共同接口: 老平台方块和设计台共用同一套数据包
 */
public interface IPlatformBuilderMenu {
	void applySettings(int upFill, int downFill, int modeIndex);

	void build(ServerPlayer player);
}