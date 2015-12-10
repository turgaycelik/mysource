package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;

/**
 * Provides the ability to generated and multiple secure tokens for a given user and type.
 * <p/>
 * The token and type can then be resolved back to that user.
 * <p/>
 * Tokens provided by these implementations should be generated securly (i.e. using {@link
 * com.atlassian.security.random.DefaultSecureTokenGenerator}) expire after 30 minutes and only be available for use
 * once!
 *
 * @since v4.4
 */
public interface SecureUserTokenManager
{
    /**
     * Token will be usable only for types of requests specified via this enum
     */
    public static enum TokenType
    {
        /**
         * SCREENSHOT tokens can be used to attach screenshots.
         */
        SCREENSHOT
    }

    /**
     * Given a user and tokentype this method creates a new secure token and returns this token. A particular user can
     * have multiple tokens mapped at any given time.
     *
     * @param user The user this token is for
     * @param tokenType The {@link TokenType} for this token
     * @return A new token or null if no user was provided
     */
    String generateToken(final User user, final TokenType tokenType);

    /**
     * Given a token and tokenType, this method returns the User that was mapped to this token and then revokes the
     * token to ensure it can't be used again.
     *
     * @param token A secure token
     * @param tokenType The {@link TokenType} for this token
     * @return The User mapped to this token, or null if no mapping can be found.
     */
    User useToken(final String token, final TokenType tokenType);
}
