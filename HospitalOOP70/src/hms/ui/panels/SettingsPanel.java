package hms.ui.panels;

import hms.dao.UserDAO;
import hms.db.DBConnection;
import hms.security.UserSession;
import hms.ui.UITheme;
import hms.ui.components.ModernTablePanel;
import hms.ui.components.StyledButton;
import java.awt.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Dedicated Settings and System Configuration Screen.
 * Provides interactive MySQL connectivity testing, session diagnostics,
 * runtime environment statistics, and administrator account management.
 */
public class SettingsPanel extends JPanel {

    private final JLabel lblDbStatus;
    private final JLabel lblDbDetails;
    private final DefaultTableModel userTableModel;
    private final ModernTablePanel userTablePanel;

    public SettingsPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header Title
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setOpaque(false);
        JLabel lblHeading = new JLabel("SYSTEM SETTINGS & DATABASE CONFIGURATION");
        lblHeading.setFont(UITheme.FONT_PAGE_TITLE);
        lblHeading.setForeground(UITheme.PRIMARY_NAVY);
        JLabel lblSub = new JLabel("Database connectivity status, active user session details, and runtime diagnostics");
        lblSub.setFont(UITheme.FONT_SUBTITLE);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        header.add(lblHeading);
        header.add(lblSub);
        add(header, BorderLayout.NORTH);

        // Center: Scrollable Container with settings cards
        JPanel contentCards = new JPanel();
        contentCards.setLayout(new BoxLayout(contentCards, BoxLayout.Y_AXIS));
        contentCards.setOpaque(false);

        // Card 1: Database Connectivity Status
        JPanel cardDb = UITheme.createCardPanel();
        cardDb.setLayout(new BorderLayout(0, 12));
        cardDb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        JLabel lblDbTitle = new JLabel("MySQL Database Connectivity");
        lblDbTitle.setFont(UITheme.FONT_CARD_TITLE);
        lblDbTitle.setForeground(UITheme.PRIMARY_NAVY);
        cardDb.add(lblDbTitle, BorderLayout.NORTH);

        JPanel dbBody = new JPanel(new GridLayout(2, 1, 0, 6));
        dbBody.setOpaque(false);

        boolean isConnected = DBConnection.isConnected();
        lblDbStatus = new JLabel(isConnected ? 
            "Connected to MySQL Database (Target: " + DBConnection.getDatabaseName() + ")" :
            "Running in Offline Demo Mode (Database Disconnected)");
        lblDbStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDbStatus.setForeground(isConnected ? UITheme.SUCCESS_GREEN : new Color(0xF5, 0x9E, 0x0B));

        lblDbDetails = new JLabel(String.format(
            "Host: %s | Port: %s | Database: %s | User: %s | Password: [From DB_PASSWORD env: %s]",
            DBConnection.getHost(), DBConnection.getPort(), DBConnection.getDatabaseName(),
            DBConnection.getUsername(), System.getenv("DB_PASSWORD") != null ? "CONFIGURED" : "NOT SET"
        ));
        lblDbDetails.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDbDetails.setForeground(UITheme.TEXT_MUTED);

        dbBody.add(lblDbStatus);
        dbBody.add(lblDbDetails);
        cardDb.add(dbBody, BorderLayout.CENTER);

        JPanel dbButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        dbButtons.setOpaque(false);

        StyledButton btnTestConn = new StyledButton("Test Connection", StyledButton.Type.PRIMARY);
        btnTestConn.setPreferredSize(new Dimension(160, 34));
        btnTestConn.addActionListener(e -> testDatabaseConnection());
        dbButtons.add(btnTestConn);

        cardDb.add(dbButtons, BorderLayout.SOUTH);
        contentCards.add(cardDb);
        contentCards.add(Box.createVerticalStrut(16));

        // Card 2: Active User Session
        JPanel cardSession = UITheme.createCardPanel();
        cardSession.setLayout(new BorderLayout(0, 12));
        cardSession.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        JLabel lblSessionTitle = new JLabel("Authenticated Session & RBAC Role");
        lblSessionTitle.setFont(UITheme.FONT_CARD_TITLE);
        lblSessionTitle.setForeground(UITheme.PRIMARY_NAVY);
        cardSession.add(lblSessionTitle, BorderLayout.NORTH);

