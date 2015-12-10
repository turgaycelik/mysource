package com.atlassian.jira.dev.reference.plugin.rpc;

/**
 * Reference SOAP service interface.
 *
 * @since v4.4
 */
public interface ReferenceSoapService {

    /**
     * Get reference plugin version
     *
     * @return reference plugin version
     */
    String getVersion();

    /**
     * Concatenate two string parameters!<br/>
     * WINNING!
     *
     * @param paramOne one
     * @param paramTwo two
     * @return concatenated string
     * @winning
     */
    String concatenate(String paramOne, String paramTwo);

    /**
     * Add two numbers.<br/>
     * WINNING!
     *
     * @param one number one
     * @param two number two
     * @return sum
     * @winning
     */
    int add(int one, int two);
}
