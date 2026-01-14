package com.client.ui.panel;

import com.client.Client;
import com.client.DrawingArea;

import java.awt.Rectangle;

public class OrbsPanel extends PanelManager.BasePanel {
	public OrbsPanel(int id, Rectangle bounds) {
		super(id, bounds, true, true, "Orbs");
	}

	@Override
	public void draw(Client client) {
		Rectangle bounds = getBounds();
		int headerHeight = PanelManager.getPanelHeaderHeight(client, this);
		int clipLeft = DrawingArea.topX;
		int clipTop = DrawingArea.topY;
		int clipRight = DrawingArea.bottomX;
		int clipBottom = DrawingArea.bottomY;

		DrawingArea.setDrawingArea(bounds.y + bounds.height, bounds.x, bounds.x + bounds.width, bounds.y + headerHeight);
		client.drawOrbsAt(bounds.x, bounds.y + headerHeight);
		DrawingArea.setDrawingArea(clipBottom, clipLeft, clipRight, clipTop);
	}

	@Override
	public boolean handleMouse(Client client, int mouseX, int mouseY) {
		Rectangle bounds = getBounds();
		int headerHeight = PanelManager.getPanelHeaderHeight(client, this);
		int absoluteX = bounds.x + mouseX;
		int absoluteY = bounds.y + mouseY;
		client.updateRs3OrbHovers(absoluteX, absoluteY, bounds.x, bounds.y + headerHeight);
		client.processRs3OrbActions(absoluteX, absoluteY, bounds.x, bounds.y + headerHeight);
		return true;
	}

	@Override
	public boolean handleClick(Client client, int mouseX, int mouseY) {
		Rectangle bounds = getBounds();
		int headerHeight = PanelManager.getPanelHeaderHeight(client, this);
		int absoluteX = bounds.x + mouseX;
		int absoluteY = bounds.y + mouseY;
		client.processRs3OrbClick(absoluteX, absoluteY, bounds.x, bounds.y + headerHeight);
		return true;
	}
}
