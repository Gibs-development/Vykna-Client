package com.client.ui.panel;

import java.awt.Rectangle;

public abstract class WidgetPanel extends PanelManager.BasePanel {
	protected WidgetPanel(int id, Rectangle bounds) {
		super(id, bounds, true, true, "", false, bounds.width, bounds.height, false);
	}

	@Override
	public boolean drawsBackground() {
		return false;
	}

	@Override
	public boolean isScrollable() {
		return false;
	}
}
