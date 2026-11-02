package hms.ui;

import hms.security.UserSession;
import hms.ui.components.HeaderPanel;
import hms.ui.components.SidebarPanel;
import hms.ui.panels.*;
import java.awt.*;
import javax.swing.*;

/**
 * Main application window for the Government Hospital Management System.
 * Hosts the top HeaderPanel, left SidebarPanel, and a CardLayout center content container
 * allowing fluid switching across all clinical & administrative screens.
 * Enforces Role-Based Access Control (RBAC) permissions.
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel cardsPanel;
    private final SidebarPanel sidebarPanel;
    private final HeaderPanel headerPanel;
    private final LoginFrame loginFrame;

    // References to panels for programmatic navigation
    private final DashboardPanel dashboardPanel;
    private final PatientRegistrationPanel registrationPanel;
    private final PatientRecordsPanel recordsPanel;
    private final PriorityQueuePanel priorityQueuePanel;
    private final OPDConsultationPanel opdPanel;
    private final OPDToIPDWorkflowPanel workflowPanel;
    private final EmergencyRegistrationPanel emergencyPanel;
    private final AdmissionPanel admissionPanel;
    private final BedManagementPanel bedMgmtPanel;
    private final DoctorManagementPanel doctorPanel;
    private final HospitalManagementPanel hospitalPanel;
    private final DischargePanel dischargePanel;
    private final ReferralPanel referralPanel;
    private final PatientHistoryPanel historyPanel;
    private final ReportsPanel reportsPanel;
    private final SettingsPanel settingsPanel;

    public MainFrame(String userRole, LoginFrame loginFrame) {
        this.loginFrame = loginFrame;

        setTitle("GOVERNMENT HOSPITAL MANAGEMENT SYSTEM");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Responsive initial sizing
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (screenSize.width <= 1440 || screenSize.height <= 820) {
            setSize(Math.min(1360, screenSize.width - 20), Math.min(730, screenSize.height - 40));
        } else {
            setSize(1440, 900);
        }
        setMinimumSize(new Dimension(1024, 620));
        setLocationRelativeTo(null); // Center window on screen

        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.LIGHT_BG);

        // Center Content Container using CardLayout
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(UITheme.LIGHT_BG);

        // Initialize all Panels
        dashboardPanel      = new DashboardPanel(this::navigateToPage);
        registrationPanel   = new PatientRegistrationPanel(this::navigateToPage);
        recordsPanel        = new PatientRecordsPanel();
        priorityQueuePanel  = new PriorityQueuePanel();
        opdPanel            = new OPDConsultationPanel(this::navigateToPage);
        workflowPanel       = new OPDToIPDWorkflowPanel(this::navigateToPage);
        emergencyPanel      = new EmergencyRegistrationPanel(this::navigateToPage);
        admissionPanel      = new AdmissionPanel();
        bedMgmtPanel        = new BedManagementPanel();
        doctorPanel         = new DoctorManagementPanel();
        hospitalPanel       = new HospitalManagementPanel();
        dischargePanel      = new DischargePanel();
        referralPanel       = new ReferralPanel();
        historyPanel        = new PatientHistoryPanel();
        reportsPanel        = new ReportsPanel();
        settingsPanel       = new SettingsPanel();

        // Header Panel with search integration
        headerPanel = new HeaderPanel(userRole, this::confirmAndLogout, this::handleGlobalSearch);
        add(headerPanel, BorderLayout.NORTH);

        // Left Sidebar Navigation
        sidebarPanel = new SidebarPanel(this::navigateToPage);
        add(sidebarPanel, BorderLayout.WEST);

        // Add to CardLayout with exact navigation keys
        cardsPanel.add(dashboardPanel,    SidebarPanel.NAV_DASHBOARD);
        cardsPanel.add(registrationPanel, SidebarPanel.NAV_REGISTRATION);
        cardsPanel.add(recordsPanel,       SidebarPanel.NAV_RECORDS);
        cardsPanel.add(priorityQueuePanel, SidebarPanel.NAV_PRIORITY_QUEUE);
        cardsPanel.add(opdPanel,           SidebarPanel.NAV_OPD_CONSULTATION);
        cardsPanel.add(workflowPanel,      SidebarPanel.NAV_OPD_IPD_WORKFLOW);
        cardsPanel.add(emergencyPanel,     SidebarPanel.NAV_EMERGENCY);
        cardsPanel.add(admissionPanel,     SidebarPanel.NAV_ADMISSIONS);
        cardsPanel.add(bedMgmtPanel,       SidebarPanel.NAV_BED_MANAGEMENT);
        cardsPanel.add(doctorPanel,        SidebarPanel.NAV_DOCTORS);
        cardsPanel.add(hospitalPanel,      SidebarPanel.NAV_HOSPITALS);
        cardsPanel.add(dischargePanel,     SidebarPanel.NAV_DISCHARGE);
        cardsPanel.add(referralPanel,      SidebarPanel.NAV_REFERRALS);
        cardsPanel.add(historyPanel,       SidebarPanel.NAV_HISTORY);
        cardsPanel.add(reportsPanel,       SidebarPanel.NAV_REPORTS);
        cardsPanel.add(settingsPanel,      SidebarPanel.NAV_SETTINGS);

        add(cardsPanel, BorderLayout.CENTER);

        // Default to Dashboard
        navigateToPage(SidebarPanel.NAV_DASHBOARD);
    }

    private void handleGlobalSearch(String query) {
        navigateToPage(SidebarPanel.NAV_RECORDS);
        if (recordsPanel != null) {
            recordsPanel.searchByQuery(query);
        }
    }

    public void navigateToPage(String pageName) {
        if (SidebarPanel.NAV_LOGOUT.equals(pageName)) {
            confirmAndLogout();
            return;
        }

        // Check RBAC permission for this screen
        UserSession session = UserSession.getInstance();
        String screenKey = mapPageNameToKey(pageName);
        if (!session.canAccessScreen(screenKey)) {
            JOptionPane.showMessageDialog(this,
                "Access Restricted:\n\nYour current active role [" + session.getRole() + "] does not have permission\n" +
                "to access the '" + pageName + "' module.\n\n" +
                "Please log in with an authorized role account (e.g. Administrator, Doctor, or Staff).",
                "RBAC Access Restriction",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Show the corresponding card
        cardLayout.show(cardsPanel, pageName);
        sidebarPanel.selectItem(pageName, false);

        // Trigger panel specific refreshes if needed
        if (SidebarPanel.NAV_DASHBOARD.equals(pageName)) {
            dashboardPanel.refreshData();
        } else if (SidebarPanel.NAV_RECORDS.equals(pageName)) {
            recordsPanel.applyFilter();
        } else if (SidebarPanel.NAV_PRIORITY_QUEUE.equals(pageName)) {
            priorityQueuePanel.refreshQueue();
        } else if (SidebarPanel.NAV_OPD_CONSULTATION.equals(pageName)) {
            opdPanel.refreshData();
        } else if (SidebarPanel.NAV_OPD_IPD_WORKFLOW.equals(pageName)) {
            workflowPanel.refreshData();
        } else if (SidebarPanel.NAV_ADMISSIONS.equals(pageName)) {
            admissionPanel.refreshData();
        } else if (SidebarPanel.NAV_BED_MANAGEMENT.equals(pageName)) {
            bedMgmtPanel.applyFilter();
        } else if (SidebarPanel.NAV_DOCTORS.equals(pageName)) {
            doctorPanel.refreshDoctors();
        } else if (SidebarPanel.NAV_HOSPITALS.equals(pageName)) {
            hospitalPanel.refreshHospitals();
        } else if (SidebarPanel.NAV_DISCHARGE.equals(pageName)) {
            dischargePanel.refreshData();
        } else if (SidebarPanel.NAV_REFERRALS.equals(pageName)) {
            referralPanel.refreshData();
        } else if (SidebarPanel.NAV_HISTORY.equals(pageName)) {
            historyPanel.applyFilter();
        } else if (SidebarPanel.NAV_REPORTS.equals(pageName)) {
            reportsPanel.refreshReports();
        }
    }

    private String mapPageNameToKey(String pageName) {
        if (SidebarPanel.NAV_REGISTRATION.equals(pageName)) return "REGISTRATION";
        if (SidebarPanel.NAV_EMERGENCY.equals(pageName)) return "EMERGENCY";
        if (SidebarPanel.NAV_RECORDS.equals(pageName)) return "RECORDS";
        if (SidebarPanel.NAV_PRIORITY_QUEUE.equals(pageName)) return "PRIORITY_QUEUE";
        if (SidebarPanel.NAV_OPD_CONSULTATION.equals(pageName)) return "OPD_CONSULTATION";
        if (SidebarPanel.NAV_OPD_IPD_WORKFLOW.equals(pageName)) return "OPD_IPD_WORKFLOW";
        if (SidebarPanel.NAV_ADMISSIONS.equals(pageName)) return "ADMISSIONS";
        if (SidebarPanel.NAV_BED_MANAGEMENT.equals(pageName)) return "BED_MANAGEMENT";
        if (SidebarPanel.NAV_DOCTORS.equals(pageName)) return "DOCTORS";
        if (SidebarPanel.NAV_HOSPITALS.equals(pageName)) return "HOSPITALS";
        if (SidebarPanel.NAV_DISCHARGE.equals(pageName)) return "DISCHARGE";
        if (SidebarPanel.NAV_REFERRALS.equals(pageName)) return "REFERRALS";
        if (SidebarPanel.NAV_HISTORY.equals(pageName)) return "HISTORY";
        if (SidebarPanel.NAV_REPORTS.equals(pageName)) return "REPORTS";
        if (SidebarPanel.NAV_SETTINGS.equals(pageName)) return "SETTINGS";
        return "DASHBOARD";
    }

    private void confirmAndLogout() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to log out from Government Hospital Management System?",
            "Confirm Sign Out",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            UserSession.getInstance().logout();
            dispose();
            if (loginFrame != null) {
                loginFrame.resetAndShow();
            } else {
                new LoginFrame().setVisible(true);
            }
        }
    }
}
