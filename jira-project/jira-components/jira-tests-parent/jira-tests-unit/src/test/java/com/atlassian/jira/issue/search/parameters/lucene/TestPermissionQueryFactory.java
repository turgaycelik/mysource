package com.atlassian.jira.issue.search.parameters.lucene;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelImpl;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelPermission;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.SecurityTypeManager;
import com.atlassian.jira.security.type.SecurityType;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.collect.MapBuilder;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
public class TestPermissionQueryFactory
{
    private IssueSecurityLevelManager issueSecurityLevelManager;
    private PermissionManager permissionManager;
    private PermissionSchemeManager permissionSchemeManager;
    private PermissionTypeManager permissionTypeManager;
    private IssueSecuritySchemeManager issueSecuritySchemeManager;
    private SecurityTypeManager issueSecurityTypeManager;
    private ProjectFactory projectFactory;
    private User theUser;
    private ApplicationUser theAppUser;

    @Before
    public void setUp() throws Exception
    {
        issueSecurityLevelManager = mock(IssueSecurityLevelManager.class);
        permissionManager = mock(PermissionManager.class);
        permissionSchemeManager = mock(PermissionSchemeManager.class);
        permissionTypeManager = mock(PermissionTypeManager.class);
        issueSecuritySchemeManager = mock(IssueSecuritySchemeManager.class);
        issueSecurityTypeManager = mock(SecurityTypeManager.class);
        projectFactory = mock(ProjectFactory.class);
        theAppUser = new MockApplicationUser("fred");
        theUser = theAppUser.getDirectoryUser();
    }

    @After
    public void tearDown()
    {
        issueSecurityLevelManager = null;
        permissionManager = null;
        permissionSchemeManager = null;
        permissionTypeManager = null;
        issueSecuritySchemeManager = null;
        issueSecurityTypeManager = null;
        projectFactory = null;
        theUser = null;
    }

    @Test
    public void testUserHasPermissionForProjectAndSecurityTypeNullUser() throws Exception
    {
        final MockProject project = new MockProject();
        final SecurityType securityType = mock(SecurityType.class);

        when(securityType.hasPermission(project, "test")).thenReturn(true);

        final DefaultPermissionQueryFactory generator = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager,
            permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager, projectFactory);

        final boolean result = generator.userHasPermissionForProjectAndSecurityType(null, project, "test", securityType);

