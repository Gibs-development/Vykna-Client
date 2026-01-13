package com.client.ui.panel;

import com.client.Client;

import java.awt.Rectangle;

public interface UiPanel {
	int getId();

	Rectangle getBounds();

	boolean isVisible();

	void draw(Client client);

	boolean handleMouse(Client client, int mouseX, int mouseY);
}
