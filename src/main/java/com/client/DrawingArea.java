package com.client;


public class DrawingArea extends NodeSub {

	public static float[] depthBuffer; // <- add

	public static void initDrawingArea(int i, int j, int[] ai, float[] depth) {
		pixels = ai;
		width = j;
		height = i;

		depthBuffer = depth;          // ✅ keep current depth buffer in DrawingArea
		Rasterizer.depthBuffer = depth; // ✅ rasterizer writes into same buffer
		setDrawingArea(i, 0, j, 0);
	}

	public static void drawTransparentBox(int leftX, int topY, int width, int height, int rgbColour, int opacity){
		if (leftX < DrawingArea.topX) {
			width -= DrawingArea.topX - leftX;
			leftX = DrawingArea.topX;
		}
		if (topY < DrawingArea.topY) {
			height -= DrawingArea.topY - topY;
			topY = DrawingArea.topY;
		}
		if (leftX + width > bottomX)
			width = bottomX - leftX;
		if (topY + height > bottomY)
			height = bottomY - topY;
		int transparency = 256 - opacity;
		int red = (rgbColour >> 16 & 0xff) * opacity;
		int green = (rgbColour >> 8 & 0xff) * opacity;
		int blue = (rgbColour & 0xff) * opacity;
		int leftOver = DrawingArea.width - width;
		int pixelIndex = leftX + topY * DrawingArea.width;
		for(int rowIndex = 0; rowIndex < height; rowIndex++){
			for(int columnIndex = 0; columnIndex < width; columnIndex++) {
				int otherRed = (pixels[pixelIndex] >> 16 & 0xff) * transparency;
				int otherGreen = (pixels[pixelIndex] >> 8 & 0xff) * transparency;
				int otherBlue = (pixels[pixelIndex] & 0xff) * transparency;
				int transparentColour = ((red + otherRed >> 8) << 16) + ((green + otherGreen >> 8) << 8) + (blue + otherBlue >> 8);
				pixels[pixelIndex++] = transparentColour;
			}
			pixelIndex += leftOver;
		}
	}

