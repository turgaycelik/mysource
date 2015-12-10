package com.atlassian.jira.plugin.browsepanel;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.plugin.TabPanelModuleDescriptor;
import com.atlassian.jira.project.browse.BrowseContext;

/**
 * Unified interface for all fragment-based tab panels.
 *
 * @since v4.0
 */
@PublicSpi
public interface TabPanel<D extends TabPanelModuleDescriptor, C extends BrowseContext>
{
    /**
     * Initialize the tab panel panel with the plugins ProjectTabPanelModuleDescriptor.  This is usually used for
     * rendering velocity views.
     *
     * @param descriptor the descriptor for this module as defined in the plugin xml descriptor.
     */
    void init(D descriptor);

    /**
     * Used to render the tab.
     *
     * @param ctx The current context the tab is rendering in.
     * @return Escaped string with the required html.
     */
    String getHtml(C ctx);

    /**
     * Determine whether or not to show this.
     *
     * @param ctx The current context the tab is rendering in.
     * @return True if the conditions are right to display tab, otherwise false.
     */
    boolean showPanel(C ctx);
}
