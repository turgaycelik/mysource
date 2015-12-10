package com.atlassian.jira.web.action.admin.notification;

import java.util.Collections;

import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.web.action.RedirectSanitiser;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import webwork.action.Action;

import static com.atlassian.jira.util.ErrorCollectionAssert.assert1FieldError;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static webwork.action.ServletActionContext.setResponse;

@RunWith(MockitoJUnitRunner.class)
public class TestAddScheme
{
    @Mock private NotificationSchemeManager notificationSchemeManager;

    @Rule
    public MockitoContainer mockitoContainer = MockitoMocksInContainer.rule(this);

    @SuppressWarnings("UnusedDeclaration")
    @AvailableInContainer
    private RedirectSanitiser redirectSanitiser = new MockRedirectSanitiser();

    @Test
    public void shouldReturnAnErrorIfTheSchemeNameWasNotSet() throws Exception
    {
        final AddScheme addSchemeAction = new AddScheme(notificationSchemeManager)
        {
            @Override
            protected I18nHelper getI18nHelper()
            {
                return new MockI18nHelper();
            }
        };

        final String actionResult = addSchemeAction.execute();
        assertEquals(Action.INPUT, actionResult);
        assert1FieldError(addSchemeAction, "name", "admin.errors.specify.a.name.for.this.scheme");
    }

    @Test
    public void addingASchemeShouldRedirectToTheEditPageForThatScheme() throws Exception
    {
        final Scheme expectedNotificationScheme = new Scheme(1L, "NotificationScheme", "A Test Notification Scheme",
                ImmutableList.<SchemeEntity>of());

        final HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        setResponse(servletResponse);
        when(notificationSchemeManager.createSchemeObject(anyString(), anyString())).thenReturn(expectedNotificationScheme);

        final AddScheme addSchemeAction = new AddScheme(notificationSchemeManager);
        addSchemeAction.setName("This scheme");

        final String result = addSchemeAction.execute();
        assertEquals(Action.NONE, result);
        verify(servletResponse).sendRedirect("EditNotifications!default.jspa?schemeId=" + expectedNotificationScheme.getId());
    }
}
