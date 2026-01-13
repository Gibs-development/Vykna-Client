package com.client.ui.panel;

import com.client.Client;
import com.client.graphics.interfaces.RSInterface;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class PanelManager {
	private final List<UiPanel> panels = new ArrayList<>();
	private int layoutWidth = -1;
	private int layoutHeight = -1;

	public void ensureRs3Layout(Client client) {
		if (layoutWidth == Client.currentGameWidth && layoutHeight == Client.currentGameHeight) {
			return;
		}
		panels.clear();
		PanelLayout.populateRs3Panels(panels);
		layoutWidth = Client.currentGameWidth;
		layoutHeight = Client.currentGameHeight;
	}

	public void drawPanels(Client client) {
		for (UiPanel panel : panels) {
			if (panel.isVisible()) {
				panel.draw(client);
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
			if (bounds.contains(mouseX, mouseY)) {
				return panel.handleMouse(client, mouseX - bounds.x, mouseY - bounds.y);
			}
		}
		return false;
	}

	private static final class PanelLayout {
		private static final int PANEL_WIDTH = 190;
		private static final int PANEL_HEIGHT = 260;
		private static final int PANEL_PADDING = 8;
		private static final int PANEL_MARGIN = 10;

		private static void populateRs3Panels(List<UiPanel> panels) {
			int baseX = Math.max(0, Client.currentGameWidth - PANEL_WIDTH - PANEL_MARGIN);
			int baseY = Math.max(0, Client.currentGameHeight - PANEL_HEIGHT - PANEL_MARGIN);

			int prayerY = Math.max(0, baseY - PANEL_HEIGHT - PANEL_PADDING);
			int magicY = Math.max(0, prayerY - PANEL_HEIGHT - PANEL_PADDING);

			panels.add(new InventoryPanel(1, new Rectangle(baseX, baseY, PANEL_WIDTH, PANEL_HEIGHT)));
			panels.add(new PrayerPanel(2, new Rectangle(baseX, prayerY, PANEL_WIDTH, PANEL_HEIGHT)));
			panels.add(new MagicPanel(3, new Rectangle(baseX, magicY, PANEL_WIDTH, PANEL_HEIGHT)));
			panels.add(new EquipmentPanel(4, new Rectangle(baseX - PANEL_WIDTH - PANEL_PADDING, baseY, PANEL_WIDTH, PANEL_HEIGHT)));
		}
	}

	private abstract static class BasePanel implements UiPanel {
		private final int id;
		private final Rectangle bounds;
		private final boolean visible;

		private BasePanel(int id, Rectangle bounds, boolean visible) {
			this.id = id;
			this.bounds = bounds;
			this.visible = visible;
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
	}

	private abstract static class TabPanel extends BasePanel {
		private final int tabIndex;

		private TabPanel(int id, int tabIndex, Rectangle bounds) {
			super(id, bounds, true);
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
			client.pushUiOffset(getBounds().x, getBounds().y);
			client.drawInterfaceWithOffset(0, 0, rsInterface, 0);
			client.popUiOffset();
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
			client.pushUiOffset(getBounds().x, getBounds().y);
			client.buildInterfaceMenuWithOffset(0, rsInterface, mouseX, 0, mouseY, 0);
			client.popUiOffset();
			return true;
		}
	}

	private static final class InventoryPanel extends TabPanel {
		private InventoryPanel(int id, Rectangle bounds) {
			super(id, 3, bounds);
		}
	}

	private static final class PrayerPanel extends TabPanel {
		private PrayerPanel(int id, Rectangle bounds) {
			super(id, 5, bounds);
		}
	}

	private static final class MagicPanel extends TabPanel {
		private MagicPanel(int id, Rectangle bounds) {
			super(id, 6, bounds);
		}
	}

	private static final class EquipmentPanel extends BasePanel {
		private EquipmentPanel(int id, Rectangle bounds) {
			super(id, bounds, false);
		}

		@Override
		public void draw(Client client) {
		}

		@Override
		public boolean handleMouse(Client client, int mouseX, int mouseY) {
			return false;
		}
	}
}
