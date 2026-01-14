package com.client.ui.panel;

import com.client.Client;
import com.client.DrawingArea;
import com.client.graphics.interfaces.RSInterface;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

public class InventoryPanel extends PanelManager.TabPanel {
	private static final int INVENTORY_CONTAINER_ID = 3214;
	private static final int SLOT_SIZE = 32;
	private static final int CONTENT_PADDING = 4;
	private static final int MIN_COLUMNS = 2;
	private static final int MAX_COLUMNS = 8;

	private int cachedColumns = 4;
	private int cachedRows = 7;

	public InventoryPanel(int id, Rectangle bounds) {
		super(id, 3, bounds, "Inventory", true, true, 140, 200 + PanelManager.PANEL_HEADER_HEIGHT);
	}

	@Override
	public void draw(Client client) {
		applyResponsiveLayout(client);
		drawSlotGrid(client);
		super.draw(client);
	}

	@Override
	public boolean handleMouse(Client client, int mouseX, int mouseY) {
		applyResponsiveLayout(client);
		return super.handleMouse(client, mouseX, mouseY);
	}

	@Override
	public void onResize(Client client) {
		applyResponsiveLayout(client);
	}

	public Dimension clampSizeForResize(int width, int height, Client client) {
		RSInterface container = RSInterface.interfaceCache[INVENTORY_CONTAINER_ID];
		int padX = container == null ? 4 : container.invSpritePadX;
		int padY = container == null ? 4 : container.invSpritePadY;
		int headerHeight = PanelManager.getPanelHeaderHeight(client, this);
		int contentWidth = Math.max(1, width - CONTENT_PADDING * 2);
		int columns = Math.max(MIN_COLUMNS, Math.min(MAX_COLUMNS, (contentWidth + padX) / (SLOT_SIZE + padX)));
		int rows = (int) Math.ceil(28D / columns);
		int neededHeight = rows * SLOT_SIZE + Math.max(0, rows - 1) * padY + CONTENT_PADDING * 2 + headerHeight;
		int neededWidth = columns * SLOT_SIZE + Math.max(0, columns - 1) * padX + CONTENT_PADDING * 2;
		return new Dimension(Math.max(width, neededWidth), Math.max(height, neededHeight));
	}

	private void applyResponsiveLayout(Client client) {
		RSInterface container = RSInterface.interfaceCache[INVENTORY_CONTAINER_ID];
		if (container == null) {
			return;
		}
		Rectangle bounds = getBounds();
		int padX = container.invSpritePadX;
		int padY = container.invSpritePadY;
		int headerHeight = PanelManager.getPanelHeaderHeight(client, this);
		int contentWidth = Math.max(1, bounds.width - CONTENT_PADDING * 2);
		int columns = Math.max(MIN_COLUMNS, Math.min(MAX_COLUMNS, (contentWidth + padX) / (SLOT_SIZE + padX)));
		int rows = (int) Math.ceil(28D / columns);
		int requiredHeight = rows * SLOT_SIZE + Math.max(0, rows - 1) * padY + CONTENT_PADDING * 2 + headerHeight;
		bounds.height = Math.max(bounds.height, requiredHeight);
		if (columns == cachedColumns && rows == cachedRows) {
			return;
		}
		cachedColumns = columns;
		cachedRows = rows;
		int targetSize = columns * rows;
		if (container.inventoryItemId == null || container.inventoryItemId.length != targetSize) {
			int[] oldItems = container.inventoryItemId == null ? new int[0] : container.inventoryItemId;
			int[] oldAmounts = container.inventoryAmounts == null ? new int[0] : container.inventoryAmounts;
			container.inventoryItemId = new int[targetSize];
			container.inventoryAmounts = new int[targetSize];
			for (int index = 0; index < Math.min(28, targetSize); index++) {
				if (index < oldItems.length) {
					container.inventoryItemId[index] = oldItems[index];
					container.inventoryAmounts[index] = oldAmounts[index];
				}
			}
		}
		container.width = columns;
		container.height = rows;
		client.getPanelManager().saveLayout(client);
	}

	private void drawSlotGrid(Client client) {
		RSInterface container = RSInterface.interfaceCache[INVENTORY_CONTAINER_ID];
		if (container == null) {
			return;
		}
		Point containerOffset = getContainerOffset();
		Rectangle bounds = getBounds();
		int headerHeight = PanelManager.getPanelHeaderHeight(client, this);
		int padX = container.invSpritePadX;
		int padY = container.invSpritePadY;
		int startX = bounds.x + containerOffset.x;
		int startY = bounds.y + headerHeight + containerOffset.y;
		int gridColor = 0x1a1a1a;
		for (int row = 0; row < cachedRows; row++) {
			for (int col = 0; col < cachedColumns; col++) {
				int x = startX + col * (SLOT_SIZE + padX);
				int y = startY + row * (SLOT_SIZE + padY);
				DrawingArea.drawPixels(SLOT_SIZE, y, x, gridColor, SLOT_SIZE);
			}
		}
	}

	private Point getContainerOffset() {
		int interfaceId = Client.tabInterfaceIDs[getTabIndex()];
		RSInterface parent = RSInterface.interfaceCache[interfaceId];
		if (parent == null || parent.children == null) {
			return new Point(CONTENT_PADDING, CONTENT_PADDING);
		}
		for (int index = 0; index < parent.children.length; index++) {
			if (parent.children[index] == INVENTORY_CONTAINER_ID) {
				int offsetX = Math.max(CONTENT_PADDING, parent.childX[index]);
				int offsetY = Math.max(CONTENT_PADDING, parent.childY[index]);
				return new Point(offsetX, offsetY);
			}
		}
		return new Point(CONTENT_PADDING, CONTENT_PADDING);
	}

	@Override
	public boolean isScrollable() {
		return false;
	}

	public static void resetInventoryContainer() {
		RSInterface container = RSInterface.interfaceCache[INVENTORY_CONTAINER_ID];
		if (container == null) {
			return;
		}
		container.width = 4;
		container.height = 7;
		if (container.inventoryItemId == null || container.inventoryItemId.length != 28) {
			int[] oldItems = container.inventoryItemId == null ? new int[0] : container.inventoryItemId;
			int[] oldAmounts = container.inventoryAmounts == null ? new int[0] : container.inventoryAmounts;
			container.inventoryItemId = new int[28];
			container.inventoryAmounts = new int[28];
			for (int index = 0; index < Math.min(28, oldItems.length); index++) {
				container.inventoryItemId[index] = oldItems[index];
				container.inventoryAmounts[index] = oldAmounts[index];
			}
		}
	}

	public void resetCachedLayout() {
		cachedColumns = 4;
		cachedRows = 7;
	}
}
