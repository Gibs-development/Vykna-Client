package com.client.ui.panel;

import com.client.Client;
import com.client.DrawingArea;
import com.client.graphics.interfaces.RSInterface;

import java.awt.Rectangle;

public class ActionBarPanel extends PanelManager.BasePanel {
	private static final int SLOT_SIZE = 28;
	private static final int SLOT_GAP = 4;
	private static final int BAR_HEIGHT = 8;
	private static final int BAR_GAP = 4;

	public ActionBarPanel(int id, Rectangle bounds) {
		super(id, bounds, true, true, "Action Bar", true, 280, 56 + PanelManager.PANEL_HEADER_HEIGHT, false);
	}

	@Override
	public void draw(Client client) {
		Rectangle bounds = getBounds();
		int headerHeight = PanelManager.getPanelHeaderHeight(client, this);
		int contentX = bounds.x + 6;
		int contentY = bounds.y + headerHeight + 6;
		int contentWidth = bounds.width - 12;
		int barWidth = Math.max(60, contentWidth);
		drawBar(contentX, contentY, barWidth, BAR_HEIGHT, getHealthPercent(client), 0x3a3a3a, 0x8c1f1f);
		drawBar(contentX, contentY + BAR_HEIGHT + BAR_GAP, barWidth, BAR_HEIGHT, getPrayerPercent(client), 0x3a3a3a, 0x1f4f8c);
		int slotY = contentY + (BAR_HEIGHT * 2) + (BAR_GAP * 2);
		drawSlots(contentX, slotY, contentWidth);
	}

	private void drawBar(int x, int y, int width, int height, int percent, int background, int fill) {
		int clamped = Math.max(0, Math.min(100, percent));
		DrawingArea.drawPixels(height, y, x, background, width);
		int fillWidth = (width * clamped) / 100;
		if (fillWidth > 0) {
			DrawingArea.drawPixels(height, y, x, fill, fillWidth);
		}
		DrawingArea.drawPixels(1, y, x, 0x1a1a1a, width);
		DrawingArea.drawPixels(1, y + height - 1, x, 0x1a1a1a, width);
		DrawingArea.drawPixels(height, y, x, 0x1a1a1a, 1);
		DrawingArea.drawPixels(height, y, x + width - 1, 0x1a1a1a, 1);
	}

	private void drawSlots(int x, int y, int contentWidth) {
		int slotCount = 12;
		int totalWidth = slotCount * SLOT_SIZE + (slotCount - 1) * SLOT_GAP;
		int startX = x + Math.max(0, (contentWidth - totalWidth) / 2);
		for (int slot = 0; slot < slotCount; slot++) {
			int slotX = startX + slot * (SLOT_SIZE + SLOT_GAP);
			DrawingArea.drawPixels(SLOT_SIZE, y, slotX, 0x1b1b1b, SLOT_SIZE);
			DrawingArea.drawPixels(1, y, slotX, 0x2c2c2c, SLOT_SIZE);
			DrawingArea.drawPixels(1, y + SLOT_SIZE - 1, slotX, 0x2c2c2c, SLOT_SIZE);
			DrawingArea.drawPixels(SLOT_SIZE, y, slotX, 0x2c2c2c, 1);
			DrawingArea.drawPixels(SLOT_SIZE, y, slotX + SLOT_SIZE - 1, 0x2c2c2c, 1);
		}
	}

	private int getHealthPercent(Client client) {
		RSInterface current = RSInterface.interfaceCache[4016];
		RSInterface max = RSInterface.interfaceCache[4017];
		if (current == null || max == null || current.message == null || max.message == null) {
			return 0;
		}
		try {
			int currentHp = Integer.parseInt(current.message);
			int maxHp = Integer.parseInt(max.message);
			if (maxHp <= 0) {
				return 0;
			}
			int clamped = Math.max(0, Math.min(currentHp, maxHp));
			return (int) ((clamped / (double) maxHp) * 100);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private int getPrayerPercent(Client client) {
		RSInterface current = RSInterface.interfaceCache[4012];
		if (current == null || current.message == null) {
			return 0;
		}
		try {
			int currentPrayer = Integer.parseInt(current.message.replaceAll("%", ""));
			int maxPrayer = client.maxStats[5];
			if (maxPrayer <= 0) {
				return 0;
			}
			int clamped = Math.max(0, Math.min(currentPrayer, maxPrayer));
			return (int) ((clamped / (double) maxPrayer) * 100);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