	public static void clear(int color) {
		int length = width * height;
		int mod = length - (length & 0x7);
		int offset = 0;
		while (offset < mod) {
			pixels[(offset++)] = color;
			pixels[(offset++)] = color;
			pixels[(offset++)] = color;
			pixels[(offset++)] = color;
			pixels[(offset++)] = color;
			pixels[(offset++)] = color;
			pixels[(offset++)] = color;
			pixels[(offset++)] = color;
		}
		while (offset < length) {
			pixels[(offset++)] = color;
		}
	}
	public static void drawFilledCircle(int xCenter, int yCenter, int radius, int rgb, int alpha) {
		if (radius <= 0) return;
		if (alpha <= 0) return;
		if (alpha > 255) alpha = 255;

		// Pre-split color for alpha blend.
		final int srcR = (rgb >> 16) & 0xFF;
		final int srcG = (rgb >> 8) & 0xFF;
		final int srcB = rgb & 0xFF;

		final int invA = 255 - alpha;

		// Clamp vertical range to screen bounds.
		int yStart = yCenter - radius;
		int yEnd = yCenter + radius;

		if (yStart < topY) yStart = topY;
		if (yEnd >= bottomY) yEnd = bottomY - 1;

		int rSq = radius * radius;

		for (int y = yStart; y <= yEnd; y++) {
			int dy = y - yCenter;
			int dySq = dy * dy;
			int dx = (int) Math.sqrt(rSq - dySq);

			int xStart = xCenter - dx;
			int xEnd = xCenter + dx;

			if (xStart < topX) xStart = topX;
			if (xEnd >= bottomX) xEnd = bottomX - 1;

			int offset = xStart + y * width;

			for (int x = xStart; x <= xEnd; x++) {
				int dst = pixels[offset];

				int dstR = (dst >> 16) & 0xFF;
				int dstG = (dst >> 8) & 0xFF;
				int dstB = dst & 0xFF;

				int outR = (srcR * alpha + dstR * invA) >> 8;
				int outG = (srcG * alpha + dstG * invA) >> 8;
				int outB = (srcB * alpha + dstB * invA) >> 8;

				pixels[offset++] = (outR << 16) | (outG << 8) | outB;
			}
		}
	}
	public static void drawAdditiveTintedScaledSprite(
			int[] src, int srcW, int srcH,
			int dstX, int dstY, int dstW, int dstH,
			int tintRGB, int alpha255
	) {
		if (alpha255 <= 0 || dstW <= 0 || dstH <= 0) return;

		// Basic clipping to DrawingArea bounds
		int clipLeft = 0;
		int clipTop = 0;
		int clipRight = width;
		int clipBottom = height;

		int x0 = dstX;
		int y0 = dstY;
		int x1 = dstX + dstW;
		int y1 = dstY + dstH;

		if (x0 < clipLeft) x0 = clipLeft;
		if (y0 < clipTop) y0 = clipTop;
		if (x1 > clipRight) x1 = clipRight;
		if (y1 > clipBottom) y1 = clipBottom;

		if (x0 >= x1 || y0 >= y1) return;

		// Fixed-point stepping
		int uStep = (srcW << 16) / dstW;
		int vStep = (srcH << 16) / dstH;

		int tintR = (tintRGB >> 16) & 0xFF;
		int tintG = (tintRGB >> 8) & 0xFF;
		int tintB = tintRGB & 0xFF;

		int startV = (y0 - dstY) * vStep;

		for (int y = y0; y < y1; y++) {
			int v = (startV >> 16);
			int srcRow = v * srcW;

			int startU = (x0 - dstX) * uStep;
			int dstIndex = y * width + x0;

			for (int x = x0; x < x1; x++) {
				int u = (startU >> 16);
				int sp = src[srcRow + u];

				// Source alpha from sprite (ARGB)
				int sa = (sp >>> 24);
				if (sa != 0) {
					// Combine sprite alpha with requested particle alpha
					int a = (sa * alpha255) >> 8; // 0..255
					if (a != 0) {
						int sr = (sp >> 16) & 0xFF;
						int sg = (sp >> 8) & 0xFF;
						int sb = sp & 0xFF;

						// Apply tint
						sr = (sr * tintR) >> 8;
						sg = (sg * tintG) >> 8;
						sb = (sb * tintB) >> 8;

						// Apply alpha to source contribution
						sr = (sr * a) >> 8;
						sg = (sg * a) >> 8;
						sb = (sb * a) >> 8;

						int dp = pixels[dstIndex];

						int dr = (dp >> 16) & 0xFF;
						int dg = (dp >> 8) & 0xFF;
						int db = dp & 0xFF;

						// Additive clamp
						dr += sr; if (dr > 255) dr = 255;
						dg += sg; if (dg > 255) dg = 255;
						db += sb; if (db > 255) db = 255;

						pixels[dstIndex] = (dr << 16) | (dg << 8) | db;
					}
				}

				dstIndex++;
				startU += uStep;
			}

			startV += vStep;
		}
	}

	public static void drawAdditiveFilledCircle(int cx, int cy, int radius, int rgb, int alpha255) {
		if (alpha255 <= 0 || radius <= 0) return;

		int rAdd = (((rgb >> 16) & 0xFF) * alpha255) >> 8;
		int gAdd = (((rgb >> 8) & 0xFF) * alpha255) >> 8;
		int bAdd = ((rgb & 0xFF) * alpha255) >> 8;

		int r2 = radius * radius;

		int y0 = cy - radius;
		int y1 = cy + radius;

		if (y0 < 0) y0 = 0;
		if (y1 >= height) y1 = height - 1;

		for (int y = y0; y <= y1; y++) {
			int dy = y - cy;
			int dxMax = (int) Math.sqrt(r2 - dy * dy);

			int x0 = cx - dxMax;
			int x1 = cx + dxMax;

			if (x0 < 0) x0 = 0;
			if (x1 >= width) x1 = width - 1;

			int idx = y * width + x0;
			for (int x = x0; x <= x1; x++) {
				int dp = pixels[idx];

				int dr = (dp >> 16) & 0xFF;
				int dg = (dp >> 8) & 0xFF;
				int db = dp & 0xFF;

				dr += rAdd; if (dr > 255) dr = 255;
				dg += gAdd; if (dg > 255) dg = 255;
				db += bAdd; if (db > 255) db = 255;

				pixels[idx++] = (dr << 16) | (dg << 8) | db;
			}
		}
	}

