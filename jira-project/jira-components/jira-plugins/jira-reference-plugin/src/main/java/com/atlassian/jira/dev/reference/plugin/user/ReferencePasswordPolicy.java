package com.atlassian.jira.dev.reference.plugin.user;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.user.PasswordPolicy;
import com.atlassian.jira.plugin.user.WebErrorMessage;
import com.atlassian.jira.plugin.user.WebErrorMessageImpl;

import com.google.common.collect.ImmutableList;

/**
 * Provides basic example of the password-policy plugin point
 *
 * @since v6.1
 */
public class ReferencePasswordPolicy implements PasswordPolicy
{
    private static final String ADVICE = "The reference plugin disallows setting the password when the username is 'pwpolicyuser'.";
    private static final String DESCRIPTION = "Password user should never get a new password - this has been provided by the reference plugin";
    private static final String SNIPPET = "The reference plugin presents this error if you try to change the password for 'pwpolicyuser'.";
    private URI furtherInformationURI = null;

    public ReferencePasswordPolicy()
    {
        try
        {
            this.furtherInformationURI = new URI("http://www.atlassian.com");
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public Collection<WebErrorMessage> validatePolicy(@Nonnull final User user, @Nullable final String oldPassword, @Nonnull final String newPassword)
    {
        if (user.getName().equals("pwpolicyuser"))
        {
            return ImmutableList.<WebErrorMessage>of(new WebErrorMessageImpl(DESCRIPTION, SNIPPET, furtherInformationURI));
        }
        return ImmutableList.of();
    }

    @Override
    public List<String> getPolicyDescription(boolean hasOldPassword)
    {
        return ImmutableList.of(ADVICE);
    }
}
