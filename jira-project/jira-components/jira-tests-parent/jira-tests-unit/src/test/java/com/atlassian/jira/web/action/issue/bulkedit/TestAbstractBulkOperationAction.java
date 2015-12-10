package com.atlassian.jira.web.action.issue.bulkedit;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.JiraAuthenticationContext;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestAbstractBulkOperationAction
{
    @Mock
    private ColumnLayoutItemProvider columnsProvider;
    @Mock
    private JiraAuthenticationContext authenticationContext;

    private TestableAbstractBulkOperationAction action;

    @Before
    public void setUp()
    {
        MockComponentWorker mockComponentWorker = new MockComponentWorker();
        mockComponentWorker.addMock(JiraAuthenticationContext.class, authenticationContext);
        mockComponentWorker.init();

        action = new TestableAbstractBulkOperationAction(columnsProvider);
    }

    @Test
    public void getColumnsDelegatesOnColumnLayoutItemProvider() throws Exception
    {
        User user = mockLoggedInUser();
        SearchRequest searchRequest = mockSearchRequest();

        List<ColumnLayoutItem> expectedColumns = someColumns();
        when(columnsProvider.getColumns(user, searchRequest)).thenReturn(expectedColumns);

        List<ColumnLayoutItem> actualColumns = action.getColumns();

        assertThat(actualColumns, is(expectedColumns));
    }

    private User mockLoggedInUser()
    {
        User user = mock(User.class);
        when(authenticationContext.getLoggedInUser()).thenReturn(user);
        return user;
    }

    private SearchRequest mockSearchRequest()
    {
        SearchRequest searchRequest = mock(SearchRequest.class);
        action.setSearchRequest(searchRequest);
        return searchRequest;
    }

    private List<ColumnLayoutItem> someColumns()
    {
        return Arrays.asList(mock(ColumnLayoutItem.class));
    }

    private class TestableAbstractBulkOperationAction extends AbstractBulkOperationAction
    {
        private SearchRequest searchRequest;
        private ColumnLayoutItemProvider columnsProvider;

        public TestableAbstractBulkOperationAction(final ColumnLayoutItemProvider columnsProvider)
        {
            super(null, null);
            this.columnsProvider = columnsProvider;
        }

        public void setSearchRequest(SearchRequest searchRequest)
        {
            this.searchRequest = searchRequest;
        }

        @Override
        public SearchRequest getSearchRequest()
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
