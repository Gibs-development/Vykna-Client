package com.client.ui.panel;

import com.client.Client;
import com.client.DrawingArea;

import java.awt.Rectangle;

public class ChatPanel extends PanelManager.BasePanel {
	private static final int HEADER_HEIGHT = 24;
	private static final int INPUT_HEIGHT = 22;
	private static final int PADDING = 4;

	public ChatPanel(int id, Rectangle bounds) {
		super(id, bounds, true, true, "Chat", true, 360, 180 + PanelManager.PANEL_HEADER_HEIGHT, false);
	}

	@Override
	public void draw(Client client) {
		Rectangle bounds = getBounds();
		int headerHeight = PanelManager.getPanelHeaderHeight(client, this);
		Layout layout = layout(bounds, headerHeight);
		client.setRs3ChatLayout(layout.headerRect, layout.messageRect, layout.inputRect);
		int clipLeft = DrawingArea.topX;
		int clipTop = DrawingArea.topY;
		int clipRight = DrawingArea.bottomX;
		int clipBottom = DrawingArea.bottomY;

		DrawingArea.setDrawingArea(bounds.y + bounds.height, bounds.x, bounds.x + bounds.width, bounds.y + headerHeight);
		client.drawChatAreaAt(bounds.x, bounds.y + headerHeight, bounds.width, bounds.height - headerHeight);
		DrawingArea.setDrawingArea(clipBottom, clipLeft, clipRight, clipTop);
	}

	@Override
	public boolean handleMouse(Client client, int mouseX, int mouseY) {
		Rectangle bounds = getBounds();
		int headerHeight = PanelManager.getPanelHeaderHeight(client, this);
		Layout layout = layout(bounds, headerHeight);
		client.setRs3ChatLayout(layout.headerRect, layout.messageRect, layout.inputRect);
		int baseX = bounds.x;
		int baseY = bounds.y + headerHeight;
		int absoluteX = baseX + mouseX;
		int absoluteY = baseY + mouseY;
		client.processRs3ChatModeClick(absoluteX, absoluteY, absoluteX, absoluteY, false, baseX, baseY, bounds.width, bounds.height - headerHeight);
		client.updateChatScroll(absoluteX, absoluteY, baseX, baseY, bounds.width, bounds.height - headerHeight);
		return true;
	}

	@Override
	public boolean handleClick(Client client, int mouseX, int mouseY) {
		Rectangle bounds = getBounds();
		int headerHeight = PanelManager.getPanelHeaderHeight(client, this);
		Layout layout = layout(bounds, headerHeight);
		client.setRs3ChatLayout(layout.headerRect, layout.messageRect, layout.inputRect);
		int baseX = bounds.x;
		int baseY = bounds.y + headerHeight;
		int absoluteX = baseX + mouseX;
		int absoluteY = baseY + mouseY;
		client.processRs3ChatModeClick(absoluteX, absoluteY, absoluteX, absoluteY, true, baseX, baseY, bounds.width, bounds.height - headerHeight);
		client.updateChatScroll(absoluteX, absoluteY, baseX, baseY, bounds.width, bounds.height - headerHeight);
		return true;
	}

	@Override
	public boolean isClosable() {
		return false;
	}

	@Override
	public boolean isScrollable() {
		return false;
	}

	private Layout layout(Rectangle bounds, int headerHeight) {
		int areaX = bounds.x;
		int areaY = bounds.y + headerHeight;
		int areaWidth = bounds.width;
		int areaHeight = bounds.height - headerHeight;
		int innerWidth = Math.max(0, areaWidth - PADDING * 2);
		int innerHeight = Math.max(0, areaHeight - PADDING * 2);
		Rectangle headerRect = new Rectangle(areaX + PADDING, areaY + PADDING, innerWidth, HEADER_HEIGHT);
		Rectangle inputRect = new Rectangle(areaX + PADDING,
				areaY + areaHeight - INPUT_HEIGHT - PADDING,
				innerWidth,
				INPUT_HEIGHT);
		int messageTop = headerRect.y + headerRect.height + PADDING;
		int messageBottom = inputRect.y - PADDING;
		int messageHeight = Math.max(0, messageBottom - messageTop);
		Rectangle messageRect = new Rectangle(areaX + PADDING, messageTop, innerWidth, messageHeight);
		return new Layout(headerRect, messageRect, inputRect);
	}

	private static final class Layout {
		private final Rectangle headerRect;
		private final Rectangle messageRect;
		private final Rectangle inputRect;

		private Layout(Rectangle headerRect, Rectangle messageRect, Rectangle inputRect) {
			this.headerRect = headerRect;
			this.messageRect = messageRect;
			this.inputRect = inputRect;
		}
	}
}
