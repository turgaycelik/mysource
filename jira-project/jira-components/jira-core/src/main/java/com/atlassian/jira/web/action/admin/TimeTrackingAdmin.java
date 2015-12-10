/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.config.CoreFeatures;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.option.TextOption;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

@WebSudoRequired
public class TimeTrackingAdmin extends JiraWebActionSupport
{
    private static final Long SAMPLE_DURATION = 131400L; // 36.5 hours

    private final ApplicationProperties applicationProperties;
    private final FieldManager fieldManager;
    private final JiraDurationUtils jiraDurationUtils;
    private final JiraAuthenticationContext authenticationContext;
    private final PluginAccessor pluginAccessor;
    private final ReindexMessageManager reindexMessageManager;
    private final FeatureManager featureManager;
    private String hoursPerDay;
    private String daysPerWeek;
    private String timeTrackingFormat;
    private boolean legacyModeGetter;
    private boolean legacyModeSetter;
    private boolean isCopyComment;
    private DateUtils.Duration defaultUnit;

    public TimeTrackingAdmin(final ApplicationProperties applicationProperties, final FieldManager fieldManager, final JiraDurationUtils jiraDurationUtils, final JiraAuthenticationContext authenticationContext, final PluginAccessor pluginAccessor, final ReindexMessageManager reindexMessageManager, final FeatureManager featureManager)
    {
        this.applicationProperties = applicationProperties;
        this.fieldManager = fieldManager;
        this.jiraDurationUtils = jiraDurationUtils;
        this.authenticationContext = authenticationContext;
        this.pluginAccessor = pluginAccessor;
        this.reindexMessageManager = notNull("reindexMessageManager", reindexMessageManager);
        this.featureManager = featureManager;
        hoursPerDay = applicationProperties.getDefaultBackedString(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY);
        daysPerWeek = applicationProperties.getDefaultBackedString(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK);
        timeTrackingFormat = applicationProperties.getDefaultBackedString(APKeys.JIRA_TIMETRACKING_FORMAT);
        try
        {
            defaultUnit = DateUtils.Duration.valueOf(applicationProperties.getDefaultBackedString(APKeys.JIRA_TIMETRACKING_DEFAULT_UNIT));
        }
        catch (IllegalArgumentException e)
        {
            defaultUnit = DateUtils.Duration.MINUTE;
        }
        catch (NullPointerException e)
        {
            defaultUnit = DateUtils.Duration.MINUTE;
        }
    }