	public static void drawTintedScaledSprite(
			int[] src, int srcW, int srcH,
			int dstX, int dstY, int dstW, int dstH,
			int tintRGB, int globalAlpha
	) {
		if (src == null || pixels == null) return;
		if (dstW <= 0 || dstH <= 0) return;
		if (globalAlpha <= 0) return;
		if (globalAlpha > 255) globalAlpha = 255;

		// Clip to drawing area bounds
		int startX = Math.max(dstX, 0);
		int startY = Math.max(dstY, 0);
		int endX = Math.min(dstX + dstW, width);
		int endY = Math.min(dstY + dstH, height);
		if (startX >= endX || startY >= endY) return;

		final int tr = (tintRGB >> 16) & 255;
		final int tg = (tintRGB >> 8) & 255;
		final int tb = tintRGB & 255;

		for (int y = startY; y < endY; y++) {
			int sy = ((y - dstY) * srcH) / dstH;
			int dstIndex = y * width + startX;

			for (int x = startX; x < endX; x++) {
				int sx = ((x - dstX) * srcW) / dstW;
				int sp = src[sy * srcW + sx];

				int sa = (sp >>> 24);             // sprite alpha
				if (sa == 0) { dstIndex++; continue; }

				// combine sprite alpha with global particle alpha
				int a = (sa * globalAlpha) / 255;
				if (a <= 0) { dstIndex++; continue; }

				// sprite rgb (lets you use grayscale/noisy masks too)
				int sr = (sp >> 16) & 255;
				int sg = (sp >> 8) & 255;
				int sb = sp & 255;

				// multiply tint by sprite rgb
				int r = (sr * tr) / 255;
				int g = (sg * tg) / 255;
				int b = (sb * tb) / 255;

				int dp = pixels[dstIndex];
				int dr = (dp >> 16) & 255;
				int dg = (dp >> 8) & 255;
				int db = dp & 255;

				int inv = 255 - a;

				int outR = (r * a + dr * inv) / 255;
				int outG = (g * a + dg * inv) / 255;
				int outB = (b * a + db * inv) / 255;

				pixels[dstIndex++] = (outR << 16) | (outG << 8) | outB;
			}
		}
	}

	public static void method336(int i, int j, int k, int l, int i1) {
		if (k < topX) {
			i1 -= topX - k;
			k = topX;
		}
		if (j < topY) {
			i -= topY - j;
			j = topY;
		}
		if (k + i1 > bottomX)
			i1 = bottomX - k;
		if (j + i > bottomY)
			i = bottomY - j;
		int k1 = width - i1;
		int l1 = k + j * width;
		for (int i2 = -i; i2 < 0; i2++) {
			for (int j2 = -i1; j2 < 0; j2++)
				pixels[l1++] = l;

			l1 += k1;
		}

	}
    public static void drawRoundedRectangle(int x, int y, int width, int height, int color,
                                            int alpha, boolean filled, boolean shadowed) {
        if (shadowed)
            drawRoundedRectangle(x + 1, y + 1, width, height, 0, alpha, filled,
                    false);
        if (alpha == -1) {
            if (filled) {
                drawHorizontalLine(y + 1, color, width - 4, x + 2);// method339
                drawHorizontalLine(y + height - 2, color, width - 4, x + 2);// method339
                drawPixels(height - 4, y + 2, x + 1, color, width - 2);// method336
            }
            drawHorizontalLine(y, color, width - 4, x + 2);// method339
            drawHorizontalLine(y + height - 1, color, width - 4, x + 2);// method339
            method341(y + 2, color, height - 4, x);// method341
            method341(y + 2, color, height - 4, x + width - 1);// method341
            drawPixels(1, y + 1, x + 1, color, 1);// method336
            drawPixels(1, y + 1, x + width - 2, color, 1);// method336
            drawPixels(1, y + height - 2, x + width - 2, color, 1);// method336
            drawPixels(1, y + height - 2, x + 1, color, 1);// method336
        } else if (alpha != -1) {
            if (filled) {
                method340(color, width - 4, y + 1, alpha, x + 2);// method340
                method340(color, width - 4, y + height - 2, alpha, x + 2);// method340
                method335(color, y + 2, width - 2, height - 4, alpha, x + 1);// method335
            }
            method340(color, width - 4, y, alpha, x + 2);// method340
            method340(color, width - 4, y + height - 1, alpha, x + 2);// method340
            method342(color, x, alpha, y + 2, height - 4);// method342
            method342(color, x + width - 1, alpha, y + 2, height - 4);// method342
            method335(color, y + 1, 1, 1, alpha, x + 1);// method335
            method335(color, y + 1, 1, 1, alpha, x + width - 2);// method335
            method335(color, y + height - 2, 1, 1, alpha, x + 1);// method335
            method335(color, y + height - 2, 1, 1, alpha, x + width - 2);// method335
        }
    }

