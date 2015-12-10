package com.atlassian.jira.bc.whitelist;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.util.ErrorCollection;

import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * Service to control whitelist rules currently used for allowing which http requests gadgets are allowed to make. This
 * can be used in future to whitelist any http requests!  Only system administrators should be allowed to modify the
 * whitelist implemenation
 *
 * @since v4.3
 * @deprecated Replaced by {@link com.atlassian.plugins.whitelist.WhitelistService} and
 *      {@link com.atlassian.plugins.whitelist.OutboundWhitelist}. Since v6.1.
 */
@PublicApi
@Deprecated
public interface WhitelistService
{
    /**
     * Returns a list of rules that are currently allowed in the whitelist. The list will contain entries like: <ul>
     * <li>http://www.atlassian.com/</li> <li>http://www.google.com/*</li> <li>=http://jira.atlassian.com/<li>
     * <li>\/.*www.*\/</li> </ul>
     *
     * @deprecated Replaced by {@link com.atlassian.plugins.whitelist.WhitelistService#getAll()}. Since v6.1.
     *
     * @param context The service context with the current user & error collection
     * @return Result containing the list of rules or an error message
     */
    @Deprecated
    WhitelistResult getRules(final JiraServiceContext context);

    /**
     * Validates that the current user is allowed to update the whitelist by checking if they are a system
     * administrator.
     *
     * @deprecated Replaced by
     *      {@link com.atlassian.plugins.whitelist.WhitelistService#add(com.atlassian.plugins.whitelist.WhitelistRule)},
     *      {@link com.atlassian.plugins.whitelist.WhitelistService#remove(int)},
     *      {@link com.atlassian.plugins.whitelist.WhitelistService#disableWhitelist()} and
     *      {@link com.atlassian.plugins.whitelist.WhitelistService#enableWhitelist()}. Since v6.1.
     *
     * @param context The service context with the current user & error collection
     * @param rules List of rule strings
     * @param disabled true if the whitelist should be turned off
     * @return A validation result that can be used to persist the new whitelist rules.
     */
    @Deprecated
    WhitelistUpdateValidationResult validateUpdateRules(final JiraServiceContext context, final List<String> rules, final boolean disabled);

    /**
     * Takes the validation result from {@link #validateUpdateRules(com.atlassian.jira.bc.JiraServiceContext,
     * java.util.List, boolean)} and persists the new rules.
     *
     * @deprecated Replaced by
     *      {@link com.atlassian.plugins.whitelist.WhitelistService#add(com.atlassian.plugins.whitelist.WhitelistRule)},
     *      {@link com.atlassian.plugins.whitelist.WhitelistService#remove(int)},
     *      {@link com.atlassian.plugins.whitelist.WhitelistService#disableWhitelist()} and
     *      {@link com.atlassian.plugins.whitelist.WhitelistService#enableWhitelist()}. Since v6.1.
     *
     * @param result a validation result obtained by calling {@link #validateUpdateRules(com.atlassian.jira.bc.JiraServiceContext,
     * java.util.List, boolean)}
     * @return Result containing the newly peristed whitelist
     */
    @Deprecated
    WhitelistResult updateRules(final WhitelistUpdateValidationResult result);

    /**
     * Returns true if the whitelist is currently disabled (meaning all requests are allowed).
     *
     * @deprecated Replaced by {@link com.atlassian.plugins.whitelist.WhitelistService#isWhitelistEnabled()}. Since v6.1.
     *
     * @return true if the whitelist is currently disabled (meaning all requests are allowed)
     */
    @Deprecated
    boolean isDisabled();

    /**
     * Checks if requests to the provided URI are allowed according to the current whitelist configuration
     *
     * @deprecated Replaced by {@link com.atlassian.plugins.whitelist.OutboundWhitelist#isAllowed(java.net.URI)}. Since v6.1.
     *
     * @param uri The uri a http request is made to
     * @return true if requests are allowed, false otherwise
     */
    @Deprecated
    boolean isAllowed(final URI uri);


    @PublicApi
    @Deprecated
    public static class WhitelistResult extends ServiceResultImpl
    {
        private final List<String> rules;

        public WhitelistResult(ErrorCollection errorCollection)
        {
            super(errorCollection);
            this.rules = Collections.emptyList();
        }

        public WhitelistResult(ErrorCollection errorCollection, final List<String> rules)
        {
            super(errorCollection);
            this.rules = rules;
        }

        public List<String> getRules()
        {
            return rules;
        }
    }

    @PublicApi
    @Deprecated
    public static class WhitelistUpdateValidationResult extends WhitelistResult
    {
        private final boolean disabled;

        public WhitelistUpdateValidationResult(final ErrorCollection errorCollection)
        {
            super(errorCollection);
            this.disabled = true;
        }

        public WhitelistUpdateValidationResult(final ErrorCollection errorCollection, final List<String> rules, final boolean enabled)
        {
            super(errorCollection, rules);
            this.disabled = enabled;
        }

        public boolean getDisabled()
        {
            return disabled;
        }
    }
}
