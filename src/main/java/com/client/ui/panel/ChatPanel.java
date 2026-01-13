package com.client.ui.panel;

import com.client.Client;
import com.client.DrawingArea;

import java.awt.Rectangle;

public class ChatPanel extends PanelManager.BasePanel {
	public ChatPanel(int id, Rectangle bounds) {
		super(id, bounds, true, true, "Chat");
	}

	@Override
	public void draw(Client client) {
		Rectangle bounds = getBounds();
		int clipLeft = DrawingArea.topX;
		int clipTop = DrawingArea.topY;
		int clipRight = DrawingArea.bottomX;
		int clipBottom = DrawingArea.bottomY;

		DrawingArea.setDrawingArea(bounds.y + bounds.height, bounds.x, bounds.x + bounds.width, bounds.y + PanelManager.PANEL_HEADER_HEIGHT);
		client.drawChatAreaAt(bounds.x, bounds.y + PanelManager.PANEL_HEADER_HEIGHT);
		DrawingArea.setDrawingArea(clipBottom, clipLeft, clipRight, clipTop);
	}

	@Override
	public boolean handleMouse(Client client, int mouseX, int mouseY) {
		Rectangle bounds = getBounds();
		int baseX = bounds.x;
		int baseY = bounds.y + PanelManager.PANEL_HEADER_HEIGHT;
		int absoluteX = baseX + mouseX;
		int absoluteY = baseY + mouseY;
		client.processRs3ChatModeClick(absoluteX, absoluteY, absoluteX, absoluteY, false, baseX, baseY);
		client.updateChatScroll(absoluteX, absoluteY, baseX, baseY);
		return true;
	}

	@Override
	public boolean handleClick(Client client, int mouseX, int mouseY) {
		Rectangle bounds = getBounds();
		int baseX = bounds.x;
		int baseY = bounds.y + PanelManager.PANEL_HEADER_HEIGHT;
		int absoluteX = baseX + mouseX;
		int absoluteY = baseY + mouseY;
		client.processRs3ChatModeClick(absoluteX, absoluteY, absoluteX, absoluteY, true, baseX, baseY);
		client.updateChatScroll(absoluteX, absoluteY, baseX, baseY);
		return true;
	}
}
