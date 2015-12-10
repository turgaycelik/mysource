package com.atlassian.jira.jql.builder;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.Operands;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * Test for {@link com.atlassian.jira.jql.builder.DefaultJqlClauseBuilder}.
 *
 * @since v4.0
 */
public class TestDefaultConditionBuilder
{
    @Test
    public void testConstructorBad() throws Exception
    {
        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        replay(jqlClauseBuilder);

        try
        {
            new DefaultConditionBuilder("blah", null);
            fail("Expecting an exception.");
        }
        catch (IllegalArgumentException expected)
        {
            //expected
        }

        try
        {
            new DefaultConditionBuilder(null, jqlClauseBuilder);
            fail("Expecting an exception.");
        }
        catch (IllegalArgumentException expected)
        {
            //expected
        }

        verify(jqlClauseBuilder);
    }

    @Test
    public void testEqString() throws Exception
    {
        final String name = "fieldName";
        final String value = "mine";

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addStringCondition(name, Operator.EQUALS, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.eq(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testEqOperand() throws Exception
    {
        final String name = "fieldName";
        final Operand value = new SingleValueOperand("4372738");

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addCondition(name, Operator.EQUALS, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.eq(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testEqLong() throws Exception
    {
        final String name = "fieldName";
        final long value = 1;

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addNumberCondition(name, Operator.EQUALS, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.eq(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testEqBuilder() throws Exception
    {
        final String name = "fieldName";
        final long value = 1;

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addNumberCondition(name, Operator.EQUALS, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        final ValueBuilder valueBuilder = builder.eq();
        assertNotNull(valueBuilder);
        assertSame(jqlClauseBuilder, valueBuilder.number(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testEqEmpty() throws Exception
    {
        final String name = "fieldName";
        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addCondition(name, Operator.EQUALS, EmptyOperand.EMPTY)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.eqEmpty());

        verify(jqlClauseBuilder);
    }

    @Test
    public void testEqDate() throws Exception
    {
        assertDate("testEqDate", Operator.EQUALS, new TestCallable<Date, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final Date argument)
            {
                return builder.eq(argument);
            }
        });        
    }

    @Test
    public void testEqFunction() throws Exception
    {
        assertTestFunction("fieldName", Operator.EQUALS, "myFunc", new TestCallable<FunctionArgument<Void>, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<Void> argument)
            {
                return builder.eqFunc(argument.funcName);
            }
        });
    }

    @Test
    public void testEqFunctionVarArgs() throws Exception
    {
        assertFunctionVarArgs("clauseName", Operator.EQUALS, "funcName", new TestCallable<FunctionArgument<String[]>, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<String[]> argument)
            {
                return builder.eqFunc(argument.getFuncName(), argument.getArgument());
            }
        });
    }

    @Test
    public void testEqFunctionCollection() throws Exception
    {
        class EqFunctionCallable implements TestCallable<FunctionArgument<Collection<String>>, JqlClauseBuilder>
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<Collection<String>> argument)
            {
                return builder.eqFunc(argument.getFuncName(), argument.getArgument());
            }
        }

        assertFunctionCollection("testOne", Operator.EQUALS, "funcName", new EqFunctionCallable(), Collections.<String>emptyList());
        Collection<String> args = CollectionBuilder.newBuilder("arg1", "arg2", "...", "argN").asCollection();
        assertFunctionCollection("testTwo", Operator.EQUALS, "funcName", new EqFunctionCallable(), args);
    }

    @Test
    public void testNotEqString() throws Exception
    {
        final String name = "fieldName";
        final String value = "mine";

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addStringCondition(name, Operator.NOT_EQUALS, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.notEq(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testNotEqOperand() throws Exception
    {
        final String name = "fieldName";
        final Operand value = new SingleValueOperand("4372738");

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addCondition(name, Operator.NOT_EQUALS, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.notEq(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testNotEqLong() throws Exception
    {
        final String name = "fieldName";
        final long value = 1;

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addNumberCondition(name, Operator.NOT_EQUALS, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.notEq(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testNotEqBuilder() throws Exception
    {
        final String name = "fieldName";
        final long value = 1;

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addNumberCondition(name, Operator.NOT_EQUALS, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        final ValueBuilder valueBuilder = builder.notEq();
        assertNotNull(valueBuilder);
        assertSame(jqlClauseBuilder, valueBuilder.number(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testNotEqEmpty() throws Exception
    {
        final String name = "fieldName";
        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addCondition(name, Operator.NOT_EQUALS, EmptyOperand.EMPTY)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.notEqEmpty());

        verify(jqlClauseBuilder);
    }

    @Test
    public void testNotEqDate() throws Exception
    {
        assertDate("testNotEqDate", Operator.NOT_EQUALS, new TestCallable<Date, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final Date argument)
            {
                return builder.notEq(argument);
            }
        });
    }
    
    @Test
    public void testNotEqFunction() throws Exception
    {
        assertTestFunction("fieldName", Operator.NOT_EQUALS, "testNotEqFunction", new TestCallable<FunctionArgument<Void>, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<Void> argument)
            {
                return builder.notEqFunc(argument.getFuncName());
            }
        });
    }

    @Test
    public void testNotEqFunctionVarArgs() throws Exception
    {
        assertFunctionVarArgs("clauseName", Operator.NOT_EQUALS, "testNotEqFunctionVarArgs", new TestCallable<FunctionArgument<String[]>, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<String[]> argument)
            {
                return builder.notEqFunc(argument.getFuncName(), argument.getArgument());
            }
        });
    }

    @Test
    public void testNotEqFunctionCollection() throws Exception
    {
        class NotEqFunctionCallable implements TestCallable<FunctionArgument<Collection<String>>, JqlClauseBuilder>
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<Collection<String>> argument)
            {
                return builder.notEqFunc(argument.getFuncName(), argument.getArgument());
            }
        }

        assertFunctionCollection("testOne", Operator.NOT_EQUALS, "ertter", new NotEqFunctionCallable(), Collections.<String>emptyList());
        Collection<String> args = CollectionBuilder.newBuilder("arg1", "arg2", "...", "argN").asCollection();
        assertFunctionCollection("adsa", Operator.NOT_EQUALS, "ertter", new NotEqFunctionCallable(), args);
    }

    @Test
    public void testLikeString() throws Exception
    {
        final String name = "fieldName";
        final String value = "mine";

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addStringCondition(name, Operator.LIKE, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.like(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testLikeOperand() throws Exception
    {
        final String name = "fieldName";
        final Operand value = new SingleValueOperand("4372738");

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addCondition(name, Operator.LIKE, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.like(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testLikeLong() throws Exception
    {
        final String name = "fieldName";
        final long value = 1;

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addNumberCondition(name, Operator.LIKE, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.like(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testLikeBuilder() throws Exception
    {
        final String name = "fieldName";
        final long value = 1;

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addNumberCondition(name, Operator.LIKE, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        final ValueBuilder valueBuilder = builder.like();
        assertNotNull(valueBuilder);
        assertSame(jqlClauseBuilder, valueBuilder.number(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testLikeDate() throws Exception
    {
        assertDate("testNotNotNotNotLikeDate", Operator.LIKE, new TestCallable<Date, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final Date argument)
            {
                return builder.like(argument);
            }
        });
    }

    @Test
    public void testLikeFunction() throws Exception
    {
        assertTestFunction("aaaa", Operator.LIKE, "testLikeFunction", new TestCallable<FunctionArgument<Void>, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<Void> argument)
            {
                return builder.likeFunc(argument.getFuncName());
            }
        });
    }

    @Test
    public void testLikeFunctionVarArgs() throws Exception
    {
        assertFunctionVarArgs("bbbb", Operator.LIKE, "testLikeFunctionVarArgs", new TestCallable<FunctionArgument<String[]>, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<String[]> argument)
            {
                return builder.likeFunc(argument.getFuncName(), argument.getArgument());
            }
        });
    }

    @Test
    public void testLikeFunctionCollection() throws Exception
    {
        class LikeFunctionCallable implements TestCallable<FunctionArgument<Collection<String>>, JqlClauseBuilder>
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<Collection<String>> argument)
            {
                return builder.likeFunc(argument.getFuncName(), argument.getArgument());
            }
        }

        assertFunctionCollection("testOne", Operator.LIKE, "ertter", new LikeFunctionCallable(), Collections.<String>emptyList());
        Collection<String> args = CollectionBuilder.newBuilder("arg1", "arg2", "...", "argN").asCollection();
        assertFunctionCollection("adsa", Operator.LIKE, "ertter", new LikeFunctionCallable(), args);
    }

    @Test
    public void testNotlikeOperand() throws Exception
    {
        final String name = "fieldName";
        final Operand value = new SingleValueOperand("4372738");

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addCondition(name, Operator.NOT_LIKE, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.notLike(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testNotlikeLong() throws Exception
    {
        final String name = "fieldName";
        final long value = 1;

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addNumberCondition(name, Operator.NOT_LIKE, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.notLike(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testNotlikeBuilder() throws Exception
    {
        final String name = "fieldName";
        final long value = 1;

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addNumberCondition(name, Operator.NOT_LIKE, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        final ValueBuilder valueBuilder = builder.notLike();
        assertNotNull(valueBuilder);
        assertSame(jqlClauseBuilder, valueBuilder.number(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testNotLikeDate() throws Exception
    {
        assertDate("testNotLikeDate", Operator.NOT_LIKE, new TestCallable<Date, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final Date argument)
            {
                return builder.notLike(argument);
            }
        });
    }

    @Test
    public void testNotLikeFunction() throws Exception
    {
        assertTestFunction("ewtkjwijrwke", Operator.NOT_LIKE, "wqrkw;lkrl;ew",  new TestCallable<FunctionArgument<Void>, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<Void> argument)
            {
                return builder.notLikeFunc(argument.getFuncName());
            }
        });
    }

    @Test
    public void testNotLikeFunctionVarArgs() throws Exception
    {
        assertFunctionVarArgs("qewqke;lqkewq", Operator.NOT_LIKE, "af;alk;elkq;elwqdsajfaghekjrtiuywqroi", new TestCallable<FunctionArgument<String[]>, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<String[]> argument)
            {
                return builder.notLikeFunc(argument.getFuncName(), argument.getArgument());
            }
        });
    }

    @Test
    public void testNotLikeFunctionCollection() throws Exception
    {
        class NotLikeFunctionCallable implements TestCallable<FunctionArgument<Collection<String>>, JqlClauseBuilder>
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<Collection<String>> argument)
            {
                return builder.notLikeFunc(argument.getFuncName(), argument.getArgument());
            }
        }

        assertFunctionCollection("qweqwrewr", Operator.NOT_LIKE, "ewqewqeq", new NotLikeFunctionCallable(), Collections.<String>emptyList());
        Collection<String> args = CollectionBuilder.newBuilder("arg1", "arg2", "...", "argN").asCollection();
        assertFunctionCollection("qewqewqewqe", Operator.NOT_LIKE, "ertter", new NotLikeFunctionCallable(), args);
    }

    @Test
    public void testIsEmpty() throws Exception
    {
        final String name = "fieldName";

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addCondition(name, Operator.IS, EmptyOperand.EMPTY)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.isEmpty());

        verify(jqlClauseBuilder);
    }

    @Test
    public void testIsBuilder() throws Exception
    {
        final String name = "fieldName";

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addCondition(name, Operator.IS, EmptyOperand.EMPTY)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        final ValueBuilder valueBuilder = builder.is();
        assertNotNull(valueBuilder);
        assertSame(jqlClauseBuilder, valueBuilder.empty());

        verify(jqlClauseBuilder);
    }

    @Test
    public void testIsNotEmpty() throws Exception
    {
        final String name = "fieldName";

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addCondition(name, Operator.IS_NOT, EmptyOperand.EMPTY)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.isNotEmpty());

        verify(jqlClauseBuilder);
    }

    @Test
    public void testIsNotBuilder() throws Exception
    {
        final String name = "fieldName";

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addCondition(name, Operator.IS_NOT, EmptyOperand.EMPTY)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        final ValueBuilder valueBuilder = builder.isNot();
        assertNotNull(valueBuilder);
        assertSame(jqlClauseBuilder, valueBuilder.empty());

        verify(jqlClauseBuilder);
    }

    @Test
    public void testLtString() throws Exception
    {
        final String name = "fieldName";
        final String value = "mine";

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addStringCondition(name, Operator.LESS_THAN, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.lt(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testLtOperand() throws Exception
    {
        final String name = "fieldName";
        final Operand value = new SingleValueOperand("4372738");

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addCondition(name, Operator.LESS_THAN, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.lt(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testLtLong() throws Exception
    {
        final String name = "fieldName";
        final long value = 1;

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addNumberCondition(name, Operator.LESS_THAN, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.lt(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testLtBuilder() throws Exception
    {
        final String name = "fieldName";
        final long value = 1;

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addNumberCondition(name, Operator.LESS_THAN, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        final ValueBuilder valueBuilder = builder.lt();
        assertNotNull(valueBuilder);
        assertSame(jqlClauseBuilder, valueBuilder.number(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testLtDate() throws Exception
    {
        assertDate("testLtDate", Operator.LESS_THAN, new TestCallable<Date, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final Date argument)
            {
                return builder.lt(argument);
            }
        });
    }

    @Test
    public void testLtFunction() throws Exception
    {
        assertTestFunction("asdfgh", Operator.LESS_THAN, "23j3j",  new TestCallable<FunctionArgument<Void>, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<Void> argument)
            {
                return builder.ltFunc(argument.getFuncName());
            }
        });
    }

    @Test
    public void testLtFunctionVarArgs() throws Exception
    {
        assertFunctionVarArgs("zxcvbn", Operator.LESS_THAN, "fndsfsjfkjlsd", new TestCallable<FunctionArgument<String[]>, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<String[]> argument)
            {
                return builder.ltFunc(argument.getFuncName(), argument.getArgument());
            }
        });
    }

    @Test
    public void testLtFunctionCollection() throws Exception
    {
        class LtFunctionCallable implements TestCallable<FunctionArgument<Collection<String>>, JqlClauseBuilder>
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<Collection<String>> argument)
            {
                return builder.ltFunc(argument.getFuncName(), argument.getArgument());
            }
        }

        assertFunctionCollection("euroiwuer", Operator.LESS_THAN, "wwww", new LtFunctionCallable(), Collections.<String>emptyList());
        Collection<String> args = CollectionBuilder.newBuilder("arg1", "arg2", "...", "argN").asCollection();
        assertFunctionCollection("aaaa", Operator.LESS_THAN, "bbb", new LtFunctionCallable(), args);
    }

    @Test
    public void testLtEqOperand() throws Exception
    {
        final String name = "fieldName";
        final Operand value = new SingleValueOperand("4372738");

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addCondition(name, Operator.LESS_THAN_EQUALS, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.ltEq(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testLtEqLong() throws Exception
    {
        final String name = "fieldName";
        final long value = 1;

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addNumberCondition(name, Operator.LESS_THAN_EQUALS, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.ltEq(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testLtEqBuilder() throws Exception
    {
        final String name = "fieldName";
        final long value = 1;

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addNumberCondition(name, Operator.LESS_THAN_EQUALS, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        final ValueBuilder valueBuilder = builder.ltEq();
        assertNotNull(valueBuilder);
        assertSame(jqlClauseBuilder, valueBuilder.number(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testLtEqDate() throws Exception
    {
        assertDate("testLtEqDate", Operator.LESS_THAN_EQUALS, new TestCallable<Date, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final Date argument)
            {
                return builder.ltEq(argument);
            }
        });
    }

    @Test
    public void testLtEqFunction() throws Exception
    {
        assertTestFunction("aaaa", Operator.LESS_THAN_EQUALS, "aaaaaaaa",  new TestCallable<FunctionArgument<Void>, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<Void> argument)
            {
                return builder.ltEqFunc(argument.getFuncName());
            }
        });
    }

    @Test
    public void testLtEqFunctionVarArgs() throws Exception
    {
        assertFunctionVarArgs("bbbbbb", Operator.LESS_THAN_EQUALS, "bbbbbbb", new TestCallable<FunctionArgument<String[]>, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<String[]> argument)
            {
                return builder.ltEqFunc(argument.getFuncName(), argument.getArgument());
            }
        });
    }

    @Test
    public void testLtEqFunctionCollection() throws Exception
    {
        class LtEqFunctionCallable implements TestCallable<FunctionArgument<Collection<String>>, JqlClauseBuilder>
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<Collection<String>> argument)
            {
                return builder.ltEqFunc(argument.getFuncName(), argument.getArgument());
            }
        }

        assertFunctionCollection("ccccc", Operator.LESS_THAN_EQUALS, "ccccc", new LtEqFunctionCallable(), Collections.<String>emptyList());
        Collection<String> args = CollectionBuilder.newBuilder("arg1", "arg2", "...", "argN").asCollection();
        assertFunctionCollection("dddddd", Operator.LESS_THAN_EQUALS, "dddd", new LtEqFunctionCallable(), args);
    }


    @Test
    public void testGtString() throws Exception
    {
        final String name = "fieldName";
        final String value = "mine";

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addStringCondition(name, Operator.GREATER_THAN, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.gt(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testGtOperand() throws Exception
    {
        final String name = "fieldName";
        final Operand value = new SingleValueOperand("4372738");

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addCondition(name, Operator.GREATER_THAN, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.gt(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testGtLong() throws Exception
    {
        final String name = "fieldName";
        final long value = 1;

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addNumberCondition(name, Operator.GREATER_THAN, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.gt(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testGtBuilder() throws Exception
    {
        final String name = "fieldName";
        final long value = 1;

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addNumberCondition(name, Operator.GREATER_THAN, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        final ValueBuilder valueBuilder = builder.gt();
        assertNotNull(valueBuilder);
        assertSame(jqlClauseBuilder, valueBuilder.number(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testGtDate() throws Exception
    {
        assertDate("testGtDate", Operator.GREATER_THAN, new TestCallable<Date, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final Date argument)
            {
                return builder.gt(argument);
            }
        });
    }

    @Test
    public void testGtFunction() throws Exception
    {
        assertTestFunction("qwerty", Operator.GREATER_THAN, "qwerty", new TestCallable<FunctionArgument<Void>, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<Void> argument)
            {
                return builder.gtFunc(argument.getFuncName());
            }
        });
    }

    @Test
    public void testGtFunctionVarArgs() throws Exception
    {
        assertFunctionVarArgs("zxcvbnm", Operator.GREATER_THAN, "zxcvbnm", new TestCallable<FunctionArgument<String[]>, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<String[]> argument)
            {
                return builder.gtFunc(argument.getFuncName(), argument.getArgument());
            }
        });
    }

    @Test
    public void testGtFunctionCollection() throws Exception
    {
        class GtFunctionCallable implements TestCallable<FunctionArgument<Collection<String>>, JqlClauseBuilder>
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<Collection<String>> argument)
            {
                return builder.gtFunc(argument.getFuncName(), argument.getArgument());
            }
        }

        assertFunctionCollection("ccccc", Operator.GREATER_THAN, "ccccc", new GtFunctionCallable(), Collections.<String>emptyList());
        Collection<String> args = CollectionBuilder.newBuilder("arg1", "arg2", "...", "argN").asCollection();
        assertFunctionCollection("dddddd", Operator.GREATER_THAN, "dddd", new GtFunctionCallable(), args);
    }


    @Test
    public void testGtEqOperand() throws Exception
    {
        final String name = "fieldName";
        final Operand value = new SingleValueOperand("4372738");

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addCondition(name, Operator.GREATER_THAN_EQUALS, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.gtEq(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testGtEqLong() throws Exception
    {
        final String name = "fieldName";
        final long value = 1;

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addNumberCondition(name, Operator.GREATER_THAN_EQUALS, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.gtEq(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testGtEqBuilder() throws Exception
    {
        final String name = "fieldName";
        final long value = 1;

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addNumberCondition(name, Operator.GREATER_THAN_EQUALS, value)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        final ValueBuilder valueBuilder = builder.gtEq();
        assertNotNull(valueBuilder);
        assertSame(jqlClauseBuilder, valueBuilder.number(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testGtEqDate() throws Exception
    {
        assertDate("testGtEqDate", Operator.GREATER_THAN_EQUALS, new TestCallable<Date, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final Date argument)
            {
                return builder.gtEq(argument);
            }
        });
    }

    @Test
    public void testGtEqFunction() throws Exception
    {
        assertTestFunction("ghjkl", Operator.GREATER_THAN_EQUALS, "fghjkl", new TestCallable<FunctionArgument<Void>, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<Void> argument)
            {
                return builder.gtEqFunc(argument.getFuncName());
            }
        });
    }

    @Test
    public void testGtEqFunctionVarArgs() throws Exception
    {
        assertFunctionVarArgs("zxcvbnm", Operator.GREATER_THAN_EQUALS, "zxcvbnm", new TestCallable<FunctionArgument<String[]>, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<String[]> argument)
            {
                return builder.gtEqFunc(argument.getFuncName(), argument.getArgument());
            }
        });
    }

    @Test
    public void testGtEqFunctionCollection() throws Exception
    {
        class GtEqFunctionCallable implements TestCallable<FunctionArgument<Collection<String>>, JqlClauseBuilder>
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<Collection<String>> argument)
            {
                return builder.gtEqFunc(argument.getFuncName(), argument.getArgument());
            }
        }

        assertFunctionCollection("ccccc", Operator.GREATER_THAN_EQUALS, "ccccc", new GtEqFunctionCallable(), Collections.<String>emptyList());
        Collection<String> args = CollectionBuilder.newBuilder("arg1", "arg2", "...", "argN").asCollection();
        assertFunctionCollection("dddddd", Operator.GREATER_THAN_EQUALS, "dddd", new GtEqFunctionCallable(), args);
    }

    @Test
    public void testInStrings() throws Exception
    {
        final String name = "fieldName";
        final String value1 = "value1";
        final String value2 = "value2";

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addStringCondition(name, Operator.IN, value1, value2)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.in(value1, value2));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testInCollectionStrings() throws Exception
    {
        final String name = "fieldName";
        final String value1 = "value1";
        final String value2 = "value2";
        final Collection<String> values = CollectionBuilder.newBuilder(value1, value2).asList();

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addStringCondition(name, Operator.IN, values)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.inStrings(values));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testInLongs() throws Exception
    {
        final String name = "fieldName";
        final long value1 = 5;
        final long value2 = 94092840932L;

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addNumberCondition(name, Operator.IN, value1, value2)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.in(value1, value2));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testInCollectionLongs() throws Exception
    {
        final String name = "fieldName";
        final long value1 = 56690342893482L;
        final long value2 = -2381932L;
        final Collection<Long> values = CollectionBuilder.newBuilder(value1, value2).asList();

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addNumberCondition(name, Operator.IN, values)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.inNumbers(values));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testInOperands() throws Exception
    {
        final String name = "fieldName";
        final Operand value1 = Operands.valueOf(5L);

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addCondition(name, Operator.IN, new Operand[] { value1 })).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.in(value1));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testInCollectionOperands() throws Exception
    {
        final String name = "fieldName";
        final Operand value1 = Operands.valueOf(56690342893482L);
        final Operand value2 = Operands.valueOf(-2381932L);
        final Collection<Operand> values = CollectionBuilder.newBuilder(value1, value2).asList();

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addCondition(name, Operator.IN, values)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.inOperands(values));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testInDatesVarArgs() throws Exception
    {
        final String name = "fieldName";
        final Date date1 = new Date(248294038423L);
        final Date date2 = new Date(2837229588L);

        Date[] dates = new Date[]{date1, date2};

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addDateCondition(name, Operator.IN, dates)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.in(dates));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testInDatesCollection() throws Exception
    {
        final String name = "fieldName";
        final Date date1 = new Date(2482943L);
        final Date date2 = new Date(283722679588L);

        Collection<Date> values = CollectionBuilder.newBuilder(date1, date2).asCollection();

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addDateCondition(name, Operator.IN, values)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.inDates(values));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testInFunction() throws Exception
    {
        assertTestFunction("qqqq", Operator.IN, "qqqqqq", new TestCallable<FunctionArgument<Void>, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<Void> argument)
            {
                return builder.inFunc(argument.getFuncName());
            }
        });
    }

    @Test
    public void testInFunctionVarArgs() throws Exception
    {
        assertFunctionVarArgs("zxcvb", Operator.IN, "lkjhggf", new TestCallable<FunctionArgument<String[]>, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<String[]> argument)
            {
                return builder.inFunc(argument.getFuncName(), argument.getArgument());
            }
        });
    }

    @Test
    public void testInFunctionCollection() throws Exception
    {
        class InFunctionCallable implements TestCallable<FunctionArgument<Collection<String>>, JqlClauseBuilder>
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<Collection<String>> argument)
            {
                return builder.inFunc(argument.getFuncName(), argument.getArgument());
            }
        }

        assertFunctionCollection("ccccc", Operator.IN, "ccccc", new InFunctionCallable(), Collections.<String>emptyList());
        Collection<String> args = CollectionBuilder.newBuilder("arg1", "arg2", "...", "argN").asCollection();
        assertFunctionCollection("dqweqweqw", Operator.IN, "wqwqwq", new InFunctionCallable(), args);
    }

    @Test
    public void testInBuilder() throws Exception
    {
        final String name = "fieldName";
        final long value = 5L;

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addNumberCondition(name, Operator.IN, new Long[] { value })).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        final ValueBuilder valueBuilder = builder.in();
        assertNotNull(valueBuilder);
        assertSame(jqlClauseBuilder, valueBuilder.numbers(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testNotInStrings() throws Exception
    {
        final String name = "fieldName";
        final String value1 = "value1";
        final String value2 = "value2";

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addStringCondition(name, Operator.NOT_IN, value1, value2)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.notIn(value1, value2));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testNotInCollectionStrings() throws Exception
    {
        final String name = "fieldName";
        final String value1 = "value1";
        final String value2 = "value2";
        final Collection<String> values = CollectionBuilder.newBuilder(value1, value2).asList();

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addStringCondition(name, Operator.NOT_IN, values)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.notInStrings(values));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testNotInLongs() throws Exception
    {
        final String name = "fieldName";
        final long value1 = 5;
        final long value2 = 94092840932L;

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addNumberCondition(name, Operator.NOT_IN, value1, value2)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.notIn(value1, value2));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testNotInCollectionLongs() throws Exception
    {
        final String name = "fieldName";
        final long value1 = 56690342893482L;
        final long value2 = -2381932L;
        final Collection<Long> values = CollectionBuilder.newBuilder(value1, value2).asList();

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addNumberCondition(name, Operator.NOT_IN, values)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.notInNumbers(values));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testNotInOperands() throws Exception
    {
        final String name = "fieldName";
        final Operand value1 = Operands.valueOf(5L);

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addCondition(name, Operator.NOT_IN, new Operand[] { value1 })).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.notIn(value1));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testNotInCollectionOperands() throws Exception
    {
        final String name = "fieldName";
        final Operand value1 = Operands.valueOf(56690342893482L);
        final Operand value2 = Operands.valueOf(-2381932L);
        final Collection<Operand> values = CollectionBuilder.newBuilder(value1, value2).asList();

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addCondition(name, Operator.NOT_IN, values)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.notInOperands(values));

        verify(jqlClauseBuilder);
    }

        @Test
        public void testNotInDatesVarArgs() throws Exception
    {
        final String name = "fieldName";
        final Date date1 = new Date(248294038423L);
        final Date date2 = new Date(2837229588L);

        Date[] dates = new Date[]{date1, date2};

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addDateCondition(name, Operator.NOT_IN, dates)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.notIn(dates));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testNotInDatesCollection() throws Exception
    {
        final String name = "fieldName";
        final Date date1 = new Date(2482943L);
        final Date date2 = new Date(283722679588L);

        Collection<Date> values = CollectionBuilder.newBuilder(date1, date2).asCollection();

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addDateCondition(name, Operator.NOT_IN, values)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, builder.notInDates(values));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testNotInBuilder() throws Exception
    {
        final String name = "fieldName";
        final long value = 5L;

        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addNumberCondition(name, Operator.NOT_IN, new Long[] { value })).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(name, jqlClauseBuilder);
        final ValueBuilder valueBuilder = builder.notIn();
        assertNotNull(valueBuilder);
        assertSame(jqlClauseBuilder, valueBuilder.numbers(value));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testNotInFunction() throws Exception
    {
        assertTestFunction("qqqq", Operator.NOT_IN, "qqqqqq", new TestCallable<FunctionArgument<Void>, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<Void> argument)
            {
                return builder.notInFunc(argument.getFuncName());
            }
        });
    }

    @Test
    public void testNotInFunctionVarArgs() throws Exception
    {
        assertFunctionVarArgs("zxcvb", Operator.NOT_IN, "lkjhggf", new TestCallable<FunctionArgument<String[]>, JqlClauseBuilder>()
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<String[]> argument)
            {
                return builder.notInFunc(argument.getFuncName(), argument.getArgument());
            }
        });
    }

    @Test
    public void testNotInFunctionCollection() throws Exception
    {
        class NotInFunctionCallable implements TestCallable<FunctionArgument<Collection<String>>, JqlClauseBuilder>
        {
            public JqlClauseBuilder call(final ConditionBuilder builder, final FunctionArgument<Collection<String>> argument)
            {
                return builder.notInFunc(argument.getFuncName(), argument.getArgument());
            }
        }

        assertFunctionCollection("ccccc", Operator.NOT_IN, "ccccc", new NotInFunctionCallable(), Collections.<String>emptyList());
        Collection<String> args = CollectionBuilder.newBuilder("arg1", "arg2", "...", "argN").asCollection();
        assertFunctionCollection("dqweqweqw", Operator.NOT_IN, "wqwqwq", new NotInFunctionCallable(), args);
    }

    @Test
    public void testDateRange() throws Exception
    {
        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        final String fieldName = "testDateRange";
        final Date startDate = null;
        final Date endDate = new Date(4723842835492738L);

        expect(jqlClauseBuilder.addDateRangeCondition(fieldName, startDate, endDate)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(fieldName, jqlClauseBuilder);

        assertSame(jqlClauseBuilder, builder.range(startDate, endDate));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testStringRange() throws Exception
    {
        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        final String fieldName = "testStringRange";
        final String startDate = "574835kfsdj";
        final String endDate = null;

        expect(jqlClauseBuilder.addStringRangeCondition(fieldName, startDate, endDate)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(fieldName, jqlClauseBuilder);

        assertSame(jqlClauseBuilder, builder.range(startDate, endDate));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testLongRange() throws Exception
    {
        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        final String fieldName = "testLongRange";
        final Long start = 6L;
        final Long end = 773983L;

        expect(jqlClauseBuilder.addNumberRangeCondition(fieldName, start, end)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(fieldName, jqlClauseBuilder);

        assertSame(jqlClauseBuilder, builder.range(start, end));

        verify(jqlClauseBuilder);
    }

    @Test
    public void testOperandRange() throws Exception
    {
        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        final String fieldName = "testLongRange";
        final Operand start = Operands.valueOf("574835kfsdj");
        final Operand end = Operands.valueOf(6L);

        expect(jqlClauseBuilder.addRangeCondition(fieldName, start, end)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(fieldName, jqlClauseBuilder);

        assertSame(jqlClauseBuilder, builder.range(start, end));

        verify(jqlClauseBuilder);
    }

    private void assertTestFunction(String fieldName, Operator operator, String funcName,
            TestCallable<FunctionArgument<Void>, JqlClauseBuilder> functionCallable)
    {
        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addFunctionCondition(fieldName, operator, funcName)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(fieldName, jqlClauseBuilder);

        assertSame(jqlClauseBuilder, functionCallable.call(builder, new FunctionArgument<Void>(funcName, null)));

        verify(jqlClauseBuilder);
    }

    private void assertFunctionVarArgs(String clauseName, Operator operator, String funcName,
            TestCallable<FunctionArgument<String[]>, JqlClauseBuilder> functionCallable, String... args)
    {
        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addFunctionCondition(clauseName, operator, funcName, args)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(clauseName, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, functionCallable.call(builder, new FunctionArgument<String[]>(funcName, args)));

        verify(jqlClauseBuilder);
    }

    private void assertFunctionCollection(String clauseName, Operator operator, String funcName,
            TestCallable<FunctionArgument<Collection<String>>, JqlClauseBuilder> callable, Collection<String> args)
    {
        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        expect(jqlClauseBuilder.addFunctionCondition(clauseName, operator, funcName, args)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(clauseName, jqlClauseBuilder);
        assertSame(jqlClauseBuilder, callable.call(builder, new FunctionArgument<Collection<String>>(funcName, args)));

        verify(jqlClauseBuilder);
    }

    private void assertDate(String clauseName, Operator operator, TestCallable<Date, JqlClauseBuilder> call)
    {
        final JqlClauseBuilder jqlClauseBuilder = createMock(JqlClauseBuilder.class);

        Date date = new Date();

        expect(jqlClauseBuilder.addDateCondition(clauseName, operator, date)).andReturn(jqlClauseBuilder);
        replay(jqlClauseBuilder);

        DefaultConditionBuilder builder = new DefaultConditionBuilder(clauseName, jqlClauseBuilder);

        assertSame(jqlClauseBuilder, call.call(builder, date));

        verify(jqlClauseBuilder);
    }

    private interface TestCallable<A, V>
    {
        V call(ConditionBuilder builder, A argument);
    }

    private static class FunctionArgument<A>
    {
        private final String funcName;
        private final A argument;

        private FunctionArgument(final String funcName, final A argument)
        {
            this.funcName = funcName;
            this.argument = argument;
        }

        public String getFuncName()
        {
            return funcName;
        }

        public A getArgument()
        {
            return argument;
        }
    }
}
