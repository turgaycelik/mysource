package com.atlassian.jira.issue.search.quicksearch;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.google.common.collect.ImmutableList;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class TestProjectAwareQuickSearchHandlerImpl
{
    @Rule
    public final InitMockitoMocks mockitoFtw = new InitMockitoMocks(this);

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Mock
    private ProjectManager mockProjectManager;

    @Mock
    private PermissionManager mockPermissionManager;

    @Mock
    private JiraAuthenticationContext mockAuthenticationContext;

    @Mock
    private User user;

    private ProjectAwareQuickSearchHandler projectAwareHandler;

    @Before
    public void setUp() throws Exception
    {
        user = new MockUser("testUser");
        when(mockAuthenticationContext.getLoggedInUser()).thenReturn(user);

        projectAwareHandler = new ProjectAwareQuickSearchHandlerImpl(mockProjectManager, mockPermissionManager, mockAuthenticationContext);
    }

    @Test
    public void shouldFindValidProject() throws Exception
    {
        final GenericValue project = createProject(10000L);

        assertThat(
                (List<GenericValue>) projectAwareHandler.getProjects(buildQuickSearchResult(idOf(project))),
                Matchers.<GenericValue>containsInAnyOrder(project)
        );

        verifyZeroInteractions(mockPermissionManager, mockAuthenticationContext);
    }

    @Test
    public void shouldReturnEmptySearchForNonExistentProjects() throws Exception
    {
        final long id = 10004L;
        createProject(id);

        assertEquals(
                projectAwareHandler.getProjects(buildQuickSearchResult(Long.toString(id + 1))),
                Collections.singletonList(null)
        );

        verifyZeroInteractions(mockPermissionManager, mockAuthenticationContext);
    }

    @Test
    public void shouldThrowNumberFormatExceptionOnMalformedInput() throws Exception
    {
        expectNumberFormatExceptionFor("invalid");
    }

    @Test
    public void shouldThrowNumberFormatExceptionOnEmptyInput() throws Exception
    {
        expectNumberFormatExceptionFor("");
    }

    @Test
    public void shouldThrowNumberFormatExceptionOnNullInput() throws Exception
    {
        expectNumberFormatExceptionFor(null);
    }

    @Test
    public void testReturnedBrowseableProjectsForNoInput()
    {
        final long id = 10090L;
        final GenericValue project = createProject(id);
        when(mockPermissionManager.getProjects(Permissions.BROWSE, user)).thenReturn(Collections.singletonList(project));

        assertThat(
                (List<GenericValue>) projectAwareHandler.getProjects(buildQuickSearchResult()),
                Matchers.<GenericValue>containsInAnyOrder(project)
        );
    }

    @Test
    public void shouldReturnAllBrowseableProjectsForNarrowerSearch()
    {
        final GenericValue project1 = createProject(10010L);
        final GenericValue project2 = createProject(10020L);
        final GenericValue project3 = createProject(10030L);

        when(mockPermissionManager.getProjects(Permissions.BROWSE, user)).thenReturn(ImmutableList.of(project1, project2, project3));

        assertThat(
                (List<GenericValue>) projectAwareHandler.getProjects(buildQuickSearchResult(idOf(project1), idOf(project2))),
                Matchers.<GenericValue>containsInAnyOrder(project1, project2, project3)
        );
    }

    @Test
    public void shouldReturnOnlyBrowseableProjectsForWiderSearch()
    {
        final GenericValue project1 = createProject(10010L);
        final GenericValue project2 = createProject(10020L);
        final GenericValue project3 = createProject(10030L);

        when(mockPermissionManager.getProjects(Permissions.BROWSE, user)).thenReturn(ImmutableList.of(project1, project2));

        assertThat(
                (List<GenericValue>) projectAwareHandler.getProjects(buildQuickSearchResult(idOf(project1), idOf(project2), idOf(project3))),
                Matchers.<GenericValue>containsInAnyOrder(project1, project2)
        );
    }

    @Test
    public void shouldReturnBrowseableProjectsForMultipleInputEvenIfMissed()
    {
        final GenericValue project1 = createProject(10011L);
        final GenericValue project2 = createProject(10021L);
        createProject(10031L);

        when(mockPermissionManager.getProjects(Permissions.BROWSE, user)).thenReturn(ImmutableList.of(project1, project2));

        assertThat(
                (List<GenericValue>) projectAwareHandler.getProjects(buildQuickSearchResult("1234", "5678")),
                Matchers.<GenericValue>containsInAnyOrder(project1, project2)
        );
    }

    @Test
    public void shouldNotThrowNumberFormatExceptionForMultipleInput()
    {
        final GenericValue project1 = createProject(10011L);
        final GenericValue project2 = createProject(10021L);
        createProject(10031L);

        when(mockPermissionManager.getProjects(Permissions.BROWSE, user)).thenReturn(ImmutableList.of(project1, project2));

        assertThat(
                (List<GenericValue>) projectAwareHandler.getProjects(buildQuickSearchResult("invalid", "input")),
                Matchers.<GenericValue>containsInAnyOrder(project1, project2)
        );
    }

    @Test
    public void shouldNotThrowNumberFormatExceptionForMultipleEmptyInputs()
    {
        final GenericValue project1 = createProject(10011L);
        final GenericValue project2 = createProject(10021L);
        createProject(10031L);

        when(mockPermissionManager.getProjects(Permissions.BROWSE, user)).thenReturn(ImmutableList.of(project1, project2));

        assertThat(
                (List<GenericValue>) projectAwareHandler.getProjects(buildQuickSearchResult("", "")),
                Matchers.<GenericValue>containsInAnyOrder(project1, project2)
        );
    }

    @Test
    public void shouldNotThrowNumberFormatExceptionForMultipleNullInputs()
    {
        final GenericValue project1 = createProject(10011L);
        final GenericValue project2 = createProject(10021L);
        createProject(10031L);

        when(mockPermissionManager.getProjects(Permissions.BROWSE, user)).thenReturn(ImmutableList.of(project1, project2));

        assertThat(
                (List<GenericValue>) projectAwareHandler.getProjects(buildQuickSearchResult(null, null)),
                Matchers.<GenericValue>containsInAnyOrder(project1, project2)
        );
    }

    private String idOf(final GenericValue project)
    {
        return project.get("id").toString();
    }

    private GenericValue createProject(final long id)
    {
        final GenericValue project = new MockGenericValue("project", id);
        when(mockProjectManager.getProject(id)).thenReturn(project);
        return project;
    }

    private void expectNumberFormatExceptionFor(final String input)
    {
        expectedException.expect(NumberFormatException.class);
        try
        {
            projectAwareHandler.getProjects(buildQuickSearchResult(input));
        }
        finally
        {
            verifyZeroInteractions(mockPermissionManager, mockAuthenticationContext);
        }
    }

    private QuickSearchResult buildQuickSearchResult(final String... projectIds)
    {
        final QuickSearchResult searchResult = new ModifiableQuickSearchResult("searchInput");
        for (final String projectId : projectIds)
        {
            searchResult.addSearchParameter("pid", projectId);
        }
        return searchResult;
    }
}
