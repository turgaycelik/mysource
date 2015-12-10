package com.atlassian.jira.functest.framework;

import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermissionUtils;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.HttpUnitDialog;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Node;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;

/**
 * Implementation of Dashboard.
 *
 * @since v3.13
 */
class DashboardImpl extends AbstractFuncTestUtil implements Dashboard
{
    private static final String CONFIGURE_OFF_ID = "configure_off";
    private static final String CONFIGURE_ON_ID = "configure_on";

    private final HtmlPage page;
    private final Navigation navigation;

    DashboardImpl(WebTester tester, JIRAEnvironmentData environmentData, Navigation navigation)
    {
        super(tester, environmentData, 2);
        this.navigation = navigation;
        page = new HtmlPage(tester);
    }

    public Dashboard enableConfigureMode()
    {
        if (tester.getDialog().isLinkPresent(CONFIGURE_OFF_ID))
        {
            // we are already on
            return this;
        }
        if (tester.getDialog().isLinkPresent(CONFIGURE_ON_ID))
        {
            // we are not on but we have the ON button available
            tester.clickLink(CONFIGURE_ON_ID);
            return this;
        }
        return navigateTo();
    }

    public Dashboard disableConfigureMode()
    {
        HttpUnitDialog d = tester.getDialog();
        if (d.isLinkPresent(CONFIGURE_ON_ID))
        {
            // we are already off since
            return this;
        }
        tester.clickLink(CONFIGURE_OFF_ID);
        return this;
    }

    public Dashboard navigateTo()
    {
        tester.gotoPage("secure/Dashboard.jspa");
        return this;
    }

    public Dashboard navigateTo(final long pageId)
    {
        tester.gotoPage("secure/Dashboard.jspa?selectPageId=" + pageId);
        return this;
    }

    public Dashboard navigateToFavourites()
    {
        tester.gotoPage("secure/ConfigurePortalPages!default.jspa?view=favourite");
        return this;
    }

    public Dashboard navigateToMy()
    {
        tester.gotoPage("secure/ConfigurePortalPages!default.jspa?view=my");
        return this;
    }

    public Dashboard navigateToPopular()
    {
        tester.gotoPage("secure/ConfigurePortalPages!default.jspa?view=popular");
        return this;
    }

    public Dashboard navigateToSearch()
    {
        tester.gotoPage("secure/ConfigurePortalPages!default.jspa?view=search");
        return this;
    }

    public Dashboard favouriteDashboard(final long id)
    {
        tester.gotoPage(page.addXsrfToken("/secure/AddFavourite.jspa?entityType=PortalPage&entityId=" + id));
        return this;
    }

    public Dashboard unFavouriteDashboard(final long id)
    {
        tester.gotoPage(page.addXsrfToken("/secure/RemoveFavourite.jspa?entityType=PortalPage&entityId=" + id));
        return this;
    }

    public Dashboard resetUserSessionState()
    {
        tester.gotoPage("/secure/Dashboard.jspa?resetPortal=true");
        return this;
    }

    public Dashboard resetToDefault()
    {
        tester.gotoPage(page.addXsrfToken("/secure/RestoreDefaultDashboard.jspa?confirm=true"));
        return this;
    }

    public Long getDashboardPageId(final String dashboardPageName, final Locator pagesLocator)
    {
        XPathLocator links = new XPathLocator(pagesLocator.getNode(), "tbody/tr//a[text() = '" + dashboardPageName + "']/@href");
        Node link = links.getNode();
        if (link != null)
        {
            String href = link.getNodeValue();
            final String str = "selectPageId=";
            int equalsIndex = href.lastIndexOf(str);
            if (equalsIndex != -1)
            {
                try
                {
                    return Long.valueOf(href.substring(equalsIndex + str.length()));
                }
                catch (NumberFormatException ignored)
                {
                }
            }
        }
        return null;
    }

    public Dashboard navigateToFullConfigure(final Long dashboardPageId)
    {
        tester.gotoPage("secure/DashboardConfig!default.jspa?selectPageId=" + dashboardPageId);
        return this;
    }

    public Dashboard navigateToDefaultFullConfigure()
    {
        tester.gotoPage("secure/admin/jira/EditDefaultDashboard!default.jspa");
        return this;
    }

