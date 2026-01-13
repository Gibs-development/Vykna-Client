package com.client.ui.panel;

import com.client.Client;
import com.client.DrawingArea;

import java.awt.Rectangle;

public class MinimapPanel extends PanelManager.BasePanel {
	public MinimapPanel(int id, Rectangle bounds) {
		super(id, bounds, true, true, "Minimap");
	}

	@Override
	public void draw(Client client) {
		Rectangle bounds = getBounds();
		int clipLeft = DrawingArea.topX;
		int clipTop = DrawingArea.topY;
		int clipRight = DrawingArea.bottomX;
		int clipBottom = DrawingArea.bottomY;

		DrawingArea.setDrawingArea(bounds.y + bounds.height, bounds.x, bounds.x + bounds.width, bounds.y + PanelManager.PANEL_HEADER_HEIGHT);
		client.drawMinimapAt(bounds.x, bounds.y + PanelManager.PANEL_HEADER_HEIGHT);
		DrawingArea.setDrawingArea(clipBottom, clipLeft, clipRight, clipTop);
	}

	@Override
	public boolean handleMouse(Client client, int mouseX, int mouseY) {
		Rectangle bounds = getBounds();
		int absoluteX = bounds.x + mouseX;
		int absoluteY = bounds.y + mouseY;
		client.updateRs3MinimapHovers(absoluteX, absoluteY, bounds.x, bounds.y + PanelManager.PANEL_HEADER_HEIGHT);
		client.processRs3MinimapActions(absoluteX, absoluteY, bounds.x, bounds.y + PanelManager.PANEL_HEADER_HEIGHT);
		return true;
	}

	@Override
	public boolean handleClick(Client client, int mouseX, int mouseY) {
		Rectangle bounds = getBounds();
		int absoluteX = bounds.x + mouseX;
		int absoluteY = bounds.y + mouseY;
		client.processRs3MinimapClick(absoluteX, absoluteY, bounds.x, bounds.y + PanelManager.PANEL_HEADER_HEIGHT);
		return true;
	}
}
