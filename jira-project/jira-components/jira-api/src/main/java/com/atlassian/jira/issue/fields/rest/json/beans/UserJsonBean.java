package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @since v5.0
 */
public class UserJsonBean
{
    @JsonProperty
    private String self;

    @JsonProperty
    private String name;

    @JsonProperty
    private String key;

    @JsonProperty
    private String emailAddress;

    @JsonProperty
    private Map<String, String> avatarUrls;

    @JsonProperty
    private String displayName;

    @JsonProperty
    private boolean active;

    public String getSelf()
    {
        return self;
    }

    public void setSelf(String self)
    {
        this.self = self;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getEmailAddress()
    {
        return emailAddress;
    }

    /**
     * @deprecated Use {@link #setEmailAddress(String, com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.util.EmailFormatter)}
     */
    @Deprecated
    public void setEmailAddress(String emailAddress)
    {
        setEmailAddress(emailAddress, ComponentAccessor.getComponent(JiraAuthenticationContext.class).getUser(), ComponentAccessor.getComponent(EmailFormatter.class));
    }

    public void setEmailAddress(String emailAddress, ApplicationUser loggedInUser, EmailFormatter emailFormatter)
    {
        this.emailAddress = emailFormatter.formatEmail(emailAddress, loggedInUser != null ? loggedInUser.getDirectoryUser() : null);
    }

    public Map<String, String> getAvatarUrls()
    {
        return avatarUrls;
    }

    public void setAvatarUrls(Map<String, String> avatarUrls)
    {
        this.avatarUrls = avatarUrls;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    /**
     * @deprecated Use {@link #shortBeans(java.util.Collection, JiraBaseUrls, com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.util.EmailFormatter)}
     */
    @Deprecated
    public static Collection<UserJsonBean> shortBeans(final Collection<User> users, final JiraBaseUrls urls)
    {
        return shortBeans(users, urls, ComponentAccessor.getComponent(JiraAuthenticationContext.class).getUser(), ComponentAccessor.getComponent(EmailFormatter.class));
    }

    public static Collection<UserJsonBean> shortBeans(final Collection<User> users, final JiraBaseUrls urls, final ApplicationUser loggedInUser, final EmailFormatter emailFormatter)
    {
        if (users == null)
        {
            return null;
        }
        Collection<UserJsonBean> result = Lists.newArrayListWithCapacity(users.size());
        for (User from : users)
        {
            result.add(shortBean(from, urls, loggedInUser, emailFormatter));
        }

        return result;
    }

    /**
     * @deprecated Use {@link #shortBeanCollection(java.util.Collection, JiraBaseUrls, com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.util.EmailFormatter)}
     */
    @Deprecated
    public static Collection<UserJsonBean> shortBeanCollection(final Collection<ApplicationUser> users, final JiraBaseUrls urls)
    {
        return shortBeanCollection(users, urls, ComponentAccessor.getComponent(JiraAuthenticationContext.class).getUser(), ComponentAccessor.getComponent(EmailFormatter.class));
    }

    public static Collection<UserJsonBean> shortBeanCollection(final Collection<ApplicationUser> users, final JiraBaseUrls urls, final ApplicationUser loggedInUser, final EmailFormatter emailFormatter)
    {
        if (users == null)
        {
            return null;
        }
        Collection<UserJsonBean> result = Lists.newArrayListWithCapacity(users.size());
        for (ApplicationUser from : users)
        {
            result.add(shortBean(from, urls, loggedInUser, emailFormatter));
        }

        return result;
    }

    /**
     * @deprecated Use {@link #shortBean(com.atlassian.jira.user.ApplicationUser, JiraBaseUrls, com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.util.EmailFormatter)}
     */
    @Deprecated
    public static UserJsonBean shortBean(final User user, final JiraBaseUrls urls)
    {
        return shortBean(user, urls, ComponentAccessor.getComponent(JiraAuthenticationContext.class).getUser(), ComponentAccessor.getComponent(EmailFormatter.class));
    }

    /**
     *
     * @return null if the input is null
     */
    public static UserJsonBean shortBean(final User user, final JiraBaseUrls urls, final ApplicationUser loggedInUser, final EmailFormatter emailFormatter)
    {
        if (user == null)
        {
            return null;
        }
        final UserJsonBean bean = new UserJsonBean();
        bean.self = urls.restApi2BaseUrl() + "user?username=" + JiraUrlCodec.encode(user.getName());
        bean.name = user.getName();
        bean.displayName = user.getDisplayName();
        bean.emailAddress = emailFormatter.formatEmail(user, loggedInUser != null ? loggedInUser.getDirectoryUser() : null);
        bean.active  = user.isActive();
        bean.avatarUrls = getAvatarURLs(user);
        return bean;
    }

    /**
     * @deprecated Use {@link #shortBean(com.atlassian.jira.user.ApplicationUser, JiraBaseUrls, com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.util.EmailFormatter)}
     */
    @Deprecated
    public static UserJsonBean shortBean(final ApplicationUser user, final JiraBaseUrls urls)
    {
        return shortBean(user, urls, ComponentAccessor.getComponent(JiraAuthenticationContext.class).getUser(), ComponentAccessor.getComponent(EmailFormatter.class));
    }

    /**
     *
     * @return null if the input is null
     */
    public static UserJsonBean shortBean(final ApplicationUser user, final JiraBaseUrls urls, final ApplicationUser currentUser, final EmailFormatter emailFormatter)
    {
        if (user == null)
        {
            return null;
        }
        final UserJsonBean bean = new UserJsonBean();
        bean.self = urls.restApi2BaseUrl() + "user?username=" + JiraUrlCodec.encode(user.getUsername());
        bean.name = user.getUsername();
        bean.displayName = user.getDisplayName();
        bean.emailAddress = emailFormatter.formatEmail(user.getEmailAddress(), currentUser != null ? currentUser.getDirectoryUser() : null);
        bean.active  = user.isActive();
        bean.avatarUrls = getAvatarURLs(user);
        return bean;
    }

    private static Map<String, String> getAvatarURLs(User user)
    {
        return getAvatarURLs(ApplicationUsers.from(user));
    }

    private static Map<String, String> getAvatarURLs(ApplicationUser user)
    {
        final AvatarService avatarService = ComponentAccessor.getAvatarService();

        final Map<String, String> avatarUrls = new HashMap<String, String>();
        for (Avatar.Size size : Avatar.Size.values())
        {
            final int px = size.getPixels();
            if (px > 48) continue; // TODO JRADEV-20790 - Don't output higher res URLs in our REST endpoints until we start using them ourselves.
            final String sizeName = String.format("%dx%d",px,px);
            avatarUrls.put(sizeName, avatarService.getAvatarAbsoluteURL(user, user, size).toString());
        }
        return avatarUrls;
    }


    public static final UserJsonBean USER_DOC_EXAMPLE = new UserJsonBean();
    public static final UserJsonBean USER_SHORT_DOC_EXAMPLE = new UserJsonBean();
    static
    {
        USER_DOC_EXAMPLE.setSelf("http://www.example.com/jira/rest/api/2/user?username=fred");
        USER_DOC_EXAMPLE.setName("fred");
        USER_DOC_EXAMPLE.emailAddress = "fred@example.com";
        USER_DOC_EXAMPLE.setDisplayName("Fred F. User");
        USER_DOC_EXAMPLE.setActive(true);
        USER_DOC_EXAMPLE.setAvatarUrls(MapBuilder.<String, String>newBuilder()
                .add("16x16", "http://www.example.com/jira/secure/useravatar?size=xsmall&ownerId=fred")
                .add("24x24", "http://www.example.com/jira/secure/useravatar?size=small&ownerId=fred")
                .add("32x32", "http://www.example.com/jira/secure/useravatar?size=medium&ownerId=fred")
                .add("48x48", "http://www.example.com/jira/secure/useravatar?size=large&ownerId=fred")
                .toMap());

        USER_SHORT_DOC_EXAMPLE.setSelf(USER_DOC_EXAMPLE.getSelf());
        USER_SHORT_DOC_EXAMPLE.setName(USER_DOC_EXAMPLE.getName());
        USER_SHORT_DOC_EXAMPLE.setDisplayName(USER_DOC_EXAMPLE.getDisplayName());
    }
}

