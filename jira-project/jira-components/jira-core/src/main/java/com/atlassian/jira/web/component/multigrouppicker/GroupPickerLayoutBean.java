package com.atlassian.jira.web.component.multigrouppicker;

import com.atlassian.jira.web.component.PickerLayoutBean;

public class GroupPickerLayoutBean extends PickerLayoutBean
{
    public GroupPickerLayoutBean(final String i18nPrefix, String removeGroupsPrefix, String removeGroupsAction, String addGroupAction)
    {
        super(i18nPrefix, removeGroupsPrefix, removeGroupsAction, addGroupAction, false, "/secure/popups/GroupPickerBrowser.jspa", "groupNames", "groupNames");
    }
}
