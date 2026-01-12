package com.client.renderer;

public interface Renderer {
    void drawFrame(RenderContext context);

    void resize(int width, int height);

    void shutdown();
}
