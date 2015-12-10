package com.atlassian.jira.datetime;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.InjectableComponent;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Interface for JIRA date time formatters. All formatters are time zone-aware (the actual time zone that they use when
 * formatting dates will depend on how they were created). Formatters  can be injected directly into other classes or
 * alternatively created using {@code DateTimeFormatterFactory}. See <a href="http://docs.atlassian.com/software/jira/docs/api/latest/com/atlassian/jira/datetime/package-summary.html#usage">
 * com.atlassian.jira.datetime</a> for examples of how to use DateTimeFormatter. Example:
 * <p/>
 * All implementations of this interface are thread safe, and are therefore safe to cache and reuse across different
 * requests.
 *
 * @see DateTimeFormatterFactory
 * @since 4.4
 */
@ThreadSafe
@InjectableComponent
public interface DateTimeFormatter
{
    /**
     * Formats a Date as a human-readable string, using the date/time style returned by {@linkplain #getStyle}.
     *
     * @param date a Date instance
     * @return a String containing a formatted date
     */
    String format(Date date);

    /**
     * Parses a date from the given text, returning a new Date. The text will be interpreted as being in the timezone of
     * this formatter.
     *
     * @param text a String containing a date
     * @return a new Date
     * @throws IllegalArgumentException if the input text can not be parsed
     * @throws UnsupportedOperationException if this strategy does not support parsing
     */
    Date parse(String text) throws IllegalArgumentException, UnsupportedOperationException;

    /**
     * Returns a new formatter that will use the time zone and locale of the user that is logged in when format and/or
     * parse are called, if any (as specified by {@link com.atlassian.jira.security.JiraAuthenticationContext
     * JiraAuthenticationContext}). If there is no logged in user, or if the logged in user has not configured a time
     * zone and/or locale, the JIRA default time zone and/or locale is used.
     * <p/>
     * It is intended that clients will reuse the formatter obtained from this method across requests: it will always
     * use the time zone and locale of the currently logged in user.
     * 
     * <p/>
     * <strong>Important:</strong>
     * You should use system time zone when you use this method together with {@link DateTimeStyle#DATE} style as
     * it formats date without time information.
     *
     * @return a new DateTimeFormatter
     * @see com.atlassian.jira.security.JiraAuthenticationContext#getLoggedInUser()
     * @see #withSystemZone()
     */
    DateTimeFormatter forLoggedInUser();

    /**
     * Returns a new formatter that will use the given user's time zone and locale. If the user argument is null, this
     * formatter will use the default JIRA time zone and locale at invocation time.
     *
     * @param user the User whose time zone and locale the new formatter will use
     * @return a new DateTimeFormatter
     */
    DateTimeFormatter forUser(@Nullable User user);

    /**
     * Returns a new formatter that will use the JIRA default time zone to format and parse dates.
     *
     * @return a new DateTimeFormatter
     */
    DateTimeFormatter withDefaultZone();

    /**
     * Returns a new formatter that will use the system time zone to format and parse dates.
     *
     * @return a new DateTimeFormatter
     * @see java.util.TimeZone#getDefault()
     */
    DateTimeFormatter withSystemZone();

    /**
     * Returns a new formatter that will use the specified zone instead of the JIRA default time zone. If the time zone
     * argument is null, this formatter will use the JIRA default time zone at invocation time.
     *
     * @param timeZone a TimeZone
     * @return a new DateTimeFormatter
     */
    DateTimeFormatter withZone(@Nullable TimeZone timeZone);

    /**
     * Returns a new formatter that will use the JIRA default locale to format and parse dates.
     *
     * @return a new DateTimeFormatter
     */
    DateTimeFormatter withDefaultLocale();

    /**
     * Specifies the Locale to use when formatting dates. If the locale argument is null, this formatter will use the
     * JIRA default locale at invocation time.
     *
     * @param locale a Locale
     * @return a new DateTimeFormatter
     */
    DateTimeFormatter withLocale(@Nullable Locale locale);

    /**
     * Specifies the style to use when formatting dates. If the style argument is null, this formatter will use the JIRA
     * default style at invocation time.
     *
     * @param style a DateTimeStyle
     * @return a new DateTimeFormatter
     */
    DateTimeFormatter withStyle(@Nullable DateTimeStyle style);

    /**
     * Returns this formatter's time zone. If null, this formatter will use the default JIRA time zone at invocation
     * time.
     *
     * @return the TimeZone
     */
    TimeZone getZone();

    /*
     * Returns this formatter's locale. If null, this formatter will use the default JIRA locale at invocation time.
     *
     * @return the Locale
     */
    Locale getLocale();

    /**
     * Returns this formatter's style. If null, this formatter will use the JIRA default date time style at invocation
     * time.
     *
     * @return the DateTimeStyle
     */
    DateTimeStyle getStyle();

    /**
     * Returns a hint for this formatter. This hint can be shown on screen to help the user understand the format that
     * can be parsed by this formatter. Note that the hint should be understandable by a human, but not necessarily by a
     * Java date formatter.
     *
     * @return a String containing a pattern
     */
    String getFormatHint();
}
