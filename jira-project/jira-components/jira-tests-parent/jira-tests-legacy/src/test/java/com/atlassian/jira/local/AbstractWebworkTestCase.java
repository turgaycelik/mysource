/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.local;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.project.DefaultProjectManager;
import com.atlassian.jira.project.ProjectManager;

/**
 * An abstract class that makes sure the WW ActionContext is reset after each test
 *
 * @deprecated v4.3 - Please stop using these TestCases
 */
@Deprecated
public abstract class AbstractWebworkTestCase extends LegacyJiraMockTestCase
{
    public AbstractWebworkTestCase(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        UtilsForTests.cleanWebWork();
        JiraTestUtil.loginUser(null);
    }

    protected void tearDown() throws Exception
    {
        UtilsForTests.cleanWebWork();
        JiraTestUtil.loginUser(null);
        super.tearDown();
    }
}
