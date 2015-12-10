package com.atlassian.jira.issue.fields.rest;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Set;

/**
 * TODO: Document this class / interface here
 *
 * @since v5.0
 */
public class ResolutionRestFieldOperationsHandler extends AbstractFieldOperationsHandler<String>
{
    private final ConstantsManager constantsManager;

    public ResolutionRestFieldOperationsHandler(ConstantsManager constantsManager, I18nHelper i18nHelper)
    {
        super(i18nHelper);
        this.constantsManager = constantsManager;
    }

    @Override
    protected String getInitialValue(Issue issue, ErrorCollection errors)
    {
        return issue.getResolutionId();
    }

    @Override
    protected String getInitialCreateValue()
    {
        return null;
    }

    @Override
    protected void finaliseOperation(String finalValue, IssueInputParameters parameters, ErrorCollection errors)
    {
        parameters.setResolutionId(finalValue);
    }

    @Override
    public Set<String> getSupportedOperations()
    {
        return ImmutableSet.of(StandardOperation.SET.getName());
    }

    @Override
    protected String handleSetOperation(IssueContext issueCtx, Issue issue, String fieldId, String currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        String resolutionId = operationValue.asObjectWithProperty("id", IssueFieldConstants.RESOLUTION, errors);
        if (StringUtils.isBlank(resolutionId))
        {
            String resolutionName = operationValue.asObjectWithProperty("name", IssueFieldConstants.RESOLUTION, errors);
            {
                if (StringUtils.isNotBlank(resolutionName))
                {
                    Collection<Resolution> allResolutions = constantsManager.getResolutionObjects();
                    for (Resolution resolution : allResolutions)
                    {
                        if (resolution.getName().equals(resolutionName))
                        {
                            resolutionId = resolution.getId();
                        }
                    }
                    if (resolutionId == null)
                    {
                        errors.addError(fieldId, i18nHelper.getText("rest.resolution.name.invalid", resolutionName), ErrorCollection.Reason.VALIDATION_FAILED);
                    }
                }
                else
                {
                    errors.addError(fieldId, i18nHelper.getText("rest.resolution.no.name.or.id"), ErrorCollection.Reason.VALIDATION_FAILED);
                }
            }
        }
        return resolutionId;
    }
}