	public static void drawAlphaGradient(int x, int y, int gradientWidth, int gradientHeight, int startColor, int endColor, int alpha) {
		int k1 = 0;
		int l1 = 0x10000 / gradientHeight;
		if(x < topX) {
			gradientWidth -= topX - x;
			x = topX;
		}
		if(y < topY) {
			k1 += (topY - y) * l1;
			gradientHeight -= topY - y;
			y = topY;
		}
		if(x + gradientWidth > bottomX)
			gradientWidth = bottomX - x;
		if(y + gradientHeight > bottomY)
			gradientHeight = bottomY - y;
		int i2 = width - gradientWidth;
		int result_alpha = 256 - alpha;
		int total_pixels = x + y * width;
		for(int k2 = -gradientHeight; k2 < 0; k2++) {
			int gradient1 = 0x10000 - k1 >> 8;
			int gradient2 = k1 >> 8;
			int gradient_color = ((startColor & 0xff00ff) * gradient1 + (endColor & 0xff00ff) * gradient2 & 0xff00ff00) + ((startColor & 0xff00) * gradient1 + (endColor & 0xff00) * gradient2 & 0xff0000) >>> 8;
			int color = ((gradient_color & 0xff00ff) * alpha >> 8 & 0xff00ff) + ((gradient_color & 0xff00) * alpha >> 8 & 0xff00);
			for(int k3 = -gradientWidth; k3 < 0; k3++) {
				int colored_pixel = pixels[total_pixels];
				colored_pixel = ((colored_pixel & 0xff00ff) * result_alpha >> 8 & 0xff00ff) + ((colored_pixel & 0xff00) * result_alpha >> 8 & 0xff00);
				pixels[total_pixels++] = color + colored_pixel;
			}
			total_pixels += i2;
			k1 += l1;
		}
	}

	public static void drawPixelsWithOpacity2(int xPos, int yPos, int pixelWidth, int pixelHeight, int color, int opacityLevel) {
		drawPixelsWithOpacity(color, yPos, pixelWidth, pixelHeight, opacityLevel, xPos);
	}

    public static void drawPixelsWithOpacity(int color, int yPos, int pixelWidth, int pixelHeight, int opacityLevel,
											 int xPos) {
		if (xPos < topX) {
			pixelWidth -= topX - xPos;
			xPos = topX;
		}
		if (yPos < topY) {
			pixelHeight -= topY - yPos;
			yPos = topY;
		}
		if (xPos + pixelWidth > bottomX) {
			pixelWidth = bottomX - xPos;
		}
		if (yPos + pixelHeight > bottomY) {
			pixelHeight = bottomY - yPos;
		}
		int l1 = 256 - opacityLevel;
		int i2 = (color >> 16 & 0xFF) * opacityLevel;
		int j2 = (color >> 8 & 0xFF) * opacityLevel;
		int k2 = (color & 0xFF) * opacityLevel;
		int k3 = width - pixelWidth;
		int l3 = xPos + yPos * width;
		if (l3 > pixels.length - 1) {
			l3 = pixels.length - 1;
		}
		for (int i4 = 0; i4 < pixelHeight; i4++) {
			for (int j4 = -pixelWidth; j4 < 0; j4++) {
				int l2 = (pixels[l3] >> 16 & 0xFF) * l1;
				int i3 = (pixels[l3] >> 8 & 0xFF) * l1;
				int j3 = (pixels[l3] & 0xFF) * l1;
				int k4 = (i2 + l2 >> 8 << 16) + (j2 + i3 >> 8 << 8) + (k2 + j3 >> 8);
				pixels[(l3++)] = k4;
			}
			l3 += k3;
		}
	}

