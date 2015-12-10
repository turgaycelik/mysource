package com.atlassian.jira.issue.customfields.impl.rest;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.rest.AbstractFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.StandardOperation;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.ImmutableSet;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @since v5.0
 */
public class SingleVersionCustomFieldOperationsHandler extends AbstractFieldOperationsHandler<String>
{
    private final VersionManager versionManager;
    private final CustomField field;

    public SingleVersionCustomFieldOperationsHandler(CustomField field, VersionManager versionManager, I18nHelper i18nHelper)
    {
        super(i18nHelper);
        this.versionManager = versionManager;
        this.field = field;
    }

    @Override
    public Set<String> getSupportedOperations()
    {
        return ImmutableSet.of(StandardOperation.SET.getName());
    }

    @Override
    protected String handleSetOperation(IssueContext issueCtx, Issue issue, String fieldId, String currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        if (operationValue.getData() == null)
        {
            return "";
        }
        Project project = issueCtx.getProjectObject();
        if (project == null)
        {
            // This should only happen on create and the error will be reported serarately.
            return "";
        }
        String versionId = operationValue.asObjectWithProperty("id", fieldId, errors);
        if (versionId != null)
        {

            Version version = null;
            try
            {
                version = versionManager.getVersion(Long.valueOf(versionId));
            }
            catch (NumberFormatException e)
            {
                errors.addError(fieldId, i18nHelper.getText("admin.errors.version.id.invalid", versionId), ErrorCollection.Reason.VALIDATION_FAILED);
            }
            if (version != null && version.getProjectObject().getId().equals(project.getId()))
            {
                return versionId;
            }
            else
            {
                errors.addError(fieldId, i18nHelper.getText("admin.errors.version.id.invalid", versionId), ErrorCollection.Reason.VALIDATION_FAILED);
            }
        }
        else
        {
            String versionName = operationValue.asObjectWithProperty("name", fieldId, errors);
            if (versionName != null)
            {
                Version version = versionManager.getVersion(project.getId(), versionName);
                if (version != null && version.getProjectObject().getId().equals(project.getId()))
                {
                    return version.getId().toString();
                }
                else
                {
                    errors.addError(fieldId, i18nHelper.getText("admin.errors.version.name.invalid", versionName), ErrorCollection.Reason.VALIDATION_FAILED);
                }
            }
        }
        return "";
    }

    @Override
    protected String getInitialCreateValue()
    {
        return null;
    }

    @Override
    protected String getInitialValue(Issue issue, ErrorCollection errors)
    {
        List<Version> fieldValue = (List<Version>) field.getValue(issue);
        if (fieldValue != null && fieldValue.size() > 0)
        {
            return fieldValue.get(0).getId().toString();
        }
        return null;
    }

    @Override
    protected void finaliseOperation(String finalValue, IssueInputParameters parameters, ErrorCollection errors)
    {
        parameters.addCustomFieldValue(field.getId(), finalValue);
    }
}
