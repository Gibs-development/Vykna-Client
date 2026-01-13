package com.client.ui.panel;

import com.client.Client;
import com.client.DrawingArea;
import com.client.graphics.interfaces.RSInterface;
import com.client.utilities.settings.Settings;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class PanelManager {
	private static final int PANEL_HEADER_HEIGHT = 18;
	private static final int PANEL_BACKGROUND = 0x141414;
	private static final int PANEL_HEADER = 0x1c1c1c;
	private static final int PANEL_BORDER = 0x2c2c2c;
	private static final int PANEL_TEXT = 0xffffff;
	private final List<UiPanel> panels = new ArrayList<>();
	private int layoutWidth = -1;
	private int layoutHeight = -1;
	private UiPanel activePanel;
	private boolean dragging;
	private int dragOffsetX;
	private int dragOffsetY;
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
				drawPanelBackground(client, panel);
				panel.draw(client);
				if (editMode && panel == activePanel) {
					drawSelectionOutline(panel.getBounds());
				}
			}
		}
	}

	public boolean handleMouse(Client client, int mouseX, int mouseY) {
		for (int index = panels.size() - 1; index >= 0; index--) {
			UiPanel panel = panels.get(index);
			if (!panel.isVisible()) {
				continue;
			}
			Rectangle bounds = panel.getBounds();
			if (panel.contains(mouseX, mouseY)) {
				return panel.handleMouse(client, mouseX - bounds.x, mouseY - bounds.y);
			}
		}
		return false;
	}

	public void handleEditModeInput(Client client, int mouseX, int mouseY, boolean mouseDown) {
		if (!mouseDown && mouseDownLastFrame && dragging) {
			dragging = false;
			saveLayoutToSettings(client);
		}

		if (mouseDown && !mouseDownLastFrame) {
			UiPanel hit = getTopmostPanelAt(mouseX, mouseY);
			if (hit != null && hit.draggable()) {
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

		mouseDownLastFrame = mouseDown;
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

	private UiPanel getTopmostPanelAt(int mouseX, int mouseY) {
		for (int index = panels.size() - 1; index >= 0; index--) {
			UiPanel panel = panels.get(index);
			if (panel.isVisible() && panel.contains(mouseX, mouseY)) {
				return panel;
			}
		}
		return null;
	}

	private void bringToFront(UiPanel panel) {
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
				((BasePanel) panel).setVisible(layout.isVisible());
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
		DrawingArea.drawPixels(1, bounds.y, bounds.x, 0x00ffff, bounds.width);
		DrawingArea.drawPixels(1, bounds.y + bounds.height - 1, bounds.x, 0x00ffff, bounds.width);
		DrawingArea.drawPixels(bounds.height, bounds.y, bounds.x, 0x00ffff, 1);
		DrawingArea.drawPixels(bounds.height, bounds.y, bounds.x + bounds.width - 1, 0x00ffff, 1);
	}

	private int clamp(int value, int min, int max) {
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
		private static final int PANEL_HEIGHT = 260;
		private static final int PANEL_PADDING = 8;
		private static final int PANEL_MARGIN = 10;

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

			panels.add(new InventoryPanel(1, new Rectangle(baseX, inventoryY, PANEL_WIDTH, PANEL_HEIGHT)));
			panels.add(new PrayerPanel(2, new Rectangle(baseX, prayerY, PANEL_WIDTH, PANEL_HEIGHT)));
			panels.add(new MagicPanel(3, new Rectangle(baseX, magicY, PANEL_WIDTH, PANEL_HEIGHT)));
			panels.add(new EquipmentPanel(4, new Rectangle(baseX - PANEL_WIDTH - PANEL_PADDING, inventoryY, PANEL_WIDTH, PANEL_HEIGHT)));
		}
	}

	private abstract static class BasePanel implements UiPanel {
		private final int id;
		private final Rectangle bounds;
		private boolean visible;
		private final boolean draggable;

		private BasePanel(int id, Rectangle bounds, boolean visible, boolean draggable) {
			this.id = id;
			this.bounds = bounds;
			this.visible = visible;
			this.draggable = draggable;
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
		public boolean contains(int mouseX, int mouseY) {
			return bounds.contains(mouseX, mouseY);
		}

		@Override
		public void setPosition(int x, int y) {
			bounds.setLocation(x, y);
		}

		private void setVisible(boolean visible) {
			this.visible = visible;
		}
	}

	private abstract static class TabPanel extends BasePanel {
		private final int tabIndex;
		private final String title;

		private TabPanel(int id, int tabIndex, Rectangle bounds, String title) {
			super(id, bounds, true, true);
			this.tabIndex = tabIndex;
			this.title = title;
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
			int clipLeft = DrawingArea.topX;
			int clipTop = DrawingArea.topY;
			int clipRight = DrawingArea.bottomX;
			int clipBottom = DrawingArea.bottomY;

			DrawingArea.setDrawingArea(bounds.y + bounds.height, bounds.x, bounds.x + bounds.width, bounds.y + PANEL_HEADER_HEIGHT);
			client.pushUiOffset(bounds.x, bounds.y + PANEL_HEADER_HEIGHT);
			client.drawInterfaceWithOffset(0, 0, rsInterface, 0);
			client.popUiOffset();
			DrawingArea.setDrawingArea(clipBottom, clipLeft, clipRight, clipTop);
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
			client.buildInterfaceMenuWithOffset(0, rsInterface, mouseX, 0, adjustedMouseY, 0);
			client.popUiOffset();
			return true;
		}
	}

	private static final class InventoryPanel extends TabPanel {
		private InventoryPanel(int id, Rectangle bounds) {
			super(id, 3, bounds, "Inventory");
		}
	}

	private static final class PrayerPanel extends TabPanel {
		private PrayerPanel(int id, Rectangle bounds) {
			super(id, 5, bounds, "Prayer");
		}
	}

	private static final class MagicPanel extends TabPanel {
		private MagicPanel(int id, Rectangle bounds) {
			super(id, 6, bounds, "Magic");
		}
	}

	private static final class EquipmentPanel extends BasePanel {
		private EquipmentPanel(int id, Rectangle bounds) {
			super(id, bounds, false, false);
		}

		@Override
		public void draw(Client client) {
		}

		@Override
		public boolean handleMouse(Client client, int mouseX, int mouseY) {
			return false;
		}
	}

	private void drawPanelBackground(Client client, UiPanel panel) {
		Rectangle bounds = panel.getBounds();
		DrawingArea.drawPixels(bounds.height, bounds.y, bounds.x, PANEL_BACKGROUND, bounds.width);
		DrawingArea.drawPixels(PANEL_HEADER_HEIGHT, bounds.y, bounds.x, PANEL_HEADER, bounds.width);
		DrawingArea.drawPixels(1, bounds.y, bounds.x, PANEL_BORDER, bounds.width);
		DrawingArea.drawPixels(1, bounds.y + bounds.height - 1, bounds.x, PANEL_BORDER, bounds.width);
		DrawingArea.drawPixels(bounds.height, bounds.y, bounds.x, PANEL_BORDER, 1);
		DrawingArea.drawPixels(bounds.height, bounds.y, bounds.x + bounds.width - 1, PANEL_BORDER, 1);
		DrawingArea.drawPixels(1, bounds.y + PANEL_HEADER_HEIGHT, bounds.x, PANEL_BORDER, bounds.width);
		if (panel instanceof TabPanel) {
			TabPanel tabPanel = (TabPanel) panel;
			client.newSmallFont.drawBasicString(tabPanel.title, bounds.x + 6, bounds.y + 13, PANEL_TEXT, 0);
		}
	}
}
