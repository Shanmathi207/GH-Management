
package hms.ui.components;

import hms.ui.UITheme;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Modern Light Healthcare Sidebar Navigation Panel.
 * Improved width, text visibility, spacing and alignment.
 */
public class SidebarPanel extends JPanel {

    public static final String NAV_DASHBOARD = "Dashboard";
    public static final String NAV_REGISTRATION = "Patient Registration";
    public static final String NAV_RECORDS = "Patient Records";
    public static final String NAV_PRIORITY_QUEUE = "Priority Queue";
    public static final String NAV_OPD_CONSULTATION = "OPD Consultation";
    public static final String NAV_OPD_IPD_WORKFLOW = "OPD to IPD Workflow";
    public static final String NAV_EMERGENCY = "Emergency Registration";
    public static final String NAV_ADMISSIONS = "Admission Requests";
    public static final String NAV_BED_MANAGEMENT = "Bed Management";
    public static final String NAV_DOCTORS = "Doctor Management";
    public static final String NAV_HOSPITALS = "Hospital Management";
    public static final String NAV_DISCHARGE = "Patient Discharge";
    public static final String NAV_REFERRALS = "Hospital Referrals";
    public static final String NAV_HISTORY = "Patient History";
    public static final String NAV_REPORTS = "Reports & Statistics";
    public static final String NAV_SETTINGS = "Settings";
    public static final String NAV_LOGOUT = "Logout";

    public static final String NAV_DOCTORS_HOSPITALS = NAV_DOCTORS;

    public static final String[] NAV_ITEMS = {
        NAV_DASHBOARD,
        NAV_REGISTRATION,
        NAV_RECORDS,
        NAV_PRIORITY_QUEUE,
        NAV_OPD_CONSULTATION,
        NAV_OPD_IPD_WORKFLOW,
        NAV_EMERGENCY,
        NAV_ADMISSIONS,
        NAV_BED_MANAGEMENT,
        NAV_DOCTORS,
        NAV_HOSPITALS,
        NAV_DISCHARGE,
        NAV_REFERRALS,
        NAV_HISTORY,
        NAV_REPORTS,
        NAV_SETTINGS,
        NAV_LOGOUT
    };

    private final List<NavItemButton> navButtons = new ArrayList<>();
    private final Consumer<String> onNavigate;
    private String currentSelected = NAV_DASHBOARD;

    // Sidebar dimensions
    private static final int SIDEBAR_WIDTH = 390;
    private static final int BUTTON_HEIGHT = 38;

