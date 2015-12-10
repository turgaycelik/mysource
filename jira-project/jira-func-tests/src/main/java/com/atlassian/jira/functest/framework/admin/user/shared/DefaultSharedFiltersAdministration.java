package com.atlassian.jira.functest.framework.admin.user.shared;

import com.atlassian.jira.functest.framework.LocatorFactory;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.parser.filter.FilterItem;
import com.atlassian.jira.functest.framework.parser.filter.FilterTableParser;
import com.atlassian.jira.functest.framework.util.form.FormParameterUtil;
import net.sourceforge.jwebunit.WebTester;

import java.util.List;

/**
 *
 * @since v4.4.1
 */
public class DefaultSharedFiltersAdministration implements SharedFiltersAdministration
{
    private static final String SHARED_FILTERS_ADMINISTRATION_LINK_ID = "shared_filters";
    private final WebTester tester;
    private final Navigation navigation;
    private final LocatorFactory locators;

    public DefaultSharedFiltersAdministration(final WebTester tester, final Navigation navigation,
            final LocatorFactory locators)
    {
        this.tester = tester;
        this.navigation = navigation;
        this.locators = locators;
    }

    @Override
    public SharedFiltersAdministration goTo()
    {
        navigation.gotoAdminSection(SHARED_FILTERS_ADMINISTRATION_LINK_ID);
        return this;
    }

    @Override
    public SharedFiltersAdministration search(final String searchText, final String owner)
    {
        tester.setWorkingForm("search-filters-form");
        tester.setFormElement("searchName", searchText);
        tester.setFormElement("searchOwnerUserName", owner);
        tester.submit();
        return this;
    }

    @Override
    public SharedFiltersAdministration searchAll()
    {
        return search("","");
    }

    @Override
    public Filters filters()
    {
        return new DefaultFilters(locators);
    }

    @Override
    public SharedFiltersAdministration deleteFilter(long filterId)
    {
        tester.clickLink(getDeleteLinkIdForFilter(filterId));
        tester.setWorkingForm(getDeleteConfirmationFormForFilterId(filterId));
        tester.submit();
        return this;
    }

    public  SharedFiltersAdministration changeFilterOwner(long filterId, String newOwner)
    {
        tester.clickLink(getChangeOwnerLinkIdForFilter(filterId));
        FormParameterUtil formParameterUtil = new FormParameterUtil(tester, getChangeOwnerFormForFilterId(filterId), "ChangeOwner");
        formParameterUtil.addOptionToHtmlSelect("owner", new String[] { newOwner });
        formParameterUtil.setFormElement("owner", newOwner);
        formParameterUtil.submitForm();
        // using FormParameterUtil has the unfortunate side effect of buggering up the tester dialog state
        // so we need to reset the tester - this means having to reload the page.
        tester.reset();
        return goTo().searchAll();
    }

    private String getDeleteLinkIdForFilter(long filterId)
    {
        return "delete_"+filterId;
    }

    private String getChangeOwnerLinkIdForFilter(long filterId)
    {
        return "change_owner_"+filterId;
    }


    private String getDeleteConfirmationFormForFilterId(long filterId)
    {
        return "delete-filter-confirm-form-"+filterId;
    }

    private String getChangeOwnerFormForFilterId(long filterId)
    {
        return "change-owner-form-"+filterId;
    }

    /**
     *
     * @since v4.4.1
     */
    public static class DefaultFilters implements Filters
    {
        private final LocatorFactory locators;

        public DefaultFilters(final LocatorFactory locators)
        {
            this.locators = locators;
        }

        @Override
        public List<FilterItem> list()
        {
            return new FilterTableParser(locators).parse("mf_browse");
        }

    }
}
