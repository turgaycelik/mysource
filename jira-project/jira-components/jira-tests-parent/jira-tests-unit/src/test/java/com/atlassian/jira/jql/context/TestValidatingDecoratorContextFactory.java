package com.atlassian.jira.jql.context;

import java.util.Collections;

import com.atlassian.jira.jql.validator.OperatorUsageValidator;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link com.atlassian.jira.jql.context.ValidatingDecoratorContextFactory}.
 *
 * @since v4.0
 */
public class TestValidatingDecoratorContextFactory extends MockControllerTestCase
{
    @Test
    public void testInvalidTerminalClause() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl("one", Operator.GREATER_THAN, 2L);
        final ClauseContext context = ClauseContextImpl.createGlobalClauseContext();

        final OperatorUsageValidator usageValidator = getMock(OperatorUsageValidator.class);
        expect(usageValidator.check(null, clause)).andReturn(false);

        ValidatingDecoratorContextFactory factory = instantiate(ValidatingDecoratorContextFactory.class);
        assertEquals(context, factory.getClauseContext(null, clause));

        verify();
    }

    @Test
    public void testValidTerminalClause() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl("one", Operator.GREATER_THAN, 2L);

        final ProjectIssueTypeContext projectCtx = new ProjectIssueTypeContextImpl(
                new ProjectContextImpl(10L),
                new IssueTypeContextImpl("47"));

        final ClauseContext expectedContext = new ClauseContextImpl(Collections.singleton(projectCtx));

        final OperatorUsageValidator usageValidator = getMock(OperatorUsageValidator.class);
        expect(usageValidator.check(null, clause)).andReturn(true);

        final ClauseContextFactory delegate = getMock(ClauseContextFactory.class);
        expect(delegate.getClauseContext(null, clause)).andReturn(expectedContext);

        ValidatingDecoratorContextFactory factory = instantiate(ValidatingDecoratorContextFactory.class);
        assertEquals(expectedContext, factory.getClauseContext(null, clause));

        verify();
    }
}