    public SidebarPanel(Consumer<String> onNavigate) {

        this.onNavigate = onNavigate;

        setLayout(new BorderLayout());
        setBackground(UITheme.SIDEBAR_BG);

        // Fixed sidebar width leaves enough room for every navigation label.
        setPreferredSize(new Dimension(SIDEBAR_WIDTH, 800));
        setMinimumSize(new Dimension(SIDEBAR_WIDTH, 600));
        setMaximumSize(new Dimension(SIDEBAR_WIDTH, Integer.MAX_VALUE));

        // Vertical divider on the right side
        setBorder(BorderFactory.createMatteBorder(
                0, 0, 0, 1, UITheme.SIDEBAR_BORDER));

        // Main sidebar content
        JPanel contentPanel = new JPanel();

        contentPanel.setLayout(new BoxLayout(
                contentPanel, BoxLayout.Y_AXIS));

        contentPanel.setBackground(UITheme.SIDEBAR_BG);

        contentPanel.setBorder(
                new EmptyBorder(14, 10, 14, 10));

        for (String item : NAV_ITEMS) {

            if (item.equals(NAV_DASHBOARD)) {
                contentPanel.add(createSectionHeader("MAIN"));
                contentPanel.add(Box.createVerticalStrut(4));

            } else if (item.equals(NAV_ADMISSIONS)) {
                contentPanel.add(Box.createVerticalStrut(10));
                contentPanel.add(createSectionHeader("INPATIENT & BEDS"));
                contentPanel.add(Box.createVerticalStrut(4));

            } else if (item.equals(NAV_DOCTORS)) {
                contentPanel.add(Box.createVerticalStrut(10));
                contentPanel.add(createSectionHeader("FACILITIES & NETWORK"));
                contentPanel.add(Box.createVerticalStrut(4));

            } else if (item.equals(NAV_HISTORY)) {
                contentPanel.add(Box.createVerticalStrut(10));
                contentPanel.add(createSectionHeader("SYSTEM & AUDIT"));
                contentPanel.add(Box.createVerticalStrut(4));
            }

            NavItemButton btn = new NavItemButton(
                    item,
                    item.equals(currentSelected));

            btn.addActionListener(e -> selectItem(item));

            navButtons.add(btn);

            contentPanel.add(btn);
            contentPanel.add(Box.createVerticalStrut(3));
        }

        contentPanel.add(Box.createVerticalGlue());

        // Footer
        JPanel footer = new JPanel(new GridLayout(2, 1, 0, 2));

        footer.setOpaque(false);

        footer.setBorder(
                new EmptyBorder(12, 14, 6, 12));

        JLabel lblVer = new JLabel("HospitalOOP70 Healthcare");

        lblVer.setFont(new Font(
                "Segoe UI", Font.BOLD, 10));

        lblVer.setForeground(UITheme.SIDEBAR_HEADING);

        JLabel lblSub = new JLabel(
                "Government Administration v2.0");

        lblSub.setFont(new Font(
                "Segoe UI", Font.PLAIN, 10));

        lblSub.setForeground(new Color(0xA0, 0xB0, 0xC0));

        footer.add(lblVer);
        footer.add(lblSub);

        contentPanel.add(footer);

        // Vertical scrolling
        JScrollPane scrollPane = new JScrollPane(contentPanel);

        scrollPane.setBorder(null);

        scrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        scrollPane.getViewport().setBackground(UITheme.SIDEBAR_BG);

        scrollPane.getVerticalScrollBar().setUnitIncrement(14);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JLabel createSectionHeader(String title) {

        JLabel lbl = new JLabel(title);

        lbl.setFont(new Font(
                "Segoe UI", Font.BOLD, 10));

        lbl.setForeground(UITheme.SIDEBAR_HEADING);

        lbl.setBorder(
                new EmptyBorder(4, 14, 2, 0));

        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        return lbl;
    }

    public void selectItem(String item) {
        selectItem(item, true);
    }

    public void selectItem(String item, boolean triggerCallback) {

        this.currentSelected = item;

        for (NavItemButton btn : navButtons) {
            btn.setSelected(
                    btn.getItemName().equals(item));
        }

        if (triggerCallback && onNavigate != null) {
            onNavigate.accept(item);
        }
    }

    /**
     * Navigation button with complete text visibility
     * and consistent icon alignment.
     */
    private static class NavItemButton extends JButton {

        private final String itemName;

        private boolean isSelected;
        private boolean isHovered;

        private static final int ICON_X = 14;
        private static final int ICON_SIZE = 16;
        private static final int TEXT_X = 42;

        public NavItemButton(String itemName, boolean isSelected) {

            this.itemName = itemName;
            this.isSelected = isSelected;

            setToolTipText(itemName);

            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);

            setCursor(new Cursor(Cursor.HAND_CURSOR));

            setAlignmentX(Component.LEFT_ALIGNMENT);

            Dimension dim = new Dimension(360, BUTTON_HEIGHT);
            setPreferredSize(dim);
            setMinimumSize(new Dimension(340, BUTTON_HEIGHT));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));

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

