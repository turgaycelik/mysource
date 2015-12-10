package com.atlassian.jira.config;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.beehive.ClusterLockService;
import com.atlassian.beehive.simple.SimpleClusterLockService;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeImpl;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.matchers.IterableMatchers;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.config.ConstantsManager.ISSUE_TYPE_CONSTANT_TYPE;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v5.0
 */
@RunWith (ListeningMockitoRunner.class)
public class TestDefaultIssueTypeManager
{
    private ConstantsManager constantsManager;
    private IssueIndexManager issueIndexManager;
    private OfBizDelegator ofBizDelegator;
    private ProjectManager projectManager;
    private DefaultIssueTypeManager issueTypeManager;
    private WorkflowManager workflowManager;
    private FieldLayoutManager fieldLayoutManager;
    private IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private IssueTypeSchemeManager issueTypeSchemeManager;
    private WorkflowSchemeManager workflowSchemeManager;
    private FieldConfigSchemeManager fieldConfigSchemeManager;
    private MockIssueConstantFactory factory;

    public static final GenericValue IRRELEVANT = new MockGenericValue("irrelevant", 888L);
    public static final long AVAILABILITY_ISSUE_TYPE_CHECK_SOURCE_ID = 123;
    public static final long AVAILABILITY_ISSUE_TYPE_CHECK_DEST_ID = 124;


    @Before
    public void setUp()
    {
        constantsManager = mock(ConstantsManager.class);
        issueIndexManager = mock(IssueIndexManager.class);
        ofBizDelegator = mock(OfBizDelegator.class);
        projectManager = mock(ProjectManager.class);
        workflowManager = mock(WorkflowManager.class);
        fieldLayoutManager = mock(FieldLayoutManager.class);
        issueTypeScreenSchemeManager = mock(IssueTypeScreenSchemeManager.class);
        issueTypeSchemeManager = mock(IssueTypeSchemeManager.class);
        workflowSchemeManager = mock(WorkflowSchemeManager.class);
        fieldConfigSchemeManager = mock(FieldConfigSchemeManager.class);
        CustomFieldManager customFieldManager = mock(CustomFieldManager.class);
        EventPublisher eventPublisher = mock(EventPublisher.class);
        factory = new MockIssueConstantFactory();
        final ClusterLockService clusterLockService = new SimpleClusterLockService();
        issueTypeManager = new DefaultIssueTypeManager(constantsManager, ofBizDelegator, issueIndexManager,
                projectManager, workflowManager, fieldLayoutManager, issueTypeScreenSchemeManager, issueTypeSchemeManager,
                workflowSchemeManager, fieldConfigSchemeManager, customFieldManager, eventPublisher, factory, clusterLockService)
        {
            @Override
            protected String getNextStringId() throws GenericEntityException
            {
                return "10";
            }

            @Override
            protected void removePropertySet(GenericValue constantGv)
            {
            }
        };
        issueTypeManager.start();
    }


    @Test
    public void testCreateStandardIssueType() throws Exception
    {
        MockGenericValue bugIssueTypeGV = new MockGenericValue("IssueType", 1l);
        bugIssueTypeGV.set("name", "Bug");
        bugIssueTypeGV.set("description", "A bug");
        bugIssueTypeGV.set("iconurl", "http://www.toastmasters.com");
        IssueType bugIssueType = factory.createIssueType(bugIssueTypeGV);

        MockGenericValue featureIssueTypeGV = new MockGenericValue("IssueType", 2l);
        featureIssueTypeGV.set("name", "Feature");
        featureIssueTypeGV.set("description", "A new feature");
        featureIssueTypeGV.set("iconurl", "http://www.atlassian.com");
        IssueType featureIssueType = factory.createIssueType(featureIssueTypeGV);

        when(constantsManager.getAllIssueTypeObjects()).thenReturn(newArrayList(bugIssueType, featureIssueType));

        MockGenericValue usabilityIssueTypeGV = new MockGenericValue("IssueType", 10l);
        usabilityIssueTypeGV.set("name", "Usability Design");
        usabilityIssueTypeGV.set("description", "Usability design for a new feature");
        usabilityIssueTypeGV.set("iconurl", "http://www.usability.com");
        final IssueTypeFieldsdArgumentMatcher matcher = new IssueTypeFieldsdArgumentMatcher(
                "10", "Usability Design", "Usability design for a new feature", "http://www.usability.com", null);
        when(ofBizDelegator.createValue(eq(ISSUE_TYPE_CONSTANT_TYPE), argThat(matcher))).thenReturn(usabilityIssueTypeGV);

        IssueType issueType = issueTypeManager.createIssueType("Usability Design", "Usability design for a new feature", "http://www.usability.com");
        assertEquals("Usability Design", issueType.getName());

        verify(issueTypeSchemeManager).addOptionToDefault("10");
        verify(constantsManager).refreshIssueTypes();
    }

