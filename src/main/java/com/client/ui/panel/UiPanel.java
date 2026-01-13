package com.client.ui.panel;

import com.client.Client;

import java.awt.Rectangle;

public interface UiPanel {
	int getId();

	Rectangle getBounds();

	boolean isVisible();

	boolean draggable();

	String getTitle();

	boolean contains(int mouseX, int mouseY);

	void setPosition(int x, int y);

	void draw(Client client);

	boolean handleMouse(Client client, int mouseX, int mouseY);
}
