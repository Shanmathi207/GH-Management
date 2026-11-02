package hms.ui.panels;

import hms.AdmissionRequest;
import hms.Bed;
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
 * Admission Requests Panel for formal inpatient bed requests,
 * capacity checks, and direct bed allocation workflows.
 */
public class AdmissionPanel extends JPanel {

    private final DemoDataStore store;

    private final JComboBox<String> cmbPatient;
    private final JTextField txtPatientName;
    private final JTextField txtRequestId;
    private final JComboBox<String> cmbBedRequirement;
    private final JComboBox<String> cmbHospital;
    private final JTextField txtRequestStatus;

    private final DefaultTableModel requestsTableModel;
    private final JLabel lblAvailabilityStatus;
    private final JLabel lblOpdRef;

    public AdmissionPanel() {
        this.store = DemoDataStore.getInstance();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header Title
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setOpaque(false);
        JLabel lblHeading = new JLabel("INPATIENT ADMISSION REQUESTS");
        lblHeading.setFont(UITheme.FONT_PAGE_TITLE);
        lblHeading.setForeground(UITheme.PRIMARY_NAVY);
        JLabel lblSub = new JLabel("Inpatient triage, bed reservation requests, and hospital resource capacity verification");
        lblSub.setFont(UITheme.FONT_SUBTITLE);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        header.add(lblHeading);
        header.add(lblSub);
        add(header, BorderLayout.NORTH);

        // Center split
        JPanel split = new JPanel(new GridLayout(1, 2, 16, 0));
        split.setOpaque(false);

        // Left Card: Request Creation & Allocation Form
        JPanel cardForm = UITheme.createCardPanel();
        cardForm.setLayout(new BorderLayout(0, 12));

        FormPanel form = new FormPanel("New Admission Request");

        cmbPatient = UITheme.createComboBox(new String[0]);
        cmbPatient.addActionListener(e -> onPatientSelected());
        form.addField("Select Patient *:", cmbPatient);

        txtPatientName = UITheme.createTextField(20);
        txtPatientName.setEditable(false);
        txtPatientName.setBackground(new Color(0xF1, 0xF5, 0xF9));
        form.addField("Patient Full Name:", txtPatientName);

        txtRequestId = UITheme.createTextField(20);
        txtRequestId.setEditable(false);
        txtRequestId.setBackground(new Color(0xF1, 0xF5, 0xF9));
        updateGeneratedReqId();
        form.addField("Generated Request ID:", txtRequestId);

        String[] bedTypes = {"General Bed", "ICU Bed"};
        cmbBedRequirement = UITheme.createComboBox(bedTypes);
        form.addField("Bed Requirement *:", cmbBedRequirement);

        cmbHospital = UITheme.createComboBox(new String[0]);
        form.addField("Target Hospital *:", cmbHospital);

        txtRequestStatus = UITheme.createTextField(20);
        txtRequestStatus.setEditable(false);
        txtRequestStatus.setText("READY TO SUBMIT");
        txtRequestStatus.setBackground(new Color(0xF1, 0xF5, 0xF9));
        form.addField("Request Status:", txtRequestStatus);

        lblAvailabilityStatus = new JLabel("Click 'Check Bed Availability' to inspect available beds.");
        lblAvailabilityStatus.setFont(UITheme.FONT_LABEL);
        lblAvailabilityStatus.setForeground(UITheme.PRIMARY_BLUE);
        lblAvailabilityStatus.setBorder(new EmptyBorder(6, 0, 6, 0));
        form.addFullWidthComponent(lblAvailabilityStatus);

        lblOpdRef = new JLabel("OPD Consultation Ref: Direct Inpatient Placement");
        lblOpdRef.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblOpdRef.setForeground(UITheme.TEXT_MUTED);
        lblOpdRef.setBorder(new EmptyBorder(2, 0, 4, 0));
        form.addFullWidthComponent(lblOpdRef);

        // Buttons
        StyledButton btnCheck = new StyledButton("Check Availability", StyledButton.Type.SECONDARY);
        btnCheck.addActionListener(e -> handleCheckAvailability());

        StyledButton btnCreate = new StyledButton("Create Request", StyledButton.Type.PRIMARY);
        btnCreate.addActionListener(e -> handleCreateRequest());

        StyledButton btnAllocate = new StyledButton("Allocate Bed", StyledButton.Type.SUCCESS);
        btnAllocate.addActionListener(e -> handleAllocateBed());

        form.addActionButton(btnCheck);
        form.addActionButton(btnCreate);
        form.addActionButton(btnAllocate);

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.getVerticalScrollBar().setUnitIncrement(14);
        cardForm.add(formScroll, BorderLayout.CENTER);
        split.add(cardForm);

        // Right Card: Admission Requests List
        JPanel cardTable = UITheme.createCardPanel();
        cardTable.setLayout(new BorderLayout(0, 10));
        JLabel lblTblTitle = UITheme.createSectionTitle("Active Admission Requests");
        cardTable.add(lblTblTitle, BorderLayout.NORTH);

        String[] cols = {"Req ID", "Patient ID", "Name", "Bed Type", "Hospital", "Status"};
        requestsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        ModernTablePanel reqTable = new ModernTablePanel(requestsTableModel);
        reqTable.setColumnWidths(90, 95, 145, 105, 90, 95);
        reqTable.applyStatusRendererToColumn(5); // Status pill
        cardTable.add(reqTable, BorderLayout.CENTER);

        split.add(cardTable);
        add(split, BorderLayout.CENTER);

        store.addChangeListener(this::refreshData);
        refreshData();
    }

