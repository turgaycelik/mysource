package com.atlassian.jira.functest.framework.admin;

import java.util.List;

/**
 * A page object useful for retrieving what tabs are currently shown in the admin section.
 *
 * @since v4.4
 */
public interface AdminTabs
{

    /**
     * Returns the current page heading for the admin section.
     *
     * @return the current page heading for the admin section.
     */
    String getPageHeading();

    /**
     * Returns a list of tabgroups that are currently shown on the page
     *
     * @return a list of tabgroups that are currently shown on the page
     */
    List<TabGroup> getCurrentTabs();

    /**
     * Fetches the total cound of tabs shown
     *
     * @return the total cound of tabs shown
     */
    int getNumberOfTabs();

    static final class TabGroup
    {
        private final List<Tab> tabs;

        public TabGroup(List<Tab> tabs)
        {
            this.tabs = tabs;
        }

        public List<Tab> getTabs()
        {
            return tabs;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            TabGroup tabGroup = (TabGroup) o;

            if (!tabs.equals(tabGroup.tabs)) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            return tabs.hashCode();
        }

        @Override
        public String toString()
        {
            return "TabGroup " + tabs;
        }
    }

    static final class Tab
    {
        private final String name;
        private final boolean selected;

        public Tab(String name, boolean selected)
        {
            this.name = name;
            this.selected = selected;
        }

        public String getName()
        {
            return name;
        }

        public boolean isSelected()
        {
            return selected;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            Tab tab = (Tab) o;

            if (selected != tab.selected) { return false; }
            if (!name.equals(tab.name)) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = name.hashCode();
            result = 31 * result + (selected ? 1 : 0);
            return result;
        }

        @Override
        public String toString()
        {
            String tab = "Tab ('" + name + "'";
            if (selected)
            {
                tab += ", selected)";
            }
            else
            {
                tab += ")";
            }
            return tab;
        }
    }
}
