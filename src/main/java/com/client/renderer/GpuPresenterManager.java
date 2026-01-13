package com.client.renderer;

import org.lwjgl.opengl.awt.GLData;

import java.awt.*;
import java.util.Arrays;

public final class GpuPresenterManager {
    public enum PresenterState {
        OFF,
        ENABLING,
        ON,
        DISABLING
    }

    private final Object lifecycleLock = new Object();
    private boolean debugEnabled = false;
    private GpuPresenter presenter;
    private int[] framePixels;
    private int frameWidth;
    private int frameHeight;
    private boolean enabled;
    private boolean linearFilter;
    private boolean vsyncEnabled = true;
    private boolean skipUploadWhenUnfocused = true;
    private float lastFrameTimeMs;
    private volatile PresenterState state = PresenterState.OFF;
    private volatile String pendingGpuInfoMessage;
    private volatile Throwable initFailure;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled || Boolean.getBoolean("gpu.debug");
        if (presenter != null) {
            presenter.setDebugEnabled(this.debugEnabled);
        }
    }

    public PresenterState getState() {
        return state;
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
        // AWTGLCanvas doesn't expose setSwapInterval().
        // We'll implement platform-specific swap control later.
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
        return presenter;
    }

    public Component createPresenterIfNeeded() {
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
        GpuPresenter activePresenter = presenter;
        if (activePresenter == null) {
            return;
        }
        long frameStart = System.nanoTime();
        RenderContext context = new RenderContext(framePixels, frameWidth, frameHeight, canvasWidth, canvasHeight,
            vsyncEnabled, focused, skipUploadWhenUnfocused, sharpen, saturation);
        try {
            synchronized (lifecycleLock) {
                if (!enabled) {
                    return;
                }
                activePresenter.present(context);
            }
        } catch (RuntimeException ex) {
            recordInitFailure(ex);
            return;
        }
        lastFrameTimeMs = (System.nanoTime() - frameStart) / 1_000_000f;
        if (debugEnabled) {
            System.out.println(String.format("[GPU] Frame time: %.3f ms", lastFrameTimeMs));
        }

        String gpuInfo = activePresenter.consumeGpuInfoMessage();
        if (gpuInfo != null) {
            pendingGpuInfoMessage = gpuInfo;
        }
    }

    public void shutdown() {
        synchronized (lifecycleLock) {
            if (presenter != null) {
                presenter.shutdown();
                presenter = null;
            }
            enabled = false;
            state = PresenterState.OFF;
        }
    }

    public void beginEnable() {
        synchronized (lifecycleLock) {
            state = PresenterState.ENABLING;
            enabled = true;
            initFailure = null;
            pendingGpuInfoMessage = null;
        }
    }

    public void markEnabled() {
        synchronized (lifecycleLock) {
            state = PresenterState.ON;
        }
    }

    public void beginDisable() {
        synchronized (lifecycleLock) {
            state = PresenterState.DISABLING;
            enabled = false;
        }
    }

    public String consumeGpuInfoMessage() {
        String message = pendingGpuInfoMessage;
        pendingGpuInfoMessage = null;
        return message;
    }

    public Throwable consumeInitFailure() {
        Throwable failure = initFailure;
        initFailure = null;
        return failure;
    }

    private void ensurePresenter() {
        synchronized (lifecycleLock) {
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
            presenter.setDebugEnabled(debugEnabled);
            presenter.resetGpuInfoMessage();
        }
    }

    private void recordInitFailure(RuntimeException ex) {
        if (initFailure == null) {
            initFailure = ex;
        }
        pendingGpuInfoMessage = null;
        enabled = false;
        state = PresenterState.OFF;
    }
}
