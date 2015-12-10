package com.atlassian.jira.jql.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.jql.parser.antlr.JqlLexer;
import com.atlassian.jira.jql.util.JqlStringSupportImpl;

import org.antlr.runtime.Token;
import org.apache.commons.lang.StringUtils;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Factory for {@link com.atlassian.jira.jql.parser.JqlParseErrorMessage} objects.
 *
 * @since v4.0
 */
public class JqlParseErrorMessages
{
    private JqlParseErrorMessages()
    {
        //This is an abstract factory class, no need to construct it.
    }

    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException("Why are you trying to clone me?");
    }

    public static JqlParseErrorMessage reservedWord(final String reservedWord, final int antlrLine, final int antlrColumn)
    {
        notBlank("reservedWord", reservedWord);
        return createMessage("jql.parse.reserved.word", new Position(antlrLine, antlrColumn), normalizeString(reservedWord));
    }

    public static JqlParseErrorMessage illegalEsacpe(final String illegalEscape, final int antlrLine, final int antlrColumn)
    {
        final Position pos = new Position(antlrLine, antlrColumn);
        final String normalizedString = normalizeString(illegalEscape);
        if (StringUtils.isBlank(illegalEscape))
        {
            return createMessage("jql.parse.illegal.escape.blank", pos);
        }
        else
        {
            return createMessage("jql.parse.illegal.escape", pos, normalizedString);
        }
    }

    public static JqlParseErrorMessage unfinishedString(final String currentString, final int antlrLine, final int antlrColumn)
    {
        final Position pos = new Position(antlrLine, antlrColumn);
        final String normalizedString = normalizeString(currentString);

        if (StringUtils.isBlank(currentString))
        {
            return createMessage("jql.parse.unfinished.string.blank", pos);
        }
        else
        {
            return createMessage("jql.parse.unfinished.string", pos, normalizedString);
        }
    }

    public static JqlParseErrorMessage illegalCharacter(final char currentChar, final int antlrLine, final int antlrColumn)
    {
        final Position pos = new Position(antlrLine, antlrColumn);
        final String escapeChar = JqlStringSupportImpl.encodeCharacterForce(currentChar);
        final String printableChar = getPrintableCharacter(currentChar);

        return createMessage("jql.parse.illegal.character", pos, printableChar, escapeChar);
    }

    public static JqlParseErrorMessage reservedCharacter(final char currentChar, final int antlrLine, final int antlrColumn)
    {
        final Position pos = new Position(antlrLine, antlrColumn);
        final String escapeChar = JqlStringSupportImpl.encodeCharacterForce(currentChar);
        final String printableChar = getPrintableCharacter(currentChar);

        return createMessage("jql.parse.reserved.character", pos, printableChar, escapeChar);
    }

    public static JqlParseErrorMessage genericParseError()
    {
        return new JqlParseErrorMessage("jql.parse.unknown.no.pos", -1, -1);
    }

    public static JqlParseErrorMessage genericParseError(final int antlrLine, final int antlrColumn)
    {
        final Position pos = new Position(antlrLine, antlrColumn);
        return createMessage("jql.parse.unknown", pos);
    }

    public static JqlParseErrorMessage genericParseError(final Token token)
    {
        notNull("token", token);
        final Position position = new Position(token);
        if (isEofToken(token))
        {
            return new JqlParseErrorMessage("jql.parse.unknown.no.pos", position.getLine(), position.getColumn());
        }
        else
        {
            return createMessage("jql.parse.unknown", position);
        }
    }

    public static JqlParseErrorMessage illegalNumber(final String number, final int antlrLine, final int antlrColumn)
    {
        notBlank("number", number);

        final Position pos = new Position(antlrLine, antlrColumn);
        return createMessage("jql.parse.illegal.number", pos, normalizeString(number), Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public static JqlParseErrorMessage emptyFieldName(final int antlrLine, final int antlrColumn)
    {
        return createMessage("jql.parse.empty.field", new Position(antlrLine, antlrColumn));
    }

    public static JqlParseErrorMessage emptyFunctionName(final int antlrLine, final int antlrColumn)
    {
        return createMessage("jql.parse.empty.function", new Position(antlrLine, antlrColumn));
    }

    public static JqlParseErrorMessage badFieldName(Token token)
    {
        notNull("token", token);

        final Position pos = new Position(token);
        if (isEofToken(token))
        {
            return new JqlParseErrorMessage("jql.parse.no.field.eof", pos.getLine(), pos.getColumn());
        }
        else
        {
            if (token.getType() == JqlLexer.LBRACKET)
            {
                return createMessage("jql.parse.no.cf.field", pos);
            }
            else
            {
                return createMessage("jql.parse.no.field", pos, normalizeString(token.getText()));
            }
        }
    }

    public static JqlParseErrorMessage badSortOrder(final Token token)
    {
        notNull("token", token);

        final Position pos = new Position(token);
        if (isEofToken(token))
        {
            return new JqlParseErrorMessage("jql.parse.no.order.eof", pos.getLine(), pos.getColumn());
        }
        else
        {
            return createMessage("jql.parse.no.order", pos, normalizeString(token.getText()));
        }
    }

    public static JqlParseErrorMessage badOperator(final Token token)
    {
        notNull("token", token);

        final Position pos = new Position(token);
        if (isEofToken(token))
        {
            return new JqlParseErrorMessage("jql.parse.no.operator.eof", pos.getLine(), pos.getColumn());
        }
        else
        {
            return createMessage("jql.parse.no.operator", pos, normalizeString(token.getText()));
        }
    }

    public static JqlParseErrorMessage badPropertyArgument(final Token token)
    {
        notNull("token", token);

        final Position pos = new Position(token);
        if (isEofToken(token))
        {
            return new JqlParseErrorMessage("jql.parse.bad.property.id.eof", pos.getLine(), pos.getColumn());
        }
        else
        {
            return createMessage("jql.parse.bad.property.id", pos, normalizeString(token.getText()));
        }
    }

    public static JqlParseErrorMessage badCustomFieldId(final Token token)
    {
        notNull("token", token);

        final Position pos = new Position(token);
        if (isEofToken(token))
        {
            return new JqlParseErrorMessage("jql.parse.bad.custom.field.id.eof", pos.getLine(), pos.getColumn());
        }
        else
        {
            return createMessage("jql.parse.bad.custom.field.id", pos, normalizeString(token.getText()));
        }
    }

    public static JqlParseErrorMessage badFunctionArgument(final Token token)
    {
        notNull("token", token);

        final Position pos = new Position(token);
        if (isEofToken(token))
        {
            return new JqlParseErrorMessage("jql.parse.bad.function.argument.eof", pos.getLine(), pos.getColumn());
        }
        else
        {
            return createMessage("jql.parse.bad.function.argument", pos, normalizeString(token.getText()));
        }
    }

    public static JqlParseErrorMessage needLogicalOperator(final Token token)
    {
        notNull("token", token);

        final Position pos = new Position(token);
        if (isEofToken(token))
        {
            return new JqlParseErrorMessage("jql.parse.logical.operator.eof", pos.getLine(), pos.getColumn());
        }
        else
        {
            return createMessage("jql.parse.logical.operator", pos, normalizeString(token.getText()));
        }
    }

    public static JqlParseErrorMessage badOperand(final Token token)
    {
        notNull("token", token);

        final Position pos = new Position(token);
        if (isEofToken(token))
        {
            return new JqlParseErrorMessage("jql.parse.bad.operand.eof", pos.getLine(), pos.getColumn());
        }
        else
        {
            return createMessage("jql.parse.bad.operand", pos, normalizeString(token.getText()));
        }
    }

    public static JqlParseErrorMessage emptyFunctionArgument(final Token token)
    {
        notNull("token", token);

        return createMessage("jql.parse.empty.function.argument", new Position(token));
    }


    public static JqlParseErrorMessage expectedText(final Token token, String expected)
    {
        notNull("token", token);
        notBlank("expected", expected);

        final Position pos = new Position(token);
        if (isEofToken(token))
        {
            return new JqlParseErrorMessage("jql.parse.expected.text.eof", pos.getLine(), pos.getColumn(), expected);
        }
        else
        {
            return createMessage("jql.parse.expected.text", pos, expected, normalizeString(token.getText()));
        }
    }

    public static JqlParseErrorMessage expectedText(final Token token, String expected1, String expected2)
    {
        notNull("token", token);
        notBlank("expected1", expected1);
        notBlank("expected2", expected2);

        final Position pos = new Position(token);
        if (isEofToken(token))
        {
            return new JqlParseErrorMessage("jql.parse.expected.text.2.eof", pos.getLine(), pos.getColumn(), expected1, expected2);
        }
        else
        {
            return createMessage("jql.parse.expected.text.2", pos, expected1, expected2, normalizeString(token.getText()));
        }
    }

    public static JqlParseErrorMessage unsupportedPredicate(final String predicate , final String operator)
    {
        notNull("predicate", predicate);
        notBlank("operator", operator);
        final Position pos = new Position(0, 0);
        return createMessage("jql.parse.predicate.unsupported",pos, operator, predicate );
    }

    public static JqlParseErrorMessage unsupportedOperand(final String operator, final String operand)
    {
        notNull("operand", operand);
        notBlank("operator", operator);
        final Position pos = new Position(0, 0);
        return createMessage("jql.parse.operand.unsupported",pos, operator, operand );
    }

    private static JqlParseErrorMessage createMessage(String key, Position pos, Object...args)
    {
        List<Object> arguments = new ArrayList<Object>(args.length + 2);
        arguments.add(pos.getLineString());
        arguments.add(pos.getColumnString());
        arguments.addAll(Arrays.asList(args));
        return new JqlParseErrorMessage(key, pos.getLine(), pos.getColumn(), arguments);
    }

    private static String normalizeString(String argument)
    {
        if (argument != null)
        {
            return argument.replace('\n', ' ').replace('\r', ' ');
        }
        else
        {
            return argument;
        }
    }

    private static String getPrintableCharacter(char c)
    {
        final Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(c);
        if (JqlStringSupportImpl.isJqlControl(c) || Character.isWhitespace(c) ||
                unicodeBlock == null || unicodeBlock == Character.UnicodeBlock.SPECIALS)
        {
            if (c == '\t')
            {
                return String.format("TAB");
            }
            else
            {
                return String.format("U+%04X", (int) c);
            }
        }
        else
        {
            return String.valueOf(c);
        }
    }

    private static boolean isEofToken(final Token token)
    {
        return token.getType() == Token.EOF;
    }

    private static class Position
    {
        private int line;
        private int column;

        private Position(final Token token)
        {
            this (token.getLine(), token.getCharPositionInLine());
        }

        private Position(final int line, final int column)
        {
            this.line = normalizeLine(line);
            this.column = normalizeColumn(column);
        }

        private static int normalizeLine(int line)
        {
            return line <= 0 ? -1 : line;
        }

        private static int normalizeColumn(int column)
        {
            return column < 0 ? -1 : column + 1;
        }

        public int getLine()
        {
            return line;
        }

        public int getColumn()
        {
            return column;
        }

        public String getLineString()
        {
            if (line < 0)
            {
                return "?";
            }
            else
            {
                return String.valueOf(line);
            }
        }

        public String getColumnString()
        {
            if (line < 0 || column < 0)
            {
                return "?";
            }
            else
            {
                return String.valueOf(column);
            }
        }
    }
}
