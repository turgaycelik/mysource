package com.atlassian.jira.util;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.ResourceBundle;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.bean.MockI18nBean;

import com.mockobjects.dynamic.Mock;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestJiraDurationUtils
{
    private CacheManager cacheManager;

    @Before
    public void setup()
    {
        cacheManager = new MemoryCacheManager();
    }

    @Test
    public void testPrettyDurationFormatter()
    {
        JiraDurationUtils.PrettyDurationFormatter formatter =
                new JiraDurationUtils.PrettyDurationFormatter(24, 7, new MockI18nBean());

        String duration = formatter.format(new Long(0));
        assertEquals("0 minutes", duration);

        // negatives are not handled
        duration = formatter.format(new Long(-630));
        assertEquals("", duration);

        duration = formatter.format(new Long(630)); // 10m 30s
        assertEquals("10 minutes", duration);

        duration = formatter.format(new Long(3700)); // 1h 1m 40s
        assertEquals("1 hour, 1 minute", duration);

        duration = formatter.format(new Long(90100)); // 1d 1h 1m 40s
        assertEquals("1 day, 1 hour, 1 minute", duration);

        duration = formatter.format(new Long(694900)); // 1w 1d 1h 1m 40s
        assertEquals("1 week, 1 day, 1 hour, 1 minute", duration);
    }

    @Test
    public void testHoursDurationFormatter()
    {
        JiraDurationUtils.HoursDurationFormatter formatter = new JiraDurationUtils.HoursDurationFormatter(new MockI18nBean());

        String duration = formatter.format(new Long(0));
        assertEquals("0h", duration);

        // negatives are not handled
        duration = formatter.format(new Long(-630));
        assertEquals("", duration);

        duration = formatter.format(new Long(630)); // 10m 30s
        assertEquals("10.5m", duration);

        duration = formatter.format(new Long(3700)); // 1h 1m 40s
        assertEquals("1h 1m", duration);

        duration = formatter.format(new Long(90100)); // 1d 1h 1m 40s
        assertEquals("25h 1m", duration);

        duration = formatter.format(new Long(694900)); // 1w 1d 1h 1m 40s
        assertEquals("193h 1m", duration);

        assertEquals("5.5h", formatter.format(new Long(19800)));
        assertEquals("7.18h", formatter.format(new Long(25848)));
    }

    @Test
    public void testDaysDurationFormatter8()
    {
        JiraDurationUtils.DaysDurationFormatter formatter = new JiraDurationUtils.DaysDurationFormatter(8, new MockI18nBean());

        String duration = formatter.format(new Long(0));
        assertEquals("0d", duration);

        // negatives are not handled
        duration = formatter.format(new Long(-630));
        assertEquals("", duration);

        duration = formatter.format(new Long(630)); // 10m 30s
        assertEquals("10.5m", duration);

        duration = formatter.format(new Long(3700)); // 1h 1m 40s
        assertEquals("1h 1m", duration);

        duration = formatter.format(new Long(90100)); // 1d 1h 1m 40s
        assertEquals("3d 1h 1m", duration);

        duration = formatter.format(new Long(694900)); // 1w 1d 1h 1m 40s
        assertEquals("24d 1h 1m", duration);
    }

    @Test
    public void testDaysDurationFormatter24()
    {
        JiraDurationUtils.DaysDurationFormatter formatter = new JiraDurationUtils.DaysDurationFormatter(24, new MockI18nBean());
        String duration = formatter.format(new Long(0));
        assertEquals("0d", duration);

        // negatives are not handled
        duration = formatter.format(new Long(-630));
        assertEquals("", duration);

        duration = formatter.format(new Long(630)); // 10m 30s
        assertEquals("10.5m", duration);

        assertEquals("5.5d", formatter.format(new Long(475200)));
        assertEquals("2.25d", formatter.format(new Long(194400)));

        duration = formatter.format(new Long(3700)); // 1h 1m 40s
        assertEquals("1h 1m", duration);

        duration = formatter.format(new Long(90100)); // 1d 1h 1m 40s
        assertEquals("1d 1h 1m", duration);

        duration = formatter.format(new Long(694900)); // 1w 1d 1h 1m 40s
        assertEquals("8d 1h 1m", duration);
    }

    @Test
    public void testJiraDurationUtilsPretty()
    {
        Mock ac = new Mock(JiraAuthenticationContext.class);

        ApplicationProperties ap = new MockApplicationProperties();
        ap.setString(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY, "24");
        ap.setString(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK, "7");
        ap.setString(APKeys.JIRA_TIMETRACKING_FORMAT, JiraDurationUtils.FORMAT_PRETTY);

        final TimeTrackingConfiguration.PropertiesAdaptor timeTrackingConfiguration = new TimeTrackingConfiguration.PropertiesAdaptor(ap);

        JiraDurationUtils jdu = new JiraDurationUtils(ap, (JiraAuthenticationContext) ac.proxy(), timeTrackingConfiguration, null, new MockI18nBean.MockI18nBeanFactory(), cacheManager);
        JiraDurationUtils.PrettyDurationFormatter durationFormatter = (JiraDurationUtils.PrettyDurationFormatter) jdu.formatterRef.get();
        assertEquals(BigDecimal.valueOf(24), durationFormatter.getHoursPerDay());
        assertEquals(BigDecimal.valueOf(7), durationFormatter.getDaysPerWeek());
        ac.verify();
    }

    @Test
    public void testJiraDurationUtilsDays()
    {
        Mock ac = new Mock(JiraAuthenticationContext.class);

        ApplicationProperties ap = new MockApplicationProperties();
        ap.setString(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY, "24");
        ap.setString(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK, "7");
        ap.setString(APKeys.JIRA_TIMETRACKING_FORMAT, JiraDurationUtils.FORMAT_DAYS);
        final TimeTrackingConfiguration.PropertiesAdaptor timeTrackingConfiguration = new TimeTrackingConfiguration.PropertiesAdaptor(ap);
        JiraDurationUtils jdu = new JiraDurationUtils(ap, (JiraAuthenticationContext) ac.proxy(), timeTrackingConfiguration, null, new MockI18nBean.MockI18nBeanFactory(), cacheManager);
        JiraDurationUtils.DaysDurationFormatter daysDurationFormatterLong = (JiraDurationUtils.DaysDurationFormatter) jdu.formatterRef.get();
        assertEquals(BigDecimal.valueOf(24), daysDurationFormatterLong.getHoursPerDay());
        ac.verify();
    }

    @Test
    public void testJiraDurationUtilsHours()
    {
        Mock ac = new Mock(JiraAuthenticationContext.class);

        ApplicationProperties ap = new MockApplicationProperties();
        ap.setString(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY, "24");
        ap.setString(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK, "7");
        ap.setString(APKeys.JIRA_TIMETRACKING_FORMAT, JiraDurationUtils.FORMAT_HOURS);
        final TimeTrackingConfiguration.PropertiesAdaptor timeTrackingConfiguration = new TimeTrackingConfiguration.PropertiesAdaptor(ap);
        JiraDurationUtils jdu = new JiraDurationUtils(ap, (JiraAuthenticationContext) ac.proxy(), timeTrackingConfiguration, null, new MockI18nBean.MockI18nBeanFactory(), cacheManager);
        assertTrue(jdu.formatterRef.get() instanceof JiraDurationUtils.HoursDurationFormatter);
        ac.verify();
    }

    @Test
    public void formattingDurationByLocaleShouldUseEntireResourceBundleNotJustDefault()
    {
        // Set up
        final long duration = 1234;
        final int hoursPerDay = 20;
        final int daysPerWeek = 6;
        final I18nHelper mockI18nHelper = mock(I18nHelper.class);
        final ResourceBundle mockResourceBundle = mock(ResourceBundle.class);
        final String prettyDuration = "Ooh, nice!";
        final JiraDurationUtils.DurationFormatter durationFormatter = new JiraDurationUtils.PrettyDurationFormatter(hoursPerDay, daysPerWeek, mockI18nHelper)
        {
            @Override
            String getDurationPrettySeconds(final Long actualDuration, final ResourceBundle resourceBundle,
                    final long secondsPerDay, final long secondsPerWeek)
            {
                assertSame(mockResourceBundle, resourceBundle);
                assertEquals(duration, actualDuration.longValue());
                assertEquals(hoursPerDay * 3600, secondsPerDay);
                assertEquals(daysPerWeek * secondsPerDay, secondsPerWeek);
                return prettyDuration;
            }
        };
        final Locale locale = Locale.getDefault();
        when(mockI18nHelper.getResourceBundle()).thenReturn(mockResourceBundle);


        // Invoke
        final String actualPrettyDuration = durationFormatter.format(duration, locale);

        // Check
        assertEquals(prettyDuration, actualPrettyDuration);
    }

    @Test
    public void formattingDurationWithoutLocaleShouldUseDefaultResourceBundle()
    {
        // Set up
        final long duration = 1234;
        final int hoursPerDay = 20;
        final int daysPerWeek = 6;
        final I18nHelper mockI18nHelper = mock(I18nHelper.class);
        final ResourceBundle mockResourceBundle = mock(ResourceBundle.class);
        final String prettyDuration = "Ooh, nice!";
        final JiraDurationUtils.DurationFormatter durationFormatter = new JiraDurationUtils.PrettyDurationFormatter(hoursPerDay, daysPerWeek, mockI18nHelper)
        {
            @Override
            String getDurationPrettySeconds(final Long actualDuration, final ResourceBundle resourceBundle,
                    final long secondsPerDay, final long secondsPerWeek)
            {
                assertSame(mockResourceBundle, resourceBundle);
                assertEquals(duration, actualDuration.longValue());
                assertEquals(hoursPerDay * 3600, secondsPerDay);
                assertEquals(daysPerWeek * secondsPerDay, secondsPerWeek);
                return prettyDuration;
            }
        };
        when(mockI18nHelper.getDefaultResourceBundle()).thenReturn(mockResourceBundle);


        // Invoke
        final String actualPrettyDuration = durationFormatter.format(duration);

        // Check
        assertEquals(prettyDuration, actualPrettyDuration);
    }
}
