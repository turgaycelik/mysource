/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.eventtype;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.DefaultEventTypeManager;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.template.TemplateManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebSudoRequired
public class ListEventTypes extends JiraWebActionSupport
{
    private static final int SHORT_LIST_COUNT = 3;

    private final EventTypeManager eventTypeManager;
    private final WorkflowManager workflowManager;
    private final TemplateManager templateManager;

    private String name;
    private String description;
    private Long templateId;

    private String type;
    private Long eventTypeId;
    // Used to confirm edit and delete actions
    private boolean confirmed;
    private List<Scheme> allNotificationSchemes;

    public ListEventTypes(EventTypeManager eventTypeManager, WorkflowManager workflowManager, TemplateManager templateManager)
    {
        this.eventTypeManager = eventTypeManager;
        this.workflowManager = workflowManager;
        this.templateManager = templateManager;
    }

    // ---- Webwork actions --------------------------------------------------------------------------------------------
    protected String doExecute() throws Exception
    {
        return SUCCESS;
    }

    @RequiresXsrfCheck
    public String doAddEventType()
    {
        if (!TextUtils.stringSet(name))
        {
            addError("name", getText("admin.event.types.errors.specify.name"));
        }
        else if (eventTypeManager.isEventTypeExists(name))
        {
            addError("name", getText("admin.event.types.errors.not.unique"));
        }

        if (templateId == null || templateId == -1)
        {
            addError("templateId", getText("admin.event.types.errors.select.template"));
        }

        if (invalidInput())
            return INPUT;

        EventType newEventType = new EventType(name, description, templateId);
        eventTypeManager.addEventType(newEventType);

        // reset "Add New Event" fields
        setTemplateId(-1L);
        setName(null);
        setDescription(null);

        return getRedirect("ListEventTypes.jspa");
    }

    @RequiresXsrfCheck
    public String doDeleteEventType()
    {
        EventType eventType = eventTypeManager.getEventType(eventTypeId);

        if (!eventTypeManager.isActive(eventType))
        {
            if (confirmed)
            {
                eventTypeManager.deleteEventType(eventTypeId);

                if (invalidInput())
                    return ERROR;

                return getRedirect("ListEventTypes.jspa");
            }
            else
            {
                return INPUT;
            }
        }
        else
        {
            addErrorMessage(getText("admin.event.types.errors.delete.active"));
            return INPUT;
        }
    }

    @RequiresXsrfCheck
    public String doEditEventType()
    {
        if (confirmed)
        {
            // Validate name
            if (TextUtils.stringSet(name))
            {
                // Check if name is unique or if name is not changed
                if (eventTypeManager.isEventTypeExists(name) && !name.equals(eventTypeManager.getEventType(eventTypeId).getName()))
                {
                    addError("name", getText("admin.event.types.errors.not.unique"));
                }
            }
            else
            {
                addError("name", getText("admin.event.types.errors.specify.name"));
            }

            // Validate template selection
            if (templateId == null || templateId == -1)
            {
                addError("templateId", getText("admin.event.types.errors.select.template"));
            }

            if (invalidInput())
            {
                return INPUT;
            }
            else
            {
                // Commit the edit
                eventTypeManager.editEventType(eventTypeId, name, description, templateId);
                // Reset the name and description so as 'AddEventType' fields are blank when returning to list view
                setName(null);
                setDescription(null);
                setTemplateId(-1L);

                return getRedirect("ListEventTypes.jspa");
            }
        }
        else
        {
            return INPUT;
        }
    }

    /**
     * Determine a suitable stepId for the *ViewWorkflowTransition* link in the event type list.
     * It is only necessary to retireve the first step id for the link as the screen for the transition is the same for
     * each step.
     * <p/>
     * The initial step *Create Issue* does not have a related step id and is not needed for the link - so return null
     * in this case.
     *
     * @param workflowName
     * @param actionDescriptorId
     */
    public String getStepId(String workflowName, long actionDescriptorId)
    {
        return workflowManager.getStepId(actionDescriptorId, workflowName);
    }

