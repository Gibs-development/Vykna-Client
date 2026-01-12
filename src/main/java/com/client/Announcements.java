package com.client;

/**
 * Login/viewport announcement "tip card" (no scrolling).
 *
 * - Sits near the bottom of the game viewport (great for login screen)
 * - Fade-out -> gap -> fade-in (no merged/overlap text)
 * - Subtle dark glass strip + red accent line
 * - Feathered bar edges
 * - Tiny "ember" drift particles with a soft halo
 *
 * Java 11 compatible.
 */
public class Announcements {

    // ---- Tuning ----
    private static final int BAR_HEIGHT = 24;

    // Timing (assuming ~50fps)
    private static final int HOLD = 110;       // hold message visible
    private static final int FADE_OUT = 18;    // fade old out
    private static final int GAP = 6;          // no text for a moment
    private static final int FADE_IN = 18;     // fade new in
    private static final int CYCLE = HOLD + FADE_OUT + GAP + FADE_IN;

    // Messages
    private static final String[] ANNOUNCEMENTS = {
            "[INFO] You control the development, make a suggestion in the side panel today!",
            "[TIP] Voting gives you some great benefits each day and only takes a second!",
            "[INFO] Have an idea? Or see something that needs to change? Let us know!",
            "[DISCORD] Don't miss out on events and being part of the community, join our discord!"
    };

    // State
    private static int ticks = 0;
    private static int fadeTick = 0;
    private static int emberSeed = 1337;

    public static void displayAnnouncements() {
        final Client c = Client.getInstance();
        if (c == null) return;

        int w = Client.currentGameWidth;
        if (w <= 0) w = Configuration.frameWidth;

        int h = Client.currentGameHeight;
        if (h <= 0) h = Configuration.frameHeight;

        // Near bottom edge
        int y = h - BAR_HEIGHT - 6;

        fadeTick++;

        int idxA = ticks % ANNOUNCEMENTS.length;
        int idxB = (ticks + 1) % ANNOUNCEMENTS.length;

        int t = fadeTick % CYCLE;

        // Figure out which message is currently being shown
        // During FADE_IN we show B, otherwise we show A.
        boolean showingB = t >= (HOLD + FADE_OUT + GAP);

        Msg msgA = parse(ANNOUNCEMENTS[idxA]);
        Msg msgB = parse(ANNOUNCEMENTS[idxB]);

        int alphaA = 0;
        int alphaB = 0;

        if (t < HOLD) {
            alphaA = 255;
            alphaB = 0;
        } else if (t < HOLD + FADE_OUT) {
            float p = (t - HOLD) / (float) FADE_OUT;
            alphaA = (int) (255 * (1f - p));
            alphaB = 0;
        } else if (t < HOLD + FADE_OUT + GAP) {
            alphaA = 0;
            alphaB = 0;
        } else {
            float p = (t - (HOLD + FADE_OUT + GAP)) / (float) FADE_IN;
            alphaA = 0;
            alphaB = (int) (255 * p);
        }

        // Advance at end of cycle
        if (t == CYCLE - 1) ticks++;

        // ---- Background: subtle "glass" strip ----
        int leftPad = 10;
        int rightPad = 10;

        int barX = leftPad;
        int barW = w - leftPad - rightPad;

        // Main bar
        TextDrawingArea.drawAlphaGradient(barX, y, barW, BAR_HEIGHT, 0x0d0e10, 0x17191c, 120);

        // Feather edges (slightly stronger looks nicer)
        featherEdges(barX, y, barW, BAR_HEIGHT, 10);

        // Red accent line along bottom
        DrawingArea.drawHorizontalLine(y + BAR_HEIGHT - 1, barX + 2, barW - 4, 0x7a1b1b);

        // Embers
        drawEmbers(barX + 2, y + 2, barW - 4, BAR_HEIGHT - 4);

        // ---- Centered text ----
        int textY = y + 16;

        // Choose the currently visible string for stable centering
        Msg dominant = showingB ? msgB : msgA;
        String dominantLine = buildLine(dominant);

        int lineW = textWidth(dominantLine);
        int x = barX + Math.max(8, (barW - lineW) / 2);

        // Draw (note: no overlap merging now due to fade schedule)
        drawFadedLine(x, textY, msgA, alphaA);
        drawFadedLine(x, textY, msgB, alphaB);
    }

    // ---------------------------
    // Helpers
    // ---------------------------

    private static final class Msg {
        String prefix;
        String msg;
        int prefixCol;
    }

