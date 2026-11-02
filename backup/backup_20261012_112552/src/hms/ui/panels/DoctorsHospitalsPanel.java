package hms.ui.panels;

import hms.Doctor;
import hms.Hospital;
import hms.ui.UITheme;
import hms.ui.components.ModernTablePanel;
import hms.ui.components.StyledButton;
import hms.ui.demo.DemoDataStore;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Doctors and Hospitals Panel featuring tabbed views of hospital network facilities
 * and clinical medical staff on-duty statuses.
 */
public class DoctorsHospitalsPanel extends JPanel {

    private final DemoDataStore store;

    // Hospitals Tab
    private final DefaultTableModel hospitalsTableModel;
    private final ModernTablePanel hospitalsTablePanel;
    private final JTextField txtSearchHospital;

    // Doctors Tab
    private final DefaultTableModel doctorsTableModel;
    private final ModernTablePanel doctorsTablePanel;
    private final JTextField txtSearchDoctor;

    public DoctorsHospitalsPanel() {
        this.store = DemoDataStore.getInstance();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header Title
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setOpaque(false);
        JLabel lblHeading = new JLabel("HEALTHCARE FACILITIES & CLINICAL DIRECTORY");
        lblHeading.setFont(UITheme.FONT_PAGE_TITLE);
        lblHeading.setForeground(UITheme.PRIMARY_NAVY);
        JLabel lblSub = new JLabel("Government network hospitals, baseline bed quotas, and attending physician rosters");
        lblSub.setFont(UITheme.FONT_SUBTITLE);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        header.add(lblHeading);
        header.add(lblSub);
        add(header, BorderLayout.NORTH);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BUTTON);

        // ================= TAB 1: HOSPITALS =================
        JPanel panelHospitals = new JPanel(new BorderLayout(0, 12));
        panelHospitals.setBackground(UITheme.LIGHT_BG);
        panelHospitals.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel hospToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        hospToolbar.setOpaque(false);
        hospToolbar.add(UITheme.createFormLabel("Search Hospital:"));
        txtSearchHospital = UITheme.createTextField(14);
        txtSearchHospital.addActionListener(e -> refreshHospitals());
        hospToolbar.add(txtSearchHospital);

        StyledButton btnHospSearch = new StyledButton("Search", StyledButton.Type.PRIMARY);
        btnHospSearch.setPreferredSize(new Dimension(85, 32));
        btnHospSearch.addActionListener(e -> refreshHospitals());
        hospToolbar.add(btnHospSearch);

        StyledButton btnHospRefresh = new StyledButton("🔄 Refresh", StyledButton.Type.SECONDARY);
        btnHospRefresh.setPreferredSize(new Dimension(95, 32));
        btnHospRefresh.addActionListener(e -> {
            txtSearchHospital.setText("");
            refreshHospitals();
        });
        hospToolbar.add(btnHospRefresh);

        panelHospitals.add(hospToolbar, BorderLayout.NORTH);

        String[] hospCols = {
            "Hospital ID", "Hospital Name", "District Location",
            "Gen Bed Quota", "ICU Bed Quota", "Available Gen Beds", "Available ICU Beds", "Network Status"
        };
        hospitalsTableModel = new DefaultTableModel(hospCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        hospitalsTablePanel = new ModernTablePanel(hospitalsTableModel);
        hospitalsTablePanel.applyStatusRendererToColumn(7);
        panelHospitals.add(hospitalsTablePanel, BorderLayout.CENTER);

        tabbedPane.addTab("🏥 Hospitals Network", panelHospitals);

        // ================= TAB 2: DOCTORS =================
        JPanel panelDoctors = new JPanel(new BorderLayout(0, 12));
        panelDoctors.setBackground(UITheme.LIGHT_BG);
        panelDoctors.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel docToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        docToolbar.setOpaque(false);
        docToolbar.add(UITheme.createFormLabel("Search Doctor / Dept:"));
        txtSearchDoctor = UITheme.createTextField(14);
        txtSearchDoctor.addActionListener(e -> refreshDoctors());
        docToolbar.add(txtSearchDoctor);

        StyledButton btnDocSearch = new StyledButton("Search", StyledButton.Type.PRIMARY);
        btnDocSearch.setPreferredSize(new Dimension(85, 32));
        btnDocSearch.addActionListener(e -> refreshDoctors());
        docToolbar.add(btnDocSearch);

        StyledButton btnToggleDuty = new StyledButton("⚡ Toggle Duty Status", StyledButton.Type.SUCCESS);
        btnToggleDuty.setPreferredSize(new Dimension(165, 32));
        btnToggleDuty.addActionListener(e -> handleToggleDuty());
        docToolbar.add(btnToggleDuty);

        StyledButton btnDocRefresh = new StyledButton("🔄 Refresh", StyledButton.Type.SECONDARY);
        btnDocRefresh.setPreferredSize(new Dimension(95, 32));
        btnDocRefresh.addActionListener(e -> {
            txtSearchDoctor.setText("");
            refreshDoctors();
        });
        docToolbar.add(btnDocRefresh);

        panelDoctors.add(docToolbar, BorderLayout.NORTH);

        String[] docCols = {
            "Doctor ID", "Physician Name", "Specialty Department",
            "Hospital Facility", "Shift Duty Status"
        };
        doctorsTableModel = new DefaultTableModel(docCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        doctorsTablePanel = new ModernTablePanel(doctorsTableModel);
        doctorsTablePanel.applyStatusRendererToColumn(4);
        panelDoctors.add(doctorsTablePanel, BorderLayout.CENTER);

        tabbedPane.addTab("👨‍⚕️ Medical Doctors Roster", panelDoctors);

        add(tabbedPane, BorderLayout.CENTER);

        store.addChangeListener(this::refreshAll);
        refreshAll();
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

    public void refreshAll() {
        refreshHospitals();
        refreshDoctors();
    }

    private void refreshHospitals() {
        String q = txtSearchHospital.getText().trim().toLowerCase();
        hospitalsTableModel.setRowCount(0);

        for (Hospital h : store.getHospitals()) {
            if (q.isEmpty() || h.getHospitalName().toLowerCase().contains(q) || h.getHospitalId().toLowerCase().contains(q) || h.getLocation().toLowerCase().contains(q)) {
                hospitalsTableModel.addRow(new Object[]{
                    h.getHospitalId(),
                    h.getHospitalName(),
                    h.getLocation(),
                    h.getGeneralBedCapacity(),
                    h.getIcuBedCapacity(),
                    h.getAvailableGeneralBedsCount(),
                    h.getAvailableICUBedsCount(),
                    "ACTIVE NODE"
                });
            }
        }
    }

    private void refreshDoctors() {
        String q = txtSearchDoctor.getText().trim().toLowerCase();
        doctorsTableModel.setRowCount(0);

        for (Doctor d : store.getDoctors()) {
            if (q.isEmpty() || d.getName().toLowerCase().contains(q) || d.getDepartment().toLowerCase().contains(q) || d.getDoctorId().toLowerCase().contains(q)) {
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
