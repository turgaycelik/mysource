package com.atlassian.jira.web.action.issue.util;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.web.action.issue.IssueSearchLimits;
import com.atlassian.jira.web.action.issue.IssueSearchLimitsImpl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the SearchLimits class.
 *
 * @since v4.3
 */
public class TestSearchLimits extends MockControllerTestCase
{
    /**
     * Make sure the helper returns the correct tempMax.
     */
    @Test
    public void testGetMaxResults()
    {
        final int expectedMax = 56673;

        final ApplicationProperties properties = new MockApplicationProperties();
        properties.setText(APKeys.JIRA_SEARCH_VIEWS_DEFAULT_MAX, Integer.toString(expectedMax));

        mockController.addObjectInstance(properties);

        final IssueSearchLimits issueSearchLimits = mockController.instantiate(IssueSearchLimitsImpl.class);
        final int actualMax = issueSearchLimits.getMaxResults();
        assertEquals(expectedMax, actualMax);

        mockController.verify();
    }

    /**
     * Make sure the helper returns the correct tempMax.
     */
    @Test
    public void testGetMaxResultsBadNumber()
    {
        final ApplicationProperties properties = new MockApplicationProperties();
        properties.setText(APKeys.JIRA_SEARCH_VIEWS_DEFAULT_MAX, "notANumber");

        mockController.addObjectInstance(properties);

        final IssueSearchLimits issueSearchLimits = mockController.instantiate(IssueSearchLimitsImpl.class);
        final int actualMax = issueSearchLimits.getMaxResults();
        assertEquals(1000, actualMax);

        mockController.verify();
    }

    /**
     * Make sure the helper returns the correct tempMax when property is null.
     */
    @Test
    public void testGetMaxResultsEmpty()
    {
        final ApplicationProperties properties = new MockApplicationProperties();
        properties.setText(APKeys.JIRA_SEARCH_VIEWS_DEFAULT_MAX, "");

        mockController.addObjectInstance(properties);

        final IssueSearchLimits issueSearchLimits = mockController.instantiate(IssueSearchLimitsImpl.class);
        final int actualMax = issueSearchLimits.getMaxResults();
        assertEquals(1000, actualMax);

        mockController.verify();
    }
}
