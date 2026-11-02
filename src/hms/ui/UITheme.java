package hms.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * UITheme centralizes design system constants, color palette,
 * typography, and custom painting helpers for the Government Hospital Management System.
 * Standardized on a clean, modern, professional healthcare light theme.
 */
public class UITheme {

    // Primary Light Healthcare Color Palette
    public static final Color PRIMARY_BLUE      = new Color(0x28, 0x78, 0xDF); // #2878DF
    public static final Color BLUE_HOVER        = new Color(0x1E, 0x68, 0xCA);
    public static final Color SIDEBAR_BG        = new Color(0xED, 0xF4, 0xFC); // #EDF4FC
    public static final Color SIDEBAR_TEXT      = new Color(0x42, 0x5B, 0x78); // #425B78
    public static final Color SIDEBAR_ICON      = new Color(0x60, 0x7D, 0x9B); // #607D9B
    public static final Color SIDEBAR_ACTIVE_BG = new Color(0xD8, 0xE9, 0xFF); // #D8E9FF
    public static final Color SIDEBAR_ACTIVE_TXT= new Color(0x17, 0x64, 0xC0); // #1764C0
    public static final Color SIDEBAR_INDICATOR = new Color(0x28, 0x78, 0xDF); // #2878DF
    public static final Color SIDEBAR_HEADING   = new Color(0x82, 0x95, 0xAA); // #8295AA
    public static final Color SIDEBAR_BORDER    = new Color(0xDC, 0xE6, 0xF0); // #DCE6F0

    public static final Color LIGHT_BG          = new Color(0xF5, 0xF8, 0xFC); // #F5F8FC
    public static final Color WHITE_CARD        = new Color(0xFF, 0xFF, 0xFF); // #FFFFFF
    public static final Color PANEL_BG          = new Color(0xF8, 0xFA, 0xFC);
    public static final Color TEXT_MAIN         = new Color(0x24, 0x3B, 0x53); // #243B53
    public static final Color TEXT_MUTED        = new Color(0x64, 0x74, 0x8B); // #64748B
    public static final Color BORDER_COLOR      = new Color(0xE1, 0xE8, 0xF0); // #E1E8F0
    public static final Color SUCCESS_GREEN     = new Color(0x16, 0xA3, 0x4A); // Clinical Green (#16A34A)
    public static final Color SUCCESS_LIGHT     = new Color(0xDC, 0xFC, 0xE7);
    public static final Color WARNING_AMBER     = new Color(0xF5, 0x9E, 0x0B); // Amber Warning (#F59E0B)
    public static final Color WARNING_LIGHT     = new Color(0xFE, 0xF3, 0xC7);
    public static final Color ERROR_RED         = new Color(0xDC, 0x26, 0x26); // Emergency Red (#DC2626)
    public static final Color DANGER_RED        = ERROR_RED;
    public static final Color ERROR_LIGHT       = new Color(0xFE, 0xE2, 0xE2);
    public static final Color INFO_BLUE_BG      = new Color(0xDB, 0xEA, 0xFE);
    public static final Color TEXT_DARK         = TEXT_MAIN;

    // Legacy palette compatibility
    public static final Color PRIMARY_NAVY      = new Color(0x17, 0x64, 0xC0); // Modern deep clinical blue
    public static final Color NAVY_HOVER        = new Color(0x1E, 0x68, 0xCA);
    public static final Color NAVY_ACTIVE       = new Color(0x25, 0x47, 0x75);

    // Semantic Accents
    public static final Color ACCENT_PURPLE     = new Color(0x7C, 0x3A, 0xED);
    public static final Color ACCENT_TEAL       = new Color(0x0D, 0x94, 0x88);
    public static final Color ACCENT_SKY        = new Color(0x02, 0x84, 0xC7);

    // Modern Healthcare Typography
    public static final Font FONT_HEADER_TITLE  = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_PAGE_TITLE    = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SECTION       = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_CARD_TITLE    = FONT_SECTION;
    public static final Font FONT_SUBTITLE      = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_LABEL         = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_REGULAR       = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BUTTON        = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_STAT_VAL      = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_BADGE         = new Font("Segoe UI", Font.BOLD, 11);

    /**
     * Creates an anti-aliased rounded card panel with subtle drop shadow and crisp border.
     */
    public static JPanel createCardPanel() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                // Soft, subtle shadow
                g2.setColor(new Color(0, 0, 0, 8));
                g2.fillRoundRect(2, 3, w - 4, h - 4, 12, 12);
                // White card background
                g2.setColor(WHITE_CARD);
                g2.fillRoundRect(0, 0, w - 2, h - 2, 12, 12);
                // Crisp border
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, w - 2, h - 2, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        return card;
    }

    /**
     * Creates a styled text field with rounded borders and padding.
     */
    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_REGULAR);
        tf.setForeground(TEXT_MAIN);
        tf.setCaretColor(PRIMARY_BLUE);
        tf.setBackground(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));
        return tf;
    }

    /**
     * Creates a styled password field.
     */
    public static JPasswordField createPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        pf.setFont(FONT_REGULAR);
        pf.setForeground(TEXT_MAIN);
        pf.setCaretColor(PRIMARY_BLUE);
        pf.setBackground(Color.WHITE);
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));
        return pf;
    }

    /**
     * Creates a styled combo box with clean border and clear font.
     */
    public static <T> JComboBox<T> createComboBox(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setFont(FONT_REGULAR);
        cb.setBackground(Color.WHITE);
        cb.setForeground(TEXT_MAIN);
        cb.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        return cb;
    }

    /**
     * Creates a form field label.
     */
    public static JLabel createFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_MAIN);
        return lbl;
    }

    /**
     * Creates a modern section header label.
     */
    public static JLabel createSectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SECTION);
        lbl.setForeground(TEXT_MAIN);
        return lbl;
    }

    /**
     * Creates a badge pill component for status display.
     */
    public static JLabel createBadge(String text, Color bgColor, Color fgColor) {
        JLabel badge = new JLabel(text);
        badge.setFont(FONT_BADGE);
        badge.setForeground(fgColor);
        badge.setOpaque(true);
        badge.setBackground(bgColor);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        return badge;
    }
}
