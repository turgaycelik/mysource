package com.atlassian.jira.datetime;

import com.atlassian.crowd.embedded.api.User;
import org.joda.time.DateTimeZone;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Date/time-related settings fetcher.
 *
 * @since 4.4
 */
interface DateTimeSettings
{
    /**
     * Returns the locale to use when displaying dates to the given user. If the user argument is null, or if the user
     * has not configured a locale, the default JIRA locale will be used.
     *
     * @param user a User
     * @return a Locale
     */
    Locale localeFor(@Nullable User user);

    /**
     * Returns the time zone that is configured for the given user. If the user argument is null, or if the user has not
     * configured a time zone, returns the JIRA default time zone.
     *
     * @param user a User, or null
     * @return a TimeZone
     */
    DateTimeZone timeZoneFor(@Nullable User user);
}
