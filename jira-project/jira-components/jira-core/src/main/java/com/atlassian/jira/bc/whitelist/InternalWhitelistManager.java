package com.atlassian.jira.bc.whitelist;

import com.atlassian.annotations.Internal;

import java.net.URI;
import java.util.List;

/**
 * Marker interface to distinguish the actual implementation of {@link WhitelistManager} living in a plugin from
 * the delegation placeholder in JIRA core.
 * @since 6.1
 */
@Internal
public interface InternalWhitelistManager
{
    /**
     * Returns a list of rules that are currently allowed in the whitelist. The list will contain entries like: <ul>
     * <li>http://www.atlassian.com/</li> <li>http://www.google.com/*</li> <li>=http://jira.atlassian.com/<li>
     * <li>\/.*www.*\/</li> </ul>
     *
     * @return a list of allowed rules
     */
    List<String> getRules();

    /**
     * Used to update the whitelist configuration.  Takes a list of rules as well as a boolean flag that allows
     * switching the whitelist off completely.
     * <p/>
     * The method then returns the peristed rules
     *
     * @param rules List of rules to persist
     * @param disabled True if the whitelist should be switched off
     * @return A list of persisted rules
     */
    List<String> updateRules(List<String> rules, boolean disabled);

    /**
     * Checks if requests to the provided URI are allowed according to the current whitelist configuration
     *
     * @param uri The uri a http request is made to
     * @return true if requests are allowed, false otherwise
     */
    boolean isAllowed(URI uri);

    /**
     * @return true if the whitelist is currently disabled (meaning all requests are allowed)
     */
    boolean isDisabled();
}
