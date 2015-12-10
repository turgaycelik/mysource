package com.atlassian.jira.issue.fields.rest.json.beans;

import java.util.List;

import com.atlassian.jira.security.Permissions;

import com.google.common.collect.Lists;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class NotificationJsonBean
{
    @JsonProperty
    private String subject = "";

    @JsonProperty
    private String textBody = "";

    @JsonProperty
    private String htmlBody = "";

    @JsonProperty
    private ToJsonBean to = new ToJsonBean();

    @JsonProperty
    private RestrictJsonBean restrict = new RestrictJsonBean();

    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    public void setTextBody(String textBody)
    {
        this.textBody = textBody;
    }

    public void setHtmlBody(String htmlBody)
    {
        this.htmlBody = htmlBody;
    }

    public void setTo(ToJsonBean to)
    {
        this.to = to;
    }

    public void setRestrict(RestrictJsonBean restrict)
    {
        this.restrict = restrict;
    }

    public String getSubject()
    {
        return subject;
    }

    public String getTextBody()
    {
        return textBody;
    }

    public String getHtmlBody()
    {
        return htmlBody;
    }

    public ToJsonBean getTo()
    {
        return to;
    }

    public RestrictJsonBean getRestrict()
    {
        return restrict;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class ToJsonBean
    {
        @JsonProperty
        private boolean reporter = false;

        @JsonProperty
        private boolean assignee = false;

        @JsonProperty
        private boolean watchers = false;

        @JsonProperty
        private boolean voters = false;

//        @JsonProperty
//        private List<String> emails = Lists.newArrayList();

        @JsonProperty
        private List<UserJsonBean> users = Lists.newArrayList();

        @JsonProperty
        private List<GroupJsonBean> groups = Lists.newArrayList();

        public void setReporter(boolean reporter)
        {
            this.reporter = reporter;
        }

        public void setAssignee(boolean assignee)
        {
            this.assignee = assignee;
        }

        public void setWatchers(boolean watchers)
        {
            this.watchers = watchers;
        }

        public void setVoters(boolean voters)
        {
            this.voters = voters;
        }

//        public void setEmails(List<String> emails)
//        {
//            this.emails = emails;
//        }

        public void setUsers(List<UserJsonBean> users)
        {
            this.users = users;
        }

        public void setGroups(List<GroupJsonBean> groups)
        {
            this.groups = groups;
        }

        public boolean isReporter()
        {
            return reporter;
        }

        public boolean isAssignee()
        {
            return assignee;
        }

        public boolean isWatchers()
        {
            return watchers;
        }

        public boolean isVoters()
        {
            return voters;
        }

//        public List<String> getEmails()
//        {
//            return emails;
//        }

        public List<UserJsonBean> getUsers()
        {
            return users;
        }

        public List<GroupJsonBean> getGroups()
        {
            return groups;
        }
    }

    public static final class RestrictJsonBean
    {
        @JsonProperty
        private List<GroupJsonBean> groups = Lists.newArrayList();

        @JsonProperty
        private List<PermissionJsonBean> permissions = Lists.newArrayList();

        public void setGroups(List<GroupJsonBean> groups)
        {
            this.groups = groups;
        }

        public void setPermissions(List<PermissionJsonBean> permissions)
        {
            this.permissions = permissions;
        }

        public List<GroupJsonBean> getGroups()
        {
            return groups;
        }

        public List<PermissionJsonBean> getPermissions()
        {
            return permissions;
        }
    }

    public static final NotificationJsonBean DOC_UPDATE_EXAMPLE = new NotificationJsonBean();

    static
    {
        final UserJsonBean fred = new UserJsonBean();
        fred.setName("fred");

        final GroupJsonBean notificationGroup = GroupJsonBean.BuildDocExampleUsers("notification-group");

        DOC_UPDATE_EXAMPLE.subject = "Duis eu justo eget augue iaculis fermentum.";
        DOC_UPDATE_EXAMPLE.textBody = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque eget venenatis elit. Duis eu justo eget augue iaculis fermentum. Sed semper quam laoreet nisi egestas at posuere augue semper.";
        DOC_UPDATE_EXAMPLE.htmlBody = "Lorem ipsum <strong>dolor</strong> sit amet, consectetur adipiscing elit. Pellentesque eget venenatis elit. Duis eu justo eget augue iaculis fermentum. Sed semper quam laoreet nisi egestas at posuere augue semper.";
        DOC_UPDATE_EXAMPLE.to.watchers = true;
        DOC_UPDATE_EXAMPLE.to.voters = true;
//        DOC_UPDATE_EXAMPLE.to.emails.add("user@example.com");
        DOC_UPDATE_EXAMPLE.to.users.add(fred);
        DOC_UPDATE_EXAMPLE.to.groups.add(notificationGroup);
        DOC_UPDATE_EXAMPLE.restrict.groups.add(notificationGroup);
        DOC_UPDATE_EXAMPLE.restrict.permissions.add(PermissionJsonBean.fullBean(Permissions.Permission.BROWSE));
    }
}
