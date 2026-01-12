package com.client.ui.shell;

import javax.swing.*;
import java.awt.*;

public final class Divider extends JComponent {
    public Divider() {
        setPreferredSize(new Dimension(1, 10));
        setMinimumSize(new Dimension(1, 10));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        setOpaque(false);
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            int y = getHeight() / 2;
            g2.setColor(new Color(32, 34, 36));
            g2.drawLine(0, y, getWidth(), y);
        } finally {
            g2.dispose();
        }
    }
}
