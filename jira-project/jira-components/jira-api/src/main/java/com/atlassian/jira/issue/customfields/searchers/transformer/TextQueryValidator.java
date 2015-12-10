package com.atlassian.jira.issue.customfields.searchers.transformer;

import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.issue.search.util.TextTermEscaper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;

import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;

/**
 * A single class to validate text queries (LIKE) - used in few places to handle validation consistently.
 *
 * @since v6.1
 */
@Internal
public class TextQueryValidator
{
    private static final Logger log = Logger.getLogger(TextQueryValidator.class);
    public static final Pattern BAD_RANGEIN_PATTERN = Pattern.compile("Cannot parse.*Was expecting.*(\"TO\"|<RANGEIN_QUOTED>|<RANGEIN_GOOP>|\"]\"|\"}\").*", Pattern.DOTALL);
    public static final Pattern BAD_RANGEEX_PATTERN = Pattern.compile("Cannot parse.*Was expecting.*(\"TO\"|<RANGEEX_QUOTED>|<RANGEEX_GOOP>|\"]\"|\"}\").*", Pattern.DOTALL);

    public MessageSet validate(final QueryParser queryParser, final String query, final String fieldName,
            @Nullable final String sourceFunction, final boolean shortMessage, final I18nHelper i18n)
    {
        final MessageSet messageSet = new MessageSetImpl();
        try
        {
            queryParser.parse(TextTermEscaper.escape(query));
            // if it didn't throw an exception it must be valid
        }
        catch (ParseException e)
        {
            handleException(fieldName, sourceFunction, query, i18n, messageSet, shortMessage, e);
        }
        catch (RuntimeException re)
        {
            // JRA-27018  FuzzyQuery throws IllegalArgumentException instead of ParseException :(
            handleException(fieldName, sourceFunction, query, i18n, messageSet, shortMessage, re);
        }
        return messageSet;
    }

    private void handleException(final String fieldName, @Nullable final String sourceFunction, final String value,
            final I18nHelper i18n, final MessageSet messageSet, final boolean useShortMessage, final Exception ex)
    {
        final String errorMessage = translateException(ex, i18n, fieldName, sourceFunction, useShortMessage, value);

        if (errorMessage != null)
        {
            messageSet.addErrorMessage(errorMessage);
        }
        else
        {
            log.debug(String.format("Unable to parse the text '%s' for field '%s'.", value, fieldName), ex);
            if (sourceFunction != null) {
                messageSet.addErrorMessage(useShortMessage ? i18n.getText("navigator.error.parse.function", sourceFunction)
                        : i18n.getText("jira.jql.text.clause.does.not.parse.function", fieldName, sourceFunction));
            }
            else {
                messageSet.addErrorMessage(useShortMessage ? i18n.getText("navigator.error.parse")
                        : i18n.getText("jira.jql.text.clause.does.not.parse", value, fieldName));
            }
        }
    }

    /**
     * This method handles known lucene errors into user-friendly error message.
     */
    @Nullable
    private String translateException(final Exception ex, final I18nHelper i18n, final String fieldName,
            @Nullable final String sourceFunction, final boolean shortMessage, final String value)
    {
        if (ex instanceof ParseException)
        {
            final ParseException parseException = (ParseException) ex;
            final String exMessage = parseException.getMessage();
            if (exMessage != null)
            {
                if (exMessage.endsWith("'*' or '?' not allowed as first character in WildcardQuery"))
                {
                    return getErrorMessage(i18n, sourceFunction, shortMessage, fieldName, value, "jira.jql.text.clause.bad.start.in.wildcard");
                }
                else if (BAD_RANGEIN_PATTERN.matcher(exMessage).matches() || BAD_RANGEEX_PATTERN.matcher(exMessage).matches()) {
                    return getErrorMessage(i18n, sourceFunction, shortMessage, fieldName, value, "jira.jql.text.clause.incorrect.range.query");
                }
            }
        }
        return null;
    }

    private String getErrorMessage(final I18nHelper i18n, @Nullable final String sourceFunction,
            final boolean shortMessage, final String fieldName, final String value, final String i18nMessagePrefix) {
        if (sourceFunction != null) {
            return shortMessage
                    ? i18n.getText(i18nMessagePrefix + ".function.short", sourceFunction)
                    : i18n.getText(i18nMessagePrefix  + ".function", sourceFunction, fieldName);
        }
        else {
            return shortMessage
                    ? i18n.getText(i18nMessagePrefix  + ".short")
                    : i18n.getText(i18nMessagePrefix, value, fieldName);
        }
    }
}
