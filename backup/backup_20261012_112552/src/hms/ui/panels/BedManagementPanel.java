package hms.ui.panels;

import hms.Bed;
import hms.Hospital;
import hms.Patient;
import hms.ui.UITheme;
import hms.ui.components.ModernTablePanel;
import hms.ui.components.StyledButton;
import hms.ui.demo.DemoDataStore;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Bed Management Panel for tracking real-time hospital inpatient beds,
 * enforcing sanitization cleaning lifecycles, and managing allocations.
 */
public class BedManagementPanel extends JPanel {

    private final DemoDataStore store;
    private final DefaultTableModel tableModel;
    private final ModernTablePanel tablePanel;

    private final JComboBox<String> cmbHospitalFilter;
    private final JComboBox<String> cmbStatusFilter;
    private final JLabel lblSummaryStats;

    public BedManagementPanel() {
        this.store = DemoDataStore.getInstance();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header Title
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setOpaque(false);
        JLabel lblHeading = new JLabel("INPATIENT BED & WARD MANAGEMENT");
        lblHeading.setFont(UITheme.FONT_PAGE_TITLE);
        lblHeading.setForeground(UITheme.PRIMARY_NAVY);
        JLabel lblSub = new JLabel("Facility occupancy status, strict terminal cleaning workflows, and inpatient bed telemetry");
        lblSub.setFont(UITheme.FONT_SUBTITLE);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        header.add(lblHeading);
        header.add(lblSub);
        add(header, BorderLayout.NORTH);

        // Center Card
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);

        filterBar.add(UITheme.createFormLabel("Hospital:"));
        cmbHospitalFilter = UITheme.createComboBox(new String[]{"ALL"});
        cmbHospitalFilter.addActionListener(e -> applyFilter());
        filterBar.add(cmbHospitalFilter);

        filterBar.add(UITheme.createFormLabel("Status:"));
        String[] statuses = {"ALL", "AVAILABLE", "OCCUPIED", "CLEANING"};
        cmbStatusFilter = UITheme.createComboBox(statuses);
        cmbStatusFilter.addActionListener(e -> applyFilter());
        filterBar.add(cmbStatusFilter);

        toolbar.add(filterBar, BorderLayout.WEST);

        // Actions
        JPanel actionToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionToolbar.setOpaque(false);

        StyledButton btnRefresh = new StyledButton("🔄 Refresh", StyledButton.Type.SECONDARY);
        btnRefresh.setPreferredSize(new Dimension(100, 32));
        btnRefresh.addActionListener(e -> applyFilter());

        StyledButton btnClean = new StyledButton("✨ Finish Bed Cleaning", StyledButton.Type.SUCCESS);
        btnClean.setPreferredSize(new Dimension(175, 32));
        btnClean.addActionListener(e -> handleFinishCleaning());

        StyledButton btnAllocate = new StyledButton("+ Allocate Bed", StyledButton.Type.PRIMARY);
        btnAllocate.setPreferredSize(new Dimension(125, 32));
        btnAllocate.addActionListener(e -> handleAllocateSelected());

        actionToolbar.add(btnRefresh);
        actionToolbar.add(btnClean);
        actionToolbar.add(btnAllocate);
        toolbar.add(actionToolbar, BorderLayout.EAST);

        card.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {
            "Hospital ID", "Hospital Name", "Bed ID", "Bed Type",
            "Department", "Bed Status", "Assigned Patient ID"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tablePanel = new ModernTablePanel(tableModel);
        tablePanel.applyStatusRendererToColumn(5); // Colored status pill (Green/Red/Orange)
        card.add(tablePanel, BorderLayout.CENTER);

        // Bottom status summary
        lblSummaryStats = new JLabel("Loading bed census summary...");
        lblSummaryStats.setFont(UITheme.FONT_SUBTITLE);
        lblSummaryStats.setForeground(UITheme.TEXT_MUTED);
        lblSummaryStats.setBorder(new EmptyBorder(6, 6, 0, 0));
        card.add(lblSummaryStats, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);

        store.addChangeListener(this::applyFilter);
        refreshHospitalDropdown();
        applyFilter();
    }

    private void refreshHospitalDropdown() {
        cmbHospitalFilter.removeAllItems();
        cmbHospitalFilter.addItem("ALL");
        for (Hospital h : store.getHospitals()) {
            cmbHospitalFilter.addItem(h.getHospitalId() + " - " + h.getHospitalName());
        }
    }

