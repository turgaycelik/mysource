package com.atlassian.jira.bc.whitelist;

import com.atlassian.annotations.PublicApi;

import java.net.URI;
import java.util.List;

/**
 * Manager to control whitelist rules currently used for allowing which http requests gadgets are allowed to make. This
 * can be used in future to whitelist any http requests!
 *
 * @since v4.3
 * @deprecated Replaced by {@link com.atlassian.plugins.whitelist.WhitelistService} and
 *      {@link com.atlassian.plugins.whitelist.OutboundWhitelist}. Since v6.1.
 */
@PublicApi
@Deprecated
public interface WhitelistManager
{
    /**
     * Returns a list of rules that are currently allowed in the whitelist. The list will contain entries like: <ul>
     * <li>http://www.atlassian.com/</li> <li>http://www.google.com/*</li> <li>=http://jira.atlassian.com/<li>
     * <li>\/.*www.*\/</li> </ul>
     *
     * @deprecated Replaced by {@link com.atlassian.plugins.whitelist.WhitelistService#getAll()}. Since v6.1.
     *
     * @return a list of allowed rules
     */
    @Deprecated
    List<String> getRules();

    /**
     * Used to update the whitelist configuration.  Takes a list of rules as well as a boolean flag that allows
     * switching the whitelist off completely.
     * <p/>
     * The method then returns the peristed rules
     *
     * @deprecated Replaced by
     *      {@link com.atlassian.plugins.whitelist.WhitelistService#add(com.atlassian.plugins.whitelist.WhitelistRule)},
     *      {@link com.atlassian.plugins.whitelist.WhitelistService#remove(int)},
     *      {@link com.atlassian.plugins.whitelist.WhitelistService#disableWhitelist()} and
     *      {@link com.atlassian.plugins.whitelist.WhitelistService#enableWhitelist()}. Since v6.1.
     *
     * @param rules List of rules to persist
     * @param disabled True if the whitelist should be switched off
     * @return A list of persisted rules
     */
    @Deprecated
    List<String> updateRules(List<String> rules, boolean disabled);

    /**
     * Checks if requests to the provided URI are allowed according to the current whitelist configuration
     *
     * @deprecated Replaced by {@link com.atlassian.plugins.whitelist.OutboundWhitelist#isAllowed(java.net.URI)}. Since v6.1.
     *
     * @param uri The uri a http request is made to
     * @return true if requests are allowed, false otherwise
     */
    @Deprecated
    boolean isAllowed(URI uri);

    /**
     * Returns true if the whitelist is currently disabled (meaning all requests are allowed).
     *
     * @deprecated Replaced by {@link com.atlassian.plugins.whitelist.WhitelistService#isWhitelistEnabled()}. Since v6.1.
     *
     * @return true if the whitelist is currently disabled (meaning all requests are allowed)
     */
    @Deprecated
    boolean isDisabled();
}
