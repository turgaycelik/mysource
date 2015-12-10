package com.atlassian.jira.jql.query;

import java.util.Date;
import java.util.List;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlDateSupport;

import com.google.common.collect.ImmutableList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestAbstractDateOperatorQueryFactory
{
    @Mock private JqlDateSupport jqlDateSupport;

    @Test
    public void testGetDateValues() throws Exception
    {
        // Set up
        final Date longDate = new Date(123456789L);
        final Date stringDate = new Date(987654321L);
        when(jqlDateSupport.convertToDate(10L)).thenReturn(longDate);
        when(jqlDateSupport.convertToDate("10")).thenReturn(stringDate);

        final AbstractDateOperatorQueryFactory factory = new AbstractDateOperatorQueryFactory(jqlDateSupport) {};

        // Invoke
        final List<Date> dateValues =
                factory.getDateValues(ImmutableList.of(createLiteral(10L), createLiteral("10"), new QueryLiteral()));

        // Check
        assertEquals(3, dateValues.size());
        assertTrue(dateValues.contains(longDate));
        assertTrue(dateValues.contains(stringDate));
        assertTrue(dateValues.contains(null));
    }
}
