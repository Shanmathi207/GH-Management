package hms.ui.components;

import hms.ui.UITheme;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Top application header featuring dark navy styling, application title,
 * current role badge, live digital clock, and logout action.
 */
public class HeaderPanel extends JPanel {

    private final JLabel lblClock;
    private final JLabel lblRoleBadge;
    private final StyledButton btnLogout;
    private final Runnable logoutAction;

    public HeaderPanel(String role, Runnable logoutAction) {
        this.logoutAction = logoutAction;

        setLayout(new BorderLayout(15, 0));
        setBackground(UITheme.PRIMARY_NAVY);
        setBorder(new EmptyBorder(12, 24, 12, 24));
        setPreferredSize(new Dimension(1440, 65));

        // Left Branding
        JPanel leftBrand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftBrand.setOpaque(false);

        // Simple medical cross drawn in icon
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.fillRect(getWidth() / 2 - 3, 6, 6, getHeight() - 12);
                g2.fillRect(6, getHeight() / 2 - 3, getWidth() - 12, 6);
                g2.dispose();
            }
        };
        iconPanel.setPreferredSize(new Dimension(34, 34));
        iconPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("GOVERNMENT HOSPITAL MANAGEMENT SYSTEM");
        lblTitle.setFont(UITheme.FONT_HEADER_TITLE);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Central Health Administration • Hospital Resource & Bed Grid");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(0x94, 0xA3, 0xB8));

        titlePanel.add(lblTitle);
        titlePanel.add(lblSub);

        leftBrand.add(iconPanel);
        leftBrand.add(titlePanel);
        add(leftBrand, BorderLayout.WEST);

        // Right info: Role + Clock + Logout
        JPanel rightInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        rightInfo.setOpaque(false);

        lblRoleBadge = new JLabel(role != null ? role.toUpperCase() : "ADMINISTRATOR");
        lblRoleBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblRoleBadge.setForeground(Color.WHITE);
        lblRoleBadge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x3B, 0x82, 0xF6), 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));

        lblClock = new JLabel();
        lblClock.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblClock.setForeground(new Color(0xCB, 0xD5, 0xE1));
        updateTime();

        // 1-second clock timer
        Timer timer = new Timer(1000, e -> updateTime());
        timer.start();

        btnLogout = new StyledButton("Logout", StyledButton.Type.DANGER);
        btnLogout.setPreferredSize(new Dimension(95, 32));
        btnLogout.addActionListener(e -> {
            if (this.logoutAction != null) {
                this.logoutAction.run();
            }
        });

        // Database indicator badge
        boolean dbConnected = hms.db.DBConnection.isConnected();
        JLabel lblDb = new JLabel(dbConnected ? "● DB ONLINE" : "○ DB OFFLINE");
        lblDb.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblDb.setForeground(dbConnected ? UITheme.SUCCESS_GREEN : new Color(0xF5, 0x9E, 0x0B));
        lblDb.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(dbConnected ? UITheme.SUCCESS_GREEN : new Color(0xF5, 0x9E, 0x0B), 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        rightInfo.add(lblDb);
        rightInfo.add(lblRoleBadge);
        rightInfo.add(lblClock);
        rightInfo.add(btnLogout);
        add(rightInfo, BorderLayout.EAST);
    }

    private void updateTime() {
        lblClock.setText("🕒 " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy • HH:mm:ss")));
    }

    public void setUserRole(String role) {
        lblRoleBadge.setText(role != null ? role.toUpperCase() : "ADMINISTRATOR");
    }
}
