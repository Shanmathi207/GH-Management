package hms.ui.panels;

import hms.Bed;
import hms.GeneralBed;
import hms.ICUBed;
import hms.Patient;
import hms.ui.UITheme;
import hms.ui.components.StyledButton;
import hms.ui.components.SummaryCard;
import hms.ui.demo.DemoDataStore;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Executive Clinical & Resource Reports Panel featuring custom painted Swing charts,
 * bed utilization indicators, and triage distribution metrics.
 */
public class ReportsPanel extends JPanel {

    private final DemoDataStore store;

    private final SummaryCard cardAdmissionRate;
    private final SummaryCard cardDischargeRate;
    private final SummaryCard cardBedTurnaround;

    private final JProgressBar progGenUtilization;
    private final JLabel lblGenPercent;
    private final JProgressBar progIcuUtilization;
    private final JLabel lblIcuPercent;

    private final CustomBarChartPanel visitTypeChart;
    private final CustomBarChartPanel severityChart;

    public ReportsPanel() {
        this.store = DemoDataStore.getInstance();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header Title
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleGrp = new JPanel(new GridLayout(2, 1, 0, 2));
        titleGrp.setOpaque(false);
        JLabel lblHeading = new JLabel("EXECUTIVE CLINICAL & RESOURCE REPORTS");
        lblHeading.setFont(UITheme.FONT_PAGE_TITLE);
        lblHeading.setForeground(UITheme.PRIMARY_NAVY);
        JLabel lblSub = new JLabel("Aggregate utilization metrics, triage volume distribution, and capacity analysis");
        lblSub.setFont(UITheme.FONT_SUBTITLE);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        titleGrp.add(lblHeading);
        titleGrp.add(lblSub);

        StyledButton btnRefresh = new StyledButton("🔄 Refresh Reports", StyledButton.Type.PRIMARY);
        btnRefresh.setPreferredSize(new Dimension(160, 36));
        btnRefresh.addActionListener(e -> refreshReports());

        header.add(titleGrp, BorderLayout.WEST);
        header.add(btnRefresh, BorderLayout.EAST);

        // Center Scrollable View
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        contentPanel.add(header);
        contentPanel.add(Box.createVerticalStrut(14));

        // Prototype Banner
        JPanel banner = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        banner.setBackground(new Color(0xFE, 0xF3, 0xC7));
        banner.setBorder(BorderFactory.createLineBorder(new Color(0xF5, 0x9E, 0x0B), 1, true));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel lblBanner = new JLabel("ℹ️ Notice: Real-time clinical reports and capacity indicators calculated dynamically from MySQL records and active triage queues.");
        lblBanner.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblBanner.setForeground(new Color(0x92, 0x40, 0x0E));
        banner.add(lblBanner);
        contentPanel.add(banner);
        contentPanel.add(Box.createVerticalStrut(16));

        // Metric Cards Row
        JPanel metricsRow = new JPanel(new GridLayout(1, 3, 14, 0));
        metricsRow.setOpaque(false);
        metricsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        cardAdmissionRate = new SummaryCard("Total Admitted Census", "0", "Current inpatient occupancy", UITheme.PRIMARY_BLUE);
        cardDischargeRate = new SummaryCard("Cumulative Discharges", "0", "Patients successfully treated", UITheme.SUCCESS_GREEN);
        cardBedTurnaround = new SummaryCard("Active Beds in Cleaning", "0", "Terminal sanitization turnaround", UITheme.WARNING_AMBER);

        metricsRow.add(cardAdmissionRate);
        metricsRow.add(cardDischargeRate);
        metricsRow.add(cardBedTurnaround);

        contentPanel.add(metricsRow);
        contentPanel.add(Box.createVerticalStrut(16));

        // Middle Row: Two Custom Charts
        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsRow.setOpaque(false);
        chartsRow.setPreferredSize(new Dimension(1000, 260));

        // Chart 1: Visit Type Breakdown
        JPanel cardChart1 = UITheme.createCardPanel();
        cardChart1.setLayout(new BorderLayout(0, 10));
        cardChart1.add(UITheme.createSectionTitle("Patient Volume by Visit Category"), BorderLayout.NORTH);
        visitTypeChart = new CustomBarChartPanel("Patients");
        cardChart1.add(visitTypeChart, BorderLayout.CENTER);
        chartsRow.add(cardChart1);

        // Chart 2: Triage Severity Breakdown
        JPanel cardChart2 = UITheme.createCardPanel();
        cardChart2.setLayout(new BorderLayout(0, 10));
        cardChart2.add(UITheme.createSectionTitle("Waiting Queue Patients by Triage Severity"), BorderLayout.NORTH);
        severityChart = new CustomBarChartPanel("In Queue");
        cardChart2.add(severityChart, BorderLayout.CENTER);
        chartsRow.add(cardChart2);

        contentPanel.add(chartsRow);
        contentPanel.add(Box.createVerticalStrut(16));

        // Bottom Row: Bed Utilization Progress Bars Card
        JPanel cardUtilization = UITheme.createCardPanel();
        cardUtilization.setLayout(new GridLayout(2, 1, 0, 12));
        cardUtilization.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        // General Bed Bar
        JPanel genPanel = new JPanel(new BorderLayout(12, 4));
        genPanel.setOpaque(false);
        JPanel genLabels = new JPanel(new BorderLayout());
        genLabels.setOpaque(false);
        JLabel lblGenTitle = new JLabel("General Ward Bed Utilization (Occupied / Quota)");
        lblGenTitle.setFont(UITheme.FONT_LABEL);
        lblGenTitle.setForeground(UITheme.PRIMARY_NAVY);
        lblGenPercent = new JLabel("0%");
        lblGenPercent.setFont(UITheme.FONT_LABEL);
        lblGenPercent.setForeground(UITheme.PRIMARY_BLUE);
        genLabels.add(lblGenTitle, BorderLayout.WEST);
        genLabels.add(lblGenPercent, BorderLayout.EAST);
        genPanel.add(genLabels, BorderLayout.NORTH);

        progGenUtilization = new JProgressBar(0, 100);
        progGenUtilization.setPreferredSize(new Dimension(400, 18));
        progGenUtilization.setForeground(UITheme.PRIMARY_BLUE);
        progGenUtilization.setBackground(new Color(0xE2, 0xE8, 0xF0));
        progGenUtilization.setBorderPainted(false);
        genPanel.add(progGenUtilization, BorderLayout.CENTER);

        // ICU Bed Bar
        JPanel icuPanel = new JPanel(new BorderLayout(12, 4));
        icuPanel.setOpaque(false);
        JPanel icuLabels = new JPanel(new BorderLayout());
        icuLabels.setOpaque(false);
        JLabel lblIcuTitle = new JLabel("ICU Critical Care Utilization (Occupied / Quota)");
        lblIcuTitle.setFont(UITheme.FONT_LABEL);
        lblIcuTitle.setForeground(UITheme.PRIMARY_NAVY);
        lblIcuPercent = new JLabel("0%");
        lblIcuPercent.setFont(UITheme.FONT_LABEL);
        lblIcuPercent.setForeground(UITheme.ERROR_RED);
        icuLabels.add(lblIcuTitle, BorderLayout.WEST);
        icuLabels.add(lblIcuPercent, BorderLayout.EAST);
        icuPanel.add(icuLabels, BorderLayout.NORTH);

        progIcuUtilization = new JProgressBar(0, 100);
        progIcuUtilization.setPreferredSize(new Dimension(400, 18));
        progIcuUtilization.setForeground(UITheme.ERROR_RED);
        progIcuUtilization.setBackground(new Color(0xE2, 0xE8, 0xF0));
        progIcuUtilization.setBorderPainted(false);
        icuPanel.add(progIcuUtilization, BorderLayout.CENTER);

        cardUtilization.add(genPanel);
        cardUtilization.add(icuPanel);

        contentPanel.add(cardUtilization);

        JScrollPane mainScroll = new JScrollPane(contentPanel);
        mainScroll.setBorder(null);
        mainScroll.getViewport().setBackground(UITheme.LIGHT_BG);
        mainScroll.getVerticalScrollBar().setUnitIncrement(14);
        add(mainScroll, BorderLayout.CENTER);

        store.addChangeListener(this::refreshReports);
        refreshReports();
    }

