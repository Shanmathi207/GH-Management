package hms.ui.panels;

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
 * Dedicated Hospital Management Screen.
 * Displays healthcare facilities network, district locations, inpatient capacities,
 * and live bed occupancy totals across connected institutions.
 */
public class HospitalManagementPanel extends JPanel {

    private final DemoDataStore store;
    private final DefaultTableModel hospitalsTableModel;
    private final ModernTablePanel hospitalsTablePanel;
    private final JTextField txtSearchHospital;

    public HospitalManagementPanel() {
        this.store = DemoDataStore.getInstance();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header Title
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setOpaque(false);
        JLabel lblHeading = new JLabel("🏥 GOVERNMENT HOSPITALS & HEALTHCARE NETWORK");
        lblHeading.setFont(UITheme.FONT_PAGE_TITLE);
        lblHeading.setForeground(UITheme.PRIMARY_NAVY);
        JLabel lblSub = new JLabel("Directory of public district hospitals, emergency trauma facilities, and centralized bed grid capacity");
        lblSub.setFont(UITheme.FONT_SUBTITLE);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        header.add(lblHeading);
        header.add(lblSub);
        add(header, BorderLayout.NORTH);

        // Center Card Panel
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        // Toolbar
        JPanel hospToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        hospToolbar.setOpaque(false);

        hospToolbar.add(UITheme.createFormLabel("Search Facility / Location:"));
        txtSearchHospital = UITheme.createTextField(16);
        txtSearchHospital.addActionListener(e -> refreshHospitals());
        hospToolbar.add(txtSearchHospital);

        StyledButton btnHospSearch = new StyledButton("Search", StyledButton.Type.PRIMARY);
        btnHospSearch.setPreferredSize(new Dimension(85, 32));
        btnHospSearch.addActionListener(e -> refreshHospitals());
        hospToolbar.add(btnHospSearch);

        StyledButton btnViewDetails = new StyledButton("ℹ️ Facility Details", StyledButton.Type.NAVY);
        btnViewDetails.setPreferredSize(new Dimension(145, 32));
        btnViewDetails.addActionListener(e -> showFacilityDetails());
        hospToolbar.add(btnViewDetails);

        StyledButton btnHospRefresh = new StyledButton("🔄 Refresh", StyledButton.Type.SECONDARY);
        btnHospRefresh.setPreferredSize(new Dimension(95, 32));
        btnHospRefresh.addActionListener(e -> {
            txtSearchHospital.setText("");
            refreshHospitals();
        });
        hospToolbar.add(btnHospRefresh);

        card.add(hospToolbar, BorderLayout.NORTH);

        // Table
        String[] hospCols = {
            "Hospital ID", "Facility Name", "District Location",
            "General Bed Quota", "ICU Bed Quota", "Available Gen Beds", "Available ICU Beds", "Network Node"
        };
        hospitalsTableModel = new DefaultTableModel(hospCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        hospitalsTablePanel = new ModernTablePanel(hospitalsTableModel);
        hospitalsTablePanel.setColumnWidths(90, 240, 130, 120, 110, 120, 120, 100);
        hospitalsTablePanel.applyStatusRendererToColumn(7); // Status badge
        card.add(hospitalsTablePanel, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);

        store.addChangeListener(this::refreshHospitals);
        refreshHospitals();
    }

    private void showFacilityDetails() {
        int row = hospitalsTablePanel.getTable().getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a hospital row to inspect details.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String hospId = (String) hospitalsTableModel.getValueAt(row, 0);
        Hospital h = store.getHospitalById(hospId);
        if (h != null) {
            String details = String.format(
                "Hospital Facility Overview:\n\n" +
                "ID: %s\n" +
                "Name: %s\n" +
                "District Location: %s\n" +
                "General Bed Capacity: %d (Available: %d)\n" +
                "ICU Bed Capacity: %d (Available: %d)\n" +
                "Affiliated Doctors: %d\n" +
                "Status: Active District Network Node\n",
                h.getHospitalId(), h.getHospitalName(), h.getLocation(),
                h.getGeneralBedCapacity(), h.getAvailableGeneralBedsCount(),
                h.getIcuBedCapacity(), h.getAvailableICUBedsCount(),
                h.getDoctors().size()
            );
            JOptionPane.showMessageDialog(this, details, "Facility Information - " + h.getHospitalName(), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void refreshHospitals() {
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
}
