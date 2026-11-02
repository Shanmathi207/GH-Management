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

        // Bottom Action Toolbar
        actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
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
}
