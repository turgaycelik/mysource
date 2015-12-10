package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.jira.issue.search.ClauseNames;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Simple test for {@link DateSearcherConfig}.
 *
 * @since v4.0
 */
public class TestDateSearcherConfig
{
    @Test
    public void testConstructorBad() throws Exception
    {
        final ClauseNames clauseNames = new ClauseNames("blah");
        try
        {
            new DateSearcherConfig("", clauseNames, "");
            fail("Expected an exception to be thrown.");
        }
        catch (IllegalArgumentException expected)
        {
        }

        try
        {
            new DateSearcherConfig(null, clauseNames, "");
            fail("Expected an exception to be thrown.");
        }
        catch (IllegalArgumentException expected)
        {
        }

        try
        {
            new DateSearcherConfig("   ", clauseNames, "");
            fail("Expected an exception to be thrown.");
        }
        catch (IllegalArgumentException expected)
        {
        }

        try
        {
            new DateSearcherConfig("blah", null, "");
            fail("Expected an exception to be thrown.");
        }
        catch (IllegalArgumentException expected)
        {
        }

        try
        {
            new DateSearcherConfig("blah", clauseNames, "");
            fail("Expected an exception to be thrown.");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    @Test
    public void testConstructor() throws Exception
    {
        final String id = "test";
        final String jqlName = "clause";
        ClauseNames clauseNames = new ClauseNames(jqlName);

        final DateSearcherConfig searcherConfig = new DateSearcherConfig(id, clauseNames, jqlName);
        assertEquals(jqlName, searcherConfig.getClauseNames().getPrimaryName());
        assertEquals(jqlName, searcherConfig.getFieldName());
        assertEquals(id, searcherConfig.getId());
        assertEquals(id + ":before", searcherConfig.getBeforeField());
        assertEquals(id + ":after", searcherConfig.getAfterField());
        assertEquals(id + ":previous", searcherConfig.getPreviousField());
        assertEquals(id + ":next", searcherConfig.getNextField());
    }
}
