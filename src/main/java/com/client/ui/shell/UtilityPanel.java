package com.client.ui.shell;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Announcements + timers + tracking in one tab.
 */
final class UtilityPanel extends JPanel implements PropertyChangeListener {

    private final JLabel nextBossLabel = new JLabel("Unknown");
    private final JProgressBar donationBar = new JProgressBar();
    private final JLabel donationLabel = new JLabel("0/0");
    private final JProgressBar voteBar = new JProgressBar();
    private final JLabel voteLabel = new JLabel("0/0");

    private final JLabel slayerTaskLabel = new JLabel("No task");
    private final JLabel slayerRemainingLabel = new JLabel("-");
    private final JLabel slayerPointsLabel = new JLabel("0");

    private final JLabel votePointsLabel = new JLabel("0");
    private final JLabel donationPointsLabel = new JLabel("0");

    private final Timer tick;

    UtilityPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(18, 20, 22));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel title = new JLabel("Utility");
        title.setForeground(new Color(230, 230, 230));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        content.add(title);
        content.add(Box.createVerticalStrut(10));

        content.add(sectionHeader("Announcements / Events"));
        content.add(kvRow("Next Global Boss", nextBossLabel));

        JPanel eventBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        eventBtns.setOpaque(false);
        JButton askEvents = smallButton("Ask server");
        askEvents.setToolTipText("Sends ::events (change server-side if you prefer)" );
        askEvents.addActionListener(e -> VyknaShell.sendCommand("events"));
        JButton set5 = smallButton("+5m");
        set5.setToolTipText("Local-only quick set (useful while you wire server messages)");
        set5.addActionListener(e -> bumpBossTimerMinutes(5));
        JButton set30 = smallButton("+30m");
        set30.addActionListener(e -> bumpBossTimerMinutes(30));
        eventBtns.add(askEvents);
        eventBtns.add(set5);
        eventBtns.add(set30);
        content.add(Box.createVerticalStrut(4));
        content.add(eventBtns);

        content.add(Box.createVerticalStrut(12));
        content.add(sectionHeader("Goals"));
        content.add(progressRow("Donation goal", donationBar, donationLabel));
        content.add(Box.createVerticalStrut(6));
        content.add(progressRow("Vote goal", voteBar, voteLabel));

        JPanel goalBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        goalBtns.setOpaque(false);
        JButton askGoals = smallButton("Ask server");
        askGoals.setToolTipText("Sends ::goals (or handle it server-side)");
        askGoals.addActionListener(e -> VyknaShell.sendCommand("goals"));
        goalBtns.add(askGoals);
        content.add(Box.createVerticalStrut(4));
        content.add(goalBtns);

        content.add(Box.createVerticalStrut(12));
        content.add(sectionHeader("Slayer"));
        content.add(kvRow("Task", slayerTaskLabel));
        content.add(kvRow("Remaining", slayerRemainingLabel));
        content.add(kvRow("Points", slayerPointsLabel));

        JPanel slayerBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        slayerBtns.setOpaque(false);
        JButton askSlayer = smallButton("Ask server");
        askSlayer.setToolTipText("Sends ::task (common on RSPS)" );
        askSlayer.addActionListener(e -> VyknaShell.sendCommand("task"));
        JButton clear = smallButton("Clear");
        clear.setToolTipText("Clears the client-side view (does not change your real task)");
        clear.addActionListener(e -> UtilityState.INSTANCE.setSlayerTask("", 0, 0));
        slayerBtns.add(askSlayer);
        slayerBtns.add(clear);
        content.add(Box.createVerticalStrut(4));
        content.add(slayerBtns);

        content.add(Box.createVerticalStrut(12));
        content.add(sectionHeader("Points"));
        content.add(kvRow("Vote points", votePointsLabel));
        content.add(kvRow("Donation points", donationPointsLabel));

        JPanel pointsBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pointsBtns.setOpaque(false);
        JButton askPoints = smallButton("Ask server");
        askPoints.setToolTipText("Sends ::points (optional)");
        askPoints.addActionListener(e -> VyknaShell.sendCommand("points"));
        pointsBtns.add(askPoints);
        content.add(Box.createVerticalStrut(4));
        content.add(pointsBtns);

        content.add(Box.createVerticalStrut(12));
        JLabel hint = new JLabel("Auto updates from chat if server prints lines like: 'Next global boss in 05:00', 'Donation goal: 120/500', 'You have 35 slayer points'" );
        hint.setForeground(new Color(140, 145, 150));
        hint.setFont(hint.getFont().deriveFont(12f));
        content.add(hint);

        add(new JScrollPane(content) {{
            setBorder(BorderFactory.createEmptyBorder());
            getVerticalScrollBar().setUnitIncrement(16);
            setOpaque(false);
            getViewport().setOpaque(false);
        }}, BorderLayout.CENTER);

        // Style progress bars
        styleProgress(donationBar);
        styleProgress(voteBar);

        UtilityState.INSTANCE.addListener(this);
        refreshAll();

        tick = new Timer(1000, e -> refreshCountdown());
        tick.start();
    }

    private void bumpBossTimerMinutes(int mins) {
        long now = System.currentTimeMillis();
        long cur = UtilityState.INSTANCE.getNextGlobalBossEpochMs();
        long base = cur > now ? cur : now;
        UtilityState.INSTANCE.setNextGlobalBossEpochMs(base + mins * 60_000L);
    }

    private void refreshAll() {
        refreshCountdown();
        refreshGoals();
        refreshSlayer();
        refreshPoints();
    }

    private void refreshCountdown() {
        long epoch = UtilityState.INSTANCE.getNextGlobalBossEpochMs();
        if (epoch <= 0L) {
            nextBossLabel.setText("Unknown");
            return;
        }
        long now = System.currentTimeMillis();
        long diff = epoch - now;
        if (diff <= 0L) {
            nextBossLabel.setText("Now / soon");
            return;
        }
        long totalSec = diff / 1000L;
        long h = totalSec / 3600L;
        long m = (totalSec % 3600L) / 60L;
        long s = totalSec % 60L;
        if (h > 0) nextBossLabel.setText(h + "h " + m + "m " + s + "s");
        else nextBossLabel.setText(m + "m " + s + "s");
    }

    private void refreshGoals() {
        int dc = UtilityState.INSTANCE.getDonationCurrent();
        int dt = UtilityState.INSTANCE.getDonationTarget();
        donationLabel.setText(dc + "/" + dt);
        donationBar.setMaximum(Math.max(1, dt));
        donationBar.setValue(Math.min(dc, Math.max(1, dt)));

        int vc = UtilityState.INSTANCE.getVoteCurrent();
        int vt = UtilityState.INSTANCE.getVoteTarget();
        voteLabel.setText(vc + "/" + vt);
        voteBar.setMaximum(Math.max(1, vt));
        voteBar.setValue(Math.min(vc, Math.max(1, vt)));
    }

    private void refreshSlayer() {
        String name = UtilityState.INSTANCE.getSlayerTaskName();
        int amt = UtilityState.INSTANCE.getSlayerTaskAmount();
        int rem = UtilityState.INSTANCE.getSlayerTaskRemaining();
        if (name == null || name.trim().isEmpty() || amt <= 0) {
            slayerTaskLabel.setText("No task");
            slayerRemainingLabel.setText("-");
        } else {
            slayerTaskLabel.setText(amt + " " + name);
            slayerRemainingLabel.setText(String.valueOf(rem));
        }
        slayerPointsLabel.setText(String.valueOf(UtilityState.INSTANCE.getSlayerPoints()));
    }

    private void refreshPoints() {
        votePointsLabel.setText(String.valueOf(UtilityState.INSTANCE.getVotePoints()));
        donationPointsLabel.setText(String.valueOf(UtilityState.INSTANCE.getDonationPoints()));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "nextGlobalBossEpochMs":
                SwingUtilities.invokeLater(this::refreshCountdown);
                break;
            case "donationGoal":
            case "voteGoal":
                SwingUtilities.invokeLater(this::refreshGoals);
                break;
            case "slayerTask":
            case "slayerRemaining":
            case "slayerPoints":
                SwingUtilities.invokeLater(this::refreshSlayer);
                break;
            case "votePoints":
            case "donationPoints":
                SwingUtilities.invokeLater(this::refreshPoints);
                break;
        }
    }

    private static JLabel sectionHeader(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(200, 205, 210));
        l.setFont(l.getFont().deriveFont(Font.BOLD, 13f));
        l.setBorder(new EmptyBorder(0, 0, 6, 0));
        return l;
    }

    private static JPanel kvRow(String key, JLabel value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel k = new JLabel(key);
        k.setForeground(new Color(155, 160, 165));
        value.setForeground(new Color(230, 230, 230));
        row.add(k, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        row.setBorder(new EmptyBorder(0, 0, 4, 0));
        return row;
    }

    private static JPanel progressRow(String key, JProgressBar bar, JLabel value) {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel k = new JLabel(key);
        k.setForeground(new Color(155, 160, 165));
        value.setForeground(new Color(230, 230, 230));
        top.add(k, BorderLayout.WEST);
        top.add(value, BorderLayout.EAST);

        bar.setBorder(BorderFactory.createLineBorder(new Color(45, 49, 53)));
        bar.setPreferredSize(new Dimension(250, 18));
        bar.setStringPainted(false);

        wrap.add(top);
        wrap.add(bar);
        return wrap;
    }

    private static JButton smallButton(String text) {
        JButton b = new JButton(text);
        b.setFocusable(false);
        b.setForeground(new Color(230, 230, 230));
        b.setBackground(new Color(30, 33, 36));
        b.setBorder(BorderFactory.createLineBorder(new Color(45, 49, 53)));
        b.setPreferredSize(new Dimension(92, 30));
        return b;
    }

    private static void styleProgress(JProgressBar bar) {
        bar.setBackground(new Color(12, 13, 14));
        bar.setForeground(new Color(75, 170, 120));
    }
}
