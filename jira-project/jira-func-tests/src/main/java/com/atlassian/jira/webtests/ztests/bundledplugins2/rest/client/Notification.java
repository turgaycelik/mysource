package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.testkit.client.restclient.Group;
import com.atlassian.jira.testkit.client.restclient.UserBean;
import com.google.common.collect.Lists;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.List;

public final class Notification
{
    public String subject;
    public String textBody;
    public String htmlBody;
    public To to = new To();
    public Restrict restrict = new Restrict();

    public Notification subject(String subject)
    {
        this.subject = subject;
        return this;
    }

    public Notification textBody(String text)
    {
        this.textBody = text;
        return this;
    }

    public Notification htmlBody(String html)
    {
        this.htmlBody = html;
        return this;
    }

    public Notification toReporter()
    {
        to.reporter = true;
        return this;
    }

    public Notification toAssignee()
    {
        to.assignee = true;
        return this;
    }

    public Notification toWatchers()
    {
        to.watchers = true;
        return this;
    }

    public Notification toVoters()
    {
        to.voters = true;
        return this;
    }

    public Notification toEmail(String email)
    {
        to.emails.add(email);
        return this;
    }

    public Notification toUser(String user)
    {
        UserBean userBean = new UserBean();
        userBean.name = user;
        to.users.add(userBean);
        return this;
    }

    public Notification toGroup(String group)
    {
        to.groups.add(new Group().name(group));
        return this;
    }

    public Notification restrictToGroup(String group)
    {
        restrict.groups.add(new Group().name(group));
        return this;
    }

    public Notification restrictToPermission(int permissionId)
    {
        restrict.permissions.add(new Permission().id(permissionId));
        return this;
    }

    public Notification restrictToPermission(String permissionKey)
    {
        restrict.permissions.add(new Permission().key(permissionKey));
        return this;
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static final class To
    {
        public boolean reporter;
        public boolean assignee;
        public boolean watchers;
        public boolean voters;
        public final List<String> emails = Lists.newArrayList();
        public final List<UserBean> users = Lists.newArrayList();
        public final List<Group> groups = Lists.newArrayList();

        @Override
        public int hashCode()
        {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(Object obj)
        {
            return EqualsBuilder.reflectionEquals(this, obj);
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    public static final class Restrict
    {
        public final List<Group> groups = Lists.newArrayList();
        public final List<Permission> permissions = Lists.newArrayList();

        @Override
        public int hashCode()
        {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(Object obj)
        {
            return EqualsBuilder.reflectionEquals(this, obj);
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
