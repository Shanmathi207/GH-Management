package hms.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * UITheme centralizes design system constants, color palette,
 * typography, and custom painting helpers for the Government Hospital Management System.
 */
public class UITheme {

    // Color Palette
    public static final Color PRIMARY_NAVY = new Color(0x14, 0x2B, 0x4A); // #142B4A
    public static final Color NAVY_HOVER    = new Color(0x1C, 0x38, 0x5E);
    public static final Color NAVY_ACTIVE   = new Color(0x25, 0x47, 0x75);
    public static final Color PRIMARY_BLUE  = new Color(0x25, 0x63, 0xEB); // #2563EB
    public static final Color BLUE_HOVER    = new Color(0x1D, 0x4E, 0xD8);
    public static final Color LIGHT_BG      = new Color(0xF4, 0xF7, 0xFB); // #F4F7FB
    public static final Color WHITE_CARD    = new Color(0xFF, 0xFF, 0xFF); // #FFFFFF
    public static final Color PANEL_BG      = new Color(0xF8, 0xFA, 0xFC);
    public static final Color TEXT_MAIN     = new Color(0x1E, 0x29, 0x3B); // #1E293B
    public static final Color TEXT_MUTED    = new Color(0x64, 0x74, 0x8B); // #64748B
    public static final Color BORDER_COLOR  = new Color(0xE2, 0xE8, 0xF0); // #E2E8F0
    public static final Color SUCCESS_GREEN = new Color(0x16, 0xA3, 0x4A); // #16A34A
    public static final Color SUCCESS_LIGHT = new Color(0xDC, 0xFC, 0xE7);
    public static final Color WARNING_AMBER = new Color(0xF5, 0x9E, 0x0B); // #F59E0B
    public static final Color WARNING_LIGHT = new Color(0xFE, 0xF3, 0xC7);
    public static final Color ERROR_RED     = new Color(0xDC, 0x26, 0x26); // #DC2626
    public static final Color ERROR_LIGHT   = new Color(0xFE, 0xE2, 0xE2);
    public static final Color INFO_BLUE_BG  = new Color(0xDB, 0xEA, 0xFE);

    // Modern Typography
    public static final Font FONT_HEADER_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_PAGE_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SECTION      = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SUBTITLE     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_LABEL        = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_REGULAR      = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BUTTON       = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_STAT_VAL     = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_BADGE        = new Font("Segoe UI", Font.BOLD, 11);

    /**
     * Creates an anti-aliased rounded card panel with soft border.
     */
    public static JPanel createCardPanel() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE_CARD);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        return card;
    }

    /**
     * Creates a styled text field.
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
     * Creates a styled combo box.
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
        lbl.setForeground(PRIMARY_NAVY);
        return lbl;
    }
}
