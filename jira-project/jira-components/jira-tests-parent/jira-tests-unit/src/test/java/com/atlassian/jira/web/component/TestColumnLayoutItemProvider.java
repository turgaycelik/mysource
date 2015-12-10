package com.atlassian.jira.web.component;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.external.ExternalException;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayout;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.mock.component.MockComponentWorker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestColumnLayoutItemProvider
{
    @Mock
    private ColumnLayoutManager columnLayoutManager;

    private ColumnLayoutItemProvider provider;

    @Before
    public void setUp()
    {
        MockComponentWorker mockComponentWorker = new MockComponentWorker();
        mockComponentWorker.addMock(ColumnLayoutManager.class, columnLayoutManager);
        mockComponentWorker.init();

        provider = new ColumnLayoutItemProvider();
    }

    @Test
    public void getColumnsReturnsUserColumnsWhenThereIsNoSearchRequest() throws Exception
    {
        User user = anyUser();
        SearchRequest searchRequest = null;

        List<ColumnLayoutItem> userLayoutColumns = someColumns();
        ColumnLayout columnLayout = columnLayoutWith(userLayoutColumns);
        when(columnLayoutManager.getColumnLayout(user)).thenReturn(columnLayout);

        List<ColumnLayoutItem> columns = provider.getColumns(user, searchRequest);

        assertThat(columns, is(userLayoutColumns));
        assertWeDoNotIncurOnPerformanceIssues(columnLayout);
    }

    @Test
    public void getColumnsReturnsUserColumnsWhenSearchRequestIsNotLoaded() throws Exception
    {
        User user = anyUser();
        SearchRequest searchRequest = notLoadedSearchRequest();

        List<ColumnLayoutItem> userLayoutColumns = someColumns();
        ColumnLayout columnLayout = columnLayoutWith(userLayoutColumns);
        when(columnLayoutManager.getColumnLayout(user)).thenReturn(columnLayout);

        List<ColumnLayoutItem> columns = provider.getColumns(user, searchRequest);

        assertThat(columns, is(userLayoutColumns));
        assertWeDoNotIncurOnPerformanceIssues(columnLayout);
    }

    @Test
    public void getColumnsReturnsUserColumnsWhenSearchRequestIsLoadedButDoesNotHaveAColumnLayout() throws Exception
    {
        User user = anyUser();
        SearchRequest searchRequest = loadedSearchRequestWithoutLayout();

        List<ColumnLayoutItem> userLayoutColumns = someColumns();
        ColumnLayout columnLayout = columnLayoutWith(userLayoutColumns);
        when(columnLayoutManager.getColumnLayout(user)).thenReturn(columnLayout);

        List<ColumnLayoutItem> columns = provider.getColumns(user, searchRequest);

        assertThat(columns, is(userLayoutColumns));
        assertWeDoNotIncurOnPerformanceIssues(columnLayout);
    }

    @Test
    public void getColumnsReturnsSearchRequestColumnsWhenSearchRequestIsLoadedAndHasAColumnLayout() throws Exception
    {
        User user = anyUser();
        SearchRequest searchRequest = loadedSearchRequestWithLayout();

        List<ColumnLayoutItem> searchRequestLayoutColumns = someColumns();
        ColumnLayout columnLayout = columnLayoutWith(searchRequestLayoutColumns);
        when(columnLayoutManager.getColumnLayout(user, searchRequest)).thenReturn(columnLayout);

        List<ColumnLayoutItem> columns = provider.getColumns(user, searchRequest);

        assertThat(columns, is(searchRequestLayoutColumns));
        assertWeDoNotIncurOnPerformanceIssues(columnLayout);
    }

    @Test
    public void getUserColumns() throws Exception
    {
        User user = anyUser();

        List<ColumnLayoutItem> userLayoutColumns = someColumns();
        ColumnLayout columnLayout = columnLayoutWithVisibleColumnsForUser(user, userLayoutColumns);
        when(columnLayoutManager.getColumnLayout(user)).thenReturn(columnLayout);

        List<ColumnLayoutItem> columns = provider.getUserColumns(user);

        assertThat(columns, is(userLayoutColumns));
        assertWeDoNotIncurOnPerformanceIssues(columnLayout);
    }

    private List<ColumnLayoutItem> someColumns()
    {
        return Arrays.asList(mock(ColumnLayoutItem.class));
    }

    private ColumnLayout columnLayoutWith(final List<ColumnLayoutItem> columns)
    {
        ColumnLayout columnLayout = mock(ColumnLayout.class);
        when(columnLayout.getColumnLayoutItems()).thenReturn(columns);
        return columnLayout;
    }

    private ColumnLayout columnLayoutWithVisibleColumnsForUser(final User user, final List<ColumnLayoutItem> columns) throws Exception
    {
        ColumnLayout columnLayout = mock(ColumnLayout.class);
        when(columnLayout.getAllVisibleColumnLayoutItems(user)).thenReturn(columns);
        return columnLayout;
    }

    private User anyUser()
    {
        return mock(User.class);
    }

    private void assertWeDoNotIncurOnPerformanceIssues(ColumnLayout columnLayout) throws Exception
    {
        // This verify is very important. Do not remove it. We do not want you to call getVisibleColumnLayoutItems since
        // it has a big penalty on performance on large instances of JIRA. See https://jira.atlassian.com/browse/JRA-25721
        // for more details on the issue
        verify(columnLayout, never()).getVisibleColumnLayoutItems(any(User.class), any(QueryContext.class));
    }

    private SearchRequest loadedSearchRequestWithLayout()
    {
        return mockSearchRequest(true, true);
    }

    private SearchRequest loadedSearchRequestWithoutLayout()
    {
        return mockSearchRequest(true, false);
    }

    private SearchRequest notLoadedSearchRequest()
    {
        return mockSearchRequest(false, false);
    }

    private SearchRequest mockSearchRequest(boolean loaded, boolean hasColumnLayout)
    {
        SearchRequest searchRequest = mock(SearchRequest.class);
        when(searchRequest.isLoaded()).thenReturn(loaded);
        when(searchRequest.useColumns()).thenReturn(hasColumnLayout);
        return searchRequest;
    }
}
