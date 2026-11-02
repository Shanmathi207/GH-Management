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
 * Features a balanced, responsive two-column layout with fully visible buttons,
 * properly aligned form labels, and a cleanly fitted inpatient table.
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

        setLayout(new BorderLayout(0, 14));
        setBackground(UITheme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header Title
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setOpaque(false);
        JLabel lblHeading = new JLabel("PATIENT DISCHARGE & BED RELEASE");
        lblHeading.setFont(UITheme.FONT_PAGE_TITLE);
        lblHeading.setForeground(UITheme.PRIMARY_NAVY);
        JLabel lblSub = new JLabel("Final discharge clearance documentation, summary notes, and sanitization dispatch");
        lblSub.setFont(UITheme.FONT_SUBTITLE);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        header.add(lblHeading);
        header.add(lblSub);
        add(header, BorderLayout.NORTH);

        // Center split using GridBagLayout for proportional, responsive column widths
        JPanel split = new JPanel(new GridBagLayout());
        split.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // ================= LEFT COLUMN: DISCHARGE CLEARANCE DOSSIER =================
        JPanel cardForm = UITheme.createCardPanel();
        cardForm.setLayout(new BorderLayout(0, 10));

        JLabel lblFormTitle = UITheme.createSectionTitle("Discharge Clearance Dossier");
        cardForm.add(lblFormTitle, BorderLayout.NORTH);

        FormPanel form = new FormPanel(null);

        // 1. Search Patient ID with Find Admitted Patient button
        JPanel searchBox = new JPanel(new BorderLayout(6, 0));
        searchBox.setOpaque(false);
        txtSearchPatientId = UITheme.createTextField(10);
        txtSearchPatientId.addActionListener(e -> handleFindPatient());
        StyledButton btnFind = new StyledButton("Find Admitted", StyledButton.Type.PRIMARY);
        btnFind.setPreferredSize(new Dimension(125, 30));
        btnFind.addActionListener(e -> handleFindPatient());
        searchBox.add(txtSearchPatientId, BorderLayout.CENTER);
        searchBox.add(btnFind, BorderLayout.EAST);
        form.addField("Search Patient ID:", searchBox);

        // 2. Select Active Patient dropdown
        cmbAdmittedPatients = UITheme.createComboBox(new String[0]);
        cmbAdmittedPatients.addActionListener(e -> onAdmittedPatientSelected());
        form.addField("Select Active Patient:", cmbAdmittedPatients);

        // 3. Patient Full Name
        txtPatientName = UITheme.createTextField(20);
        txtPatientName.setEditable(false);
        txtPatientName.setBackground(new Color(0xF1, 0xF5, 0xF9));
        form.addField("Patient Full Name:", txtPatientName);

        // 4. Assigned Bed ID
        txtCurrentBed = UITheme.createTextField(20);
        txtCurrentBed.setEditable(false);
        txtCurrentBed.setBackground(new Color(0xF1, 0xF5, 0xF9));
        form.addField("Assigned Bed ID:", txtCurrentBed);

        // 5. Hospital Facility
        txtHospital = UITheme.createTextField(20);
        txtHospital.setEditable(false);
        txtHospital.setBackground(new Color(0xF1, 0xF5, 0xF9));
        form.addField("Hospital Facility:", txtHospital);

        // 6. Discharge Timestamp
        txtDischargeDateTime = UITheme.createTextField(20);
        txtDischargeDateTime.setEditable(false);
        txtDischargeDateTime.setBackground(new Color(0xF1, 0xF5, 0xF9));
        txtDischargeDateTime.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        form.addField("Discharge Timestamp:", txtDischargeDateTime);

        // 7. Discharge Summary / Notes
        txtDischargeNotes = new JTextArea(3, 20);
        txtDischargeNotes.setFont(UITheme.FONT_REGULAR);
        txtDischargeNotes.setLineWrap(true);
        txtDischargeNotes.setWrapStyleWord(true);
        txtDischargeNotes.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        JScrollPane notesScroll = new JScrollPane(txtDischargeNotes);
        notesScroll.setPreferredSize(new Dimension(200, 65));
        notesScroll.setBorder(null);
        form.addField("Discharge Notes / Summary:", notesScroll);

        // Notice Label
        JLabel lblProtocol = new JLabel("Notice: Bed automatically transitions to 'CLEANING' state upon release.");
        lblProtocol.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblProtocol.setForeground(new Color(0xB4, 0x53, 0x09));
        form.addFullWidthComponent(lblProtocol);

        // Balanced, fully-visible 2-row button bar
        JPanel buttonContainer = new JPanel(new GridLayout(2, 1, 0, 8));
        buttonContainer.setOpaque(false);
        buttonContainer.setBorder(new EmptyBorder(8, 0, 4, 0));

        JPanel row1Buttons = new JPanel(new GridLayout(1, 2, 8, 0));
        row1Buttons.setOpaque(false);
        StyledButton btnTreatmentNote = new StyledButton("+ Treatment Note", StyledButton.Type.SECONDARY);
        btnTreatmentNote.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnTreatmentNote.addActionListener(e -> handleAddTreatmentNote());
        StyledButton btnPreview = new StyledButton("Preview Summary", StyledButton.Type.NAVY);
        btnPreview.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPreview.addActionListener(e -> handlePreviewSummary());
        row1Buttons.add(btnTreatmentNote);
        row1Buttons.add(btnPreview);

        StyledButton btnDischarge = new StyledButton("Process Patient Discharge", StyledButton.Type.DANGER);
        btnDischarge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDischarge.setPreferredSize(new Dimension(Integer.MAX_VALUE, 36));
        btnDischarge.addActionListener(e -> handleConfirmDischarge());

        buttonContainer.add(row1Buttons);
        buttonContainer.add(btnDischarge);

        form.addFullWidthComponent(buttonContainer);

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.getVerticalScrollBar().setUnitIncrement(14);
        cardForm.add(formScroll, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.weightx = 0.50;
        gbc.insets = new Insets(0, 0, 0, 10);
        split.add(cardForm, gbc);

        // ================= RIGHT COLUMN: CURRENTLY ADMITTED INPATIENTS =================
        JPanel cardTable = UITheme.createCardPanel();
        cardTable.setLayout(new BorderLayout(0, 10));
        JLabel lblTblTitle = UITheme.createSectionTitle("Currently Admitted Inpatients");
        cardTable.add(lblTblTitle, BorderLayout.NORTH);

        String[] cols = {"Patient ID", "Patient Name", "Assigned Bed", "Hospital", "Status"};
        admittedTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablePanel = new ModernTablePanel(admittedTableModel);
        tablePanel.setColumnWidths(85, 135, 95, 80, 85);
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

        gbc.gridx = 1;
        gbc.weightx = 0.50;
        gbc.insets = new Insets(0, 6, 0, 0);
        split.add(cardTable, gbc);

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

        // Highlight matching in combo box if present
        for (int i = 0; i < cmbAdmittedPatients.getItemCount(); i++) {
            if (cmbAdmittedPatients.getItemAt(i).startsWith(pId)) {
                cmbAdmittedPatients.setSelectedIndex(i);
                break;
            }
        }
    }

    private void onAdmittedPatientSelected() {
        String sel = (String) cmbAdmittedPatients.getSelectedItem();
        if (sel != null && sel.contains(" - ")) {
            String pId = sel.split(" - ")[0];
            selectPatientById(pId, false);
        }
    }

    private void handlePreviewSummary() {
        String pName = txtPatientName.getText().trim();
        String bedId = txtCurrentBed.getText().trim();
        if (pName.isEmpty() || bedId.isEmpty() || "N/A".equalsIgnoreCase(bedId)) {
            JOptionPane.showMessageDialog(this, "Please select an admitted patient first.", "Preview", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String pId = resolveSelectedPatientId(pName, bedId);
        showDischargeSummaryDialog(pId, pName, bedId, txtHospital.getText().trim(), 
            txtDischargeNotes.getText().trim(), txtDischargeDateTime.getText().trim(), false);
    }

    private String resolveSelectedPatientId(String pName, String bedId) {
        for (Patient p : store.getPatients()) {
            if (pName.equalsIgnoreCase(p.getName()) && bedId.equalsIgnoreCase(p.getAssignedBedId())) {
                return p.getPatientId();
            }
        }
        String sel = (String) cmbAdmittedPatients.getSelectedItem();
        if (sel != null && sel.contains(" - ")) {
            return sel.split(" - ")[0];
        }
        return "PAT-UNKNOWN";
    }

    private void handleAddTreatmentNote() {
        String pName = txtPatientName.getText().trim();
        String bedId = txtCurrentBed.getText().trim();
        String note = txtDischargeNotes.getText().trim();

        if (pName.isEmpty() || bedId.isEmpty() || "N/A".equalsIgnoreCase(bedId)) {
            JOptionPane.showMessageDialog(this, "Please select an active admitted patient first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (note.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter treatment progress / clinical notes to record.", "Input Required", JOptionPane.WARNING_MESSAGE);
            txtDischargeNotes.requestFocus();
            return;
        }

        String pId = resolveSelectedPatientId(pName, bedId);
        store.recordInpatientTreatmentNote(pId, note, hms.security.UserSession.getInstance().getFullName());

        JOptionPane.showMessageDialog(this,
            "INPATIENT TREATMENT NOTE RECORDED\n\n" +
            "Patient ID : " + pId + " (" + pName + ")\n" +
            "Bed ID     : " + bedId + "\n" +
            "Note       : " + note + "\n\n" +
            "Clinical progress note logged to permanent audit ledger.",
            "Treatment Progress Updated",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleConfirmDischarge() {
        String pName = txtPatientName.getText().trim();
        String bedId = txtCurrentBed.getText().trim();
        String notes = txtDischargeNotes.getText().trim();
        String dischargeTime = txtDischargeDateTime.getText().trim();
        String hospital = txtHospital.getText().trim();

        if (pName.isEmpty() || bedId.isEmpty() || "N/A".equalsIgnoreCase(bedId)) {
            JOptionPane.showMessageDialog(this,
                "Please select an admitted patient to discharge.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String pId = resolveSelectedPatientId(pName, bedId);
        if (pId == null || "PAT-UNKNOWN".equals(pId)) {
            JOptionPane.showMessageDialog(this, "Could not resolve patient for discharge.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to proceed with discharging this patient?\n\n" +
            "Patient ID   : " + pId + " (" + pName + ")\n" +
            "Assigned Bed : " + bedId + "\n\n" +
            "SECURITY & PROTOCOL NOTICE:\n" +
            "• Bed " + bedId + " will be released immediately from the patient.\n" +
            "• Bed status will transition to 'CLEANING' (sanitization required before next intake).\n" +
            "• Patient status will update to 'DISCHARGED'.",
            "Confirm Inpatient Discharge",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            String finalNotes = notes.isEmpty() ? "Standard clinical discharge following recovery." : notes;
            boolean success = store.dischargePatient(pId, finalNotes);
            if (success) {
                showDischargeSummaryDialog(pId, pName, bedId, hospital, finalNotes, dischargeTime, true);

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

    private void showDischargeSummaryDialog(String pId, String pName, String bedId, String hospitalId, 
                                             String notes, String dischargeTime, boolean finalized) {
        JTextArea certArea = new JTextArea();
        certArea.setEditable(false);
        certArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        certArea.setText(
            "================================================================================\n" +
            "              GOVERNMENT DISTRICT GENERAL HOSPITAL CLINICAL NETWORK             \n" +
            "                        OFFICIAL PATIENT DISCHARGE SUMMARY                      \n" +
            "================================================================================\n\n" +
            " Document Status        : " + (finalized ? "FINALIZED & EXECUTED (DISCHARGED)" : "DRAFT PREVIEW (PENDING CONFIRMATION)") + "\n" +
            " Discharge Timestamp    : " + dischargeTime + "\n" +
            " Facility ID            : " + hospitalId + "\n\n" +
            " PATIENT PARTICULARS:\n" +
            " --------------------\n" +
            " Patient ID             : " + pId + "\n" +
            " Patient Full Name      : " + pName + "\n" +
            " Released Inpatient Bed : " + bedId + "\n\n" +
            " CLINICAL DISCHARGE NOTES & SUMMARY:\n" +
            " -----------------------------------\n" +
            " " + notes + "\n\n" +
            " INFECTION CONTROL & BED SANITIZATION PROTOCOL:\n" +
            " ----------------------------------------------\n" +
            " Bed " + bedId + " has been locked into 'CLEANING' state.\n" +
            " Terminal sanitization and linen replacement must be verified in Bed Management\n" +
            " before this unit can accept subsequent admissions.\n\n" +
            " AUTHORIZATION:\n" +
            " --------------\n" +
            " Authorized by          : " + hms.security.UserSession.getInstance().getFullName() + "\n" +
            " Role / Credential      : " + hms.security.UserSession.getInstance().getRole() + "\n" +
            " Signature Verification : [DIGITALLY AUTHORIZED VIA HOSPITAL CLINICAL PORTAL]\n" +
            "================================================================================\n"
        );
        certArea.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane scroll = new JScrollPane(certArea);
        scroll.setPreferredSize(new Dimension(650, 420));

        JOptionPane.showMessageDialog(this, scroll, 
            finalized ? "Official Discharge Summary - " + pId : "Preview Discharge Summary - " + pId, 
            JOptionPane.INFORMATION_MESSAGE);
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
