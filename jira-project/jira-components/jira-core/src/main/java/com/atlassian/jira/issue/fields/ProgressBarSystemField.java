package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.statistics.LongFieldStatisticsMapper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.TimeTrackingGraphBeanFactory;

/**
 * Field that displays a progress bar in the Navigator
 */
public class ProgressBarSystemField extends AbstractProgressBarSystemField
{
    private static final String PROGRESS_BAR_NAME = "common.concepts.progress.bar";
    private final TimeTrackingGraphBeanFactory factory;

    public ProgressBarSystemField(VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties,
            JiraAuthenticationContext authenticationContext, TimeTrackingGraphBeanFactory factory)
    {
        super(IssueFieldConstants.PROGRESS, PROGRESS_BAR_NAME, PROGRESS_BAR_NAME, templatingEngine, applicationProperties, authenticationContext);
        this.factory = factory;
    }

    @Override
    protected TimeTrackingParameters getTimeTrackingGraphBeanParameters(final Issue issue, final I18nHelper helper)
    {
        return new TimeTrackingParameters(issue.getTimeSpent(), issue.getOriginalEstimate(), issue.getEstimate(),
                factory.createBean(issue, TimeTrackingGraphBeanFactory.Style.NORMAL, helper));
    }

    /**
     * Always returns 'dpb'.
     *
     * @return always returns 'dpb'
     */
    protected String getDisplayId()
    {
        return "dpb";
    }

    /**
     * Returns {@link LongFieldStatisticsMapper#PROGRESS} reference
     * @return {@link LongFieldStatisticsMapper#PROGRESS} reference
     */
    public LuceneFieldSorter getSorter()
    {
        return LongFieldStatisticsMapper.PROGRESS;
    }
}
