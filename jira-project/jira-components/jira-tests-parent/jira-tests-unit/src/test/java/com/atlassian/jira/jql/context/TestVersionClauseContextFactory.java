package com.atlassian.jira.jql.context;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.VersionResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestVersionClauseContextFactory extends MockControllerTestCase
{
    private JqlOperandResolver jqlOperandResolver;
    private VersionResolver versionResolver;
    private PermissionManager permissionManager;
    private User theUser = null;

    @Before
    public void setUp() throws Exception
    {
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        final VersionManager versionManager = mockController.getMock(VersionManager.class);
        versionResolver = new VersionResolver(versionManager);
        mockController.addObjectInstance(versionResolver);
        permissionManager = mockController.getMock(PermissionManager.class);
    }

    @Test
    public void testGetContextFromClauseSingleValueEqualityOperand() throws Exception
    {
        final MockProject project = new MockProject(1234L);
        final SingleValueOperand operand = new SingleValueOperand("blarg");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.EQUALS, operand);

        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(createLiteral("blarg")).asList());

        final Version version = new MockVersion(10, "version", project);

        VersionResolver versionResolver = new VersionResolver(mockController.getMock(VersionManager.class))
        {
            @Override
            public Version get(final Long id)
            {
                return version;
            }
        };
        final Set<ProjectIssueTypeContext> issueTypeContexts = CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("it"))).asListOrderedSet();

        mockController.replay();
        VersionClauseContextFactory factory = new VersionClauseContextFactory(jqlOperandResolver, versionResolver, permissionManager)
        {
            @Override
            List<Long> getIds(final QueryLiteral literal)
            {
                return CollectionBuilder.newBuilder(10L).asList();
            }

            @Override
            Set<ProjectIssueTypeContext> getContextsForProject(final User searcher, final Project project)
            {
                return issueTypeContexts;
            }
        };

        final ClauseContext result = factory.getContextFromClause(theUser, clause);
        ClauseContext expectedResult = new ClauseContextImpl(issueTypeContexts);

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetContextFromClauseSingleValueNegationOperand() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blarg");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.NOT_EQUALS, operand);

        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(createLiteral(10L)).asList());

        final MockProject project1 = new MockProject(1234L);
        final MockProject project2 = new MockProject(5678L);
        final MockProject project3 = new MockProject(9876L);
        final Version excludedVersion = new MockVersion(10, "excludedVersion", project1);
        final Version version1 = new MockVersion(15, "version1", project2);
        final Version version2 = new MockVersion(20, "version2", project3);

        VersionResolver versionResolver = new VersionResolver(mockController.getMock(VersionManager.class))
        {
            @Override
            public Version get(final Long id)
            {
                if (id.equals(10L))
                {
                    return excludedVersion;
                }
                return null;
            }

            @Override
            public Collection<Version> getAll()
            {
                return CollectionBuilder.newBuilder(excludedVersion, version1, version2).asList();
            }
        };

        final Set<ProjectIssueTypeContext> issueTypeContexts1 = CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("it"))).asListOrderedSet();
        final Set<ProjectIssueTypeContext> issueTypeContexts2 = CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(50L), new IssueTypeContextImpl("it2"))).asListOrderedSet();

        mockController.replay();
        VersionClauseContextFactory factory = new VersionClauseContextFactory(jqlOperandResolver, versionResolver, permissionManager)
        {
            @Override
            List<Long> getIds(final QueryLiteral literal)
            {
                return CollectionBuilder.newBuilder(10L).asList();
            }

            @Override
            Set<ProjectIssueTypeContext> getContextsForProject(final User searcher, final Project project)
            {
                if (project.getId().equals(5678L))
                {
                    return issueTypeContexts1;
                }
                else if (project.getId().equals(9876L))
                {
                    return issueTypeContexts2;
                }
                return null;
            }
        };

        final ClauseContext result = factory.getContextFromClause(theUser, clause);
        ClauseContext expectedResult = new ClauseContextImpl(new HashSet<ProjectIssueTypeContext>(CollectionUtils.union(issueTypeContexts1, issueTypeContexts2)));

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetContextFromClauseMultipleValueNegationOperand() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blarg");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.NOT_EQUALS, operand);

        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(createLiteral(10L)).asList());

        final MockProject project1 = new MockProject(1234L);
        final MockProject project2 = new MockProject(5678L);
        final MockProject project3 = new MockProject(9876L);
        final Version excludedVersion1 = new MockVersion(10, "excludedVersion1", project1);
        final Version excludedVersion2 = new MockVersion(20, "excludedVersion2", project3);
        final Version version1 = new MockVersion(15, "version1", project2);

        VersionResolver versionResolver = new VersionResolver(mockController.getMock(VersionManager.class))
        {
            @Override
            public Version get(final Long id)
            {
                if (id.equals(10L))
                {
                    return excludedVersion1;
                }
                if (id.equals(20L))
                {
                    return excludedVersion2;
                }
                return null;
            }

            @Override
            public Collection<Version> getAll()
            {
                return CollectionBuilder.newBuilder(excludedVersion1, version1, excludedVersion2).asList();
            }
        };

        final Set<ProjectIssueTypeContext> issueTypeContexts1 = CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("it"))).asListOrderedSet();

        mockController.replay();
        VersionClauseContextFactory factory = new VersionClauseContextFactory(jqlOperandResolver, versionResolver, permissionManager)
        {
            @Override
            List<Long> getIds(final QueryLiteral literal)
            {
                return CollectionBuilder.newBuilder(10L, 20L).asList();
            }

            @Override
            Set<ProjectIssueTypeContext> getContextsForProject(final User searcher, final Project project)
            {
                if (project.getId().equals(5678L))
                {
                    return issueTypeContexts1;
                }
                return null;
            }
        };

        final ClauseContext result = factory.getContextFromClause(theUser, clause);
        ClauseContext expectedResult = new ClauseContextImpl(issueTypeContexts1);

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetContextFromClauseSingleValueRelationalOperand() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("specifiedVersion");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.GREATER_THAN, operand);

        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(createLiteral(10L)).asList());

        final Project project = new MockProject(50, "name");

        final Version specifiedVersion = new MockVersion(10, "specifiedVersion", project, 1L);
        final Version version1 = new MockVersion(15, "version1", project, 2L);
        final Version version2 = new MockVersion(20, "version2", project, 3L);

        VersionResolver versionResolver = new VersionResolver(mockController.getMock(VersionManager.class))
        {
            @Override
            public Version get(final Long id)
            {
                if (id.equals(10L))
                {
                    return specifiedVersion;
                }
                return null;
            }

            @Override
            public Collection<Version> getAll()
            {
                return CollectionBuilder.newBuilder(specifiedVersion, version1, version2).asList();
            }
        };

        final Set<ProjectIssueTypeContext> issueTypeContexts1 = CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(50L), new IssueTypeContextImpl("it2"))).asListOrderedSet();

        mockController.replay();
        VersionClauseContextFactory factory = new VersionClauseContextFactory(jqlOperandResolver, versionResolver, permissionManager)
        {
            boolean first = true;
            @Override
            List<Long> getIds(final QueryLiteral literal)
            {
                return CollectionBuilder.newBuilder(10L).asList();
            }

            @Override
            Set<ProjectIssueTypeContext> getContextsForProject(final User searcher, final Project project)
            {
                if (project.getId() == 50L)
                {
                    return issueTypeContexts1;
                }
                else
                {
                    throw new IllegalStateException();
                }
            }
        };

        final ClauseContext result = factory.getContextFromClause(theUser, clause);
        ClauseContext expectedResult = new ClauseContextImpl(new HashSet<ProjectIssueTypeContext>(issueTypeContexts1));

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetContextFromClauseMultiValueEqualityOperand() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blarg");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.EQUALS, operand);

        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(createLiteral(10L), createLiteral(20L)).asList());

        final MockProject project1 = new MockProject(5678L);
        final MockProject project2 = new MockProject(9876L);
        final Version version1 = new MockVersion(10, "version", project1);
        final Version version2 = new MockVersion(20, "version", project2);

        VersionResolver versionResolver = new VersionResolver(mockController.getMock(VersionManager.class))
        {
            @Override
            public Version get(final Long id)
            {
                if (id.equals(10L))
                {
                    return version1;
                } else if (id.equals(20L))
                {
                    return version2;
                }
                return null;
            }
        };

        final Set<ProjectIssueTypeContext> issueTypeContexts1 = CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("it"))).asListOrderedSet();
        final Set<ProjectIssueTypeContext> issueTypeContexts2 = CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(20L), new IssueTypeContextImpl("it2"))).asListOrderedSet();

        mockController.replay();
        VersionClauseContextFactory factory = new VersionClauseContextFactory(jqlOperandResolver, versionResolver, permissionManager)
        {
            @Override
            List<Long> getIds(final QueryLiteral literal)
            {
                return CollectionBuilder.newBuilder(literal.getLongValue()).asList();
            }

            @Override
            Set<ProjectIssueTypeContext> getContextsForProject(final User searcher, final Project project)
            {
                if (project.getId().equals(5678L))
                {
                    return issueTypeContexts1;
                }
                else if (project.getId().equals(9876L))
                {
                    return issueTypeContexts2;
                }
                return null;
            }
        };

        final ClauseContext result = factory.getContextFromClause(theUser, clause);
        ClauseContext expectedResult = new ClauseContextImpl(new HashSet<ProjectIssueTypeContext>(CollectionUtils.union(issueTypeContexts1, issueTypeContexts2)));

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetContextFromClauseEmpty() throws Exception
    {
        final EmptyOperand operand = EmptyOperand.EMPTY;
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.IS, operand);

        jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();

        mockController.replay();
        VersionClauseContextFactory factory = new VersionClauseContextFactory(jqlOperandResolver, versionResolver, permissionManager);

        final ClauseContext result = factory.getContextFromClause(theUser, clause);
        ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetContextFromClauseNullLiterals() throws Exception
    {
        final EmptyOperand operand = EmptyOperand.EMPTY;
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.IS, operand);

        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(null);

        mockController.replay();
        VersionClauseContextFactory factory = new VersionClauseContextFactory(jqlOperandResolver, versionResolver, permissionManager);

        final ClauseContext result = factory.getContextFromClause(theUser, clause);
        ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetContextFromClauseInvalidOperator() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blarg");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.LIKE, operand);

        VersionClauseContextFactory factory = mockController.instantiateAndReplay(VersionClauseContextFactory.class);

        final ClauseContext result = factory.getContextFromClause(theUser, clause);
        ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        assertEquals(expectedResult, result);

        mockController.verify();
    }
}
