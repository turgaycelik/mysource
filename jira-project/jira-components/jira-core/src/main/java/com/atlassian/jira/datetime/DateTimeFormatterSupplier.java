package com.atlassian.jira.datetime;

import org.joda.time.DateTimeZone;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Interface for DateTimeFormatterSupplier.
 *
 * @since v4.4
 */
interface DateTimeFormatterSupplier
{
    /**
     * Returns a DateTimeFormatter that will use the given time zone, locale, and style. If the time zone parameter is
     * null, then the returned formatter uses the JIRA default time zone. If the locale parameter is null, then the
     * returned formatter uses the JIRA default locale.
     *
     * @param style a DateTimeStyle, or null
     * @param timeZone a time zone source, or null
     * @param locale a locale source, or null
     * @return a DateTimeFormatter
     */
    DateTimeFormatter getFormatterFor(@Nullable DateTimeStyle style, @Nullable Source<DateTimeZone> timeZone, @Nullable Source<Locale> locale);
}
