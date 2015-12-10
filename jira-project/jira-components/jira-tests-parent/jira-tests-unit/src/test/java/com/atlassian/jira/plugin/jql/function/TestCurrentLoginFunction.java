package com.atlassian.jira.plugin.jql.function;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.bc.security.login.LoginInfo;
import com.atlassian.jira.bc.security.login.LoginService;
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

import org.easymock.EasyMock;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.1
 */
public class TestCurrentLoginFunction
{
    private TerminalClause terminalClause = null;

    @Test
    public void testValidateTooManyArguments() throws Exception
    {
        CurrentLoginFunction loginFunction = new CurrentLoginFunction(null)
        {
            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }
        };
        FunctionOperand function = new FunctionOperand(CurrentLoginFunction.FUNCTION_CURRENT_LOGIN, Arrays.asList("should", "not", "be", "here"));
        final MessageSet messageSet = loginFunction.validate(null, function, terminalClause);
        assertTrue(messageSet.hasAnyMessages());
        assertTrue(messageSet.hasAnyErrors());
        assertFalse(messageSet.hasAnyWarnings());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'currentLogin' expected '0' arguments but received '4'.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testDataType() throws Exception
    {
        CurrentLoginFunction handler = new CurrentLoginFunction(null);
        assertEquals(JiraDataTypes.DATE, handler.getDataType());
    }

    @Test
    public void testGetValuesNullContext() throws Exception
    {
        CurrentLoginFunction loginFunction = new CurrentLoginFunction(null);
        FunctionOperand function = new FunctionOperand(CurrentLoginFunction.FUNCTION_CURRENT_LOGIN, Collections.<String>emptyList());
        final List<QueryLiteral> value = loginFunction.getValues(null, function, terminalClause);
        assertNotNull(value);
        assertEquals(0, value.size());
    }

    @Test
    public void testGetValuesNullUser() throws Exception
    {
        CurrentLoginFunction loginFunction = new CurrentLoginFunction(null);
        FunctionOperand function = new FunctionOperand(CurrentLoginFunction.FUNCTION_CURRENT_LOGIN, Collections.<String>emptyList());
        final QueryCreationContext context = new QueryCreationContextImpl((ApplicationUser) null);
        final List<QueryLiteral> value = loginFunction.getValues(context, function, terminalClause);
        assertNotNull(value);
        assertEquals(0, value.size());
    }

    @Test
    public void testGetValues() throws Exception
    {
        final ApplicationUser user = new MockApplicationUser("bob");

        final LoginService loginService = EasyMock.createMock(LoginService.class);
        final long LOGIN = 2345678900000L;
        final LoginInfo loginInfo = new LoginInfo() {
            public Long getLastLoginTime()
            {
                return LOGIN;
            }

            public Long getPreviousLoginTime()
            {
                return 1234567890000L;
            }

            public Long getLastFailedLoginTime()
            {
                return null;
            }

            public Long getLoginCount()
            {
                return 2L;
            }

            public Long getCurrentFailedLoginCount()
            {
                return null;
            }

            public Long getTotalFailedLoginCount()
            {
                return null;
            }

            public Long getMaxAuthenticationAttemptsAllowed()
            {
                return null;
            }

            public boolean isElevatedSecurityCheckRequired()
            {
                return false;
            }
        };
        EasyMock.expect(loginService.getLoginInfo(user.getName())).andReturn(loginInfo);

        EasyMock.replay(loginService);

        CurrentLoginFunction loginFunction = new CurrentLoginFunction(loginService);
        FunctionOperand function = new FunctionOperand(CurrentLoginFunction.FUNCTION_CURRENT_LOGIN, Collections.<String>emptyList());

        final QueryCreationContext context = new QueryCreationContextImpl(user);
        final List<QueryLiteral> value = loginFunction.getValues(context, function, terminalClause);
        assertNotNull(value);
        assertEquals(1, value.size());
        assertEquals(LOGIN, value.get(0).getLongValue().longValue());

        EasyMock.verify(loginService);
    }

    @Test
    public void testGetMinimumNumberOfExpectedArguments() throws Exception
    {
        CurrentLoginFunction loginFunction = new CurrentLoginFunction(null);
        assertEquals(0, loginFunction.getMinimumNumberOfExpectedArguments());
    }
}
