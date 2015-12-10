package com.atlassian.jira.plugin.webfragment.descriptors;

import com.atlassian.jira.plugin.webfragment.SimpleLinkFactory;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WeightedDescriptor;

/**
 * Plugin in Module descriptor that defines a {@link com.atlassian.jira.plugin.webfragment.SimpleLinkFactory}.  This is used
 * by the {@link com.atlassian.jira.plugin.webfragment.SimpleLinkManager} to display a list of simple links.
 *
 * @since v4.0
 * @deprecated since v6.3 - use {@link com.atlassian.plugin.web.descriptors.WebItemProviderModuleDescriptor}
 */
public interface SimpleLinkFactoryModuleDescriptor extends ModuleDescriptor<SimpleLinkFactory>, WeightedDescriptor
{
    /**
     * Whether or not this should be lazily loaded.  This should be true if the list is expensive.
     * Note that this can be ignored if the list does not support lazily loaded links.
     *
     * @return true if the list is expensive to generate and should be loaded lazily if possible, false otherwise.
     */
    boolean shouldBeLazy();

    /**
     * The section that this list will be inserted into.  This usually refers to a
     * {@link com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor} location + "/" + key.
     *
     * This should never return null.
     *
     * @return  The section that this list will be inserted into.
     */
    String getSection();

}
