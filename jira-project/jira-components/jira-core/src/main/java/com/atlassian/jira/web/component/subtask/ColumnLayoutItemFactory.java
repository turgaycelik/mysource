package com.atlassian.jira.web.component.subtask;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bean.SubTaskBean;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraWebUtils;
import com.atlassian.jira.web.component.SimpleColumnLayoutItem;
import com.opensymphony.util.TextUtils;

import java.util.Map;

/**
 * Factory class used to create specific {@link com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem} object.
 * This replaces SubTaskColumnLayoutFactory to becaome a more generic one.
 *
 * @since 4.0
 */
public class ColumnLayoutItemFactory
{
    private final PermissionManager permissionManager;
    private final VelocityTemplatingEngine templatingEngine;
    private final JiraAuthenticationContext authenticationContext;
    private final XsrfTokenGenerator xsrfTokenGenerator;
    private final I18nHelper.BeanFactory beanFactory;

    public ColumnLayoutItemFactory(final PermissionManager permissionManager, final VelocityTemplatingEngine templatingEngine,
            final JiraAuthenticationContext authenticationContext, final XsrfTokenGenerator xsrfTokenGenerator,
            final I18nHelper.BeanFactory beanFactory)
    {
        this.permissionManager = permissionManager;
        this.templatingEngine = templatingEngine;
        this.authenticationContext = authenticationContext;
        this.xsrfTokenGenerator = xsrfTokenGenerator;
        this.beanFactory = beanFactory;
    }

    /**
     * Create a column for SubTaskView for reordering (displaying reorder arrows)
     *
     * @param user The current user
     * @param parentIssue The parent issue of the subtasks
     * @param subTaskBean The subtask bean containing subtasks in sequence
     * @param subTaskView The view being displayed - {@link com.atlassian.jira.bean.SubTaskBean#SUB_TASK_VIEW_ALL} or {@link com.atlassian.jira.bean.SubTaskBean#SUB_TASK_VIEW_UNRESOLVED}
     * @return The column layout item used to render the reordering column.
     */
    public ColumnLayoutItem getSubTaskReorderColumn(final User user, final Issue parentIssue, final SubTaskBean subTaskBean, final String subTaskView)
    {
        // Note that as we use the display sequence field - we need to create a new one of these for each page, and it can't be cached.
        final I18nHelper i18n = beanFactory.getInstance(user);
        return new SubTaskReorderColumnLayoutItem(permissionManager, subTaskBean, subTaskView, parentIssue, user, i18n);
    }

    /**
     * Creates a ColumnLayoutItem that displays an AJAX dropdown that displays a list of all available actions and operations for that issue.
     *
     * @return The ColumnLayoutItem that displays an AJAX dropdown
     */
    public ColumnLayoutItem getActionsAndOperationsColumn()
    {
        return new ActionsAndOperationsColumnLayoutItem(templatingEngine, authenticationContext, xsrfTokenGenerator);
    }

    /**
     * Displays a simple ColumnLayoutItem that displays a sequence. I.e. 1, 2, 3, 4 ,..
     *
     * @return a simple ColumnLayoutItem that displays a sequence.
     */
    public ColumnLayoutItem getSubTaskDisplaySequenceColumn()
    {
        return new SimpleColumnLayoutItem()
        {
            // this is a bit ugly in that the sequence isn't passed in to us.  We just have to assume the render method is only called once
            int displaySequence = 0;

            @Override
            public String getHtml(Map displayParams, Issue issue)
            {
                if (issue.getResolutionObject() != null)
                    return "<div style=\"padding: 0 18px 0 0; background-image: url( '../images/icons/accept.png' ); background-repeat: no-repeat; background-position: 100% 50%;\">" + ++displaySequence + ".</div>";
                else
                    return "" + ++displaySequence + ".";

            }

            @Override
            protected String getColumnCssClass()
            {
                return "stsequence";
            }
        };
    }
    public ColumnLayoutItem getSubTaskSimpleSummaryColumn()
    {
        return new SimpleColumnLayoutItem()
        {
            // this is a bit ugly in that the sequence isn't passed in to us.  We just have to assume the render method is only called once
            final int displaySequence = 0;
            final String contextPath = JiraWebUtils.getHttpRequest().getContextPath();

            @Override
            public String getHtml(Map displayParams, Issue issue)
            {
                final StringBuilder html = new StringBuilder();

                html.append("<a class='issue-link' data-issue-key='")
                    .append(issue.getKey())
                    .append("' href='")
                    .append(contextPath)
                    .append("/browse/")
                    .append(issue.getKey())
                    .append("'>")
                    .append(TextUtils.htmlEncode(issue.getSummary()))
                    .append("</a>");

                return html.toString();
            }

            @Override
            protected String getColumnCssClass()
            {
                return "stsummary";
            }
        };
    }
}
