package hms.ui.panels;

import hms.Doctor;
import hms.Encounter;
import hms.Hospital;
import hms.Patient;
import hms.ui.UITheme;
import hms.ui.components.FormPanel;
import hms.ui.components.ModernTablePanel;
import hms.ui.components.SidebarPanel;
import hms.ui.components.StyledButton;
import hms.ui.demo.DemoDataStore;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * OPD Consultation Panel for managing outpatient clinical encounters:
 * - Patient selection and doctor assignment
 * - Symptoms, clinical diagnosis, prescription, and physician notes
 * - Actions: Complete OPD, Follow-up Required, and Recommend IPD Admission
 * - Tabs for Recorded Encounters and Waiting/Consulting Patient queues
 */
public class OPDConsultationPanel extends JPanel {

    private final DemoDataStore store;
    private final Consumer<String> onNavigate;

    private final JComboBox<String> cmbPatient;
    private final JComboBox<String> cmbHospital;
    private final JComboBox<String> cmbDepartment;
    private final JComboBox<String> cmbDoctor;

    // Clinical Input Fields
    private final JTextField txtSymptoms;
    private final JTextField txtDiagnosis;
    private final JTextField txtPrescription;
    private final JTextArea txtNotes;
    private final JLabel lblCurrentEncounter;

    // IPD Admission Recommendation controls
    private final JCheckBox chkRecommendAdmission;
    private final JComboBox<String> cmbRecommendedBed;
    private final JTextField txtAdmissionReason;
    private final JPanel pnlAdmissionBox;

    // Tables
    private final DefaultTableModel encounterTableModel;
    private final DefaultTableModel waitingTableModel;

    public OPDConsultationPanel() {
        this(null);
    }

