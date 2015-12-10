package com.atlassian.jira.issue.resolution;

import com.atlassian.jira.issue.IssueConstantImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.BaseUrl;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import org.ofbiz.core.entity.GenericValue;

public class ResolutionImpl extends IssueConstantImpl implements Resolution
{
    public ResolutionImpl(GenericValue genericValue, TranslationManager translationManager,
            JiraAuthenticationContext authenticationContext, BaseUrl locator)
    {
        super(genericValue, translationManager, authenticationContext, locator);
    }
}
