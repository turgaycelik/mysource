package com.atlassian.jira.jql.query;

import java.util.List;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.IndexValueConverter;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestAbstractActualValueOperatorQueryFactory
{
    @Mock private IndexValueConverter indexValueConverter;

    @After
    public void tearDown() throws Exception
    {
        indexValueConverter = null;
    }

    @Test
    public void testGetIndexValues() throws Exception
    {
        final QueryLiteral literal1 = createLiteral(10L);
        final QueryLiteral literal2 = createLiteral("10");
        final QueryLiteral literal3 = new QueryLiteral();

        when(indexValueConverter.convertToIndexValue(literal1)).thenReturn("Something");

        final AbstractActualValueOperatorQueryFactory factory = new Fixture(indexValueConverter);

        final List<QueryLiteral> inputList = CollectionBuilder.newBuilder(literal1, literal2, literal3).asList();
        final List<String> result = factory.getIndexValues(inputList);

        assertThat(result, containsInAnyOrder("Something", null));

        verify(indexValueConverter).convertToIndexValue(literal1);
        verify(indexValueConverter).convertToIndexValue(literal2);
        verifyNoMoreInteractions(indexValueConverter);
    }

    static class Fixture extends AbstractActualValueOperatorQueryFactory
    {
        Fixture(IndexValueConverter indexValueConverter)
        {
            super(indexValueConverter);
        }
    }
}
