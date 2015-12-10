package com.atlassian.jira.issue.search.searchers;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.mock.issue.search.MockSearchContext;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.Query;

import org.junit.Test;

import webwork.action.Action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A test for {@link com.atlassian.jira.issue.search.searchers.SearcherGroup}.
 *
 * @since v4.0
 */
public class TestSearcherGroup
{
    @Test
    public void testConstructor() throws Exception
    {
        //Group test1.
        List<IssueSearcher<?>> list = CollectionBuilder.<IssueSearcher<?>>newBuilder(new MockCustomFieldSearcher("id1"), new MockCustomFieldSearcher("id3")).asList();
        SearcherGroup group = new SearcherGroup(SearcherGroupType.ISSUE, list);
        assertSame(SearcherGroupType.ISSUE, group.getType());
        assertEquals(list, group.getSearchers());
        assertEquals(SearcherGroupType.ISSUE.getI18nKey(), group.getTitleKey());
        assertTrue(group.isPrintHeader());

        //Group test2.
        list = CollectionBuilder.<IssueSearcher<?>>newBuilder(new MockCustomFieldSearcher("547753yrfuwe")).asList();
        group = new SearcherGroup(SearcherGroupType.DATE, list);
        assertSame(SearcherGroupType.DATE, group.getType());
        assertEquals(list, group.getSearchers());
        assertEquals(SearcherGroupType.DATE.getI18nKey(), group.getTitleKey());
        assertTrue(group.isPrintHeader());

        //Group test3.
        list = Collections.emptyList();
        group = new SearcherGroup(SearcherGroupType.CONTEXT, list);
        assertSame(SearcherGroupType.CONTEXT, group.getType());
        assertEquals(list, group.getSearchers());
        assertEquals(SearcherGroupType.CONTEXT.getI18nKey(), group.getTitleKey());
        assertFalse(group.isPrintHeader());
    }

    @Test
    public void testConstructorBad() throws Exception
    {
        try
        {
            new SearcherGroup(null, Collections.<IssueSearcher<?>>emptyList());
            fail("Should not accept invalid argument.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        try
        {
            new SearcherGroup(SearcherGroupType.DATE, null);
            fail("Should not accept invalid argument.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }
    }

    @Test
    public void testIsShown() throws Exception
    {
        MockCustomFieldSearcher searcher1 = new MockCustomFieldSearcher("id1");
        MockCustomFieldSearcher searcher2 = new MockCustomFieldSearcher("id3");

        searcher1.setRenderer(new MockSearchRenderer(false));
        searcher2.setRenderer(new MockSearchRenderer(true));

        List<IssueSearcher<?>> list = CollectionBuilder.<IssueSearcher<?>>newBuilder(searcher1, searcher2).asList();

        SearcherGroup group = new SearcherGroup(SearcherGroupType.CONTEXT, list);
        assertTrue(group.isShown(null, new MockSearchContext()));

        searcher1.setRenderer(new MockSearchRenderer(false));
        searcher2.setRenderer(new MockSearchRenderer(false));

        group = new SearcherGroup(SearcherGroupType.CONTEXT, list);
        assertFalse(group.isShown(null, new MockSearchContext()));

        group = new SearcherGroup(SearcherGroupType.CONTEXT, Collections.<IssueSearcher<?>>emptyList());
        assertFalse(group.isShown(null, new MockSearchContext()));

    }

    @Test
    public void testIsShownBadArg() throws Exception
    {
        SearcherGroup group = new SearcherGroup(SearcherGroupType.CONTEXT, Collections.<IssueSearcher<?>>emptyList());
        try
        {
            group.isShown(null, null);
            fail("Should not accept null context");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }
    }

    private static class MockSearchRenderer implements SearchRenderer
    {
        private final boolean shown;

        public MockSearchRenderer(final boolean shown)
        {
            this.shown = shown;
        }

        public String getEditHtml(final User user, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map<?, ?> displayParameters, final Action action)
        {
            return null;
        }

        public boolean isShown(final User user, final SearchContext searchContext)
        {
            return shown;
        }

        public String getViewHtml(final User user, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map<?, ?> displayParameters, final Action action)
        {
            return null;
        }

        public boolean isRelevantForQuery(final User user, final Query query)
        {
            return false;
        }
    }
}
