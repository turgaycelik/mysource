package com.atlassian.jira.jql.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.atlassian.jira.jql.parser.antlr.JqlLexer;
import com.atlassian.jira.jql.parser.antlr.JqlParser;
import com.atlassian.jira.jql.parser.antlr.RuntimeRecognitionException;
import com.atlassian.jira.jql.util.JqlCustomFieldId;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.OrderByImpl;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.apache.commons.lang.StringUtils;

import net.jcip.annotations.ThreadSafe;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * An implementation of {@link JqlQueryParser} that implements the query parser using ANTLR.
 *
 * @since v4.0
 */
@ThreadSafe
public final class DefaultJqlQueryParser implements JqlQueryParser
{
    public final Query parseQuery(final String jqlQuery) throws JqlParseException
    {
        notNull("jqlQuery", jqlQuery);
        final JqlParser.query_return aReturn = parseClause(jqlQuery);
        // Never let a parsed order by be null, this means no sorts and we want the default sorts.
        final OrderBy orderByClause = (aReturn.order == null) ? new OrderByImpl() : aReturn.order;
        return new QueryImpl(aReturn.clause, orderByClause, jqlQuery);
    }

    public boolean isValidFieldName(final String fieldName)
    {
        notNull("fieldName", fieldName);
        try
        {
            if (JqlCustomFieldId.isJqlCustomFieldId(fieldName))
            {
                return true;
            }
            else
            {
                return StringUtils.equals(fieldName, createJqlParser(fieldName).fieldCheck().getName());
            }
        }
        catch (RecognitionException e)
        {
            return false;
        }
        catch (RuntimeRecognitionException e)
        {
            return false;
        }
    }

    public boolean isValidFunctionArgument(final String argument)
    {
        notNull("argument", argument);
        try
        {
            return StringUtils.equals(argument, createJqlParser(argument).argumentCheck());
        }
        catch (RecognitionException e)
        {
            return false;
        }
        catch (RuntimeRecognitionException e)
        {
            return false;
        }
    }

    public boolean isValidFunctionName(final String functionName)
    {
        notNull("functionName", functionName);
        try
        {
            return StringUtils.equals(functionName, createJqlParser(functionName).funcNameCheck());
        }
        catch (RecognitionException e)
        {
            return false;
        }
        catch (RuntimeRecognitionException e)
        {
            return false;
        }
    }

    public boolean isValidValue(final String value)
    {
        notNull("value", value);
        try
        {
            return isLong(value) || StringUtils.equals(value, createJqlParser(value).stringValueCheck());
        }
        catch (RecognitionException e)
        {
            return false;
        }
        catch (RuntimeRecognitionException e)
        {
            return false;
        }
    }

    private JqlParser.query_return parseClause(final String clauseString) throws JqlParseException
    {
        try
        {
            try
            {
                return createJqlParser(clauseString).query();
            }
            catch (RecognitionException e)
            {
                throw new JqlParseException(JqlParseErrorMessages.genericParseError(e.token), e);
            }
        }
        catch (RuntimeRecognitionException e)
        {
            //Our code throws this exception when we really want ANTLR to stop working. At the moment ANTLR produces a
            //lexer will not quit on errors, but will rather drop input. We use this RuntimeException as a workaround.
            throw new JqlParseException(e.getParseErrorMessage(), e);
        }
    }

    private JqlParser createJqlParser(final String clauseString) throws RuntimeRecognitionException
    {
        final JqlLexer lexer = new JqlLexer(new ANTLRStringStream(clauseString));
        return new JqlParser(new CommonTokenStream(lexer));
    }

    private boolean isLong(String longString)
    {
        try
        {
            Long.parseLong(longString);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    /*
     * Can be useful when testing the parser.
     */
    public static void main(String[] args) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
        DefaultJqlQueryParser parser = new DefaultJqlQueryParser();
        String line = reader.readLine();
        while (line != null)
        {
            try
            {
                final Query query = parser.parseQuery(line);
                System.out.printf("Got query: '%s'%n", query);
            }
            catch (JqlParseException e)
            {
                System.out.println("Parse error occured: " + e.getParseErrorMessage());
                e.printStackTrace(System.out);
            }
            catch (Exception e)
            {
                System.err.println("Unexpected error occured: " + e);
                e.printStackTrace(System.err);
            }

            line = reader.readLine();
        }
    }
}
