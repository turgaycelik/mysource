package com.atlassian.jira.issue.fields.rest;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.beans.ComponentJsonBean;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

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
public class ComponentsRestFieldOperationsHandler extends AbstractFieldOperationsHandler<Collection<String>>
{
    private final ProjectComponentManager projectComponentManager;

    public ComponentsRestFieldOperationsHandler(ProjectComponentManager projectComponentManager, I18nHelper i18nHelper)
    {
        super(i18nHelper);
        this.projectComponentManager = projectComponentManager;
    }

    @Override
    public Set<String> getSupportedOperations()
    {
        return ImmutableSet.of(StandardOperation.ADD.getName(), StandardOperation.SET.getName(), StandardOperation.REMOVE.getName());
    }

    @Override
    protected Collection<String> handleRemoveOperation(IssueContext issueCtx, Issue issue, String fieldId, Collection<String> currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        final ComponentJsonBean bean = operationValue.convertValue(IssueFieldConstants.COMPONENTS, ComponentJsonBean.class, errors);
        if (bean == null) {
            return currentFieldValue;
        }
        String componentId = bean.getId();
        if (componentId == null)
        {
            String componentName = bean.getName();
            if (componentName != null)
            {
                ProjectComponent component = projectComponentManager.findByComponentName(issueCtx.getProjectObject().getId(), componentName);
                if (component != null)
                {
                    componentId = component.getId().toString();
                }
                else
                {
                    errors.addError(fieldId, i18nHelper.getText("admin.errors.component.name.invalid", componentName), ErrorCollection.Reason.VALIDATION_FAILED);
                }
            }
        }
        if (componentId == null)
        {
            errors.addError(fieldId, i18nHelper.getText("admin.errors.component.id.or.name.required"), ErrorCollection.Reason.VALIDATION_FAILED);
            return currentFieldValue;
        }
        return filter(currentFieldValue, not(equalTo(componentId)));
    }

    @Override
    protected Collection<String> handleSetOperation(IssueContext issueCtx, Issue issue, String fieldId, Collection<String> currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        Set<String> uniqueComponentIds = new HashSet<String>();
        List<String> componentIds = operationValue.asArrayOfObjectsWithId(IssueFieldConstants.COMPONENTS, errors);
        if (componentIds != null)
        {
            uniqueComponentIds.addAll(componentIds);
        }
        List<String> componentNames = operationValue.asArrayOfObjectsWithProperty("name", IssueFieldConstants.COMPONENTS, errors);
        if (componentNames != null)
        {
            for (String componentName : componentNames)
            {
                ProjectComponent component = projectComponentManager.findByComponentName(issueCtx.getProjectObject().getId(), componentName);
                if (component != null)
                {
                    uniqueComponentIds.add(component.getId().toString());
                }
                else
                {
                    errors.addError(fieldId, i18nHelper.getText("admin.errors.component.name.invalid", componentName), ErrorCollection.Reason.VALIDATION_FAILED);
                }
            }
        }
        return uniqueComponentIds;
    }

    @Override
    protected Collection<String> handleAddOperation(IssueContext issueCtx, Issue issue, String fieldId, Collection<String> currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        String componentId = operationValue.asObjectWithProperty("id", IssueFieldConstants.COMPONENTS, errors);
        if (componentId == null)
        {
            String componentName = operationValue.asObjectWithProperty("name", IssueFieldConstants.COMPONENTS, errors);
            if (componentName != null)
            {
                ProjectComponent component = projectComponentManager.findByComponentName(issueCtx.getProjectObject().getId(), componentName);
                if (component != null)
                {
                    componentId = component.getId().toString();
                }
                else
                {
                    errors.addError(fieldId, i18nHelper.getText("admin.errors.component.name.invalid", componentName), ErrorCollection.Reason.VALIDATION_FAILED);
                }
            }
            else
            {
                errors.addError(fieldId, i18nHelper.getText("admin.errors.component.id.or.name.required"), ErrorCollection.Reason.VALIDATION_FAILED);
            }
        }
        if (componentId == null)
        {
            return null;
        }
        List<String> newList = new ArrayList<String>(currentFieldValue);
        if (!newList.contains(componentId)) {
            newList.add(componentId);
        }
        return newList;
    }

    @Override
    protected Collection<String> getInitialCreateValue()
    {
        return Collections.emptyList();
    }

    @Override
    protected Collection<String> getInitialValue(Issue issue, ErrorCollection errors)
    {
        Iterable<String> components = Iterables.transform(issue.getComponentObjects(), new Function<ProjectComponent, String>()
        {
            @Override
            public String apply(ProjectComponent from)
            {
                return from.getId().toString();
            }
        });
        return Lists.newArrayList(components);
    }

    @Override
    protected void finaliseOperation(Collection<String> finalValue, IssueInputParameters parameters, ErrorCollection errors)
    {
        Long[] ids = toLongIds(finalValue, IssueFieldConstants.COMPONENTS, errors);
        if (ids != null)
        {
            parameters.setComponentIds(ids);
        }
    }
}
