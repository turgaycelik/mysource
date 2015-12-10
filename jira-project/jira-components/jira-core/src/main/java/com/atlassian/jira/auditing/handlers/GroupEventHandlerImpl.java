package com.atlassian.jira.auditing.handlers;

import com.atlassian.crowd.embedded.core.FilteredGroupsProvider;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.crowd.event.DirectoryEvent;
import com.atlassian.crowd.event.group.GroupCreatedEvent;
import com.atlassian.crowd.event.group.GroupDeletedEvent;
import com.atlassian.crowd.event.group.GroupMembershipCreatedEvent;
import com.atlassian.crowd.event.group.GroupMembershipDeletedEvent;
import com.atlassian.crowd.model.membership.MembershipType;
import com.atlassian.fugue.Option;
import com.atlassian.jira.auditing.AffectedGroup;
import com.atlassian.jira.auditing.AffectedUser;
import com.atlassian.jira.auditing.AuditingCategory;
import com.atlassian.jira.auditing.RecordRequest;
import com.atlassian.jira.component.ComponentAccessor;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;


/**
 * @since v6.2
 */
public class GroupEventHandlerImpl implements GroupEventHandler
{

    final ImmutableSet<String> filteredGroups;

    public GroupEventHandlerImpl(FilteredGroupsProvider filteredGroupsProvider)
    {
        Preconditions.checkNotNull(filteredGroupsProvider);
        // filtered groups are treated in a case-insensitive manner.
        // We convert them all to lowercase for comparison.
        filteredGroups = ImmutableSet.copyOf(
                Collections2.transform(filteredGroupsProvider.getGroups(), IdentifierUtils.TO_LOWER_CASE));
    }

    @Override
    public Option<RecordRequest> onGroupCreatedEvent(GroupCreatedEvent event)
    {
        return ifGroupNotHidden(event, event.getGroup().getName()).map(new Function<GroupCreatedEvent, RecordRequest>()
        {
            @Override
            public RecordRequest apply(GroupCreatedEvent event)
            {
                return new RecordRequest(AuditingCategory.GROUP_MANAGEMENT, "jira.auditing.group.created")
                        .forObject(new AffectedGroup(event.getGroup().getName(), event.getDirectory()));
            }
        });
    }

    @Override
    public Option<RecordRequest> onGroupDeletedEvent(final GroupDeletedEvent event)
    {
        return ifGroupNotHidden(event, event.getGroupName()).map(new Function<GroupDeletedEvent, RecordRequest>()
        {
            @Override
            public RecordRequest apply(GroupDeletedEvent event)
            {
                return new RecordRequest(AuditingCategory.GROUP_MANAGEMENT, "jira.auditing.group.deleted")
                        .forObject(new AffectedGroup(event.getGroupName(), event.getDirectory()));
            }
        });
    }

    @Override
    public Option<RecordRequest> onGroupMembershipCreatedEvent(final GroupMembershipCreatedEvent event)
    {
        return ifGroupNotHidden(event, event.getGroupName()).map(new Function<GroupMembershipCreatedEvent, RecordRequest>()
        {
            @Override
            public RecordRequest apply(GroupMembershipCreatedEvent event)
            {
                if (event.getMembershipType().equals(MembershipType.GROUP_GROUP))
                {
                    return new RecordRequest(AuditingCategory.GROUP_MANAGEMENT, "jira.auditing.group.added.to.group")
                            .forObject(new AffectedGroup(event.getGroupName(), event.getDirectory()))
                                    //event.getEntityName() returns group name for this MembershipType
                            .withAssociatedItems(new AffectedGroup(event.getEntityName(), event.getDirectory()));
                }

                if (event.getMembershipType().equals(MembershipType.GROUP_USER))
                {
                    final String userKey = ComponentAccessor.getUserKeyService().getKeyForUsername(event.getEntityName());
                    return new RecordRequest(AuditingCategory.GROUP_MANAGEMENT, "jira.auditing.user.added.to.group")
                            .forObject(new AffectedGroup(event.getGroupName(), event.getDirectory()))
                                    //event.getEntityName() returns user name for this MembershipType
                            .withAssociatedItems(new AffectedUser(event.getEntityName(), userKey, event.getDirectory()));
                }

                throw new IllegalArgumentException("Unknown membership type: " + event.getMembershipType());
            }
        });
    }

    @Override
    public Option<RecordRequest> onGroupMembershipDeletedEvent(final GroupMembershipDeletedEvent event)
    {
        return ifGroupNotHidden(event, event.getGroupName()).map(new Function<GroupMembershipDeletedEvent, RecordRequest>()
        {
            @Override
            public RecordRequest apply(GroupMembershipDeletedEvent event)
            {
                if (event.getMembershipType().equals(MembershipType.GROUP_GROUP))
                {
                    return new RecordRequest(AuditingCategory.GROUP_MANAGEMENT, "jira.auditing.group.removed.from.group")
                            //event.getEntityName() returns group name for this MembershipType
                            .forObject(new AffectedGroup(event.getGroupName(), event.getDirectory()))
                            .withAssociatedItems(new AffectedGroup(event.getEntityName(), event.getDirectory()));
                }

                if (event.getMembershipType().equals(MembershipType.GROUP_USER))
                {
                    final String userKey = ComponentAccessor.getUserKeyService().getKeyForUsername(event.getEntityName());
                    return new RecordRequest(AuditingCategory.GROUP_MANAGEMENT, "jira.auditing.user.removed.from.group")
                            //event.getEntityName() returns user name for this MembershipType
                            .forObject(new AffectedGroup(event.getGroupName(), event.getDirectory()))
                            .withAssociatedItems(new AffectedUser(event.getEntityName(), userKey, event.getDirectory()));
                }

                throw new IllegalArgumentException("Unknown membership type: " + event.getMembershipType());

            }
        });
    }

    private <T extends DirectoryEvent> Option<T> ifGroupNotHidden(T event, String groupName)
    {
        if (isGroupToBeFiltered(groupName))
        {
            return Option.none();
        }
        else
        {
            return Option.some(event);
        }
    }

    private boolean isGroupToBeFiltered(String groupname)
    {
        return filteredGroups.contains(toLowerCase(groupname));
    }
}
