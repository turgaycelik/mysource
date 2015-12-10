package com.atlassian.jira.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CachedReference;
import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.DurationUtils;
import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.security.JiraAuthenticationContext;

import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Util class reponsible for printing durations in various formats. <p> Note that this class uses the Duration
 * formatting as configured for time-tracking and is therefore quite specific. For more generic duration formatting see
 * {@link com.atlassian.core.util.DateUtils} </p>
 */
public class JiraDurationUtils implements Startable
{
    private static final Logger log = Logger.getLogger(JiraDurationUtils.class);
    private final TimeTrackingConfiguration timeTrackingConfiguration;
    private final EventPublisher eventPublisher;

    /**
     * pretty formatter aplication property value
     */
    public static final String FORMAT_PRETTY = "pretty";

    /**
     * hours formatter application property value
     */
    public static final String FORMAT_HOURS = "hours";

    /**
     * days formatter application property value
     */
    public static final String FORMAT_DAYS = "days";

    /**
     * resource key for day unit
     */
    private static final String UNIT_DAY = "core.durationutils.unit.day";

    /**
     * resource key for hour unit
     */
    private static final String UNIT_HOUR = "core.durationutils.unit.hour";

    /**
     * resource key for minute unit.
     */
    private static final String UNIT_MINUTE = "core.durationutils.unit.minute";

    /** Map of duration tokens.  For now these are in English Only. */
    private static final Map<String, DateUtils.Duration> TOKEN_MAP = new HashMap<String, DateUtils.Duration>();
    static
    {
        TOKEN_MAP.put("w", DateUtils.Duration.WEEK);
        TOKEN_MAP.put("d", DateUtils.Duration.DAY);
        TOKEN_MAP.put("h", DateUtils.Duration.HOUR);
        TOKEN_MAP.put("m", DateUtils.Duration.MINUTE);
    }

    /**
     * duration formatter currently in use
     */
    protected CachedReference<DurationFormatter> formatterRef;

    /**
     * Sets the duration formatter based on the settings in the application properties and authentication context
     *
     * @param applicationProperties application properties
     * @param authenticationContext the context of the logged in user, used to get an I18nHelper appropriate for the
     * user
     * @param timeTrackingConfiguration the current time tracking configuration
     * @param eventPublisher event publisher so the duration utils can be notified when an import occurs.
     * @param i18nFactory the creator of i18nBean classes.
     *
     * @deprecated Use alternate constructor
     *    {@link #JiraDurationUtils(ApplicationProperties, JiraAuthenticationContext, TimeTrackingConfiguration, EventPublisher, I18nHelper.BeanFactory, CacheManager)}
     */
    public JiraDurationUtils(final ApplicationProperties applicationProperties, final JiraAuthenticationContext authenticationContext,
            final TimeTrackingConfiguration timeTrackingConfiguration, final EventPublisher eventPublisher,
            final I18nHelper.BeanFactory i18nFactory)
    {
        this(applicationProperties, authenticationContext, timeTrackingConfiguration, eventPublisher, i18nFactory,
                ComponentAccessor.getComponent(CacheManager.class));
    }

