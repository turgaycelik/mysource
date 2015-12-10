package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.datetime.LocalDate;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlLocalDateSupport;
import org.apache.commons.lang.StringUtils;

import java.util.Date;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v4.4
 */
@Internal
public class RelativeDateSearcherInputHelper extends AbstractDateSearchInputHelper
{
    private final JqlLocalDateSupport jqlLocalDateSupport;
    private final DateTimeFormatterFactory dateTimeFormatterFactory;

    public RelativeDateSearcherInputHelper(DateSearcherConfig config, JqlOperandResolver operandResolver, JqlLocalDateSupport jqlLocalDateSupport, DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        super(config, operandResolver);
        this.dateTimeFormatterFactory = notNull("dateTimeFormatterFactory", dateTimeFormatterFactory);
        this.jqlLocalDateSupport = notNull("jqlLocalDateSupport", jqlLocalDateSupport);
    }

    @Override
    ParseDateResult getValidNavigatorDate(QueryLiteral dateLiteral, boolean allowTimeComponent)
    {
        final LocalDate localDate;
        if (dateLiteral.getLongValue() != null)
        {
            localDate = jqlLocalDateSupport.convertToLocalDate(dateLiteral.getLongValue());

            // if our long didnt convert, we should just return the literal as a string
            if (localDate == null)
            {
                return new ParseDateResult(true, dateLiteral.getLongValue().toString());
            }
        }
        else if (StringUtils.isNotBlank(dateLiteral.getStringValue()))
        {
            localDate = jqlLocalDateSupport.convertToLocalDate(dateLiteral.getStringValue());

            // if our string didnt convert, we should just return the literal as a string
            if (localDate == null)
            {
                return new ParseDateResult(true, dateLiteral.getStringValue());
            }
        }
        else
        {
            return null;
        }

        Date date = jqlLocalDateSupport.convertToDate(localDate);
        String dateString = dateTimeFormatterFactory.formatter().withStyle(DateTimeStyle.DATE_PICKER).withSystemZone().format(date);
        return new ParseDateResult(true, dateString);
    }

}
