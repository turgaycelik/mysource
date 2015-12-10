package com.atlassian.jira.configurator.gui;

import com.atlassian.jira.configurator.config.Validator;

import javax.swing.*;
import java.awt.*;

public class LabelledComponent extends JPanel
{
    private final JLabel label;
    private final JComponent component;

    public LabelledComponent(final String labelText, final JComponent component)
    {
        setLayout(new BorderLayout(4, 0));
        label = new JLabel(labelText);
        this.component = component;
        add(label, BorderLayout.CENTER);
        add(component, BorderLayout.EAST);
    }

    public String label()
    {
        return label.getText();
    }

    public boolean isUsingDefault()
    {
        return false;
    }

    public int getPreferredLabelWidth()
    {
        return label.getPreferredSize().width;
    }

    public void setLabelWidth(final int width)
    {
        label.setPreferredSize(new Dimension(width, label.getPreferredSize().height));
    }

    public void setValue(Object newValue)
    {
        if (component instanceof JTextField)
        {
            ((JTextField)component).setText((newValue != null) ? newValue.toString() : "");
            return;
        }
        if (component instanceof JCheckBox)
        {
            ((JCheckBox)component).setSelected((newValue instanceof Boolean) ? ((Boolean)newValue) : false);
            return;
        }
        throw new IllegalStateException("Unexpected component class: " + component.getClass().getName());
    }

    public String getValue()
    {
        if (isUsingDefault())
        {
            return null;
        }
        if (component instanceof JTextField)
        {
            return ((JTextField)component).getText();
        }
        if (component instanceof JCheckBox)
        {
            return ((JCheckBox)component).isSelected() ? "true" : "false";
        }
        throw new IllegalStateException("Unexpected component class: " + component.getClass().getName());
    }
}
