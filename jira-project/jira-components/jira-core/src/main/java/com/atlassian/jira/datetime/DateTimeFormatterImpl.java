package com.atlassian.jira.datetime;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.dbc.Assertions;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Thread safe date time formatter. This class just holds the configuration that will be used for time zone and locale
 * (e.g. which user's locale to use) but all the real formatting/parsing work is performed by the delegate
 * DateTimeFormatStrategy.
 *
 * @since 4.4
 */
@ThreadSafe
class DateTimeFormatterImpl implements DateTimeFormatter
{
    private final DateTimeFormatStrategy formatStrategy;
    private final Source<DateTimeZone> zone;
    private final Source<Locale> locale;
    private final DateTimeFormatterSupplier supplier;
    private final DateTimeSettings settings;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    DateTimeFormatterImpl(DateTimeFormatStrategy formatStrategy, DateTimeFormatterSupplier supplier, DateTimeSettings settings, JiraAuthenticationContext jiraAuthenticationContext, @Nullable Source<DateTimeZone> zone, @Nullable Source<Locale> locale)
    {
        this.formatStrategy = Assertions.notNull(formatStrategy);
        this.supplier = Assertions.notNull(supplier);
        this.settings = Assertions.notNull(settings);
        this.jiraAuthenticationContext = Assertions.notNull(jiraAuthenticationContext);
        this.zone = zone != null ? zone : new DefaultTimeZone();
        this.locale = locale != null ? locale : new DefaultLocale();
    }

    @Override
    public String format(Date date)
    {
        return formatStrategy.format(new DateTime(date, zone.get()), locale.get());
    }

    @Override
    public Date parse(String text)
    {
        return formatStrategy.parse(text, zone.get(), locale.get());
    }

    @Override
    public DateTimeFormatter forLoggedInUser()
    {
        return supplier.getFormatterFor(getStyle(), new AuthContextTimeZone(), new AuthContextLocale());
    }

    @Override
    public DateTimeFormatter forUser(@Nullable User user)
    {
        return supplier.getFormatterFor(getStyle(), constant(settings.timeZoneFor(user)), constant(settings.localeFor(user)));
    }

    @Override
    public DateTimeFormatter withDefaultZone()
    {
        return withZone(null);
    }

    @Override
    public DateTimeFormatter withSystemZone()
    {
        SystemTimeZone systemZone = new SystemTimeZone();
        if (zone.equals(systemZone))
        {
            return this;
        }

        return supplier.getFormatterFor(getStyle(), systemZone, locale);
    }

    @Override
    public DateTimeFormatter withZone(@Nullable TimeZone timeZone)
    {
        Source<DateTimeZone> timeZoneSource = timeZone != null ? constant(DateTimeZone.forTimeZone(timeZone)) : new DefaultTimeZone();
        if (zone.equals(timeZoneSource))
        {
            return this;
        }

        return supplier.getFormatterFor(getStyle(), timeZoneSource, locale);
    }

    @Override
    public DateTimeFormatter withDefaultLocale()
    {
        return withLocale(null);
    }

    @Override
    public DateTimeFormatter withLocale(@Nullable Locale locale)
    {
        Source<Locale> localeOverride = locale != null ? constant(locale) : new DefaultLocale();
        if (this.locale.equals(localeOverride))
        {
            return this;
        }

        return supplier.getFormatterFor(getStyle(), zone, localeOverride);
    }

    @Override
    public DateTimeFormatter withStyle(DateTimeStyle style)
    {
        if (style == getStyle())
        {
            return this;
        }

        return supplier.getFormatterFor(style, zone, locale);
    }

    @Override
    public TimeZone getZone()
    {
        return zone.isOverride() ? zone.get().toTimeZone() : null;
    }

    @Override
    public Locale getLocale()
    {
        return locale.isOverride() ? locale.get() : null;
    }

    @Override
    public DateTimeStyle getStyle()
    {
        return formatStrategy.style();
    }

    @Override
    public String getFormatHint()
    {
        return formatStrategy.pattern();
    }

    /**
     * Returns a Constant having the given value.
     *
     * @param value the value to use in the constant
     * @return a Constant
     */
    static <T> ConstantSource<T> constant(T value)
    {
        return new ConstantSource<T>(value);
    }

    /**
     * Returns the locale of the currently logged in user.
     */
    class AuthContextLocale extends FromSettingsSource<Locale>
    {
        @Override
        public Locale get()
        {
            return settings.localeFor(jiraAuthenticationContext.getLoggedInUser());
        }
    }

    /**
     * Returns the locale of the provided user.
     */
    class DefaultLocale extends FromSettingsSource<Locale>
    {
        @Override
        public Locale get()
        {
            return settings.localeFor(null);
        }
    }

    /**
     * Returns the time zone of the currently logged in user.
     */
    class AuthContextTimeZone extends FromSettingsSource<DateTimeZone>
    {
        @Override
        public DateTimeZone get()
        {
            return settings.timeZoneFor(jiraAuthenticationContext.getLoggedInUser());
        }
    }

    /**
     * Returns the time zone of the provided user.
     */
    class DefaultTimeZone extends FromSettingsSource<DateTimeZone>
    {
        @Override
        public DateTimeZone get()
        {
            return settings.timeZoneFor(null);
        }
    }

    /**
     * Returns the system time zone.
     */
    class SystemTimeZone extends FromSettingsSource<DateTimeZone>
    {
        @Override
        public DateTimeZone get()
        {
            // do it this way because other libraries might not update JODA default, causing the java.util and JODA
            // default to be out of sync.
            return DateTimeZone.forTimeZone(TimeZone.getDefault());
        }
    }

    /**
     * Returns a specific override value.
     */
    static class ConstantSource<T> extends SourceTemplate implements Source<T>
    {
        private final T override;

        public ConstantSource(T override) { this.override = Assertions.notNull(override); }

        @Override
        public boolean isOverride() { return true; }

        @Override
        public T get() { return override; }
    }

    /**
     * Base class for sources that get the value from DateTimeSettings.
     */
    static abstract class FromSettingsSource<T> extends SourceTemplate implements Source<T>
    {
        @Override
        public boolean isOverride() { return false; }
    }

    /**
     * Abstract source class that implements equals/hashCode.
     */
    abstract static class SourceTemplate
    {
        @Override
        public int hashCode() { return HashCodeBuilder.reflectionHashCode(this); }

        @Override
        public boolean equals(Object obj) { return EqualsBuilder.reflectionEquals(this, obj); }
    }
}
