package hms.ui.panels;

import hms.Doctor;
import hms.Hospital;
import hms.dao.DoctorDAO;
import hms.db.DBConnection;
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
 * Dedicated Doctor Management Screen.
 * Displays medical staff directory, specialty departments, on-duty scheduling,
 * full physician profiles, and allows registering new physicians directly into MySQL.
 */
public class DoctorManagementPanel extends JPanel {

    private final DemoDataStore store;
    private final DefaultTableModel doctorsTableModel;
    private final ModernTablePanel doctorsTablePanel;
    private final JTextField txtSearchDoctor;
    private final JComboBox<String> cmbDeptFilter;

    public DoctorManagementPanel() {
        this.store = DemoDataStore.getInstance();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header Title
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setOpaque(false);
        JLabel lblHeading = new JLabel("MEDICAL PHYSICIANS & ROSTER DIRECTORY");
        lblHeading.setFont(UITheme.FONT_PAGE_TITLE);
        lblHeading.setForeground(UITheme.PRIMARY_NAVY);
        JLabel lblSub = new JLabel("Directory of government medical officers, clinical department rosters, and active duty status");
        lblSub.setFont(UITheme.FONT_SUBTITLE);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        header.add(lblHeading);
        header.add(lblSub);
        add(header, BorderLayout.NORTH);

        // Center Card Panel
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        // Toolbar
        JPanel docToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        docToolbar.setOpaque(false);

        docToolbar.add(UITheme.createFormLabel("Search Doctor:"));
        txtSearchDoctor = UITheme.createTextField(12);
        txtSearchDoctor.addActionListener(e -> refreshDoctors());
        docToolbar.add(txtSearchDoctor);

        docToolbar.add(UITheme.createFormLabel("Department:"));
        cmbDeptFilter = UITheme.createComboBox(new String[]{
            "ALL", "Cardiology", "ICU / Critical Care", "General Medicine", "Surgery", "Pediatrics", "Emergency / Trauma", "Neurology"
        });
        cmbDeptFilter.addActionListener(e -> refreshDoctors());
        docToolbar.add(cmbDeptFilter);

        StyledButton btnSearch = new StyledButton("Search", StyledButton.Type.PRIMARY);
        btnSearch.setPreferredSize(new Dimension(85, 32));
        btnSearch.addActionListener(e -> refreshDoctors());
        docToolbar.add(btnSearch);

        StyledButton btnToggleDuty = new StyledButton("Toggle Duty Status", StyledButton.Type.SUCCESS);
        btnToggleDuty.setPreferredSize(new Dimension(150, 32));
        btnToggleDuty.addActionListener(e -> handleToggleDuty());
        docToolbar.add(btnToggleDuty);

        StyledButton btnViewDetails = new StyledButton("View Doctor Profile", StyledButton.Type.NAVY);
        btnViewDetails.setPreferredSize(new Dimension(155, 32));
        btnViewDetails.addActionListener(e -> showDoctorDetails());
        docToolbar.add(btnViewDetails);

        StyledButton btnAddDoc = new StyledButton("Add Physician", StyledButton.Type.PRIMARY);
        btnAddDoc.setPreferredSize(new Dimension(125, 32));
        btnAddDoc.addActionListener(e -> showAddDoctorDialog());
        docToolbar.add(btnAddDoc);

        StyledButton btnRefresh = new StyledButton("Refresh", StyledButton.Type.SECONDARY);
        btnRefresh.setPreferredSize(new Dimension(85, 32));
        btnRefresh.addActionListener(e -> {
            txtSearchDoctor.setText("");
            cmbDeptFilter.setSelectedIndex(0);
            refreshDoctors();
        });
        docToolbar.add(btnRefresh);

        card.add(docToolbar, BorderLayout.NORTH);

        // Table
        String[] docCols = {
            "Doctor ID", "Physician Name", "Specialty Department", "Affiliated Hospital", "Current Duty Status"
        };
        doctorsTableModel = new DefaultTableModel(docCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        doctorsTablePanel = new ModernTablePanel(doctorsTableModel);
        doctorsTablePanel.setColumnWidths(95, 180, 160, 200, 120);
        doctorsTablePanel.applyStatusRendererToColumn(4); // Duty status badge
        card.add(doctorsTablePanel, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);

        store.addChangeListener(this::refreshDoctors);
        refreshDoctors();
    }

    private void handleToggleDuty() {
        int row = doctorsTablePanel.getTable().getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a physician row to toggle duty status.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String docId = (String) doctorsTableModel.getValueAt(row, 0);
        Doctor doc = store.getDoctorById(docId);
        if (doc != null) {
            boolean newStatus = !doc.isOnDuty();
            store.updateDoctorDuty(docId, newStatus);
            JOptionPane.showMessageDialog(this,
                doc.getName() + " is now " + (newStatus ? "ON DUTY" : "OFF DUTY") + ".",
                "Duty Status Updated",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showDoctorDetails() {
        int row = doctorsTablePanel.getTable().getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a physician row to inspect profile details.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String docId = (String) doctorsTableModel.getValueAt(row, 0);
        Doctor doc = store.getDoctorById(docId);
        if (doc == null) return;

        // Calculate consultations count
        int encCount = 0;
        for (hms.Encounter enc : store.getEncounters()) {
            if (docId.equalsIgnoreCase(enc.getDoctorId()) || doc.getName().equalsIgnoreCase(enc.getDoctorName())) {
                encCount++;
            }
        }

        JTextArea detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        detailArea.setText(
            "==========================================================\n" +
            "            ATTENDING PHYSICIAN PROFILE                  \n" +
            "==========================================================\n\n" +
            " Doctor ID            : " + doc.getDoctorId() + "\n" +
            " Full Name            : " + doc.getName() + "\n" +
            " Specialty Department : " + doc.getDepartment() + "\n" +
            " Affiliated Facility  : " + doc.getHospitalName() + " (" + doc.getHospitalId() + ")\n" +
            " Active Shift Status  : " + (doc.isOnDuty() ? "ON DUTY (Active Triage & OPD)" : "OFF DUTY (Shift Complete)") + "\n" +
            " Contact Telephony    : +91 44 2530 5000 (Ext. " + doc.getDoctorId().replaceAll("\\D", "10") + ")\n" +
            " Registered License   : MCI-TN-" + (Math.abs(doc.getDoctorId().hashCode()) % 90000 + 10000) + "\n" +
            " Total Consultations  : " + encCount + " Encounters Documented\n\n" +
            "==========================================================\n" +
            " Verified Clinical Staff Roster • Central Health Administration"
        );
        detailArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(detailArea);
        scroll.setPreferredSize(new Dimension(540, 340));
        JOptionPane.showMessageDialog(this, scroll, "Physician Dossier - " + doc.getName(), JOptionPane.PLAIN_MESSAGE);
    }

    private void showAddDoctorDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register New Physician", true);
        dialog.setSize(420, 360);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 14));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtNewName = UITheme.createTextField(15);
        JComboBox<String> cmbNewDept = UITheme.createComboBox(new String[]{
            "Cardiology", "ICU / Critical Care", "General Medicine", "Surgery", "Pediatrics", "Emergency / Trauma", "Neurology"
        });
        JComboBox<String> cmbNewHosp = UITheme.createComboBox(new String[]{});
        for (Hospital h : store.getHospitals()) {
            cmbNewHosp.addItem(h.getHospitalName() + " [" + h.getHospitalId() + "]");
        }
        JCheckBox chkOnDuty = new JCheckBox("On Active Shift Duty", true);

        form.add(UITheme.createFormLabel("Physician Name:"));
        form.add(txtNewName);
        form.add(UITheme.createFormLabel("Department:"));
        form.add(cmbNewDept);
        form.add(UITheme.createFormLabel("Hospital:"));
        form.add(cmbNewHosp);
        form.add(UITheme.createFormLabel("Shift Status:"));
        form.add(chkOnDuty);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        StyledButton btnSave = new StyledButton("Save Physician", StyledButton.Type.PRIMARY);
        btnSave.addActionListener(e -> {
            String name = txtNewName.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter physician name.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String dept = (String) cmbNewDept.getSelectedItem();
            String hospSel = (String) cmbNewHosp.getSelectedItem();
            String hospId = "H001";
            String hospName = "Government General Hospital";
            if (hospSel != null && hospSel.contains("[") && hospSel.contains("]")) {
                hospId = hospSel.substring(hospSel.indexOf('[') + 1, hospSel.indexOf(']')).trim();
                hospName = hospSel.substring(0, hospSel.indexOf('[')).trim();
            }

            int count = store.getDoctors().size() + 1;
            String docId = "DOC-" + (count < 10 ? "0" + count : count);
            Doctor newDoc = new Doctor(docId, name.startsWith("Dr.") ? name : "Dr. " + name, dept, hospId, hospName, chkOnDuty.isSelected());
            store.addDemoDoctor(newDoc);

            if (DBConnection.isConnected()) {
                new DoctorDAO().save(newDoc);
            }
            store.addHistoryRecord(docId, "STAFF REGISTRATION", "New medical physician " + newDoc.getName() + " registered under " + dept, "Medical Superintendent");
            store.notifyListeners();

            dialog.dispose();
            JOptionPane.showMessageDialog(this, newDoc.getName() + " successfully registered with ID: " + docId, "Physician Registered", JOptionPane.INFORMATION_MESSAGE);
        });

        StyledButton btnCancel = new StyledButton("Cancel", StyledButton.Type.SECONDARY);
        btnCancel.addActionListener(e -> dialog.dispose());

        btnBar.add(btnCancel);
        btnBar.add(btnSave);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnBar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public void refreshDoctors() {
        String q = txtSearchDoctor.getText().trim().toLowerCase();
        String deptFilter = (String) cmbDeptFilter.getSelectedItem();
        doctorsTableModel.setRowCount(0);

        for (Doctor d : store.getDoctors()) {
            boolean matchesQuery = q.isEmpty() || d.getName().toLowerCase().contains(q) || d.getDepartment().toLowerCase().contains(q) || d.getDoctorId().toLowerCase().contains(q);
            boolean matchesDept = deptFilter == null || "ALL".equalsIgnoreCase(deptFilter) || d.getDepartment().equalsIgnoreCase(deptFilter);

            if (matchesQuery && matchesDept) {
                doctorsTableModel.addRow(new Object[]{
                    d.getDoctorId(),
                    d.getName(),
                    d.getDepartment(),
                    d.getHospitalName(),
                    d.isOnDuty() ? "ON DUTY" : "OFF DUTY"
                });
            }
        }
    }
}
