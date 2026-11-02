package hms.ui.components;

import hms.ui.UITheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Custom table cell renderer that draws stylish colored status badge pills
 * for beds, patients, priority levels, and admission requests.
 */
public class StatusCellRenderer extends DefaultTableCellRenderer {

    private String text = "";
    private Color bgBadge = UITheme.INFO_BLUE_BG;
    private Color fgBadge = UITheme.PRIMARY_BLUE;

    public StatusCellRenderer() {
        setHorizontalAlignment(CENTER);
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        text = value != null ? value.toString() : "";
        String upper = text.toUpperCase();

        // Alternating row background for unselected cell
        if (isSelected) {
            setBackground(new Color(0xE0, 0xED, 0xFD));
        } else {
            setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
        }

        // Determine pill colors based on semantics
        if (upper.contains("AVAILABLE") || upper.contains("COMPLETED") || upper.contains("ALLOCATED") || upper.equals("5") || upper.contains("NON-URGENT")) {
            bgBadge = UITheme.SUCCESS_LIGHT;
            fgBadge = UITheme.SUCCESS_GREEN;
        } else if (upper.contains("OCCUPIED") || upper.contains("CRITICAL") || upper.equals("1") || upper.contains("EMERGENCY")) {
            bgBadge = UITheme.ERROR_LIGHT;
            fgBadge = UITheme.ERROR_RED;
        } else if (upper.contains("CLEANING") || upper.contains("PENDING") || upper.contains("HIGH") || upper.equals("2") || upper.contains("MEDIUM") || upper.equals("3")) {
            bgBadge = UITheme.WARNING_LIGHT;
            fgBadge = new Color(0xB4, 0x53, 0x09);
        } else if (upper.contains("ADMITTED") || upper.contains("CALLED") || upper.contains("IN QUEUE") || upper.contains("WAITING")) {
            bgBadge = new Color(0xEE, 0xE8, 0xAA);
            bgBadge = UITheme.INFO_BLUE_BG;
            fgBadge = UITheme.PRIMARY_BLUE;
        } else {
            bgBadge = new Color(0xE2, 0xE8, 0xF0);
            fgBadge = UITheme.TEXT_MUTED;
        }

        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (text == null || text.trim().isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setFont(UITheme.FONT_BADGE);
        FontMetrics fm = g2.getFontMetrics();
        int strWidth = fm.stringWidth(text);
        int badgeWidth = Math.min(getWidth() - 10, strWidth + 18);
        int badgeHeight = 22;

        int badgeX = (getWidth() - badgeWidth) / 2;
        int badgeY = (getHeight() - badgeHeight) / 2;

        // Draw pill background
        g2.setColor(bgBadge);
        g2.fillRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, 12, 12);

        // Draw pill border
        g2.setColor(new Color(fgBadge.getRed(), fgBadge.getGreen(), fgBadge.getBlue(), 60));
        g2.drawRoundRect(badgeX, badgeY, badgeWidth - 1, badgeHeight - 1, 12, 12);

        // Draw text
        g2.setColor(fgBadge);
        int textX = badgeX + (badgeWidth - strWidth) / 2;
        int textY = badgeY + (badgeHeight + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(text, textX, textY);

        g2.dispose();
    }
}
