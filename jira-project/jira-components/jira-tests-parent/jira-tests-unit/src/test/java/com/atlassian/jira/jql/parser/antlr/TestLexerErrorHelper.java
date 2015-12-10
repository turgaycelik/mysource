package com.atlassian.jira.jql.parser.antlr;

import com.atlassian.jira.jql.parser.JqlParseErrorMessage;
import com.atlassian.jira.jql.parser.JqlParseErrorMessages;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * Test for {@link com.atlassian.jira.jql.parser.antlr.LexerErrorHelper}.
 *
 * @since v4.0
 */
public class TestLexerErrorHelper
{
    @Test
    public void testCotr() throws Exception
    {
        try
        {
            new LexerErrorHelper(null, null);
            fail("Illegal Argument Exception");
        }
        catch (IllegalArgumentException e)
        {
            //illegal argument exception expected.
        }
        
        try
        {
            new LexerErrorHelper(null, new AntlrPosition(JqlLexer.AMPER, new ANTLRStringStream("qwerty")));
            fail("Illegal Argument Exception");
        }
        catch (IllegalArgumentException e)
        {
            //illegal argument exception expected.
        }
    }

    @Test
    public void testUnknownError() throws Exception
    {
        ANTLRStringStream stream = new ANTLRStringStream("t\nhis is a very long test string");
        stream.seek(5);

        LexerErrorHelper helper = new LexerErrorHelper(stream, new AntlrPosition(JqlLexer.AMPER_AMPER, stream));
        JqlParseErrorMessage expectedMessage = JqlParseErrorMessages.genericParseError(2, 3);
        RecognitionException expectedCause = new RecognitionException();

        assertResult(helper, expectedMessage, expectedCause);
    }

    @Test
    public void testNoPosition() throws Exception
    {
        ANTLRStringStream stream = new ANTLRStringStream("t\nhis is a very long test string");
        stream.seek(5);

        LexerErrorHelper helper = new LexerErrorHelper(stream, null);
        JqlParseErrorMessage expectedMessage = JqlParseErrorMessages.genericParseError(2, 3);
        RecognitionException expectedCause = new RecognitionException();

        assertResult(helper, expectedMessage, expectedCause);
    }

    @Test
    public void testHandleEscape() throws Exception
    {
        //Check unfinished escape sequence.
        String input = "this\\";
        ANTLRStringStream stream = new ANTLRStringStream(input);
        stream.seek(input.length() - 1);
        AntlrPosition antlrPosition = new AntlrPosition(JqlLexer.ESCAPE, stream);
        LexerErrorHelper helper = new LexerErrorHelper(stream, antlrPosition);
        JqlParseErrorMessage expectedMessage = JqlParseErrorMessages.illegalEsacpe(null, 1, 4);
        RecognitionException expectedCause = new RecognitionException();

        assertResult(helper, expectedMessage, expectedCause);

        //Check an illegal escape sequence.
        stream = new ANTLRStringStream("this\\n");
        //Set the escape start for the position.
        stream.seek(4);
        antlrPosition = new AntlrPosition(JqlLexer.ESCAPE, stream);
        //Move to the first error character.
        stream.consume();
        helper = new LexerErrorHelper(stream, antlrPosition);

        expectedMessage = JqlParseErrorMessages.illegalEsacpe("\\n", 1, 4);
        expectedCause = new RecognitionException();

        assertResult(helper, expectedMessage, expectedCause);
    }

    @Test
    public void testHandleStringErrorEof() throws Exception
    {
        //Check an unfinished string.
        String input = "this = \"";
        ANTLRStringStream stream = new ANTLRStringStream(input);

        //The string position starts before the end.
        stream.seek(input.length() - 1);
        AntlrPosition antlrPosition = new AntlrPosition(JqlLexer.SQUOTE_STRING, stream);
        //Move to the EOF.
        stream.consume();

        LexerErrorHelper helper = new LexerErrorHelper(stream, antlrPosition);
        JqlParseErrorMessage expectedMessage = JqlParseErrorMessages.unfinishedString("", 1, 7);
        RecognitionException expectedCause = new RecognitionException();

        assertResult(helper, expectedMessage, expectedCause);

        //Check an unfinished string to EOF.
        input = "\"this";
        stream = new ANTLRStringStream(input);

        //String starts at the start.
        antlrPosition = new AntlrPosition(JqlLexer.SQUOTE_STRING, stream);
        stream.seek(input.length());

        helper = new LexerErrorHelper(stream, antlrPosition);
        expectedMessage = JqlParseErrorMessages.unfinishedString("this", 1, 0);
        expectedCause = new RecognitionException();

        assertResult(helper, expectedMessage, expectedCause);
    }

    @Test
    public void testHandleStringErrorNewline() throws Exception
    {
        //Check an unfinished string on newline.
        String input = "this=\"\nblah";
        ANTLRStringStream stream = new ANTLRStringStream(input);

        //The string position starts before the end.
        stream.seek(5);
        AntlrPosition antlrPosition = new AntlrPosition(JqlLexer.QUOTE_STRING, stream);
        //Move to the EOF.
        stream.consume();

        LexerErrorHelper helper = new LexerErrorHelper(stream, antlrPosition);
        JqlParseErrorMessage expectedMessage = JqlParseErrorMessages.unfinishedString("", 1, 5);
        RecognitionException expectedCause = new RecognitionException();

        assertResult(helper, expectedMessage, expectedCause);

        //Check an unfinished string on newline with trailing text.
        input = "\"this\nistheend";
        stream = new ANTLRStringStream(input);

        //String starts at the start.
        antlrPosition = new AntlrPosition(JqlLexer.SQUOTE_STRING, stream);
        stream.seek(5);

        helper = new LexerErrorHelper(stream, antlrPosition);
        expectedMessage = JqlParseErrorMessages.unfinishedString("this", 1, 0);
        expectedCause = new RecognitionException();

        assertResult(helper, expectedMessage, expectedCause);
    }

