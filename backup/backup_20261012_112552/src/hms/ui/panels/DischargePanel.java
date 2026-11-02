package hms.ui.panels;

import hms.Bed;
import hms.Patient;
import hms.ui.UITheme;
import hms.ui.components.FormPanel;
import hms.ui.components.ModernTablePanel;
import hms.ui.components.StyledButton;
import hms.ui.demo.DemoDataStore;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Patient Discharge Panel for processing inpatient discharges,
 * releasing bed assignments, and transitioning beds into the sanitization cycle.
 */
public class DischargePanel extends JPanel {

    private final DemoDataStore store;

    private final JTextField txtSearchPatientId;
    private final JComboBox<String> cmbAdmittedPatients;
    private final JTextField txtPatientName;
    private final JTextField txtCurrentBed;
    private final JTextField txtHospital;
    private final JTextField txtDischargeDateTime;
    private final JTextArea txtDischargeNotes;
    private final DefaultTableModel admittedTableModel;
    private final ModernTablePanel tablePanel;

    public DischargePanel() {
        this.store = DemoDataStore.getInstance();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header Title
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setOpaque(false);
        JLabel lblHeading = new JLabel("PATIENT DISCHARGE & BED RELEASE");
        lblHeading.setFont(UITheme.FONT_PAGE_TITLE);
        lblHeading.setForeground(UITheme.PRIMARY_NAVY);
        JLabel lblSub = new JLabel("Final discharge checkout, clinical clearance documentation, and bed cleaning dispatch");
        lblSub.setFont(UITheme.FONT_SUBTITLE);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        header.add(lblHeading);
        header.add(lblSub);
        add(header, BorderLayout.NORTH);

        // Center split
        JPanel split = new JPanel(new GridLayout(1, 2, 16, 0));
        split.setOpaque(false);

        // Left Card: Discharge Form
        JPanel cardForm = UITheme.createCardPanel();
        cardForm.setLayout(new BorderLayout(0, 12));

        FormPanel form = new FormPanel("Discharge Clearance Dossier");

        // Search row
        JPanel searchBox = new JPanel(new BorderLayout(6, 0));
        searchBox.setOpaque(false);
        txtSearchPatientId = UITheme.createTextField(10);
        StyledButton btnFind = new StyledButton("Find Admitted", StyledButton.Type.PRIMARY);
        btnFind.setPreferredSize(new Dimension(120, 32));
        btnFind.addActionListener(e -> handleFindPatient());
        searchBox.add(txtSearchPatientId, BorderLayout.CENTER);
        searchBox.add(btnFind, BorderLayout.EAST);
        form.addField("Search Patient ID:", searchBox);

        cmbAdmittedPatients = UITheme.createComboBox(new String[0]);
        cmbAdmittedPatients.addActionListener(e -> onAdmittedPatientSelected());
        form.addField("Or Select Active Patient:", cmbAdmittedPatients);

        txtPatientName = UITheme.createTextField(20);
        txtPatientName.setEditable(false);
        txtPatientName.setBackground(new Color(0xF1, 0xF5, 0xF9));
        form.addField("Patient Full Name:", txtPatientName);

        txtCurrentBed = UITheme.createTextField(20);
        txtCurrentBed.setEditable(false);
        txtCurrentBed.setBackground(new Color(0xF1, 0xF5, 0xF9));
        form.addField("Assigned Bed ID:", txtCurrentBed);

        txtHospital = UITheme.createTextField(20);
        txtHospital.setEditable(false);
        txtHospital.setBackground(new Color(0xF1, 0xF5, 0xF9));
        form.addField("Hospital Facility:", txtHospital);

        txtDischargeDateTime = UITheme.createTextField(20);
        txtDischargeDateTime.setEditable(false);
        txtDischargeDateTime.setBackground(new Color(0xF1, 0xF5, 0xF9));
        txtDischargeDateTime.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        form.addField("Discharge Timestamp:", txtDischargeDateTime);

        txtDischargeNotes = new JTextArea(3, 20);
        txtDischargeNotes.setFont(UITheme.FONT_REGULAR);
        txtDischargeNotes.setLineWrap(true);
        txtDischargeNotes.setWrapStyleWord(true);
        txtDischargeNotes.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        form.addField("Discharge Summary / Notes:", new JScrollPane(txtDischargeNotes));

        JLabel lblProtocol = new JLabel("Notice: On discharge, the bed will automatically transition to 'CLEANING' state.");
        lblProtocol.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblProtocol.setForeground(new Color(0xB4, 0x53, 0x09));
        form.addFullWidthComponent(lblProtocol);

        StyledButton btnDischarge = new StyledButton("Process Patient Discharge", StyledButton.Type.DANGER);
        btnDischarge.addActionListener(e -> handleConfirmDischarge());
        form.addActionButton(btnDischarge);

        cardForm.add(form, BorderLayout.CENTER);
        split.add(cardForm);

        // Right Card: Admitted Patients Table
        JPanel cardTable = UITheme.createCardPanel();
        cardTable.setLayout(new BorderLayout(0, 10));
        JLabel lblTblTitle = UITheme.createSectionTitle("Currently Admitted Inpatients");
        cardTable.add(lblTblTitle, BorderLayout.NORTH);

        String[] cols = {"Patient ID", "Name", "Assigned Bed", "Hospital", "Status"};
        admittedTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablePanel = new ModernTablePanel(admittedTableModel);
        tablePanel.applyStatusRendererToColumn(4);

        // Click row to populate form
        tablePanel.getTable().getSelectionModel().addListSelectionListener(e -> {
            int row = tablePanel.getTable().getSelectedRow();
            if (row >= 0) {
                String pId = (String) admittedTableModel.getValueAt(row, 0);
                selectPatientById(pId, false);
            }
        });

        cardTable.add(tablePanel, BorderLayout.CENTER);
        split.add(cardTable);

        add(split, BorderLayout.CENTER);

        store.addChangeListener(this::refreshData);
        refreshData();
    }

