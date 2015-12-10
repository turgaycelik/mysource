package com.atlassian.jira.security.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUsers;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

/**
 * The common class for IssueField SecurityTypes that rely on a simple field (ie a field of the Issue Generic Value).
 *
 * @since v4.3
 */
public abstract class SimpleIssueFieldSecurityType extends AbstractIssueFieldSecurityType
{
    protected abstract String getField();

    /**
     * Defines whether the given user has permission to see the given issue.
     *
     * @param user          the User for whom permission is being determined.
     * @param issueCreation not used.
     * @param issueGv       the issue.
     * @param argument      a parameter to be optionally used by overriders.
     * @return true only if the User has permission to see the issue, false if issueGv is not an issue.
     */
    @Override
    protected boolean hasIssuePermission(User user, boolean issueCreation, GenericValue issueGv, String argument)
    {
        if (!"Issue".equals(issueGv.getEntityName()))
        {
            return false;
        }

        // TODO Is this really necessary?  It isn't documented and we aren't doing it anywhere else...
        if (user == null)
        {
            user = (User) ActionContext.getPrincipal();
        }

        final String appUserKey = ApplicationUsers.getKeyFor(user);
        return appUserKey != null && appUserKey.equals(issueGv.getString(getField()));
    }

    @Override
    protected boolean hasIssuePermission(User user, boolean issueCreation, Issue issue, String parameter)
    {
        final String appUserKey = ApplicationUsers.getKeyFor(user);
        return appUserKey != null && appUserKey.equals(getFieldValue(issue));
    }


    /**
     * Returns the user key value for the given issue for the field that this security type checks.
     * @param issue the Issue
     * @return the user key value for the given issue for the field that this security type checks.
     */
    protected abstract String getFieldValue(Issue issue);
}