    private void updateGeneratedReqId() {
        txtRequestId.setText("REQ-" + (store.getAdmissionRequests().size() + 301));
    }

    private void onPatientSelected() {
        String sel = (String) cmbPatient.getSelectedItem();
        if (sel != null && sel.contains(" - ")) {
            String pId = sel.split(" - ")[0];
            Patient p = store.getPatientById(pId);
            if (p != null) {
                txtPatientName.setText(p.getName());

                // Inspect latest OPD encounter
                hms.Encounter latestEnc = null;
                for (hms.Encounter enc : store.getEncounters()) {
                    if (enc.getPatientId().equalsIgnoreCase(pId)) {
                        latestEnc = enc;
                        break;
                    }
                }

                if (latestEnc != null) {
                    lblOpdRef.setText("OPD Enc: " + latestEnc.getEncounterId() + " | Doctor: " + latestEnc.getDoctorName() + 
                                     " | Diagnosis: " + latestEnc.getDiagnosis());
                    lblOpdRef.setForeground(UITheme.PRIMARY_BLUE);
                } else {
                    lblOpdRef.setText("OPD Consultation Ref: Direct Inpatient Placement");
                    lblOpdRef.setForeground(UITheme.TEXT_MUTED);
                }

                if (p.getStatus().contains("ICU") || p.getPriority() <= 2) {
                    cmbBedRequirement.setSelectedItem("ICU Bed");
                } else {
                    cmbBedRequirement.setSelectedItem("General Bed");
                }

                handleCheckAvailability();
            }
        }
    }

    private void handleCheckAvailability() {
        String hospItem = (String) cmbHospital.getSelectedItem();
        String bedReq = (String) cmbBedRequirement.getSelectedItem();
        if (hospItem == null) return;
        String hospId = hospItem.split(" - ")[0];

        int availCount = 0;
        for (Bed b : store.getBeds()) {
            if (b.getHospitalId().equalsIgnoreCase(hospId) && !b.isOccupied() && !b.isCleaning() && b.supportsRequirement(bedReq)) {
                availCount++;
            }
        }

        if (availCount > 0) {
            lblAvailabilityStatus.setText(availCount + " " + bedReq + "(s) currently AVAILABLE at " + hospId);
            lblAvailabilityStatus.setForeground(UITheme.SUCCESS_GREEN);
        } else {
            lblAvailabilityStatus.setText("No compatible " + bedReq + " available right now at " + hospId);
            lblAvailabilityStatus.setForeground(UITheme.ERROR_RED);
        }
    }

