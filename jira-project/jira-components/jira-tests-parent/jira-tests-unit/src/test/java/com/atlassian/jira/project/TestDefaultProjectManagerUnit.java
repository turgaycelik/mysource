package com.atlassian.jira.project;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.event.project.ProjectAvatarUpdateEvent;
import com.atlassian.jira.event.project.ProjectCategoryChangeEvent;
import com.atlassian.jira.event.project.ProjectCategoryUpdateEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.junit.rules.MockComponentContainer;
import com.atlassian.jira.matchers.ErrorMatchers;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.util.ProjectKeyStoreImpl;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.transaction.MockTransactionSupport;
import com.atlassian.jira.transaction.TransactionSupport;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.opensymphony.module.propertyset.PropertySet;

import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.project.ProjectRelationConstants.PROJECT_CATEGORY_ASSOC;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
public class TestDefaultProjectManagerUnit
{

    @Rule
    public InitMockitoMocks mocks = new InitMockitoMocks(this);

    @Rule
    public MockComponentContainer container = new MockComponentContainer(this);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private OfBizDelegator mockDelegator;

    @Mock
    private PropertiesManager propertiesManager;

    @Mock
    private PropertySet propertySet;

    @Mock
    private JsonEntityPropertyManager jsonEntityPropertyManager;

    private TransactionSupport transactionSupport = new MockTransactionSupport();

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private NodeAssociationStore nodeAssociationStore;

    @Mock
    private ProjectCategoryStore projectCategoryStore;

    @Before
    public void setup()
    {
        container.addMock(OfBizDelegator.class, mockDelegator);
    }

    @Test
    public void testCreateProjectValidation()
    {

        final ProjectRoleManager projectRoleManager = mock(ProjectRoleManager.class);
        final GenericValue mockGenericValue = new MockGenericValue("Project", ImmutableMap.of("id", 1000L, "key", "key"));
        final ProjectImpl project = new ProjectImpl(mockGenericValue);
        final FieldMap params = new FieldMap()
                .add("key", "key")
                .add("originalkey", "key")
                .add("name", "name")
                .add("url", null)
                .add("lead", "lead")
                .add("description", null)
                .add("counter", 0L)
                .add("assigneetype", null)
                .add("avatar", 12345L);

        when(mockDelegator.createValue("Project", params)).thenReturn(mockGenericValue);


        final DefaultProjectManager projectManager = new DefaultProjectManager(mockDelegator, null, null, null, null,
                null, null, null, null, new ProjectKeyStoreImpl(mockDelegator), transactionSupport, null, jsonEntityPropertyManager, eventPublisher);
        try
        {
            projectManager.createProject(null, "KEY", "Some description", "lead", "http://blah/", new Long(3));
            fail("Should have thrown an error about the name");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("name should not be null!", e.getMessage());
        }
        try
        {
            projectManager.createProject("name", null, "Some description", "lead", "http://blah/", new Long(3));
            fail("Should have thrown an error about the key");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("key should not be null!", e.getMessage());
        }

        try
        {
            projectManager.createProject("name", "key", "Some description", null, "http://blah/", new Long(3));
            fail("Should have thrown an error about the lead");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("lead should not be null!", e.getMessage());
        }

        final DefaultProjectManager projectManager2 = new DefaultProjectManager(mockDelegator, null, null, projectRoleManager,
                null, getMockAvatarManager(), null, null, null, new ProjectKeyStoreImpl(mockDelegator), transactionSupport, null, jsonEntityPropertyManager, eventPublisher);
        try
        {
            projectManager2.createProject("name", "key", null, "lead", null, null);
        }
        catch (IllegalArgumentException e)
        {
            fail("Optional arguments are being required!");
        }
        verify(projectRoleManager).applyDefaultsRolesToProject(project);
    }

