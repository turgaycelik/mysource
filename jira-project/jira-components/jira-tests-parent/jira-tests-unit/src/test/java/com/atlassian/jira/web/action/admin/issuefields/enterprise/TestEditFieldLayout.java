/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.admin.issuefields.AbstractConfigureFieldLayout;
import com.atlassian.jira.web.action.admin.issuefields.AbstractTestViewIssueFields;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.ofbiz.core.entity.GenericValue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (ListeningMockitoRunner.class)
public class TestEditFieldLayout extends AbstractTestViewIssueFields
{
    private ConfigureFieldLayout editFieldLayout;
    private GenericValue testEntity;

    public TestEditFieldLayout()
    {
        super();
    }

    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        // Create a scheme
        testEntity = mock(GenericValue.class);
        when(testEntity.getString("name")).thenReturn("Name");
        UtilsForTests.cleanWebWork();
    }

    public void setNewVif()
    {
        editFieldLayout = new ConfigureFieldLayout(null, null, reindexMessageManager, fieldManager, fieldLayoutSchemeHelper, null, null, managedConfigurationItemService)
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
        editFieldLayout.setId(testEntity.getLong("id"));
    }

    public AbstractConfigureFieldLayout getVif()
    {
        if (editFieldLayout == null)
        {
            setNewVif();
        }
        return editFieldLayout;
    }

    public void refreshVif()
    {
        editFieldLayout = null;
    }
}
