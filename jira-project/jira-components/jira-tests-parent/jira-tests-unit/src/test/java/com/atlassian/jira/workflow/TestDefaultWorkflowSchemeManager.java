package com.atlassian.jira.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import com.atlassian.beehive.ClusterLockService;
import com.atlassian.beehive.simple.SimpleClusterLockService;
import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.event.workflow.WorkflowSchemeCreatedEvent;
import com.atlassian.jira.event.workflow.WorkflowSchemeDeletedEvent;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.task.MockTaskDescriptor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NoopI18nFactory;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.admin.workflow.WorkflowMigrationResult;
import com.atlassian.jira.workflow.migration.WorkflowSchemeMigrationTaskAccessor;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Iterables.elementsEqual;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Iterables.transform;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Test the default workflow scheme manager
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({"deprecation", "ConstantConditions"})
public class TestDefaultWorkflowSchemeManager
{
    private MockSimpleAuthenticationContext context;
    private AssignableWorkflowScheme defaultScheme;
    
    @Mock
    private UserManager userManager;

    @Mock
    protected EventPublisher mockEventPublisher;

    @Mock
    private Cache<Long, Map<String, GenericValue>> workflowSchemeEntityCache;

    @Mock @AvailableInContainer
    protected OfBizDelegator ofBizDelegator;

    private ApplicationUser user;

    private I18nHelper.BeanFactory i18nFactory;
    private CacheManager cacheManager;
    private ClusterLockService clusterLockService;

    @Rule
    public RuleChain ruleChain = MockitoMocksInContainer.forTest(this);

    @Before
    public void setup()
    {
        cacheManager = new MemoryCacheManager();
        clusterLockService = new SimpleClusterLockService();
        context = new MockSimpleAuthenticationContext(new MockUser("bbain"), Locale.ENGLISH, new MockI18nHelper());
        defaultScheme = new DefaultWorkflowScheme(context);
        when(userManager.getUserByKey(Mockito.notNull(String.class))).thenAnswer(new Answer<ApplicationUser>()
        {
            @Override
            public ApplicationUser answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                return new MockApplicationUser((String) invocationOnMock.getArguments()[0]);
            }
        });

        user = new MockApplicationUser("admin");

