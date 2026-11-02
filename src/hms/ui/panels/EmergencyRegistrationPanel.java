package hms.ui.panels;

import hms.Doctor;
import hms.Patient;
import hms.ui.UITheme;
import hms.ui.components.StyledButton;
import hms.ui.demo.DemoDataStore;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Dedicated Emergency Patient Registration Screen.
 * Implements rapid triage, high-priority intake (Severity 1 / 2),
 * automated trauma doctor assignment, and fast-track admission/referral routing.
 */
public class EmergencyRegistrationPanel extends JPanel {

    private final DemoDataStore store;
    private final Consumer<String> onNavigate;

    private final JTextField txtName;
    private final JTextField txtAge;
    private final JComboBox<String> cmbGender;
    private final JTextField txtContact;
    private final JComboBox<String> cmbSeverity;
    private final JComboBox<String> cmbDepartment;
    private final JComboBox<String> cmbDoctor;
    private final JTextArea txtTraumaNotes;
    private final JLabel lblStatusMsg;

    public EmergencyRegistrationPanel(Consumer<String> onNavigate) {
        this.store = DemoDataStore.getInstance();
        this.onNavigate = onNavigate;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header Title
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setOpaque(false);
        JLabel lblHeading = new JLabel("🚨 EMERGENCY PATIENT REGISTRATION & RAPID TRIAGE");
        lblHeading.setFont(UITheme.FONT_PAGE_TITLE);
        lblHeading.setForeground(new Color(0xDC, 0x26, 0x26)); // Emergency Red
        JLabel lblSub = new JLabel("High-priority emergency intake • Bypasses routine queues • Auto-routes to ICU/Trauma");
        lblSub.setFont(UITheme.FONT_SUBTITLE);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        header.add(lblHeading);
        header.add(lblSub);
        add(header, BorderLayout.NORTH);

        // Center: Form Card
        JPanel centerCard = UITheme.createCardPanel();
        centerCard.setLayout(new BorderLayout(0, 16));

        // Academic Notice Banner
        JPanel noticeBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        noticeBanner.setBackground(new Color(0xFE, 0xF2, 0xF2));
        noticeBanner.setBorder(BorderFactory.createLineBorder(new Color(0xFE, 0xCA, 0xCA), 1, true));
        JLabel lblNotice = new JLabel("⚠ ACADEMIC SIMULATION: Priority 1 (Critical) and Priority 2 (High) emergency arrivals receive immediate queue escalation.");
        lblNotice.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblNotice.setForeground(new Color(0x99, 0x1B, 0x1B));
        noticeBanner.add(lblNotice);
        centerCard.add(noticeBanner, BorderLayout.NORTH);

        // Form Grid
        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Name & Age
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.5;
        JPanel pName = new JPanel(new BorderLayout(0, 4)); pName.setOpaque(false);
        pName.add(UITheme.createFormLabel("PATIENT FULL NAME *"), BorderLayout.NORTH);
        txtName = UITheme.createTextField(20);
        pName.add(txtName, BorderLayout.CENTER);
        formGrid.add(pName, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.5;
        JPanel pAge = new JPanel(new BorderLayout(0, 4)); pAge.setOpaque(false);
        pAge.add(UITheme.createFormLabel("AGE *"), BorderLayout.NORTH);
        txtAge = UITheme.createTextField(5);
        pAge.add(txtAge, BorderLayout.CENTER);
        formGrid.add(pAge, gbc);

        // Row 1: Gender & Contact Number
        gbc.gridx = 0; gbc.gridy = 1;
        JPanel pGender = new JPanel(new BorderLayout(0, 4)); pGender.setOpaque(false);
        pGender.add(UITheme.createFormLabel("GENDER *"), BorderLayout.NORTH);
        cmbGender = UITheme.createComboBox(new String[]{"Male", "Female", "Other", "Unknown"});
        pGender.add(cmbGender, BorderLayout.CENTER);
        formGrid.add(pGender, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        JPanel pContact = new JPanel(new BorderLayout(0, 4)); pContact.setOpaque(false);
        pContact.add(UITheme.createFormLabel("EMERGENCY CONTACT / ATTENDANT PHONE"), BorderLayout.NORTH);
        txtContact = UITheme.createTextField(15);
        pContact.add(txtContact, BorderLayout.CENTER);
        formGrid.add(pContact, gbc);

        // Row 2: Severity Triage & Department
        gbc.gridx = 0; gbc.gridy = 2;
        JPanel pSeverity = new JPanel(new BorderLayout(0, 4)); pSeverity.setOpaque(false);
        pSeverity.add(UITheme.createFormLabel("TRIAGE SEVERITY LEVEL *"), BorderLayout.NORTH);
        cmbSeverity = UITheme.createComboBox(new String[]{
            "Level 1 - Critical (Immediate Resuscitation / ICU)",
            "Level 2 - High (Severe Acute Trauma / Surgery)",
            "Level 3 - Medium (Urgent Evaluation)"
        });
        pSeverity.add(cmbSeverity, BorderLayout.CENTER);
        formGrid.add(pSeverity, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        JPanel pDept = new JPanel(new BorderLayout(0, 4)); pDept.setOpaque(false);
        pDept.add(UITheme.createFormLabel("EMERGENCY CLINICAL UNIT *"), BorderLayout.NORTH);
        cmbDepartment = UITheme.createComboBox(new String[]{
            "Emergency / Trauma",
            "ICU / Critical Care",
            "Surgery",
            "Cardiology",
            "General Medicine"
        });
        pDept.add(cmbDepartment, BorderLayout.CENTER);
        formGrid.add(pDept, gbc);

        // Row 3: Assigned Physician
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JPanel pDoc = new JPanel(new BorderLayout(0, 4)); pDoc.setOpaque(false);
        pDoc.add(UITheme.createFormLabel("ASSIGN ATTENDING EMERGENCY PHYSICIAN *"), BorderLayout.NORTH);
        cmbDoctor = UITheme.createComboBox(new String[]{});
        populateDoctors();
        pDoc.add(cmbDoctor, BorderLayout.CENTER);
        formGrid.add(pDoc, gbc);

        // Row 4: Initial Trauma & Symptoms Notes
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JPanel pNotes = new JPanel(new BorderLayout(0, 4)); pNotes.setOpaque(false);
        pNotes.add(UITheme.createFormLabel("INITIAL TRAUMA OBSERVATION & TRIAGE NOTES"), BorderLayout.NORTH);
        txtTraumaNotes = new JTextArea(3, 30);
        txtTraumaNotes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTraumaNotes.setLineWrap(true);
        txtTraumaNotes.setWrapStyleWord(true);
        JScrollPane scrollNotes = new JScrollPane(txtTraumaNotes);
        scrollNotes.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        pNotes.add(scrollNotes, BorderLayout.CENTER);
        formGrid.add(pNotes, gbc);

        // Row 5: Status Message
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        lblStatusMsg = new JLabel(" ");
        lblStatusMsg.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formGrid.add(lblStatusMsg, gbc);

        // Row 6: Action Buttons
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        btnPanel.setOpaque(false);

        StyledButton btnClear = new StyledButton("Clear Fields", StyledButton.Type.SECONDARY);
        btnClear.setPreferredSize(new Dimension(120, 36));
        btnClear.addActionListener(e -> clearForm());
        btnPanel.add(btnClear);

        StyledButton btnSubmit = new StyledButton("🚨 Admit Emergency Patient", StyledButton.Type.DANGER);
        btnSubmit.setPreferredSize(new Dimension(220, 36));
        btnSubmit.addActionListener(e -> handleEmergencyIntake());
        btnPanel.add(btnSubmit);

        formGrid.add(btnPanel, gbc);

        centerCard.add(formGrid, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(centerCard);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        add(scroll, BorderLayout.CENTER);

        store.addChangeListener(this::populateDoctors);
    }

    private void populateDoctors() {
        cmbDoctor.removeAllItems();
        List<Doctor> docs = store.getDoctors();
        for (Doctor d : docs) {
            if (d.isOnDuty()) {
                cmbDoctor.addItem(d.getName() + " (" + d.getDepartment() + ") [" + d.getDoctorId() + "]");
            }
        }
        if (cmbDoctor.getItemCount() == 0) {
            for (Doctor d : docs) {
                cmbDoctor.addItem(d.getName() + " (" + d.getDepartment() + ") [" + d.getDoctorId() + "]");
            }
        }
    }

    private void clearForm() {
        txtName.setText("");
        txtAge.setText("");
        txtContact.setText("");
        txtTraumaNotes.setText("");
        cmbSeverity.setSelectedIndex(0);
        cmbDepartment.setSelectedIndex(0);
        lblStatusMsg.setText(" ");
    }

    private void handleEmergencyIntake() {
        String name = txtName.getText().trim();
        String ageStr = txtAge.getText().trim();
        String gender = (String) cmbGender.getSelectedItem();
        String contact = txtContact.getText().trim();
        String dept = (String) cmbDepartment.getSelectedItem();
        String docItem = (String) cmbDoctor.getSelectedItem();
        String notes = txtTraumaNotes.getText().trim();

        if (name.isEmpty()) {
            lblStatusMsg.setForeground(UITheme.DANGER_RED);
            lblStatusMsg.setText("⚠ Please enter the patient's name.");
            txtName.requestFocus();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (age <= 0 || age > 125) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            lblStatusMsg.setForeground(UITheme.DANGER_RED);
            lblStatusMsg.setText("⚠ Please enter a valid age between 1 and 125.");
            txtAge.requestFocus();
            return;
        }

        int severity = 1;
        int sevIndex = cmbSeverity.getSelectedIndex();
        if (sevIndex == 0) severity = 1;
        else if (sevIndex == 1) severity = 2;
        else severity = 3;

        String docName = (docItem != null) ? docItem.split("\\(")[0].trim() : "On-Duty Trauma Team";
        String docId = "DOC-01";
        if (docItem != null && docItem.contains("[") && docItem.contains("]")) {
            docId = docItem.substring(docItem.indexOf('[') + 1, docItem.indexOf(']')).trim();
        }

        Patient p = store.registerEmergencyPatient(name, age, gender, contact, severity, dept, docName);
        if (p != null) {
            // Automatically log an initial emergency encounter
            store.saveConsultation(p.getPatientId(), docId, docName, dept, "H001",
                notes.isEmpty() ? "Emergency arrival triaged as Priority " + severity : notes,
                "Acute Emergency Triage Level " + severity,
                "Immediate medical resuscitation and clinical monitoring",
                notes);

            lblStatusMsg.setForeground(UITheme.SUCCESS_GREEN);
            lblStatusMsg.setText("✔ Patient " + p.getPatientId() + " (" + name + ") registered as PRIORITY " + severity + " in Emergency Queue!");

            int choice = JOptionPane.showOptionDialog(this,
                "Emergency Patient " + p.getPatientId() + " successfully triaged into Priority Queue!\n\n" +
                "Patient: " + name + " (Age " + age + ")\n" +
                "Priority: Level " + severity + " (Highest Priority Escalation)\n" +
                "Attending Physician: " + docName + "\n\n" +
                "Would you like to navigate to Bed Management or Priority Queue now?",
                "Emergency Triage Complete",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Bed Management", "Priority Queue", "Stay Here"},
                "Bed Management");

            clearForm();

            if (choice == 0 && onNavigate != null) {
                onNavigate.accept("Bed Management");
            } else if (choice == 1 && onNavigate != null) {
                onNavigate.accept("Priority Queue");
            }
        }
    }
}
