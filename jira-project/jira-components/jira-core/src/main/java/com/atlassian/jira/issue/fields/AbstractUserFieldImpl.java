package com.atlassian.jira.issue.fields;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.UserComparator;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.search.handlers.SearchHandlerFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;

/**
 * Base class for single user-based fields
 * @since v5.2
 */
public abstract class AbstractUserFieldImpl extends AbstractOrderableNavigableFieldImpl implements UserField
{

    private final UserHistoryManager userHistoryManager;

    protected AbstractUserFieldImpl(String id, String name, VelocityTemplatingEngine templatingEngine,
            ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext,
            PermissionManager permissionManager, SearchHandlerFactory searchHandlerFactory,
            UserHistoryManager userHistoryManager)
    {
        super(id, name, templatingEngine, applicationProperties, authenticationContext, permissionManager, searchHandlerFactory);
        this.userHistoryManager = userHistoryManager;
    }

    /**
     * If the value of this field has changed in the issue, the new user is added to the "used user" history
     */
    protected void addToUsedUserHistoryIfValueChanged(MutableIssue issue)
    {
        ModifiedValue modifiedValue = issue.getModifiedFields().get(getId());
        if (modifiedValue == null) {
            return;
        }
        final User newUser = (User) modifiedValue.getNewValue();
        final User oldUser = (User) modifiedValue.getOldValue();
        final User loggedInUser = authenticationContext.getLoggedInUser();

        //JRADEV-14962 Don't add current to user history; we don't want to clobber genuine choices with current user.
        if (newUser != null && !UserComparator.equal(oldUser, newUser) && !UserComparator.equal(newUser, loggedInUser)) {
            userHistoryManager.addUserToHistory(UserHistoryItem.USED_USER, loggedInUser, newUser);
        }
    }

}