    public void refreshReports() {
        int admitted = store.getAdmittedPatientsCount();
        int discharges = store.getTotalDischargesCount();
        int cleaning = 0;
        int totalGen = 0, occGen = 0;
        int totalIcu = 0, occIcu = 0;

        for (Bed b : store.getBeds()) {
            if (b.isCleaning()) cleaning++;
            if (b instanceof GeneralBed) {
                totalGen++;
                if (b.isOccupied()) occGen++;
            } else if (b instanceof ICUBed) {
                totalIcu++;
                if (b.isOccupied()) occIcu++;
            }
        }

        cardAdmissionRate.setValue(String.valueOf(admitted));
        cardDischargeRate.setValue(String.valueOf(discharges));
        cardBedTurnaround.setValue(String.valueOf(cleaning));

        // Calculate Percentages
        int genPct = totalGen > 0 ? (int) Math.round((occGen * 100.0) / totalGen) : 0;
        progGenUtilization.setValue(genPct);
        lblGenPercent.setText(genPct + "% (" + occGen + " / " + totalGen + " Beds Occupied)");

        int icuPct = totalIcu > 0 ? (int) Math.round((occIcu * 100.0) / totalIcu) : 0;
        progIcuUtilization.setValue(icuPct);
        lblIcuPercent.setText(icuPct + "% (" + occIcu + " / " + totalIcu + " Beds Occupied)");

        // Visit Types Breakdown
        int opd = 0, ipd = 0, emg = 0;
        for (Patient p : store.getPatients()) {
            String vt = p.getVisitType() != null ? p.getVisitType().toUpperCase() : "OPD";
            if (vt.contains("OPD")) opd++;
            else if (vt.contains("IPD")) ipd++;
            else if (vt.contains("EMERGENCY")) emg++;
            else opd++;
        }
        Map<String, Integer> visitData = new HashMap<>();
        visitData.put("OPD", opd);
        visitData.put("IPD", ipd);
        visitData.put("EMERGENCY", emg);
        visitTypeChart.setData(visitData, new Color[]{UITheme.PRIMARY_BLUE, new Color(0x8B, 0x5C, 0xF6), UITheme.ERROR_RED});

        // Severity Breakdown for waiting patients
        int s1 = 0, s2 = 0, s3 = 0, s4 = 0, s5 = 0;
        for (Patient p : store.getSortedQueueList()) {
            switch (p.getPriority()) {
                case 1: s1++; break;
                case 2: s2++; break;
                case 3: s3++; break;
                case 4: s4++; break;
                default: s5++; break;
            }
        }
        Map<String, Integer> sevData = new HashMap<>();
        sevData.put("P1: Critical", s1);
        sevData.put("P2: High", s2);
        sevData.put("P3: Medium", s3);
        sevData.put("P4: Low", s4);
        sevData.put("P5: Routine", s5);
        severityChart.setData(sevData, new Color[]{
            UITheme.ERROR_RED, UITheme.WARNING_AMBER, new Color(0x3B, 0x82, 0xF6), UITheme.SUCCESS_GREEN, Color.GRAY
        });
    }