    /**
     * Sets the duration formatter based on the settings in the application properties and authentication context
     *
     * @param applicationProperties application properties
     * @param authenticationContext the context of the logged in user, used to get an I18nHelper appropriate for the
     * user
     * @param timeTrackingConfiguration the current time tracking configuration
     * @param eventPublisher event publisher so the duration utils can be notified when an import occurs.
     * @param i18nFactory the creator of i18nBean classes.
     * @param cacheManager the cache manager
     *
     * @since v6.2
     */
    public JiraDurationUtils(final ApplicationProperties applicationProperties, final JiraAuthenticationContext authenticationContext,
            final TimeTrackingConfiguration timeTrackingConfiguration, final EventPublisher eventPublisher,
            final I18nHelper.BeanFactory i18nFactory, CacheManager cacheManager)
    {
        this.timeTrackingConfiguration = timeTrackingConfiguration;
        this.eventPublisher = eventPublisher;
        this.formatterRef = cacheManager.getCachedReference(JiraDurationUtils.class,  "formatterRef",
                new com.atlassian.cache.Supplier<DurationFormatter>()
                    {
                        @Override
                        public DurationFormatter get()
                        {
                            final BigDecimal hoursPerDay = timeTrackingConfiguration.getHoursPerDay();
                            final BigDecimal daysPerWeek = timeTrackingConfiguration.getDaysPerWeek();
                            final String format = applicationProperties.getDefaultBackedString(APKeys.JIRA_TIMETRACKING_FORMAT);
                            final AuthContextI18nLocator i18nLocator = new AuthContextI18nLocator(i18nFactory, authenticationContext);

                            if (FORMAT_HOURS.equals(format))
                            {
                                return new HoursDurationFormatter(i18nLocator);
                            }
                            else if (FORMAT_DAYS.equals(format))
                            {
                                return new DaysDurationFormatter(hoursPerDay, i18nLocator);
                            }
                            else if (FORMAT_PRETTY.equals(format))
                            {
                                return new PrettyDurationFormatter(hoursPerDay, daysPerWeek, i18nLocator);
                            }
                            else
                            {
                                log.warn("Duration format not configured! Please set the " + APKeys.JIRA_TIMETRACKING_FORMAT + " property");
                                return new PrettyDurationFormatter(hoursPerDay, daysPerWeek, i18nLocator);
                            }
                        }
                    });
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        formatterRef.reset();
    }

    /**
     * Sets the duration formatter with a new instance of a particular formatter chosen based on the settings in the
     * application properties and authentication context
     *
     * @param applicationProperties application properties
     * @param authenticationContext authentication context
     */
    public void updateFormatters(ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext)
    {
        formatterRef.reset();
    }

    /**
     * Returns i18n resource key for the current formatter
     *
     * @return i18n resource key
     */
    public String getI18nKey()
    {
        return formatterRef.get().getI18nKey();
    }

    /**
     * Formats time duration with default (system) locale
     *
     * @param duration time duration to format
     * @return formatted time duration
     */
    public String getFormattedDuration(Long duration)
    {
        return formatterRef.get().format(duration);
    }

    /**
     * Formats time duration with given locale
     *
     * @param duration time duration to format
     * @param locale user's locale
     * @return formatted time duration
     */
    public String getFormattedDuration(Long duration, Locale locale)
    {
        return formatterRef.get().format(duration, locale);
    }

    /**
     * Formats time duration in the most compact way possible.
     *
     * @param duration time duration to format
     * @return formatted time duration
     */
    public String getShortFormattedDuration(Long duration)
    {
        return formatterRef.get().shortFormat(duration);
    }

    /**
     * Formats time duration in the most compact way possible.
     *
     * @param duration time in seconds
     * @param locale locale to use.  This is used only for translation, not for decimal formatting.
     * @return formatted time duration
     */
    public String getShortFormattedDuration(final Long duration, final Locale locale)
    {
        return formatterRef.get().shortFormat(duration, locale);
    }

    /**
     * Turn a duration string into the number of seconds that it represents, taking into account JIRA's configuration
     * (i.e. how many hours are in a day, how many days are in a week, etc)
     *
     * @param duration string in JIRA's duration format (i.e. "20h")
     * @param locale Locale to use when interpreting the duration string
     * @return number of seconds in the duration string
     * @throws InvalidDurationException when the duration cannot be parsed
     */
    public Long parseDuration(final String duration, Locale locale) throws InvalidDurationException
    {
        //Retrieve the number of hours per day and number of days per week
        final BigDecimal hoursPerDay = timeTrackingConfiguration.getHoursPerDay();
        final BigDecimal daysPerWeek = timeTrackingConfiguration.getDaysPerWeek();
        final BigDecimal secondsPerHour = BigDecimal.valueOf(DateUtils.Duration.HOUR.getSeconds());
        final long secondsPerDay = hoursPerDay.multiply(secondsPerHour).longValueExact();
        final long secondsPerWeek = daysPerWeek.multiply(hoursPerDay).multiply(secondsPerHour).longValueExact();
        return DurationUtils.getDurationSeconds(duration, secondsPerDay, secondsPerWeek, timeTrackingConfiguration.getDefaultUnit(), locale, TOKEN_MAP);
    }

