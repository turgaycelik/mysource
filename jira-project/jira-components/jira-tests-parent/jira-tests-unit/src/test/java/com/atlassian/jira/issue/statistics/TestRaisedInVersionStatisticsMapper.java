/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.project.version.VersionManager;

import com.mockobjects.dynamic.Mock;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestRaisedInVersionStatisticsMapper
{
    @Test
    public void testEquals()
    {
        RaisedInVersionStatisticsMapper mapper = new RaisedInVersionStatisticsMapper(null);
        assertTrue(mapper.equals(mapper));
        assertEquals(mapper.hashCode(), mapper.hashCode());

        Mock mockIssueManager = new Mock(VersionManager.class);
        mockIssueManager.setStrict(true);

        RaisedInVersionStatisticsMapper mapper2 = new RaisedInVersionStatisticsMapper((VersionManager) mockIssueManager.proxy());
        assertTrue(mapper.equals(mapper2));
        assertEquals(mapper.hashCode(), mapper2.hashCode());

        assertFalse(mapper.equals(null));
        assertFalse(mapper.equals(new Object()));
        assertFalse(mapper.equals(new IssueKeyStatisticsMapper()));
        assertFalse(mapper.equals(new IssueTypeStatisticsMapper(null)));
        // ensure two VersionStatisticsMapper are not equal even though they both extend
        // VersionStatisticsMapper where the equals and hashCode implementation lives
        assertFalse(mapper.equals(new FixForVersionStatisticsMapper(null)));

        mockIssueManager.verify();
    }

    @Test
    public void testEqualsArchived()
    {
        RaisedInVersionStatisticsMapper mapper = new RaisedInVersionStatisticsMapper(null);

        Mock mockIssueManager = new Mock(VersionManager.class);
        mockIssueManager.setStrict(true);

        RaisedInVersionStatisticsMapper mapper2 = new RaisedInVersionStatisticsMapper((VersionManager) mockIssueManager.proxy(), true);
        assertTrue(mapper.equals(mapper2));
        assertEquals(mapper.hashCode(), mapper2.hashCode());

        RaisedInVersionStatisticsMapper mapper3 = new RaisedInVersionStatisticsMapper((VersionManager) mockIssueManager.proxy(), false);
        assertFalse(mapper.equals(mapper3));
        assertFalse(mapper.hashCode() == mapper3.hashCode());

        RaisedInVersionStatisticsMapper mapper4 = new RaisedInVersionStatisticsMapper((VersionManager) mockIssueManager.proxy(), false);
        assertTrue(mapper3.equals(mapper4));
        assertTrue(mapper3.hashCode() == mapper4.hashCode());

        mockIssueManager.verify();
    }

}
