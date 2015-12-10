package com.atlassian.jira.plugin.user;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.tracker.PluginModuleTracker;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;

/**
 * @since v6.1
 */
public class PasswordPolicyManagerImplTest
{
    @Test
    public void testGetWarningsWhenPluginReturnsResult() throws Exception
    {
        final URI furtherInformation = new URI("http://www.atlassian.com");
        final WebErrorMessage expectedWarning = new WebErrorMessageImpl(
                "I've just picked up a fault in the AE-35 unit.  It's going to go 100% failure in 72 hours.",
                "I'm sorry, Dave.  I'm afraid I can't do that.",
                furtherInformation);
        final PasswordPolicy passwordPolicy = new MockPasswordPolicy( ImmutableList.of(expectedWarning));
        final PasswordPolicyManager passwordPolicyManager = getPasswordPolicyManager(passwordPolicy);

        final Collection<WebErrorMessage> actual = passwordPolicyManager.checkPolicy("", "", "", "");
        assertEquals(ImmutableList.of(expectedWarning), actual);
    }

    @Test
    public void testGetWarningsWithNullWarnings() throws Exception
    {
        final PasswordPolicy passwordPolicy = new MockPasswordPolicy();
        final PasswordPolicyManagerImpl passwordPolicyManager = getPasswordPolicyManager(passwordPolicy);
        final Collection<WebErrorMessage> actual = passwordPolicyManager.checkPolicy("", "", "", "");
        assertEquals(ImmutableList.<WebErrorMessage>of(), actual);
    }

    @Test
    public void testGetWarningsWithEmptyWarnings() throws Exception
    {
        final PasswordPolicy passwordPolicy = new MockPasswordPolicy(ImmutableList.<WebErrorMessage>of());
        final PasswordPolicyManagerImpl passwordPolicyManager = getPasswordPolicyManager(passwordPolicy);
        final Collection<WebErrorMessage> actual = passwordPolicyManager.checkPolicy("", "", "", "");
        assertEquals(ImmutableList.<WebErrorMessage>of(), actual);
    }

    @Test
    public void testGetAdviceMessagesWhenPluginReturnsResult() throws Exception
    {
        final String advice = "I wonder what this button does?";
        final PasswordPolicy passwordPolicy = new MockPasswordPolicy(ImmutableList.of(advice));
        final PasswordPolicyManager passwordPolicyManager = getPasswordPolicyManager(passwordPolicy);

        final List<String> actual = passwordPolicyManager.getPolicyDescription(true);
        assertEquals(ImmutableList.of(advice), actual);
    }

    @Test
    public void testGetAdviceMessagesPassesFalseValueOnToPluginModule() throws Exception
    {
        final String advice = "I wonder what this button does?";
        final PasswordPolicy passwordPolicy = new MockPasswordPolicy(ImmutableList.of(advice));
        final PasswordPolicyManager passwordPolicyManager = getPasswordPolicyManager(passwordPolicy);

        final List<String> actual = passwordPolicyManager.getPolicyDescription(false);
        assertEquals(ImmutableList.<String>of(), actual);
    }

    @Test
    public void testGetAdviceMessagesWithNullAdviceMessages() throws Exception
    {
        final PasswordPolicyManager passwordPolicyManager = getPasswordPolicyManager(new MockPasswordPolicy());
        final List<String> actual = passwordPolicyManager.getPolicyDescription(true);
        assertEquals(ImmutableList.<String>of(), actual);
    }

    @Test
    public void testGetAdviceMessagesWithEmptyAdviceMessages() throws Exception
    {
        final PasswordPolicyManager passwordPolicyManager = getPasswordPolicyManager(new MockPasswordPolicy(ImmutableList.<String>of()));
        final List<String> actual = passwordPolicyManager.getPolicyDescription(true);
        assertEquals(ImmutableList.<String>of(), actual);
    }



    private static PasswordPolicyManagerImpl getPasswordPolicyManager(final PasswordPolicy pluginToCall)
    {
        return new PasswordPolicyManagerImpl(null,null)
        {

            @Override
            PluginModuleTracker<PasswordPolicy, PasswordPolicyModuleDescriptor> createTracker(PluginAccessor pluginAccessor, PluginEventManager pluginEventManager)
            {
                return null;
            }

            Iterable<PasswordPolicy> enabledModules()
            {
                return newArrayList(pluginToCall);
            }
        };
    }

    static class MockPasswordPolicy implements PasswordPolicy
    {
        final Collection<WebErrorMessage> mockWebErrorMessages;
        final List<String> adviceMessages;

        public MockPasswordPolicy()
        {
            mockWebErrorMessages = null;
            adviceMessages = null;
        }

        public MockPasswordPolicy(Collection<WebErrorMessage> userOwnedEntites)
        {
            mockWebErrorMessages = userOwnedEntites;
            adviceMessages = null;
        }

        public MockPasswordPolicy(List<String> adviceMessages)
        {
            mockWebErrorMessages = null;
            this.adviceMessages = adviceMessages;
        }

        @Override
        public Collection<WebErrorMessage> validatePolicy(@Nonnull final User user,
                @Nullable final String oldPassword, @Nonnull final String newPassword)
        {
            return mockWebErrorMessages;
        }

        @Override
        public List<String> getPolicyDescription(boolean hasOldPassword)
        {
            return hasOldPassword ? adviceMessages : ImmutableList.<String>of();
        }
    }
}