	public static void method338(int i, int j, int k, int l, int i1, int j1) {
		method340(l, i1, i, k, j1);
		method340(l, i1, (i + j) - 1, k, j1);
		if (j >= 3) {
			method342(l, j1, k, i + 1, j - 2);
			method342(l, (j1 + i1) - 1, k, i + 1, j - 2);
		}
	}

	public static void method339(int i, int j, int k, int l) {
		if (i < topY || i >= bottomY)
			return;
		if (l < topX) {
			k -= topX - l;
			l = topX;
		}
		if (l + k > bottomX)
			k = bottomX - l;
		int i1 = l + i * width;
		for (int j1 = 0; j1 < k; j1++)
			pixels[i1 + j1] = j;

	}

	private static void method340(int i, int j, int k, int l, int i1) {
		if (k < topY || k >= bottomY)
			return;
		if (i1 < topX) {
			j -= topX - i1;
			i1 = topX;
		}
		if (i1 + j > bottomX)
			j = bottomX - i1;
		int j1 = 256 - l;
		int k1 = (i >> 16 & 0xff) * l;
		int l1 = (i >> 8 & 0xff) * l;
		int i2 = (i & 0xff) * l;
		int i3 = i1 + k * width;
		for (int j3 = 0; j3 < j; j3++) {
			int j2 = (pixels[i3] >> 16 & 0xff) * j1;
			int k2 = (pixels[i3] >> 8 & 0xff) * j1;
			int l2 = (pixels[i3] & 0xff) * j1;
			int k3 = ((k1 + j2 >> 8) << 16) + ((l1 + k2 >> 8) << 8) + (i2 + l2 >> 8);
			pixels[i3++] = k3;
		}

	}

	public static void method341(int i, int j, int k, int l) {
		if (l < topX || l >= bottomX)
			return;
		if (i < topY) {
			k -= topY - i;
			i = topY;
		}
		if (i + k > bottomY)
			k = bottomY - i;
		int j1 = l + i * width;
		for (int k1 = 0; k1 < k; k1++)
			pixels[j1 + k1 * width] = j;

	}

	private static void method342(int i, int j, int k, int l, int i1) {
		if (j < topX || j >= bottomX)
			return;
		if (l < topY) {
			i1 -= topY - l;
			l = topY;
		}
		if (l + i1 > bottomY)
			i1 = bottomY - l;
		int j1 = 256 - k;
		int k1 = (i >> 16 & 0xff) * k;
		int l1 = (i >> 8 & 0xff) * k;
		int i2 = (i & 0xff) * k;
		int i3 = j + l * width;
		for (int j3 = 0; j3 < i1; j3++) {
			int j2 = (pixels[i3] >> 16 & 0xff) * j1;
			int k2 = (pixels[i3] >> 8 & 0xff) * j1;
			int l2 = (pixels[i3] & 0xff) * j1;
			int k3 = ((k1 + j2 >> 8) << 16) + ((l1 + k2 >> 8) << 8) + (i2 + l2 >> 8);
			pixels[i3] = k3;
			i3 += width;
		}
	}
	public static void drawBox(int leftX, int topY, int width, int height, int rgbColour) {
		if (leftX < DrawingArea.topX) {
			width -= DrawingArea.topX - leftX;
			leftX = DrawingArea.topX;
		}
		if (topY < DrawingArea.topY) {
			height -= DrawingArea.topY - topY;
			topY = DrawingArea.topY;
		}
		if (leftX + width > bottomX)
			width = bottomX - leftX;
		if (topY + height > bottomY)
			height = bottomY - topY;
		int leftOver = DrawingArea.width - width;
		int pixelIndex = leftX + topY * DrawingArea.width;
		for (int rowIndex = 0; rowIndex < height; rowIndex++) {
			for (int columnIndex = 0; columnIndex < width; columnIndex++)
				pixels[pixelIndex++] = rgbColour;
			pixelIndex += leftOver;
		}
	}

