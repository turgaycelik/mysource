package com.atlassian.jira.issue.search.quicksearch;

import com.atlassian.crowd.model.user.User;
import com.atlassian.jira.bc.project.component.MutableProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import junit.framework.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestComponentQuickSearchHandler
{

    public static final List<String> NO_WORDS = Collections.emptyList();

    @Test
    public void testComponentQuickSearchHandlerModifySearchResultSingleProject()
    {
        //set up the test environment (1 project to many components)
        final GenericValue project = new MockGenericValue("Project", ImmutableMap.of("id", 987L));

        final ProjectComponent component1 = getProjectComponent(101L, "c1 three", "c1 description", "lead", 1, project.getLong("id"));
        final ProjectComponent component2 = getProjectComponent(102L, "three c2 two", "c2 description", "lead", 2, project.getLong("id"));
        final ProjectComponent component3 = getProjectComponent(103L, "three two one c3", "c3 description", "lead", 3, project.getLong("id"));
        final List<ProjectComponent> availableComponents = ImmutableList.of(component1, component2, component3);

        //map all the components to a single project
        final Map<GenericValue, List<ProjectComponent>> projectToComponents = ImmutableMap.of(
                project, availableComponents
        );

        runCommonTestCases(projectToComponents, component1, component2, component3);
    }

    @Test
    public void testComponentQuickSearchHandlerModifySearchResultMultipleProjects()
    {
        //set up the test environment (2 projects to varying components)
        final GenericValue project1 = new MockGenericValue("Project", ImmutableMap.of("id", 987L));
        final GenericValue project2 = new MockGenericValue("Project", ImmutableMap.of("id", 988L));

        final ProjectComponent proj1comp1 = getProjectComponent(101L, "c1 three", "c1 description", "lead", 1, project1.getLong("id"));
        final ProjectComponent proj2comp1 = getProjectComponent(102L, "three c2 two", "c2 description", "lead", 2, project2.getLong("id"));
        final ProjectComponent proj2comp2 = getProjectComponent(103L, "three two one c3", "c3 description", "lead", 3, project2.getLong("id"));

        final List<ProjectComponent> project1Components = ImmutableList.of(proj1comp1);
        final List<ProjectComponent> project2Components = ImmutableList.of(proj2comp1, proj2comp2);

        //map the 3 components to 2 different projects
        final Map<GenericValue, List<ProjectComponent>> projectToComponents = ImmutableMap.of(
                project1, project1Components,
                project2, project2Components
        );

        runCommonTestCases(projectToComponents, proj1comp1, proj2comp1, proj2comp2);
    }

    private void runCommonTestCases(final Map<GenericValue, List<ProjectComponent>> projectToComponents, final ProjectComponent proj1comp1, final ProjectComponent proj2comp1, final ProjectComponent proj2comp2)
    {
        //search for no components (search input should not be changed)
        List<String> expectedComponentNames = null;
        testComponentQuickSearchHandlerModifyResults("pre mid suf", ImmutableList.of("pre", "mid", "suf"), projectToComponents, expectedComponentNames);

        //search for nothing (ie no components)
        expectedComponentNames = null;
        testComponentQuickSearchHandlerModifyResults("", NO_WORDS, projectToComponents, expectedComponentNames);

        //search for c1 directly
        expectedComponentNames = ImmutableList.of(proj1comp1.getName());
        testComponentQuickSearchHandlerModifyResults("c:c1", NO_WORDS, projectToComponents, expectedComponentNames);

        //search for c2 directly with some white space
        expectedComponentNames = ImmutableList.of(proj2comp1.getName());
        testComponentQuickSearchHandlerModifyResults(" c:c2 ", NO_WORDS, projectToComponents, expectedComponentNames);

        //search for c3 directly with extra words
        expectedComponentNames = ImmutableList.of(proj2comp2.getName());
        testComponentQuickSearchHandlerModifyResults("hello v:two world c:c3 def", ImmutableList.of("hello", "v:two", "world", "def"),
                projectToComponents, expectedComponentNames);

        //search for c3 using another unique keyword in c3 and with extra words at start
        expectedComponentNames = ImmutableList.of(proj2comp2.getName());
        testComponentQuickSearchHandlerModifyResults("prefix c:one", ImmutableList.of("prefix"), projectToComponents, expectedComponentNames);

        //search for 2 components with the common whole word "two"
        expectedComponentNames = ImmutableList.of(proj2comp1.getName(), proj2comp2.getName());
        testComponentQuickSearchHandlerModifyResults("c:two suffix", ImmutableList.of("suffix"), projectToComponents, expectedComponentNames);

        //search for 3 components with the common whole word "three"
        expectedComponentNames = ImmutableList.of(proj1comp1.getName(), proj2comp1.getName(), proj2comp2.getName());
        testComponentQuickSearchHandlerModifyResults("pre c:three suf", ImmutableList.of("pre", "suf"), projectToComponents, expectedComponentNames);
    }

    private void testComponentQuickSearchHandlerModifyResults(final String searchInput, final List<String> untouchedWords, final Map<GenericValue, List<ProjectComponent>> projectToComponents, final Collection<String> expectedComponentNames)
    {
        final QuickSearchResult quickSearchResult = new ModifiableQuickSearchResult(searchInput);

        final ProjectManager mockProjectManager = mock(ProjectManager.class);
        final PermissionManager mockPermissionManager = mock(PermissionManager.class);
        final ProjectComponentManager mockProjectComponentManager = mock(ProjectComponentManager.class);

        when(mockProjectComponentManager.findAllForProject(anyLong())).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable
            {
                final Long projectId = (Long) invocationOnMock.getArguments()[0];
                for (final GenericValue project : projectToComponents.keySet())
                {
                    if (projectId.equals(project.getLong("id")))
                    {
                        return (Collection) projectToComponents.get(project);
                    }
                }
                return null;
            }
        });

        //if single project
        if (projectToComponents.size() == 1)
        {
            final GenericValue project = Iterables.getOnlyElement(projectToComponents.keySet());
            //add the single project to the search result
            quickSearchResult.addSearchParameter("pid", project.getString("id"));
            if (expectedComponentNames != null)
            {
                when(mockProjectManager.getProject(project.getLong("id"))).thenReturn(project);
            }
        }
        else
        //else multiple project
        {
            //add each project to the search result
            for (final GenericValue project : projectToComponents.keySet())
            {
                quickSearchResult.addSearchParameter("pid", project.getString("id"));
            }
            if (expectedComponentNames != null)
            {
                when(mockPermissionManager.getProjects(eq(Permissions.BROWSE), (User) Mockito.isNull())).thenReturn(projectToComponents.keySet());
            }
        }

        // run the actual tested component
        final ComponentQuickSearchHandler quickSearchHandler = new ComponentQuickSearchHandler(mockProjectComponentManager,
                mockProjectManager, mockPermissionManager, getAuthenticationContext());
        quickSearchHandler.modifySearchResult(quickSearchResult);

        if (expectedComponentNames != null)
        {
            assertEquals(new HashSet(expectedComponentNames), new HashSet(quickSearchResult.getSearchParameters("component")));
            verify(mockProjectComponentManager, times(projectToComponents.size())).findAllForProject(anyLong());
        }
        else
        {
            Assert.assertNull(quickSearchResult.getSearchParameters("component"));
            verify(mockProjectComponentManager, never()).findAllForProject(anyLong());
        }

        //ensure that the other search words have not been removed/lost
        for (final String untouchedWord : untouchedWords)
        {
            assertThat(quickSearchResult.getSearchInput(), containsString(untouchedWord));
        }
    }

    //--------------------------------------------------------------------------------------------------- Helper Methods

    private ProjectComponent getProjectComponent(final Long id, final String name, final String description, final String lead, final long assigneeType, final Long projectId)
    {
        return new MutableProjectComponent(id, name, description, lead, assigneeType, projectId);
    }

    protected JiraAuthenticationContext getAuthenticationContext()
    {
        return new MockSimpleAuthenticationContext(null);
    }
}
