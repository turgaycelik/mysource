package com.atlassian.jira.issue.fields;

import com.atlassian.core.util.StringUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.renderer.RenderableField;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.RestAwareField;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.search.handlers.SearchHandlerFactory;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;
import webwork.action.Action;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public abstract class AbstractTextSystemField extends AbstractOrderableNavigableFieldImpl implements RenderableField, RestAwareField
{
    private static final Logger log = Logger.getLogger(AbstractTextSystemField.class);
    private RendererManager rendererManager;

    public AbstractTextSystemField(String id, String name, VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties,
            JiraAuthenticationContext authenticationContext, RendererManager rendererManager, PermissionManager permissionManager, SearchHandlerFactory searchHandlerFactory)
    {
        super(id, name, templatingEngine, applicationProperties, authenticationContext, permissionManager, searchHandlerFactory);
        this.rendererManager = rendererManager;
    }

    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map dispayParameters)
    {
        return getEditHtml(fieldLayoutItem, operationContext, action, issue, dispayParameters);
    }

    protected abstract String getEditTemplateName();

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map<String, Object> velocityParams = getVelocityParams(getAuthenticationContext().getI18nHelper(), displayParams, issue);
        populateVelocityParams(fieldLayoutItem, issue, velocityParams);
        return renderTemplate(getColumnViewTemplateName(), velocityParams);
    }

    protected abstract String getColumnViewTemplateName();

    protected void populateVelocityParams(Map fieldValuesHolder, Map params)
    {
        if (isRenderable())
        {
            params.put("rendererParams", new HashMap());
        }
        // populate the params with some edit rendering preferences for these fields
        params.put(getId(), fieldValuesHolder.get(getId()));
    }

    protected void populateVelocityParams(FieldLayoutItem fieldLayoutItem, Issue issue, Map<String, Object> params)
    {
        params.put("fieldLayoutItem", fieldLayoutItem);
        params.put("rendererManager", rendererManager);
        params.put("issue", issue);
        params.put(getId(), getValueFromIssue(issue));
    }

    public Object getValueFromParams(Map params)
    {
        return params.get(getId());
    }

    public void populateParamsFromString(Map<String, Object> fieldValuesHolder, String stringValue, Issue issue) throws FieldValidationException
    {
        fieldValuesHolder.put(getId(), stringValue);
    }

    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        populateVelocityParams(operationContext.getFieldValuesHolder(), velocityParams);
        if (isRenderable())
        {
            String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;
            velocityParams.put("rendererDescriptor", rendererManager.getRendererForType(rendererType).getDescriptor());
        }
        return renderTemplate(getEditTemplateName(), velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        if (isRenderable())
        {
            velocityParams.put("value", rendererManager.getRenderedContent(fieldLayoutItem, issue));
        }
        else
        {
            velocityParams.put("value", getValueFromIssue(issue));
        }
        return getViewHtml(velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Object value, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        if (isRenderable())
        {
            IssueRenderContext context;
            if (issue != null)
            {
                context = issue.getIssueRenderContext();
            }
            else
            {
                context = new IssueRenderContext(null);
            }

            // get the rendered value without specifying an issue for context
            String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;
            velocityParams.put("value", rendererManager.getRenderedContent(rendererType, (String) value, context));
        }
        else
        {
            velocityParams.put("value", value);
        }
        return getViewHtml(velocityParams);
    }

    private String getViewHtml(Map velocityParams)
    {
        velocityParams.put("invertedCollapsedState", isInvertCollapsedState());
        return renderTemplate("textfield-view.vm", velocityParams);
    }

    /**
     * Override and return true if you want the default state to be collapsed.
     *
      * @return true if the field should be collapsed by default.  False otherwise.
     */
    protected boolean isInvertCollapsedState()
    {
        return false;
    }

    public void createValue(Issue issue, Object value)
    {
        // The field is recorded on the issue itself so there is nothing to do
    }

    public void updateValue(FieldLayoutItem fieldLayoutItem, Issue issue, ModifiedValue modifiedValue, IssueChangeHolder issueChangeHolder)
    {
        // NOTE: No need to update issue in the data store as the value is recorded on the issue itself.
        String currentValue = (modifiedValue.getOldValue() == null ? null : modifiedValue.getOldValue().toString());
        String value = (modifiedValue.getNewValue() == null ? null: modifiedValue.getNewValue().toString());

        if (!valuesEqual(currentValue, value))
        {
            ChangeItemBean cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getChangeLogFieldName(), null, getChangelogValue(currentValue), null, getChangelogValue(value));
            issueChangeHolder.addChangeItem(cib);
        }
    }

    protected boolean valuesEqual(String currentValue, String value)
    {
       return StringUtils.equalsIgnoreLineTerminators(currentValue, value);
    }

    public MessagedResult needsMove(Collection originalIssues, Issue targetIssue, FieldLayoutItem targetFieldLayoutItem)
    {
        for (final Object originalIssue1 : originalIssues)
        {
            Issue originalIssue = (Issue) originalIssue1;

            // As the field is just text - it does not need to be updated unless its empty and is required in the target
            // field layout
            if (targetFieldLayoutItem != null && targetFieldLayoutItem.isRequired() && !hasValue(originalIssue))
            {
                return new MessagedResult(true);
            }

            // As we do not prompt a user to update subtasks values, we do not populate the field with default values.
            if (hasValue(targetIssue) && hasValue(originalIssue) && originalIssue.isSubTask() && targetIssue.isSubTask())
            {
                return new MessagedResult(false);
            }

            // Also if the field is renderable and the render types differ prompt with an edit
            if (isRenderable() && hasValue(originalIssue))
            {
                FieldLayoutItem fieldLayoutItem = null;
                try
                {
                    fieldLayoutItem = ComponentAccessor.getFieldLayoutManager().getFieldLayout(originalIssue.getProjectObject(), originalIssue.getIssueTypeObject().getId()).getFieldLayoutItem(getId());
                }
                catch (DataAccessException e)
                {
                    log.warn(getName() + " field was unable to resolve the field layout item for issue " + originalIssue.getId(), e);
                }

                String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;
                String targetRendererType = (targetFieldLayoutItem != null) ? targetFieldLayoutItem.getRendererType() : null;
                if (!rendererTypesEqual(rendererType, targetRendererType))
                {
                    if (originalIssues.size() > 1)
                    {
                        return new MessagedResult(false, getAuthenticationContext().getI18nHelper().getText("renderer.bulk.move.warning"), MessagedResult.WARNING);
                    }
                    else
                    {
                        return new MessagedResult(true);
                    }
                }
            }
        }
        return new MessagedResult(false);
    }

    public void populateForMove(Map<String, Object> fieldValuesHolder, Issue originalIssue, Issue targetIssue)
    {
        // Set the current value
        fieldValuesHolder.put(getId(), getValueFromIssue(originalIssue));
    }

    public boolean hasValue(Issue issue)
    {
        return TextUtils.stringSet(getValueFromIssue(issue));
    }

    public void populateFromIssue(Map<String, Object> fieldValuesHolder, Issue issue)
    {
        fieldValuesHolder.put(getId(), getValueFromIssue(issue));
    }

    public abstract String getValueFromIssue(Issue issue);

    public boolean isRenderable()
    {
        return true;
    }

    protected String getChangeLogFieldName()
    {
        return getId();
    }

    protected String getChangelogValue(Object value)
    {
        if (value == null || "".equals(value))
            return null;
        else
            return (String) value;
    }

    protected Object getRelevantParams(Map<String, String[]> params)
    {
        String[] value = params.get(getId());
        if (value != null && value.length > 0)
        {
            return value[0];
        }
        else
        {
            return null;
        }
    }

    public void populateDefaults(Map<String, Object> fieldValuesHolder, Issue issue)
    {
        fieldValuesHolder.put(getId(), "");
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        return new FieldTypeInfo(null, null);
    }

    @Override
    public JsonType getJsonSchema()
    {
        return JsonTypeBuilder.system(JsonType.STRING_TYPE, getId());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(Issue issue, boolean renderedVersionRequired, FieldLayoutItem fieldLayoutItem)
    {
        FieldJsonRepresentation fieldJsonRepresentation = new FieldJsonRepresentation(new JsonData(getValueFromIssue(issue)));
        if (renderedVersionRequired && isRenderable() && fieldLayoutItem != null && org.apache.commons.lang.StringUtils.isNotBlank(fieldLayoutItem.getRendererType()))
        {
            fieldJsonRepresentation.setRenderedData(new JsonData(rendererManager.getRenderedContent(fieldLayoutItem, issue)));
        }
        return fieldJsonRepresentation;
    }
}
