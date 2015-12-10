package com.atlassian.jira.permission;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.opensymphony.workflow.loader.StepDescriptor;
import org.ofbiz.core.entity.GenericValue;

/**
 * Represents the context in which a permission evaluation is to be made.
 * Permission requests are of the form: subject, verb, object (eg. "User fred wishes to comment on ABC-123"), where:
 * subject = User object
 * verb = permission Id
 * object = Issue, project etc.
 * A PermissionContext encapsulates the object.
 * <p>
 * To create PermissionContext objects, use the {@link PermissionContextFactory}
 * @see PermissionContextFactory
 */
public interface PermissionContext
{
    /**
     * Returns the Project in this context.
     * @return the Project in this context.
     * @deprecated Use {@link #getProjectObject()} instead. Since v5.0.
     */
    GenericValue getProject();

    /**
     * Returns the Project in this context.
     * @return the Project in this context.
     */
    Project getProjectObject();

    public Issue getIssue();

    /**
     * Whether this PermissionContext has an existing fully-formed issue (not one that is still being created).
     * This returns false on the second issue creation page, and on the quick sub-task creation form, where the
     * issue type is unknown.
     * @return true if this PermissionContext has an existing fully-formed issue.
     *
     * @deprecated Use {@link #hasIssuePermissions()} instead. Since v5.0.
     */
    boolean isHasCreatedIssue();

    Status getStatus();

    /**
     * Returns the relevant workflow step for the Issue in this context
     * @return the relevant workflow step for the Issue in this context
     */
    StepDescriptor getRelevantStepDescriptor();

    /**
     * Whether we have enough information to look up issue-specific (workflow) permissions.
     * <p>
     * True if this PermissionContext has an existing fully-formed issue (not one that is still being created).
     * @return true if this PermissionContext has an existing fully-formed issue.
     */
    boolean hasIssuePermissions();
}