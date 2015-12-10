package com.atlassian.jira.studio.startup;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.util.concurrent.LazyReference;
import com.google.common.annotations.VisibleForTesting;

import javax.annotation.Nonnull;

/**
 * Class used to find the {@link com.atlassian.jira.studio.startup.StudioStartupHooks} that JIRA should be using.
 *
 * @since v4.4.1
 */
public final class StudioStartupHooksLocator
{
    private StudioStartupHooksLocator() {}

    private static volatile StudioStartupHooks preset;

    @ClusterSafe ("Programming artifacts only.")
    private static LazyReference<StudioStartupHooks> ref = new LazyReference<StudioStartupHooks>()
    {
        private final Locator[] LOCATORS = new Locator[]{new SystemPropertyLocator(), new ServiceLocator(), new JiraDefaultLocator()};

        @Override
        @Nonnull
        protected StudioStartupHooks create()
        {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null)
            {
                classLoader = StudioStartupHooksLocator.class.getClassLoader();
            }

            for (Locator locator : LOCATORS)
            {
                StudioStartupHooks hooks = locator.locate(classLoader);
                if (hooks != null)
                {
                    return hooks;
                }
            }
            throw new IllegalStateException("Unable to find StudioStartupHooks.");
        }
    };

    public static StudioStartupHooks getStudioStartupHooks()
    {
        final StudioStartupHooks presetHooks = preset;
        if (presetHooks != null)
        {
            return presetHooks;
        }
        else
        {
            return ref.get();
        }
    }

    @VisibleForTesting
    static void setStartupHooks(StudioStartupHooks hooks)
    {
        preset = hooks;
    }

    @VisibleForTesting
    static void resetStartupHooks()
    {
        preset = null;
    }
}
