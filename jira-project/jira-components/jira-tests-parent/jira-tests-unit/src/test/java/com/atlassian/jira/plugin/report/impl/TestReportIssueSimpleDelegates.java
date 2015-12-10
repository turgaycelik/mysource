package com.atlassian.jira.plugin.report.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculator;

import org.apache.commons.collections.PredicateUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestReportIssueSimpleDelegates
{
    private final AggregateTimeTrackingCalculator calculator = new AggregateTimeTrackingCalculator()
    {

        public AggregateTimeTrackingBean getAggregates(Issue issue)
        {
            return new AggregateTimeTrackingBean(new Long(1), new Long(2), new Long(3), 0);
        }
    };
    private final Issue issue = new MockSubTaskedIssue(new Long(1));
    private final ReportIssue reportIssue = new ReportIssue(issue, calculator, null, null, null, PredicateUtils.truePredicate());

    @Test
    public void testKey()
    {
        assertEquals(issue.getKey(), reportIssue.getKey());
    }

    @Test
    public void testSummary()
    {
        assertEquals(issue.getSummary(), reportIssue.getSummary());
    }

    @Test
    public void testStatus()
    {
        assertEquals(issue.getStatusObject(), reportIssue.getStatus());
    }

    @Test
    public void testPriority()
    {
        assertEquals(issue.getPriority(), reportIssue.getPriority());
    }

    @Test
    public void testIssueType()
    {
        assertEquals(issue.getIssueTypeObject(), reportIssue.getIssueType());
    }

    @Test
    public void testGetIssue()
    {
        assertEquals(issue, reportIssue.getIssue());
    }
}
