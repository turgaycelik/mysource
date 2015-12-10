package com.atlassian.jira.license;

/**
 * A factory to create license string from messages and hashes.
 *
 * @since v4.0
 */
public interface LicenseStringFactory
{
    /**
     * Creates a license String from an old license message and hash
     *
     * @param message the message
     * @param hash the hash
     * @return a license string represented by the message and hash, {@code null} if any error happens.
     */
    String create(String message, String hash);
}
