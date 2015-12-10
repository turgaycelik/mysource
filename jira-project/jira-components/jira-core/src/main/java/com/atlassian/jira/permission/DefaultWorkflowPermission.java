package com.atlassian.jira.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.SecurityType;
import com.atlassian.jira.workflow.WorkflowUtil;
import org.ofbiz.core.entity.GenericValue;

import java.util.Set;

/**
 * Represents a single permission granted in the JIRA workflow XML, eg:
 * <pre>
 &lt;meta name="jira.permission.edit.group">${pkey}-interest&lt;/meta>
 </pre>
 or
 <pre>
 &lt;meta name="jira.permission.delete.lead">&lt;/meta>
 </pre>
 */
public class DefaultWorkflowPermission implements WorkflowPermission
{
    public static final String PREFIX = "jira.permission.";
    public static final String PREFIX_PARENT = PREFIX + "subtasks.";

    int permission;
    SecurityType grantType;
    String value;
    private boolean parentPermission;

    protected DefaultWorkflowPermission(int permission, SecurityType grantType, String value, boolean isParentPermission)
    {
        this.permission = permission;
        this.grantType = grantType;
        this.value = value;
        parentPermission = isParentPermission;
    }

    public Set getUsers(PermissionContext ctx)
    {
        return grantType.getUsers(ctx, getValueFor(ctx.getIssue()));
    }

    public boolean allows(final int permission, final Issue issue, final User user)
    {
        if (user == null) return grantType.hasPermission(issue, getValueFor(issue));
        else return grantType.hasPermission(issue, getValueFor(issue), user, false);
    }

    public String toString()
    {
        return (parentPermission ? "parent " : "") + "workflow perm granting " + Permissions.getShortName(permission) + " to " + grantType.getDisplayName() + " '" + value + "'";
    }


    /** Get the value of the meta attribute, with '${pkey}' evaluated to the project key. */
    private String getValueFor(Issue issue)
    {
        GenericValue project = issue.getProject();
        return WorkflowUtil.interpolateProjectKey(project, value);
    }

}
