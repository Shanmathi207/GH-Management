package hms.ui.panels;

import hms.Doctor;
import hms.Encounter;
import hms.Hospital;
import hms.Patient;
import hms.ui.UITheme;
import hms.ui.components.FormPanel;
import hms.ui.components.ModernTablePanel;
import hms.ui.components.StyledButton;
import hms.ui.demo.DemoDataStore;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * OPD Consultation Panel for managing physician encounters, clinical notes,
 * and outpatient diagnoses.
 */
public class OPDConsultationPanel extends JPanel {

    private final DemoDataStore store;

    private final JComboBox<String> cmbPatient;
    private final JComboBox<String> cmbHospital;
    private final JComboBox<String> cmbDepartment;
    private final JComboBox<String> cmbDoctor;
    private final JTextArea txtNotes;
    private final JLabel lblCurrentEncounter;

    private final DefaultTableModel encounterTableModel;
    private String activeEncounterId = null;

    public OPDConsultationPanel() {
        this.store = DemoDataStore.getInstance();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header Title
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setOpaque(false);
        JLabel lblHeading = new JLabel("OUTPATIENT (OPD) CONSULTATION & ENCOUNTERS");
        lblHeading.setFont(UITheme.FONT_PAGE_TITLE);
        lblHeading.setForeground(UITheme.PRIMARY_NAVY);
        JLabel lblSub = new JLabel("Physician encounter workflows, clinical notes documentation, and diagnosis recording");
        lblSub.setFont(UITheme.FONT_SUBTITLE);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        header.add(lblHeading);
        header.add(lblSub);
        add(header, BorderLayout.NORTH);

        // Center split: Left Form (Encounter intake) & Right Table (Encounter history)
        JPanel centerSplit = new JPanel(new GridLayout(1, 2, 16, 0));
        centerSplit.setOpaque(false);

        // Left Card: Form
        JPanel cardForm = UITheme.createCardPanel();
        cardForm.setLayout(new BorderLayout(0, 12));

        lblCurrentEncounter = new JLabel("Encounter Status: Ready to start new encounter");
        lblCurrentEncounter.setFont(UITheme.FONT_LABEL);
        lblCurrentEncounter.setForeground(UITheme.PRIMARY_BLUE);
        cardForm.add(lblCurrentEncounter, BorderLayout.NORTH);

        FormPanel form = new FormPanel(null);

        cmbPatient = UITheme.createComboBox(new String[0]);
        form.addField("Select Patient *:", cmbPatient);

        cmbHospital = UITheme.createComboBox(new String[0]);
        cmbHospital.addActionListener(e -> updateDoctorList());
        form.addField("Select Hospital *:", cmbHospital);

        String[] depts = {"General Medicine", "Cardiology", "Surgery", "Pediatrics", "ICU", "Trauma"};
        cmbDepartment = UITheme.createComboBox(depts);
        cmbDepartment.addActionListener(e -> updateDoctorList());
        form.addField("Department *:", cmbDepartment);

        cmbDoctor = UITheme.createComboBox(new String[0]);
        form.addField("Attending Doctor *:", cmbDoctor);

        txtNotes = new JTextArea(4, 20);
        txtNotes.setFont(UITheme.FONT_REGULAR);
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        txtNotes.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        JScrollPane notesScroll = new JScrollPane(txtNotes);
        notesScroll.setBorder(null);
        form.addField("Clinical Notes / Diagnosis:", notesScroll);

        // Buttons
        StyledButton btnStart = new StyledButton("Start Encounter", StyledButton.Type.PRIMARY);
        btnStart.addActionListener(e -> handleStartEncounter());

        StyledButton btnSave = new StyledButton("Save Consultation", StyledButton.Type.SECONDARY);
        btnSave.addActionListener(e -> handleSaveConsultation(false));

        StyledButton btnComplete = new StyledButton("Complete Encounter", StyledButton.Type.SUCCESS);
        btnComplete.addActionListener(e -> handleSaveConsultation(true));

        form.addActionButton(btnStart);
        form.addActionButton(btnSave);
        form.addActionButton(btnComplete);

        cardForm.add(form, BorderLayout.CENTER);
        centerSplit.add(cardForm);

        // Right Card: Recent Encounters Table
        JPanel cardHistory = UITheme.createCardPanel();
        cardHistory.setLayout(new BorderLayout(0, 10));
        JLabel lblTableTitle = UITheme.createSectionTitle("Recorded Clinical Encounters");
        cardHistory.add(lblTableTitle, BorderLayout.NORTH);

        String[] cols = {"Encounter ID", "Patient ID", "Doctor", "Department", "Date & Time", "Status"};
        encounterTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        ModernTablePanel encounterTable = new ModernTablePanel(encounterTableModel);
        encounterTable.applyStatusRendererToColumn(5);
        cardHistory.add(encounterTable, BorderLayout.CENTER);

        centerSplit.add(cardHistory);

        add(centerSplit, BorderLayout.CENTER);

        store.addChangeListener(this::refreshData);
        refreshData();
    }

