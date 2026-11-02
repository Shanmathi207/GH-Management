package hms.ui.components;

import hms.ui.UITheme;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Modern dashboard metric card displaying title, big numeric value, subtitle,
 * and an accent color badge.
 */
public class SummaryCard extends JPanel {

    private final JLabel lblTitle;
    private final JLabel lblValue;
    private final JLabel lblSubtitle;
    private final Color accentColor;
    private boolean isHovered = false;

    public SummaryCard(String title, String initialValue, String subtitle, Color accentColor) {
        this.accentColor = accentColor != null ? accentColor : UITheme.PRIMARY_BLUE;

        setLayout(new BorderLayout(8, 6));
        setOpaque(false);
        setBorder(new EmptyBorder(16, 18, 16, 18));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(UITheme.TEXT_MUTED);

        lblValue = new JLabel(initialValue);
        lblValue.setFont(UITheme.FONT_STAT_VAL);
        lblValue.setForeground(UITheme.TEXT_MAIN);

        lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(UITheme.FONT_SUBTITLE);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblValue.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        centerPanel.add(lblTitle);
        centerPanel.add(Box.createVerticalStrut(6));
        centerPanel.add(lblValue);
        centerPanel.add(Box.createVerticalStrut(4));
        centerPanel.add(lblSubtitle);

        add(centerPanel, BorderLayout.CENTER);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    public void setValue(String value) {
        lblValue.setText(value);
    }

    public void setSubtitle(String subtitle) {
        lblSubtitle.setText(subtitle);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth() - 1;
        int h = getHeight() - 1;

        // Draw card background
        g2.setColor(isHovered ? new Color(0xFA, 0xFC, 0xFF) : UITheme.WHITE_CARD);
        g2.fillRoundRect(0, 0, w, h, 14, 14);

        // Draw left accent bar
        g2.setColor(accentColor);
        g2.fillRoundRect(0, 0, 6, h, 6, 6);

        // Draw border
        g2.setColor(isHovered ? UITheme.PRIMARY_BLUE : UITheme.BORDER_COLOR);
        g2.drawRoundRect(0, 0, w, h, 14, 14);

        g2.dispose();
        super.paintComponent(g);
    }
}