	public static void drawVerticalLine2(int xPosition, int yPosition, int height, int rgbColour){
		if(xPosition < topX || xPosition >= bottomX)
			return;
		if(yPosition < topY){
			height -= topY - yPosition;
			yPosition = topY;
		}
		if(yPosition + height > bottomY)
			height = bottomY - yPosition;
		int pixelIndex = xPosition + yPosition * width;
		for(int rowIndex = 0; rowIndex < height; rowIndex++)
			pixels[pixelIndex + rowIndex * width] = rgbColour;
	}

	public static void drawHorizontalLine2(int xPosition, int yPosition, int width, int rgbColour){
		if(yPosition < topY || yPosition >= bottomY)
			return;
		if(xPosition < topX){
			width -= topX - xPosition;
			xPosition = topX;
		}
		if(xPosition + width > bottomX)
			width = bottomX - xPosition;
		int pixelIndex = xPosition + yPosition * DrawingArea.width;
		for(int i = 0; i < width; i++)
			pixels[pixelIndex + i] = rgbColour;
	}

	public static void drawBoxOutline(int leftX, int topY, int width, int height, int rgbColour){
		drawHorizontalLine2(leftX, topY, width, rgbColour);
		drawHorizontalLine2(leftX, (topY + height) - 1, width, rgbColour);
		drawVerticalLine2(leftX, topY, height, rgbColour);
		drawVerticalLine2((leftX + width) - 1, topY, height, rgbColour);
	}

	public static void drawVerticalLine(int xPosition, int yPosition, int height, int rgbColour) {
		if (xPosition < topX || xPosition >= bottomX)
			return;
		if (yPosition < topY) {
			height -= topY - yPosition;
			yPosition = topY;
		}
		if (yPosition + height > bottomY)
			height = bottomY - yPosition;
		int pixelIndex = xPosition + yPosition * width;
		for (int rowIndex = 0; rowIndex < height; rowIndex++)
			pixels[pixelIndex + rowIndex * width] = rgbColour;
	}

	public static void drawAlphaBox(int x, int y, int lineWidth, int lineHeight, int color, int alpha) {// drawAlphaHorizontalLine
		if (y < topY) {
			if (y > (topY - lineHeight)) {
				lineHeight -= (topY - y);
				y += (topY - y);
			} else {
				return;
			}
		}
		if (y + lineHeight > bottomY) {
			lineHeight -= y + lineHeight - bottomY;
		}
		//if (y >= bottomY - lineHeight)
		//return;
		if (x < topX) {
			lineWidth -= topX - x;
			x = topX;
		}
		if (x + lineWidth > bottomX)
			lineWidth = bottomX - x;
		for(int yOff = 0; yOff < lineHeight; yOff++) {
			int i3 = x + (y + (yOff)) * width;
			for (int j3 = 0; j3 < lineWidth; j3++) {
				//int alpha2 = (lineWidth-j3) / (lineWidth/alpha);
				int j1 = 256 - alpha;//alpha2 is for gradient
				int k1 = (color >> 16 & 0xff) * alpha;
				int l1 = (color >> 8 & 0xff) * alpha;
				int i2 = (color & 0xff) * alpha;
				int j2 = (pixels[i3] >> 16 & 0xff) * j1;
				int k2 = (pixels[i3] >> 8 & 0xff) * j1;
				int l2 = (pixels[i3] & 0xff) * j1;
				int k3 = ((k1 + j2 >> 8) << 16) + ((l1 + k2 >> 8) << 8)
						+ (i2 + l2 >> 8);
				pixels[i3++] = k3;
			}
		}
	}

	public static void defaultDrawingAreaSize() {
		topX = 0;
		topY = 0;
		bottomX = width;
		clip_bottom = height;//could cause issues cam so keep eye out
		bottomY = height;
		centerX = bottomX;
		centerY = bottomX / 2;
	}



