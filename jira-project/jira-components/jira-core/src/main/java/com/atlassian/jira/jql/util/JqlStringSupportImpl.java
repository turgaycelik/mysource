package com.atlassian.jira.jql.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.atlassian.fugue.Effect;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.Property;
import com.atlassian.query.order.SearchSort;

import org.apache.commons.lang.StringUtils;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Some utility code to help with JQL strings.
 * <p/>
 * The JQL grammar depends on the implementation {@link #isReservedString} method from this class. The other methods
 * depend upon the definitions within the JQL grammar (Jql.g). Changing the grammar will likely require changing this
 * class.
 *
 * @since v4.0
 */
public final class JqlStringSupportImpl implements JqlStringSupport
{
    private static final Map<Character, Character> STRING_DECODE_MAPPING;
    private static final Map<Character, String> STRING_ENCODE_MAPPING;
    private static final char QUOTE_CHAR = '"';
    private static final char SQUOTE_CHAR = '\'';

    public static final Set<String> RESERVED_WORDS;

    static
    {
        Map<Character, Character> decoderMapping = new HashMap<Character, Character>();
        decoderMapping.put('t', '\t');
        decoderMapping.put('n', '\n');
        decoderMapping.put('r', '\r');
        decoderMapping.put('\\', '\\');
        decoderMapping.put(QUOTE_CHAR, QUOTE_CHAR);
        decoderMapping.put(SQUOTE_CHAR, SQUOTE_CHAR);
        decoderMapping.put(' ', ' ');

        STRING_DECODE_MAPPING = Collections.unmodifiableMap(decoderMapping);

        Map<Character, String> encoderStringMapping = new HashMap<Character, String>();
        encoderStringMapping.put('\t', "\\t");
        encoderStringMapping.put('\n', "\\n");
        encoderStringMapping.put('\r', "\\r");
        encoderStringMapping.put(QUOTE_CHAR, "\\\"");
        encoderStringMapping.put(SQUOTE_CHAR, "\\'");
        encoderStringMapping.put('\\', "\\\\");

        STRING_ENCODE_MAPPING = Collections.unmodifiableMap(encoderStringMapping);

        //NOTE: Changing the contents of this method will change the strings that the JQL parser will parse, so think
        // about the change you are about to make.
        // Also, see TestReservedWords.java in the func_test project if you are going to make changes.
        CollectionBuilder<String> builder = CollectionBuilder.newBuilder();
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
        RESERVED_WORDS = builder.asSet();
    }

    private final JqlQueryParser parser;

    public JqlStringSupportImpl(JqlQueryParser parser)
    {
        this.parser = notNull("parser", parser);
    }

    public Set<String> getJqlReservedWords()
    {
        return RESERVED_WORDS;
    }

    public String encodeStringValue(final String value)
    {
        notNull("value", value);
        if (isLong(value) || !parser.isValidValue(value))
        {
            return encodeAsQuotedString(value, false);
        }
        else
        {
            return value;
        }
    }

    public String encodeValue(final String value)
    {
        notNull("value", value);
        if (!parser.isValidValue(value))
        {
            return encodeAsQuotedString(value, false);
        }
        else
        {
            return value;
        }
    }

    public String encodeFunctionArgument(final String argument)
    {
        if (!parser.isValidFunctionArgument(argument))
        {
            return encodeAsQuotedString(argument, true);
        }
        else
        {
            return argument;
        }
    }

    public String encodeFunctionName(final String functionName)
    {
        if (!parser.isValidFunctionName(functionName))
        {
            return encodeAsQuotedString(functionName, true);
        }
        else
        {
            return functionName;
        }
    }

    public String encodeFieldName(final String fieldName)
    {
        if (!parser.isValidFieldName(fieldName))
        {
            return encodeAsQuotedString(fieldName, true);
        }
        else
        {
            return fieldName;
        }
    }

    public String generateJqlString(final Query query)
    {
        notNull("query", query);

        final StringBuilder builder = new StringBuilder();
        if (query.getWhereClause() != null)
        {
            final ToJqlStringVisitor jqlStringVisitor = new ToJqlStringVisitor(this);
            builder.append(jqlStringVisitor.toJqlString(query.getWhereClause()));
        }
        if (query.getOrderByClause() != null && !query.getOrderByClause().getSearchSorts().isEmpty())
        {
            if (builder.length() != 0)
            {
                builder.append(" ");
            }
            builder.append("ORDER BY ");

            for (Iterator<SearchSort> sortIterator = query.getOrderByClause().getSearchSorts().iterator(); sortIterator.hasNext();)
            {
                SearchSort sort = sortIterator.next();
                builder.append(encodeFieldName(sort.getField()));
                sort.getProperty().foreach(new Effect<Property>()
                {
                    @Override
                    public void apply(final Property property)
                    {
                        builder.append("[")
                                .append(encodeFieldName(property.getKeysAsString()))
                                .append("].")
                                .append(encodeFieldName(property.getObjectReferencesAsString()));
                    }
                });
                if (sort.getOrder() != null)
                {
                    builder.append(" ").append(sort.getOrder());
                }
                if (sortIterator.hasNext())
                {
                    builder.append(", ");
                }
            }
        }

        return builder.toString();
    }

    public String generateJqlString(final Clause clause)
    {
        final ToJqlStringVisitor jqlStringVisitor = new ToJqlStringVisitor(this);
        return jqlStringVisitor.toJqlString(clause);
    }

    private static boolean isLong(final String str)
    {
        try
        {
            Long.parseLong(str);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    /**
     * Remove escaping JQL escaping from the passed string.
     *
     * @param string the string to decode.
     * @return the decoded string.
     * @throws IllegalArgumentException if the input string contains invalid escape sequences.
     */
    public static String decode(final String string) throws IllegalArgumentException
    {
        if (StringUtils.isBlank(string))
        {
            return string;
        }

        StringBuilder stringBuilder = null;
        for (int position = 0; position < string.length();)
        {
            final char currentCharacter = string.charAt(position);
            position++;
            if (currentCharacter == '\\')
            {
                if (position >= string.length())
                {
                    throw new IllegalArgumentException("Unterminated escape sequence.");
                }
                if (stringBuilder == null)
                {
                    stringBuilder = new StringBuilder(string.length());
                    if (position > 1)
                    {
                        stringBuilder.append(string.substring(0, position - 1));
                    }
                }
                final char escapeCharacter = string.charAt(position);
                position++;
                final Character substituteChar = STRING_DECODE_MAPPING.get(escapeCharacter);
                if (substituteChar == null)
                {
                    //Maybe some unicode escaping ?
                    if (escapeCharacter == 'u')
                    {
                        if (position + 4 > string.length())
                        {
                            throw new IllegalArgumentException("Unterminated escape sequence '\\u" + string.substring(position) + "'.");
                        }
                        final String hexString = string.substring(position, position + 4);
                        position += 4;
                        try
                        {
                            final int i = Integer.parseInt(hexString, 16);
                            if (i < 0)
                            {
                                throw new IllegalArgumentException("Illegal unicode escape '\\u" + hexString + "'.");
                            }
                            else
                            {
                                stringBuilder.append((char) i);
                            }
                        }
                        catch (NumberFormatException e)
                        {
                            throw new IllegalArgumentException("Illegal unicode escape '\\u" + hexString + "'.", e);
                        }
                    }
                    else
                    {
                        throw new IllegalArgumentException("Illegal escape sequence '\\" + escapeCharacter + "'.");
                    }
                }
                else
                {
                    stringBuilder.append(substituteChar);
                }
            }
            else if (stringBuilder != null)
            {
                stringBuilder.append(currentCharacter);
            }
        }

        return stringBuilder == null ? string : stringBuilder.toString();
    }

    /**
     * Encode the passed string into a valid JQL encoded quoted string.
     *
     * @param string the string to encode.
     * @return the encoded string.
     */
    public static String encodeAsQuotedString(String string)
    {
        return encodeAsQuotedString(string, false);
    }

    /**
     * Encode the passed string into a valid JQL encoded quoted string. A JQL string can represent newline (\n) and
     * carriage return (\r) in two different ways: Its raw value or its escaped value. The passed boolean flag can be
     * used to control which representation is used.
     *
     * @param string the string to encode.
     * @param escapeNewline should escape and newline characters be escaped.
     * @return the encoded string.
     */
    public static String encodeAsQuotedString(String string, final boolean escapeNewline)
    {
        if (string == null)
        {
            return null;
        }

        StringBuilder builder = null;
        for (int position = 0; position < string.length(); position++)
        {
            final char currentCharacter = string.charAt(position);
            final String appendString;

            //Newlines don't need to be quoted in strings.
            if (escapeNewline || (currentCharacter != '\r' && currentCharacter != '\n'))
            {
                appendString = encodeCharacter(currentCharacter, SQUOTE_CHAR, false);
            }
            else
            {
                appendString = null;
            }
            if (appendString != null)
            {
                if (builder == null)
                {
                    builder = new StringBuilder(string.length());
                    builder.append(QUOTE_CHAR);
                    if (position > 0)
                    {
                        builder.append(string.substring(0, position));
                    }
                }
                builder.append(appendString);
            }
            else if (builder != null)
            {
                builder.append(currentCharacter);
            }
        }
        return builder == null ? QUOTE_CHAR + string + QUOTE_CHAR : builder.append(QUOTE_CHAR).toString();
    }

    /**
     * Encode the passed character so that it may be used in JQL. null will be returned if the string does not need
     * to be encoded and the encoding has not been forced.
     *
     * @param character the character to encode.
     * @param ignoredCharacter the character not to encode. -1 can be passed to indicate that this character should be
     * excluded from encoding. This setting overrides force for this character.
     * @param force when true, the passed character will be encoded even if it does not need to be.
     * @return the encoded character or null if the passed character did not need to be encoded.
     */
    private static String encodeCharacter(char character, int ignoredCharacter, boolean force)
    {
        if (ignoredCharacter >= 0 && character == (char)ignoredCharacter)
        {
            return null;
        }

        final String encodedCharacter = STRING_ENCODE_MAPPING.get(character);
        if (encodedCharacter == null && (force || isJqlControl(character)))
        {
            return String.format("\\u%04x", (int) character);
        }
        return encodedCharacter;
    }

    /**
     * Escape the passed character so that it may be used in JQL. The character is escaped even if it does not need to be.
     *
     * @param character the character to escape.
     * @return the escaped character.
     */
    public static String encodeCharacterForce(char character)
    {
        return encodeCharacter(character, -1, true);
    }

    /**
     * Encode the passed character so that it may be used in JQL. null will be returned if the character does not need
     * to be encoded.
     *
     * @param character the character to encode.
     * @return the encoded character or null if it does not need to be encoded.
     */
    public static String encodeCharacter(char character)
    {
        return encodeCharacter(character, -1, false);
    }

    /**
     * Tell the caller if the passed string is a reserved JQL string. We do this in here rather than the grammar because
     * ANTLR does not deal well (generates a huge and very slow lexer) when matching lots of different tokens. In fact,
     * the ANTLR grammar calls this method internally to see if a JQL string is reserved.
     *
     * @param string the word to test.
     * @return true if the passed string is a JQL reserved word.
     */
    public static boolean isReservedString(String string)
    {
        notNull("string", string);

        //NOTE: Changing the implementation of this method will change the strings that the JQL parser will parse. We can
        //simply call toLowerCase here becasue all the reserved words are ENGLISH.
        return RESERVED_WORDS.contains(string.toLowerCase(Locale.ENGLISH));
    }


    /**
     * Tells if caller if the passed character is considered a control character by JQL.
     * <p/>
     * NOTE: This method duplicates some logic from the grammar. If the grammar changes then this method will also need
     * to change. We have replicated the logic for effeciency reasons.
     *
     * @param c the character to check.
     * @return true if the passed character is a JQL control character, false otherwise.
     */
    public static boolean isJqlControl(char c)
    {
        /*
        From the JQL grammar:

        fragment CONTROLCHARS
            :	'\u0000'..'\u0009'  //Exclude '\n' (\u000a)
            |   '\u000b'..'\u000c'  //Exclude '\r' (\u000d)
            |   '\u000e'..'\u001f'
            |	'\u007f'..'\u009f'
            //The following are Unicode non-characters. We don't want to parse them. Importantly, we wish
            //to ignore U+FFFF since ANTLR evilly uses this internally to represent EOF which can cause very
            //strange behaviour. For example, the Lexer will incorrectly tokenise the POSNUMBER 1234 as a STRING
            //when U+FFFF is not excluded from STRING.
            //
            //http://en.wikipedia.org/wiki/Unicode
            | 	'\ufdd0'..'\ufdef'
            |	'\ufffe'..'\uffff'
            ;

         */
        return (c >= '\u0000' && c <= '\u0009') ||
                (c >= '\u000b' && c <= '\u000c') ||
                (c >= '\u000e' && c <= '\u001f') ||
                (c >= '\u007f' && c <= '\u009f') ||
                (c >= '\ufdd0' && c <= '\ufdef') ||
                (c >= '\ufffe' && c <= '\uffff');
    }
}
