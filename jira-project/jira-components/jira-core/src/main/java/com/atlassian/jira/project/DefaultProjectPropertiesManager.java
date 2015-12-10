package com.atlassian.jira.project;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.opensymphony.module.propertyset.PropertySet;

/**
 * Default implementation for {@link ProjectPropertiesManager}.
 *
 * @see ProjectPropertiesManager
 * @since v6.1
 */
public class DefaultProjectPropertiesManager implements ProjectPropertiesManager
{
    @Override
    public PropertySet getPropertySet(Project project)
    {
        return OFBizPropertyUtils.getCachingPropertySet(project.getGenericValue());
    }

}
