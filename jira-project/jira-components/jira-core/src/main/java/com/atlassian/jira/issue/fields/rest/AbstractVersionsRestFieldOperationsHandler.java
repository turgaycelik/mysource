package com.atlassian.jira.issue.fields.rest;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Collections2.filter;

/**
 * @since v5.0
 */
public abstract class AbstractVersionsRestFieldOperationsHandler extends AbstractFieldOperationsHandler<Collection<String>>
{
    private final VersionManager versionManager;

    public AbstractVersionsRestFieldOperationsHandler(VersionManager versionManager, I18nHelper i18nHelper)
    {
        super(i18nHelper);
        this.versionManager = versionManager;
    }

    @Override
    protected Collection<String> getInitialCreateValue()
    {
        return Collections.emptyList();
    }

    @Override
    public Set<String> getSupportedOperations()
    {
        return ImmutableSet.of(StandardOperation.SET.getName(), StandardOperation.ADD.getName(), StandardOperation.REMOVE.getName());
    }

    @Override
    protected Collection<String> handleRemoveOperation(IssueContext issueCtx, Issue issue, String fieldId, Collection<String> currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        String versionId = operationValue.asObjectWithProperty("id", getFieldName(), errors);
        if (versionId == null)
        {
            String versionName = operationValue.asObjectWithProperty("name", getFieldName(), errors);
            if (versionName != null)
            {
                Version version = versionManager.getVersion(issueCtx.getProjectObject().getId(), versionName);
                if (version != null)
                {
                    versionId = version.getId().toString();
                }
                else
                {
                    errors.addError(fieldId, i18nHelper.getText("admin.errors.version.name.invalid", versionName), ErrorCollection.Reason.VALIDATION_FAILED);
                }
            }
        }
        if (versionId == null)
        {
            return currentFieldValue;
        }
        return filter(currentFieldValue, not(equalTo(versionId)));
    }

    protected Long[] toVersionIds(Collection<String> ids, ErrorCollection errors)
    {
        return toLongIds(ids, getFieldName(), errors);
    }

    @Override
    protected Collection<String> handleSetOperation(IssueContext issueCtx, Issue issue, String fieldId, Collection<String> currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        if (operationValue.isNull())
        {
            return Collections.emptyList();
        }
        Project project = issueCtx.getProjectObject();
        if (project == null)
        {
            // This should only happen on create and the error will be reported serarately.
            return Collections.emptyList();
        }

        Set<String> uniqueVersionIds = new HashSet<String>();
        List<String> versionIds = operationValue.asArrayOfObjectsWithId(getFieldName(), errors);
        if (versionIds != null)
        {
            for (String versionId : versionIds)
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
                    uniqueVersionIds.add(version.getId().toString());
                }
                else
                {
                    errors.addError(fieldId, i18nHelper.getText("admin.errors.version.id.invalid", versionId), ErrorCollection.Reason.VALIDATION_FAILED);
                }
            }
        }
        List<String> versionNames = operationValue.asArrayOfObjectsWithProperty("name", getFieldName(), errors);
        if (versionNames != null)
        {
            for (String versionName : versionNames)
            {
                Version version = versionManager.getVersion(project.getId(), versionName);
                if (version != null)
                {
                    uniqueVersionIds.add(version.getId().toString());
                }
                else
                {
                    errors.addError(fieldId, i18nHelper.getText("admin.errors.version.name.invalid", versionName), ErrorCollection.Reason.VALIDATION_FAILED);
                }
            }
        }
        return uniqueVersionIds;
    }

    protected abstract String getFieldName();

    @Override
    protected Collection<String> handleAddOperation(IssueContext issueCtx, Issue issue, String fieldId, Collection<String> currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        List<String> newList = new ArrayList<String>(currentFieldValue);

        String versionId = operationValue.asObjectWithProperty("id", getFieldName(), errors);
        Project project = issueCtx.getProjectObject();
        if (project == null)
        {
            // This should only happen on create and the error will be reported serarately.
            return newList;
        }
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
                if (!newList.contains(versionId))
                {
                    newList.add(version.getId().toString());
                }
            }
            else
            {
                errors.addError(fieldId, i18nHelper.getText("admin.errors.version.id.invalid", versionId), ErrorCollection.Reason.VALIDATION_FAILED);
            }

        }
        else
        {
            String versionName = operationValue.asObjectWithProperty("name", getFieldName(), errors);
            if (versionName != null)
            {
                Version version = versionManager.getVersion(project.getId(), versionName);
                if (version != null)
                {
                    if (!newList.contains(versionId))
                    {
                        newList.add(version.getId().toString());
                    }
                }
                else
                {
                    errors.addError(fieldId, i18nHelper.getText("admin.errors.version.name.invalid", versionName), ErrorCollection.Reason.VALIDATION_FAILED);
                }
            }
            else
            {
                errors.addError(fieldId, i18nHelper.getText("admin.errors.version.id.or.name.required"), ErrorCollection.Reason.VALIDATION_FAILED);
            }
        }

        return newList;
    }

}
