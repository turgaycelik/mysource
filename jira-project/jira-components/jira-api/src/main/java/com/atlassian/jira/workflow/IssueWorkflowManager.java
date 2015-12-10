package com.atlassian.jira.workflow;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;

import com.opensymphony.workflow.loader.ActionDescriptor;

import java.util.Collection;
import java.util.List;

/**
 * Works with workflows on Issues.
 * <p>
 * While {@link WorkflowManager} deals with the global administration of Workflows, this Manager supplies operations
 * that work on the wokrflow and current state of an individual Issue.
 *
 * @since v5.0
 *
 * @see com.atlassian.jira.issue.IssueManager
 * @see WorkflowManager
 */
@PublicApi
public interface IssueWorkflowManager
{
    /**
     * Returns the Workflow actions that are valid for the given Issue in its current state for current user.
     * <p/>
     * This will call getAvailableActions below with {@link com.atlassian.jira.workflow.TransitionOptions#defaults()}.
     *
     * @param issue the Issue
     * @return the Workflow actions that are valid for the given Issue in its current state.
     * @deprecated since v6.3
     */
    @Deprecated
    Collection<ActionDescriptor> getAvailableActions(Issue issue);

    /**
     * Returns the Workflow actions that are valid for the given Issue in its current state for given user.
     * <p/>
     * This will call getAvailableActions below with {@link com.atlassian.jira.workflow.TransitionOptions#defaults()}.
     *
     * @param issue the Issue
     * @param user user to check the permissions for
     * @return the Workflow actions that are valid for the given Issue in its current state.
     * @since v6.3
     */
    Collection<ActionDescriptor> getAvailableActions(Issue issue, ApplicationUser user);

    /**
     * Returns the Workflow actions that are valid for the given Issue in its current state for current user.
     *
     * @param issue the Issue
     * @param transitionOptions options to skip conditions, permissions while performing action validation
     * @return the Workflow actions that are valid for the given Issue in its current state.
     * @since v6.3
     * @deprecated since v6.3
     */
    @Deprecated
    Collection<ActionDescriptor> getAvailableActions(Issue issue, TransitionOptions transitionOptions);

    /**
     * Returns the Workflow actions that are valid for the given Issue in its current state for given user.
     *
     * @param issue the Issue
     * @param transitionOptions options to skip conditions, permissions while performing action validation
     * @param user user to check the permissions for
     * @return the Workflow actions that are valid for the given Issue in its current state.
     * @since v6.3
     */
    Collection<ActionDescriptor> getAvailableActions(Issue issue, TransitionOptions transitionOptions, ApplicationUser user);

    /**
     * Returns the Workflow actions that are valid for the given Issue in its current state for current user.
     * The list is sorted by the sequence number.
     * <p/>
     * This will call getSortedAvailableActions below with {@link com.atlassian.jira.workflow.TransitionOptions#defaults()}.
     *
     * @param issue  the Issue
     * @return the Workflow actions that are valid for the given Issue in its current state.
     * @deprecated since v6.3
     */
    @Deprecated
    List<ActionDescriptor> getSortedAvailableActions(Issue issue);

    /**
     * Returns the Workflow actions that are valid for the given Issue in its current state for given user.
     * The list is sorted by the sequence number.
     * <p/>
     * This will call getSortedAvailableActions below with {@link com.atlassian.jira.workflow.TransitionOptions#defaults()}.
     *
     * @param issue  the Issue
     * @param user user to check the permissions for
     * @return the Workflow actions that are valid for the given Issue in its current state.
     * @since v6.3
     */
    List<ActionDescriptor> getSortedAvailableActions(Issue issue, ApplicationUser user);

    /**
     * Returns the Workflow actions that are valid for the given Issue in its current state for current user.
     * The list is sorted by the sequence number.
     *
     * @param issue  the Issue
     * @param transitionOptions options to skip conditions, permissions while performing action validation
     * @return the Workflow actions that are valid for the given Issue in its current state.
     * @since v6.3
     * @deprecated since v6.3
     */
    @Deprecated
    List<ActionDescriptor> getSortedAvailableActions(Issue issue, TransitionOptions transitionOptions);

    /**
     * Returns the Workflow actions that are valid for the given Issue in its current state for given user.
     * The list is sorted by the sequence number.
     *
     * @param issue  the Issue
     * @param transitionOptions options to skip conditions, permissions while performing action validation
     * @param user user to check the permissions for
     * @return the Workflow actions that are valid for the given Issue in its current state.
     * @since v6.3
     */
    List<ActionDescriptor> getSortedAvailableActions(Issue issue, TransitionOptions transitionOptions, ApplicationUser user);

    /**
     * Returns true if the given transition ID is valid for the given issue and current user.
     * <p/>
     * This will call isValidAction below with {@link com.atlassian.jira.workflow.TransitionOptions#defaults()}.
     *
     * @param issue the Issue
     * @param action the id of the action we want to transition
     * @return true if it is ok to use the given transition on this issue.
     * @deprecated since v6.3
     */
    @Deprecated
    boolean isValidAction(Issue issue, int action);

    /**
     * Returns true if the given transition ID is valid for the given issue and given user.
     * <p/>
     * This will call isValidAction below with {@link com.atlassian.jira.workflow.TransitionOptions#defaults()}.
     *
     * @param issue the Issue
     * @param actionId the id of the action we want to transition
     * @param user user to check the permissions for
     * @return true if it is ok to use the given transition on this issue.
     * @since v6.3
     */
    boolean isValidAction(Issue issue, int actionId, ApplicationUser user);

    /**
     * Returns true if the given transition ID is valid for the given issue and current user.
     *
     * @param issue the Issue
     * @param action the id of the action we want to transition
     * @param transitionOptions options to skip conditions, permissions while performing action validation
     * @return true if it is ok to use the given transition on this issue.
     * @since v6.3
     * @deprecated since v6.3
     */
    @Deprecated
    boolean isValidAction(Issue issue, int action, TransitionOptions transitionOptions);

    /**
     * Returns true if the given transition ID is valid for the given issue and current user.
     *
     * @param issue the Issue
     * @param actionId the id of the action we want to transition
     * @param transitionOptions options to skip conditions, permissions while performing action validation
     * @param user user to check the permissions for
     * @return true if it is ok to use the given transition on this issue.
     * @since v6.3
     */
    boolean isValidAction(Issue issue, int actionId, TransitionOptions transitionOptions, ApplicationUser user);
}
