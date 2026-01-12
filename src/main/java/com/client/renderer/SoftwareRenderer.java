package com.client.renderer;

public final class SoftwareRenderer implements Renderer {
    @Override
    public void drawFrame(RenderContext context) {
        // Software renderer stays in existing pipeline; present handled elsewhere.
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void shutdown() {
    }
}
