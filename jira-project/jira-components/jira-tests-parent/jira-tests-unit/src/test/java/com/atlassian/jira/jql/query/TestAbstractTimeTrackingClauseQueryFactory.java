package com.atlassian.jira.jql.query;

import java.util.List;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.JqlTimetrackingDurationSupport;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestAbstractTimeTrackingClauseQueryFactory
{
    @Mock private JqlTimetrackingDurationSupport jqlTimetrackingDurationSupport;
    @Mock private ApplicationProperties applicationProperties;

    private MockJqlOperandResolver jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();

    @After
    public void tearDown()
    {
        jqlOperandResolver = null;
        jqlTimetrackingDurationSupport = null;
        applicationProperties = null;
    }

    @Test
    public void testTimeTrackingDisabled() throws Exception
    {
        final AbstractTimeTrackingClauseQueryFactory factory = new AbstractTimeTrackingClauseQueryFactory("field", jqlOperandResolver, jqlTimetrackingDurationSupport, applicationProperties);
        final QueryFactoryResult result = factory.getQuery(null, new TerminalClauseImpl("clause", Operator.EQUALS, "a"));
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testTimeTrackingEnabled() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("clause", Operator.EQUALS, "a");

        when(applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING)).thenReturn(true);

        final GenericClauseQueryFactory genericClauseQueryFactory = mock(GenericClauseQueryFactory.class);

        final AbstractTimeTrackingClauseQueryFactory factory = new AbstractTimeTrackingClauseQueryFactory("field", jqlOperandResolver, jqlTimetrackingDurationSupport, applicationProperties)
        {
            @Override
            GenericClauseQueryFactory createGenericClauseQueryFactory(final String indexField, final JqlOperandResolver operandResolver, final List<OperatorSpecificQueryFactory> operatorFactories)
            {
                return genericClauseQueryFactory;
            }
        };

        factory.getQuery(null, clause);

        verify(genericClauseQueryFactory).getQuery(null, clause);
    }
}
