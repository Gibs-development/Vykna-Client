package com.client.ui.shell;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

final class MarketPanel extends JPanel implements PropertyChangeListener {

    private final JTextArea feed = new JTextArea();
    private final JTextField input = new JTextField();

    MarketPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(18, 20, 22));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel title = new JLabel("Market");
        title.setForeground(new Color(230, 230, 230));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        top.add(title, BorderLayout.WEST);

        JButton request = smallButton("Request");
        request.setToolTipText("Asks the server to print the latest market lines (optional)");
        request.addActionListener(e -> VyknaShell.sendCommand("market"));
        top.add(request, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        feed.setEditable(false);
        feed.setLineWrap(true);
        feed.setWrapStyleWord(true);
        feed.setBackground(new Color(12, 13, 14));
        feed.setForeground(new Color(220, 220, 220));
        feed.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scroller = new JScrollPane(feed);
        scroller.setBorder(BorderFactory.createLineBorder(new Color(45, 49, 53)));
        add(scroller, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(6, 0));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(8, 0, 0, 0));

        input.setBackground(new Color(30, 33, 36));
        input.setForeground(new Color(230, 230, 230));
        input.setCaretColor(new Color(230, 230, 230));
        input.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 49, 53)),
                new EmptyBorder(8, 8, 8, 8)
        ));
        input.addActionListener(e -> send());

        JButton send = smallButton("Send");
        send.addActionListener(e -> send());

        bottom.add(input, BorderLayout.CENTER);
        bottom.add(send, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        // Load existing snapshot
        List<String> lines = UtilityState.INSTANCE.snapshotMarket();
        for (String line : lines) append(line);

        UtilityState.INSTANCE.addListener(this);
    }

    private void send() {
        String msg = input.getText();
        if (msg == null) msg = "";
        msg = msg.trim();
        if (msg.isEmpty()) return;

        // Server command: ::market <message>
        VyknaShell.sendCommand("market " + msg);
        input.setText("");
    }

    private void append(String line) {
        if (feed.getText().length() > 0) feed.append("\n");
        feed.append(line);
        feed.setCaretPosition(feed.getDocument().getLength());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!"market".equals(evt.getPropertyName())) return;
        Object v = evt.getNewValue();
        if (!(v instanceof String)) return;
        SwingUtilities.invokeLater(() -> append((String) v));
    }

    private static JButton smallButton(String text) {
        JButton b = new JButton(text);
        b.setFocusable(false);
        b.setForeground(new Color(230, 230, 230));
        b.setBackground(new Color(30, 33, 36));
        b.setBorder(BorderFactory.createLineBorder(new Color(45, 49, 53)));
        b.setPreferredSize(new Dimension(78, 32));
        return b;
    }
}
