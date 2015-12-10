package com.atlassian.jira.jql.builder;

import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * Test for {@link PrecedenceSimpleClauseBuilder}.
 *
 * @since v4.0
 */
public class TestPrecedenceSimpleClauseBuilder
{
    @Test
    public void testStartState() throws Exception
    {
        final PrecedenceSimpleClauseBuilder builder = new PrecedenceSimpleClauseBuilder();
        assertNull(builder.build());
        assertEquals("", builder.toString());
        assertEquals("", builder.copy().toString());

        builder.and();

        assertNull(builder.build());
        assertEquals("", builder.toString());
        assertEquals("", builder.copy().toString());

        builder.or();

        assertNull(builder.build());
        assertEquals("", builder.toString());
        assertEquals("", builder.copy().toString());

        try
        {
            builder.endsub();
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        final Clause expectedClause = new TerminalClauseImpl("test", Operator.EQUALS, "pass");
        SimpleClauseBuilder copy = builder.copy();
        assertEquals(expectedClause, copy.clause(expectedClause).build());

        copy = builder.copy();
        assertNull(copy.build());
    }

    @Test
    public void testNotState() throws Exception
    {
        final PrecedenceSimpleClauseBuilder builder = new PrecedenceSimpleClauseBuilder();
        builder.not();

        assertEquals("NOT", builder.toString());

        try
        {
            builder.and();
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        try
        {
            builder.or();
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        try
        {
            builder.build();
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        try
        {
            builder.endsub();
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        builder.not();
        assertEquals("NOT NOT", builder.toString());

        try
        {
            builder.and();
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        try
        {
            builder.or();
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        try
        {
            builder.build();
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        try
        {
            builder.endsub();
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        assertEquals("NOT NOT NOT", builder.copy().not().toString());

        final Clause expectedClause = new TerminalClauseImpl("test", Operator.EQUALS, "pass");
        final Clause expectedClause2 = new TerminalClauseImpl("test2", Operator.EQUALS, "pass2");
        final SimpleClauseBuilder copy = builder.copy();

        copy.clause(expectedClause);
        assertEquals(new NotClause(new NotClause(expectedClause)), copy.build());

        builder.sub().clause(expectedClause).or().clause(expectedClause2).endsub();
        assertEquals(new NotClause(new NotClause(new OrClause(expectedClause, expectedClause2))), builder.build());

    }

    @Test
    public void testSingleClause() throws Exception
    {
        final Clause expectedClause = new TerminalClauseImpl("test", Operator.EQUALS, "pass");
        final PrecedenceSimpleClauseBuilder builder = new PrecedenceSimpleClauseBuilder();

        assertSame(builder, builder.clause(expectedClause));
        assertEquals(expectedClause, builder.build());
        assertEquals("{test = \"pass\"}", builder.toString());

        try
        {
            builder.not();
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        try
        {
            builder.clause(new TerminalClauseImpl("test2", Operator.IS, "ERROR"));
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        try
        {
            builder.endsub();
            fail("This was expected.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        assertSame(builder, builder.and());
    }

    @Test
    public void testOperatorState() throws Exception
    {
        final Clause termClause1 = new TerminalClauseImpl("test1", Operator.EQUALS, "pass1");
        final Clause termClause2 = new TerminalClauseImpl("test2", Operator.EQUALS, "pass2");
        final Clause termClause3 = new TerminalClauseImpl("test3", Operator.EQUALS, "pass3");

        final PrecedenceSimpleClauseBuilder builder = new PrecedenceSimpleClauseBuilder();

        assertSame(builder, builder.clause(termClause1));

        try
        {
            builder.not();
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        try
        {
            builder.sub();
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        try
        {
            builder.clause(termClause2);
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        try
        {
            builder.endsub();
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        assertEquals(termClause1, builder.build());
        assertEquals(new AndClause(termClause1, termClause2), builder.copy().and().clause(termClause2).build());
        assertEquals(new OrClause(termClause1, termClause2), builder.copy().or().clause(termClause2).build());

        //We now have a sub-clause in the expected state.
        builder.and().sub().clause(termClause2);

        try
        {
            builder.not();
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        try
        {
            builder.sub();
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        try
        {
            builder.clause(termClause2);
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        try
        {
            builder.build();
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        builder.endsub();

        assertEquals(new AndClause(termClause1, termClause2), builder.build());
        assertEquals(new AndClause(termClause1, termClause2, termClause3), builder.and().clause(termClause3).build());
    }

    @Test
    public void testClauseState() throws Exception
    {
        final Clause termClause1 = new TerminalClauseImpl("test1", Operator.EQUALS, "pass1");
        final Clause termClause2 = new TerminalClauseImpl("test2", Operator.NOT_EQUALS, "pass2");
        final Clause termClause3 = new TerminalClauseImpl("test3", Operator.NOT_IN, "pass3");
        final Clause termClause4 = new TerminalClauseImpl("test4", Operator.GREATER_THAN_EQUALS, "pass4");

        final PrecedenceSimpleClauseBuilder builder = new PrecedenceSimpleClauseBuilder();
        builder.clause(termClause1).and();

        try
        {
            builder.and();
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        try
        {
            builder.or();
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        try
        {
            builder.endsub();
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        try
        {
            builder.build();
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }


        assertEquals(new AndClause(termClause1, termClause2), builder.copy().clause(termClause2).build());
        assertEquals(new AndClause(termClause1, new NotClause(termClause2)), builder.copy().not().clause(termClause2).build());
        assertEquals(new AndClause(termClause1, new OrClause(termClause3, termClause4)), builder.sub().clause(termClause3).or().clause(termClause4).endsub().build());
    }

    @Test
    public void testStartGroupState() throws Exception
    {
        final Clause termClause2 = new TerminalClauseImpl("test2", Operator.NOT_EQUALS, "pass2");
        final Clause termClause3 = new TerminalClauseImpl("test3", Operator.NOT_IN, "pass3");
        final Clause termClause4 = new TerminalClauseImpl("test4", Operator.GREATER_THAN_EQUALS, "pass4");

        final SimpleClauseBuilder builder = new PrecedenceSimpleClauseBuilder().sub();

        try
        {
            builder.and();
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        try
        {
            builder.or();
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        try
        {
            builder.endsub();
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        try
        {
            builder.build();
            fail("Expected exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }


        assertEquals(termClause2, builder.copy().clause(termClause2).endsub().build());
        assertEquals(new NotClause(termClause2), builder.copy().not().clause(termClause2).endsub().build());
        assertEquals(new OrClause(termClause3, termClause4), builder.sub().clause(termClause3).or().clause(termClause4).endsub().endsub().build());

    }

    @Test
    public void testOperatorPrecedence() throws Exception
    {
        final Clause termClause1 = new TerminalClauseImpl("test1", Operator.EQUALS, "pass1");
        final Clause termClause2 = new TerminalClauseImpl("test2", Operator.EQUALS, "pass2");
        final Clause termClause3 = new TerminalClauseImpl("test3", Operator.EQUALS, "pass3");
        final Clause termClause4 = new TerminalClauseImpl("test4", Operator.EQUALS, "pass4");

        {
            final PrecedenceSimpleClauseBuilder builder = new PrecedenceSimpleClauseBuilder();
            builder.clause(termClause1).or().clause(termClause2).and().clause(termClause3);

            Clause expectedClause = new OrClause(termClause1, new AndClause(termClause2, termClause3));
            assertEquals(expectedClause, builder.build());

            builder.or().clause(termClause4);
            expectedClause = new OrClause(termClause1, new AndClause(termClause2, termClause3), termClause4);
            assertEquals(expectedClause, builder.build());
        }

        {
            final PrecedenceSimpleClauseBuilder builder = new PrecedenceSimpleClauseBuilder();
            builder.not().clause(termClause1).and().clause(termClause2).or().clause(termClause3);

            Clause expectedClause = new OrClause(new AndClause(new NotClause(termClause1), termClause2), termClause3);
            assertEquals(expectedClause, builder.build());
        }
    }

    @Test
    public void testSubExpression() throws Exception
    {
        final Clause termClause1 = new TerminalClauseImpl("test1", Operator.EQUALS, "pass1");
        final Clause termClause2 = new TerminalClauseImpl("test2", Operator.EQUALS, "pass2");
        final Clause termClause3 = new TerminalClauseImpl("test3", Operator.EQUALS, "pass3");

        final PrecedenceSimpleClauseBuilder rootBuilder = new PrecedenceSimpleClauseBuilder();
        SimpleClauseBuilder subBuilder = rootBuilder.sub();
        assertSame(subBuilder, rootBuilder);

        try
        {
            rootBuilder.and();
            fail("Expected and exception");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        try
        {
            rootBuilder.or();
            fail("Expected and exception");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        try
        {
            rootBuilder.endsub();
            fail("Expected and exception");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        try
        {
            rootBuilder.build();
            fail("Expected and exception");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        assertSame(subBuilder, subBuilder.clause(termClause1).or().clause(termClause2));

        final SimpleClauseBuilder copy = rootBuilder.copy();
        assertSame(rootBuilder, rootBuilder.endsub().and().clause(termClause3));
        assertNotSame(rootBuilder, copy.endsub().and().clause(termClause3));

        try
        {
            rootBuilder.sub();
            fail("Expected and exception");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        final OrClause subClause = new OrClause(termClause1, termClause2);
        Clause expectedClause = new AndClause(subClause, termClause3);
        assertEquals(expectedClause, rootBuilder.build());
        assertEquals(expectedClause, subBuilder.build());

        //try and end a sub-expression when it is incomplete.
        try
        {
            rootBuilder.and().sub().sub().clause(termClause1).and().endsub();
            fail("Expected and exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        //Lets make a copy of the builder and try to build it while a sub-expression remains incomplete.
        subBuilder = rootBuilder.copy().clause(termClause3).endsub();
        try
        {
            subBuilder.build();
            fail("Expected and exception.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }
    }

    @Test
    public void testCopy() throws Exception
    {
        final Clause termClause1 = new TerminalClauseImpl("test1", Operator.EQUALS, "pass1");
        final Clause termClause2 = new TerminalClauseImpl("test2", Operator.EQUALS, "pass2");
        final Clause termClause3 = new TerminalClauseImpl("test3", Operator.EQUALS, "pass3");

        final PrecedenceSimpleClauseBuilder builder = new PrecedenceSimpleClauseBuilder();
        builder.not().clause(termClause1);

        final SimpleClauseBuilder copy = builder.copy();
        copy.and().clause(termClause2);

        final Clause notClause = new NotClause(termClause1);
        assertEquals(notClause, builder.build());
        assertEquals(new AndClause(notClause, termClause2), copy.build());

        assertEquals(new OrClause(notClause, termClause3), builder.or().clause(termClause3).build());
        assertEquals(new AndClause(notClause, termClause2), copy.build());
    }

    @Test
    public void testComplexCondition() throws Exception
    {
        final Clause termClause1 = new TerminalClauseImpl("test1", Operator.EQUALS, "pass1");
        final Clause termClause2 = new TerminalClauseImpl("test2", Operator.EQUALS, "pass2");
        final Clause termClause3 = new TerminalClauseImpl("test3", Operator.EQUALS, "pass3");
        final Clause termClause4 = new TerminalClauseImpl("test4", Operator.EQUALS, "pass4");

        SimpleClauseBuilder builder = new PrecedenceSimpleClauseBuilder();
        builder.clause(termClause1);
        assertEquals(termClause1, builder.build());

        Clause expectedClause = new AndClause(termClause1, termClause2);
        assertEquals(expectedClause, builder.and().copy().sub().clause(termClause2).endsub().build());

        expectedClause = new AndClause(termClause1, new NotClause(new NotClause(new OrClause(termClause2, termClause3, new NotClause(termClause4)))));
        assertEquals(expectedClause, builder.not().not().sub().clause(termClause2).or().clause(termClause3).or().not().clause(termClause4).endsub().build());
    }

    @Test
    public void testToString() throws Exception
    {
        final Clause termClause1 = new TerminalClauseImpl("test1", Operator.EQUALS, "pass1");
        final Clause termClause2 = new TerminalClauseImpl("test2", Operator.EQUALS, "pass2");
        final Clause termClause3 = new TerminalClauseImpl("test3", Operator.EQUALS, "pass3");

        SimpleClauseBuilder builder = new PrecedenceSimpleClauseBuilder();
        assertEqualsNoSpaces("", builder.toString());
        assertEqualsNoSpaces("NOT", builder.not().toString());
        assertEqualsNoSpaces("NOT (", builder.sub().toString());
        assertEqualsNoSpaces("NOT (NOT", builder.not().toString());
        assertEqualsNoSpaces("NOT (NOT (", builder.sub().toString());
        assertEqualsNoSpaces("NOT (NOT ({test1 = \"pass1\"}", builder.clause(termClause1).toString());
        assertEqualsNoSpaces("NOT (NOT ({test1 = \"pass1\"} OR", builder.or().toString());
        assertEqualsNoSpaces("NOT (NOT ({test1 = \"pass1\"} OR NOT", builder.not().toString());
        assertEqualsNoSpaces("NOT (NOT ({test1 = \"pass1\"} OR NOT {test2 = \"pass2\"}", builder.clause(termClause2).toString());
        assertEqualsNoSpaces("NOT (NOT ({test1 = \"pass1\"} OR NOT {test2 = \"pass2\"})", builder.endsub().toString());
        assertEqualsNoSpaces("NOT (NOT ({test1 = \"pass1\"} OR NOT {test2 = \"pass2\"}) AND", builder.and().toString());
        assertEqualsNoSpaces("NOT (NOT ({test1 = \"pass1\"} OR NOT {test2 = \"pass2\"}) AND {test3 = \"pass3\"}", builder.clause(termClause3).toString());
        assertEqualsNoSpaces("NOT (NOT ({test1 = \"pass1\"} OR NOT {test2 = \"pass2\"}) AND {test3 = \"pass3\"})", builder.endsub().toString());

        builder = new PrecedenceSimpleClauseBuilder();
        assertEqualsNoSpaces("", builder.toString());
        assertEqualsNoSpaces("NOT", builder.not().toString());
        assertEqualsNoSpaces("NOT (", builder.sub().toString());
        assertEqualsNoSpaces("NOT ({test1 = \"pass1\"}", builder.clause(termClause1).toString());
        assertEqualsNoSpaces("NOT {test1 = \"pass1\"}", builder.endsub().toString());
    }

    @Test
    public void testAndDefault() throws Exception
    {
        final Clause termClause1 = new TerminalClauseImpl("test1", Operator.EQUALS, "pass1");
        final Clause termClause2 = new TerminalClauseImpl("test2", Operator.EQUALS, "pass2");
        final Clause termClause3 = new TerminalClauseImpl("test3", Operator.EQUALS, "pass3");
        final Clause termClause4 = new TerminalClauseImpl("test4", Operator.GREATER_THAN_EQUALS, "pass4");

        SimpleClauseBuilder builder = new PrecedenceSimpleClauseBuilder();
        builder.clause(termClause1);
        try
        {
            builder.clause(termClause2);
            fail("Expecting an error");
        }
        catch(IllegalStateException e)
        {
            //expected.
        }
        Clause expectedClause = new AndClause(termClause1, termClause2);
        assertEquals(expectedClause, builder.defaultAnd().clause(termClause2).build());

        expectedClause = new OrClause(new AndClause(termClause1, termClause2), termClause3);
        assertEquals(expectedClause, builder.or().clause(termClause3).build());

        try
        {
            builder.defaultNone().clause(termClause4);
            fail("Expecting an error");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        expectedClause = new OrClause(new AndClause(termClause1, termClause2), new AndClause(termClause3, termClause4));
        assertEquals(expectedClause, builder.and().clause(termClause4).build());

        builder = new PrecedenceSimpleClauseBuilder().defaultAnd();
        builder.clause(termClause1).not().clause(termClause2).sub().clause(termClause3).clause(termClause4).endsub();
        expectedClause = new AndClause(termClause1, new NotClause(termClause2), new AndClause(termClause3, termClause4));
        assertEquals(expectedClause, builder.build());
    }

    @Test
    public void testOrDefault() throws Exception
    {
        final Clause termClause1 = new TerminalClauseImpl("test1", Operator.EQUALS, "pass1");
        final Clause termClause2 = new TerminalClauseImpl("test2", Operator.EQUALS, "pass2");
        final Clause termClause3 = new TerminalClauseImpl("test3", Operator.EQUALS, "pass3");
        final Clause termClause4 = new TerminalClauseImpl("test4", Operator.GREATER_THAN_EQUALS, "pass4");

        SimpleClauseBuilder builder = new PrecedenceSimpleClauseBuilder();
        builder.clause(termClause1);
        try
        {
            builder.clause(termClause2);
            fail("Expecting an error");
        }
        catch(IllegalStateException e)
        {
            //expected.
        }

        Clause expectedClause = new OrClause(termClause1, termClause2);
        assertEquals(expectedClause, builder.defaultOr().clause(termClause2).build());

        expectedClause = new OrClause(termClause1, new AndClause(termClause2, termClause3));
        assertEquals(expectedClause, builder.and().clause(termClause3).build());
        try
        {
            builder.defaultNone().clause(termClause4);
            fail("Expecting an error");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        expectedClause = new OrClause(termClause1, new AndClause(termClause2, termClause3), termClause4);
        assertEquals(expectedClause,
                builder.or().clause(termClause4).build());

        builder = new PrecedenceSimpleClauseBuilder().defaultOr();
        builder.clause(termClause1).not().clause(termClause2).sub().clause(termClause3).clause(termClause4).endsub();
        expectedClause = new OrClause(termClause1, new NotClause(termClause2), new OrClause(termClause3, termClause4));
        assertEquals(expectedClause, builder.build());
    }

    @Test
    public void testClear() throws Exception
    {
        final Clause termClause1 = new TerminalClauseImpl("test1", Operator.EQUALS, "pass1");
        final Clause termClause2 = new TerminalClauseImpl("test2", Operator.EQUALS, "pass2");
        final Clause termClause3 = new TerminalClauseImpl("test3", Operator.EQUALS, "pass3");

        SimpleClauseBuilder builder = new PrecedenceSimpleClauseBuilder();
        builder.clause(termClause1).defaultAnd().clause(termClause2).clause(termClause3);

        builder.clear();

        //Has the builder been reset?
        assertNull(builder.build());

        builder.sub().clause(termClause1).and().not().sub();
        builder.clear();

        //Has the builder been cleared.
        assertNull(builder.build());

        builder.clause(termClause1);
        assertEquals(termClause1, builder.build());

        builder.clear();
        //Has the builder been cleared.
        assertNull(builder.build());
    }

    private void assertEqualsNoSpaces(String expected, String actual) throws Exception
    {
        if (!normaliseSpace(expected).equals(normaliseSpace(actual)))
        {
            fail(String.format("Assertion failed.%n Expected '%s'.%n Actual '%s'.%n", expected, actual));
        }
    }

    private static String normaliseSpace(String string)
    {
        return string.replaceAll("\\s+", "");
    }
}
