package com.atlassian.jira.web.action.admin.notification;

import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.scheme.SchemeManager;

import org.hamcrest.core.IsEqual;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class TestViewSchemes
{
    @Test
    public void getSchemeManagerShouldReturnProvidedSchemeManager()
    {
        final NotificationSchemeManager notificationSchemeManager = mock(NotificationSchemeManager.class);

        final ViewSchemes viewSchemes = new ViewSchemes(notificationSchemeManager);

        final SchemeManager actualSchemeManager = viewSchemes.getSchemeManager();

        assertThat(actualSchemeManager, IsEqual.<SchemeManager>equalTo(notificationSchemeManager));
    }
}
