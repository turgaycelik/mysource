package com.atlassian.jira.plugin.jql.function;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.permission.LiteralSanitiser;
import com.atlassian.jira.jql.permission.MockLiteralSanitiser;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.mock.plugin.jql.operand.MockJqlFunctionModuleDescriptor;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import com.google.common.collect.ImmutableList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.picocontainer.Converting;

import static com.atlassian.jira.util.MessageSetAssert.assert1ErrorNoWarnings;
import static com.atlassian.jira.util.MessageSetAssert.assertNoMessages;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestAbstractVersionsFunction
{
    private static final String FUNC_NAME = "funcName";

    @Mock PermissionManager permissionManager;
    @Mock IndexInfoResolver<Project> projectResolver;
    @Mock NameResolver<Project> nameResolver;
    @Mock VersionManager versionManager;

    private TerminalClause terminalClause = null;
    private ApplicationUser theUser = new MockApplicationUser("Bob");
    private QueryCreationContext queryCreationContext;

    @Before
    public void setUp()
    {
        queryCreationContext = new QueryCreationContextImpl(theUser);
    }

    @After
    public void tearDown()
    {
        permissionManager = null;
        projectResolver = null;
        nameResolver = null;
        terminalClause = null;
        theUser = null;
        queryCreationContext = null;
    }

    @Test
    public void testDataType() throws Exception
    {
        AbstractVersionsFunction handler = createVersionFunction();
        assertEquals(JiraDataTypes.VERSION, handler.getDataType());
    }

    @Test
    public void testValidateZeroArgs() throws Exception
    {
        AbstractVersionsFunction handler = createVersionFunction();

        final MessageSet messageSet = handler.validate(theUser.getDirectoryUser(), new FunctionOperand(FUNC_NAME), terminalClause);
        assertNoMessages(messageSet);
    }

    @Test
    public void testValidateOneArgNotAProject() throws Exception
    {
        when(projectResolver.getIndexedValues("arg")).thenReturn(Collections.<String>emptyList());

        AbstractVersionsFunction handler = createVersionFunction();

        final MessageSet messageSet = handler.validate(theUser.getDirectoryUser(), new FunctionOperand(FUNC_NAME, Collections.singletonList("arg")), terminalClause);
        assert1ErrorNoWarnings(messageSet, "Could not resolve the project 'arg' provided to function 'funcName'.");
    }

    @Test
    public void testValidateTwoArgsSecondNotAProject() throws Exception
    {
        final MockProject project = new MockProject(1L);

        when(projectResolver.getIndexedValues("arg1")).thenReturn(Collections.singletonList("1"));
        when(projectResolver.getIndexedValues("arg2")).thenReturn(Collections.<String>emptyList());
        when(nameResolver.get(1L)).thenReturn(project);
        when(permissionManager.hasPermission(Permissions.BROWSE, project, theUser)).thenReturn(true);

        AbstractVersionsFunction handler = createVersionFunction();

        final MessageSet messageSet = handler.validate(theUser.getDirectoryUser(), new FunctionOperand(FUNC_NAME, ImmutableList.of("arg1", "arg2")), terminalClause);
        assert1ErrorNoWarnings(messageSet, "Could not resolve the project 'arg2' provided to function 'funcName'.");
    }

    @Test
    public void testValidateTwoArgsSecondNoPermission() throws Exception
    {
        final MockProject project1 = new MockProject(1L);
        final MockProject project2 = new MockProject(2L);

        when(projectResolver.getIndexedValues("arg1")).thenReturn(Collections.singletonList("1"));
        when(projectResolver.getIndexedValues("arg2")).thenReturn(Collections.singletonList("2"));

        when(nameResolver.get(1L)).thenReturn(project1);
        when(nameResolver.get(2L)).thenReturn(project2);

        when(permissionManager.hasPermission(Permissions.BROWSE, project1, theUser)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.BROWSE, project2, theUser)).thenReturn(false);

        AbstractVersionsFunction handler = createVersionFunction();

        final MessageSet messageSet = handler.validate(theUser.getDirectoryUser(), new FunctionOperand(FUNC_NAME, ImmutableList.of("arg1", "arg2")), terminalClause);
        assert1ErrorNoWarnings(messageSet, "Could not resolve the project 'arg2' provided to function 'funcName'.");
    }

    @Test
    public void testValidateOneArgTooManyProjects() throws Exception
    {
        when(projectResolver.getIndexedValues("arg")).thenReturn(ImmutableList.of("1", "2"));

        AbstractVersionsFunction handler = createVersionFunction();

        try
        {
            handler.validate(theUser.getDirectoryUser(), new FunctionOperand(FUNC_NAME, Collections.singletonList("arg")), terminalClause);
            fail("Expected exception for too many projects returned");
        }
        catch (IllegalArgumentException expected) {}
    }

    @Test
    public void testGetValuesZeroArgsOneViewableProjectOneNonViewable() throws Exception
    {
        final MockProject project1 = new MockProject(1L);
        final MockProject project2 = new MockProject(2L);
        final MockVersion version1 = new MockVersion(1L, "Version 1", project1);
        final MockVersion version2 = new MockVersion(2L, "Version 2", project2);
        project1.setVersions(ImmutableList.<Version>of(version1));
        project2.setVersions(ImmutableList.<Version>of(version2));

        when(versionManager.getVersionsReleased(1L, true)).thenReturn(ImmutableList.<Version>of(version1));
        when(permissionManager.getProjects(Permissions.BROWSE, theUser)).thenReturn(ImmutableList.<Project>of(project1));

        AbstractVersionsFunction handler = createVersionFunction(ImmutableList.<Version>of(version1, version2), versionManager);

        final FunctionOperand operand = new FunctionOperand(FUNC_NAME, Collections.<String>emptyList());
        final List<QueryLiteral> values = handler.getValues(queryCreationContext, operand, terminalClause);
        assertTrue(values.contains(new QueryLiteral(operand, 1L)));
        assertFalse(values.contains(new QueryLiteral(operand, 2L)));
    }

    @Test
    public void testGetValuesZeroArgsOneViewableProjectOneNonViewableOverrideSecurity() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl(theUser, true);
        final Project project1 = new MockProject(1L);
        final Project project2 = new MockProject(2L);
        final MockVersion version1 = new MockVersion(1L, "Version 1", project1);
        final MockVersion version2 = new MockVersion(2L, "Version 2", project2);

        AbstractVersionsFunction handler = createVersionFunction(ImmutableList.<Version>of(version1, version2), null);

        final FunctionOperand operand = new FunctionOperand(FUNC_NAME, Collections.<String>emptyList());
        final List<QueryLiteral> values = handler.getValues(queryCreationContext, operand, terminalClause);
        assertTrue(values.contains(new QueryLiteral(operand, 1L)));
        assertTrue(values.contains(new QueryLiteral(operand, 2L)));
        assertEquals(operand, values.get(0).getSourceOperand());
        assertEquals(operand, values.get(1).getSourceOperand());
    }

    @Test
    public void testGetValuesOneArgGood() throws Exception
    {
        final MockVersion version1 = new MockVersion(1L, "Version 1");
        final MockVersion version2 = new MockVersion(2L, "Version 2");
        final MockProject project = new MockProject(100L);

        when(projectResolver.getIndexedValues("arg")).thenReturn(Collections.singletonList("100"));
        when(nameResolver.get(100L)).thenReturn(project);
        when(permissionManager.hasPermission(Permissions.BROWSE, project, theUser)).thenReturn(true);

        final VersionManager versionManager = mock(VersionManager.class);
        when(versionManager.getVersionsReleased(100L, true)).thenReturn(ImmutableList.<Version>of(version1, version2));

        AbstractVersionsFunction handler = createVersionFunction(null, versionManager);

        final FunctionOperand operand = new FunctionOperand(FUNC_NAME, Collections.singletonList("arg"));
        final List<QueryLiteral> values = handler.getValues(queryCreationContext, operand, terminalClause);
        assertTrue(values.contains(new QueryLiteral(operand, 1L)));
        assertTrue(values.contains(new QueryLiteral(operand, 2L)));
        assertEquals(operand, values.get(0).getSourceOperand());
        assertEquals(operand, values.get(1).getSourceOperand());
    }

    @Test
    public void testGetValuesOneArgNoResolvedProject() throws Exception
    {
        when(projectResolver.getIndexedValues("arg")).thenReturn(ImmutableList.<String>of());

        AbstractVersionsFunction handler = createVersionFunction(null, null);

        final List<QueryLiteral> values = handler.getValues(queryCreationContext, new FunctionOperand(FUNC_NAME, Collections.singletonList("arg")), terminalClause);
        assertTrue(values.isEmpty());
    }

    @Test
    public void testGetValuesOneArgMultipleResolvedProjects() throws Exception
    {
        when(projectResolver.getIndexedValues("arg")).thenReturn(ImmutableList.of("1", "2"));

        AbstractVersionsFunction handler = createVersionFunction(null, null);

        final List<QueryLiteral> values = handler.getValues(queryCreationContext, new FunctionOperand(FUNC_NAME, Collections.singletonList("arg")), terminalClause);
        assertTrue(values.isEmpty());
    }

    @Test
    public void testGetValuesTwoArgsGood() throws Exception
    {
        final MockVersion version1 = new MockVersion(1L, "Version 1");
        final MockVersion version2 = new MockVersion(2L, "Version 2");

        final MockProject project1 = new MockProject(100L);
        final MockProject project2 = new MockProject(200L);

        when(projectResolver.getIndexedValues("arg1")).thenReturn(Collections.singletonList("100"));
        when(projectResolver.getIndexedValues("arg2")).thenReturn(Collections.singletonList("200"));

        when(nameResolver.get(100L)).thenReturn(project1);
        when(nameResolver.get(200L)).thenReturn(project2);

        when(permissionManager.hasPermission(Permissions.BROWSE, project1, theUser)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.BROWSE, project2, theUser)).thenReturn(true);

        final VersionManager versionManager = mock(VersionManager.class);
        when(versionManager.getVersionsReleased(100L, true)).thenReturn(ImmutableList.<Version>of(version2));
        when(versionManager.getVersionsReleased(200L, true)).thenReturn(ImmutableList.<Version>of(version1));

        AbstractVersionsFunction handler = createVersionFunction(null, versionManager);

        final FunctionOperand operand = new FunctionOperand(FUNC_NAME, ImmutableList.of("arg1", "arg2"));
        final List<QueryLiteral> values = handler.getValues(queryCreationContext, operand, terminalClause);
        assertTrue(values.contains(new QueryLiteral(operand, 1L)));
        assertTrue(values.contains(new QueryLiteral(operand, 2L)));
        assertEquals(operand, values.get(0).getSourceOperand());
        assertEquals(operand, values.get(1).getSourceOperand());
    }

    @Test
    public void testGetValuesTwoArgsOneGoodOneDoesntResolve() throws Exception
    {
        final MockVersion version2 = new MockVersion(2L, "Version 2");

        final MockProject project1 = new MockProject(100L);

        when(projectResolver.getIndexedValues("arg1")).thenReturn(Collections.singletonList("100"));
        when(projectResolver.getIndexedValues("arg2")).thenReturn(Collections.<String>emptyList());

        when(nameResolver.get(100L)).thenReturn(project1);

        when(permissionManager.hasPermission(Permissions.BROWSE, project1, theUser)).thenReturn(true);

        final VersionManager versionManager = mock(VersionManager.class);
        when(versionManager.getVersionsReleased(100L, true)).thenReturn(ImmutableList.<Version>of(version2));

        AbstractVersionsFunction handler = createVersionFunction(null, versionManager);

        final FunctionOperand operand = new FunctionOperand(FUNC_NAME, ImmutableList.of("arg1", "arg2"));
        final List<QueryLiteral> values = handler.getValues(queryCreationContext, operand, terminalClause);
        assertTrue(values.contains(new QueryLiteral(operand, 2L)));
        assertEquals(operand, values.get(0).getSourceOperand());
    }

    @Test
    public void testGetValuesTwoArgsOneGoodOneNoPermission() throws Exception
    {
        final MockVersion version2 = new MockVersion(2L, "Version 2");

        final MockProject project1 = new MockProject(100L);
        final MockProject project2 = new MockProject(200L);

        when(projectResolver.getIndexedValues("arg1")).thenReturn(Collections.singletonList("100"));
        when(projectResolver.getIndexedValues("arg2")).thenReturn(Collections.singletonList("200"));

        when(nameResolver.get(100L)).thenReturn(project1);
        when(nameResolver.get(200L)).thenReturn(project2);

        when(permissionManager.hasPermission(Permissions.BROWSE, project1, theUser)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.BROWSE, project2, theUser)).thenReturn(false);

        final VersionManager versionManager = mock(VersionManager.class);
        when(versionManager.getVersionsReleased(100L, true)).thenReturn(ImmutableList.<Version>of(version2));

        AbstractVersionsFunction handler = createVersionFunction(null, versionManager);

        final FunctionOperand operand = new FunctionOperand(FUNC_NAME, ImmutableList.of("arg1", "arg2"));
        final List<QueryLiteral> values = handler.getValues(queryCreationContext, operand, terminalClause);
        assertTrue(values.contains(new QueryLiteral(operand, 2L)));
        assertEquals(operand, values.get(0).getSourceOperand());
    }

    @Test
    public void testGetValuesTwoArgsOneGoodOneNoPermissionOverrideSecurity() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl(theUser, true);
        final MockVersion version1 = new MockVersion(1L, "Version 1");
        final MockVersion version2 = new MockVersion(2L, "Version 2");

        when(projectResolver.getIndexedValues("arg1")).thenReturn(Collections.singletonList("100"));
        when(projectResolver.getIndexedValues("arg2")).thenReturn(Collections.singletonList("200"));

        final VersionManager versionManager = mock(VersionManager.class);
        when(versionManager.getVersionsReleased(100L, true)).thenReturn(ImmutableList.<Version>of(version1));
        when(versionManager.getVersionsReleased(200L, true)).thenReturn(ImmutableList.<Version>of(version2));

        AbstractVersionsFunction handler = createVersionFunction(null, versionManager);

        final FunctionOperand operand = new FunctionOperand(FUNC_NAME, ImmutableList.of("arg1", "arg2"));
        final List<QueryLiteral> values = handler.getValues(queryCreationContext, operand, terminalClause);
        assertTrue(values.contains(new QueryLiteral(operand, 1L)));
        assertTrue(values.contains(new QueryLiteral(operand, 2L)));
        assertEquals(operand, values.get(0).getSourceOperand());
        assertEquals(operand, values.get(1).getSourceOperand());
    }

    @Test
    public void testSanitiseEmptyArgs() throws Exception
    {
        AbstractVersionsFunction handler = createVersionFunction(new MessageSetImpl());

        final FunctionOperand inputOperand = new FunctionOperand(FUNC_NAME);
        final FunctionOperand cleanOperand = handler.sanitiseOperand(theUser.getDirectoryUser(), inputOperand);
        assertSame(cleanOperand, inputOperand);
    }

    @Test
    public void testSanitiseNotModified() throws Exception
    {
        final FunctionOperand inputOperand = new FunctionOperand(FUNC_NAME, "arg1", "arg2");
        final MockLiteralSanitiser sanitiser = new MockLiteralSanitiser(new LiteralSanitiser.Result(false, null), new QueryLiteral(inputOperand, "arg1"), new QueryLiteral(inputOperand, "arg2"));
        AbstractVersionsFunction handler = createVersionFunction(sanitiser);

        final FunctionOperand cleanOperand = handler.sanitiseOperand(theUser.getDirectoryUser(), inputOperand);
        assertSame(cleanOperand, inputOperand);
    }

    @Test
    public void testSanitiseModified() throws Exception
    {
        final FunctionOperand inputOperand = new FunctionOperand(FUNC_NAME, "arg1", "arg2");
        final MockLiteralSanitiser sanitiser = new MockLiteralSanitiser(new LiteralSanitiser.Result(true, Collections.singletonList(new QueryLiteral(inputOperand, "clean"))),
                new QueryLiteral(inputOperand, "arg1"),
                new QueryLiteral(inputOperand, "arg2"));
        AbstractVersionsFunction handler = createVersionFunction(sanitiser);

        final FunctionOperand expectedOperand = new FunctionOperand(FUNC_NAME, "clean");
        final FunctionOperand cleanOperand = handler.sanitiseOperand(theUser.getDirectoryUser(), inputOperand);
        assertEquals(expectedOperand, cleanOperand);
    }

    @Test
    public void testGetMinimumNumberOfExpectedArguments() throws Exception
    {
        AbstractVersionsFunction handler = createVersionFunction((LiteralSanitiser) null);
        assertEquals(0, handler.getMinimumNumberOfExpectedArguments());
    }

    private AbstractVersionsFunction createVersionFunction()
    {
        final AbstractVersionsFunction function = new AbstractVersionsFunction(nameResolver, permissionManager)
        {
            @Override
            protected IndexInfoResolver<Project> createIndexInfoResolver(final NameResolver<Project> projectNameResolver)
            {
                return projectResolver;
            }

            @Override
            protected Collection<Version> getAllVersions(User user)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            protected Collection<Version> getVersionsForProject(final Long projectId)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }
        };
        function.init(MockJqlFunctionModuleDescriptor.create(FUNC_NAME, true));
        return function;
    }

    private AbstractVersionsFunction createVersionFunction(final Collection<Version> allVersions, final VersionManager versionManager)
    {
        return new AbstractVersionsFunction(nameResolver, permissionManager)
        {
            @Override
            protected IndexInfoResolver<Project> createIndexInfoResolver(final NameResolver<Project> projectNameResolver)
            {
                return projectResolver;
            }

            @Override
            protected Collection<Version> getAllVersions(User user)
            {
                if (allVersions == null)
                {
                    fail("Not expecting this call");
                }
                return allVersions;
            }

            @Override
            protected Collection<Version> getVersionsForProject(final Long projectId)
            {
                if (versionManager == null)
                {
                    fail("Not expecting this call");
                    return null;
                }
                return versionManager.getVersionsReleased(projectId, true);
            }

            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }
        };
    }

    private AbstractVersionsFunction createVersionFunction(final MessageSet errors)
    {
        return new AbstractVersionsFunction(nameResolver, permissionManager)
        {
            @Override
            protected Collection<Version> getAllVersions(User user)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            protected Collection<Version> getVersionsForProject(final Long projectId)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }

            @Override
            public MessageSet validate(User searcher, final FunctionOperand operand, final TerminalClause terminalClause)
            {
                return errors;
            }
        };
    }

    private AbstractVersionsFunction createVersionFunction(final LiteralSanitiser sanitiser)
    {
        return new AbstractVersionsFunction(nameResolver, permissionManager)
        {
            @Override
            protected Collection<Version> getAllVersions(User user)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            protected Collection<Version> getVersionsForProject(final Long projectId)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }

            @Override
            public MessageSet validate(User searcher, final FunctionOperand operand, final TerminalClause terminalClause)
            {
                return new MessageSetImpl();
            }

            @Override
            LiteralSanitiser createLiteralSanitiser(final User user)
            {
                return sanitiser;
            }
        };
    }
}