        public String getItemName() {
            return itemName;
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Background
            if (isSelected) {

                g2.setColor(UITheme.SIDEBAR_ACTIVE_BG);

                g2.fillRoundRect(
                        4, 1, w - 8, h - 2, 8, 8);

                // Active indicator
                g2.setColor(UITheme.SIDEBAR_INDICATOR);

                g2.fillRoundRect(
                        4, 6, 3, h - 12, 2, 2);

            } else if (isHovered) {

                g2.setColor(new Color(0xE2, 0xEE, 0xFA));

                g2.fillRoundRect(
                        4, 1, w - 8, h - 2, 8, 8);
            }

            // Icon and text colors
            Color iconColor = isSelected
                    ? UITheme.SIDEBAR_INDICATOR
                    : (isHovered
                            ? UITheme.SIDEBAR_ACTIVE_TXT
                            : UITheme.SIDEBAR_ICON);

            Color textColor = isSelected
                    ? UITheme.SIDEBAR_ACTIVE_TXT
                    : (isHovered
                            ? UITheme.SIDEBAR_ACTIVE_TXT
                            : UITheme.SIDEBAR_TEXT);

            // Icon alignment
            int iconY = (h - ICON_SIZE) / 2;

            drawMenuIcon(
                    g2,
                    itemName,
                    ICON_X,
                    iconY,
                    ICON_SIZE,
                    iconColor);

            // Complete menu text
            g2.setColor(textColor);

            g2.setFont(new Font(
                    "Segoe UI",
                    isSelected ? Font.BOLD : Font.PLAIN,
                    12));

            FontMetrics fm = g2.getFontMetrics();

            int textY = (h + fm.getAscent() - fm.getDescent()) / 2;

            g2.drawString(itemName, TEXT_X, textY);

            g2.dispose();
        }

