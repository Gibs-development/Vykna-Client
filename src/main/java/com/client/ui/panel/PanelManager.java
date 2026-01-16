package com.client.ui.panel;

import com.client.Client;
import com.client.DrawingArea;
import com.client.graphics.interfaces.RSInterface;
import com.client.sound.Sound;
import com.client.sound.SoundType;
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
	private static final int DOCK_PREVIEW_ALPHA = 100;
	private static final int DOCK_SNAP_THRESHOLD = 12;
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
	public static final int PANEL_ID_ACTION_BAR = 34;
	private final List<UiPanel> panels = new ArrayList<>();
	private final Map<Integer, Rectangle> preferredBounds = new HashMap<>();
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
	private Rectangle dragStartBounds;
	private Rectangle resizeStartBounds;
	private boolean invalidPlacement;
	private ResizeHandle resizeHandle;
	private DockCandidate dockCandidate;

	public void ensureRs3Layout(Client client) {
		if (layoutWidth == Client.currentGameWidth && layoutHeight == Client.currentGameHeight && !panels.isEmpty()) {
			return;
		}
		panels.clear();
		preferredBounds.clear();
		PanelLayout.populateRs3Panels(panels);
		for (UiPanel panel : panels) {
			preferredBounds.put(panel.getId(), new Rectangle(panel.getBounds()));
		}
		applySavedLayout(client);
		layoutWidth = Client.currentGameWidth;
		layoutHeight = Client.currentGameHeight;
	}

	public void drawPanels(Client client) {
		for (UiPanel panel : panels) {
			if (panel.isVisible()) {
				if (panel.drawsBackground()) {
					drawPanelBackground(client, panel);
				}
				panel.draw(client);
			}
		}
	}

	public void drawEditOverlays(Client client) {
		if (!client.isRs3EditModeActive()) {
			return;
		}
		for (UiPanel panel : panels) {
			if (!panel.isVisible()) {
				continue;
			}
			if (panel.isClosable() && isHeaderVisible(client, panel)) {
				drawCloseButton(client, panel);
			}
			drawResizeHandle(client, panel);
			if (panel == activePanel) {
				drawSelectionOutline(panel.getBounds());
			}
		}
		if (dockCandidate != null) {
			drawDockPreview(dockCandidate);
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
		boolean rs3Mode = client.isRs3InterfaceStyleActive();
		if (!mouseDown && mouseDownLastFrame && (dragging || resizing)) {
			if (rs3Mode && activePanel != null) {
				if (dragging && dockCandidate != null && isPlacementValid(dockCandidate.bounds, activePanel)) {
					activePanel.setPosition(dockCandidate.bounds.x, dockCandidate.bounds.y);
				}
				Rectangle target = new Rectangle(activePanel.getBounds());
				if (!isPlacementValid(target, activePanel)) {
					Rectangle resolved = resolveCollision(target, activePanel);
					if (resolved != null) {
						activePanel.setPosition(resolved.x, resolved.y);
					} else {
						Rectangle fallback = dragStartBounds != null ? dragStartBounds : resizeStartBounds;
						if (fallback != null) {
							activePanel.setPosition(fallback.x, fallback.y);
							activePanel.setSize(fallback.width, fallback.height);
						}
						Sound.getSound().playSound(1042, SoundType.SOUND, 0);
					}
				}
				preferredBounds.put(activePanel.getId(), new Rectangle(activePanel.getBounds()));
			}
			dragging = false;
			resizing = false;
			invalidPlacement = false;
			dockCandidate = null;
			resizeHandle = null;
			saveLayoutToSettings(client);
		}

		if (mouseDown && !mouseDownLastFrame) {
			invalidPlacement = false;
			UiPanel hit = getTopmostPanelAt(mouseX, mouseY);
			if (hit != null && hit.drawsBackground() && hit.isClosable()
					&& isHeaderVisible(client, hit) && isOnCloseButton(hit, mouseX, mouseY)) {
				if (hit instanceof BasePanel) {
					((BasePanel) hit).setVisible(false);
				}
				saveLayoutToSettings(client);
				activePanel = null;
				mouseDownLastFrame = mouseDown;
				return;
			}
			ResizeHandle handle = hit != null && hit.resizable() ? getResizeHandle(client, hit, mouseX, mouseY) : null;
			if (handle != null) {
				activePanel = hit;
				bringToFront(hit);
				Rectangle bounds = hit.getBounds();
				resizeStartBounds = new Rectangle(bounds);
				resizeStartX = mouseX;
				resizeStartY = mouseY;
				resizeStartWidth = bounds.width;
				resizeStartHeight = bounds.height;
				resizeHandle = handle;
				dockCandidate = null;
				resizing = true;
			} else if (hit != null && hit.draggable()) {
				activePanel = hit;
				bringToFront(hit);
				Rectangle bounds = hit.getBounds();
				dragStartBounds = new Rectangle(bounds);
				dragOffsetX = mouseX - bounds.x;
				dragOffsetY = mouseY - bounds.y;
				dockCandidate = null;
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
			dockCandidate = rs3Mode ? findDockCandidate(activePanel) : null;
			invalidPlacement = rs3Mode && !isPlacementValid(activePanel.getBounds(), activePanel);
			if (dockCandidate != null) {
				invalidPlacement = false;
			}
		}

		if (mouseDown && resizing && activePanel != null) {
			int deltaX = mouseX - resizeStartX;
			int deltaY = mouseY - resizeStartY;
			int newX = resizeStartBounds.x;
			int newY = resizeStartBounds.y;
			int newWidth = resizeStartWidth;
			int newHeight = resizeStartHeight;
			int right = resizeStartBounds.x + resizeStartBounds.width;
			int bottom = resizeStartBounds.y + resizeStartBounds.height;
			if (resizeHandle == ResizeHandle.TOP_LEFT) {
				newX = resizeStartBounds.x + deltaX;
				newY = resizeStartBounds.y + deltaY;
				newX = clamp(newX, 0, right - activePanel.getMinWidth());
				newY = clamp(newY, 0, bottom - activePanel.getMinHeight());
				newWidth = right - newX;
				newHeight = bottom - newY;
			} else if (resizeHandle == ResizeHandle.TOP_RIGHT) {
				newY = resizeStartBounds.y + deltaY;
				newY = clamp(newY, 0, bottom - activePanel.getMinHeight());
				newWidth = resizeStartWidth + deltaX;
				int maxWidth = Math.max(activePanel.getMinWidth(), Client.currentGameWidth - resizeStartBounds.x);
				newWidth = clamp(newWidth, activePanel.getMinWidth(), maxWidth);
				newHeight = bottom - newY;
			}
			if (activePanel.keepAspectRatio()) {
				int size = Math.max(newWidth, newHeight);
				newWidth = size;
				newHeight = size;
				if (resizeHandle == ResizeHandle.TOP_LEFT) {
					newX = right - newWidth;
					newY = bottom - newHeight;
				} else if (resizeHandle == ResizeHandle.TOP_RIGHT) {
					newY = bottom - newHeight;
				}
			}
			if (activePanel instanceof InventoryPanel) {
				Dimension clamped = ((InventoryPanel) activePanel).clampSizeForResize(newWidth, newHeight, client);
				newWidth = clamped.width;
				newHeight = clamped.height;
				if (resizeHandle == ResizeHandle.TOP_LEFT) {
					newX = right - newWidth;
					newY = bottom - newHeight;
				} else if (resizeHandle == ResizeHandle.TOP_RIGHT) {
					newY = bottom - newHeight;
				}
			}
			newWidth = Math.max(activePanel.getMinWidth(), newWidth);
			newHeight = Math.max(activePanel.getMinHeight(), newHeight);
			newX = clamp(newX, 0, Client.currentGameWidth - newWidth);
			newY = clamp(newY, 0, Client.currentGameHeight - newHeight);
			if (resizeHandle == ResizeHandle.TOP_LEFT) {
				newWidth = right - newX;
				newHeight = bottom - newY;
			} else if (resizeHandle == ResizeHandle.TOP_RIGHT) {
				newHeight = bottom - newY;
			}
			newWidth = clamp(newWidth, activePanel.getMinWidth(), Client.currentGameWidth - newX);
			newHeight = clamp(newHeight, activePanel.getMinHeight(), Client.currentGameHeight - newY);
			activePanel.setPosition(newX, newY);
			activePanel.setSize(newWidth, newHeight);
			activePanel.onResize(client);
			invalidPlacement = rs3Mode && !isPlacementValid(activePanel.getBounds(), activePanel);
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
		tabPanel.scrollBy(rotation * 30, rsInterface, tabPanel.getContentBounds(client));
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
		preferredBounds.clear();
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
				preferredBounds.put(panel.getId(), new Rectangle(panel.getBounds()));
				continue;
			}
			Rectangle preferred = new Rectangle(layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight());
			preferredBounds.put(panel.getId(), preferred);
			int width = clamp(preferred.width, panel.getMinWidth(), Client.currentGameWidth);
			int height = clamp(preferred.height, panel.getMinHeight(), Client.currentGameHeight);
			panel.getBounds().setSize(width, height);
			int clampedX = clamp(preferred.x, 0, Client.currentGameWidth - panel.getBounds().width);
			int clampedY = clamp(preferred.y, 0, Client.currentGameHeight - panel.getBounds().height);
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
			Rectangle bounds = preferredBounds.getOrDefault(panel.getId(), panel.getBounds());
			settings.getRs3PanelLayouts().put(panel.getId(), new Settings.Rs3PanelLayout(
					bounds.x, bounds.y, bounds.width, bounds.height, panel.isVisible()));
		}
	}

	private void drawSelectionOutline(Rectangle bounds) {
		int highlight = invalidPlacement ? 0xd1362b : 0xffd24a;
		DrawingArea.drawPixels(1, bounds.y, bounds.x, highlight, bounds.width);
		DrawingArea.drawPixels(1, bounds.y + bounds.height - 1, bounds.x, highlight, bounds.width);
		DrawingArea.drawPixels(bounds.height, bounds.y, bounds.x, highlight, 1);
		DrawingArea.drawPixels(bounds.height, bounds.y, bounds.x + bounds.width - 1, highlight, 1);
	}

	private boolean isPlacementValid(Rectangle bounds, UiPanel ignore) {
		if (bounds.x < 0 || bounds.y < 0) {
			return false;
		}
		if (bounds.x + bounds.width > Client.currentGameWidth) {
			return false;
		}
		if (bounds.y + bounds.height > Client.currentGameHeight) {
			return false;
		}
		for (UiPanel panel : panels) {
			if (panel == ignore || !panel.isVisible()) {
				continue;
			}
			if (bounds.intersects(panel.getBounds())) {
				return false;
			}
		}
		return true;
	}

	private Rectangle resolveCollision(Rectangle bounds, UiPanel ignore) {
		if (isPlacementValid(bounds, ignore)) {
			return bounds;
		}
		int step = 8;
		int maxRadius = Math.max(Client.currentGameWidth, Client.currentGameHeight);
		int baseX = bounds.x;
		int baseY = bounds.y;
		for (int radius = step; radius <= maxRadius; radius += step) {
			int left = baseX - radius;
			int right = baseX + radius;
			int top = baseY - radius;
			int bottom = baseY + radius;
			Rectangle candidate = new Rectangle(bounds);
			int[] xs = { left, baseX, right, baseX };
			int[] ys = { baseY, top, baseY, bottom };
			for (int index = 0; index < xs.length; index++) {
				candidate.setLocation(xs[index], ys[index]);
				candidate.x = clamp(candidate.x, 0, Client.currentGameWidth - candidate.width);
				candidate.y = clamp(candidate.y, 0, Client.currentGameHeight - candidate.height);
				if (isPlacementValid(candidate, ignore)) {
					return new Rectangle(candidate);
				}
			}
		}
		return null;
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
		private static final int ACTION_BAR_WIDTH = 420;
		private static final int ACTION_BAR_HEIGHT = 70;
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
			panels.add(new ActionBarPanel(PANEL_ID_ACTION_BAR, new Rectangle(
					Math.max(PANEL_MARGIN, (Client.currentGameWidth - ACTION_BAR_WIDTH) / 2),
					Math.max(PANEL_MARGIN, Client.currentGameHeight - ACTION_BAR_HEIGHT - PANEL_MARGIN),
					ACTION_BAR_WIDTH, ACTION_BAR_HEIGHT)));
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
			Rectangle contentBounds = getContentBounds(client);
			updateInterfaceLayout(client, rsInterface, contentBounds);
			int scrollPosition = getScrollPosition(rsInterface, contentBounds);
			int clipLeft = DrawingArea.topX;
			int clipTop = DrawingArea.topY;
			int clipRight = DrawingArea.bottomX;
			int clipBottom = DrawingArea.bottomY;

			DrawingArea.setDrawingArea(contentBounds.y + contentBounds.height, contentBounds.x, contentBounds.x + contentBounds.width, contentBounds.y);
			client.pushUiOffset(contentBounds.x, contentBounds.y);
			client.drawInterfaceWithOffset(scrollPosition, 0, rsInterface, 0);
			client.popUiOffset();
			DrawingArea.setDrawingArea(clipBottom, clipLeft, clipRight, clipTop);
			if (needsScroll(rsInterface, contentBounds)) {
				int scrollHeight = contentBounds.height;
				int scrollMax = getContentHeight(rsInterface);
				client.drawScrollbar(scrollHeight, scrollOffset, contentBounds.y, contentBounds.x + contentBounds.width - 16, scrollMax);
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
			Rectangle contentBounds = getContentBounds(client);
			int adjustedMouseY = mouseY - (contentBounds.y - getBounds().y);
			int adjustedMouseX = mouseX - (contentBounds.x - getBounds().x);
			if (adjustedMouseY < 0 || adjustedMouseX < 0) {
				return false;
			}
			updateInterfaceLayout(client, rsInterface, contentBounds);
			client.pushUiOffset(contentBounds.x, contentBounds.y);
			client.buildInterfaceMenuWithOffset(0, rsInterface, adjustedMouseX, 0, adjustedMouseY, getScrollPosition(rsInterface, contentBounds));
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
			Rectangle contentBounds = getContentBounds(client);
			int adjustedMouseY = mouseY - (contentBounds.y - getBounds().y);
			int adjustedMouseX = mouseX - (contentBounds.x - getBounds().x);
			if (adjustedMouseY < 0 || adjustedMouseX < 0) {
				return false;
			}
			updateInterfaceLayout(client, rsInterface, contentBounds);
			client.pushUiOffset(contentBounds.x, contentBounds.y);
			client.buildInterfaceMenuWithOffset(0, rsInterface, adjustedMouseX, 0, adjustedMouseY, getScrollPosition(rsInterface, contentBounds));
			client.popUiOffset();
			return true;
		}

		private boolean needsScroll(RSInterface rsInterface, Rectangle bounds) {
			if (!isScrollable()) {
				return false;
			}
			return getContentHeight(rsInterface) > bounds.height;
		}

		private int getContentHeight(RSInterface rsInterface) {
			return Math.max(rsInterface.height, rsInterface.scrollMax);
		}

		protected int getInterfaceContentHeight(RSInterface rsInterface) {
			int maxHeight = rsInterface.height;
			if (rsInterface.children == null) {
				return maxHeight;
			}
			for (int index = 0; index < rsInterface.children.length; index++) {
				RSInterface child = RSInterface.interfaceCache[rsInterface.children[index]];
				if (child == null) {
					continue;
				}
				int childBottom = rsInterface.childY[index] + child.height + child.anInt265;
				if (childBottom > maxHeight) {
					maxHeight = childBottom;
				}
			}
			return maxHeight;
		}

		private int getScrollPosition(RSInterface rsInterface, Rectangle bounds) {
			if (!needsScroll(rsInterface, bounds)) {
				scrollOffset = 0;
				return 0;
			}
			int maxScroll = Math.max(0, getContentHeight(rsInterface) - bounds.height);
			scrollOffset = clamp(scrollOffset, 0, maxScroll);
			return scrollOffset;
		}

		void scrollBy(int delta, RSInterface rsInterface, Rectangle bounds) {
			if (!needsScroll(rsInterface, bounds)) {
				scrollOffset = 0;
				return;
			}
			int maxScroll = Math.max(0, getContentHeight(rsInterface) - bounds.height);
			scrollOffset = clamp(scrollOffset + delta, 0, maxScroll);
		}

		int getTabIndex() {
			return tabIndex;
		}

		protected void updateInterfaceLayout(Client client, RSInterface rsInterface, Rectangle bounds) {
			rsInterface.width = bounds.width;
			rsInterface.height = bounds.height;
		}

		protected int getContentPadding(Client client, Rectangle bounds) {
			return 0;
		}

		Rectangle getContentBounds(Client client) {
			Rectangle bounds = getBounds();
			int headerHeight = PanelManager.getPanelHeaderHeight(client, this);
			int padding = getContentPadding(client, bounds);
			return new Rectangle(
					bounds.x + padding,
					bounds.y + headerHeight + padding,
					Math.max(0, bounds.width - padding * 2),
					Math.max(0, bounds.height - headerHeight - padding * 2));
		}
	}

	private static final class PrayerPanel extends TabPanel {
		private PrayerPanel(int id, Rectangle bounds) {
			super(id, 5, bounds, "Prayer", true, true, 120, 160 + PANEL_HEADER_HEIGHT);
		}

		@Override
		protected void updateInterfaceLayout(Client client, RSInterface rsInterface, Rectangle bounds) {
			super.updateInterfaceLayout(client, rsInterface, bounds);
			rsInterface.scrollMax = Math.max(rsInterface.height, getInterfaceContentHeight(rsInterface));
		}

		@Override
		protected int getContentPadding(Client client, Rectangle bounds) {
			int headerHeight = PanelManager.getPanelHeaderHeight(client, this);
			int availableHeight = bounds.height - headerHeight;
			return (availableHeight < 210 || bounds.width < 170) ? 2 : 6;
		}
	}

	private static final class MagicPanel extends TabPanel {
		private MagicPanel(int id, Rectangle bounds) {
			super(id, 6, bounds, "Magic", true, true, 120, 160 + PANEL_HEADER_HEIGHT);
		}

		@Override
		protected void updateInterfaceLayout(Client client, RSInterface rsInterface, Rectangle bounds) {
			super.updateInterfaceLayout(client, rsInterface, bounds);
			rsInterface.scrollMax = Math.max(rsInterface.height, getInterfaceContentHeight(rsInterface));
		}

		@Override
		protected int getContentPadding(Client client, Rectangle bounds) {
			int headerHeight = PanelManager.getPanelHeaderHeight(client, this);
			int availableHeight = bounds.height - headerHeight;
			return (availableHeight < 210 || bounds.width < 170) ? 2 : 6;
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
				int headerHeight = PanelManager.getPanelHeaderHeight(client, this);
				applyExpandedLayout(bounds, headerHeight);
			} else {
				restoreLayout();
			}
			expandedMode = shouldExpand;
		}

		private void applyExpandedLayout(Rectangle bounds, int headerHeight) {
			RSInterface rsInterface = RSInterface.interfaceCache[EQUIPMENT_INTERFACE_ID];
			if (rsInterface == null || rsInterface.children == null) {
				return;
			}
			cacheOriginalPositions(rsInterface);
			int centerX = bounds.width / 2;
			int centerY = headerHeight + (bounds.height - headerHeight) / 2;
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
		int backgroundAlpha = 255;
		Settings settings = Client.getUserSettings();
		if (settings != null) {
			backgroundColor = settings.getRs3PanelBackgroundColor();
			if (client.isRs3InterfaceStyleActive()) {
				int transparency = settings.getRs3InterfaceTransparency();
				backgroundAlpha = 255 - (transparency * 155 / 60);
				backgroundAlpha = clamp(backgroundAlpha, 100, 255);
			}
		}
		int headerColor = adjustColor(backgroundColor, 10);
		Rectangle bounds = panel.getBounds();
		int headerHeight = getPanelHeaderHeight(client, panel);
		if (backgroundAlpha < 255) {
			DrawingArea.drawAlphaPixels(bounds.x, bounds.y, bounds.width, bounds.height, backgroundColor, backgroundAlpha);
			if (headerHeight > 0) {
				DrawingArea.drawAlphaPixels(bounds.x, bounds.y, bounds.width, headerHeight, headerColor, backgroundAlpha);
			}
		} else {
			DrawingArea.drawPixels(bounds.height, bounds.y, bounds.x, backgroundColor, bounds.width);
			if (headerHeight > 0) {
				DrawingArea.drawPixels(headerHeight, bounds.y, bounds.x, headerColor, bounds.width);
			}
		}
		DrawingArea.drawPixels(1, bounds.y, bounds.x, PANEL_BORDER, bounds.width);
		DrawingArea.drawPixels(1, bounds.y + bounds.height - 1, bounds.x, PANEL_BORDER, bounds.width);
		DrawingArea.drawPixels(bounds.height, bounds.y, bounds.x, PANEL_BORDER, 1);
		DrawingArea.drawPixels(bounds.height, bounds.y, bounds.x + bounds.width - 1, PANEL_BORDER, 1);
		if (headerHeight > 0) {
			DrawingArea.drawPixels(1, bounds.y + headerHeight, bounds.x, PANEL_BORDER, bounds.width);
			String title = panel.getTitle();
			if (client.isRs3EditModeActive() && title != null && !title.isEmpty()) {
				client.newSmallFont.drawBasicString(title, bounds.x + 6, bounds.y + 13, PANEL_TEXT, 0);
			}
		}
	}

	private void drawResizeHandle(Client client, UiPanel panel) {
		if (!panel.resizable() || !panel.drawsBackground() || !isHeaderVisible(client, panel)) {
			return;
		}
		Rectangle bounds = panel.getBounds();
		drawCornerHandle(bounds.x, bounds.y);
		drawCornerHandle(getRightResizeHandleX(client, panel), bounds.y);
	}

	private ResizeHandle getResizeHandle(Client client, UiPanel panel, int mouseX, int mouseY) {
		if (!panel.drawsBackground()) {
			return null;
		}
		Rectangle bounds = panel.getBounds();
		int leftX = bounds.x;
		int rightX = getRightResizeHandleX(client, panel);
		int topY = bounds.y;
		if (mouseY >= topY && mouseY <= topY + RESIZE_HANDLE_SIZE) {
			if (mouseX >= leftX && mouseX <= leftX + RESIZE_HANDLE_SIZE) {
				return ResizeHandle.TOP_LEFT;
			}
			if (mouseX >= rightX && mouseX <= rightX + RESIZE_HANDLE_SIZE) {
				return ResizeHandle.TOP_RIGHT;
			}
		}
		return null;
	}

	private void drawCornerHandle(int x, int y) {
		DrawingArea.drawPixels(RESIZE_HANDLE_SIZE, y, x, 0x2a2a2a, RESIZE_HANDLE_SIZE);
		DrawingArea.drawPixels(1, y, x, 0x3a3a3a, RESIZE_HANDLE_SIZE);
		DrawingArea.drawPixels(1, y + RESIZE_HANDLE_SIZE - 1, x, 0x3a3a3a, RESIZE_HANDLE_SIZE);
		DrawingArea.drawPixels(RESIZE_HANDLE_SIZE, y, x, 0x3a3a3a, 1);
		DrawingArea.drawPixels(RESIZE_HANDLE_SIZE, y, x + RESIZE_HANDLE_SIZE - 1, 0x3a3a3a, 1);
	}

	private int getRightResizeHandleX(Client client, UiPanel panel) {
		Rectangle bounds = panel.getBounds();
		int rightX = bounds.x + bounds.width - RESIZE_HANDLE_SIZE;
		if (panel.isClosable() && isHeaderVisible(client, panel)) {
			int closeX = bounds.x + bounds.width - CLOSE_BUTTON_SIZE - CLOSE_BUTTON_PADDING;
			int adjusted = closeX - RESIZE_HANDLE_SIZE;
			if (adjusted >= bounds.x + RESIZE_HANDLE_SIZE) {
				rightX = Math.min(rightX, adjusted);
			}
		}
		return rightX;
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

	static int getPanelHeaderHeight(Client client, UiPanel panel) {
		if (client == null || !client.isRs3InterfaceStyleActive()) {
			return PANEL_HEADER_HEIGHT;
		}
		if (client.isRs3EditModeActive()) {
			return PANEL_HEADER_HEIGHT;
		}
		return panel != null && !panel.isClosable() ? PANEL_HEADER_HEIGHT : 0;
	}

	private static boolean isHeaderVisible(Client client, UiPanel panel) {
		return getPanelHeaderHeight(client, panel) > 0;
	}

	private DockCandidate findDockCandidate(UiPanel panel) {
		Rectangle bounds = panel.getBounds();
		DockCandidate best = null;
		int bestDistance = DOCK_SNAP_THRESHOLD + 1;
		Rectangle screen = new Rectangle(0, 0, Client.currentGameWidth, Client.currentGameHeight);
		best = pickDockCandidate(best, screen, bounds, DockSide.LEFT, Math.abs(bounds.x), bestDistance);
		bestDistance = best == null ? bestDistance : best.distance;
		best = pickDockCandidate(best, screen, bounds, DockSide.RIGHT, Math.abs((bounds.x + bounds.width) - screen.width), bestDistance);
		bestDistance = best == null ? bestDistance : best.distance;
		best = pickDockCandidate(best, screen, bounds, DockSide.TOP, Math.abs(bounds.y), bestDistance);
		bestDistance = best == null ? bestDistance : best.distance;
		best = pickDockCandidate(best, screen, bounds, DockSide.BOTTOM, Math.abs((bounds.y + bounds.height) - screen.height), bestDistance);
		bestDistance = best == null ? bestDistance : best.distance;
		for (UiPanel other : panels) {
			if (other == panel || !other.isVisible()) {
				continue;
			}
			Rectangle target = other.getBounds();
			if (overlapsVertically(bounds, target)) {
				best = pickDockCandidate(best, target, bounds, DockSide.LEFT, Math.abs(bounds.x - (target.x + target.width)), bestDistance);
				bestDistance = best == null ? bestDistance : best.distance;
				best = pickDockCandidate(best, target, bounds, DockSide.RIGHT, Math.abs(bounds.x + bounds.width - target.x), bestDistance);
				bestDistance = best == null ? bestDistance : best.distance;
			}
			if (overlapsHorizontally(bounds, target)) {
				best = pickDockCandidate(best, target, bounds, DockSide.TOP, Math.abs(bounds.y - (target.y + target.height)), bestDistance);
				bestDistance = best == null ? bestDistance : best.distance;
				best = pickDockCandidate(best, target, bounds, DockSide.BOTTOM, Math.abs(bounds.y + bounds.height - target.y), bestDistance);
				bestDistance = best == null ? bestDistance : best.distance;
			}
		}
		if (best == null || !isPlacementValid(best.bounds, panel)) {
			return null;
		}
		return best;
	}

	private DockCandidate pickDockCandidate(DockCandidate current, Rectangle target, Rectangle bounds, DockSide side, int distance, int bestDistance) {
		if (distance > DOCK_SNAP_THRESHOLD || distance >= bestDistance) {
			return current;
		}
		Rectangle snap = new Rectangle(bounds);
		switch (side) {
			case LEFT:
				snap.x = target.x - bounds.width;
				if (target.x == 0) {
					snap.x = 0;
				}
				break;
			case RIGHT:
				snap.x = target.x + target.width;
				if (target.width == Client.currentGameWidth) {
					snap.x = Client.currentGameWidth - bounds.width;
				}
				break;
			case TOP:
				snap.y = target.y - bounds.height;
				if (target.y == 0) {
					snap.y = 0;
				}
				break;
			case BOTTOM:
				snap.y = target.y + target.height;
				if (target.height == Client.currentGameHeight) {
					snap.y = Client.currentGameHeight - bounds.height;
				}
				break;
			default:
				break;
		}
		snap.x = clamp(snap.x, 0, Client.currentGameWidth - bounds.width);
		snap.y = clamp(snap.y, 0, Client.currentGameHeight - bounds.height);
		return new DockCandidate(snap, distance);
	}

	private boolean overlapsVertically(Rectangle a, Rectangle b) {
		return a.y < b.y + b.height && a.y + a.height > b.y;
	}

	private boolean overlapsHorizontally(Rectangle a, Rectangle b) {
		return a.x < b.x + b.width && a.x + a.width > b.x;
	}

	private void drawDockPreview(DockCandidate candidate) {
		if (candidate == null || candidate.bounds == null) {
			return;
		}
		if (DrawingArea.pixels == null) {
			return;
		}

		// Clamp to the current raster. During live resize, width/height can be briefly inconsistent,
		// so also clamp height against pixels.length / width.
		int rasterW = DrawingArea.width;
		if (rasterW <= 0) {
			return;
		}

		int rasterH = DrawingArea.height;
		int maxHFromPixels = DrawingArea.pixels.length / rasterW;
		if (maxHFromPixels <= 0) {
			return;
		}
		if (rasterH > maxHFromPixels) {
			rasterH = maxHFromPixels;
		}

		Rectangle b = candidate.bounds;

		int x = b.x;
		int y = b.y;
		int w = b.width;
		int h = b.height;

		if (w <= 0 || h <= 0) {
			return;
		}

		// Clip left/top
		if (x < 0) {
			w += x;
			x = 0;
		}
		if (y < 0) {
			h += y;
			y = 0;
		}

		// Clip right/bottom
		if (x + w > rasterW) {
			w = rasterW - x;
		}
		if (y + h > rasterH) {
			h = rasterH - y;
		}

		if (w <= 0 || h <= 0) {
			return;
		}

		// Draw preview safely
		DrawingArea.drawAlphaPixels(x, y, w, h, 0xffd24a, DOCK_PREVIEW_ALPHA);

		// Outline should match the clamped preview (not the original b that may be off-screen)
		drawSelectionOutline(new Rectangle(x, y, w, h));
	}


	private enum ResizeHandle {
		TOP_LEFT,
		TOP_RIGHT
	}

	private enum DockSide {
		LEFT,
		RIGHT,
		TOP,
		BOTTOM
	}

	private static final class DockCandidate {
		private final Rectangle bounds;
		private final int distance;

		private DockCandidate(Rectangle bounds, int distance) {
			this.bounds = bounds;
			this.distance = distance;
		}
	}
}
