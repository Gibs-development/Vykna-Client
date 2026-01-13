package com.client.ui.shell;

import com.client.Client;
import com.client.RSFont;
import com.client.Rasterizer;
import com.client.Sprite;
import com.client.features.gameframe.ScreenMode;
import com.client.features.settings.Preferences;
import com.client.graphics.interfaces.settings.Setting;
import com.client.graphics.interfaces.settings.SettingsInterface;
import com.client.graphics.interfaces.dropdown.StretchedModeMenu;
import com.client.graphics.interfaces.impl.QuestTab;
import com.client.utilities.settings.InterfaceStyle;
import com.client.utilities.settings.Settings;
import com.client.utilities.settings.SettingsManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicSliderUI;
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


    private boolean sidebarHidden = false;

    // Icon tabs
    private IconTabButton homeBtn;
    private IconTabButton utilBtn;
    private IconTabButton marketBtn;
    private IconTabButton linksBtn;
    private final IconTabButton settingsBtn;
    private final IconTabButton characterBtn;
    private final CharacterInfoPanel characterPanel;
    private static final Map<Integer, ImageIcon> chatIconCache = new HashMap<>();

    // Need these to fix the “cut off” issue

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

        TitleBar titleBar = new TitleBar(title, this);
        root.add(titleBar, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(true);
        center.setBackground(BG);
        root.add(center, BorderLayout.CENTER);

        // Game
        gameWrap.setOpaque(true);
        gameWrap.setBackground(Color.BLACK);
        gameWrap.add((Component) client, BorderLayout.CENTER);

        Dimension fixed = ScreenMode.FIXED.getDimensions();
        gameWrap.setPreferredSize(fixed);
        gameWrap.setMinimumSize(fixed);

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

        // Tabs in your requested order
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

        // Panels (placeholders for now - replace with real ones later)
        JPanel settingsPanel  = new SettingsPanel();
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

        // Keep content away from scrollbar
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

        // Theme pass
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

        pack();
    }


    /** Called by TitleBar button. */
    public void toggleSidebar() {
        setSidebarHidden(!sidebarHidden);
    }

    public boolean isSidebarHidden() {
        return sidebarHidden;
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

        // ✅ ensure scroll + viewport recompute sizes for active card
        cardViewport.sync();
        scroll.revalidate();
        scroll.repaint();

        // optional: always scroll to top when switching tabs
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
        Dimension applied = new Dimension(size);
        gameWrap.setPreferredSize(applied);
        gameWrap.setMinimumSize(applied);
        gameWrap.revalidate();
        gameWrap.repaint();
        pack();
    }


    /** Dark-theme + compact pass for sidebar controls. */
    private static void applyThemeRecursive(Component c) {
        if (c == null) return;

        // font shrink (skip if already tiny)
        if (c instanceof JComponent) {
            Font f = c.getFont();
            if (f != null && f.getSize2D() > FONT_BASE) {
                c.setFont(f.deriveFont(FONT_BASE));
            }
        }

        if (c instanceof JLabel) {
            JLabel l = (JLabel) c;
            l.setForeground(TEXT);

            // If it's already bold / large, make it a compact header
            Font f = l.getFont();
            if (f != null && f.isBold() && f.getSize2D() >= FONT_BASE) {
                l.setFont(f.deriveFont(Font.BOLD, FONT_HEADER));
                // subtle divider under headers
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(32, 34, 36)),
                        BorderFactory.createEmptyBorder(0, 0, 6, 0)
                ));
            }
        } else if (c instanceof AbstractButton) {
            // Theme JButtons and JToggleButtons, but skip our custom icon tabs and titlebar buttons
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
        } else if (c instanceof JSlider) {
            JSlider slider = (JSlider) c;
            slider.setBackground(BG);
            slider.setForeground(TEXT);
            slider.setUI(new ShellSliderUI(slider));
        } else if (c instanceof JComboBox) {
            @SuppressWarnings("rawtypes")
            JComboBox cb = (JComboBox) c;
            cb.setBackground(PANEL);
            cb.setForeground(TEXT);
            cb.setBorder(BorderFactory.createLineBorder(BORDER));
            cb.setRenderer(new ShellComboBoxRenderer());
            cb.setFocusable(false);
        } else if (c instanceof JList) {
            @SuppressWarnings("rawtypes")
            JList list = (JList) c;
            list.setBackground(PANEL);
            list.setForeground(TEXT);
            list.setSelectionBackground(new Color(32, 34, 36));
            list.setSelectionForeground(TEXT);
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
        // keep it compact and consistent
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
    private static final class IconTabButton extends JToggleButton {

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

        private final IconType icon;
        private final ImageIcon iconImg;

        private boolean hover = false;
        private boolean active = false;

        IconTabButton(String tooltip, IconType icon) {
            super();
            this.icon = icon;
            this.iconImg = IconResources.load(icon.path, 25);

            setToolTipText(tooltip);

            setUI(new BasicButtonUI());
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);

            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // RuneLite-ish sizing
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

                // Draw PNG icon
                if (iconImg != null) {
                    int iw = iconImg.getIconWidth();
                    int ih = iconImg.getIconHeight();
                    int ix = (w - iw) / 2;
                    int iy = (h - ih) / 2;

                    // Slight “dim” effect when inactive
// Slight “dim” effect when inactive
                    if (!active && !hover) {
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
                    } else {
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                    }

                    iconImg.paintIcon(this, g2, ix, iy);

// reset
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

                } else {
                    // Fallback: draw a tiny dot so you can tell it's missing
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
     * This stops “Utility panel gets cut off”.
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

            // Find currently visible card
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
        @Override public boolean getScrollableTracksViewportWidth() { return true; }   // ✅ key line
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }


    /** Subtle gradient background for sidebar. */
    private static final class SidebarPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                int w = getWidth();
                int h = getHeight();

                GradientPaint gp = new GradientPaint(0, 0, new Color(16, 17, 19), 0, h, new Color(12, 13, 14));
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);

                g2.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 16));
                g2.fillOval(w - 220, -140, 320, 320);

                super.paintComponent(g);
            } finally {
                g2.dispose();
            }
        }
    }
    private static final class SectionTitle extends JLabel {
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

    private static JPanel cardRoot() {
        JPanel p = new JPanel();
        p.setOpaque(true);
        p.setBackground(BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(8, 10, 10, 10));
        return p;
    }

    private static final class SettingsPanel extends JPanel {
        private final JPanel body;

        SettingsPanel() {
            super(new BorderLayout());
            setOpaque(true);
            setBackground(BG);

            Settings settings = Client.getUserSettings();
            if (settings == null) {
                settings = Settings.getDefault();
                Client.setUserSettings(settings);
            }

            body = cardRoot();
            body.add(new SectionTitle("Settings"));

            body.add(sectionHeader("Graphics"));
            addSettingToggle(SettingsInterface.ANTI_ALIASING, isTrue(settings.isAntiAliasing()));
            addSettingToggle(SettingsInterface.FOG, isTrue(settings.isFog()));
            addSettingToggle(SettingsInterface.SMOOTH_SHADING, isTrue(settings.isSmoothShading()));
            addSettingToggle(SettingsInterface.TILE_BLENDING, isTrue(settings.isTileBlending()));
            addSettingToggle(SettingsInterface.STATUS_BARS, isTrue(isStatusBarsEnabled()));
            addSettingDropdown(SettingsInterface.DRAW_DISTANCE, drawDistanceIndex(settings.getDrawDistance()));
            addSettingDropdown(SettingsInterface.STRETCHED_MODE, booleanToIndex(settings.isStretchedMode()));
            body.add(sliderRow("Brightness", Preferences.getPreferences().brightness * 100.0, 60, 100, value -> {
                Preferences.getPreferences().brightness = value / 100.0;
                Rasterizer.setBrightness(Preferences.getPreferences().brightness);
                Preferences.save();
            }));

            body.add(sectionHeader("Interface"));
            addSettingDropdown(SettingsInterface.INTERFACE_STYLE, interfaceStyleIndex(settings.getInterfaceStyle()));
            addSettingToggle(SettingsInterface.OLD_GAMEFRAME, isTrue(settings.isOldGameframe()));
            addSettingDropdown(SettingsInterface.INVENTORY_MENU, inventoryMenuIndex());
            addSettingDropdown(SettingsInterface.CHAT_EFFECT, settings.getChatColor());
            addSettingToggle(SettingsInterface.GROUND_ITEM_NAMES, isTrue(settings.isGroundItemOverlay()));
            addSettingToggle(SettingsInterface.MENU_HOVERS, isTrue(isMenuHoversEnabled()));
            addSettingToggle(SettingsInterface.PLAYER_PROFILE, false);
            addSettingToggle(SettingsInterface.GAME_TIMERS, isTrue(settings.isGameTimers()));
            addSettingDropdown(SettingsInterface.PM_NOTIFICATION, booleanToIndex(Preferences.getPreferences().pmNotifications));
            addRs3EditModeControls(settings);

            body.add(sectionHeader("Gameplay"));
            addSettingToggle(SettingsInterface.BOUNTY_HUNTER, isTrue(settings.isBountyHunter()));
            addSettingToggle(SettingsInterface.ENTITY_TARGET, isTrue(settings.isShowEntityTarget()));
            addSettingDropdown(SettingsInterface.DRAG, dragTimeIndex());

            body.add(sectionHeader("Misc"));
            addSettingToggle(SettingsInterface.ROOF, isTrue(!isRemoveRoofsEnabled()));
            addSettingToggle(SettingsInterface.PVP_TAB, false);

            add(body, BorderLayout.CENTER);
        }

        // ---------------- UI pieces ----------------

        private JPanel row(String label, JComponent control) {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(true);
            row.setBackground(BG);
            row.setBorder(new EmptyBorder(5, 2, 5, 2));

            JLabel l = new JLabel(label);
            l.setForeground(TEXT);
            l.setFont(l.getFont().deriveFont(12f));

            row.add(l, BorderLayout.WEST);
            row.add(control, BorderLayout.EAST);
            return row;
        }

        private JPanel sectionHeader(String title) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(true);
            p.setBackground(BG);
            p.setBorder(new EmptyBorder(8, 0, 2, 0));
            JLabel label = new JLabel(title);
            label.setForeground(TEXT_DIM);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
            p.add(label, BorderLayout.WEST);
            return p;
        }

        private void addSettingToggle(Setting setting, boolean initial) {
            JToggleButton toggle = pillToggle(initial);
            toggle.addActionListener(e -> {
                int option = toggle.isSelected() ? 0 : 1;
                setting.getMenuItem().select(option, null);
                persistSettings();
                syncToggleVisual(toggle);
            });
            body.add(row(setting.getSettingName(), toggle));
        }

        private void addSettingDropdown(Setting setting, int selectedIndex) {
            JComboBox<String> combo = new JComboBox<>(setting.getOptions());
            combo.setSelectedIndex(Math.max(0, Math.min(selectedIndex, setting.getOptions().length - 1)));
            combo.addActionListener(e -> {
                int index = combo.getSelectedIndex();
                setting.getMenuItem().select(index, null);
                if (setting == SettingsInterface.STRETCHED_MODE) {
                    StretchedModeMenu.updateStretchedMode(index == 0);
                }
                if (setting == SettingsInterface.INTERFACE_STYLE) {
                    refreshRs3Controls();
                }
                persistSettings();
            });
            body.add(row(setting.getSettingName(), combo));
        }

        private JPanel sliderRow(String label, double initialValue, int min, int max, SliderValueConsumer onChange) {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(true);
            row.setBackground(BG);
            row.setBorder(new EmptyBorder(8, 2, 8, 2));

            JLabel l = new JLabel(label);
            l.setForeground(TEXT);
            l.setFont(l.getFont().deriveFont(12f));
            row.add(l, BorderLayout.WEST);

            int initial = (int) Math.round(initialValue);
            JSlider slider = new JSlider(min, max, initial);
            slider.setOpaque(false);
            slider.setPreferredSize(new Dimension(150, 20));
            slider.addChangeListener(e -> {
                if (!slider.getValueIsAdjusting()) {
                    onChange.accept(slider.getValue());
                }
            });
            row.add(slider, BorderLayout.EAST);
            return row;
        }

        private JToggleButton pillToggle(boolean def) {
            JToggleButton t = new JToggleButton(def ? "ON" : "OFF", def);
            t.setFocusable(false);
            t.setOpaque(true);
            t.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(34, 36, 40)),
                    new EmptyBorder(6, 12, 6, 12)
            ));
            syncToggleVisual(t);
            return t;
        }

        private void syncToggleVisual(JToggleButton t) {
            boolean on = t.isSelected();
            t.setText(on ? "ON" : "OFF");
            t.setForeground(on ? TEXT : TEXT_DIM);
            t.setBackground(on ? new Color(24, 26, 28) : new Color(16, 17, 19));
        }

        private int drawDistanceIndex(int drawDistance) {
            if (drawDistance == 30) return 0;
            if (drawDistance == 40) return 1;
            if (drawDistance == 50) return 2;
            if (drawDistance == 60) return 3;
            return 4;
        }

        private int interfaceStyleIndex(InterfaceStyle style) {
            return style == InterfaceStyle.RS3 ? 1 : 0;
        }

        private JToggleButton rs3EditModeToggle;
        private JButton rs3ResetLayoutButton;

        private void addRs3EditModeControls(Settings settings) {
            rs3EditModeToggle = pillToggle(settings.isRs3EditMode());
            rs3EditModeToggle.addActionListener(e -> {
                if (settings.getInterfaceStyle() != InterfaceStyle.RS3) {
                    rs3EditModeToggle.setSelected(false);
                    syncToggleVisual(rs3EditModeToggle);
                    return;
                }
                Client.getInstance().setRs3EditMode(rs3EditModeToggle.isSelected());
                persistSettings();
                syncToggleVisual(rs3EditModeToggle);
            });

            rs3ResetLayoutButton = new JButton("Reset");
            rs3ResetLayoutButton.addActionListener(e -> {
                if (settings.getInterfaceStyle() != InterfaceStyle.RS3) {
                    return;
                }
                Client.getInstance().resetRs3PanelLayout();
                persistSettings();
            });

            body.add(row("RS3 Edit Mode", rs3EditModeToggle));
            body.add(row("Reset RS3 Layout", rs3ResetLayoutButton));
            refreshRs3Controls();
        }

        private void refreshRs3Controls() {
            Settings settings = Client.getUserSettings();
            if (settings == null) {
                return;
            }
            boolean rs3 = settings.getInterfaceStyle() == InterfaceStyle.RS3;
            if (rs3EditModeToggle != null) {
                rs3EditModeToggle.setEnabled(rs3);
                rs3EditModeToggle.setSelected(settings.isRs3EditMode());
                syncToggleVisual(rs3EditModeToggle);
                if (!rs3) {
                    Client instance = Client.getInstance();
                    if (instance != null) {
                        instance.setRs3EditMode(false);
                    }
                    rs3EditModeToggle.setSelected(false);
                    syncToggleVisual(rs3EditModeToggle);
                }
            }
            if (rs3ResetLayoutButton != null) {
                rs3ResetLayoutButton.setEnabled(rs3);
            }
        }

        private int inventoryMenuIndex() {
            if (!Client.getUserSettings().isInventoryContextMenu()) {
                return 0;
            }
            int color = Client.getUserSettings().getStartMenuColor();
            if (color == 0xFF00FF) return 1;
            if (color == 0x00FF00) return 2;
            if (color == 0x00FFFF) return 3;
            if (color == 0xFF0000) return 4;
            return 1;
        }

        private int dragTimeIndex() {
            int drag = Preferences.getPreferences().dragTime;
            if (drag == 5) return 0;
            if (drag == 6) return 1;
            if (drag == 8) return 2;
            if (drag == 10) return 3;
            return 4;
        }

        private boolean isStatusBarsEnabled() {
            return com.client.Configuration.statusBars;
        }

        private boolean isRemoveRoofsEnabled() {
            return Client.removeRoofs;
        }

        private boolean isMenuHoversEnabled() {
            return com.client.Configuration.menuHovers;
        }

        private boolean isTrue(boolean value) {
            return value;
        }

        private int booleanToIndex(boolean value) {
            return value ? 0 : 1;
        }

        private void persistSettings() {
            try {
                SettingsManager.saveSettings(Client.getInstance());
            } catch (Exception ignored) {
            }
            Preferences.save();
        }
    }


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

            // Big PNG space placeholder
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
                        // Placeholder: later we’ll hook this to fill login fields or send a client command.
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
        if (iconId < 0) {
            return null;
        }
        if (RSFont.chatImages == null || iconId >= RSFont.chatImages.length) {
            return null;
        }
        Sprite sprite = RSFont.chatImages[iconId];
        if (sprite == null || sprite.myPixels == null) {
            return null;
        }
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

    private static final class ShellComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            list.setBackground(PANEL);
            list.setSelectionBackground(new Color(30, 32, 35));
            list.setSelectionForeground(TEXT);
            label.setBackground(isSelected ? new Color(30, 32, 35) : PANEL);
            label.setForeground(TEXT);
            label.setBorder(new EmptyBorder(2, 6, 2, 6));
            return label;
        }
    }

    private static final class ShellSliderUI extends BasicSliderUI {
        private static final int TRACK_HEIGHT = 6;
        private static final int THUMB_SIZE = 12;

        ShellSliderUI(JSlider b) {
            super(b);
        }

        @Override
        protected Dimension getThumbSize() {
            return new Dimension(THUMB_SIZE, THUMB_SIZE);
        }

        @Override
        public void paintTrack(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cy = trackRect.y + (trackRect.height - TRACK_HEIGHT) / 2;
                g2.setColor(new Color(32, 34, 36));
                g2.fillRoundRect(trackRect.x, cy, trackRect.width, TRACK_HEIGHT, TRACK_HEIGHT, TRACK_HEIGHT);
            } finally {
                g2.dispose();
            }
        }

        @Override
        public void paintThumb(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT);
                g2.fillOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
                g2.setColor(BORDER);
                g2.drawOval(thumbRect.x, thumbRect.y, thumbRect.width - 1, thumbRect.height - 1);
            } finally {
                g2.dispose();
            }
        }
    }

    private interface SliderValueConsumer {
        void accept(double value);
    }

}
