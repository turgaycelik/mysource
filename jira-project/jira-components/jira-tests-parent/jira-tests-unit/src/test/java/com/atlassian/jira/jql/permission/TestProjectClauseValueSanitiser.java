package com.atlassian.jira.jql.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

/**
 * @since v4.0
 */
public class TestProjectClauseValueSanitiser extends MockControllerTestCase
{
    private User theUser;

    @Before
    public void setUp() throws Exception
    {
        theUser = new MockUser("fred");
    }

    @Test
    public void testSanitiseOperandDoesNotChange() throws Exception
    {
        final SingleValueOperand inputOperand = new SingleValueOperand("HSP");
        final TerminalClause clause = new TerminalClauseImpl("project", Operator.EQUALS, inputOperand);

        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        final NameResolver<Project> projectResolver = mockController.getMock(NameResolver.class);

        final ProjectClauseValueSanitiser.ProjectOperandSanitisingVisitor visitor = new ProjectClauseValueSanitiser.ProjectOperandSanitisingVisitor(jqlOperandResolver, projectResolver, permissionManager, theUser, clause)
        {
            @Override
            public Operand visit(final SingleValueOperand singleValueOperand)
            {
                assertEquals(inputOperand, singleValueOperand);
                return singleValueOperand;
            }
        };

        final ProjectClauseValueSanitiser sanitiser = new ProjectClauseValueSanitiser(permissionManager, jqlOperandResolver, projectResolver)
        {
            @Override
            ProjectOperandSanitisingVisitor createOperandVisitor(final User user, final TerminalClause terminalClause)
            {
                return visitor;
            }
        };

        mockController.replay();

        final Clause result = sanitiser.sanitise(theUser, clause);
        assertSame(result, clause);
        
        mockController.verify();
    }

    @Test
    public void testSanitiseOperandChangesToSingle() throws Exception
    {
        final SingleValueOperand inputOperand = new SingleValueOperand("HSP");
        final SingleValueOperand outputOperand = new SingleValueOperand(10000L);

        final TerminalClause clause = new TerminalClauseImpl("project", Operator.EQUALS, inputOperand);
        final TerminalClause expectedClause = new TerminalClauseImpl("project", Operator.EQUALS, outputOperand);

        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        final NameResolver<Project> projectResolver = mockController.getMock(NameResolver.class);
        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);

        final ProjectClauseValueSanitiser.ProjectOperandSanitisingVisitor visitor = new ProjectClauseValueSanitiser.ProjectOperandSanitisingVisitor(jqlOperandResolver, projectResolver, permissionManager, theUser, clause)
        {
            @Override
            public Operand visit(final SingleValueOperand singleValueOperand)
            {
                assertEquals(inputOperand, singleValueOperand);
                return outputOperand;
            }
        };

        final ProjectClauseValueSanitiser sanitiser = new ProjectClauseValueSanitiser(permissionManager, jqlOperandResolver, projectResolver)
        {
            @Override
            ProjectOperandSanitisingVisitor createOperandVisitor(final User user, final TerminalClause terminalClause)
            {
                return visitor;
            }
        };

        mockController.replay();

        final Clause result = sanitiser.sanitise(theUser, clause);
        assertNotSame(result, clause);
        assertEquals(result, expectedClause);

        mockController.verify();
    }
    
    @Test
    public void testSanitiseOperandChangesToMulti() throws Exception
    {
        final SingleValueOperand inputOperand = new SingleValueOperand("HSP");
        final SingleValueOperand outputOperand = new SingleValueOperand(10000L);

        final TerminalClause clause = new TerminalClauseImpl("project", Operator.IN, new MultiValueOperand(inputOperand));
        final TerminalClause expectedClause = new TerminalClauseImpl("project", Operator.IN, new MultiValueOperand(outputOperand));

        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        final NameResolver<Project> projectResolver = mockController.getMock(NameResolver.class);
        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);

        final ProjectClauseValueSanitiser.ProjectOperandSanitisingVisitor visitor = new ProjectClauseValueSanitiser.ProjectOperandSanitisingVisitor(jqlOperandResolver, projectResolver, permissionManager, theUser, clause)
        {
            @Override
            public Operand visit(final SingleValueOperand singleValueOperand)
            {
                assertEquals(inputOperand, singleValueOperand);
                return outputOperand;
            }
        };

        final ProjectClauseValueSanitiser sanitiser = new ProjectClauseValueSanitiser(permissionManager, jqlOperandResolver, projectResolver)
        {
            @Override
            ProjectOperandSanitisingVisitor createOperandVisitor(final User user, final TerminalClause terminalClause)
            {
                return visitor;
            }
        };

        mockController.replay();

        final Clause result = sanitiser.sanitise(theUser, clause);
        assertNotSame(result, clause);
        assertEquals(result, expectedClause);

        mockController.verify();
    }
}
