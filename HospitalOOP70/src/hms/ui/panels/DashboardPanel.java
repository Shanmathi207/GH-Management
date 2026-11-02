package hms.ui.panels;

import hms.Bed;
import hms.Doctor;
import hms.Patient;
import hms.ui.UITheme;
import hms.ui.components.*;
import hms.ui.demo.DemoDataStore;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Main Hospital Operations Dashboard displaying all 8 core telemetry statistics:
 * Total Patients, OPD Patients, IPD Patients, Available Beds, Occupied Beds,
 * Doctors, Pending Admissions, and Hospital Referrals, along with operational charts,
 * quick clinical actions, recent registrations, and activity feed.
 */
public class DashboardPanel extends JPanel {

    private final DemoDataStore store;
    private final Consumer<String> navigationConsumer;

    // 8 Required Metric Cards
    private final SummaryCard cardTotalPatients;
    private final SummaryCard cardOpdPatients;
    private final SummaryCard cardIpdPatients;
    private final SummaryCard cardAvailBeds;
    private final SummaryCard cardOccupiedBeds;
    private final SummaryCard cardDoctors;
    private final SummaryCard cardPendingAdmissions;
    private final SummaryCard cardReferrals;

    // Table & Feeds
    private final DefaultTableModel recentTableModel;
    private final DefaultListModel<String> activityListModel;

    // Visual Chart / Capacity components
    private final JProgressBar prgBedCapacity;
    private final JLabel lblBedRatio;
    private final JPanel pnlDeptBars;

