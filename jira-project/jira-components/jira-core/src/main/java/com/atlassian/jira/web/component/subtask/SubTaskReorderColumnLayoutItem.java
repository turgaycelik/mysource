package com.atlassian.jira.web.component.subtask;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bean.SubTask;
import com.atlassian.jira.bean.SubTaskBean;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraWebUtils;
import com.atlassian.jira.web.component.SimpleColumnLayoutItem;

import java.util.Collection;
import java.util.Map;

/**
 * This class displays a column which displays arrows for changing the sequence of sub-tasks on the view issue page.
 * <p>
 * Note that a new instance should be created for every view issue page, as it uses sequence numbers which need to be
 * reset after each use.
 */
public class SubTaskReorderColumnLayoutItem extends SimpleColumnLayoutItem
{
    int displaySequence = 0;
    private final String contextPath = JiraWebUtils.getHttpRequest().getContextPath();

    final Collection subTasks;
    private final SubTaskBean subTaskBean;
    private final String subTaskView;
    private final I18nHelper i18n;
    private final boolean allowedReorderSubTasks;
    private final Issue parentIssue;

    public SubTaskReorderColumnLayoutItem(PermissionManager permissionManager, SubTaskBean subTaskBean, String subTaskView, Issue parentIssue, User user, I18nHelper i18n)
    {
        this.subTaskBean = subTaskBean;
        this.subTaskView = subTaskView;
        this.i18n = i18n;
        allowedReorderSubTasks = permissionManager.hasPermission(Permissions.EDIT_ISSUE, parentIssue, user);
        this.parentIssue = parentIssue;
        subTasks = subTaskBean.getSubTasks(subTaskView);
    }

    protected String getColumnCssClass()
    {
        return "streorder";
    }

    public String getHtml(Map displayParams, Issue issue)
    {
        // this is a bit ugly in that the sequence isn't passed in to us.  We just have to assume the render method is only called once
        displaySequence++;
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"subtask-reorder\">");
        if (allowedReorderSubTasks)
        {
            Long subtaskSequence = getCurrentSubTaskSequence(issue, subTasks);
            if (displaySequence != 1)
            {
                html.append("<a class=\"icon icon-sort-up\" href=\"")
                        .append(contextPath)
                        .append("/secure/MoveIssueLink.jspa?id=")
                        .append(parentIssue.getId())
                        .append("&currentSubTaskSequence=")
                        .append(subtaskSequence)
                        .append("&subTaskSequence=")
                        .append(subTaskBean.getPreviousSequence(subtaskSequence, subTaskView))
                        .append("\" title=\"")
                        .append(i18n.getText("admin.workflowdescriptor.move.up"))
                        .append("\"><span>")
                        .append(i18n.getText("admin.workflowdescriptor.move.up"))
                        .append("</span></a>");
            }
            else
            {
                html.append("<img src=\"")
                        .append(contextPath)
                        .append("/images/border/spacer.gif\" class=\"sortArrow\" alt=\"\" />");
            }

            if (displaySequence != subTasks.size())
            {
                html.append("<a class=\"icon icon-sort-down\" href=\"")
                        .append(contextPath)
                        .append("/secure/MoveIssueLink.jspa?id=")
                        .append(parentIssue.getId())
                        .append("&currentSubTaskSequence=")
                        .append(subtaskSequence)
                        .append("&subTaskSequence=")
                        .append(subTaskBean.getNextSequence(subtaskSequence, subTaskView))
                        .append("\" title=\"")
                        .append(i18n.getText("admin.workflowdescriptor.move.down"))
                        .append("\"><span>")
                        .append(i18n.getText("admin.workflowdescriptor.move.down"))
                        .append("</span></a>");
            }
            else
            {
                html.append("<img src=\"")
                        .append(contextPath)
                        .append("/images/border/spacer.gif\" class=\"sortArrow\" alt=\"\" />");
            }
        }
        else
        {
            html.append("&nbsp;");
        }
        html.append("</div>");
        return html.toString();
    }

    private Long getCurrentSubTaskSequence(Issue issue, Collection subTasks)
    {
        for (final Object subTask1 : subTasks)
        {
            SubTask subTask = (SubTask) subTask1;
            if (subTask.getSubTask().equals(issue))
            {
                return subTask.getSequence();
            }
        }
        return new Long(-1);
    }

}
