package com.atlassian.jira.pageobjects.xsrf;

import com.atlassian.jira.pageobjects.pages.JiraLoginPage;

/**
 * Represents a user's Xsrf interaction.
 *
 * @since v5.0.1
 */
public interface Xsrf
{
    boolean hasParamaters();
    boolean hasRequestParameters();
    boolean hasRequestParameter(String parameterName);
    boolean canRetry();
    <P> P retry(Class<P> page, Object... args);
    JiraLoginPage login();
    public boolean isSessionExpired();
    public boolean isXsrfCheckFailed();
}
