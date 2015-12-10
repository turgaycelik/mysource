package com.atlassian.jira.plugin.componentpanel;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.plugin.browsepanel.TabPanel;

/**
 * @since v3.10
 */
@PublicSpi
public interface ComponentTabPanel extends TabPanel<ComponentTabPanelModuleDescriptor, BrowseComponentContext>
{
}