	public void drawAlphaGradientOnSprite(Sprite sprite, int x, int y, int gradientWidth,
										  int gradientHeight, int startColor, int endColor, int alpha) {
		int k1 = 0;
		int l1 = 0x10000 / gradientHeight;
		if (x < topX) {
			gradientWidth -= topX - x;
			x = topX;
		}
		if (y < topY) {
			k1 += (topY - y) * l1;
			gradientHeight -= topY - y;
			y = topY;
		}
		if (x + gradientWidth > bottomX)
			gradientWidth = bottomX - x;
		if (y + gradientHeight > bottomY)
			gradientHeight = bottomY - y;
		int i2 = width - gradientWidth;
		int result_alpha = 256 - alpha;
		int total_pixels = x + y * width;
		for (int k2 = -gradientHeight; k2 < 0; k2++) {
			int gradient1 = 0x10000 - k1 >> 8;
			int gradient2 = k1 >> 8;
			int gradient_color = ((startColor & 0xff00ff) * gradient1
					+ (endColor & 0xff00ff) * gradient2 & 0xff00ff00)
					+ ((startColor & 0xff00) * gradient1 + (endColor & 0xff00)
					* gradient2 & 0xff0000) >>> 8;
			int color = ((gradient_color & 0xff00ff) * alpha >> 8 & 0xff00ff)
					+ ((gradient_color & 0xff00) * alpha >> 8 & 0xff00);
			for (int k3 = -gradientWidth; k3 < 0; k3++) {
				int colored_pixel = pixels[total_pixels];
				colored_pixel = ((colored_pixel & 0xff00ff) * result_alpha >> 8 & 0xff00ff)
						+ ((colored_pixel & 0xff00) * result_alpha >> 8 & 0xff00);
				pixels[total_pixels++] = color + colored_pixel;
			}
			total_pixels += i2;
			k1 += l1;
		}
	}



	public static void setDrawingArea(int _height, int _topX, int _width, int _topY) {
		if (_topX < 0)
			_topX = 0;
		if (_topY < 0)
			_topY = 0;
		if (_width > width)
			_width = width;
		if (_height > height)
			_height = height;
		topX = _topX;
		topY = _topY;
		bottomX = _width;
		bottomY = _height;
		centerX = bottomX;
		centerY = bottomX / 2;
		anInt1387 = bottomY / 2;
	}

	public static void setAllPixelsToZero() {
		int i = width * height;
		for (int j = 0; j < i; j++)
			pixels[j] = 0;
	}

	public static boolean drawHorizontalLine(int yPos, int lineColor, int lineWidth, int xPos) {// method339
		if (yPos < topY || yPos >= bottomY) {
			return false;
		}
		if (xPos < topX) {
			lineWidth -= topX - xPos;
			xPos = topX;
		}
		if (xPos + lineWidth > bottomX)
			lineWidth = bottomX - xPos;
		int i1 = xPos + yPos * width;
		for (int j1 = 0; j1 < lineWidth; j1++)
			pixels[i1 + j1] = lineColor;
		return true;
	}

	public static void method335(int i, int j, int k, int l, int i1, int k1) {
		if (k1 < topX) {
			k -= topX - k1;
			k1 = topX;
		}
		if (j < topY) {
			l -= topY - j;
			j = topY;
		}
		if (k1 + k > bottomX)
			k = bottomX - k1;
		if (j + l > bottomY)
			l = bottomY - j;
		int l1 = 256 - i1;
		int i2 = (i >> 16 & 0xff) * i1;
		int j2 = (i >> 8 & 0xff) * i1;
		int k2 = (i & 0xff) * i1;
		int k3 = width - k;
		int l3 = k1 + j * width;
		for (int i4 = 0; i4 < l; i4++) {
			for (int j4 = -k; j4 < 0; j4++) {
				int l2 = (pixels[l3] >> 16 & 0xff) * l1;
				int i3 = (pixels[l3] >> 8 & 0xff) * l1;
				int j3 = (pixels[l3] & 0xff) * l1;
				int k4 = ((i2 + l2 >> 8) << 16) + ((j2 + i3 >> 8) << 8)
						+ (k2 + j3 >> 8);
				pixels[l3++] = k4;
			}

			l3 += k3;
		}
	}

	public static void drawBorder(int x, int y, int width, int height, int color) {
		DrawingArea.drawPixels(1, y, x, color, width);
		DrawingArea.drawPixels(height, y, x, color, 1);
		DrawingArea.drawPixels(1, y + height, x, color, width + 1);
		DrawingArea.drawPixels(height, y, x + width, color, 1);
	}

