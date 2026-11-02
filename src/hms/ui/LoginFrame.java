package hms.ui;

import hms.ui.components.StyledButton;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Modern, professional Desktop Login Frame for the Government Hospital Management System.
 * Implements demo-only authentication workflow with role selection and input validation.
 */
public class LoginFrame extends JFrame {

    private final JTextField txtUsername;
    private final JPasswordField txtPassword;
    private final JCheckBox chkShowPassword;
    private final JComboBox<String> cmbRole;
    private final JLabel lblErrorMessage;

    public LoginFrame() {
        setTitle("Government Hospital Management System - Authentication");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 620);
        setMinimumSize(new Dimension(850, 540));
        setLocationRelativeTo(null); // Center on screen
        getContentPane().setBackground(UITheme.LIGHT_BG);
        setLayout(new BorderLayout());

        // Split Layout: Left Decorative Hero Panel & Right Form Panel
        JPanel mainContainer = new JPanel(new GridLayout(1, 2));
        mainContainer.setOpaque(false);

        // ================= LEFT HERO PANEL =================
        JPanel leftHero = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Navy Gradient Background
                GradientPaint gp = new GradientPaint(
                    0, 0, UITheme.PRIMARY_NAVY,
                    getWidth(), getHeight(), new Color(0x0A, 0x19, 0x2F)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Decorative Medical Cross Emblem in Background
                g2.setColor(new Color(0x25, 0x63, 0xEB, 40));
                int cx = getWidth() / 2;
                int cy = getHeight() / 2 - 30;
                int crossSize = 180;
                int armWidth = 54;
                g2.fillRoundRect(cx - armWidth / 2, cy - crossSize / 2, armWidth, crossSize, 20, 20);
                g2.fillRoundRect(cx - crossSize / 2, cy - armWidth / 2, crossSize, armWidth, 20, 20);

                // Subtle grid accents
                g2.setColor(new Color(255, 255, 255, 12));
                for (int y = 0; y < getHeight(); y += 40) {
                    g2.drawLine(0, y, getWidth(), y);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };
        leftHero.setLayout(new BorderLayout());
        leftHero.setBorder(new EmptyBorder(48, 40, 48, 40));

        JPanel heroContent = new JPanel();
        heroContent.setLayout(new BoxLayout(heroContent, BoxLayout.Y_AXIS));
        heroContent.setOpaque(false);

        // Medical Badge Icon
        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(Color.WHITE);
                g2.fillRect(getWidth() / 2 - 4, 10, 8, getHeight() - 20);
                g2.fillRect(10, getHeight() / 2 - 4, getWidth() - 20, 8);
                g2.dispose();
            }
        };
        badge.setPreferredSize(new Dimension(54, 54));
        badge.setMaximumSize(new Dimension(54, 54));
        badge.setOpaque(false);
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblHeroTitle = new JLabel("<html>GOVERNMENT<br>HOSPITAL<br>MANAGEMENT SYSTEM</html>");
        lblHeroTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblHeroTitle.setForeground(Color.WHITE);
        lblHeroTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblHeroSub = new JLabel("<html>Patient Registration, Inpatient Admission,<br>Triage Queueing, and Inter-Hospital<br>Resource Coordination.</html>");
        lblHeroSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblHeroSub.setForeground(new Color(0x94, 0xA3, 0xB8));
        lblHeroSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblHeroBadge = new JLabel("SECURE DESKTOP APPLICATION");
        lblHeroBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblHeroBadge.setForeground(UITheme.SUCCESS_GREEN);
        lblHeroBadge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.SUCCESS_GREEN, 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        lblHeroBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

        heroContent.add(badge);
        heroContent.add(Box.createVerticalStrut(24));
        heroContent.add(lblHeroTitle);
        heroContent.add(Box.createVerticalStrut(16));
        heroContent.add(lblHeroSub);
        heroContent.add(Box.createVerticalStrut(24));
        heroContent.add(lblHeroBadge);
        heroContent.add(Box.createVerticalGlue());

        leftHero.add(heroContent, BorderLayout.CENTER);

        JLabel lblHeroFoot = new JLabel("District Healthcare Network • Version 1.0");
        lblHeroFoot.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHeroFoot.setForeground(new Color(0x64, 0x74, 0x8B));
        leftHero.add(lblHeroFoot, BorderLayout.SOUTH);

        mainContainer.add(leftHero);

        // ================= RIGHT LOGIN FORM PANEL =================
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(40, 48, 40, 48));

        JPanel formCard = new JPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setOpaque(false);
        formCard.setPreferredSize(new Dimension(380, 500));

        JLabel lblSign = new JLabel("Sign In to Portal");
        lblSign.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblSign.setForeground(UITheme.PRIMARY_NAVY);
        lblSign.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel("Enter your staff credentials to access clinical tools.");
        lblDesc.setFont(UITheme.FONT_SUBTITLE);
        lblDesc.setForeground(UITheme.TEXT_MUTED);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Form Fields
        JLabel lblRole = UITheme.createFormLabel("SELECT USER ROLE");
        lblRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        String[] roles = {"Administrator", "Doctor", "Receptionist", "Hospital Staff"};
        cmbRole = UITheme.createComboBox(roles);
        cmbRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbRole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JLabel lblUser = UITheme.createFormLabel("USERNAME");
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtUsername = UITheme.createTextField(20);
        txtUsername.setText("admin"); // Demo default for convenience
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JLabel lblPass = UITheme.createFormLabel("PASSWORD");
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtPassword = UITheme.createPasswordField(20);
        txtPassword.setText("hospital123"); // Demo default
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        chkShowPassword = new JCheckBox("Show Password");
        chkShowPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShowPassword.setForeground(UITheme.TEXT_MUTED);
        chkShowPassword.setOpaque(false);
        chkShowPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkShowPassword.addActionListener(e -> {
            if (chkShowPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('•');
            }
        });

        lblErrorMessage = new JLabel(" ");
        lblErrorMessage.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblErrorMessage.setForeground(UITheme.ERROR_RED);
        lblErrorMessage.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Buttons
        StyledButton btnLogin = new StyledButton("Login to System", StyledButton.Type.PRIMARY);
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnLogin.addActionListener(e -> handleLogin());

        StyledButton btnClear = new StyledButton("Clear Credentials", StyledButton.Type.SECONDARY);
        btnClear.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnClear.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnClear.addActionListener(e -> {
            txtUsername.setText("");
            txtPassword.setText("");
            lblErrorMessage.setText(" ");
        });

        // Database / Demo status box
        boolean dbConnected = hms.db.DBConnection.isConnected();
        JPanel demoHint = new JPanel(new GridLayout(2, 1, 0, 2));
        demoHint.setOpaque(true);
        demoHint.setBackground(dbConnected ? new Color(0xEC, 0xFD, 0xF5) : new Color(0xF8, 0xFA, 0xFC));
        demoHint.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(dbConnected ? UITheme.SUCCESS_GREEN : UITheme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        demoHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        demoHint.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel lblHint1 = new JLabel(dbConnected ? 
            "● MySQL Connected: hospital_oop_db" : 
            "○ Offline Demo Mode (Set DB_PASSWORD env for MySQL)");
        lblHint1.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblHint1.setForeground(dbConnected ? UITheme.SUCCESS_GREEN : UITheme.PRIMARY_BLUE);

        JLabel lblHint2 = new JLabel(dbConnected ? 
            "Authenticated securely via `users` table (admin / staff)" : 
            "Demo Credentials: admin / hospital123 or staff / hospital123");
        lblHint2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblHint2.setForeground(UITheme.TEXT_MUTED);
        demoHint.add(lblHint1);
        demoHint.add(lblHint2);

        JLabel lblFooter = new JLabel("Hospital Management System | Secure Clinical Portal");
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblFooter.setForeground(UITheme.TEXT_MUTED);
        lblFooter.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Assemble Form
        formCard.add(lblSign);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(lblDesc);
        formCard.add(Box.createVerticalStrut(20));
        formCard.add(lblRole);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(cmbRole);
        formCard.add(Box.createVerticalStrut(12));
        formCard.add(lblUser);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(txtUsername);
        formCard.add(Box.createVerticalStrut(12));
        formCard.add(lblPass);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(txtPassword);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(chkShowPassword);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(lblErrorMessage);
        formCard.add(Box.createVerticalStrut(14));
        formCard.add(btnLogin);
        formCard.add(Box.createVerticalStrut(8));
        formCard.add(btnClear);
        formCard.add(Box.createVerticalStrut(16));
        formCard.add(demoHint);
        formCard.add(Box.createVerticalGlue());
        formCard.add(lblFooter);

        rightPanel.add(formCard);
        mainContainer.add(rightPanel);

        add(mainContainer, BorderLayout.CENTER);

        // Enter key submits form
        txtUsername.addActionListener(e -> handleLogin());
        txtPassword.addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String role = (String) cmbRole.getSelectedItem();

        // Validations
        if (username.isEmpty()) {
            lblErrorMessage.setText("⚠ Please enter your username.");
            txtUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            lblErrorMessage.setText("⚠ Please enter your password.");
            txtPassword.requestFocus();
            return;
        }

        // Authentication Verification
        if (hms.db.DBConnection.isConnected()) {
            hms.dao.UserDAO userDAO = new hms.dao.UserDAO();
            boolean authenticated = userDAO.authenticate(username, password, role);
            if (!authenticated) {
                lblErrorMessage.setText("⚠ Invalid credentials or role mismatch for database account.");
                txtPassword.requestFocus();
                return;
            }
            hms.security.UserSession.getInstance().login(username, role, username);
        } else {
            // Standalone offline demo verification
            boolean validDemo = ("admin".equalsIgnoreCase(username) || "staff".equalsIgnoreCase(username) ||
                                 "doctor".equalsIgnoreCase(username) || "receptionist".equalsIgnoreCase(username)) 
                                 && "hospital123".equals(password);
            if (!validDemo && !password.isEmpty()) {
                lblErrorMessage.setText("⚠ Demo Login: Use admin / hospital123, doctor / hospital123, or staff / hospital123");
                txtPassword.requestFocus();
                return;
            }
            hms.security.UserSession.getInstance().login(username, role, username);
        }

        lblErrorMessage.setText(" ");

        // Open MainFrame
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame(role, this);
            mainFrame.setVisible(true);
            this.setVisible(false); // Hide login window
        });
    }

    public void resetAndShow() {
        txtPassword.setText("");
        lblErrorMessage.setText(" ");
        setVisible(true);
    }
}
