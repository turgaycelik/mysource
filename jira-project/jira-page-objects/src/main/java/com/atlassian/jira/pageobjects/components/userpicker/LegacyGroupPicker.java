package com.atlassian.jira.pageobjects.components.userpicker;

import com.atlassian.pageobjects.elements.PageElement;

import javax.annotation.Nullable;

/**
 * Legacy group picker.
 *
 * @since v5.2
 * @since v5.2
 */
public class LegacyGroupPicker extends LegacyTriggerPicker
{


    public LegacyGroupPicker(@Nullable PageElement form, String pickerId)
    {
        super(form, pickerId);
    }

    public LegacyGroupPicker(String pickerId)
    {
        super(pickerId);
    }

    public GroupPickerPopup openPopup()
    {
        return (GroupPickerPopup) getPopup().open();
    }

    public GroupPickerPopup getPopup()
    {
        return pageBinder.bind(GroupPickerPopup.class, this);
    }
}
