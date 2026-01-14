package com.client.ui.shell;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Clean RuneLite-ish title bar with drawn window controls (no font glyph issues).
 */
public final class TitleBar extends JPanel {

    private final JFrame frame;
    private Point dragOffset;
    private final WindowControlButton maxBtn;

    public TitleBar(String title, JFrame frame) {
        this.frame = frame;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(10, 30));
        setOpaque(true);
        setBackground(new Color(10, 11, 12));

        JLabel titleLabel = new JLabel("  " + title);
        titleLabel.setForeground(VyknaShell.TEXT);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 12f));
        add(titleLabel, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 5));
        controls.setOpaque(false);

        // Sidebar toggle (drawn chevron)
        WindowControlButton sidebarBtn = new WindowControlButton(WindowControlButton.Type.SIDEBAR);
        sidebarBtn.setToolTipText("Toggle sidebar");
        sidebarBtn.addActionListener(() -> {
            if (frame instanceof VyknaShell) {
                VyknaShell shell = (VyknaShell) frame;
                shell.toggleSidebar();
                sidebarBtn.setSidebarClosed(shell.isSidebarHidden());
            }
        });

        WindowControlButton minBtn = new WindowControlButton(WindowControlButton.Type.MINIMIZE);
        minBtn.setToolTipText("Minimize");
        minBtn.addActionListener(() -> frame.setState(Frame.ICONIFIED));

        maxBtn = new WindowControlButton(WindowControlButton.Type.MAXIMIZE);
        maxBtn.setToolTipText("Maximize");
        maxBtn.addActionListener(() -> {
            if (frame instanceof VyknaShell) {
                ((VyknaShell) frame).toggleMaximize();
            }
        });

        WindowControlButton closeBtn = new WindowControlButton(WindowControlButton.Type.CLOSE);
        closeBtn.setToolTipText("Close");
        closeBtn.addActionListener(() -> {
            frame.dispose();
            System.exit(0);
        });

        if (frame instanceof VyknaShell) {
            controls.add(sidebarBtn);
        }
        controls.add(minBtn);
        controls.add(maxBtn);
        controls.add(closeBtn);

        add(controls, BorderLayout.EAST);

        // Drag window
        MouseAdapter drag = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (frame instanceof VyknaShell && ((VyknaShell) frame).isMaximized()) {
                    return;
                }
                dragOffset = e.getPoint();
            }
            @Override public void mouseDragged(MouseEvent e) {
                if (frame instanceof VyknaShell && ((VyknaShell) frame).isMaximized()) {
                    return;
                }
                if (dragOffset == null) return;
                Point p = e.getLocationOnScreen();
                frame.setLocation(p.x - dragOffset.x, p.y - dragOffset.y);
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (frame instanceof VyknaShell) {
                    ((VyknaShell) frame).updateRestoreBounds();
                }
            }
        };
        addMouseListener(drag);
        addMouseMotionListener(drag);
        titleLabel.addMouseListener(drag);
        titleLabel.addMouseMotionListener(drag);
    }

    public void setMaximized(boolean maximized) {
        maxBtn.setMaximized(maximized);
    }

    /**
     * Drawn window control button (no text = no “...” fallback).
     */
    static final class WindowControlButton extends JComponent {
        enum Type { SIDEBAR, MINIMIZE, MAXIMIZE, CLOSE }

        private final Type type;
        private boolean hover = false;
        private boolean pressed = false;
        private boolean sidebarClosed = false;
        private boolean maximized = false;

        WindowControlButton(Type type) {
            this.type = type;
            setPreferredSize(new Dimension(26, 20));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            MouseAdapter m = new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; pressed = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
                @Override public void mouseReleased(MouseEvent e) {
                    if (pressed && hover) fireAction();
                    pressed = false;
                    repaint();
                }
            };
            addMouseListener(m);
        }

        void setSidebarClosed(boolean closed) {
            this.sidebarClosed = closed;
            repaint();
        }

        void setMaximized(boolean maximized) {
            this.maximized = maximized;
            repaint();
        }

        private Runnable onClick;

        void addActionListener(Runnable r) { this.onClick = r; }

        private void fireAction() {
            if (onClick != null) onClick.run();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                Color bg = hover ? new Color(26, 28, 30) : new Color(18, 19, 21);
                Color border = VyknaShell.BORDER;

                if (type == Type.CLOSE && hover) {
                    bg = new Color(VyknaShell.ACCENT.getRed(), VyknaShell.ACCENT.getGreen(), VyknaShell.ACCENT.getBlue(), 70);
                    border = VyknaShell.ACCENT;
                }
                if (pressed) bg = new Color(20, 21, 23);

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, w, h, 8, 8);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);

                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(VyknaShell.TEXT);

                int cx = w / 2;
                int cy = h / 2;

                switch (type) {
                    case MINIMIZE:
                        g2.drawLine(cx - 6, cy + 4, cx + 6, cy + 4);
                        break;

                    case MAXIMIZE:
                        if (maximized) {
                            g2.drawRect(cx - 5, cy - 5, 10, 8);
                            g2.drawRect(cx - 7, cy - 3, 10, 8);
                        } else {
                            g2.drawRect(cx - 6, cy - 5, 12, 10);
                        }
                        break;

                    case CLOSE:
                        g2.drawLine(cx - 5, cy - 5, cx + 5, cy + 5);
                        g2.drawLine(cx + 5, cy - 5, cx - 5, cy + 5);
                        break;

                    case SIDEBAR:
                        // Draw chevrons that indicate collapse/expand
                        if (!sidebarClosed) {
                            // collapse: < <
                            g2.drawLine(cx + 3, cy - 5, cx - 2, cy);
                            g2.drawLine(cx - 2, cy, cx + 3, cy + 5);

                            g2.drawLine(cx - 1, cy - 5, cx - 6, cy);
                            g2.drawLine(cx - 6, cy, cx - 1, cy + 5);
                        } else {
                            // expand: > >
                            g2.drawLine(cx - 3, cy - 5, cx + 2, cy);
                            g2.drawLine(cx + 2, cy, cx - 3, cy + 5);

                            g2.drawLine(cx + 1, cy - 5, cx + 6, cy);
                            g2.drawLine(cx + 6, cy, cx + 1, cy + 5);
                        }
                        break;
                }


            } finally {
                g2.dispose();
            }
        }
    }
}
