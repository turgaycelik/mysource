package com.atlassian.jira.plugin.assignee;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.ErrorCollection;

import java.util.Map;

/**
 * Used to determine the default assignee for an issue. This class is used when an issue
 * is created or updated (e.g edited or transitioned through workfow, etc) and the user selectes
 * 'Automatic' from the assignee drop down. The implementation of this class is responsible
 * for resolving 'Automatic' to the actual user the issue will be assigned to.
 *
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public interface AssigneeResolver
{

    /**
     * This method is called to validate input when a user selects 'Automatic' during an issue operation, e.g. creating,
     * editing, moving an issue, etc.
     * <p>
     * This method allows the implementation of this interface to validate the input and
     * report any errors back to the caller (most likely the user interface). Most often the errors reported are if
     * the default assignee does not have the {@link com.atlassian.jira.security.Permissions#ASSIGNABLE_USER} permission.
     * This method is called before {@link #getDefaultAssignee(com.atlassian.jira.issue.Issue, java.util.Map)}.
     * <p>
     * The fieldValuesHolder holds values of issue fields that
     * have been selected during the operation. For exmaple, if an issue is being transitioned through workflow
     * and the Screen for the workflow transition shows 3 fields (e.g. description, fix versions, and assignee)
     * the fieldValuesHolder will hold the values of these 3 issue fields. The keys of fieldValuesHolder are issue
     * field ids that can be found in {@link com.atlassian.jira.issue.IssueFieldConstants}.
     *
     * @param issue the issue the assignee will be selected for.
     * @param fieldValuesHolder holds values of fields that appear on the Screen of the operation that is being
     * performed. The keys are ids of issue fields. The issue field ids are found in
     * {@link com.atlassian.jira.issue.IssueFieldConstants}
     * @return ErrorCollection with errors (if any). Others an empty ErrorCollection is returned.
     */
    ErrorCollection validateDefaultAssignee(Issue issue, Map fieldValuesHolder);

    /**
     * Determines the actual User the issue should be assigned to. This method is invoked when an issue
     * is being created, edited, moved, etc.
     * <p>
     * This method is invoked to actually deterine the User the issue is assigned to. This method is invoked
     * after {@link #validateDefaultAssignee(com.atlassian.jira.issue.Issue, java.util.Map)}.
     *
     * @param issue the issue the assignee will be selected for.
     * @param fieldValuesHolder is the same as for {@link #validateDefaultAssignee(com.atlassian.jira.issue.Issue, java.util.Map)}.
     * {@link com.atlassian.jira.issue.IssueFieldConstants}
     * @return the user the issue should be assigned to.
     * @deprecated Since 4.3 Use {@link #getDefaultAssigneeObject(com.atlassian.jira.issue.Issue, java.util.Map)}.
     */
    User getDefaultAssignee(Issue issue, Map fieldValuesHolder);

    /**
     * Determines the default User the issue should be assigned to.
     *
     * This method is invoked when an issue is being created, edited, moved, etc.
     * <p>
     * When used in GUI operations, this method will be invoked after {@link #validateDefaultAssignee(com.atlassian.jira.issue.Issue, java.util.Map)}.
     * However, it can also be called by itself (eg when creating an issue from an incoming email).
     *
     * @param issue the issue the assignee will be selected for.
     * @param fieldValuesHolder is the same as for {@link #validateDefaultAssignee(com.atlassian.jira.issue.Issue, java.util.Map)}.
     * @return the user the issue should be assigned to.
     *
     * @throws com.atlassian.jira.project.DefaultAssigneeException If the default assignee is invalid (eg user does not have assign permission) .
     */
    User getDefaultAssigneeObject(Issue issue, Map fieldValuesHolder);
}
