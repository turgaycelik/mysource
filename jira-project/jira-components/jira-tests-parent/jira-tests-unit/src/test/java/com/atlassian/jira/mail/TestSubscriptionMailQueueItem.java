package com.atlassian.jira.mail;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayout;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutException;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.component.ColumnLayoutItemProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestSubscriptionMailQueueItem
{
    @Mock
    private ColumnLayoutItemProvider columnsProvider;

    private TestableSubscriptionMailQueueItem queueItem;

    @Before
    public void setUp()
    {
        queueItem = new TestableSubscriptionMailQueueItem(columnsProvider);
    }

    @Test
    public void getColumnsReturnsUserColumnsIfThereIsNoSearchRequest() throws Exception
    {
        ApplicationUser user = mockApplicationUser();
        SearchRequest searchRequest = null;

        List<ColumnLayoutItem> expectedColumns = someColumns();
        when(columnsProvider.getUserColumns(user.getDirectoryUser())).thenReturn(expectedColumns);

        queueItem.setSearchRequest(searchRequest);
        List<ColumnLayoutItem> actualColumns = queueItem.getColumns(user);

        assertThat(actualColumns, is(expectedColumns));
    }

    @Test
    public void getColumnsTakesIntoAccountSearchResultWhenReturningTheColumnsIfThereIsASearchResult() throws Exception
    {
        ApplicationUser user = mockApplicationUser();
        SearchRequest searchRequest = mock(SearchRequest.class);

        List<ColumnLayoutItem> expectedColumns = someColumns();
        when(columnsProvider.getColumns(user.getDirectoryUser(), searchRequest)).thenReturn(expectedColumns);

        queueItem.setSearchRequest(searchRequest);
        List<ColumnLayoutItem> actualColumns = queueItem.getColumns(user);

        assertThat(actualColumns, is(expectedColumns));
    }

    private List<ColumnLayoutItem> someColumns()
    {
        return Arrays.asList(mock(ColumnLayoutItem.class));
    }

    private ApplicationUser mockApplicationUser()
    {
        ApplicationUser user = mock(ApplicationUser.class);
        when(user.getDirectoryUser()).thenReturn(mock(User.class));
        return user;
    }

    private class TestableSubscriptionMailQueueItem extends SubscriptionMailQueueItem
    {
        private SearchRequest searchRequest;
        private ColumnLayoutItemProvider columnsProvider;

        TestableSubscriptionMailQueueItem(ColumnLayoutItemProvider columnsProvider)
        {
            super(null, null, null, null, null, null);
            this.columnsProvider = columnsProvider;
        }

        private void setSearchRequest(SearchRequest searchRequest)
        {
            this.searchRequest = searchRequest;
        }

        @Override
        SearchRequest getSearchRequest()
        {
            return this.searchRequest;
        }

        @Override
        ColumnLayoutItemProvider getColumnsProvider()
        {
            return this.columnsProvider;
        }
    }
}
