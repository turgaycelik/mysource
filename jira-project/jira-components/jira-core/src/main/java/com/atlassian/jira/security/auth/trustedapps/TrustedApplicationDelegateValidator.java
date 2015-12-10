package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.dbc.Null;

/**
 * Combines the semantic and syntactic validators.
 *
 * @since v3.12
 */
public class TrustedApplicationDelegateValidator implements TrustedApplicationValidator
{
    private final TrustedApplicationValidator[] validators;

    /**
     * Convenience ctor for combining two validators.
     * 
     * @param first the first validator (usually Syntactic)
     * @param second the second validator (usually Semantic)
     */
    TrustedApplicationDelegateValidator(TrustedApplicationValidator first, TrustedApplicationValidator second)
    {
        this(new TrustedApplicationValidator[] { first, second });
    }

    TrustedApplicationDelegateValidator(TrustedApplicationValidator[] validators)
    {
        Null.not("validators", validators);
        for (int i = 0; i < validators.length; i++)
        {
            Null.not(String.valueOf(i), validators[i]);
        }
        this.validators = validators;
    }

    /**
     * Runs the validate() method on all validators in order of registration, regardless of failure in earlier validators
     *
     * @param context the service context that contains the error collection and the user details, must not be null
     * @param helper  so we can i18n the messages
     * @param application the SimpleTrustedApplication containing the trusted application details.
     * @return true if passed, false otherwise
     */
    public boolean validate(JiraServiceContext context, I18nHelper helper, SimpleTrustedApplication application)
    {
        boolean result = true;
        for (TrustedApplicationValidator validator : validators)
        {
            result = validator.validate(context, helper, application) && result;
        }
        return result;
    }
}
