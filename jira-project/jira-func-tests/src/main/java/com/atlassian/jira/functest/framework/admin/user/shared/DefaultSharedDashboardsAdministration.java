package com.atlassian.jira.functest.framework.admin.user.shared;

import com.atlassian.jira.functest.framework.LocatorFactory;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.parser.dashboard.DashboardItem;
import com.atlassian.jira.functest.framework.parser.dashboard.DashboardTableParser;
import com.atlassian.jira.functest.framework.parser.filter.FilterItem;
import com.atlassian.jira.functest.framework.parser.filter.FilterTableParser;
import com.atlassian.jira.functest.framework.util.form.FormParameterUtil;
import net.sourceforge.jwebunit.WebTester;

import java.util.List;

/**
 *
 * @since v4.4.1
 */
public class DefaultSharedDashboardsAdministration implements SharedDashboardsAdministration
{
    private static final String SHARED_FILTERS_ADMINISTRATION_LINK_ID = "shared_dashboards";
    private static final String DELETE_PORTAL_PAGE = "delete-portal-page";
    private final WebTester tester;
    private final Navigation navigation;
    private final LocatorFactory locators;

    public DefaultSharedDashboardsAdministration(final WebTester tester, final Navigation navigation,
            final LocatorFactory locators)
    {
        this.tester = tester;
        this.navigation = navigation;
        this.locators = locators;
    }

    @Override
    public SharedDashboardsAdministration goTo()
    {
        navigation.gotoAdminSection(SHARED_FILTERS_ADMINISTRATION_LINK_ID);
        return this;
    }

    @Override
    public SharedDashboardsAdministration search(final String searchText, final String owner)
    {
        tester.setWorkingForm("search-dashboards-form");
        tester.setFormElement("searchName", searchText);
        tester.setFormElement("searchOwnerUserName", owner);
        tester.submit();
        return this;
    }

    @Override
    public SharedDashboardsAdministration searchAll()
    {
        return search("","");
    }

    @Override
    public Dashboards dashboards()
    {
        return new DefaultDashboards(locators);
    }

    @Override
    public SharedDashboardsAdministration deleteDashboard(long dashboardId)
    {
        tester.clickLink(getDeleteLinkIdForDashboard(dashboardId));
        tester.setWorkingForm(DELETE_PORTAL_PAGE);
        tester.submit();
        return this;
    }

    public  SharedDashboardsAdministration changeDashboardOwner(long dashboardId, String newOwner)
    {
        tester.clickLink(getChangeOwnerLinkIdForDashboard(dashboardId));
        FormParameterUtil formParameterUtil = new FormParameterUtil(tester, getChangeOwnerFormForDashboardId(dashboardId), "ChangeOwner");
        formParameterUtil.addOptionToHtmlSelect("owner", new String[] { newOwner });
        formParameterUtil.setFormElement("owner", newOwner);
        formParameterUtil.submitForm();
        // using FormParameterUtil has the unfortunate side effect of buggering up the tester dialog state
        // so we need to reset the tester - this means having to reload the page.
        tester.reset();
        return goTo().searchAll();
    }

    private String getDeleteLinkIdForDashboard(long dashboardId)
    {
        return "delete_"+dashboardId;
    }

    private String getChangeOwnerLinkIdForDashboard(long dashboardId)
    {
        return "change_owner_"+dashboardId;
    }


    private String getDeleteConfirmationFormForDashboardId(long dashboardId)
    {
        return DELETE_PORTAL_PAGE +"-" + dashboardId;
    }

    private String getChangeOwnerFormForDashboardId(long dashboardId)
    {
        return "change-owner-form-"+dashboardId;
    }

    /**
     *
     * @since v4.4.1
     */
    public static class DefaultDashboards implements Dashboards
    {
        private final LocatorFactory locators;

        public DefaultDashboards(final LocatorFactory locators)
        {
            this.locators = locators;
        }

        @Override
        public List<DashboardItem> list()
        {
            return new DashboardTableParser(locators).parse("pp_browse");
        }

    }
}
