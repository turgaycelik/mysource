package com.atlassian.jira.plugin.link.applinks;

import com.atlassian.applinks.api.ApplicationType;

/**
 * @since v5.0
 */
public class AppLinkUtils
{
    private AppLinkUtils()
    {
        // Utility class
    }

    public static Class<ApplicationType> getApplicationTypeClass(String type)
    {
        try
        {
            final Class<?> typeClass = ApplicationType.class.getClassLoader().loadClass(type);
            if (ApplicationType.class.isAssignableFrom(typeClass))
            {
                return (Class<ApplicationType>) typeClass;
            }
            else
            {
               throw new IllegalArgumentException("Type class '" + typeClass + "' must implement " + ApplicationType.class + ".");
            }
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalArgumentException(e);
        }
    }
}
