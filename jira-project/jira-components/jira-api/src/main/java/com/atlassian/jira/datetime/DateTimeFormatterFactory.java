package com.atlassian.jira.datetime;

import com.atlassian.jira.util.InjectableComponent;

/**
 * This factory is used to create {@link DateTimeFormatter} instances within JIRA. By default a formatter will be
 * configured to use the JIRA default time zone, locale, and date style.
 *
 * @since 4.4
 */
@InjectableComponent
public interface DateTimeFormatterFactory
{
    /**
     * Creates a new DateTimeFormatter that uses the JIRA default time zone, locale, and {@link
     * DateTimeStyle#RELATIVE relative} date style. To get a "smart" formatter that automatically uses the time zone and locale of the
     * currently logged in user, create a new formatter by calling {@link com.atlassian.jira.datetime.DateTimeFormatter#forLoggedInUser()
     * DateTimeFormatter.forLoggedInUser()} on the formatter returned by this method, e.g.:
     * <p/>
     * <pre>
     *     DateTimeFormatter userFormatter = dateTimeFormatterFactory.formatter().forLoggedInUser();
     * </pre>
     * Note that since JIRA 5.0 you can get a {@code DateTimeFormatter} injected directly into your class (in that case
     * there is no need to use this factory class).
     * <p/>
     * A formatter obtained in this fashion may be safely reused across requests.
     *
     * @return a new DateTimeFormatter
     */
    DateTimeFormatter formatter();
}
