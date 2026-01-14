package com.client.ui.panel;

import com.client.Client;

import java.awt.Rectangle;

public class RunOrbPanel extends WidgetPanel {
	public RunOrbPanel(int id, Rectangle bounds) {
		super(id, bounds);
	}

	@Override
	public void draw(Client client) {
		Rectangle bounds = getBounds();
		client.drawRunOrbAt(bounds.x, bounds.y);
	}

	@Override
	public boolean handleMouse(Client client, int mouseX, int mouseY) {
		client.setRunHover(true);
		client.processRs3OrbActions(bounds().x + mouseX, bounds().y + mouseY, bounds().x, bounds().y);
		return true;
	}

	@Override
	public boolean handleClick(Client client, int mouseX, int mouseY) {
		client.processRs3OrbClick(bounds().x + mouseX, bounds().y + mouseY, bounds().x, bounds().y);
		return true;
	}

	private Rectangle bounds() {
		return getBounds();
	}
}