        /**
         * Draw healthcare navigation icons.
         */
        private void drawMenuIcon(
                Graphics2D g2,
                String item,
                int x,
                int y,
                int size,
                Color c) {

            g2.setColor(c);

            g2.setStroke(
                    new BasicStroke(
                            1.6f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND));

            switch (item) {

                case NAV_DASHBOARD:
                    g2.drawRoundRect(x + 1, y + 1, 5, 5, 2, 2);
                    g2.drawRoundRect(x + 8, y + 1, 5, 5, 2, 2);
                    g2.drawRoundRect(x + 1, y + 8, 5, 5, 2, 2);
                    g2.drawRoundRect(x + 8, y + 8, 5, 5, 2, 2);
                    break;

                case NAV_REGISTRATION:
                    g2.drawOval(x + 3, y + 1, 6, 6);
                    g2.drawArc(x + 1, y + 8, 10, 7, 0, 180);
                    g2.drawLine(x + 13, y + 4, x + 13, y + 8);
                    g2.drawLine(x + 11, y + 6, x + 15, y + 6);
                    break;

                case NAV_RECORDS:
                    g2.drawRoundRect(x + 2, y + 2, 11, 13, 2, 2);
                    g2.fillRect(x + 5, y + 1, 5, 2);
                    g2.drawLine(x + 5, y + 6, x + 10, y + 6);
                    g2.drawLine(x + 5, y + 9, x + 10, y + 9);
                    g2.drawLine(x + 5, y + 12, x + 8, y + 12);
                    break;

                case NAV_PRIORITY_QUEUE:
                    g2.drawLine(x + 1, y + 3, x + 13, y + 3);
                    g2.drawLine(x + 1, y + 7, x + 10, y + 7);
                    g2.drawLine(x + 1, y + 11, x + 7, y + 11);
                    g2.fillOval(x + 12, y + 9, 3, 3);
                    break;

                case NAV_OPD_CONSULTATION:
                    g2.drawArc(x + 2, y + 2, 8, 8, 180, 180);
                    g2.drawLine(x + 2, y + 2, x + 2, y + 5);
                    g2.drawLine(x + 10, y + 2, x + 10, y + 5);
                    g2.drawLine(x + 6, y + 10, x + 6, y + 12);
                    g2.drawOval(x + 5, y + 12, 4, 3);
                    break;

                case NAV_OPD_IPD_WORKFLOW:
                    g2.drawLine(x + 1, y + 7, x + 13, y + 7);
                    g2.drawLine(x + 9, y + 3, x + 13, y + 7);
                    g2.drawLine(x + 9, y + 11, x + 13, y + 7);
                    g2.drawOval(x + 2, y + 11, 3, 3);
                    g2.drawOval(x + 9, y + 11, 3, 3);
                    break;

                case NAV_EMERGENCY:
                    g2.drawLine(x + 8, y + 1, x + 4, y + 8);
                    g2.drawLine(x + 4, y + 8, x + 9, y + 8);
                    g2.drawLine(x + 9, y + 8, x + 6, y + 15);
                    break;

                case NAV_ADMISSIONS:
                    g2.drawRoundRect(x + 2, y + 1, 10, 14, 2, 2);
                    g2.drawLine(x + 7, y + 5, x + 7, y + 11);
                    g2.drawLine(x + 4, y + 8, x + 7, y + 5);
                    g2.drawLine(x + 10, y + 8, x + 7, y + 5);
                    break;

                case NAV_BED_MANAGEMENT:
                    g2.drawLine(x + 1, y + 4, x + 1, y + 14);
                    g2.drawLine(x + 14, y + 8, x + 14, y + 14);
                    g2.drawLine(x + 1, y + 10, x + 14, y + 10);
                    g2.drawRoundRect(x + 3, y + 7, 3, 2, 1, 1);
                    break;

                case NAV_DOCTORS:
                    g2.drawOval(x + 4, y + 1, 6, 6);
                    g2.drawArc(x + 1, y + 8, 12, 7, 0, 180);
                    g2.drawLine(x + 7, y + 10, x + 7, y + 14);
                    break;

                case NAV_HOSPITALS:
                    g2.drawRoundRect(x + 2, y + 4, 11, 11, 2, 2);
                    g2.drawLine(x + 7, y + 6, x + 7, y + 10);
                    g2.drawLine(x + 5, y + 8, x + 9, y + 8);
                    g2.drawLine(x + 6, y + 15, x + 6, y + 12);
                    g2.drawLine(x + 8, y + 15, x + 8, y + 12);
                    break;

                case NAV_DISCHARGE:
                    g2.drawRect(x + 1, y + 2, 7, 12);
                    g2.drawLine(x + 6, y + 8, x + 14, y + 8);
                    g2.drawLine(x + 11, y + 5, x + 14, y + 8);
                    g2.drawLine(x + 11, y + 11, x + 14, y + 8);
                    break;

                case NAV_REFERRALS:
                    g2.drawOval(x + 2, y + 3, 4, 4);
                    g2.drawOval(x + 10, y + 3, 4, 4);
                    g2.drawOval(x + 6, y + 10, 4, 4);
                    g2.drawLine(x + 5, y + 6, x + 7, y + 10);
                    g2.drawLine(x + 10, y + 6, x + 8, y + 10);
                    break;

                case NAV_HISTORY:
                    g2.drawOval(x + 1, y + 1, 13, 13);
                    g2.drawLine(x + 7, y + 4, x + 7, y + 8);
                    g2.drawLine(x + 7, y + 8, x + 10, y + 8);
                    break;

                case NAV_REPORTS:
                    g2.drawLine(x + 1, y + 14, x + 14, y + 14);
                    g2.fillRect(x + 3, y + 9, 2, 5);
                    g2.fillRect(x + 7, y + 5, 2, 9);
                    g2.fillRect(x + 11, y + 2, 2, 12);
                    break;

                case NAV_SETTINGS:
                    g2.drawLine(x + 2, y + 4, x + 13, y + 4);
                    g2.drawOval(x + 4, y + 2, 3, 4);
                    g2.drawLine(x + 2, y + 8, x + 13, y + 8);
                    g2.drawOval(x + 9, y + 6, 3, 4);
                    g2.drawLine(x + 2, y + 12, x + 13, y + 12);
                    g2.drawOval(x + 5, y + 10, 3, 4);
                    break;

                case NAV_LOGOUT:
                    g2.drawLine(x + 6, y + 3, x + 2, y + 3);
                    g2.drawLine(x + 2, y + 3, x + 2, y + 13);
                    g2.drawLine(x + 2, y + 13, x + 6, y + 13);
                    g2.drawLine(x + 5, y + 8, x + 14, y + 8);
                    g2.drawLine(x + 11, y + 5, x + 14, y + 8);
                    g2.drawLine(x + 11, y + 11, x + 14, y + 8);
                    break;

                default:
                    g2.fillOval(x + 5, y + 5, 5, 5);
                    break;
            }
        }
    }
}
