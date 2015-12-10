/**
 * A small set of classes that allow clients to format and parse dates in JIRA, in a way that is time zone and locale
 * aware.
 *
 * <h2>Time zones</h2>
 * <p>When working with dates and times inside JIRA, it is important to understand the following time zone concepts.</p>
 * <ul>
 *     <li><b>System time zone:</b> the time zone of the JVM, as defined by {@link java.util.TimeZone#getDefault()},</li>
 *     <li><b>Default user time zone:</b> the JIRA default time zone, as configured by an administrator, and</li>
 *     <li><b>User time zone:</b> the user-specific time zone, as configured by each user.</li>
 * </ul>
 * <p>
 * By default, <b>formatters in this package will operate using the time zone of the user that is currently logged
 * in</b>. If the default user time zone has not configured, it defaults to the system time zone. Similarly, if a user
 * has not configured a user time zone, JIRA will use the default user time zone when displaying date and times to that
 * user.
 * </p>
 * <h2>Locales</h2>
 * <p>
 * JIRA is capable of displaying dates in the default JIRA locale, and also in the user's locale, when a user has
 * explicitly configured a different locale. By default, <b>formatters in this package will operate using the locale of
 * the user that is currently logged in</b>.
 * </p>
 * <h2 id="usage">Usage</h2>
 * <p>
 * Most of the work is done by implementations of the {@link DateTimeFormatter} interface. If you intent to use date,
 * and especially date/time values in your plugin, you should have a {@link DateTimeFormatterFactory} injected into your
 * plugin classes, which you can use to build <code>DateTimeFormatter</code> instances, as in the following example.
 * </p>
 * <pre>
 *     public class MyPluginClass
 *     {
 *         private final DateTimeFormatter dateTimeFormatter;
 *
 *         public MyPluginClass(DateTimeFormatter dateTimeFormatter)
 *         {
 *             // call forLoggedInUser() to associate the formatter with the currently logged in user
 *             this.dateTimeFormatter = dateTimeFormatter.forLoggedInUser();
 *         }
 *
 *         // formats dates in the user's time zone and with the user's locale
 *         public String currentDate()
 *         {
 *             return dateTimeFormatter.withStyle(COMPLETE).format(new Date());
 *         }
 *
 *         // formats dates in UTC and with the user's locale
 *         public String currentDateInUTC()
 *         {
 *             return dateTimeFormatter.withZone(TimeZone.UTC).format(new Date());
 *         }
 *
 *         // formats dates in the user's time zone and in Dutch
 *         public String currentDateInDutch()
 *         {
 *             return dateTimeFormatter.withLocale(new Locale("nl")).format(new Date());
 *         }
 *     }
 * </pre>
 *
 * @since 4.4
 */
package com.atlassian.jira.datetime;
