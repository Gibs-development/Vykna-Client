package com.client.ui.panel;

import com.client.Client;
import com.client.DrawingArea;
import com.client.graphics.interfaces.RSInterface;
import com.client.utilities.settings.Settings;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PanelManager {
	public static final int PANEL_HEADER_HEIGHT = 18;
	private static final int PANEL_BACKGROUND = 0x141414;
	private static final int PANEL_HEADER = 0x1c1c1c;
	private static final int PANEL_BORDER = 0x2c2c2c;
	private static final int PANEL_TEXT = 0xffffff;
	private static final int RESIZE_HANDLE_SIZE = 12;
	private static final int CLOSE_BUTTON_SIZE = 12;
	private static final int CLOSE_BUTTON_PADDING = 4;
	public static final int PANEL_ID_INVENTORY = 1;
	public static final int PANEL_ID_PRAYER = 2;
	public static final int PANEL_ID_MAGIC = 3;
	public static final int PANEL_ID_EQUIPMENT = 4;
	public static final int PANEL_ID_QUEST = 5;
	public static final int PANEL_ID_STATS = 6;
	public static final int PANEL_ID_SKILLS = 7;
	public static final int PANEL_ID_CLAN = 8;
	public static final int PANEL_ID_FRIENDS = 9;
	public static final int PANEL_ID_SETTINGS = 10;
	public static final int PANEL_ID_EMOTES = 11;
	public static final int PANEL_ID_MUSIC = 12;
	public static final int PANEL_ID_NOTES = 13;
	public static final int PANEL_ID_LOGOUT = 14;
	public static final int PANEL_ID_MINIMAP_BASE = 20;
	public static final int PANEL_ID_CHAT = 21;
	public static final int PANEL_ID_TAB_BAR = 22;
	public static final int PANEL_ID_ORBS = 23;
	public static final int PANEL_ID_COMPASS = 24;
	public static final int PANEL_ID_HP_ORB = 25;
	public static final int PANEL_ID_PRAYER_ORB = 26;
	public static final int PANEL_ID_RUN_ORB = 27;
	public static final int PANEL_ID_SPEC_ORB = 28;
	public static final int PANEL_ID_XP_ORB = 29;
	public static final int PANEL_ID_MONEY_POUCH = 30;
	public static final int PANEL_ID_WORLD_MAP = 31;
	public static final int PANEL_ID_TELEPORT = 32;
	public static final int PANEL_ID_XP_PANEL = 33;
	private final List<UiPanel> panels = new ArrayList<>();
	private int layoutWidth = -1;
	private int layoutHeight = -1;
	private UiPanel activePanel;
	private boolean dragging;
	private boolean resizing;
	private int dragOffsetX;
	private int dragOffsetY;
	private int resizeStartX;
	private int resizeStartY;
	private int resizeStartWidth;
	private int resizeStartHeight;
	private boolean mouseDownLastFrame;

	public void ensureRs3Layout(Client client) {
		if (layoutWidth == Client.currentGameWidth && layoutHeight == Client.currentGameHeight && !panels.isEmpty()) {
			return;
		}
		panels.clear();
		PanelLayout.populateRs3Panels(panels);
		applySavedLayout(client);
		layoutWidth = Client.currentGameWidth;
		layoutHeight = Client.currentGameHeight;
	}

	public void drawPanels(Client client, boolean editMode) {
		for (UiPanel panel : panels) {
			if (panel.isVisible()) {
				if (panel.drawsBackground()) {
					drawPanelBackground(client, panel);
					if (editMode && panel.isClosable()) {
						drawCloseButton(client, panel);
					}
				}
				panel.draw(client);
				if (editMode) {
					drawResizeHandle(panel);
					if (panel == activePanel) {
						drawSelectionOutline(panel.getBounds());
					}
				}
			}
		}
	}

	public boolean handleMouse(Client client, int mouseX, int mouseY) {
		if (dragging || resizing) {
			return false;
		}
		client.clearOrbHovers();
		for (int index = panels.size() - 1; index >= 0; index--) {
			UiPanel panel = panels.get(index);
			if (!panel.isVisible()) {
				continue;
			}
			Rectangle bounds = panel.getBounds();
			if (panel.contains(mouseX, mouseY)) {
				panel.handleMouse(client, mouseX - bounds.x, mouseY - bounds.y);
				return true;
			}
		}
		return false;
	}

	public boolean handleRightClick(Client client, int mouseX, int mouseY) {
		if (dragging || resizing) {
			return false;
		}
		for (int index = panels.size() - 1; index >= 0; index--) {
			UiPanel panel = panels.get(index);
			if (!panel.isVisible()) {
				continue;
			}
			Rectangle bounds = panel.getBounds();
			if (panel.contains(mouseX, mouseY)) {
				return panel.handleRightClick(client, mouseX - bounds.x, mouseY - bounds.y);
			}
		}
		return false;
	}

	public boolean handleClick(Client client, int mouseX, int mouseY, boolean mouseClicked) {
		if (!mouseClicked || dragging || resizing) {
			return false;
		}
		for (int index = panels.size() - 1; index >= 0; index--) {
			UiPanel panel = panels.get(index);
			if (!panel.isVisible()) {
				continue;
			}
			Rectangle bounds = panel.getBounds();
			if (panel.contains(mouseX, mouseY)) {
				panel.handleClick(client, mouseX - bounds.x, mouseY - bounds.y);
				client.performMenuActionIfAvailable();
				return true;
			}
		}
		return false;
	}

	public void handleEditModeInput(Client client, int mouseX, int mouseY, boolean mouseDown) {
		if (!mouseDown && mouseDownLastFrame && (dragging || resizing)) {
			dragging = false;
			resizing = false;
			saveLayoutToSettings(client);
		}

		if (mouseDown && !mouseDownLastFrame) {
			UiPanel hit = getTopmostPanelAt(mouseX, mouseY);
			if (hit != null && hit.drawsBackground() && hit.isClosable() && isOnCloseButton(hit, mouseX, mouseY)) {
				if (hit instanceof BasePanel) {
					((BasePanel) hit).setVisible(false);
				}
				saveLayoutToSettings(client);
				activePanel = null;
				mouseDownLastFrame = mouseDown;
				return;
			}
			if (hit != null && hit.resizable() && isOnResizeHandle(hit, mouseX, mouseY)) {
				activePanel = hit;
				bringToFront(hit);
				Rectangle bounds = hit.getBounds();
				resizeStartX = mouseX;
				resizeStartY = mouseY;
				resizeStartWidth = bounds.width;
				resizeStartHeight = bounds.height;
				resizing = true;
			} else if (hit != null && hit.draggable()) {
				activePanel = hit;
				bringToFront(hit);
				Rectangle bounds = hit.getBounds();
				dragOffsetX = mouseX - bounds.x;
				dragOffsetY = mouseY - bounds.y;
				dragging = true;
			} else {
				activePanel = null;
			}
		}

		if (mouseDown && dragging && activePanel != null) {
			Rectangle bounds = activePanel.getBounds();
			int newX = mouseX - dragOffsetX;
			int newY = mouseY - dragOffsetY;
			newX = clamp(newX, 0, Client.currentGameWidth - bounds.width);
			newY = clamp(newY, 0, Client.currentGameHeight - bounds.height);
			activePanel.setPosition(newX, newY);
		}

		if (mouseDown && resizing && activePanel != null) {
			int deltaX = mouseX - resizeStartX;
			int deltaY = mouseY - resizeStartY;
			int newWidth = resizeStartWidth + deltaX;
			int newHeight = resizeStartHeight + deltaY;
			if (activePanel.keepAspectRatio()) {
				int size = Math.max(newWidth, newHeight);
				newWidth = size;
				newHeight = size;
			}
			if (activePanel instanceof InventoryPanel) {
				Dimension clamped = ((InventoryPanel) activePanel).clampSizeForResize(newWidth, newHeight, client);
				newWidth = clamped.width;
				newHeight = clamped.height;
			}
			newWidth = Math.max(activePanel.getMinWidth(), newWidth);
			newHeight = Math.max(activePanel.getMinHeight(), newHeight);
			Rectangle bounds = activePanel.getBounds();
			int maxWidth = Math.max(activePanel.getMinWidth(), Client.currentGameWidth - bounds.x);
			int maxHeight = Math.max(activePanel.getMinHeight(), Client.currentGameHeight - bounds.y);
			newWidth = Math.min(newWidth, maxWidth);
			newHeight = Math.min(newHeight, maxHeight);
			activePanel.setSize(newWidth, newHeight);
			activePanel.onResize(client);
		}

		mouseDownLastFrame = mouseDown;
	}

	public boolean handleMouseWheel(Client client, int mouseX, int mouseY, int rotation) {
		UiPanel hit = getTopmostPanelAt(mouseX, mouseY);
		if (!(hit instanceof TabPanel)) {
			return false;
		}
		if (!hit.isScrollable()) {
			return false;
		}
		TabPanel tabPanel = (TabPanel) hit;
		int interfaceId = Client.tabInterfaceIDs[tabPanel.getTabIndex()];
		if (interfaceId <= 0) {
			return false;
		}
		RSInterface rsInterface = RSInterface.interfaceCache[interfaceId];
		if (rsInterface == null) {
			return false;
		}
		tabPanel.scrollBy(rotation * 30, rsInterface, tabPanel.getBounds());
		return true;
	}

	public void resetLayout(Client client) {
		client.getUserSettings().clearRs3PanelLayouts();
		activePanel = null;
		dragging = false;
		mouseDownLastFrame = false;
		layoutWidth = -1;
		layoutHeight = -1;
		panels.clear();
		ensureRs3Layout(client);
	}

	public void saveLayout(Client client) {
		saveLayoutToSettings(client);
	}

	public UiPanel getPanel(int id) {
		for (UiPanel panel : panels) {
			if (panel.getId() == id) {
				return panel;
			}
		}
		return null;
	}

	public boolean isPanelVisible(int id) {
		UiPanel panel = getPanel(id);
		return panel != null && panel.isVisible();
	}

	public void togglePanelVisibility(int id) {
		UiPanel panel = getPanel(id);
		if (!(panel instanceof BasePanel)) {
			return;
		}
		BasePanel basePanel = (BasePanel) panel;
		boolean visible = !basePanel.isVisible();
		basePanel.setVisible(visible);
		if (visible) {
			bringToFront(panel);
		}
	}

	public boolean isDragging() {
		return dragging || resizing;
	}

	public boolean isMouseOverPanel(int mouseX, int mouseY) {
		return getTopmostPanelAt(mouseX, mouseY) != null;
	}

	private UiPanel getTopmostPanelAt(int mouseX, int mouseY) {
		for (int index = panels.size() - 1; index >= 0; index--) {
			UiPanel panel = panels.get(index);
			if (panel.isVisible() && panel.contains(mouseX, mouseY)) {
				return panel;
			}
		}
		return null;
	}

	public void bringToFront(UiPanel panel) {
		panels.remove(panel);
		panels.add(panel);
	}

	private void applySavedLayout(Client client) {
		Settings settings = Client.getUserSettings();
		if (settings == null) {
			return;
		}
		for (UiPanel panel : panels) {
			Settings.Rs3PanelLayout layout = settings.getRs3PanelLayouts().get(panel.getId());
			if (layout == null) {
				continue;
			}
			panel.getBounds().setSize(layout.getWidth(), layout.getHeight());
			int clampedX = clamp(layout.getX(), 0, Client.currentGameWidth - panel.getBounds().width);
			int clampedY = clamp(layout.getY(), 0, Client.currentGameHeight - panel.getBounds().height);
			panel.setPosition(clampedX, clampedY);
			if (panel instanceof BasePanel) {
				boolean visible = layout.isVisible() || !panel.isClosable();
				((BasePanel) panel).setVisible(visible);
			}
		}
	}

	private void saveLayoutToSettings(Client client) {
		Settings settings = Client.getUserSettings();
		if (settings == null) {
			return;
		}
		for (UiPanel panel : panels) {
			Rectangle bounds = panel.getBounds();
			settings.getRs3PanelLayouts().put(panel.getId(), new Settings.Rs3PanelLayout(
					bounds.x, bounds.y, bounds.width, bounds.height, panel.isVisible()));
		}
	}

	private void drawSelectionOutline(Rectangle bounds) {
		int highlight = 0xffd24a;
		DrawingArea.drawPixels(1, bounds.y, bounds.x, highlight, bounds.width);
		DrawingArea.drawPixels(1, bounds.y + bounds.height - 1, bounds.x, highlight, bounds.width);
		DrawingArea.drawPixels(bounds.height, bounds.y, bounds.x, highlight, 1);
		DrawingArea.drawPixels(bounds.height, bounds.y, bounds.x + bounds.width - 1, highlight, 1);
	}

	private static int clamp(int value, int min, int max) {
		if (value < min) {
			return min;
		}
		if (value > max) {
			return max;
		}
		return value;
	}

	private static final class PanelLayout {
		private static final int PANEL_WIDTH = 190;
		private static final int PANEL_HEIGHT = 260 + PANEL_HEADER_HEIGHT;
		private static final int PANEL_PADDING = 8;
		private static final int PANEL_MARGIN = 10;
		private static final int MINIMAP_PANEL_WIDTH = 200;
		private static final int MINIMAP_PANEL_HEIGHT = 200 + PANEL_HEADER_HEIGHT;
		private static final int COMPASS_SIZE = 36;
		private static final int ORB_SIZE = 52;
		private static final int XP_BUTTON_WIDTH = 24;
		private static final int XP_BUTTON_HEIGHT = 20;
		private static final int MONEY_POUCH_WIDTH = 70;
		private static final int MONEY_POUCH_HEIGHT = 34;
		private static final int WORLD_MAP_SIZE = 30;
		private static final int TELEPORT_WIDTH = 20;
		private static final int TELEPORT_HEIGHT = 20;
		private static final int XP_PANEL_WIDTH = 130;
		private static final int XP_PANEL_HEIGHT = 28;
		private static final int CHAT_PANEL_WIDTH = 516;
		private static final int CHAT_PANEL_HEIGHT = 165 + PANEL_HEADER_HEIGHT;
		private static final int TAB_BAR_PANEL_WIDTH = 76;
		private static final int TAB_BAR_PANEL_HEIGHT = 7 * 36 + PANEL_HEADER_HEIGHT;

		private static void populateRs3Panels(List<UiPanel> panels) {
			int baseX = Math.max(0, Client.currentGameWidth - PANEL_WIDTH - PANEL_MARGIN);
			int inventoryY = Math.max(0, Client.currentGameHeight - PANEL_HEIGHT - PANEL_MARGIN);
			int prayerY = inventoryY - PANEL_HEIGHT - PANEL_PADDING;
			int magicY = prayerY - PANEL_HEIGHT - PANEL_PADDING;

			if (magicY < PANEL_MARGIN) {
				int shiftDown = PANEL_MARGIN - magicY;
				magicY += shiftDown;
				prayerY += shiftDown;
				inventoryY += shiftDown;
			}

			int bottomOverflow = inventoryY + PANEL_HEIGHT + PANEL_MARGIN - Client.currentGameHeight;
			if (bottomOverflow > 0) {
				magicY -= bottomOverflow;
				prayerY -= bottomOverflow;
				inventoryY -= bottomOverflow;
			}

			panels.add(new InventoryPanel(PANEL_ID_INVENTORY, new Rectangle(baseX, inventoryY, PANEL_WIDTH, PANEL_HEIGHT)));
			panels.add(new PrayerPanel(PANEL_ID_PRAYER, new Rectangle(baseX, prayerY, PANEL_WIDTH, PANEL_HEIGHT)));
			panels.add(new MagicPanel(PANEL_ID_MAGIC, new Rectangle(baseX, magicY, PANEL_WIDTH, PANEL_HEIGHT)));
			panels.add(new EquipmentPanel(PANEL_ID_EQUIPMENT, new Rectangle(baseX - PANEL_WIDTH - PANEL_PADDING, inventoryY, PANEL_WIDTH, PANEL_HEIGHT)));
			panels.add(new TabPanel(PANEL_ID_QUEST, 0, new Rectangle(baseX - PANEL_WIDTH - PANEL_PADDING, prayerY, PANEL_WIDTH, PANEL_HEIGHT), "Quest", false));
			panels.add(new TabPanel(PANEL_ID_STATS, 1, new Rectangle(baseX - PANEL_WIDTH - PANEL_PADDING, magicY, PANEL_WIDTH, PANEL_HEIGHT), "Stats", false));
			panels.add(new TabPanel(PANEL_ID_SKILLS, 2, new Rectangle(baseX - PANEL_WIDTH - PANEL_PADDING * 2 - PANEL_WIDTH, inventoryY, PANEL_WIDTH, PANEL_HEIGHT), "Skills", false));
			panels.add(new TabPanel(PANEL_ID_CLAN, 7, new Rectangle(baseX - PANEL_WIDTH - PANEL_PADDING * 2 - PANEL_WIDTH, prayerY, PANEL_WIDTH, PANEL_HEIGHT), "Clan", false));
			panels.add(new TabPanel(PANEL_ID_FRIENDS, 8, new Rectangle(baseX - PANEL_WIDTH - PANEL_PADDING * 2 - PANEL_WIDTH, magicY, PANEL_WIDTH, PANEL_HEIGHT), "Friends", false));
			panels.add(new TabPanel(PANEL_ID_SETTINGS, 9, new Rectangle(baseX - PANEL_WIDTH - PANEL_PADDING * 3 - PANEL_WIDTH, inventoryY, PANEL_WIDTH, PANEL_HEIGHT), "Settings", false));
			panels.add(new TabPanel(PANEL_ID_EMOTES, 10, new Rectangle(baseX - PANEL_WIDTH - PANEL_PADDING * 3 - PANEL_WIDTH, prayerY, PANEL_WIDTH, PANEL_HEIGHT), "Emotes", false));
			panels.add(new TabPanel(PANEL_ID_MUSIC, 11, new Rectangle(baseX - PANEL_WIDTH - PANEL_PADDING * 3 - PANEL_WIDTH, magicY, PANEL_WIDTH, PANEL_HEIGHT), "Music", false));
			panels.add(new TabPanel(PANEL_ID_NOTES, 12, new Rectangle(baseX - PANEL_WIDTH - PANEL_PADDING * 4 - PANEL_WIDTH, inventoryY, PANEL_WIDTH, PANEL_HEIGHT), "Notes", false));
			panels.add(new TabPanel(PANEL_ID_LOGOUT, 13, new Rectangle(baseX - PANEL_WIDTH - PANEL_PADDING * 4 - PANEL_WIDTH, prayerY, PANEL_WIDTH, PANEL_HEIGHT), "Logout", false));

			int minimapX = Math.max(PANEL_MARGIN, Client.currentGameWidth - MINIMAP_PANEL_WIDTH - PANEL_MARGIN);
			int minimapY = PANEL_MARGIN;
			int orbsX = minimapX - PANEL_PADDING;
			int orbsContentY = minimapY + MINIMAP_PANEL_HEIGHT + PANEL_PADDING + PANEL_HEADER_HEIGHT;
			int chatX = PANEL_MARGIN;
			int chatY = Math.max(PANEL_MARGIN, Client.currentGameHeight - CHAT_PANEL_HEIGHT - PANEL_MARGIN);
			int tabBarX = Math.max(PANEL_MARGIN, minimapX - TAB_BAR_PANEL_WIDTH - PANEL_PADDING);
			int tabBarY = minimapY;
			panels.add(new MinimapBasePanel(PANEL_ID_MINIMAP_BASE, new Rectangle(minimapX, minimapY, MINIMAP_PANEL_WIDTH, MINIMAP_PANEL_HEIGHT)));
			panels.add(new CompassPanel(PANEL_ID_COMPASS, new Rectangle(minimapX + 6, minimapY + PANEL_HEADER_HEIGHT + 6, COMPASS_SIZE, COMPASS_SIZE)));
			panels.add(new HpOrbPanel(PANEL_ID_HP_ORB, new Rectangle(orbsX + 7, orbsContentY + 41, ORB_SIZE, ORB_SIZE)));
			panels.add(new PrayerOrbPanel(PANEL_ID_PRAYER_ORB, new Rectangle(orbsX + 7, orbsContentY + 75, ORB_SIZE, ORB_SIZE)));
			panels.add(new RunOrbPanel(PANEL_ID_RUN_ORB, new Rectangle(orbsX + 31, orbsContentY + 132, ORB_SIZE, 30)));
			panels.add(new SpecialOrbPanel(PANEL_ID_SPEC_ORB, new Rectangle(orbsX + 37, orbsContentY + 139, ORB_SIZE, ORB_SIZE)));
			panels.add(new XpOrbPanel(PANEL_ID_XP_ORB, new Rectangle(orbsX + 12, orbsContentY + 27, XP_BUTTON_WIDTH, XP_BUTTON_HEIGHT)));
			panels.add(new MoneyPouchPanel(PANEL_ID_MONEY_POUCH, new Rectangle(orbsX + 152, orbsContentY + 154, MONEY_POUCH_WIDTH, MONEY_POUCH_HEIGHT)));
			panels.add(new WorldMapPanel(PANEL_ID_WORLD_MAP, new Rectangle(orbsX + 183, orbsContentY + 143, WORLD_MAP_SIZE, WORLD_MAP_SIZE)));
			panels.add(new TeleportPanel(PANEL_ID_TELEPORT, new Rectangle(orbsX + 123, orbsContentY + 160, TELEPORT_WIDTH, TELEPORT_HEIGHT)));
			panels.add(new XpPanel(PANEL_ID_XP_PANEL, new Rectangle(Client.currentGameWidth - 365, PANEL_MARGIN, XP_PANEL_WIDTH, XP_PANEL_HEIGHT)));
			panels.add(new ChatPanel(PANEL_ID_CHAT, new Rectangle(chatX, chatY, CHAT_PANEL_WIDTH, CHAT_PANEL_HEIGHT)));
			panels.add(new TabBarPanel(PANEL_ID_TAB_BAR, new Rectangle(tabBarX, tabBarY, TAB_BAR_PANEL_WIDTH, TAB_BAR_PANEL_HEIGHT)));
		}
	}

	static class BasePanel implements UiPanel {
		private final int id;
		private final Rectangle bounds;
		private boolean visible;
		private final boolean draggable;
		private final String title;
		private final boolean resizable;
		private final int minWidth;
		private final int minHeight;
		private final boolean keepAspectRatio;

		BasePanel(int id, Rectangle bounds, boolean visible, boolean draggable, String title) {
			this(id, bounds, visible, draggable, title, false, bounds.width, bounds.height, false);
		}

		BasePanel(int id, Rectangle bounds, boolean visible, boolean draggable, String title,
				 boolean resizable, int minWidth, int minHeight, boolean keepAspectRatio) {
			this.id = id;
			this.bounds = bounds;
			this.visible = visible;
			this.draggable = draggable;
			this.title = title;
			this.resizable = resizable;
			this.minWidth = minWidth;
			this.minHeight = minHeight;
			this.keepAspectRatio = keepAspectRatio;
		}

		@Override
		public int getId() {
			return id;
		}

		@Override
		public Rectangle getBounds() {
			return bounds;
		}

		@Override
		public boolean isVisible() {
			return visible;
		}

		@Override
		public boolean draggable() {
			return draggable;
		}

		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public boolean contains(int mouseX, int mouseY) {
			return bounds.contains(mouseX, mouseY);
		}

		@Override
		public void setPosition(int x, int y) {
			bounds.setLocation(x, y);
		}

		@Override
		public void setSize(int width, int height) {
			bounds.setSize(width, height);
		}

		@Override
		public void draw(Client client) {

		}

		@Override
		public boolean handleMouse(Client client, int mouseX, int mouseY) {
			return false;
		}

		private void setVisible(boolean visible) {
			this.visible = visible;
		}

		@Override
		public boolean handleClick(Client client, int mouseX, int mouseY) {
			return false;
		}

		@Override
		public boolean resizable() {
			return resizable;
		}

		@Override
		public int getMinWidth() {
			return minWidth;
		}

		@Override
		public int getMinHeight() {
			return minHeight;
		}

		@Override
		public boolean keepAspectRatio() {
			return keepAspectRatio;
		}

		@Override
		public boolean drawsBackground() {
			return true;
		}

		@Override
		public boolean isClosable() {
			return true;
		}

		@Override
		public boolean isScrollable() {
			return true;
		}
	}

	static class TabPanel extends BasePanel {
		private final int tabIndex;
		private int scrollOffset;

		private TabPanel(int id, int tabIndex, Rectangle bounds, String title, boolean visible) {
			super(id, bounds, visible, true, title);
			this.tabIndex = tabIndex;
		}

		TabPanel(int id, int tabIndex, Rectangle bounds, String title, boolean visible, boolean resizable, int minWidth, int minHeight) {
			super(id, bounds, visible, true, title, resizable, minWidth, minHeight, false);
			this.tabIndex = tabIndex;
		}

		@Override
		public void draw(Client client) {
			int interfaceId = Client.tabInterfaceIDs[tabIndex];
			if (interfaceId <= 0) {
				return;
			}
			RSInterface rsInterface = RSInterface.interfaceCache[interfaceId];
			if (rsInterface == null) {
				return;
			}
			Rectangle bounds = getBounds();
			applyInterfaceBounds(rsInterface, bounds);
			int scrollPosition = getScrollPosition(rsInterface, bounds);
			int clipLeft = DrawingArea.topX;
			int clipTop = DrawingArea.topY;
			int clipRight = DrawingArea.bottomX;
			int clipBottom = DrawingArea.bottomY;

			DrawingArea.setDrawingArea(bounds.y + bounds.height, bounds.x, bounds.x + bounds.width, bounds.y + PANEL_HEADER_HEIGHT);
			client.pushUiOffset(bounds.x, bounds.y + PANEL_HEADER_HEIGHT);
			client.drawInterfaceWithOffset(scrollPosition, 0, rsInterface, 0);
			client.popUiOffset();
			DrawingArea.setDrawingArea(clipBottom, clipLeft, clipRight, clipTop);
			if (needsScroll(rsInterface, bounds)) {
				int scrollHeight = bounds.height - PANEL_HEADER_HEIGHT;
				int scrollMax = getContentHeight(rsInterface);
				client.drawScrollbar(scrollHeight, scrollOffset, bounds.y + PANEL_HEADER_HEIGHT, bounds.x + bounds.width - 16, scrollMax);
			}
		}

		@Override
		public boolean handleMouse(Client client, int mouseX, int mouseY) {
			int interfaceId = Client.tabInterfaceIDs[tabIndex];
			if (interfaceId <= 0) {
				return false;
			}
			RSInterface rsInterface = RSInterface.interfaceCache[interfaceId];
			if (rsInterface == null) {
				return false;
			}
			int adjustedMouseY = mouseY - PANEL_HEADER_HEIGHT;
			if (adjustedMouseY < 0) {
				return false;
			}
			Rectangle bounds = getBounds();
			client.pushUiOffset(bounds.x, bounds.y + PANEL_HEADER_HEIGHT);
			client.buildInterfaceMenuWithOffset(0, rsInterface, mouseX, 0, adjustedMouseY, getScrollPosition(rsInterface, bounds));
			client.popUiOffset();
			return true;
		}

		@Override
		public boolean handleClick(Client client, int mouseX, int mouseY) {
			return true;
		}

		@Override
		public boolean handleRightClick(Client client, int mouseX, int mouseY) {
			int interfaceId = Client.tabInterfaceIDs[tabIndex];
			if (interfaceId <= 0) {
				return false;
			}
			RSInterface rsInterface = RSInterface.interfaceCache[interfaceId];
			if (rsInterface == null) {
				return false;
			}
			int adjustedMouseY = mouseY - PANEL_HEADER_HEIGHT;
			if (adjustedMouseY < 0) {
				return false;
			}
			Rectangle bounds = getBounds();
			client.pushUiOffset(bounds.x, bounds.y + PANEL_HEADER_HEIGHT);
			client.buildInterfaceMenuWithOffset(0, rsInterface, mouseX, 0, adjustedMouseY, getScrollPosition(rsInterface, bounds));
			client.popUiOffset();
			return true;
		}

		private boolean needsScroll(RSInterface rsInterface, Rectangle bounds) {
			if (!isScrollable()) {
				return false;
			}
			return getContentHeight(rsInterface) > bounds.height - PANEL_HEADER_HEIGHT;
		}

		private int getContentHeight(RSInterface rsInterface) {
			return Math.max(rsInterface.height, rsInterface.scrollMax);
		}

		private int getScrollPosition(RSInterface rsInterface, Rectangle bounds) {
			if (!needsScroll(rsInterface, bounds)) {
				scrollOffset = 0;
				return 0;
			}
			int maxScroll = Math.max(0, getContentHeight(rsInterface) - (bounds.height - PANEL_HEADER_HEIGHT));
			scrollOffset = clamp(scrollOffset, 0, maxScroll);
			return scrollOffset;
		}

		void scrollBy(int delta, RSInterface rsInterface, Rectangle bounds) {
			if (!needsScroll(rsInterface, bounds)) {
				scrollOffset = 0;
				return;
			}
			int maxScroll = Math.max(0, getContentHeight(rsInterface) - (bounds.height - PANEL_HEADER_HEIGHT));
			scrollOffset = clamp(scrollOffset + delta, 0, maxScroll);
		}

		int getTabIndex() {
			return tabIndex;
		}

		private void applyInterfaceBounds(RSInterface rsInterface, Rectangle bounds) {
			rsInterface.width = bounds.width;
			rsInterface.height = bounds.height - PANEL_HEADER_HEIGHT;
		}
	}

	private static final class PrayerPanel extends TabPanel {
		private PrayerPanel(int id, Rectangle bounds) {
			super(id, 5, bounds, "Prayer", true, true, 160, 200 + PANEL_HEADER_HEIGHT);
		}
	}

	private static final class MagicPanel extends TabPanel {
		private MagicPanel(int id, Rectangle bounds) {
			super(id, 6, bounds, "Magic", true, true, 160, 200 + PANEL_HEADER_HEIGHT);
		}
	}

	private static final class EquipmentPanel extends TabPanel {
		private static final int EXPANDED_MIN_WIDTH = 240;
		private static final int EXPANDED_MIN_HEIGHT = 300;
		private static final int EQUIPMENT_INTERFACE_ID = 1644;
		private static final int CHARACTER_CHILD_ID = 15125;
		private static final int[] SLOT_CHILD_IDS = {
				1645, 1646, 1647, 1648, 1649, 1650, 1651, 1652, 1653, 1654, 1655
		};
		private boolean expandedMode;
		private final Map<Integer, Point> originalPositions = new HashMap<>();

		private EquipmentPanel(int id, Rectangle bounds) {
			super(id, 4, bounds, "Equipment", false, true, 160, 200 + PANEL_HEADER_HEIGHT);
		}

		@Override
		public void draw(Client client) {
			updateLayout(client);
			super.draw(client);
		}

		@Override
		public boolean handleMouse(Client client, int mouseX, int mouseY) {
			updateLayout(client);
			return super.handleMouse(client, mouseX, mouseY);
		}

		private void updateLayout(Client client) {
			if (!client.isRs3InterfaceStyleActive()) {
				restoreLayout();
				return;
			}
			int interfaceId = Client.tabInterfaceIDs[getTabIndex()];
			if (interfaceId != EQUIPMENT_INTERFACE_ID) {
				restoreLayout();
				return;
			}
			Rectangle bounds = getBounds();
			boolean shouldExpand = bounds.width >= EXPANDED_MIN_WIDTH && bounds.height >= EXPANDED_MIN_HEIGHT;
			if (shouldExpand == expandedMode) {
				return;
			}
			if (shouldExpand) {
				applyExpandedLayout(bounds);
			} else {
				restoreLayout();
			}
			expandedMode = shouldExpand;
		}

		private void applyExpandedLayout(Rectangle bounds) {
			RSInterface rsInterface = RSInterface.interfaceCache[EQUIPMENT_INTERFACE_ID];
			if (rsInterface == null || rsInterface.children == null) {
				return;
			}
			cacheOriginalPositions(rsInterface);
			int centerX = bounds.width / 2;
			int centerY = PANEL_HEADER_HEIGHT + (bounds.height - PANEL_HEADER_HEIGHT) / 2;
			setChildPosition(rsInterface, CHARACTER_CHILD_ID, centerX - 32, centerY - 70);
			setChildPosition(rsInterface, 1645, centerX - 18, centerY - 132);
			setChildPosition(rsInterface, 1646, centerX - 86, centerY - 90);
			setChildPosition(rsInterface, 1647, centerX - 18, centerY - 90);
			setChildPosition(rsInterface, 1648, centerX - 126, centerY - 20);
			setChildPosition(rsInterface, 1649, centerX - 18, centerY - 20);
			setChildPosition(rsInterface, 1650, centerX + 90, centerY - 20);
			setChildPosition(rsInterface, 1651, centerX - 18, centerY + 50);
			setChildPosition(rsInterface, 1652, centerX - 126, centerY + 50);
			setChildPosition(rsInterface, 1653, centerX - 18, centerY + 110);
			setChildPosition(rsInterface, 1654, centerX + 90, centerY + 110);
			setChildPosition(rsInterface, 1655, centerX + 90, centerY - 90);
		}

		private void restoreLayout() {
			RSInterface rsInterface = RSInterface.interfaceCache[EQUIPMENT_INTERFACE_ID];
			if (rsInterface == null || rsInterface.children == null || originalPositions.isEmpty()) {
				return;
			}
			for (Map.Entry<Integer, Point> entry : originalPositions.entrySet()) {
				setChildPosition(rsInterface, entry.getKey(), entry.getValue().x, entry.getValue().y);
			}
		}

		private void cacheOriginalPositions(RSInterface rsInterface) {
			if (!originalPositions.isEmpty()) {
				return;
			}
			cacheChildPosition(rsInterface, CHARACTER_CHILD_ID);
			for (int childId : SLOT_CHILD_IDS) {
				cacheChildPosition(rsInterface, childId);
			}
		}

		private void cacheChildPosition(RSInterface rsInterface, int childId) {
			for (int index = 0; index < rsInterface.children.length; index++) {
				if (rsInterface.children[index] == childId) {
					originalPositions.put(childId, new Point(rsInterface.childX[index], rsInterface.childY[index]));
					return;
				}
			}
		}

		private void setChildPosition(RSInterface rsInterface, int childId, int x, int y) {
			for (int index = 0; index < rsInterface.children.length; index++) {
				if (rsInterface.children[index] == childId) {
					rsInterface.childX[index] = x;
					rsInterface.childY[index] = y;
					return;
				}
			}
		}
	}

	private void drawPanelBackground(Client client, UiPanel panel) {
		int backgroundColor = PANEL_BACKGROUND;
		Settings settings = Client.getUserSettings();
		if (settings != null) {
			backgroundColor = settings.getRs3PanelBackgroundColor();
		}
		int headerColor = adjustColor(backgroundColor, 10);
		Rectangle bounds = panel.getBounds();
		DrawingArea.drawPixels(bounds.height, bounds.y, bounds.x, backgroundColor, bounds.width);
		DrawingArea.drawPixels(PANEL_HEADER_HEIGHT, bounds.y, bounds.x, headerColor, bounds.width);
		DrawingArea.drawPixels(1, bounds.y, bounds.x, PANEL_BORDER, bounds.width);
		DrawingArea.drawPixels(1, bounds.y + bounds.height - 1, bounds.x, PANEL_BORDER, bounds.width);
		DrawingArea.drawPixels(bounds.height, bounds.y, bounds.x, PANEL_BORDER, 1);
		DrawingArea.drawPixels(bounds.height, bounds.y, bounds.x + bounds.width - 1, PANEL_BORDER, 1);
		DrawingArea.drawPixels(1, bounds.y + PANEL_HEADER_HEIGHT, bounds.x, PANEL_BORDER, bounds.width);
		String title = panel.getTitle();
		if (title != null && !title.isEmpty()) {
			client.newSmallFont.drawBasicString(title, bounds.x + 6, bounds.y + 13, PANEL_TEXT, 0);
		}
	}

	private void drawResizeHandle(UiPanel panel) {
		if (!panel.resizable() || !panel.drawsBackground()) {
			return;
		}
		Rectangle bounds = panel.getBounds();
		int x = bounds.x + bounds.width - RESIZE_HANDLE_SIZE;
		int y = bounds.y + bounds.height - RESIZE_HANDLE_SIZE;
		DrawingArea.drawPixels(RESIZE_HANDLE_SIZE, y, x, 0x2a2a2a, RESIZE_HANDLE_SIZE);
		DrawingArea.drawPixels(1, y, x, 0x3a3a3a, RESIZE_HANDLE_SIZE);
		DrawingArea.drawPixels(1, y + RESIZE_HANDLE_SIZE - 1, x, 0x3a3a3a, RESIZE_HANDLE_SIZE);
		DrawingArea.drawPixels(RESIZE_HANDLE_SIZE, y, x, 0x3a3a3a, 1);
		DrawingArea.drawPixels(RESIZE_HANDLE_SIZE, y, x + RESIZE_HANDLE_SIZE - 1, 0x3a3a3a, 1);
	}

	private boolean isOnResizeHandle(UiPanel panel, int mouseX, int mouseY) {
		if (!panel.drawsBackground()) {
			return false;
		}
		Rectangle bounds = panel.getBounds();
		int handleX = bounds.x + bounds.width - RESIZE_HANDLE_SIZE;
		int handleY = bounds.y + bounds.height - RESIZE_HANDLE_SIZE;
		return mouseX >= handleX && mouseY >= handleY;
	}

	private void drawCloseButton(Client client, UiPanel panel) {
		Rectangle bounds = panel.getBounds();
		int x = bounds.x + bounds.width - CLOSE_BUTTON_SIZE - CLOSE_BUTTON_PADDING;
		int y = bounds.y + (PANEL_HEADER_HEIGHT - CLOSE_BUTTON_SIZE) / 2;
		DrawingArea.drawPixels(CLOSE_BUTTON_SIZE, y, x, 0x2a2a2a, CLOSE_BUTTON_SIZE);
		DrawingArea.drawPixels(1, y, x, 0x3a3a3a, CLOSE_BUTTON_SIZE);
		DrawingArea.drawPixels(1, y + CLOSE_BUTTON_SIZE - 1, x, 0x3a3a3a, CLOSE_BUTTON_SIZE);
		DrawingArea.drawPixels(CLOSE_BUTTON_SIZE, y, x, 0x3a3a3a, 1);
		DrawingArea.drawPixels(CLOSE_BUTTON_SIZE, y, x + CLOSE_BUTTON_SIZE - 1, 0x3a3a3a, 1);
		client.newSmallFont.drawCenteredString("X", x + CLOSE_BUTTON_SIZE / 2, y + 9, 0xffffff, 0);
	}

	private boolean isOnCloseButton(UiPanel panel, int mouseX, int mouseY) {
		Rectangle bounds = panel.getBounds();
		int x = bounds.x + bounds.width - CLOSE_BUTTON_SIZE - CLOSE_BUTTON_PADDING;
		int y = bounds.y + (PANEL_HEADER_HEIGHT - CLOSE_BUTTON_SIZE) / 2;
		return mouseX >= x && mouseX <= x + CLOSE_BUTTON_SIZE && mouseY >= y && mouseY <= y + CLOSE_BUTTON_SIZE;
	}

	private int adjustColor(int color, int delta) {
		int r = Math.min(255, Math.max(0, ((color >> 16) & 0xff) + delta));
		int g = Math.min(255, Math.max(0, ((color >> 8) & 0xff) + delta));
		int b = Math.min(255, Math.max(0, (color & 0xff) + delta));
		return (r << 16) | (g << 8) | b;
	}
}
