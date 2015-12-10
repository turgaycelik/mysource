package com.atlassian.jira.plugin.jql.function;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for the {@link com.atlassian.jira.plugin.jql.function.CurrentUserFunction}.
 *
 * @since v4.0
 */
public class TestCurrentUserFunction
{
    private ApplicationUser user;
    private QueryCreationContext queryCreationContext;
    private final TerminalClause terminalClause =null;

    @Before
    public void setUp() throws Exception
    {
        user = new MockApplicationUser("wgibson", "William Gibson", "wgibson@neuromancer.net");
        queryCreationContext = new QueryCreationContextImpl(user);
    }

    @Test
    public void testDataType() throws Exception
    {
        final CurrentUserFunction currentUserFunction = new CurrentUserFunction();
        assertEquals(JiraDataTypes.USER, currentUserFunction.getDataType());        
    }

    @Test
    public void testValidateTooMayArguments()
    {
        final CurrentUserFunction currentUserFunction = new CurrentUserFunction()
        {
            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }
        };
        final FunctionOperand function = new FunctionOperand("currentUser", ImmutableList.of("badArgument"));
        final MessageSet errorCollection = currentUserFunction.validate(user.getDirectoryUser(), function, terminalClause);
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("Function 'currentUser' expected '0' arguments but received '1'.", errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testGetValuesHappyPath()
    {
        final CurrentUserFunction currentUserFunction = new CurrentUserFunction();
        final FunctionOperand function = new FunctionOperand("currentUser", Collections.<String>emptyList());
        final List<QueryLiteral> values = currentUserFunction.getValues(queryCreationContext, function, terminalClause);
        assertEquals(1, values.size());
        assertEquals(user.getName(), values.get(0).getStringValue());
        assertEquals(function, values.get(0).getSourceOperand());
    }

    @Test
    public void testGetValuesNullContext()
    {
        final CurrentUserFunction currentUserFunction = new CurrentUserFunction();
        final FunctionOperand function = new FunctionOperand("currentUser", Collections.<String>emptyList());
        assertEquals(Collections.<QueryLiteral>emptyList(), currentUserFunction.getValues(null, function, terminalClause));
    }

    @Test
    public void testGetValuesNullUserInContext()
    {
        final CurrentUserFunction currentUserFunction = new CurrentUserFunction();
        final FunctionOperand function = new FunctionOperand("currentUser", Collections.<String>emptyList());
        assertEquals(Collections.<QueryLiteral>emptyList(), currentUserFunction.getValues(new QueryCreationContextImpl((ApplicationUser) null), function, terminalClause));
    }

    @Test
    public void testGetMinimumNumberOfExpectedArguments()
    {
        final CurrentUserFunction currentUserFunction = new CurrentUserFunction();
        assertEquals(0, currentUserFunction.getMinimumNumberOfExpectedArguments());
    }

}
