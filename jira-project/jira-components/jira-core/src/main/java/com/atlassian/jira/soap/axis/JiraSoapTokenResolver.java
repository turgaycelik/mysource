package com.atlassian.jira.soap.axis;

/**
 * Objects that implement this interface can resolve SOAP tokens back to user names.
 * to
 *
 * @since v3.13.2
 *
 */
public interface JiraSoapTokenResolver
{
    /**
     * The JiraAxisTokenResolver is asked to resolve a token back into user name.  if it can resolve a name
     * it should return null.
     *
     * @param token the token to resolve
     * @return a name of the user presented by this token or null if it cant be resolved
     */
    public String resolveTokenToUserName(String token);

    /**
     * The implementer is expected to return the parameter index that the "token" field
     * occurs on for the named operation.  For example the if the token is the first
     * parameter the implementer should return 0.
     *
     * @param operationName the name of the operation
     * @return the zero based index of the token parameter in that operation or -1 if it has not token
     */
    public int getTokenParameterIndex(String operationName);
}
