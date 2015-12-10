/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.config.ConstantsManager;

import com.mockobjects.dynamic.Mock;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestIssueTypeStatisticsMapper
{
    @Test
    public void testEquals()
    {
        IssueTypeStatisticsMapper mapper = new IssueTypeStatisticsMapper(null);
        assertEquals(mapper, mapper);
        assertEquals(mapper.hashCode(), mapper.hashCode());

        IssueTypeStatisticsMapper mapper2 = new IssueTypeStatisticsMapper(null);
        assertEquals(mapper, mapper2);

        Mock mockConstantsManager = new Mock(ConstantsManager.class);
        mockConstantsManager.setStrict(true);

        assertTrue(mapper.equals(new IssueTypeStatisticsMapper((ConstantsManager) mockConstantsManager.proxy())));
        assertEquals(mapper.hashCode(), new IssueTypeStatisticsMapper((ConstantsManager) mockConstantsManager.proxy()).hashCode());
        mockConstantsManager.verify();

        assertFalse(mapper.equals(null));
        assertFalse(mapper.equals(new Object()));
        assertFalse(mapper.equals(new IssueKeyStatisticsMapper()));
        // Mappers of different issue "constants" are never equal to each other 
        assertFalse(mapper.equals(new PriorityStatisticsMapper(null)));
        assertFalse(mapper.equals(new StatusStatisticsMapper(null)));
        assertFalse(mapper.equals(new ResolutionStatisticsMapper(null)));
    }
}
