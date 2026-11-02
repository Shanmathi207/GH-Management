package hms.ui.components;

import hms.ui.UITheme;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Modern Light Healthcare Sidebar Navigation Panel.
 * Uses a clean light blue and white palette (#EDF4FC, #D8E9FF, #1764C0, #2878DF),
 * precise vertical icon and label alignment, uniform button heights, and distinct category groupings.
 */
public class SidebarPanel extends JPanel {

    public static final String NAV_DASHBOARD           = "Dashboard";
    public static final String NAV_REGISTRATION         = "Patient Registration";
    public static final String NAV_RECORDS              = "Patient Records";
    public static final String NAV_PRIORITY_QUEUE       = "Priority Queue";
    public static final String NAV_OPD_CONSULTATION     = "OPD Consultation";
    public static final String NAV_OPD_IPD_WORKFLOW     = "OPD to IPD Workflow";
    public static final String NAV_EMERGENCY            = "Emergency Registration";
    public static final String NAV_ADMISSIONS           = "Admission Requests";
    public static final String NAV_BED_MANAGEMENT       = "Bed Management";
    public static final String NAV_DOCTORS              = "Doctor Management";
    public static final String NAV_HOSPITALS            = "Hospital Management";
    public static final String NAV_DISCHARGE            = "Patient Discharge";
    public static final String NAV_REFERRALS            = "Hospital Referrals";
    public static final String NAV_HISTORY              = "Patient History";
    public static final String NAV_REPORTS              = "Reports & Statistics";
    public static final String NAV_SETTINGS             = "Settings";
    public static final String NAV_LOGOUT               = "Logout";

    // Legacy alias compatibility
    public static final String NAV_DOCTORS_HOSPITALS    = NAV_DOCTORS;

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

    public SidebarPanel(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;

        setLayout(new BorderLayout());
        setBackground(UITheme.SIDEBAR_BG);
        setPreferredSize(new Dimension(250, 800));
        setMinimumSize(new Dimension(250, 600));
        setMaximumSize(new Dimension(250, Integer.MAX_VALUE));

        // Right subtle border dividing sidebar from center content
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.SIDEBAR_BORDER));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UITheme.SIDEBAR_BG);
        contentPanel.setBorder(new EmptyBorder(14, 10, 14, 10));

        for (String item : NAV_ITEMS) {
            // Group Section Headings
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

            NavItemButton btn = new NavItemButton(item, item.equals(currentSelected));
            btn.addActionListener(e -> selectItem(item));
            navButtons.add(btn);
            contentPanel.add(btn);
            contentPanel.add(Box.createVerticalStrut(2));
        }

        contentPanel.add(Box.createVerticalGlue());

        // Footer version info
        JPanel footer = new JPanel(new GridLayout(2, 1, 0, 2));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(12, 14, 6, 12));
        JLabel lblVer = new JLabel("HospitalOOP70 Healthcare");
        lblVer.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblVer.setForeground(UITheme.SIDEBAR_HEADING);
        JLabel lblSub = new JLabel("Government Administration v2.0");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSub.setForeground(new Color(0xA0, 0xB0, 0xC0));
        footer.add(lblVer);
        footer.add(lblSub);
        contentPanel.add(footer);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(UITheme.SIDEBAR_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JLabel createSectionHeader(String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(UITheme.SIDEBAR_HEADING);
        lbl.setBorder(new EmptyBorder(4, 14, 2, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    public void selectItem(String item) {
        selectItem(item, true);
    }

    public void selectItem(String item, boolean triggerCallback) {
        this.currentSelected = item;
        for (NavItemButton btn : navButtons) {
            btn.setSelected(btn.getItemName().equals(item));
        }
        if (triggerCallback && onNavigate != null) {
            onNavigate.accept(item);
        }
    }

    /**
     * Reusable navigation button with custom icon rendering, uniform height,
     * exact horizontal text alignment, and light active background styling.
     */
    private static class NavItemButton extends JButton {
        private final String itemName;
        private boolean isSelected;
        private boolean isHovered;

        private static final int BUTTON_HEIGHT = 36;
        private static final int ICON_X = 14;
        private static final int ICON_SIZE = 16;
        private static final int TEXT_X = 40;

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

            Dimension dim = new Dimension(230, BUTTON_HEIGHT);
            setPreferredSize(dim);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));
            setMinimumSize(new Dimension(215, BUTTON_HEIGHT));

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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Background & Indicator
            if (isSelected) {
                // Active light blue rounded background
                g2.setColor(UITheme.SIDEBAR_ACTIVE_BG);
                g2.fillRoundRect(4, 1, w - 8, h - 2, 8, 8);

                // Slim active indicator bar on left
                g2.setColor(UITheme.SIDEBAR_INDICATOR);
                g2.fillRoundRect(4, 6, 3, h - 12, 2, 2);
            } else if (isHovered) {
                // Subtle hover background
                g2.setColor(new Color(0xE2, 0xEE, 0xFA));
                g2.fillRoundRect(4, 1, w - 8, h - 2, 8, 8);
            }

            // Colors for Icon & Text
            Color iconColor = isSelected ? UITheme.SIDEBAR_INDICATOR : (isHovered ? UITheme.SIDEBAR_ACTIVE_TXT : UITheme.SIDEBAR_ICON);
            Color textColor = isSelected ? UITheme.SIDEBAR_ACTIVE_TXT : (isHovered ? UITheme.SIDEBAR_ACTIVE_TXT : UITheme.SIDEBAR_TEXT);

            // Draw Icon at exact fixed position
            int iconY = (h - ICON_SIZE) / 2;
            drawMenuIcon(g2, itemName, ICON_X, iconY, ICON_SIZE, iconColor);

            // Draw Text Label at exact fixed horizontal position
            g2.setColor(textColor);
            g2.setFont(new Font("Segoe UI", isSelected ? Font.BOLD : Font.PLAIN, 12));
            FontMetrics fm = g2.getFontMetrics();
            int textY = (h + fm.getAscent() - fm.getDescent()) / 2;

            // Clip text gracefully if window is narrow
            String label = itemName;
            int maxTextW = w - TEXT_X - 8;
            if (fm.stringWidth(label) > maxTextW) {
                while (label.length() > 3 && fm.stringWidth(label + "...") > maxTextW) {
                    label = label.substring(0, label.length() - 1);
                }
                label = label + "...";
            }
            g2.drawString(label, TEXT_X, textY);

            g2.dispose();
        }

        /**
         * Renders clean, consistent 16x16 vector healthcare icons for all navigation items.
         */
        private void drawMenuIcon(Graphics2D g2, String item, int x, int y, int size, Color c) {
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (item) {
                case NAV_DASHBOARD:
                    // 4-quadrant dashboard grid
                    g2.drawRoundRect(x + 1, y + 1, 5, 5, 2, 2);
                    g2.drawRoundRect(x + 8, y + 1, 5, 5, 2, 2);
                    g2.drawRoundRect(x + 1, y + 8, 5, 5, 2, 2);
                    g2.drawRoundRect(x + 8, y + 8, 5, 5, 2, 2);
                    break;

                case NAV_REGISTRATION:
                    // Person silhouette with plus
                    g2.drawOval(x + 3, y + 1, 6, 6); // head
                    g2.drawArc(x + 1, y + 8, 10, 7, 0, 180); // shoulders
                    g2.drawLine(x + 13, y + 4, x + 13, y + 8); // plus vertical
                    g2.drawLine(x + 11, y + 6, x + 15, y + 6); // plus horizontal
                    break;

                case NAV_RECORDS:
                    // Clipboard with document lines
                    g2.drawRoundRect(x + 2, y + 2, 11, 13, 2, 2);
                    g2.fillRect(x + 5, y + 1, 5, 2); // clip tab
                    g2.drawLine(x + 5, y + 6, x + 10, y + 6);
                    g2.drawLine(x + 5, y + 9, x + 10, y + 9);
                    g2.drawLine(x + 5, y + 12, x + 8, y + 12);
                    break;

                case NAV_PRIORITY_QUEUE:
                    // Ranked priority bars
                    g2.drawLine(x + 1, y + 3, x + 13, y + 3);
                    g2.drawLine(x + 1, y + 7, x + 10, y + 7);
                    g2.drawLine(x + 1, y + 11, x + 7, y + 11);
                    g2.fillOval(x + 12, y + 9, 3, 3);
                    break;

                case NAV_OPD_CONSULTATION:
                    // Stethoscope / clinical examination
                    g2.drawArc(x + 2, y + 2, 8, 8, 180, 180); // U tube
                    g2.drawLine(x + 2, y + 2, x + 2, y + 5);
                    g2.drawLine(x + 10, y + 2, x + 10, y + 5);
                    g2.drawLine(x + 6, y + 10, x + 6, y + 12);
                    g2.drawOval(x + 5, y + 12, 4, 3); // bell
                    break;

                case NAV_OPD_IPD_WORKFLOW:
                    // Workflow arrow progression
                    g2.drawLine(x + 1, y + 7, x + 13, y + 7);
                    g2.drawLine(x + 9, y + 3, x + 13, y + 7);
                    g2.drawLine(x + 9, y + 11, x + 13, y + 7);
                    g2.drawOval(x + 2, y + 11, 3, 3);
                    g2.drawOval(x + 9, y + 11, 3, 3);
                    break;

                case NAV_EMERGENCY:
                    // Emergency pulse / alert lightning
                    g2.drawLine(x + 8, y + 1, x + 4, y + 8);
                    g2.drawLine(x + 4, y + 8, x + 9, y + 8);
                    g2.drawLine(x + 9, y + 8, x + 6, y + 15);
                    break;

                case NAV_ADMISSIONS:
                    // Admission intake with arrow
                    g2.drawRoundRect(x + 2, y + 1, 10, 14, 2, 2);
                    g2.drawLine(x + 7, y + 5, x + 7, y + 11);
                    g2.drawLine(x + 4, y + 8, x + 7, y + 5);
                    g2.drawLine(x + 10, y + 8, x + 7, y + 5);
                    break;

                case NAV_BED_MANAGEMENT:
                    // Inpatient hospital bed
                    g2.drawLine(x + 1, y + 4, x + 1, y + 14); // headboard
                    g2.drawLine(x + 14, y + 8, x + 14, y + 14); // footboard
                    g2.drawLine(x + 1, y + 10, x + 14, y + 10); // mattress
                    g2.drawRoundRect(x + 3, y + 7, 3, 2, 1, 1); // pillow
                    break;

                case NAV_DOCTORS:
                    // Physician / stethoscope badge
                    g2.drawOval(x + 4, y + 1, 6, 6);
                    g2.drawArc(x + 1, y + 8, 12, 7, 0, 180);
                    g2.drawLine(x + 7, y + 10, x + 7, y + 14);
                    break;

                case NAV_HOSPITALS:
                    // Hospital facility building with cross
                    g2.drawRoundRect(x + 2, y + 4, 11, 11, 2, 2);
                    g2.drawLine(x + 7, y + 6, x + 7, y + 10); // cross V
                    g2.drawLine(x + 5, y + 8, x + 9, y + 8); // cross H
                    g2.drawLine(x + 6, y + 15, x + 6, y + 12); // door
                    g2.drawLine(x + 8, y + 15, x + 8, y + 12);
                    break;

                case NAV_DISCHARGE:
                    // Discharge clearance exit
                    g2.drawRect(x + 1, y + 2, 7, 12);
                    g2.drawLine(x + 6, y + 8, x + 14, y + 8); // exit arrow
                    g2.drawLine(x + 11, y + 5, x + 14, y + 8);
                    g2.drawLine(x + 11, y + 11, x + 14, y + 8);
                    break;

                case NAV_REFERRALS:
                    // Inter-hospital network nodes
                    g2.drawOval(x + 2, y + 3, 4, 4);
                    g2.drawOval(x + 10, y + 3, 4, 4);
                    g2.drawOval(x + 6, y + 10, 4, 4);
                    g2.drawLine(x + 5, y + 6, x + 7, y + 10);
                    g2.drawLine(x + 10, y + 6, x + 8, y + 10);
                    break;

                case NAV_HISTORY:
                    // Audit clock
                    g2.drawOval(x + 1, y + 1, 13, 13);
                    g2.drawLine(x + 7, y + 4, x + 7, y + 8);
                    g2.drawLine(x + 7, y + 8, x + 10, y + 8);
                    break;

                case NAV_REPORTS:
                    // Statistics bar chart
                    g2.drawLine(x + 1, y + 14, x + 14, y + 14); // axis
                    g2.fillRect(x + 3, y + 9, 2, 5);
                    g2.fillRect(x + 7, y + 5, 2, 9);
                    g2.fillRect(x + 11, y + 2, 2, 12);
                    break;

                case NAV_SETTINGS:
                    // Configuration sliders
                    g2.drawLine(x + 2, y + 4, x + 13, y + 4);
                    g2.drawOval(x + 4, y + 2, 3, 4);
                    g2.drawLine(x + 2, y + 8, x + 13, y + 8);
                    g2.drawOval(x + 9, y + 6, 3, 4);
                    g2.drawLine(x + 2, y + 12, x + 13, y + 12);
                    g2.drawOval(x + 5, y + 10, 3, 4);
                    break;

                case NAV_LOGOUT:
                    // Logout exit bracket
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
