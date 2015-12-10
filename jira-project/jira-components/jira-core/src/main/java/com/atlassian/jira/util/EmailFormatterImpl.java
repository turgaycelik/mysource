package com.atlassian.jira.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.opensymphony.util.TextUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EmailFormatterImpl implements EmailFormatter
{
    private final ApplicationProperties applicationProperties;
    private static final char SYMBOL_AT = '@';
    private static final char SYMBOL_DOT = '.';
    private static final String TEXT_AT = " at ";
    private static final String TEXT_DOT = " dot ";
    private static final String VISIBILITY_PUBLIC = "show";
    private static final String VISIBILITY_USER = "user";
    private static final String VISIBILITY_MASKED = "mask";

    public EmailFormatterImpl(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    /**
     * Emails are visible by user if email visibility is set to
     * show (public) or mask (masked) or user (show to logged in users only)
     */
    public boolean emailVisible(User user)
    {
        String emailVisibility = applicationProperties.getString(APKeys.JIRA_OPTION_EMAIL_VISIBLE);

        return VISIBILITY_PUBLIC.equals(emailVisibility) || VISIBILITY_MASKED.equals(emailVisibility) ||
                (VISIBILITY_USER.equals(emailVisibility) && user != null);
    }

    public String formatEmail(User user, User currentUser)
    {
        if (user != null)
        {
            return formatEmail(user.getEmailAddress(), currentUser);
        }
        else
        {
            return null;
        }
    }

    public String formatEmail(final String email, final boolean isCurrentUserLoggedIn)
    {
        String emailVisibility = applicationProperties.getString(APKeys.JIRA_OPTION_EMAIL_VISIBLE);

        // Ensure that original behaviour is maintained if the property has not been set
        if (!TextUtils.stringSet(emailVisibility))
        {
            applicationProperties.setString(APKeys.JIRA_OPTION_EMAIL_VISIBLE, VISIBILITY_PUBLIC);
            emailVisibility = VISIBILITY_PUBLIC;
        }

        if (VISIBILITY_PUBLIC.equals(emailVisibility) || (VISIBILITY_USER.equals(emailVisibility) && isCurrentUserLoggedIn))
        {
            return email;
        }
        else if (VISIBILITY_MASKED.equals(emailVisibility))
        {
            StringBuilder result = new StringBuilder(email);
            result = replacePattern(result, SYMBOL_AT, TEXT_AT);
            result = replacePattern(result, SYMBOL_DOT, TEXT_DOT);
            return result.toString();
        }
        else
        {
            return null;
        }
    }

    public String formatEmail(String email, @Nullable User currentUser)
    {
        return formatEmail(email, currentUser != null);
    }

    /**
     * Returns a HTML link for the e-mail if appropriate.
     * REMEMBER not to escape the string returned by this method as this method does this already!
     */
    public @Nonnull String formatEmailAsLink(String email, @Nullable User currentUser)
    {
        String emailVisibility = applicationProperties.getString(APKeys.JIRA_OPTION_EMAIL_VISIBLE);
        if (VISIBILITY_PUBLIC.equals(emailVisibility) || (VISIBILITY_USER.equals(emailVisibility) && currentUser != null))
        {
            return "<a href=\"mailto:" + TextUtils.htmlEncode(email) + "\">" + TextUtils.htmlEncode(formatEmail(email, currentUser)) + "</a>";
        }
        else
        {
            return TextUtils.htmlEncode(formatEmail(email, currentUser));
        }
    }

    // Replaces originalPattern with newPattern in the specified string buffer
    private StringBuilder replacePattern(StringBuilder stringBuilder, char originalChar, String replacementText)
    {
        if (stringBuilder == null || stringBuilder.length() < 1)
        {
            return stringBuilder;
        }

        for (int i=0; i<stringBuilder.length(); ++i)
        {
            if (stringBuilder.charAt(i) == originalChar)
            {
                stringBuilder.replace(i, i+1, replacementText);
            }
        }
        return stringBuilder;
    }
}