    @Test
    public void testCreateProject()
    {
        final FieldMap params = new FieldMap()
                .add("key", "HSP")
                .add("originalkey", "HSP")
                .add("name", "homosapien")
                .add("url", "http://blah/")
                .add("lead", "lead")
                .add("description", "Project about humans")
                .add("counter", 0L)
                .add("assigneetype", 3L)
                .add("avatar", 12345L);
        final FieldMap result = new FieldMap(params).add("id", 1000L);
        when(mockDelegator.createValue("Project", params)).thenReturn(new MockGenericValue("Project", result));
        ProjectRoleManager projectRoleManager = mock(ProjectRoleManager.class);

        final DefaultProjectManager projectManager2 = new DefaultProjectManager(mockDelegator, null, null, projectRoleManager,
                null, getMockAvatarManager(), null, null, null, new ProjectKeyStoreImpl(mockDelegator), transactionSupport, null, jsonEntityPropertyManager, eventPublisher);
        final Project project = projectManager2.createProject("homosapien", "HSP", "Project about humans", "lead", "http://blah/", new Long(3));
        verify(projectRoleManager).applyDefaultsRolesToProject(project);

        assertThat(project.getId(), equalTo(result.get("id")));
        assertThat(project.getName(), equalTo(result.get("name")));
        assertThat(project.getKey(), equalTo(result.get("key")));
        assertThat(project.getUrl(), equalTo(result.get("url")));
        assertThat(project.getLeadUserKey(), equalTo(result.get("lead")));
        assertThat(project.getDescription(), equalTo(result.get("description")));
    }

    @Test
    public void testGetProjectObjByCurrentKeyIgnoreCaseFoundByAllProjects() throws Exception
    {
        final MockProject mockProject = new MockProject(456, "DEF");
        final DefaultProjectManager defaultProjectManager = new DefaultProjectManager(mockDelegator, null, null, null,
                null, null, null, null, null, new ProjectKeyStoreImpl(mockDelegator), transactionSupport, null, jsonEntityPropertyManager, eventPublisher)
        {
            @Override
            public GenericValue getProjectByKey(final String key)
            {
                return null;
            }

            @Override
            public List<Project> getProjectObjects() throws DataAccessException
            {
                return CollectionBuilder.<Project>newBuilder(new MockProject(123, "ABC"), mockProject).asList();
            }
        };

        assertEquals(mockProject, defaultProjectManager.getProjectByCurrentKeyIgnoreCase("dEf"));
    }

    @Test
    public void testGetAllProjectKeys()
    {
        when(mockDelegator.findByAnd(eq("ProjectKey"), eq(MapBuilder.<String, Object>newBuilder().add("projectId", 11l).toMap()))).thenReturn(
                Lists.<GenericValue>newArrayList(
                        new MockGenericValue("ProjectKey", ImmutableMap.of("projectId", 11l, "projectKey", "ABC")),
                        new MockGenericValue("ProjectKey", ImmutableMap.of("projectId", 11l, "projectKey", "OLDKEY")))
        );

        final DefaultProjectManager projectManager = new DefaultProjectManager(mockDelegator, null, null, null,
                null, null, null, null, null, new ProjectKeyStoreImpl(mockDelegator), transactionSupport, null, jsonEntityPropertyManager, eventPublisher);

        Collection<String> projectKeys = projectManager.getAllProjectKeys(11l);
        assertThat(projectKeys, IsCollectionWithSize.hasSize(2));
        assertThat(projectKeys, IsCollectionContaining.hasItems("ABC", "OLDKEY"));
    }

    @Test
    public void testGetProjectByKeyIgnoreCaseFindsByPreviousKey() throws Exception
    {
        final MockProject mockProject = new MockProject(456, "XCV");

        when(mockDelegator.findById("Project", mockProject.getId())).thenReturn(mockProject.getGenericValue());

        final DefaultProjectManager defaultProjectManager = new DefaultProjectManager(mockDelegator, null, new MockProjectFactory(), null,
                null, null, null, null, null, new ProjectKeyStoreImpl(mockDelegator) {
            @Nonnull
            @Override
            public Map<String, Long> getAllProjectKeys()
            {
                return ImmutableMap.of("DEF", 456l);
            }
        }, transactionSupport, null, jsonEntityPropertyManager, eventPublisher);

        assertThat(mockProject.getKey(), equalTo(defaultProjectManager.getProjectObjByKeyIgnoreCase("dEf").getKey()));
        assertThat(mockProject.getName(), equalTo(defaultProjectManager.getProjectObjByKeyIgnoreCase("dEf").getName()));
    }

    @Test
    public void testGetProjectObjByKeyIgnoreCaseFoundByExactMatch() throws Exception
    {
        final MockGenericValue genericValue = new MockGenericValue("Project", EasyMap.build("id", 123L, "key", "DEF"));
        final ProjectFactory projectFactory = mock(ProjectFactory.class);
        when(projectFactory.getProject(genericValue)).thenReturn(new ProjectImpl(genericValue));
        final DefaultProjectManager defaultProjectManager = new DefaultProjectManager(mockDelegator, null, projectFactory,
                null, null, null, null, null, null, new ProjectKeyStoreImpl(mockDelegator), transactionSupport, null, jsonEntityPropertyManager, eventPublisher)
        {
            @Override
            public GenericValue getProjectByKey(final String key)
            {
                return genericValue;
            }

            @Override
            public List<Project> getProjectObjects() throws DataAccessException
            {
                return Collections.emptyList();
            }
        };

        assertEquals("DEF", defaultProjectManager.getProjectByCurrentKeyIgnoreCase("DEF").getKey());
    }

