package com.atlassian.jira.security.auth.trustedapps;

/**
 * Resolves the production dependencies (collates all the individual validators).
 *
 * @since v3.12
 */
///CLOVER:OFF
public class DefaultTrustedApplicationValidator extends TrustedApplicationDelegateValidator
{
    public DefaultTrustedApplicationValidator(TrustedApplicationManager manager)
    {
        super(new TrustedApplicationSyntacticValidator(), new TrustedApplicationSemanticValidator(manager));
    }
}
