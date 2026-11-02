package hms.ui.panels;

import hms.Hospital;
import hms.Patient;
import hms.ReferralService;
import hms.RoutePlanner;
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
 * Hospital Referrals Panel for inter-facility transfers,
 * Dijkstra shortest path travel calculation, and capacity routing.
 */
public class ReferralPanel extends JPanel {

    private final DemoDataStore store;

    private final JComboBox<String> cmbPatient;
    private final JComboBox<String> cmbSourceHospital;
    private final JComboBox<String> cmbBedType;
    private final JComboBox<String> cmbDestHospital;
    private final JLabel lblRouteResult;
    private final JLabel lblTravelTime;
    private final DefaultTableModel eligibleTableModel;
    private final ModernTablePanel tablePanel;

    public ReferralPanel() {
        this.store = DemoDataStore.getInstance();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header Title
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setOpaque(false);
        JLabel lblHeading = new JLabel("INTER-HOSPITAL PATIENT REFERRALS & ROUTING");
        lblHeading.setFont(UITheme.FONT_PAGE_TITLE);
        lblHeading.setForeground(UITheme.PRIMARY_NAVY);
        JLabel lblSub = new JLabel("Dijkstra's shortest path graph solver for emergency transfers and cross-facility bed coordination");
        lblSub.setFont(UITheme.FONT_SUBTITLE);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        header.add(lblHeading);
        header.add(lblSub);
        add(header, BorderLayout.NORTH);

        // Center split
        JPanel split = new JPanel(new GridLayout(1, 2, 16, 0));
        split.setOpaque(false);

        // Left Card: Form
        JPanel cardForm = UITheme.createCardPanel();
        cardForm.setLayout(new BorderLayout(0, 12));

        FormPanel form = new FormPanel("Transfer Planning Parameters");

        cmbPatient = UITheme.createComboBox(new String[0]);
        form.addField("Select Patient *:", cmbPatient);

        cmbSourceHospital = UITheme.createComboBox(new String[0]);
        cmbSourceHospital.addActionListener(e -> updateDestOptions());
        form.addField("Source Hospital *:", cmbSourceHospital);

        String[] bedTypes = {"General Bed", "ICU Bed"};
        cmbBedType = UITheme.createComboBox(bedTypes);
        cmbBedType.addActionListener(e -> handleFindEligible(false));
        form.addField("Required Bed Type *:", cmbBedType);

        cmbDestHospital = UITheme.createComboBox(new String[0]);
        cmbDestHospital.addActionListener(e -> handleCalculateRoute());
        form.addField("Destination Hospital *:", cmbDestHospital);

        // Route Calculation Display Box
        JPanel routeBox = new JPanel(new GridLayout(3, 1, 4, 4));
        routeBox.setOpaque(true);
        routeBox.setBackground(new Color(0xF1, 0xF5, 0xF9));
        routeBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        lblRouteResult = new JLabel("Route Path: Click 'Calculate Shortest Route'");
        lblRouteResult.setFont(UITheme.FONT_LABEL);
        lblRouteResult.setForeground(UITheme.PRIMARY_NAVY);

        lblTravelTime = new JLabel("Estimated Transit Weight: — minutes");
        lblTravelTime.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTravelTime.setForeground(UITheme.PRIMARY_BLUE);

        JLabel lblDisclaimer = new JLabel("⚠ Warning: Route weights are fictional demonstration values and do not represent actual travel times.");
        lblDisclaimer.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblDisclaimer.setForeground(UITheme.ERROR_RED);

        routeBox.add(lblRouteResult);
        routeBox.add(lblTravelTime);
        routeBox.add(lblDisclaimer);
        form.addFullWidthComponent(routeBox);

        // Buttons
        StyledButton btnFindEligible = new StyledButton("Find Eligible Hospitals", StyledButton.Type.SECONDARY);
        btnFindEligible.addActionListener(e -> handleFindEligible(true));

        StyledButton btnCalcRoute = new StyledButton("Calculate Shortest Route", StyledButton.Type.PRIMARY);
        btnCalcRoute.addActionListener(e -> handleCalculateRoute());

        StyledButton btnConfirm = new StyledButton("🚑 Confirm Referral", StyledButton.Type.SUCCESS);
        btnConfirm.addActionListener(e -> handleConfirmReferral());

        form.addActionButton(btnFindEligible);
        form.addActionButton(btnCalcRoute);
        form.addActionButton(btnConfirm);

        cardForm.add(form, BorderLayout.CENTER);
        split.add(cardForm);

        // Right Card: Eligible Hospitals Table
        JPanel cardTable = UITheme.createCardPanel();
        cardTable.setLayout(new BorderLayout(0, 10));
        JLabel lblTblTitle = UITheme.createSectionTitle("Eligible Destination Hospitals & Capacity");
        cardTable.add(lblTblTitle, BorderLayout.NORTH);

        String[] cols = {"Hospital ID", "Hospital Name", "Compatible Beds", "Dijkstra Route Time", "Status"};
        eligibleTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablePanel = new ModernTablePanel(eligibleTableModel);
        tablePanel.applyStatusRendererToColumn(4);

        // Selecting a table row syncs with the destination dropdown
        tablePanel.getTable().getSelectionModel().addListSelectionListener(e -> {
            int row = tablePanel.getTable().getSelectedRow();
            if (row >= 0) {
                String hId = (String) eligibleTableModel.getValueAt(row, 0);
                for (int i = 0; i < cmbDestHospital.getItemCount(); i++) {
                    if (cmbDestHospital.getItemAt(i).startsWith(hId)) {
                        cmbDestHospital.setSelectedIndex(i);
                        break;
                    }
                }
            }
        });

        cardTable.add(tablePanel, BorderLayout.CENTER);
        split.add(cardTable);

        add(split, BorderLayout.CENTER);

        store.addChangeListener(this::refreshData);
        refreshData();
    }