    @Test
    public void testCreateStandardIssueTypeDuplicateName() throws Exception
    {
        MockGenericValue bugIssueTypeGV = new MockGenericValue("IssueType", 1l);
        bugIssueTypeGV.set("name", "Bug");
        bugIssueTypeGV.set("description", "A bug");
        bugIssueTypeGV.set("iconurl", "http://www.toastmasters.com");
        IssueType bugIssueType = factory.createIssueType(bugIssueTypeGV);

        MockGenericValue featureIssueTypeGV = new MockGenericValue("IssueType", 2l);
        featureIssueTypeGV.set("name", "Feature");
        featureIssueTypeGV.set("description", "A new feature");
        featureIssueTypeGV.set("iconurl", "http://www.atlassian.com");
        IssueType featureIssueType = factory.createIssueType(featureIssueTypeGV);

        when(constantsManager.getAllIssueTypeObjects()).thenReturn(newArrayList(bugIssueType, featureIssueType));

        try
        {
            issueTypeManager.createIssueType(" fEAtUre ", "Usability design for a new feature", "http://www.usability.com");
            fail("Expected failure: An issue type with the name ' fEAtUre ' exists already.");
        }
        catch (IllegalStateException ex)
        {
            assertEquals("An issue type with the name ' fEAtUre ' exists already.", ex.getMessage());
        }
    }

