package com.atlassian.jira.issue.views.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.LinkCollection;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactory;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.bean.TimeTrackingGraphBean;
import com.atlassian.jira.web.bean.TimeTrackingGraphBeanFactory;
import org.apache.log4j.Logger;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Util class for the Issue views. Provides a number of shared methods.
 */
public class DefaultIssueViewUtil implements IssueViewUtil
{
    public static final Logger log = Logger.getLogger(DefaultIssueViewUtil.class);

    private final IssueLinkManager issueLinkManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final RendererManager rendererManager;
    private final JiraDurationUtils jiraDurationUtils;
    private final AggregateTimeTrackingCalculatorFactory aggregateTimeTrackingCalculatorFactory;
    private final TimeTrackingGraphBeanFactory timeTrackingGraphBeanFactory;

    public DefaultIssueViewUtil(final IssueLinkManager issueLinkManager, final FieldLayoutManager fieldLayoutManager,
            final RendererManager rendererManager, final JiraDurationUtils jiraDurationUtils,
            final AggregateTimeTrackingCalculatorFactory aggregateTimeTrackingCalculatorFactory,
            final TimeTrackingGraphBeanFactory timeTrackingGraphBeanFactory)
    {
        this.timeTrackingGraphBeanFactory = notNull("timeTrackingGraphBeanFactory", timeTrackingGraphBeanFactory);
        this.aggregateTimeTrackingCalculatorFactory = notNull("aggregateTimeTrackingCalculatorFactory", aggregateTimeTrackingCalculatorFactory);
        this.issueLinkManager = notNull("issueLinkManager", issueLinkManager);
        this.fieldLayoutManager = notNull("fieldLayoutManager", fieldLayoutManager);
        this.rendererManager = notNull("rendererManager", rendererManager);
        this.jiraDurationUtils = notNull("jiraDurationUtils", jiraDurationUtils);
    }

    /* (non-Javadoc)
     * @see com.atlassian.jira.issue.views.util.IssueViewUtil#getPrettyDuration(java.lang.Long)
     */
    public String getPrettyDuration(final Long v)
    {
        return jiraDurationUtils.getFormattedDuration(v);
    }

    /* (non-Javadoc)
     * @see com.atlassian.jira.issue.views.util.IssueViewUtil#getLinkCollection(com.atlassian.jira.issue.Issue, com.opensymphony.user.User)
     */
    public LinkCollection getLinkCollection(final Issue issue, final User user)
    {
        return issueLinkManager.getLinkCollection(issue, user);
    }

    /* (non-Javadoc)
     * @see com.atlassian.jira.issue.views.util.IssueViewUtil#getRenderedContent(java.lang.String, java.lang.String, com.atlassian.jira.issue.Issue)
     */
    public String getRenderedContent(final String fieldName, final String value, final Issue issue)
    {
        final Project project = issue.getProjectObject();
        final IssueType issueTypeObject = issue.getIssueTypeObject();
        if ((project != null) && (issueTypeObject != null))
        {
            final FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(project, issueTypeObject.getId());
            if (fieldLayout != null)
            {
                final FieldLayoutItem fieldLayoutItem = fieldLayout.getFieldLayoutItem(fieldName);

                if (fieldLayoutItem != null)
                {
                    return rendererManager.getRenderedContent(fieldLayoutItem.getRendererType(), value, issue.getIssueRenderContext());
                }
                else
                {
                    log.debug("fieldLayoutItem was null");
                }
            }
            else
            {
                log.debug("fieldLayout was null!");
            }
        }
        else
        {
            if (project == null)
            {
                log.debug("issue project was null!");
            }
            if (issueTypeObject == null)
            {
                log.debug("issueTypeObject was null!");
            }
        }
        return value;
    }

    public AggregateTimeTrackingBean createAggregateBean(final Issue issue)
    {
        return aggregateTimeTrackingCalculatorFactory.getCalculator(issue).getAggregates(issue);
    }

    public TimeTrackingGraphBean createTimeTrackingBean(AggregateTimeTrackingBean bean, I18nHelper helper)
    {
        return timeTrackingGraphBeanFactory.createBean(bean, TimeTrackingGraphBeanFactory.Style.NORMAL, helper);
    }
}
