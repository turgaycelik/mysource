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

public class TestResolutionStatisticsMapper
{
    @Test
    public void testEquals()
    {
        ResolutionStatisticsMapper mapper = new ResolutionStatisticsMapper(null);
        assertEquals(mapper, mapper);
        assertEquals(mapper.hashCode(), mapper.hashCode());

        ResolutionStatisticsMapper mapper2 = new ResolutionStatisticsMapper(null);
        assertEquals(mapper, mapper2);

        Mock mockConstantsManager = new Mock(ConstantsManager.class);
        mockConstantsManager.setStrict(true);

        assertTrue(mapper.equals(new ResolutionStatisticsMapper((ConstantsManager) mockConstantsManager.proxy())));
        assertEquals(mapper.hashCode(), new ResolutionStatisticsMapper((ConstantsManager) mockConstantsManager.proxy()).hashCode());
        mockConstantsManager.verify();

        assertFalse(mapper.equals(null));
        assertFalse(mapper.equals(new Object()));
        assertFalse(mapper.equals(new IssueKeyStatisticsMapper()));
        // Mappers of different issue "constants" are never equal to each other
        assertFalse(mapper.equals(new IssueTypeStatisticsMapper(null)));
        assertFalse(mapper.equals(new StatusStatisticsMapper(null)));
        assertFalse(mapper.equals(new PriorityStatisticsMapper(null)));
    }
}
