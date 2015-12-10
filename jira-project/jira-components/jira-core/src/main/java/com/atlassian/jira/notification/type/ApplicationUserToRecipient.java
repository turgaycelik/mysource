package com.atlassian.jira.notification.type;

import javax.annotation.Nonnull;

import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.user.ApplicationUser;

import com.google.common.base.Function;

/**
 * Converts an {@link ApplicationUser} to a {@link com.atlassian.jira.notification.NotificationRecipient}.
 *
 * @since v6.0
 */
public class ApplicationUserToRecipient implements Function<ApplicationUser, NotificationRecipient>
{
    public static final ApplicationUserToRecipient INSTANCE = new ApplicationUserToRecipient();

    @Override
    public NotificationRecipient apply(@Nonnull ApplicationUser user)
    {
        return new NotificationRecipient(user);
    }
}
