package hms.ui.panels;

import hms.AdmissionRequest;
import hms.Bed;
import hms.Doctor;
import hms.Encounter;
import hms.Hospital;
import hms.Patient;
import hms.security.UserSession;
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
 * Integrated OPD to IPD Workflow Control Center.
 * Implements the complete end-to-end patient journey:
 * 1. OPD Registration
 * 2. Doctor Consultation
 * 3. Inpatient Admission Recommendation
 * 4. IPD Admission & Bed Allocation
 * 5. Inpatient Care & Treatment Progress
 * 6. Patient Discharge & Bed Sanitization Turnaround
 */
public class OPDToIPDWorkflowPanel extends JPanel {

    private final DemoDataStore store;
    private final Consumer<String> onNavigate;

    private final JComboBox<String> cmbPatientSelect;
    private final JLabel lblCurrentStageBadge;
    private final StepperBar stepperBar;

    // Milestone details components
    private final JLabel lblPatientSummary;
    private final JLabel lblMilestoneDescription;
    private final JPanel actionContainer;
    private final DefaultTableModel timelineTableModel;
    private final ModernTablePanel timelineTable;

    public OPDToIPDWorkflowPanel() {
        this(null);
    }

    public OPDToIPDWorkflowPanel(Consumer<String> onNavigate) {
        this.store = DemoDataStore.getInstance();
        this.onNavigate = onNavigate;

        setLayout(new BorderLayout(0, 14));
        setBackground(UITheme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setOpaque(false);
        JLabel lblHeading = new JLabel("INTEGRATED OPD TO IPD CLINICAL JOURNEY");
        lblHeading.setFont(UITheme.FONT_PAGE_TITLE);
        lblHeading.setForeground(UITheme.PRIMARY_NAVY);
        JLabel lblSub = new JLabel("Complete clinical pipeline: Registration ➔ Doctor Consultation ➔ IPD Recommendation ➔ Bed Allocation ➔ Inpatient Treatment ➔ Discharge");
        lblSub.setFont(UITheme.FONT_SUBTITLE);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        header.add(lblHeading);
        header.add(lblSub);
        add(header, BorderLayout.NORTH);

        // Center Container
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);

        // Top Control: Patient Selector & 6-Stage Stepper
        JPanel topBar = UITheme.createCardPanel();
        topBar.setLayout(new BorderLayout(14, 10));

        JPanel selectRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        selectRow.setOpaque(false);
        selectRow.add(UITheme.createFormLabel("Select Active Patient:"));
        cmbPatientSelect = UITheme.createComboBox(new String[0]);
        cmbPatientSelect.setPreferredSize(new Dimension(320, 32));
        cmbPatientSelect.addActionListener(e -> updateActivePatientView());
        selectRow.add(cmbPatientSelect);

        lblCurrentStageBadge = new JLabel("STAGE: OPD REGISTRATION");
        lblCurrentStageBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCurrentStageBadge.setForeground(UITheme.PRIMARY_BLUE);
        selectRow.add(Box.createHorizontalStrut(15));
        selectRow.add(lblCurrentStageBadge);

        topBar.add(selectRow, BorderLayout.NORTH);

        stepperBar = new StepperBar();
        topBar.add(stepperBar, BorderLayout.CENTER);

        centerPanel.add(topBar, BorderLayout.NORTH);

        // Main Split: Left Milestone Operations & Right Patient Dossier/Timeline
        JPanel mainSplit = new JPanel(new GridLayout(1, 2, 14, 0));
        mainSplit.setOpaque(false);

        // Left Card: Current Stage Execution Desk
        JPanel cardOperations = UITheme.createCardPanel();
        cardOperations.setLayout(new BorderLayout(0, 12));

        JPanel opHeader = new JPanel(new GridLayout(2, 1, 0, 4));
        opHeader.setOpaque(false);
        JLabel lblOpTitle = UITheme.createSectionTitle("Current Milestone Clinical Execution");
        lblMilestoneDescription = new JLabel("Select a patient to inspect their active clinical milestone.");
        lblMilestoneDescription.setFont(UITheme.FONT_REGULAR);
        lblMilestoneDescription.setForeground(UITheme.TEXT_MUTED);
        opHeader.add(lblOpTitle);
        opHeader.add(lblMilestoneDescription);
        cardOperations.add(opHeader, BorderLayout.NORTH);

        actionContainer = new JPanel();
        actionContainer.setLayout(new BoxLayout(actionContainer, BoxLayout.Y_AXIS));
        actionContainer.setOpaque(false);
        JScrollPane actionScroll = new JScrollPane(actionContainer);
        actionScroll.setBorder(null);
        actionScroll.setOpaque(false);
        actionScroll.getViewport().setOpaque(false);
        cardOperations.add(actionScroll, BorderLayout.CENTER);

        mainSplit.add(cardOperations);

        // Right Card: Longitudinal Patient Dossier & Activity History
        JPanel cardTimeline = UITheme.createCardPanel();
        cardTimeline.setLayout(new BorderLayout(0, 10));

        JPanel timelineHeader = new JPanel(new GridLayout(2, 1, 0, 4));
        timelineHeader.setOpaque(false);
        JLabel lblTimeTitle = UITheme.createSectionTitle("Clinical Dossier & Longitudinal Audit Trail");
        lblPatientSummary = new JLabel("Patient: None Selected");
        lblPatientSummary.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPatientSummary.setForeground(UITheme.PRIMARY_NAVY);
        timelineHeader.add(lblTimeTitle);
        timelineHeader.add(lblPatientSummary);
        cardTimeline.add(timelineHeader, BorderLayout.NORTH);

        String[] cols = {"Timestamp", "Milestone Action", "Details", "Medical Staff"};
        timelineTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        timelineTable = new ModernTablePanel(timelineTableModel);
        timelineTable.setColumnWidths(135, 120, 220, 110);
        timelineTable.applyStatusRendererToColumn(1);
        cardTimeline.add(timelineTable, BorderLayout.CENTER);

        mainSplit.add(cardTimeline);
        centerPanel.add(mainSplit, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        store.addChangeListener(this::refreshData);
        refreshData();
    }

    private void updateActivePatientView() {
        String sel = (String) cmbPatientSelect.getSelectedItem();
        if (sel == null || !sel.contains(" - ")) {
            lblPatientSummary.setText("Patient: None Selected");
            lblCurrentStageBadge.setText("STAGE: N/A");
            stepperBar.setActiveStep(1);
            actionContainer.removeAll();
            actionContainer.revalidate();
            actionContainer.repaint();
            return;
        }

        String pId = sel.split(" - ")[0];
        Patient p = store.getPatientById(pId);
        if (p == null) return;

        int stage = determinePatientStage(p);
        stepperBar.setActiveStep(stage);

        lblPatientSummary.setText("Patient: " + p.getPatientId() + " - " + p.getName() + " | Age: " + p.getAge() + 
                                 " | Priority: " + p.getPriority() + " | Current Status: " + p.getStatus());

        updateMilestoneOperations(p, stage);
        updateTimelineHistory(pId);
    }

    private int determinePatientStage(Patient p) {
        String st = p.getStatus().toUpperCase();
        if (st.contains("DISCHARGED")) {
            return 6;
        } else if (st.contains("ADMITTED")) {
            return 5; // Inpatient Care & Treatment
        } else if (st.contains("RECOMMENDED") || p.getAssignedBedId() != null) {
            return 4; // Bed Allocation pending
        } else if (st.contains("CONSULTATION COMPLETED") || st.contains("CONSULTED")) {
            return 3; // Consultation done, ready for admission recommendation
        } else if (st.contains("CALLED") || st.contains("CONSULTING")) {
            return 2; // In consultation
        } else {
            return 1; // Registered in queue
        }
    }

    private void updateMilestoneOperations(Patient p, int stage) {
        actionContainer.removeAll();

        switch (stage) {
            case 1: // OPD Registered in Queue
                lblCurrentStageBadge.setText("STAGE 1: OPD REGISTERED & IN TRIAGE QUEUE");
                lblMilestoneDescription.setText("Patient is registered and queued in the deterministic PriorityQueue. Ready for doctor consultation.");

                FormPanel f1 = new FormPanel("Step 1: Outpatient Intake Dossier");
                f1.addField("Patient Name:", new JLabel(p.getName() + " (" + p.getGender() + ", Age " + p.getAge() + ")"));
                f1.addField("Visit Type & Priority:", new JLabel(p.getVisitType() + " | Severity Level " + p.getPriority()));
                f1.addField("Department:", new JLabel(p.getDepartment()));
                f1.addField("Registration Time:", new JLabel(p.getRegistrationDateTime()));

                StyledButton btnCall = new StyledButton("Call Patient to Consultation Room", StyledButton.Type.PRIMARY);
                btnCall.addActionListener(e -> {
                    p.setStatus("CALLED - CONSULTING");
                    store.addHistoryRecord(p.getPatientId(), "CONSULTATION", "Called from Priority Queue for Physician Examination", UserSession.getInstance().getFullName());
                    store.notifyListeners();
                });
                f1.addActionButton(btnCall);
                actionContainer.add(f1);
                break;

            case 2: // In Consultation
            case 3: // Consultation Done / Ready for IPD Recommendation
                lblCurrentStageBadge.setText(stage == 2 ? "STAGE 2: DOCTOR CLINICAL CONSULTATION" : "STAGE 3: INPATIENT (IPD) ADMISSION RECOMMENDATION");
                lblMilestoneDescription.setText("Physician examines patient, records symptoms and diagnosis, and recommends inpatient bed placement if needed.");

                FormPanel f2 = new FormPanel("Step 2 & 3: Clinical Consultation & Admission Recommendation");
                f2.addField("Patient:", new JLabel(p.getName() + " [" + p.getPatientId() + "]"));

                JComboBox<String> cmbDoctor = UITheme.createComboBox(new String[0]);
                for (Doctor d : store.getDoctors()) {
                    cmbDoctor.addItem(d.getDoctorId() + " - " + d.getName() + " (" + d.getDepartment() + ")");
                }
                f2.addField("Attending Physician *:", cmbDoctor);

                JTextField txtSymptoms = UITheme.createTextField(20);
                txtSymptoms.setText("Persistent dyspnea, fever, chest tightness");
                f2.addField("Presenting Symptoms *:", txtSymptoms);

                JTextField txtDiagnosis = UITheme.createTextField(20);
                txtDiagnosis.setText("Acute Severe Pneumonia with Respiratory Distress");
                f2.addField("Clinical Diagnosis *:", txtDiagnosis);

                JComboBox<String> cmbBedReq = UITheme.createComboBox(new String[]{"General Bed", "ICU Bed"});
                if (p.getPriority() <= 2) cmbBedReq.setSelectedItem("ICU Bed");
                f2.addField("Recommended IPD Bed *:", cmbBedReq);

                JTextField txtJustification = UITheme.createTextField(20);
                txtJustification.setText("Critical hypoxia requiring continuous vital monitoring & IV therapy");
                f2.addField("Admission Justification *:", txtJustification);

                StyledButton btnSaveEnc = new StyledButton("Save Consultation Only", StyledButton.Type.SECONDARY);
                btnSaveEnc.addActionListener(e -> {
                    String docStr = (String) cmbDoctor.getSelectedItem();
                    String docName = docStr != null && docStr.contains(" - ") ? docStr.split(" - ")[1] : "Attending Doctor";
                    String docId = docStr != null && docStr.contains(" - ") ? docStr.split(" - ")[0] : "DOC-01";
                    store.saveConsultation(p.getPatientId(), docId, docName, p.getDepartment(), "H001",
                        txtSymptoms.getText().trim(), txtDiagnosis.getText().trim(), "Outpatient prescription dispensed", "Consultation complete");
                    JOptionPane.showMessageDialog(this, "OPD Consultation encounter recorded successfully.", "Consultation Complete", JOptionPane.INFORMATION_MESSAGE);
                });

                StyledButton btnRecommend = new StyledButton("Recommend Inpatient (IPD) Admission ➔", StyledButton.Type.PRIMARY);
                btnRecommend.addActionListener(e -> {
                    String docStr = (String) cmbDoctor.getSelectedItem();
                    String docName = docStr != null && docStr.contains(" - ") ? docStr.split(" - ")[1] : "Attending Doctor";
                    String docId = docStr != null && docStr.contains(" - ") ? docStr.split(" - ")[0] : "DOC-01";
                    String bedReq = (String) cmbBedReq.getSelectedItem();
                    String just = txtJustification.getText().trim();

                    store.recommendAdmission(p.getPatientId(), docId, docName, p.getDepartment(), "H001",
                        txtSymptoms.getText().trim(), txtDiagnosis.getText().trim(), just, bedReq, just);

                    JOptionPane.showMessageDialog(this,
                        "INPATIENT ADMISSION RECOMMENDED!\n\n" +
                        "Patient ID       : " + p.getPatientId() + "\n" +
                        "Recommended Bed  : " + bedReq + "\n" +
                        "Justification    : " + just + "\n\n" +
                        "The patient is now ready for Bed Allocation in Stage 4.",
                        "Admission Recommendation Created",
                        JOptionPane.INFORMATION_MESSAGE);
                });

                f2.addActionButton(btnSaveEnc);
                f2.addActionButton(btnRecommend);
                actionContainer.add(f2);
                break;

            case 4: // Admission Recommended -> Allocate Bed
                lblCurrentStageBadge.setText("STAGE 4: INPATIENT (IPD) BED ALLOCATION");
                lblMilestoneDescription.setText("Verify real-time bed availability and allocate a compatible General or ICU Bed for inpatient admission.");

                FormPanel f4 = new FormPanel("Step 4: Inpatient Bed Placement");
                f4.addField("Patient:", new JLabel(p.getName() + " [" + p.getPatientId() + "]"));
                f4.addField("Status:", new JLabel(p.getStatus()));

                String targetBedReq = p.getStatus().contains("ICU") ? "ICU Bed" : "General Bed";
                f4.addField("Required Bed Type:", new JLabel(targetBedReq));

                // Find available compatible beds
                JComboBox<String> cmbAvailBeds = UITheme.createComboBox(new String[0]);
                for (Bed b : store.getBeds()) {
                    if ("H001".equalsIgnoreCase(b.getHospitalId()) && !b.isOccupied() && !b.isCleaning() && b.supportsRequirement(targetBedReq)) {
                        cmbAvailBeds.addItem(b.getBedId() + " (" + b.getBedType() + " - " + b.getDepartment() + ")");
                    }
                }
                f4.addField("Select Available Bed *:", cmbAvailBeds);

                StyledButton btnAllocate = new StyledButton("Confirm Bed Allocation ➔", StyledButton.Type.SUCCESS);
                btnAllocate.addActionListener(e -> {
                    String bedSel = (String) cmbAvailBeds.getSelectedItem();
                    if (bedSel == null) {
                        JOptionPane.showMessageDialog(this, "No available " + targetBedReq + "s in facility. Please initiate an Inter-Hospital Referral.", "Bed Unavailable", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    String bedId = bedSel.split(" ")[0];
                    boolean ok = store.allocateBed(p.getPatientId(), bedId);
                    if (ok) {
                        JOptionPane.showMessageDialog(this,
                            "BED ALLOCATION CONFIRMED!\n\n" +
                            "Patient ID : " + p.getPatientId() + "\n" +
                            "Assigned   : Bed " + bedId + "\n" +
                            "Status     : Inpatient Admitted\n\n" +
                            "Patient now enters Stage 5: Inpatient Treatment & Monitoring.",
                            "Inpatient Admitted",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                });
                f4.addActionButton(btnAllocate);
                actionContainer.add(f4);
                break;

            case 5: // Inpatient Care & Treatment
                lblCurrentStageBadge.setText("STAGE 5: INPATIENT CARE & CLINICAL MONITORING");
                lblMilestoneDescription.setText("Patient is actively admitted in inpatient ward. Record daily rounds, clinical progress, and prepare for discharge.");

                FormPanel f5 = new FormPanel("Step 5: Inpatient Treatment & Clinical Rounds");
                f5.addField("Patient:", new JLabel(p.getName() + " [" + p.getPatientId() + "]"));
                f5.addField("Assigned Bed:", new JLabel(p.getAssignedBedId() != null ? p.getAssignedBedId() : "Active Inpatient Ward"));
                f5.addField("Current Status:", new JLabel(p.getStatus()));

                JTextArea txtTreatmentProgress = new JTextArea(3, 20);
                txtTreatmentProgress.setFont(UITheme.FONT_REGULAR);
                txtTreatmentProgress.setLineWrap(true);
                txtTreatmentProgress.setWrapStyleWord(true);
                txtTreatmentProgress.setText("Vitals stable (BP 120/80, SpO2 98%). Responding well to intravenous antibiotics and nebulization.");
                f5.addField("Treatment Progress / Rounds Note:", new JScrollPane(txtTreatmentProgress));

                StyledButton btnAddNote = new StyledButton("+ Add Inpatient Progress Note", StyledButton.Type.PRIMARY);
                btnAddNote.addActionListener(e -> {
                    String note = txtTreatmentProgress.getText().trim();
                    if (!note.isEmpty()) {
                        store.recordInpatientTreatmentNote(p.getPatientId(), note, UserSession.getInstance().getFullName());
                        JOptionPane.showMessageDialog(this, "Inpatient treatment progress note logged to patient dossier.", "Progress Recorded", JOptionPane.INFORMATION_MESSAGE);
                    }
                });

                StyledButton btnProceedDischarge = new StyledButton("Proceed to Clinical Discharge ➔", StyledButton.Type.DANGER);
                btnProceedDischarge.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(this,
                        "Clear Patient " + p.getName() + " for hospital discharge?\n" +
                        "Notice: Bed " + p.getAssignedBedId() + " will automatically transition to 'CLEANING' state for infection control.",
                        "Confirm Discharge Clearance",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                    if (confirm == JOptionPane.YES_OPTION) {
                        store.dischargePatient(p.getPatientId(), txtTreatmentProgress.getText().trim());
                        JOptionPane.showMessageDialog(this,
                            "PATIENT DISCHARGE COMPLETE\n\n" +
                            "Patient " + p.getName() + " has been discharged.\n" +
                            "Bed enters CLEANING state for terminal disinfection.",
                            "Discharge Finalized",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                });

                f5.addActionButton(btnAddNote);
                f5.addActionButton(btnProceedDischarge);
                actionContainer.add(f5);
                break;

            case 6: // Discharged & Bed Cleaning
                lblCurrentStageBadge.setText("STAGE 6: DISCHARGED & TERMINAL BED SANITIZATION");
                lblMilestoneDescription.setText("Patient has completed the full clinical journey and is discharged. Ensure infection control turnaround for the released bed.");

                FormPanel f6 = new FormPanel("Step 6: Discharge Finalization & Bed Sanitization");
                f6.addField("Patient:", new JLabel(p.getName() + " [" + p.getPatientId() + "]"));
                f6.addField("Final Status:", new JLabel(p.getStatus()));

                // Look for cleaning beds
                JComboBox<String> cmbCleaningBeds = UITheme.createComboBox(new String[0]);
                for (Bed b : store.getBeds()) {
                    if (b.isCleaning()) {
                        cmbCleaningBeds.addItem(b.getBedId() + " (" + b.getDepartment() + " - " + b.getBedType() + ")");
                    }
                }
                f6.addField("Beds Currently in CLEANING:", cmbCleaningBeds);

                StyledButton btnSanitize = new StyledButton("Verify Sanitization (Restore to AVAILABLE)", StyledButton.Type.SUCCESS);
                btnSanitize.addActionListener(e -> {
                    String cleanSel = (String) cmbCleaningBeds.getSelectedItem();
                    if (cleanSel == null) {
                        JOptionPane.showMessageDialog(this, "No beds currently awaiting cleaning verification.", "All Clean", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    String bId = cleanSel.split(" ")[0];
                    store.finishBedCleaning(bId);
                    JOptionPane.showMessageDialog(this, "Bed " + bId + " terminal sanitization verified!\nRestored to AVAILABLE for new admissions.", "Sanitization Complete", JOptionPane.INFORMATION_MESSAGE);
                });
                f6.addActionButton(btnSanitize);
                actionContainer.add(f6);
                break;
        }

        actionContainer.revalidate();
        actionContainer.repaint();
    }

    private void updateTimelineHistory(String patientId) {
        timelineTableModel.setRowCount(0);
        for (DemoDataStore.HistoryRecord h : store.getActivityHistory()) {
            if (patientId.equalsIgnoreCase(h.getPatientId()) || "FACILITY".equalsIgnoreCase(h.getPatientId())) {
                timelineTableModel.addRow(new Object[]{
                    h.getTimestamp(),
                    h.getActivityType(),
                    h.getDescription(),
                    h.getStaffUser()
                });
            }
        }
    }

    public void refreshData() {
        String prevSel = (String) cmbPatientSelect.getSelectedItem();
        cmbPatientSelect.removeAllItems();
        for (Patient p : store.getPatients()) {
            cmbPatientSelect.addItem(p.getPatientId() + " - " + p.getName() + " (" + p.getStatus() + ")");
        }
        if (prevSel != null) {
            for (int i = 0; i < cmbPatientSelect.getItemCount(); i++) {
                if (cmbPatientSelect.getItemAt(i).equals(prevSel)) {
                    cmbPatientSelect.setSelectedIndex(i);
                    break;
                }
            }
        }
        updateActivePatientView();
    }

    /**
     * StepperBar visual component displaying the 6-stage clinical progression.
     */
    private static class StepperBar extends JPanel {
        private int activeStep = 1;

        private static final String[] STEPS = {
            "1. OPD Intake",
            "2. Consultation",
            "3. IPD Recommendation",
            "4. Bed Allocation",
            "5. Inpatient Care",
            "6. Discharge"
        };

        public StepperBar() {
            setPreferredSize(new Dimension(800, 52));
            setMinimumSize(new Dimension(600, 50));
            setOpaque(false);
        }

        public void setActiveStep(int step) {
            this.activeStep = step;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int count = STEPS.length;
            int stepSpacing = (w - 60) / (count - 1);
            int circleR = 12;
            int centerY = 20;

            // Draw connecting progress bar
            g2.setColor(new Color(0xE2, 0xE8, 0xF0));
            g2.setStroke(new BasicStroke(4.0f));
            g2.drawLine(30, centerY, w - 30, centerY);

            // Draw completed active progress bar
            int activeX = 30 + (activeStep - 1) * stepSpacing;
            g2.setColor(UITheme.PRIMARY_BLUE);
            g2.drawLine(30, centerY, activeX, centerY);

            // Draw Step Nodes
            for (int i = 0; i < count; i++) {
                int stepNum = i + 1;
                int nx = 30 + i * stepSpacing;

                boolean isCompleted = stepNum < activeStep;
                boolean isCurrent = stepNum == activeStep;

                if (isCompleted) {
                    g2.setColor(UITheme.SUCCESS_GREEN);
                    g2.fillOval(nx - circleR, centerY - circleR, circleR * 2, circleR * 2);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    g2.drawString("✓", nx - 4, centerY + 4);
                } else if (isCurrent) {
                    g2.setColor(UITheme.PRIMARY_BLUE);
                    g2.fillOval(nx - circleR - 2, centerY - circleR - 2, (circleR + 2) * 2, (circleR + 2) * 2);
                    g2.setColor(Color.WHITE);
                    g2.fillOval(nx - circleR + 2, centerY - circleR + 2, (circleR - 2) * 2, (circleR - 2) * 2);
                    g2.setColor(UITheme.PRIMARY_BLUE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    g2.drawString(String.valueOf(stepNum), nx - 3, centerY + 4);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillOval(nx - circleR, centerY - circleR, circleR * 2, circleR * 2);
                    g2.setColor(new Color(0xCB, 0xD5, 0xE1));
                    g2.setStroke(new BasicStroke(2.0f));
                    g2.drawOval(nx - circleR, centerY - circleR, circleR * 2, circleR * 2);
                    g2.setColor(new Color(0x94, 0xA3, 0xB8));
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    g2.drawString(String.valueOf(stepNum), nx - 3, centerY + 4);
                }

                // Step Label Text
                g2.setFont(new Font("Segoe UI", isCurrent ? Font.BOLD : Font.PLAIN, 10));
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(STEPS[i]);
                g2.setColor(isCurrent ? UITheme.PRIMARY_NAVY : (isCompleted ? UITheme.SUCCESS_GREEN : UITheme.TEXT_MUTED));
                g2.drawString(STEPS[i], nx - tw / 2, centerY + 24);
            }

            g2.dispose();
        }
    }
}
