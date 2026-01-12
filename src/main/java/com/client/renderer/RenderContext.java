package com.client.renderer;

public final class RenderContext {
    private final int[] pixels;
    private final int bufferWidth;
    private final int bufferHeight;
    private final int canvasWidth;
    private final int canvasHeight;
    private final boolean vsyncEnabled;
    private final boolean focused;
    private final boolean skipUploadWhenUnfocused;
    private final float sharpen;
    private final float saturation;

    public RenderContext(int[] pixels, int bufferWidth, int bufferHeight, int canvasWidth, int canvasHeight,
                         boolean vsyncEnabled, boolean focused, boolean skipUploadWhenUnfocused,
                         float sharpen, float saturation) {
        this.pixels = pixels;
        this.bufferWidth = bufferWidth;
        this.bufferHeight = bufferHeight;
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.vsyncEnabled = vsyncEnabled;
        this.focused = focused;
        this.skipUploadWhenUnfocused = skipUploadWhenUnfocused;
        this.sharpen = sharpen;
        this.saturation = saturation;
    }

    public int[] getPixels() {
        return pixels;
    }

    public int getBufferWidth() {
        return bufferWidth;
    }

    public int getBufferHeight() {
        return bufferHeight;
    }

    public int getCanvasWidth() {
        return canvasWidth;
    }

    public int getCanvasHeight() {
        return canvasHeight;
    }

    public boolean isVsyncEnabled() {
        return vsyncEnabled;
    }

    public boolean isFocused() {
        return focused;
    }

    public boolean shouldSkipUploadWhenUnfocused() {
        return skipUploadWhenUnfocused;
    }

    public float getSharpen() {
        return sharpen;
    }

    public float getSaturation() {
        return saturation;
    }
}
