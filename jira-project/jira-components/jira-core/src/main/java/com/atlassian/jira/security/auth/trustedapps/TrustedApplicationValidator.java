package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.util.I18nHelper;

/**
 * Reponsible for doing validations of SimpleTrustedApplication data.
 *
 * @since v3.12
 */
public interface TrustedApplicationValidator
{
    /**
     * Validate the details of the trusted application .
     *
     * @param context the service context that contains the error collection and the user details, must not be null
     * @param helper  so we can i18n the messages
     * @param application the SimpleTrustedApplication containing the trusted application details.
     * @return true if passed, false otherwise
     */
    boolean validate(JiraServiceContext context, I18nHelper helper, SimpleTrustedApplication application);
}
