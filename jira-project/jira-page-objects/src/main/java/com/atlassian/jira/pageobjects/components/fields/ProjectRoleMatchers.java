package com.atlassian.jira.pageobjects.components.fields;

import com.atlassian.jira.pageobjects.pages.admin.roles.ViewProjectRolesPage;
import com.atlassian.jira.util.dbc.Assertions;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matcher for Project Role Rows
 *
 * @since v5.2
 */
public class ProjectRoleMatchers
{
    public static Matcher<ViewProjectRolesPage.ProjectRole> hasProjectRoleName(final String projectRoleName)
    {
        Assertions.notNull("projectRoleName", projectRoleName);
        return new TypeSafeMatcher<ViewProjectRolesPage.ProjectRole>()
        {
            @Override
            public boolean matchesSafely(ViewProjectRolesPage.ProjectRole projectRole)
            {
                return projectRole.getProjectRoleName().now().matches(projectRoleName);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("Issue list contains issue with projectRoleName ").appendValue(projectRoleName);
            }
        };
    }
}