    public Dashboard addPage(final SharedEntityInfo info, final Long cloneId)
    {
        final Set permissions = info.getSharingPermissions();
        if (info.isFavourite() && (permissions == null || permissions.isEmpty()))
        {
            tester.gotoPage("secure/AddPortalPage!default.jspa");
            tester.setFormElement("portalPageName", info.getName());
            if (!StringUtils.isBlank(info.getDescription()))
            {
                tester.setFormElement("portalPageDescription", info.getDescription());
            }
            if (cloneId != null)
            {
                tester.getDialog().getForm().setParameter("clonePageId", String.valueOf(cloneId));
            }
            tester.submit("add_submit");
        }
        else
        {
            //This is a hack to get around the fact that JWebUnit does not support
            //setting hidden fields. Ahhhh.....
            addUsingPut(info, cloneId);
        }
        return this;
    }

    public Dashboard editPage(final SharedEntityInfo info)
    {
        final Set permissions = info.getSharingPermissions();
        if (info.isFavourite() && (permissions == null || permissions.isEmpty()))
        {
            tester.gotoPage("secure/EditPortalPage!default.jspa?pageId=" + info.getId());
            tester.setFormElement("portalPageName", info.getName());
            if (!StringUtils.isBlank(info.getDescription()))
            {
                tester.setFormElement("portalPageDescription", info.getDescription());
            }
            tester.submit("update_submit");
        }
        else
        {
            //This is a hack to get around the fact that JWebUnit does not support
            //setting hidden fields. Ahhhh.....
            editUsingPut(info);
        }
        return this;
    }

    /**
     * Adds the Dashboard directly using a GET. This gets around the problem where JWebUnit cannot change hidden
     * fields.
     *
     * @param info    the DashboardInfo in play
     * @param cloneId the id of the page to clone. Can be null to clone blank.
     */
    private void addUsingPut(final SharedEntityInfo info, final Long cloneId)
    {
        navigateToMy(); // make sure atl_token is current
        tester.gotoPage(page.addXsrfToken(createAddUrl(info, cloneId)));
    }

    private String createAddUrl(final SharedEntityInfo info, final Long cloneId)
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("secure/AddPortalPage.jspa?submit=Add");
        if (!StringUtils.isBlank(info.getName()))
        {
            buffer.append("&portalPageName=").append(encode(info.getName()));
        }
        if (!StringUtils.isBlank(info.getDescription()))
        {
            buffer.append("&portalPageDescription=").append(encode(info.getDescription()));
        }
        // we will use the blank if not set
        if (cloneId != null)
        {
            buffer.append("&clonePageId=").append(cloneId);
        }
        final Set permissions = info.getSharingPermissions();
        if (permissions != null)
        {
            buffer.append("&shareValues=").append(encode(TestSharingPermissionUtils.createJsonString(permissions)));
        }
        buffer.append("&favourite=").append(String.valueOf(info.isFavourite()));

        return buffer.toString();
    }

    /**
     * Edits the Dashboard directly using a GET. This gets around the problem where JWebUnit cannot change hidden
     * fields.
     *
     * @param info the DashboardInfo in play
     */
    private void editUsingPut(final SharedEntityInfo info)
    {
        tester.gotoPage(page.addXsrfToken(createEditUrl(info)));
    }

    private String createEditUrl(final SharedEntityInfo info)
    {

        StringBuilder buffer = new StringBuilder();
        buffer.append("secure/EditPortalPage.jspa?submit=Update&pageId=").append(info.getId());
        if (!StringUtils.isBlank(info.getName()))
        {
            buffer.append("&portalPageName=").append(encode(info.getName()));
        }
        if (!StringUtils.isBlank(info.getDescription()))
        {
            buffer.append("&portalPageDescription=").append(encode(info.getDescription()));
        }
        final Set permissions = info.getSharingPermissions();
        if (permissions != null)
        {
            buffer.append("&shareValues=").append(encode(TestSharingPermissionUtils.createJsonString(permissions)));
        }
        buffer.append("&favourite=").append(String.valueOf(info.isFavourite()));
        return buffer.toString();
    }

    /**
     * HTML encode the argument.
     *
     * @param data string to encode.
     * @return the encoded string.
     */
    private static String encode(String data)
    {
        try
        {
            return URLEncoder.encode(data, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }
}
