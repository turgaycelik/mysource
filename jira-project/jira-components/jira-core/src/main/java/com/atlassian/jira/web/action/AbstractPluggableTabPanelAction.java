package com.atlassian.jira.web.action;

import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.plugin.PluggableTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptorComparator;
import com.atlassian.plugin.PluginAccessor;
import webwork.action.ActionContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

/**
 * Manages the setting, retrieval, checking and activating of a "selected tab"
 * for any page that wishes to provide navigation to "tab panels".
 *
 * @since v6.1
 */
public abstract class AbstractPluggableTabPanelAction<TabPanelClass extends PluggableTabPanelModuleDescriptor> extends IssueActionSupport
{
    protected final PluginAccessor pluginAccessor;

    private List<TabPanelClass> tabPanels;
    private String persistenceKey;

    public AbstractPluggableTabPanelAction(PluginAccessor pluginAccessor)
    {
        super();
        this.pluginAccessor = pluginAccessor;
        this.persistenceKey = AbstractPluggableTabPanelAction.class.toString(); // For lack of a sensible default.
    }

    /**
     *
     * @return A list of {@link TabPanelClass} objects, never null
     */
    public List<TabPanelClass> getTabPanels()
    {
        if (tabPanels == null)
        {
            tabPanels = initTabPanels();
        }
        return tabPanels;
    }

    /**
     * Retrieves and initialises the tab panels via the plugin accessor
     *
     * @return list of {@link TabPanelClass} objects, never null
     * @since v3.10
     */
    protected List<TabPanelClass> initTabPanels()
    {
        final List<TabPanelClass> tabPanels = getTabPanelModuleDescriptors();
        List<TabPanelClass> filteredTabPanels;
        filteredTabPanels = Lists.newArrayList(Collections2.filter(tabPanels, new Predicate<TabPanelClass>()
        {
            @Override
            public boolean apply(@Nullable final TabPanelClass tabPanelClass)
            {
                try
                {
                    if (tabPanelClass == null) return false;
                    if (isTabPanelHidden(tabPanelClass)) return false;
                }
                catch (PermissionException e)
                {
                    return false;
                }
                return true;
            }
        }));
        Collections.sort(filteredTabPanels, ModuleDescriptorComparator.COMPARATOR);
        return filteredTabPanels;
    }

    /**
     * Retrieves the tab panels for this page via the plugin accessor.
     *
     * Used by initialisation method to collect the list of tab panels for this page
     */
    protected abstract List<TabPanelClass> getTabPanelModuleDescriptors();

    /**
     * Returns true if the tab panel of the given descriptor should be hidden from the current view
     *
     * @param descriptor module descriptor
     * @return true if hidden, false otherwise
     * @throws PermissionException if project is invalid or not visible to the current user
     * @since v3.10
     */
    protected abstract boolean isTabPanelHidden(final TabPanelClass descriptor) throws PermissionException;

    protected boolean canSeeTab(final String tabKey)
    {
        if (tabKey == null)
        {
            return false;
        }
        final StringTokenizer st = new StringTokenizer(tabKey, ":");
        if (st.countTokens() == 2)
        {
            // Get the key of the currently selected tab
            st.nextToken();
            final String tabName = st.nextToken();
            // Iterate over the available project tab panels
            for (final TabPanelClass tabPanel : getTabPanels())
            {
                if ((tabName != null) && tabName.equals(tabPanel.getKey()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public TabPanelClass getSelectedTabPanel()
    {
        return (TabPanelClass)pluginAccessor.getEnabledPluginModule(getSelectedTab());
    }

    @SuppressWarnings({"unused"}) // Used in JSPs to mark the 'active' state of the navigation.
    public String getSelected()
    {
        return getSelectedTab();
    }

    /**
     * Retrieve the name of the tab panel that is selected.
     *
     * Protected because it's used in some error log messages.
     *
     * @return The complete module key of the selected tab.
     *         If no tab is currently selected, it will return the first available tab on the page.
     *         If there are no tabs to select, returns null.
     */
    protected String getSelectedTab()
    {
        final String currentKey = (String) getSession().get(persistenceKey);
        if (canSeeTab(currentKey))
        {
            return currentKey;
        }

        final List<TabPanelClass> projectTabPanels = getTabPanels();
        if (!projectTabPanels.isEmpty())
        {
            final String key = (projectTabPanels.get(0)).getCompleteKey();
            setSelectedTab(key);
            return key;
        }

        return null;
    }

    /**
     * Set the name of the selected tab.
     *
     * Used by {@link com.atlassian.jira.webwork.JiraSafeActionParameterSetter#setActionProperty(java.lang.reflect.Method, webwork.action.Action, String[])}.
     *
     * @param selectedTab a complete module descriptor key for the {@link TabPanelClass} to set as the selected tab.
     */
    public void setSelectedTab(final String selectedTab)
    {
        getSession().put(persistenceKey, selectedTab);
    }

    private Map<String, Object> getSession()
    {
        return ActionContext.getSession();
    }

    public void setPersistenceKey(@Nonnull String persistenceKey)
    {
        this.persistenceKey = persistenceKey;
    }
}