package com.atlassian.jira.notification;

import com.atlassian.jira.user.ApplicationUser;

/**
 */
public class MockNotificationRecipient extends NotificationRecipient
{

    private String preference = MIMETYPE_HTML;

    public MockNotificationRecipient(ApplicationUser user)
    {
        super(user);
    }

    public MockNotificationRecipient(String pEmail)
    {
        super(pEmail);
    }

    @Override
    String getFormatPreference(ApplicationUser user)
    {
        return preference;
    }

    public void setFormatPreference(String preference) {
        this.preference = preference;
    }
}
