package it.com.atlassian.jira.plugin.headernav;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class CommonHeader
{
    @Inject
    protected PageBinder pageBinder;

    @Inject
    protected PageElementFinder elementFinder;

    @ElementBy(cssSelector = "#header > .aui-header")
    protected PageElement header;

    public static CommonHeader visit(@Nonnull final JiraTestedProduct product)
    {
        final CommonHeader header = product.getPageBinder().bind(CommonHeader.class);
        assertTrue("Common header is not visible.", header.isAt().byDefaultTimeout());
        return header;
    }
    
    public TimedCondition isAt()
    {
        return header.timed().isPresent();
    }

    public boolean hasAppSwitcher()
    {
        return header.find(By.id("logo")).isPresent();
    }

    public boolean hasAppSwitcherMenu()
    {
        return header.find(By.id("app-switcher")).isPresent();
    }

    @Nullable
    public String getAppSwitcherMenuLinkTarget()
    {
        final PageElement appswitcher = header.find(By.id("logo")).find(By.tagName("a"));
        return appswitcher.getAttribute("href");
    }

    public boolean hasMainHeaderLinks()
    {
        return getMainHeaderListItems().size() > 0;
    }

    @Nonnull
    public List<String> getMainHeaderLinkIds()
    {
        return getAllNavLinkIds(getMainHeaderListItems());
    }

    public boolean hasCreateIssueButton()
    {
        return header.find(By.id("create_link")).isPresent();
    }

    public boolean hasQuickSearch()
    {
        return header.find(By.id("quicksearch")).isPresent();
    }

    public boolean hasHelpMenu()
    {
        return header.find(By.id("system-help-menu")).isPresent();
    }

    public boolean hasAdminMenu()
    {
        return header.find(By.id("system-admin-menu")).isPresent();
    }

    @Nullable
    public String getAdminMenuLinkTarget()
    {
        final PageElement adminMenuLink = header.find(By.id("system-admin-menu")).find(By.tagName("a"));
        return adminMenuLink.getAttribute("href");
    }

    public boolean adminMenuHasAdminLink(final String href)
    {
        List<PageElement> links = findAdminMenuContent().findAll(By.tagName("a"));
        return Iterables.any(links, new Predicate<PageElement>()
        {
            @Override
            public boolean apply(@Nullable PageElement pageElement)
            {
                return href.equals(pageElement.getAttribute("href"));
            }
        });
    }

    @Nonnull
    public List<String> getAdminMenuLinkIds()
    {
        final PageElement adminMenuContainer = findAdminMenuContent();
        return getAllNavLinkIds(adminMenuContainer);
    }

    private PageElement findAdminMenuContent()
    {
        return header.find(By.id("system-admin-menu-content"));
    }

    public boolean hasLoginButton()
    {
        return header.find(By.className("login-link")).isPresent();
    }

    public boolean hasUserOptionsMenu()
    {
        return header.find(By.id("user-options-content")).isPresent();
    }

    private List<PageElement> getMainHeaderListItems()
    {
        return header.find(By.className("aui-header-primary")).find(By.className("aui-nav")).findAll(By.tagName("li"));
    }

    private List<String> getAllNavLinkIds(@Nonnull final List<PageElement> mainHeaderLinksContainer)
    {
        List<String> results = new ArrayList<String>();
        for (PageElement container : mainHeaderLinksContainer)
        {
            results.addAll(getAllNavLinkIds(container));
        }
        return results;
    }

    @Nonnull
    private List<String> getAllNavLinkIds(@Nonnull final PageElement mainHeaderLinksContainer)
    {
        final List<PageElement> mainHeaderLinks = mainHeaderLinksContainer.findAll(By.cssSelector("a.aui-nav-link"));
        return Lists.newArrayList(Lists.transform(mainHeaderLinks, new Function<PageElement, String>()
        {
            @Override
            public String apply(@Nullable final PageElement navLink)
            {
                return navLink != null ? navLink.getAttribute("id") : null;
            }
        }));
    }
}
