package com.atlassian.jira.jql.parser.antlr;

import com.atlassian.jira.jql.parser.JqlParseErrorMessage;
import com.atlassian.jira.jql.parser.JqlParseErrorMessages;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.RecognitionException;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Helps with Jql Lexer error handling.
 *
 * @since v4.0
 */
class LexerErrorHelper
{
    private final CharStream stream;
    private final AntlrPosition position;

    LexerErrorHelper(final CharStream stream, final AntlrPosition position)
    {
        this.stream = notNull("stream", stream);
        this.position = position;
    }

    void handleError(RecognitionException re)
    {
        final JqlParseErrorMessage message;
        if (position == null)
        {
            message = JqlParseErrorMessages.genericParseError(stream.getLine(), stream.getCharPositionInLine());
        }
        else
        {
            switch (position.getTokenType())
            {
                case JqlLexer.ESCAPE:
                    message = handleEscape();
                    break;
                case JqlLexer.SQUOTE_STRING:
                case JqlLexer.QUOTE_STRING:
                    message = handleStringError();
                    break;
                case JqlLexer.ERRORCHAR:
                    message = handleErrorCharacter();
                    break;
                case JqlLexer.ERROR_RESERVED:
                    message = handleReservedCharacter();
                    break;
                default:
                    message = JqlParseErrorMessages.genericParseError(position.getLineNumber(), position.getCharNumber());
                    break;
            }
        }
        throw new RuntimeRecognitionException(message, re);
    }

    /*
     * This is called when ANTLR finds a character that the grammar does not recognise. The grammar
     * lexer uses a DFA to decide if a character is in error or not. This can mean that legal characters
     * come as an error because they do not form a valid token. For example, the JQL 'comment ~ "' will
     * produce an ERRORCHAR token for the quote because the character after is not a valid string character.
     * Because of this we have to do some extra checks here.
     */
    private JqlParseErrorMessage handleErrorCharacter()
    {
        final JqlParseErrorMessage message;
        char currentChar = (char) stream.LT(1);
        if (isQuote(currentChar))
        {
            //This can happen when the quote is at the end of the string (e.g. comment ~ ")

            //is the next token the EOF.
            boolean nextEof = stream.LT(2) == CharStream.EOF;
            if (nextEof)
            {
                message = JqlParseErrorMessages.unfinishedString(null, position.getLineNumber(), position.getCharNumber());
            }
            else
            {
                //we need to consume to get an accurate error line and position.
                final int marker = stream.mark();
                stream.consume();
                final char nextChar = (char) stream.LT(1);

                if (isNewLine(nextChar))
                {
                    message = JqlParseErrorMessages.unfinishedString(null, position.getLineNumber(), position.getCharNumber());
                }
                else
                {
                    message = JqlParseErrorMessages.illegalCharacter(nextChar, stream.getLine(), stream.getCharPositionInLine());
                }
                stream.rewind(marker);
            }
        }
        else if (isEscape(currentChar))
        {
            //This can happen (e.g. comment ~ "\)

            //is the next token EOF.
            final boolean nextEof = stream.LT(2) == CharStream.EOF;
            final String text = nextEof ? null : stream.substring(position.getIndex(), getIndex() + 1);
            message = JqlParseErrorMessages.illegalEsacpe(text, position.getLineNumber(), position.getCharNumber());
        }
        else
        {
            //Assume that it is an illegal character.
            message = JqlParseErrorMessages.illegalCharacter(currentChar, position.getLineNumber(), position.getCharNumber());
        }

        return message;
    }

    /*
     * Called when we see an reserved character that is not escaped. 
     */
    private JqlParseErrorMessage handleReservedCharacter()
    {
        char currentChar = (char) stream.LT(1);
        return JqlParseErrorMessages.reservedCharacter(currentChar, position.getLineNumber(), position.getCharNumber());
    }

    /*
     * Called when we get an error when trying to tokenise a quoted or single quoted string.
     * Three main errors can occur here:
     *  1. There is an unfinished string before the eof.
     *  2. There is a newline in a string.
     *  3. There is some kind of illegal character.
     */
    private JqlParseErrorMessage handleStringError()
    {
        final JqlParseErrorMessage message;
        int currentInt = stream.LT(1);
        if (currentInt == CharStream.EOF)
        {
            //End the query without closing a string.
            String text = stream.substring(position.getIndex() + 1, getIndex() - 1);
            message = JqlParseErrorMessages.unfinishedString(text, position.getLineNumber(), position.getCharNumber());
        }
        else
        {
            char currentChar = (char)currentInt;
            if (isNewLine(currentChar))
            {
                //End the line without closing a string.
                String text = stream.substring(position.getIndex() + 1, getIndex() - 1);
                message = JqlParseErrorMessages.unfinishedString(text, position.getLineNumber(), position.getCharNumber());
            }
            else
            {
                //Some form of illegal character in the string.
                message = JqlParseErrorMessages.illegalCharacter(currentChar, stream.getLine(), stream.getCharPositionInLine());
            }
        }
        return message;
    }

    /*
     * Called when we see an escape that it incorrect (e.g. bad = "ththt\c"). This method is not called
     * when you try to start a string with an illegal character (e.g. bad = \c) because ANTLR detects
     * that it is not a valid start to a string and considers it an error character (i.e. ERRORCHAR).
     */
    private JqlParseErrorMessage handleEscape()
    {
        final int index = stream.LT(1) == CharStream.EOF ? getIndex() - 1 : getIndex();
        final String text = index <= position.getIndex() ? null : stream.substring(position.getIndex(), index);
        //We have some sort of escaping error.
        return JqlParseErrorMessages.illegalEsacpe(text, position.getLineNumber(), position.getCharNumber());
    }

    private static boolean isNewLine(char ch)
    {
        return ch == '\r' || ch == '\n';
    }

    private static boolean isQuote(char ch)
    {
        return ch == '\'' || ch == '"';
    }

    private static boolean isEscape(char ch)
    {
        return ch == '\\';
    }

    private int getIndex()
    {
        return stream.index();
    }
}
