package com.atlassian.jira.auditing.handlers;

import java.util.List;

import com.atlassian.crowd.event.user.AutoUserUpdatedEvent;
import com.atlassian.crowd.event.user.ResetPasswordEvent;
import com.atlassian.crowd.event.user.UserAttributeDeletedEvent;
import com.atlassian.crowd.event.user.UserAttributeStoredEvent;
import com.atlassian.crowd.event.user.UserCreatedEvent;
import com.atlassian.crowd.event.user.UserCredentialUpdatedEvent;
import com.atlassian.crowd.event.user.UserDeletedEvent;
import com.atlassian.crowd.event.user.UserEditedEvent;
import com.atlassian.crowd.event.user.UserRenamedEvent;
import com.atlassian.crowd.event.user.UserUpdatedEvent;
import com.atlassian.crowd.model.user.User;
import com.atlassian.fugue.Option;
import com.atlassian.jira.auditing.AffectedUser;
import com.atlassian.jira.auditing.AuditingCategory;
import com.atlassian.jira.auditing.ChangedValue;
import com.atlassian.jira.auditing.RecordRequest;
import com.atlassian.jira.component.ComponentAccessor;
import com.google.common.base.Function;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;
import static com.atlassian.jira.auditing.handlers.HandlerUtils.requestIfThereAreAnyValues;

/**
 * @since v6.2
 */
public class UserEventHandlerImpl implements UserEventHandler
{
    @Override
    public Option<RecordRequest> handleUserUpdatedEvent(UserUpdatedEvent event)
    {
        if (event instanceof AutoUserUpdatedEvent)
        {
            return onAutoUserUpdatedEvent((AutoUserUpdatedEvent) event);
        }
        else if (event instanceof ResetPasswordEvent)
        {
            return option(onResetPasswordEvent((ResetPasswordEvent) event));
        }
        else if (event instanceof UserRenamedEvent)
        {
            return option(onUserRenamedEvent((UserRenamedEvent) event));
        }
        else if (event instanceof UserAttributeStoredEvent || event instanceof UserAttributeDeletedEvent)
        {
            return none(); //we ignore attribute changes
        }
        else if (event instanceof UserEditedEvent)
        {
            return onUserEditedEvent((UserEditedEvent) event);
        }
        else
        {
            throw new RuntimeException("Unsupported event: " + event.getClass().getName());
        }
    }

    private Option<RecordRequest> onAutoUserUpdatedEvent(AutoUserUpdatedEvent event)
    {
        return userUpdated(event.getOriginalUser(), event.getUser());
    }

    private Option<RecordRequest> onUserEditedEvent(UserEditedEvent event)
    {
        return userUpdated(event.getOriginalUser(), event.getUser());
    }

    private Option<RecordRequest> userUpdated(final User originalUser, final User user)
    {
        return requestIfThereAreAnyValues(
                buildChangedValues(originalUser, user),
                new Function<List<ChangedValue>, RecordRequest>()
                {
                    @Override
                    public RecordRequest apply(final List<ChangedValue> changedValues)
                    {
                        return new RecordRequest(AuditingCategory.USER_MANAGEMENT, "jira.auditing.user.updated")
                                .withChangedValues(changedValues)
                                .forObject(new AffectedUser(user));
                    }
                });
    }

    private RecordRequest onResetPasswordEvent(ResetPasswordEvent event)
    {
        return new RecordRequest(AuditingCategory.USER_MANAGEMENT, "jira.auditing.user.password.reset")
                .forObject(new AffectedUser(event.getUser()));
    }

    private RecordRequest onUserRenamedEvent(UserRenamedEvent event)
    {
        return new RecordRequest(AuditingCategory.USER_MANAGEMENT, "jira.auditing.user.renamed")
                .withChangedValues(new ChangedValuesBuilder().addIfDifferent("common.words.username", event.getOldUsername(), event.getUser().getName()).build())
                .forObject(new AffectedUser(event.getUser()));
    }

    private List<ChangedValue> buildChangedValues(final User originalUser, final User currentUser)
    {
        final ChangedValuesBuilder changedValues = new ChangedValuesBuilder();

        changedValues.addIfDifferent("common.words.username", originalUser == null ? null : originalUser.getName(), currentUser.getName())
                .addIfDifferent("common.words.fullname", originalUser == null ? null : originalUser.getDisplayName(), currentUser.getDisplayName())
                .addIfDifferent("common.words.email", originalUser == null ? null : originalUser.getEmailAddress(), currentUser.getEmailAddress())
                .addIfDifferent("admin.common.phrases.active.inactive", originalUser == null ? null : stringBooleanToActiveInactive(originalUser.isActive()), stringBooleanToActiveInactive(currentUser.isActive()));
        return changedValues.build();
    }

    private String stringBooleanToActiveInactive(Boolean tBoolean)
    {
        if (tBoolean == null)
        {
            return "";
        }
        return tBoolean.equals(Boolean.TRUE) ? "Active" : "Inactive";
    }

    @Override
    public RecordRequest handleUserCreatedEvent(final UserCreatedEvent event)
    {
        User user = event.getUser();
        return new RecordRequest(AuditingCategory.USER_MANAGEMENT, "jira.auditing.user.created")
                .withChangedValues(buildChangedValues(user))
                .forObject(new AffectedUser(user));
    }

    @Override
    public RecordRequest handleUserDeletedEvent(final UserDeletedEvent event)
    {
        final String userKey = ComponentAccessor.getUserKeyService().getKeyForUsername(event.getUsername());
        return new RecordRequest(AuditingCategory.USER_MANAGEMENT, "jira.auditing.user.deleted")
                .forObject(new AffectedUser(event.getUsername(), userKey, event.getDirectory()));
    }

    @Override
    public RecordRequest handleUserCredentialUpdatedEvent(final UserCredentialUpdatedEvent event)
    {
        final String userKey = ComponentAccessor.getUserKeyService().getKeyForUsername(event.getUsername());
        return new RecordRequest(AuditingCategory.USER_MANAGEMENT, "jira.auditing.user.password.changed")
                .forObject(new AffectedUser(event.getUsername(), userKey, event.getDirectory()));
    }

    private List<ChangedValue> buildChangedValues(final User currentUser)
    {
        return buildChangedValues(null, currentUser);
    }
}
