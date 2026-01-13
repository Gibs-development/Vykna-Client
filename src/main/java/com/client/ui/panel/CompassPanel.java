package com.client.ui.panel;

import com.client.Client;

import java.awt.Rectangle;

public class CompassPanel extends WidgetPanel {
	public CompassPanel(int id, Rectangle bounds) {
		super(id, bounds);
	}

	@Override
	public void draw(Client client) {
		Rectangle bounds = getBounds();
		client.drawRs3CompassAt(bounds.x, bounds.y);
	}

	@Override
	public boolean handleMouse(Client client, int mouseX, int mouseY) {
		Rectangle bounds = getBounds();
		client.processRs3CompassActions(bounds.x + mouseX, bounds.y + mouseY, bounds);
		return true;
	}

	@Override
	public boolean handleClick(Client client, int mouseX, int mouseY) {
		Rectangle bounds = getBounds();
		client.processRs3CompassClick(bounds.x + mouseX, bounds.y + mouseY, bounds);
		return true;
	}
}
