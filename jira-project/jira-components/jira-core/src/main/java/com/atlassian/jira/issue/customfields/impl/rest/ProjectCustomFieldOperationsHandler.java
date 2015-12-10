package com.atlassian.jira.issue.customfields.impl.rest;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.rest.StandardOperation;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.ImmutableSet;
import org.ofbiz.core.entity.GenericValue;

import java.util.Set;

/**
 * @since v5.0
 */
public class ProjectCustomFieldOperationsHandler extends AbstractCustomFieldOperationsHandler<String>
{
    private final ProjectManager projectManager;

    public ProjectCustomFieldOperationsHandler(ProjectManager projectManager, CustomField field, I18nHelper i18nHelper)
    {
        super(field, i18nHelper);
        this.projectManager = projectManager;
    }

    @Override
    public Set<String> getSupportedOperations()
    {
        return ImmutableSet.of(StandardOperation.SET.getName());
    }

    @Override
    protected String handleSetOperation(IssueContext issueCtx, Issue issue, String currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        if (operationValue.isNull())
        {
            return null;
        }
        // Projects can be identified by Key or Id.  Id has priority as always.
        String projectId = operationValue.asObjectWithProperty("id", field.getId(), errors);
        if (projectId == null)
        {
            String projectKey = operationValue.asObjectWithProperty("key", field.getId(), errors);
            {
                if (projectKey != null)
                {
                    Project project = projectManager.getProjectObjByKey(projectKey);
                    if (project == null)
                    {
                        errors.addError(field.getId(), i18nHelper.getText("rest.project.key.invalid", projectKey), ErrorCollection.Reason.VALIDATION_FAILED);
                    }
                    else
                    {
                        projectId = project.getId().toString();
                    }
                }
                else
                {
                    errors.addError(field.getId(), i18nHelper.getText("rest.project.key.or.id.required"), ErrorCollection.Reason.VALIDATION_FAILED);
                }
            }
        }
        return projectId;
    }

    /**
     * compute the "currentValue" to be passed to applyOperation()
     */
    protected String getInitialValue(Issue issue, ErrorCollection errors)
    {
        GenericValue projectGV = (GenericValue) field.getValue(issue);
        if (projectGV == null)
        {
            return null;
        }
        Project project = new ProjectImpl(projectGV);
        return project.getId().toString();
    }

    /**
     * compute the "currentValue" to be passed to applyOperation()
     * @param issueCtx
     */
    protected String getInitialCreateValue(IssueContext issueCtx)
    {
        return null;
    }

    @Override
    protected void finaliseOperation(String finalValue, IssueInputParameters parameters, ErrorCollection errors)
    {
        parameters.addCustomFieldValue(field.getId(), finalValue);
    }

}