    @Test
    public void testUpdateEntityLinks() throws Exception
    {
        final DefaultProjectManager defaultProjectManager = new DefaultProjectManager(mockDelegator, null, null,
                null, null, null, null, null, null, null, null, propertiesManager, jsonEntityPropertyManager, eventPublisher);
        when(propertiesManager.getPropertySet()).thenReturn(propertySet);
        when(propertySet.getKeys("applinks.local.OLD.")).thenReturn(ImmutableSet.of("applinks.local.OLD.1", "applinks.local.OLD.2"));
        when(propertySet.getText("applinks.local.OLD.1")).thenReturn("data1");
        when(propertySet.getText("applinks.local.OLD.2")).thenReturn("data2");
        when(propertySet.getText("applinks.local.OLDER.3")).thenReturn("data3");

        defaultProjectManager.updateEntityLinks("OLD", "NEW");

        verify(propertySet).remove("applinks.local.OLD.1");
        verify(propertySet).remove("applinks.local.OLD.2");
        verify(propertySet).setText("applinks.local.NEW.1", "data1");
        verify(propertySet).setText("applinks.local.NEW.2", "data2");
        verify(propertySet, never()).setText(eq("applinks.local.NEWER.3"), anyString());
        verify(propertySet, never()).remove("applinks.local.OLDER.3");
    }

    @Test
    public void testUpdateEntityLinksWithHackyKey() throws Exception
    {
        final DefaultProjectManager defaultProjectManager = new DefaultProjectManager(mockDelegator, null, null,
                null, null, null, null, null, null, null, null, propertiesManager, jsonEntityPropertyManager, eventPublisher);
        when(propertiesManager.getPropertySet()).thenReturn(propertySet);
        when(propertySet.getKeys("applinks.local.local.")).thenReturn(ImmutableSet.of("applinks.local.local.1", "applinks.local.local.2"));
        when(propertySet.getText("applinks.local.local.1")).thenReturn("data1");
        when(propertySet.getText("applinks.local.local.2")).thenReturn("data2");

        defaultProjectManager.updateEntityLinks("local", "LOCAL");

        verify(propertySet).remove("applinks.local.local.1");
        verify(propertySet).remove("applinks.local.local.2");
        verify(propertySet).setText("applinks.local.LOCAL.1", "data1");
        verify(propertySet).setText("applinks.local.LOCAL.2", "data2");
    }

    @Test
    public void projectCategoryChangeShouldThrowExceptionWhenProjectIsNull() {

        final ProjectCategory mockProjectCategory = mock(ProjectCategory.class);
        final DefaultProjectManager defaultProjectManager = new DefaultProjectManager(mockDelegator, null, null,
                null, null, null, null, null, null, null, null, propertiesManager, jsonEntityPropertyManager, eventPublisher);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expect(ErrorMatchers.withMessage("Cannot associate a category with a null project"));
        defaultProjectManager.setProjectCategory(null, mockProjectCategory);
    }

    @Test
    public void projectCategoryChangeEventShouldBeEmptyWhenProjectCategoryIsNullAndNoCategoryAssigned() {

        final Project mockProject = mock(Project.class);
        final DefaultProjectManager defaultProjectManager = new DefaultProjectManager(mockDelegator, null, null,
                null, null, null, null, null, null, null, null, propertiesManager, jsonEntityPropertyManager, eventPublisher) {
            @Override
            public ProjectCategory getProjectCategoryForProject(final Project project) throws DataAccessException
            {
                return null;
            }
        };

        defaultProjectManager.setProjectCategory(mockProject, null);

        verify(eventPublisher, never()).publish(any());
    }

