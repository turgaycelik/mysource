package com.atlassian.jira.web.action.filter;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.get;

/**
 * Class that represents "tabs" displayed when looking through filters.
 *
 * @since v3.13
 */
public final class FilterViewTabs
{
    public static final Tab POPULAR = new Tab("popular");
    public static final Tab SEARCH = new Tab("search");
    public static final Tab FAVOURITES = new Tab("favourites");
    public static final Tab MY = new Tab("my");
    public static final Tab PROJECT = new Tab("projects");

    public final Map <String, Tab> tabs;
    public final Tab defaultTab;
    public Tab firstTab = null;

    public FilterViewTabs(final List<Tab> tabs, final Tab defaultTab)
    {
        firstTab = get(tabs, 0);
        this.tabs = Maps.uniqueIndex(tabs, new Function<Tab, String>()
        {
            @Override
            public String apply(@Nullable Tab aTab)
            {
                if (aTab != null)
                {
                    return aTab.getName();
                }
                return "";
            }
        });
        this.defaultTab = defaultTab;
    }

    public boolean isValid(final String tabName)
    {
        return getTab(tabName) != null;
    }

    public boolean isFirst(final String tabName)
    {
        return firstTab.nameEquals(tabName);
    }

    public Tab getTab(final String tabName)
    {
        if (tabName == null)
        {
            return null;
        }
        else
        {
            return tabs.get(tabName.toLowerCase());
        }
    }

    /**
     * Works out which view (tab) the user should be on based on the requested tab.
     *
     * @param tabName    the tab the user has asked to be on, null is OK and results in a default.
     * @return the tab the user should be put on, possibly different to the request based on permissions etc.
     */
    public Tab getTabSafely(final String tabName)
    {
        final Tab returnTab = getTab(tabName);
        return returnTab == null ? defaultTab : returnTab;
    }

    /**
     * Class that represents a tab on a filter view.
     *
     * @since v3.13
     */
    public final static class Tab
    {
        private final String name;

        public Tab(final String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        public boolean nameEquals(final String name)
        {
            return this.name.equalsIgnoreCase(name);
        }

        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if ((o == null) || (getClass() != o.getClass()))
            {
                return false;
            }

            final Tab tab = (Tab) o;
            return name.equals(tab.name);
        }

        public int hashCode()
        {
            return name.hashCode();
        }

        public String toString()
        {
            return "Tab [" + getName() + "]";
        }
    }
}