    private static Msg parse(String raw) {
        Msg m = new Msg();
        m.prefixCol = 0xbfc4c9;
        m.prefix = null;
        m.msg = raw;

        if (raw.startsWith("[") && raw.contains("]")) {
            int end = raw.indexOf(']');
            if (end > 1 && end < 14) {
                m.prefix = raw.substring(0, end + 1);
                m.msg = raw.substring(end + 1).trim();

                if ("[NEWS]".equals(m.prefix)) m.prefixCol = 0xd64a4a;
                else if ("[TIP]".equals(m.prefix)) m.prefixCol = 0xe0c25a;
                else if ("[DISCORD]".equals(m.prefix)) m.prefixCol = 0x6b79ff;
                else if ("[INFO]".equals(m.prefix)) m.prefixCol = 0xbfc4c9;
            }
        }
        return m;
    }

    private static String buildLine(Msg m) {
        if (m.prefix == null) return m.msg;
        return m.prefix + " " + m.msg;
    }

    private static int textWidth(String s) {
        // If this doesn't compile, your width method is named differently.
        // Common alternatives: method384(s) / getTextDisplayedWidth(s)
        return Client.smallText.getTextWidth(s);
    }

    private static void drawFadedLine(int x, int y, Msg m, int alpha) {
        if (alpha <= 0) return;

        int shadow = scaleRgb(0x000000, alpha);
        int msgCol = scaleRgb(0xe8eaed, alpha);
        int prefixCol = scaleRgb(m.prefixCol, alpha);

        int xx = x;

        if (m.prefix != null) {
            Client.smallText.method389(true, xx + 1, shadow, m.prefix, y);
            Client.smallText.method389(true, xx, prefixCol, m.prefix, y);
            xx += textWidth(m.prefix) + 8; // use real width for perfect spacing
        }

        Client.smallText.method389(true, xx + 1, shadow, m.msg, y);
        Client.smallText.method389(true, xx, msgCol, m.msg, y);
    }

    /**
     * Scale RGB brightness by alpha (0..255). Not true alpha, but looks great on dark UI.
     */
    private static int scaleRgb(int rgb, int alpha) {
        float a = alpha / 255f;
        int r = (int) (((rgb >> 16) & 0xff) * a);
        int g = (int) (((rgb >> 8) & 0xff) * a);
        int b = (int) ((rgb & 0xff) * a);
        return (r << 16) | (g << 8) | b;
    }

    private static void featherEdges(int x, int y, int w, int h, int feather) {
        // Fade edges by drawing 1px alpha columns to "round/soften" the bar
        for (int i = 0; i < feather; i++) {
            float p = i / (float) feather;

            // left: strong -> weak
            int aL = (int) (120 * (1f - p));
            TextDrawingArea.drawAlphaGradient(x + i, y, 1, h, 0x0d0e10, 0x17191c, aL);

            // right: weak -> strong (reverse so it fades out at the edge)
            int aR = (int) (120 * p);
            TextDrawingArea.drawAlphaGradient(x + w - feather + i, y, 1, h, 0x0d0e10, 0x17191c, aR);
        }
    }

    /**
     * Cheap "embers": a few drifting pixels in red/orange with a soft halo.
     */
    private static void drawEmbers(int x, int y, int w, int h) {
        for (int i = 0; i < 12; i++) {
            int r = rand() & 0x7fffffff;

            int ex = x + (r % w);
            int ey = y + ((r >>> 8) % h);

            ex -= (fadeTick / 2) % w;
            ey -= (fadeTick / 18) % h;

            if (ex < x) ex += w;
            if (ey < y) ey += h;

            int col;
            int pick = (r >>> 16) & 3;
            if (pick == 0) col = 0xffa24a;
            else if (pick == 1) col = 0xff6b4a;
            else col = 0xd64a4a;

            // core
            DrawingArea.drawPixels(1, ey, ex, col, 1);

            // halo (soft)
            int halo = 0x2a0e0e;
            if (ex > x) DrawingArea.drawPixels(1, ey, ex - 1, halo, 1);
            if (ex + 1 < x + w) DrawingArea.drawPixels(1, ey, ex + 1, halo, 1);
            if (ey > y) DrawingArea.drawPixels(1, ey - 1, ex, halo, 1);
            if (ey + 1 < y + h) DrawingArea.drawPixels(1, ey + 1, ex, halo, 1);
        }
    }

    private static int rand() {
        emberSeed = emberSeed * 1103515245 + 12345 + (fadeTick * 17);
        return emberSeed;
    }
}
