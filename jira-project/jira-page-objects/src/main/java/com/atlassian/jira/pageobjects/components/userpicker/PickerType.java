package com.atlassian.jira.pageobjects.components.userpicker;

/**
 * Encapsulates identifiers specific to given picker type.
 *
 * @since 5.0
 */
public enum PickerType
{

    USER_PICKER("UserPicker"),
    GROUP_PICKER("GroupPicker"),
    ICON_PICKER("IconPicker"),
    ISSUE_PICKER("IssueSelectorPopup");
    // TODO other?


    private final String windowName;

    PickerType(String windowName)
    {
        this.windowName = windowName;
    }

    public String windowName()
    {
        return windowName;
    }
}
