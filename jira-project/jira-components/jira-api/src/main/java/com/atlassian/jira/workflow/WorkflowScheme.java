package com.atlassian.jira.workflow;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Represents the workflow scheme for JIRA.
 */
public interface WorkflowScheme
{
    /**
     * The id of the workflow scheme. Will only be null when not stored.
     *
     * @return the id of the workflow scheme.
     */
    Long getId();

    /**
     * The name of the workflow scheme.
     *
     * @return the name of the workflow scheme.
     */
    String getName();

    /**
     * The description of the workflow scheme.
     *
     * @return the description of the workflow scheme.
     */
    String getDescription();

    /**
     * Is the scheme a draft.
     *
     * @return true if this sceheme is a draft; false otherwise.
     */
    boolean isDraft();

    /**
     * Is the scheme the system default.
     *
     * @return true if this sceheme if the system default; false otherwise.
     */
    boolean isDefault();

    /**
     * Returns the workflow to use given the passed issueTypeId.
     *
     * @param issueTypeId the issue type.
     * @return the workflow associated with the passed issue type. Never null.
     */
    @Nonnull
    String getActualWorkflow(String issueTypeId);

    /**
     * Get the default workflow for the scheme. Never null.
     *
     * @return the default workflow for the scheme.
     */
    @Nonnull
    String getActualDefaultWorkflow();

    /**
     * Returns a map of the form {@code issueTypeId -> workflowName}. The {@code null} issueTypeId is used to hold
     * the value of the default workflow (if configured).
     *
     * @return Returns a map of the form {@code issueTypeId -> workflowName}.
     */
    @Nonnull
    Map<String, String> getMappings();

    /**
     * Return the default workflow as saved in the database. Can return null if no default is stored.
     *
     * @return the default workflow as configured in the database or null if no such default is configured.
     */
    String getConfiguredDefaultWorkflow();

    /**
     * Return the Workflow for the passed IssueType as saved in the databse. Can return null if no Workflow is associated
     * with the passed IssueType.
     *
     * @return the Workflow associated with the passed workflow.
     */
    String getConfiguredWorkflow(String issueTypeId);

    /**
     * A builder that can be used to change a workflow scheme.
     *
     * @param <T> the type of builder to return.
     */
    public interface Builder<T extends Builder<T>>
    {
        String getDefaultWorkflow();
        String getMapping(@Nonnull String issueTypeId);
        Map<String, String> getMappings();
        Long getId();
        boolean isDraft();
        boolean isDefault();
        String getDescription();
        String getName();

        /**
         * Set the default workflow for the scheme.
         *
         * @param workflowName the default workflow for the scheme.
         * @return the builder.
         */
        @Nonnull
        T setDefaultWorkflow(@Nonnull String workflowName);

        /**
         * Set the workflow for the passed issue type.
         *
         * @param issueTypeId the issue type to map.
         * @param workflowName the workflow to map
         * @return the builder.
         */
        @Nonnull
        T setMapping(@Nonnull String issueTypeId, @Nonnull String workflowName);

        /**
         * Set the workflow map for the scheme. Its basically a mapping from {@code issueTypeId -> workflowName}.
         *
         * @param mappings the mappings to set in the scheme.
         * @return this builder.
         */
        @Nonnull
        T setMappings(@Nonnull Map<String, String> mappings);

        /**
         * Remove the mapping for the passed issue type from the scheme.
         * @param issueTypeId the issue type whose mapping is to be removed.
         * @return this builder.
         */
        @Nonnull
        T removeMapping(@Nonnull String issueTypeId);

        /**
         * Remove the default mapping from the scheme.
         *
         * @return this builder.
         */
        @Nonnull
        T removeDefault();

        /**
         * Remove all the mappings currently stored in the builder.
         *
         * @return this builder
         */
        @Nonnull
        T clearMappings();

        /**
         * Remove all explicit references to the passed workflow from the scheme. This method sets the default workflow
         * to null if it matches the passed workflow.
         *
         * @param workflowName the name of the workflow to remove.
         * @return this builder.
         */
        @Nonnull
        T removeWorkflow(@Nonnull String workflowName);
    }
}
