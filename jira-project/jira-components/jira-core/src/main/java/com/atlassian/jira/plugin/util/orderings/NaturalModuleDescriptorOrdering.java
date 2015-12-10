package com.atlassian.jira.plugin.util.orderings;

import com.atlassian.plugin.ModuleDescriptor;
import com.google.common.collect.Ordering;

/**
* Orders module descriptors according to their &quot;natural&quot; ordering, based on the descriptors
* {@link com.atlassian.plugin.ModuleDescriptor#getCompleteKey()}.
*
* @since v4.4
*/
public class NaturalModuleDescriptorOrdering extends Ordering<ModuleDescriptor>
{
    NaturalModuleDescriptorOrdering(){} // Make sure the constructor is only accesible from this package

    @Override
    public int compare(ModuleDescriptor o1, ModuleDescriptor o2)
    {
        return o1.getCompleteKey().compareTo(o2.getCompleteKey());
    }
}
