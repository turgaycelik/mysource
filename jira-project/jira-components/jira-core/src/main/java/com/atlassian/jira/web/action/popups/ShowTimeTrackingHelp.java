package com.atlassian.jira.web.action.popups;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.JiraWebActionSupport;

import java.util.Map;

/**
 * Provides contextual help for time tracking and log work fields.
 *
 * @since v4.2
 */
public class ShowTimeTrackingHelp extends JiraWebActionSupport
{
    private ApplicationProperties applicationProperties;

    public ShowTimeTrackingHelp(final ApplicationProperties applicationProperties, final WorklogService worklogService, final IssueService issueService)
    {
        this.applicationProperties = applicationProperties;
    }

    public boolean isCommentCopiedToWorkDescription()
    {
        return applicationProperties.getOption(APKeys.JIRA_TIMETRACKING_COPY_COMMENT_TO_WORK_DESC_ON_TRANSITION);
    }

    public String getDefaultTimeUnit()
    {
        final Map<String, String> map = MapBuilder.<String, String>newBuilder()
                .add(DateUtils.Duration.MINUTE.name(), getText("core.dateutils.minute"))
                .add(DateUtils.Duration.HOUR.name(), getText("core.dateutils.hour"))
                .add(DateUtils.Duration.DAY.name(), getText("core.dateutils.day"))
                .add(DateUtils.Duration.WEEK.name(), getText("core.dateutils.week")).toMap();
        DateUtils.Duration defaultUnit;
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
        return map.get(defaultUnit.toString());
    }

    public String getDaysPerWeek()                         
    {
        return applicationProperties.getDefaultBackedString(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK);
    }

    public String getHoursPerDay()
    {
        return applicationProperties.getDefaultBackedString(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY);
    }
}
 