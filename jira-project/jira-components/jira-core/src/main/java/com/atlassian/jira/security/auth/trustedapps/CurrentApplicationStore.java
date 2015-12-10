package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.jira.util.OnDemand;
import com.atlassian.security.auth.trustedapps.CurrentApplication;

import java.security.KeyPair;

/**
 * Store for JIRA's own {@link CurrentApplication}.
 *
 * @since v5.0
 */
@OnDemand
public interface CurrentApplicationStore
{
    /**
     * Return {@link} JIRA's {@link com.atlassian.security.auth.trustedapps.CurrentApplication}. Will create a
     * CurrentApplication if one does not exist or has been set.
     *
     * @return JIRA's CurrentApplication.
     */
    CurrentApplication getCurrentApplication();

    /**
     * Get the current KeyPair used by trusted apps.
     *
     * @return the {@link java.security.KeyPair} used by trustedapps.
     */
    @OnDemand
    KeyPair getKeyPair();

    /**
     * Set the current application's (aka JIRA's) trusted application properties.
     *
     * @param applicationId the name of JIRA's trusted application. Passing an empty or null string will result
     *  in an {@link IllegalArgumentException}.
     * @param pair public and private key used for trusted apps. Passing null for pair or either the private or
     * public key will result in an {@link IllegalArgumentException}.
     */
    @OnDemand
    void setCurrentApplication(String applicationId, KeyPair pair);

    /**
     * Set the current application's (aka JIRA's) trusted application properties.
     *
     * @param applicationId the name of JIRA's trusted application. Passing null will set an
     * automatically generated ID.
     * @param publicKey string representation of the public key. Must be a valid key or a
     * {@link IllegalArgumentException} will be thrown.
     * @param privateKey string representation of the private key. Must be valid key or a
     * {@link IllegalArgumentException} will be thrown.
     */
    @OnDemand
    void setCurrentApplication(String applicationId, String publicKey, String privateKey);
}
