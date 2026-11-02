package hms.ui.panels;

import hms.Patient;
import hms.ui.UITheme;
import hms.ui.components.*;
import hms.ui.demo.DemoDataStore;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Main Hospital Dashboard displaying real-time summary statistics,
 * quick action buttons, recent registrations, bed status overview, and activity feed.
 */
public class DashboardPanel extends JPanel {

    private final DemoDataStore store;
    private final Consumer<String> navigationConsumer;

    private final SummaryCard cardTotalPatients;
    private final SummaryCard cardWaitingPatients;
    private final SummaryCard cardAdmitted;
    private final SummaryCard cardGenBeds;
    private final SummaryCard cardIcuBeds;
    private final SummaryCard cardDischarges;

    private final DefaultTableModel recentTableModel;
    private final DefaultListModel<String> activityListModel;

    private final JLabel lblGenAvailableText;
    private final JLabel lblIcuAvailableText;
    private final JLabel lblPriorityCriticalText;

    public DashboardPanel(Consumer<String> navigationConsumer) {
        this.store = DemoDataStore.getInstance();
        this.navigationConsumer = navigationConsumer;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Top Section: Title & Quick Actions
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel titleGrp = new JPanel(new GridLayout(2, 1, 0, 2));
        titleGrp.setOpaque(false);
        JLabel lblHeading = new JLabel("HOSPITAL OPERATIONS DASHBOARD");
        lblHeading.setFont(UITheme.FONT_PAGE_TITLE);
        lblHeading.setForeground(UITheme.PRIMARY_NAVY);
        JLabel lblSub = new JLabel("Real-time clinical census, acute triage queues, and bed allocation telemetry");
        lblSub.setFont(UITheme.FONT_SUBTITLE);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        titleGrp.add(lblHeading);
        titleGrp.add(lblSub);

        JPanel quickActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        quickActions.setOpaque(false);

        StyledButton btnNewPatient = new StyledButton("+ Register Patient", StyledButton.Type.PRIMARY);
        btnNewPatient.addActionListener(e -> navigateTo(SidebarPanel.NAV_REGISTRATION));

        StyledButton btnBedMgmt = new StyledButton("Bed Management", StyledButton.Type.SECONDARY);
        btnBedMgmt.addActionListener(e -> navigateTo(SidebarPanel.NAV_BED_MANAGEMENT));

        StyledButton btnCallNext = new StyledButton("⚡ Call Next Patient", StyledButton.Type.SUCCESS);
        btnCallNext.addActionListener(e -> {
            Patient called = store.callNextPatient();
            if (called != null) {
                JOptionPane.showMessageDialog(this,
                    "NEXT PATIENT CALLED:\n\n" +
                    "ID: " + called.getPatientId() + "\n" +
                    "Name: " + called.getName() + "\n" +
                    "Severity: " + called.getPriority() + " (" + called.getEmergencyLevel() + ")\n" +
                    "Department: " + called.getDepartment() + "\n\n" +
                    "Status updated to: CALLED - CONSULTING",
                    "Patient Called for Encounter",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Priority Queue is currently empty. No patients waiting.",
                    "Priority Queue",
                    JOptionPane.WARNING_MESSAGE);
            }
        });

        quickActions.add(btnNewPatient);
        quickActions.add(btnBedMgmt);
        quickActions.add(btnCallNext);

        topBar.add(titleGrp, BorderLayout.WEST);
        topBar.add(quickActions, BorderLayout.EAST);

        // Center Content with ScrollPane
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setOpaque(false);

        scrollContent.add(topBar);
        scrollContent.add(Box.createVerticalStrut(16));

        // Metric Cards Grid (2x3 or 1x6)
        JPanel cardsGrid = new JPanel(new GridLayout(2, 3, 14, 14));
        cardsGrid.setOpaque(false);
        cardsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        cardTotalPatients   = new SummaryCard("Total Registered Patients", "0", "All time in-memory demo records", UITheme.PRIMARY_BLUE);
        cardWaitingPatients = new SummaryCard("Waiting Patients", "0", "Active in Priority Queue", UITheme.WARNING_AMBER);
        cardAdmitted        = new SummaryCard("Currently Admitted", "0", "Occupying Inpatient Beds", new Color(0x8B, 0x5C, 0xF6));
        cardGenBeds         = new SummaryCard("Available General Beds", "0", "General Wards & Specialty", UITheme.SUCCESS_GREEN);
        cardIcuBeds         = new SummaryCard("Available ICU Beds", "0", "Critical & Intensive Care", UITheme.ERROR_RED);
        cardDischarges      = new SummaryCard("Total Discharges", "0", "Completed patient stays", new Color(0x0E, 0xA5, 0xE9));

        cardsGrid.add(cardTotalPatients);
        cardsGrid.add(cardWaitingPatients);
        cardsGrid.add(cardAdmitted);
        cardsGrid.add(cardGenBeds);
        cardsGrid.add(cardIcuBeds);
        cardsGrid.add(cardDischarges);

        scrollContent.add(cardsGrid);
        scrollContent.add(Box.createVerticalStrut(18));

        // Middle Section: Left Table (Recent Patients) & Right Cards (Bed & Priority Overview)
        JPanel middleSplit = new JPanel(new GridLayout(1, 2, 16, 0));
        middleSplit.setOpaque(false);

        // Left Card: Recent Patient Registrations
        JPanel cardRecent = UITheme.createCardPanel();
        cardRecent.setLayout(new BorderLayout(0, 10));

        JPanel recentHead = new JPanel(new BorderLayout());
        recentHead.setOpaque(false);
        JLabel lblRecentTitle = UITheme.createSectionTitle("Recent Patient Registrations");
        StyledButton btnViewAll = new StyledButton("View All Records", StyledButton.Type.SECONDARY);
        btnViewAll.setPreferredSize(new Dimension(130, 28));
        btnViewAll.addActionListener(e -> navigateTo(SidebarPanel.NAV_RECORDS));
        recentHead.add(lblRecentTitle, BorderLayout.WEST);
        recentHead.add(btnViewAll, BorderLayout.EAST);
        cardRecent.add(recentHead, BorderLayout.NORTH);

        String[] cols = {"Patient ID", "Name", "Visit Type", "Severity", "Status"};
        recentTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        ModernTablePanel tablePanel = new ModernTablePanel(recentTableModel);
        tablePanel.applyStatusRendererToColumn(3);
        tablePanel.applyStatusRendererToColumn(4);
        cardRecent.add(tablePanel, BorderLayout.CENTER);

        middleSplit.add(cardRecent);

        // Right Card: Operational Overviews & Live Feed
        JPanel rightCol = new JPanel(new GridLayout(2, 1, 0, 14));
        rightCol.setOpaque(false);

        // Bed & Priority Mini Stats Card
        JPanel cardOverviews = UITheme.createCardPanel();
        cardOverviews.setLayout(new GridLayout(4, 1, 6, 6));
        JLabel lblOverviewTitle = UITheme.createSectionTitle("Resource & Queue Telemetry");
        lblOverviewTitle.setBorder(new EmptyBorder(0, 0, 4, 0));
        cardOverviews.add(lblOverviewTitle);

        lblGenAvailableText = new JLabel("• General Ward Capacity: Loading...");
        lblGenAvailableText.setFont(UITheme.FONT_REGULAR);
        lblGenAvailableText.setForeground(UITheme.TEXT_MAIN);

        lblIcuAvailableText = new JLabel("• ICU Critical Care: Loading...");
        lblIcuAvailableText.setFont(UITheme.FONT_REGULAR);
        lblIcuAvailableText.setForeground(UITheme.TEXT_MAIN);

        lblPriorityCriticalText = new JLabel("• Emergency Triage (Priority 1): Loading...");
        lblPriorityCriticalText.setFont(UITheme.FONT_REGULAR);
        lblPriorityCriticalText.setForeground(UITheme.ERROR_RED);

        cardOverviews.add(lblGenAvailableText);
        cardOverviews.add(lblIcuAvailableText);
        cardOverviews.add(lblPriorityCriticalText);

        rightCol.add(cardOverviews);

        // Recent Activity Card
        JPanel cardActivity = UITheme.createCardPanel();
        cardActivity.setLayout(new BorderLayout(0, 8));
        JLabel lblActTitle = UITheme.createSectionTitle("Recent Hospital Activity Log");
        cardActivity.add(lblActTitle, BorderLayout.NORTH);

        activityListModel = new DefaultListModel<>();
        JList<String> activityList = new JList<>(activityListModel);
        activityList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        activityList.setForeground(UITheme.TEXT_MAIN);
        activityList.setSelectionBackground(UITheme.INFO_BLUE_BG);
        activityList.setFixedCellHeight(26);

        JScrollPane actScroll = new JScrollPane(activityList);
        actScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        cardActivity.add(actScroll, BorderLayout.CENTER);

        rightCol.add(cardActivity);

        middleSplit.add(rightCol);

        scrollContent.add(middleSplit);

        JScrollPane mainScroll = new JScrollPane(scrollContent);
        mainScroll.setBorder(null);
        mainScroll.getViewport().setBackground(UITheme.LIGHT_BG);
        mainScroll.getVerticalScrollBar().setUnitIncrement(14);
        add(mainScroll, BorderLayout.CENTER);

        // Subscribe to reactive store changes
        store.addChangeListener(this::refreshData);

        refreshData();
    }

