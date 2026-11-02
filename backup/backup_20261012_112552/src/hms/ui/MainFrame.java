package hms.ui;

import hms.ui.components.HeaderPanel;
import hms.ui.components.SidebarPanel;
import hms.ui.panels.*;
import java.awt.*;
import javax.swing.*;

/**
 * Main application window for the Government Hospital Management System.
 * Hosts the top HeaderPanel, left SidebarPanel, and a CardLayout center content container
 * allowing fluid switching across all 12 clinical & administrative panels.
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
    private final AdmissionPanel admissionPanel;
    private final BedManagementPanel bedMgmtPanel;
    private final DischargePanel dischargePanel;
    private final ReferralPanel referralPanel;
    private final DoctorsHospitalsPanel doctorsHospitalsPanel;
    private final PatientHistoryPanel historyPanel;
    private final ReportsPanel reportsPanel;

    public MainFrame(String userRole, LoginFrame loginFrame) {
        this.loginFrame = loginFrame;

        setTitle("GOVERNMENT HOSPITAL MANAGEMENT SYSTEM");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1440, 900);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null); // Center window on screen

        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.LIGHT_BG);

        // Header Panel
        headerPanel = new HeaderPanel(userRole, this::confirmAndLogout);
        add(headerPanel, BorderLayout.NORTH);

        // Left Sidebar Navigation
        sidebarPanel = new SidebarPanel(this::navigateToPage);
        add(sidebarPanel, BorderLayout.WEST);

        // Center Content Container using CardLayout
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(UITheme.LIGHT_BG);

        // Initialize all 12 Panels
        dashboardPanel          = new DashboardPanel(this::navigateToPage);
        registrationPanel       = new PatientRegistrationPanel(this::navigateToPage);
        recordsPanel            = new PatientRecordsPanel();
        priorityQueuePanel      = new PriorityQueuePanel();
        opdPanel                = new OPDConsultationPanel();
        admissionPanel          = new AdmissionPanel();
        bedMgmtPanel            = new BedManagementPanel();
        dischargePanel          = new DischargePanel();
        referralPanel           = new ReferralPanel();
        doctorsHospitalsPanel   = new DoctorsHospitalsPanel();
        historyPanel            = new PatientHistoryPanel();
        reportsPanel            = new ReportsPanel();

        // Add to CardLayout with exact navigation keys
        cardsPanel.add(dashboardPanel, SidebarPanel.NAV_DASHBOARD);
        cardsPanel.add(registrationPanel, SidebarPanel.NAV_REGISTRATION);
        cardsPanel.add(recordsPanel, SidebarPanel.NAV_RECORDS);
        cardsPanel.add(priorityQueuePanel, SidebarPanel.NAV_PRIORITY_QUEUE);
        cardsPanel.add(opdPanel, SidebarPanel.NAV_OPD_CONSULTATION);
        cardsPanel.add(admissionPanel, SidebarPanel.NAV_ADMISSIONS);
        cardsPanel.add(bedMgmtPanel, SidebarPanel.NAV_BED_MANAGEMENT);
        cardsPanel.add(dischargePanel, SidebarPanel.NAV_DISCHARGE);
        cardsPanel.add(referralPanel, SidebarPanel.NAV_REFERRALS);
        cardsPanel.add(doctorsHospitalsPanel, SidebarPanel.NAV_DOCTORS_HOSPITALS);
        cardsPanel.add(historyPanel, SidebarPanel.NAV_HISTORY);
        cardsPanel.add(reportsPanel, SidebarPanel.NAV_REPORTS);

        add(cardsPanel, BorderLayout.CENTER);

        // Default to Dashboard
        navigateToPage(SidebarPanel.NAV_DASHBOARD);
    }

    public void navigateToPage(String pageName) {
        if (SidebarPanel.NAV_LOGOUT.equals(pageName)) {
            confirmAndLogout();
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
        } else if (SidebarPanel.NAV_BED_MANAGEMENT.equals(pageName)) {
            bedMgmtPanel.applyFilter();
        } else if (SidebarPanel.NAV_DISCHARGE.equals(pageName)) {
            dischargePanel.refreshData();
        } else if (SidebarPanel.NAV_REFERRALS.equals(pageName)) {
            referralPanel.refreshData();
        } else if (SidebarPanel.NAV_DOCTORS_HOSPITALS.equals(pageName)) {
            doctorsHospitalsPanel.refreshAll();
        } else if (SidebarPanel.NAV_HISTORY.equals(pageName)) {
            historyPanel.applyFilter();
        } else if (SidebarPanel.NAV_REPORTS.equals(pageName)) {
            reportsPanel.refreshReports();
        }
    }

    private void confirmAndLogout() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to log out from Government Hospital Management System?",
            "Confirm Sign Out",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            if (loginFrame != null) {
                loginFrame.resetAndShow();
            } else {
                new LoginFrame().setVisible(true);
            }
        }
    }
}