    /**
     * Turn a duration string into the number of seconds that it represents, taking into account JIRA's configuration
     * (i.e. how many hours are in a day, how many days are in a week, etc)
     *
     * This uses the default locale of Locale.UK and is retained for backwards compatibility
     *
     * @param duration string in JIRA's duration format (i.e. "20h")
     * @return number of seconds in the duration string
     * @throws InvalidDurationException when the duration cannot be parsed
     * @deprecated since v4.4. Use {@link #parseDuration(String location, Locale locale)} instead.
     */
    public Long parseDuration(final String duration) throws InvalidDurationException
    {
        return parseDuration(duration, Locale.UK);
    }

    /**
     * This interface defines methods for formatting time duration
     */
    public interface DurationFormatter
    {
        String getI18nKey();

        /**
         * Formats a given time duration with default (system) locale.
         *
         * @param duration time duration to format
         * @return formatted time duration
         */
        String format(Long duration);

        /**
         * Formats a given time duration with given locale.
         *
         * @param duration time duration to format
         * @param locale user's locale
         * @return formatted time duration
         */
        String format(Long duration, Locale locale);

        /**
         * Formats a given time duration with given locale in the most compact way possible.
         *
         * @param duration time duration to format
         * @return formatted time duration
         */
        String shortFormat(Long duration);

        /**
         * Formats a given time duration with the given locale in the most compact way possible
         *
         * @param duration time duration in seconds
         * @param locale locale to use
         * @return formatted time duration
         */
        String shortFormat(Long duration, Locale locale);
    }

    /**
     * This formatter formats time duration to "pretty" format, such as 3 weeks, 2 days, 1 hour, 15 minutes. This value
     * is formatted according to system or user's locale.
     */
    public static class PrettyDurationFormatter implements DurationFormatter
    {
        public static final String KEY_FORMAT_PRETTY = "admin.globalsettings.timetracking.format.pretty";

        private final BigDecimal hoursPerDay;
        private final BigDecimal daysPerWeek;
        private final I18nLocator locator;

        public PrettyDurationFormatter(int hoursPerDay, int daysPerWeek, I18nHelper i18nBean)
        {
            this(BigDecimal.valueOf(hoursPerDay), BigDecimal.valueOf(daysPerWeek), i18nBean);
        }

        public PrettyDurationFormatter(final BigDecimal hoursPerDay, final BigDecimal daysPerWeek, final I18nHelper i18nBean)
        {
            this (hoursPerDay, daysPerWeek, new FixedI18nLocator(i18nBean));
        }

        private PrettyDurationFormatter(final BigDecimal hoursPerDay, final BigDecimal daysPerWeek, final I18nLocator locator)
        {
            this.hoursPerDay = hoursPerDay;
            this.daysPerWeek = daysPerWeek;
            this.locator = locator;
        }

        public String getI18nKey()
        {
            return KEY_FORMAT_PRETTY;
        }

        public String format(Long duration)
        {
            return format(duration, locator.getHelper().getDefaultResourceBundle());
        }

        public String format(Long duration, Locale locale)
        {
            return format(duration, locator.getHelper(locale).getResourceBundle());
        }

        private String format(final Long duration, final ResourceBundle resourceBundle)
        {
            final BigDecimal secondsPerHour = BigDecimal.valueOf(DateUtils.Duration.HOUR.getSeconds());
            final long secondsPerDay = hoursPerDay.multiply(secondsPerHour).longValueExact();
            final long secondsPerWeek = daysPerWeek.multiply(hoursPerDay).multiply(secondsPerHour).longValueExact();
            return getDurationPrettySeconds(duration, resourceBundle, secondsPerDay, secondsPerWeek);
        }

        @VisibleForTesting
        String getDurationPrettySeconds(final Long duration, final ResourceBundle resourceBundle,
                final long secondsPerDay, final long secondsPerWeek)
        {
            return DateUtils.getDurationPrettySeconds(duration, secondsPerDay, secondsPerWeek, resourceBundle);
        }

        /**
         * Used by the Time Tracking report to show shorter durations than full pretty format.
         *
         * @return a duration String in the format "1d 3h 30m".
         */
        public String shortFormat(Long duration)
        {
            return shortFormat(duration, Locale.UK);
        }

        public String shortFormat(final Long duration, final Locale locale)
        {
            final BigDecimal secondsPerHour = BigDecimal.valueOf(DateUtils.Duration.HOUR.getSeconds());
            final long secondsPerDay = hoursPerDay.multiply(secondsPerHour).longValueExact();
            final long secondsPerWeek = daysPerWeek.multiply(hoursPerDay).multiply(secondsPerHour).longValueExact();
            return DateUtils.getDurationStringSeconds(duration, secondsPerDay, secondsPerWeek);
        }

