package com.atlassian.jira.plugin.versionpanel;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.plugin.browsepanel.TabPanel;

/**
 * Version Tab Panel
 *
 * @since v3.10
 */
@PublicSpi
public interface VersionTabPanel extends TabPanel<VersionTabPanelModuleDescriptor, BrowseVersionContext>
{
}
