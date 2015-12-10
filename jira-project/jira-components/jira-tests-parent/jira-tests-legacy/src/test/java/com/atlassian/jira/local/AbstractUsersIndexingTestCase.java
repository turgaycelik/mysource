/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.local;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.web.action.RedirectSanitiser;

/**
 * This test is to be extended by any test case which creates
 * mock users. It will handle all removals after each test.
 *
 * @deprecated v4.3 - Please stop using these TestCases
 */
public abstract class AbstractUsersIndexingTestCase extends AbstractUsersTestCase
{
    private IssueIndexManager old;

    public AbstractUsersIndexingTestCase(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        old = ManagerFactory.getIndexManager();
        ManagerFactory.addService(RedirectSanitiser.class, new MockRedirectSanitiser());
        ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_DEFAULT_AVATAR_ID, "12345");
    }

    @Override
    protected void tearDown() throws Exception
    {
        ManagerFactory.addService(IssueIndexManager.class, old);
        super.tearDown();
    }
}
