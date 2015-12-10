package com.atlassian.jira.web.component;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem;
import com.atlassian.jira.issue.search.SearchRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestTableLayoutFactory
{
    @Mock
    private ColumnLayoutItemProvider columnsProvider;

    private TestableTableLayoutFactory factory;

    @Before
    public void setUp()
    {
        factory = new TestableTableLayoutFactory(columnsProvider);
    }

    @Test (expected = NullPointerException.class)
    public void getUserColumnsThrowsExceptionIfSearchRequestIsNull()
    {
        factory.getUserColumns(null, mock(User.class));
    }

    @Test
    public void getUserColumnsDelegatesOnColumnLayoutItemProvider() throws Exception
    {
        SearchRequest searchRequest = mock(SearchRequest.class);
        User user = mock(User.class);

        List<ColumnLayoutItem> expectedColumns = someColumns();
        when(columnsProvider.getColumns(user, searchRequest)).thenReturn(expectedColumns);

        List<ColumnLayoutItem> actualColumns = factory.getUserColumns(searchRequest, user);

        assertThat(actualColumns, is(expectedColumns));
    }

    @Test
    public void getUserColumnsWrapsAnyExceptionInsideARuntimeException() throws Exception
    {
        Exception originalException = new Exception();
        List<ColumnLayoutItem> expectedColumns = someColumns();
        when(columnsProvider.getColumns(any(User.class), any(SearchRequest.class))).thenThrow(originalException);

        try
        {
            factory.getUserColumns(null, null);
            fail("A RuntimeException was expected to be thrown");
        }
        catch (RuntimeException e)
        {
            assertThat(e.getCause(), is(originalException.getCause()));
        }
    }

    private List<ColumnLayoutItem> someColumns()
    {
        return Arrays.asList(mock(ColumnLayoutItem.class));
    }

    private class TestableTableLayoutFactory extends TableLayoutFactory
    {
        private ColumnLayoutItemProvider columnsProvider;

        public TestableTableLayoutFactory(ColumnLayoutItemProvider columnsProvider)
        {
            super(null, null, null, null, null, null);
            this.columnsProvider = columnsProvider;
        }

        @Override
        ColumnLayoutItemProvider getColumnsProvider()
        {
            return this.columnsProvider;
        }
    }
}
