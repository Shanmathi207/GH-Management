package hms.ui.panels;

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
 * Patient History Panel providing an audit trail of all patient clinical events,
 * admission lifecycle changes, sanitization workflows, and inter-hospital transfers.
 */
public class PatientHistoryPanel extends JPanel {

    private final DemoDataStore store;
    private final DefaultTableModel tableModel;
    private final ModernTablePanel tablePanel;

    private final JTextField txtSearchPatientId;
    private final JComboBox<String> cmbActivityTypeFilter;
    private final JLabel lblLogCount;

    public PatientHistoryPanel() {
        this.store = DemoDataStore.getInstance();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header Title
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setOpaque(false);
        JLabel lblHeading = new JLabel("CLINICAL AUDIT TRAIL & PATIENT HISTORY");
        lblHeading.setFont(UITheme.FONT_PAGE_TITLE);
        lblHeading.setForeground(UITheme.PRIMARY_NAVY);
        JLabel lblSub = new JLabel("Immutable chronological ledger of registration, consultation encounters, bed allocations, and discharges");
        lblSub.setFont(UITheme.FONT_SUBTITLE);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        header.add(lblHeading);
        header.add(lblSub);
        add(header, BorderLayout.NORTH);

        // Center Card
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        toolbar.add(UITheme.createFormLabel("Patient ID:"));
        txtSearchPatientId = UITheme.createTextField(10);
        txtSearchPatientId.addActionListener(e -> applyFilter());
        toolbar.add(txtSearchPatientId);

        toolbar.add(UITheme.createFormLabel("Activity Type:"));
        String[] types = {
            "ALL", "REGISTRATION", "CONSULTATION", "ADMISSION", 
            "DISCHARGE", "BED CLEANING", "REFERRAL", "DUTY UPDATE"
        };
        cmbActivityTypeFilter = UITheme.createComboBox(types);
        cmbActivityTypeFilter.addActionListener(e -> applyFilter());
        toolbar.add(cmbActivityTypeFilter);

        StyledButton btnFilter = new StyledButton("Filter Logs", StyledButton.Type.PRIMARY);
        btnFilter.setPreferredSize(new Dimension(105, 32));
        btnFilter.addActionListener(e -> applyFilter());
        toolbar.add(btnFilter);

        StyledButton btnReset = new StyledButton("Reset", StyledButton.Type.SECONDARY);
        btnReset.setPreferredSize(new Dimension(75, 32));
        btnReset.addActionListener(e -> {
            txtSearchPatientId.setText("");
            cmbActivityTypeFilter.setSelectedIndex(0);
            applyFilter();
        });
        toolbar.add(btnReset);

        StyledButton btnRefresh = new StyledButton("🔄 Refresh", StyledButton.Type.SECONDARY);
        btnRefresh.setPreferredSize(new Dimension(95, 32));
        btnRefresh.addActionListener(e -> applyFilter());
        toolbar.add(btnRefresh);

        card.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {
            "History ID", "Patient ID", "Activity Type",
            "Event Description", "Timestamp", "Logged By"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablePanel = new ModernTablePanel(tableModel);
        tablePanel.applyStatusRendererToColumn(2); // Activity badge
        card.add(tablePanel, BorderLayout.CENTER);

        lblLogCount = new JLabel("Audit Ledger: Loading entries...");
        lblLogCount.setFont(UITheme.FONT_SUBTITLE);
        lblLogCount.setForeground(UITheme.TEXT_MUTED);
        lblLogCount.setBorder(new EmptyBorder(6, 6, 0, 0));
        card.add(lblLogCount, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);

        store.addChangeListener(this::applyFilter);
        applyFilter();
    }

    public void applyFilter() {
        String pQuery = txtSearchPatientId.getText().trim().toLowerCase();
        String typeFilter = (String) cmbActivityTypeFilter.getSelectedItem();

        tableModel.setRowCount(0);
        List<DemoDataStore.HistoryRecord> logs = store.getActivityHistory();
        int matched = 0;

        for (DemoDataStore.HistoryRecord r : logs) {
            boolean matchP = pQuery.isEmpty() || r.getPatientId().toLowerCase().contains(pQuery);
            boolean matchType = typeFilter == null || "ALL".equalsIgnoreCase(typeFilter) || r.getActivityType().equalsIgnoreCase(typeFilter);

            if (matchP && matchType) {
                tableModel.addRow(new Object[]{
                    r.getHistoryId(),
                    r.getPatientId(),
                    r.getActivityType(),
                    r.getDescription(),
                    r.getTimestamp(),
                    r.getStaffUser()
                });
                matched++;
            }
        }

        lblLogCount.setText("Audit Ledger: " + matched + " chronological events logged (Showing most recent first)");
    }
}
