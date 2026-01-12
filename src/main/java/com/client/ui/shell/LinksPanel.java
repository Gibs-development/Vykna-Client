package com.client.ui.shell;

import com.client.Configuration;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

final class LinksPanel extends JPanel {

    LinksPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(18, 20, 22));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel title = new JLabel("Links");
        title.setForeground(new Color(230, 230, 230));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        content.add(title);
        content.add(Box.createVerticalStrut(10));

        addLink(content, "Website", "https://" + Configuration.WEBSITE);
        addLink(content, "Highscores", "https://" + Configuration.WEBSITE + "/highscores");
        addLink(content, "Store", "https://" + Configuration.WEBSITE + "/store");
        addLink(content, "Wiki", "https://" + Configuration.WEBSITE + "/wiki");

        content.add(Box.createVerticalStrut(14));
        JLabel hint = new JLabel("You can hardcode more links in LinksPanel.java");
        hint.setForeground(new Color(140, 145, 150));
        hint.setFont(hint.getFont().deriveFont(12f));
        content.add(hint);

        add(content, BorderLayout.NORTH);
    }

    private static void addLink(JPanel parent, String text, String url) {
        JButton b = new JButton(text);
        b.setFocusable(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        b.setForeground(new Color(230, 230, 230));
        b.setBackground(new Color(30, 33, 36));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 49, 53)),
                new EmptyBorder(0, 10, 0, 10)
        ));
        b.addActionListener(e -> VyknaShell.openBrowser(url));
        parent.add(b);
        parent.add(Box.createVerticalStrut(6));
    }
}
