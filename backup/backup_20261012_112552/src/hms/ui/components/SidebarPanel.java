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
 * Dark Navy Sidebar Navigation Panel housing the 13 navigation items in exact required order.
 */
public class SidebarPanel extends JPanel {

    public static final String NAV_DASHBOARD       = "Dashboard";
    public static final String NAV_REGISTRATION     = "Patient Registration";
    public static final String NAV_RECORDS          = "Patient Records";
    public static final String NAV_PRIORITY_QUEUE   = "Priority Queue";
    public static final String NAV_OPD_CONSULTATION = "OPD Consultation";
    public static final String NAV_ADMISSIONS       = "Admission Requests";
    public static final String NAV_BED_MANAGEMENT   = "Bed Management";
    public static final String NAV_DISCHARGE        = "Patient Discharge";
    public static final String NAV_REFERRALS        = "Hospital Referrals";
    public static final String NAV_DOCTORS_HOSPITALS= "Doctors and Hospitals";
    public static final String NAV_HISTORY          = "Patient History";
    public static final String NAV_REPORTS          = "Reports";
    public static final String NAV_LOGOUT           = "Logout";

    public static final String[] NAV_ITEMS = {
        NAV_DASHBOARD,
        NAV_REGISTRATION,
        NAV_RECORDS,
        NAV_PRIORITY_QUEUE,
        NAV_OPD_CONSULTATION,
        NAV_ADMISSIONS,
        NAV_BED_MANAGEMENT,
        NAV_DISCHARGE,
        NAV_REFERRALS,
        NAV_DOCTORS_HOSPITALS,
        NAV_HISTORY,
        NAV_REPORTS,
        NAV_LOGOUT
    };

    private static final String[] NAV_ICONS = {
        "📊", "📝", "📋", "⚡", "🩺", "🛏️", "🏨", "🚪", "🚑", "👨‍⚕️", "📜", "📈", "🚪"
    };

    private final List<NavItemButton> navButtons = new ArrayList<>();
    private final Consumer<String> onNavigate;
    private String currentSelected = NAV_DASHBOARD;

    public SidebarPanel(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;

        setLayout(new BorderLayout());
        setBackground(UITheme.PRIMARY_NAVY);
        setPreferredSize(new Dimension(250, 800));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UITheme.PRIMARY_NAVY);
        contentPanel.setBorder(new EmptyBorder(16, 12, 16, 12));

        // Section Title
        JLabel lblNav = new JLabel("NAVIGATION MENU");
        lblNav.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblNav.setForeground(new Color(0x64, 0x74, 0x8B));
        lblNav.setBorder(new EmptyBorder(0, 10, 10, 0));
        contentPanel.add(lblNav);

        for (int i = 0; i < NAV_ITEMS.length; i++) {
            String item = NAV_ITEMS[i];
            String icon = NAV_ICONS[i];
            NavItemButton btn = new NavItemButton(item, icon, item.equals(currentSelected));
            btn.addActionListener(e -> selectItem(item));
            navButtons.add(btn);
            contentPanel.add(btn);
            contentPanel.add(Box.createVerticalStrut(4));
        }

        contentPanel.add(Box.createVerticalGlue());

        // Footer version info
        JPanel footer = new JPanel(new GridLayout(2, 1));
        footer.setBackground(UITheme.PRIMARY_NAVY);
        footer.setBorder(new EmptyBorder(12, 10, 12, 10));
        JLabel lblVer = new JLabel("HospitalOOP70 v1.0");
        lblVer.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblVer.setForeground(new Color(0x94, 0xA3, 0xB8));
        JLabel lblSub = new JLabel("Government Hospital System");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSub.setForeground(new Color(0x64, 0x74, 0x8B));
        footer.add(lblVer);
        footer.add(lblSub);
        contentPanel.add(footer);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(UITheme.PRIMARY_NAVY);
        add(scrollPane, BorderLayout.CENTER);
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

    private static class NavItemButton extends JButton {
        private final String itemName;
        private final String iconText;
        private boolean isSelected;
        private boolean isHovered;

        public NavItemButton(String itemName, String iconText, boolean isSelected) {
            this.itemName = itemName;
            this.iconText = iconText;
            this.isSelected = isSelected;

            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(230, 40));
            setPreferredSize(new Dimension(225, 38));

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

        public String getItemName() { return itemName; }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isSelected) {
                g2.setColor(UITheme.PRIMARY_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            } else if (isHovered) {
                g2.setColor(UITheme.NAVY_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            }

            // Draw icon & label
            g2.setColor(isSelected ? Color.WHITE : (isHovered ? Color.WHITE : new Color(0xCB, 0xD5, 0xE1)));
            g2.setFont(getFont());

            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

            g2.drawString(iconText, 14, y);
            g2.drawString(itemName, 40, y);

            // Active bar accent if selected
            if (isSelected) {
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(getWidth() - 4, 8, 3, getHeight() - 16, 2, 2);
            }

            g2.dispose();
        }
    }
}