    private void handleFinishCleaning() {
        int selectedRow = tablePanel.getTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a bed currently in 'CLEANING' status to mark as sanitized.",
                "Selection Required",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String bedId = (String) tableModel.getValueAt(selectedRow, 2);
        String currentStatus = (String) tableModel.getValueAt(selectedRow, 5);

        if (!"CLEANING".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this,
                "Bed " + bedId + " is currently '" + currentStatus + "', not 'CLEANING'.\n" +
                "Only beds undergoing sanitization can be cleared.",
                "Action Not Allowed",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean success = store.finishBedCleaning(bedId);
        if (success) {
            JOptionPane.showMessageDialog(this,
                "Bed " + bedId + " sanitization protocol complete!\n" +
                "Status has been restored to AVAILABLE for patient admissions.",
                "Sanitization Verified",
                JOptionPane.INFORMATION_MESSAGE);
            applyFilter();
        }
    }

    private void handleAllocateSelected() {
        int selectedRow = tablePanel.getTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an 'AVAILABLE' bed from the table to allocate.",
                "Selection Required",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String bedId = (String) tableModel.getValueAt(selectedRow, 2);
        String currentStatus = (String) tableModel.getValueAt(selectedRow, 5);
        String bedType = (String) tableModel.getValueAt(selectedRow, 3);

        if ("OCCUPIED".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this,
                "Bed " + bedId + " is already OCCUPIED.\nDouble-allocation is strictly prohibited.",
                "Allocation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if ("CLEANING".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this,
                "Bed " + bedId + " is currently in CLEANING / SANITIZATION.\n" +
                "Complete the cleaning protocol before allocating a new patient.",
                "Bed Not Ready",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Show prompt to pick patient
        List<Patient> patients = store.getPatients();
        String[] patientChoices = new String[patients.size()];
        for (int i = 0; i < patients.size(); i++) {
            Patient p = patients.get(i);
            patientChoices[i] = p.getPatientId() + " - " + p.getName() + " (" + p.getStatus() + ")";
        }

        if (patientChoices.length == 0) {
            JOptionPane.showMessageDialog(this, "No registered patients found to allocate.", "No Patients", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String chosen = (String) JOptionPane.showInputDialog(this,
            "Select patient to allocate to Bed " + bedId + " (" + bedType + "):",
            "Assign Patient to Bed",
            JOptionPane.PLAIN_MESSAGE,
            null,
            patientChoices,
            patientChoices[0]);

        if (chosen != null && !chosen.isEmpty()) {
            String pId = chosen.split(" - ")[0];
            boolean success = store.allocateBed(pId, bedId);
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Patient " + pId + " successfully allocated to Bed " + bedId + "!",
                    "Allocation Complete",
                    JOptionPane.INFORMATION_MESSAGE);
                applyFilter();
            }
        }
    }

    public void applyFilter() {
        String hospFilter = (String) cmbHospitalFilter.getSelectedItem();
        String statusFilter = (String) cmbStatusFilter.getSelectedItem();

        String hospIdQuery = (hospFilter != null && hospFilter.contains(" - ")) ? hospFilter.split(" - ")[0] : null;

        tableModel.setRowCount(0);
        int total = 0, avail = 0, occ = 0, clean = 0;

        for (Bed b : store.getBeds()) {
            total++;
            String status = b.getStatus();
            if ("AVAILABLE".equalsIgnoreCase(status)) avail++;
            else if ("OCCUPIED".equalsIgnoreCase(status)) occ++;
            else if ("CLEANING".equalsIgnoreCase(status)) clean++;

            boolean matchHosp = hospIdQuery == null || b.getHospitalId().equalsIgnoreCase(hospIdQuery);
            boolean matchStatus = statusFilter == null || "ALL".equalsIgnoreCase(statusFilter) || status.equalsIgnoreCase(statusFilter);

            if (matchHosp && matchStatus) {
                Hospital h = store.getHospitalById(b.getHospitalId());
                String hospName = h != null ? h.getHospitalName() : b.getHospitalId();

                tableModel.addRow(new Object[]{
                    b.getHospitalId(),
                    hospName,
                    b.getBedId(),
                    b.getBedType(),
                    b.getDepartment(),
                    status,
                    b.getPatientId() != null ? b.getPatientId() : "—"
                });
            }
        }

        lblSummaryStats.setText("Bed Census: " + total + " Total Beds • " + 
            avail + " Available (Green) • " + 
            occ + " Occupied (Red) • " + 
            clean + " Cleaning / Sanitization (Orange)");
    }
}
