package com.atlassian.jira.notification;

import java.util.List;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.security.Permissions;

import com.google.common.collect.ImmutableMap;

/**
 * @see AdhocNotificationService
 * @since 5.2
 */
@PublicApi
public interface NotificationBuilder
{
    NotificationBuilder setTemplate(String template);

    String getTemplate();

    NotificationBuilder setTemplateParams(ImmutableMap<String, Object> params);

    ImmutableMap<String, Object> getTemplateParams();

    NotificationBuilder setToReporter(boolean toReporter);

    boolean isToReporter();

    NotificationBuilder setToAssignee(boolean toReporter);

    boolean isToAssignee();

    NotificationBuilder setToWatchers(boolean toWatchers);

    boolean isToWatchers();

    NotificationBuilder setToVoters(boolean toVoters);

    boolean isToVoters();

    NotificationBuilder addToEmail(String email);

    NotificationBuilder addToEmails(List<String> emails);

    List<String> getToEmails();

    NotificationBuilder addToGroup(String group);

    NotificationBuilder addToGroups(List<String> groups);

    List<String> getToGroups();

    NotificationBuilder addToUser(String user);

    NotificationBuilder addToUsers(List<String> users);

    List<String> getToUsers();

    /**
     * If group restrictions are added, then the recipients <strong>must</strong> belong to at least one of the added groups.
     *
     * @param group the group to add
     * @return {@code this}
     * @see #addRestrictGroups(List)
     */
    NotificationBuilder addRestrictGroup(String group);

    /**
     * If group restrictions are added, then the recipients <strong>must</strong> belong to at least one of the added groups.
     *
     * @param groups a list of groups to be added
     * @return {@code this}
     * @see #addRestrictGroup(String)
     */
    NotificationBuilder addRestrictGroups(List<String> groups);

    List<String> getRestrictGroups();

    /**
     * If permission restrictions are added, then the recipients <strong>must</strong> belong to at least one of the added permissions.
     *
     * @param permission the permission to add
     * @return {@code this}
     * @see #addRestrictPermissions(List)
     */
    NotificationBuilder addRestrictPermission(Permissions.Permission permission);

    /**
     * If permission restrictions are added, then the recipients <strong>must</strong> belong to at least one of the added permissions.
     *
     * @param permissions the list of permissions to add
     * @return {@code this}
     * @see #addRestrictPermission(Permissions.Permission)
     */
    NotificationBuilder addRestrictPermissions(List<Permissions.Permission> permissions);
}
