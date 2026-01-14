package com.client.ui.panel;

import com.client.Client;

import java.awt.Rectangle;

public class CompassPanel extends WidgetPanel {
	public CompassPanel(int id, Rectangle bounds) {
		super(id, bounds);
	}

	@Override
	public void draw(Client client) {
		Rectangle bounds = resolveBounds(client);
		if (bounds == null) {
			return;
		}
		client.drawRs3CompassAt(bounds.x, bounds.y);
	}

	@Override
	public boolean handleMouse(Client client, int mouseX, int mouseY) {
		Rectangle bounds = resolveBounds(client);
		if (bounds == null) {
			return false;
		}
		client.processRs3CompassActions(bounds.x + mouseX, bounds.y + mouseY, bounds);
		return true;
	}

	@Override
	public boolean handleClick(Client client, int mouseX, int mouseY) {
		Rectangle bounds = resolveBounds(client);
		if (bounds == null) {
			return false;
		}
		client.processRs3CompassClick(bounds.x + mouseX, bounds.y + mouseY, bounds);
		return true;
	}

	private Rectangle resolveBounds(Client client) {
		if (client == null || !client.isRs3InterfaceStyleActive() || client.isRs3EditModeActive()) {
			return getBounds();
		}
		UiPanel minimap = client.getPanelManager().getPanel(PanelManager.PANEL_ID_MINIMAP_BASE);
		if (minimap == null) {
			return getBounds();
		}
		Rectangle minimapBounds = minimap.getBounds();
		int headerHeight = PanelManager.getPanelHeaderHeight(client, minimap);
		Rectangle compassBounds = client.getRs3CompassBounds(minimapBounds.x, minimapBounds.y + headerHeight,
				minimapBounds.width, minimapBounds.height - headerHeight);
		return compassBounds == null ? getBounds() : compassBounds;
	}
}
