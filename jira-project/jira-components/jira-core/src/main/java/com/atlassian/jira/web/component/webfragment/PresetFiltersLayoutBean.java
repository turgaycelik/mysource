package com.atlassian.jira.web.component.webfragment;

import com.atlassian.jira.plugin.webfragment.descriptors.JiraWebItemModuleDescriptor;

/**
 * This class provides context information for preset filters.  It's main purpose really
 * is to hide the Unscheduled filter in the preset filters when viewing a version.
 *
 * This should probably be extended sometime in the future, such that a list of hidden filters
 * can be passed in via the constructor.  Currently this isn't necessary though.
 * @since v3.10
 */
public class PresetFiltersLayoutBean implements ContextLayoutBean
{
    public static final String FILTER_UNSCHEDULED = "filter_unscheduled";

    public boolean isDisplayableItemHidden(JiraWebItemModuleDescriptor item)
    {
        return item != null && FILTER_UNSCHEDULED.equals(item.getKey());
    }
}