	public static void drawPixels(int drawHeight, int yPosition, int xPositon, int color, int drawWidth) {
		if (xPositon < topX) {
			drawWidth -= topX - xPositon;
			xPositon = topX;
		}
		if (yPosition < topY) {
			drawHeight -= topY - yPosition;
			yPosition = topY;
		}
		if (xPositon + drawWidth > bottomX)
			drawWidth = bottomX - xPositon;
		if (yPosition + drawHeight > bottomY)
			drawHeight = bottomY - yPosition;
		int k1 = width - drawWidth;
		int l1 = xPositon + yPosition * width;
		for (int i2 = -drawHeight; i2 < 0; i2++) {
			for (int j2 = -drawWidth; j2 < 0; j2++)
				pixels[l1++] = color;

			l1 += k1;
		}

	}

	public static void fillRectangle(int x, int y, int width, int height, int colour) {
		if (x < topX) {
			width -= topX - x;
			x = topX;
		}
		if (y < topY) {
			height -= topY - y;
			y = topY;
		}
		if (x + width > bottomX)
			width = bottomX - x;
		if (y + height > bottomY)
			height = bottomY - y;
		int k1 = DrawingArea.width - width;
		int l1 = x + y * DrawingArea.width;
		if (l1 > pixels.length - 1) {
			l1 = pixels.length - 1;
		}
		for (int i2 = -height; i2 < 0; i2++) {
			for (int j2 = -width; j2 < 0; j2++)
				pixels[l1++] = colour;

			l1 += k1;
		}

	}

	public static void fillPixels(int i, int j, int k, int l, int i1) {
		method339(i1, l, j, i);
		method339((i1 + k) - 1, l, j, i);
		method341(i1, l, k, i);
		method341(i1, l, k, (i + j) - 1);
	}


	DrawingArea() {
	}

	public static int pixels[];
	public static int width;
	public static int height;
	public static int topY;
	public static int bottomY;
	public static int topX;
	public static int bottomX;
	public static int centerX;
	public static int centerY;
	public static int anInt1387;


	//Money pouch C.T
	public static void fillCircle(int x, int y, int radius, int color) {
		int y1 = y - radius;
		if (y1 < 0) {
			y1 = 0;
		}
		int y2 = y + radius;
		if (y2 >= height) {
			y2 = height - 1;
		}
		for (int iy = y1; iy <= y2; iy++) {
			int dy = iy - y;
			int dist = (int) Math.sqrt(radius * radius - dy * dy);
			int x1 = x - dist;
			if (x1 < 0) {
				x1 = 0;
			}
			int x2 = x + dist;
			if (x2 >= width) {
				x2 = width - 1;
			}
			int pos = x1 + iy * width;
			for (int ix = x1; ix <= x2; ix++) {
				pixels[pos++] = color;
			}
		}
	}
	public static int clip_bottom;
	public static void drawAlphaPixels(int x, int y, int w, int h, int color, int alpha) {
		if (x < topX) {
			w -= topX - x;
			x = topX;
		}
		if (y < topY) {
			h -= topY - y;
			y = topY;
		}
		if (x + w > bottomX)
			w = bottomX - x;
		if (y + h > clip_bottom)
			h = clip_bottom - y;
		int l1 = 256 - alpha;
		int i2 = (color >> 16 & 0xff) * alpha;
		int j2 = (color >> 8 & 0xff) * alpha;
		int k2 = (color & 0xff) * alpha;
		int k3 = width - w;
		int l3 = x + y * width;
		for (int i4 = 0; i4 < h; i4++) {
			for (int j4 = -w; j4 < 0; j4++) {
				int l2 = (pixels[l3] >> 16 & 0xff) * l1;
				int i3 = (pixels[l3] >> 8 & 0xff) * l1;
				int j3 = (pixels[l3] & 0xff) * l1;
				int k4 = ((i2 + l2 >> 8) << 16) + ((j2 + i3 >> 8) << 8)
						+ (k2 + j3 >> 8);
				pixels[l3++] = k4;
			}

			l3 += k3;
		}
	}


}
