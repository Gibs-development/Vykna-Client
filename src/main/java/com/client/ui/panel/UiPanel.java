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

	void setSize(int width, int height);

	void draw(Client client);

	boolean handleMouse(Client client, int mouseX, int mouseY);

	boolean handleClick(Client client, int mouseX, int mouseY);

	default boolean handleRightClick(Client client, int mouseX, int mouseY) {
		return false;
	}

	default boolean resizable() {
		return false;
	}

	default int getMinWidth() {
		return getBounds().width;
	}

	default int getMinHeight() {
		return getBounds().height;
	}

	default boolean keepAspectRatio() {
		return false;
	}

	default boolean drawsBackground() {
		return true;
	}

	default boolean isClosable() {
		return true;
	}

	default boolean isScrollable() {
		return true;
	}

	default void onResize(Client client) {
	}
}
