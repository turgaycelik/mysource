package com.atlassian.jira.web.component.multiuserpicker;

import com.atlassian.jira.web.component.PickerLayoutBean;

public class UserPickerLayoutBean extends PickerLayoutBean
{
    public UserPickerLayoutBean(final String i18nPrefix, String removeUsersPrefix, String removeUsersAction, String addUserAction)
    {
        super(i18nPrefix, removeUsersPrefix, removeUsersAction, addUserAction, true, "/secure/popups/UserPickerBrowser.jspa", "userNames", "userNames");
    }
}
