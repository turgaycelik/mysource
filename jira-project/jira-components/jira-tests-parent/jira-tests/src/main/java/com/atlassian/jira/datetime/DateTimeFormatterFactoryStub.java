package com.atlassian.jira.datetime;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.RealClock;
import com.google.common.collect.ImmutableMap;
import org.easymock.IAnswer;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import javax.annotation.Nullable;
import java.util.Locale;

import static com.atlassian.jira.datetime.DateTimeFormatterImpl.constant;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

/**
 * Stub for DateTimeFormatterFactory, useful for unit tests.
 *
 * @since v4.4
 */
public class DateTimeFormatterFactoryStub implements DateTimeFormatterFactory
{
    // default JIRA time zone and locale
    private DateTimeZone jiraTimeZone = DateTimeZone.getDefault();
    private Locale jiraLocale = Locale.getDefault();

    // user's configured time zone and locale
    private DateTimeZone userTimeZone = DateTimeZone.getDefault();
    private Locale userLocale = Locale.getDefault();

    private boolean useRelativeDates = true;

    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final ImmutableMap<DateTimeStyle, DateTimeFormatStrategy> patterns;

    public DateTimeFormatterFactoryStub()
    {
        jiraAuthenticationContext = createMock(JiraAuthenticationContext.class);
        expect(jiraAuthenticationContext.getLoggedInUser()).andStubReturn(new MockUser("mockMeNot"));
        replay(jiraAuthenticationContext);

        ApplicationProperties applicationProperties = createMock(ApplicationProperties.class);
        expect(applicationProperties.getOption(APKeys.JIRA_LF_DATE_RELATIVE)).andStubAnswer(new RelativeDatesAnswer());
        replay(applicationProperties);

        patterns = ImmutableMap.<DateTimeStyle, DateTimeFormatStrategy>builder()
                .put(DateTimeStyle.RELATIVE, new DateTimeRelativeFormatter(new ServiceProviderStub(), new JodaFormatterStub(), applicationProperties, RealClock.getInstance()))
                .put(DateTimeStyle.COMPLETE, new DateTimeCompleteFormatter(new ServiceProviderStub(), new JodaFormatterStub()))
                .put(DateTimeStyle.DATE, new DateTimeDateFormatter(new ServiceProviderStub(), new JodaFormatterStub()))
                .put(DateTimeStyle.TIME, new DateTimeTimeFormatter(new ServiceProviderStub(), new JodaFormatterStub()))
                .put(DateTimeStyle.DATE_TIME_PICKER, new DateTimePickerFormatter(new ServiceProviderStub(), new JodaFormatterStub()))
                .put(DateTimeStyle.DATE_PICKER, new DateTimeDatePickerFormatter(new ServiceProviderStub(), new JodaFormatterStub()))
                .put(DateTimeStyle.ISO_8601_DATE_TIME, new DateTimeISO8601DateTimeFormatter(new JodaFormatterStub()))
                .build();
    }

    public DateTimeFormatterFactoryStub relativeDates(boolean useRelativeDates)
    {
        this.useRelativeDates = useRelativeDates;
        return this;
    }

    public DateTimeFormatterFactoryStub jiraTimeZone(DateTimeZone timeZone)
    {
        this.jiraTimeZone = timeZone;
        return this;
    }

    public DateTimeFormatterFactoryStub userTimeZone(DateTimeZone timeZone)
    {
        userTimeZone = timeZone;
        return this;
    }

    public DateTimeFormatterFactoryStub jiraLocale(Locale locale)
    {
        this.jiraLocale = locale;
        return this;
    }

    public DateTimeFormatterFactoryStub userLocale(Locale locale)
    {
        this.userLocale = locale;
        return this;
    }

    @Override
    public DateTimeFormatter formatter()
    {
        return new DateTimeFormatterSupplierStub().getFormatterFor(DateTimeStyle.RELATIVE, constant(userTimeZone), constant(userLocale));
    }

    class JodaFormatterStub implements JodaFormatterSupplier
    {
        @Override
        public org.joda.time.format.DateTimeFormatter get(Key key)
        {
            return DateTimeFormat.forPattern(key.pattern).withLocale(key.locale);
        }
    }

    class ServiceProviderStub implements DateTimeFormatterServiceProvider
    {
        @Override
        public String getDefaultBackedString(String key)
        {
            if (APKeys.JIRA_LF_DATE_TIME.equals(key))
            {
                return "hh:mm a";
            }

            if (APKeys.JIRA_LF_DATE_DAY.equals(key))
            {
                return "EEEE hh:mm a";
            }

            if (APKeys.JIRA_LF_DATE_COMPLETE.equals(key))
            {
                return "dd/MMM/yy hh:mm a";
            }

            if (APKeys.JIRA_LF_DATE_DMY.equals(key))
            {
                return "dd/MMM/yy";
            }

            throw new IllegalArgumentException(key);
        }

        @Override
        public String getUnescapedText(String key)
        {
            if ("common.concepts.today".equals(key))
            {
                return "Today {0}";
            }

            if ("common.concepts.yesterday".equals(key))
            {
                return "Yesterday {0}";
            }

            // if we change the formatters we'll need to update this stub
            throw new IllegalArgumentException();
        }

        @Override
        public String getText(String key, Object... parameters)
        {
            return null;
        }
    }

    private class DateTimeFormatterSupplierStub implements DateTimeFormatterSupplier
    {
        @Override
        public DateTimeFormatter getFormatterFor(DateTimeStyle style, @Nullable Source<DateTimeZone> timeZone, @Nullable Source<Locale> locale)
        {
            DateTimeFormatStrategy strategy = patterns.get(style);
            if (strategy == null)
            {
                throw new IllegalStateException(String.format("%s is not supported by %s", style, DateTimeFormatterFactoryStub.class.getSimpleName()));
            }

            return new DateTimeFormatterImpl(strategy, this, new DateTimeSettingsStub(), jiraAuthenticationContext, timeZone, locale);
        }
    }

    private class DateTimeSettingsStub implements DateTimeSettings
    {
        @Override
        public Locale localeFor(@Nullable User user)
        {
            return user == null ? jiraLocale : userLocale;
        }

        @Override
        public DateTimeZone timeZoneFor(@Nullable User user)
        {
            return user == null ? jiraTimeZone : userTimeZone;
        }
    }

    private class RelativeDatesAnswer implements IAnswer<Boolean>
    {
        @Override
        public Boolean answer() throws Throwable
        {
            return useRelativeDates;
        }
    }
}