        UserSession session = UserSession.getInstance();
        JPanel sessionBody = new JPanel(new GridLayout(2, 2, 10, 6));
        sessionBody.setOpaque(false);

        sessionBody.add(createDetailLabel("Logged-in User", session.getUsername() + " (" + session.getFullName() + ")"));
        sessionBody.add(createDetailLabel("Assigned Role", session.getRole().toUpperCase()));
        sessionBody.add(createDetailLabel("Login Timestamp", session.getLoginTimestamp()));
        sessionBody.add(createDetailLabel("Access Level", session.isAdmin() ? "Full System Administrator" : "Role-Restricted Clinical Operator"));

        cardSession.add(sessionBody, BorderLayout.CENTER);
        contentCards.add(cardSession);
        contentCards.add(Box.createVerticalStrut(16));

        // Card 3: Runtime Environment & Architecture
        JPanel cardSys = UITheme.createCardPanel();
        cardSys.setLayout(new BorderLayout(0, 12));
        cardSys.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JLabel lblSysTitle = new JLabel("Runtime Environment & Technical Architecture");
        lblSysTitle.setFont(UITheme.FONT_CARD_TITLE);
        lblSysTitle.setForeground(UITheme.PRIMARY_NAVY);
        cardSys.add(lblSysTitle, BorderLayout.NORTH);

        JPanel sysBody = new JPanel(new GridLayout(2, 2, 10, 6));
        sysBody.setOpaque(false);

        sysBody.add(createDetailLabel("Java Runtime", System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")"));
        sysBody.add(createDetailLabel("Operating System", System.getProperty("os.name") + " " + System.getProperty("os.arch")));
        sysBody.add(createDetailLabel("Architecture Pattern", "MVC + Service Layer + DAO + Java Swing"));
        sysBody.add(createDetailLabel("Algorithms Implemented", "PriorityQueue (Deterministic Triage), Dijkstra (Shortest Path)"));

        cardSys.add(sysBody, BorderLayout.CENTER);
        contentCards.add(cardSys);
        contentCards.add(Box.createVerticalStrut(16));

        // Card 4: RBAC User Accounts Table (Admin view)
        JPanel cardUsers = UITheme.createCardPanel();
        cardUsers.setLayout(new BorderLayout(0, 12));
        cardUsers.setPreferredSize(new Dimension(800, 240));

        JPanel userHeader = new JPanel(new BorderLayout());
        userHeader.setOpaque(false);
        JLabel lblUserTableTitle = new JLabel("Registered Application Users (RBAC Roster)");
        lblUserTableTitle.setFont(UITheme.FONT_CARD_TITLE);
        lblUserTableTitle.setForeground(UITheme.PRIMARY_NAVY);
        userHeader.add(lblUserTableTitle, BorderLayout.WEST);

        StyledButton btnAddUser = new StyledButton("Add User", StyledButton.Type.NAVY);
        btnAddUser.setPreferredSize(new Dimension(120, 30));
        btnAddUser.addActionListener(e -> showAddUserDialog());
        userHeader.add(btnAddUser, BorderLayout.EAST);
        cardUsers.add(userHeader, BorderLayout.NORTH);

        userTableModel = new DefaultTableModel(new String[]{"User ID", "Username", "Role", "Full Name", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        userTablePanel = new ModernTablePanel(userTableModel);
        userTablePanel.applyStatusRendererToColumn(4);
        cardUsers.add(userTablePanel, BorderLayout.CENTER);
        contentCards.add(cardUsers);

        JScrollPane mainScroll = new JScrollPane(contentCards);
        mainScroll.setBorder(null);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(mainScroll, BorderLayout.CENTER);

        refreshUsers();
    }

    private JPanel createDetailLabel(String label, String value) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(UITheme.TEXT_MUTED);
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        val.setForeground(UITheme.TEXT_DARK);
        p.add(lbl);
        p.add(val);
        return p;
    }

