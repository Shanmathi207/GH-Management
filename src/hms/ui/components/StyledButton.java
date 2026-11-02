package hms.ui.components;

import hms.ui.UITheme;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

/**
 * Reusable modern button with rounded corners, hover transitions, and semantic variants.
 */
public class StyledButton extends JButton {

    public enum Type {
        PRIMARY, SECONDARY, SUCCESS, DANGER, NAVY
    }

    private final Type type;
    private boolean isHovered = false;
    private boolean isPressed = false;

    public StyledButton(String text) {
        this(text, Type.PRIMARY);
    }

    public StyledButton(String text, Type type) {
        super(text);
        this.type = type != null ? type : Type.PRIMARY;

        setFont(UITheme.FONT_BUTTON);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

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

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(d.width + 28, 110), Math.max(d.height + 12, 38));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bg;
        Color fg = Color.WHITE;
        Color border = null;

        switch (type) {
            case PRIMARY:
                bg = isPressed ? UITheme.NAVY_ACTIVE : (isHovered ? UITheme.BLUE_HOVER : UITheme.PRIMARY_BLUE);
                break;
            case SUCCESS:
                bg = isPressed ? new Color(0x15, 0x80, 0x3D) : (isHovered ? new Color(0x15, 0x80, 0x3D) : UITheme.SUCCESS_GREEN);
                break;
            case DANGER:
                bg = isPressed ? new Color(0xB9, 0x1C, 0x1C) : (isHovered ? new Color(0xB9, 0x1C, 0x1C) : UITheme.ERROR_RED);
                break;
            case NAVY:
                bg = isPressed ? UITheme.NAVY_ACTIVE : (isHovered ? UITheme.NAVY_HOVER : UITheme.PRIMARY_NAVY);
                break;
            case SECONDARY:
            default:
                bg = isPressed ? new Color(0xE2, 0xE8, 0xF0) : (isHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
                fg = UITheme.TEXT_MAIN;
                border = UITheme.BORDER_COLOR;
                break;
        }

        // Draw background
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

        // Draw border if secondary
        if (border != null) {
            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
        }

        // Draw text
        g2.setColor(fg);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int textX = (getWidth() - fm.stringWidth(getText())) / 2;
        int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }
}
