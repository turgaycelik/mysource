package com.atlassian.jira.issue.search.quicksearch;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.web.util.OutlookDate;

import java.util.Date;
import java.util.Map;

import static com.atlassian.jira.datetime.DateTimeStyle.DATE_PICKER;

/**
 * A date searcher that can handle QuickSearches of the type <code>field:&lt;param&gt;</code>
 * where <code>param</code> is one of:
 * <ul>
 * <li>today</li>
 * <li>yesterday</li>
 * <li>tomorrow</li>
 * <li>Relative date.  eg: '-1w', '2d'</li>
 * <li>2 Relative dates.  eg: '-1w,4h', '-7d,7d'</li>
 * </ul>
 *
 * Note that relative dates are separated by a comma, and they cannot contain any spaces.
 * Therefore only one time slice is allowed.
 */
public abstract class DateQuickSearchHandler extends SingleWordQuickSearchHandler
{
    private final DateTimeFormatterFactory dateTimeFormatterFactory;

    protected DateQuickSearchHandler(DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        this.dateTimeFormatterFactory = dateTimeFormatterFactory;
    }

    protected Map handleWord(String word, QuickSearchResult searchResult)
    {
        String prefix = getPrefix();
        if (word != null && word.length() > prefix.length() && word.startsWith(prefix))
            word = word.substring(prefix.length());
        else
            return null;

        DateTimeFormatter dateTimeFormatter = dateTimeFormatterFactory.formatter().forLoggedInUser().withStyle(DATE_PICKER);

        final String datePickerToday = dateTimeFormatter.format(new Date());
        final String datePickerTomorrow = dateTimeFormatter.format(new Date(System.currentTimeMillis() + OutlookDate.DAY));
        final String datePickerAfterTomorrow = dateTimeFormatter.format(new Date(System.currentTimeMillis() + (2 * OutlookDate.DAY)));
        final String datePickerYesterday = dateTimeFormatter.format(new Date(System.currentTimeMillis() - OutlookDate.DAY));

        String paramName = getSearchParamName();
        String paramAfter = paramName + ":after";
        String paramBefore = paramName + ":before";
        String paramPrevious = paramName + ":previous";
        String paramNext = paramName + ":next";

        if ("today".equals(word))
            return EasyMap.build(paramAfter, datePickerToday, paramBefore, datePickerTomorrow);
        else if ("yesterday".equals(word))
            return EasyMap.build(paramAfter, datePickerYesterday, paramBefore, datePickerToday);
        else if ("tomorrow".equals(word))
            return EasyMap.build(paramAfter, datePickerToday, paramBefore, datePickerAfterTomorrow);
        else if (word.indexOf(',') != -1)
        {
            String firstTerm = word.substring(0, word.indexOf(','));
            String secondTerm = word.substring(word.indexOf(',') + 1);
            return EasyMap.build(paramPrevious, firstTerm, paramNext, secondTerm);
        }
        else
            return EasyMap.build(paramPrevious, word);

    }

    protected abstract String getPrefix();

    protected abstract String getSearchParamName();

}