    private void testDatabaseConnection() {
        try {
            long start = System.currentTimeMillis();
            Connection conn = DBConnection.getConnection();
            long latency = System.currentTimeMillis() - start;

            DatabaseMetaData meta = conn.getMetaData();
            String dbProduct = meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion();

            lblDbStatus.setForeground(UITheme.SUCCESS_GREEN);
            lblDbStatus.setText("Connected to MySQL Database (Target: " + DBConnection.getDatabaseName() + ")");
            lblDbDetails.setText("Connected successfully to " + dbProduct + " (Latency: " + latency + " ms)");

            JOptionPane.showMessageDialog(this,
                "MySQL Connection Test Successful!\n\n" +
                "Database: " + DBConnection.getDatabaseName() + "\n" +
                "User: " + DBConnection.getUsername() + "\n" +
                "Server: " + dbProduct + "\n" +
                "Roundtrip Latency: " + latency + " ms\n" +
                "Persistence: Fully Active",
                "Database Online",
                JOptionPane.INFORMATION_MESSAGE);

            refreshUsers();
        } catch (Exception ex) {
            lblDbStatus.setForeground(UITheme.DANGER_RED);
            lblDbStatus.setText("Database Connection Failed");
            lblDbDetails.setText("Error: " + ex.getMessage());

            JOptionPane.showMessageDialog(this,
                "MySQL Connection Test Failed:\n\n" +
                ex.getMessage() + "\n\n" +
                "Troubleshooting Steps:\n" +
                "1. Ensure MySQL Server 8.0 is running on port 3306.\n" +
                "2. Ensure DB_PASSWORD environment variable is set.\n" +
                "3. Ensure database/schema.sql and seed.sql are imported.\n" +
                "4. Note: Application seamlessly operates in offline demo mode.",
                "Database Offline",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refreshUsers() {
        userTableModel.setRowCount(0);
        UserDAO dao = new UserDAO();
        java.util.List<UserDAO.UserRecord> list = dao.findAll();
        if (list.isEmpty()) {
            userTableModel.addRow(new Object[]{1, "admin", "Administrator", "Dr. System Administrator", "ACTIVE"});
            userTableModel.addRow(new Object[]{2, "doctor", "Doctor", "Dr. Rajesh Sharma (MD)", "ACTIVE"});
            userTableModel.addRow(new Object[]{3, "reception", "Receptionist", "Triage Intake Desk", "ACTIVE"});
            userTableModel.addRow(new Object[]{4, "staff", "Hospital Staff", "Staff Nurse / Ward Manager", "ACTIVE"});
        } else {
            for (UserDAO.UserRecord u : list) {
                userTableModel.addRow(new Object[]{
                    u.getUserId(),
                    u.getUsername(),
                    u.getRole(),
                    u.getFullName(),
                    "ACTIVE"
                });
            }
        }
    }

    private void showAddUserDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create Staff Account", true);
        dialog.setSize(400, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtNewUser = UITheme.createTextField(15);
        JPasswordField txtNewPass = new JPasswordField(15);
        JComboBox<String> cmbNewRole = UITheme.createComboBox(new String[]{"Administrator", "Doctor", "Receptionist", "Hospital Staff"});
        JTextField txtFullName = UITheme.createTextField(15);

        form.add(UITheme.createFormLabel("Username:"));
        form.add(txtNewUser);
        form.add(UITheme.createFormLabel("Password:"));
        form.add(txtNewPass);
        form.add(UITheme.createFormLabel("Role:"));
        form.add(cmbNewRole);
        form.add(UITheme.createFormLabel("Full Name:"));
        form.add(txtFullName);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        StyledButton btnSave = new StyledButton("Create User", StyledButton.Type.PRIMARY);
        btnSave.addActionListener(e -> {
            String u = txtNewUser.getText().trim();
            String p = new String(txtNewPass.getPassword()).trim();
            String r = (String) cmbNewRole.getSelectedItem();
            String fn = txtFullName.getText().trim();

            if (u.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Username and password cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            UserDAO dao = new UserDAO();
            boolean ok = dao.saveUser(u, p, r, fn.isEmpty() ? u : fn);
            if (ok) {
                dialog.dispose();
                refreshUsers();
                JOptionPane.showMessageDialog(this, "User account '" + u + "' created successfully.", "Account Created", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to save user. Verify database connection.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        StyledButton btnCancel = new StyledButton("Cancel", StyledButton.Type.SECONDARY);
        btnCancel.addActionListener(e -> dialog.dispose());

        btnBar.add(btnCancel);
        btnBar.add(btnSave);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnBar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
