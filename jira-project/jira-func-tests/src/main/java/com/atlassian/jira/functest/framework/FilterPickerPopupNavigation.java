package com.atlassian.jira.functest.framework;

import com.atlassian.jira.functest.framework.navigation.FilterNavigation;
import com.atlassian.jira.functest.framework.parser.filter.FilterItem;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermission;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.Transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * An implementation of the FilterNavigation interface that works with the FilterPicker action. Does not support
 * all the methods because that view permits no operations (edit, delete, set-as-favourite etc.) because it is
 * supposed to be solely for picking a filter. Unsupported operations throw UnsupportedOperationException.
 *
 * @since v3.13
 */
public class FilterPickerPopupNavigation implements FilterNavigation
{
    private WebTester tester;

    /**
     * Clones and returns a {@link com.atlassian.jira.functest.framework.parser.filter.FilterItem} which
     * has expectations removed that cannot be met by the Favourite view. Enables the setting of expectations
     * in FilterResults without polluting expectation code with view-specific column omissions.
     */
    private static final Transformer FAVOURITE_FILTER_ITEM = new Transformer()
    {
        public Object transform(final Object o)
        {
            final FilterItem filterItem = (FilterItem) o;
            return new FilterItem(filterItem).setNoOperations().setNoFavCount().setFav(false).setNoFavCount().setSubscriptions(0).setSharing(null);
        }
    };

    /**
     * Clones and returns a {@link com.atlassian.jira.functest.framework.parser.filter.FilterItem} which
     * has expectations removed that cannot be met by the Search Results (and Popular) view. Enables the setting of
     * expectations in FilterResults without polluting expectation code with view-specific column omissions.
     */
    private static final Transformer SEARCH_FILTER_ITEM = new Transformer()
    {
        public Object transform(final Object o)
        {
            final FilterItem filterItem = (FilterItem) o;
            return new FilterItem(filterItem).setNoOperations().setFav(false).setSubscriptions(0);
        }
    };

    public FilterPickerPopupNavigation(final WebTester tester)
    {
        this.tester = tester;
    }

    public void addFavourite(final int id)
    {
        throw new UnsupportedOperationException("You can't do this from the filter picker popup");
    }

    public void removeFavourite(final int id)
    {
        throw new UnsupportedOperationException("You can't do this from the filter picker popup");

    }

    public void goToDefault()
    {
        tester.gotoPage("secure/FilterPickerPopup.jspa");
    }

    public void manageSubscriptions(final int filterId)
    {
        throw new UnsupportedOperationException("You can't do this from the filter picker popup");

    }

    @Override
    public void addSubscription(int filterId)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void favouriteFilters()
    {
        tester.gotoPage("secure/FilterPickerPopup.jspa?filterView=favourite");
    }

    public void myFilters()
    {
        throw new UnsupportedOperationException("You can't do this from the filter picker popup");
    }

    public void allFilters()
    {
        tester.gotoPage("secure/FilterPickerPopup.jspa?filterView=search&pressedSearchButton=true&searchName=&searchOwner=&Search=");
    }

    public void popularFilters()
    {
        tester.gotoPage("secure/FilterPickerPopup.jspa?filterView=popular");
    }

    public void searchFilters()
    {
        tester.gotoPage("secure/FilterPickerPopup.jspa?filterView=search");
    }

    public long createFilter(final String filterName, final String filterDesc)
    {
        throw new UnsupportedOperationException("You can't do this from the filter picker popup");
    }

    @Override
    public long createFilter(String filterName, String filterDesc, Set<TestSharingPermission> sharingPermissions)
    {
        throw new UnsupportedOperationException("You can't do this from the filter picker popup");
    }

    public void deleteFilter(final int id)
    {
        throw new UnsupportedOperationException("You can't do this from the filter picker popup");
    }

    public void findFilters(final String filterName)
    {
        tester.getDialog().setFormParameter("searchName", filterName);
        tester.submit("Search");
    }

    public List<FilterItem> sanitiseSearchFilterItems(final List<FilterItem> expectedItems)
    {
        List<FilterItem> sanitisedList = ListUtils.transformedList(new ArrayList(), SEARCH_FILTER_ITEM);
        sanitisedList.addAll(expectedItems);
        return sanitisedList;
    }

    public List<FilterItem> sanitiseFavouriteFilterItems(final List<FilterItem> expectedItems)
    {
        List<FilterItem> sanitisedList = ListUtils.transformedList(new ArrayList(), FAVOURITE_FILTER_ITEM);
        sanitisedList.addAll(expectedItems);
        return sanitisedList;
    }

    public String getActionBaseUrl()
    {
        return "FilterPickerPopup.jspa";
    }

    public FilterNavigation projects()
    {
        tester.gotoPage("secure/FilterPickerPopup.jspa?filterView=projects&showProjects=true");
        return this;
    }
}
