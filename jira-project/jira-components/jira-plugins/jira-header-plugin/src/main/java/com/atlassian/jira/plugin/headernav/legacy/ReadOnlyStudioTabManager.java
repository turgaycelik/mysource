package com.atlassian.jira.plugin.headernav.legacy;

import java.util.List;

/**
 * Allow us to get StudioTabs, for migration purposes
 */
public interface ReadOnlyStudioTabManager
{
    String DEFAULT_TABS_KEY = "_default";

    List<StudioTab> getAllTabs(String projectKey);
}
