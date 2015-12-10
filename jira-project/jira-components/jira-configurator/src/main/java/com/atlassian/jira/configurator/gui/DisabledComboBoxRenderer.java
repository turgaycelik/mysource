package com.atlassian.jira.configurator.gui;

import com.atlassian.jira.configurator.config.WebServerProfile;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.util.List;

public class DisabledComboBoxRenderer extends BasicComboBoxRenderer
{
    private final List<WebServerProfile> usableProfiles;

    public DisabledComboBoxRenderer(@Nonnull final List<WebServerProfile> usableProfiles)
    {
        this.usableProfiles = usableProfiles;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        final WebServerProfile profile = (WebServerProfile) value;
        final boolean unsupportedProfile = !usableProfiles.contains(profile);
        if (unsupportedProfile) {
            setBackground(list.getBackground());
            setForeground(UIManager.getColor("Label.disabledForeground"));
        }
        setFont(list.getFont());
        setText(profile.getLabel());
        return this;
    }
}