    public OPDConsultationPanel(Consumer<String> onNavigate) {
        this.store = DemoDataStore.getInstance();
        this.onNavigate = onNavigate;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header Title
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setOpaque(false);
        JLabel lblHeading = new JLabel("OUTPATIENT (OPD) CONSULTATION & CLINICAL ENCOUNTERS");
        lblHeading.setFont(UITheme.FONT_PAGE_TITLE);
        lblHeading.setForeground(UITheme.PRIMARY_NAVY);
        JLabel lblSub = new JLabel("Physician encounter workflows, clinical examinations, prescriptions, and IPD admission decisions");
        lblSub.setFont(UITheme.FONT_SUBTITLE);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        header.add(lblHeading);
        header.add(lblSub);
        add(header, BorderLayout.NORTH);

        // Center split: Left Consultation Form & Right Patient / Encounter Data Tabs
        JPanel centerSplit = new JPanel(new GridLayout(1, 2, 14, 0));
        centerSplit.setOpaque(false);

        // ================= LEFT: CONSULTATION FORM =================
        JPanel cardForm = UITheme.createCardPanel();
        cardForm.setLayout(new BorderLayout(0, 10));

        lblCurrentEncounter = new JLabel("Encounter Status: Ready for Patient Examination");
        lblCurrentEncounter.setFont(UITheme.FONT_LABEL);
        lblCurrentEncounter.setForeground(UITheme.PRIMARY_BLUE);
        cardForm.add(lblCurrentEncounter, BorderLayout.NORTH);

        FormPanel form = new FormPanel("Clinical Examination & Doctor Assessment");

        cmbPatient = UITheme.createComboBox(new String[0]);
        form.addField("Select Patient *:", cmbPatient);

        cmbHospital = UITheme.createComboBox(new String[0]);
        cmbHospital.addActionListener(e -> updateDoctorList());
        form.addField("Hospital Facility *:", cmbHospital);

        String[] depts = {"General Medicine", "Cardiology", "Surgery", "Pediatrics", "ICU / Critical Care", "Trauma & Emergency"};
        cmbDepartment = UITheme.createComboBox(depts);
        cmbDepartment.addActionListener(e -> updateDoctorList());
        form.addField("Department *:", cmbDepartment);

        cmbDoctor = UITheme.createComboBox(new String[0]);
        form.addField("Attending Doctor *:", cmbDoctor);

        txtSymptoms = UITheme.createTextField(20);
        form.addField("Presenting Symptoms *:", txtSymptoms);

        txtDiagnosis = UITheme.createTextField(20);
        form.addField("Clinical Diagnosis *:", txtDiagnosis);

        txtPrescription = UITheme.createTextField(20);
        form.addField("Prescription / Rx *:", txtPrescription);

        txtNotes = new JTextArea(3, 20);
        txtNotes.setFont(UITheme.FONT_REGULAR);
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        txtNotes.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        JScrollPane notesScroll = new JScrollPane(txtNotes);
        notesScroll.setBorder(null);
        form.addField("Doctor Examination Notes:", notesScroll);

        // IPD Admission Recommendation Sub-Panel
        pnlAdmissionBox = new JPanel(new GridLayout(3, 1, 4, 4));
        pnlAdmissionBox.setOpaque(true);
        pnlAdmissionBox.setBackground(new Color(0xF1, 0xF5, 0xF9));
        pnlAdmissionBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        chkRecommendAdmission = new JCheckBox("Recommend Inpatient (IPD) Admission");
        chkRecommendAdmission.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chkRecommendAdmission.setForeground(UITheme.PRIMARY_NAVY);
        chkRecommendAdmission.setOpaque(false);

        JPanel bedReqRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bedReqRow.setOpaque(false);
        bedReqRow.add(new JLabel("Recommended Bed:"));
        cmbRecommendedBed = UITheme.createComboBox(new String[]{"General Bed", "ICU Bed"});
        cmbRecommendedBed.setPreferredSize(new Dimension(140, 28));
        cmbRecommendedBed.setEnabled(false);
        bedReqRow.add(cmbRecommendedBed);

        JPanel reasonRow = new JPanel(new BorderLayout(8, 0));
        reasonRow.setOpaque(false);
        reasonRow.add(new JLabel("Justification:"), BorderLayout.WEST);
        txtAdmissionReason = UITheme.createTextField(15);
        txtAdmissionReason.setEnabled(false);
        reasonRow.add(txtAdmissionReason, BorderLayout.CENTER);

        chkRecommendAdmission.addActionListener(e -> {
            boolean active = chkRecommendAdmission.isSelected();
            cmbRecommendedBed.setEnabled(active);
            txtAdmissionReason.setEnabled(active);
            if (active && txtAdmissionReason.getText().trim().isEmpty()) {
                txtAdmissionReason.setText("Patient requires inpatient monitoring & treatment");
            }
        });

        pnlAdmissionBox.add(chkRecommendAdmission);
        pnlAdmissionBox.add(bedReqRow);
        pnlAdmissionBox.add(reasonRow);
        form.addFullWidthComponent(pnlAdmissionBox);

        // 3 Key Consultation Action Buttons
        StyledButton btnCompleteOPD = new StyledButton("Complete OPD", StyledButton.Type.SUCCESS);
        btnCompleteOPD.setPreferredSize(new Dimension(130, 34));
        btnCompleteOPD.addActionListener(e -> handleCompleteOPD());

        StyledButton btnFollowUp = new StyledButton("Follow-up Required", StyledButton.Type.SECONDARY);
        btnFollowUp.setPreferredSize(new Dimension(145, 34));
        btnFollowUp.addActionListener(e -> handleFollowUpRequired());

        StyledButton btnRecommendIPD = new StyledButton("Recommend IPD Admission", StyledButton.Type.PRIMARY);
        btnRecommendIPD.setPreferredSize(new Dimension(185, 34));
        btnRecommendIPD.addActionListener(e -> handleRecommendAdmission());

        form.addActionButton(btnCompleteOPD);
        form.addActionButton(btnFollowUp);
        form.addActionButton(btnRecommendIPD);

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.getVerticalScrollBar().setUnitIncrement(14);
        cardForm.add(formScroll, BorderLayout.CENTER);
        centerSplit.add(cardForm);

        // ================= RIGHT: TABS (ENCOUNTERS & WAITING QUEUE) =================
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BUTTON);

