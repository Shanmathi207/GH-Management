package hms.ui.panels;

import hms.Patient;
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
 * Patient Records Panel featuring searchable, filterable JTable
 * displaying patient demographics, visit categories, triage severity, and clinical statuses.
 */
public class PatientRecordsPanel extends JPanel {

    private final DemoDataStore store;
    private final DefaultTableModel tableModel;
    private final ModernTablePanel tablePanel;

    private final JTextField txtSearchId;
    private final JTextField txtSearchName;
    private final JComboBox<String> cmbFilterVisitType;
    private final JLabel lblCountStatus;

    public PatientRecordsPanel() {
        this.store = DemoDataStore.getInstance();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header Title
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setOpaque(false);
        JLabel lblHeading = new JLabel("PATIENT RECORDS DIRECTORY");
        lblHeading.setFont(UITheme.FONT_PAGE_TITLE);
        lblHeading.setForeground(UITheme.PRIMARY_NAVY);
        JLabel lblSub = new JLabel("Master census of registered patients, active clinical visits, and status tracking");
        lblSub.setFont(UITheme.FONT_SUBTITLE);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        header.add(lblHeading);
        header.add(lblSub);
        add(header, BorderLayout.NORTH);

        // Center: Card containing Search toolbar and Table
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        // Filter / Search Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        toolbar.setOpaque(false);

        toolbar.add(UITheme.createFormLabel("Search ID:"));
        txtSearchId = UITheme.createTextField(9);
        toolbar.add(txtSearchId);

        toolbar.add(UITheme.createFormLabel("Name:"));
        txtSearchName = UITheme.createTextField(11);
        toolbar.add(txtSearchName);

        toolbar.add(UITheme.createFormLabel("Visit Type:"));
        String[] types = {"ALL", "OPD", "IPD", "EMERGENCY"};
        cmbFilterVisitType = UITheme.createComboBox(types);
        toolbar.add(cmbFilterVisitType);

        StyledButton btnFilter = new StyledButton("Apply Filter", StyledButton.Type.PRIMARY);
        btnFilter.setPreferredSize(new Dimension(105, 32));
        btnFilter.addActionListener(e -> applyFilter());
        toolbar.add(btnFilter);

        StyledButton btnReset = new StyledButton("Reset", StyledButton.Type.SECONDARY);
        btnReset.setPreferredSize(new Dimension(80, 32));
        btnReset.addActionListener(e -> resetFilter());
        toolbar.add(btnReset);

        StyledButton btnRefresh = new StyledButton("🔄 Refresh", StyledButton.Type.SECONDARY);
        btnRefresh.setPreferredSize(new Dimension(95, 32));
        btnRefresh.addActionListener(e -> applyFilter());
        toolbar.add(btnRefresh);

        StyledButton btnDetails = new StyledButton("📋 View Details", StyledButton.Type.NAVY);
        btnDetails.setPreferredSize(new Dimension(120, 32));
        btnDetails.addActionListener(e -> showPatientDetails());
        toolbar.add(btnDetails);

        card.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Patient ID", "Name", "Age", "Gender", "Visit Type", "Severity", "Status", "Registration Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tablePanel = new ModernTablePanel(tableModel);
        tablePanel.applyStatusRendererToColumn(5); // Severity badge
        tablePanel.applyStatusRendererToColumn(6); // Status badge
        card.add(tablePanel, BorderLayout.CENTER);

        // Bottom status label
        lblCountStatus = new JLabel("Showing all patients");
        lblCountStatus.setFont(UITheme.FONT_SUBTITLE);
        lblCountStatus.setForeground(UITheme.TEXT_MUTED);
        lblCountStatus.setBorder(new EmptyBorder(6, 6, 0, 0));
        card.add(lblCountStatus, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);

        // Real-time filtering as typing
        txtSearchId.addActionListener(e -> applyFilter());
        txtSearchName.addActionListener(e -> applyFilter());
        cmbFilterVisitType.addActionListener(e -> applyFilter());

        // React to store changes
        store.addChangeListener(this::applyFilter);

        applyFilter();
    }

    private void resetFilter() {
        txtSearchId.setText("");
        txtSearchName.setText("");
        cmbFilterVisitType.setSelectedIndex(0);
        applyFilter();
    }

    public void applyFilter() {
        String queryId = txtSearchId.getText().trim().toLowerCase();
        String queryName = txtSearchName.getText().trim().toLowerCase();
        String selectedType = (String) cmbFilterVisitType.getSelectedItem();

        tableModel.setRowCount(0);
        List<Patient> list = store.getPatients();
        int matched = 0;

        for (Patient p : list) {
            boolean matchId = queryId.isEmpty() || p.getPatientId().toLowerCase().contains(queryId);
            boolean matchName = queryName.isEmpty() || p.getName().toLowerCase().contains(queryName);
            boolean matchType = "ALL".equalsIgnoreCase(selectedType) || p.getVisitType().equalsIgnoreCase(selectedType);

            if (matchId && matchName && matchType) {
                tableModel.addRow(new Object[]{
                    p.getPatientId(),
                    p.getName(),
                    p.getAge(),
                    p.getGender(),
                    p.getVisitType(),
                    p.getPriority() + " (" + p.getEmergencyLevel() + ")",
                    p.getStatus(),
                    p.getRegistrationDateTime()
                });
                matched++;
            }
        }

        lblCountStatus.setText("Showing " + matched + " of " + list.size() + " total patient records");
    }

    private void showPatientDetails() {
        int selectedRow = tablePanel.getTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a patient row to view full clinical details.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String pId = (String) tableModel.getValueAt(selectedRow, 0);
        Patient p = store.getPatientById(pId);
        if (p == null) return;

        JTextArea detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        detailArea.setText(
            "==========================================================\n" +
            "            PATIENT CLINICAL DOSSIER                     \n" +
            "==========================================================\n\n" +
            " Patient ID         : " + p.getPatientId() + "\n" +
            " Full Name          : " + p.getName() + "\n" +
            " Age / Gender       : " + p.getAge() + " years / " + p.getGender() + "\n" +
            " Visit Category     : " + p.getVisitType() + "\n" +
            " Department         : " + p.getDepartment() + "\n" +
            " Triage Severity    : Level " + p.getPriority() + " [" + p.getEmergencyLevel() + "]\n" +
            " Required Resource  : " + p.getRequiredResource() + "\n" +
            " Clinical Status    : " + p.getStatus() + "\n" +
            " Assigned Bed       : " + (p.getAssignedBedId() != null ? p.getAssignedBedId() : "None (Outpatient / Pending)") + "\n" +
            " Primary Hospital   : " + p.getAssignedHospitalId() + "\n" +
            " Registered Date    : " + p.getRegistrationDateTime() + "\n" +
            " Queue Arrival Seq  : " + p.getArrivalSequence() + "\n\n" +
            "==========================================================\n" +
            " Note: In-memory demonstration entity. Ready for JDBC DAO."
        );
        detailArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(detailArea);
        scroll.setPreferredSize(new Dimension(540, 360));

        JOptionPane.showMessageDialog(this, scroll, "Patient Details - " + p.getPatientId(), JOptionPane.PLAIN_MESSAGE);
    }
}
