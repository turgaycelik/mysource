package com.atlassian.jira.configurator.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Marks a component with a checkbox.  The internal component is disabled
 * when the checkbox is unchecked and reappears when the checkbox is checked.
 *
 * @since v5.1
 */
public class OptionalLabelledComponent extends LabelledComponent implements ItemListener
{
    private final JComponent component;
    private final String defaultValue;
    private final JCheckBox checkbox;

    public OptionalLabelledComponent(final String label, final JComponent component, final String defaultValue)
    {
        super(label, component);
        this.component = component;
        this.defaultValue = defaultValue;

        checkbox = new JCheckBox();
        checkbox.addItemListener(this);
        add(checkbox, BorderLayout.WEST);
        onDisabled();
    }

    private void onEnabled()
    {
        component.setEnabled(true);
        checkbox.setToolTipText("Uncheck to use the default setting of " + defaultValue);
    }

    private void onDisabled()
    {
        component.setEnabled(false);
        checkbox.setToolTipText("Check to override the default setting of " + defaultValue);
    }

    @Override
    public boolean isUsingDefault()
    {
        return !component.isEnabled();
    }

    @Override
    public void setValue(Object newValue)
    {
        super.setValue(newValue);
        if (newValue != null)
        {
            checkbox.setSelected(true);
            onEnabled();
        }
        else
        {
            checkbox.setSelected(false);
            onDisabled();
        }
    }


    @Override
    public void itemStateChanged(ItemEvent e)
    {
        // PARANOID - if it wasn't our checkbox, I don't know why we got this
        if (e.getItemSelectable() != checkbox)
        {
            return;
        }
        switch (e.getStateChange())
        {
            case ItemEvent.SELECTED:
                onEnabled();;
                break;
            case ItemEvent.DESELECTED:
                onDisabled();
                break;
        }
    }
}
