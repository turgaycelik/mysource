package com.atlassian.jira.auditing.handlers;

import com.atlassian.jira.auditing.RecordRequest;
import com.atlassian.jira.event.permission.GlobalPermissionAddedEvent;
import com.atlassian.jira.event.permission.GlobalPermissionDeletedEvent;
import com.atlassian.jira.event.permission.PermissionAddedEvent;
import com.atlassian.jira.event.permission.PermissionDeletedEvent;

/**
 *
 * @since v6.2
 */
public interface PermissionChangeHandler
{
    RecordRequest onPermissionAddedEvent(PermissionAddedEvent event);

    RecordRequest onGlobalPermissionAddedEvent(GlobalPermissionAddedEvent event);

    RecordRequest onGlobalPermissionDeletedEvent(GlobalPermissionDeletedEvent event);

    RecordRequest onPermissionDeletedEvent(PermissionDeletedEvent event);
}