        public BigDecimal getHoursPerDay()
        {
            return hoursPerDay;
        }

        public BigDecimal getDaysPerWeek()
        {
            return daysPerWeek;
        }
    }

    /**
     * This formatter formats time duration to hours only. The resulting string returned is a real number representing
     * the number of hours. This value is then formatted according to system or user's locale.
     */
    public static class HoursDurationFormatter implements DurationFormatter
    {
        public static final String KEY_FORMAT_HOURS = "admin.globalsettings.timetracking.format.hours";

        private final I18nLocator locator;

        public HoursDurationFormatter(I18nHelper i18nBean)
        {
            this.locator = new FixedI18nLocator(i18nBean);
        }

        private HoursDurationFormatter(I18nLocator locator)
        {
            this.locator = locator;
        }

        public String getI18nKey()
        {
            return KEY_FORMAT_HOURS;
        }

        /**
         * Converts the given time duration into hours and formats it based on the system locale. If the duration given
         * is a negative number, returns an empty string.
         *
         * @param duration time duration to format
         * @return formatted time duration or an empty string
         */
        public String format(Long duration)
        {
            return format(duration, locator.getHelper(), TimeFormatWithPrecision.Scale.DEFAULT);
        }

        /**
         * Converts the given time duration into hours and formats it based on the given user's locale. If the duration
         * given is a negative number, returns an empty string.
         *
         * @param duration time duration to format
         * @param locale user's locale
         * @return formatted time duration or an empty string
         */
        public String format(Long duration, Locale locale)
        {
            return format(duration, locator.getHelper(locale), TimeFormatWithPrecision.Scale.DEFAULT);
        }

        public String shortFormat(Long duration)
        {
            return format(duration);
        }

        public String shortFormat(final Long duration, final Locale locale)
        {
            return format(duration, locale);
        }

        /**
         * Converts the given time duration into hours and formats it based on the given i18n bean. If the duration
         * given is a negative number, returns an empty string.
         *
         * @param duration time duration to format
         * @param i18nBean i18n bean
         * @param scale scale to use when when formatting fractional units
         * @return formatted time duration or an empty string
         */
        String format(Long duration, I18nHelper i18nBean, final TimeFormatWithPrecision.Scale scale)
        {
            if (duration >= 0)
            {
                Locale userLocale = locator.getHelper().getLocale();
                final TimeFormatWithPrecision result = calculateFractionalTimeUnit(duration, (int) DateUtils.Duration.HOUR.getSeconds(), i18nBean, UNIT_HOUR, scale, userLocale);
                if (result.remainder == 0)
                {
                    return result.formatted;
                }
                else
                {
                    if (StringUtils.isEmpty(result.formatted))
                    {
                        return calculateFractionalTimeUnit(result.remainder, (int) DateUtils.Duration.MINUTE.getSeconds(), i18nBean, UNIT_MINUTE, scale, userLocale).formatted;
                    }
                    else
                    {
                        return result.formatted + ' ' + calculateFractionalTimeUnit(result.remainder, (int) DateUtils.Duration.MINUTE.getSeconds(), i18nBean, UNIT_MINUTE, scale, userLocale).formatted;
                    }
                }
            }
            return "";
        }
    }

    /**
     * This formatter formats time duration to days only. The resulting string returned is a real number representing
     * the number of days. This value is then formatted according to system or user's locale.
     */
    public static class DaysDurationFormatter implements DurationFormatter
    {
        public static final String KEY_FORMAT_DAYS = "admin.globalsettings.timetracking.format.days";

        private final BigDecimal hoursPerDay;
        private final I18nLocator locator;

        /**
         * @param hoursPerDay numbers of hours in a day
         * @param i18nBean i18nHelper to translate strings
         * @deprecated #DaysDurationFormatter(BigDecimal, I18nHelper) is preferred now
         * that we can fractional number of hours per day
         */
        public DaysDurationFormatter(int hoursPerDay, I18nHelper i18nBean)
        {
            this(BigDecimal.valueOf(hoursPerDay), i18nBean);
        }