    @Test
    public void testHandleStringIllegalCharacter() throws Exception
    {
        //Check a string with illegal characters.
        String input = "wwwww\uffffblah";
        ANTLRStringStream stream = new ANTLRStringStream(input);

        stream.seek(5);
        AntlrPosition antlrPosition = new AntlrPosition(JqlLexer.QUOTE_STRING, stream);

        LexerErrorHelper helper = new LexerErrorHelper(stream, antlrPosition);
        JqlParseErrorMessage expectedMessage = JqlParseErrorMessages.illegalCharacter('\uffff', 1, 5);
        RecognitionException expectedCause = new RecognitionException();

        assertResult(helper, expectedMessage, expectedCause);
    }

    @Test
    public void testHandleErrorCharacterString() throws Exception
    {
        //Check a that ends with a quote.
        String input = "comment=\"";
        ANTLRStringStream stream = new ANTLRStringStream(input);

        stream.seek(8);
        AntlrPosition antlrPosition = new AntlrPosition(JqlLexer.ERRORCHAR, stream);

        LexerErrorHelper helper = new LexerErrorHelper(stream, antlrPosition);
        JqlParseErrorMessage expectedMessage = JqlParseErrorMessages.unfinishedString(null, 1, 8);
        RecognitionException expectedCause = new RecognitionException();

        assertResult(helper, expectedMessage, expectedCause);

        //Check what happens when we have quote followed by an illegal character.
        input = "comment=\"\ufdd4";
        stream = new ANTLRStringStream(input);

        stream.seek(8);
        antlrPosition = new AntlrPosition(JqlLexer.ERRORCHAR, stream);

        helper = new LexerErrorHelper(stream, antlrPosition);
        expectedMessage = JqlParseErrorMessages.illegalCharacter('\ufdd4', 1, 9);
        expectedCause = new RecognitionException();

        assertResult(helper, expectedMessage, expectedCause);

        //Check what happens when we have quote followed by a newline.
        input = "comment=\"\r";
        stream = new ANTLRStringStream(input);

        stream.seek(8);
        antlrPosition = new AntlrPosition(JqlLexer.ERRORCHAR, stream);

        helper = new LexerErrorHelper(stream, antlrPosition);
        expectedMessage = JqlParseErrorMessages.unfinishedString(null, 1, 8);
        expectedCause = new RecognitionException();

        assertResult(helper, expectedMessage, expectedCause);
    }

    @Test
    public void testHandleErrorCharacterEscape() throws Exception
    {
        //Check a query that ends with an empty escape.
        String input = "comment=\\";
        ANTLRStringStream stream = new ANTLRStringStream(input);

        stream.seek(8);
        AntlrPosition antlrPosition = new AntlrPosition(JqlLexer.ERRORCHAR, stream);

        LexerErrorHelper helper = new LexerErrorHelper(stream, antlrPosition);
        JqlParseErrorMessage expectedMessage = JqlParseErrorMessages.illegalEsacpe(null, 1, 8);
        RecognitionException expectedCause = new RecognitionException();

        assertResult(helper, expectedMessage, expectedCause);

        //Check a query that ends with an illegal escape.
        input = "comment=\\c";
        stream = new ANTLRStringStream(input);

        stream.seek(8);
        antlrPosition = new AntlrPosition(JqlLexer.ERRORCHAR, stream);

        helper = new LexerErrorHelper(stream, antlrPosition);
        expectedMessage = JqlParseErrorMessages.illegalEsacpe("\\c", 1, 8);
        expectedCause = new RecognitionException();

        assertResult(helper, expectedMessage, expectedCause);
    }

    @Test
    public void testHandleErrorCharacterIllegalCharacter() throws Exception
    {
        //Check a query that ends with an empty escape.
        String input = "comment=bad\uffffchatacer";
        ANTLRStringStream stream = new ANTLRStringStream(input);

        stream.seek(11);
        AntlrPosition antlrPosition = new AntlrPosition(JqlLexer.ERRORCHAR, stream);

        LexerErrorHelper helper = new LexerErrorHelper(stream, antlrPosition);
        JqlParseErrorMessage expectedMessage = JqlParseErrorMessages.illegalCharacter('\uffff', 1, 11);
        RecognitionException expectedCause = new RecognitionException();

        assertResult(helper, expectedMessage, expectedCause);
    }

    @Test
    public void testReserved() throws Exception
    {
        String input = "chat#acer";
        ANTLRStringStream stream = new ANTLRStringStream(input);

        stream.seek(4);
        AntlrPosition antlrPosition = new AntlrPosition(JqlLexer.ERROR_RESERVED, stream);

        LexerErrorHelper helper = new LexerErrorHelper(stream, antlrPosition);
        JqlParseErrorMessage expectedMessage = JqlParseErrorMessages.reservedCharacter('#', 1, 4);

        assertResult(helper, expectedMessage, null);

    }

    private void assertResult(final LexerErrorHelper helper, final JqlParseErrorMessage expectedMessage, final RecognitionException expectedCause)
    {
        try
        {
            helper.handleError(expectedCause);
            fail("Expected exception");
        }
        catch (RuntimeRecognitionException e)
        {
            assertEquals(expectedMessage, e.getParseErrorMessage());
            assertSame(expectedCause, e.getCause());
        }
    }
}
