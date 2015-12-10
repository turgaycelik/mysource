package com.atlassian.jira.bc.workflow;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.DraftWorkflowScheme;
import com.atlassian.jira.workflow.WorkflowScheme;

import javax.annotation.Nonnull;

@ExperimentalApi
public interface WorkflowSchemeService
{
    /**
     * Return a builder that can be used to create a {@link AssignableWorkflowScheme}. The actual scheme will not
     * be created in JIRA until the {@link #createScheme(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.workflow.AssignableWorkflowScheme)}
     * method is called.
     *
     * @return a builder that can be used to create a new {@link AssignableWorkflowScheme}.
     */
    AssignableWorkflowScheme.Builder assignableBuilder();

    /**
     * Return a builder that can be used to create a {@link DraftWorkflowScheme} for the passed workflow scheme.
     * The actual scheme will not be created in JIRA until the {@link #createDraft(com.atlassian.jira.user.ApplicationUser, DraftWorkflowScheme)}
     * method is called.
     *
     * @return a builder that can be used to create a new {@link DraftWorkflowScheme}.
     */
    DraftWorkflowScheme.Builder draftBuilder(AssignableWorkflowScheme parent);

    /**
     * Create a new workflow scheme.
     *
     * @param creator the user creating the scheme.
     * @param scheme the scheme to create.
     * @return the result of the operation.
     */
    @Nonnull
    ServiceOutcome<AssignableWorkflowScheme> createScheme(ApplicationUser creator, @Nonnull AssignableWorkflowScheme scheme);

    /**
     * Create a draft for the passed workflow scheme.
     *
     * To create a draft scheme the parent must be:
     * <ol>
     *     <li>Active: Used by a project.</li>
     *     <li>Draftless: The parent must not already has a draft workflow scheme.</li>
     *     <li>Not a draft</li>
     * </ol>
     *
     * @param creator the user creating the draft.
     * @param parentId the workflow scheme to create a draft for.
     * @return result containing the new draft scheme or any errors that occur.
     */
    ServiceOutcome<DraftWorkflowScheme> createDraft(ApplicationUser creator, long parentId);

    /**
     * Create a draft workflow scheme. The draft can be created using the builder returned from the
     * {@link #draftBuilder(com.atlassian.jira.workflow.AssignableWorkflowScheme)} method.
     *
     * To create a draft scheme the parent must be:
     * <ol>
     *     <li>Active: Used by a project.</li>
     *     <li>Draftless: The parent must not already has a draft workflow scheme.</li>
     *     <li>Not a draft</li>
     * </ol>
     *
     * @param creator the user creating the draft.
     * @param draftWorkflowScheme the draft to create.
     * @return result containing the new draft scheme or any errors that occur.
     */
    ServiceOutcome<DraftWorkflowScheme> createDraft(ApplicationUser creator, DraftWorkflowScheme draftWorkflowScheme);

    /**
     * Return the workflow scheme for the passed id.
     *
     * @param user the user searching for the scheme.
     * @param id the id of the workflow scheme to find.
     * @return the result of the lookup.
     */
    ServiceOutcome<AssignableWorkflowScheme> getWorkflowScheme(ApplicationUser user, long id);

    /**
     * Return draft of the passed workflow scheme if it actually exists. A succesful outcome with null result
     * will be returned if the draft does not exist.
     *
     * @param user the user making the request.
     * @param parentScheme the workflow scheme whose draft we are searching for.
     * @return the result. A succesful outcome with null result will be returned if the draft does not exist.
     */
    ServiceOutcome<DraftWorkflowScheme> getDraftWorkflowScheme(ApplicationUser user, @Nonnull AssignableWorkflowScheme parentScheme);

    /**
     * Return draft of the passed workflow scheme if it actually exists. An unsuccessful outcome
     * will be returned if the draft does not exist.
     *
     * @param user the user making the request.
     * @param parentScheme the workflow scheme whose draft we are searching for.
     * @return the result. An unsuccessful outcome will be returned if the draft does not exist.
     */
    ServiceOutcome<DraftWorkflowScheme> getDraftWorkflowSchemeNotNull(ApplicationUser user, @Nonnull AssignableWorkflowScheme parentScheme);

    /**
     * Delete the passed workflow scheme. A workflow scheme can only be deleted if its a draft or its not
     * active.
     *
     * @param user the user deleting the scheme.
     * @param scheme the scheme.
     *
     * @return the result of the operation.
     */
    ServiceOutcome<Void> deleteWorkflowScheme(ApplicationUser user, @Nonnull WorkflowScheme scheme);

    /**
     * Is the passed workflow scheme being used by a project in JIRA.
     *
     * @param workflowScheme the workflow scheme to test.
     * @return true if the workflow scheme is being used false otherwise.
     */
    boolean isActive(WorkflowScheme workflowScheme);

    /**
     * Return the workflow scheme associated with passed project.
     *
     * @param project the project.
     * @return the result of the operation.
     */
    ServiceOutcome<AssignableWorkflowScheme> getSchemeForProject(ApplicationUser user, @Nonnull Project project);

    /**
     * Save changes to the passed workflow scheme.
     *
     * @param user the user making the changes.
     * @param scheme the scheme to change.
     * @return the scheme as now stored in the database.
     */
    ServiceOutcome<AssignableWorkflowScheme> updateWorkflowScheme(ApplicationUser user, @Nonnull AssignableWorkflowScheme scheme);

    /**
     * Validate that the passed scheme can be saved.
     *
     * @param user the user making the changes.
     * @param scheme the scheme to change.
     * @return the scheme as now stored in the database.
     */
    ServiceOutcome<Void> validateUpdateWorkflowScheme(ApplicationUser user, @Nonnull AssignableWorkflowScheme scheme);

    /**
     * Save changes to the passed workflow scheme.
     *
     * @param user the user making the changes.
     * @param scheme the scheme to change.
     * @return the scheme as now stored in the database.
     */
    ServiceOutcome<DraftWorkflowScheme> updateWorkflowScheme(ApplicationUser user, @Nonnull DraftWorkflowScheme scheme);

    /**
     * Return the number of projects that use the passed scheme.
     *
     * @param assignableWorkflowScheme the scheme to check.
     *
     * @return the number of projects that use this scheme.
     */
    int getUsageCount(@Nonnull AssignableWorkflowScheme assignableWorkflowScheme);

    /**
     * Tells the caller if the passed project is using the default workflow scheme.
     *
     * @param project the project to check.
     * @return true if the passed project is using the default scheme, false otherwise.
     */
    boolean isUsingDefaultScheme(@Nonnull Project project);
}
