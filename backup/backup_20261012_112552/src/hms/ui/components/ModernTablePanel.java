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
 * with padding, custom header rendering, alternating rows, and smooth selection.
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
                    c.setBackground(new Color(0xDB, 0xEA, 0xFE));
                }
                return c;
            }
        };

        table.setRowHeight(36);
        table.setFont(UITheme.FONT_REGULAR);
        table.setForeground(UITheme.TEXT_MAIN);
        table.setGridColor(UITheme.BORDER_COLOR);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setSelectionBackground(new Color(0xDB, 0xEA, 0xFE));
        table.setSelectionForeground(UITheme.PRIMARY_NAVY);
        table.setFillsViewportHeight(true);

        // Header Styling
        JTableHeader header = table.getTableHeader();
        header.setFont(UITheme.FONT_LABEL);
        header.setForeground(UITheme.PRIMARY_NAVY);
        header.setBackground(new Color(0xF1, 0xF5, 0xF9));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR));

        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(scrollPane, BorderLayout.CENTER);
    }

    public JTable getTable() {
        return table;
    }

    public JPanel getTopToolbar() {
        return topToolbar;
    }

    public void applyStatusRendererToColumn(int columnIndex) {
        if (columnIndex >= 0 && columnIndex < table.getColumnCount()) {
            table.getColumnModel().getColumn(columnIndex).setCellRenderer(new StatusCellRenderer());
        }
    }
}
