package com.atlassian.jira.jql.clause;

import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestDeMorgansVisitor
{
    @Test
    public void testNoNots() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl("blarg", Operator.EQUALS, "blarg");
        TerminalClause terminalClause2 = new TerminalClauseImpl("blargal", Operator.LESS_THAN, "blargal");

        TerminalClause terminalClause3 = new TerminalClauseImpl("garg", Operator.GREATER_THAN_EQUALS, "garg");
        TerminalClause terminalClause4 = new TerminalClauseImpl("gargle", Operator.LIKE, "gargle");

        OrClause orClause1 = new OrClause(terminalClause1, terminalClause2);
        OrClause orClause2 = new OrClause(terminalClause4, terminalClause3);

        AndClause topClause = new AndClause(orClause1, orClause2);

        DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        final Clause result = deMorgansVisitor.visit(topClause);
        assertEquals(topClause, result);
    }

    @Test
    public void testNotSingleEquals() throws Exception
    {
        TerminalClause equalsClause = new TerminalClauseImpl("blarg", Operator.EQUALS, "blarg");
        NotClause notClause = new NotClause(equalsClause);

        TerminalClause expectedClause = new TerminalClauseImpl("blarg", Operator.NOT_EQUALS, "blarg");

        DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        final Clause result = deMorgansVisitor.visit(notClause);
        assertEquals(expectedClause, result);
    }

    @Test
    public void testNotSingleNotEquals() throws Exception
    {
        TerminalClause equalsClause = new TerminalClauseImpl("blarg", Operator.NOT_EQUALS, "blarg");
        NotClause notClause = new NotClause(equalsClause);

        TerminalClause expectedClause = new TerminalClauseImpl("blarg", Operator.EQUALS, "blarg");

        DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        final Clause result = deMorgansVisitor.visit(notClause);
        assertEquals(expectedClause, result);
    }

    @Test
    public void testNotSingleGreaterThan() throws Exception
    {
        TerminalClause equalsClause = new TerminalClauseImpl("blarg", Operator.GREATER_THAN, "blarg");
        NotClause notClause = new NotClause(equalsClause);

        TerminalClause expectedClause = new TerminalClauseImpl("blarg", Operator.LESS_THAN_EQUALS, "blarg");

        DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        final Clause result = deMorgansVisitor.visit(notClause);
        assertEquals(expectedClause, result);
    }

    @Test
    public void testNotSingleGreaterThanEquals() throws Exception
    {
        TerminalClause equalsClause = new TerminalClauseImpl("blarg", Operator.GREATER_THAN_EQUALS, "blarg");
        NotClause notClause = new NotClause(equalsClause);

        TerminalClause expectedClause = new TerminalClauseImpl("blarg", Operator.LESS_THAN, "blarg");

        DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        final Clause result = deMorgansVisitor.visit(notClause);
        assertEquals(expectedClause, result);
    }

    @Test
    public void testNotSingleLessThan() throws Exception
    {
        TerminalClause equalsClause = new TerminalClauseImpl("blarg", Operator.LESS_THAN, "blarg");
        NotClause notClause = new NotClause(equalsClause);

        TerminalClause expectedClause = new TerminalClauseImpl("blarg", Operator.GREATER_THAN_EQUALS, "blarg");

        DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        final Clause result = deMorgansVisitor.visit(notClause);
        assertEquals(expectedClause, result);
    }
    
    @Test
    public void testNotSingleLessThanEquals() throws Exception
    {
        TerminalClause equalsClause = new TerminalClauseImpl("blarg", Operator.LESS_THAN_EQUALS, "blarg");
        NotClause notClause = new NotClause(equalsClause);

        TerminalClause expectedClause = new TerminalClauseImpl("blarg", Operator.GREATER_THAN, "blarg");

        DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        final Clause result = deMorgansVisitor.visit(notClause);
        assertEquals(expectedClause, result);
    }

    @Test
    public void testNotSingleLike() throws Exception
    {
        TerminalClause equalsClause = new TerminalClauseImpl("blarg", Operator.LIKE, "blarg");
        NotClause notClause = new NotClause(equalsClause);

        TerminalClause expectedClause = new TerminalClauseImpl("blarg", Operator.NOT_LIKE, "blarg");

        DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        final Clause result = deMorgansVisitor.visit(notClause);
        assertEquals(expectedClause, result);
    }

    @Test
    public void testNotSingleNotLike() throws Exception
    {
        TerminalClause equalsClause = new TerminalClauseImpl("blarg", Operator.NOT_LIKE, "blarg");
        NotClause notClause = new NotClause(equalsClause);

        TerminalClause expectedClause = new TerminalClauseImpl("blarg", Operator.LIKE, "blarg");

        DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        final Clause result = deMorgansVisitor.visit(notClause);
        assertEquals(expectedClause, result);
    }

    @Test
    public void testNotSingleIs() throws Exception
    {
        TerminalClause equalsClause = new TerminalClauseImpl("blarg", Operator.IS, "blarg");
        NotClause notClause = new NotClause(equalsClause);

        TerminalClause expectedClause = new TerminalClauseImpl("blarg", Operator.IS_NOT, "blarg");

        DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        final Clause result = deMorgansVisitor.visit(notClause);
        assertEquals(expectedClause, result);
    }

    @Test
    public void testNotSingleIn() throws Exception
    {
        TerminalClause equalsClause = new TerminalClauseImpl("blarg", Operator.IN, "blarg");
        NotClause notClause = new NotClause(equalsClause);

        TerminalClause expectedClause = new TerminalClauseImpl("blarg", Operator.NOT_IN, "blarg");

        DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        final Clause result = deMorgansVisitor.visit(notClause);
        assertEquals(expectedClause, result);
    }

    @Test
    public void testNotSingleIsNot() throws Exception
    {
        TerminalClause equalsClause = new TerminalClauseImpl("blarg", Operator.IS_NOT, "blarg");
        NotClause notClause = new NotClause(equalsClause);

        TerminalClause expectedClause = new TerminalClauseImpl("blarg", Operator.IS, "blarg");

        DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        final Clause result = deMorgansVisitor.visit(notClause);
        assertEquals(expectedClause, result);
    }

    @Test
    public void testNotSingleNotIn() throws Exception
    {
        TerminalClause equalsClause = new TerminalClauseImpl("blarg", Operator.NOT_IN, "blarg");
        NotClause notClause = new NotClause(equalsClause);

        TerminalClause expectedClause = new TerminalClauseImpl("blarg", Operator.IN, "blarg");

        DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        final Clause result = deMorgansVisitor.visit(notClause);
        assertEquals(expectedClause, result);
    }

    @Test
    public void testNotAnd() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl("blarg", Operator.EQUALS, "blarg");
        TerminalClause terminalClause2 = new TerminalClauseImpl("blargal", Operator.LESS_THAN, "blargal");
        AndClause andClause = new AndClause(terminalClause1, terminalClause2);
        NotClause notClause = new NotClause(andClause);

        TerminalClause terminalClause3 = new TerminalClauseImpl("blarg", Operator.NOT_EQUALS, "blarg");
        TerminalClause terminalClause4 = new TerminalClauseImpl("blargal", Operator.GREATER_THAN_EQUALS, "blargal");
        OrClause expectedClause = new OrClause(terminalClause3, terminalClause4);

        DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        final Clause result = deMorgansVisitor.visit(notClause);
        assertEquals(expectedClause, result);
    }

    @Test
    public void testNotOr() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl("blarg", Operator.GREATER_THAN_EQUALS, "blarg");
        TerminalClause terminalClause2 = new TerminalClauseImpl("blargal", Operator.NOT_EQUALS, "blargal");
        OrClause orClause = new OrClause(terminalClause1, terminalClause2);
        NotClause notClause = new NotClause(orClause);

        TerminalClause terminalClause3 = new TerminalClauseImpl("blarg", Operator.LESS_THAN, "blarg");
        TerminalClause terminalClause4 = new TerminalClauseImpl("blargal", Operator.EQUALS, "blargal");
        AndClause expectedClause = new AndClause(terminalClause3, terminalClause4);

        DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        final Clause result = deMorgansVisitor.visit(notClause);
        assertEquals(expectedClause, result);
    }

    @Test
    public void testNotAndWithLike() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl("blarg", Operator.LIKE, "blarg");
        TerminalClause terminalClause2 = new TerminalClauseImpl("blargal", Operator.LESS_THAN, "blargal");
        AndClause andClause = new AndClause(terminalClause1, terminalClause2);
        NotClause notClause = new NotClause(andClause);

        TerminalClause notLiked = new TerminalClauseImpl("blarg", Operator.NOT_LIKE, "blarg");
        TerminalClause terminalClause4 = new TerminalClauseImpl("blargal", Operator.GREATER_THAN_EQUALS, "blargal");
        OrClause expectedClause = new OrClause(notLiked, terminalClause4);

        DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        final Clause result = deMorgansVisitor.visit(notClause);
        assertEquals(expectedClause, result);
    }

    @Test
    public void testTwoLevel() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl("blarg", Operator.LIKE, "blarg");
        TerminalClause terminalClause2 = new TerminalClauseImpl("blargal", Operator.LESS_THAN, "blargal");
        NotClause notAndClause = new NotClause(new AndClause(terminalClause1, terminalClause2));

        TerminalClause terminalClause3 = new TerminalClauseImpl("blarg", Operator.GREATER_THAN_EQUALS, "blarg");
        TerminalClause terminalClause4 = new TerminalClauseImpl("blargal", Operator.EQUALS, "blargal");
        NotClause notOrClause = new NotClause(new OrClause(terminalClause3, terminalClause4));
        AndClause topClause = new AndClause(notOrClause, notAndClause);

        TerminalClause notLiked = new TerminalClauseImpl("blarg", Operator.NOT_LIKE, "blarg");
        TerminalClause expected1 = new TerminalClauseImpl("blargal", Operator.GREATER_THAN_EQUALS, "blargal");
        OrClause expOrClause = new OrClause(notLiked, expected1);

        TerminalClause expected3 = new TerminalClauseImpl("blarg", Operator.LESS_THAN, "blarg");
        TerminalClause expected4 = new TerminalClauseImpl("blargal", Operator.NOT_EQUALS, "blargal");
        AndClause expAndClause = new AndClause(expected3, expected4);
        AndClause expectedTopClause = new AndClause(expAndClause, expOrClause);

        DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        final Clause result = deMorgansVisitor.visit(topClause);
        assertEquals(expectedTopClause, result);
    }

    @Test
    public void testTwoNots() throws Exception
    {
        TerminalClause terminalClause =  new TerminalClauseImpl("blargal", Operator.LESS_THAN, "blargal");
        NotClause notClause = new NotClause( new NotClause(terminalClause));

        DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        final Clause result = deMorgansVisitor.visit(notClause);
        assertEquals(terminalClause, result);
    }

    @Test
    public void testThreeNots() throws Exception
    {
        TerminalClause terminalClause =  new TerminalClauseImpl("blargal", Operator.LESS_THAN, "blargal");
        TerminalClause expectedClause =  new TerminalClauseImpl("blargal", Operator.GREATER_THAN_EQUALS, "blargal");
        NotClause notClause = new NotClause(new NotClause(new NotClause(terminalClause)));

        DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        final Clause result = deMorgansVisitor.visit(notClause);
        assertEquals(expectedClause, result);
    }

    @Test
    public void testTwoLevelNot() throws Exception
    {
        TerminalClause terminalClause1 = new TerminalClauseImpl("blarg", Operator.LIKE, "blarg");
        TerminalClause terminalClause2 = new TerminalClauseImpl("blargal", Operator.LESS_THAN, "blargal");
        AndClause andClause = new AndClause(terminalClause1, terminalClause2);

        TerminalClause terminalClause3 = new TerminalClauseImpl("garg", Operator.GREATER_THAN_EQUALS, "garg");
        TerminalClause terminalClause4 = new TerminalClauseImpl("gargle", Operator.EQUALS, "gargle");
        NotClause notOrClause = new NotClause(new OrClause(terminalClause3, terminalClause4));
        NotClause topClause = new NotClause(new AndClause(andClause, notOrClause));

        TerminalClause notLiked = new TerminalClauseImpl("blarg", Operator.NOT_LIKE, "blarg");
        TerminalClause expected1 = new TerminalClauseImpl("blargal", Operator.GREATER_THAN_EQUALS, "blargal");
        OrClause expOrClause1 = new OrClause(notLiked, expected1);

        TerminalClause expected3 = new TerminalClauseImpl("garg", Operator.GREATER_THAN_EQUALS, "garg");
        TerminalClause expected4 = new TerminalClauseImpl("gargle", Operator.EQUALS, "gargle");
        OrClause expOrClause2 = new OrClause(expected3, expected4);
        OrClause expectedTopClause = new OrClause(expOrClause1, expOrClause2);

        DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        final Clause result = deMorgansVisitor.visit(topClause);
        assertEquals(expectedTopClause, result);
    }
}
