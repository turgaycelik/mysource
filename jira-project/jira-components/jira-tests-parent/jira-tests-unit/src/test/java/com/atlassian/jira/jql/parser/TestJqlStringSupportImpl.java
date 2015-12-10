package com.atlassian.jira.jql.parser;

import java.util.Set;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.util.JqlStringSupportImpl;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.OrderByImpl;
import com.atlassian.query.order.SearchSort;
import com.atlassian.query.order.SortOrder;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * TTest for {@link com.atlassian.jira.jql.util.JqlStringSupportImpl}.
 *
 * @since v4.0
 */
public class TestJqlStringSupportImpl
{
    @Before
    public void setUp() throws Exception
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
    }

    @Test
    public void testDecodeValid() throws Exception
    {
        assertEquals("jill", JqlStringSupportImpl.decode("jill"));
        assertEquals("", JqlStringSupportImpl.decode(""));
        assertEquals(null, JqlStringSupportImpl.decode(null));
        assertEquals("   ", JqlStringSupportImpl.decode("   "));
        assertEquals("this \u5678", JqlStringSupportImpl.decode("this \\u5678"));
        assertEquals("this \t\nthis is very bad\r", JqlStringSupportImpl.decode("this \\t\\nthis is very bad\\r"));
        assertEquals("\t", JqlStringSupportImpl.decode("\\t"));
        assertEquals("\\t\n", JqlStringSupportImpl.decode("\\\\t\\n"));
        assertEquals(" this is a \u7463string with an escaped space.", JqlStringSupportImpl.decode("\\ this is a \\u7463string with an escaped space."));
        assertEquals("don\"t", JqlStringSupportImpl.decode("don\\\"t"));
        assertEquals("don't", JqlStringSupportImpl.decode("don\\\'t"));
    }

    @Test
    public void testDecodeInvalid() throws Exception
    {
        assertInvalidDecode("\\");
        assertInvalidDecode("\\q");
        assertInvalidDecode("bebwrnb\\qewner");
        assertInvalidDecode("bebwrnb\\u377gqewner");
        assertInvalidDecode("bebwrnb\\u377 ");
        assertInvalidDecode("bebwrnb\\u 377 ");
        assertInvalidDecode("bebwrnb\\u-377");
        assertInvalidDecode("bebwrnb\\u377");
        assertInvalidDecode("rjhewkjherkwjherkjwhe\\");
    }

    private void assertInvalidDecode(final String input)
    {
        try
        {
            JqlStringSupportImpl.decode(input);
            fail("String '" + input + "' should have failed.");
        }
        catch (IllegalArgumentException expected)
        {
            //ignored.
        }
    }

    @Test
    public void testIsReservedString() throws Exception
    {
        final CollectionBuilder<String> builder = CollectionBuilder.newBuilder();
        builder.addAll("abort", "access", "add", "after", "alias", "all", "alter", "and", "any", "as", "asc");
        builder.addAll("audit", "avg", "before", "begin", "between", "boolean", "break", "by", "byte", "catch", "cf", "changed");
        builder.addAll("char", "character", "check", "checkpoint", "collate", "collation", "column", "commit", "connect", "continue");
        builder.addAll("count", "create", "current", "date", "decimal", "declare", "decrement", "default", "defaults", "define", "delete");
        builder.addAll("delimiter", "desc", "difference", "distinct", "divide", "do", "double", "drop", "else", "empty", "encoding");
        builder.addAll("end", "equals", "escape", "exclusive", "exec", "execute", "exists", "explain", "false", "fetch", "file", "field");
        builder.addAll("first", "float", "for", "from", "function", "go", "goto", "grant", "greater", "group", "having");
        builder.addAll("identified", "if", "immediate", "in", "increment", "index", "initial", "inner", "inout", "input", "insert");
        builder.addAll("int", "integer", "intersect", "intersection", "into", "is", "isempty", "isnull", "join", "last", "left");
        builder.addAll("less", "like", "limit", "lock", "long", "max", "min", "minus", "mode", "modify");
        builder.addAll("modulo", "more", "multiply", "next", "noaudit", "not", "notin", "nowait", "null", "number", "object");
        builder.addAll("of", "on", "option", "or", "order", "outer", "output", "power", "previous", "prior", "privileges");
        builder.addAll("public", "raise", "raw", "remainder", "rename", "resource", "return", "returns", "revoke", "right", "row");
        builder.addAll("rowid", "rownum", "rows", "select", "session", "set", "share", "size", "sqrt", "start", "strict");
        builder.addAll("string", "subtract", "sum", "synonym", "table", "then", "to", "trans", "transaction", "trigger", "true");
        builder.addAll("uid", "union", "unique", "update", "user", "validate", "values", "view", "was", "when", "whenever", "where");
        builder.addAll("while", "with");

        final Set<String> strings = builder.asSet();

        assertEquals(strings, JqlStringSupportImpl.RESERVED_WORDS);

        for (String string : strings)
        {
            assertTrue(JqlStringSupportImpl.isReservedString(string));
        }

        assertFalse(JqlStringSupportImpl.isReservedString(""));
        assertFalse(JqlStringSupportImpl.isReservedString("otherstring"));
    }

    @Test
    public void testEncodeAsQuotedString() throws Exception
    {
        //simple wrapping test.
        assertEquals("\"jack\"", JqlStringSupportImpl.encodeAsQuotedString("jack"));
        assertEquals("\"jack and jill\"", JqlStringSupportImpl.encodeAsQuotedString("jack and jill"));
        assertNull(JqlStringSupportImpl.encodeAsQuotedString(null));
        assertEquals("\"\"", JqlStringSupportImpl.encodeAsQuotedString(""));
        assertEquals("\"\uef12 is some kind of unicode character\"", JqlStringSupportImpl.encodeAsQuotedString("\uef12 is some kind of unicode character"));

        //test things that need to be encoded.
        assertEquals("\"\\n\"", JqlStringSupportImpl.encodeAsQuotedString("\n", true));
        assertEquals("\"\n\"", JqlStringSupportImpl.encodeAsQuotedString("\n", false));
        assertEquals("\"Tab: \\t, CR: \\r, NL: \\n\"", JqlStringSupportImpl.encodeAsQuotedString("Tab: \t, CR: \r, NL: \n", true));
        assertEquals("\"Tab: \\t, CR: \r, NL: \n\"", JqlStringSupportImpl.encodeAsQuotedString("Tab: \t, CR: \r, NL: \n", false));
        assertEquals("\"Control Character: \\u000c \\u0005\"", JqlStringSupportImpl.encodeAsQuotedString("Control Character: \f \u0005"));
        assertEquals("\"Double quotes (\\\") need to be escaped.\"", JqlStringSupportImpl.encodeAsQuotedString("Double quotes (\") need to be escaped."));
        assertEquals("\"This backslash \\\\ also needs escaping\"", JqlStringSupportImpl.encodeAsQuotedString("This backslash \\ also needs escaping"));

        //test to make sure we handle all the control characters.
        for (int currentCodePoint = 0; currentCodePoint <= 0xffff; currentCodePoint++)
        {
            char currentChar = (char) currentCodePoint;
            if (currentChar == '\t' || currentChar == '\r' || currentChar == '\n' || currentChar == '"' || currentChar == '\\')
            {
                continue;
            }

            final String actualValue = JqlStringSupportImpl.encodeAsQuotedString(String.format("encodeMe %c", currentChar));
            final String expectedValue;
            if (isJqlControlCharacter(currentChar))
            {
                expectedValue = String.format("\"encodeMe \\u%04x\"", currentCodePoint);
            }
            else
            {
                expectedValue = String.format("\"encodeMe %c\"", currentChar);
            }

            assertEquals(expectedValue, actualValue);
        }
    }

    @Test
    public void testEncodeCharacter() throws Exception
    {
        assertCharacterEncoding(new Function<Character, String>()
        {
            public String get(final Character input)
            {
                return JqlStringSupportImpl.encodeCharacter(input);
            }
        }, false);
    }

    @Test
    public void testEncodeCharacterForce() throws Exception
    {
        assertCharacterEncoding(new Function<Character, String>()
        {
            public String get(final Character input)
            {
                return JqlStringSupportImpl.encodeCharacterForce(input);
            }
        }, true);
    }


    @Test
    public void testEncodeStringValueNumber() throws Exception
    {
        final JqlQueryParser mockParser = createMock(JqlQueryParser.class);

        replay(mockParser);
        JqlStringSupportImpl support = new JqlStringSupportImpl(mockParser);
        assertEquals("\"10002\"", support.encodeStringValue("10002"));

        verify(mockParser);
    }

    @Test
    public void testEncodeStringValueInvalid() throws Exception
    {
        final String value = "tes\ntme";
        final JqlQueryParser mockParser = createMock(JqlQueryParser.class);
        expect(mockParser.isValidValue(value)).andReturn(false);

        replay(mockParser);
        JqlStringSupportImpl support = new JqlStringSupportImpl(mockParser);
        assertEquals("\"tes\ntme\"", support.encodeStringValue(value));

        verify(mockParser);
    }

    @Test
    public void testEncodeStringValueValid() throws Exception
    {
        final JqlQueryParser mockParser = createMock(JqlQueryParser.class);
        expect(mockParser.isValidValue("testme2")).andReturn(true);

        replay(mockParser);
        JqlStringSupportImpl support = new JqlStringSupportImpl(mockParser);
        assertEquals("testme2", support.encodeStringValue("testme2"));

        verify(mockParser);
    }

    @Test
    public void testEncodeValueNumber() throws Exception
    {
        final JqlQueryParser mockParser = createMock(JqlQueryParser.class);

        expect(mockParser.isValidValue("10002")).andReturn(true);

        replay(mockParser);
        JqlStringSupportImpl support = new JqlStringSupportImpl(mockParser);
        assertEquals("10002", support.encodeValue("10002"));

        verify(mockParser);
    }

    @Test
    public void testEncodeValueInvalid() throws Exception
    {
        final String value = "tes\ntme";
        final JqlQueryParser mockParser = createMock(JqlQueryParser.class);
        expect(mockParser.isValidValue(value)).andReturn(false);

        replay(mockParser);
        JqlStringSupportImpl support = new JqlStringSupportImpl(mockParser);
        assertEquals("\"tes\ntme\"", support.encodeValue(value));

        verify(mockParser);
    }

    @Test
    public void testEncodeValueValid() throws Exception
    {
        final JqlQueryParser mockParser = createMock(JqlQueryParser.class);
        expect(mockParser.isValidValue("testme2")).andReturn(true);

        replay(mockParser);
        JqlStringSupportImpl support = new JqlStringSupportImpl(mockParser);
        assertEquals("testme2", support.encodeValue("testme2"));

        verify(mockParser);
    }

    @Test
    public void testEncodeFunctionArgumentValid() throws Exception
    {
        final JqlQueryParser mockParser = createMock(JqlQueryParser.class);
        expect(mockParser.isValidFunctionArgument("testme2")).andReturn(true);

        replay(mockParser);
        JqlStringSupportImpl support = new JqlStringSupportImpl(mockParser);
        assertEquals("testme2", support.encodeFunctionArgument("testme2"));

        verify(mockParser);
    }

    @Test
    public void testEncodeFunctionArgumentInvalid() throws Exception
    {
        final String argument = "tes\ntme";
        final JqlQueryParser mockParser = createMock(JqlQueryParser.class);
        expect(mockParser.isValidFunctionArgument(argument)).andReturn(false);

        replay(mockParser);
        JqlStringSupportImpl support = new JqlStringSupportImpl(mockParser);
        assertEquals("\"tes\\ntme\"", support.encodeFunctionArgument(argument));

        verify(mockParser);
    }

    @Test
    public void testEncodeFunctionNameValid() throws Exception
    {
        final JqlQueryParser mockParser = createMock(JqlQueryParser.class);
        expect(mockParser.isValidFunctionName("testme2")).andReturn(true);

        replay(mockParser);
        JqlStringSupportImpl support = new JqlStringSupportImpl(mockParser);
        assertEquals("testme2", support.encodeFunctionName("testme2"));

        verify(mockParser);
    }

    @Test
    public void testEncodeFunctionNameInvalid() throws Exception
    {
        final JqlQueryParser mockParser = createMock(JqlQueryParser.class);
        expect(mockParser.isValidFunctionName("testme")).andReturn(false);

        replay(mockParser);
        JqlStringSupportImpl support = new JqlStringSupportImpl(mockParser);
        assertEquals("\"testme\"", support.encodeFunctionName("testme"));

        verify(mockParser);
    }

    @Test
    public void testEncodeFieldNameInvalid() throws Exception
    {
        final String name = "test\nme";
        final JqlQueryParser mockParser = createMock(JqlQueryParser.class);
        expect(mockParser.isValidFieldName(name)).andReturn(false);

        replay(mockParser);

        JqlStringSupportImpl support = new JqlStringSupportImpl(mockParser);
        assertEquals("\"test\\nme\"", support.encodeFieldName(name));

        verify(mockParser);
    }

    @Test
    public void testEncodeFieldNameValid() throws Exception
    {
        final JqlQueryParser mockParser = createMock(JqlQueryParser.class);
        expect(mockParser.isValidFieldName("testme")).andReturn(true);

        replay(mockParser);
        JqlStringSupportImpl support = new JqlStringSupportImpl(mockParser);
        assertEquals("testme", support.encodeFieldName("testme"));

        verify(mockParser);
    }

    @Test
    public void testGenerateJqlStringEmpty() throws Exception
    {
        final JqlQueryParser mockParser = createNiceMock(JqlQueryParser.class);
        expect(mockParser.isValidFieldName(EasyMock.<String>anyObject())).andReturn(true).anyTimes();

        replay(mockParser);

        final JqlStringSupportImpl support = new JqlStringSupportImpl(mockParser);
        final JqlClauseBuilder builder = JqlQueryBuilder.newBuilder().where().addCondition("qwerty").eq("").or().assigneeUser("dylan");

        Query query = new QueryImpl(builder.buildClause(), "ignore = me");

        assertEquals("qwerty = \"\" OR assignee = \"dylan\"", support.generateJqlString(query));

        verify(mockParser);
    }

    @Test
    public void testGenerateJqlStringWithClauseNoSorts() throws Exception
    {
        final JqlQueryParser mockParser = createNiceMock(JqlQueryParser.class);
        expect(mockParser.isValidFieldName(EasyMock.<String>anyObject())).andReturn(true).anyTimes();

        replay(mockParser);

        final JqlStringSupportImpl support = new JqlStringSupportImpl(mockParser);
        final JqlClauseBuilder builder = JqlQueryBuilder.newBuilder().where().defaultAnd();
        builder.addStringCondition("field", "value1", "value2", "value3");
        builder.addCondition("field2", Operator.LESS_THAN, new FunctionOperand("funcName", "arg1", "arg2", "arg3"));

        Query query = new QueryImpl(builder.buildClause(), "ignore = me");

        assertEquals("field in (\"value1\", \"value2\", \"value3\") AND field2 < \"funcName\"(\"arg1\", \"arg2\", \"arg3\")", support.generateJqlString(query));

        verify(mockParser);
    }

    @Test
    public void testGenerateJqlStringNoClauseWithSorts() throws Exception
    {
        final JqlQueryParser mockParser = createNiceMock(JqlQueryParser.class);
        expect(mockParser.isValidFieldName(EasyMock.<String>anyObject())).andReturn(false).anyTimes();

        replay(mockParser);

        final QueryImpl query = new QueryImpl(null, new OrderByImpl(new SearchSort("qwerty", SortOrder.DESC), new SearchSort("blah\" qq")), null);
        final JqlStringSupportImpl support = new JqlStringSupportImpl(mockParser);

        assertEquals("ORDER BY \"qwerty\" DESC, \"blah\\\" qq\"", support.generateJqlString(query));

        verify(mockParser);
    }

    @Test
    public void testGenerateJqlStringWithClauseAndSorts() throws Exception
    {
        final JqlQueryParser mockParser = createNiceMock(JqlQueryParser.class);
        expect(mockParser.isValidFieldName(EasyMock.<String>anyObject())).andReturn(true).anyTimes();

        replay(mockParser);

        final JqlClauseBuilder builder = JqlQueryBuilder.newBuilder().where();
        builder.addStringCondition("field", "value1", "value2", "value3");

        final OrderBy orderBy = new OrderByImpl(new SearchSort("qwerty", SortOrder.ASC), new SearchSort("gOOd"));
        Query query = new QueryImpl(builder.buildClause(), orderBy, "ignore = me");

        final JqlStringSupportImpl support = new JqlStringSupportImpl(mockParser);

        assertEquals("field in (\"value1\", \"value2\", \"value3\") ORDER BY qwerty ASC, gOOd", support.generateJqlString(query));

        verify(mockParser);
    }

    @Test
    public void testGenerateJqlNoClauseOrSorts() throws Exception
    {
        final JqlQueryParser mockParser = createNiceMock(JqlQueryParser.class);
        replay(mockParser);

        final Query query = new QueryImpl();

        final JqlStringSupportImpl support = new JqlStringSupportImpl(mockParser);

        assertEquals("", support.generateJqlString(query));

        verify(mockParser);

    }

    @Test
    public void testIsControlCharacter() throws Exception
    {
        for (int currentCodePoint = 0; currentCodePoint <= 0xffff; currentCodePoint++)
        {
            char currentChar = (char) currentCodePoint;
            assertEquals(isJqlControlCharacter(currentChar),
                    JqlStringSupportImpl.isJqlControl(currentChar));
        }
    }

    @Test
    public void testGenerateJqlStringClause() throws Exception
    {
        final JqlQueryParser mockParser = createNiceMock(JqlQueryParser.class);
        expect(mockParser.isValidFieldName(EasyMock.<String>anyObject())).andReturn(true).anyTimes();

        replay(mockParser);

        final JqlStringSupportImpl support = new JqlStringSupportImpl(mockParser);
        final JqlClauseBuilder builder = JqlQueryBuilder.newBuilder().where().defaultAnd();
        builder.addStringCondition("field", "value1", "value2", "value3");
        builder.addCondition("field2", Operator.LESS_THAN, new FunctionOperand("funcName", "arg1", "arg2", "arg3"));

        assertEquals("field in (\"value1\", \"value2\", \"value3\") AND field2 < \"funcName\"(\"arg1\", \"arg2\", \"arg3\")", support.generateJqlString(builder.buildClause()));

        verify(mockParser);
    }

    private void assertCharacterEncoding(Function<Character, String> function, boolean force)
    {
        //test to make sure we handle all the control characters.
        for (int currentCodePoint = 0; currentCodePoint <= 0xffff; currentCodePoint++)
        {
            final char currentChar = (char) currentCodePoint;
            final String acutalEncoding = function.get(currentChar);
            switch (currentChar)
            {
                case '\t':
                    assertEquals("\\t", acutalEncoding);
                    break;
                case '\r':
                    assertEquals("\\r", acutalEncoding);
                    break;
                case '\n':
                    assertEquals("\\n", acutalEncoding);
                    break;
                case '\'':
                    assertEquals("\\'", acutalEncoding);
                    break;
                case '"':
                    assertEquals("\\\"", acutalEncoding);
                    break;
                case '\\':
                    assertEquals("\\\\", acutalEncoding);
                    break;
                default:

                    if (force || isJqlControlCharacter(currentChar))
                    {
                        final String expectedValue = String.format("\\u%04x", currentCodePoint);
                        assertEquals(expectedValue, acutalEncoding);
                    }
                    else
                    {
                        assertNull("The character '" + currentChar + "' should not be encoded.", acutalEncoding);
                    }
                    break;
            }
        }
    }

    private static boolean isJqlControlCharacter(char c)
    {
        return (c >= '\u0000' && c <= '\u0009') ||
                (c >= '\u000b' && c <= '\u000c') ||
                (c >= '\u000e' && c <= '\u001f') ||
                (c >= '\u007f' && c <= '\u009f') ||
                (c >= '\ufdd0' && c <= '\ufdef') ||
                (c >= '\ufffe' && c <= '\uffff');
    }
}
