package com.atlassian.jira.jql.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.collect.CollectionBuilder;

import com.google.common.collect.ImmutableMap;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestFieldConfigSchemeClauseContextUtil extends MockControllerTestCase
{
    private IssueTypeSchemeManager issueTypeSchemeManager;
    private ConstantsManager constantsManager;
    private PermissionManager permissionManager;
    private ProjectFactory projectFactory;
    private User searcher;
    private List<Project> projects = new ArrayList<Project>();
    final Project mockProject1 = new MockProject(10L, "test project1");
    final Project mockProject2 = new MockProject(20L, "test project2");

    @Before
    public void setUp() throws Exception
    {
        issueTypeSchemeManager = mockController.getMock(IssueTypeSchemeManager.class);
        constantsManager = mockController.getMock(ConstantsManager.class);
        permissionManager = mockController.getMock(PermissionManager.class);
        projectFactory = mockController.getMock(ProjectFactory.class);
        searcher = null;

        projects.add(mockProject1);
        projects.add(mockProject2);
    }

    @Test
    public void testGetFieldConfigSchemeFromContextOneGlobalOneNonGlobalVisible() throws Exception
    {
        final FieldConfigScheme global = EasyMock.createMock(FieldConfigScheme.class);
        final FieldConfigScheme issueType = EasyMock.createMock(FieldConfigScheme.class);
        final FieldConfigScheme project = EasyMock.createMock(FieldConfigScheme.class);
        final FieldConfigScheme both = EasyMock.createMock(FieldConfigScheme.class);

        setExpectations(global, issueType, project, both);
        _testGetMostSpeficScheme(CollectionBuilder.newBuilder(global, issueType).asList(), issueType);

        setExpectations(global, issueType, project, both);
        _testGetMostSpeficScheme(CollectionBuilder.newBuilder(global, project).asList(), project);

        setExpectations(global, issueType, project, both);
        _testGetMostSpeficScheme(CollectionBuilder.newBuilder(project, global).asList(), project);

        setExpectations(global, issueType, project, both);
        _testGetMostSpeficScheme(CollectionBuilder.newBuilder(issueType, global).asList(), issueType);

        setExpectations(global, issueType, project, both);
        _testGetMostSpeficScheme(CollectionBuilder.newBuilder(global, issueType, both, project).asList(), both);

        setExpectations(global, issueType, project, both);
        _testGetMostSpeficScheme(CollectionBuilder.newBuilder(project, global, both).asList(), both);

        setExpectations(global, issueType, project, both);
        _testGetMostSpeficScheme(CollectionBuilder.newBuilder(both, issueType, global).asList(), both);
        
        mockController.replay();
    }

    private void setExpectations(final FieldConfigScheme global, final FieldConfigScheme issueType, final FieldConfigScheme project, final FieldConfigScheme both)
    {
        EasyMock.reset(global);
        EasyMock.expect(global.isGlobal()).andReturn(true).anyTimes();
        EasyMock.expect(global.isAllIssueTypes()).andReturn(true).anyTimes();
        EasyMock.expect(global.isAllProjects()).andReturn(true).anyTimes();

        EasyMock.reset(issueType);
        EasyMock.expect(issueType.isGlobal()).andReturn(false).anyTimes();
        EasyMock.expect(issueType.isAllIssueTypes()).andReturn(false).anyTimes();
        EasyMock.expect(issueType.isAllProjects()).andReturn(true).anyTimes();

        EasyMock.reset(project);
        EasyMock.expect(project.isGlobal()).andReturn(false).anyTimes();
        EasyMock.expect(project.isAllIssueTypes()).andReturn(true).anyTimes();
        EasyMock.expect(project.isAllProjects()).andReturn(false).anyTimes();

        EasyMock.reset(both);
        EasyMock.expect(both.isGlobal()).andReturn(false).anyTimes();
        EasyMock.expect(both.isAllIssueTypes()).andReturn(false).anyTimes();
        EasyMock.expect(both.isAllProjects()).andReturn(false).anyTimes();
    }

    private void _testGetMostSpeficScheme(List<FieldConfigScheme> schemes, FieldConfigScheme mostSpecific)
    {
        final CustomField customField = EasyMock.createMock(CustomField.class);
        EasyMock.expect(customField.getConfigurationSchemes()).andReturn(schemes);

        QueryContext queryContext = new QueryContextImpl(new ClauseContextImpl());

        for (FieldConfigScheme scheme : schemes)
        {
            EasyMock.replay(scheme);
        }
        EasyMock.replay(customField);
        final FieldConfigSchemeClauseContextUtil util = new FieldConfigSchemeClauseContextUtil(issueTypeSchemeManager, constantsManager, permissionManager, projectFactory)
        {
            @Override
            public boolean isConfigSchemeVisibleUnderContext(final QueryContext queryContext, final FieldConfigScheme fieldConfigScheme)
            {
                return true;
            }
        };

        final FieldConfigScheme result = util.getFieldConfigSchemeFromContext(queryContext, customField);
        assertEquals(mostSpecific, result);
        for (FieldConfigScheme scheme : schemes)
        {
            EasyMock.verify(scheme);
        }
        EasyMock.verify(customField);
    }

    @Test
    public void testAddProjectIssueTypeContextsForProjectsAllIssueTypes() throws Exception
    {
        final FieldConfigScheme fieldConfigScheme = mockController.getMock(FieldConfigScheme.class);
        fieldConfigScheme.isAllIssueTypes();
        mockController.setDefaultReturnValue(true);

        mockController.replay();

        final FieldConfigSchemeClauseContextUtil util = new FieldConfigSchemeClauseContextUtil(issueTypeSchemeManager, constantsManager, permissionManager, projectFactory);

        final Set<ProjectIssueTypeContext> projectIssueTypeContextSet = util.addProjectIssueTypeContextsForProjects(fieldConfigScheme, projects);

        final Set<ProjectIssueTypeContext> expectedContextSet = CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(mockProject1.getId()), AllIssueTypesContext.INSTANCE),
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(mockProject2.getId()), AllIssueTypesContext.INSTANCE)).asSet();

        assertEquals(expectedContextSet, projectIssueTypeContextSet);
        mockController.verify();
    }

    @Test
    public void testAddProjectIssueTypeContextsForProjectsNullIssueTypes() throws Exception
    {
        final FieldConfigScheme fieldConfigScheme = mockController.getMock(FieldConfigScheme.class);
        fieldConfigScheme.isAllIssueTypes();
        mockController.setDefaultReturnValue(false);

        fieldConfigScheme.getAssociatedIssueTypes();
        mockController.setDefaultReturnValue(null);

        mockController.replay();

        final FieldConfigSchemeClauseContextUtil util = new FieldConfigSchemeClauseContextUtil(issueTypeSchemeManager, constantsManager, permissionManager, projectFactory);

        final Set<ProjectIssueTypeContext> projectIssueTypeContextSet = util.addProjectIssueTypeContextsForProjects(fieldConfigScheme, projects);

        final Set<ProjectIssueTypeContext> expectedContextSet = CollectionBuilder.<ProjectIssueTypeContext>newBuilder().asSet();

        assertEquals(expectedContextSet, projectIssueTypeContextSet);
        mockController.verify();
    }

    @Test
    public void testAddProjectIssueTypeContextsForProjectsOneProjectAllIssueTypesOtherConstrained() throws Exception
    {
        final IssueType issueType1 = new MockIssueType("it1", "test type1");
        final IssueType issueType2 = new MockIssueType("it2", "test type2");

        final MockGenericValue issueTypeGv1 = new MockGenericValue("blah", ImmutableMap.of("id", "it1"));

        final FieldConfigScheme fieldConfigScheme = mockController.getMock(FieldConfigScheme.class);
        fieldConfigScheme.isAllIssueTypes();
        mockController.setReturnValue(true);
        fieldConfigScheme.isAllIssueTypes();
        mockController.setReturnValue(false);
        fieldConfigScheme.getAssociatedIssueTypes();
        mockController.setDefaultReturnValue(Collections.singleton(issueTypeGv1));

        issueTypeSchemeManager.getIssueTypesForProject(mockProject2);
        mockController.setReturnValue(CollectionBuilder.newBuilder(issueType1, issueType2).asCollection());

        constantsManager.getIssueTypeObject("it1");
        mockController.setReturnValue(issueType1);

        mockController.replay();

        final FieldConfigSchemeClauseContextUtil util = new FieldConfigSchemeClauseContextUtil(issueTypeSchemeManager, constantsManager, permissionManager, projectFactory);

        final Set<ProjectIssueTypeContext> projectIssueTypeContextSet = util.addProjectIssueTypeContextsForProjects(fieldConfigScheme, projects);

        final Set<ProjectIssueTypeContext> expectedContextSet = CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(mockProject1.getId()), AllIssueTypesContext.INSTANCE),
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(mockProject2.getId()), new IssueTypeContextImpl("it1"))).asSet();

        assertEquals(expectedContextSet, projectIssueTypeContextSet);
        mockController.verify();

    }

    @Test
    public void testGetClauseContextForFieldContextGlobalProjectIssueTypeConfiguration() throws Exception
    {
        final FieldConfigScheme fieldConfigScheme = mockController.getMock(FieldConfigScheme.class);
        fieldConfigScheme.isAllProjects();
        mockController.setReturnValue(true);
        fieldConfigScheme.isAllIssueTypes();
        mockController.setReturnValue(true);

        mockController.replay();

        final FieldConfigSchemeClauseContextUtil util = new FieldConfigSchemeClauseContextUtil(issueTypeSchemeManager, constantsManager, permissionManager, projectFactory);

        final ClauseContext clauseContext = util.getContextForConfigScheme(null, fieldConfigScheme);

        final ClauseContext expectedContext = ClauseContextImpl.createGlobalClauseContext();

        assertEquals(expectedContext, clauseContext);
        mockController.verify();
    }

    @Test
    public void testGetClauseContextForFieldContextGlobalProjectSpecifiedIssueTypeConfiguration() throws Exception
    {
        final FieldConfigScheme fieldConfigScheme = mockController.getMock(FieldConfigScheme.class);
        fieldConfigScheme.isAllProjects();
        mockController.setReturnValue(true);
        fieldConfigScheme.isAllIssueTypes();
        mockController.setReturnValue(false);

        final MockGenericValue type1 = new MockGenericValue("issuetype", ImmutableMap.of("id", "it1"));
        final MockGenericValue type2 = new MockGenericValue("issuetype", ImmutableMap.of("id", "it2"));

        fieldConfigScheme.getAssociatedIssueTypes();
        mockController.setReturnValue(CollectionBuilder.newBuilder(type1, type2).asSet());

        mockController.replay();

        final Set<ProjectIssueTypeContext> expectedContextSet = CollectionBuilder.<ProjectIssueTypeContext>newBuilder(
                new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("it1")),
                new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("it2"))
        ).asSet();

        final FieldConfigSchemeClauseContextUtil util = new FieldConfigSchemeClauseContextUtil(issueTypeSchemeManager, constantsManager, permissionManager, projectFactory)
        {
            @Override
            Set<ProjectIssueTypeContext> addProjectIssueTypeContextsForProjects(final FieldConfigScheme fieldConfigScheme, final Collection<Project> associatedProjects)
            {
                return expectedContextSet;
            }
        };

        final ClauseContext clauseContext = util.getContextForConfigScheme(null, fieldConfigScheme);

        final ClauseContext expectedContext = new ClauseContextImpl(expectedContextSet);

        assertEquals(expectedContext, clauseContext);
        mockController.verify();
    }

    @Test
    public void testGetClauseContextForFieldContextSpecifiedProjectSpecifiedIssueTypeConfiguration() throws Exception
    {
        final FieldConfigScheme fieldConfigScheme = mockController.getMock(FieldConfigScheme.class);

        fieldConfigScheme.isAllProjects();
        mockController.setReturnValue(false);

        permissionManager.getProjectObjects(Permissions.BROWSE, searcher);
        mockController.setReturnValue(CollectionBuilder.<Project>newBuilder(mockProject1, mockProject2).asList());

        // dont bother mocking the associated projects return value properly as its passed straight into getprojects
        fieldConfigScheme.getAssociatedProjectObjects();
        mockController.setReturnValue(Arrays.asList(mockProject1));

        mockController.replay();

        final ProjectIssueTypeContextImpl projectIssueTypeContext = new ProjectIssueTypeContextImpl(new ProjectContextImpl(mockProject1.getId()), new IssueTypeContextImpl("it1"));
        final Set<ProjectIssueTypeContext> expectedContextSet = CollectionBuilder.<ProjectIssueTypeContext>newBuilder(projectIssueTypeContext).asSet();

        final FieldConfigSchemeClauseContextUtil util = new FieldConfigSchemeClauseContextUtil(issueTypeSchemeManager, constantsManager, permissionManager, projectFactory)
        {
            @Override
            Set<ProjectIssueTypeContext> addProjectIssueTypeContextsForProjects(final FieldConfigScheme fieldConfigScheme, final Collection<Project> associatedProjects)
            {
                return expectedContextSet;
            }
        };

        final ClauseContext clauseContext = util.getContextForConfigScheme(null, fieldConfigScheme);

        final ClauseContext expectedContext = new ClauseContextImpl(expectedContextSet);

        assertEquals(expectedContext, clauseContext);
        mockController.verify();
    }

    @Test
    public void testGetIssueTypeIdsForScheme() throws Exception
    {
        final FieldConfigScheme configScheme = mockController.getMock(FieldConfigScheme.class);
        configScheme.getAssociatedIssueTypes();
        mockController.setReturnValue(CollectionBuilder.newBuilder(
                new MockGenericValue("IssueType", ImmutableMap.of("id", "1")),
                new MockGenericValue("IssueType", ImmutableMap.of("id", "2"))
        ).asSet());

        mockController.replay();
        final FieldConfigSchemeClauseContextUtil schemeClauseContextUtil = new FieldConfigSchemeClauseContextUtil(issueTypeSchemeManager, constantsManager, permissionManager, projectFactory);
        final Set<String> result = schemeClauseContextUtil.getIssueTypeIdsForScheme(configScheme);

        Set<String> expectedResult = CollectionBuilder.newBuilder("1", "2").asSet();
        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetIssueTypeIdsForSchemeNullAssociatedIds() throws Exception
    {
        final FieldConfigScheme configScheme = mockController.getMock(FieldConfigScheme.class);
        configScheme.getAssociatedIssueTypes();
        mockController.setReturnValue(null);

        mockController.replay();
        final FieldConfigSchemeClauseContextUtil schemeClauseContextUtil = new FieldConfigSchemeClauseContextUtil(issueTypeSchemeManager, constantsManager, permissionManager, projectFactory);
        final Set<String> result = schemeClauseContextUtil.getIssueTypeIdsForScheme(configScheme);

        Set<String> expectedResult = CollectionBuilder.<String>newBuilder().asSet();
        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetProjectIdsForScheme() throws Exception
    {
        final FieldConfigScheme configScheme = mockController.getMock(FieldConfigScheme.class);
        final List<MockProject> projects =Arrays.asList(new MockProject(1), new MockProject(2));

        configScheme.getAssociatedProjectObjects();
        mockController.setReturnValue(projects);

        mockController.replay();
        final FieldConfigSchemeClauseContextUtil schemeClauseContextUtil = new FieldConfigSchemeClauseContextUtil(issueTypeSchemeManager, constantsManager, permissionManager, projectFactory);
        final Set<Long> result = schemeClauseContextUtil.getProjectIdsForScheme(configScheme);

        Set<Long> expectedResult = CollectionBuilder.newBuilder(1L, 2L).asSet();
        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetProjectIdsForSchemeNullProjects() throws Exception
    {
        final FieldConfigScheme configScheme = mockController.getMock(FieldConfigScheme.class);

        configScheme.getAssociatedProjectObjects();
        mockController.setReturnValue(null);

        mockController.replay();
        final FieldConfigSchemeClauseContextUtil schemeClauseContextUtil = new FieldConfigSchemeClauseContextUtil(issueTypeSchemeManager, constantsManager, permissionManager, projectFactory);
        final Set<Long> result = schemeClauseContextUtil.getProjectIdsForScheme(configScheme);

        assertTrue(result.isEmpty());

        mockController.verify();
    }

    @Test
    public void testFieldConfigSchemeContainsContextProjectsUsingAllProjectContext() throws Exception
    {
        final QueryContextImpl queryContext = new QueryContextImpl(ClauseContextImpl.createGlobalClauseContext());

        final FieldConfigScheme configScheme = mockController.getMock(FieldConfigScheme.class);

        final AtomicBoolean called = new AtomicBoolean(false);

        mockController.replay();

        final FieldConfigSchemeClauseContextUtil util = new FieldConfigSchemeClauseContextUtil(issueTypeSchemeManager, constantsManager, permissionManager, projectFactory)
        {
            @Override
            Set<Long> getProjectIdsForScheme(final FieldConfigScheme fieldConfigScheme)
            {
                called.set(true);
                return Collections.singleton(100L);
            }
        };

        assertFalse(util.fieldConfigSchemeContainsContextProjects(queryContext, configScheme));
        assertTrue(called.get());
        
        mockController.verify();
    }

    @Test
    public void testFieldConfigSchemeContainsContextProjectsUsingSpecificProjectContextDoesntMatch() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(100L), AllIssueTypesContext.INSTANCE);
        final ProjectIssueTypeContext projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(200L), AllIssueTypesContext.INSTANCE);
        final QueryContextImpl queryContext = new QueryContextImpl(new ClauseContextImpl(
                CollectionBuilder.newBuilder(projectIssueTypeContext1, projectIssueTypeContext2).asSet()));

        final FieldConfigScheme configScheme = mockController.getMock(FieldConfigScheme.class);

        final AtomicBoolean called = new AtomicBoolean(false);

        mockController.replay();

        final FieldConfigSchemeClauseContextUtil util = new FieldConfigSchemeClauseContextUtil(issueTypeSchemeManager, constantsManager, permissionManager, projectFactory)
        {
            @Override
            Set<Long> getProjectIdsForScheme(final FieldConfigScheme fieldConfigScheme)
            {
                called.set(true);
                return CollectionBuilder.newBuilder(300L, 400L).asSet();
            }
        };

        assertFalse(util.fieldConfigSchemeContainsContextProjects(queryContext, configScheme));
        assertTrue(called.get());

        mockController.verify();
    }

    @Test
    public void testFieldConfigSchemeContainsContextProjectsUsingSpecificProjectContextMatches() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(100L), AllIssueTypesContext.INSTANCE);
        final ProjectIssueTypeContext projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(200L), AllIssueTypesContext.INSTANCE);
        final QueryContextImpl queryContext = new QueryContextImpl(new ClauseContextImpl(
                CollectionBuilder.newBuilder(projectIssueTypeContext1, projectIssueTypeContext2).asSet()));

        final FieldConfigScheme configScheme = mockController.getMock(FieldConfigScheme.class);

        final AtomicBoolean called = new AtomicBoolean(false);

        mockController.replay();

        final FieldConfigSchemeClauseContextUtil util = new FieldConfigSchemeClauseContextUtil(issueTypeSchemeManager, constantsManager, permissionManager, projectFactory)
        {
            @Override
            Set<Long> getProjectIdsForScheme(final FieldConfigScheme fieldConfigScheme)
            {
                called.set(true);
                return CollectionBuilder.newBuilder(200L, 400L).asSet();
            }
        };

        assertTrue(util.fieldConfigSchemeContainsContextProjects(queryContext, configScheme));
        assertTrue(called.get());

        mockController.verify();
    }


    @Test
    public void testFieldConfigSchemeContainsContextIssueTypesUsingAllIssueTypesContext() throws Exception
    {
        final QueryContextImpl queryContext = new QueryContextImpl(ClauseContextImpl.createGlobalClauseContext());

        final FieldConfigScheme configScheme = mockController.getMock(FieldConfigScheme.class);

        final AtomicBoolean called = new AtomicBoolean(false);

        mockController.replay();

        final FieldConfigSchemeClauseContextUtil util = new FieldConfigSchemeClauseContextUtil(issueTypeSchemeManager, constantsManager, permissionManager, projectFactory)
        {
            @Override
            Set<String> getIssueTypeIdsForScheme(final FieldConfigScheme fieldConfigScheme)
            {
                called.set(true);
                return Collections.singleton("100");
            }
        };

        assertFalse(util.fieldConfigSchemeContainsContextIssueTypes(queryContext, configScheme));
        assertTrue(called.get());

        mockController.verify();
    }

    @Test
    public void testFieldConfigSchemeContainsContextIssueTypesUsingSpecificIssueTypesContextDoesntMatch() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("100"));
        final ProjectIssueTypeContext projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("200"));
        final QueryContextImpl queryContext = new QueryContextImpl(new ClauseContextImpl(
                CollectionBuilder.newBuilder(projectIssueTypeContext1, projectIssueTypeContext2).asSet()));

        final FieldConfigScheme configScheme = mockController.getMock(FieldConfigScheme.class);

        final AtomicBoolean called = new AtomicBoolean(false);

        mockController.replay();

        final FieldConfigSchemeClauseContextUtil util = new FieldConfigSchemeClauseContextUtil(issueTypeSchemeManager, constantsManager, permissionManager, projectFactory)
        {
            @Override
            Set<String> getIssueTypeIdsForScheme(final FieldConfigScheme fieldConfigScheme)
            {
                called.set(true);
                return CollectionBuilder.newBuilder("300", "400").asSet();
            }
        };

        assertFalse(util.fieldConfigSchemeContainsContextIssueTypes(queryContext, configScheme));
        assertTrue(called.get());

        mockController.verify();
    }

    @Test
    public void testFieldConfigSchemeContainsContextIssueTypesUsingSpecificIssueTypesContextMatches() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("100"));
        final ProjectIssueTypeContext projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("200"));
        final QueryContextImpl queryContext = new QueryContextImpl(new ClauseContextImpl(
                CollectionBuilder.newBuilder(projectIssueTypeContext1, projectIssueTypeContext2).asSet()));

        final FieldConfigScheme configScheme = mockController.getMock(FieldConfigScheme.class);

        final AtomicBoolean called = new AtomicBoolean(false);

        mockController.replay();

        final FieldConfigSchemeClauseContextUtil util = new FieldConfigSchemeClauseContextUtil(issueTypeSchemeManager, constantsManager, permissionManager, projectFactory)
        {
            @Override
            Set<String> getIssueTypeIdsForScheme(final FieldConfigScheme fieldConfigScheme)
            {
                called.set(true);
                return CollectionBuilder.newBuilder("200", "400").asSet();
            }
        };

        assertTrue(util.fieldConfigSchemeContainsContextIssueTypes(queryContext, configScheme));
        assertTrue(called.get());

        mockController.verify();
    }

    @Test
    public void testFieldConfigSchemeContainsContextMappingProjectMatchButNoIssueTypeMatch() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("100"));
        final ProjectIssueTypeContext projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("200"));
        final ProjectIssueTypeContext projectIssueTypeContext3 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(20L), new IssueTypeContextImpl("100"));
        final QueryContextImpl queryContext = new QueryContextImpl(new ClauseContextImpl(
                CollectionBuilder.newBuilder(projectIssueTypeContext1, projectIssueTypeContext2, projectIssueTypeContext3).asSet()));

        final FieldConfigScheme configScheme = mockController.getMock(FieldConfigScheme.class);


        final AtomicBoolean getProjsCalled = new AtomicBoolean(false);
        final AtomicBoolean getTypesCalled = new AtomicBoolean(false);
        mockController.replay();
        final FieldConfigSchemeClauseContextUtil util = new FieldConfigSchemeClauseContextUtil(issueTypeSchemeManager, constantsManager, permissionManager, projectFactory)
        {
            @Override
            Set<Long> getProjectIdsForScheme(final FieldConfigScheme fieldConfigScheme)
            {
                getProjsCalled.set(true);
                return CollectionBuilder.newBuilder(10L, 20L).asSet();
            }

            @Override
            Set<String> getIssueTypeIdsForScheme(final FieldConfigScheme fieldConfigScheme)
            {
                getTypesCalled.set(true);
                return CollectionBuilder.newBuilder("300", "400").asSet();
            }
        };

        assertFalse(util.fieldConfigSchemeContainsContextMapping(queryContext, configScheme));
        assertTrue(getProjsCalled.get());
        assertTrue(getTypesCalled.get());
        mockController.verify();
    }

    @Test
    public void testFieldConfigSchemeContainsContextMappingMatch() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("100"));
        final ProjectIssueTypeContext projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("200"));
        final ProjectIssueTypeContext projectIssueTypeContext3 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(20L), new IssueTypeContextImpl("100"));
        final QueryContextImpl queryContext = new QueryContextImpl(new ClauseContextImpl(
                CollectionBuilder.newBuilder(projectIssueTypeContext1, projectIssueTypeContext2, projectIssueTypeContext3).asSet()));

        final FieldConfigScheme configScheme = mockController.getMock(FieldConfigScheme.class);


        final AtomicBoolean getProjsCalled = new AtomicBoolean(false);
        final AtomicBoolean getTypesCalled = new AtomicBoolean(false);
        mockController.replay();
        final FieldConfigSchemeClauseContextUtil util = new FieldConfigSchemeClauseContextUtil(issueTypeSchemeManager, constantsManager, permissionManager, projectFactory)
        {
            @Override
            Set<Long> getProjectIdsForScheme(final FieldConfigScheme fieldConfigScheme)
            {
                getProjsCalled.set(true);
                return CollectionBuilder.newBuilder(10L, 30L).asSet();
            }

            @Override
            Set<String> getIssueTypeIdsForScheme(final FieldConfigScheme fieldConfigScheme)
            {
                getTypesCalled.set(true);
                return CollectionBuilder.newBuilder("200", "400").asSet();
            }
        };

        assertTrue(util.fieldConfigSchemeContainsContextMapping(queryContext, configScheme));
        assertTrue(getProjsCalled.get());
        assertTrue(getTypesCalled.get());
        mockController.verify();
    }

    @Test
    public void testFieldConfigSchemeContainsContextMappingNoProjectMatch() throws Exception
    {
        final ProjectIssueTypeContext projectIssueTypeContext1 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("100"));
        final ProjectIssueTypeContext projectIssueTypeContext2 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("200"));
        final ProjectIssueTypeContext projectIssueTypeContext3 = new ProjectIssueTypeContextImpl(new ProjectContextImpl(20L), new IssueTypeContextImpl("100"));
        final QueryContextImpl queryContext = new QueryContextImpl(new ClauseContextImpl(
                CollectionBuilder.newBuilder(projectIssueTypeContext1, projectIssueTypeContext2, projectIssueTypeContext3).asSet()));

        final FieldConfigScheme configScheme = mockController.getMock(FieldConfigScheme.class);


        final AtomicBoolean getProjsCalled = new AtomicBoolean(false);
        final AtomicBoolean getTypesCalled = new AtomicBoolean(false);
        mockController.replay();
        final FieldConfigSchemeClauseContextUtil util = new FieldConfigSchemeClauseContextUtil(issueTypeSchemeManager, constantsManager, permissionManager, projectFactory)
        {
            @Override
            Set<Long> getProjectIdsForScheme(final FieldConfigScheme fieldConfigScheme)
            {
                getProjsCalled.set(true);
                return CollectionBuilder.newBuilder(30L, 40L).asSet();
            }

            @Override
            Set<String> getIssueTypeIdsForScheme(final FieldConfigScheme fieldConfigScheme)
            {
                getTypesCalled.set(true);
                return CollectionBuilder.newBuilder("200", "400").asSet();
            }
        };

        assertFalse(util.fieldConfigSchemeContainsContextMapping(queryContext, configScheme));
        assertTrue(getProjsCalled.get());
        assertTrue(getTypesCalled.get());
        mockController.verify();
    }

    @Test
    public void testIsConfigSchemeVisibleUnderContextGlobal() throws Exception
    {
        final FieldConfigScheme configScheme = mockController.getMock(FieldConfigScheme.class);
        configScheme.isGlobal();
        mockController.setReturnValue(true);

        final QueryContextImpl context = new QueryContextImpl(new ClauseContextImpl());
        mockController.replay();
        final FieldConfigSchemeClauseContextUtil util = new FieldConfigSchemeClauseContextUtil(issueTypeSchemeManager, constantsManager, permissionManager, projectFactory);
        assertTrue(util.isConfigSchemeVisibleUnderContext(context, configScheme));
        mockController.verify();
    }

    @Test
    public void testIsConfigSchemeVisibleUnderContextAllIssueTypes() throws Exception
    {
        final FieldConfigScheme configScheme = mockController.getMock(FieldConfigScheme.class);

        configScheme.isGlobal();
        mockController.setReturnValue(false);

        configScheme.isAllIssueTypes();
        mockController.setReturnValue(true);

        final QueryContextImpl context = new QueryContextImpl(new ClauseContextImpl());

        mockController.replay();
        final AtomicBoolean projsCalled = new AtomicBoolean(false);
        final FieldConfigSchemeClauseContextUtil util = new FieldConfigSchemeClauseContextUtil(issueTypeSchemeManager, constantsManager, permissionManager, projectFactory)
        {
            @Override
            boolean fieldConfigSchemeContainsContextProjects(final QueryContext queryContext, final FieldConfigScheme fieldConfigScheme)
            {
                projsCalled.set(true);
                return true;
            }
        };

        assertTrue(util.isConfigSchemeVisibleUnderContext(context, configScheme));
        assertTrue(projsCalled.get());
        mockController.verify();
    }

    @Test
    public void testIsConfigSchemeVisibleUnderContextAllProjects() throws Exception
    {
        final FieldConfigScheme configScheme = mockController.getMock(FieldConfigScheme.class);

        configScheme.isGlobal();
        mockController.setReturnValue(false);

        configScheme.isAllIssueTypes();
        mockController.setReturnValue(false);

        configScheme.isAllProjects();
        mockController.setReturnValue(true);

        final QueryContextImpl context = new QueryContextImpl(new ClauseContextImpl());

        mockController.replay();
        final AtomicBoolean projsCalled = new AtomicBoolean(false);
        final AtomicBoolean typesCalled = new AtomicBoolean(false);
        final FieldConfigSchemeClauseContextUtil util = new FieldConfigSchemeClauseContextUtil(issueTypeSchemeManager, constantsManager, permissionManager, projectFactory)
        {
            @Override
            boolean fieldConfigSchemeContainsContextProjects(final QueryContext queryContext, final FieldConfigScheme fieldConfigScheme)
            {
                projsCalled.set(true);
                return true;
            }

            @Override
            boolean fieldConfigSchemeContainsContextIssueTypes(final QueryContext queryContext, final FieldConfigScheme fieldConfigScheme)
            {
                typesCalled.set(true);
                return true;
            }
        };

        assertTrue(util.isConfigSchemeVisibleUnderContext(context, configScheme));
        assertFalse(projsCalled.get());
        assertTrue(typesCalled.get());
        mockController.verify();
    }

    @Test
    public void testIsConfigSchemeVisibleUnderContextNotAll() throws Exception
    {
        final FieldConfigScheme configScheme = mockController.getMock(FieldConfigScheme.class);

        configScheme.isGlobal();
        mockController.setReturnValue(false);

        configScheme.isAllIssueTypes();
        mockController.setReturnValue(false);

        configScheme.isAllProjects();
        mockController.setReturnValue(false);

        final QueryContextImpl context = new QueryContextImpl(new ClauseContextImpl());

        mockController.replay();
        final AtomicBoolean projsCalled = new AtomicBoolean(false);
        final AtomicBoolean typesCalled = new AtomicBoolean(false);
        final AtomicBoolean mappingCalled = new AtomicBoolean(false);
        final FieldConfigSchemeClauseContextUtil util = new FieldConfigSchemeClauseContextUtil(issueTypeSchemeManager, constantsManager, permissionManager, projectFactory)
        {
            @Override
            boolean fieldConfigSchemeContainsContextProjects(final QueryContext queryContext, final FieldConfigScheme fieldConfigScheme)
            {
                projsCalled.set(true);
                return true;
            }

            @Override
            boolean fieldConfigSchemeContainsContextIssueTypes(final QueryContext queryContext, final FieldConfigScheme fieldConfigScheme)
            {
                typesCalled.set(true);
                return true;
            }

            @Override
            boolean fieldConfigSchemeContainsContextMapping(final QueryContext queryContext, final FieldConfigScheme fieldConfigScheme)
            {
                mappingCalled.set(true);
                return true;
            }
        };

        assertTrue(util.isConfigSchemeVisibleUnderContext(context, configScheme));
        assertFalse(projsCalled.get());
        assertFalse(typesCalled.get());
        assertTrue(mappingCalled.get());
        mockController.verify();
    }

    @Test
    public void testAddProjectIssueTypeContextsForIssueTypesOnlyNullIssueTypes() throws Exception
    {
        final FieldConfigScheme fieldConfigScheme = createMock(FieldConfigScheme.class);
        expect(fieldConfigScheme.getAssociatedIssueTypes()).andReturn(null);

        replay(fieldConfigScheme);

        final FieldConfigSchemeClauseContextUtil util = new FieldConfigSchemeClauseContextUtil(issueTypeSchemeManager, constantsManager, permissionManager, projectFactory);
        final Set<ProjectIssueTypeContext> contextSet = util.addProjectIssueTypeContextsForIssueTypesOnly(fieldConfigScheme);
        assertTrue(contextSet.isEmpty());

        verify(fieldConfigScheme);
    }
}
