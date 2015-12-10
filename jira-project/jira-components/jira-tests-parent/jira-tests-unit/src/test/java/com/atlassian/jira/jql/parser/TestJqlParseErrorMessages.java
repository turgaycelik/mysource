package com.atlassian.jira.jql.parser;

import com.atlassian.jira.jql.parser.antlr.JqlLexer;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test for {@link com.atlassian.jira.jql.parser.JqlParseErrorMessages}.
 *
 * @since v4.0
 */
public class TestJqlParseErrorMessages
{
    @Test
    public void testReservedWord() throws Exception
    {
        JqlParseErrorMessage expectedMessage = new JqlParseErrorMessage("jql.parse.reserved.word", 1, 21, "1", "21", "reserved");
        JqlParseErrorMessage actualMessage = JqlParseErrorMessages.reservedWord("reserved", 1, 20);
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.reserved.word", -1, 11, "?", "?", "reserved");
        actualMessage = JqlParseErrorMessages.reservedWord("reserved", -1, 10);
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.reserved.word", 10, -1, "10", "?", "reserved");
        actualMessage = JqlParseErrorMessages.reservedWord("reserved", 10, -1);
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.reserved.word", 10, -1, "10", "?", "rese rv ed");
        actualMessage = JqlParseErrorMessages.reservedWord("rese\nrv\red", 10, -1);
        assertEquals(expectedMessage, actualMessage);

        try
        {
            JqlParseErrorMessages.reservedWord(null, 1, 20);
            fail("Error is expected.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }
    }

    @Test
    public void testIllegalEsacpe() throws Exception
    {
        JqlParseErrorMessage expectedMessage = new JqlParseErrorMessage("jql.parse.illegal.escape", 1, 21, "1", "21", "\\u484");
        JqlParseErrorMessage actualMessage = JqlParseErrorMessages.illegalEsacpe("\\u484", 1, 20);
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.illegal.escape", 1, 21, "1", "21", "\\u4 84");
        actualMessage = JqlParseErrorMessages.illegalEsacpe("\\u4\n84", 1, 20);
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.illegal.escape.blank", 1, -2902, "1", "?");
        actualMessage = JqlParseErrorMessages.illegalEsacpe(null, 1, -1);
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.illegal.escape.blank", -1, 2003, "?", "?");
        actualMessage = JqlParseErrorMessages.illegalEsacpe("   ", -1, 2002);
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void testReservedCharacter() throws Exception
    {
        JqlParseErrorMessage expectedMessage = new JqlParseErrorMessage("jql.parse.reserved.character", 1, 21, "1", "21", "a", "\\u0061");
        JqlParseErrorMessage actualMessage = JqlParseErrorMessages.reservedCharacter('a', 1, 20);
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.reserved.character", 7, 8, "7", "8", "U+000A", "\\n");
        actualMessage = JqlParseErrorMessages.reservedCharacter('\n', 7, 7);
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.reserved.character", -1, 8, "?", "?", "U+FFFF", "\\uffff");
        actualMessage = JqlParseErrorMessages.reservedCharacter('\uffff', -1, 7);
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.reserved.character", 4, 8, "4", "8", "TAB", "\\t");
        actualMessage = JqlParseErrorMessages.reservedCharacter('\t', 4, 7);
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void testIllegalCharacter() throws Exception
    {
        JqlParseErrorMessage expectedMessage = new JqlParseErrorMessage("jql.parse.illegal.character", 1, 21, "1", "21", "a", "\\u0061");
        JqlParseErrorMessage actualMessage = JqlParseErrorMessages.illegalCharacter('a', 1, 20);
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.illegal.character", 7, 8, "7", "8", "U+000D", "\\r");
        actualMessage = JqlParseErrorMessages.illegalCharacter('\r', 7, 7);
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.illegal.character", -1, 8, "?", "?", "U+FFFF", "\\uffff");
        actualMessage = JqlParseErrorMessages.illegalCharacter('\uffff', -1, 7);
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.illegal.character", 4, 9, "4", "9", "TAB", "\\t");
        actualMessage = JqlParseErrorMessages.illegalCharacter('\t', 4, 8);
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void testGenericErrorPos() throws Exception
    {
        JqlParseErrorMessage expectedMessage = new JqlParseErrorMessage("jql.parse.unknown", 1, 21, "1", "21");
        JqlParseErrorMessage actualMessage = JqlParseErrorMessages.genericParseError(1, 20);
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.unknown", -1, 21, "?", "?");
        actualMessage = JqlParseErrorMessages.genericParseError(-2, 20);
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.unknown", 99, -1, "99", "?");
        actualMessage = JqlParseErrorMessages.genericParseError(99, -27387198);
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void testGenericError() throws Exception
    {
        JqlParseErrorMessage expectedMessage = new JqlParseErrorMessage("jql.parse.unknown.no.pos", -1, -1);
        JqlParseErrorMessage actualMessage = JqlParseErrorMessages.genericParseError();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void testGenericErrorToken() throws Exception
    {
        JqlParseErrorMessage expectedMessage = new JqlParseErrorMessage("jql.parse.unknown", 1, 21, "1", "21");
        JqlParseErrorMessage actualMessage = JqlParseErrorMessages.genericParseError(createToken(null, 1, 20));
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.unknown", -1, 21, "?", "?");
        actualMessage = JqlParseErrorMessages.genericParseError(createToken(null, -2, 20));
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.unknown", 99, -1, "99", "?");
        actualMessage = JqlParseErrorMessages.genericParseError(createToken(null, 99, -27387198));
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.unknown.no.pos", -1, -1);
        actualMessage = JqlParseErrorMessages.genericParseError(createEOFToken());
        assertEquals(expectedMessage, actualMessage);

        try
        {
            JqlParseErrorMessages.genericParseError(null);
            fail("Expected an error to be thrown.");
        }
        catch (IllegalArgumentException ignored)
        {
            //expected.
        }
    }

    @Test
    public void testUnfinishedString() throws Exception
    {
        JqlParseErrorMessage expectedMessage = new JqlParseErrorMessage("jql.parse.unfinished.string.blank", -1, -1, "?", "?");
        JqlParseErrorMessage actualMessage = JqlParseErrorMessages.unfinishedString(null, -1, -1);
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.unfinished.string", -1, -1, "?", "?", "dylan");
        actualMessage = JqlParseErrorMessages.unfinishedString("dylan", -20000, -1);
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.unfinished.string", -1, -1, "?", "?", "d ylan");
        actualMessage = JqlParseErrorMessages.unfinishedString("d\nylan", -20000, -1);
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void testIllegalNumber() throws Exception
    {
        final String number = "373738";
        JqlParseErrorMessage expectedMessage = new JqlParseErrorMessage("jql.parse.illegal.number", 1, 5, "1", "5", number, Long.MIN_VALUE, Long.MAX_VALUE);
        JqlParseErrorMessage actualMessage = JqlParseErrorMessages.illegalNumber(number, 1, 4);
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.illegal.number", 1, 5, "1", "5", "7383 eeee", Long.MIN_VALUE, Long.MAX_VALUE);
        actualMessage = JqlParseErrorMessages.illegalNumber("7383\neeee", 1, 4);
        assertEquals(expectedMessage, actualMessage);

        try
        {
            JqlParseErrorMessages.illegalNumber(null, 1, 4);
            fail("Expected an error to be thrown.");
        }
        catch (IllegalArgumentException ignored)
        {
            //expected.
        }
    }

    @Test
    public void testEmptyFieldName() throws Exception
    {
        JqlParseErrorMessage expectedMessage = new JqlParseErrorMessage("jql.parse.empty.field", 1, 21, "1", "21");
        JqlParseErrorMessage actualMessage = JqlParseErrorMessages.emptyFieldName(1, 20);
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.empty.field", -1, 21, "?", "?");
        actualMessage = JqlParseErrorMessages.emptyFieldName(-2, 20);
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.empty.field", 99, -1, "99", "?");
        actualMessage = JqlParseErrorMessages.emptyFieldName(99, -27387198);
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void testEmptyFunctionName() throws Exception
    {
        JqlParseErrorMessage expectedMessage = new JqlParseErrorMessage("jql.parse.empty.function", 1, 21, "1", "21");
        JqlParseErrorMessage actualMessage = JqlParseErrorMessages.emptyFunctionName(1, 20);
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.empty.function", -1, 21, "?", "?");
        actualMessage = JqlParseErrorMessages.emptyFunctionName(-2, 20);
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.empty.function", 99, -1, "99", "?");
        actualMessage = JqlParseErrorMessages.emptyFunctionName(99, -27387198);
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void testBadFieldName() throws Exception
    {
        JqlParseErrorMessage expectedMessage = new JqlParseErrorMessage("jql.parse.no.field.eof", -1, -1);
        JqlParseErrorMessage actualMessage = JqlParseErrorMessages.badFieldName(createEOFToken());
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.no.field", 99, 3, "99", "3", "bad");
        actualMessage = JqlParseErrorMessages.badFieldName(createToken("bad", 99, 2));
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.no.field", 99, -1, "99", "?", "ba dder");
        actualMessage = JqlParseErrorMessages.badFieldName(createToken("ba\ndder", 99, -232482907));
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.no.cf.field", 99, -1, "99", "?");
        actualMessage = JqlParseErrorMessages.badFieldName(createToken("badder", 99, -232482907, JqlLexer.LBRACKET));
        assertEquals(expectedMessage, actualMessage);

        try
        {
            JqlParseErrorMessages.badFieldName(null);
            fail("Expected an error to be thrown.");
        }
        catch (IllegalArgumentException ignored)
        {
            //expected.
        }
    }

    @Test
    public void testBadSortOrder() throws Exception
    {
        JqlParseErrorMessage expectedMessage = new JqlParseErrorMessage("jql.parse.no.order.eof", -1, -1);
        JqlParseErrorMessage actualMessage = JqlParseErrorMessages.badSortOrder(createEOFToken());
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.no.order", 99, 3, "99", "3", "bad");
        actualMessage = JqlParseErrorMessages.badSortOrder(createToken("bad", 99, 2));
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.no.order", -38, 124, "?", "?", "ba dder");
        actualMessage = JqlParseErrorMessages.badSortOrder(createToken("ba\ndder", -32, 123));
        assertEquals(expectedMessage, actualMessage);

        try
        {
            JqlParseErrorMessages.badSortOrder(null);
            fail("Expected an error to be thrown.");
        }
        catch (IllegalArgumentException ignored)
        {
            //expected.
        }
    }

    @Test
    public void testBadOperator() throws Exception
    {
        JqlParseErrorMessage expectedMessage = new JqlParseErrorMessage("jql.parse.no.operator.eof", -1, -1);
        JqlParseErrorMessage actualMessage = JqlParseErrorMessages.badOperator(createEOFToken());
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.no.operator", 99, 3, "99", "3", "bad");
        actualMessage = JqlParseErrorMessages.badOperator(createToken("bad", 99, 2));
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.no.operator", 1, -3, "1", "?", "bad der");
        actualMessage = JqlParseErrorMessages.badOperator(createToken("bad\nder", 1, -3));
        assertEquals(expectedMessage, actualMessage);

        try
        {
            JqlParseErrorMessages.badOperator(null);
            fail("Expected an error to be thrown.");
        }
        catch (IllegalArgumentException ignored)
        {
            //expected.
        }
    }

    @Test
    public void testBadCustomFieldId() throws Exception
    {
        JqlParseErrorMessage expectedMessage = new JqlParseErrorMessage("jql.parse.bad.custom.field.id.eof", -1, -1);
        JqlParseErrorMessage actualMessage = JqlParseErrorMessages.badCustomFieldId(createEOFToken());
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.bad.custom.field.id", 99, 3, "99", "3", "bad");
        actualMessage = JqlParseErrorMessages.badCustomFieldId(createToken("bad", 99, 2));
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.bad.custom.field.id", 1, -3, "1", "?", "bad der");
        actualMessage = JqlParseErrorMessages.badCustomFieldId(createToken("bad\nder", 1, -3));
        assertEquals(expectedMessage, actualMessage);

        try
        {
            JqlParseErrorMessages.badCustomFieldId(null);
            fail("Expected an error to be thrown.");
        }
        catch (IllegalArgumentException ignored)
        {
            //expected.
        }
    }

    @Test
    public void testNeedLogicalOperator() throws Exception
    {
        JqlParseErrorMessage expectedMessage = new JqlParseErrorMessage("jql.parse.logical.operator.eof", -1, -1);
        JqlParseErrorMessage actualMessage = JqlParseErrorMessages.needLogicalOperator(createEOFToken());
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.logical.operator", 5, 3, "5", "3", "b a d");
        actualMessage = JqlParseErrorMessages.needLogicalOperator(createToken("b\ra\nd", 5, 2));
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.logical.operator", 1, -3, "1", "?", "badder");
        actualMessage = JqlParseErrorMessages.needLogicalOperator(createToken("badder", 1, -3));
        assertEquals(expectedMessage, actualMessage);

        try
        {
            JqlParseErrorMessages.needLogicalOperator(null);
            fail("Expected an error to be thrown.");
        }
        catch (IllegalArgumentException ignored)
        {
            //expected.
        }
    }

    @Test
    public void testBadOperand() throws Exception
    {
        JqlParseErrorMessage expectedMessage = new JqlParseErrorMessage("jql.parse.bad.operand.eof", -1, -1);
        JqlParseErrorMessage actualMessage = JqlParseErrorMessages.badOperand(createEOFToken());
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.bad.operand", 5, -2829, "5", "?", "bad");
        actualMessage = JqlParseErrorMessages.badOperand(createToken("bad", 5, -2829));
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.bad.operand", -22828, -3, "?", "?", "ba d der");
        actualMessage = JqlParseErrorMessages.badOperand(createToken("ba\rd\nder", -22828, -3));
        assertEquals(expectedMessage, actualMessage);

        try
        {
            JqlParseErrorMessages.badOperand(null);
            fail("Expected an error to be thrown.");
        }
        catch (IllegalArgumentException ignored)
        {
            //expected.
        }
    }

    @Test
    public void testBadFunctionArgument() throws Exception
    {
        JqlParseErrorMessage expectedMessage = new JqlParseErrorMessage("jql.parse.bad.function.argument.eof", -1, -1);
        JqlParseErrorMessage actualMessage = JqlParseErrorMessages.badFunctionArgument(createEOFToken());
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.bad.function.argument", 1, 3, "1", "3", "b  ad");
        actualMessage = JqlParseErrorMessages.badFunctionArgument(createToken("b\n\nad", 1, 2));
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.bad.function.argument", -1, -3, "?", "?", "badder");
        actualMessage = JqlParseErrorMessages.badFunctionArgument(createToken("badder", -1, -3));
        assertEquals(expectedMessage, actualMessage);

        try
        {
            JqlParseErrorMessages.badFunctionArgument(null);
            fail("Expected an error to be thrown.");
        }
        catch (IllegalArgumentException ignored)
        {
            //expected.
        }
    }

    @Test
    public void testEmptyFunctionArgument() throws Exception
    {
        JqlParseErrorMessage expectedMessage = new JqlParseErrorMessage("jql.parse.empty.function.argument", -1, -1, "?", "?");
        JqlParseErrorMessage actualMessage = JqlParseErrorMessages.emptyFunctionArgument(createEOFToken());
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.empty.function.argument", 1, 3, "1", "3");
        actualMessage = JqlParseErrorMessages.emptyFunctionArgument(createToken("bad", 1, 2));
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.empty.function.argument", -1, -3, "?", "?");
        actualMessage = JqlParseErrorMessages.emptyFunctionArgument(createToken("badder", -1, -3));
        assertEquals(expectedMessage, actualMessage);

        try
        {
            JqlParseErrorMessages.badFunctionArgument(null);
            fail("Expected an error to be thrown.");
        }
        catch (IllegalArgumentException ignored)
        {
            //expected.
        }
    }

    @Test
    public void testExpectedText() throws Exception
    {
        JqlParseErrorMessage expectedMessage = new JqlParseErrorMessage("jql.parse.expected.text.eof", -1, -1, "end");
        JqlParseErrorMessage actualMessage = JqlParseErrorMessages.expectedText(createEOFToken(), "end");
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.expected.text", 99, 3, "99", "3", "by", "bad");
        actualMessage = JqlParseErrorMessages.expectedText(createToken("bad", 99, 2), "by");
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.expected.text", -38, 124, "?", "?", "order", "ba dd er");
        actualMessage = JqlParseErrorMessages.expectedText(createToken("ba\ndd\ner", -32, 123), "order");
        assertEquals(expectedMessage, actualMessage);

        try
        {
            JqlParseErrorMessages.expectedText(null, "rjek");
            fail("Expected an error to be thrown.");
        }
        catch (IllegalArgumentException ignored)
        {
            //expected.
        }

        try
        {
            JqlParseErrorMessages.expectedText(createEOFToken(), null);
            fail("Expected an error to be thrown.");
        }
        catch (IllegalArgumentException ignored)
        {
            //expected.
        }
    }

    @Test
    public void testExpectedText2() throws Exception
    {
        JqlParseErrorMessage expectedMessage = new JqlParseErrorMessage("jql.parse.expected.text.2.eof", -1, -1, "end", "here");
        JqlParseErrorMessage actualMessage = JqlParseErrorMessages.expectedText(createEOFToken(), "end", "here");
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.expected.text.2", 99, 3, "99", "3", "by", "order", "bad");
        actualMessage = JqlParseErrorMessages.expectedText(createToken("bad", 99, 2), "by", "order");
        assertEquals(expectedMessage, actualMessage);

        expectedMessage = new JqlParseErrorMessage("jql.parse.expected.text.2", -38, 124, "?", "?", "order", "by", "ba  dder");
        actualMessage = JqlParseErrorMessages.expectedText(createToken("ba\r\ndder", -32, 123), "order", "by");
        assertEquals(expectedMessage, actualMessage);

        try
        {
            JqlParseErrorMessages.expectedText(null, "rjek", "rgeh");
            fail("Expected an error to be thrown.");
        }
        catch (IllegalArgumentException ignored)
        {
            //expected.
        }

        try
        {
            JqlParseErrorMessages.expectedText(createEOFToken(), null, "hjsahdkas");
            fail("Expected an error to be thrown.");
        }
        catch (IllegalArgumentException ignored)
        {
            //expected.
        }

        try
        {
            JqlParseErrorMessages.expectedText(createEOFToken(), "hrewhrejw", null);
            fail("Expected an error to be thrown.");
        }
        catch (IllegalArgumentException ignored)
        {
            //expected.
        }
    }

    private static Token createEOFToken()
    {
        return new CommonToken(Token.EOF);
    }
    private static Token createToken(String text, int line, int position)
    {
        return createToken(text, line, position, 10);
    }

    private static Token createToken(String text, int line, int position, final int type)
    {
        CommonToken token = new CommonToken(type);
        token.setLine(line);
        token.setCharPositionInLine(position);
        token.setText(text);
        return token;
    }
}