    @Test
    public void projectCategoryChangeEventShouldBePublishedWhenOldCategoryChangedToNone() {

        final Project mockProject = mock(Project.class);
        final ProjectCategory mockOldProjectCategory = mock(ProjectCategory.class);

        final DefaultProjectManager defaultProjectManager = new DefaultProjectManager(mockDelegator, nodeAssociationStore, null,
                null, null, null, null, null, null, null, null, propertiesManager, jsonEntityPropertyManager, eventPublisher) {
            @Override
            public ProjectCategory getProjectCategoryForProject(final Project project) throws DataAccessException
            {
                return mockOldProjectCategory;
            }
        };

        when(mockOldProjectCategory.getId()).thenReturn(1L);
        when(mockOldProjectCategory.getName()).thenReturn("Old Category");

        defaultProjectManager.setProjectCategory(mockProject, null);

        verify(nodeAssociationStore).removeAssociation(ProjectRelationConstants.PROJECT_CATEGORY_ASSOC, mockProject.getId(), mockOldProjectCategory.getId());

        final ProjectCategoryChangeEvent.Builder builder = new ProjectCategoryChangeEvent.Builder(mockProject);
        builder.addProject(mockProject);
        builder.addOldCategory(mockOldProjectCategory);
        final ProjectCategoryChangeEvent event = builder.build();

        ArgumentCaptor<ProjectCategoryChangeEvent> argument = ArgumentCaptor.forClass(ProjectCategoryChangeEvent.class);
        verify(eventPublisher).publish(argument.capture());
        assertEquals(event, argument.getValue());
    }

    @Test
    public void projectCategoryChangeEventShouldBePublishedWhenOldCategoryChangedToNew() {

        final Project mockProject = mock(Project.class);
        final ProjectCategory mockNewProjectCategory = mock(ProjectCategory.class);
        final ProjectCategory mockOldProjectCategory = mock(ProjectCategory.class);

        final DefaultProjectManager defaultProjectManager = new DefaultProjectManager(mockDelegator, nodeAssociationStore, null,
                null, null, null, null, null, null, null, null, propertiesManager, jsonEntityPropertyManager, eventPublisher) {
            @Override
            public ProjectCategory getProjectCategoryForProject(final Project project) throws DataAccessException
            {
                return mockOldProjectCategory;
            }
        };

        when(mockOldProjectCategory.getId()).thenReturn(1L);
        when(mockOldProjectCategory.getName()).thenReturn("Old Category");
        when(mockNewProjectCategory.getId()).thenReturn(2L);
        when(mockNewProjectCategory.getName()).thenReturn("New Category");

        defaultProjectManager.setProjectCategory(mockProject, mockNewProjectCategory);

        verify(nodeAssociationStore).removeAssociation(ProjectRelationConstants.PROJECT_CATEGORY_ASSOC, mockProject.getId(), mockOldProjectCategory.getId());
        verify(nodeAssociationStore).createAssociation(PROJECT_CATEGORY_ASSOC, mockProject.getId(), mockNewProjectCategory.getId());

        final ProjectCategoryChangeEvent.Builder builder = new ProjectCategoryChangeEvent.Builder(mockProject);
        builder.addOldCategory(mockOldProjectCategory);
        builder.addNewCategory(mockNewProjectCategory);
        builder.addProject(mockProject);
        final ProjectCategoryChangeEvent event = builder.build();

        ArgumentCaptor<ProjectCategoryChangeEvent> argument = ArgumentCaptor.forClass(ProjectCategoryChangeEvent.class);
        verify(eventPublisher).publish(argument.capture());
        assertEquals(event,argument.getValue());
    }

    @Test
    public void projectCategoryChangeEventShouldBePublishedWhenNoneChangedToNew() {

        final Project mockProject = mock(Project.class);
        final ProjectCategory mockNewProjectCategory = mock(ProjectCategory.class);

        final DefaultProjectManager defaultProjectManager = new DefaultProjectManager(mockDelegator, nodeAssociationStore, null,
                null, null, null, null, null, null, null, null, propertiesManager, jsonEntityPropertyManager, eventPublisher) {
            @Override
            public ProjectCategory getProjectCategoryForProject(final Project project) throws DataAccessException
            {
                return mockNewProjectCategory;
            }
        };

        when(mockNewProjectCategory.getId()).thenReturn(2L);
        when(mockNewProjectCategory.getName()).thenReturn("New Category");

        defaultProjectManager.setProjectCategory(mockProject, mockNewProjectCategory);

        ArgumentCaptor<ProjectCategoryChangeEvent> argument = ArgumentCaptor.forClass(ProjectCategoryChangeEvent.class);
        verify(eventPublisher,never()).publish(argument.capture());
    }

