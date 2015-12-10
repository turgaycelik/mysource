package com.atlassian.jira.issue.search.searchers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link com.atlassian.jira.issue.search.searchers.SearcherComparatorFactory.SearcherComparator}.
 *
 * @since v4.0
 */
public class TestSearcherComparator
{
    @Test
    public void testComparator()
    {
        List<Class<? extends IssueSearcher<?>>> searcher = CollectionBuilder.<Class<? extends IssueSearcher<?>>>newBuilder(OneIssueSearcher.class, TwoIssueSearcher.class, ThreeIssueSearcher.class).asList();

        Comparator<IssueSearcher<?>> issueSearcherComparator = new SearcherComparatorFactory.SearcherComparator(searcher);

        OneIssueSearcher searcher1 = new OneIssueSearcher();
        TwoIssueSearcher searcher2 = new TwoIssueSearcher();
        ThreeIssueSearcher searcher3 = new ThreeIssueSearcher();
        FourIssueSearcher searcher4 = new FourIssueSearcher();
        FiveIssueSearcher searcher5 = new FiveIssueSearcher();

        assertEquals(0, issueSearcherComparator.compare(searcher1, searcher1));
        assertTrue(issueSearcherComparator.compare(searcher1, searcher2) < 0);
        assertTrue(issueSearcherComparator.compare(searcher2, searcher1) > 0);
        assertTrue(issueSearcherComparator.compare(searcher1, searcher3) < 0);
        assertTrue(issueSearcherComparator.compare(searcher3, searcher1) > 0);
        assertTrue(issueSearcherComparator.compare(searcher1, searcher4) < 0);
        assertTrue(issueSearcherComparator.compare(searcher4, searcher3) > 0);

        //Objects not in the list should be equal.
        assertEquals(0, issueSearcherComparator.compare(searcher4, searcher5));
        assertEquals(0, issueSearcherComparator.compare(searcher5, searcher4));

        List<MockCustomFieldSearcher> sort = CollectionBuilder.newBuilder(searcher5, searcher3, searcher1, searcher2, searcher4).asMutableList();
        Collections.sort(sort, issueSearcherComparator);

        List<MockCustomFieldSearcher> expectedList = CollectionBuilder.newBuilder(searcher1, searcher2, searcher3, searcher5, searcher4).asList();
        assertEquals(expectedList, sort);
    }

    private static class OneIssueSearcher extends MockCustomFieldSearcher
    {
        public OneIssueSearcher()
        {
            super("one");
        }
    }

    private static class TwoIssueSearcher extends MockCustomFieldSearcher
    {
        public TwoIssueSearcher()
        {
            super("two");
        }
    }

    private static class ThreeIssueSearcher extends MockCustomFieldSearcher
    {
        public ThreeIssueSearcher()
        {
            super("three");
        }
    }

    private static class FourIssueSearcher extends MockCustomFieldSearcher
    {
        public FourIssueSearcher()
        {
            super("four");
        }
    }

    private static class FiveIssueSearcher extends MockCustomFieldSearcher
    {
        public FiveIssueSearcher()
        {
            super("five");
        }
    }
}
