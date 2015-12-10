package com.atlassian.jira.crowd.embedded;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.ApplicationImpl;
import com.atlassian.crowd.model.application.RemoteAddress;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.util.ValidationFailureException;

import java.util.List;
import java.util.Set;

/**
 * Service to support configuration of JIRA as a Crowd Service.
 */
public interface JaacsService
{
    /**
     * Retrieve a list of configured remote addresses.
     *
     * @param jiraServiceContext The service context.
     * @param applicationId Application Id
     * @return List of configured remote addresses.
     */
    Set<RemoteAddress> getRemoteAddresses(JiraServiceContext jiraServiceContext, long applicationId);

    /**
     * Validate adding a new remote address.
     *
     * @param remoteAddress A new address to add
     * @param jiraServiceContext The service context.
     * @param applicationId Application Id
     * @return true if validation passes
     */
    boolean validateAddRemoteAddress(JiraServiceContext jiraServiceContext, String remoteAddress, long applicationId);

    /**
     * Adding a new remote address.
     *
     * @param jiraServiceContext The service context.
     * @param remoteAddress A new address to add
     * @param applicationId Application Id
     * @return true if validation passes
     */
    boolean addRemoteAddress(JiraServiceContext jiraServiceContext, String remoteAddress, long applicationId);

    /**
     * Validate deleting a remote address.
     *
     * @param jiraServiceContext The service context.
     * @param applicationId Application Id
     * @return true if validation passes and action succeeds
     */
    boolean validateDeleteApplication(JiraServiceContext jiraServiceContext, long applicationId);

    /**
     * Delete a remote address.
     *
     * @param jiraServiceContext The service context.
     * @param applicationId Application Id
     * @return true if validation passes
     */
    boolean deleteApplication(JiraServiceContext jiraServiceContext, long applicationId);

    /**
     * Validate resetting a password.
     *
     * @param password A password string
     * @param jiraServiceContext The service context.
     * @param applicationId Application Id
     * @return true if validation passes
     */
    boolean validateResetPassword(JiraServiceContext jiraServiceContext, String password, long applicationId);

    /**
     * Reset a password.
     *
     * @param password A password string
     * @param jiraServiceContext The service context.
     * @param applicationId Application Id
     * @return true if validation passes and action succeeds
     */
    boolean resetPassword(JiraServiceContext jiraServiceContext, String password, long applicationId);

    /**
     * Returns a list containing all non-permanent Crowd applications.
     *
     * @param remoteUser a User representing the user on whose behalf to perform the call
     * @return a new List
     * @throws com.atlassian.jira.util.ValidationFailureException if there is a problem
     */
    List<Application> findAll(User remoteUser) throws ValidationFailureException;

    /**
     * Returns the Application having the given id.
     *
     * @param remoteUser a User representing the user on whose behalf to perform the call
     * @param applicationId a Long containing an application id, or null if it doesn't exist
     * @return an Application
     * @throws com.atlassian.jira.util.ValidationFailureException if there is a problem
     */
    ApplicationImpl findById(User remoteUser, Long applicationId) throws ValidationFailureException;

    /**
     * Creates a new Application
     *
     * @param remoteUser a User representing the user on whose behalf to perform the call
     * @param application an Application to create
     * @throws com.atlassian.jira.util.ValidationFailureException if there is a problem
     */
    void create(User remoteUser, Application application) throws ValidationFailureException;

    /**
     * Updates an Application. The application having the given id will be updated with the remaining contents.
     *
     * @param remoteUser a User representing the user on whose behalf to perform the call
     * @param updatedApplication an Application to update
     * @throws com.atlassian.jira.util.ValidationFailureException if there is a problem
     */
    void update(User remoteUser, Application updatedApplication) throws ValidationFailureException;
}
