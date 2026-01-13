/*
 * Copyright (c) 2024
 * All rights reserved.
 */
package net.runelite.client.ui.overlay;

import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

public class VyknaStatusOverlay extends Overlay
{
	private final PanelComponent panelComponent = new PanelComponent();

	public VyknaStatusOverlay()
	{
		setPosition(OverlayPosition.TOP_LEFT);
		setPriority(OverlayPriority.LOW);
		setLayer(OverlayLayer.UNDER_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		panelComponent.getChildren().clear();
		panelComponent.getChildren().add(LineComponent.builder()
			.left("RuneLite active")
			.build());
		return panelComponent.render(graphics);
	}
}
