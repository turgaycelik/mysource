/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.statistics;

import org.apache.lucene.document.NumberTools;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestVotesStatisticsMapper
{
    @Test
    public void testEquals()
    {
        VotesStatisticsMapper sorter = new VotesStatisticsMapper();
        assertEquals(sorter, sorter);
        assertEquals(sorter.hashCode(), sorter.hashCode());

        VotesStatisticsMapper sorter2 = new VotesStatisticsMapper();
        assertEquals(sorter, sorter2);
        assertEquals(sorter.hashCode(), sorter2.hashCode());

        assertFalse(sorter.equals(null));
        assertFalse(sorter.equals(new Object()));
        assertFalse(sorter.equals(new IssueKeyStatisticsMapper()));
    }

    @Test
    public void testGetDocumentValue() throws Exception
    {
        VotesStatisticsMapper sorter = new VotesStatisticsMapper();
        assertEquals(10L, sorter.getValueFromLuceneField(NumberTools.longToString(10L)));
        assertEquals(-1L, sorter.getValueFromLuceneField(NumberTools.longToString(-1)));
        assertEquals(0L, sorter.getValueFromLuceneField(NumberTools.longToString(0L)));
        assertEquals(NumberTools.stringToLong(NumberTools.MAX_STRING_VALUE), sorter.getValueFromLuceneField(NumberTools.MAX_STRING_VALUE));
        assertEquals(NumberTools.stringToLong(NumberTools.MIN_STRING_VALUE), sorter.getValueFromLuceneField(NumberTools.MIN_STRING_VALUE));
    }
}