    @Test
    public void testEditIssueTypes() throws Exception
    {
        MockGenericValue bugIssueTypeGV = new MockGenericValue("IssueType", 1l);
        bugIssueTypeGV.set("name", "Bug");
        bugIssueTypeGV.set("description", "A bug");
        bugIssueTypeGV.set("iconurl", "http://www.toastmasters.com");
        IssueType bugIssueType = factory.createIssueType(bugIssueTypeGV);

        MockGenericValue featureIssueTypeGV = new MockGenericValue("IssueType", 2l)
        {
            @Override
            public void store() throws GenericEntityException
            {
            }
        };
        featureIssueTypeGV.set("name", "Feature");
        featureIssueTypeGV.set("description", "A new feature");
        featureIssueTypeGV.set("iconurl", "http://www.atlassian.com");
        IssueType featureIssueType = factory.createIssueType(featureIssueTypeGV);

        when(constantsManager.getAllIssueTypeObjects()).thenReturn(newArrayList(bugIssueType, featureIssueType));

        issueTypeManager.editIssueType(featureIssueType, "Small Feature", "new description", "http://test.de");

        assertEquals("Small Feature", featureIssueType.getName());
        assertEquals("new description", featureIssueType.getDescription());
        assertEquals("http://test.de", featureIssueType.getIconUrl());

        verify(constantsManager).refreshIssueTypes();
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testRemoveIssueTypes() throws Exception
    {
        final GenericValue issueGV = new MockGenericValue("Issue", 1234l);
        final BooleanHolder removedBugIssueType = new BooleanHolder();
        GenericValue bugIssueTypeGV = new MockGenericValue("IssueType", 1l)
        {
            @Override
            public List<GenericValue> getRelated(String s) throws GenericEntityException
            {
                if (s.equals("ChildIssue"))
                {
                    return newArrayList(issueGV);
                }
                throw new UnsupportedOperationException("Method call not mocked!");
            }

            @Override
            public void remove() throws GenericEntityException
            {
                removedBugIssueType.booleanValue = true;
            }
        };
        bugIssueTypeGV.set("name", "Bug");
        bugIssueTypeGV.set("description", "A bug");
        bugIssueTypeGV.set("iconurl", "http://www.toastmasters.com");
        IssueType bugIssueType = factory.createIssueType(bugIssueTypeGV);

        MockGenericValue featureIssueTypeGV = new MockGenericValue("IssueType", 2l);
        featureIssueTypeGV.set("name", "Feature");
        featureIssueTypeGV.set("description", "A new feature");
        featureIssueTypeGV.set("iconurl", "http://www.atlassian.com");
        IssueType featureIssueType = factory.createIssueType(featureIssueTypeGV);

        when(constantsManager.getAllIssueTypeObjects()).thenReturn(newArrayList(bugIssueType, featureIssueType));
        when(constantsManager.getIssueTypeObject("1")).thenReturn(bugIssueType);
        when(constantsManager.getIssueTypeObject("2")).thenReturn(featureIssueType);

        GenericValue projectGV = new MockGenericValue("Project", 1234l);
        when(projectManager.getProjects()).thenReturn(newArrayList(projectGV));

        JiraWorkflow workflow = mock(JiraWorkflow.class);
        when(workflowManager.getWorkflow(1234l, "1")).thenReturn(workflow);

        when(fieldLayoutManager.getFieldLayoutSchemes()).thenReturn(Collections.<FieldLayoutScheme>emptyList());
        when(issueTypeScreenSchemeManager.getIssueTypeScreenSchemes()).thenReturn(Collections.<IssueTypeScreenScheme>emptyList());

        when(constantsManager.getIssueType("1")).thenReturn(bugIssueTypeGV);
        when(constantsManager.getSubTaskIssueTypeObjects()).thenReturn(Collections.<IssueType>emptyList());
        when(constantsManager.getIssueTypes()).thenReturn(newArrayList(bugIssueTypeGV, featureIssueTypeGV));
        when(workflowManager.getWorkflow(1234l, "2")).thenReturn(workflow);

        MockGenericValue workflowScheme = new MockGenericValue("WorkflowScheme", 10l);
        when(workflowSchemeManager.getSchemes()).thenReturn(Lists.<GenericValue>newArrayList(workflowScheme));
        MockGenericValue workflowSchemeEntity = new MockGenericValue("WorkflowSchemeEntity", 20l);
        workflowSchemeEntity.set("issuetype", 1);

        when(workflowSchemeManager.getEntities(workflowScheme)).thenReturn(Lists.<GenericValue>newArrayList(workflowSchemeEntity));


        MockGenericValue fieldLayoutSchemeGV = new MockGenericValue("FieldLayoutScheme", 10l);
        final BooleanHolder removedFieldLayoutSchemed = new BooleanHolder();

        FieldLayoutScheme fieldLayoutScheme = new FieldLayoutSchemeImpl(fieldLayoutManager, fieldLayoutSchemeGV)
        {
            @Override
            public boolean containsEntity(String issueTypeId)
            {
                return issueTypeId.equals("1");
            }

            @Override
            public void removeEntity(String issueTypeId)
            {
                if (issueTypeId.equals("1"))
                {
                    removedFieldLayoutSchemed.booleanValue = true;
                }
            }
        };
        when(fieldLayoutManager.getFieldLayoutSchemes()).thenReturn(newArrayList(fieldLayoutScheme));

        MockGenericValue issueTypeScreenSchemeGV = new MockGenericValue("IssueTypeScreenScheme", 10l);

        final BooleanHolder removedIssueTypeScreenScheme = new BooleanHolder();
        IssueTypeScreenScheme issueTypeScreenScheme =
                new IssueTypeScreenSchemeImpl(issueTypeScreenSchemeManager, issueTypeScreenSchemeGV)
        {
            @Override
            public boolean containsEntity(String issueTypeId)
            {
                return issueTypeId.equals("1");
            }

            @Override
            public void removeEntity(String issueTypeId)
            {
                if (issueTypeId.equals("1"))
                {
                    removedIssueTypeScreenScheme.booleanValue = true;
                }
            }
        };
        when(issueTypeScreenSchemeManager.getIssueTypeScreenSchemes()).thenReturn(newArrayList(issueTypeScreenScheme));

        issueTypeManager.removeIssueType("1", "2");

        assertTrue(removedFieldLayoutSchemed.booleanValue);
        verify(workflowSchemeManager).deleteEntity(20l);
        assertTrue(removedIssueTypeScreenScheme.booleanValue);
        verify(fieldConfigSchemeManager).removeInvalidFieldConfigSchemesForIssueType(bugIssueType);
        verify(constantsManager).refreshIssueTypes();
        verify(issueTypeSchemeManager).removeOptionFromAllSchemes("1");

        verify(issueIndexManager).reIndex(issueGV);
        verify(ofBizDelegator).storeAll(newArrayList(issueGV));
        assertTrue(removedBugIssueType.booleanValue);
        verify(constantsManager).refreshIssueTypes();
    }

    @Test
    public void testAvailableIssueTypesChecksSucceedsWithSameScreensWorkflowsAndLayoutSchemes() throws Exception
    {
        final IssueType sourceIssueType = prepareAvailabilityTestCase();
        availabilityTcWithCommonFieldLayouts();
        availabilityTcWithCommonWorkflows();
        availabilityTcWithCommonScreenSchemes();

        assertEquals(Long.toString(AVAILABILITY_ISSUE_TYPE_CHECK_DEST_ID),
                getOnlyElement(issueTypeManager.getAvailableIssueTypes(sourceIssueType)).getId());
    }

    @Test
    public void testAvailableIssueTypesChecksFiltersOutdifferentWorkflows() throws Exception
    {
        final IssueType sourceIssueType = prepareAvailabilityTestCase();
        availabilityTcWithCommonFieldLayouts();
        availabilityTcWithCommonScreenSchemes();

        // mock separate workflows:
        final JiraWorkflow workflow1 = mock(JiraWorkflow.class);
        final JiraWorkflow workflow2 = mock(JiraWorkflow.class);

        when(workflowManager.getWorkflow(anyLong(), eq(Long.toString(AVAILABILITY_ISSUE_TYPE_CHECK_SOURCE_ID)))).thenReturn(workflow1);
        when(workflowManager.getWorkflow(anyLong(), eq(Long.toString(AVAILABILITY_ISSUE_TYPE_CHECK_DEST_ID)))).thenReturn(workflow2);

        assertThat(issueTypeManager.getAvailableIssueTypes(sourceIssueType), IterableMatchers.emptyIterable(IssueType.class));
    }

    @Test
    public void testAvailableIssueTypesChecksFiltersOutdifferentFieldLayouts() throws Exception
    {
        final IssueType sourceIssueType = prepareAvailabilityTestCase();
        availabilityTcWithCommonWorkflows();
        availabilityTcWithCommonScreenSchemes();

        // mock separate field layouts:
        final long commonFieldLayoutId1 = 7776;
        final long commonFieldLayoutId2 = 7778;

        final FieldLayoutScheme fieldLayoutScheme1 = mock(FieldLayoutScheme.class);
        final FieldLayoutScheme fieldLayoutScheme2 = mock(FieldLayoutScheme.class);
        when(fieldLayoutManager.getFieldLayoutSchemes()).thenReturn(ImmutableList.of(fieldLayoutScheme1, fieldLayoutScheme2));
        when(fieldLayoutScheme1.getProjects()).thenReturn(singletonList(IRRELEVANT));
        when(fieldLayoutScheme2.getProjects()).thenReturn(singletonList(IRRELEVANT));

        when(fieldLayoutScheme1.getFieldLayoutId(anyString())).thenReturn(commonFieldLayoutId1);
        // mismatch on one of the layout schemes:
        when(fieldLayoutScheme2.getFieldLayoutId(Long.toString(AVAILABILITY_ISSUE_TYPE_CHECK_SOURCE_ID))).thenReturn(commonFieldLayoutId1);
        when(fieldLayoutScheme2.getFieldLayoutId(Long.toString(AVAILABILITY_ISSUE_TYPE_CHECK_DEST_ID))).thenReturn(commonFieldLayoutId2);

        assertThat(issueTypeManager.getAvailableIssueTypes(sourceIssueType), IterableMatchers.emptyIterable(IssueType.class));
    }

    @Test
    public void testAvailableIssueTypesChecksFiltersOutdifferentScreenSchemes() throws Exception
    {
        final IssueType sourceIssueType = prepareAvailabilityTestCase();
        availabilityTcWithCommonWorkflows();
        availabilityTcWithCommonFieldLayouts();

        // mock separate screen schemes:
        final FieldScreenScheme fieldScreenScheme1 = mock(FieldScreenScheme.class);
        final FieldScreenScheme fieldScreenScheme2 = mock(FieldScreenScheme.class);

        final IssueTypeScreenScheme itss1 = mock(IssueTypeScreenScheme.class);
        final IssueTypeScreenScheme itss2 = mock(IssueTypeScreenScheme.class);
        when(issueTypeScreenSchemeManager.getIssueTypeScreenSchemes()).thenReturn(ImmutableList.of(itss1, itss2));
        when(itss1.getProjects()).thenReturn(singletonList(IRRELEVANT));
        when(itss2.getProjects()).thenReturn(singletonList(IRRELEVANT));

        final IssueTypeScreenSchemeEntity entity1 = mock(IssueTypeScreenSchemeEntity.class);
        final IssueTypeScreenSchemeEntity entity2 = mock(IssueTypeScreenSchemeEntity.class);
        when(entity1.getFieldScreenScheme()).thenReturn(fieldScreenScheme1);
        when(entity2.getFieldScreenScheme()).thenReturn(fieldScreenScheme2);

        // mismatch on screen schemes (entities will provide different schemes, mocked just above)
        when(itss1.getEntity(Long.toString(AVAILABILITY_ISSUE_TYPE_CHECK_SOURCE_ID))).thenReturn(entity1);
        when(itss1.getEntity(Long.toString(AVAILABILITY_ISSUE_TYPE_CHECK_DEST_ID))).thenReturn(entity2);

        when(itss2.getEntity(anyString())).thenReturn(entity2);

        assertThat(issueTypeManager.getAvailableIssueTypes(sourceIssueType), IterableMatchers.emptyIterable(IssueType.class));
    }

    @Test
    public void shouldCreateOneIssueTypeWithAvatarWhenTheSameIssuetypeCreatedFromThreads() throws InterruptedException
    {
        // given
        final String ISSUE_TYPE_NAME = "Crap";

        configMocksToSeeIssueTypeAddition(ISSUE_TYPE_NAME);

        final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        // when
        for (int i = 0; i < 2 * Runtime.getRuntime().availableProcessors(); ++i)
        {
            executorService.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    issueTypeManager.createIssueType(ISSUE_TYPE_NAME, "Crappy", 888L);
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);

        // then
        verify(ofBizDelegator, times(1)).createValue(eq(ISSUE_TYPE_CONSTANT_TYPE), anyMapOf(String.class, Object.class));
    }

    @Test
    public void shouldCreateOneIssueTypeWithTypeWhenTheSameIssuetypeCreatedFromThreads() throws InterruptedException
    {
        // given
        final String ISSUE_TYPE_NAME = "Crap";

        configMocksToSeeIssueTypeAddition(ISSUE_TYPE_NAME);

        final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        // when
        for (int i = 0; i < 2 * Runtime.getRuntime().availableProcessors(); ++i)
        {
            executorService.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    issueTypeManager.createIssueType(ISSUE_TYPE_NAME, "Crappy", "icon");
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);

        // then
        verify(ofBizDelegator, times(1)).createValue(eq(ISSUE_TYPE_CONSTANT_TYPE), anyMapOf(String.class, Object.class));
    }

    private void configMocksToSeeIssueTypeAddition(final String issueName)
    {
        final AtomicBoolean issueTypeAdded = new AtomicBoolean(false);
        when(ofBizDelegator.createValue(eq(ISSUE_TYPE_CONSTANT_TYPE), anyMapOf(String.class, Object.class)))
                .thenAnswer(new Answer<GenericValue>()
                {
                    @Override
                    public GenericValue answer(final InvocationOnMock invocation)
                    {
                        issueTypeAdded.set(true);

                        return IRRELEVANT;
                    }
                });
        when(constantsManager.getAllIssueTypeObjects()).thenAnswer(new Answer<Collection<IssueType>>()
        {
            @Override
            public Collection<IssueType> answer(final InvocationOnMock invocation) throws Throwable
            {
                return issueTypeAdded.get() ?
                        ImmutableList.of((IssueType) new MockIssueType(1l, issueName)) :
                        ImmutableList.<IssueType>of();
            }
        });
    }

    private void availabilityTcWithCommonScreenSchemes()
    {
        final FieldScreenScheme commonFieldScreenScheme = mock(FieldScreenScheme.class);
        final IssueTypeScreenScheme itss1 = mock(IssueTypeScreenScheme.class);
        final IssueTypeScreenScheme itss2 = mock(IssueTypeScreenScheme.class);
        when(issueTypeScreenSchemeManager.getIssueTypeScreenSchemes()).thenReturn(ImmutableList.of(itss1, itss2));
        when(itss1.getProjects()).thenReturn(singletonList(IRRELEVANT));
        when(itss2.getProjects()).thenReturn(singletonList(IRRELEVANT));
        final IssueTypeScreenSchemeEntity entity = mock(IssueTypeScreenSchemeEntity.class);
        when(itss1.getEntity(anyString())).thenReturn(entity);
        when(itss2.getEntity(anyString())).thenReturn(entity);
        when(entity.getFieldScreenScheme()).thenReturn(commonFieldScreenScheme);
    }

    private void availabilityTcWithCommonWorkflows()
    {
        final JiraWorkflow sameWorkflowForEveryone = mock(JiraWorkflow.class);
        when(workflowManager.getWorkflow(anyLong(), anyString())).thenReturn(sameWorkflowForEveryone);
    }

    private void availabilityTcWithCommonFieldLayouts()
    {
        final long commonFieldLayoutId = 6789;
        final FieldLayoutScheme fieldLayoutScheme1 = mock(FieldLayoutScheme.class);
        final FieldLayoutScheme fieldLayoutScheme2 = mock(FieldLayoutScheme.class);
        when(fieldLayoutManager.getFieldLayoutSchemes()).thenReturn(ImmutableList.of(fieldLayoutScheme1, fieldLayoutScheme2));
        when(fieldLayoutScheme1.getProjects()).thenReturn(singletonList(IRRELEVANT));
        when(fieldLayoutScheme2.getProjects()).thenReturn(singletonList(IRRELEVANT));
        when(fieldLayoutScheme1.getFieldLayoutId(anyString())).thenReturn(commonFieldLayoutId);
        when(fieldLayoutScheme2.getFieldLayoutId(anyString())).thenReturn(commonFieldLayoutId);
    }

    @SuppressWarnings("deprecation")
    private IssueType prepareAvailabilityTestCase()
    {
        final IssueType sourceIssueType = new MockIssueType(AVAILABILITY_ISSUE_TYPE_CHECK_SOURCE_ID, "Source");
        final GenericValue srcGv = new MockGenericValue("IssueType", AVAILABILITY_ISSUE_TYPE_CHECK_SOURCE_ID);
        final GenericValue destGv = new MockGenericValue("IssueType", AVAILABILITY_ISSUE_TYPE_CHECK_DEST_ID);
        final GenericValue project1 = new MockGenericValue("project", 1L);
        final GenericValue project2 = new MockGenericValue("project", 2L);
        when(projectManager.getProjects()).thenReturn(ImmutableList.of(project1, project2));
        when(constantsManager.getIssueType(sourceIssueType.getId())).thenReturn(IRRELEVANT);
        when(constantsManager.getIssueTypes()).thenReturn(ImmutableList.of(srcGv, destGv));
        return sourceIssueType;
    }

    private static class IssueTypeFieldsdArgumentMatcher extends ArgumentMatcher<Map<String, Object>>
    {
        private final String id;
        private final String name;
        private final String description;
        private final String iconUrl;
        private final String style;

        IssueTypeFieldsdArgumentMatcher(String id, String name, String description, String iconUrl, String style)
        {
            this.id = id;
            this.name = name;
            this.description = description;
            this.iconUrl = iconUrl;
            this.style = style;
        }

        public boolean matches(final Object o)
        {
            if (!(o instanceof Map))
            {
                return false;
            }
            final Map<?, ?> gv = (Map) o;
            return id.equals(gv.get("id"))
                    && name.equals(gv.get("name"))
                    && description.equals(gv.get("description"))
                    && iconUrl.equals(gv.get("iconurl"))
                    && (style == null ? gv.get("style") == null : style.equals(gv.get("style")));
        }
    }
}
