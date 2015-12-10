package com.atlassian.jira.permission;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.project.Project;
import com.opensymphony.workflow.loader.ActionDescriptor;
import org.ofbiz.core.entity.GenericValue;

/**
 */
public interface PermissionContextFactory
{
    /**
     * @deprecated Use {@link #getPermissionContext(Issue)} or {@link #getPermissionContext(Project)} instead. Since v5.0.
     */
    PermissionContext getPermissionContext(GenericValue projectOrIssue);

    PermissionContext getPermissionContext(Issue issue);

    PermissionContext getPermissionContext(Issue issue, ActionDescriptor actionDescriptor);

    PermissionContext getPermissionContext(Project project);

    /**
     * @deprecated Use {@link #getPermissionContext(Issue,ActionDescriptor)} instead. Since v5.0.
     */
    PermissionContext getPermissionContext(OperationContext operationContext, Issue issue);
}