        assertTrue(result);
    }

    @Test
    public void testUserHasPermissionForProjectAndSecurityTypeUser() throws Exception
    {
        final Project project = new MockProject();
        final SecurityType securityType = mock(SecurityType.class);

        when(securityType.hasPermission(project, "test", theUser, false)).thenReturn(true);

        final DefaultPermissionQueryFactory generator = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager,
                permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager, projectFactory);
        final boolean result = generator.userHasPermissionForProjectAndSecurityType(theAppUser, project, "test", securityType);
        assertTrue(result);
    }

    @Test
    public void testCollectProjectTermsUserHasNoPermission() throws Exception
    {
        final Project mockProject = new MockProject();
        final Set<Query> collect = new LinkedHashSet<Query>();
        final GenericValue schemeGV = new MockGenericValue("PermissionScheme");
        final GenericValue entityGV = createEntityGV("type", "parameter");
        final SecurityType securityType = mock(SecurityType.class);

        when(permissionSchemeManager.getSchemes(mockProject.getGenericValue())).thenReturn(Collections.singletonList(schemeGV));

        when(permissionSchemeManager.getEntities(schemeGV, (long) Permissions.BROWSE)).thenReturn(
                Collections.singletonList(entityGV));

        when(permissionTypeManager.getSecurityType("type")).thenReturn(securityType);

        final DefaultPermissionQueryFactory generator = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager,
            permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager, projectFactory)
        {
            @Override
            boolean userHasPermissionForProjectAndSecurityType(final ApplicationUser searcher, final Project project, final String parameter, final SecurityType s)
            {
                assertSame(theAppUser, searcher);
                assertSame(mockProject, project);
                assertSame(securityType, s);
                return false;
            }
        };

        generator.collectProjectTerms(mockProject, theAppUser, collect, Permissions.BROWSE);

        assertEquals(0, collect.size());
    }

    @Test
    public void testCollectProjectTermsNoQueryGenerated() throws Exception
    {
        final Project mockProject = new MockProject();
        final Set<Query> collect = new LinkedHashSet<Query>();
        final GenericValue schemeGV = new MockGenericValue("PermissionScheme");
        final GenericValue entityGV = createEntityGV("type", "parameter");
        final SecurityType securityType = mock(SecurityType.class);

        when(permissionSchemeManager.getSchemes(mockProject.getGenericValue())).thenReturn(Collections.singletonList(schemeGV));

        when(permissionSchemeManager.getEntities(schemeGV, (long) Permissions.BROWSE)).thenReturn(
                Collections.singletonList(entityGV));

        when(permissionTypeManager.getSecurityType("type")).thenReturn(securityType);

        when(securityType.getQuery(theUser, mockProject, "parameter")).thenReturn(null);

        final DefaultPermissionQueryFactory generator = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager,
            permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager, projectFactory)
        {
            @Override
            boolean userHasPermissionForProjectAndSecurityType(final ApplicationUser searcher, final Project project, final String parameter, final SecurityType s)
            {
                assertSame(theAppUser, searcher);
                assertSame(mockProject, project);
                assertSame(securityType, s);
                return true;
            }
        };

        generator.collectProjectTerms(mockProject, theAppUser, collect, Permissions.BROWSE);

        assertEquals(0, collect.size());
    }

    @Test
    public void testCollectProjectTermsHappyPath() throws Exception
    {
        final Project mockProject = new MockProject();
        final Set<Query> collect = new LinkedHashSet<Query>();
        final GenericValue schemeGV = new MockGenericValue("PermissionScheme");
        final GenericValue entityGV = createEntityGV("type", "parameter");
        final SecurityType securityType = mock(SecurityType.class);
        final Query projectQuery = new TermQuery(new Term("project", "123"));

        when(permissionSchemeManager.getSchemes(mockProject.getGenericValue())).thenReturn(Collections.singletonList(schemeGV));

        when(permissionSchemeManager.getEntities(schemeGV, (long) Permissions.BROWSE)).thenReturn(
                Collections.singletonList(entityGV));

        when(permissionTypeManager.getSecurityType("type")).thenReturn(securityType);

        when(securityType.getQuery(theUser, mockProject, "parameter")).thenReturn(projectQuery);

        final DefaultPermissionQueryFactory generator = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager,
            permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager, projectFactory)
        {
            @Override
            boolean userHasPermissionForProjectAndSecurityType(final ApplicationUser searcher, final Project project, final String parameter, final SecurityType s)
            {
                assertSame(theAppUser, searcher);
                assertSame(mockProject, project);
                assertSame(securityType, s);
                return true;
            }
        };

        generator.collectProjectTerms(mockProject, theAppUser, collect, Permissions.BROWSE);

        assertEquals(1, collect.size());
        assertTrue(collect.contains(projectQuery));
    }

    @Test
    public void testCollectProjectTermsNullSecurityType() throws Exception
    {
        final Project mockProject = new MockProject();
        final Set<Query> collect = new LinkedHashSet<Query>();
        final GenericValue schemeGV = new MockGenericValue("PermissionScheme");
        final GenericValue entityGV = createEntityGV("type", "parameter");
        final Query projectQuery = new TermQuery(new Term("project", "123"));

        when(permissionSchemeManager.getSchemes(mockProject.getGenericValue())).thenReturn(Collections.singletonList(schemeGV));

        when(permissionSchemeManager.getEntities(schemeGV, (long) Permissions.BROWSE)).thenReturn(Collections.singletonList(entityGV));

        when(permissionTypeManager.getSecurityType("type")).thenReturn(null);

        final DefaultPermissionQueryFactory generator = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager, permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager, projectFactory)
        {
            @Override
            boolean userHasPermissionForProjectAndSecurityType(final ApplicationUser searcher, final Project project, final String parameter, final SecurityType s)
            {
                assertSame(theAppUser, searcher);
                assertSame(mockProject, project);
                return true;
            }
        };

        generator.collectProjectTerms(mockProject, theAppUser, collect, Permissions.BROWSE);

        assertEquals(0, collect.size());
    }

    @Test
    public void testCollectSecurityLevelTermsUserHasNoPermission() throws Exception
    {
        final Project mockProject = new MockProject();
        final Set<Query> collect = new LinkedHashSet<Query>();
        final IssueSecurityLevelPermission securityLevelPermission = createIssueSecurityLevelPermission("type", "parameter");
        final SecurityType securityType = mock(SecurityType.class);

        when(issueSecurityLevelManager.getUsersSecurityLevels(mockProject, theUser)).thenReturn(
                Collections.singletonList(createSecurityLevel(123L)));

        when(issueSecuritySchemeManager.getPermissionsBySecurityLevel(123L)).thenReturn(Collections.singletonList(securityLevelPermission));

        when(issueSecurityTypeManager.getSecurityType("type")).thenReturn(securityType);

        final DefaultPermissionQueryFactory generator = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager,
            permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager, projectFactory)
        {
            @Override
            boolean userHasPermissionForProjectAndSecurityType(final ApplicationUser searcher, final Project project, final String parameter, final SecurityType s)
            {
                assertSame(theAppUser, searcher);
                assertSame(mockProject, project);
                assertSame(securityType, s);
                return false;
            }
        };

        generator.collectSecurityLevelTerms(mockProject, theAppUser, collect);
        assertEquals(0, collect.size());
    }

    @Test
    public void testCollectSecurityLevelTermsNoQueryGenerated() throws Exception
    {
        final Project projectObject = new MockProject(555L);
        final Set<Query> collect = new LinkedHashSet<Query>();
        final IssueSecurityLevel securityLevel = createSecurityLevel(123L);
        final IssueSecurityLevelPermission securityLevelPermission = createIssueSecurityLevelPermission("type", "parameter");
        final SecurityType securityType = mock(SecurityType.class);

        when(issueSecurityLevelManager.getUsersSecurityLevels(projectObject, theUser)).thenReturn(
                Collections.singletonList(securityLevel));

        when(issueSecuritySchemeManager.getPermissionsBySecurityLevel(123L)).thenReturn(Collections.singletonList(securityLevelPermission));

        when(issueSecurityTypeManager.getSecurityType("type")).thenReturn(securityType);

        when(securityType.getQuery(theUser, projectObject, securityLevel, "parameter")).thenReturn(null);

        final DefaultPermissionQueryFactory generator = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager,
            permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager, projectFactory)
        {
            @Override
            boolean userHasPermissionForProjectAndSecurityType(final ApplicationUser searcher, final Project project, String parameter, final SecurityType s)
            {
                assertSame(theAppUser, searcher);
                assertSame(projectObject, project);
                assertSame(securityType, s);
                return true;
            }
        };

        generator.collectSecurityLevelTerms(projectObject, theAppUser, collect);
        assertEquals(0, collect.size());
    }

    @Test
    public void testCollectSecurityLevelTermsHappyPath() throws Exception
    {
        final Project projectObject = new MockProject(555L);
        final Set<Query> collect = new LinkedHashSet<Query>();
        final IssueSecurityLevel securityLevel = createSecurityLevel(123L);
        final IssueSecurityLevelPermission securityLevelPermission = createIssueSecurityLevelPermission("type", "parameter");
        final SecurityType securityType = mock(SecurityType.class);
        final Query securityLevelQuery = new TermQuery(new Term("issue_security_level", "123"));

        when(issueSecurityLevelManager.getUsersSecurityLevels(projectObject, theUser)).thenReturn(
                Collections.singletonList(securityLevel));
        when(issueSecuritySchemeManager.getPermissionsBySecurityLevel(123L)).thenReturn(Collections.singletonList(securityLevelPermission));
        when(issueSecurityTypeManager.getSecurityType("type")).thenReturn(securityType);
        when(securityType.getQuery(theUser, projectObject, securityLevel, "parameter")).thenReturn(securityLevelQuery);

        final DefaultPermissionQueryFactory generator = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager,
            permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager, projectFactory)
        {
            @Override
            boolean userHasPermissionForProjectAndSecurityType(final ApplicationUser searcher, final Project project, final String parameter, final SecurityType s)
            {
                assertSame(theAppUser, searcher);
                assertSame(projectObject, project);
                assertSame(securityType, s);
                return true;
            }
        };

        generator.collectSecurityLevelTerms(projectObject, theAppUser, collect);
        assertEquals(1, collect.size());
        assertTrue(collect.contains(securityLevelQuery));
    }

    @Test
    public void testCollectSecurityLevelTermsNullSecurityType() throws Exception
    {
        final Project projectObject = new MockProject(555L);
        final Set<Query> collect = new LinkedHashSet<Query>();
        final IssueSecurityLevel securityLevel = createSecurityLevel(123L);
        final IssueSecurityLevelPermission securityLevelPermission = createIssueSecurityLevelPermission("type", "parameter");
        final Query securityLevelQuery = new TermQuery(new Term("issue_security_level", "123"));

        when(issueSecurityLevelManager.getUsersSecurityLevels(projectObject, theUser)).thenReturn(Collections.singletonList(securityLevel));
        when(issueSecuritySchemeManager.getPermissionsBySecurityLevel(123L)).thenReturn(Collections.singletonList(securityLevelPermission));
        when(issueSecurityTypeManager.getSecurityType("type")).thenReturn(null);

        final DefaultPermissionQueryFactory generator = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager, permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager, projectFactory)
        {
            @Override
            boolean userHasPermissionForProjectAndSecurityType(final ApplicationUser searcher, final Project project, final String parameter, final SecurityType s)
            {
                assertSame(theAppUser, searcher);
                assertSame(projectObject, project);
                return true;
            }
        };

        generator.collectSecurityLevelTerms(projectObject, theAppUser, collect);
        // Ignore nulls
        assertEquals(0, collect.size());
    }

    @Test
    public void testGenerateQueryNoProjects() throws Exception
    {
        when(permissionManager.getProjectObjects(Permissions.BROWSE, theUser)).thenReturn(Collections.<Project>emptyList());

        final DefaultPermissionQueryFactory generator = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager,
            permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager, projectFactory)
        {
            @Override
            void collectProjectTerms(final Project p, final ApplicationUser searcher, final Set<Query> queries, final int permissionId) throws GenericEntityException
            {
                fail("Should not have been called");
            }

            @Override
            void collectSecurityLevelTerms(final Project p, final ApplicationUser searcher, final Set<Query> queries) throws GenericEntityException
            {
                fail("Should not have been called");
            }
        };

        final Query result = generator.getQuery(theAppUser, Permissions.BROWSE);
        final BooleanQuery expected = new BooleanQuery();
        assertEquals(expected, result);
    }

    @Test
    public void testGenerateQueryNoProjectQueries() throws Exception
    {
        final Project mockProject = new MockProject();

        when(permissionManager.getProjectObjects(Permissions.BROWSE, theUser)).thenReturn(Collections.singletonList(mockProject));

        final DefaultPermissionQueryFactory generator = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager,
            permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager, projectFactory)
        {
            @Override
            void collectProjectTerms(final Project p, final ApplicationUser searcher, final Set<Query> queries, final int permissionId) throws GenericEntityException
            {
                assertSame(mockProject, p);
                assertSame(theUser, searcher);

                // dont add anything
            }

            @Override
            void collectSecurityLevelTerms(final Project p, final ApplicationUser searcher, final Set<Query> queries) throws GenericEntityException
            {
                fail("Should not be called if no project queries");
            }
        };

        final Query result = generator.getQuery(theAppUser, Permissions.BROWSE);
        final BooleanQuery expected = new BooleanQuery();
        assertEquals(expected, result);
    }

    @Test
    public void testGenerateQueryHappyPath() throws Exception
    {
        final Project mockProject = new MockProject();
        final Query projectQuery = new TermQuery(new Term("project", "123"));
        final Query securityLevelQuery = new TermQuery(new Term(SystemSearchConstants.forSecurityLevel().getIndexField(), "123"));
        final Query noSecurityLevelQuery = new TermQuery(new Term(SystemSearchConstants.forSecurityLevel().getIndexField(), "-1"));

        when(permissionManager.getProjects(Permissions.BROWSE, theAppUser)).thenReturn(Collections.singletonList(mockProject));

        final DefaultPermissionQueryFactory generator = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager,
            permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager, projectFactory)
        {
            @Override
            void collectProjectTerms(final Project p, final ApplicationUser searcher, final Set<Query> queries, final int permissionId) throws GenericEntityException
            {
                assertSame(mockProject, p);
                assertSame(theAppUser, searcher);
                queries.add(projectQuery);
            }

            @Override
            void collectSecurityLevelTerms(final Project p, final ApplicationUser searcher, final Set<Query> queries) throws GenericEntityException
            {
                assertSame(mockProject, p);
                assertSame(theAppUser, searcher);
                queries.add(securityLevelQuery);
            }
        };

        final Query result = generator.getQuery(theAppUser, Permissions.BROWSE);
        final BooleanQuery expected = new BooleanQuery();
        final BooleanQuery expectedProjectQuery = new BooleanQuery();
        expectedProjectQuery.add(projectQuery, BooleanClause.Occur.SHOULD);
        final BooleanQuery expectedSecurityLevelQuery = new BooleanQuery();
        expectedSecurityLevelQuery.add(noSecurityLevelQuery, BooleanClause.Occur.SHOULD);
        expectedSecurityLevelQuery.add(securityLevelQuery, BooleanClause.Occur.SHOULD);
        expected.add(expectedProjectQuery, BooleanClause.Occur.MUST);
        expected.add(expectedSecurityLevelQuery, BooleanClause.Occur.MUST);

        assertEquals(expected, result);
    }

    @Test
    public void testGenerateQueryNoSecurityLevelQueries() throws Exception
    {
        final Project mockProject = new MockProject();
        final Query projectQuery = new TermQuery(new Term("project", "123"));
        final Query noSecurityLevelQuery = new TermQuery(new Term(SystemSearchConstants.forSecurityLevel().getIndexField(), "-1"));

        when(permissionManager.getProjects(Permissions.BROWSE, theAppUser)).thenReturn(Collections.singletonList(mockProject));

        final DefaultPermissionQueryFactory generator = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager,
            permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager, projectFactory)
        {
            @Override
            void collectProjectTerms(final Project p, final ApplicationUser searcher, final Set<Query> queries, final int permissionId) throws GenericEntityException
            {
                assertSame(mockProject, p);
                assertSame(theAppUser, searcher);
                queries.add(projectQuery);
            }

            @Override
            void collectSecurityLevelTerms(final Project p, final ApplicationUser searcher, final Set<Query> queries) throws GenericEntityException
            {
                assertSame(mockProject, p);
                assertSame(theAppUser, searcher);
                // don't add anything
            }
        };

        final Query result = generator.getQuery(theAppUser, Permissions.BROWSE);

        final BooleanQuery expected = new BooleanQuery();
        final BooleanQuery expectedProjectQuery = new BooleanQuery();
        expectedProjectQuery.add(projectQuery, BooleanClause.Occur.SHOULD);
        final BooleanQuery expectedSecurityLevelQuery = new BooleanQuery();
        expectedSecurityLevelQuery.add(noSecurityLevelQuery, BooleanClause.Occur.SHOULD);
        expected.add(expectedProjectQuery, BooleanClause.Occur.MUST);
        expected.add(expectedSecurityLevelQuery, BooleanClause.Occur.MUST);

        assertEquals(expected, result);
    }

    private GenericValue createEntityGV(final String type, final String parameter)
    {
        return new MockGenericValue("Entity", MapBuilder.newBuilder().add("type", type).add("parameter", parameter).toMap());
    }

    private IssueSecurityLevelPermission createIssueSecurityLevelPermission(final String type, final String parameter)
    {
        return new IssueSecurityLevelPermission(1L, 1L, 1L, type, parameter);
    }

    private IssueSecurityLevel createSecurityLevel(final Long id)
    {
        return new IssueSecurityLevelImpl(id, "sec" + id, "", 10183L);
    }
}
