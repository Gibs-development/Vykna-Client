package com.client.renderer;

import javax.swing.*;
import java.awt.*;

public final class GameComponentHost extends JPanel {
    public GameComponentHost() {
        super(new BorderLayout());
        setOpaque(true);
        setBackground(Color.BLACK);
    }

    public void setGameComponent(Component component) {
        removeAll();
        if (component != null) {
            add(component, BorderLayout.CENTER);
        }
        revalidate();
        repaint();
    }
}