        public DaysDurationFormatter(final BigDecimal hoursPerDay, I18nHelper i18nBean)
        {
            this(hoursPerDay, new FixedI18nLocator(i18nBean));
        }

        private DaysDurationFormatter(final BigDecimal hoursPerDay, I18nLocator locator)
        {
            this.hoursPerDay = hoursPerDay;
            this.locator = locator;
        }

        public String getI18nKey()
        {
            return KEY_FORMAT_DAYS;
        }

        /**
         * Converts the given time duration into days and formats it based on the system locale. If the duration given
         * is a negative number, returns an empty string.
         *
         * @param duration time duration to format
         * @return formatted time duration or an empty string
         */
        public String format(Long duration)
        {
            return format(duration, locator.getHelper(), TimeFormatWithPrecision.Scale.DEFAULT);
        }

        /**
         * Converts the given time duration into days and formats it based on the given user's locale. If the duration
         * given is a negative number, returns an empty string.
         *
         * @param duration time duration to format
         * @param locale user's locale
         * @return formatted time duration or an empty string
         */
        public String format(Long duration, Locale locale)
        {
            return format(duration, locator.getHelper(locale), TimeFormatWithPrecision.Scale.DEFAULT);
        }

        public String shortFormat(Long duration)
        {
            return format(duration);
        }

        public String shortFormat(final Long duration, final Locale locale)
        {
            return format(duration, locator.getHelper(locale), TimeFormatWithPrecision.Scale.DEFAULT);
        }

        public BigDecimal getHoursPerDay()
        {
            return hoursPerDay;
        }

        /**
         * Converts the given time duration into days and formats it based on the given i18n bean. If the duration given
         * is a negative number, returns an empty string.
         *
         * @param duration time duration to format
         * @param i18nBean i18n bean
         * @param scale scale to use when when formatting fractional units
         * @return formatted time duration or an empty string
         */
        private String format(Long duration, I18nHelper i18nBean, final TimeFormatWithPrecision.Scale scale)
        {
            if (duration >= 0)
            {
                Locale userLocale = locator.getHelper().getLocale();
                final TimeFormatWithPrecision result = calculateFractionalTimeUnit(duration, hoursPerDay.multiply(BigDecimal.valueOf(DateUtils.Duration.HOUR.getSeconds())).intValueExact(), i18nBean, UNIT_DAY, scale, userLocale);
                if (result.remainder == 0)
                {
                    return result.formatted;
                }
                else
                {
                    if (StringUtils.isEmpty(result.formatted))
                    {
                        return new HoursDurationFormatter(locator).format(result.remainder, i18nBean, scale);
                    }
                    else
                    {
                        return result.formatted + ' ' + new HoursDurationFormatter(locator).format(result.remainder, i18nBean, scale);
                    }
                }
            }
            return "";
        }
    }

    /**
     * This is just a tuple that allows us to denote whether the formatting lost precision or not.
     *
     * @see {#calculateFractionalTimeUnit}
     */
    static private final class TimeFormatWithPrecision
    {
        /**
         * The formatted string for this time unit i.e. "12d 14h"
         */
        public final String formatted;

        /**
         * Any remaining seconds that didn't fit into the conversion at the scale requested. e.g. 63 seconds formatted
         * into minutes, with a scale of NO_DECIMALS, would result in the formatted string of "1m" and a remainder "3".
         */
        public final long remainder;

        /**
         * How much scale to use when converting seconds to a string format. For instance given 100 minutes how many
         * hours is that? SCALE    RESULT 0        1 1        1.7 2        1.67 3        1.667
         */
        public enum Scale
        {
            NO_DECIMALS(0),
            DEFAULT(2);

            public final int scale;

            private Scale(final int scale)
            {
                this.scale = scale;
            }
        }

        public TimeFormatWithPrecision(final String formatted, final long remainder)
        {
            this.formatted = formatted;
            this.remainder = remainder;
        }
    }

