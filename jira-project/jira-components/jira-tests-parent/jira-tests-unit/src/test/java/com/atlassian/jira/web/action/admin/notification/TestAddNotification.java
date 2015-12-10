package com.atlassian.jira.web.action.admin.notification;

import java.util.Map;

import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.notification.NotificationType;
import com.atlassian.jira.notification.NotificationTypeManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.RedirectSanitiser;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockitoAnnotations.Mock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestAddNotification
{

    private final static Long SELECTED_PROJECT_ID = 11l;
    private final static Long SCHEME_ID = 1l;
    private final static Long[] EVENT_TYPE_IDS = new Long[] { 5l, 10l, 15l };
    private final static String TYPE = "EHLO";

    @Rule
    public MockitoContainer mockitoContainer = new MockitoContainer(this);

    @Mock
    @AvailableInContainer
    private EventTypeManager eventTypeManager;
    @Mock
    @AvailableInContainer
    private NotificationTypeManager notificationTypeManager;
    @Mock
    @AvailableInContainer
    private NotificationSchemeManager notificationSchemeManager;
    @Mock
    @AvailableInContainer
    private ProjectManager projectManager;
    @Mock
    @AvailableInContainer
    private RedirectSanitiser redirectSanitiser;
    @Mock
    @AvailableInContainer
    JiraAuthenticationContext jiraAuthenticationContext;

    private I18nHelper i18nHelper = new MockI18nHelper();

    @Before
    public void setUp() throws GenericEntityException
    {
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);

        NotificationType notificationType = mock(NotificationType.class);
        when(notificationTypeManager.getNotificationType(TYPE)).thenReturn(notificationType);
        for (Long eventTypeId : EVENT_TYPE_IDS)
        {
            when(notificationSchemeManager.hasEntities(any(GenericValue.class), eq(eventTypeId), eq(TYPE), anyString(), isNull(Long.class))).thenReturn(true);
        }
    }

    @Test
    public void testDoExecuteDoesNotDuplicateEntities() throws Exception
    {
        AddNotification addNotification = createAddNotification(SCHEME_ID, EVENT_TYPE_IDS, TYPE, SELECTED_PROJECT_ID);

        final String redirectUrl = addNotification.doExecute();

        verify(notificationSchemeManager, never()).createSchemeEntity(any(GenericValue.class), any(SchemeEntity.class));
    }

    @Test
    public void testDoExecuteCalledCreateSchemeEntityWithCorrectParams() throws Exception
    {
        Long newEventTypeId = 34l;
        when(notificationSchemeManager.hasEntities(any(GenericValue.class), eq(newEventTypeId), eq(TYPE), anyString(), isNull(Long.class))).thenReturn(false);
        GenericValue mockScheme = mock(GenericValue.class);

        AddNotification addNotification = createAddNotification(SCHEME_ID, new Long[] { newEventTypeId }, TYPE, SELECTED_PROJECT_ID);
        when(notificationSchemeManager.getScheme(SCHEME_ID)).thenReturn(mockScheme);

        final String redirectUrl = addNotification.doExecute();

        verify(notificationSchemeManager).createSchemeEntity(eq(mockScheme), any(SchemeEntity.class));
    }

    @Test
    public void testDoValidationChecksSchemeNotNull() throws GenericEntityException
    {
        assertArrayEquals(new String[] { "admin.errors.notifications.must.select.scheme", "admin.errors.notifications.must.select.type" },
                validateAddNotification(null, null, null, null));
        when(notificationSchemeManager.getScheme(SCHEME_ID)).thenReturn(mock(GenericValue.class));
        assertArrayEquals(new String[] { "admin.errors.notifications.must.select.type" }, validateAddNotification(SCHEME_ID, null, null, null));
    }

    @Test
    public void testDoValidationChecksEventTypeIdsNotNull()
    {
        Map<String, String> errors = validateAddNotificationWithErrors(SCHEME_ID, null, TYPE, null);
        assertThat(errors.keySet(), hasItem("eventTypeIds"));
        assertEquals("admin.errors.notifications.must.select.notification.to.add", errors.get("eventTypeIds"));

        assertThat(validateAddNotificationWithErrors(SCHEME_ID, EVENT_TYPE_IDS, TYPE, null).keySet(), not(hasItem("eventTypeIds")));
    }

    @Test
    public void testDoValidationChecksTypeStringIsSet() throws GenericEntityException
    {
        when(notificationSchemeManager.getScheme(SCHEME_ID)).thenReturn(mock(GenericValue.class));
        assertArrayEquals(new String[] { "admin.errors.notifications.must.select.type" }, validateAddNotification(SCHEME_ID, null, null, null));
        assertArrayEquals(new String[] { "admin.errors.notifications.fill.out.box" }, validateAddNotification(SCHEME_ID, null, TYPE, null));
    }

    @Test
    public void testDoValidationValidatesNotificationType() throws GenericEntityException
    {
        when(notificationSchemeManager.getScheme(SCHEME_ID)).thenReturn(mock(GenericValue.class));
        assertArrayEquals(new String[] { "admin.errors.notifications.fill.out.box" }, validateAddNotification(SCHEME_ID, null, TYPE, null));

        NotificationType notificationType = mock(NotificationType.class);
        when(notificationType.doValidation(anyString(), any(Map.class))).thenReturn(true);
        when(notificationTypeManager.getNotificationType(TYPE)).thenReturn(notificationType);
        assertArrayEquals(new String[] { }, validateAddNotification(SCHEME_ID, null, TYPE, null));
    }

    @Test
    public void testDoValidationDoesntThrowGenericEntityException() throws Exception
    {
        when(notificationSchemeManager.getScheme(SCHEME_ID)).thenThrow(new GenericEntityException(""));
        assertArrayEquals(new String[] { "admin.errors.notifications.error.occured [\n]" }, validateAddNotification(SCHEME_ID, null, null, null));
    }

    private String[] validateAddNotification(Long schemeId, Long[] eventTypeIds, String type, Long selectedProjectId)
    {
        AddNotification addNotification = createAddNotification(schemeId, eventTypeIds, type, selectedProjectId);
        addNotification.doValidation();
        return addNotification.getErrorMessages().toArray(new String[] { });
    }

    private Map<String, String> validateAddNotificationWithErrors(Long schemeId, Long[] eventTypeIds, String type, Long selectedProjectId)
    {
        AddNotification addNotification = createAddNotification(schemeId, eventTypeIds, type, selectedProjectId);
        addNotification.doValidation();
        return addNotification.getErrors();
    }

    private AddNotification createAddNotification(Long schemeId, Long[] eventTypeIds, String type, Long selectedProjectId)
    {
        AddNotification addNotification = new AddNotification(eventTypeManager, notificationTypeManager, notificationSchemeManager);
        addNotification.setSelectedProjectId(selectedProjectId);
        addNotification.setSchemeId(schemeId);
        addNotification.setEventTypeIds(eventTypeIds);
        addNotification.setType(type);
        return addNotification;
    }

}