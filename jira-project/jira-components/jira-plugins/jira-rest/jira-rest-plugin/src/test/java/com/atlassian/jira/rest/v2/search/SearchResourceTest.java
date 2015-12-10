package com.atlassian.jira.rest.v2.search;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.rest.v2.issue.builder.BeanBuilderFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.issue.IssueSearchLimits;
import com.atlassian.jira.web.bean.PagerFilter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class SearchResourceTest
{
    @Mock
    SearchService searchService;

    @Mock
    JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    IssueSearchLimits issueSearchLimits;

    @Mock
    BeanBuilderFactory beanBuildFactory;

    @Test
    public void testCreateFilterShouldNotCreateUnlimitedFilterWhenMaxResultsIsMinusOne() throws Exception
    {
        when(issueSearchLimits.getMaxResults()).thenReturn(200);

        SearchResource search = new SearchResource(searchService, jiraAuthenticationContext, issueSearchLimits, beanBuildFactory);
        PagerFilter filter = search.createFilter(0, -1);

        assertThat(filter.getMax(), equalTo(200));
    }

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }
}
