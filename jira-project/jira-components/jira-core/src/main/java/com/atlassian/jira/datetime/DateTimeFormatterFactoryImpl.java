package com.atlassian.jira.datetime;

import com.atlassian.core.util.Clock;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneResolver;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.RealClock;
import com.google.common.collect.ImmutableMap;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Locale;
import java.util.TimeZone;

/**
 * This class is responsible for providing DateTimeFormatter instances to JIRA and plugin code.
 *
 * @since v4.4
 */
@EventComponent
public class DateTimeFormatterFactoryImpl implements DateTimeFormatterFactory
{
    /**
     * Logger for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(DateTimeFormatterFactoryImpl.class);

    private final DateTimeFormatterSupplier formatterSupplier = new FormatterSupplier();
    private final JodaFormatterCache jodaFormatterCache = new JodaFormatterCache();
    private final DateTimeSettings settings = new SettingsSupplier();

    /**
     * Known formatters. These are all immutable.
     */
    final ImmutableMap<DateTimeStyle, DateTimeFormatStrategy> formatters;

    private final TimeZoneResolver timeZoneResolver;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final ApplicationProperties applicationProperties;
    private final I18nHelper.BeanFactory i18n;
    private final Clock clock;

    public DateTimeFormatterFactoryImpl(TimeZoneResolver timeZoneResolver, JiraAuthenticationContext jiraAuthenticationContext, ApplicationProperties applicationProperties, I18nHelper.BeanFactory i18nFactory)
    {
        this(timeZoneResolver, jiraAuthenticationContext, applicationProperties, i18nFactory, RealClock.getInstance());
    }

    DateTimeFormatterFactoryImpl(TimeZoneResolver timeZoneResolver, JiraAuthenticationContext jiraAuthenticationContext, ApplicationProperties applicationProperties, I18nHelper.BeanFactory i18nFactory, Clock clock)
    {
        this.timeZoneResolver = timeZoneResolver;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.applicationProperties = applicationProperties;
        this.i18n = i18nFactory;
        this.clock = clock;

        // NB: The formatters have to be initialized AFTER all fields have been set.
        this.formatters = createFormatters();
        log.trace("Known date time formatters: {}", formatters);
    }

    @Override
    public DateTimeFormatter formatter()
    {
        return formatterSupplier.getFormatterFor(DateTimeStyle.RELATIVE, null, null);
    }

    /**
     * Clears this instance's cache.
     *
     * @param event a ClearCacheEvent
     */
    @EventListener
    public void onClearCache(ClearCacheEvent event)
    {
        jodaFormatterCache.clear();
        log.trace("Cleared formatter cache due to event: {}", event);
    }

    /**
     * Creates a new ImmutableMap of DateTimeStyle->DateTimeFormatStrategy.
     *
     * @return an ImmutableMap
     */
    private ImmutableMap<DateTimeStyle, DateTimeFormatStrategy> createFormatters()
    {
        DateTimeFormatterServiceProvider serviceProvider = new ServiceProvider();

        return ImmutableMap.<DateTimeStyle, DateTimeFormatStrategy>builder()
                .put(DateTimeStyle.RELATIVE, new DateTimeRelativeFormatter(serviceProvider, jodaFormatterCache, applicationProperties, clock))
                .put(DateTimeStyle.COMPLETE, new DateTimeCompleteFormatter(serviceProvider, jodaFormatterCache))
                .put(DateTimeStyle.TIME, new DateTimeTimeFormatter(serviceProvider, jodaFormatterCache))
                .put(DateTimeStyle.DATE, new DateTimeDateFormatter(serviceProvider, jodaFormatterCache))
                .put(DateTimeStyle.DATE_PICKER, new DateTimeDatePickerFormatter(serviceProvider, jodaFormatterCache))
                .put(DateTimeStyle.DATE_TIME_PICKER, new DateTimePickerFormatter(serviceProvider, jodaFormatterCache))
                .put(DateTimeStyle.RELATIVE_WITH_TIME_ONLY, new DateTimeRelativeDatesWithTimeFormatter(serviceProvider, applicationProperties, timeZoneResolver, jiraAuthenticationContext, jodaFormatterCache, clock))
                .put(DateTimeStyle.RELATIVE_WITHOUT_TIME, new DateTimeRelativeNoTimeFormatter(serviceProvider, applicationProperties, timeZoneResolver, jiraAuthenticationContext, jodaFormatterCache, clock))
                .put(DateTimeStyle.RELATIVE_ALWAYS_WITH_TIME, new DateTimeRelativeDatesAlwaysWithTime(serviceProvider, applicationProperties, timeZoneResolver, jiraAuthenticationContext, jodaFormatterCache, clock))
                .put(DateTimeStyle.ISO_8601_DATE, new DateTimeISO8601DateFormatter(jodaFormatterCache))
                .put(DateTimeStyle.ISO_8601_DATE_TIME, new DateTimeISO8601DateTimeFormatter(jodaFormatterCache))
                .put(DateTimeStyle.RSS_RFC822_DATE_TIME, new DateTimeRFC822DateTimeFormatter(jodaFormatterCache))
                .build();
    }

    @ThreadSafe
    class FormatterSupplier implements DateTimeFormatterSupplier
    {
        @Override
        public DateTimeFormatter getFormatterFor(DateTimeStyle style, Source<DateTimeZone> timeZone, Source<Locale> locale)
        {
            DateTimeFormatStrategy strategy = formatters.get(style != null ? style : DateTimeStyle.RELATIVE);

            return new DateTimeFormatterImpl(strategy, formatterSupplier, settings, jiraAuthenticationContext, timeZone, locale);
        }
    }

    /**
     * Provides services to formatter strategies.
     */
    @ThreadSafe
    class ServiceProvider implements DateTimeFormatterServiceProvider
    {
        public org.joda.time.format.DateTimeFormatter formatter(String pattern, Locale locale)
        {
            return jodaFormatterCache.get(new JodaFormatterCache.Key(pattern, locale));
        }

        public String getDefaultBackedString(String key)
        {
            return applicationProperties.getDefaultBackedString(key);
        }

        public String getUnescapedText(String key)
        {
            return i18n.getInstance(jiraAuthenticationContext.getLoggedInUser()).getUnescapedText(key);
        }

        public String getText(String key, Object... parameters)
        {
            return i18n.getInstance(jiraAuthenticationContext.getLoggedInUser()).getText(key, parameters);
        }

    }

    private class SettingsSupplier implements DateTimeSettings
    {
        @Override
        public DateTimeZone timeZoneFor(@Nullable User user)
        {
            final JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(user);
            if (user != null)
            {
                TimeZone userTimeZone = timeZoneResolver.getUserTimeZone(serviceContext);
                if (userTimeZone != null)
                {
                    return DateTimeZone.forTimeZone(userTimeZone);
                }
            }

            return DateTimeZone.forTimeZone(timeZoneResolver.getDefaultTimeZone(serviceContext));
        }

        @Override
        public Locale localeFor(@Nullable User user)
        {
            Locale userLocale = user != null ? i18n.getInstance(user).getLocale() : null;
            if (userLocale != null)
            {
                return userLocale;
            }

            Locale defaultLocale = applicationProperties.getDefaultLocale();
            if (defaultLocale != null)
            {
                return defaultLocale;
            }

            return Locale.getDefault();
        }
    }
}
