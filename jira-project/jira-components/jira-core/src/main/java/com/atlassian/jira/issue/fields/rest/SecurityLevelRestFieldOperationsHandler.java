package com.atlassian.jira.issue.fields.rest;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Set;

/**
 * @since v5.0
 */
public class SecurityLevelRestFieldOperationsHandler extends AbstractFieldOperationsHandler<String>
{
    private final IssueSecurityLevelManager issueSecurityLevelManager;
    private final JiraAuthenticationContext authenticationContext;

    public SecurityLevelRestFieldOperationsHandler(IssueSecurityLevelManager issueSecurityLevelManager, JiraAuthenticationContext authenticationContext, I18nHelper i18nHelper)
    {
        super(i18nHelper);
        this.issueSecurityLevelManager = issueSecurityLevelManager;
        this.authenticationContext = authenticationContext;
    }

    @Override
    public Set<String> getSupportedOperations()
    {
        return ImmutableSet.of(StandardOperation.SET.getName());
    }

    @Override
    protected String getInitialCreateValue()
    {
        return null;
    }

    @Override
    protected String getInitialValue(Issue issue, ErrorCollection errors)
    {
        return issue.getSecurityLevelId() != null ? issue.getSecurityLevelId().toString() : null;
    }

    @Override
    protected void finaliseOperation(String finalValue, IssueInputParameters parameters, ErrorCollection errors)
    {
        Long value = finalValue != null ? Long.valueOf(finalValue) : null;
        parameters.setSecurityLevelId(value);
    }

    @Override
    protected String handleSetOperation(IssueContext issueCtx, Issue issue, String fieldId, String currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        if (operationValue.getData() == null)
        {
            return null;
        }

        String securityLevelId = operationValue.asObjectWithProperty("id", IssueFieldConstants.SECURITY, errors);
        if (securityLevelId == null)
        {
            String securityLevelName = operationValue.asObjectWithProperty("name", IssueFieldConstants.SECURITY, errors);
            {
                if (securityLevelName != null)
                {
                    Collection<IssueSecurityLevel> issueSecurityLevels = issueSecurityLevelManager.getUsersSecurityLevels(issueCtx.getProjectObject(), authenticationContext.getLoggedInUser());
                    for (IssueSecurityLevel securitylevel : issueSecurityLevels)
                    {
                        if (securitylevel.getName().equals(securityLevelName))
                        {
                            securityLevelId = securitylevel.getId().toString();
                        }
                    }
                    if (securityLevelId == null)
                    {
                        errors.addError(fieldId, i18nHelper.getText("rest.securitylevel.name.invalid", securityLevelName), ErrorCollection.Reason.VALIDATION_FAILED);
                    }
                }
                else
                {
                    errors.addError(fieldId, i18nHelper.getText("rest.securitylevel.no.name.or.id"), ErrorCollection.Reason.VALIDATION_FAILED);
                }
            }
        }
        return securityLevelId;
    }

}
