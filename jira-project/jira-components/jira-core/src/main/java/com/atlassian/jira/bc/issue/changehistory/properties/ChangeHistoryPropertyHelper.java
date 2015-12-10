package com.atlassian.jira.bc.issue.changehistory.properties;

import com.atlassian.fugue.Either;
import com.atlassian.fugue.Function2;
import com.atlassian.fugue.Option;
import com.atlassian.jira.bc.issue.properties.IssuePropertyHelper;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyHelper;
import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.event.entity.EntityPropertyDeletedEvent;
import com.atlassian.jira.event.entity.EntityPropertySetEvent;
import com.atlassian.jira.event.issue.changehistory.property.ChangeHistoryPropertyDeletedEvent;
import com.atlassian.jira.event.issue.changehistory.property.ChangeHistoryPropertySetEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.google.common.base.Function;

import javax.annotation.Nullable;

/**
 * EnitityPropertyHelper for ChangeHistories
 * @since JIRA 6.3
 */
public class ChangeHistoryPropertyHelper implements EntityPropertyHelper<ChangeHistory>
{
    private final IssueManager issueManager;
    private final I18nHelper i18n;

    private final CheckPermissionFunction<ChangeHistory> readPermissionFunction;
    private final CheckPermissionFunction<ChangeHistory> editPermissionFunction;
    private final Function2<ApplicationUser, EntityProperty, EntityPropertyDeletedEvent> createDeleteEventFunction;
    private final Function2<ApplicationUser, EntityProperty, EntityPropertySetEvent> createSetEventFunction;
    private final Function<Long, Option<ChangeHistory>> idToChangeHistoryFunction;

    public ChangeHistoryPropertyHelper(final IssueManager issueManager,
            final IssuePropertyHelper issuePropertyHelper,
            final I18nHelper i18n,
            final ChangeHistoryManager changeHistoryManager)
    {
        this.issueManager = issueManager;
        this.i18n = i18n;

        this.editPermissionFunction = new ChangeGroupPermissionFunction(issuePropertyHelper.hasEditPermissionFunction());
        this.readPermissionFunction = new ChangeGroupPermissionFunction(issuePropertyHelper.hasReadPermissionFunction());
        this.createDeleteEventFunction = new Function2<ApplicationUser, EntityProperty, EntityPropertyDeletedEvent>()
        {
            @Override
            public EntityPropertyDeletedEvent apply(final ApplicationUser user, final EntityProperty entityProperty)
            {
                return new ChangeHistoryPropertyDeletedEvent(entityProperty, user);
            }
        };
        this.createSetEventFunction = new Function2<ApplicationUser, EntityProperty, EntityPropertySetEvent>()
        {
            @Override
            public EntityPropertySetEvent apply(final ApplicationUser user, final EntityProperty entityProperty)
            {
                return new ChangeHistoryPropertySetEvent(entityProperty, user);
            }
        };
        this.idToChangeHistoryFunction = new Function<Long, Option<ChangeHistory>>()
        {
            @Override
            public Option<ChangeHistory> apply(@Nullable final Long input)
            {
                return Option.option(changeHistoryManager.getChangeHistoryById(input));
            }
        };
    }

    @Override
    public CheckPermissionFunction<ChangeHistory> hasEditPermissionFunction()
    {
        return editPermissionFunction;
    }

    @Override
    public CheckPermissionFunction<ChangeHistory> hasReadPermissionFunction()
    {
        return readPermissionFunction;
    }

    @Override
    public Function<Long, Option<ChangeHistory>> getEntityByIdFunction()
    {
        return idToChangeHistoryFunction;
    }

    @Override
    public Function2<ApplicationUser, EntityProperty, ? extends EntityPropertySetEvent> createSetPropertyEventFunction()
    {
        return createSetEventFunction;
    }

    @Override
    public Function2<ApplicationUser, EntityProperty, ? extends EntityPropertyDeletedEvent> createDeletePropertyEventFunction()
    {
        return createDeleteEventFunction;
    }

    @Override
    public EntityPropertyType getEntityPropertyType()
    {
        return EntityPropertyType.CHANGE_HISTORY_PROPERTY;
    }

    private class ChangeGroupPermissionFunction implements CheckPermissionFunction<ChangeHistory>
    {
        private final Function2<ApplicationUser, Issue, ErrorCollection> issuePermissionFunction;

        private ChangeGroupPermissionFunction(final Function2<ApplicationUser, Issue, ErrorCollection> issuePermissionFunction)
        {
            this.issuePermissionFunction = issuePermissionFunction;
        }

        private Either<ErrorCollection, Issue> getIssueFromChangeHistory(final ChangeHistory changeGroup)
        {
            if (changeGroup != null && changeGroup.getIssueId() != null) {
                final Issue issueObject = issueManager.getIssueObject(changeGroup.getIssueId());
                if (issueObject != null)
                {
                    return Either.right(issueObject);
                }
            }

            final ErrorCollection issueNotFound = new SimpleErrorCollection();
            issueNotFound.addErrorMessage(i18n.getText("issue.does.not.exist.title"), ErrorCollection.Reason.NOT_FOUND);
            return Either.left(issueNotFound);
        }

        @Override
        public ErrorCollection apply(final ApplicationUser user, final ChangeHistory changeGroup)
        {
            Either<ErrorCollection, Issue> issue = getIssueFromChangeHistory(changeGroup);
            if (issue.isLeft()) {
                return issue.left().get();
            }

            return issuePermissionFunction.apply(user, issue.right().get());
        }
    }
}
