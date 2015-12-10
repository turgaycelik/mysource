package com.atlassian.jira.jql.values;

import java.util.List;
import java.util.Locale;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.MockProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestComponentClauseValuesGenerator
{
    private static final ApplicationUser ANONYMOUS = null;

    @Mock private ProjectComponentManager projectComponentManager;
    @Mock private ProjectManager projectManager;
    @Mock private PermissionManager permissionManager;

    private ComponentClauseValuesGenerator valuesGenerator;



    @Before
    public void setUp()
    {
        valuesGenerator = new ComponentClauseValuesGenerator(projectComponentManager, projectManager, permissionManager)
        {
            @Override
            Locale getLocale(final User searcher)
            {
                return Locale.ENGLISH;
            }
        };
    }

    @After
    public void tearDown()
    {
        projectComponentManager = null;
        projectManager = null;
        permissionManager = null;
        valuesGenerator = null;
    }

    private void givenProjectComponents()
    {
        final ProjectComponent component1 = new MockProjectComponent(1L, "Aa comp", 1L);
        final ProjectComponent component2 = new MockProjectComponent(2L, "A comp", 1L);
        final ProjectComponent component3 = new MockProjectComponent(3L, "B comp", 1L);
        final ProjectComponent component4 = new MockProjectComponent(4L, "C comp", 2L);
        final ProjectComponent component5 = new MockProjectComponent(5L, "D comp", 1L);
        when(projectComponentManager.findAll()).thenReturn(ImmutableList.of(component5, component4, component3, component2, component1));
    }

    private void givenProject(final Long id, final String key, final String name, final boolean canBrowse)
    {
        final MockProject project = new MockProject(id, key, name);
        when(projectManager.getProjectObj(id)).thenReturn(project);
        if (canBrowse)
        {
            when(permissionManager.hasPermission(Permissions.BROWSE, project, ANONYMOUS)).thenReturn(true);
        }
    }


    @Test
    public void testGetPossibleValuesHappyPath() throws Exception
    {
        givenProjectComponents();
        givenProject(1L, "TST", "Test", true);
        givenProject(2L, "ANA", "Another", true);

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "comp", "", 10);
        assertThat(possibleValues, hasResults("Aa comp", "A comp", "B comp", "C comp", "D comp"));
    }

    @Test
    public void testGetPossibleValuesMatchFullValue() throws Exception
    {
        givenProjectComponents();
        givenProject(1L, "TST", "Test", true);

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "comp", "Aa comp", 10);
        assertThat(possibleValues, hasResults("Aa comp"));
    }

    @Test
    public void testGetPossibleValuesExactMatchWithOthers() throws Exception
    {
        // Use a custom component2
        final ProjectComponent component1 = new MockProjectComponent(1L, "Aa comp", 1L);
        final ProjectComponent component2 = new MockProjectComponent(2L, "Aa comp blah", 1L);
        final ProjectComponent component3 = new MockProjectComponent(3L, "B comp", 1L);
        final ProjectComponent component4 = new MockProjectComponent(4L, "C comp", 2L);
        final ProjectComponent component5 = new MockProjectComponent(5L, "D comp", 1L);
        when(projectComponentManager.findAll()).thenReturn(ImmutableList.of(component5, component4, component3, component2, component1));

        givenProject(1L, "TST", "Test", true);

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "comp", "Aa comp", 10);
        assertThat(possibleValues, hasResults("Aa comp", "Aa comp blah"));
    }

    @Test
    public void testGetPossibleValuesNoMatching() throws Exception
    {
        givenProjectComponents();
        givenProject(1L, "TST", "Test", true);
        givenProject(2L, "ANA", "Another", true);

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "comp", "F", 10);
        assertThat(possibleValues, hasResults());
    }

    @Test
    public void testGetPossibleValuesSomeMatching() throws Exception
    {
        givenProjectComponents();
        givenProject(1L, "TST", "Test", true);
        givenProject(2L, "ANA", "Another", true);

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "comp", "a", 10);
        assertThat(possibleValues, hasResults("Aa comp", "A comp"));
    }

    @Test
    public void testGetPossibleValuesHitMax() throws Exception
    {
        givenProjectComponents();
        givenProject(1L, "TST", "Test", true);
        givenProject(2L, "ANA", "Another", true);

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "comp", "", 4);
        assertThat(possibleValues, hasResults("Aa comp", "A comp", "B comp", "C comp"));
    }

    @Test
    public void testGetPossibleValuesCompoentReferencesProjectDoesNotExist() throws Exception
    {
        givenProjectComponents();
        givenProject(2L, "ANA", "Another", true);

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "comp", "", 10);
        assertThat(possibleValues, hasResults("C comp"));
    }

    @Test
    public void testGetPossibleValuesNoPermForProject1() throws Exception
    {
        givenProjectComponents();
        givenProject(1L, "TST", "Test", true);
        givenProject(2L, "ANA", "Another", false);

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "comp", "", 10);
        assertThat(possibleValues, hasResults("Aa comp", "A comp", "B comp", "D comp"));
    }

    @Test
    public void testGetPossibleValuesNoPermForProject2() throws Exception
    {
        givenProjectComponents();
        givenProject(1L, "TST", "Test", false);
        givenProject(2L, "ANA", "Another", true);

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "comp", "", 10);
        assertThat(possibleValues, hasResults("C comp"));
    }



    private static Matcher<ClauseValuesGenerator.Results> hasResults(final String... expectedValues)
    {
        return new HasResultsMatcher(expectedValues);
    }

    static class HasResultsMatcher extends TypeSafeMatcher<ClauseValuesGenerator.Results>
    {
        private final List<ClauseValuesGenerator.Result> expectedResults;
        private final Matcher<?> delegateMatcher;

        HasResultsMatcher(final String... expectedValues)
        {
            if (expectedValues.length == 0)
            {
                expectedResults = ImmutableList.of();
                delegateMatcher = empty();
            }
            else
            {
                expectedResults = Lists.transform(asList(expectedValues), TO_RESULT);
                delegateMatcher = contains(Iterables.toArray(expectedResults, ClauseValuesGenerator.Result.class));
            }
        }

        @Override
        protected boolean matchesSafely(final ClauseValuesGenerator.Results results)
        {
            return delegateMatcher.matches(results.getResults());
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText("results containing ").appendValue(expectedResults);
        }
    }

    private static Function<String,ClauseValuesGenerator.Result> TO_RESULT = new Function<String,ClauseValuesGenerator.Result>()
    {
        @Override
        public ClauseValuesGenerator.Result apply(final String value)
        {
            return new ClauseValuesGenerator.Result(value);
        }
    };
}