    private void navigateTo(String page) {
        if (navigationConsumer != null) {
            navigationConsumer.accept(page);
        }
    }

    public void refreshData() {
        cardTotalPatients.setValue(String.valueOf(store.getTotalPatientsCount()));
        cardWaitingPatients.setValue(String.valueOf(store.getWaitingPatientsCount()));
        cardAdmitted.setValue(String.valueOf(store.getAdmittedPatientsCount()));
        cardGenBeds.setValue(String.valueOf(store.getAvailableGeneralBedsCount()));
        cardIcuBeds.setValue(String.valueOf(store.getAvailableIcuBedsCount()));
        cardDischarges.setValue(String.valueOf(store.getTotalDischargesCount()));

        lblGenAvailableText.setText("• General Beds: " + store.getAvailableGeneralBedsCount() + " available for standard admission");
        lblIcuAvailableText.setText("• ICU Beds: " + store.getAvailableIcuBedsCount() + " available for critical emergency admissions");
        lblPriorityCriticalText.setText("• Priority Queue: " + store.getWaitingPatientsCount() + " patients waiting across departments");

        // Refresh recent table
        recentTableModel.setRowCount(0);
        List<Patient> patients = store.getPatients();
        int count = Math.min(8, patients.size());
        for (int i = 0; i < count; i++) {
            Patient p = patients.get(i);
            recentTableModel.addRow(new Object[]{
                p.getPatientId(),
                p.getName(),
                p.getVisitType(),
                p.getPriority() + " (" + p.getEmergencyLevel() + ")",
                p.getStatus()
            });
        }

        // Refresh activity list
        activityListModel.clear();
        List<DemoDataStore.HistoryRecord> logs = store.getActivityHistory();
        int logCount = Math.min(10, logs.size());
        for (int i = 0; i < logCount; i++) {
            DemoDataStore.HistoryRecord r = logs.get(i);
            activityListModel.addElement("[" + r.getTimestamp().substring(11) + "] " + r.getActivityType() + ": " + r.getDescription());
        }
    }
}
