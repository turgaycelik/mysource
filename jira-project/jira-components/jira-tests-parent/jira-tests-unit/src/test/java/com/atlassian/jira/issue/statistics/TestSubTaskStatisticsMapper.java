/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.IssueManager;

import com.mockobjects.dynamic.Mock;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestSubTaskStatisticsMapper
{

    @Test
    public void testEquals()
    {
        SubTaskStatisticsMapper mapper = new SubTaskStatisticsMapper(null);
        assertTrue(mapper.equals(mapper));
        assertEquals(mapper.hashCode(), mapper.hashCode());

        Mock mockIssueManager = new Mock(IssueManager.class);
        mockIssueManager.setStrict(true);

        SubTaskStatisticsMapper mapper2 = new SubTaskStatisticsMapper((IssueManager) mockIssueManager.proxy());
        assertTrue(mapper.equals(mapper2));
        assertEquals(mapper.hashCode(), mapper2.hashCode());

        assertFalse(mapper.equals(null));
        assertFalse(mapper.equals(new Object()));
        assertFalse(mapper.equals(new IssueKeyStatisticsMapper()));
        assertFalse(mapper.equals(new IssueTypeStatisticsMapper(null)));

        mockIssueManager.verify();
    }
}
