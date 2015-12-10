/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.event.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CachedReference;
import com.atlassian.cache.Supplier;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.EventTypeOrderTransformer;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;

import com.google.common.collect.ImmutableMap;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.comparators.TransformingComparator;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

@EventComponent
public class DefaultEventTypeManager implements EventTypeManager
{
    private static final Logger log = Logger.getLogger(DefaultEventTypeManager.class);

    public static final String EVENT_TYPE_ID = "eventTypeId";

    private final OfBizDelegator delegator;
    private final WorkflowManager workflowManager;
    private final NotificationSchemeManager notificationSchemeManager;
    private final Comparator<EventType> eventTypeComparator = new TransformingComparator(new EventTypeOrderTransformer());

    private CachedReference<Map<Long,EventType>> eventTypesMapRef;

    public DefaultEventTypeManager(OfBizDelegator delegator, WorkflowManager workflowManager, NotificationSchemeManager notificationSchemeManager,
            CacheManager cacheManager)
    {
        this.delegator = delegator;
        this.workflowManager = workflowManager;
        this.notificationSchemeManager = notificationSchemeManager;
        eventTypesMapRef = cacheManager.getCachedReference(DefaultEventTypeManager.class, "eventTypesMapRef",
                new Supplier<Map<Long, EventType>>()
                {
                    @Override
                    public Map<Long, EventType> get()
                    {
                        return loadEventTypesMap();
                    }
                });
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        clearCache();
    }

    // ---- Retrieval methods ------------------------------------------------------------------------------------------

    public Collection<EventType> getEventTypes()
    {
        return getEventTypesMap().values();
    }

    public Map<Long,EventType> getEventTypesMap()
    {
        return eventTypesMapRef.get();
    }


    public EventType getEventType(Long id)
    {
        EventType eventType = getEventTypesMap().get(id);
        if (eventType == null)
        {
            GenericValue issueEventTypeGV = retrieveEntityByPrimaryKey(EasyMap.build("id", id));
            if (issueEventTypeGV == null)
            {
                throw new IllegalArgumentException("No event type with id " + id);
            }
            eventType = new EventType(issueEventTypeGV);
        }
        return eventType;
    }

    // ---- Event Type specific methods --------------------------------------------------------------------------------

    public boolean isActive(EventType eventType)
    {
        return !(getAssociatedWorkflows(eventType, true).isEmpty() && getAssociatedNotificationSchemes(eventType).isEmpty());
    }

    public MultiMap getAssociatedWorkflows(EventType eventType, boolean statusCheck)
    {
        MultiMap workflowTransitionMap = new MultiValueMap();

        Collection<JiraWorkflow> workflows = workflowManager.getWorkflows();
        Long eventTypeId = eventType.getId();

        for (final JiraWorkflow workflow : workflows)
        {
            Map<ActionDescriptor, Collection<FunctionDescriptor>> transitionPostFunctionMap = workflowManager.getPostFunctionsForWorkflow(workflow);

            Collection<ActionDescriptor> keys = transitionPostFunctionMap.keySet();

            for (final ActionDescriptor actionDescriptor : keys)
            {
                Collection<FunctionDescriptor> postFunctions = transitionPostFunctionMap.get(actionDescriptor);

                for (final FunctionDescriptor functionDescriptor : postFunctions)
                {
                    if (functionDescriptor.getArgs().containsKey(EVENT_TYPE_ID) &&
                            eventTypeId.equals(new Long((String) functionDescriptor.getArgs().get(EVENT_TYPE_ID))))
                    {
                        workflowTransitionMap.put(workflow.getName(), actionDescriptor);

                        // Exit now as we only need one association for a status check
                        if (statusCheck)
                        {
                            return workflowTransitionMap;
                        }
                    }
                }
            }
        }

        return workflowTransitionMap;
    }

    public Map<Long,String> getAssociatedNotificationSchemes(EventType eventType)
    {
        return notificationSchemeManager.getSchemesMapByConditions(EasyMap.build(EVENT_TYPE_ID, eventType.getId()));
    }

    // ---- Add, Edit, Delete methods ----------------------------------------------------------------------------------

    public void addEventType(EventType eventType)
    {
        Map params = new HashMap();
        params.put("name", eventType.getName());
        params.put("description", eventType.getDescription());
        params.put("templateId", eventType.getTemplateId());
        params.put("type", null);

        delegator.createValue(EventType.EVENT_TYPE, params);

        clearCache();
    }

    public void editEventType(Long eventTypeId, String name, String description, Long templateId)
    {
        GenericValue eventTypeGV = retrieveEntityByPrimaryKey(EasyMap.build("id", eventTypeId));
        eventTypeGV.set("name", name);
        eventTypeGV.set("description", description);
        eventTypeGV.set("templateId", templateId);
        delegator.store(eventTypeGV);

        clearCache();
    }

    public void deleteEventType(Long eventTypeId)
    {
        Map params = new HashMap();
        params.put("id", eventTypeId);
        delegator.removeByAnd(EventType.EVENT_TYPE, params);

        clearCache();
    }

    // ---- Validation methods -----------------------------------------------------------------------------------------

    public boolean isEventTypeExists(String issueEventTypeName)
    {
        if (issueEventTypeName == null)
        {
            throw new IllegalArgumentException("EventTypeName must not be null.");
        }

        for (EventType eventType : getEventTypes())
        {
            if (issueEventTypeName.equals(eventType.getName()))
            {
                return true;
            }
        }
        return false;
    }

    public boolean isEventTypeExists(Long eventTypeId)
    {
        if (eventTypeId == null)
        {
            throw new IllegalArgumentException("EventTypeId must not be null.");
        }
        return getEventTypesMap().containsKey(eventTypeId);
    }

    private Map<Long,EventType> loadEventTypesMap()
    {
        final ImmutableMap.Builder<Long,EventType> eventTypeMap = ImmutableMap.builder();
        for (EventType eventType : retrieveAllEntities())
        {
            eventTypeMap.put(eventType.getId(), eventType);
        }
        return eventTypeMap.build();
    }

    // ---- Database methods -------------------------------------------------------------------------------------------

    /**
     * Return a list of {@link com.atlassian.jira.event.type.EventType}s extracted from the database using the specified params.
     *
     * @return Collection   all event types within the system.
     */
    private Collection<EventType> retrieveAllEntities()
    {

        final Collection<GenericValue> eventTypeGVs = delegator.findAll(EventType.EVENT_TYPE);
        final List<EventType> eventTypes = new ArrayList<EventType>(eventTypeGVs.size());
        for (GenericValue eventTypeGV : eventTypeGVs)
        {
            eventTypes.add(new EventType(eventTypeGV));
        }
        Collections.sort(eventTypes, eventTypeComparator);
        return eventTypes;
    }

    private GenericValue retrieveEntityByPrimaryKey(Map params)
    {
        return delegator.findByPrimaryKey(EventType.EVENT_TYPE, params);
    }

    // ---- Helper methods ---------------------------------------------------------------------------------------------

    public void clearCache()
    {
        eventTypesMapRef.reset();
    }
}
