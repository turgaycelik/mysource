package com.atlassian.jira.rest.v2.issue;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Used to update user details
 *
 * @since v6.1
 */
@JsonIgnoreProperties (ignoreUnknown = true)
public class UserWriteBean
{
    /**
     * User bean example used in auto-generated documentation.
     */
    private static final UserWriteBean DOC_EXAMPLE_CREATE;
    private static final UserWriteBean DOC_EXAMPLE_CREATED;
    private static final UserWriteBean DOC_EXAMPLE;
    private static final UserWriteBean DOC_EXAMPLE_UPDATE;
    private static final UserWriteBean DOC_EXAMPLE_UPDATED;
    private static final UserWriteBean DOC_EXAMPLE_UPDATE_MYSELF;
    private static final UserWriteBean DOC_EXAMPLE_UPDATED_MYSELF;

    static
    {
        final String key = "charlie";
        DOC_EXAMPLE_CREATE = new Builder()
                .name("charlie")
                .password("abracadabra")
                .emailAddress("charlie@atlassian.com")
                .displayName("Charlie of Atlassian")
                .toUserBean();

        DOC_EXAMPLE_CREATED = new Builder()
                .self(Examples.JIRA_BASE_URL + Examples.REST_BASE_URL + "/user/" + key)
                .key(key)
                .name("charlie")
                .emailAddress("charlie@atlassian.com")
                .displayName("Charlie of Atlassian")
                .toUserBean();

        DOC_EXAMPLE = new Builder()
                .self(Examples.JIRA_BASE_URL + Examples.REST_BASE_URL + "/user/" + key)
                .key(key)
                .name("charlie")
                .emailAddress("charlie@atlassian.com")
                .displayName("Charlie of Atlassian")
                .toUserBean();

        DOC_EXAMPLE_UPDATE = new Builder()
                .name("eddie")
                .emailAddress("eddie@atlassian.com")
                .displayName("Eddie of Atlassian")
                .toUserBean();

        DOC_EXAMPLE_UPDATED = new Builder()
                .self(Examples.JIRA_BASE_URL + Examples.REST_BASE_URL + "/user/" + key)
                .key(key)
                .name("eddie")
                .emailAddress("eddie@atlassian.com")
                .displayName("Eddie of Atlassian")
                .toUserBean();

        DOC_EXAMPLE_UPDATE_MYSELF = new Builder()
                .emailAddress("eddie@atlassian.com")
                .displayName("Eddie of Atlassian")
                .toUserBean();

        DOC_EXAMPLE_UPDATED_MYSELF = new Builder()
                .self(Examples.JIRA_BASE_URL + Examples.REST_BASE_URL + "/user/" + key)
                .key(key)
                .name("eddie")
                .emailAddress("eddie@atlassian.com")
                .displayName("Eddie of Atlassian")
                .toUserBean();

    }

    @JsonProperty
    private String self;

    @JsonProperty
    private String key;

    @JsonProperty
    private String name;

    @JsonProperty
    private String password;

    @JsonProperty
    private String emailAddress;

    @JsonProperty
    private String displayName;

    @JsonProperty
    private String notification;


    private UserWriteBean() {}

    public UserWriteBean(final String self, final String key, final String name, final String password, final String emailAddress, final String displayName, final String notification)
    {
        this.self = self;
        this.key = key;
        this.name = name;
        this.password = password;
        this.emailAddress = emailAddress;
        this.displayName = displayName;
        this.notification = notification;
    }

    //TODO: add those attributes later?
/*
    @JsonProperty
    private Map<String, URI> avatarUrls;
*/

/*
    @JsonProperty
    private String timeZone;

    @JsonProperty
    private SimpleListWrapper<GroupJsonBean> groups;
*/

    public String getSelf()
    {
        return self;
    }

    public String getKey()
    {
        return key;
    }

    public String getName()
    {
        return name;
    }

    public String getPassword()
    {
        return password;
    }

    public String getEmailAddress()
    {
        return emailAddress;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public String getNotification()
    {
        return notification;
    }

    /**
     * Used to aid in the construction of an Immutable User object.
     */
    public static final class Builder
    {
        private String self;

        private String key;

        private String name;

        private String password;

        private String emailAddress;

        private String displayName;

        private String notification;

        /**
         * Returns an immutable User object with the properties set in this builder.
         * @return an immutable User object with the properties set in this builder.
         */
        public UserWriteBean toUserBean()
        {
            return new UserWriteBean(self, key, name, password, emailAddress, displayName, notification);
        }

        public Builder self(final String self)
        {
            this.self = self;
            return this;
        }

        public Builder key(final String key)
        {
            this.key = key;
            return this;
        }

        public Builder name(final String name)
        {
            this.name = name;
            return this;
        }

        public Builder password(final String password)
        {
            this.password = password;
            return this;
        }

        public Builder emailAddress(final String emailAddress)
        {
            this.emailAddress = emailAddress;
            return this;
        }

        public Builder displayName(final String displayName)
        {
            this.displayName = displayName;
            return this;
        }

        public Builder notification(final String notification)
        {
            this.notification = notification;
            return this;
        }
    }
}
