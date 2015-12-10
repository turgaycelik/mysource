package com.atlassian.jira.plugin.jql.function;

import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.issue.watchers.WatchedIssuesAccessor;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.plugin.jql.operand.MockJqlFunctionModuleDescriptor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.matchers.QueryLiteralMatchers.emptyIterable;
import static com.atlassian.jira.matchers.QueryLiteralMatchers.literal;
import static com.atlassian.jira.util.MessageSetAssert.assert1ErrorNoWarnings;
import static com.atlassian.jira.util.MessageSetAssert.assertNoMessages;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestWatchedIssuesFunction
{
    private static final ApplicationUser ANONYMOUS = null;
    private static final User FRED = new MockUser("fred");

    @Rule
    public MockitoContainer mockitoContainer = MockitoMocksInContainer.rule(this);

    @Mock WatchedIssuesAccessor watchedIssues;
    QueryCreationContext queryCreationContext;
    TerminalClause terminalClause;

    WatchedIssuesFunction watchedIssuesFunction;

    @Before
    public void setUp() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl(FRED);
        watchedIssuesFunction = new WatchedIssuesFunction(watchedIssues);
    }

    @Test
    public void testDataType() throws Exception
    {
        assertEquals(JiraDataTypes.ISSUE, watchedIssuesFunction.getDataType());
    }

    @Test
    public void testValidateWatchingDisabled() throws Exception
    {
        watchedIssuesFunction.init(MockJqlFunctionModuleDescriptor.create("watchedIssues", true));

        final MessageSet messageSet = watchedIssuesFunction.validate(null, new FunctionOperand("watchedIssues"), terminalClause);
        assert1ErrorNoWarnings(messageSet, "Function 'watchedIssues' cannot be called as watching issues is currently disabled.");
    }

    @Test
    public void testValidateWrongArgs() throws Exception
    {
        when(watchedIssues.isWatchingEnabled()).thenReturn(true);

        watchedIssuesFunction.init(MockJqlFunctionModuleDescriptor.create("watchedIssues", true));

        final MessageSet messageSet = watchedIssuesFunction.validate(new MockUser("bob"), new FunctionOperand(
                "watchedIssues", "badArg"), terminalClause);
        assert1ErrorNoWarnings(messageSet, "Function 'watchedIssues' expected '0' arguments but received '1'.");
    }

    @Test
    public void testValidateAnonymous() throws Exception
    {
        when(watchedIssues.isWatchingEnabled()).thenReturn(true);

        watchedIssuesFunction.init(MockJqlFunctionModuleDescriptor.create("watchedIssues", true));

        final MessageSet messageSet = watchedIssuesFunction.validate(null, new FunctionOperand("watchedIssues"), terminalClause);
        assert1ErrorNoWarnings(messageSet, "Function 'watchedIssues' cannot be called as anonymous user.");
    }

    @Test
    public void testValidateHappyPath() throws Exception
    {
        when(watchedIssues.isWatchingEnabled()).thenReturn(true);

        watchedIssuesFunction.init(MockJqlFunctionModuleDescriptor.create("watchedIssues", true));

        final MessageSet messageSet = watchedIssuesFunction.validate(new MockUser("bob"),
                new FunctionOperand("watchedIssues"), terminalClause);
        assertNoMessages(messageSet);
    }

    @Test
    public void testGetWatchedIssuesDoNotOverrideSecurity() throws Exception
    {
        final Iterable<Long> watches = ImmutableList.of(55L);
        when(watchedIssues.getWatchedIssueIds(FRED, FRED, WatchedIssuesAccessor.Security.RESPECT)).thenReturn(watches);

        final Iterable<Long> result = watchedIssuesFunction.getWatchedIssues(FRED, false);
        assertThat(result, equalTo(watches));
    }

    @Test
    public void testGetWatchedIssuesOverrideSecurity() throws Exception
    {
        final Iterable<Long> watches = ImmutableList.of(55L);
        when(watchedIssues.getWatchedIssueIds(FRED, FRED, WatchedIssuesAccessor.Security.OVERRIDE)).thenReturn(watches);

        final Iterable<Long> result = watchedIssuesFunction.getWatchedIssues(FRED, true);
        assertThat(result, equalTo(watches));
    }

    @Test
    public void testGetValuesHappyPath() throws Exception
    {
        final List<Long> watches = ImmutableList.of(55L);
        when(watchedIssues.getWatchedIssueIds(FRED, FRED, WatchedIssuesAccessor.Security.RESPECT)).thenReturn(watches);

        final FunctionOperand operand = new FunctionOperand("watchedIssues");
        final List<QueryLiteral> result = watchedIssuesFunction.getValues(queryCreationContext, operand, terminalClause);
        assertThat(result, contains(literal(55L)));
    }

    @Test
    public void testGetValuesAnonymous() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl(ANONYMOUS);

        final FunctionOperand operand = new FunctionOperand("watchedIssues");
        final List<QueryLiteral> result = watchedIssuesFunction.getValues(queryCreationContext, operand, terminalClause);
        assertThat(result, emptyIterable());
    }

    @Test
    public void testGetMinimumNumberOfExpectedArguments() throws Exception
    {
        assertEquals(0, watchedIssuesFunction.getMinimumNumberOfExpectedArguments());
    }

}