    private void handleFindPatient() {
        String query = txtSearchPatientId.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a Patient ID to search.", "Search", JOptionPane.WARNING_MESSAGE);
            return;
        }
        selectPatientById(query, true);
    }

    private void selectPatientById(String pId, boolean showWarning) {
        Patient p = store.getPatientById(pId);
        if (p == null) {
            if (showWarning) {
                JOptionPane.showMessageDialog(this, "Patient ID '" + pId + "' not found.", "Not Found", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        if (p.getAssignedBedId() == null && !p.getStatus().contains("ADMITTED")) {
            if (showWarning) {
                JOptionPane.showMessageDialog(this,
                    "Patient " + pId + " (" + p.getName() + ") is NOT currently admitted to any bed.\nCurrent status: " + p.getStatus(),
                    "Patient Not Admitted",
                    JOptionPane.WARNING_MESSAGE);
            }
            return;
        }

        txtPatientName.setText(p.getName());
        txtCurrentBed.setText(p.getAssignedBedId() != null ? p.getAssignedBedId() : "N/A");
        txtHospital.setText(p.getAssignedHospitalId() != null ? p.getAssignedHospitalId() : "H001");
        txtDischargeDateTime.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    private void onAdmittedPatientSelected() {
        String sel = (String) cmbAdmittedPatients.getSelectedItem();
        if (sel != null && sel.contains(" - ")) {
            String pId = sel.split(" - ")[0];
            selectPatientById(pId, false);
        }
    }

    private void handleConfirmDischarge() {
        String pName = txtPatientName.getText().trim();
        String bedId = txtCurrentBed.getText().trim();
        String notes = txtDischargeNotes.getText().trim();

        if (pName.isEmpty() || bedId.isEmpty() || "N/A".equalsIgnoreCase(bedId)) {
            JOptionPane.showMessageDialog(this,
                "Please select an admitted patient to discharge.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get patient ID
        String pId = null;
        for (Patient p : store.getPatients()) {
            if (pName.equalsIgnoreCase(p.getName()) && bedId.equalsIgnoreCase(p.getAssignedBedId())) {
                pId = p.getPatientId();
                break;
            }
        }
        if (pId == null) {
            String sel = (String) cmbAdmittedPatients.getSelectedItem();
            if (sel != null && sel.contains(" - ")) pId = sel.split(" - ")[0];
        }

        if (pId == null) {
            JOptionPane.showMessageDialog(this, "Could not resolve patient for discharge.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to proceed with discharging this patient?\n\n" +
            "Patient ID   : " + pId + " (" + pName + ")\n" +
            "Assigned Bed : " + bedId + "\n\n" +
            "SECURITY PROTOCOL NOTICE:\n" +
            "• Bed " + bedId + " will be released immediately from the patient.\n" +
            "• Bed status will enter 'CLEANING' (it will NOT be available until sanitized).\n" +
            "• Patient status will update to 'DISCHARGED'.",
            "Confirm Inpatient Discharge",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = store.dischargePatient(pId, notes.isEmpty() ? "Standard clinical discharge" : notes);
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "DISCHARGE COMPLETED SUCCESSFULLY!\n\n" +
                    "Patient " + pId + " has been discharged.\n" +
                    "Bed " + bedId + " has entered CLEANING protocol.\n" +
                    "Discharge count incremented in Dashboard telemetry.",
                    "Discharge Verified",
                    JOptionPane.INFORMATION_MESSAGE);

                txtPatientName.setText("");
                txtCurrentBed.setText("");
                txtHospital.setText("");
                txtDischargeNotes.setText("");
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to discharge patient. Please verify admission status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshData() {
        cmbAdmittedPatients.removeAllItems();
        admittedTableModel.setRowCount(0);

        for (Patient p : store.getPatients()) {
            if (p.getAssignedBedId() != null || p.getStatus().contains("ADMITTED")) {
                cmbAdmittedPatients.addItem(p.getPatientId() + " - " + p.getName() + " [Bed: " + p.getAssignedBedId() + "]");
                admittedTableModel.addRow(new Object[]{
                    p.getPatientId(),
                    p.getName(),
                    p.getAssignedBedId() != null ? p.getAssignedBedId() : "Pending",
                    p.getAssignedHospitalId() != null ? p.getAssignedHospitalId() : "H001",
                    p.getStatus()
                });
            }
        }
    }
}
