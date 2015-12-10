package com.atlassian.jira.issue.search.searchers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.search.searchers.impl.FixForVersionsSearcher;
import com.atlassian.jira.issue.search.searchers.impl.IssueTypeSearcher;
import com.atlassian.jira.issue.search.searchers.impl.ProjectSearcher;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.IssueTypeResolver;
import com.atlassian.jira.jql.resolver.ProjectResolver;
import com.atlassian.jira.jql.resolver.VersionResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.3
 */
public class TestSearcherComparatorFactory extends MockControllerTestCase
{
    @Test
    public void testCompareAndSort()
    {
        // Set up Mocks
        JqlOperandResolver mockJqlOperandResolver = createMock(JqlOperandResolver.class);
        FieldFlagOperandRegistry fieldFlagOperandRegistry = createMock(FieldFlagOperandRegistry.class);
        VersionResolver versionResolver = createMock(VersionResolver.class);

        ProjectSearcher projectSearcher = new ProjectSearcher(null, createMock(ProjectResolver.class), null, null, null, null, null, null, null, null);
        IssueTypeSearcher issueTypeSearcher = new IssueTypeSearcher(null, mockJqlOperandResolver, null, null, null, createMock(IssueTypeResolver.class), null, null, null, null, fieldFlagOperandRegistry, null);
        FixForVersionsSearcher fixForVersionsSearcher = new FixForVersionsSearcher(versionResolver, mockJqlOperandResolver, fieldFlagOperandRegistry, null, null, null, null, null, null, null);

        mockController.replay();

        // Get the CONTEXT Searcher Comparator
        final Comparator<IssueSearcher<?>> issueSearcherComparator = SearcherComparatorFactory.getSearcherComparator(SearcherGroupType.CONTEXT);

        assertEquals(0, issueSearcherComparator.compare(projectSearcher, projectSearcher));
        assertTrue(issueSearcherComparator.compare(projectSearcher, issueTypeSearcher) < 0);
        assertTrue(issueSearcherComparator.compare(issueTypeSearcher, projectSearcher) > 0);
        assertTrue(issueSearcherComparator.compare(issueTypeSearcher, fixForVersionsSearcher) < 0);
        assertTrue(issueSearcherComparator.compare(fixForVersionsSearcher, issueTypeSearcher) > 0);
        assertTrue(issueSearcherComparator.compare(projectSearcher, fixForVersionsSearcher) < 0);
        assertTrue(issueSearcherComparator.compare(fixForVersionsSearcher, projectSearcher) > 0);

        List<IssueSearcher<SearchableField>> searchers = CollectionBuilder.<IssueSearcher<SearchableField>>newBuilder(issueTypeSearcher, fixForVersionsSearcher, projectSearcher).asMutableList();
        Collections.sort(searchers, issueSearcherComparator);

        List<IssueSearcher<SearchableField>> expectedList = CollectionBuilder.<IssueSearcher<SearchableField>>newBuilder(projectSearcher, issueTypeSearcher, fixForVersionsSearcher).asList();
        assertEquals(expectedList, searchers);
    }
}
