package com.client.utilities.settings;

import java.awt.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Grant_ | www.rune-server.ee/members/grant_ | 12/10/19
 *
 */
public class Settings implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4394926495169279946L;

	public static Settings getDefault() {
		Settings settings = new Settings();
		settings.interfaceStyle = InterfaceStyle.OSRS;
		settings.rs3EditMode = false;
		settings.rs3PanelBackgroundColor = 0x141414;
		settings.rs3InterfaceTransparency = 0;
		settings.oldGameframe = false;
		settings.gameTimers = true;
		settings.antiAliasing = false;
		settings.groundItemOverlay = true;
		settings.fog = false;
		settings.smoothShading = false;
		settings.tileBlending = false;
		settings.inventoryContextMenu = false;
		settings.startMenuColor = SettingsManager.DEFAULT_START_MENU_COLOR;
		settings.chatColor = SettingsManager.DEFAULT_CHAT_COLOR_OPTION;
		settings.bountyHunter = true;
		settings.showEntityTarget = true;
		settings.drawDistance = 30;
		return settings;
	}

	private boolean oldGameframe;
	private InterfaceStyle interfaceStyle;
	private static boolean rs3EditMode;
	private int rs3PanelBackgroundColor;
	private int rs3InterfaceTransparency;
	private boolean gameTimers;
	private boolean antiAliasing;
	private boolean groundItemOverlay;
	private boolean fog;
	private boolean smoothShading;
	private boolean tileBlending;
	private boolean inventoryContextMenu;
	private int startMenuColor;
	private int chatColor;
	private boolean bountyHunter;
	private boolean showEntityTarget;
	private int drawDistance;
	private boolean stretchedMode;
	private Dimension stretchedModeDimensions;
	private Rectangle rs3ViewportBounds;
	private Map<Integer, Rs3PanelLayout> rs3PanelLayouts;

	public Settings() {}

	public boolean isOldGameframe() {
		return oldGameframe;
	}

	public void setOldGameframe(boolean oldGameframe) {
		this.oldGameframe = oldGameframe;
	}

	public InterfaceStyle getInterfaceStyle() {
		return interfaceStyle == null ? InterfaceStyle.OSRS : interfaceStyle;
	}

	public void setInterfaceStyle(InterfaceStyle interfaceStyle) {
		this.interfaceStyle = interfaceStyle;
	}

	public static boolean isRs3EditMode() {
		return rs3EditMode;
	}

	public void setRs3EditMode(boolean rs3EditMode) {
		this.rs3EditMode = rs3EditMode;
	}

	public int getRs3PanelBackgroundColor() {
		return rs3PanelBackgroundColor == 0 ? 0x141414 : rs3PanelBackgroundColor;
	}

	public void setRs3PanelBackgroundColor(int rs3PanelBackgroundColor) {
		this.rs3PanelBackgroundColor = rs3PanelBackgroundColor;
	}

	public int getRs3InterfaceTransparency() {
		return rs3InterfaceTransparency;
	}

	public void setRs3InterfaceTransparency(int rs3InterfaceTransparency) {
		this.rs3InterfaceTransparency = rs3InterfaceTransparency;
	}

	public Map<Integer, Rs3PanelLayout> getRs3PanelLayouts() {
		if (rs3PanelLayouts == null) {
			rs3PanelLayouts = new HashMap<>();
		}
		return rs3PanelLayouts;
	}

	public void setRs3PanelLayouts(Map<Integer, Rs3PanelLayout> rs3PanelLayouts) {
		this.rs3PanelLayouts = rs3PanelLayouts;
	}

	public void clearRs3PanelLayouts() {
		if (rs3PanelLayouts != null) {
			rs3PanelLayouts.clear();
		}
	}

	public boolean isAntiAliasing() {
		return antiAliasing;
	}

	public void setAntiAliasing(boolean antiAliasing) {
		this.antiAliasing = antiAliasing;
	}

	public boolean isFog() {
		return fog;
	}

	public void setFog(boolean fog) {
		this.fog = fog;
	}

	public boolean isTileBlending() {
		return tileBlending;
	}

	public void setTileBlending(boolean tileBlending) {
		this.tileBlending = tileBlending;
	}

	public boolean isSmoothShading() {
		return smoothShading;
	}

	public void setSmoothShading(boolean smoothShading) {
		this.smoothShading = smoothShading;
	}

	public boolean isInventoryContextMenu() {
		return inventoryContextMenu;
	}

	public void setInventoryContextMenu(boolean inventoryContextMenu) {
		this.inventoryContextMenu = inventoryContextMenu;
	}

	public int getChatColor() {
		return chatColor;
	}

	public void setChatColor(int chatColor) {
		this.chatColor = chatColor;
	}

	public boolean isBountyHunter() {
		return bountyHunter;
	}

	public void setBountyHunter(boolean bountyHunter) {
		this.bountyHunter = bountyHunter;
	}

	public boolean isShowEntityTarget() {
		return showEntityTarget;
	}

	public void setShowEntityTarget(boolean showEntityTarget) {
		this.showEntityTarget = showEntityTarget;
	}

	public boolean isGameTimers() {
		return gameTimers;
	}

	public void setGameTimers(boolean gameTimers) {
		this.gameTimers = gameTimers;
	}

	public boolean isGroundItemOverlay() {
		return groundItemOverlay;
	}

	public void setGroundItemOverlay(boolean groundItemOverlay) {
		this.groundItemOverlay = groundItemOverlay;
	}

	public int getStartMenuColor() {
		return startMenuColor;
	}

	public void setStartMenuColor(int startMenuColor) {
		this.startMenuColor = startMenuColor;
	}

	public int getDrawDistance() {
		return drawDistance;
	}

	public void setDrawDistance(int drawDistance) {
		this.drawDistance = drawDistance;
	}

	public boolean isStretchedMode() {
		return stretchedMode;
	}

	public void setStretchedMode(boolean stretchedMode) {
		this.stretchedMode = stretchedMode;
	}

	public Dimension getStretchedModeDimensions() {
		return stretchedModeDimensions;
	}

	public void setStretchedModeDimensions(Dimension stretchedModeDimensions) {
		this.stretchedModeDimensions = stretchedModeDimensions;
	}

	public Rectangle getRs3ViewportBounds() {
		return rs3ViewportBounds == null ? null : new Rectangle(rs3ViewportBounds);
	}

	public void setRs3ViewportBounds(Rectangle rs3ViewportBounds) {
		this.rs3ViewportBounds = rs3ViewportBounds == null ? null : new Rectangle(rs3ViewportBounds);
	}


	private boolean statusBars;
	public void setStatusBars(boolean statusBars) {
		this.statusBars = statusBars;
	}

	public static class Rs3PanelLayout implements Serializable {
		private final int x;
		private final int y;
		private final int width;
		private final int height;
		private final boolean visible;

		public Rs3PanelLayout(int x, int y, int width, int height, boolean visible) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.visible = visible;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		public boolean isVisible() {
			return visible;
		}
	}
}