    @Test
    public void projectCategoryChangeEventShouldNotBePublishedWhenCategoryNotChanged() {

        final Project mockProject = mock(Project.class);
        final ProjectCategory mockNewProjectCategory = mock(ProjectCategory.class);

        final DefaultProjectManager defaultProjectManager = new DefaultProjectManager(mockDelegator, nodeAssociationStore, null,
                null, null, null, null, null, null, null, null, propertiesManager, jsonEntityPropertyManager, eventPublisher) {
            @Override
            public ProjectCategory getProjectCategoryForProject(final Project project) throws DataAccessException
            {
                return null;
            }
        };

        when(mockNewProjectCategory.getId()).thenReturn(2L);
        when(mockNewProjectCategory.getName()).thenReturn("New Category");

        defaultProjectManager.setProjectCategory(mockProject, mockNewProjectCategory);

        verify(nodeAssociationStore).createAssociation(PROJECT_CATEGORY_ASSOC, mockProject.getId(), mockNewProjectCategory.getId());

        final ProjectCategoryChangeEvent.Builder builder = new ProjectCategoryChangeEvent.Builder(mockProject);
        builder.addNewCategory(mockNewProjectCategory);
        final ProjectCategoryChangeEvent event = builder.build();

        ArgumentCaptor<ProjectCategoryChangeEvent> argument = ArgumentCaptor.forClass(ProjectCategoryChangeEvent.class);
        verify(eventPublisher).publish(argument.capture());
        assertEquals(event,argument.getValue());
    }

    @Test
    public void checkAvatarChangeEventIsPublished()
    {
        final Project mockProject = mock(Project.class);
        final Avatar mockAvatar = mock(Avatar.class);
        final GenericValue mockGenericValue = mock(GenericValue.class);
        final IssueSecurityLevelManager mockIssueSecurityLevelManager = mock(IssueSecurityLevelManager.class);

        final DefaultProjectManager defaultProjectManager = new DefaultProjectManager(mockDelegator, nodeAssociationStore, null,
                null, null, null, null, null, null, null, transactionSupport, null, jsonEntityPropertyManager, eventPublisher);

        container.addMock(IssueSecurityLevelManager.class, mockIssueSecurityLevelManager);
        when(mockDelegator.makeValue("Project")).thenReturn(mockGenericValue);
        when(mockProject.getId()).thenReturn(10L);

        when(mockProject.getAvatar()).thenReturn(mockAvatar);
        when(mockAvatar.getId()).thenReturn(100L);

        defaultProjectManager.updateProject(mockProject, "Name", "Desc", "admin", "url", 1L, 200L, null);

        ProjectAvatarUpdateEvent event = new ProjectAvatarUpdateEvent(mockProject, 200L);
        ArgumentCaptor<ProjectCategoryChangeEvent> argument = ArgumentCaptor.forClass(ProjectCategoryChangeEvent.class);
        verify(eventPublisher).publish(argument.capture());
        assertEquals(event,argument.getValue());
    }

    @Test
    public void checkAvatarChangeEventIsNotPublishWhenAvatarIdNotChanged()
    {
        final Project mockProject = mock(Project.class);
        final Avatar mockAvatar = mock(Avatar.class);
        final GenericValue mockGenericValue = mock(GenericValue.class);
        final IssueSecurityLevelManager mockIssueSecurityLevelManager = mock(IssueSecurityLevelManager.class);

        final DefaultProjectManager defaultProjectManager = new DefaultProjectManager(mockDelegator, nodeAssociationStore, null,
                null, null, null, null, null, null, null, transactionSupport, null, jsonEntityPropertyManager, eventPublisher);

        container.addMock(IssueSecurityLevelManager.class, mockIssueSecurityLevelManager);
        when(mockDelegator.makeValue("Project")).thenReturn(mockGenericValue);
        when(mockProject.getId()).thenReturn(10L);
        when(mockProject.getAvatar()).thenReturn(mockAvatar);
        when(mockAvatar.getId()).thenReturn(100L);

        defaultProjectManager.updateProject(mockProject, "Name", "Desc", "admin", "url", 1L, 100L, null);

        final ArgumentCaptor<ProjectCategoryChangeEvent> argument = ArgumentCaptor.forClass(ProjectCategoryChangeEvent.class);
        verify(eventPublisher, never()).publish(argument.capture());
    }

