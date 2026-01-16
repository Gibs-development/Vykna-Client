package com.client.ui.shell;

import com.client.Client;
import com.client.RSFont;
import com.client.Sprite;
import com.client.features.gameframe.ScreenMode;
import com.client.graphics.interfaces.impl.QuestTab;
import com.client.utilities.settings.InterfaceStyle;
import com.client.utilities.settings.Settings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class VyknaShell extends JFrame {

    private static final int SIDEBAR_WIDTH = 320;
    private static final int ICON_STRIP_WIDTH = 50;
    private static final int RESIZE_MARGIN = 6;

    private final JPanel sidebar = new JPanel();
    private final JPanel iconStrip = new JPanel();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private CardViewport cardViewport;
    private JScrollPane scroll;
    private final JPanel gameWrap = new JPanel(new BorderLayout());

    // Theme
    static final Color BG = new Color(14, 15, 16);
    static final Color PANEL = new Color(20, 21, 23);
    static final Color PANEL_2 = new Color(24, 26, 28);
    static final Color BORDER = new Color(38, 41, 44);

    static final Color TEXT = new Color(230, 232, 235);
    static final Color TEXT_DIM = new Color(170, 175, 180);

    // Red accent
    static final Color ACCENT = new Color(210, 40, 40);

    // Compact sizing
    private static final float FONT_BASE = 11.0f;
    private static final float FONT_HEADER = 12.5f;

    private final Client client;

    // Shell-local sizing state (don’t rely on Client.currentScreenMode being updated in sync)
    private ScreenMode shellMode = ScreenMode.FIXED;
    private boolean shellResizable = false;

    private boolean sidebarHidden = false;
    private boolean resizingWindow = false;
    private Point resizeStart;
    private Dimension resizeStartSize;
    private Rectangle resizeStartBounds;
    private ResizeDirection resizeDirection = ResizeDirection.NONE;
    private Rectangle restoreBounds;
    private boolean maximized = false;
    private final TitleBar titleBar;

    // Icon tabs
    private final IconTabButton settingsBtn;
    private final IconTabButton characterBtn;
    private final CharacterInfoPanel characterPanel;

    private static final Map<Integer, ImageIcon> chatIconCache = new HashMap<>();

    public VyknaShell(String title, Client client) {
        super(title);
        this.client = client;

        setUndecorated(true);
        setBackground(BG);

        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(true);
        root.setBackground(BG);
        root.setBorder(BorderFactory.createLineBorder(BORDER));
        setContentPane(root);

        titleBar = new TitleBar(title, this);
        root.add(titleBar, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(true);
        center.setBackground(BG);
        root.add(center, BorderLayout.CENTER);

        // Game
        gameWrap.setOpaque(true);
        gameWrap.setBackground(Color.BLACK);
        Component gameComponent = (Component) client;
        gameWrap.add(gameComponent, BorderLayout.CENTER);

        Dimension fixed = ScreenMode.FIXED.getDimensions();
        gameWrap.setPreferredSize(fixed);
        gameWrap.setMinimumSize(fixed);
        gameWrap.setMaximumSize(fixed);
        gameComponent.setPreferredSize(fixed);
        gameComponent.setMinimumSize(fixed);
        gameComponent.setMaximumSize(fixed);

        gameWrap.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                ((Component) client).requestFocusInWindow();
            }
        });

        center.add(gameWrap, BorderLayout.CENTER);

        // Sidebar
        sidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH, fixed.height));
        sidebar.setOpaque(true);
        sidebar.setBackground(BG);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER));
        sidebar.setLayout(new BorderLayout());
        center.add(sidebar, BorderLayout.EAST);

        // Icon strip
        iconStrip.setOpaque(true);
        iconStrip.setBackground(new Color(12, 13, 14));
        iconStrip.setBorder(new EmptyBorder(8, 8, 8, 8));
        iconStrip.setLayout(new BoxLayout(iconStrip, BoxLayout.Y_AXIS));
        iconStrip.setPreferredSize(new Dimension(ICON_STRIP_WIDTH, 10));
        sidebar.add(iconStrip, BorderLayout.WEST);

        ButtonGroup group = new ButtonGroup();

        characterBtn = new IconTabButton("Character Information", IconTabButton.IconType.CHARACTER);
        final IconTabButton marketBtn    = new IconTabButton("Market", IconTabButton.IconType.MARKET);
        final IconTabButton linksBtn     = new IconTabButton("Links", IconTabButton.IconType.LINKS);
        final IconTabButton newsBtn      = new IconTabButton("News", IconTabButton.IconType.NEWS);
        final IconTabButton patchBtn     = new IconTabButton("Patch Notes", IconTabButton.IconType.PATCH);
        final IconTabButton savedBtn     = new IconTabButton("Saved Accounts", IconTabButton.IconType.SAVED);
        settingsBtn  = new IconTabButton("Settings", IconTabButton.IconType.SETTINGS);
        final IconTabButton supportBtn   = new IconTabButton("Contact Support", IconTabButton.IconType.SUPPORT);

        group.add(characterBtn);
        group.add(marketBtn);
        group.add(linksBtn);
        group.add(newsBtn);
        group.add(patchBtn);
        group.add(savedBtn);
        group.add(settingsBtn);
        group.add(supportBtn);

        iconStrip.add(characterBtn);
        iconStrip.add(Box.createVerticalStrut(8));
        iconStrip.add(marketBtn);
        iconStrip.add(Box.createVerticalStrut(8));
        iconStrip.add(linksBtn);
        iconStrip.add(Box.createVerticalStrut(8));
        iconStrip.add(newsBtn);
        iconStrip.add(Box.createVerticalStrut(8));
        iconStrip.add(patchBtn);
        iconStrip.add(Box.createVerticalStrut(8));
        iconStrip.add(savedBtn);
        iconStrip.add(Box.createVerticalStrut(8));
        iconStrip.add(settingsBtn);
        iconStrip.add(Box.createVerticalStrut(8));
        iconStrip.add(supportBtn);
        iconStrip.add(Box.createVerticalGlue());

        // Content area
        JPanel contentWrap = new JPanel(new BorderLayout());
        contentWrap.setOpaque(true);
        contentWrap.setBackground(BG);
        contentWrap.setBorder(new EmptyBorder(0, 0, 0, 0));
        sidebar.add(contentWrap, BorderLayout.CENTER);

        // Panels
        JPanel settingsPanel  = new SettingsPanel(this);
        characterPanel = new CharacterInfoPanel();
        JPanel marketPanel    = new ComingSoonPanel("Market", "Coming soon...");
        JPanel linksPanel     = new LinksQuickPanel();
        JPanel newsPanel      = new NewsPanel();
        JPanel patchPanel     = new PatchNotesPanel();
        JPanel savedPanel     = new SavedAccountsPanel();
        JPanel supportPanel   = new SupportPanel();

        cards.setOpaque(true);
        cards.setBackground(BG);

        cards.add(settingsPanel,  "settings");
        cards.add(characterPanel, "character");
        cards.add(marketPanel,    "market");
        cards.add(linksPanel,     "links");
        cards.add(newsPanel,      "news");
        cards.add(patchPanel,     "patch");
        cards.add(savedPanel,     "saved");
        cards.add(supportPanel,   "support");

        // Scroll + width-tracking fix
        cardViewport = new CardViewport(cards);
        scroll = new JScrollPane(cardViewport);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        scroll.setViewportBorder(new EmptyBorder(0, 0, 0, 0));
        scroll.getViewport().setBackground(BG);

        JScrollBar vbar = scroll.getVerticalScrollBar();
        vbar.setUnitIncrement(16);
        vbar.setPreferredSize(new Dimension(10, 10));
        scroll.setBackground(BG);

        vbar.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = new Color(45, 48, 52);
                this.trackColor = new Color(18, 19, 21);
            }
            @Override protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int orientation) { return zeroButton(); }
            private JButton zeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setMinimumSize(new Dimension(0, 0));
                b.setMaximumSize(new Dimension(0, 0));
                return b;
            }
        });

        contentWrap.add(scroll, BorderLayout.CENTER);

        // Theme pass (global)
        applyThemeRecursive(cards);

        // Actions
        settingsBtn.addActionListener(e -> show("settings", settingsBtn));
        characterBtn.addActionListener(e -> show("character", characterBtn));
        marketBtn.addActionListener(e -> show("market", marketBtn));
        linksBtn.addActionListener(e -> show("links", linksBtn));
        newsBtn.addActionListener(e -> show("news", newsBtn));
        patchBtn.addActionListener(e -> show("patch", patchBtn));
        savedBtn.addActionListener(e -> show("saved", savedBtn));
        supportBtn.addActionListener(e -> show("support", supportBtn));

        // Default tab
        settingsBtn.setSelected(true);
        show("settings", settingsBtn);

        // Install resize handlers AFTER layout is built (otherwise bounds are wrong)
        installResizeHandler(root);
        installResizeHandler(root, titleBar);
        installResizeHandler(root, center);
        installResizeHandler(root, gameWrap);
        installResizeHandler(root, sidebar);
        installResizeHandler(root, iconStrip);
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (!(event instanceof MouseEvent)) return;
            MouseEvent me = (MouseEvent) event;
            if (me.getID() != MouseEvent.MOUSE_MOVED) return;

            if (!canResizeShell() || maximized) return;

            Point p = SwingUtilities.convertPoint(me.getComponent(), me.getPoint(), getRootPane());
            boolean edge = p.x <= RESIZE_MARGIN
                    || p.y <= RESIZE_MARGIN
                    || p.x >= getRootPane().getWidth() - RESIZE_MARGIN
                    || p.y >= getRootPane().getHeight() - RESIZE_MARGIN;

            if (!edge) {
                setCursor(Cursor.getDefaultCursor());
            }
        }, AWTEvent.MOUSE_MOTION_EVENT_MASK);

