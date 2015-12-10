package com.atlassian.jira.issue.tabpanels;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.action.IssueActionComparator;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class WorklogTabPanel extends AbstractIssueTabPanel
{
    private final WorklogService worklogService;
    private final JiraDurationUtils jiraDurationUtils;
    private final FieldLayoutManager fieldLayoutManager;
    private final RendererManager rendererManager;
    private final ApplicationProperties applicationProperties;
    private final FieldVisibilityManager fieldVisibilityManager;

    public WorklogTabPanel(final WorklogService worklogService, final JiraDurationUtils jiraDurationUtils,
            final FieldLayoutManager fieldLayoutManager, final RendererManager rendererManager,
            final ApplicationProperties applicationProperties, final FieldVisibilityManager fieldVisibilityManager)
    {
        this.worklogService = worklogService;
        this.jiraDurationUtils = jiraDurationUtils;
        this.fieldLayoutManager = fieldLayoutManager;
        this.rendererManager = rendererManager;
        this.applicationProperties = applicationProperties;
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    @Override
    public List<IssueAction> getActions(Issue issue, User remoteUser)
    {
        JiraServiceContextImpl context = new JiraServiceContextImpl(remoteUser, new SimpleErrorCollection());
        List<Worklog> userWorklogs = worklogService.getByIssueVisibleToUser(context, issue);
        List<IssueAction> worklogs = Lists.newArrayList();
        final Locale userLocale = context.getI18nBean().getLocale();

        for (Worklog userWorklog : userWorklogs)
        {
            boolean canEditWorklog = worklogService.hasPermissionToUpdate(context, userWorklog);
            boolean canDeleteWorklog = worklogService.hasPermissionToDelete(context, userWorklog);
            worklogs.add(new WorklogAction(descriptor, userWorklog, jiraDurationUtils, canEditWorklog, canDeleteWorklog, fieldLayoutManager, rendererManager, userLocale));
        }

        // This is a bit of a hack to indicate that there are no comments to display
        if (worklogs.isEmpty())
        {
            IssueAction action = new GenericMessageAction(descriptor.getI18nBean().getText("viewissue.nowork"));
            return Lists.newArrayList(action);
        }

        Collections.sort(worklogs, IssueActionComparator.COMPARATOR);
        return worklogs;
    }

    @Override
    public boolean showPanel(Issue issue, User remoteUser)
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING)
                && fieldVisibilityManager.isFieldVisible(IssueFieldConstants.TIMETRACKING, issue);
    }
}