    private void updateDestOptions() {
        String src = (String) cmbSourceHospital.getSelectedItem();
        String srcId = (src != null && src.contains(" - ")) ? src.split(" - ")[0] : null;

        cmbDestHospital.removeAllItems();
        for (Hospital h : store.getHospitals()) {
            if (srcId == null || !h.getHospitalId().equalsIgnoreCase(srcId)) {
                cmbDestHospital.addItem(h.getHospitalId() + " - " + h.getHospitalName());
            }
        }
    }

    private void handleFindEligible(boolean showMessageIfEmpty) {
        String src = (String) cmbSourceHospital.getSelectedItem();
        String bedReq = (String) cmbBedType.getSelectedItem();
        if (src == null || !src.contains(" - ")) return;
        String srcId = src.split(" - ")[0];

        List<ReferralService.DestinationCandidate> candidates = 
            store.getReferralService().findEligibleDestinations(srcId, bedReq, store.getHospitals());

        eligibleTableModel.setRowCount(0);
        for (ReferralService.DestinationCandidate c : candidates) {
            eligibleTableModel.addRow(new Object[]{
                c.getHospital().getHospitalId(),
                c.getHospital().getHospitalName(),
                c.getAvailableCompatibleBeds() + " Bed(s)",
                c.getEstimatedMinutes() + " mins (Dijkstra)",
                "ELIGIBLE DESTINATION"
            });
        }

        if (showMessageIfEmpty && candidates.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No external hospitals currently have available compatible " + bedReq + "s.",
                "Zero Capacity Available",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private RoutePlanner.RouteResult handleCalculateRoute() {
        String src = (String) cmbSourceHospital.getSelectedItem();
        String dst = (String) cmbDestHospital.getSelectedItem();
        if (src == null || dst == null) return null;

        String srcId = src.split(" - ")[0];
        String dstId = dst.split(" - ")[0];

        RoutePlanner.RouteResult res = store.getRoutePlanner().findShortestPath(srcId, dstId);
        if (res.isFound()) {
            lblRouteResult.setText("Optimal Dijkstra Path: " + res.getPathString());
            lblTravelTime.setText("Estimated Transit Weight: " + res.getTotalMinutes() + " fictional minutes");
        } else {
            lblRouteResult.setText("Route Path: No graph connection found");
            lblTravelTime.setText("Estimated Transit Weight: Indeterminate");
        }
        return res;
    }

    private void handleConfirmReferral() {
        String pat = (String) cmbPatient.getSelectedItem();
        String src = (String) cmbSourceHospital.getSelectedItem();
        String dst = (String) cmbDestHospital.getSelectedItem();
        String bedType = (String) cmbBedType.getSelectedItem();

        if (pat == null || src == null || dst == null) {
            JOptionPane.showMessageDialog(this, "Please select Patient, Source Hospital, and Destination Hospital.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String pId = pat.split(" - ")[0];
        String srcId = src.split(" - ")[0];
        String dstId = dst.split(" - ")[0];

        RoutePlanner.RouteResult route = store.getRoutePlanner().findShortestPath(srcId, dstId);
        int mins = route.isFound() ? route.getTotalMinutes() : 25;

        // Verify destination has capacity
        Hospital destHosp = store.getHospitalById(dstId);
        boolean isIcu = bedType.contains("ICU");
        int avail = isIcu ? destHosp.getAvailableICUBedsCount() : destHosp.getAvailableGeneralBedsCount();
        if (avail <= 0) {
            int proceed = JOptionPane.showConfirmDialog(this,
                "Warning: Destination hospital " + destHosp.getHospitalName() + " currently has 0 available " + bedType + "s.\n" +
                "Are you sure you wish to dispatch an emergency referral regardless?",
                "Bed Capacity Warning",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            if (proceed != JOptionPane.YES_OPTION) return;
        }

        store.createReferral(pId, srcId, dstId, bedType, mins);

        JOptionPane.showMessageDialog(this,
            "INTER-HOSPITAL REFERRAL CONFIRMED!\n\n" +
            "Patient ID    : " + pId + "\n" +
            "Transfer Route: " + (route.isFound() ? route.getPathString() : (srcId + " ➔ " + dstId)) + "\n" +
            "Dijkstra Time : " + mins + " minutes (Fictional route weight)\n" +
            "Required Bed  : " + bedType + "\n\n" +
            "Patient status updated to: REFERRED TO " + dstId + "\n" +
            "Transfer dispatch logged in Patient History.",
            "Referral Dispatched",
            JOptionPane.INFORMATION_MESSAGE);
    }

    public void refreshData() {
        cmbPatient.removeAllItems();
        for (Patient p : store.getPatients()) {
            cmbPatient.addItem(p.getPatientId() + " - " + p.getName() + " (" + p.getStatus() + ")");
        }

        cmbSourceHospital.removeAllItems();
        for (Hospital h : store.getHospitals()) {
            cmbSourceHospital.addItem(h.getHospitalId() + " - " + h.getHospitalName());
        }

        updateDestOptions();
        handleFindEligible(false);
        handleCalculateRoute();
    }
}