    private void handleStartEncounter() {
        if (cmbPatient.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient to start an encounter.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        activeEncounterId = "ENC-" + (System.currentTimeMillis() % 10000);
        lblCurrentEncounter.setText("Encounter Status: ACTIVE [" + activeEncounterId + "] in progress");
        lblCurrentEncounter.setForeground(UITheme.SUCCESS_GREEN);
        JOptionPane.showMessageDialog(this, "Encounter " + activeEncounterId + " initialized.\nYou may now enter clinical examination notes.", "Encounter Started", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleSaveConsultation(boolean complete) {
        String patStr = (String) cmbPatient.getSelectedItem();
        String hospStr = (String) cmbHospital.getSelectedItem();
        String dept = (String) cmbDepartment.getSelectedItem();
        String docStr = (String) cmbDoctor.getSelectedItem();
        String notes = txtNotes.getText().trim();

        if (patStr == null || hospStr == null || docStr == null) {
            JOptionPane.showMessageDialog(this, "Please select Patient, Hospital, and Doctor.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (notes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter consultation notes / clinical findings.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtNotes.requestFocus();
            return;
        }

        String patientId = patStr.split(" - ")[0];
        String doctorId = docStr.split(" - ")[0];
        String doctorName = docStr.contains(" - ") ? docStr.split(" - ")[1] : docStr;
        String hospitalId = hospStr.split(" - ")[0];

        Encounter enc = store.saveConsultation(patientId, doctorId, doctorName, dept, hospitalId, notes);

        JOptionPane.showMessageDialog(this,
            "CONSULTATION SAVED SUCCESSFULLY\n\n" +
            "Encounter ID : " + enc.getEncounterId() + "\n" +
            "Patient ID   : " + patientId + "\n" +
            "Doctor       : " + doctorName + " (" + dept + ")\n" +
            "Hospital     : " + hospitalId + "\n" +
            "Status       : " + (complete ? "COMPLETED" : "DOCUMENTED") + "\n\n" +
            "Patient status updated and activity recorded.",
            "Consultation Complete",
            JOptionPane.INFORMATION_MESSAGE);

        txtNotes.setText("");
        activeEncounterId = null;
        lblCurrentEncounter.setText("Encounter Status: Ready to start new encounter");
        lblCurrentEncounter.setForeground(UITheme.PRIMARY_BLUE);
    }

    public void refreshData() {
        // Refresh Patients Dropdown
        cmbPatient.removeAllItems();
        for (Patient p : store.getPatients()) {
            cmbPatient.addItem(p.getPatientId() + " - " + p.getName() + " (" + p.getStatus() + ")");
        }

        // Refresh Hospitals Dropdown
        cmbHospital.removeAllItems();
        for (Hospital h : store.getHospitals()) {
            cmbHospital.addItem(h.getHospitalId() + " - " + h.getHospitalName());
        }

        updateDoctorList();

        // Refresh Encounters Table
        encounterTableModel.setRowCount(0);
        for (Encounter enc : store.getEncounters()) {
            encounterTableModel.addRow(new Object[]{
                enc.getEncounterId(),
                enc.getPatientId(),
                enc.getDoctorName(),
                enc.getDepartment(),
                enc.getEncounterDate(),
                enc.getStatus()
            });
        }
    }

    private void updateDoctorList() {
        cmbDoctor.removeAllItems();
        String selectedDept = (String) cmbDepartment.getSelectedItem();
        List<Doctor> doctors = store.getDoctors();
        for (Doctor d : doctors) {
            if (selectedDept == null || d.getDepartment().equalsIgnoreCase(selectedDept) || d.getDepartment().contains("General")) {
                cmbDoctor.addItem(d.getDoctorId() + " - " + d.getName() + " (" + (d.isOnDuty() ? "On Duty" : "Off Duty") + ")");
            }
        }
        if (cmbDoctor.getItemCount() == 0 && !doctors.isEmpty()) {
            for (Doctor d : doctors) {
                cmbDoctor.addItem(d.getDoctorId() + " - " + d.getName());
            }
        }
    }
}
