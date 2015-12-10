package com.atlassian.jira.studio;

import com.atlassian.jira.plugin.studio.StudioHooks;
import com.atlassian.jira.plugin.studio.StudioLicenseHooks;
import com.atlassian.util.concurrent.Function;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Implementation of {@link com.atlassian.jira.plugin.studio.StudioHooks} used when no plugins are installed that
 * implement the interface.
 *
 * @since v4.4.2
 */
class VanillaStudioHooks implements StudioHooks
{
    private static final VanillaStudioHooks instance = new VanillaStudioHooks();

    static VanillaStudioHooks getInstance()
    {
        return instance;
    }

    private VanillaStudioHooks() {}

    @Nonnull
    @Override
    public StudioLicenseHooks getLicenseHooks()
    {
        return VanillaStudioLicenseHooks.instance;
    }

    private static class VanillaStudioLicenseHooks implements StudioLicenseHooks
    {
        private static final VanillaStudioLicenseHooks instance = new VanillaStudioLicenseHooks();

        @Override
        public void clearActiveUserCount(Function<Void, Void> method)
        {
            method.get(null);
        }

        @Override
        public boolean hasExceededUserLimit(Function<Void, Boolean> method)
        {
            return method.get(null);
        }

        @Override
        public boolean canActivateNumberOfUsers(int numUsers, Function<Integer, Boolean> method)
        {
            return method.get(numUsers);
        }

        @Override
        public boolean canActivateUsers(Collection<String> userNames, Function<Collection<String>, Boolean> method)
        {
            return method.get(userNames);
        }
    }
}
