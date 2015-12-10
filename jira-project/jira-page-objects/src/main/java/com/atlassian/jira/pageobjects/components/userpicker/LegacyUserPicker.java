package com.atlassian.jira.pageobjects.components.userpicker;

import com.atlassian.pageobjects.elements.PageElement;

import javax.annotation.Nullable;

/**
 * Legacy user picker.
 *
 * @since v5.0
 */
public class LegacyUserPicker extends LegacyPicker
{


    public LegacyUserPicker(@Nullable PageElement form, String pickerId)
    {
        super(form, pickerId);
    }

    public LegacyUserPicker(String pickerId)
    {
        super(pickerId);
    }


    public UserPickerPopup openPopup()
    {
        return (UserPickerPopup) getPopup().open();
    }

    public UserPickerPopup getPopup()
    {
        return pageBinder.bind(UserPickerPopup.class, this);
    }
}