//        // Ensure edge-resize works even over scrollbars/cards (glass pane sits above everything)
//        JComponent glass = (JComponent) getGlassPane();
//        glass.setVisible(true);
//        glass.setOpaque(false);
//        installResizeHandler(root, glass);
        ResizeGlassPane glass = new ResizeGlassPane();
        setGlassPane(glass);
        glass.setVisible(true);
        installResizeHandler(root, glass);
        SwingUtilities.invokeLater(() -> {
            glass.setBounds(0, 0, getRootPane().getWidth(), getRootPane().getHeight());
            glass.revalidate();
            glass.repaint();
        });

        pack();

        Settings settings = Client.getUserSettings();
        if (settings != null) {
            updateResizableState(Client.currentScreenMode, settings.getInterfaceStyle());
        } else {
            updateResizableState(ScreenMode.FIXED, InterfaceStyle.OSRS);
        }
    }

    /** Called by TitleBar button. */
    public void toggleSidebar() {
        setSidebarHidden(!sidebarHidden);
    }

    public boolean isSidebarHidden() {
        return sidebarHidden;
    }

    static void styleComboBox(JComboBox<?> cb) {
        cb.setOpaque(true);
        cb.setBackground(PANEL);
        cb.setForeground(TEXT);
        cb.setBorder(BorderFactory.createLineBorder(BORDER));
        cb.setFocusable(false);

        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setOpaque(true);
                l.setBorder(new EmptyBorder(4, 8, 4, 8));
                l.setForeground(isSelected ? TEXT : TEXT_DIM);
                l.setBackground(isSelected ? new Color(30, 32, 35) : PANEL);
                list.setBackground(PANEL);
                list.setSelectionBackground(new Color(30, 32, 35));
                list.setSelectionForeground(TEXT);
                return l;
            }
        });

        // Style popup border if accessible
        Object child = cb.getUI().getAccessibleChild(cb, 0);
        if (child instanceof JPopupMenu) {
            JPopupMenu popup = (JPopupMenu) child;
            popup.setBorder(BorderFactory.createLineBorder(BORDER));
            popup.setBackground(PANEL);
            popup.setOpaque(true);
        }
    }

    private final class ResizeGlassPane extends JComponent {
        ResizeGlassPane() {
            setOpaque(false);
            setVisible(true);

            // keep it in sync with the root pane size
            setLayout(null);
        }

        @Override
        public boolean contains(int x, int y) {
            if (!shellResizable || maximized) return false;

            // Use the root pane size (reliable) rather than glass size (sometimes 0)
            JRootPane rp = getRootPane();
            if (rp == null) return false;

            int w = rp.getWidth();
            int h = rp.getHeight();
            if (w <= 0 || h <= 0) return false;

            boolean left = x <= RESIZE_MARGIN;
            boolean right = x >= w - RESIZE_MARGIN;
            boolean top = y <= RESIZE_MARGIN;
            boolean bottom = y >= h - RESIZE_MARGIN;

            return left || right || top || bottom;
        }

        @Override
        public void doLayout() {
            // Always cover the whole root pane
            JRootPane rp = getRootPane();
            if (rp != null) {
                setBounds(0, 0, rp.getWidth(), rp.getHeight());
            } else {
                super.doLayout();
            }
        }
    }


    private void setSidebarHidden(boolean hide) {
        if (this.sidebarHidden == hide) return;
        this.sidebarHidden = hide;

        Point loc = getLocation();
        sidebar.setVisible(!hide);

        pack();
        setLocation(loc);

        SwingUtilities.invokeLater(() -> ((Component) client).requestFocusInWindow());
    }

    private void show(String key, IconTabButton btn) {
        cardLayout.show(cards, key);
        setActive(btn);

        if ("character".equals(key)) {
            characterPanel.refresh();
        }

        cardViewport.sync();
        scroll.revalidate();
        scroll.repaint();

        SwingUtilities.invokeLater(() -> scroll.getViewport().setViewPosition(new Point(0, 0)));
        SwingUtilities.invokeLater(() -> ((Component) client).requestFocusInWindow());
    }

    private void setActive(IconTabButton active) {
        for (Component c : iconStrip.getComponents()) {
            if (c instanceof IconTabButton) {
                ((IconTabButton) c).setActive(c == active);
            }
        }
        iconStrip.repaint();
    }

    public void showSettingsTab() {
        show("settings", settingsBtn);
    }

    public void updateGameSize(Dimension size) {
        if (size == null) {
            return;
        }

        // In resizable mode, don't clamp maximum sizes
        if (shellMode == ScreenMode.RESIZABLE) {
            gameWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            ((Component) client).setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        }

        Dimension applied = new Dimension(size);
        gameWrap.setPreferredSize(applied);
        if (shellMode == ScreenMode.RESIZABLE) {
            gameWrap.setMinimumSize(ScreenMode.RESIZABLE.getDimensions());
        } else {
            gameWrap.setMinimumSize(applied);
        }
        gameWrap.setMaximumSize(applied);

        Component gameComponent = (Component) client;
        gameComponent.setPreferredSize(applied);
        gameComponent.setMinimumSize(applied);
        gameComponent.setMaximumSize(applied);
        gameComponent.setSize(applied);

        gameWrap.revalidate();
        gameWrap.repaint();

        // Don’t fight the user while resizing
        if (shellMode == ScreenMode.FIXED) {
            applyFixedSizing();
        } else {
            revalidate();
            repaint();
        }
    }

    public void updateResizableState(ScreenMode mode, InterfaceStyle style) {
        this.shellMode = mode;
        this.shellResizable = (mode == ScreenMode.RESIZABLE);
        setResizable(shellResizable); // undecorated → no OS handles, but keep consistent

        if (mode == ScreenMode.FIXED) {
            applyFixedSizing(); // true “hug fixed”
        } else {
            applyResizableSizing();
        }
        System.out.println("[Shell] updateResizableState -> " + mode + " shellResizable=" + (mode == ScreenMode.RESIZABLE));
    }

    private boolean canResizeShell() {
        return shellResizable && !maximized;
    }

    private void installResizeHandler(JComponent root) {
        installResizeHandler(root, root);
    }

    private void installResizeHandler(JComponent root, JComponent target) {
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!canResizeShell() || maximized) {
                    setCursor(Cursor.getDefaultCursor());
                    return;
                }
                Point rootPoint = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), root);
                resizeDirection = getResizeDirection(rootPoint, root);
                setCursor(cursorForDirection(resizeDirection));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (!canResizeShell()) {
                    return;
                }
                if (maximized) {
                    return;
                }
                Point rootPoint = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), root);
                resizeDirection = getResizeDirection(rootPoint, root);
                if (resizeDirection != ResizeDirection.NONE) {
                    resizingWindow = true;
                    resizeStart = e.getLocationOnScreen();
                    resizeStartSize = getSize();
                    resizeStartBounds = getBounds();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                resizingWindow = false;
                resizeDirection = ResizeDirection.NONE;
                updateRestoreBounds();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!resizingWindow) {
                    return;
                }
                Point current = e.getLocationOnScreen();
                int deltaX = current.x - resizeStart.x;
                int deltaY = current.y - resizeStart.y;
                int newWidth = resizeStartSize.width;
                int newHeight = resizeStartSize.height;
                int newX = resizeStartBounds.x;
                int newY = resizeStartBounds.y;

                if (resizeDirection.hasEast()) {
                    newWidth = resizeStartSize.width + deltaX;
                }
                if (resizeDirection.hasSouth()) {
                    newHeight = resizeStartSize.height + deltaY;
                }
                if (resizeDirection.hasWest()) {
                    newWidth = resizeStartSize.width - deltaX;
                    newX = resizeStartBounds.x + deltaX;
                }
                if (resizeDirection.hasNorth()) {
                    newHeight = resizeStartSize.height - deltaY;
                    newY = resizeStartBounds.y + deltaY;
                }
                Dimension min = getMinimumSize();
                newWidth = Math.max(min.width, newWidth);
                newHeight = Math.max(min.height, newHeight);
                setBounds(newX, newY, newWidth, newHeight);
                revalidate();
            }
        };
        target.addMouseListener(adapter);
        target.addMouseMotionListener(adapter);
    }

    void toggleMaximize() {
        if (!canResizeShell()) {
            return;
        }
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle screenBounds = env.getMaximumWindowBounds();
        if (!maximized) {
            restoreBounds = getBounds();
            setBounds(screenBounds);
            maximized = true;
        } else {
            if (restoreBounds != null) {
                setBounds(restoreBounds);
            }
            maximized = false;
        }
        titleBar.setMaximized(maximized);
        if (!maximized) {
            restoreBounds = getBounds();
        }
        revalidate();
    }

    boolean isMaximized() {
        return maximized;
    }

    void updateRestoreBounds() {
        if (!maximized) {
            restoreBounds = getBounds();
        }
    }

    private void applyFixedSizing() {
        Dimension fixed = ScreenMode.FIXED.getDimensions();
        sidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH, fixed.height));

        gameWrap.setPreferredSize(fixed);
        gameWrap.setMinimumSize(fixed);
        gameWrap.setMaximumSize(fixed);

        Component gameComponent = (Component) client;
        gameComponent.setPreferredSize(fixed);
        gameComponent.setMinimumSize(fixed);
        gameComponent.setMaximumSize(fixed);

        // Let the frame shrink to packed size, then lock minimum to it
        setMinimumSize(new Dimension(0, 0));
        maximized = false;
        titleBar.setMaximized(false);

        Point loc = getLocation();
        pack();
        setLocation(loc);

        setMinimumSize(getSize());
        updateRestoreBounds();
    }

    private void applyResizableSizing() {
        // remove fixed clamps
        gameWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        ((Component) client).setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        int minW = 765 + (sidebarHidden ? 0 : (SIDEBAR_WIDTH + ICON_STRIP_WIDTH));
        int minH = 503 + 80;
        setMinimumSize(new Dimension(minW, minH));

        revalidate();
        repaint();
        updateRestoreBounds();
    }

    private ResizeDirection getResizeDirection(Point point, JComponent root) {
        boolean left = point.x <= RESIZE_MARGIN;
        boolean right = point.x >= root.getWidth() - RESIZE_MARGIN;
        boolean top = point.y <= RESIZE_MARGIN;
        boolean bottom = point.y >= root.getHeight() - RESIZE_MARGIN;

        if (top && left) return ResizeDirection.NW;
        if (top && right) return ResizeDirection.NE;
        if (bottom && left) return ResizeDirection.SW;
        if (bottom && right) return ResizeDirection.SE;
        if (top) return ResizeDirection.N;
        if (bottom) return ResizeDirection.S;
        if (left) return ResizeDirection.W;
        if (right) return ResizeDirection.E;
        return ResizeDirection.NONE;
    }

    private Cursor cursorForDirection(ResizeDirection direction) {
        switch (direction) {
            case N:  return Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
            case S:  return Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
            case E:  return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
            case W:  return Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
            case NE: return Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
            case NW: return Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
            case SE: return Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
            case SW: return Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
            default: return Cursor.getDefaultCursor();
        }
    }

    private enum ResizeDirection {
        NONE(false, false, false, false),
        N(true, false, false, false),
        S(false, false, true, false),
        E(false, false, false, true),
        W(false, true, false, false),
        NE(true, false, false, true),
        NW(true, true, false, false),
        SE(false, false, true, true),
        SW(false, true, true, false);

        private final boolean north, west, south, east;

        ResizeDirection(boolean north, boolean west, boolean south, boolean east) {
            this.north = north;
            this.west = west;
            this.south = south;
            this.east = east;
        }

        boolean hasNorth() { return north; }
        boolean hasWest()  { return west; }
        boolean hasSouth() { return south; }
        boolean hasEast()  { return east; }
    }

    /** Dark-theme + compact pass for sidebar controls. */
    static void applyThemeRecursive(Component c) {
        if (c == null) return;

        if (c instanceof JComponent) {
            Font f = c.getFont();
            if (f != null && f.getSize2D() > FONT_BASE) {
                c.setFont(f.deriveFont(FONT_BASE));
            }
        }

        if (c instanceof JLabel) {
            JLabel l = (JLabel) c;
            l.setForeground(TEXT);

            Font f = l.getFont();
            if (f != null && f.isBold() && f.getSize2D() >= FONT_BASE) {
                l.setFont(f.deriveFont(Font.BOLD, FONT_HEADER));
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(32, 34, 36)),
                        BorderFactory.createEmptyBorder(0, 0, 6, 0)
                ));
            }
        } else if (c instanceof AbstractButton) {
            AbstractButton b = (AbstractButton) c;

            if (b instanceof IconTabButton) {
                // do nothing
            } else if (b instanceof JButton) {
                themeButton((JButton) b);
            } else if (b instanceof JToggleButton) {
                themeToggle((JToggleButton) b);
            }
        } else if (c instanceof JTextField) {
            JTextField tf = (JTextField) c;
            tf.setBackground(PANEL);
            tf.setForeground(TEXT);
            tf.setCaretColor(TEXT);
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER),
                    BorderFactory.createEmptyBorder(5, 7, 5, 7)
            ));
        } else if (c instanceof JTextArea) {
            JTextArea ta = (JTextArea) c;
            ta.setBackground(PANEL);
            ta.setForeground(TEXT);
            ta.setCaretColor(TEXT);
            ta.setBorder(BorderFactory.createEmptyBorder(5, 7, 5, 7));
        } else if (c instanceof JScrollPane) {
            JScrollPane sp = (JScrollPane) c;
            sp.setBorder(BorderFactory.createLineBorder(BORDER));
            sp.getViewport().setBackground(BG);
            sp.setBackground(BG);
        } else if (c instanceof JPanel) {
            JPanel p = (JPanel) c;
            if (p.getBackground() == null) p.setBackground(BG);
        } else if (c instanceof JProgressBar) {
            JProgressBar pb = (JProgressBar) c;
            pb.setBackground(PANEL);
            pb.setForeground(ACCENT);
            pb.setBorder(BorderFactory.createLineBorder(BORDER));
        }

        if (c instanceof Container) {
            Container ct = (Container) c;
            for (Component child : ct.getComponents()) {
                applyThemeRecursive(child);
            }
        }
    }

    private static void themeButton(JButton b) {
        b.setUI(new BasicButtonUI());
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);

        b.setForeground(TEXT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(b.getFont().deriveFont(Font.BOLD, FONT_BASE));
        b.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.putClientProperty("hover", Boolean.TRUE); b.repaint(); }
            @Override public void mouseExited(MouseEvent e) { b.putClientProperty("hover", Boolean.FALSE); b.repaint(); }
        });

        b.setUI(new BasicButtonUI() {
            @Override public void paint(Graphics g, JComponent c) {
                JButton btn = (JButton) c;
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = btn.getWidth();
                    int h = btn.getHeight();

                    boolean hover = Boolean.TRUE.equals(btn.getClientProperty("hover"));
                    boolean pressed = btn.getModel().isArmed() || btn.getModel().isPressed();

                    Color bg = pressed ? new Color(22, 23, 25) : (hover ? new Color(30, 32, 35) : PANEL_2);

                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, w, h, 9, 9);

                    g2.setColor(BORDER);
                    g2.drawRoundRect(0, 0, w - 1, h - 1, 9, 9);

                    super.paint(g2, c);
                } finally {
                    g2.dispose();
                }
            }
        });
    }

    private static void themeToggle(JToggleButton b) {
        b.setUI(new BasicButtonUI());
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);

        b.setForeground(TEXT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(b.getFont().deriveFont(Font.BOLD, FONT_BASE));
        b.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
    }

    static void openBrowser(String url) {
        try { Desktop.getDesktop().browse(new URI(url)); } catch (Exception ignored) {}
    }

    static void sendCommand(String command) {
        Client c = Client.getInstance();
        if (c != null) c.sendClientCommand(command);
    }

    /**
     * Icon-only tab button, RuneLite-style.
     */
    static final class IconTabButton extends JToggleButton {

        enum IconType {
            CHARACTER("/vykna/icons/character_information.png"),
            PATCH("/vykna/icons/patch_notes.png"),
            NEWS("/vykna/icons/news.png"),
            SETTINGS("/vykna/icons/settings.png"),
            MARKET("/vykna/icons/market.png"),
            LINKS("/vykna/icons/links.png"),
            SAVED("/vykna/icons/saved_accounts.png"),
            SUPPORT("/vykna/icons/contact_support.png");

            final String path;
            IconType(String path) { this.path = path; }
        }

        private final ImageIcon iconImg;

        private boolean hover = false;
        private boolean active = false;

        IconTabButton(String tooltip, IconType icon) {
            super();
            this.iconImg = IconResources.load(icon.path, 25);

            setToolTipText(tooltip);

            setUI(new BasicButtonUI());
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);

            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            setPreferredSize(new Dimension(36, 36));
            setMaximumSize(new Dimension(36, 36));
            setMinimumSize(new Dimension(36, 36));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        void setActive(boolean a) {
            active = a;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                Color bg = active
                        ? new Color(22, 23, 25)
                        : (hover ? new Color(26, 28, 30) : new Color(18, 19, 21));

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, w, h, 10, 10);

                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);

                if (active) {
                    g2.setColor(ACCENT);
                    g2.fillRoundRect(2, 6, 4, h - 12, 10, 10);
                }

                if (iconImg != null) {
                    int iw = iconImg.getIconWidth();
                    int ih = iconImg.getIconHeight();
                    int ix = (w - iw) / 2;
                    int iy = (h - ih) / 2;

                    if (!active && !hover) {
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
                    } else {
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                    }

                    iconImg.paintIcon(this, g2, ix, iy);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                } else {
                    g2.setColor(new Color(200, 60, 60));
                    g2.fillOval(w / 2 - 2, h / 2 - 2, 4, 4);
                }

            } finally {
                g2.dispose();
            }
        }
    }

    /**
     * ✅ Key fix: a viewport that reports preferred size of the currently visible card.
     */
    private static final class CardViewport extends JPanel implements Scrollable {
        private final JPanel cardPanel;

        CardViewport(JPanel cardPanel) {
            super(new BorderLayout());
            this.cardPanel = cardPanel;
            setOpaque(true);
            setBackground(BG);
            add(cardPanel, BorderLayout.CENTER);
        }

        void sync() {
            revalidate();
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            int vw = -1;
            Container p = getParent();
            if (p instanceof JViewport) {
                vw = ((JViewport) p).getWidth();
            }

            for (Component c : cardPanel.getComponents()) {
                if (c.isVisible()) {
                    Dimension d = c.getPreferredSize();
                    int width = (vw > 0) ? vw : d.width;
                    return new Dimension(width, d.height + 10);
                }
            }
            return super.getPreferredSize();
        }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) { return 64; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    static final class SectionTitle extends JLabel {
        SectionTitle(String text) {
            super(text);
            setForeground(TEXT);
            setFont(getFont().deriveFont(Font.BOLD, 13f));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(32, 34, 36)),
                    BorderFactory.createEmptyBorder(0, 0, 8, 0)
            ));
        }
    }

    static JPanel cardRoot() {
        JPanel p = new JPanel();
        p.setOpaque(true);
        p.setBackground(BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(8, 10, 10, 10));
        return p;
    }

    // --- Other panels unchanged (Character/Links/etc) ---

    private static final class CharacterInfoPanel extends JPanel {
        private final JPanel listPanel;

        CharacterInfoPanel() {
            super(new BorderLayout());
            setOpaque(true);
            setBackground(BG);

            JPanel body = cardRoot();
            body.add(new SectionTitle("Character Information"));

            listPanel = new JPanel();
            listPanel.setOpaque(true);
            listPanel.setBackground(BG);
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            body.add(listPanel);

            add(body, BorderLayout.CENTER);
            refresh();
        }

        void refresh() {
            listPanel.removeAll();
            List<String> infoLines = QuestTab.getInfoLines();
            if (infoLines.isEmpty()) {
                listPanel.add(textLine("No character data loaded yet."));
            } else {
                boolean first = true;
                for (String line : infoLines) {
                    if (line == null || line.trim().isEmpty()) {
                        continue;
                    }
                    LineData data = parseLine(line);
                    if (!first) {
                        listPanel.add(divider());
                    }
                    listPanel.add(lineRow(data));
                    first = false;
                }
            }
            listPanel.revalidate();
            listPanel.repaint();
        }

        private JPanel lineRow(LineData data) {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(true);
            row.setBackground(BG);
            row.setBorder(new EmptyBorder(4, 0, 4, 0));

            if (data.icon != null) {
                JLabel iconLabel = new JLabel(data.icon);
                iconLabel.setBorder(new EmptyBorder(0, 0, 0, 6));
                row.add(iconLabel, BorderLayout.WEST);
            }

            JLabel label = new JLabel(data.text);
            label.setForeground(data.color != null ? data.color : TEXT_DIM);
            if (data.header) {
                label.setFont(label.getFont().deriveFont(Font.BOLD, 12.5f));
                label.setForeground(TEXT);
            }
            row.add(label, BorderLayout.CENTER);
            return row;
        }

        private JPanel textLine(String text) {
            return lineRow(new LineData(text, TEXT_DIM, null, false));
        }

        private LineData parseLine(String line) {
            String normalized = RSFont.handleOldSyntax(line);
            ImageIcon icon = extractIcon(normalized);
            Color color = extractColor(normalized);
            String text = normalized
                    .replaceAll("<img=\\d+>", "")
                    .replaceAll("<col=[^>]+>", "")
                    .replaceAll("</col>", "")
                    .trim();
            boolean header = !text.contains(":") && (text.contains("Information") || icon != null);
            return new LineData(text, color, icon, header);
        }

        private ImageIcon extractIcon(String line) {
            int start = line.indexOf("<img=");
            if (start == -1) {
                return null;
            }
            int end = line.indexOf(">", start);
            if (end == -1) {
                return null;
            }
            String value = line.substring(start + 5, end).trim();
            try {
                int iconId = Integer.parseInt(value);
                return loadChatIcon(iconId);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        private Color extractColor(String line) {
            int start = line.indexOf("<col=");
            if (start == -1) {
                return null;
            }
            int end = line.indexOf(">", start);
            if (end == -1) {
                return null;
            }
            String value = line.substring(start + 5, end).trim();
            try {
                int rgb = Integer.parseInt(value, 16);
                return new Color(rgb);
            } catch (NumberFormatException ex) {
                try {
                    int rgb = Integer.parseInt(value);
                    return new Color(rgb);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }

        private JComponent divider() {
            JPanel p = new JPanel();
            p.setOpaque(true);
            p.setBackground(BG);
            p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(32, 34, 36)));
            p.setPreferredSize(new Dimension(10, 6));
            p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
            return p;
        }
    }

    private static final class ComingSoonPanel extends JPanel {
        ComingSoonPanel(String title, String msg) {
            super(new BorderLayout());
            setOpaque(true);
            setBackground(BG);

            JPanel body = cardRoot();
            body.add(new SectionTitle(title));

            JLabel label = new JLabel(msg);
            label.setForeground(TEXT_DIM);
            body.add(label);

            add(body, BorderLayout.CENTER);
        }
    }

    private static final class LinksQuickPanel extends JPanel {
        LinksQuickPanel() {
            super(new BorderLayout());
            setOpaque(true);
            setBackground(BG);

            JPanel body = cardRoot();
            body.add(new SectionTitle("Links"));

            body.add(linkBtn("Website", "https://your-site-here"));
            body.add(linkBtn("Discord", "https://discord.gg/yourcode"));
            body.add(linkBtn("Vote", "https://your-vote-link"));
            body.add(linkBtn("Donate", "https://your-donate-link"));
            body.add(linkBtn("YouTube", "https://youtube.com/@yourchannel"));

            add(body, BorderLayout.CENTER);
        }

        private JButton linkBtn(String text, String url) {
            JButton b = new JButton(text);
            b.addActionListener(e -> openBrowser(url));
            return b;
        }
    }

    private static final class NewsPanel extends JPanel {
        NewsPanel() {
            super(new BorderLayout());
            setOpaque(true);
            setBackground(BG);

            JPanel body = cardRoot();
            body.add(new SectionTitle("News"));

            JPanel hero = new JPanel();
            hero.setOpaque(true);
            hero.setBackground(new Color(18, 19, 21));
            hero.setPreferredSize(new Dimension(10, 140));
            hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
            hero.setBorder(BorderFactory.createLineBorder(BORDER));

            JLabel heroText = new JLabel("NEWS IMAGE (PNG) HERE");
            heroText.setForeground(TEXT_DIM);
            hero.add(heroText);

            body.add(hero);
            body.add(Box.createVerticalStrut(10));

            JLabel title = new JLabel("NEW DATE HERE");
            title.setForeground(TEXT);
            title.setFont(title.getFont().deriveFont(Font.BOLD, 12.5f));
            body.add(title);

            body.add(Box.createVerticalStrut(6));

            JTextArea desc = new JTextArea("Lorem ipsum...");
            desc.setLineWrap(true);
            desc.setWrapStyleWord(true);
            desc.setEditable(false);
            desc.setOpaque(false);
            desc.setForeground(TEXT_DIM);
            body.add(desc);

            body.add(Box.createVerticalStrut(12));

            JButton view = new JButton("View full update on Discord");
            view.addActionListener(e -> openBrowser("https://discord.gg/yourcode"));
            body.add(view);

            add(body, BorderLayout.CENTER);
        }
    }

    private static final class PatchNotesPanel extends JPanel {
        PatchNotesPanel() {
            super(new BorderLayout());
            setOpaque(true);
            setBackground(BG);

            JPanel body = cardRoot();
            body.add(new SectionTitle("Patch Notes"));

            body.add(dropdown("2026-01-08", "• Example change 1\n• Example change 2\n• Example fix 3"));
            body.add(Box.createVerticalStrut(8));
            body.add(dropdown("2026-01-01", "• Happy new year patch\n• Balance tweaks\n• Bug fixes"));

            add(body, BorderLayout.CENTER);
        }

        private JComponent dropdown(String date, String text) {
            JPanel wrap = new JPanel(new BorderLayout());
            wrap.setOpaque(true);
            wrap.setBackground(BG);
            wrap.setBorder(BorderFactory.createLineBorder(new Color(32, 34, 36)));

            JButton header = new JButton(date);
            header.setHorizontalAlignment(SwingConstants.LEFT);

            JTextArea area = new JTextArea(text);
            area.setEditable(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);

            JPanel inner = new JPanel(new BorderLayout());
            inner.setOpaque(true);
            inner.setBackground(BG);
            inner.setBorder(new EmptyBorder(8, 8, 8, 8));
            inner.add(area, BorderLayout.CENTER);
            inner.setVisible(false);

            header.addActionListener(e -> inner.setVisible(!inner.isVisible()));

            wrap.add(header, BorderLayout.NORTH);
            wrap.add(inner, BorderLayout.CENTER);
            return wrap;
        }
    }

    private static final class SavedAccountsPanel extends JPanel {
        SavedAccountsPanel() {
            super(new BorderLayout());
            setOpaque(true);
            setBackground(BG);

            JPanel body = cardRoot();
            body.add(new SectionTitle("Saved Accounts"));

            JLabel info = new JLabel("Click an account to auto-fill/login (passwords hidden).");
            info.setForeground(TEXT_DIM);
            body.add(info);
            body.add(Box.createVerticalStrut(8));

            DefaultListModel<String> model = new DefaultListModel<>();
            model.addElement("Main (hidden)");
            model.addElement("Iron (hidden)");

            JList<String> list = new JList<>(model);
            list.setVisibleRowCount(8);
            list.setBorder(new EmptyBorder(6, 6, 6, 6));

            list.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    String sel = list.getSelectedValue();
                    if (sel != null) {
                        System.out.println("[SavedAccounts] Selected: " + sel);
                    }
                }
            });

            JScrollPane sp = new JScrollPane(list);
            body.add(sp);

            add(body, BorderLayout.CENTER);
        }
    }

    private static final class SupportPanel extends JPanel {
        SupportPanel() {
            super(new BorderLayout());
            setOpaque(true);
            setBackground(BG);

            JPanel body = cardRoot();
            body.add(new SectionTitle("Contact Support"));

            JLabel info = new JLabel("Need help? Join our Discord.");
            info.setForeground(TEXT_DIM);
            body.add(info);
            body.add(Box.createVerticalStrut(10));

            JButton b = new JButton("Open Discord");
            b.addActionListener(e -> openBrowser("https://discord.gg/yourcode"));
            body.add(b);

            add(body, BorderLayout.CENTER);
        }
    }

    private static ImageIcon loadChatIcon(int iconId) {
        if (iconId < 0) return null;
        if (RSFont.chatImages == null || iconId >= RSFont.chatImages.length) return null;

        Sprite sprite = RSFont.chatImages[iconId];
        if (sprite == null || sprite.myPixels == null) return null;

        return chatIconCache.computeIfAbsent(iconId, key -> {
            Image image = Toolkit.getDefaultToolkit().createImage(
                    new MemoryImageSource(sprite.myWidth, sprite.myHeight, ColorModel.getRGBdefault(), sprite.myPixels, 0, sprite.myWidth)
            );
            return new ImageIcon(image);
        });
    }

    private static final class LineData {
        private final String text;
        private final Color color;
        private final ImageIcon icon;
        private final boolean header;

        private LineData(String text, Color color, ImageIcon icon, boolean header) {
            this.text = text;
            this.color = color;
            this.icon = icon;
            this.header = header;
        }
    }
}
