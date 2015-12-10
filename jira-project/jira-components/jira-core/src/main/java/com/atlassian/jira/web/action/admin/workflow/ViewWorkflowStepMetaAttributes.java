package com.atlassian.jira.web.action.admin.workflow;

import java.net.URLEncoder;
import java.util.Map;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowPropertyEditor;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import com.google.common.collect.Maps;
import com.opensymphony.workflow.loader.StepDescriptor;

@WebSudoRequired
public class ViewWorkflowStepMetaAttributes extends AbstractWorkflowAction
{
    private final StepDescriptor step;
    private final WorkflowPropertyEditor.WorkflowPropertyEditorFactory editor;
    private Map<String, String> metaAtrributes;
    private String attributeKey;
    private String attributeValue;

    public ViewWorkflowStepMetaAttributes(JiraWorkflow workflow, StepDescriptor step,
            WorkflowPropertyEditor.WorkflowPropertyEditorFactory editor)
    {
        super(workflow);
        this.step = step;
        this.editor = editor;

        initializeAndFilterAttributes();
    }

    private Map<String, String> getEntityMetaAttributes()
    {
        return step.getMetaAttributes();
    }

    private String getViewRedirect() throws Exception
    {
        return getRedirect("ViewWorkflowStepMetaAttributes.jspa" + getBasicWorkflowParameters() +
                           "&workflowStep=" + getStep().getId());
    }

    public String getRemoveAttributeUrl(String key)
    {
        return "RemoveWorkflowStepMetaAttribute.jspa" + getBasicWorkflowParameters() +
               "&atl_token=" + getXsrfToken() +
               "&workflowStep=" + getStep().getId() +
               "&attributeKey=" + URLEncoder.encode(key);
    }

    public StepDescriptor getStep()
    {
        return step;
    }

    private void initializeAndFilterAttributes()
    {
        metaAtrributes = Maps.newLinkedHashMap();

        // Remove reserved meta attributes from the map - as they should not be shown
        for (final Map.Entry<String, String> entry : getEntityMetaAttributes().entrySet())
        {
            final String key = entry.getKey();
            if (!WorkflowUtil.isReservedKey(key))
            {
                metaAtrributes.put(key, entry.getValue());
            }
        }
    }

    public Map<String, String> getMetaAttributes()
    {
        return metaAtrributes;
    }

    @RequiresXsrfCheck
    public String doAddMetaAttribute() throws Exception
    {
        final ServiceOutcome<?> outcome = createEditor().addProperty(attributeKey, attributeValue);
        if (!outcome.isValid())
        {
            addErrorCollection(outcome.getErrorCollection());
            return INPUT;
        }
        else
        {
            return getViewRedirect();
        }
    }

    @RequiresXsrfCheck
    public String doRemoveMetaAttribute() throws Exception
    {
        final ServiceOutcome<?> outcome = createEditor().deleteProperty(attributeKey);
        if (!outcome.isValid())
        {
            addErrorCollection(outcome.getErrorCollection());
            return INPUT;
        }
        else
        {
            return getViewRedirect();
        }
    }

    private WorkflowPropertyEditor createEditor()
    {
        return editor.stepPropertyEditor(workflow, step)
                .nameKey("attributeKey")
                .valueKey("attributeValue");
    }

    public String getAttributeKey()
    {
        return attributeKey;
    }

    public void setAttributeKey(String attributeKey)
    {
        this.attributeKey = attributeKey;
    }

    public String getAttributeValue()
    {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue)
    {
        this.attributeValue = attributeValue;
    }
}
