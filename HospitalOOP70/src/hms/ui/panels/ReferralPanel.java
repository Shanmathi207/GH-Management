package hms.ui.panels;

import hms.Hospital;
import hms.Patient;
import hms.ReferralService;
import hms.RoutePlanner;
import hms.ui.UITheme;
import hms.ui.components.FormPanel;
import hms.ui.components.HospitalMapCanvas;
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
 * interactive Dijkstra shortest path travel calculation,
 * live visual hospital network mapping, and multi-hospital capacity comparison.
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

    // Interactive Map & Comparison components
    private final HospitalMapCanvas mapCanvas;
    private final DefaultTableModel comparisonTableModel;
    private final ModernTablePanel comparisonTablePanel;

    public ReferralPanel() {
        this.store = DemoDataStore.getInstance();

        setLayout(new BorderLayout(0, 14));
        setBackground(UITheme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header Title
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setOpaque(false);
        JLabel lblHeading = new JLabel("INTER-HOSPITAL PATIENT REFERRALS & ROUTING");
        lblHeading.setFont(UITheme.FONT_PAGE_TITLE);
        lblHeading.setForeground(UITheme.PRIMARY_NAVY);
        JLabel lblSub = new JLabel("Interactive visual hospital network map, Dijkstra's shortest path calculation, and multi-facility capacity comparison");
        lblSub.setFont(UITheme.FONT_SUBTITLE);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        header.add(lblHeading);
        header.add(lblSub);
        add(header, BorderLayout.NORTH);

        // Tabbed Container for Map View and Comparison Matrix
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(Color.WHITE);

        // ================= TAB 1: INTERACTIVE MAP & ROUTING =================
        JPanel mapTab = new JPanel(new BorderLayout(14, 0));
        mapTab.setOpaque(false);

        // Left Card: Transfer Parameters & Route Dossier
        JPanel cardForm = UITheme.createCardPanel();
        cardForm.setPreferredSize(new Dimension(440, 520));
        cardForm.setMinimumSize(new Dimension(380, 480));
        cardForm.setLayout(new BorderLayout(0, 10));

        FormPanel form = new FormPanel("Transfer Planning Parameters");

        cmbPatient = UITheme.createComboBox(new String[0]);
        form.addField("Select Patient *:", cmbPatient);

        cmbSourceHospital = UITheme.createComboBox(new String[0]);
        cmbSourceHospital.addActionListener(e -> {
            updateDestOptions();
            syncMapSelection();
            refreshComparisonMatrix();
        });
        form.addField("Source Hospital *:", cmbSourceHospital);

        String[] bedTypes = {"General Bed", "ICU Bed"};
        cmbBedType = UITheme.createComboBox(bedTypes);
        cmbBedType.addActionListener(e -> {
            handleFindEligible(false);
            refreshComparisonMatrix();
        });
        form.addField("Required Bed Type *:", cmbBedType);

        cmbDestHospital = UITheme.createComboBox(new String[0]);
        cmbDestHospital.addActionListener(e -> {
            handleCalculateRoute();
            syncMapSelection();
        });
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

        // Action Buttons
        StyledButton btnFindEligible = new StyledButton("Find Eligible", StyledButton.Type.SECONDARY);
        btnFindEligible.addActionListener(e -> handleFindEligible(true));

        StyledButton btnCalcRoute = new StyledButton("Calculate Route", StyledButton.Type.PRIMARY);
        btnCalcRoute.addActionListener(e -> handleCalculateRoute());

        StyledButton btnConfirm = new StyledButton("Confirm Referral", StyledButton.Type.SUCCESS);
        btnConfirm.addActionListener(e -> handleConfirmReferral());

        form.addActionButton(btnFindEligible);
        form.addActionButton(btnCalcRoute);
        form.addActionButton(btnConfirm);

        // Eligible candidates table snippet on left
        String[] cols = {"Hospital ID", "Hospital Name", "Compatible Beds", "Dijkstra Route Time", "Status"};
        eligibleTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablePanel = new ModernTablePanel(eligibleTableModel);
        tablePanel.setColumnWidths(85, 140, 100, 110, 85);
        tablePanel.applyStatusRendererToColumn(4);
        tablePanel.setPreferredSize(new Dimension(420, 150));

        // Sync table row click to destination dropdown
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

        JPanel leftFormContainer = new JPanel(new BorderLayout(0, 8));
        leftFormContainer.setOpaque(false);
        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.getVerticalScrollBar().setUnitIncrement(14);
        leftFormContainer.add(formScroll, BorderLayout.CENTER);
        leftFormContainer.add(tablePanel, BorderLayout.SOUTH);
        cardForm.add(leftFormContainer, BorderLayout.CENTER);

        mapTab.add(cardForm, BorderLayout.WEST);

        // Right Card: Interactive Hospital Map Canvas
        JPanel cardMap = UITheme.createCardPanel();
        cardMap.setLayout(new BorderLayout(0, 8));

        JPanel mapHeaderBar = new JPanel(new BorderLayout());
        mapHeaderBar.setOpaque(false);
        JLabel lblMapTitle = UITheme.createSectionTitle("Regional Health Facility Network Graph (Dijkstra Active Path)");
        mapHeaderBar.add(lblMapTitle, BorderLayout.WEST);

        JLabel lblMapSubtitle = new JLabel("Click any facility node to set as Destination | Right-click to set as Source");
        lblMapSubtitle.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblMapSubtitle.setForeground(UITheme.PRIMARY_BLUE);
        mapHeaderBar.add(lblMapSubtitle, BorderLayout.EAST);
        cardMap.add(mapHeaderBar, BorderLayout.NORTH);

        mapCanvas = new HospitalMapCanvas();
        mapCanvas.setOnHospitalSelected((hospId, isSource) -> {
            if (isSource) {
                for (int i = 0; i < cmbSourceHospital.getItemCount(); i++) {
                    if (cmbSourceHospital.getItemAt(i).startsWith(hospId)) {
                        cmbSourceHospital.setSelectedIndex(i);
                        break;
                    }
                }
            } else {
                for (int i = 0; i < cmbDestHospital.getItemCount(); i++) {
                    if (cmbDestHospital.getItemAt(i).startsWith(hospId)) {
                        cmbDestHospital.setSelectedIndex(i);
                        break;
                    }
                }
            }
            handleCalculateRoute();
            refreshComparisonMatrix();
        });
        cardMap.add(mapCanvas, BorderLayout.CENTER);
        mapTab.add(cardMap, BorderLayout.CENTER);

        tabbedPane.addTab("🗺️ Interactive Hospital Map & Dijkstra Routing", mapTab);

        // ================= TAB 2: HOSPITAL CAPACITY & DISTANCE COMPARISON =================
        JPanel comparisonTab = new JPanel(new BorderLayout(0, 12));
        comparisonTab.setOpaque(false);
        comparisonTab.setBorder(new EmptyBorder(8, 0, 0, 0));

        JPanel cardComp = UITheme.createCardPanel();
        cardComp.setLayout(new BorderLayout(0, 10));

        JPanel compHeader = new JPanel(new BorderLayout());
        compHeader.setOpaque(false);
        JLabel lblCompTitle = UITheme.createSectionTitle("Multi-Hospital Bed Availability & Transfer Comparison Matrix");
        JLabel lblCompSub = new JLabel("Comparative analysis of partner facilities based on Dijkstra transit minutes and real-time bed capacity");
        lblCompSub.setFont(UITheme.FONT_SUBTITLE);
        lblCompSub.setForeground(UITheme.TEXT_MUTED);
        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);
        titleBox.add(lblCompTitle);
        titleBox.add(lblCompSub);
        compHeader.add(titleBox, BorderLayout.WEST);

        StyledButton btnRefreshComp = new StyledButton("Refresh Comparison", StyledButton.Type.SECONDARY);
        btnRefreshComp.addActionListener(e -> refreshComparisonMatrix());
        compHeader.add(btnRefreshComp, BorderLayout.EAST);
        cardComp.add(compHeader, BorderLayout.NORTH);

        String[] compCols = {
            "Hospital ID", "Hospital Name", "Location / Zone", "Dijkstra Transit Time",
            "Avail General Beds", "Avail ICU Beds", "Doctors On Duty", "Transfer Suitability"
        };
        comparisonTableModel = new DefaultTableModel(compCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        comparisonTablePanel = new ModernTablePanel(comparisonTableModel);
        comparisonTablePanel.setColumnWidths(90, 220, 130, 140, 130, 110, 110, 180);
        comparisonTablePanel.applyStatusRendererToColumn(7);

        // Click row in comparison table selects that hospital as destination
        comparisonTablePanel.getTable().getSelectionModel().addListSelectionListener(e -> {
            int row = comparisonTablePanel.getTable().getSelectedRow();
            if (row >= 0) {
                String hId = (String) comparisonTableModel.getValueAt(row, 0);
                for (int i = 0; i < cmbDestHospital.getItemCount(); i++) {
                    if (cmbDestHospital.getItemAt(i).startsWith(hId)) {
                        cmbDestHospital.setSelectedIndex(i);
                        break;
                    }
                }
                handleCalculateRoute();
            }
        });

        cardComp.add(comparisonTablePanel, BorderLayout.CENTER);
        comparisonTab.add(cardComp, BorderLayout.CENTER);

        tabbedPane.addTab("📊 Multi-Hospital Capacity & Route Comparison Matrix", comparisonTab);

        add(tabbedPane, BorderLayout.CENTER);

        store.addChangeListener(this::refreshData);
        refreshData();
    }

    private void syncMapSelection() {
        String src = (String) cmbSourceHospital.getSelectedItem();
        String dst = (String) cmbDestHospital.getSelectedItem();
        if (src != null && src.contains(" - ")) {
            mapCanvas.setSourceHospital(src.split(" - ")[0]);
        }
        if (dst != null && dst.contains(" - ")) {
            mapCanvas.setDestinationHospital(dst.split(" - ")[0]);
        }
    }

    private void updateDestOptions() {
        String src = (String) cmbSourceHospital.getSelectedItem();
        String srcId = (src != null && src.contains(" - ")) ? src.split(" - ")[0] : null;

        String prevSelected = (String) cmbDestHospital.getSelectedItem();
        cmbDestHospital.removeAllItems();
        for (Hospital h : store.getHospitals()) {
            if (srcId == null || !h.getHospitalId().equalsIgnoreCase(srcId)) {
                cmbDestHospital.addItem(h.getHospitalId() + " - " + h.getHospitalName());
            }
        }
        if (prevSelected != null) {
            for (int i = 0; i < cmbDestHospital.getItemCount(); i++) {
                if (cmbDestHospital.getItemAt(i).equalsIgnoreCase(prevSelected)) {
                    cmbDestHospital.setSelectedIndex(i);
                    break;
                }
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

        mapCanvas.setSourceHospital(srcId);
        mapCanvas.setDestinationHospital(dstId);
        mapCanvas.setActiveRoute(res);

        return res;
    }

    private void refreshComparisonMatrix() {
        String src = (String) cmbSourceHospital.getSelectedItem();
        String srcId = (src != null && src.contains(" - ")) ? src.split(" - ")[0] : "H001";
        String bedReq = (String) cmbBedType.getSelectedItem();
        boolean isIcu = bedReq != null && bedReq.contains("ICU");

        comparisonTableModel.setRowCount(0);
        for (Hospital h : store.getHospitals()) {
            if (h.getHospitalId().equalsIgnoreCase(srcId)) continue;

            RoutePlanner.RouteResult route = store.getRoutePlanner().findShortestPath(srcId, h.getHospitalId());
            int mins = route.isFound() ? route.getTotalMinutes() : 999;
            int genAvail = h.getAvailableGeneralBedsCount();
            int icuAvail = h.getAvailableICUBedsCount();
            int reqAvail = isIcu ? icuAvail : genAvail;

            long dutyDocs = h.getDoctors().stream().filter(d -> d.isOnDuty()).count();

            String suitability;
            if (reqAvail > 0 && mins <= 20) {
                suitability = "RECOMMENDED (" + mins + "m, " + reqAvail + " beds)";
            } else if (reqAvail > 0) {
                suitability = "ELIGIBLE (" + reqAvail + " beds)";
            } else {
                suitability = "FULL CAPACITY (0 " + (isIcu ? "ICU" : "Gen") + ")";
            }

            comparisonTableModel.addRow(new Object[]{
                h.getHospitalId(),
                h.getHospitalName(),
                h.getLocation(),
                route.isFound() ? (mins + " mins (" + route.getPathString() + ")") : "No route",
                genAvail + " / " + h.getGeneralBedCapacity(),
                icuAvail + " / " + h.getIcuBedCapacity(),
                dutyDocs + " on duty",
                suitability
            });
        }
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

        mapCanvas.repaint();
        refreshComparisonMatrix();
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

        String prevSrc = (String) cmbSourceHospital.getSelectedItem();
        cmbSourceHospital.removeAllItems();
        for (Hospital h : store.getHospitals()) {
            cmbSourceHospital.addItem(h.getHospitalId() + " - " + h.getHospitalName());
        }
        if (prevSrc != null) {
            for (int i = 0; i < cmbSourceHospital.getItemCount(); i++) {
                if (cmbSourceHospital.getItemAt(i).equals(prevSrc)) {
                    cmbSourceHospital.setSelectedIndex(i);
                    break;
                }
            }
        }

        updateDestOptions();
        handleFindEligible(false);
        handleCalculateRoute();
        refreshComparisonMatrix();
        mapCanvas.repaint();
    }
}
