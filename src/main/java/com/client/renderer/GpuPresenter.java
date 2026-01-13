package com.client.renderer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public final class GpuPresenter extends AWTGLCanvas {

    private static final String VERTEX_SHADER = ""
            + "#version 120\n"
            + "attribute vec2 aPos;\n"
            + "attribute vec2 aUv;\n"
            + "varying vec2 vUv;\n"
            + "void main() {\n"
            + "  vUv = aUv;\n"
            + "  gl_Position = vec4(aPos, 0.0, 1.0);\n"
            + "}\n";

    private static final String FRAGMENT_SHADER = ""
            + "#version 120\n"
            + "uniform sampler2D uTex;\n"
            + "uniform vec2 uTexel;\n"
            + "uniform float uSharpen;\n"
            + "uniform float uSaturation;\n"
            + "varying vec2 vUv;\n"
            + "vec3 applySaturation(vec3 color, float saturation) {\n"
            + "  float luma = dot(color, vec3(0.299, 0.587, 0.114));\n"
            + "  return mix(vec3(luma), color, saturation);\n"
            + "}\n"
            + "void main() {\n"
            + "  vec3 center = texture2D(uTex, vUv).rgb;\n"
            + "  vec3 north = texture2D(uTex, vUv + vec2(0.0, uTexel.y)).rgb;\n"
            + "  vec3 south = texture2D(uTex, vUv - vec2(0.0, uTexel.y)).rgb;\n"
            + "  vec3 east  = texture2D(uTex, vUv + vec2(uTexel.x, 0.0)).rgb;\n"
            + "  vec3 west  = texture2D(uTex, vUv - vec2(uTexel.x, 0.0)).rgb;\n"
            + "  vec3 blur = (north + south + east + west) * 0.25;\n"
            + "  vec3 sharpened = center + (center - blur) * uSharpen;\n"
            + "  vec3 saturated = applySaturation(sharpened, uSaturation);\n"
            + "  gl_FragColor = vec4(saturated, 1.0);\n"
            + "}\n";

    private int programId = -1;
    private int vertexShaderId = -1;
    private int fragmentShaderId = -1;

    private int textureId = -1;
    private int vertexBufferId = -1;
    private int indexBufferId = -1;

    private int attribPos = -1;
    private int attribUv = -1;

    private int uniformTexel = -1;
    private int uniformSharpen = -1;
    private int uniformSaturation = -1;
    private int uniformTexture = -1;

    private int textureWidth = -1;
    private int textureHeight = -1;

    private boolean initialized = false;
    private final boolean debugEnabled = Boolean.getBoolean("gpu.debug");
    private boolean linearFilter = false;

    public GpuPresenter(GLData data) {
        super(data);
        setFocusable(true);
        setIgnoreRepaint(true);
    }

    public void setLinearFilter(boolean linearFilter) {
        this.linearFilter = linearFilter;

        // Only touch GL state inside the GL context.
        if (initialized && textureId != -1) {
            try {
                runInContext(() -> {
                    bindTextureParameters();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Call this from your client loop to draw the latest software frame.
     */
    public void present(RenderContext context) {
        try {
            runInContext(() -> {
                if (!initialized) {
                    GL.createCapabilities();
                    setupProgram();
                    setupBuffers();
                    setupTexture();
                    initialized = true;

                    if (debugEnabled) {
                        System.out.println("[GPU] Vendor: " + glGetString(GL_VENDOR));
                        System.out.println("[GPU] Renderer: " + glGetString(GL_RENDERER));
                        System.out.println("[GPU] Version: " + glGetString(GL_VERSION));
                    }
                }

                renderFrameInContext(context);
            });

            // Present backbuffer after drawing.
            swapBuffers();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void initGL() {
        // We init lazily in present(). Keep empty.
    }

    @Override
    public void paintGL() {
        // Rendering is driven by present() from the client loop.
    }

    public void shutdown() {
        if (!initialized) return;

        try {
            runInContext(() -> {
                glUseProgram(0);

                if (programId != -1) glDeleteProgram(programId);
                if (vertexShaderId != -1) glDeleteShader(vertexShaderId);
                if (fragmentShaderId != -1) glDeleteShader(fragmentShaderId);

                if (textureId != -1) glDeleteTextures(textureId);
                if (vertexBufferId != -1) glDeleteBuffers(vertexBufferId);
                if (indexBufferId != -1) glDeleteBuffers(indexBufferId);

                programId = -1;
                vertexShaderId = -1;
                fragmentShaderId = -1;
                textureId = -1;
                vertexBufferId = -1;
                indexBufferId = -1;

                initialized = false;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderFrameInContext(RenderContext context) {
        glViewport(0, 0, context.getCanvasWidth(), context.getCanvasHeight());
        glClearColor(0f, 0f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT);

        glUseProgram(programId);

        glUniform1i(uniformTexture, 0);
        glUniform2f(uniformTexel,
                1.0f / context.getBufferWidth(),
                1.0f / context.getBufferHeight());
        glUniform1f(uniformSharpen, context.getSharpen());
        glUniform1f(uniformSaturation, context.getSaturation());

        if (!context.shouldSkipUploadWhenUnfocused() || context.isFocused()) {
            uploadTexture(context);
        }

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);

        glEnableVertexAttribArray(attribPos);
        glEnableVertexAttribArray(attribUv);

        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
        glVertexAttribPointer(attribPos, 2, GL_FLOAT, false, 16, 0);
        glVertexAttribPointer(attribUv, 2, GL_FLOAT, false, 16, 8);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferId);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glDisableVertexAttribArray(attribPos);
        glDisableVertexAttribArray(attribUv);

        glBindTexture(GL_TEXTURE_2D, 0);
        glUseProgram(0);
    }

    private void setupProgram() {
        vertexShaderId = compileShader(GL_VERTEX_SHADER, VERTEX_SHADER);
        fragmentShaderId = compileShader(GL_FRAGMENT_SHADER, FRAGMENT_SHADER);

        programId = glCreateProgram();
        glAttachShader(programId, vertexShaderId);
        glAttachShader(programId, fragmentShaderId);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            String log = glGetProgramInfoLog(programId);
            System.err.println("[GPU] Shader link failed: " + log);
        }

        attribPos = glGetAttribLocation(programId, "aPos");
        attribUv = glGetAttribLocation(programId, "aUv");

        uniformTexel = glGetUniformLocation(programId, "uTexel");
        uniformSharpen = glGetUniformLocation(programId, "uSharpen");
        uniformSaturation = glGetUniformLocation(programId, "uSaturation");
        uniformTexture = glGetUniformLocation(programId, "uTex");
    }

    private void setupBuffers() {
        float[] vertices = {
                -1f, -1f, 0f, 0f,
                1f, -1f, 1f, 0f,
                1f,  1f, 1f, 1f,
                -1f,  1f, 0f, 1f
        };
        int[] indices = {0, 1, 2, 2, 3, 0};

        vertexBufferId = glGenBuffers();
        indexBufferId = glGenBuffers();

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
        vertexBuffer.put(vertices).flip();

        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        ByteBuffer indexBuffer = BufferUtils.createByteBuffer(indices.length * 4);
        for (int index : indices) {
            indexBuffer.putInt(index);
        }
        indexBuffer.flip();

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    private void setupTexture() {
        textureId = glGenTextures();
        bindTextureParameters();
    }

    private void bindTextureParameters() {
        glBindTexture(GL_TEXTURE_2D, textureId);

        int filter = linearFilter ? GL_LINEAR : GL_NEAREST;
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    private int compileShader(int type, String src) {
        int shader = glCreateShader(type);
        glShaderSource(shader, src);
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(shader);
            System.err.println("[GPU] Shader compile failed: " + log);
        }
        return shader;
    }

    private void uploadTexture(RenderContext context) {
        long start = System.nanoTime();

        int width = context.getBufferWidth();
        int height = context.getBufferHeight();

        if (width != textureWidth || height != textureHeight) {
            textureWidth = width;
            textureHeight = height;

            glBindTexture(GL_TEXTURE_2D, textureId);
            glTexImage2D(
                    GL_TEXTURE_2D, 0, GL_RGBA8,
                    width, height, 0,
                    GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV,
                    (ByteBuffer) null
            );
            glBindTexture(GL_TEXTURE_2D, 0);
        }

        glBindTexture(GL_TEXTURE_2D, textureId);

        ByteBuffer buffer = BufferUtils.createByteBuffer(context.getPixels().length * 4);
        buffer.asIntBuffer().put(context.getPixels());

        glTexSubImage2D(
                GL_TEXTURE_2D, 0,
                0, 0,
                width, height,
                GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV,
                buffer
        );

        if (linearFilter) {
            glGenerateMipmap(GL_TEXTURE_2D);
        }

        glBindTexture(GL_TEXTURE_2D, 0);

        if (debugEnabled) {
            float millis = (System.nanoTime() - start) / 1_000_000f;
            System.out.println(String.format("[GPU] Upload time: %.3f ms", millis));
        }
    }
}
