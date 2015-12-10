package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.LocatorEntry;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @since v4.4
 */
public class AdminTabsImpl extends AbstractFuncTestUtil implements AdminTabs
{
    public AdminTabsImpl(WebTester tester, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData, 2);
    }

    @Override
    public String getPageHeading()
    {
        return new CssLocator(tester, "#admin-page-heading").getText();
    }

    @Override
    public List<TabGroup> getCurrentTabs()
    {
        final List<TabGroup> ret = new ArrayList<TabGroup>();
        final Locator locator = new CssLocator(tester, ".page-type-admin .tabs-menu li.aui-tabs-group");
        if(locator.hasNodes())
        {
            for (Node tabGroupNode : locator.getNodes())
            {
                final List<Tab> tabs = new ArrayList<Tab>();
                final CssLocator tabLocator = new CssLocator(tabGroupNode, "ul li");
                final Iterator<LocatorEntry> tabIterator = tabLocator.iterator();
                while (tabIterator.hasNext())
                {
                    final LocatorEntry locatorEntry = tabIterator.next();
                    final String classValue = locatorEntry.getNode().getAttributes().getNamedItem("class").getNodeValue();
                    tabs.add(new Tab(locatorEntry.getText(), classValue.contains("active-tab")));
                }
                ret.add(new TabGroup(tabs));
            }
        }
        return ret;
    }

    @Override
    public int getNumberOfTabs()
    {
        final List<TabGroup> currentTabs = getCurrentTabs();
        int count = 0;
        for (TabGroup currentTab : currentTabs)
        {
            for (Tab tab : currentTab.getTabs())
            {
                count++;
            }
        }
        return count;
    }
}
