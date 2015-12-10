package com.atlassian.jira.configurator.gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.util.ArrayList;
import java.util.List;

public class ConfigPanelBuilder
{
    private final JPanel panel;
    private List<LabelledComponent> labelledComponents = new ArrayList<LabelledComponent>();

    public ConfigPanelBuilder()
    {
        panel = new JPanel(new VerticalFlowLayout());
    }

    public LabelledComponent add(final String label, final JComponent component)
    {
        final LabelledComponent labelledComponent = new LabelledComponent(label, component);
        panel.add(labelledComponent);
        labelledComponents.add(labelledComponent);
        return labelledComponent;
    }

    public LabelledComponent add(final String label, final JComponent component, final String defaultValue)
    {
        final LabelledComponent labelledComponent = new OptionalLabelledComponent(label, component, defaultValue);
        panel.add(labelledComponent);
        labelledComponents.add(labelledComponent);
        return labelledComponent;
    }

    public JPanel getPanel()
    {
        setLabelWidths();
        return panel;
    }

    private void setLabelWidths()
    {
        // Find the max width of all labels
        int maxWidth = 0;
        for (LabelledComponent labelledComponent : labelledComponents)
        {
            int width = labelledComponent.getPreferredLabelWidth();
            if (width > maxWidth)
            {
                maxWidth = width;
            }
        }

        // now set all labels to this width
        for (LabelledComponent labelledComponent : labelledComponents)
        {
            labelledComponent.setLabelWidth(maxWidth);
        }        
    }

    /**
     * Gives the panel a titled border with the given title.
     * @param title The title.
     */
    public void setTitle(final String title)
    {
        panel.setBorder(new TitledBorder(title));        
    }
}
