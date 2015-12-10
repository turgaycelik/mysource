package com.atlassian.jira.template;

import java.util.Map;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.local.testutils.UtilsForTestSetup;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.scheme.SchemeEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.event.type.EventType.EVENT_TYPE;
import static com.atlassian.jira.event.type.EventType.ISSUE_CREATED_ID;
import static com.atlassian.jira.event.type.EventType.ISSUE_DELETED_ID;
import static com.atlassian.jira.event.type.EventType.JIRA_SYSTEM_EVENT_TYPE;
import static com.atlassian.jira.template.Template.TEMPLATE_TYPE_FILTERSUB;
import static com.atlassian.jira.template.Template.TEMPLATE_TYPE_ISSUEEVENT;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@SuppressWarnings("deprecation")
public class TestDefaultTemplateManager
{
    static {
        UtilsForTestSetup.loadDatabaseDriver();
    }

    private DefaultTemplateManager templateManager;
    @Mock private ApplicationProperties mockAppProps;
    @Mock private EventTypeManager mockEventTypeManager;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        new MockComponentWorker().init()
                .addMock(EventTypeManager.class, mockEventTypeManager)
                .addMock(OfBizDelegator.class, new MockOfBizDelegator());
        templateManager = new DefaultTemplateManager(mockAppProps, mockEventTypeManager);
    }

    @After
    public void tearDown() throws Exception
    {
        ComponentAccessor.initialiseWorker(null);
        UtilsForTestSetup.deleteAllEntities();
    }

    @Test
    public void testGetIssueEventTemplateTypes()
    {
        assertGetTemplateTypes(TEMPLATE_TYPE_ISSUEEVENT, 17);
    }

    @Test
    public void testGetFilterSubTemplateTypes()
    {
        assertGetTemplateTypes(TEMPLATE_TYPE_FILTERSUB, 1);
    }

    private void assertGetTemplateTypes(final String type, final int expectedCount)
    {
        // Invoke
        final Map<Long, Template> templateMap = templateManager.getTemplatesMap(type);

        // Check
        assertEquals(expectedCount, templateMap.size());
        for (final Template template : templateMap.values())
        {
            assertEquals(type, template.getType());
        }
    }

    @Test
    public void testGetIssueCreatedTemplatebyId()
    {
        // Set up
        final Long issueCreatedTemplateId = 1L;

        // Invoke
        Template template = templateManager.getTemplate(issueCreatedTemplateId);

        // Check
        assertEquals(issueCreatedTemplateId, template.getId());
        assertEquals("Issue Created", template.getName());
        assertEquals(TEMPLATE_TYPE_ISSUEEVENT, template.getType());
    }

    @Test
    public void testGetDefaultTemplate()
    {
        // Add event
        final EventTypeManager eventTypeManager = ComponentAccessor.getEventTypeManager();
        final Map eventTypeParamsMap = EasyMap.build("id", 1L, "name", "Issue Created",
                "description", "This is the issue created event type descrition", "type", JIRA_SYSTEM_EVENT_TYPE);
        final GenericValue issueEventTypeGV = UtilsForTests.getTestEntity(EVENT_TYPE, eventTypeParamsMap);
        final EventType eventType = new EventType(issueEventTypeGV);
        eventTypeManager.addEventType(eventType);

        templateManager = new DefaultTemplateManager(mockAppProps, eventTypeManager);

        // Invoke
        final Template template = templateManager.getDefaultTemplate(eventType);

        // Check
        final Long issueCreatedTemplateId = 1L;
        assertEquals(issueCreatedTemplateId, template.getId());
        assertEquals("Issue Created", template.getName());
        assertEquals(TEMPLATE_TYPE_ISSUEEVENT, template.getType());
    }

    @Test
    public void testGetTemplateByNotificationEntity() throws GenericEntityException
    {
        // Add event
        EventTypeManager eventTypeManager = ComponentAccessor.getEventTypeManager();
        Map eventTypeParamasMap = EasyMap.build("id", 1L, "name", "Issue Created",
                "description", "This is the issue created event type descrition", "type", JIRA_SYSTEM_EVENT_TYPE);
        GenericValue issueEventTypeGV = UtilsForTests.getTestEntity(EVENT_TYPE, eventTypeParamasMap);
        EventType eventType = new EventType(issueEventTypeGV);
        eventTypeManager.addEventType(eventType);
        when(mockEventTypeManager.getEventType(1L)).thenReturn(eventType);

        // The scheme entity is not assocaited with a template - should default back to the event it is associated with.
        SchemeEntity notificationSchemeEntity =
                new SchemeEntity(10000L, "Current_Assignee", null, ISSUE_CREATED_ID, null, null);

        // Invoke
        final Template template = templateManager.getTemplate(notificationSchemeEntity);

        // Check
        Long issueCreatedTemplateId = 1L;
        assertEquals(issueCreatedTemplateId, template.getId());
        assertEquals("Issue Created", template.getName());
        assertEquals(TEMPLATE_TYPE_ISSUEEVENT, template.getType());
    }

    @Test
    public void testGetTemplateByNotificationEntityWithNonDefaultTemplate() throws GenericEntityException
    {
        // Add event
        EventTypeManager eventTypeManager = ComponentAccessor.getEventTypeManager();
        Map eventTypeParamasMap = EasyMap.build("id", 1L, "name", "Issue Created", "description",
                "This is the issue created event type descrition", "type", JIRA_SYSTEM_EVENT_TYPE);
        GenericValue issueEventTypeGV = UtilsForTests.getTestEntity(EVENT_TYPE, eventTypeParamasMap);
        EventType eventType = new EventType(issueEventTypeGV);
        eventTypeManager.addEventType(eventType);

        // The scheme entity is associated with a template - should use this template rather than default associated
        // with event that has been fired. I.e the 'Issue Deleted' event has been fired - but the 'Issue Created'
        // template will be used.
        SchemeEntity notificationSchemeEntity =
                new SchemeEntity(10000L, "Current_Assignee", null, ISSUE_DELETED_ID, ISSUE_CREATED_ID, null);

        // Invoke
        final Template template = templateManager.getTemplate(notificationSchemeEntity);

        // Check
        Long issueCreatedTemplateId = 1L;
        assertEquals(issueCreatedTemplateId, template.getId());
        assertEquals("Issue Created", template.getName());
        assertEquals(TEMPLATE_TYPE_ISSUEEVENT, template.getType());
    }
}
