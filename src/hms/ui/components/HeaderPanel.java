package hms.ui.components;

import hms.security.UserSession;
import hms.ui.UITheme;
import hms.ui.demo.DemoDataStore;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Top application header designed in a clean, light healthcare theme (#FFFFFF background,
 * subtle #DCE6F0 bottom border, deep slate branding, global search, alerts, user profile, and clock).
 */
public class HeaderPanel extends JPanel {

    private final JLabel lblClock;
    private final JLabel lblRoleBadge;
    private final JLabel lblDb;
    private final JLabel lblNotifications;
    private final JTextField txtGlobalSearch;
    private final StyledButton btnLogout;
    private final Runnable logoutAction;
    private final Consumer<String> searchAction;

    public HeaderPanel(String role, Runnable logoutAction) {
        this(role, logoutAction, null);
    }

    public HeaderPanel(String role, Runnable logoutAction, Consumer<String> searchAction) {
        this.logoutAction = logoutAction;
        this.searchAction = searchAction;

        setLayout(new BorderLayout(16, 0));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.SIDEBAR_BORDER),
            new EmptyBorder(10, 20, 10, 20)
        ));
        setPreferredSize(new Dimension(1100, 72));
        setMinimumSize(new Dimension(850, 70));

        // ================= LEFT BRANDING =================
        JPanel leftBrand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftBrand.setOpaque(false);

        // Medical Cross Hospital Logo (38x38 rounded container)
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                int w = getWidth();
                int h = getHeight();
                int barWidth = 6;
                int barLength = 20;
                // Vertical bar
                g2.fillRect((w - barWidth) / 2, (h - barLength) / 2, barWidth, barLength);
                // Horizontal bar
                g2.fillRect((h - barLength) / 2, (w - barWidth) / 2, barLength, barWidth);
                g2.dispose();
            }
        };
        iconPanel.setPreferredSize(new Dimension(38, 38));
        iconPanel.setOpaque(false);

        // Title and Subtitle Block
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("GOVERNMENT HOSPITAL MANAGEMENT SYSTEM");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(UITheme.TEXT_MAIN);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("Central Health Administration • Clinical Operations Network");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(UITheme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(lblSub);

        leftBrand.add(iconPanel);
        leftBrand.add(titlePanel);
        add(leftBrand, BorderLayout.WEST);

        // ================= CENTER / RIGHT TOOLBAR =================
        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        rightBar.setOpaque(false);

        // Global Search input
        JPanel searchBox = new JPanel(new BorderLayout(4, 0));
        searchBox.setOpaque(false);
        txtGlobalSearch = new JTextField(12);
        txtGlobalSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGlobalSearch.setForeground(UITheme.TEXT_MAIN);
        txtGlobalSearch.setBackground(UITheme.LIGHT_BG);
        txtGlobalSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.SIDEBAR_BORDER, 1, true),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        txtGlobalSearch.setToolTipText("Global search patient by ID or Name (Press Enter)");
        txtGlobalSearch.addActionListener(e -> triggerSearch());

        StyledButton btnSearch = new StyledButton("Search", StyledButton.Type.PRIMARY);
        btnSearch.setPreferredSize(new Dimension(75, 30));
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnSearch.addActionListener(e -> triggerSearch());

        searchBox.add(txtGlobalSearch, BorderLayout.CENTER);
        searchBox.add(btnSearch, BorderLayout.EAST);
        rightBar.add(searchBox);

        // Notification alert pill
        lblNotifications = new JLabel("Alerts: 0");
        lblNotifications.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblNotifications.setForeground(new Color(0x92, 0x40, 0x0E));
        lblNotifications.setBackground(new Color(0xFE, 0xF3, 0xC7));
        lblNotifications.setOpaque(true);
        lblNotifications.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xF5, 0x9E, 0x0B), 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        lblNotifications.setToolTipText("Active triage alerts and pending admission requests");
        rightBar.add(lblNotifications);

        // Database status badge
        boolean dbConnected = hms.db.DBConnection.isConnected();
        lblDb = new JLabel(dbConnected ? "DB ONLINE" : "DB OFFLINE");
        lblDb.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblDb.setForeground(dbConnected ? new Color(0x16, 0x65, 0x34) : new Color(0x92, 0x40, 0x0E));
        lblDb.setBackground(dbConnected ? new Color(0xDC, 0xFC, 0xE7) : new Color(0xFE, 0xF3, 0xC7));
        lblDb.setOpaque(true);
        lblDb.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(dbConnected ? UITheme.SUCCESS_GREEN : new Color(0xF5, 0x9E, 0x0B), 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        rightBar.add(lblDb);

        // User profile pill (avatar circle + role)
        JPanel userProfilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        userProfilePanel.setOpaque(false);

        JPanel avatarCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY_BLUE);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                String init = (role != null && !role.isEmpty()) ? role.substring(0, 1).toUpperCase() : "A";
                g2.drawString(init, (getWidth() - fm.stringWidth(init)) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatarCircle.setPreferredSize(new Dimension(26, 26));
        avatarCircle.setOpaque(false);

        lblRoleBadge = new JLabel(role != null ? role.toUpperCase() : "ADMINISTRATOR");
        lblRoleBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblRoleBadge.setForeground(UITheme.TEXT_MAIN);

        userProfilePanel.add(avatarCircle);
        userProfilePanel.add(lblRoleBadge);
        rightBar.add(userProfilePanel);

        // Live Clock
        lblClock = new JLabel();
        lblClock.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblClock.setForeground(UITheme.TEXT_MUTED);
        updateTime();

        Timer timer = new Timer(1000, e -> {
            updateTime();
            updateAlertCount();
        });
        timer.start();
        rightBar.add(lblClock);

        // Logout Button
        btnLogout = new StyledButton("Logout", StyledButton.Type.DANGER);
        btnLogout.setPreferredSize(new Dimension(80, 30));
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnLogout.addActionListener(e -> {
            if (this.logoutAction != null) {
                this.logoutAction.run();
            }
        });
        rightBar.add(btnLogout);

        add(rightBar, BorderLayout.EAST);
        updateAlertCount();
    }

    private void triggerSearch() {
        String q = txtGlobalSearch.getText().trim();
        if (!q.isEmpty() && searchAction != null) {
            searchAction.accept(q);
        }
    }

    private void updateTime() {
        lblClock.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy  HH:mm:ss")));
    }

    private void updateAlertCount() {
        try {
            DemoDataStore store = DemoDataStore.getInstance();
            int pendingAdm = 0;
            for (hms.AdmissionRequest ar : store.getAdmissionRequests()) {
                if ("PENDING".equalsIgnoreCase(ar.getStatus())) pendingAdm++;
            }
            int critical = 0;
            for (hms.Patient p : store.getPriorityQueue()) {
                if (p.getPriority() <= 2) critical++;
            }
            int total = pendingAdm + critical;
            lblNotifications.setText("Alerts: " + total);
        } catch (Exception ignored) {
        }
    }

    public void setUserRole(String role) {
        lblRoleBadge.setText(role != null ? role.toUpperCase() : "ADMINISTRATOR");
    }
}
