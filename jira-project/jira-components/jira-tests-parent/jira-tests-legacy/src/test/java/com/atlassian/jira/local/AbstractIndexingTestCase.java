/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.local;

/**
 * @deprecated v4.3 - Please stop using these TestCases
 */
public abstract class AbstractIndexingTestCase extends LegacyJiraMockTestCase
{
    public AbstractIndexingTestCase(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
}
