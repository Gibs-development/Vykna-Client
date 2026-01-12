package com.client;

import com.client.features.gameframe.ScreenMode;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.*;
import java.awt.*;
import java.awt.image.*;

public final class RSImageProducer {
	
	public RSImageProducer(int width, int height, Component component) {
		this.width = width;
		this.height = height;
		this.component = component;
		int count = width * height;
		canvasRaster = new int[count];
		image = new BufferedImage(COLOR_MODEL, Raster.createWritableRaster(
				COLOR_MODEL.createCompatibleSampleModel(width, height),
				new DataBufferInt(canvasRaster, count), null), false,
				new Hashtable<Object, Object>());
		initDrawingArea();
	}

	public void drawGraphics(int x, int y, Graphics gfx) {
		draw(gfx, x, y);
	}

	public void draw(Graphics gfx, int x, int y) {
		drawScaledImage(image, gfx, x, y, width, height);
	}

	public void draw(Graphics gfx, int x, int y, int clipX, int clipY,
			int clipWidth, int clipHeight) {
		Shape tmp = gfx.getClip();
		try {
			clip.x = clipX;
			clip.y = clipY;
			clip.width = clipWidth;
			clip.height = clipHeight;
			gfx.setClip(clip);
			gfx.drawImage(image, x, y, component);
		} finally {
			gfx.setClip(tmp);
		}
	}

	public void initDrawingArea() {
		DrawingArea.initDrawingArea(height, width, canvasRaster, null);
	}

	public final int[] canvasRaster;
	public final int width;
	public final int height;
	public final BufferedImage image;
	public final Component component;
	private final Rectangle clip = new Rectangle();
	private static final ColorModel COLOR_MODEL = new DirectColorModel(32, 0xff0000, 0xff00, 0xff);

	public static Point2D.Double getStretchScale() {
		if (Client.currentScreenMode == ScreenMode.FIXED) {
			double widthScale = (double) Client.instance.getGameComponent().getWidth() / (double) ScreenMode.FIXED.getWidth();
			double heightScale = (double) Client.instance.getGameComponent().getHeight() / (double) ScreenMode.FIXED.getHeight();
			return new Point2D.Double(Math.max(1, widthScale), Math.max(1, heightScale));
		}

		return new Point2D.Double(1, 1);
	}

	public static void drawScaledImage(Image image, Graphics gfx, int x, int y, int width, int height) {
		if (Client.currentScreenMode == ScreenMode.FIXED && Client.stretched) {
			Graphics2D gfx2d = (Graphics2D) gfx;
			gfx2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

			Point2D.Double scale = getStretchScale();
			int stretchedWidth = (int) Math.floor((double) width * scale.getX());
			int stretchedHeight = (int) Math.floor((double) height * scale.getY());
			x = (int) (x * scale.getX());
			y = (int) (y * scale.getY());

			gfx2d.drawImage(image, x, y, stretchedWidth, stretchedHeight, Color.BLACK, null);
		} else {
			gfx.drawImage(image, x, y, null);
		}
	}
}