    private void handleCreateRequest() {
        String patItem = (String) cmbPatient.getSelectedItem();
        String hospItem = (String) cmbHospital.getSelectedItem();
        String bedReq = (String) cmbBedRequirement.getSelectedItem();

        if (patItem == null || hospItem == null) {
            JOptionPane.showMessageDialog(this, "Please select Patient and Hospital.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String patientId = patItem.split(" - ")[0];
        String hospitalId = hospItem.split(" - ")[0];

        AdmissionRequest req = store.createAdmissionRequest(patientId, bedReq, hospitalId);
        if (req == null) {
            JOptionPane.showMessageDialog(this,
                "A pending admission request already exists for Patient " + patientId + ".\nDuplicate pending requests are prevented.",
                "Duplicate Request Prevented",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
            "ADMISSION REQUEST CREATED\n\n" +
            "Request ID  : " + req.getRequestId() + "\n" +
            "Patient ID  : " + patientId + "\n" +
            "Bed Type    : " + bedReq + "\n" +
            "Hospital    : " + hospitalId + "\n\n" +
            "Status: PENDING ALLOCATION",
            "Admission Request Submitted",
            JOptionPane.INFORMATION_MESSAGE);

        updateGeneratedReqId();
    }

    private void handleAllocateBed() {
        String patItem = (String) cmbPatient.getSelectedItem();
        String hospItem = (String) cmbHospital.getSelectedItem();
        String bedReq = (String) cmbBedRequirement.getSelectedItem();

        if (patItem == null || hospItem == null) {
            JOptionPane.showMessageDialog(this, "Please select Patient and Hospital.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String patientId = patItem.split(" - ")[0];
        String hospitalId = hospItem.split(" - ")[0];

        // Find compatible available bed
        Bed targetBed = null;
        for (Bed b : store.getBeds()) {
            if (b.getHospitalId().equalsIgnoreCase(hospitalId) && !b.isOccupied() && !b.isCleaning() && b.supportsRequirement(bedReq)) {
                targetBed = b;
                break;
            }
        }

        if (targetBed == null) {
            JOptionPane.showMessageDialog(this,
                "No available compatible " + bedReq + " found in " + hospitalId + ".\n" +
                "Please verify Bed Management or initiate an Inter-Hospital Referral.",
                "Bed Unavailable",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean success = store.allocateBed(patientId, targetBed.getBedId());
        if (success) {
            JOptionPane.showMessageDialog(this,
                "BED ALLOCATION CONFIRMED!\n\n" +
                "Patient ID : " + patientId + "\n" +
                "Bed ID     : " + targetBed.getBedId() + " (" + targetBed.getBedType() + ")\n" +
                "Department : " + targetBed.getDepartment() + "\n" +
                "Hospital   : " + hospitalId + "\n\n" +
                "Bed is now marked OCCUPIED.\n" +
                "Patient status updated to ADMITTED.",
                "Allocation Successful",
                JOptionPane.INFORMATION_MESSAGE);
            handleCheckAvailability();
        } else {
            JOptionPane.showMessageDialog(this, "Bed allocation failed. Patient or bed status conflict.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshData() {
        cmbPatient.removeAllItems();
        for (Patient p : store.getPatients()) {
            cmbPatient.addItem(p.getPatientId() + " - " + p.getName() + " (" + p.getStatus() + ")");
        }

        cmbHospital.removeAllItems();
        for (Hospital h : store.getHospitals()) {
            cmbHospital.addItem(h.getHospitalId() + " - " + h.getHospitalName());
        }

        requestsTableModel.setRowCount(0);
        for (AdmissionRequest r : store.getAdmissionRequests()) {
            requestsTableModel.addRow(new Object[]{
                r.getRequestId(),
                r.getPatient() != null ? r.getPatient().getPatientId() : "N/A",
                r.getPatient() != null ? r.getPatient().getName() : "N/A",
                r.getBedRequirement(),
                r.getHospitalId(),
                r.getStatus()
            });
        }

        updateGeneratedReqId();
    }
}
