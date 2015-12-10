/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.I18nHelper;

public class TestViewIssueFields extends AbstractTestViewIssueFields
{
    private ViewIssueFields viewIssueFields;

    public TestViewIssueFields()
    {
        super();
    }

    public void setNewVif()
    {
        viewIssueFields = new ViewIssueFields(null, null, reindexMessageManager, fieldManager, fieldLayoutSchemeHelper, null, null, managedConfigurationItemService)
        {
            protected I18nHelper getI18nHelper()
            {
                return i18Helper;
            }

            public User getLoggedInUser()
            {
                return mockUser;
            }
        };
    }

    public AbstractConfigureFieldLayout getVif()
    {
        if (viewIssueFields == null)
        {
            setNewVif();
        }
        return viewIssueFields;
    }

    public void refreshVif()
    {
        viewIssueFields = null;
    }
}
