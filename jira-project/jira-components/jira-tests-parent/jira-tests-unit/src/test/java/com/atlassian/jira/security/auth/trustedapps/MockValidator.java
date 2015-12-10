package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.util.I18nHelper;

class MockValidator implements TrustedApplicationValidator
{
    private final boolean result;
    int calledCount;

    MockValidator()
    {
        this(true);
    }

    MockValidator(boolean result)
    {
        this.result = result;
    }

    public boolean validate(JiraServiceContext context, I18nHelper helper, SimpleTrustedApplication builder)
    {
        calledCount++;
        return result;
    }
}