    /**
     * Creates a short list of the workflow transitions limited to {@link ListEventTypes#SHORT_LIST_COUNT}
     *
     * @param transitions   a collection of workflow transitions   
     * @return List         a list of workflow transitions limited to {@link ListEventTypes#SHORT_LIST_COUNT} in size
     */
    public List getShortList(Collection<ActionDescriptor> transitions)
    {
        int count = 0;
        List<ActionDescriptor> shortList = new ArrayList<ActionDescriptor>();

        for (ActionDescriptor transition : transitions)
        {
            shortList.add(transition);
            count++;

            if (count >= SHORT_LIST_COUNT)
            {
                break;
            }
        }

        return shortList;
    }

    public EventTypeManager getEventTypeManager()
    {
        return eventTypeManager;
    }


    /**
     * Determines which workflows and transitions are associated with the specified eventType.
     * <p/>
     * The event type can be associated with a workflow through a post function on any of the workflow transitions.
     *
     * @param eventType   event type
     * @return MultiMap     {@link com.atlassian.jira.web.action.issue.bulkedit.WorkflowTransitionKey}s -> transitions
     */
    public MultiMap getAssociatedWorkflows(EventType eventType)
    {
        // JRA-25880 For performance we do this work in the Action, so we can re-use the results of workflowManager.getWorkflows()
        Collection<JiraWorkflow> workflows = getWorkflows();
        Long eventTypeId = eventType.getId();

        MultiMap workflowTransitionMap = new MultiHashMap();
        for (final JiraWorkflow workflow : workflows)
        {
            Map<ActionDescriptor, Collection<FunctionDescriptor>> transitionPostFunctionMap = workflowManager.getPostFunctionsForWorkflow(workflow);

            Collection<ActionDescriptor> keys = transitionPostFunctionMap.keySet();

            for (final ActionDescriptor actionDescriptor : keys)
            {
                Collection<FunctionDescriptor> postFunctions = transitionPostFunctionMap.get(actionDescriptor);

                for (final FunctionDescriptor functionDescriptor : postFunctions)
                {
                    if (functionDescriptor.getArgs().containsKey(DefaultEventTypeManager.EVENT_TYPE_ID) &&
                            eventTypeId.equals(new Long((String) functionDescriptor.getArgs().get(DefaultEventTypeManager.EVENT_TYPE_ID))))
                    {
                        workflowTransitionMap.put(workflow.getName(), actionDescriptor);
                    }
                }
            }
        }

        return workflowTransitionMap;
    }

    Collection<JiraWorkflow> workflows = null;
    private Collection<JiraWorkflow> getWorkflows()
    {
        if (workflows == null)
        {
            // Expensive call
            workflows = workflowManager.getWorkflows();
        }
        return workflows;
    }

    /**
     * Retrieve a map of scheme ids to scheme names that are associsated with this EventType
     *
     * @param eventType the EventType
     * @return Map of scheme ids -> scheme names
     */
    public Map<Long, String> getAssociatedNotificationSchemes(EventType eventType)
    {
        Long eventTypeId = eventType.getId();

        // JRA-25880 For performance we do this work in the Action, so we can re-use the results of NotificationSchemeManager.getSchemeObjects()
        final List<Scheme> notificationSchemes = getAllNotificationSchemes();
        Map<Long,String> associatedSchemes = new HashMap<Long, String>();
        for (Scheme notificationScheme : notificationSchemes)
        {
            for (SchemeEntity schemeEntity : notificationScheme.getEntities())
            {
                if (eventTypeId.equals(schemeEntity.getEntityTypeId()))
                {
                    associatedSchemes.put(notificationScheme.getId().longValue(), notificationScheme.getName());
                }
            }
        }
        return associatedSchemes;
    }

    private List<Scheme> getAllNotificationSchemes()
    {
        if (allNotificationSchemes == null)
        {
            // Expensive call
            allNotificationSchemes = ComponentAccessor.getNotificationSchemeManager().getSchemeObjects();
        }
        return allNotificationSchemes;
    }

    public TemplateManager getTemplateManager()
    {
        return templateManager;
    }

    // ---- Getters & Setters ------------------------------------------------------------------------------------------

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed)
    {
        this.confirmed = confirmed;
    }

    public Long getEventTypeId()
    {
        return eventTypeId;
    }

    public void setEventTypeId(Long eventTypeId)
    {
        this.eventTypeId = eventTypeId;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public Long getTemplateId()
    {
        return templateId;
    }

    public void setTemplateId(Long templateId)
    {
        this.templateId = templateId;
    }

}
