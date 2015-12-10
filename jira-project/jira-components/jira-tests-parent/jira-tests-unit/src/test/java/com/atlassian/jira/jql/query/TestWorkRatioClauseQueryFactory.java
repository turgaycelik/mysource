package com.atlassian.jira.jql.query;

import java.util.List;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestWorkRatioClauseQueryFactory extends MockControllerTestCase
{
    private MockJqlOperandResolver jqlOperandResolver;
    private ApplicationProperties applicationProperties;

    @Before
    public void setUp() throws Exception
    {
       jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();
        applicationProperties = mockController.getMock(ApplicationProperties.class);
    }

    @Test
    public void testTimeTrackingDisabled() throws Exception
    {
        applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
        mockController.setReturnValue(false);

        mockController.replay();
        final WorkRatioClauseQueryFactory factory = new WorkRatioClauseQueryFactory(jqlOperandResolver, applicationProperties);
        final QueryFactoryResult result = factory.getQuery(null, new TerminalClauseImpl("clause", Operator.EQUALS, "a"));
        assertEquals(QueryFactoryResult.createFalseResult(), result);
        mockController.verify();
    }

    @Test
    public void testTimeTrackingEnabled() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("clause", Operator.EQUALS, "a");

        applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
        mockController.setReturnValue(true);

        final GenericClauseQueryFactory genericClauseQueryFactory = mockController.getMock(GenericClauseQueryFactory.class);
        genericClauseQueryFactory.getQuery(null, clause);
        mockController.setReturnValue(null);

        mockController.replay();
        final WorkRatioClauseQueryFactory factory = new WorkRatioClauseQueryFactory(jqlOperandResolver, applicationProperties)
        {
            @Override
            GenericClauseQueryFactory createGenericClauseQueryFactory(final JqlOperandResolver operandResolver, final List<OperatorSpecificQueryFactory> operatorFactories)
            {
                return genericClauseQueryFactory;
            }
        };

        factory.getQuery(null, clause);
        mockController.verify();
    }
}
