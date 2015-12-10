package com.atlassian.jira.jql.query;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.search.parameters.lucene.CachedWrappedFilterCache;
import com.atlassian.jira.issue.search.parameters.lucene.PermissionsFilterGenerator;
import com.atlassian.jira.issue.search.util.LuceneQueryModifier;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TermQuery;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import static com.atlassian.query.operator.Operator.GREATER_THAN;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.apache.lucene.search.BooleanClause.Occur.SHOULD;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
public class TestCommentClauseQueryFactory
{
    private static final User ANONYMOUS = null;
    private static final String GROUP_1 = "group1";
    private static final String GROUP_2 = "group2";
    private static final String GROUP_3 = "group3";
    private static final ProjectRoleImpl PROJECT_ROLE_1 = new ProjectRoleImpl(11L, "one", "o");
    private static final ProjectRoleImpl PROJECT_ROLE_2 = new ProjectRoleImpl(22L, "two", "tt");
    private static final ProjectRoleImpl PROJECT_ROLE_3 = new ProjectRoleImpl(33L, "three", "ttt");

    @Rule
    public RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);

    @Mock PermissionManager permissionManager;
    @Mock ProjectRoleManager projectRoleManager;
    @Mock SearchProviderFactory searchProviderFactory;
    @Mock CachedWrappedFilterCache cachedWrappedFilterCache;
    @Mock PermissionsFilterGenerator permissionsFilterGenerator;

    JqlOperandResolver operandResolver = MockJqlOperandResolver.createSimpleSupport();
    QueryCreationContext queryCreationContext;

    @Mock
    @AvailableInContainer
    private ApplicationProperties applicationProperties;

    CommentClauseQueryFactory commentClauseQueryFactory;

    // don't really care about testing the modification - just use a dummy implementation
    LuceneQueryModifier luceneQueryModifier = new LuceneQueryModifier()
    {
        public Query getModifiedQuery(final Query originalQuery)
        {
            return originalQuery;
        }
    };

    @Before
    public void setUp() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl(ANONYMOUS);
        commentClauseQueryFactory = new CommentClauseQueryFactory(null, null, operandResolver, null, luceneQueryModifier, permissionsFilterGenerator)
        {
            @Override
            CachedWrappedFilterCache getCachedWrappedFilterCache()
            {
                return cachedWrappedFilterCache;
            }
        };
    }

    @After
    public void tearDown() throws Exception
    {
        permissionManager = null;
        projectRoleManager = null;
        searchProviderFactory = null;
        cachedWrappedFilterCache = null;
        permissionsFilterGenerator = null;
        operandResolver = null;
        queryCreationContext = null;
        commentClauseQueryFactory = null;
    }

    @Test
    public void testValidateClauseOperators() throws Exception
    {
        assertOperatorValidity(false, Operator.GREATER_THAN);
        assertOperatorValidity(false, Operator.GREATER_THAN_EQUALS);
        assertOperatorValidity(false, Operator.LESS_THAN);
        assertOperatorValidity(false, Operator.LESS_THAN_EQUALS);
        assertOperatorValidity(false, Operator.EQUALS);
        assertOperatorValidity(false, Operator.NOT_EQUALS);
        assertOperatorValidity(false, Operator.IN);
    }

    @Test
    public void testValidateClauseHappyPath() throws Exception
    {
        assertOperatorValidity(true, Operator.LIKE);
        assertOperatorValidity(true, Operator.NOT_LIKE);
    }

    @Test
    public void testCreateProjectVisibilityQueryEmptyProjectIds() throws Exception
    {
        assertNull(commentClauseQueryFactory.createProjectVisibilityQuery(Collections.<Long>emptyList()));
    }

    @Test
    public void testCreateProjectVisibilityQueryHappyPath() throws Exception
    {
        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new TermQuery(new Term(DocumentConstants.PROJECT_ID, "23")), SHOULD);
        expectedQuery.add(new TermQuery(new Term(DocumentConstants.PROJECT_ID, "33")), SHOULD);
        expectedQuery.add(new TermQuery(new Term(DocumentConstants.PROJECT_ID, "34")), SHOULD);

        final BooleanQuery actualQuery = commentClauseQueryFactory.createProjectVisibilityQuery(newArrayList(23L, 33L, 34L));
        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testCreateLevelRestrictionQueryForCommentsGroupRestrictions() throws Exception
    {
        final List<Long> projectIds = newArrayList(1L, 2L, 3L);

        final User mockUser = new MockUser("testDude");

        when(projectRoleManager.createProjectIdToProjectRolesMap(mockUser, projectIds))
                .thenReturn(new ProjectRoleManager.ProjectIdToProjectRoleIdsMap());

        commentClauseQueryFactory = new CommentClauseQueryFactory(null, projectRoleManager, operandResolver, null, luceneQueryModifier, null)
        {
            @Override
            Set<String> getGroups(final User searcher)
            {
                return ImmutableSet.of("group1", "group2", "group3");
            }
        };

        final BooleanQuery levelQuery = commentClauseQueryFactory.createLevelRestrictionQueryForComments(projectIds, mockUser);
        BooleanQuery expectedQuery = new BooleanQuery();
        Query noGroupOrProjectQuery = commentClauseQueryFactory.createNoGroupOrProjectRoleLevelQuery();
        expectedQuery.add(noGroupOrProjectQuery, SHOULD);
        BooleanQuery groupQuery = new BooleanQuery();
        groupQuery.add(new TermQuery(new Term(DocumentConstants.COMMENT_LEVEL, "group1")), SHOULD);
        groupQuery.add(new TermQuery(new Term(DocumentConstants.COMMENT_LEVEL, "group2")), SHOULD);
        groupQuery.add(new TermQuery(new Term(DocumentConstants.COMMENT_LEVEL, "group3")), SHOULD);
        expectedQuery.add(groupQuery, SHOULD);
        assertEquals(expectedQuery, levelQuery);
    }

    @Test
    public void testCreateLevelRestrictionQueryForCommentsRoleRestrictions() throws Exception
    {
        List<Long> projectIds = newArrayList(1L, 2L, 3L);

        final User mockUser = new MockUser("testDude");
        final ProjectRoleManager.ProjectIdToProjectRoleIdsMap idsMap = new ProjectRoleManager.ProjectIdToProjectRoleIdsMap();
        idsMap.add(1L, 123L);
        idsMap.add(2L, 345L);
        when(projectRoleManager.createProjectIdToProjectRolesMap(mockUser, projectIds))
                .thenReturn(idsMap);

        final AtomicBoolean createRoleQueryCalled = new AtomicBoolean(false);
        commentClauseQueryFactory = new CommentClauseQueryFactory(null, projectRoleManager, operandResolver, null, luceneQueryModifier, null)
        {
            @Override
            Set<String> getGroups(final User searcher)
            {
                return Collections.emptySet();
            }

            @Override
            Query createProjectRoleLevelQuery(final ProjectRoleManager.ProjectIdToProjectRoleIdsMap projectIdToProjectRolesMap)
            {
                assertEquals(idsMap, projectIdToProjectRolesMap);
                createRoleQueryCalled.set(true);
                return new BooleanQuery();
            }
        };

        final BooleanQuery levelQuery = commentClauseQueryFactory.createLevelRestrictionQueryForComments(projectIds, mockUser);

        BooleanQuery expectedQuery = new BooleanQuery();
        Query noGroupOrProjectQuery = commentClauseQueryFactory.createNoGroupOrProjectRoleLevelQuery();
        expectedQuery.add(noGroupOrProjectQuery, SHOULD);
        BooleanQuery roleQuery = new BooleanQuery();
        expectedQuery.add(roleQuery, SHOULD);
        assertEquals(expectedQuery, levelQuery);

        assertTrue("createRoleQueryCalled", createRoleQueryCalled.get());
    }

    @Test
    public void testCreateLevelRestrictionQueryForCommentsNullUser() throws Exception
    {
        final List<Long> projectIds = newArrayList(1L, 2L, 3L);

        commentClauseQueryFactory = new CommentClauseQueryFactory(null, projectRoleManager,
                operandResolver, null, luceneQueryModifier, null);

        final BooleanQuery levelQuery = commentClauseQueryFactory.createLevelRestrictionQueryForComments(projectIds, ANONYMOUS);

        BooleanQuery expectedQuery = new BooleanQuery();
        Query noGroupOrProjectQuery = commentClauseQueryFactory.createNoGroupOrProjectRoleLevelQuery();
        expectedQuery.add(noGroupOrProjectQuery, SHOULD);
        assertEquals(expectedQuery, levelQuery);
    }

    @Test
    public void testGetVisibleProjectIdsHappyPath() throws Exception
    {
        List<Project> projects = Lists.<Project>newArrayList(
                new MockProject(12),
                new MockProject(14));

        final PermissionManager mockPermissionManager = mock(PermissionManager.class);
        when(mockPermissionManager.getProjectObjects(Permissions.BROWSE, ANONYMOUS)).thenReturn(projects);

        commentClauseQueryFactory = new CommentClauseQueryFactory(mockPermissionManager, null,
                operandResolver, null, luceneQueryModifier, null);
        assertEquals(ImmutableList.of(12L, 14L), commentClauseQueryFactory.getVisibleProjectIds(null));
    }

    @Test
    public void testGetVisibleProjectIdsNullProject() throws Exception
    {
        List<Project> projects = Lists.<Project>newArrayList(new MockProject(12), null);

        final PermissionManager mockPermissionManager = mock(PermissionManager.class);
        when(mockPermissionManager.getProjectObjects(Permissions.BROWSE, null))
                .thenReturn(projects);

        commentClauseQueryFactory = new CommentClauseQueryFactory(mockPermissionManager, null,
                operandResolver, null, luceneQueryModifier, null);

        assertEquals(ImmutableList.of(12L), commentClauseQueryFactory.getVisibleProjectIds(null));
    }

    @Test
    public void testGetVisibleProjectIdsNullProjects() throws Exception
    {
        final PermissionManager mockPermissionManager = mock(PermissionManager.class);
        commentClauseQueryFactory = new CommentClauseQueryFactory(mockPermissionManager, null,
                operandResolver, null, luceneQueryModifier, null);

        assertEquals(ImmutableList.<Long>of(), commentClauseQueryFactory.getVisibleProjectIds(null));
    }

    @Test
    public void testGetQueryHappyPath() throws Exception
    {
        final AtomicBoolean visProjCalled = new AtomicBoolean(false);
        final AtomicBoolean createLevelCalled = new AtomicBoolean(false);
        final AtomicBoolean delegateCalled = new AtomicBoolean(false);
        final AtomicBoolean genCalled = new AtomicBoolean(false);
        commentClauseQueryFactory = new CommentClauseQueryFactory(null, null, operandResolver, null, luceneQueryModifier, null)
        {
            @Override
            List<Long> getVisibleProjectIds(final User searcher)
            {
                visProjCalled.set(true);
                return newArrayList(1L, 2L);
            }

            @Override
            BooleanQuery createLevelRestrictionQueryForComments(final List<Long> projectIds, final User searcher)
            {
                createLevelCalled.set(true);
                return new BooleanQuery();
            }

            @Override
            ClauseQueryFactory getDelegate(final JqlOperandResolver operandRegistry)
            {
                return new ClauseQueryFactory()
                {

                    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
                    {
                        delegateCalled.set(true);
                        return QueryFactoryResult.createFalseResult();
                    }

                };
            }

            @Override
            QueryFactoryResult generateIssueIdQueryFromCommentQuery(final Query commentIndexQuery, final QueryCreationContext creationContext)
            {
                genCalled.set(true);
                return QueryFactoryResult.createFalseResult();
            }
        };

        commentClauseQueryFactory.getQuery(queryCreationContext, new TerminalClauseImpl(IssueFieldConstants.COMMENT, Operator.LIKE, "test"));

        assertTrue("visProjCalled", visProjCalled.get());
        assertTrue("createLevelCalled", createLevelCalled.get());
        assertTrue("delegateCalled", delegateCalled.get());
        assertTrue("genCalled", genCalled.get());
    }

    @Test
    public void testGetQueryOverrideSecurity() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl(ANONYMOUS, true);

        final AtomicBoolean delegateCalled = new AtomicBoolean(false);
        final AtomicBoolean genCalled = new AtomicBoolean(false);
        commentClauseQueryFactory = new CommentClauseQueryFactory(null, null, operandResolver, null, luceneQueryModifier, null)
        {
            @Override
            ClauseQueryFactory getDelegate(final JqlOperandResolver operandRegistry)
            {
                return new ClauseQueryFactory()
                {
                    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
                    {
                        delegateCalled.set(true);
                        final TermQuery query = new TermQuery(new Term("comment", "test"));
                        return new QueryFactoryResult(query);
                    }
                };
            }

            @Override
            QueryFactoryResult generateIssueIdQueryFromCommentQuery(final Query commentIndexQuery, final QueryCreationContext creationContext)
            {
                assertThat(commentIndexQuery.toString(), containsString("comment:test"));
                genCalled.set(true);
                return QueryFactoryResult.createFalseResult();
            }
        };

        commentClauseQueryFactory.getQuery(queryCreationContext, new TerminalClauseImpl(IssueFieldConstants.COMMENT, Operator.LIKE, "test"));

        assertTrue("delegateCalled", delegateCalled.get());
        assertTrue("genCalled", genCalled.get());
    }

    @Test
    public void testGetQueryNoVisibleProjects() throws Exception
    {
        when(permissionManager.getProjectObjects(Permissions.BROWSE, ANONYMOUS))
                .thenReturn(Collections.<Project>emptyList());

        commentClauseQueryFactory = new Fixture();

        final QueryFactoryResult result = commentClauseQueryFactory.getQuery(queryCreationContext, new TerminalClauseImpl(IssueFieldConstants.COMMENT, Operator.LIKE, "test"));
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testGetQueryInvalidClause() throws Exception
    {
        commentClauseQueryFactory = new Fixture();

        final QueryFactoryResult result = commentClauseQueryFactory.getQuery(queryCreationContext, new TerminalClauseImpl(IssueFieldConstants.COMMENT, GREATER_THAN, "test"));
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    /**
     * Test that the query returned has level and role_level set to -1
     */
    @Test
    public void testCreateNoGroupOrProjectRoleLevelQuery()
    {
        final Query query = commentClauseQueryFactory.createNoGroupOrProjectRoleLevelQuery();
        assertEquals("+level:-1 +role_level:-1", query.toString());
    }

    @Test
    public void testCreateGroupLevelQuery() throws Exception
    {
        Query result = commentClauseQueryFactory.createGroupLevelQuery(null);
        assertEquals(new BooleanQuery(), result);

        result = commentClauseQueryFactory.createGroupLevelQuery(new HashSet<String>());
        assertEquals(new BooleanQuery(), result);

        Set<String> groups = newHashSet(GROUP_1);
        Query query = commentClauseQueryFactory.createGroupLevelQuery(groups);
        assertEquals("level:" + GROUP_1, query.toString());

        groups = newHashSet(GROUP_1, GROUP_2);
        query = commentClauseQueryFactory.createGroupLevelQuery(groups);
        assertThat(asList(query.toString().split(" ")), containsInAnyOrder("level:" + GROUP_1, "level:" + GROUP_2));

        groups = newHashSet(GROUP_1, GROUP_2, GROUP_3);
        query = commentClauseQueryFactory.createGroupLevelQuery(groups);
        assertThat(asList(query.toString().split(" ")), containsInAnyOrder("level:" + GROUP_1, "level:" + GROUP_2, "level:" + GROUP_3));
    }

    @Test
    public void testCreateProjectRoleLevelQuery() throws Exception
    {
        Query result = commentClauseQueryFactory.createProjectRoleLevelQuery(null);
        assertEquals(new BooleanQuery(), result);

        result = commentClauseQueryFactory.createProjectRoleLevelQuery(new ProjectRoleManager.ProjectIdToProjectRoleIdsMap());
        assertEquals(new BooleanQuery(), result);

        final ProjectRoleManager.ProjectIdToProjectRoleIdsMap map = new ProjectRoleManager.ProjectIdToProjectRoleIdsMap();
        map.add(1L, null);
        map.add(2L, PROJECT_ROLE_1.getId());
        map.add(2L, PROJECT_ROLE_2.getId());
        map.add(3L, PROJECT_ROLE_1.getId());
        map.add(3L, PROJECT_ROLE_2.getId());
        map.add(3L, PROJECT_ROLE_3.getId());

        final Query query = commentClauseQueryFactory.createProjectRoleLevelQuery(map);
        final String queryString = query.toString();

        // Project 1: No project role terms
        assertThat(queryString, not(hasProjectRole(1L, PROJECT_ROLE_1)));
        assertThat(queryString, not(hasProjectRole(1L, PROJECT_ROLE_2)));
        assertThat(queryString, not(hasProjectRole(1L, PROJECT_ROLE_3)));

        // Project 2: Project roles 1 and 2
        assertThat(queryString, hasProjectRole(2L, PROJECT_ROLE_1));
        assertThat(queryString, hasProjectRole(2L, PROJECT_ROLE_2));
        assertThat(queryString, not(hasProjectRole(2L, PROJECT_ROLE_3)));

        // Project 3: All of them
        assertThat(queryString, hasProjectRole(3L, PROJECT_ROLE_1));
        assertThat(queryString, hasProjectRole(3L, PROJECT_ROLE_2));
        assertThat(queryString, hasProjectRole(3L, PROJECT_ROLE_3));
    }

    @Test
    public void testCreateCommentInProjectAndUserInRoleQuery()
    {
        Query result = commentClauseQueryFactory.createCommentInProjectAndUserInRoleQuery(0L, null);
        assertEquals(new BooleanQuery(), result);

        result = commentClauseQueryFactory.createCommentInProjectAndUserInRoleQuery(null, 0L);
        assertEquals(new BooleanQuery(), result);

        final Long projectId = 123L;
        final Long projectRoleId = 567L;

        final Query query = commentClauseQueryFactory.createCommentInProjectAndUserInRoleQuery(projectId, projectRoleId);
        assertThat(query.toString(), hasProjectRole(projectId, projectRoleId));
    }

    @Test
    public void testGetPermissionsFilterOverrideSecurity() throws Exception
    {
        assertNull(commentClauseQueryFactory.getPermissionsFilter(true, null));
    }

    @Test
    public void testGetPermissionsFilterNotFromCache() throws Exception
    {
        when(permissionsFilterGenerator.getQuery(null)).thenReturn(new BooleanQuery());
        assertNotNull(commentClauseQueryFactory.getPermissionsFilter(false, null));
    }

    @Test
    public void testGetPermissionsFilterFromCache() throws Exception
    {
        when(cachedWrappedFilterCache.getFilter(null)).thenReturn(new QueryWrapperFilter(new BooleanQuery()));
        assertNotNull(commentClauseQueryFactory.getPermissionsFilter(false, null));
    }

    private void assertOperatorValidity(boolean expected, Operator operator)
    {
        final boolean result = commentClauseQueryFactory.isClauseValid(new TerminalClauseImpl(IssueFieldConstants.COMMENT, operator, "test"));
        assertThat("Validity of " + operator.name(), result, is(expected));
    }



    class Fixture extends CommentClauseQueryFactory
    {
        Fixture()
        {
            super(permissionManager, projectRoleManager, operandResolver, searchProviderFactory,
                    luceneQueryModifier, permissionsFilterGenerator);
        }
    }



    static Matcher<String> hasProjectRole(final long projectId, final ProjectRole role)
    {
        return new ProjectAndRoleQueryMatcher(projectId, role.toString(), role.getId());
    }

    static Matcher<String> hasProjectRole(final long projectId, final long roleId)
    {
        return new ProjectAndRoleQueryMatcher(projectId, String.valueOf(roleId), roleId);
    }

    static class ProjectAndRoleQueryMatcher extends TypeSafeMatcher<String>
    {
        private final long projectId;
        private final String role;
        private final String expected;

        ProjectAndRoleQueryMatcher(final long projectId, final String role, final long roleId)
        {
            this.projectId = projectId;
            this.role = role;
            this.expected = "(+projid:" + projectId + " +role_level:" + roleId + ')';
        }

        @Override
        protected boolean matchesSafely(final String queryString)
        {
            // Either a ()'d substring, or the entire string equal without the ()'s
            return queryString.contains(expected) ||
                    queryString.equals(expected.substring(1, expected.length()-1));
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText("query string containing term for project ID ").appendValue(projectId)
                    .appendText(" and project role ").appendValue(role).appendText("; expected=[")
                    .appendText(expected).appendText("]");
        }
    }
}
