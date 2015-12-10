package com.atlassian.jira.rest.v2.issue;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.rest.v2.avatar.AvatarUrls;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.rest.json.beans.GroupJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.GroupJsonBeanBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.EmailFormatter;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Builder for UserBean instances.
 *
 * @since v4.2
 */
public class UserBeanBuilder
{
    /**
     * The JIRA Base URLs
     */
    private final JiraBaseUrls jiraBaseUrls;

    /**
     * The User.
     */
    private ApplicationUser user;
    private String username;

    private List<String> groups;
    /**
     * The currently logged in user.
     */
    private ApplicationUser loggedInUser;

    /**
     * Indicates whether the logged in user has been set.
     */
    private boolean loggedInUserIsSet;

    /**
     * The EmailFormatter.
     */
    private EmailFormatter emailFormatter;

    /**
     * The time zone of the user.
     */
    private TimeZone timeZone;

    /**
     * The AvatarService.
     */
    private AvatarService avatarService;

    /**
     * url to self
     */
    private URI self;

    /**
     * Creates new instance of UserBeanBuilder. Requires JiraBaseUrls to build values of self parameter.
     * @param jiraBaseUrls An instance of JiraBaseUrls
     */
    public UserBeanBuilder(final JiraBaseUrls jiraBaseUrls)
    {
        Preconditions.checkNotNull(jiraBaseUrls, "JiraBaseUrls cannot be null!");
        this.jiraBaseUrls = jiraBaseUrls;
    }

    /**
     * Sets the User.
     *
     * @param user a User
     * @return this
     */
    public UserBeanBuilder user(User user)
    {
        this.user = user == null ? null : ApplicationUsers.from(user);
        return this;
    }

    /**
     * Sets the User.
     *
     * @param user a User
     * @return this
     */
    public UserBeanBuilder user(ApplicationUser user)
    {
        this.user = user;
        return this;
    }

    /**
     * Sets the user using a username and UserManager.
     * If the given User no longer exists, we still create a UserBean with the given username
     *
     * @param username The username
     * @param userManager The UserManager
     * @return this Builder
     */
    public UserBeanBuilder user(final String username, final UserManager userManager)
    {
        this.username = username;
        this.user = userManager.getUserByName(username);
        return this;
    }

    /**
     * Sets the groups that this user belongs to.
     *
     * @param groups the groups that this user belongs to.
     * @return this
     */
    public UserBeanBuilder groups(List<String> groups)
    {
        this.groups = groups;
        return this;
    }


    /**
     * Sets the self URI.
     *
     * @param self self URI.
     * @return this
     */
    public UserBeanBuilder self(URI self)
    {
        this.self = self;
        return this;
    }

    /**
     * Sets the currently logged in user.
     *
     * @param loggedInUser a User
     * @return this
     */
    public UserBeanBuilder loggedInUser(User loggedInUser)
    {
        this.loggedInUser = ApplicationUsers.from(loggedInUser);
        loggedInUserIsSet = true;
        return this;
    }

    /**
     * Sets the currently logged in user.
     *
     * @param loggedInUser a User
     * @return this
     */
    public UserBeanBuilder loggedInUser(ApplicationUser loggedInUser)
    {
        this.loggedInUser = loggedInUser;
        loggedInUserIsSet = true;
        return this;
    }

    public UserBeanBuilder timeZone(TimeZone timeZone)
    {
        if (timeZone != null)
        {
            this.timeZone = timeZone;
        }

        return this;
    }

    /**
     * Sets the EmailFormatter to use for users' email addresses.
     *
     * @param emailFormatter an EmailFormatter
     * @return this
     */
    public UserBeanBuilder emailFormatter(EmailFormatter emailFormatter)
    {
        this.emailFormatter = emailFormatter;
        return this;
    }

    /**
     * Sets the AvatarService to use for constructing the avatar URL.
     *
     * @param avatarService an AvatarService
     * @return this
     */
    public UserBeanBuilder avatarService(AvatarService avatarService)
    {
        this.avatarService = avatarService;
        return this;
    }

    /**
     * Returns a new UserBean with the name, self, and author properties set.
     *
     * @return a new UserBean
     */
    public UserBean buildShort()
    {
        if (user != null)
        {
            return new UserBean(
                    self != null ? self : createSelfLink(),
                    user.getKey(),
                    user.getUsername(),
                    user.getDisplayName(),
                    user.isActive(),
                    getAvatarURLs()
            );
        }

        return buildSimple();
    }

    /**
     * Returns a new UserBean with all properties set.
     *
     * @return a new UserBean
     */
    public UserBean buildFull()
    {
        if (user == null)
        {
            return buildMid();
        }

        if (groups == null) { throw new IllegalStateException("groups not set"); }
        if (emailFormatter == null) { throw new IllegalStateException("emailFormatter not set"); }
        if (!loggedInUserIsSet) { throw new IllegalStateException("loggedInUser not set"); }
        if (timeZone == null) { throw new IllegalStateException("timeZone not set"); }
        if (avatarService == null) { throw new IllegalStateException("avatarService not set"); }

        return new UserBean(
                    self != null ? self : createSelfLink(),
                    user.getKey(),
                    user.getUsername(),
                    user.getDisplayName(),
                    user.isActive(),
                    emailFormatter.formatEmail(user.getEmailAddress(), loggedInUser.getDirectoryUser()),
                    Lists.transform(groups, new GroupNameToGroupJsonBean(jiraBaseUrls)),
                    getAvatarURLs(),
                    timeZone);
    }

    /**
     * Returns a new UserBean with all properties set.
     *
     * @return a new UserBean
     */
    public UserBean buildMid()
    {
        if (user == null)
        {
            return buildSimple();
        }

        if (!loggedInUserIsSet) { throw new IllegalStateException("loggedInUser not set"); }
        if (emailFormatter == null) { throw new IllegalStateException("emailFormatter not set"); }
        if (timeZone == null) { throw new IllegalStateException("timeZone not set"); }

        return new UserBean(
                    self != null ? self : createSelfLink(),
                    user.getKey(),
                    user.getUsername(),
                    user.getDisplayName(),
                    user.isActive(),
                    emailFormatter.formatEmail(user.getEmailAddress(), loggedInUser.getDirectoryUser()),
                    null,
                    getAvatarURLs(),
                    timeZone);
    }

    private UserBean buildSimple()
    {
        if (username == null)
        {
            return null;
        }
        // TODO
        return new UserBean(null, null, username, null, false, null);
    }

    protected URI createSelfLink()
    {
        return UriBuilder.fromPath(jiraBaseUrls.restApi2BaseUrl())
                .path(UserResource.class)
                .queryParam("username", "{0}") // JRADEV-3622. Workaround for percent encoding problem.
                .build(user.getUsername()); //JRADEV-22338: FIXME: we should start returning key instead of username here!
    }

    private Map<String, URI> getAvatarURLs()
    {
        Avatar avatar = ComponentAccessor.getAvatarService().getAvatar(user, user);

        return avatar != null ? AvatarUrls.getAvatarURLs(user, avatar) : null;
    }

    /**
     * Functor that creates GroupJsonBeans from grup names.
     */
    private static class GroupNameToGroupJsonBean implements Function<String, GroupJsonBean>
    {
        private final JiraBaseUrls jiraBaseUrls;

        public GroupNameToGroupJsonBean(JiraBaseUrls jiraBaseUrls)
        {
            this.jiraBaseUrls = jiraBaseUrls;
        }

        public GroupJsonBean apply(@Nullable String groupName)
        {
            return new GroupJsonBeanBuilder(jiraBaseUrls).name(groupName).build();
        }
    }
}
