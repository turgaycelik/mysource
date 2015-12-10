package com.atlassian.jira.pageobjects.gadgets;

import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import it.com.atlassian.gadgets.pages.AddGadgetDialog;
import it.com.atlassian.gadgets.pages.DashboardToolsMenu;
import it.com.atlassian.gadgets.pages.Gadget;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.List;

import static it.com.atlassian.gadgets.pages.util.DashboardGadgets.findGadgetIdByTitle;
import static it.com.atlassian.gadgets.pages.util.DashboardGadgets.hasGadgetWithId;

/**
 * The container of gadgets on the dashboard page
 *
 * @see com.atlassian.jira.pageobjects.pages.DashboardPage
 * @since v5.1
 */
public class GadgetContainer
{
    @Inject
    protected PageBinder pageBinder;

    @ElementBy (id = "add-gadget")
    protected PageElement addDialogLink;

    @ElementBy(id = "dashboard")
    protected PageElement dashboard;

    protected PageElement getTabContainer()
    {
        final List<PageElement> tabContainers = getTabContainers();
        if (tabContainers.size() != 1)
        {
            throw new IllegalStateException("Expected exactly one tab container '.vertical.tabs' on the page but was <"
                    + tabContainers.size() + ">");
        }
        return tabContainers.get(0);
    }

    protected List<PageElement> getTabContainers()
    {
        return dashboard.findAll(By.cssSelector(".vertical.tabs"));
    }

    public boolean hasTabs()
    {
        return getTabContainers().size() > 0;
    }

    public boolean canAddGadget()
    {
        return addDialogLink.isPresent();
    }

    public AddGadgetDialog openAddGadgetDialog()
    {
        if (!canAddGadget())
        {
            throw new IllegalStateException("Cannot add gadgets");
        }
        addDialogLink.click();
        return pageBinder.bind(AddGadgetDialog.class);
    }

    /**
     * Login gadget is always on the Dashboard if not logged in.
     *
     * @return Login gadget
     * @throws IllegalStateException if an user is logged in
     */
    public LoginGadget getLoginGadget()
    {
        if (!hasGaddget(LoginGadget.ID))
        {
            throw new IllegalStateException("Login gadget not available, there is probably a logged in user, or you're on"
                + " a wrong page");
        }
        return get(LoginGadget.class, LoginGadget.ID);
    }

    public boolean hasGaddget(String gadgetId)
    {
        return hasGadgetWithId(dashboard, gadgetId);
    }

    public Gadget getGadget(String gadgetId)
    {
        return pageBinder.bind(Gadget.class, gadgetId);
    }

    public <G extends Gadget> G getGadget(Class<G> gadgetClass, String gadgetId)
    {
        return pageBinder.bind(gadgetClass, gadgetId);
    }

    /**
     * Equivalent to {@link #getGadget(String)}.
     *
     * @param gadgetId gadget ID
     * @return gadget corresponding to the ID.
     */
    public Gadget get(String gadgetId)
    {
        return getGadget(gadgetId);
    }

    /**
     * Equivalent to {@link #getGadget(Class, String)}.
     *
     * @param gadgetId gadget ID
     * @param gadgetClass expected gadget type
     * @return gadget corresponding to the ID.
     */
    public <G extends Gadget> G get(Class<G> gadgetClass, String gadgetId)
    {
        return getGadget(gadgetClass, gadgetId);
    }

    /**
     * Get gadget by it's title as visible on the dashboard.
     *
     * @param gadgetTitle title of the gadget
     * @return gadget
     * @throws IllegalArgumentException if gadget with given title does not exist
     */
    public Gadget getGadgetByTitle(String gadgetTitle)
    {
        return getGadget(findGadgetIdByTitle(dashboard, gadgetTitle));
    }

    /**
     * Get gadget of given type by it's title as visible on the dashboard.
     *
     * @param gadgetClass expected gadget type
     * @param gadgetTitle title of the gadget
     * @return gadget of given type with given title
     * @throws IllegalArgumentException if gadget with given title does not exist
     */
    public <G extends Gadget> G getGadgetByTitle(Class<G> gadgetClass, String gadgetTitle)
    {
        return getGadget(gadgetClass, findGadgetIdByTitle(dashboard, gadgetTitle));
    }

    /**
     * Equivalent to {@link #getGadgetByTitle(String)}}.
     *
     * @param gadgetTitle title of the gadget
     * @return gadget with given title.
     * @throws IllegalArgumentException if gadget with given title does not exist
     */
    public Gadget getByTitle(String gadgetTitle)
    {
        return getGadgetByTitle(gadgetTitle);
    }

    /**
     * Equivalent to {@link #getGadgetByTitle(Class, String)}.
     *
     * @param gadgetClass expected gadget type
     * @param gadgetTitle gadget title
     * @return gadget with given title.
     */
    public <G extends Gadget> G getByTitle(Class<G> gadgetClass, String gadgetTitle)
    {
        return getGadgetByTitle(gadgetClass, gadgetTitle);
    }


    public DashboardToolsMenu getDashboardTools()
    {
        return pageBinder.bind(DashboardToolsMenu.class);
    }

    public DashboardToolsMenu openDashboardTools()
    {
        return getDashboardTools().open();
    }

    /**
     * Switches dashboard using the side tabs. This will only work if {@link #hasTabs()}
     * returns <code>true</code>.
     *
     * @param dashboardName name of the dashboard to switch to
     * @return this dashboard page instance
     * @throws IllegalStateException if there is no tabs (e.g. user not logged in or there is only one
     * dashboard configured for the user), or there is no dashboard with given name
     */
    public DashboardPage switchDashboard(String dashboardName)
    {
        final List<PageElement> availableViews = getTabContainer().findAll(By.tagName("span"));
        for (PageElement tab : availableViews)
        {
            final String tabName = tab.getAttribute("title");
            if (tabName.equals(dashboardName))
            {
                tab.click();
                return pageBinder.bind(DashboardPage.class);
            }
        }
        throw new IllegalStateException("No dashboard with name '" + dashboardName + "'");
    }
}
