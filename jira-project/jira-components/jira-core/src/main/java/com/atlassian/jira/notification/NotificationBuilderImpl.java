package com.atlassian.jira.notification;

import java.util.List;
import java.util.Map;

import com.atlassian.jira.security.Permissions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

final class NotificationBuilderImpl implements NotificationBuilder
{
    protected String template;
    protected Map<String, Object> templateParams;

    protected boolean toReporter;
    protected boolean toAssignee;
    protected boolean toWatchers;
    protected boolean toVoters;
    protected final List<String> toEmails = Lists.newArrayList();
    protected final List<String> toGroups = Lists.newArrayList();
    protected final List<String> toUsers = Lists.newArrayList();

    protected final List<String> restrictGroups = Lists.newArrayList();
    protected final List<Integer> restrictPermissions = Lists.newArrayList();

    NotificationBuilderImpl()
    {
    }

    public NotificationBuilder setTemplate(String template)
    {
        this.template = template;
        return this;
    }

    public String getTemplate()
    {
        return template;
    }

    public NotificationBuilder setTemplateParams(ImmutableMap<String, Object> params)
    {
        this.templateParams = params;
        return this;
    }

    public ImmutableMap<String, Object> getTemplateParams()
    {
        return ImmutableMap.copyOf(templateParams);
    }

    public NotificationBuilder setToReporter(boolean toReporter)
    {
        this.toReporter = toReporter;
        return this;
    }

    public boolean isToReporter()
    {
        return toReporter;
    }

    public NotificationBuilder setToAssignee(boolean toAssignee)
    {
        this.toAssignee = toAssignee;
        return this;
    }

    public boolean isToAssignee()
    {
        return toAssignee;
    }

    public NotificationBuilder setToWatchers(boolean toWatchers)
    {
        this.toWatchers = toWatchers;
        return this;
    }

    public boolean isToWatchers()
    {
        return toWatchers;
    }

    public NotificationBuilder setToVoters(boolean toVoters)
    {
        this.toVoters = toVoters;
        return this;
    }

    public boolean isToVoters()
    {
        return toVoters;
    }

    public NotificationBuilder addToEmail(String email)
    {
        this.toEmails.add(email);
        return this;
    }

    public NotificationBuilder addToEmails(List<String> emails)
    {
        this.toEmails.addAll(emails);
        return this;
    }

    public List<String> getToEmails()
    {
        return ImmutableList.copyOf(toEmails);
    }

    public NotificationBuilder addToGroup(String group)
    {
        this.toGroups.add(group);
        return this;
    }

    public NotificationBuilder addToGroups(List<String> groups)
    {
        this.toGroups.addAll(groups);
        return this;
    }

    public List<String> getToGroups()
    {
        return ImmutableList.copyOf(toGroups);
    }

    public NotificationBuilder addToUser(String user)
    {
        this.toUsers.add(user);
        return this;
    }

    public NotificationBuilder addToUsers(List<String> users)
    {
        this.toUsers.addAll(users);
        return this;
    }

    public List<String> getToUsers()
    {
        return ImmutableList.copyOf(toUsers);
    }

    public NotificationBuilder addRestrictGroup(String group)
    {
        this.restrictGroups.add(group);
        return this;
    }

    public NotificationBuilder addRestrictGroups(List<String> groups)
    {
        this.restrictGroups.addAll(groups);
        return this;
    }

    public List<String> getRestrictGroups()
    {
        return ImmutableList.copyOf(restrictGroups);
    }

    public NotificationBuilder addRestrictPermission(Permissions.Permission permission)
    {
        this.restrictPermissions.add(permission.getId());
        return this;
    }

    public NotificationBuilder addRestrictPermissions(List<Permissions.Permission> permissions)
    {
        for(Permissions.Permission permission : permissions)
        {
            addRestrictPermission(permission);
        }
        return this;
    }

    public List<Integer> getRestrictPermissions()
    {
        return ImmutableList.copyOf(restrictPermissions);
    }
}
