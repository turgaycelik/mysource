package com.atlassian.jira.security.auth;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.crowd.embedded.api.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * Implementations of this interface can indicate whether a user is authorised to perform a given request.
 * <p/>
 * They WILL be called for every request so you should make sure your authoriation check is somewhat performant.
 *
 * @since 5.2.3
 */
@PublicSpi
public interface Authorisation
{
    /**
     * When deciding whether to authorise a request, you can either grant it, deny or abstain from a decision
     */
    public enum Decision
    {
        GRANTED, DENIED, ABSTAIN;

        /**
         * Helper to turn GRANTED into true and anything else into false
         * <p/>
         * This is turning tri-state logic into binary logic.  Use deliberately.
         *
         * @return true if GRANTED otherwise false
         */
        public boolean toBoolean()
        {
            return this == GRANTED ? true : false;
        }

        /**
         * Helper to turn boolean answers into GRANTED or DENIED
         * <p/>
         *
         * @param answer the boolean answer
         * @return GRANTED if true otherwise DENIED
         */
        public static Decision toDecision(boolean answer)
        {
            return answer ? GRANTED : DENIED;
        }
    }

    /**
     * Called to ask whether a user is authorised to perform the given request when trying to login and estblish a new
     * session with JIRA.
     * <p/>
     * At this stage the user has been authenticated by not authorised to login.
     *
     * @param user a non null user that has been authenticated
     * @param httpServletRequest the request in play
     * @return a decision on authorisation
     */
    Decision authoriseForLogin(@Nonnull final User user, final HttpServletRequest httpServletRequest);

    /**
     * This is called by the security layers to get a set of role strings that are required for this request.  Once a
     * user has been set into the authentication context then {@link #authoriseForRole(com.atlassian.crowd.embedded.api.User,
     * javax.servlet.http.HttpServletRequest, String)} will be called to decide if they are in fact authorised to
     * execute this request.
     *
     * NOTE : If you give off a role MUST answer when you are called back via {@link #authoriseForRole(com.atlassian.crowd.embedded.api.User, javax.servlet.http.HttpServletRequest, String)}.
     *
     * @param httpServletRequest the request in play
     * @return a decision on authorisation
     */
    Set<String> getRequiredRoles(final HttpServletRequest httpServletRequest);

    /**
     * This is called by the security layers to ask whether a user is authorised to perform the given request with the
     * provided role string.
     * <p/>
     * You may be called with role strings that you did not give out.  In this case you should ABSTAIN from a decision.
     *
     * @param user a user that may be null
     * @param httpServletRequest the request in play
     * @return a decision on authorisation
     */
    Decision authoriseForRole(@Nullable final User user, final HttpServletRequest httpServletRequest, final String role);
}