    public DashboardPanel(Consumer<String> navigationConsumer) {
        this.store = DemoDataStore.getInstance();
        this.navigationConsumer = navigationConsumer;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Top Header & Quick Action Buttons
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel titleGrp = new JPanel(new GridLayout(2, 1, 0, 2));
        titleGrp.setOpaque(false);
        JLabel lblHeading = new JLabel("HOSPITAL OPERATIONS DASHBOARD");
        lblHeading.setFont(UITheme.FONT_PAGE_TITLE);
        lblHeading.setForeground(UITheme.PRIMARY_NAVY);
        JLabel lblSub = new JLabel("Real-time clinical census, acute triage telemetry, bed capacity, and inter-facility referrals");
        lblSub.setFont(UITheme.FONT_SUBTITLE);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        titleGrp.add(lblHeading);
        titleGrp.add(lblSub);

        JPanel quickActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        quickActions.setOpaque(false);

        StyledButton btnNewPatient = new StyledButton("+ Register Patient", StyledButton.Type.PRIMARY);
        btnNewPatient.setPreferredSize(new Dimension(145, 32));
        btnNewPatient.addActionListener(e -> navigateTo(SidebarPanel.NAV_REGISTRATION));

        StyledButton btnOpd = new StyledButton("OPD Consultation", StyledButton.Type.SECONDARY);
        btnOpd.setPreferredSize(new Dimension(140, 32));
        btnOpd.addActionListener(e -> navigateTo(SidebarPanel.NAV_OPD_CONSULTATION));

        StyledButton btnBedMgmt = new StyledButton("Bed Management", StyledButton.Type.SECONDARY);
        btnBedMgmt.setPreferredSize(new Dimension(135, 32));
        btnBedMgmt.addActionListener(e -> navigateTo(SidebarPanel.NAV_BED_MANAGEMENT));

        StyledButton btnWorkflow = new StyledButton("OPD to IPD Journey", StyledButton.Type.NAVY);
        btnWorkflow.setPreferredSize(new Dimension(155, 32));
        btnWorkflow.addActionListener(e -> navigateTo(SidebarPanel.NAV_OPD_IPD_WORKFLOW));

        StyledButton btnCallNext = new StyledButton("Call Next Patient", StyledButton.Type.SUCCESS);
        btnCallNext.setPreferredSize(new Dimension(140, 32));
        btnCallNext.addActionListener(e -> {
            Patient called = store.callNextPatient();
            if (called != null) {
                JOptionPane.showMessageDialog(this,
                    "PATIENT CALLED TO CONSULTATION ROOM:\n\n" +
                    "ID         : " + called.getPatientId() + "\n" +
                    "Name       : " + called.getName() + "\n" +
                    "Severity   : Level " + called.getPriority() + " (" + called.getEmergencyLevel() + ")\n" +
                    "Department : " + called.getDepartment() + "\n\n" +
                    "Status updated to: CALLED - CONSULTING",
                    "Patient Called",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Priority Queue is empty. No patients currently waiting.",
                    "Priority Queue",
                    JOptionPane.WARNING_MESSAGE);
            }
        });

        quickActions.add(btnNewPatient);
        quickActions.add(btnOpd);
        quickActions.add(btnBedMgmt);
        quickActions.add(btnWorkflow);
        quickActions.add(btnCallNext);

        topBar.add(titleGrp, BorderLayout.WEST);
        topBar.add(quickActions, BorderLayout.EAST);

        // Center Scrollable Container
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setOpaque(false);

        scrollContent.add(topBar);
        scrollContent.add(Box.createVerticalStrut(14));

        // 8 Metric Cards in 2 rows of 4 cards
        JPanel cardsGrid = new JPanel(new GridLayout(2, 4, 12, 12));
        cardsGrid.setOpaque(false);
        cardsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));

        cardTotalPatients       = new SummaryCard("Total Patients", "0", "All registered records", UITheme.PRIMARY_BLUE);
        cardOpdPatients         = new SummaryCard("OPD Patients", "0", "Outpatient intake", UITheme.ACCENT_SKY);
        cardIpdPatients         = new SummaryCard("IPD Patients", "0", "Active inpatient stays", UITheme.ACCENT_PURPLE);
        cardAvailBeds           = new SummaryCard("Available Beds", "0", "Ready for admission", UITheme.SUCCESS_GREEN);
        cardOccupiedBeds        = new SummaryCard("Occupied Beds", "0", "Inpatient occupied", new Color(0xEA, 0x58, 0x0C));
        cardDoctors             = new SummaryCard("Medical Doctors", "0", "Clinical staff roster", UITheme.PRIMARY_NAVY);
        cardPendingAdmissions   = new SummaryCard("Pending Admissions", "0", "Awaiting bed placement", UITheme.WARNING_AMBER);
        cardReferrals           = new SummaryCard("Hospital Referrals", "0", "Inter-hospital transfers", UITheme.ACCENT_TEAL);

        cardsGrid.add(cardTotalPatients);
        cardsGrid.add(cardOpdPatients);
        cardsGrid.add(cardIpdPatients);
        cardsGrid.add(cardAvailBeds);
        cardsGrid.add(cardOccupiedBeds);
        cardsGrid.add(cardDoctors);
        cardsGrid.add(cardPendingAdmissions);
        cardsGrid.add(cardReferrals);

        scrollContent.add(cardsGrid);
        scrollContent.add(Box.createVerticalStrut(16));

        // Analytics Row: Left Bed Capacity Chart & Right Department Census
        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 14, 0));
        chartsRow.setOpaque(false);
        chartsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        // Chart Card 1: Hospital Bed Capacity & Occupancy
        JPanel cardCapacity = UITheme.createCardPanel();
        cardCapacity.setLayout(new BorderLayout(0, 8));
        JLabel lblCapTitle = UITheme.createSectionTitle("Bed Utilization & Occupancy Rate");
        cardCapacity.add(lblCapTitle, BorderLayout.NORTH);

        JPanel capBody = new JPanel();
        capBody.setLayout(new BoxLayout(capBody, BoxLayout.Y_AXIS));
        capBody.setOpaque(false);

        lblBedRatio = new JLabel("Occupancy: Calculating...");
        lblBedRatio.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblBedRatio.setForeground(UITheme.PRIMARY_NAVY);

        prgBedCapacity = new JProgressBar(0, 100);
        prgBedCapacity.setValue(0);
        prgBedCapacity.setStringPainted(true);
        prgBedCapacity.setFont(new Font("Segoe UI", Font.BOLD, 11));
        prgBedCapacity.setForeground(UITheme.PRIMARY_BLUE);
        prgBedCapacity.setBackground(new Color(0xE2, 0xE8, 0xF0));
        prgBedCapacity.setPreferredSize(new Dimension(300, 22));

        JLabel lblBedLegend = new JLabel("● General Beds Available  |  ● ICU Critical Beds Available  |  ● Sanitizing Beds");
        lblBedLegend.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblBedLegend.setForeground(UITheme.TEXT_MUTED);

        capBody.add(lblBedRatio);
        capBody.add(Box.createVerticalStrut(6));
        capBody.add(prgBedCapacity);
        capBody.add(Box.createVerticalStrut(8));
        capBody.add(lblBedLegend);

        cardCapacity.add(capBody, BorderLayout.CENTER);
        chartsRow.add(cardCapacity);

        // Chart Card 2: Department Clinical Census
        JPanel cardDept = UITheme.createCardPanel();
        cardDept.setLayout(new BorderLayout(0, 8));
        JLabel lblDeptTitle = UITheme.createSectionTitle("Active Clinical Census by Specialty");
        cardDept.add(lblDeptTitle, BorderLayout.NORTH);

        pnlDeptBars = new JPanel();
        pnlDeptBars.setLayout(new GridLayout(2, 3, 8, 6));
        pnlDeptBars.setOpaque(false);
        cardDept.add(pnlDeptBars, BorderLayout.CENTER);

        chartsRow.add(cardDept);
        scrollContent.add(chartsRow);
        scrollContent.add(Box.createVerticalStrut(16));

        // Bottom Split: Recent Patients Table (Left) & Activity Log (Right)
        JPanel bottomSplit = new JPanel(new GridLayout(1, 2, 14, 0));
        bottomSplit.setOpaque(false);

        // Left: Recent Registrations Table
        JPanel cardRecent = UITheme.createCardPanel();
        cardRecent.setLayout(new BorderLayout(0, 10));

        JPanel recentHead = new JPanel(new BorderLayout());
        recentHead.setOpaque(false);
        JLabel lblRecentTitle = UITheme.createSectionTitle("Recent Patient Registrations");
        StyledButton btnViewAll = new StyledButton("View All Records", StyledButton.Type.SECONDARY);
        btnViewAll.setPreferredSize(new Dimension(130, 28));
        btnViewAll.setFont(new Font("Segoe UI", Font.BOLD, 11));
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
        tablePanel.setColumnWidths(90, 150, 90, 115, 110);
        tablePanel.applyStatusRendererToColumn(3);
        tablePanel.applyStatusRendererToColumn(4);
        cardRecent.add(tablePanel, BorderLayout.CENTER);
        bottomSplit.add(cardRecent);

        // Right: Activity Log
        JPanel cardActivity = UITheme.createCardPanel();
        cardActivity.setLayout(new BorderLayout(0, 8));
        JLabel lblActTitle = UITheme.createSectionTitle("Recent Clinical Activity Log");
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
        bottomSplit.add(cardActivity);

        scrollContent.add(bottomSplit);

        JScrollPane mainScroll = new JScrollPane(scrollContent);
        mainScroll.setBorder(null);
        mainScroll.getViewport().setBackground(UITheme.LIGHT_BG);
        mainScroll.getVerticalScrollBar().setUnitIncrement(14);
        add(mainScroll, BorderLayout.CENTER);

        // Reactive store listener
        store.addChangeListener(this::refreshData);

        refreshData();
    }

    private void navigateTo(String page) {
        if (navigationConsumer != null) {
            navigationConsumer.accept(page);
        }
    }

    public void refreshData() {
        int totalPatients = store.getTotalPatientsCount();
        int opdCount = 0;
        int ipdCount = 0;
        Map<String, Integer> deptCounts = new HashMap<>();

        for (Patient p : store.getPatients()) {
            if ("OPD".equalsIgnoreCase(p.getVisitType())) {
                opdCount++;
            }
            String st = p.getStatus() != null ? p.getStatus().toUpperCase() : "";
            if (st.contains("ADMITTED") || p.getAssignedBedId() != null) {
                ipdCount++;
            }
            String dept = p.getDepartment() != null ? p.getDepartment() : "General";
            deptCounts.put(dept, deptCounts.getOrDefault(dept, 0) + 1);
        }

        int availGen = store.getAvailableGeneralBedsCount();
        int availIcu = store.getAvailableIcuBedsCount();
        int totalAvailBeds = availGen + availIcu;
        int occupiedBeds = store.getAdmittedPatientsCount();
        int totalBeds = store.getBeds().size();

        int doctorsCount = store.getDoctors().size();

        int pendingAdmissions = 0;
        for (hms.AdmissionRequest req : store.getAdmissionRequests()) {
            if ("PENDING".equalsIgnoreCase(req.getStatus())) {
                pendingAdmissions++;
            }
        }

        int referralsCount = store.getReferrals().size();

        // Update 8 Metric Cards
        cardTotalPatients.setValue(String.valueOf(totalPatients));
        cardOpdPatients.setValue(String.valueOf(opdCount));
        cardIpdPatients.setValue(String.valueOf(ipdCount));
        cardAvailBeds.setValue(String.valueOf(totalAvailBeds));
        cardAvailBeds.setSubtitle("Gen: " + availGen + " | ICU: " + availIcu);
        cardOccupiedBeds.setValue(String.valueOf(occupiedBeds));
        cardDoctors.setValue(String.valueOf(doctorsCount));
        cardPendingAdmissions.setValue(String.valueOf(pendingAdmissions));
        cardReferrals.setValue(String.valueOf(referralsCount));

        // Update Bed Utilization Progress
        int occupancyPercent = totalBeds > 0 ? (occupiedBeds * 100) / totalBeds : 0;
        prgBedCapacity.setValue(occupancyPercent);
        prgBedCapacity.setString(occupancyPercent + "% (" + occupiedBeds + " / " + totalBeds + " Beds Occupied)");
        lblBedRatio.setText("Hospital Bed Census: " + occupiedBeds + " Occupied | " + totalAvailBeds + " Available | " + (totalBeds - occupiedBeds - totalAvailBeds) + " In Cleaning");

        // Update Department Census Cards
        pnlDeptBars.removeAll();
        String[] mainDepts = {"General Medicine", "Cardiology", "ICU", "Surgery", "Pediatrics", "Trauma"};
        for (String d : mainDepts) {
            int c = deptCounts.getOrDefault(d, 0);
            JPanel dPnl = new JPanel(new BorderLayout(4, 2));
            dPnl.setOpaque(true);
            dPnl.setBackground(new Color(0xF8, 0xFA, 0xFC));
            dPnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
            ));
            JLabel lblDName = new JLabel(d);
            lblDName.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblDName.setForeground(UITheme.TEXT_MUTED);

            JLabel lblDCount = new JLabel(c + " Patients");
            lblDCount.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblDCount.setForeground(UITheme.PRIMARY_NAVY);

            dPnl.add(lblDName, BorderLayout.NORTH);
            dPnl.add(lblDCount, BorderLayout.CENTER);
            pnlDeptBars.add(dPnl);
        }
        pnlDeptBars.revalidate();
        pnlDeptBars.repaint();

        // Refresh Recent Patients Table
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

        // Refresh Activity List
        activityListModel.clear();
        List<DemoDataStore.HistoryRecord> logs = store.getActivityHistory();
        int logCount = Math.min(10, logs.size());
        for (int i = 0; i < logCount; i++) {
            DemoDataStore.HistoryRecord r = logs.get(i);
            String timeStr = r.getTimestamp().length() > 11 ? r.getTimestamp().substring(11) : r.getTimestamp();
            activityListModel.addElement("[" + timeStr + "] " + r.getActivityType() + ": " + r.getDescription());
        }
    }
}
