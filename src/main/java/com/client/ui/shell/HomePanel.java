package com.client.ui.shell;

import com.client.Configuration;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

final class HomePanel extends JPanel {

    HomePanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(18, 20, 22));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel title = new JLabel("Vykna Utility");
        title.setForeground(new Color(230, 230, 230));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));

        JLabel sub = new JLabel("Quick actions & shortcuts");
        sub.setForeground(new Color(170, 175, 180));
        sub.setBorder(new EmptyBorder(2, 0, 12, 0));

        content.add(title);
        content.add(sub);

        content.add(rowButton("Vote", () -> VyknaShell.sendCommand("vote")));
        content.add(Box.createVerticalStrut(6));
        content.add(rowButton("Claim Vote", () -> VyknaShell.sendCommand("claim")));
        content.add(Box.createVerticalStrut(6));
        content.add(rowButton("Discord", () -> VyknaShell.sendCommand("discord")));
        content.add(Box.createVerticalStrut(6));
        content.add(rowButton("Website", () -> VyknaShell.openBrowser("https://" + Configuration.WEBSITE)));
        content.add(Box.createVerticalStrut(6));
        content.add(rowButton("Highscores", () -> VyknaShell.openBrowser("https://" + Configuration.WEBSITE + "/highscores")));

        content.add(Box.createVerticalStrut(14));
        JLabel hint = new JLabel("Tip: Utility tabs auto-update from chat (boss timer, goals, slayer)." );
        hint.setForeground(new Color(140, 145, 150));
        hint.setFont(hint.getFont().deriveFont(12f));
        content.add(hint);

        add(content, BorderLayout.NORTH);
    }

    private static JComponent rowButton(String text, Runnable action) {
        JButton b = new JButton(text);
        b.setFocusable(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        b.setPreferredSize(new Dimension(260, 36));
        b.setForeground(new Color(230, 230, 230));
        b.setBackground(new Color(30, 33, 36));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 49, 53)),
                new EmptyBorder(0, 10, 0, 10)
        ));
        b.addActionListener(e -> action.run());
        return b;
    }
}
