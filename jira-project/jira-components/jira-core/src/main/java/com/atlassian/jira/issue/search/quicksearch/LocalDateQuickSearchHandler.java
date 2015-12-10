package com.atlassian.jira.issue.search.quicksearch;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.datetime.LocalDateFactory;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.web.util.OutlookDate;

import java.util.Date;
import java.util.Map;

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
public abstract class LocalDateQuickSearchHandler extends SingleWordQuickSearchHandler
{
    private final TimeZoneManager timeZoneManager;

    protected LocalDateQuickSearchHandler(TimeZoneManager timeZoneManager)
    {
        this.timeZoneManager = timeZoneManager;
    }

    protected Map handleWord(String word, QuickSearchResult searchResult)
    {
        String prefix = getPrefix();
        if (word != null && word.length() > prefix.length() && word.startsWith(prefix))
            word = word.substring(prefix.length());
        else
            return null;

        final String datePickerToday = LocalDateFactory.getLocalDate(new Date(), timeZoneManager.getLoggedInUserTimeZone()).toString();
        final String datePickerTomorrow = LocalDateFactory.getLocalDate(new Date(System.currentTimeMillis() + OutlookDate.DAY), timeZoneManager.getLoggedInUserTimeZone()).toString();
        final String datePickerYesterday = LocalDateFactory.getLocalDate(new Date(System.currentTimeMillis() - OutlookDate.DAY), timeZoneManager.getLoggedInUserTimeZone()).toString();

        String paramName = getSearchParamName();
        String paramAfter = paramName + ":after";
        String paramPrevious = paramName + ":previous";
        String paramNext = paramName + ":next";
        String paramEquals = paramName + ":equals";

        if ("today".equals(word))
            return EasyMap.build(paramEquals, datePickerToday);
        else if ("yesterday".equals(word))
            return EasyMap.build(paramEquals, datePickerYesterday);
        else if ("tomorrow".equals(word))
            return EasyMap.build(paramEquals, datePickerTomorrow);
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
