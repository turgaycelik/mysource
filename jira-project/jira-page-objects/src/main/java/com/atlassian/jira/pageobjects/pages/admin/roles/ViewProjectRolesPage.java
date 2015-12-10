package com.atlassian.jira.pageobjects.pages.admin.roles;

import static com.atlassian.jira.pageobjects.components.fields.ProjectRoleMatchers.hasProjectRoleName;
import static com.atlassian.jira.pageobjects.framework.elements.PageElements.transformTimed;
import static com.atlassian.pageobjects.elements.query.Conditions.forMatcher;

import javax.inject.Inject;

import org.openqa.selenium.By;

import com.atlassian.jira.functest.framework.matchers.IterableMatchers;
import com.atlassian.jira.pageobjects.framework.elements.ExtendedElementFinder;
import com.atlassian.jira.pageobjects.framework.elements.PageElements;
import com.atlassian.jira.pageobjects.pages.AbstractJiraAdminPage;
import com.atlassian.jira.util.lang.GuavaPredicates;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.DataAttributeFinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.google.common.collect.Iterables;

/**
 * Page object representing the View Project roles page.
 *
 * @since v5.1
 */
public class ViewProjectRolesPage extends AbstractJiraAdminPage
{

    @Inject protected PageBinder pageBinder;
    @Inject protected ExtendedElementFinder extendedFinder;

    @ElementBy (id = "role_submit")
    protected PageElement roleSubmitLink;
    @ElementBy(id = "project_roles")
    protected PageElement projectRolesContainer;

    @Override
    public String linkId()
    {
        // todo check which one is required, there are two ids "project_role_browser" (dropdown) and "as-count-roles" (admin home page)
        // possibly need to rename it in production code
        return "project_role_browser";
    }

    @Override
    public TimedCondition isAt()
    {
        return roleSubmitLink.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return "/secure/project/ViewProjectRoles.jspa";
    }

    public TimedQuery<Iterable<ProjectRole>> getProjectRoles()
    {
        return transformTimed(timeouts,
                pageBinder,
                extendedFinder.within(projectRolesContainer.find(By.tagName("tbody"))).newQuery(By.tagName("tr"))
                        .filter(PageElements.hasClass("project-role-row"))
                        .supplier(),
                ProjectRole.class);
    }

    public TimedCondition hasProjectRole(String projectRoleName)
    {
        return forMatcher(getProjectRoles(), IterableMatchers.hasItemThat(hasProjectRoleName(projectRoleName)));
    }

    /**
     * Returns a ProjectRole given the page element id (not the project role numeric id)
     * @param projectRoleName
     * @return
     */
    public ProjectRole getProjectRole(String projectRoleName)
    {
        Poller.waitUntilTrue(hasProjectRole(projectRoleName));
        return Iterables.find(getProjectRoles().now(), GuavaPredicates.forMatcher(hasProjectRoleName(projectRoleName)));
    }

    public static class ProjectRole
    {
        @Inject PageBinder pageBinder;

        protected final PageElement rowElement;

        public ProjectRole(PageElement rowElement)
        {
            this.rowElement = rowElement;
        }

        public ViewDefaultProjectRoleActorsPage manageDefaultMembers()
        {
            final String projectRoleId = getProjectRoleId().now();
            rowElement.find(By.id("manage_" + getProjectRoleName().now())).click();
            return pageBinder.bind(ViewDefaultProjectRoleActorsPage.class, projectRoleId);
        }

        public TimedQuery<String> getProjectRoleName()
        {
            return DataAttributeFinder.query(rowElement).timed().getDataAttribute("row-for");
        }

        /**
         * Returns the numeric id of the project role (i.e. not the id with the project name)
         * @return
         */
        public TimedQuery<String> getProjectRoleId()
        {
            return DataAttributeFinder.query(rowElement).timed().getDataAttribute("project-role-id");
        }
    }
}