    /**
     * Format a duration into a decimal fractional string. If the formatting would lose precision (i.e. 11 minutes
     * cannot be represented as an decimal fractional number of hours) then the "lost" fractional units can be found in
     * the TimeFormatWithPrecision
     *
     * @param duration number of seconds
     * @param secondsPerUnit how many seconds occur in each unit of time (i.e. seconds in an hour)
     * @param i18nBean used to format the resulting string
     * @param unitI18nKey which i18nkey denotes this unit of time's format
     * @param scale how much precision to use in the resulting string, left over duration ends up in the remainder
     * @param userLocale
     * @return if the TimeFormatWithPrecision.remainder is 0 then the formatting occurred without loss of data. If
     *         remainer != 0 then you will most likely want to format the remainder in a higher-precision unit
     */
    static TimeFormatWithPrecision calculateFractionalTimeUnit(final Long duration, final int secondsPerUnit, final I18nHelper i18nBean, final String unitI18nKey, final TimeFormatWithPrecision.Scale scale, Locale userLocale)
    {
        final StringBuilder numberFormatString = new StringBuilder("#");
        final String scaleStr = StringUtils.repeat("#", scale.scale);
        if (StringUtils.isNotBlank(scaleStr))
        {
            numberFormatString.append(".").append(scaleStr);
        }

        final String SCALE_STRING = numberFormatString.toString();

        // we don't want to lose anything when we perform our maths
        final BigDecimal durationBD = BigDecimal.valueOf(duration);
        final BigDecimal secondsPerUnitBD = BigDecimal.valueOf(secondsPerUnit);

        final DecimalFormat numberFormat = (DecimalFormat) NumberFormat.getNumberInstance(userLocale);
        try
        {
            Number durationInUnit;
            // scale of 0 is a special case. in BigDecimal it means "unlimited precision" but for us it means "no decimal portion allowed"
            if (scale == TimeFormatWithPrecision.Scale.NO_DECIMALS)
            {
                durationInUnit = durationBD.divide(secondsPerUnitBD).toBigIntegerExact();
            }
            else
            {
                durationInUnit = durationBD.setScale(scale.scale, RoundingMode.UNNECESSARY).divide(secondsPerUnitBD).setScale(scale.scale, RoundingMode.UNNECESSARY);
            }

            final String format = i18nBean.getText(unitI18nKey, numberFormat.format(durationInUnit));
            return new TimeFormatWithPrecision(format, 0);
        }
        catch (ArithmeticException e)
        {
            // can't be divided without losing accuracy at the necessary scale
            // format what we can but store the remainder so our caller can handle it
            int integralUnits = (int) (duration.doubleValue() / secondsPerUnit);
            final long remainder = duration - (integralUnits * secondsPerUnit);
            String unit;
            if (integralUnits == 0)
            {
                unit = "";
            }
            else
            {
                unit = i18nBean.getText(unitI18nKey, numberFormat.format(integralUnits));
            }
            return new TimeFormatWithPrecision(unit, remainder);
        }
    }

    /**
     * Locator interface for a {@link com.atlassian.jira.util.I18nHelper}
     */
    private interface I18nLocator
    {
        I18nHelper getHelper();
        I18nHelper getHelper(Locale locale);
    }

    /**
     * Implementation of {@link com.atlassian.jira.util.JiraDurationUtils.I18nLocator} that uses the {@link com.atlassian.jira.security.JiraAuthenticationContext}
     * to locate the {@link com.atlassian.jira.util.I18nHelper} when the locale is not specified.
     */
    private static class AuthContextI18nLocator implements I18nLocator
    {
        private final I18nHelper.BeanFactory factory;
        private final JiraAuthenticationContext authenticationContext;

        private AuthContextI18nLocator(final I18nHelper.BeanFactory factory, final JiraAuthenticationContext authenticationContext)
        {
            this.factory = factory;
            this.authenticationContext = authenticationContext;
        }

        public I18nHelper getHelper()
        {
            return authenticationContext.getI18nHelper();
        }

        public I18nHelper getHelper(final Locale locale)
        {
            return factory.getInstance(locale);
        }
    }

    /**
     * Implementation of {@link com.atlassian.jira.util.JiraDurationUtils.I18nLocator} that always returns contained
     * {@link com.atlassian.jira.util.I18nHelper}.
     */
    private static class FixedI18nLocator implements I18nLocator
    {
        private final I18nHelper helper;

        public FixedI18nLocator(final I18nHelper helper)
        {
            this.helper = helper;
        }

        public I18nHelper getHelper()
        {
            return helper;
        }

        public I18nHelper getHelper(final Locale locale)
        {
            return helper;
        }
    }
}
