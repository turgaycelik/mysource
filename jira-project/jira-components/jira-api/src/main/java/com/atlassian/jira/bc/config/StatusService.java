package com.atlassian.jira.bc.config;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.JiraWorkflow;

import java.util.List;

/**
 *
 * This class contains methods for managing {@link Status}es with validation
 *
 * @since v6.1
 */
@PublicApi
public interface StatusService
{
    /**
     * Maximum length allowed for the name of a status.
     */
    static int MAX_STATUS_LENGTH = 60;


    /**
     * Validates and when validation is performed successfully creates new status basing on given parameters
     *
     * @param user User performing operation
     * @param name Name of the status. Should neither be blank nor duplicate existing name
     * @param description Optional description of the status
     * @param iconUrl URL of the icon for representing given status. May be relative or absolute. Cannot be null
     * @param statusCategory {@link StatusCategory} for given status. Cannot be null
     * @return The result of performed operation
     * @see StatusService#validateCreateStatus(com.atlassian.jira.user.ApplicationUser, String, String, String, com.atlassian.jira.issue.status.category.StatusCategory)
     */
    ServiceOutcome<Status> createStatus(ApplicationUser user, String name, String description, String iconUrl, StatusCategory statusCategory);

    /**
     * Validates new status basing on given parameters
     *
     * @param user User performing operation
     * @param name Name of the status. Should neither be blank nor duplicate existing name
     * @param description Optional description of the status
     * @param iconUrl URL of the icon for representing given status. May be relative or absolute. Cannot be null
     * @param statusCategory {@link StatusCategory} for given status. Cannot be null
     * @return The result of performed operation
     */
    ServiceResult validateCreateStatus(ApplicationUser user, String name, String description, String iconUrl, StatusCategory statusCategory);

    /**
     * Validates and when validation is performed successfully alters given status basing on given parameters
     *
     * @param user User performing operation
     * @param status The status to be edited
     * @param name Name of the status. Should neither be blank nor duplicate existing name
     * @param description Optional description of the status
     * @param iconUrl URL of the icon for representing given status. May be relative or absolute. Cannot be null
     * @param statusCategory {@link StatusCategory} for given status. Cannot be null
     * @return The result of performed operation
     * @see StatusService#validateEditStatus(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.issue.status.Status, String, String, String, com.atlassian.jira.issue.status.category.StatusCategory)
     */
    ServiceOutcome<Status> editStatus(ApplicationUser user, Status status, String name, String description, String iconUrl, StatusCategory statusCategory);

    /**
     * Validates given status basing on given parameters
     *
     * @param user User performing operation
     * @param status The status to be edited
     * @param name Name of the status. Should neither be blank nor duplicate existing name
     * @param description Optional description of the status
     * @param iconUrl URL of the icon for representing given status. May be relative or absolute. Cannot be null
     * @param statusCategory {@link StatusCategory} for given status. Cannot be null
     * @return The result of performed operation
     */
    ServiceResult validateEditStatus(ApplicationUser user, Status status, String name, String description, String iconUrl, StatusCategory statusCategory);

    /**
     * Get a status by id.
     *
     * @param user User performing operation
     * @param id status id
     * @return the {@link Status}, or null if no status with this id exists.
     */
    Status getStatusById(ApplicationUser user, String id);

    /**
     * Removes a status.
     *
     * @param user User performing operation
     * @param status Status
     * @return The result of performed operation
     */
    ServiceResult removeStatus(ApplicationUser user, Status status);

    /**
     * Validates removal of given status
     * @param user User performing operation
     * @param status The Status
     * @return
     */
    ServiceResult validateRemoveStatus(ApplicationUser user, Status status);

    /**
     * Gets a list of workflows which contains given status
     * @param user User performing operation
     * @param status The Status
     * @return
     */
    ServiceOutcome<List<JiraWorkflow>> getAssociatedWorkflows(ApplicationUser user, Status status);

    /**
     * Move a status up the order.
     *
     * @param user User performing operaiton
     * @param id Status id
     * @return
     */
    ServiceResult moveStatusUp(ApplicationUser user, String id);

    /**
     * Move a status down the order.
     *
     * @param user User performing operaiton
     * @param id Status id
     * @return
     */
    ServiceResult moveStatusDown(ApplicationUser user, String id);


    /**
     * Returns a boolean whether status lozenge is enabled or not
     *
     * @return a boolean
     */
    boolean isStatusAsLozengeEnabled();
}
