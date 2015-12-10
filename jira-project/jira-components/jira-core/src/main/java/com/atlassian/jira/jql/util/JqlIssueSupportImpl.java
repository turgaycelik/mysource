package com.atlassian.jira.jql.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.lang.Pair;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of {@link JqlIssueSupport}.
 *
 * @since v4.0
 */
public class JqlIssueSupportImpl implements JqlIssueSupport
{
    private final IssueManager issueManager;
    private final PermissionManager permissionManager;

    public JqlIssueSupportImpl(final IssueManager issueManager, final PermissionManager permissionManager)
    {
        this.issueManager = notNull("issueManager", issueManager);
        this.permissionManager = notNull("permissionManager", permissionManager);
    }

    public Issue getIssue(final long id, final User user)
    {
        return getIssue(id, ApplicationUsers.from(user));
    }

    @Override
    public Issue getIssue(final long id, final ApplicationUser user)
    {
        return checkPermission(user, issueManager.getIssueObject(id), false);
    }

    public Issue getIssue(final long id)
    {
        return issueManager.getIssueObject(id);
    }

    public List<Issue> getIssues(final String issueKey, final User user)
    {
        return getIssues(issueKey, user, false);
    }

    public List<Issue> getIssues(final String issueKey)
    {
        return getIssues(issueKey, null, true);
    }

    List<Issue> getIssues(final String issueKey, final User user, final boolean skipPermissionCheck)
    {
        final Issue issue = getIssue(issueKey, ApplicationUsers.from(user), skipPermissionCheck);
        return issue != null ? ImmutableList.of(issue) : Collections.<Issue>emptyList();
    }

    @Override
    public Issue getIssue(final String issueKey, final ApplicationUser user)
    {
        return getIssue(issueKey, user, false);
    }

    @Override
    public Issue getIssue(final String issueKey)
    {
        return getIssue(issueKey, null, true);
    }

    private Issue getIssue(final String issueKey, final ApplicationUser user, final boolean skipPermissionCheck)
    {
        if (StringUtils.isBlank(issueKey))
        {
            return null;
        }

        final Issue issue = issueManager.getIssueByKeyIgnoreCase(issueKey);
        return checkPermission(user, issue, skipPermissionCheck);
    }

    private Issue checkPermission(final ApplicationUser user, final Issue issue, final boolean skipPermissionCheck)
    {
        if (issue != null
                && (skipPermissionCheck || permissionManager.hasPermission(Permissions.BROWSE, issue, user)))
        {
            return issue;
        }
        return null;
    }

    @Nonnull
    @Override
    public Set<Pair<Long, String>> getProjectIssueTypePairsByKeys(@Nonnull final Set<String> issueKeys)
    {
        return issueManager.getProjectIssueTypePairsByKeys(issueKeys);
    }

    @Nonnull
    @Override
    public Set<Pair<Long, String>> getProjectIssueTypePairsByIds(@Nonnull final Set<Long> issueIds)
    {
        return issueManager.getProjectIssueTypePairsByIds(issueIds);
    }

    @Nonnull
    @Override
    public Set<String> getKeysOfMissingIssues(@Nonnull final Set<String> issueKeys)
    {
        return issueManager.getKeysOfMissingIssues(issueKeys);
    }

    @Nonnull
    @Override
    public Set<Long> getIdsOfMissingIssues(@Nonnull final Set<Long> issueIds)
    {
        return issueManager.getIdsOfMissingIssues(issueIds);
    }
}
