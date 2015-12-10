package com.atlassian.jira.pageobjects.components;

import javax.inject.Inject;

import com.atlassian.jira.pageobjects.components.fields.AutoComplete;
import com.atlassian.jira.pageobjects.components.fields.QueryableDropdownSelect;
import com.atlassian.jira.pageobjects.components.menu.IssuesMenu;
import com.atlassian.jira.pageobjects.components.menu.ProjectsMenu;
import com.atlassian.jira.pageobjects.dialogs.quickedit.CreateIssueDialog;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.component.Header;
import com.atlassian.pageobjects.component.WebSudoBanner;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;

import org.openqa.selenium.By;

/**
 * Representation of the JIRA header
 *
 * @since v4.4
 */
public class JiraHeader extends JiraCommonHeader implements Header
{
    private static final String ADMIN_QUICK_NAV_QUERYABLE_CONTAINER = "admin-quick-nav-queryable-container";
    private static final String HEADER_ADMINISTRATION_SUGGESTIONS = "header-administration-suggestions";

    @Inject
    protected PageElementFinder elementFinder;

    @Inject
    PageBinder pageBinder;

    @ElementBy (id = "header")
    private PageElement headerElement;

    @ElementBy (id = "header-details-user-fullname")
    private PageElement headerUserFullName;

    @ElementBy (id = "log_out")
    private PageElement logoutLink;

    private String userName;

    @Init
    public void init()
    {
        userName = headerUserFullName.isPresent() ? headerUserFullName.getText() : null;
    }

    /**
     * Gets admin quick search. If it isn't present will return null
     *
     * @return admin quick search
     */
    public AutoComplete getAdminQuickSearch()
    {
        return pageBinder.bind(QueryableDropdownSelect.class, By.id(ADMIN_QUICK_NAV_QUERYABLE_CONTAINER),
                By.id(HEADER_ADMINISTRATION_SUGGESTIONS));
    }

    public IssuesMenu getIssuesMenu()
    {
        return pageBinder.bind(IssuesMenu.class);
    }

    public ProjectsMenu getProjectsMenu()
    {
        return pageBinder.bind(ProjectsMenu.class);
    }


    public CreateIssueDialog createIssue()
    {
        elementFinder.find(By.id("create_link")).click();
        return pageBinder.bind(CreateIssueDialog.class, CreateIssueDialog.Type.ISSUE);
    }

    public boolean hasCreateLink()
    {
        return elementFinder.find(By.id("create_link")).isPresent();
    }

    public boolean isLoggedIn()
    {
        return userName != null;
    }

    @Override
    public <M extends Page> M logout(Class<M> nextPage)
    {
        logoutLink.click();
        return pageBinder.bind(nextPage);
    }

    @Override
    public WebSudoBanner getWebSudoBanner()
    {
        return pageBinder.bind(WebSudoBanner.class);
    }

    public boolean isAdmin()
    {
        return isLoggedIn() && headerElement.find(By.id("admin_link")).isPresent();
    }

    public String getCurrentUserFullName()
    {
        return userName;
    }
}
