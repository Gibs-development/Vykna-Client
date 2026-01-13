package com.client.ui.panel;

import com.client.Client;

import java.awt.Rectangle;

public class HpOrbPanel extends WidgetPanel {
	public HpOrbPanel(int id, Rectangle bounds) {
		super(id, bounds);
	}

	@Override
	public void draw(Client client) {
		Rectangle bounds = getBounds();
		client.drawHpOrbAt(bounds.x, bounds.y);
	}

	@Override
	public boolean handleMouse(Client client, int mouseX, int mouseY) {
		client.setHpHover(true);
		return true;
	}

	@Override
	public boolean handleClick(Client client, int mouseX, int mouseY) {
		return true;
	}
}
