package com.atlassian.jira.scheme;

import java.util.Locale;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.notification.DefaultNotificationSchemeManager;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.notification.NotificationTypeManager;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.permission.DefaultPermissionSchemeManager;
import com.atlassian.jira.permission.PermissionContextFactory;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.MockUserLocaleStore;
import com.atlassian.jira.user.UserLocaleStore;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestSchemeManager
{
    DefaultPermissionSchemeManager permSchemeManager;
    DefaultNotificationSchemeManager notificationSchemeManager;
    MockGenericValue permScheme;
    MockGenericValue notificationScheme;
    GenericValue project;
    GenericValue project2;

    @Mock ProjectManager projectManager;
    @Mock PermissionTypeManager permissionTypeManager;
    @Mock PermissionContextFactory permissionContextFactory;
    @Mock NodeAssociationStore nodeAssociationStore;
    @Mock GroupManager groupManager;
    @Mock EventPublisher eventPublisher;
    @Mock NotificationTypeManager notificationTypeManager;
    @Mock JiraAuthenticationContext jiraAuthenticationContext;
    @Mock I18nHelper.BeanFactory beanFactory;
    @Mock UserPreferencesManager userPreferencesManager;

    MockOfBizDelegator delegator;

    @Rule
    public InitMockitoMocks initMockitoMocks = new InitMockitoMocks(this);

    MockComponentWorker mockComponentWorker;

    @Before
    public void setUp() throws Exception
    {
        delegator = spy(new MockOfBizDelegator());


        final CacheManager cacheManager = new MemoryCacheManager();
        permSchemeManager = new DefaultPermissionSchemeManager(projectManager, permissionTypeManager, permissionContextFactory, delegator,
                new DefaultSchemeFactory(), nodeAssociationStore, groupManager, eventPublisher, cacheManager);
        notificationSchemeManager = new DefaultNotificationSchemeManager(projectManager, permissionTypeManager, permissionContextFactory, delegator,
                new DefaultSchemeFactory(), eventPublisher, notificationTypeManager, nodeAssociationStore, groupManager, userPreferencesManager, cacheManager);

        mockComponentWorker = new MockComponentWorker();
        mockComponentWorker.addMock(OfBizDelegator.class, delegator);
        mockComponentWorker.addMock(JiraAuthenticationContext.class, jiraAuthenticationContext);
        mockComponentWorker.addMock(I18nHelper.BeanFactory.class, beanFactory);
        mockComponentWorker.addMock(PermissionSchemeManager.class, permSchemeManager);
        mockComponentWorker.addMock(NotificationSchemeManager.class, notificationSchemeManager);
        mockComponentWorker.addMock(UserLocaleStore.class, new MockUserLocaleStore(Locale.ENGLISH));
        mockComponentWorker.init();

        final MockI18nHelper i18nHelper = new MockI18nHelper();
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);
        when(beanFactory.getInstance(any(Locale.class))).thenReturn(i18nHelper);

        project = UtilsForTests.getTestEntity("Project", FieldMap.build("id", 2L, "lead", "paul"));
        project2 = UtilsForTests.getTestEntity("Project", new FieldMap());

        when(projectManager.getProject(2L)).thenReturn(project);
        when(projectManager.getProjectObj(2L)).thenReturn(new MockProject(project));
    }

    protected void setupSchemes() throws CreateException, GenericEntityException
    {
        //Create a permission scheme and add to the project
        permScheme = (MockGenericValue)permSchemeManager.createScheme("PScheme", "Test Desc");
        permSchemeManager.addSchemeToProject(project, permScheme);
        when(nodeAssociationStore.getSinksFromSource(AbstractSchemeManager.PROJECT_ENTITY_NAME, project.getLong("id"), "PermissionScheme", SchemeManager.PROJECT_ASSOCIATION)).thenReturn(ImmutableList.<GenericValue>of(permScheme));
        when(nodeAssociationStore.getSourceIdsFromSink(permScheme, "Project", SchemeManager.PROJECT_ASSOCIATION)).thenReturn(ImmutableList.of(2L));

        //Create a permission scheme and add to the project
        notificationScheme = (MockGenericValue)notificationSchemeManager.createScheme("NScheme", "Test Desc");
        notificationSchemeManager.addSchemeToProject(project, notificationScheme);
        when(nodeAssociationStore.getSinksFromSource(AbstractSchemeManager.PROJECT_ENTITY_NAME, project.getLong("id"), "NotificationScheme", SchemeManager.PROJECT_ASSOCIATION)).thenReturn(ImmutableList.<GenericValue>of(notificationScheme));
        when(nodeAssociationStore.getSourceIdsFromSink(notificationScheme, "Project", SchemeManager.PROJECT_ASSOCIATION)).thenReturn(ImmutableList.of(2L));
    }

    @Test
    public void testCreateScheme() throws CreateException, GenericEntityException
    {
        setupSchemes();
        assertEquals(1, permSchemeManager.getSchemes().size());
        assertEquals(1, permSchemeManager.getSchemes(project).size());
    }

    @Test
    public void testCopyScheme() throws CreateException, GenericEntityException
    {
        setupSchemes();

        permSchemeManager.copyScheme(permSchemeManager.getScheme("PScheme"));
        assertEquals(2, permSchemeManager.getSchemes().size());
        assertEquals(1, permSchemeManager.getSchemes(project).size());
        assertNotNull(permSchemeManager.getScheme("common.words.copyof [PScheme]"));

        //copy it again and it should add the new number to it
        permSchemeManager.copyScheme(permSchemeManager.getScheme("PScheme"));
        assertNotNull(permSchemeManager.getScheme("common.words.copyxof [2] [PScheme]"));
        assertEquals(3, permSchemeManager.getSchemes().size());

        permSchemeManager.copyScheme(permSchemeManager.getScheme("common.words.copyof [PScheme]"));
        assertEquals(4, permSchemeManager.getSchemes().size());
        assertEquals(1, permSchemeManager.getSchemes(project).size());
        assertNotNull(permSchemeManager.getScheme("common.words.copyof [common.words.copyof [PScheme]]"));

        notificationSchemeManager.copyScheme(notificationSchemeManager.getScheme("NScheme"));
        assertEquals(2, notificationSchemeManager.getSchemes().size());
        assertEquals(1, notificationSchemeManager.getSchemes(project).size());
        assertNotNull(notificationSchemeManager.getScheme("common.words.copyof [NScheme]"));

        //copy it again and it should add the new number to it
        notificationSchemeManager.copyScheme(notificationSchemeManager.getScheme("NScheme"));
        assertNotNull(notificationSchemeManager.getScheme("common.words.copyxof [2] [NScheme]"));
        assertEquals(3, notificationSchemeManager.getSchemes().size());

        notificationSchemeManager.copyScheme(notificationSchemeManager.getScheme("common.words.copyof [NScheme]"));
        assertEquals(4, notificationSchemeManager.getSchemes().size());
        assertEquals(1, notificationSchemeManager.getSchemes(project).size());
        assertNotNull(notificationSchemeManager.getScheme("common.words.copyof [common.words.copyof [NScheme]]"));
    }

    @Test
    public void testCreateEntity() throws CreateException, GenericEntityException
    {
        setupSchemes();

        GenericValue permType1 = permSchemeManager.createSchemeEntity(permScheme, new SchemeEntity("type1", "group", Permissions.PROJECT_ADMIN));
        GenericValue permType2 = permSchemeManager.createSchemeEntity(permScheme, new SchemeEntity("type2", "group", (long) Permissions.PROJECT_ADMIN));
        GenericValue permType3 = permSchemeManager.createSchemeEntity(permScheme, new SchemeEntity("type3", "group", ProjectPermissions.ADMINISTER_PROJECTS));
        GenericValue permType4 = permSchemeManager.createSchemeEntity(permScheme, new SchemeEntity("type4", "group", ProjectPermissions.ADMINISTER_PROJECTS.permissionKey()));

        //Add and invalid entity. The id is an Object
        try
        {
            permSchemeManager.createSchemeEntity(permScheme, new SchemeEntity("type1", "group", new Object()));
            fail("Should have thrown a IllegalArgumentException!");
        }
        catch (IllegalArgumentException e)
        {
        }

        permScheme.setRelated("ChildSchemePermissions", ImmutableList.of(permType1, permType2, permType3, permType4));


        assertEquals(4, permSchemeManager.getEntities(permScheme).size());

        GenericValue notType1 = notificationSchemeManager.createSchemeEntity(notificationScheme, new SchemeEntity("type1", "group", EventType.ISSUE_CREATED_ID));
        GenericValue notType2 = notificationSchemeManager.createSchemeEntity(notificationScheme, new SchemeEntity("type2", "group", EventType.ISSUE_ASSIGNED_ID));

        //Add and invalid entity. The id is a string
        try
        {
            notificationSchemeManager.createSchemeEntity(notificationScheme, new SchemeEntity("type1", "group", "2"));
            fail("Should have thrown a IllegalArgumentException!");
        }
        catch (IllegalArgumentException e)
        {
        }

        notificationScheme.setRelated("ChildNotification", ImmutableList.of(notType1, notType2));

        assertEquals(2, notificationSchemeManager.getEntities(notificationScheme).size());
    }

    @Test
    public void testGetScheme() throws CreateException, GenericEntityException
    {
        setupSchemes();
        assertEquals(1, permSchemeManager.getSchemes().size());
        assertEquals(1, permSchemeManager.getSchemes(project).size());

        assertNotNull(permSchemeManager.getScheme(permScheme.getLong("id")));
        assertNotNull(permSchemeManager.getScheme(permScheme.getString("name")));

        assertEquals(0, permSchemeManager.getSchemes(project2).size());

        assertEquals(1, notificationSchemeManager.getSchemes().size());
        assertEquals(1, notificationSchemeManager.getSchemes(project).size());

        assertNotNull(notificationSchemeManager.getScheme(notificationScheme.getLong("id")));
        assertNotNull(notificationSchemeManager.getScheme(notificationScheme.getString("name")));

        assertEquals(0, notificationSchemeManager.getSchemes(project2).size());

        //test that the SchemeManagers are not using the same schemes
        assertNull(permSchemeManager.getScheme(notificationScheme.getString("name")));
        assertNull(notificationSchemeManager.getScheme(permScheme.getString("name")));
    }

    @Test
    public void testGetEntities() throws CreateException, GenericEntityException
    {
        setupSchemes();

        GenericValue permType1 = permSchemeManager.createSchemeEntity(permScheme, new SchemeEntity("type1", "group", Permissions.PROJECT_ADMIN));
        GenericValue permType2 = permSchemeManager.createSchemeEntity(permScheme, new SchemeEntity("type2", "group2", (long) Permissions.PROJECT_ADMIN));
        GenericValue permType3 = permSchemeManager.createSchemeEntity(permScheme, new SchemeEntity("type3", "group3", ProjectPermissions.ADMINISTER_PROJECTS));
        GenericValue permType4 = permSchemeManager.createSchemeEntity(permScheme, new SchemeEntity("type4", "group4", ProjectPermissions.ADMINISTER_PROJECTS.permissionKey()));

        permScheme.setRelated("ChildSchemePermissions", ImmutableList.of(permType1, permType2, permType3, permType4));


        assertEquals(4, permSchemeManager.getEntities(permScheme).size());

        assertEquals(4, permSchemeManager.getEntities(permScheme, (long )Permissions.PROJECT_ADMIN).size());

        assertEquals(1, permSchemeManager.getEntities(permScheme, (long )Permissions.PROJECT_ADMIN, "group").size());

        assertEquals(1, permSchemeManager.getEntities(permScheme, "type2", (long )Permissions.PROJECT_ADMIN).size());

        GenericValue notType1 = notificationSchemeManager.createSchemeEntity(notificationScheme, new SchemeEntity("type1", "group", EventType.ISSUE_CREATED_ID));
        GenericValue notType2 = notificationSchemeManager.createSchemeEntity(notificationScheme, new SchemeEntity("type2", "group2", EventType.ISSUE_ASSIGNED_ID));

        notificationScheme.setRelated("ChildNotification", ImmutableList.of(notType1, notType2));

        assertEquals(2, notificationSchemeManager.getEntities(notificationScheme).size());


        try
        {
            assertEquals(1, notificationSchemeManager.getEntities(notificationScheme, "2").size());
            fail("Should have thrown a IllegalArgumentException!");
        }
        catch (IllegalArgumentException e)
        {
        }
    }

    /**
     * Default notification scheme does not work with getDefaultScheme as id is not 0
     */
    @Test
    public void testCreateDefaultScheme() throws CreateException, GenericEntityException
    {
        permSchemeManager.createDefaultScheme();
        assertEquals(1, permSchemeManager.getSchemes().size());
        assertNotNull(permSchemeManager.getDefaultScheme());

        notificationSchemeManager.createDefaultScheme();
        assertEquals(1, notificationSchemeManager.getSchemes().size());
//        assertNotNull(notificationSchemeManager.getDefaultScheme());

    }

    @Test
    public void testDeleteScheme() throws CreateException, GenericEntityException
    {
        setupSchemes();
        assertEquals(1, permSchemeManager.getSchemes().size());
        permSchemeManager.deleteScheme(permScheme.getLong("id"));
        assertEquals(0, permSchemeManager.getSchemes().size());

        assertEquals(1, notificationSchemeManager.getSchemes().size());
        notificationSchemeManager.deleteScheme(notificationScheme.getLong("id"));
        assertEquals(0, notificationSchemeManager.getSchemes().size());
    }

    @Test
    public void testDeleteEntity() throws CreateException, GenericEntityException
    {
        setupSchemes();

        GenericValue entity = permSchemeManager.createSchemeEntity(permScheme, new SchemeEntity("type1", "group", Permissions.PROJECT_ADMIN));
        permSchemeManager.deleteEntity(entity.getLong("id"));
        verify(delegator).removeValue(entity);

        GenericValue entity2 = permSchemeManager.createSchemeEntity(permScheme, new SchemeEntity("type2", "group", (long) Permissions.PROJECT_ADMIN));
        permSchemeManager.deleteEntity(entity2.getLong("id"));
        verify(delegator).removeValue(entity2);

        GenericValue entity3 = permSchemeManager.createSchemeEntity(permScheme, new SchemeEntity("type3", "group", ProjectPermissions.ADMINISTER_PROJECTS));
        permSchemeManager.deleteEntity(entity3.getLong("id"));
        verify(delegator).removeValue(entity3);

        GenericValue entity4 = permSchemeManager.createSchemeEntity(permScheme, new SchemeEntity("type4", "group", ProjectPermissions.ADMINISTER_PROJECTS.permissionKey()));
        permSchemeManager.deleteEntity(entity4.getLong("id"));
        verify(delegator).removeValue(entity4);

        GenericValue entity5 = notificationSchemeManager.createSchemeEntity(notificationScheme, new SchemeEntity("type1", "group", EventType.ISSUE_CREATED_ID));
        notificationSchemeManager.deleteEntity(entity5.getLong("id"));
        verify(delegator).removeValue(entity5);
    }

    @Test
    public void testUpdateScheme() throws CreateException, GenericEntityException
    {
        setupSchemes();

        permScheme.set("name", "Test Update Scheme");
        permSchemeManager.updateScheme(permScheme);
        assertEquals(1, permSchemeManager.getSchemes().size());
        assertNotNull(permSchemeManager.getScheme("Test Update Scheme"));

        notificationScheme.set("name", "Test Update Scheme");
        notificationSchemeManager.updateScheme(notificationScheme);
        assertEquals(1, notificationSchemeManager.getSchemes().size());
        assertNotNull(notificationSchemeManager.getScheme("Test Update Scheme"));
    }

    @Test
    public void testGetProjects() throws CreateException, GenericEntityException
    {
        setupSchemes();
        assertEquals(1, permSchemeManager.getProjects(permScheme).size());
        assertEquals(project, permSchemeManager.getProjects(permScheme).get(0));

        assertEquals(1, notificationSchemeManager.getProjects(notificationScheme).size());
        assertEquals(project, notificationSchemeManager.getProjects(notificationScheme).get(0));
    }
}
