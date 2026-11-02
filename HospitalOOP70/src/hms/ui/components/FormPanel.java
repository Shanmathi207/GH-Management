package hms.ui.components;

import hms.ui.UITheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Reusable container for aligned form layouts, input controls, and action button toolbars.
 */
public class FormPanel extends JPanel {

    private final JPanel fieldsGrid;
    private final JPanel actionPanel;
    private final GridBagConstraints gbc;
    private int currentRow = 0;

    public FormPanel(String sectionTitle) {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        // Header Title
        if (sectionTitle != null && !sectionTitle.isEmpty()) {
            JLabel lblTitle = UITheme.createSectionTitle(sectionTitle);
            lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
            add(lblTitle, BorderLayout.NORTH);
        }

        // Center Form Grid
        fieldsGrid = new JPanel(new GridBagLayout());
        fieldsGrid.setOpaque(false);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        add(fieldsGrid, BorderLayout.CENTER);

        // Bottom Action Toolbar with WrapLayout to prevent clipping on resizing
        actionPanel = new JPanel(new WrapLayout(FlowLayout.RIGHT, 10, 6));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(12, 0, 0, 0));
        add(actionPanel, BorderLayout.SOUTH);
    }

    public void addField(String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = currentRow;
        gbc.weightx = 0.25;
        JLabel lbl = UITheme.createFormLabel(labelText);
        fieldsGrid.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.gridy = currentRow;
        gbc.weightx = 0.75;
        fieldsGrid.add(field, gbc);

        currentRow++;
    }

    public void addFullWidthComponent(JComponent component) {
        gbc.gridx = 0;
        gbc.gridy = currentRow;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        fieldsGrid.add(component, gbc);
        gbc.gridwidth = 1;
        currentRow++;
    }

    public void addActionButton(JButton button) {
        actionPanel.add(button);
    }

    public JPanel getActionPanel() {
        return actionPanel;
    }

    /**
     * FlowLayout subclass that recalculates preferred height when child components wrap,
     * ensuring action buttons are never pushed off-screen or clipped.
     */
    private static class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            Dimension min = layoutSize(target, false);
            min.width -= (getHgap() + 1);
            return min;
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getWidth();
                if (targetWidth == 0) {
                    Container p = target.getParent();
                    while (p != null && targetWidth == 0) {
                        targetWidth = p.getWidth();
                        p = p.getParent();
                    }
                }
                if (targetWidth == 0) {
                    targetWidth = Integer.MAX_VALUE;
                }

                int hgap = getHgap();
                int vgap = getVgap();
                Insets insets = target.getInsets();
                int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
                int maxWidth = targetWidth - horizontalInsetsAndGap;

                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0;
                int rowHeight = 0;

                int nmembers = target.getComponentCount();
                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if ((rowWidth + d.width) > maxWidth && rowWidth > 0) {
                            dim.width = Math.max(dim.width, rowWidth);
                            dim.height += rowHeight + vgap;
                            rowWidth = 0;
                            rowHeight = 0;
                        }
                        rowWidth += d.width + hgap;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }
                dim.width = Math.max(dim.width, rowWidth);
                dim.height += rowHeight;

                dim.width += insets.left + insets.right + (hgap * 2);
                dim.height += insets.top + insets.bottom + (vgap * 2);

                return dim;
            }
        }
    }
}
