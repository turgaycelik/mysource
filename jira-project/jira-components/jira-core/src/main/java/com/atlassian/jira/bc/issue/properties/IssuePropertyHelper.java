package com.atlassian.jira.bc.issue.properties;

import com.atlassian.fugue.Function2;
import com.atlassian.fugue.Option;
import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityWithKeyPropertyHelper;
import com.atlassian.jira.event.issue.property.IssuePropertyDeletedEvent;
import com.atlassian.jira.event.issue.property.IssuePropertySetEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;

import com.google.common.base.Function;

/**
 * Defines permission checking, events creation and persistence layer for {@link IssuePropertyService}.
 *
 * @since v6.2
 */
public class IssuePropertyHelper implements EntityWithKeyPropertyHelper<Issue>
{
    private final DeletePropertyEventFunction deletePropertyEventFunction;
    private final I18nHelper i18n;
    private final IssueByIdFunction issueByIdFunction;
    private final IssueByKeyFunction issueByKeyFunction;
    private final IssueManager issueManager;
    private final HasEditPermissionFunction editPermissionFunction;
    private final PermissionManager permissionManager;
    private final HasViewPermissionFunction viewPermissionFunction;
    private final SetPropertyEventFunction setPropertyEventFunction;

    public IssuePropertyHelper(I18nHelper i18n, IssueManager issueManager, PermissionManager permissionManager)
    {
        this.i18n = i18n;
        this.issueManager = issueManager;
        this.permissionManager = permissionManager;
        this.deletePropertyEventFunction = new DeletePropertyEventFunction();
        this.issueByIdFunction = new IssueByIdFunction();
        this.issueByKeyFunction = new IssueByKeyFunction();
        this.editPermissionFunction = new HasEditPermissionFunction();
        this.viewPermissionFunction = new HasViewPermissionFunction();
        this.setPropertyEventFunction = new SetPropertyEventFunction();
    }

    @Override
    public Function<String, Option<Issue>> getEntityByKeyFunction()
    {
        return issueByKeyFunction;
    }

    @Override
    public CheckPermissionFunction<Issue> hasEditPermissionFunction()
    {
        return editPermissionFunction;
    }

    @Override
    public CheckPermissionFunction<Issue> hasReadPermissionFunction()
    {
        return viewPermissionFunction;
    }

    @Override
    public Function<Long, Option<Issue>> getEntityByIdFunction()
    {
        return issueByIdFunction;
    }

    @Override
    public Function2<ApplicationUser, EntityProperty, IssuePropertySetEvent> createSetPropertyEventFunction()
    {
        return setPropertyEventFunction;
    }

    @Override
    public Function2<ApplicationUser, EntityProperty, IssuePropertyDeletedEvent> createDeletePropertyEventFunction()
    {
        return deletePropertyEventFunction;
    }

    @Override
    public EntityPropertyType getEntityPropertyType()
    {
        return EntityPropertyType.ISSUE_PROPERTY;
    }

    private class DeletePropertyEventFunction implements Function2<ApplicationUser, EntityProperty, IssuePropertyDeletedEvent>
    {
        @Override
        public IssuePropertyDeletedEvent apply(final ApplicationUser user, final EntityProperty entityProperty)
        {
            return new IssuePropertyDeletedEvent(entityProperty, user);
        }
    }

    private class IssueByKeyFunction implements Function<String, Option<Issue>>
    {
        @Override
        public Option<Issue> apply(final String issueKey)
        {
            return Option.<Issue>option(issueManager.getIssueObject(issueKey));
        }
    }

    private class IssueByIdFunction implements Function<Long, Option<Issue>>
    {
        @Override
        public Option<Issue> apply(final Long issueId)
        {
            return Option.<Issue>option(issueManager.getIssueObject(issueId));
        }
    }

    private class HasEditPermissionFunction implements CheckPermissionFunction<Issue>
    {
        @Override
        public ErrorCollection apply(final ApplicationUser user, final Issue issue)
        {
            return checkIssuePermission(user, issue, IssueAction.EDIT);
        }
    }

    private class HasViewPermissionFunction implements CheckPermissionFunction<Issue>
    {

        @Override
        public ErrorCollection apply(final ApplicationUser user, final Issue issue)
        {
            return checkIssuePermission(user, issue, IssueAction.BROWSE);
        }
    }

    private ErrorCollection checkIssuePermission(final ApplicationUser user, final Issue issue, final IssueAction issueAction)
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();
        if (!permissionManager.hasPermission(issueAction.getPermissionId(), issue, user))
        {

            ErrorCollection.Reason reason = user == null ? ErrorCollection.Reason.NOT_LOGGED_IN : ErrorCollection.Reason.FORBIDDEN;
            errorCollection.addErrorMessage(i18n.getText(issueAction.getErrorMessage()), reason);
        }
        return errorCollection;
    }

    private class SetPropertyEventFunction implements Function2<ApplicationUser, EntityProperty, IssuePropertySetEvent>
    {
        @Override
        public IssuePropertySetEvent apply(final ApplicationUser user, final EntityProperty entityProperty)
        {
            return new IssuePropertySetEvent(entityProperty, user);
        }
    }

    private static enum IssueAction
    {
        BROWSE(Permissions.BROWSE, "admin.errors.issues.no.browse.permission"),
        EDIT(Permissions.EDIT_ISSUE, "editissue.error.no.edit.permission");

        private final int permissionId;
        private final String errorMessage;

        IssueAction(int permissionId, String errorMessage)
        {
            this.permissionId = permissionId;
            this.errorMessage = errorMessage;
        }

        int getPermissionId()
        {
            return permissionId;
        }

        String getErrorMessage()
        {
            return errorMessage;
        }
    }
}
