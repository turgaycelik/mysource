package com.atlassian.jira.pageobjects.pages.admin.roles;

import javax.annotation.Nullable;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.openqa.selenium.By;

/**
 * Page object representing the Edit Members for Project Role page
 *
 * @since v5.2
 */
public class ViewDefaultProjectRoleActorsPage extends AbstractJiraPage
{
    private static final String URI = "/secure/project/ViewDefaultProjectRoleActors.jspa";
    private static Function<PageElement, String> INNER_TEXT = new Function<PageElement, String>()
    {
        @Override
        public String apply(@Nullable final PageElement pageElement)
        {
            return pageElement == null ? null : pageElement.getText();
        }
    };

    private final String roleId;

    @ElementBy (id = "role_actors")
    protected PageElement rowsContainer;

    public ViewDefaultProjectRoleActorsPage(String pageId)
    {
        this.roleId = pageId;
    }

    @Override
    public TimedCondition isAt()
    {
        return rowsContainer.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return URI + "?id=" + roleId;
    }

    protected <T> T editActors(Class<T> classType, String linkId)
    {
        rowsContainer.find(By.id(linkId)).click();
        return pageBinder.bind(classType, roleId);
    }

    public UserRoleActorActionPage editUserActors()
    {
        return editActors(UserRoleActorActionPage.class, String.format("edit_%s_atlassian-user-role-actor", roleId));
    }

    public GroupRoleActorActionPage editGroupActors()
    {
        return editActors(GroupRoleActorActionPage.class, String.format("edit_%s_atlassian-group-role-actor", roleId));
    }

    public Iterable<String> getDefaultUserRoleActors()
    {
        return Iterables.transform(Iterables.filter(spanElementsInTable(), idSuffix("-user-role-actor")), INNER_TEXT);
    }

    public Iterable<String> getDefaultGroupRoleActors()
    {
        return Iterables.transform(Iterables.filter(spanElementsInTable(), idSuffix("-group-role-actor")), INNER_TEXT);
    }

    private Iterable<PageElement> spanElementsInTable()
    {
        return rowsContainer.findAll(By.tagName("span"));
    }

    private Predicate<PageElement> idSuffix(final String suffix)
    {
        return new Predicate<PageElement>()
        {
            @Override
            public boolean apply(@Nullable final PageElement pageElement)
            {
                return pageElement != null && pageElement.getAttribute("id").endsWith(suffix);
            }
        };
    }
}
