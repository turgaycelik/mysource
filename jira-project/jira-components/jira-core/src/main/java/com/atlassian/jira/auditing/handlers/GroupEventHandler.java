package com.atlassian.jira.auditing.handlers;

import com.atlassian.crowd.event.group.GroupCreatedEvent;
import com.atlassian.crowd.event.group.GroupDeletedEvent;
import com.atlassian.crowd.event.group.GroupMembershipCreatedEvent;
import com.atlassian.crowd.event.group.GroupMembershipDeletedEvent;
import com.atlassian.fugue.Option;
import com.atlassian.jira.auditing.RecordRequest;

/**
 * @since v6.2
 */
public interface GroupEventHandler
{
    Option<RecordRequest> onGroupCreatedEvent(GroupCreatedEvent event);

    Option<RecordRequest> onGroupDeletedEvent(GroupDeletedEvent event);

    Option<RecordRequest> onGroupMembershipCreatedEvent(GroupMembershipCreatedEvent event);

    Option<RecordRequest> onGroupMembershipDeletedEvent(GroupMembershipDeletedEvent event);
}
