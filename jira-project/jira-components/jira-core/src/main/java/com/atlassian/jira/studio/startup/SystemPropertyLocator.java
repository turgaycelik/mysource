package com.atlassian.jira.studio.startup;

import javax.annotation.Nonnull;

import com.atlassian.jira.config.properties.JiraSystemProperties;

import static org.apache.commons.lang.StringUtils.stripToNull;

/**
 * Try and find some {@link StudioStartupHooks} using a system property to identify the class.
 *
 * @since v4.4.1
 */
class SystemPropertyLocator implements Locator
{
    private static final String HOOK_KEY = "jira.studio.hooks";

    @Override
    public StudioStartupHooks locate(@Nonnull ClassLoader loader)
    {
        try
        {
            String property = stripToNull(JiraSystemProperties.getInstance().getProperty(HOOK_KEY));
            if (property != null)
            {
                ClassMaker<StudioStartupHooks> factoryClassMaker = new ClassMaker<StudioStartupHooks>(StudioStartupHooks.class, loader);
                return factoryClassMaker.createInstance(property);
            }
        }
        catch (SecurityException e)
        {
            //ignored.
        }
        return null;
    }
}
