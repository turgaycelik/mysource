package com.atlassian.jira.web.action.admin.notification;

import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.RedirectSanitiser;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;

import webwork.action.Action;
import webwork.action.ServletActionContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestDeleteNotification
{
    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @Mock
    @AvailableInContainer
    private NotificationSchemeManager notificationSchemeManager;

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext authenticationContext;

    @AvailableInContainer
    private RedirectSanitiser redirectSanitiser = new MockRedirectSanitiser();

    @Mock
    @AvailableInContainer
    private ApplicationProperties applicationProperties;

    @Mock
    HttpServletResponse httpServletResponse;

    @Before
    public void setUp() throws Exception
    {
        Mockito.when(authenticationContext.getI18nHelper()).thenReturn(new MockI18nHelper());
        Mockito.when(applicationProperties.getEncoding()).thenReturn("UTF-8");
        ServletActionContext.setResponse(httpServletResponse);
    }

    private DeleteNotification setupDeleteNotification(Long id, boolean confirmed, Long schemeId)
    {
        DeleteNotification dn = new DeleteNotification();
        dn.setId(id);
        dn.setConfirmed(confirmed);
        dn.setSchemeId(schemeId);
        return dn;
    }

    @Test
    public void testValidation() throws Exception
    {
        final DeleteNotification dn = new DeleteNotification();
        final String result = dn.execute();

        assertEquals("Failed validation should return INPUT", Action.INPUT, result);
        assertThat("Failed validation should contain two messages", dn.getErrorMessages(),
                Matchers.containsInAnyOrder("admin.errors.notifications.must.select.notification.to.delete",
                                            "admin.errors.notifications.confirm.deletion"));
    }

    @Test
    public void testExecuteWithoutSpecifiedSchemeId() throws Exception
    {
        DeleteNotification dn = setupDeleteNotification(1L, true, null);

        String result = dn.execute();

        assertEquals(Action.NONE, result);
        verify(notificationSchemeManager, times(1)).deleteEntity(1L);
        verify(httpServletResponse).sendRedirect("ViewNotificationSchemes.jspa");
    }

    @Test
    public void testExecuteWithSchemeId() throws Exception
    {
        DeleteNotification dn = setupDeleteNotification(1111L, true, 1000L);

        String result = dn.execute();

        assertEquals(Action.NONE, result);
        verify(notificationSchemeManager, times(1)).deleteEntity(1111L);
        verify(httpServletResponse).sendRedirect("EditNotifications!default.jspa?schemeId=1000");
    }
}
