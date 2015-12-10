package com.atlassian.jira.issue.search;

import java.util.Collections;
import java.util.Map;

import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.ClauseVisitor;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

/**
 * @since v4.2
 */
public class TestClauseRenamingCloningVisitor
{
    private TerminalClauseImpl clause1;
    private TerminalClauseImpl clause2;
    private TerminalClauseImpl clause3;
    private TerminalClauseImpl clause4;
    private OrClause originalTree;

    @Before
    public void setUp() throws Exception
    {
        clause1 = new TerminalClauseImpl("testField1", Operator.EQUALS, new SingleValueOperand("firstValue"));
        clause2 = new TerminalClauseImpl("testField2", Operator.EQUALS, new SingleValueOperand("secondValue"));
        clause3 = new TerminalClauseImpl("testField3", Operator.EQUALS, new SingleValueOperand("thirdValue"));
        clause4 = new TerminalClauseImpl("testField1", Operator.EQUALS, new SingleValueOperand("fourthValue"));

        originalTree = new OrClause(clause4, new OrClause(new AndClause(clause1, clause2), new NotClause(clause3)));
    }

    private static TerminalClause rename(TerminalClause clause)
    {
        return new TerminalClauseImpl("renamed" + clause.getName(), clause.getOperator(), clause.getOperand());
    }

    @Test
    public void testVisitorReplace1() throws Exception
    {
        final TerminalClause renamedClause1 = rename(clause1);
        final TerminalClause renamedClause4 = rename(clause4);
        final Map<String, String> substitutions = MapBuilder.<String, String>newBuilder()
                .add(clause1.getName(), renamedClause1.getName())
                .add(clause4.getName(), renamedClause4.getName())
                .toMap();
        final ClauseVisitor<Clause> renamingVisitor = new ClauseRenamingCloningVisitor(substitutions);

        final Clause modifiedTree = originalTree.accept(renamingVisitor);
        final Clause expectedTree = new OrClause(renamedClause4, new OrClause(new AndClause(renamedClause1, clause2), new NotClause(clause3)));

        assertNotSame(originalTree, modifiedTree);
        assertEquals(expectedTree, modifiedTree);
    }

    @Test
    public void testVisitorReplace2() throws Exception
    {
        final TerminalClause renamedClause1 = rename(clause1);
        final TerminalClause renamedClause3 = rename(clause3);
        final TerminalClause renamedClause4 = rename(clause4);
        final Map<String, String> substitutions = MapBuilder.<String, String>newBuilder()
                .add(clause1.getName(), renamedClause1.getName())
                .add(clause3.getName(), renamedClause3.getName())
                .add(clause4.getName(), renamedClause4.getName())
                .toMap();
        final ClauseVisitor<Clause> renamingVisitor = new ClauseRenamingCloningVisitor(substitutions);

        final Clause modifiedTree = originalTree.accept(renamingVisitor);
        final Clause expectedTree = new OrClause(renamedClause4, new OrClause(new AndClause(renamedClause1, clause2), new NotClause(renamedClause3)));

        assertNotSame(originalTree, modifiedTree);
        assertEquals(expectedTree, modifiedTree);
    }

    @Test
    public void testVisitorReplace1IgnoreCase() throws Exception
    {
        final TerminalClause renamedClause1 = rename(clause1);
        final TerminalClause renamedClause4 = rename(clause4);
        final Map<String, String> substitutions = MapBuilder.<String, String>newBuilder()
                .add(clause1.getName().toUpperCase(), renamedClause1.getName())
                .add(clause4.getName().toUpperCase(), renamedClause4.getName())
                .toMap();
        final ClauseVisitor<Clause> renamingVisitor = new ClauseRenamingCloningVisitor(substitutions);

        final Clause modifiedTree = originalTree.accept(renamingVisitor);
        final Clause expectedTree = new OrClause(renamedClause4, new OrClause(new AndClause(renamedClause1, clause2), new NotClause(clause3)));

        assertNotSame(originalTree, modifiedTree);
        assertEquals(expectedTree, modifiedTree);
    }

    @Test
    public void testVisitorThrowsNullPointerException() throws Exception
    {
        try
        {
            new ClauseRenamingCloningVisitor(null);
            fail();
        }
        catch (Exception e)
        {
            // expected
        }
    }
    
    @Test
    public void testVisitorEmptySubstitutions() throws Exception
    {
        final ClauseVisitor<Clause> renamingVisitor = new ClauseRenamingCloningVisitor(Collections.<String, String>emptyMap());
        final Clause modifiedTree = originalTree.accept(renamingVisitor);

        assertEquals(originalTree, modifiedTree);
    }

}