    @Test
    public void checkEventNotPublishedWhenThereIsNoChangeInCategory()
    {
        final ProjectCategory mockOldProjectCategory = mock(ProjectCategory.class);
        final ProjectCategory mockNewProjectCategory = mock(ProjectCategory.class);

        final DefaultProjectManager defaultProjectManager = new DefaultProjectManager(mockDelegator, nodeAssociationStore, null,
                null, null, null, null, projectCategoryStore, null, null, transactionSupport, null, jsonEntityPropertyManager, eventPublisher);

        when(mockOldProjectCategory.getId()).thenReturn(1L);
        when(mockOldProjectCategory.getName()).thenReturn("Category");
        when(mockOldProjectCategory.getDescription()).thenReturn("Description");

        when(mockNewProjectCategory.getId()).thenReturn(1L);
        when(mockNewProjectCategory.getName()).thenReturn("Category");
        when(mockNewProjectCategory.getDescription()).thenReturn("Description");

        when(projectCategoryStore.getProjectCategory(1L)).thenReturn(mockOldProjectCategory);

        defaultProjectManager.updateProjectCategory(mockNewProjectCategory);

        verify(eventPublisher, never()).publish(any());
        verify(projectCategoryStore, never()).updateProjectCategory(mockNewProjectCategory);
    }

    @Test
    public void checkEventPublishedWhenCategoryNameIsUpdated()
    {
        final ProjectCategory mockOldProjectCategory = mock(ProjectCategory.class);
        final ProjectCategory mockNewProjectCategory = mock(ProjectCategory.class);

        final DefaultProjectManager defaultProjectManager = new DefaultProjectManager(mockDelegator, nodeAssociationStore, null,
                null, null, null, null, projectCategoryStore, null, null, transactionSupport, null, jsonEntityPropertyManager, eventPublisher);

        when(mockOldProjectCategory.getId()).thenReturn(1L);
        when(mockOldProjectCategory.getName()).thenReturn("Category");
        when(mockOldProjectCategory.getDescription()).thenReturn("Description");

        when(mockNewProjectCategory.getId()).thenReturn(1L);
        when(mockNewProjectCategory.getName()).thenReturn("New Category");
        when(mockNewProjectCategory.getDescription()).thenReturn("Description");

        when(projectCategoryStore.getProjectCategory(1L)).thenReturn(mockOldProjectCategory);

        defaultProjectManager.updateProjectCategory(mockNewProjectCategory);

        final ProjectCategoryUpdateEvent event = new ProjectCategoryUpdateEvent(mockOldProjectCategory, mockNewProjectCategory);
        final ArgumentCaptor<ProjectCategoryChangeEvent> argument = ArgumentCaptor.forClass(ProjectCategoryChangeEvent.class);
        verify(eventPublisher).publish(argument.capture());
        assertEquals(event,argument.getValue());

        verify(projectCategoryStore).updateProjectCategory(mockNewProjectCategory);
    }

    @Test
    public void checkEventPublishedWhenCategoryDescriptionIsUpdated()
    {
        final ProjectCategory mockOldProjectCategory = mock(ProjectCategory.class);
        final ProjectCategory mockNewProjectCategory = mock(ProjectCategory.class);

        final DefaultProjectManager defaultProjectManager = new DefaultProjectManager(mockDelegator, nodeAssociationStore, null,
                null, null, null, null, projectCategoryStore, null, null, transactionSupport, null, jsonEntityPropertyManager, eventPublisher);

        when(mockOldProjectCategory.getId()).thenReturn(1L);
        when(mockOldProjectCategory.getName()).thenReturn("Category");
        when(mockOldProjectCategory.getDescription()).thenReturn("Description");

        when(mockNewProjectCategory.getId()).thenReturn(1L);
        when(mockNewProjectCategory.getName()).thenReturn("Category");
        when(mockNewProjectCategory.getDescription()).thenReturn("New Description");

        when(projectCategoryStore.getProjectCategory(1L)).thenReturn(mockOldProjectCategory);

        defaultProjectManager.updateProjectCategory(mockNewProjectCategory);

        final ProjectCategoryUpdateEvent event = new ProjectCategoryUpdateEvent(mockOldProjectCategory, mockNewProjectCategory);
        final ArgumentCaptor<ProjectCategoryChangeEvent> argument = ArgumentCaptor.forClass(ProjectCategoryChangeEvent.class);
        verify(eventPublisher).publish(argument.capture());
        assertEquals(event,argument.getValue());

        verify(projectCategoryStore).updateProjectCategory(mockNewProjectCategory);
    }

    private AvatarManager getMockAvatarManager()
    {
        return (AvatarManager) DuckTypeProxy.getProxy(AvatarManager.class, new Object()
        {
            public Long getDefaultAvatarId(Avatar.Type ofType)
            {
                return 12345L;
            }
        });
    }

}
