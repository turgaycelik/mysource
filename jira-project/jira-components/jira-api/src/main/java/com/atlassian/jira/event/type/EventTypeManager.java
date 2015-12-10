package com.atlassian.jira.event.type;

import com.atlassian.annotations.PublicApi;
import org.apache.commons.collections.MultiMap;

import java.util.Collection;
import java.util.Map;

/**
 * Manages event types within the system.
 * <p/>
 * Used to add, edit, delete and retrieve event types.
 */
@PublicApi
public interface EventTypeManager
{
    /**
     * Returns an immutable collection of all known event types.
     * <p/>
     * This convenience method is exactly equivalent to
     * {@link #getEventTypesMap()}.{@link java.util.Map#values() values()}.
     *
     * @return an immutable collection of all known event types.
     */
    Collection<EventType> getEventTypes();

    /**
     * Returns an immutable map of all known event type IDs to the corresponding event types.
     * @return an immutable map of all known event type IDs to the corresponding event types.
     */
    Map<Long,EventType> getEventTypesMap();

    /**
     * Returns the event type with the specified {@code id}
     *
     * @param id The ID of the desired event type
     * @return the event type with the specified {@code id}
     * @throws IllegalArgumentException if the specified event type does not exist
     */
    EventType getEventType(Long id);

    /**
     * Determine if the specified {@code eventType} is associated with any workflow or notification scheme.
     *
     * @param eventType event type
     * @return {@code true} if {@code eventType} is associated with any workflow or notification
     *      scheme; {@code false} otherwise.
     */
    boolean isActive(EventType eventType);

    /**
     * Determines which workflows and transitions are associated with the specified eventType.
     * <p/>
     * The event type can be associated with a workflow through a post function on any of the workflow transitions.
     *
     * @param eventType   event type
     * @param statusCheck option to break on first association discovered - used when checking if event type is active
     * @return MultiMap of {@link com.atlassian.jira.web.action.issue.bulkedit.WorkflowTransitionKey}s to transitions
     */
    MultiMap getAssociatedWorkflows(EventType eventType, boolean statusCheck);

    /**
     * Return a mapping of notification scheme ID to its name for each notification scheme that is associated
     * with the specified {@code eventType}
     * <p/>
     * The event type is associated with a notification scheme if the scheme has at least one notification type and
     * template selected for that event type.
     *
     * @param eventType event type
     * @return a mapping of notification scheme ID to its name
     */
    Map<Long,String> getAssociatedNotificationSchemes(EventType eventType);

    void addEventType(EventType eventType);

    void editEventType(Long eventTypeId, String name, String description, Long templateId);

    void deleteEventType(Long eventTypeId);

    boolean isEventTypeExists(String eventTypeName);

    boolean isEventTypeExists(Long eventTypeId);

    void clearCache();
}
