package com.atlassian.jira.plugin.workflow;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.web.action.issue.EditIssue;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateIssueFieldFunctionPluginFactory extends AbstractWorkflowPluginFactory implements WorkflowPluginFunctionFactory
{
    private static final String PARAM_NAME = "eventType";
    private final FieldManager fieldManager;
    private final UserKeyService userKeyService;
    private final List fields;
    public static final String PARAM_FIELD_ID = "fieldId";
    public static final String PARAM_FIELD_VALUE = "fieldValue";
    public static final String TARGET_FIELD_NAME = "field.name";
    public static final String TARGET_FIELD_VALUE = "field.value";
    public static final String DEFAULT_VALUE = "-1";

    public UpdateIssueFieldFunctionPluginFactory(FieldManager fieldManager, UserKeyService userKeyService)
    {
        this.fieldManager = fieldManager;
        this.fields = new ArrayList(9);
        this.userKeyService = userKeyService;

        //This list of fields is hardcoded on purpose.  We dont want users to be able to edit just
        //any field via this post function.
        this.fields.add(fieldManager.getField(IssueFieldConstants.ASSIGNEE));
        this.fields.add(fieldManager.getField(IssueFieldConstants.DESCRIPTION));
        this.fields.add(fieldManager.getField(IssueFieldConstants.ENVIRONMENT));
        this.fields.add(fieldManager.getField(IssueFieldConstants.PRIORITY));
        this.fields.add(fieldManager.getField(IssueFieldConstants.RESOLUTION));
        this.fields.add(fieldManager.getField(IssueFieldConstants.SUMMARY));
        this.fields.add(fieldManager.getField(IssueFieldConstants.TIME_ORIGINAL_ESTIMATE));
        this.fields.add(fieldManager.getField(IssueFieldConstants.TIME_ESTIMATE));
        this.fields.add(fieldManager.getField(IssueFieldConstants.TIME_SPENT));
    }

    protected void getVelocityParamsForInput(Map velocityParams)
    {
        velocityParams.put("fields", fields);
        velocityParams.put("factory", this);
    }

    protected void getVelocityParamsForEdit(Map velocityParams, AbstractDescriptor descriptor)
    {
        getVelocityParamsForInput(velocityParams);

        if (!(descriptor instanceof FunctionDescriptor))
        {
            throw new IllegalArgumentException("Descriptor must be a FunctionDescriptor.");
        }

        // Get the current arguments
        FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;
        final String fieldName = (String) functionDescriptor.getArgs().get(TARGET_FIELD_NAME);
        velocityParams.put(PARAM_FIELD_ID, fieldName);
        String value = (String) functionDescriptor.getArgs().get(TARGET_FIELD_VALUE);
        if (value == null || value.equals("null"))
        {
            velocityParams.put(PARAM_FIELD_VALUE, null);
        }
        else
        {
            boolean persistedValueIsUserkey = IssueFieldConstants.ASSIGNEE.equals(fieldName) && !DEFAULT_VALUE.equals(value);
            String valueTranslatedForEdit = persistedValueIsUserkey ? userKeyService.getUsernameForKey(value) : value;
            velocityParams.put(PARAM_FIELD_VALUE, valueTranslatedForEdit);
        }
    }

    protected void getVelocityParamsForView(Map velocityParams, AbstractDescriptor descriptor)
    {
        if (!(descriptor instanceof FunctionDescriptor))
        {
            throw new IllegalArgumentException("Descriptor must be a FunctionDescriptor.");
        }

        FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;

        final String fieldName = (String) functionDescriptor.getArgs().get(TARGET_FIELD_NAME);
        Field field = fieldManager.getField(fieldName);
        velocityParams.put(PARAM_FIELD_ID, field.getNameKey());

        String fieldValue = (String) functionDescriptor.getArgs().get(TARGET_FIELD_VALUE);

        boolean persistedValueIsUserkey = IssueFieldConstants.ASSIGNEE.equals(fieldName) && !DEFAULT_VALUE.equals(fieldValue);
        String valueTranslatedForView;
        if (persistedValueIsUserkey)
        {
            valueTranslatedForView = userKeyService.getUsernameForKey(fieldValue);
        }
        else
        {
            valueTranslatedForView = fieldValue;
        }
        velocityParams.put(PARAM_FIELD_VALUE, getViewHtml(field, valueTranslatedForView));
    }

    public Map getDescriptorParams(Map conditionParams)
    {
        Map params = new HashMap();

        String fieldId = extractSingleParam(conditionParams, PARAM_FIELD_ID);
        params.put(TARGET_FIELD_NAME, fieldId);

        String fieldValue = extractSingleParam(conditionParams, PARAM_FIELD_VALUE);

        String valueTranslatedForPersistence;
        if (IssueFieldConstants.ASSIGNEE.equals(fieldId) && !DEFAULT_VALUE.equals(fieldValue))
        {
            valueTranslatedForPersistence = userKeyService.getKeyForUsername(fieldValue);
        }
        else
        {
            valueTranslatedForPersistence = fieldValue;
        }

        params.put(TARGET_FIELD_VALUE, valueTranslatedForPersistence);
        return params;
    }

    public String getEditHtml(final OrderableField field, final Object value)
    {
        OperationContext operationContext = new OperationContext()
        {
            public Map<String, Object> getFieldValuesHolder()
            {
                return EasyMap.build(field.getId(), value);
            }

            public IssueOperation getIssueOperation()
            {
                return new IssueOperation()
                {
                    // These methods are not used anywhere
                    public String getNameKey()
                    {
                        return "Populate Workflow Function";
                    }

                    public String getDescriptionKey()
                    {
                        return "Select a value that should be set on an issue when this function is executed.";
                    }
                };
            }
        };

        return field.getEditHtml(null,
                operationContext,
                JiraUtils.loadComponent(EditIssue.class),
                null,
                EasyMap.build(OrderableField.NO_HEADER_PARAM_KEY, "true", "displayNone", "true"));
    }

    // This method is called on different types of fields - not just Orderable fields
    public boolean hasCustomEditTemplate(Field field)
    {
        return IssueFieldConstants.PRIORITY.equals(field.getId()) ||
                IssueFieldConstants.ASSIGNEE.equals(field.getId()) ||
                IssueFieldConstants.RESOLUTION.equals(field.getId());
    }

    private boolean hasCustomViewTemplate(Field field)
    {
        return hasCustomEditTemplate(field);
    }

    private Object getViewHtml(Field field, String fieldValue)
    {
        if (hasCustomViewTemplate(field))
        {
            OrderableField orderableField = (OrderableField) field;
            String fieldId = field.getId();
            if (IssueFieldConstants.PRIORITY.equals(fieldId) || IssueFieldConstants.RESOLUTION.equals(fieldId))
            {
                GenericValue value = (GenericValue) orderableField.getValueFromParams(ImmutableMap.of(fieldId, fieldValue));
                if (value != null)
                {
                    return value.getString("name");
                }
            }
            else if (IssueFieldConstants.ASSIGNEE.equals(fieldId))
            {
                if (fieldValue != null) {
                    return orderableField.getValueFromParams(ImmutableMap.of(fieldId, fieldValue));
                }
            }
        }

        return fieldValue;
    }
}
