/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.notification;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.annotation.Nonnull;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A simple data holder class that represents a user and or email address who will be send a noitification from JIRA, usually via email.
 */
@PublicApi
public class NotificationRecipient
{
    public static final String MIMETYPE_HTML = "html";
    public static final String MIMETYPE_HTML_DISPLAY = "HTML";
    public static final String MIMETYPE_TEXT = "text";
    public static final String MIMETYPE_TEXT_DISPLAY = "Text";

    private final ApplicationUser user;
    private final String email;
    private final String format;

    /**
     * The format is set to html or text as specified in jira-application.properties file.
     * If this setting is not configured correctly, default to text format.
     *
     * @param user recipient user
     * @deprecated Use {@link #NotificationRecipient(ApplicationUser)} instead. Since v6.0.
     */
    public NotificationRecipient(@Nonnull final User user)
    {
        this(ApplicationUsers.from(user));
    }

    // package level for testing
    @VisibleForTesting
    String getFormatPreference(ApplicationUser user)
    {
        return ComponentAccessor.getUserPreferencesManager().getExtendedPreferences(user).getString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE);
    }

    /**
     * The format is set to html or text as specified in jira-application.properties file.
     * If this setting is not configured correctly, default to text format.
     *
     * @param user recipient user
     */
    public NotificationRecipient(@Nonnull ApplicationUser user)
    {
        this.user = notNull("It is not valid to build a notification recipient out of 'null' user", user);
        this.email = user.getEmailAddress();
        this.format = MIMETYPE_HTML.equals(getFormatPreference(user)) ? MIMETYPE_HTML : MIMETYPE_TEXT;
    }

    public NotificationRecipient(String pEmail)
    {
        user = null;
        email = pEmail;
        format = MIMETYPE_HTML;
    }

    public String getEmail()
    {
        return email;
    }

    /**
     * Returns a user if this NotificationRecipient was constructed with a user.
     * Returns null if this NotificationRecipient was constructed with an e-mail address only
     *
     * @return user recipient of this notification, or {@code null}, if recipient is an email address.
     * @deprecated Use {@link #getUser()} instead. Since v6.0.
     */
    public User getUserRecipient()
    {
        return ApplicationUsers.toDirectoryUser(user);
    }

    /**
     * Returns a user if this NotificationRecipient was constructed with a user.
     * Returns null if this NotificationRecipient was constructed with an e-mail address only
     *
     * @return user recipient of this notification, or {@code null}, if recipient is an email address.
     */
    public ApplicationUser getUser()
    {
        return user;
    }

    public boolean isHtml()
    {
        return "html".equals(format);
    }

    public String getFormat()
    {
        return format;
    }

    /**
     * Checks if the recipient is in the specified group. If this is only an email address they are not in any group.
     *
     * @param groupName group name
     * @return <code>true</code> if the user is set and is in the group, <code>false</code> otherwise
     */
    public boolean isInGroup(String groupName)
    {
        return user != null && ComponentAccessor.getGroupManager().isUserInGroup(user.getName(), groupName);
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof NotificationRecipient))
        {
            return false;
        }

        final NotificationRecipient other = (NotificationRecipient) o;
        return Objects.equal(email, other.email) && Objects.equal(user, other.user);
    }

    public int hashCode()
    {
        return Objects.hashCode(user, email);
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("user", user).
                append("email", email).
                append("format", format).
                toString();
    }
}
