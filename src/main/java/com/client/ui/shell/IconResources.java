package com.client.ui.shell;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public final class IconResources {

    private IconResources() {}

    public static ImageIcon load(String resourcePath, int size) {
        // resourcePath example: "/vykna/icons/market.png"
        try (InputStream in = IconResources.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                System.err.println("[Icons] Missing resource: " + resourcePath);
                return null;
            }
            BufferedImage img = ImageIO.read(in);
            if (img == null) {
                System.err.println("[Icons] Failed to decode: " + resourcePath);
                return null;
            }

            if (size > 0 && (img.getWidth() != size || img.getHeight() != size)) {
                Image scaled = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }

            return new ImageIcon(img);
        } catch (IOException e) {
            System.err.println("[Icons] Error loading: " + resourcePath);
            e.printStackTrace();
            return null;
        }
    }
}
