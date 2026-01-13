package com.client.ui.panel;

import com.client.Client;
import com.client.graphics.interfaces.RSInterface;

import java.awt.Dimension;
import java.awt.Rectangle;

public class InventoryPanel extends PanelManager.TabPanel {
	private static final int INVENTORY_CONTAINER_ID = 3214;
	private static final int SLOT_SIZE = 32;
	private static final int CONTENT_PADDING = 6;
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
		int contentWidth = Math.max(1, width - CONTENT_PADDING * 2);
		int columns = Math.max(MIN_COLUMNS, Math.min(MAX_COLUMNS, (contentWidth + padX) / (SLOT_SIZE + padX)));
		int rows = (int) Math.ceil(28D / columns);
		int neededHeight = rows * (SLOT_SIZE + padY) + CONTENT_PADDING * 2 + PanelManager.PANEL_HEADER_HEIGHT;
		int neededWidth = columns * (SLOT_SIZE + padX) + CONTENT_PADDING * 2;
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
		int contentWidth = Math.max(1, bounds.width - CONTENT_PADDING * 2);
		int columns = Math.max(MIN_COLUMNS, Math.min(MAX_COLUMNS, (contentWidth + padX) / (SLOT_SIZE + padX)));
		int rows = (int) Math.ceil(28D / columns);
		int requiredHeight = rows * (SLOT_SIZE + padY) + CONTENT_PADDING * 2 + PanelManager.PANEL_HEADER_HEIGHT;
		if (bounds.height < requiredHeight) {
			bounds.height = requiredHeight;
		}
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
}
