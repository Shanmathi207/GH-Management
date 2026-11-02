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
 * PriorityQueuePanel coordinates triage priority ordering using Java's PriorityQueue.
 * Orders patients deterministically by:
 * 1. Severity ascending (1 is highest priority: Critical)
 * 2. Arrival time ascending
 * 3. Sequence number ascending
 */
public class PriorityQueuePanel extends JPanel {

    private final DemoDataStore store;
    private final DefaultTableModel tableModel;
    private final ModernTablePanel tablePanel;
    private final JLabel lblQueueInfo;

    public PriorityQueuePanel() {
        this.store = DemoDataStore.getInstance();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header Title
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setOpaque(false);
        JLabel lblHeading = new JLabel("ACUTE CLINICAL PRIORITY QUEUE");
        lblHeading.setFont(UITheme.FONT_PAGE_TITLE);
        lblHeading.setForeground(UITheme.PRIMARY_NAVY);
        JLabel lblSub = new JLabel("Deterministic multi-factor triage priority queue (Severity ➔ Arrival Timestamp ➔ Sequence ID)");
        lblSub.setFont(UITheme.FONT_SUBTITLE);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        header.add(lblHeading);
        header.add(lblSub);
        add(header, BorderLayout.NORTH);

        // Center Card
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 14));

        // Toolbar with Call Next Patient & Refresh
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        lblQueueInfo = new JLabel("Active Queue Count: 0 patients waiting");
        lblQueueInfo.setFont(UITheme.FONT_SECTION);
        lblQueueInfo.setForeground(UITheme.PRIMARY_NAVY);
        toolbar.add(lblQueueInfo, BorderLayout.WEST);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);

        StyledButton btnRefresh = new StyledButton("🔄 Refresh Queue", StyledButton.Type.SECONDARY);
        btnRefresh.setPreferredSize(new Dimension(135, 34));
        btnRefresh.addActionListener(e -> refreshQueue());

        StyledButton btnCallNext = new StyledButton("⚡ Call Next Patient", StyledButton.Type.SUCCESS);
        btnCallNext.setPreferredSize(new Dimension(160, 34));
        btnCallNext.addActionListener(e -> handleCallNextPatient());

        btnBar.add(btnRefresh);
        btnBar.add(btnCallNext);
        toolbar.add(btnBar, BorderLayout.EAST);

        card.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {
            "Queue Position", "Patient ID", "Patient Name", "Department",
            "Triage Severity", "Arrival Timestamp", "Sequence No", "Status"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tablePanel = new ModernTablePanel(tableModel);
        tablePanel.applyStatusRendererToColumn(4); // Severity pill
        tablePanel.applyStatusRendererToColumn(7); // Status pill
        card.add(tablePanel, BorderLayout.CENTER);

        // Informational Note
        JLabel lblFoot = new JLabel("Priority Order Logic: Level 1 (Critical) immediately precedes Level 2 (High), followed by arrival time tie-breaking.");
        lblFoot.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblFoot.setForeground(UITheme.TEXT_MUTED);
        card.add(lblFoot, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);

        store.addChangeListener(this::refreshQueue);
        refreshQueue();
    }

    private void handleCallNextPatient() {
        if (store.getPriorityQueue().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Priority Queue is empty.\nNo patients are currently waiting to be called.",
                "Queue Empty",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        Patient called = store.callNextPatient();
        if (called != null) {
            JOptionPane.showMessageDialog(this,
                "PATIENT CALLED FROM QUEUE:\n\n" +
                "Patient ID       : " + called.getPatientId() + "\n" +
                "Name             : " + called.getName() + "\n" +
                "Age / Gender     : " + called.getAge() + " / " + called.getGender() + "\n" +
                "Department       : " + called.getDepartment() + "\n" +
                "Severity Level   : " + called.getPriority() + " (" + called.getEmergencyLevel() + ")\n" +
                "Arrival Time     : " + called.getRegistrationDateTime() + "\n\n" +
                "Status has been updated to: CALLED - CONSULTING\n" +
                "Proceed to OPD Consultation or Emergency Admission.",
                "Next Patient Dispatched",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void refreshQueue() {
        tableModel.setRowCount(0);
        List<Patient> list = store.getSortedQueueList();

        lblQueueInfo.setText("Active Queue Count: " + list.size() + " patients waiting");

        int pos = 1;
        for (Patient p : list) {
            tableModel.addRow(new Object[]{
                "#" + pos++,
                p.getPatientId(),
                p.getName(),
                p.getDepartment(),
                p.getPriority() + " (" + p.getEmergencyLevel() + ")",
                p.getRegistrationDateTime(),
                p.getArrivalSequence(),
                p.getStatus()
            });
        }
    }
}
