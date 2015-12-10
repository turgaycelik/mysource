package com.atlassian.jira.plugin.jql.function;

import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserIssueHistoryManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link com.atlassian.jira.plugin.jql.function.IssueHistoryFunction}.
 *
 * @since v4.0
 */
public class TestIssueHistoryFunction
{
    private TerminalClause terminalClause = null;
    private User theUser = null;
    private QueryCreationContext queryCreationContext;

    @Before
    public void setUp() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl(theUser);
    }

    @Test
    public void testDataType() throws Exception
    {
        final UserIssueHistoryManager userHistoryManager = createMock(UserIssueHistoryManager.class);
        replay(userHistoryManager);

        final IssueHistoryFunction handler = new MyIssueHistoryFunction(userHistoryManager);
        assertEquals(JiraDataTypes.ISSUE, handler.getDataType());        
    }

    @Test
    public void testValidateArgsHappy() throws Exception
    {
        final UserIssueHistoryManager userHistoryManager = createMock(UserIssueHistoryManager.class);
        replay(userHistoryManager);

        final IssueHistoryFunction handler = new MyIssueHistoryFunction(userHistoryManager);
        final MessageSet messageSet = handler.validate(null, new FunctionOperand(IssueHistoryFunction.FUNCTION_ISSUE_HISTORY), terminalClause);

        assertNotNull(messageSet);
        assertFalse(messageSet.hasAnyErrors());
        assertFalse(messageSet.hasAnyWarnings());

        verify(userHistoryManager);
    }

    @Test
    public void testValidateArgsBad() throws Exception
    {
        final UserIssueHistoryManager userHistoryManager = createMock(UserIssueHistoryManager.class);
        replay(userHistoryManager);

        final IssueHistoryFunction handler = new MyIssueHistoryFunction(userHistoryManager);
        final MessageSet messageSet = handler.validate(null, new FunctionOperand(IssueHistoryFunction.FUNCTION_ISSUE_HISTORY, "1"), terminalClause);

        assertNotNull(messageSet);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(Collections.<String>singleton("jira.jql.function.arg.incorrect.exact{[issueHistory, 0, 1]}"), messageSet.getErrorMessages());
        assertFalse(messageSet.hasAnyWarnings());

        verify(userHistoryManager);
    }

    @Test
    public void testGetValuesHappy() throws Exception
    {
        final UserIssueHistoryManager userHistoryManager = createMock(UserIssueHistoryManager.class);

        expect(userHistoryManager.getFullIssueHistoryWithPermissionChecks((com.atlassian.crowd.embedded.api.User) theUser)).andReturn(Collections.singletonList(new UserHistoryItem(UserHistoryItem.ISSUE, "1")));

        replay(userHistoryManager);

        final IssueHistoryFunction handler = new MyIssueHistoryFunction(userHistoryManager);
        final FunctionOperand operand = new FunctionOperand(IssueHistoryFunction.FUNCTION_ISSUE_HISTORY, "1");
        final List<QueryLiteral> expectedValues = handler.getValues(queryCreationContext, operand, terminalClause);

        assertEquals(Collections.singletonList(createLiteral(1L)), expectedValues);
        assertEquals(operand, expectedValues.get(0).getSourceOperand());

        verify(userHistoryManager);
    }

    @Test
    public void testGetValuesHappyOverrideSecurity() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl((com.atlassian.crowd.embedded.api.User) theUser, true);
        final UserIssueHistoryManager userHistoryManager = createMock(UserIssueHistoryManager.class);

        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks((com.atlassian.crowd.embedded.api.User) theUser)).andReturn(Collections.singletonList(new UserHistoryItem(UserHistoryItem.ISSUE, "1")));

        replay(userHistoryManager);

        final IssueHistoryFunction handler = new MyIssueHistoryFunction(userHistoryManager);
        final FunctionOperand operand = new FunctionOperand(IssueHistoryFunction.FUNCTION_ISSUE_HISTORY, "1");
        final List<QueryLiteral> expectedValues = handler.getValues(queryCreationContext, operand, terminalClause);

        assertEquals(Collections.singletonList(createLiteral(1L)), expectedValues);
        assertEquals(operand, expectedValues.get(0).getSourceOperand());

        verify(userHistoryManager);
    }

    @Test
    public void testGetValuesNoItems() throws Exception
    {
        final UserIssueHistoryManager userHistoryManager = createMock(UserIssueHistoryManager.class);

        expect(userHistoryManager.getFullIssueHistoryWithPermissionChecks((com.atlassian.crowd.embedded.api.User) theUser)).andReturn(Collections.<UserHistoryItem>emptyList());

        replay(userHistoryManager);

        final IssueHistoryFunction handler = new MyIssueHistoryFunction(userHistoryManager);
        final List<QueryLiteral> expectedValues = handler.getValues(queryCreationContext, new FunctionOperand(IssueHistoryFunction.FUNCTION_ISSUE_HISTORY), terminalClause);

        assertEquals(Collections.<QueryLiteral>emptyList(), expectedValues);

        verify(userHistoryManager);
    }

    @Test
    public void testGetValuesMultipleItems() throws Exception
    {
        final UserIssueHistoryManager userHistoryManager = createMock(UserIssueHistoryManager.class);

        final List<UserHistoryItem> list = CollectionBuilder.<UserHistoryItem>newBuilder(new UserHistoryItem(UserHistoryItem.ISSUE, "1"),
                new UserHistoryItem(UserHistoryItem.ISSUE, "2"), new UserHistoryItem(UserHistoryItem.ISSUE, ""),
                new UserHistoryItem(UserHistoryItem.ISSUE, "rkjwek")).asList();

        expect(userHistoryManager.getFullIssueHistoryWithPermissionChecks((com.atlassian.crowd.embedded.api.User) theUser)).andReturn(list);

        replay(userHistoryManager);

        final IssueHistoryFunction handler = new MyIssueHistoryFunction(userHistoryManager);
        final FunctionOperand operand = new FunctionOperand(IssueHistoryFunction.FUNCTION_ISSUE_HISTORY);
        final List<QueryLiteral> expectedValues = handler.getValues(queryCreationContext, operand, terminalClause);

        assertEquals(CollectionBuilder.newBuilder(createLiteral(1L), createLiteral(2L)).asList(), expectedValues);
        assertEquals(operand, expectedValues.get(0).getSourceOperand());
        assertEquals(operand, expectedValues.get(1).getSourceOperand());

        verify(userHistoryManager);
    }

    @Test
    public void testGetMinimumNumberOfExpectedArguments() throws Exception
    {
        final UserIssueHistoryManager userHistoryManager = createMock(UserIssueHistoryManager.class);

        replay(userHistoryManager);

        final IssueHistoryFunction handler = new MyIssueHistoryFunction(userHistoryManager);
        assertEquals(0, handler.getMinimumNumberOfExpectedArguments());
        verify(userHistoryManager);
    }

    private static class MyIssueHistoryFunction extends IssueHistoryFunction
    {
        public MyIssueHistoryFunction(final UserIssueHistoryManager userHistoryManager)
        {
            super(userHistoryManager);
        }

        @Override
        protected I18nHelper getI18n()
        {
            return new NoopI18nHelper();
        }
    }
}
