package com.atlassian.jira.studio;

import com.atlassian.jira.plugin.studio.StudioHooks;
import com.atlassian.jira.plugin.studio.StudioLicenseHooks;
import com.atlassian.util.concurrent.Function;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Simple implementation of {@link StudioHooks} for tests.
 *
 * @since v4.4.2
 */
public class MockStudioHooks implements StudioHooks
{
    private StudioLicenseHooks licenseHooks;

    public MockStudioHooks()
    {
        this(MockStudioLicenseHooks.getInstance());
    }

    public MockStudioHooks(StudioLicenseHooks hooks)
    {
        this.licenseHooks = hooks;
    }

    public MockStudioHooks setLicenseHooks(StudioLicenseHooks licenseHooks)
    {
        this.licenseHooks = licenseHooks;
        return this;
    }

    @Nonnull
    @Override
    public StudioLicenseHooks getLicenseHooks()
    {
        return licenseHooks;
    }

    public static class MockStudioLicenseHooks implements StudioLicenseHooks
    {
        private static MockStudioLicenseHooks instance = new MockStudioLicenseHooks();

        public static MockStudioLicenseHooks getInstance()
        {
            return instance;
        }

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
