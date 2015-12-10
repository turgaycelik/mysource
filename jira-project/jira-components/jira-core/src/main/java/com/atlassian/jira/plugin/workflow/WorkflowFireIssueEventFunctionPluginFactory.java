package com.atlassian.jira.plugin.workflow;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import org.apache.log4j.Logger;

import java.util.Map;

public class WorkflowFireIssueEventFunctionPluginFactory extends AbstractWorkflowPluginFactory implements WorkflowPluginFunctionFactory
{
    private static final Logger log = Logger.getLogger(WorkflowFireIssueEventFunctionPluginFactory.class);

    private static final String EVENT_TYPE = "eventType";
    private static final String EVENT_TYPES = "eventTypes";
    private static final String EVENT_TYPE_ID = "eventTypeId";

    private final EventTypeManager eventTypeManager;

    public WorkflowFireIssueEventFunctionPluginFactory(EventTypeManager eventTypeManager)
    {
        this.eventTypeManager = eventTypeManager;
    }

    protected void getVelocityParamsForInput(Map velocityParams)
    {
        // Get all the possible issue events
        velocityParams.put(EVENT_TYPES, getEventTypes());
    }

    protected void getVelocityParamsForEdit(Map velocityParams, AbstractDescriptor descriptor)
    {
        getVelocityParamsForInput(velocityParams);
        EventType eventType = eventTypeManager.getEventType(getEventTypeId(descriptor));
        velocityParams.put(EVENT_TYPE_ID, eventType.getId());
    }

    protected void getVelocityParamsForView(Map velocityParams, AbstractDescriptor descriptor)
    {
        EventType eventType = (EventType)getEventTypes().get(getEventTypeId(descriptor));
        velocityParams.put(EVENT_TYPE, eventType);
    }

    public Map getDescriptorParams(Map conditionParams)
    {
        if (conditionParams != null && conditionParams.containsKey(EVENT_TYPE_ID))
        {
            return EasyMap.build(EVENT_TYPE_ID, extractSingleParam(conditionParams, EVENT_TYPE_ID));
        }

        // Create a 'hard coded' parameter
        return EasyMap.build(EVENT_TYPE_ID, EventType.ISSUE_GENERICEVENT_ID.toString());
    }

    private Map getEventTypes()
    {
        return eventTypeManager.getEventTypesMap();
    }

    private Long getEventTypeId(AbstractDescriptor descriptor)
    {
        if (!(descriptor instanceof FunctionDescriptor))
        {
            throw new IllegalArgumentException("Descriptor must be a FunctionDescriptor.");
        }

        FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;

        try
        {
            return new Long((String) functionDescriptor.getArgs().get("eventTypeId"));
        }
        catch (NumberFormatException e)
        {
            log.error("Error while converting '" + functionDescriptor.getArgs().get("eventTypeId") + "' to a number.", e);
            return null;
        }
    }
}
