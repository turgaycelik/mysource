package com.atlassian.jira.jql.clause;

import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestSimpleEquivalenceComparator
{
    @Test
    public void testSingleLevelNot() throws Exception
    {
        NotClause notClause1 = new NotClause(new TerminalClauseImpl("blah", Operator.EQUALS, "blah"));
        NotClause notClause2 = new NotClause(new TerminalClauseImpl("blah", Operator.EQUALS, "diff"));
        assertEquiv(notClause1, notClause1);            
        assertUnequiv(notClause1, notClause2);
    }

    @Test
    public void testSingleLevelDifferentOperators() throws Exception
    {
        Clause clause1 = new TerminalClauseImpl("blah", Operator.EQUALS, "blah");
        Clause clause2 = new TerminalClauseImpl("blah", Operator.NOT_EQUALS, "blah");
        assertEquiv(clause1, clause1);
        assertUnequiv(clause1, clause2);
    }

    @Test
    public void testSingleTerminals() throws Exception
    {
        assertEquiv(new TerminalClauseImpl("blah", Operator.EQUALS, "blah"), new TerminalClauseImpl("blah", Operator.EQUALS, "blah"));
        assertUnequiv(new TerminalClauseImpl("blah", Operator.EQUALS, "blah"), new TerminalClauseImpl("blah", Operator.LESS_THAN, "blah"));
        assertUnequiv(new TerminalClauseImpl("blah", Operator.EQUALS, "blah"), new TerminalClauseImpl("different", Operator.EQUALS, "blah"));
    }

    @Test
    public void testSingleLevelAnd()
    {
        AndClause andClause = new AndClause(new TerminalClauseImpl("blah", Operator.EQUALS, "blah"), new TerminalClauseImpl("different", Operator.EQUALS, "blah"));
        AndClause differentOrder = new AndClause(new TerminalClauseImpl("different", Operator.EQUALS, "blah"), new TerminalClauseImpl("blah", Operator.EQUALS, "blah"));
        AndClause differentTerminals  = new AndClause(new TerminalClauseImpl("diff", Operator.EQUALS, "blah"), new TerminalClauseImpl("diff", Operator.EQUALS, "blah"));
        AndClause differentAmountOfTerminals  = new AndClause(new TerminalClauseImpl("diff", Operator.EQUALS, "blah"));

        assertEquiv(andClause, andClause);
        assertEquiv(andClause, differentOrder);
        assertUnequiv(andClause, differentTerminals);
        assertUnequiv(andClause, differentAmountOfTerminals);
    }

    @Test
    public void testSingleLevelOr()
    {
        OrClause orClause = new OrClause(new TerminalClauseImpl("blah", Operator.EQUALS, "blah"), new TerminalClauseImpl("different", Operator.EQUALS, "blah"));
        OrClause differentOrder = new OrClause(new TerminalClauseImpl("different", Operator.EQUALS, "blah"), new TerminalClauseImpl("blah", Operator.EQUALS, "blah"));
        OrClause differentTerminals  = new OrClause(new TerminalClauseImpl("diff", Operator.EQUALS, "blah"), new TerminalClauseImpl("diff", Operator.EQUALS, "blah"));
        OrClause differentAmountOfTerminals  = new OrClause(new TerminalClauseImpl("diff", Operator.EQUALS, "blah"));

        assertEquiv(orClause, orClause);
        assertEquiv(orClause, differentOrder);
        assertUnequiv(orClause, differentTerminals);
        assertUnequiv(orClause, differentAmountOfTerminals);
    }

    @Test
    public void testTwoLevels() throws Exception
    {
        OrClause orClause = new OrClause(new NotClause(new TerminalClauseImpl("blah", Operator.EQUALS, "blah")), new TerminalClauseImpl("different", Operator.EQUALS, "blah"));
        AndClause andClause = new AndClause(new TerminalClauseImpl("blah", Operator.EQUALS, "blah"), new TerminalClauseImpl("different", Operator.EQUALS, "blah"));
        OrClause orClauseWithNoNot = new OrClause(new TerminalClauseImpl("blah", Operator.EQUALS, "blah"), new TerminalClauseImpl("different", Operator.EQUALS, "blah"));
        AndClause andClauseWithDifferentTerminal = new AndClause(new TerminalClauseImpl("blah", Operator.GREATER_THAN_EQUALS, "blah"), new TerminalClauseImpl("different", Operator.EQUALS, "blah"));

        AndClause topClause = new AndClause(orClause, andClause);
        AndClause differentOrder = new AndClause(andClause, orClause);
        OrClause differentTopLevel = new OrClause(orClause, andClause);
        AndClause differentSecondLevel = new AndClause(andClause, andClause);
        AndClause differentThirdLevel = new AndClause(orClauseWithNoNot, andClause);
        AndClause differentFourthLevel = new AndClause(orClause, andClauseWithDifferentTerminal);

        assertEquiv(topClause, topClause);
        assertEquiv(topClause, differentOrder);
        assertUnequiv(topClause, differentTopLevel);
        assertUnequiv(topClause, differentSecondLevel);
        assertUnequiv(topClause, differentThirdLevel);
        assertUnequiv(topClause, differentFourthLevel);
    }

    private void assertEquiv(Clause c, Clause c1)
    {
        assertTrue(new SimpleEquivalenceComparator().isEquivalent(c, c1));
    }

    private void assertUnequiv(Clause c, Clause c1)
    {
        assertFalse(new SimpleEquivalenceComparator().isEquivalent(c, c1));
    }
}
