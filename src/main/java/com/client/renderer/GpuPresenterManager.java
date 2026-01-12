package com.client.renderer;

import org.lwjgl.opengl.awt.GLData;

import java.awt.*;
import java.util.Arrays;

public final class GpuPresenterManager {
    private final boolean debugEnabled = Boolean.getBoolean("gpu.debug");
    private GpuPresenter presenter;
    private int[] framePixels;
    private int frameWidth;
    private int frameHeight;
    private boolean enabled;
    private boolean linearFilter;
    private boolean vsyncEnabled = true;
    private boolean skipUploadWhenUnfocused = true;
    private float lastFrameTimeMs;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setLinearFilter(boolean linearFilter) {
        this.linearFilter = linearFilter;
        if (presenter != null) {
            presenter.setLinearFilter(linearFilter);
        }
    }

    public boolean isLinearFilter() {
        return linearFilter;
    }

    public void setVsyncEnabled(boolean vsyncEnabled) {
        this.vsyncEnabled = vsyncEnabled;
        if (presenter != null) {
            try {
                presenter.setSwapInterval(vsyncEnabled ? 1 : 0);
            } catch (Exception ignored) {
            }
        }
    }

    public boolean isVsyncEnabled() {
        return vsyncEnabled;
    }

    public void setSkipUploadWhenUnfocused(boolean skipUploadWhenUnfocused) {
        this.skipUploadWhenUnfocused = skipUploadWhenUnfocused;
    }

    public boolean isSkipUploadWhenUnfocused() {
        return skipUploadWhenUnfocused;
    }

    public float getLastFrameTimeMs() {
        return lastFrameTimeMs;
    }

    public Component getPresenterComponent() {
        ensurePresenter();
        return presenter;
    }

    public void beginFrame(int width, int height) {
        if (!enabled) {
            return;
        }
        if (width <= 0 || height <= 0) {
            return;
        }
        if (framePixels == null || frameWidth != width || frameHeight != height) {
            frameWidth = width;
            frameHeight = height;
            framePixels = new int[width * height];
        } else {
            Arrays.fill(framePixels, 0);
        }
    }

    public void blit(int[] pixels, int width, int height, int x, int y) {
        if (!enabled || framePixels == null || pixels == null) {
            return;
        }
        int startX = Math.max(0, x);
        int startY = Math.max(0, y);
        int endX = Math.min(frameWidth, x + width);
        int endY = Math.min(frameHeight, y + height);
        if (startX >= endX || startY >= endY) {
            return;
        }
        int srcOffset = (startY - y) * width + (startX - x);
        int dstOffset = startY * frameWidth + startX;
        int copyWidth = endX - startX;
        for (int row = startY; row < endY; row++) {
            System.arraycopy(pixels, srcOffset, framePixels, dstOffset, copyWidth);
            srcOffset += width;
            dstOffset += frameWidth;
        }
    }

    public void presentFrame(int canvasWidth, int canvasHeight, boolean focused, float sharpen, float saturation) {
        if (!enabled || framePixels == null) {
            return;
        }
        ensurePresenter();
        long frameStart = System.nanoTime();
        RenderContext context = new RenderContext(framePixels, frameWidth, frameHeight, canvasWidth, canvasHeight,
            vsyncEnabled, focused, skipUploadWhenUnfocused, sharpen, saturation);
        presenter.present(context);
        lastFrameTimeMs = (System.nanoTime() - frameStart) / 1_000_000f;
        if (debugEnabled) {
            System.out.println(String.format("[GPU] Frame time: %.3f ms", lastFrameTimeMs));
        }
    }

    public void shutdown() {
        if (presenter != null) {
            presenter.shutdown();
            presenter = null;
        }
    }

    private void ensurePresenter() {
        if (presenter != null) {
            return;
        }
        GLData data = new GLData();
        data.majorVersion = 2;
        data.minorVersion = 0;
        data.samples = 0;
        data.swapInterval = vsyncEnabled ? 1 : 0;
        presenter = new GpuPresenter(data);
        presenter.setLinearFilter(linearFilter);
    }
}
