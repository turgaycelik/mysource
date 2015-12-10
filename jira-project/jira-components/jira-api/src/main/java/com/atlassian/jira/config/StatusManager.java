package com.atlassian.jira.config;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategory;

import java.util.Collection;

/**
 * Manager for {@link Status}es.
 *
 * @since v5.0
 */
@PublicApi
public interface StatusManager
{
    /**
     * Creates a new status.
     *
     * @param name name of the status. Cannot be blank or null and has to be unique.
     * @param description description of the status.
     * @param iconUrl icon url for this status. Cannot be blank or null.
     * @param statusCategory status category of the status. Cannot be null
     * @return the new {@link Status}.
     *
     * @since 6.1
     */
    Status createStatus(String name, String description, String iconUrl, StatusCategory statusCategory);

    /**
     * Creates a new status.
     *
     * @param name name of the status. Cannot be blank or null and has to be unique.
     * @param description description of the status.
     * @param iconUrl icon url for this status. Cannot be blank or null.
     * @return the new {@link Status}.
     */
    Status createStatus(String name, String description, String iconUrl);

    /**
     * Edit an existing status.
     *
     * @param status status to edit.
     * @param name new name. Has to be unique.
     * @param description new description
     * @param iconUrl new icon url
     * @param statusCategory status category of the status. Cannot be null
     *
     * @since 6.1
     */
    void editStatus(Status status, String name, String description, String iconUrl, StatusCategory statusCategory);

    /**
     * Edit an existing status.
     *
     * @param status status to edit.
     * @param name new name. Has to be unique.
     * @param description new description
     * @param iconUrl new icon url
     */
    void editStatus(Status status, String name, String description, String iconUrl);

    /**
     * @return all {@link Status}es
     */
    Collection<Status> getStatuses();

    /**
     * Removes a status.
     *
     * @param id status id
     * @throws IllegalStateException if this status is associated with any workflow.
     * @throws IllegalArgumentException if a status with the given id does not exist.
     */
    void removeStatus(String id);

    /**
     * Get a status by id.
     *
     * @param id status id
     * @return the {@link Status}, or null if no status with this id exists.
     */
    Status getStatus(String id);

    /**
     * Move the status up in the order.
     *
     * @param id id of the status.
     * @throws IllegalArgumentException if a status with the given id does not exist.
     */
    void moveStatusUp(String id);

    /**
     * Move the status down in the order.
     *
     * @param id id of the status.
     * @throws IllegalArgumentException if a status with the given id does not exist.
     */
    void moveStatusDown(String id);
}
