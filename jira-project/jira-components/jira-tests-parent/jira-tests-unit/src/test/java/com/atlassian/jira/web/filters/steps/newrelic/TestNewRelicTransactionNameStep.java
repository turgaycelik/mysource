package com.atlassian.jira.web.filters.steps.newrelic;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestNewRelicTransactionNameStep
{
    @Test
    public void testJspaMatcher() {
        assertMatches("/*.jspa", "/secure/CommentAssignIssue!default.jspa");
    }

    @Test
    public void prefixMatching() {
        assertMatches("/browse/*", "/browse/JRA-9");
        assertMatches("/rest/speakeasy/*", "/rest/speakeasy/latest/proxy");
        assertMatches("/rest/activity-stream/*", "/rest/activity-stream/1.0/config");
    }

    private static void assertMatches(String expected, String input)
    {
        String result = NewRelicTransactionNameStep.calculateName(input);
        assertEquals(expected, result);
    }
}