    public boolean isTimeTracking()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
    }

    /**
     * JRA-13728: If Log Work module is disabled, we will show a warning in the JSP informing the user that Log Work
     * operations will not be accessible
     *
     * @return true if the module is enabled, false otherwise
     */
    public boolean isLogWorkModuleEnabled()
    {
        return pluginAccessor.isPluginModuleEnabled("com.atlassian.jira.plugin.system.issueoperations:log-work");
    }

    /**
     * JRA-13728: If Issue Operations plugin is disabled, we will show a warning in the JSP informing the user that Log
     * Work operations will not be accessible
     *
     * @return true if the plugin is enabled, false otherwise
     */
    public boolean isIssueOperationsPluginEnabled()
    {
        return pluginAccessor.isPluginEnabled("com.atlassian.jira.plugin.system.issueoperations");
    }

    public boolean isOnDemand()
    {
        return featureManager.isOnDemand();
    }

    @Override
    public String doDefault() throws Exception
    {
        legacyModeSetter = false;
        legacyModeGetter = applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING_ESTIMATES_LEGACY_BEHAVIOUR);
        isCopyComment = applicationProperties.getOption(APKeys.JIRA_TIMETRACKING_COPY_COMMENT_TO_WORK_DESC_ON_TRANSITION);
        return INPUT;
    }

    @RequiresXsrfCheck
    public String doDeactivate() throws Exception
    {
        if (isTimeTracking())
        {
            applicationProperties.setOption(APKeys.JIRA_OPTION_TIMETRACKING, false);
            fieldManager.refresh();
        }

        return getRedirect("TimeTrackingAdmin!default.jspa");
    }

    @RequiresXsrfCheck
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="RV_RETURN_VALUE_IGNORED", justification="We ignore the return value of setScale on purpose. All we need to know is it it will trigger an exception.")
    public String doActivate() throws Exception
    {
        if (!isTimeTracking())
        {
            // perform validation on some of the fields
            try
            {
                final BigDecimal hoursPerDay = new BigDecimal(getHoursPerDay());
                //noinspection ResultOfMethodCallIgnored
                hoursPerDay.setScale(2, RoundingMode.UNNECESSARY);
                if (hoursPerDay.equals(BigDecimal.ZERO) || !hoursPerDay.abs().equals(hoursPerDay))
                {
                    addError("hoursPerDay", getText("admin.globalsettings.timetracking.error.notpositive"));
                    return ERROR;
                }

                // Checks to make sure that the hours per day can resolve into a whole number of minutes. JRA-22329
                try {
                    hoursPerDay.multiply(BigDecimal.valueOf(60)).intValueExact();
                } catch (ArithmeticException e) {
                    addError("hoursPerDay", getText("admin.globalsettings.timetracking.error.invalidhoursperday"));
                    return ERROR;
                }
            }
            catch (NumberFormatException e)
            {
                addError("hoursPerDay", getText("admin.globalsettings.timetracking.error.invalidformat"));
                return ERROR;
            }
            catch (ArithmeticException e)
            {
                addError("hoursPerDay", getText("admin.globalsettings.timetracking.error.toomuchprecision"));
                return ERROR;
            }

            try
            {
                final BigDecimal daysPerWeek = new BigDecimal(getDaysPerWeek());
                //noinspection ResultOfMethodCallIgnored
                daysPerWeek.setScale(2, RoundingMode.UNNECESSARY);
                if (daysPerWeek.equals(BigDecimal.ZERO) || !daysPerWeek.abs().equals(daysPerWeek))
                {
                    addError("daysPerWeek", getText("admin.globalsettings.timetracking.error.notpositive"));
                    return ERROR;
                }
            }
            catch (NumberFormatException e)
            {
                addError("daysPerWeek", getText("admin.globalsettings.timetracking.error.invalidformat"));
                return ERROR;
            }
            catch (ArithmeticException e)
            {
                addError("daysPerWeek", getText("admin.globalsettings.timetracking.error.toomuchprecision"));
                return ERROR;
            }

            applicationProperties.setOption(APKeys.JIRA_OPTION_TIMETRACKING, true);
            applicationProperties.setString(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY, getHoursPerDay());
            applicationProperties.setString(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK, getDaysPerWeek());
            applicationProperties.setString(APKeys.JIRA_TIMETRACKING_FORMAT, getTimeTrackingFormat());
            applicationProperties.setString(APKeys.JIRA_TIMETRACKING_DEFAULT_UNIT, getDefaultUnit().toUpperCase());
            applicationProperties.setOption(APKeys.JIRA_OPTION_TIMETRACKING_ESTIMATES_LEGACY_BEHAVIOUR, legacyModeSetter);
            applicationProperties.setOption(APKeys.JIRA_TIMETRACKING_COPY_COMMENT_TO_WORK_DESC_ON_TRANSITION, isCopyComment);

            jiraDurationUtils.updateFormatters(applicationProperties, authenticationContext);
            fieldManager.refresh();

            // since we are enabling time tracking, we also need to add a reindex message
            reindexMessageManager.pushMessage(getLoggedInUser(), "admin.notifications.task.timetracking");
        }

        return getRedirect("TimeTrackingAdmin!default.jspa");
    }

    public String getHoursPerDay()
    {
        return hoursPerDay;
    }

    public void setHoursPerDay(final String hoursPerDay)
    {
        this.hoursPerDay = hoursPerDay;
    }

    public String getDefaultUnit()
    {
        return defaultUnit.toString().toUpperCase();
    }

    public String getDefaultUnitText()
    {
        final Map<DateUtils.Duration, String> map = MapBuilder.<DateUtils.Duration, String>newBuilder()
                .add(DateUtils.Duration.MINUTE, getText("core.dateutils.minute"))
                .add(DateUtils.Duration.HOUR, getText("core.dateutils.hour"))
                .add(DateUtils.Duration.DAY, getText("core.dateutils.day"))
                .add(DateUtils.Duration.WEEK, getText("core.dateutils.week")).toMap();
        return map.get(defaultUnit);
    }

    public void setDefaultUnit(final String unit)
    {
        try
        {
            defaultUnit = DateUtils.Duration.valueOf(unit.toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            defaultUnit = DateUtils.Duration.MINUTE;
        }
        catch (NullPointerException e)
        {
            defaultUnit = DateUtils.Duration.MINUTE;
        }
    }

    public String getDaysPerWeek()
    {
        return daysPerWeek;
    }

    public void setDaysPerWeek(final String daysPerWeek)
    {
        this.daysPerWeek = daysPerWeek;
    }

    public Map<String, String> getUnits()
    {
        return MapBuilder.<String, String>newBuilder()
                .add(DateUtils.Duration.MINUTE.toString().toUpperCase(), getText("core.dateutils.minute"))
                .add(DateUtils.Duration.HOUR.toString().toUpperCase(), getText("core.dateutils.hour"))
                .add(DateUtils.Duration.DAY.toString().toUpperCase(), getText("core.dateutils.day"))
                .add(DateUtils.Duration.WEEK.toString().toUpperCase(), getText("core.dateutils.week")).toListOrderedMap();
    }

    public List<TextOption> getTimeTrackingFormats()
    {
        try
        {
            final BigDecimal hoursPerDay = new BigDecimal(getHoursPerDay());
            final BigDecimal daysPerWeek = new BigDecimal(getDaysPerWeek());
            final I18nHelper i18nBean = authenticationContext.getI18nHelper();
            final String durationPretty = new JiraDurationUtils.PrettyDurationFormatter(hoursPerDay, daysPerWeek, i18nBean).format(SAMPLE_DURATION);
            final String durationDays = new JiraDurationUtils.DaysDurationFormatter(hoursPerDay, i18nBean).format(SAMPLE_DURATION);
            final String durationHours = new JiraDurationUtils.HoursDurationFormatter(i18nBean).format(SAMPLE_DURATION);
            return CollectionBuilder.newBuilder(
                    new TextOption(JiraDurationUtils.FORMAT_PRETTY, getText(JiraDurationUtils.PrettyDurationFormatter.KEY_FORMAT_PRETTY, durationPretty)),
                    new TextOption(JiraDurationUtils.FORMAT_DAYS, getText(JiraDurationUtils.DaysDurationFormatter.KEY_FORMAT_DAYS, durationDays)),
                    new TextOption(JiraDurationUtils.FORMAT_HOURS, getText(JiraDurationUtils.HoursDurationFormatter.KEY_FORMAT_HOURS, durationHours))).asList();
        }
        catch (NumberFormatException ignored)
        {
        }
        catch (ArithmeticException ignored)
        {
        }
        // can be caused by invalid input.  It will be caught above in validation
        return Collections.emptyList();
    }

    public String getTimeTrackingFormatSample()
    {
        return getText(jiraDurationUtils.getI18nKey(), jiraDurationUtils.getFormattedDuration(SAMPLE_DURATION));
    }

    public String getTimeTrackingFormat()
    {
        return timeTrackingFormat;
    }

    public void setTimeTrackingFormat(final String timeTrackingFormat)
    {
        this.timeTrackingFormat = timeTrackingFormat;
    }

    public boolean isLegacyMode()
    {
        return legacyModeGetter;
    }

    public void setLegacyMode(boolean legacyMode)
    {
        this.legacyModeSetter = legacyMode;
    }

    public boolean isCopyComment()
    {
        return isCopyComment;
    }

    public void setCopyComment(final boolean copyComment)
    {
        this.isCopyComment = copyComment;
    }
}
