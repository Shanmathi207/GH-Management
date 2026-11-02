package hms.ui.panels;

import hms.Patient;
import hms.ui.UITheme;
import hms.ui.components.FormPanel;
import hms.ui.components.SidebarPanel;
import hms.ui.components.StyledButton;
import hms.ui.demo.DemoDataStore;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Patient Registration Form Panel for registering new clinical visits,
 * performing client-side validations, and updating shared triage queues.
 */
public class PatientRegistrationPanel extends JPanel {

    private final DemoDataStore store;
    private final Consumer<String> navigationConsumer;

    private final JTextField txtPatientId;
    private final JTextField txtFullName;
    private final JTextField txtAge;
    private final JComboBox<String> cmbGender;
    private final JTextField txtContact;
    private final JTextField txtAddress;
    private final JComboBox<String> cmbVisitType;
    private final JComboBox<String> cmbSeverity;
    private final JComboBox<String> cmbDepartment;
    private final JTextField txtRegDateTime;

    public PatientRegistrationPanel(Consumer<String> navigationConsumer) {
        this.store = DemoDataStore.getInstance();
        this.navigationConsumer = navigationConsumer;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header Title
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setOpaque(false);
        JLabel lblHeading = new JLabel("PATIENT REGISTRATION");
        lblHeading.setFont(UITheme.FONT_PAGE_TITLE);
        lblHeading.setForeground(UITheme.PRIMARY_NAVY);
        JLabel lblSub = new JLabel("Clinical intake, initial emergency triage scoring, and priority queue enrollment");
        lblSub.setFont(UITheme.FONT_SUBTITLE);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        header.add(lblHeading);
        header.add(lblSub);
        add(header, BorderLayout.NORTH);

        // Center: Form inside rounded card
        JPanel cardContainer = new JPanel(new GridBagLayout());
        cardContainer.setOpaque(false);

        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 16));
        card.setPreferredSize(new Dimension(860, 680));

        FormPanel form = new FormPanel("New Patient Intake Information");

        // 1. Patient ID (Read-only generated demo ID)
        txtPatientId = UITheme.createTextField(20);
        txtPatientId.setEditable(false);
        txtPatientId.setBackground(new Color(0xF1, 0xF5, 0xF9));
        updateGeneratedId();
        form.addField("Generated Patient ID:", txtPatientId);

        // 2. Full Name
        txtFullName = UITheme.createTextField(20);
        form.addField("Full Name *:", txtFullName);

        // 3. Age
        txtAge = UITheme.createTextField(20);
        form.addField("Age * (1 - 125):", txtAge);

        // 4. Gender
        String[] genders = {"Male", "Female", "Other"};
        cmbGender = UITheme.createComboBox(genders);
        form.addField("Gender:", cmbGender);

        // 5. Contact Number
        txtContact = UITheme.createTextField(20);
        form.addField("Contact Number *:", txtContact);

        // 6. Address
        txtAddress = UITheme.createTextField(20);
        form.addField("Address / Residence:", txtAddress);

        // 7. Visit Type (IPD is only assigned through doctor-recommended admission workflow)
        String[] visitTypes = {"OPD", "EMERGENCY"};
        cmbVisitType = UITheme.createComboBox(visitTypes);
        form.addField("Visit Type *:", cmbVisitType);

        // 8. Severity (1 to 5)
        String[] severities = {
            "1 - Critical (Immediate Resuscitation)",
            "2 - High (Emergency / Urgent Care)",
            "3 - Medium (Semi-Urgent Consultation)",
            "4 - Low (Standard OPD Intake)",
            "5 - Non-Urgent (Routine Follow-up)"
        };
        cmbSeverity = UITheme.createComboBox(severities);
        cmbSeverity.setSelectedIndex(2); // default to 3
        form.addField("Triage Severity (1 to 5) *:", cmbSeverity);

        // 9. Department
        String[] depts = {
            "General Medicine", "Cardiology", "ICU / Critical Care",
            "Surgery", "Pediatrics", "Orthopedics", "Emergency & Trauma"
        };
        cmbDepartment = UITheme.createComboBox(depts);
        form.addField("Target Department:", cmbDepartment);

        // 10. Registration Timestamp (Read-only)
        txtRegDateTime = UITheme.createTextField(20);
        txtRegDateTime.setEditable(false);
        txtRegDateTime.setBackground(new Color(0xF1, 0xF5, 0xF9));
        txtRegDateTime.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        form.addField("Registration Date & Time:", txtRegDateTime);

        // Informational banner
        JLabel lblNotice = new JLabel("ℹ️ Demo Prototype Notice: Registered patients are permanently saved to MySQL and queued into Java PriorityQueue.");
        lblNotice.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblNotice.setForeground(UITheme.TEXT_MUTED);
        lblNotice.setBorder(new EmptyBorder(8, 0, 8, 0));
        form.addFullWidthComponent(lblNotice);

        // Buttons
        StyledButton btnRegister = new StyledButton("Register Patient", StyledButton.Type.PRIMARY);
        btnRegister.addActionListener(e -> handleRegister());

        StyledButton btnClear = new StyledButton("Clear Form", StyledButton.Type.SECONDARY);
        btnClear.addActionListener(e -> clearForm());

        StyledButton btnViewRecords = new StyledButton("View Patient Records", StyledButton.Type.NAVY);
        btnViewRecords.addActionListener(e -> {
            if (navigationConsumer != null) {
                navigationConsumer.accept(SidebarPanel.NAV_RECORDS);
            }
        });

        form.addActionButton(btnClear);
        form.addActionButton(btnViewRecords);
        form.addActionButton(btnRegister);

        card.add(form, BorderLayout.CENTER);
        cardContainer.add(card);

        JScrollPane scroll = new JScrollPane(cardContainer);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UITheme.LIGHT_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        add(scroll, BorderLayout.CENTER);
    }

    private void updateGeneratedId() {
        int nextIdNum = store.getPatients().size() + 101;
        txtPatientId.setText("PAT-" + nextIdNum + " (Auto-assigned)");
    }

    private void handleRegister() {
        String name = txtFullName.getText().trim();
        String ageStr = txtAge.getText().trim();
        String contact = txtContact.getText().trim();
        String address = txtAddress.getText().trim();

        // Validations
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Patient Full Name is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtFullName.requestFocus();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (age <= 0 || age > 125) {
                JOptionPane.showMessageDialog(this, "Please enter a valid age between 1 and 125.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                txtAge.requestFocus();
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Age must be a valid numeric integer.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtAge.requestFocus();
            return;
        }

        if (contact.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Contact Number is required for patient tracking.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtContact.requestFocus();
            return;
        }

        String gender = (String) cmbGender.getSelectedItem();
        String visitType = (String) cmbVisitType.getSelectedItem();
        int severity = cmbSeverity.getSelectedIndex() + 1; // 1 to 5
        String dept = (String) cmbDepartment.getSelectedItem();

        // Register in shared DemoDataStore
        Patient newPatient = store.registerPatient(name, age, gender, contact, address, visitType, severity, dept);

        JOptionPane.showMessageDialog(this,
            "SUCCESSFULLY REGISTERED PATIENT\n\n" +
            "Patient ID    : " + newPatient.getPatientId() + "\n" +
            "Name          : " + newPatient.getName() + "\n" +
            "Age / Gender  : " + newPatient.getAge() + " / " + newPatient.getGender() + "\n" +
            "Contact       : " + newPatient.getContactNumber() + "\n" +
            "Visit Type    : " + newPatient.getVisitType() + "\n" +
            "Severity      : Priority " + newPatient.getPriority() + " (" + newPatient.getEmergencyLevel() + ")\n" +
            "Department    : " + newPatient.getDepartment() + "\n\n" +
            "The patient has been permanently recorded and enrolled in\n" +
            "the Java PriorityQueue for clinical triage scheduling.",
            "Registration Confirmed",
            JOptionPane.INFORMATION_MESSAGE);

        clearForm();
    }

    private void clearForm() {
        txtFullName.setText("");
        txtAge.setText("");
        txtContact.setText("");
        txtAddress.setText("");
        cmbGender.setSelectedIndex(0);
        cmbVisitType.setSelectedIndex(0);
        cmbSeverity.setSelectedIndex(2);
        cmbDepartment.setSelectedIndex(0);
        txtRegDateTime.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        updateGeneratedId();
    }
}
