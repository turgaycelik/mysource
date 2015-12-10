package com.atlassian.jira.issue.fields.rest;

import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueRefJsonBean;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;

import static com.atlassian.jira.user.util.Users.isAnonymous;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Finds issues by id or key.
 *
 * @since v5.0
 */
public class IssueFinderImpl implements IssueFinder
{
    private final Pattern ISSUE_ID_PATTERN = Pattern.compile("^[1-9]\\d{0,17}$");

    private final JiraAuthenticationContext authContext;
    private final IssueManager issueManager;
    private final PermissionManager permissionManager;

    public IssueFinderImpl(JiraAuthenticationContext authContext, IssueManager issueManager, PermissionManager permissionManager)
    {
        this.authContext = authContext;
        this.issueManager = issueManager;
        this.permissionManager = permissionManager;
    }

    @Override
    public Issue findIssue(@Nonnull IssueRefJsonBean issueRef, @Nonnull ErrorCollection errorCollection)
    {
        if (isNotBlank(issueRef.id()) && isIssueId(issueRef.id()))
        {
            return findIssueById(Long.parseLong(issueRef.id()), errorCollection);
        }

        if (isNotBlank(issueRef.key()))
        {
            return findIssueByKey(issueRef.key(), errorCollection);
        }

        errorCollection.addErrorMessage(authContext.getI18nHelper().getText("rest.issue.key.or.id.required"));
        return null;
    }

    @Override
    public Issue findIssue(@Nullable String issueIdOrKey, @Nonnull ErrorCollection errorCollection)
    {
        if (isNotBlank(issueIdOrKey) && isIssueId(issueIdOrKey))
        {
            return findIssueById(Long.parseLong(issueIdOrKey), errorCollection);
        }
        if (isNotBlank(issueIdOrKey))
        {
            return findIssueByKey(issueIdOrKey, errorCollection);
        }
        errorCollection.addErrorMessage(authContext.getI18nHelper().getText("rest.issue.key.or.id.required"));
        return null;
    }

    private boolean isIssueId(@Nonnull final String issueIdOrKey) {
        return ISSUE_ID_PATTERN.matcher(issueIdOrKey).matches();
    }

    private Issue findIssueById(@Nonnull Long id, @Nonnull ErrorCollection errorCollection)
    {
        Issue issue = issueManager.getIssueObject(id);
        return checkIssuePermission(errorCollection, issue);
    }

    private Issue findIssueByKey(@Nonnull String key, @Nonnull ErrorCollection errorCollection)
    {
        Issue issue = issueManager.getIssueObject(key);

        if (issue == null)
        {
            key = key.toUpperCase();
            issue = issueManager.getIssueObject(key);
        }

        return checkIssuePermission(errorCollection, issue);
    }

    private Issue checkIssuePermission(ErrorCollection errorCollection, Issue issue)
    {
        if (issue == null)
        {
            errorCollection.addErrorMessage(authContext.getI18nHelper().getText("issue.does.not.exist.title"), ErrorCollection.Reason.NOT_FOUND);
            return null;
        }

        final ApplicationUser user = authContext.getUser();

        if (!permissionManager.hasPermission(Permissions.BROWSE, issue, user))
        {
            errorCollection.addErrorMessage(authContext.getI18nHelper().getText("admin.errors.issues.no.permission.to.see"));
            if (isAnonymous(user))
            {
                errorCollection.addErrorMessage(authContext.getI18nHelper().getText("login.required.title"), ErrorCollection.Reason.NOT_LOGGED_IN);
            }
            else
            {
                errorCollection.addReason(ErrorCollection.Reason.FORBIDDEN);
            }
            return null;
        }
        else
        {
            return issue;
        }

    }
}
