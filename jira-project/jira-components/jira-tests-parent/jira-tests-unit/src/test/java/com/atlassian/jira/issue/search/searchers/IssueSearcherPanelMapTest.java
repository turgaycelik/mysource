package com.atlassian.jira.issue.search.searchers;

import com.atlassian.jira.issue.search.searchers.impl.AssigneeSearcher;
import com.atlassian.jira.issue.search.searchers.impl.DueDateSearcher;
import com.atlassian.jira.issue.search.searchers.impl.ProjectSearcher;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Tests {@code IssueSearcherPanelMap}.
 *
 * @since v5.1
 */
public class IssueSearcherPanelMapTest
{
    /**
     * When given a {@code IssueSearcher} corresponding to a field displayed in
     * the "Dates" panel, {@code getPanel} should return the dates panel.
     */
    @Test
    public void testGetPanelDateField()
    {
        assertEquals(IssueSearcherPanelMap.Panel.DATES,
                IssueSearcherPanelMap.getPanel(DueDateSearcher.class));
    }

    /**
     * When given a {@code IssueSearcher} corresponding to a field displayed in
     * the "Details" panel, {@code getPanel} should return the details panel.
     */
    @Test
    public void testGetPanelDetailsField()
    {
        assertEquals(IssueSearcherPanelMap.Panel.DETAILS,
                IssueSearcherPanelMap.getPanel(ProjectSearcher.class));
    }

    /**
     * When given a non-specific {@code IssueSearcher}, {@code getPanel}
     * should return the details panel.
     */
    @Test
    public void testGetPanelGeneral()
    {
        assertEquals(IssueSearcherPanelMap.Panel.DETAILS,
                IssueSearcherPanelMap.getPanel(IssueSearcher.class));
    }

    /**
     * When given a {@code IssueSearcher} corresponding to a field displayed in
     * the "People" panel, {@code getPanel} should return the people panel.
     */
    @Test
    public void testGetPanelPeopleField()
    {
        assertEquals(IssueSearcherPanelMap.Panel.PEOPLE,
                IssueSearcherPanelMap.getPanel(AssigneeSearcher.class));
    }
}