package com.atlassian.jira.jql.context;

import java.util.Collections;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlIssueSupport;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestIssueParentClauseContextFactory extends MockControllerTestCase
{
    private SubTaskManager subTaskManager;
    private JqlOperandResolver jqlOperandResolver;
    private JqlIssueSupport jqlIssueSupport;
    private User theUser = null;

    @Before
    public void setUp() throws Exception
    {
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlIssueSupport = mockController.getMock(JqlIssueSupport.class);
        subTaskManager = mockController.getMock(SubTaskManager.class);
        EasyMock.expect(subTaskManager.isSubTasksEnabled()).andStubReturn(true);
    }

    @Test
    public void testGetClauseContext() throws Exception
    {
        final MockProject mockProject1 = new MockProject(10L, "test");
        final MockProject mockProject2 = new MockProject(11L, "test");
        final MockIssueType mockIssueType1 = new MockIssueType("10", "test");
        final MockIssueType mockIssueType2 = new MockIssueType("11", "test");
        final SingleValueOperand operand = new SingleValueOperand("blah");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);

        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(createLiteral("key"), createLiteral(10L)).asList());

        MockIssue subIssue1 = new MockIssue();
        subIssue1.setProjectObject(mockProject1);
        subIssue1.setIssueTypeObject(mockIssueType1);

        MockIssue parentIssue1 = new MockIssue();
        parentIssue1.setSubTaskObjects(Collections.singletonList(subIssue1));

        MockIssue subIssue2 = new MockIssue();
        subIssue2.setProjectObject(mockProject2);
        subIssue2.setIssueTypeObject(mockIssueType2);

        MockIssue parentIssue2 = new MockIssue();
        parentIssue2.setSubTaskObjects(Collections.singletonList(subIssue2));

        jqlIssueSupport.getIssue("key", null);
        mockController.setReturnValue(parentIssue1);
        jqlIssueSupport.getIssue(10L, (ApplicationUser) null);
        mockController.setReturnValue(parentIssue2);


        mockController.replay();

        IssueParentClauseContextFactory factory = new IssueParentClauseContextFactory(jqlIssueSupport, jqlOperandResolver, subTaskManager);
        final ClauseContext result = factory.getClauseContext(theUser, clause);
        ClauseContext expectedResult = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("10")),
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(11L), new IssueTypeContextImpl("11"))
            ).asSet());

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextWhenCalculationWasEmpty() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blah");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        
        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        mockController.replay();

        IssueParentClauseContextFactory factory = new IssueParentClauseContextFactory(jqlIssueSupport, jqlOperandResolver, subTaskManager);
        final ClauseContext result = factory.getClauseContext(theUser, clause);
        ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextWhenCalculationWasNull() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blah");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);

        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(null);

        mockController.replay();

        IssueParentClauseContextFactory factory = new IssueParentClauseContextFactory(jqlIssueSupport, jqlOperandResolver, subTaskManager);
        final ClauseContext result = factory.getClauseContext(theUser, clause);
        ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextSingleEmptyLiteral() throws Exception
    {
        final Operand operand = EmptyOperand.EMPTY;
        final TerminalClauseImpl clause = new TerminalClauseImpl("blah", Operator.IS, operand);

        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(new QueryLiteral()).asList());

        mockController.replay();

        IssueParentClauseContextFactory factory = new IssueParentClauseContextFactory(jqlIssueSupport, jqlOperandResolver, subTaskManager);
        final ClauseContext result = factory.getClauseContext(theUser, clause);
        ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextInequality() throws Exception
    {
        ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        mockController.replay();

        IssueParentClauseContextFactory factory = new IssueParentClauseContextFactory(jqlIssueSupport, jqlOperandResolver, subTaskManager);
        final ClauseContext result = factory.getClauseContext(theUser, new TerminalClauseImpl("blah", Operator.NOT_EQUALS, "blah"));

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextBadOperator() throws Exception
    {
        ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        mockController.replay();

        IssueParentClauseContextFactory factory = new IssueParentClauseContextFactory(jqlIssueSupport, jqlOperandResolver, subTaskManager);
        final ClauseContext result = factory.getClauseContext(theUser, new TerminalClauseImpl("blah", Operator.LESS_THAN, "blah"));

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextSubTasksDisabled() throws Exception
    {
        EasyMock.expect(subTaskManager.isSubTasksEnabled()).andReturn(false);

        ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        mockController.replay();

        IssueParentClauseContextFactory factory = new IssueParentClauseContextFactory(jqlIssueSupport, jqlOperandResolver, subTaskManager);
        final ClauseContext result = factory.getClauseContext(theUser, new TerminalClauseImpl("blah", Operator.LESS_THAN, "blah"));

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testIsNegationOperator() throws Exception
    {
        IssueParentClauseContextFactory factory = new IssueParentClauseContextFactory(jqlIssueSupport, jqlOperandResolver, subTaskManager);

        mockController.replay();

        assertTrue(factory.isNegationOperator(Operator.NOT_EQUALS));
        assertTrue(factory.isNegationOperator(Operator.NOT_IN));

        assertFalse(factory.isNegationOperator(Operator.IS_NOT));
        assertFalse(factory.isNegationOperator(Operator.IS));
        assertFalse(factory.isNegationOperator(Operator.EQUALS));
        assertFalse(factory.isNegationOperator(Operator.IN));
    }
}