        // Tab 1: Recorded Encounters
        JPanel panelEncounters = new JPanel(new BorderLayout(0, 10));
        panelEncounters.setBackground(UITheme.LIGHT_BG);
        panelEncounters.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"Encounter ID", "Patient ID", "Doctor", "Department", "Date & Time", "Status"};
        encounterTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        ModernTablePanel encounterTable = new ModernTablePanel(encounterTableModel);
        encounterTable.setColumnWidths(90, 95, 140, 130, 140, 95);
        encounterTable.applyStatusRendererToColumn(5);
        panelEncounters.add(encounterTable, BorderLayout.CENTER);
        tabbedPane.addTab("Clinical Encounters History", panelEncounters);

        // Tab 2: Waiting Patients in Queue
        JPanel panelWaiting = new JPanel(new BorderLayout(0, 10));
        panelWaiting.setBackground(UITheme.LIGHT_BG);
        panelWaiting.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] queueCols = {"Patient ID", "Name", "Visit Type", "Priority Level", "Status"};
        waitingTableModel = new DefaultTableModel(queueCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        ModernTablePanel waitingTable = new ModernTablePanel(waitingTableModel);
        waitingTable.setColumnWidths(90, 150, 95, 115, 120);
        waitingTable.applyStatusRendererToColumn(3);
        waitingTable.applyStatusRendererToColumn(4);

        JPanel queueToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        queueToolbar.setOpaque(false);
        StyledButton btnCallToConsult = new StyledButton("Call Selected Patient", StyledButton.Type.PRIMARY);
        btnCallToConsult.setPreferredSize(new Dimension(160, 30));
        btnCallToConsult.addActionListener(e -> {
            int row = waitingTable.getTable().getSelectedRow();
            if (row != -1) {
                String pId = (String) waitingTableModel.getValueAt(row, 0);
                for (int i = 0; i < cmbPatient.getItemCount(); i++) {
                    if (cmbPatient.getItemAt(i).startsWith(pId)) {
                        cmbPatient.setSelectedIndex(i);
                        break;
                    }
                }
                lblCurrentEncounter.setText("Encounter Status: Examining Patient " + pId);
                lblCurrentEncounter.setForeground(UITheme.SUCCESS_GREEN);
            } else {
                JOptionPane.showMessageDialog(this, "Select a patient from the queue table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });
        queueToolbar.add(btnCallToConsult);
        panelWaiting.add(queueToolbar, BorderLayout.NORTH);
        panelWaiting.add(waitingTable, BorderLayout.CENTER);
        tabbedPane.addTab("Waiting & Consulting Patients", panelWaiting);

        centerSplit.add(tabbedPane);
        add(centerSplit, BorderLayout.CENTER);

        store.addChangeListener(this::refreshData);
        refreshData();
    }

    private void handleCompleteOPD() {
        if (!validateForm()) return;

        String patStr = (String) cmbPatient.getSelectedItem();
        String hospStr = (String) cmbHospital.getSelectedItem();
        String dept = (String) cmbDepartment.getSelectedItem();
        String docStr = (String) cmbDoctor.getSelectedItem();

        String patientId = patStr.split(" - ")[0];
        String doctorId = docStr.split(" - ")[0];
        String doctorName = docStr.contains(" - ") ? docStr.split(" - ")[1] : docStr;
        String hospitalId = hospStr.split(" - ")[0];

        String symptoms = txtSymptoms.getText().trim();
        String diagnosis = txtDiagnosis.getText().trim();
        String prescription = txtPrescription.getText().trim();
        String notes = txtNotes.getText().trim();

        Encounter enc = store.saveConsultation(patientId, doctorId, doctorName, dept, hospitalId,
                                              symptoms, diagnosis, prescription, notes);

        JOptionPane.showMessageDialog(this,
            "OPD CONSULTATION COMPLETED\n\n" +
            "Encounter ID : " + enc.getEncounterId() + "\n" +
            "Patient ID   : " + patientId + "\n" +
            "Doctor       : " + doctorName + " (" + dept + ")\n" +
            "Diagnosis    : " + diagnosis + "\n" +
            "Rx           : " + prescription + "\n" +
            "Status       : CONSULTATION COMPLETED\n\n" +
            "Patient consultation recorded and closed successfully.",
            "Complete OPD",
            JOptionPane.INFORMATION_MESSAGE);

        clearForm();
    }

    private void handleFollowUpRequired() {
        if (!validateForm()) return;

        String patStr = (String) cmbPatient.getSelectedItem();
        String hospStr = (String) cmbHospital.getSelectedItem();
        String dept = (String) cmbDepartment.getSelectedItem();
        String docStr = (String) cmbDoctor.getSelectedItem();

        String patientId = patStr.split(" - ")[0];
        String doctorId = docStr.split(" - ")[0];
        String doctorName = docStr.contains(" - ") ? docStr.split(" - ")[1] : docStr;
        String hospitalId = hospStr.split(" - ")[0];

        String symptoms = txtSymptoms.getText().trim();
        String diagnosis = txtDiagnosis.getText().trim();
        String prescription = txtPrescription.getText().trim();
        String notes = txtNotes.getText().trim();

        Encounter enc = store.saveConsultation(patientId, doctorId, doctorName, dept, hospitalId,
                                              symptoms, diagnosis, prescription, "Follow-up required: " + notes);

        Patient p = store.getPatientById(patientId);
        if (p != null) {
            p.setStatus("FOLLOW-UP REQUIRED");
            if (hms.db.DBConnection.isConnected()) {
                new hms.dao.PatientDAO().update(p);
            }
        }

        JOptionPane.showMessageDialog(this,
            "FOLLOW-UP CONSULTATION RECORDED\n\n" +
            "Encounter ID : " + enc.getEncounterId() + "\n" +
            "Patient ID   : " + patientId + "\n" +
            "Doctor       : " + doctorName + "\n" +
            "Status       : FOLLOW-UP REQUIRED\n\n" +
            "Patient has been marked for scheduled clinical follow-up.",
            "Follow-up Required",
            JOptionPane.INFORMATION_MESSAGE);

        clearForm();
    }

    private void handleRecommendAdmission() {
        if (!validateForm()) return;

        String patStr = (String) cmbPatient.getSelectedItem();
        String hospStr = (String) cmbHospital.getSelectedItem();
        String dept = (String) cmbDepartment.getSelectedItem();
        String docStr = (String) cmbDoctor.getSelectedItem();

        String patientId = patStr.split(" - ")[0];
        String doctorId = docStr.split(" - ")[0];
        String doctorName = docStr.contains(" - ") ? docStr.split(" - ")[1] : docStr;
        String hospitalId = hospStr.split(" - ")[0];

        String symptoms = txtSymptoms.getText().trim();
        String diagnosis = txtDiagnosis.getText().trim();
        String prescription = txtPrescription.getText().trim();
        String notes = txtNotes.getText().trim();

        String bedReq = (String) cmbRecommendedBed.getSelectedItem();
        if (bedReq == null) bedReq = "General Bed";
        String reason = txtAdmissionReason.getText().trim();
        if (reason.isEmpty()) {
            reason = "Inpatient admission indicated for diagnosis: " + diagnosis;
        }

        store.recommendAdmission(patientId, doctorId, doctorName, dept, hospitalId,
                                 symptoms, diagnosis, prescription, bedReq, reason);

        int choice = JOptionPane.showConfirmDialog(this,
            "INPATIENT (IPD) ADMISSION RECOMMENDED!\n\n" +
            "Patient ID       : " + patientId + "\n" +
            "Attending Doctor : " + doctorName + " (" + dept + ")\n" +
            "Recommended Bed  : " + bedReq + "\n" +
            "Admission Reason : " + reason + "\n\n" +
            "An inpatient admission request has been generated (Status: ADMISSION_PENDING).\n" +
            "Would you like to navigate directly to Bed Allocation now?",
            "Admission Recommendation Created",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        clearForm();

        if (choice == JOptionPane.YES_OPTION && onNavigate != null) {
            onNavigate.accept(SidebarPanel.NAV_ADMISSIONS);
        }
    }

    private boolean validateForm() {
        if (cmbPatient.getSelectedItem() == null || cmbHospital.getSelectedItem() == null || cmbDoctor.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select Patient, Hospital, and Attending Doctor.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (txtDiagnosis.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a clinical diagnosis.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtDiagnosis.requestFocus();
            return false;
        }
        return true;
    }

    private void clearForm() {
        txtSymptoms.setText("");
        txtDiagnosis.setText("");
        txtPrescription.setText("");
        txtNotes.setText("");
        chkRecommendAdmission.setSelected(false);
        cmbRecommendedBed.setEnabled(false);
        txtAdmissionReason.setEnabled(false);
        lblCurrentEncounter.setText("Encounter Status: Ready for Patient Examination");
        lblCurrentEncounter.setForeground(UITheme.PRIMARY_BLUE);
    }

    public void refreshData() {
        String prevPatient = (String) cmbPatient.getSelectedItem();
        cmbPatient.removeAllItems();
        for (Patient p : store.getPatients()) {
            cmbPatient.addItem(p.getPatientId() + " - " + p.getName() + " (" + p.getStatus() + ")");
        }
        if (prevPatient != null) {
            for (int i = 0; i < cmbPatient.getItemCount(); i++) {
                if (cmbPatient.getItemAt(i).equals(prevPatient)) {
                    cmbPatient.setSelectedIndex(i);
                    break;
                }
            }
        }

        String prevHosp = (String) cmbHospital.getSelectedItem();
        cmbHospital.removeAllItems();
        for (Hospital h : store.getHospitals()) {
            cmbHospital.addItem(h.getHospitalId() + " - " + h.getHospitalName());
        }
        if (prevHosp != null) {
            for (int i = 0; i < cmbHospital.getItemCount(); i++) {
                if (cmbHospital.getItemAt(i).equals(prevHosp)) {
                    cmbHospital.setSelectedIndex(i);
                    break;
                }
            }
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

        // Refresh Waiting Queue Table
        waitingTableModel.setRowCount(0);
        for (Patient p : store.getSortedQueueList()) {
            waitingTableModel.addRow(new Object[]{
                p.getPatientId(),
                p.getName(),
                p.getVisitType(),
                "Level " + p.getPriority() + " (" + p.getEmergencyLevel() + ")",
                p.getStatus()
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
