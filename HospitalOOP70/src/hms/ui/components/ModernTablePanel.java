package hms.ui.components;

import hms.ui.UITheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

/**
 * ModernTablePanel provides a beautifully styled JTable wrapper
 * with responsive column sizing, alternating row colors, smooth selection,
 * and clean healthcare typography.
 */
public class ModernTablePanel extends JPanel {

    private final JTable table;
    private final JScrollPane scrollPane;
    private final JPanel topToolbar;

    public ModernTablePanel(TableModel model) {
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);

        topToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topToolbar.setOpaque(false);
        add(topToolbar, BorderLayout.NORTH);

        table = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
                } else {
                    c.setBackground(new Color(0xD8, 0xE9, 0xFF));
                }
                return c;
            }
        };

        table.setRowHeight(34);
        table.setFont(UITheme.FONT_REGULAR);
        table.setForeground(UITheme.TEXT_MAIN);
        table.setGridColor(UITheme.BORDER_COLOR);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setSelectionBackground(new Color(0xD8, 0xE9, 0xFF));
        table.setSelectionForeground(UITheme.TEXT_MAIN);
        table.setFillsViewportHeight(true);

        // Header Styling
        JTableHeader header = table.getTableHeader();
        header.setFont(UITheme.FONT_LABEL);
        header.setForeground(UITheme.TEXT_MAIN);
        header.setBackground(new Color(0xED, 0xF4, 0xFC));
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR));

        // Responsive auto-resize for tables with <= 6 columns, horizontal scroll for dense tables
        if (model.getColumnCount() <= 6) {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        } else {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for (int i = 0; i < model.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setPreferredWidth(130);
                table.getColumnModel().getColumn(i).setMinWidth(75);
            }
        }

        scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(14);

        add(scrollPane, BorderLayout.CENTER);
    }

    public JTable getTable() {
        return table;
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public JPanel getTopToolbar() {
        return topToolbar;
    }

    public void setAutoResizeMode(int mode) {
        table.setAutoResizeMode(mode);
    }

    public void setColumnWidths(int... widths) {
        // If 6 or fewer columns, fit within container width without horizontal scroll
        if (widths.length <= 6) {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        } else {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        }
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(Math.min(widths[i], 65));
        }
    }

    public void applyStatusRendererToColumn(int columnIndex) {
        if (columnIndex >= 0 && columnIndex < table.getColumnCount()) {
            table.getColumnModel().getColumn(columnIndex).setCellRenderer(new StatusCellRenderer());
        }
    }
}
