package com.atlassian.jira.config;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.priority.Priority;

import java.util.List;

/**
 * Manager for {@link Priority}ies
 *
 * @since v5.0
 */
@PublicApi
public interface PriorityManager
{
    /**
     * Create a new priority.
     *
     * @param name name of the priority. Cannot be blank or null. Must be unique.
     * @param description description of the priority
     * @param iconUrl icon url of the priority
     * @param color color for the priority.
     *
     * @return the new {@link Priority}
     */
    Priority createPriority(String name, String description, String iconUrl, String color);

    /**
     * Edit an existing priority.
     *
     * @param priority existing priority.
     * @param name name of the priority
     * @param description description of the priority
     * @param iconUrl icon Url of the priority
     * @param color color of the priority
     */
    void editPriority(Priority priority, String name, String description, String iconUrl, String color);

    /**
     * Returns all priorities. Sorted by sequence.
     * The order can be modified by calling {@link #movePriorityDown(String)} or {@link #movePriorityUp(String)}
     *
     * @return a List of {@link Priority}
     */
    List<Priority> getPriorities();

    /**
     * Removes a priority.
     * When removing the priority it will change the priority of all issues which have the priority that has been deleted to the priority with id
     * specified as the second argument.
     *
     * @param id priority id to remove.
     * @param newPriorityId priority to use for all issues which have the priority which has been removed. Cannot be null.
     */
    void removePriority(String id, String newPriorityId);

    /**
     * Returns a priority.
     *
     * @param id priority id
     *
     * @return a {@link Priority} or null if no priority with the specified id could be found.
     */
    Priority getPriority(String id);

    /**
     * Sets the default priority.
     *
     * @param id priority id
     */
    void setDefaultPriority(String id);

    /**
     * Returns the default priority.
     *
     * @return the default {@link Priority} or if none configured null.
     */
    Priority getDefaultPriority();

    /**
     * Move the resolution up in the order.
     *
     * @param id id of the resolution.
     */
    void movePriorityUp(String id);

    /**
     * Move the resolution down in the order.
     *
     * @param id id of the resolution.
     */
    void movePriorityDown(String id);

}
