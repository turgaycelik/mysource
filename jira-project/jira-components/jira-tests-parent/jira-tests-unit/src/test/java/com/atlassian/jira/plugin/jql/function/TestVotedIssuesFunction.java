package com.atlassian.jira.plugin.jql.function;

import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.issue.vote.VotedIssuesAccessor;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.plugin.jql.operand.MockJqlFunctionModuleDescriptor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestVotedIssuesFunction extends MockControllerTestCase
{
    private User theUser;
    private QueryCreationContext queryCreationContext;
    private TerminalClause terminalClause = null;

    @Before
    public void setUp() throws Exception
    {
        theUser = new MockUser("fred");
        queryCreationContext = new QueryCreationContextImpl(theUser);
    }

    @Test
    public void testDataType() throws Exception
    {
        final VotedIssuesFunction votedIssuesFunction = mockController.instantiate(VotedIssuesFunction.class);
        assertEquals(JiraDataTypes.ISSUE, votedIssuesFunction.getDataType());
    }

    @Test
    public void testValidateVotingDisabled() throws Exception
    {
        final VotedIssuesAccessor votedIssuesAccessor = mockController.getMock(VotedIssuesAccessor.class);
        votedIssuesAccessor.isVotingEnabled();
        mockController.setReturnValue(false);

        final VotedIssuesFunction votedIssuesFunction = mockController.instantiate(VotedIssuesFunction.class);
        votedIssuesFunction.init(MockJqlFunctionModuleDescriptor.create("votedIssues", true));

        final MessageSet messageSet = votedIssuesFunction.validate(null, new FunctionOperand("votedIssues"), terminalClause);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'votedIssues' cannot be called as voting on issues is currently disabled.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateWrongArgs() throws Exception
    {
        final VotedIssuesAccessor votedIssuesAccessor = mockController.getMock(VotedIssuesAccessor.class);
        votedIssuesAccessor.isVotingEnabled();
        mockController.setReturnValue(true);

        final VotedIssuesFunction votedIssuesFunction = mockController.instantiate(VotedIssuesFunction.class);
        votedIssuesFunction.init(MockJqlFunctionModuleDescriptor.create("votedIssues", true));

        final MessageSet messageSet = votedIssuesFunction.validate(new MockUser("bob"), new FunctionOperand("votedIssues", "badArg"), terminalClause);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'votedIssues' expected '0' arguments but received '1'.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateHappyPath() throws Exception
    {
        final VotedIssuesAccessor votedIssuesAccessor = mockController.getMock(VotedIssuesAccessor.class);
        votedIssuesAccessor.isVotingEnabled();
        mockController.setReturnValue(true);

        final VotedIssuesFunction votedIssuesFunction = mockController.instantiate(VotedIssuesFunction.class);
        votedIssuesFunction.init(MockJqlFunctionModuleDescriptor.create("votedIssues", true));

        final MessageSet messageSet = votedIssuesFunction.validate(new MockUser("bob"), new FunctionOperand("votedIssues"), terminalClause);
        assertFalse(messageSet.hasAnyErrors());
    }

    @Test
    public void testValidateAnonymous()
    {
        final VotedIssuesAccessor applicationProperties = mockController.getMock(VotedIssuesAccessor.class);
        applicationProperties.isVotingEnabled();
        mockController.setReturnValue(true);

        final VotedIssuesFunction votedIssuesFunction = mockController.instantiate(VotedIssuesFunction.class);
        votedIssuesFunction.init(MockJqlFunctionModuleDescriptor.create("votedIssues", true));

        final MessageSet messageSet = votedIssuesFunction.validate(null, new FunctionOperand("votedIssues"), terminalClause);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'votedIssues' cannot be called as anonymous user.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testGetVotedIssuesDontOverrideSecurity() throws Exception
    {
        final Iterable<Long> voted = Collections.singletonList(55L);

        final VotedIssuesAccessor votedIssuesAccessor = mockController.getMock(VotedIssuesAccessor.class);
        votedIssuesAccessor.getVotedIssueIds(theUser, theUser, VotedIssuesAccessor.Security.RESPECT);
        mockController.setReturnValue(voted);

        final VotedIssuesFunction votedIssuesFunction = mockController.instantiate(VotedIssuesFunction.class);

        final Iterable<Long> list = votedIssuesFunction.getVotedIssues(theUser, false);
        assertEquals(voted, list);
    }

    @Test
    public void testGetVotedIssuesOverrideSecurity() throws Exception
    {
        final Iterable<Long> voted = Collections.singletonList(55L);

        final VotedIssuesAccessor votedIssuesAccessor = mockController.getMock(VotedIssuesAccessor.class);
        votedIssuesAccessor.getVotedIssueIds(theUser, theUser, VotedIssuesAccessor.Security.OVERRIDE);
        mockController.setReturnValue(voted);

        final VotedIssuesFunction votedIssuesFunction = mockController.instantiate(VotedIssuesFunction.class);

        final Iterable<Long> list = votedIssuesFunction.getVotedIssues(theUser, true);
        assertEquals(voted, list);
    }

    @Test
    public void testGetValuesHappyPath() throws Exception
    {
        final List<Long> voted = Collections.singletonList(55L);

        final VotedIssuesAccessor votedIssuesAccessor = mockController.getMock(VotedIssuesAccessor.class);
        votedIssuesAccessor.getVotedIssueIds(theUser, theUser, VotedIssuesAccessor.Security.RESPECT);
        mockController.setReturnValue(voted);

        final VotedIssuesFunction votedIssuesFunction = mockController.instantiate(VotedIssuesFunction.class);

        final List<QueryLiteral> list = votedIssuesFunction.getValues(queryCreationContext, new FunctionOperand("votedIssues"), terminalClause);
        assertEquals(1, list.size());
        assertEquals(new Long(55), list.get(0).getLongValue());
    }

    @Test
    public void testGetValuesAnonymous()
    {
        final VotedIssuesFunction votedIssuesFunction = mockController.instantiate(VotedIssuesFunction.class);

        final List<QueryLiteral> list = votedIssuesFunction.getValues(new QueryCreationContextImpl((ApplicationUser) null), new FunctionOperand("votedIssues"), terminalClause);
        assertTrue(list.isEmpty());
    }

    @Test
    public void testGetMinimumNumberOfExpectedArguments() throws Exception
    {
        final VotedIssuesFunction votedIssuesFunction = mockController.instantiate(VotedIssuesFunction.class);
        assertEquals(0, votedIssuesFunction.getMinimumNumberOfExpectedArguments());
    }

}
