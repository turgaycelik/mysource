package com.atlassian.jira.jql.query;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;

import com.google.common.collect.ImmutableList;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestAbstractOperatorQueryFactory
{
    @Mock private IndexInfoResolver<?> indexInfoResolver;

    @After
    public void tearDown()
    {
        indexInfoResolver = null;
    }

    @Test
    public void testGetIndexValues() throws Exception
    {
        QueryLiteral literal1 = createLiteral("10");
        QueryLiteral literal2 = createLiteral(20L);
        QueryLiteral literal3 = createLiteral(30L);
        QueryLiteral literal4 = new QueryLiteral();

        when(indexInfoResolver.getIndexedValues("10")).thenReturn(ImmutableList.of("10", "20"));
        when(indexInfoResolver.getIndexedValues(20L)).thenReturn(Collections.<String>emptyList());
        when(indexInfoResolver.getIndexedValues(30L)).thenReturn(null);

        final AbstractOperatorQueryFactory<?> factory = new AbstractOperatorQueryFactory<Object>(indexInfoResolver)
        {
        };

        List<String> result = factory.getIndexValues(ImmutableList.of(literal1, literal2, literal3, literal4));
        assertThat(result, containsInAnyOrder(null, "10", "20"));

        result = factory.getIndexValues(ImmutableList.<QueryLiteral>of());
        assertThat(result, hasSize(0));

        result = factory.getIndexValues(null);
        assertThat(result, hasSize(0));
    }
}