        i18nFactory = new NoopI18nFactory();
    }

    @Test
    public void getAllAssignable()
    {
        MockAssignableWorkflowSchemeStore schemeStore = new MockAssignableWorkflowSchemeStore();

        MockAssignableWorkflowScheme scheme1 = new MockAssignableWorkflowScheme(10191L, "ZZZ");
        MockAssignableWorkflowScheme scheme3 = new MockAssignableWorkflowScheme(10192L, "aaa");
        MockAssignableWorkflowScheme scheme4 = new MockAssignableWorkflowScheme(10193L, "BBB");

        schemeStore.addStateForScheme(scheme1);
        schemeStore.addStateForScheme(scheme3);
        schemeStore.addStateForScheme(scheme4);

        final WorkflowSchemeManager defaultWorkflowSchemeManager = new DefaultWorkflowSchemeManager(
                null, null, null, null, null, null, null, mockEventPublisher, null, null, null, context, userManager,
                i18nFactory, schemeStore, cacheManager, clusterLockService);

        assertTrue(elementsEqual(of(scheme3, scheme4, scheme1),
                transform(defaultWorkflowSchemeManager.getAssignableSchemes(), MockAssignableWorkflowScheme.toMock())));
    }

    @Test
    public void testHasDraft()
    {
        final MockDraftWorkflowSchemeStore mockDraftWorkflowSchemeStore = new MockDraftWorkflowSchemeStore();

        final AssignableWorkflowScheme parentScheme = new MockAssignableWorkflowScheme(10001L, "name", "description");
        final DraftWorkflowScheme draftWorkflowScheme = new MockDraftWorkflowScheme(18291L, parentScheme);

        mockDraftWorkflowSchemeStore.addStateForScheme(draftWorkflowScheme);

        final AssignableWorkflowScheme noDraft = new MockAssignableWorkflowScheme(10002L, "name", "description");

        final AssignableWorkflowScheme noId = new MockAssignableWorkflowScheme(null, "name", "description");

        final DefaultWorkflowSchemeManager defaultWorkflowSchemeManager = new DefaultWorkflowSchemeManager(
                null, null, null, null, null, null, null, null, null, null, mockDraftWorkflowSchemeStore, context,
                userManager, i18nFactory, null, cacheManager, clusterLockService);
        assertTrue(defaultWorkflowSchemeManager.hasDraft(parentScheme));
        assertFalse(defaultWorkflowSchemeManager.hasDraft(noDraft));
        assertFalse(defaultWorkflowSchemeManager.hasDraft(defaultScheme));
        assertFalse(defaultWorkflowSchemeManager.hasDraft(noId));
    }

    @Test
    public void testCreateDraftOfBad()
    {
        final MockDraftWorkflowSchemeStore mockDraftWorkflowSchemeStore = new MockDraftWorkflowSchemeStore();

        final AssignableWorkflowScheme parentScheme = new MockAssignableWorkflowScheme(10001L, "name", "description");
        final DraftWorkflowScheme draftWorkflowScheme = new MockDraftWorkflowScheme(18291L, parentScheme);

        mockDraftWorkflowSchemeStore.addStateForScheme(draftWorkflowScheme);

        final AssignableWorkflowScheme noId = new MockAssignableWorkflowScheme(null, "name", "description");

        final DefaultWorkflowSchemeManager defaultWorkflowSchemeManager = new DefaultWorkflowSchemeManager(
                null, null, null, null, null, null, null, mockEventPublisher, null, null, mockDraftWorkflowSchemeStore,
                context, userManager, i18nFactory, null, cacheManager, clusterLockService);

        //No scheme.
        try
        {
            defaultWorkflowSchemeManager.createDraftOf(null, null);
            fail("Expected Exception");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        //No scheme
        try
        {
            defaultWorkflowSchemeManager.createDraftOf(null, noId);
            fail("Expected an error.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        //Already has draft
        try
        {
            defaultWorkflowSchemeManager.createDraftOf(null, parentScheme);
            fail("Expected an error.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        //default scheme
        try
        {
            defaultWorkflowSchemeManager.createDraftOf(null, defaultScheme);
            fail("Expected an error.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }
    }

    @Test
    public void testCreateDraftOf()
    {
        checkCreateDraftOfForUser(user);
        checkCreateDraftOfForUser(null);
    }

    private void checkCreateDraftOfForUser(ApplicationUser creator)
    {
        final MockDraftWorkflowSchemeStore store = new MockDraftWorkflowSchemeStore();

        final MockAssignableWorkflowScheme newScheme = new MockAssignableWorkflowScheme(10001L, "name", "description");
        newScheme.setMapping("one", "two");

        final DSMForTest defaultWorkflowSchemeManager = new DSMForTest(store, context, userManager, i18nFactory, null);
        defaultWorkflowSchemeManager.addScheme(newScheme);

        final DraftWorkflowScheme draft = defaultWorkflowSchemeManager.createDraftOf(creator, newScheme);

        checkWorkflowScheme(newScheme, creator, store.getLastDate(), store.getLastId(), draft);
        checkStoreContainsScheme(store, draft, creator);
    }

    @Test
    public void testCreateDraftBad()
    {
        final MockDraftWorkflowSchemeStore mockDraftWorkflowSchemeStore = new MockDraftWorkflowSchemeStore();

        final AssignableWorkflowScheme parentScheme = new MockAssignableWorkflowScheme(10001L, "name", "description");
        final DraftWorkflowScheme draftWorkflowScheme = new MockDraftWorkflowScheme(18291L, parentScheme);

        mockDraftWorkflowSchemeStore.addStateForScheme(draftWorkflowScheme);

        final AssignableWorkflowScheme noId = new MockAssignableWorkflowScheme(null, "name", "description");
        final DraftWorkflowScheme draftNoId = new MockDraftWorkflowScheme(null, noId);

        final DraftWorkflowScheme noParent = new MockDraftWorkflowScheme(null, null);

        final DefaultWorkflowSchemeManager defaultWorkflowSchemeManager = new DefaultWorkflowSchemeManager(
                null, null, null, null, null, null, null, mockEventPublisher, null, null, mockDraftWorkflowSchemeStore,
                context, userManager, i18nFactory, null, cacheManager, clusterLockService);

        //No scheme.
        try
        {
            defaultWorkflowSchemeManager.createDraft(null, null);
            fail("Expected Exception");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        //No scheme
        try
        {
            defaultWorkflowSchemeManager.createDraft(null, draftNoId);
            fail("Expected an error.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        //Already has draft
        try
        {
            defaultWorkflowSchemeManager.createDraft(null, draftWorkflowScheme);
            fail("Expected an error.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        //Draft does not have a parent.
        try
        {
            defaultWorkflowSchemeManager.createDraft(null, noParent);
            fail("Expected an error.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }
    }

    @Test
    public void testCreateDraft()
    {
        checkCreateDraft(user);
        checkCreateDraft(null);
    }

    private void checkCreateDraft(ApplicationUser user)
    {
        final MockDraftWorkflowSchemeStore store = new MockDraftWorkflowSchemeStore();

        final MockAssignableWorkflowScheme newScheme = new MockAssignableWorkflowScheme(10001L, "name", "description");
        newScheme.setMapping("one", "two");

        final MockDraftWorkflowScheme newDraftScheme = new MockDraftWorkflowScheme(null, newScheme);
        newDraftScheme.setMapping("three", "four");

        final DSMForTest defaultWorkflowSchemeManager = new DSMForTest(store, context, userManager, i18nFactory, null);
        defaultWorkflowSchemeManager.addScheme(newScheme);

        final DraftWorkflowScheme draft = defaultWorkflowSchemeManager.createDraft(user, newDraftScheme);

        checkWorkflowScheme(newDraftScheme, user, store.getLastDate(), store.getLastId(), draft);
        checkStoreContainsScheme(store, draft, user);
    }

    private void checkWorkflowScheme(WorkflowScheme expectedScheme, ApplicationUser expectedCreator, Date expectedDate,
            long expectedId, DraftWorkflowScheme actualDraft)
    {
        assertEquals((Long)expectedId, actualDraft.getId());
        assertEquals(expectedScheme.getName(), actualDraft.getName());
        assertEquals(expectedScheme.getDescription(), actualDraft.getDescription());
        assertEquals(expectedScheme.getMappings(), actualDraft.getMappings());
        assertEquals(expectedCreator, actualDraft.getLastModifiedUser());
        assertEquals(expectedDate, actualDraft.getLastModifiedDate());
    }

    private void checkStoreContainsScheme(MockDraftWorkflowSchemeStore store, DraftWorkflowScheme draft, ApplicationUser creator)
    {
        final DraftWorkflowSchemeStore.DraftState state = store.get(draft.getId());
        assertEquals(draft.getId(), state.getId());
        assertEquals(draft.getMappings(), state.getMappings());
        assertEquals(creator == null ? null : creator.getKey(), state.getLastModifiedUser());
        assertEquals(state.getLastModifiedDate(), store.getLastDate());
    }

    @Test
    public void testDraftWorkflowSchemeBuilder()
    {
        final AssignableWorkflowScheme parentScheme = new MockAssignableWorkflowScheme(10001L, "name", "description");

        final DefaultWorkflowSchemeManager defaultWorkflowSchemeManager = new DefaultWorkflowSchemeManager(
                null, null, null, null, null, null, null, mockEventPublisher, null, null, null, context, userManager,
                i18nFactory, null, cacheManager, clusterLockService);
        try
        {
            defaultWorkflowSchemeManager.draftBuilder(null);
            fail("Should not accept null parent.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }
        try
        {
            defaultWorkflowSchemeManager.draftBuilder(new MockAssignableWorkflowScheme(null, "Something"));
            fail("Should not accept workflow scheme with null id.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        DraftWorkflowScheme.Builder builder = defaultWorkflowSchemeManager.draftBuilder(parentScheme);
        assertNotNull(builder);

        DraftWorkflowScheme childScheme = builder.build();
        assertSame(parentScheme, childScheme.getParentScheme());
        assertNotNull(childScheme.getLastModifiedDate());
    }

    @Test
    public void testGetWorkflowScheme()
    {
        final DefaultWorkflowSchemeManager defaultWorkflowSchemeManager = new DefaultWorkflowSchemeManager(
                null, null, null, null, null, null, null, mockEventPublisher, null, null, null, context, userManager,
                i18nFactory, null, cacheManager, clusterLockService)
        {
            @Override
            Map<String, String> getWorkflowMap(GenericValue schemeForProject)
            {
                return ImmutableMap.of("bud", "workflows " + schemeForProject.getLong("id"));
            }

            @Override
            public GenericValue getScheme(Long id)
            {
                return new MockGenericValue("something",
                        ImmutableMap.of("name", "name of " + id,
                                "description", "description of " + id ,
                                "id", id));
            }
        };


        final WorkflowScheme workflowScheme = defaultWorkflowSchemeManager.getWorkflowSchemeObj(1277L);
        assertEquals(Long.valueOf(1277), workflowScheme.getId());
        assertEquals("name of 1277", workflowScheme.getName());
        assertEquals("description of 1277", workflowScheme.getDescription());
        assertEquals(ImmutableMap.of("bud", "workflows 1277"), workflowScheme.getMappings());
    }

    @Test
    public void testGetWorkflowSchemeObj()
    {
        final DefaultWorkflowSchemeManager defaultWorkflowSchemeManager = new DefaultWorkflowSchemeManager(
                null, null, null, null, null, null, null, mockEventPublisher, null, null, null, context, userManager,
                i18nFactory, null, cacheManager, clusterLockService)
        {
            @Override
            Map<String, String> getWorkflowMap(GenericValue schemeForProject)
            {
                return ImmutableMap.of("bud", "workflows " + schemeForProject.getLong("id"));
            }

            @Override
            GenericValue getSchemeForProject(Project project)
            {
                return new MockGenericValue("something",
                        ImmutableMap.of("name", "name of " + project.getId(),
                                "description", "description of " + project.getId(),
                                "id", project.getId()));
            }
        };

        final MockProject mockProject = new MockProject(1277L);
        final WorkflowScheme workflowScheme = defaultWorkflowSchemeManager.getWorkflowSchemeObj(mockProject);

        assertEquals(Long.valueOf(1277), workflowScheme.getId());
        assertEquals("name of 1277", workflowScheme.getName());
        assertEquals("description of 1277", workflowScheme.getDescription());
        assertEquals(ImmutableMap.of("bud", "workflows 1277"), workflowScheme.getMappings());
    }

    @Test
    public void testGetWorkflowSchemeObjDefaultScheme()
    {
        final DefaultWorkflowSchemeManager defaultWorkflowSchemeManager = new DefaultWorkflowSchemeManager(
                null, null, null, null, null, null, null, mockEventPublisher, null, null, null, context, userManager,
                i18nFactory, null, cacheManager, clusterLockService)
        {
            @Override
            Map<String, String> getWorkflowMap(GenericValue schemeForProject)
            {
                return ImmutableMap.of("bud", "workflows " + schemeForProject.getLong("id"));
            }

            @Override
            GenericValue getSchemeForProject(Project project)
            {
                return null;
            }
        };

        final MockProject mockProject = new MockProject(1277L);
        final WorkflowScheme workflowScheme = defaultWorkflowSchemeManager.getWorkflowSchemeObj(mockProject);

        assertNull(workflowScheme.getId());
        assertEquals("admin.schemes.workflows.default", workflowScheme.getName());
        assertEquals("admin.schemes.workflows.default.desc", workflowScheme.getDescription());
        assertEquals(MapBuilder.build((String) null, JiraWorkflow.DEFAULT_WORKFLOW_NAME), workflowScheme.getMappings());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDraftForParentBad()
    {
        final DefaultWorkflowSchemeManager defaultWorkflowSchemeManager = new DefaultWorkflowSchemeManager(
                null, null, null, null, null, null, null, mockEventPublisher, null, null, null, context, userManager,
                i18nFactory, null, cacheManager, clusterLockService);

        //Null scheme
        defaultWorkflowSchemeManager.getDraftForParent(null);
    }

    @Test
    public void getDraftForParent()
    {
        final AssignableWorkflowScheme parent = new MockAssignableWorkflowScheme(1028291L, "name", "description");
        final AssignableWorkflowScheme noDraft = new MockAssignableWorkflowScheme(10002L, "name", "description");
        final AssignableWorkflowScheme noId = new MockAssignableWorkflowScheme(null, "name", "description");
        final DraftWorkflowScheme draftWorkflowScheme = new MockDraftWorkflowScheme(18291L, parent);

        final MockDraftWorkflowSchemeStore store = new MockDraftWorkflowSchemeStore();
        store.addStateForScheme(draftWorkflowScheme);

        final DSMForTest schemeManager = new DSMForTest(store, context, userManager, i18nFactory, null);
        schemeManager.addScheme(parent);

        final WorkflowScheme actualDraft = schemeManager.getDraftForParent(parent);
        assertEquals(draftWorkflowScheme.getName(), actualDraft.getName());
        assertEquals(draftWorkflowScheme.getDescription(), actualDraft.getDescription());
        assertEquals(draftWorkflowScheme.getId(), actualDraft.getId());
        assertEquals(draftWorkflowScheme.getMappings(), actualDraft.getMappings());

        assertNull(schemeManager.getDraftForParent(noDraft));
        assertNull(schemeManager.getDraftForParent(noId));
        assertNull(schemeManager.getDraftForParent(defaultScheme));
    }

    @Test
    public void isActive()
    {
        final WorkflowScheme active = new MockAssignableWorkflowScheme(1028291L, "name", "description");
        final WorkflowScheme noId = new MockAssignableWorkflowScheme(null, "name", "description");
        final WorkflowScheme inactive = new MockAssignableWorkflowScheme(10002L, "name", "description");

        final AssignableWorkflowScheme parentScheme = new MockAssignableWorkflowScheme(10001L, "name", "description");
        final DraftWorkflowScheme draftWorkflowScheme = new MockDraftWorkflowScheme(18291L, parentScheme);


        final DefaultWorkflowSchemeManager schemeManager = new DefaultWorkflowSchemeManager(
                null, null, null, null, null, null, null, mockEventPublisher, null, null, null, context, userManager,
                i18nFactory, null, cacheManager, clusterLockService)
        {
            @Override
            public GenericValue getScheme(Long id)
            {
                return new MockGenericValue("Scheme", ImmutableMap.of("id", id));
            }

            @Override
            public List<GenericValue> getProjects(GenericValue scheme) throws GenericEntityException
            {
                final Long id = scheme.getLong("id");
                if (active.getId().equals(id))
                {
                    return Collections.<GenericValue>singletonList(new MockGenericValue("Project"));
                }
                else
                {
                    return Collections.emptyList();
                }
            }
        };

        assertFalse(schemeManager.isActive(noId));
        assertFalse(schemeManager.isActive(draftWorkflowScheme));
        assertTrue(schemeManager.isActive(active));
        assertFalse(schemeManager.isActive(inactive));
    }

    @Test
    public void isActiveDefaultScheme()
    {
        final Project project1 = new MockProject(1L);
        final Project project2 = new MockProject(2L);

        final ProjectManager projectManager = Mockito.mock(ProjectManager.class);
        when(projectManager.getProjectObjects()).thenReturn(Lists.newArrayList(project1));

        final DefaultWorkflowSchemeManager schemeManager = new DefaultWorkflowSchemeManager(
                projectManager, null, null, null, null, null, null, mockEventPublisher, null, null, null, context,
                userManager, i18nFactory, null, cacheManager, clusterLockService)
        {
            @Override
            public boolean isUsingDefaultScheme(Project project)
            {
                return project.equals(project2);
            }
        };

        assertFalse(schemeManager.isActive(defaultScheme));
        when(projectManager.getProjectObjects()).thenReturn(Lists.newArrayList(project1, project2));
        assertTrue(schemeManager.isActive(defaultScheme));
    }

    @Test
    public void deleteScheme()
    {
        // Set up
        final NodeAssociationStore nodeAssociationStore = Mockito.mock(NodeAssociationStore.class);
        final DraftWorkflowSchemeStore draftStore = Mockito.mock(DraftWorkflowSchemeStore.class);
        final OfBizDelegator delegator = Mockito.mock(OfBizDelegator.class);
        final WorkflowSchemeMigrationTaskAccessor taskAccessor = mock(WorkflowSchemeMigrationTaskAccessor.class);

        final DefaultWorkflowSchemeManager schemeManager = new DefaultWorkflowSchemeManager(null, null, null, null,
                null, null, delegator, mockEventPublisher, nodeAssociationStore, null, draftStore, context, userManager,
                i18nFactory, null, cacheManager, clusterLockService)
        {
            @Override
            public GenericValue getScheme(Long id)
            {
                return new MockGenericValue("Scheme", ImmutableMap.of("id", id));
            }

            @Override
            WorkflowSchemeMigrationTaskAccessor getTaskAccessor()
            {
                return taskAccessor;
            }
        };
        schemeManager.start();

        // Invoke
        schemeManager.deleteScheme(10L);
        schemeManager.deleteScheme(null);

        // Check
        verify(draftStore, only()).deleteByParentId(10);

        //Did we fire the correct event?
        verify(mockEventPublisher).register(schemeManager);
        final ArgumentCaptor<WorkflowSchemeDeletedEvent> eventCaptor =
                ArgumentCaptor.forClass(WorkflowSchemeDeletedEvent.class);
        verify(mockEventPublisher).publish(eventCaptor.capture());
        verifyNoMoreInteractions(mockEventPublisher);
        assertEquals(Long.valueOf(10), eventCaptor.getValue().getId());
    }

    @Test
    public void getDeleteWorkflowSchemeBad()
    {
        final WorkflowScheme noId = new MockAssignableWorkflowScheme(null, "name", "description");

        final WorkflowScheme activeScheme = new MockAssignableWorkflowScheme(10101L, "name", "description");

        WorkflowSchemeManager defaultWorkflowSchemeManager = new DefaultWorkflowSchemeManager(
                null, null, null, null, null, null, null, mockEventPublisher, null, null, null, context, userManager,
                i18nFactory, null, cacheManager, clusterLockService)
                {
                    @Override
                    public boolean isActive(@Nonnull WorkflowScheme scheme)
                    {
                        assertSame(scheme, activeScheme);
                        return true;
                    }
                };

        defaultWorkflowSchemeManager = Mockito.spy(defaultWorkflowSchemeManager);

        //Null scheme
        try
        {
            defaultWorkflowSchemeManager.deleteWorkflowScheme(null);
            fail("Expected an error.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        //No scheme
        try
        {
            defaultWorkflowSchemeManager.deleteWorkflowScheme(noId);
            fail("Expected an error.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        //No scheme
        try
        {
            defaultWorkflowSchemeManager.deleteWorkflowScheme(defaultScheme);
            fail("Expected an error.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        //Active scheme
        try
        {
            defaultWorkflowSchemeManager.deleteWorkflowScheme(activeScheme);
            fail("Expected an error.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }
    }

    @Test
    public void deleteWorkflowScheme() throws GenericEntityException
    {
        final AssignableWorkflowScheme parent = new MockAssignableWorkflowScheme(1028291L, "name", "description");
        final AssignableWorkflowScheme noDraft = new MockAssignableWorkflowScheme(10002L, "name", "description");

        final AssignableWorkflowScheme parentScheme = new MockAssignableWorkflowScheme(10001L, "name", "description");
        final DraftWorkflowScheme draftWorkflowScheme = new MockDraftWorkflowScheme(18291L, parentScheme);

        final DraftWorkflowSchemeStore store = Mockito.mock(DraftWorkflowSchemeStore.class);
        final WorkflowSchemeMigrationTaskAccessor taskAccessor = mock(WorkflowSchemeMigrationTaskAccessor.class);

        DefaultWorkflowSchemeManager schemeManager = new DefaultWorkflowSchemeManager(
                null, null, null, null, null, null, null, mockEventPublisher, null, null, store, context, userManager,
                i18nFactory, null, cacheManager, clusterLockService)
        {
            @Override
            WorkflowSchemeMigrationTaskAccessor getTaskAccessor()
            {
                return taskAccessor;
            }
        };
        schemeManager = Mockito.spy(schemeManager);
        doNothing().when(schemeManager).doDeleteScheme(Mockito.<AssignableWorkflowScheme>any());
        doReturn(false).when(schemeManager).isActive(Mockito.<WorkflowScheme>any());

        assertTrue(schemeManager.deleteWorkflowScheme(parent));
        verify(schemeManager).doDeleteScheme(parent);

        assertTrue(schemeManager.deleteWorkflowScheme(noDraft));
        verify(schemeManager).doDeleteScheme(noDraft);

        assertFalse(schemeManager.deleteWorkflowScheme(draftWorkflowScheme));
        verify(store).delete(draftWorkflowScheme.getId());
    }

    @Test
    public void updateDraftWorkflowScheme() throws GenericEntityException
    {
        final AssignableWorkflowScheme parent = new MockAssignableWorkflowScheme(1028291L, "name", "description");
        final DraftWorkflowScheme draft = new MockDraftWorkflowScheme(10002L, parent).setLastModifiedDate(new Date());

        MockDraftWorkflowSchemeStore store = new MockDraftWorkflowSchemeStore();
        store.addStateForScheme(draft);

        final WorkflowSchemeMigrationTaskAccessor taskAccessor = mock(WorkflowSchemeMigrationTaskAccessor.class);
        DSMForTest schemeManager = new DSMForTest(store, context, userManager, i18nFactory, null)
        {
            @Override
            WorkflowSchemeMigrationTaskAccessor getTaskAccessor()
            {
                return taskAccessor;
            }
        };
        schemeManager.addScheme(parent);

        DraftWorkflowScheme.Builder builder = draft.builder()
                .setMappings(Collections.singletonMap("one", "two"));

        DraftWorkflowScheme expectedScheme = builder.build();
        DraftWorkflowScheme newDraft = schemeManager.updateDraftWorkflowScheme(null, expectedScheme);
        checkStoreContainsScheme(store, expectedScheme, null);
        checkWorkflowScheme(expectedScheme, null, store.getLastDate(), draft.getId(), newDraft);
    }

    @Test
    public void updateDraftWorkflowSchemeBad()
    {
        final DraftWorkflowScheme noId = new MockDraftWorkflowScheme();
        MockAssignableWorkflowScheme parent = new MockAssignableWorkflowScheme(1028291L, "name", "description");
        final DraftWorkflowScheme doesntExist = new MockDraftWorkflowScheme(7383L, parent);
        final DraftWorkflowScheme migrating = new MockDraftWorkflowScheme(10002L, parent);
        final DraftWorkflowSchemeStore store = Mockito.mock(DraftWorkflowSchemeStore.class);

        final WorkflowSchemeMigrationTaskAccessor taskAccessor = mock(WorkflowSchemeMigrationTaskAccessor.class);

        WorkflowSchemeManager schemeManager = new DefaultWorkflowSchemeManager(
                null, null, null, null, null, null, null, mockEventPublisher, null, null, store, context, userManager,
                i18nFactory, null, cacheManager, clusterLockService)
        {
            @Override
            WorkflowSchemeMigrationTaskAccessor getTaskAccessor()
            {
                return taskAccessor;
            }
        };

        try
        {
            schemeManager.updateDraftWorkflowScheme(null, null);
            fail("Expected an exception.");
        }
        catch (IllegalArgumentException expected)
        {
            // Success
        }

        try
        {
            schemeManager.updateDraftWorkflowScheme(null, noId);
            fail("Expected an exception.");
        }
        catch (IllegalArgumentException expected)
        {
            // Success
        }

        try
        {
            schemeManager.updateDraftWorkflowScheme(null, doesntExist);
            fail("Expected an exception.");
        }
        catch (IllegalArgumentException expected)
        {
            // Success
        }

        when(taskAccessor.getActiveByProjects(migrating, true)).thenReturn(new MockTaskDescriptor<WorkflowMigrationResult>());
        try
        {
            schemeManager.updateDraftWorkflowScheme(null, migrating);
            fail("Expected an exception.");
        }
        catch (SchemeIsBeingMigratedException expected)
        {
            // Success
        }
    }

    @Test
    public void updateWorkflowSchemeBad()
    {
        final AssignableWorkflowScheme noId = new MockAssignableWorkflowScheme();
        final AssignableWorkflowScheme migrating = new MockAssignableWorkflowScheme(10002L, "name");

        final WorkflowSchemeMigrationTaskAccessor taskAccessor = mock(WorkflowSchemeMigrationTaskAccessor.class);

        WorkflowSchemeManager schemeManager = new DefaultWorkflowSchemeManager(
                null, null, null, null, null, null, null, mockEventPublisher, null, null, null, context, userManager,
                i18nFactory, null, cacheManager, clusterLockService)
        {
            @Override
            WorkflowSchemeMigrationTaskAccessor getTaskAccessor()
            {
                return taskAccessor;
            }
        };

        try
        {
            schemeManager.updateWorkflowScheme(null);
            fail("Expected an exception.");
        }
        catch (IllegalArgumentException expected)
        {
            // Success
        }

        try
        {
            schemeManager.updateWorkflowScheme(noId);
            fail("Expected an exception.");
        }
        catch (IllegalArgumentException expected)
        {
            // Success
        }

        when(taskAccessor.getActive(migrating)).thenReturn(new MockTaskDescriptor<WorkflowMigrationResult>());
        try
        {
            schemeManager.updateWorkflowScheme(migrating);
            fail("Expected an exception.");
        }
        catch (SchemeIsBeingMigratedException expected)
        {
            // Success
        }
    }

    @Test
    public void getProjectsUsing()
    {
        final AssignableWorkflowScheme parent = new MockAssignableWorkflowScheme(11L, "name", "description");

        final MockProject project1 = new MockProject(10, "ABC");
        final MockProject project2 = new MockProject(parent.getId(), "DEF");
        final MockProject project3 = new MockProject(12, "JKL");

        final DraftWorkflowSchemeStore store = Mockito.mock(DraftWorkflowSchemeStore.class);
        final ProjectManager projectManager = Mockito.mock(ProjectManager.class);

        when(projectManager.getProjectObjects()).thenReturn(ImmutableList.<Project>of(project1, project2, project3));

        final WorkflowSchemeManager schemeManager = new DefaultWorkflowSchemeManager(
                projectManager, null, null, null, null, null, null, mockEventPublisher, null, null, store, context,
                userManager, i18nFactory, null, cacheManager, clusterLockService)
        {
            @Override
            GenericValue getSchemeForProject(Project project)
            {
                if (project.equals(project1))
                {
                    return null;
                }
                else
                {
                    return new MockGenericValue("WorkflowScheme", project.getId());
                }
            }
        };

        assertEquals(Collections.<Project>singletonList(project1), schemeManager.getProjectsUsing(defaultScheme));
        assertEquals(Collections.<Project>singletonList(project2), schemeManager.getProjectsUsing(parent));
    }

    @Test
    public void testUpdateSchemesForRenamedWorkflow()
    {
        assertUpdateSchemesForRenamedWorkflow(null, "new", false);
        assertUpdateSchemesForRenamedWorkflow("", "new", false);
        assertUpdateSchemesForRenamedWorkflow("old", null, false);
        assertUpdateSchemesForRenamedWorkflow("old", "", false);
        assertUpdateSchemesForRenamedWorkflow("old", "new", true);
    }

    private void assertUpdateSchemesForRenamedWorkflow(
            final String oldWorkflowName, final String newWorkflowName, final boolean isExecuted)
    {
        final AtomicBoolean isClearCacheCalled = new AtomicBoolean(false);

        final OfBizDelegator mockOfBizDelegator = Mockito.mock(OfBizDelegator.class);
        final DraftWorkflowSchemeStore store = Mockito.mock(DraftWorkflowSchemeStore.class);
        final DefaultWorkflowSchemeManager workflowSchemeManager = new DefaultWorkflowSchemeManager(
                null, null, null, null, null, null, mockOfBizDelegator, mockEventPublisher, null, null, store, context,
                userManager, i18nFactory, null, cacheManager, clusterLockService)
        {
            @Override
            public void clearWorkflowCache()
            {
                isClearCacheCalled.set(true);
                super.clearWorkflowCache();
            }
        };
        workflowSchemeManager.start();

        if (isExecuted)
        {
            workflowSchemeManager.updateSchemesForRenamedWorkflow(oldWorkflowName, newWorkflowName);
            verify(mockOfBizDelegator).bulkUpdateByAnd("WorkflowSchemeEntity",
                    ImmutableMap.of("workflow", newWorkflowName), ImmutableMap.of("workflow", oldWorkflowName));
            verify(store).renameWorkflow(oldWorkflowName, newWorkflowName);
        }
        else
        {
            try
            {
                workflowSchemeManager.updateSchemesForRenamedWorkflow(oldWorkflowName, newWorkflowName);
                fail("Expected an exception to be thrown");
            }
            catch (final IllegalArgumentException e)
            {
                assertTrue(e.getMessage().contains("must not be null or empty string"));
            }
        }

        assertEquals(isExecuted, isClearCacheCalled.get());//cache should have been flushed only if executed
    }

    @Test
    public void testGetWorkflowMap() throws Exception
    {
        // Set up
        final Project project = createProject(100002L, "BJB");
        final GenericValue schemeGv = createSchemeGV(9000L, "one", "two", "three", "four");

        final DefaultWorkflowSchemeManager testingClass = new DefaultWorkflowSchemeManager(
                null, null, null, null, null, null, ofBizDelegator, mockEventPublisher, null, null, null, context,
                userManager, i18nFactory, null, cacheManager, clusterLockService)
        {
            @Override
            public List<GenericValue> getSchemes(final GenericValue projectGV) throws GenericEntityException
            {
                assertSame(project.getGenericValue(), projectGV);
                return Collections.singletonList(schemeGv);
            }
        };
        testingClass.start();

        // Invoke and check
        final Map<String, String> expectedMap = MapBuilder.<String, String>newBuilder().add(null, "one")
                .add("two", "two").add("three", "three").add("four", "four").toMutableMap();
        assertEquals(expectedMap, testingClass.getWorkflowMap(project));
        assertEquals(expectedMap, testingClass.getWorkflowMap(project));
    }

    @Test
    public void testGetWorkflowNameFromScheme() throws Exception
    {
        // Set up
        final GenericValue schemeGv = createSchemeGV(9000L, "one", "two", "three", "four");

        final DefaultWorkflowSchemeManager testingClass = new DefaultWorkflowSchemeManager(
                null, null, null, null, null, null, ofBizDelegator, mockEventPublisher, null, null, null, context,
                userManager, i18nFactory, null, cacheManager, clusterLockService);
        testingClass.start();

        // Invoke and check
        assertEquals("one", testingClass.getWorkflowName(schemeGv, "0"));
        assertEquals("two", testingClass.getWorkflowName(schemeGv, "two"));
        assertEquals("three", testingClass.getWorkflowName(schemeGv, "three"));
        assertEquals("four", testingClass.getWorkflowName(schemeGv, "four"));
        assertEquals("four", testingClass.getWorkflowName(schemeGv, "four"));
    }

    @Test
    public void testGetWorkflowNameFromProject() throws Exception
    {
        final Project project = createProject(100002L, "BJB");
        final GenericValue schemeGv = createSchemeGV(9000L);

        final DefaultWorkflowSchemeManager testingClass = new DefaultWorkflowSchemeManager(
                null, null, null, null, null, null, null, mockEventPublisher, null, null, null, context, userManager,
                i18nFactory, null, cacheManager, clusterLockService)
        {
            @Override
            public String getWorkflowName(final GenericValue scheme, final String issueType)
            {
                assertSame(schemeGv, scheme);
                return issueType;
            }

            @Override
            public List<GenericValue> getSchemes(final GenericValue projectGV) throws GenericEntityException
            {
                assertSame(project.getGenericValue(), projectGV);

                return Collections.singletonList(schemeGv);
            }
        };

        assertEquals("0", testingClass.getWorkflowName(project, "0"));
        assertEquals("two", testingClass.getWorkflowName(schemeGv, "two"));
        assertEquals("three", testingClass.getWorkflowName(schemeGv, "three"));
        assertEquals("four", testingClass.getWorkflowName(schemeGv, "four"));
        assertEquals("four", testingClass.getWorkflowName(schemeGv, "four"));
    }

    @Test
    public void testGetSchemesForWorkflowIncludingDrafts()
    {
        final MockDraftWorkflowSchemeStore store = new MockDraftWorkflowSchemeStore();

        DSMForTest defaultWorkflowSchemeManager = new DSMForTest(store, context, userManager, i18nFactory, null)
        {
            @Override
            public Collection<GenericValue> getSchemesForWorkflow(JiraWorkflow workflow)
            {
                return Arrays.<GenericValue>asList(createSchemeGV(10001L));
            }
        };
        defaultWorkflowSchemeManager.start();

        MockJiraWorkflow jiraWorkflow = new MockJiraWorkflow();
        jiraWorkflow.setName("Test workflow");

        final AssignableWorkflowScheme assignableScheme = new MockAssignableWorkflowScheme(10001L, "name");
        defaultWorkflowSchemeManager.addScheme(assignableScheme);

        final MockDraftWorkflowScheme draftWorkflowScheme = new MockDraftWorkflowScheme(247837L, assignableScheme);
        draftWorkflowScheme.setMapping("1", jiraWorkflow.getName());

        store.addStateForScheme(draftWorkflowScheme);

        final Iterable<WorkflowScheme> schemes =
                defaultWorkflowSchemeManager.getSchemesForWorkflowIncludingDrafts(jiraWorkflow);

        assertEquals(2, size(schemes));
        assertNotNull(find(schemes, new Predicate<WorkflowScheme>()
        {
            @Override
            public boolean apply(WorkflowScheme input)
            {
                return input.isDraft() && input.getId().equals(draftWorkflowScheme.getId());
            }
        }));

        assertNotNull(find(schemes, new Predicate<WorkflowScheme>()
        {
            @Override
            public boolean apply(WorkflowScheme input)
            {
                return !input.isDraft() && input.getId().equals(assignableScheme.getId());
            }
        }));
    }

    @Test
    public void copyAndDeleteDraftOfReturnsNullIfDefaultScheme()
    {
        MockDraftWorkflowSchemeStore store = new MockDraftWorkflowSchemeStore();

        final DefaultWorkflowSchemeManager manager = new DefaultWorkflowSchemeManager(
                null, null, null, null, null, null, null, mockEventPublisher, null, null, store, context, userManager,
                i18nFactory, null, cacheManager, clusterLockService);
        DefaultWorkflowSchemeManager spy = Mockito.spy(manager);

        Project project = new MockProject();

        doReturn(defaultScheme).when(spy).getWorkflowSchemeObj(project);
        assertNull(spy.cleanUpSchemeDraft(project, user.getDirectoryUser()));

        verify(spy, never()).deleteWorkflowScheme(any(WorkflowScheme.class));
        verify(spy, never()).createSchemeObject(any(String.class), any(String.class));
    }

    @Test
    public void copyAndDeleteDraftOfReturnsNullIfUsedByMultipleProjects()
    {
        MockDraftWorkflowSchemeStore store = new MockDraftWorkflowSchemeStore();

        final DefaultWorkflowSchemeManager manager = new DefaultWorkflowSchemeManager(
                null, null, null, null, null, null, null, mockEventPublisher, null, null, store, context, userManager,
                i18nFactory, null, cacheManager, clusterLockService);
        DefaultWorkflowSchemeManager spy = Mockito.spy(manager);

        Project project = new MockProject();
        AssignableWorkflowScheme scheme = new MockAssignableWorkflowScheme(10002L, "name", "description");

        doReturn(scheme).when(spy).getWorkflowSchemeObj(project);
        doReturn(asList(project, new MockProject())).when(spy).getProjectsUsing(scheme);

        assertNull(spy.cleanUpSchemeDraft(project, user.getDirectoryUser()));

        verify(spy, never()).deleteWorkflowScheme(any(WorkflowScheme.class));
        verify(spy, never()).createSchemeObject(any(String.class), any(String.class));
    }

    @Test
    public void copyAndDeleteDraftOfReturnsNullIfNoDraft()
    {
        MockDraftWorkflowSchemeStore store = new MockDraftWorkflowSchemeStore();

        final DefaultWorkflowSchemeManager manager = new DefaultWorkflowSchemeManager(
                null, null, null, null, null, null, null, mockEventPublisher, null, null, store, context, userManager,
                i18nFactory, null, cacheManager, clusterLockService);
        DefaultWorkflowSchemeManager spy = Mockito.spy(manager);

        Project project = new MockProject();
        AssignableWorkflowScheme scheme = new MockAssignableWorkflowScheme(10002L, "name", "description");

        doReturn(scheme).when(spy).getWorkflowSchemeObj(project);
        doReturn(asList(project)).when(spy).getProjectsUsing(scheme);
        doReturn(null).when(spy).getDraftForParent(scheme);

        assertNull(spy.cleanUpSchemeDraft(project, user.getDirectoryUser()));

        verify(spy, never()).deleteWorkflowScheme(any(WorkflowScheme.class));
        verify(spy, never()).createSchemeObject(any(String.class), any(String.class));
    }

    @Test
    public void copyAndDeleteDraftOfReturnsCopyOfDraftIfDraftExists() throws GenericEntityException
    {
        MockDraftWorkflowSchemeStore store = new MockDraftWorkflowSchemeStore();
        final WorkflowSchemeMigrationTaskAccessor taskAccessor = mock(WorkflowSchemeMigrationTaskAccessor.class);

        DSMForTest manager = new DSMForTest(store, context, userManager, i18nFactory, null)
        {
            @Override
            WorkflowSchemeMigrationTaskAccessor getTaskAccessor()
            {
                return taskAccessor;
            }
        };
        DefaultWorkflowSchemeManager spy = Mockito.spy(manager);

        Project project = new MockProject();
        AssignableWorkflowScheme originalScheme = new MockAssignableWorkflowScheme(10002L, "name", "description");
        MockDraftWorkflowScheme draftWorkflowScheme = new MockDraftWorkflowScheme(1000L, originalScheme);
        draftWorkflowScheme.setMapping("one", "two");

        doReturn(originalScheme).when(spy).getWorkflowSchemeObj(project);
        doReturn(asList(project)).when(spy).getProjectsUsing(originalScheme);
        doReturn(draftWorkflowScheme).when(spy).getDraftForParent(originalScheme);

        String copyName = "Copy of " + draftWorkflowScheme.getName();
        String copyDescription = draftWorkflowScheme.getDescription() + " copied";

        doReturn(copyName).when(spy).getNameForCopy(draftWorkflowScheme.getName(), 255);
        doReturn(copyDescription).when(spy).getDescriptionForCopy(user.getDirectoryUser(), originalScheme);

        AssignableWorkflowScheme copyScheme = new MockAssignableWorkflowScheme(null, copyName, copyDescription);

        doReturn(copyScheme).when(spy).createScheme(Matchers.<AssignableWorkflowScheme>any());

        AssignableWorkflowScheme copy = spy.cleanUpSchemeDraft(project, user.getDirectoryUser());

        assertEquals(copyName, copy.getName());
        assertEquals(copyDescription, copy.getDescription());

        verify(spy).deleteWorkflowScheme(draftWorkflowScheme);

        ArgumentCaptor<AssignableWorkflowScheme> captor = ArgumentCaptor.forClass(AssignableWorkflowScheme.class);
        verify(spy).createScheme(captor.capture());
        assertEquals(copyName, captor.getValue().getName());
        assertEquals(copyDescription, captor.getValue().getDescription());
    }

    @Test
    public void createWorkflowScheme()
    {
        MockAssignableWorkflowSchemeStore store = new MockAssignableWorkflowSchemeStore();

        MockAssignableWorkflowScheme expectedScheme = new MockAssignableWorkflowScheme(null, "Test");
        expectedScheme.setMapping("one", "two");

        final DefaultWorkflowSchemeManager manager = new DefaultWorkflowSchemeManager(
                null, null, null, null, null, null, ofBizDelegator, mockEventPublisher, null, null, null, context,
                userManager, i18nFactory, store, cacheManager, clusterLockService);
        AssignableWorkflowScheme actualScheme = manager.createScheme(expectedScheme);

        expectedScheme.setId(store.getLastId());

        assertEquals(expectedScheme, new MockAssignableWorkflowScheme(actualScheme));
        assertEquals(new MockAssignableWorkflowSchemeState(expectedScheme), store.get(actualScheme.getId()));

        verify(mockEventPublisher).publish(any(WorkflowSchemeCreatedEvent.class));
    }

    @Test
    public void createWorkflowSchemeUsingOldApiTriggersEvent() throws GenericEntityException
    {
        // Set up
        final SchemeFactory schemeFactory = mock(SchemeFactory.class, RETURNS_MOCKS);
        final DefaultWorkflowSchemeManager manager = new DefaultWorkflowSchemeManager(
                null, null, null, schemeFactory, null, null, ofBizDelegator, mockEventPublisher, null, null, null,
                context, userManager, i18nFactory, null, cacheManager, clusterLockService);
        manager.start();

        // Invoke
        manager.createScheme("New Scheme", "New Description");

        // Check
        verify(mockEventPublisher).publish(any(WorkflowSchemeCreatedEvent.class));
    }

    @Test
    public void assignableBuilder()
    {
        DSMForTest manager = new DSMForTest(null, context, userManager, i18nFactory, null);
        AssignableWorkflowScheme.Builder builder = manager.assignableBuilder();

        assertNotNull(builder);
    }

    @Test
    public void testIsUsingDefaultWorkflowUsingDefault()
    {
        final Project project = createProject(100002L, "BJB");
        final GenericValue schemeGv = createSchemeGV(9000L);

        final DefaultWorkflowSchemeManager testingClass = new DefaultWorkflowSchemeManager(
                null, null, null, null, null, null, null, mockEventPublisher, null, null, null, context, userManager,
                i18nFactory, null, cacheManager, clusterLockService)
        {
            @Override
            public List<GenericValue> getSchemes(final GenericValue projectGV)
            {
                assertSame(project.getGenericValue(), projectGV);

                return Collections.singletonList(schemeGv);
            }
        };

        assertFalse(testingClass.isUsingDefaultScheme(project));
    }

    @Test
    public void testIsUsingDefaultWorkflowNotUsingDefault()
    {
        final Project project = createProject(100002L, "BJB");

        final DefaultWorkflowSchemeManager testingClass = new DefaultWorkflowSchemeManager(
                null, null, null, null, null, null, null, mockEventPublisher, null, null, null, context, userManager,
                i18nFactory, null, cacheManager, clusterLockService)
        {
            @Override
            public List<GenericValue> getSchemes(final GenericValue projectGV)
            {
                assertSame(project.getGenericValue(), projectGV);

                return Collections.emptyList();
            }
        };

        assertTrue(testingClass.isUsingDefaultScheme(project));
    }

    private MockGenericValue createSchemeGV(final long id)
    {
        return createSchemeGV(id, null);
    }

    private MockGenericValue createSchemeGV(final long id, String defaultWf, String...others)
    {
        final MockGenericValue schemeGv = new MockGenericValue("WorkflowScheme");
        schemeGv.set("id", id);
        schemeGv.setRelated("ChildWorkflowSchemeEntity", createSchemeEntries(defaultWf, others));
        when(ofBizDelegator.findById("WorkflowScheme", id)).thenReturn(schemeGv);
        return schemeGv;
    }

    private static List<GenericValue> createSchemeEntries(final String defaultWorkflow, String ... args)
    {
        List<GenericValue> entries = new ArrayList<GenericValue>(args.length / 2 + 1);
        if (defaultWorkflow != null)
        {
            MockGenericValue defaultValue = new MockGenericValue("sjsjs");
            defaultValue.set("workflow", defaultWorkflow);
            defaultValue.set("issuetype", "0");
            entries.add(defaultValue);
        }

        for (String arg : args)
        {
            MockGenericValue value = new MockGenericValue("sjsjs");
            value.set("workflow", arg);
            value.set("issuetype", arg);

            entries.add(value);
        }
        return entries;
    }

    private static Project createProject(long id, String name)
    {
        final GenericValue projectGv = new MockGenericValue("Project");
        projectGv.set("id", id);
        return new MockProject(id, name, name, projectGv);
    }

    private class DSMForTest extends DefaultWorkflowSchemeManager
    {
        private Map<Long, AssignableWorkflowScheme> workflowScheme = Maps.newHashMap();

        public DSMForTest(DraftWorkflowSchemeStore draftWorkflowSchemeStore, JiraAuthenticationContext context,
                UserManager userManager, I18nHelper.BeanFactory i18nFactory,
                AssignableWorkflowSchemeStore assignableWorkflowSchemeStore)
        {
            super(null, null, null, null, null, null, ofBizDelegator, mockEventPublisher, null, null,
                    draftWorkflowSchemeStore, context, userManager, i18nFactory, assignableWorkflowSchemeStore,
                    cacheManager, clusterLockService);
        }

        private DSMForTest addScheme(AssignableWorkflowScheme assignableWorkflowScheme)
        {
            workflowScheme.put(assignableWorkflowScheme.getId(), assignableWorkflowScheme);
            return this;
        }

        @Override
        public AssignableWorkflowScheme getWorkflowSchemeObj(long id)
        {
            if (workflowScheme.containsKey(id))
            {
                return workflowScheme.get(id);
            }
            throw new IllegalArgumentException(format("Was not expecting a query for a workflow scheme with id '%d'.", id));
        }
    }
}