    /**
     * Custom lightweight bar chart rendered via Java Swing Graphics2D.
     */
    private static class CustomBarChartPanel extends JPanel {
        private Map<String, Integer> data = new HashMap<>();
        private Color[] palette = new Color[]{UITheme.PRIMARY_BLUE};
        private final String unitLabel;

        public CustomBarChartPanel(String unitLabel) {
            this.unitLabel = unitLabel;
            setOpaque(false);
        }

        public void setData(Map<String, Integer> data, Color[] palette) {
            this.data = data != null ? data : new HashMap<>();
            this.palette = palette != null ? palette : new Color[]{UITheme.PRIMARY_BLUE};
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int padLeft = 40;
            int padBottom = 35;
            int padTop = 20;
            int padRight = 20;

            int chartW = w - padLeft - padRight;
            int chartH = h - padTop - padBottom;

            int maxVal = 1;
            for (int val : data.values()) {
                if (val > maxVal) maxVal = val;
            }

            // Draw axis line
            g2.setColor(UITheme.BORDER_COLOR);
            g2.drawLine(padLeft, padTop + chartH, padLeft + chartW, padTop + chartH);

            int count = data.size();
            int barWidth = Math.max(16, (chartW / count) - 24);
            int step = chartW / count;

            int i = 0;
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                String label = entry.getKey();
                int val = entry.getValue();

                int barH = (int) Math.round((val * 1.0 / maxVal) * (chartH - 20));
                int x = padLeft + (i * step) + (step - barWidth) / 2;
                int y = padTop + chartH - barH;

                Color c = palette[i % palette.length];
                g2.setColor(c);
                g2.fillRoundRect(x, y, barWidth, barH, 6, 6);

                // Draw value on top of bar
                g2.setColor(UITheme.TEXT_MAIN);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String valStr = val + " " + unitLabel;
                FontMetrics fm = g2.getFontMetrics();
                int valX = x + (barWidth - fm.stringWidth(valStr)) / 2;
                g2.drawString(valStr, valX, y - 5);

                // Draw category label below axis
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.setColor(UITheme.TEXT_MUTED);
                int lblX = x + (barWidth - fm.stringWidth(label)) / 2;
                g2.drawString(label, lblX, padTop + chartH + 18);

                i++;
            }

            g2.dispose();
        }
    }
}
