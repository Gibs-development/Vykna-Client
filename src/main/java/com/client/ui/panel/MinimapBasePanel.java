package com.client.ui.panel;

import com.client.Client;
import com.client.DrawingArea;

import java.awt.Rectangle;

public class MinimapBasePanel extends PanelManager.BasePanel {
	public MinimapBasePanel(int id, Rectangle bounds) {
		super(id, bounds, true, true, "Minimap", true, 160, 160 + PanelManager.PANEL_HEADER_HEIGHT, false);
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
		client.drawMinimapAt(bounds.x, bounds.y + headerHeight, bounds.width, bounds.height - headerHeight);
		if (client.isRs3EditModeActive()) {
			Rectangle content = client.getRs3MinimapContentBounds(bounds.x, bounds.y + headerHeight,
					bounds.width, bounds.height - headerHeight);
			if (content != null) {
				DrawingArea.drawPixels(1, content.y, content.x, 0xffd24a, content.width);
				DrawingArea.drawPixels(1, content.y + content.height - 1, content.x, 0xffd24a, content.width);
				DrawingArea.drawPixels(content.height, content.y, content.x, 0xffd24a, 1);
				DrawingArea.drawPixels(content.height, content.y, content.x + content.width - 1, 0xffd24a, 1);
			}
		}
		DrawingArea.setDrawingArea(clipBottom, clipLeft, clipRight, clipTop);
	}

	@Override
	public boolean handleMouse(Client client, int mouseX, int mouseY) {
		Rectangle bounds = getBounds();
		int headerHeight = PanelManager.getPanelHeaderHeight(client, this);
		int absoluteX = bounds.x + mouseX;
		int absoluteY = bounds.y + mouseY;
		client.updateRs3MinimapHovers(absoluteX, absoluteY, bounds.x, bounds.y + headerHeight, bounds.width, bounds.height - headerHeight);
		client.processRs3MinimapActions(absoluteX, absoluteY, bounds.x, bounds.y + headerHeight, bounds.width, bounds.height - headerHeight);
		return true;
	}

	@Override
	public boolean handleClick(Client client, int mouseX, int mouseY) {
		Rectangle bounds = getBounds();
		int headerHeight = PanelManager.getPanelHeaderHeight(client, this);
		int absoluteX = bounds.x + mouseX;
		int absoluteY = bounds.y + mouseY;
		client.processRs3MinimapClick(absoluteX, absoluteY, bounds.x, bounds.y + headerHeight, bounds.width, bounds.height - headerHeight);
		return true;
	}
}
