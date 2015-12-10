package com.atlassian.jira.studio.startup;

/**
 * For setting mock {@link com.atlassian.jira.studio.startup.StudioStartupHooks}.
 *
 * @since v5.1.6
 */
public class MockStartupHooks
{

    public static void setHooks(StudioStartupHooks hooks)
    {
        StudioStartupHooksLocator.setStartupHooks(hooks);
    }

    public static void resetHooks()
    {
        StudioStartupHooksLocator.resetStartupHooks();
    }
}
