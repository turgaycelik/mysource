package com.atlassian.jira.issue.search;

import java.util.Collections;

import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.Lists;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * @since v4.0
 */
public class TestClauseReplacingCloningVisitor
{
    @Test
    public void testVisitorReplace1() throws Exception
    {
        final TerminalClauseImpl replacementClause1 = new TerminalClauseImpl("testField1", Operator.EQUALS, new SingleValueOperand("replacementValue"));

        final TerminalClauseImpl clause1 = new TerminalClauseImpl("testField1", Operator.EQUALS, new SingleValueOperand("firstValue"));
        final TerminalClauseImpl clause2 = new TerminalClauseImpl("testField2", Operator.EQUALS, new SingleValueOperand("secondValue"));
        final TerminalClauseImpl clause3 = new TerminalClauseImpl("testField3", Operator.EQUALS, new SingleValueOperand("thirdValue"));
        final TerminalClauseImpl clause4 = new TerminalClauseImpl("testField1", Operator.EQUALS, new SingleValueOperand("fourthValue"));

        OrClause originalTree = new OrClause(clause4, new OrClause(new AndClause(clause1, clause2), new NotClause(clause3)));

        ClauseReplacingCloningVisitor cloningVisitor = new ClauseReplacingCloningVisitor(Lists.<TerminalClause>newArrayList(replacementClause1));

        final Clause modifiedTree = originalTree.accept(cloningVisitor);

        OrClause expectedTree = new OrClause(replacementClause1, new OrClause(new AndClause(replacementClause1, clause2), new NotClause(clause3)));

        assertNotSame(originalTree, modifiedTree);
        assertEquals(expectedTree, modifiedTree);
    }

    @Test
    public void testVisitorReplace2() throws Exception
    {
        final TerminalClauseImpl replacementClause1 = new TerminalClauseImpl("testField1", Operator.EQUALS, new SingleValueOperand("replacementValue"));
        final TerminalClauseImpl replacementClause2 = new TerminalClauseImpl("testField3", Operator.EQUALS, new SingleValueOperand("replacementValue"));

        final TerminalClauseImpl clause1 = new TerminalClauseImpl("testField1", Operator.EQUALS, new SingleValueOperand("firstValue"));
        final TerminalClauseImpl clause2 = new TerminalClauseImpl("testField2", Operator.EQUALS, new SingleValueOperand("secondValue"));
        final TerminalClauseImpl clause3 = new TerminalClauseImpl("testField3", Operator.EQUALS, new SingleValueOperand("thirdValue"));
        final TerminalClauseImpl clause4 = new TerminalClauseImpl("testField1", Operator.EQUALS, new SingleValueOperand("fourthValue"));

        OrClause originalTree = new OrClause(clause4, new OrClause(new AndClause(clause1, clause2), new NotClause(clause3)));

        ClauseReplacingCloningVisitor cloningVisitor = new ClauseReplacingCloningVisitor(Lists.<TerminalClause>newArrayList(replacementClause1, replacementClause2));

        final Clause modifiedTree = originalTree.accept(cloningVisitor);

        OrClause expectedTree = new OrClause(replacementClause1, new OrClause(new AndClause(replacementClause1, clause2), new NotClause(replacementClause2)));

        assertNotSame(originalTree, modifiedTree);
        assertEquals(expectedTree, modifiedTree);
    }

    @Test
    public void testVisitorReplace2IgnoreCase() throws Exception
    {
        final TerminalClauseImpl replacementClause1 = new TerminalClauseImpl("TESTFIELD1", Operator.EQUALS, new SingleValueOperand("replacementValue"));
        final TerminalClauseImpl replacementClause2 = new TerminalClauseImpl("TESTFIELD3", Operator.EQUALS, new SingleValueOperand("replacementValue"));

        final TerminalClauseImpl clause1 = new TerminalClauseImpl("testField1", Operator.EQUALS, new SingleValueOperand("firstValue"));
        final TerminalClauseImpl clause2 = new TerminalClauseImpl("testField2", Operator.EQUALS, new SingleValueOperand("secondValue"));
        final TerminalClauseImpl clause3 = new TerminalClauseImpl("testField3", Operator.EQUALS, new SingleValueOperand("thirdValue"));
        final TerminalClauseImpl clause4 = new TerminalClauseImpl("testField1", Operator.EQUALS, new SingleValueOperand("fourthValue"));

        OrClause originalTree = new OrClause(clause4, new OrClause(new AndClause(clause1, clause2), new NotClause(clause3)));

        ClauseReplacingCloningVisitor cloningVisitor = new ClauseReplacingCloningVisitor(Lists.<TerminalClause>newArrayList(replacementClause1, replacementClause2));

        final Clause modifiedTree = originalTree.accept(cloningVisitor);

        OrClause expectedTree = new OrClause(replacementClause1, new OrClause(new AndClause(replacementClause1, clause2), new NotClause(replacementClause2)));

        assertNotSame(originalTree, modifiedTree);
        assertEquals(expectedTree, modifiedTree);
    }

    @Test
    public void testVisitorNullReplacements() throws Exception
    {
        final TerminalClauseImpl clause1 = new TerminalClauseImpl("testField1", Operator.EQUALS, new SingleValueOperand("firstValue"));
        final TerminalClauseImpl clause2 = new TerminalClauseImpl("testField2", Operator.EQUALS, new SingleValueOperand("secondValue"));
        final TerminalClauseImpl clause3 = new TerminalClauseImpl("testField3", Operator.EQUALS, new SingleValueOperand("thirdValue"));
        final TerminalClauseImpl clause4 = new TerminalClauseImpl("testField1", Operator.EQUALS, new SingleValueOperand("fourthValue"));

        OrClause originalTree = new OrClause(clause4, new OrClause(new AndClause(clause1, clause2), new NotClause(clause3)));

        ClauseReplacingCloningVisitor cloningVisitor = new ClauseReplacingCloningVisitor(null);

        final Clause modifiedTree = originalTree.accept(cloningVisitor);

        assertEquals(originalTree, modifiedTree);
    }

    @Test
    public void testVisitorEmptyReplacements() throws Exception
    {
        final TerminalClauseImpl clause1 = new TerminalClauseImpl("testField1", Operator.EQUALS, new SingleValueOperand("firstValue"));
        final TerminalClauseImpl clause2 = new TerminalClauseImpl("testField2", Operator.EQUALS, new SingleValueOperand("secondValue"));
        final TerminalClauseImpl clause3 = new TerminalClauseImpl("testField3", Operator.EQUALS, new SingleValueOperand("thirdValue"));
        final TerminalClauseImpl clause4 = new TerminalClauseImpl("testField1", Operator.EQUALS, new SingleValueOperand("fourthValue"));

        OrClause originalTree = new OrClause(clause4, new OrClause(new AndClause(clause1, clause2), new NotClause(clause3)));

        ClauseReplacingCloningVisitor cloningVisitor = new ClauseReplacingCloningVisitor(Collections.<TerminalClause>emptyList());

        final Clause modifiedTree = originalTree.accept(cloningVisitor);

        assertEquals(originalTree, modifiedTree);
    }
    
}
