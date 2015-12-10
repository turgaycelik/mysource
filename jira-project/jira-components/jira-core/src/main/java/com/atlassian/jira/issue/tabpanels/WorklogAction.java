package com.atlassian.jira.issue.tabpanels;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueAction;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.NonInjectableComponent;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

@NonInjectableComponent
public class WorklogAction extends AbstractIssueAction
{
    private static final Logger log = Logger.getLogger(WorklogAction.class);

    private final boolean canEditWorklog;
    private final boolean canDeleteWorklog;
    private final Worklog worklog;
    private final JiraDurationUtils jiraDurationUtils;
    private final FieldLayoutManager fieldLayoutManager;
    private final RendererManager rendererManager;
    private final Locale locale;
    private final Issue issue;

    public WorklogAction(IssueTabPanelModuleDescriptor descriptor, Worklog worklog, JiraDurationUtils jiraDurationUtils, boolean canEditWorklog, boolean canDeleteWorklog, final FieldLayoutManager fieldLayoutManager, final RendererManager rendererManager, final Locale locale)
    {
        super(descriptor);
        this.canDeleteWorklog = canDeleteWorklog;
        this.canEditWorklog = canEditWorklog;
        this.worklog = worklog;
        this.jiraDurationUtils = jiraDurationUtils;
        this.fieldLayoutManager = fieldLayoutManager;
        this.rendererManager = rendererManager;
        this.issue = worklog.getIssue();
        this.locale = locale;
    }

    public Date getTimePerformed()
    {
        return worklog.getStartDate();
    }

    protected void populateVelocityParams(Map params)
    {
        params.put("action", this);
        params.put("worklog", this.getWorklog());
        params.put("content", worklog.getComment());

        try
        {
            final FieldLayoutItem fieldLayoutItem = fieldLayoutManager.getFieldLayout(issue).getFieldLayoutItem(IssueFieldConstants.WORKLOG);
            if (fieldLayoutItem != null)
            {
                params.put("content", rendererManager.getRenderedContent(fieldLayoutItem.getRendererType(), worklog.getComment(), issue.getIssueRenderContext()));
            }
        }
        catch (DataAccessException e)
        {
            log.error(e);
        }
    }

    //-------------------------------------------------------------------------------- Methods used by velocity template

    public String getPrettyDuration(String duration)
    {
        return jiraDurationUtils.getFormattedDuration(new Long(duration), locale);
    }

    public Worklog getWorklog()
    {
        return worklog;
    }
    
    public boolean isCanDeleteWorklog()
    {
        return canDeleteWorklog;
    }

    public boolean isCanEditWorklog()
    {
        return canEditWorklog;
    }
}
