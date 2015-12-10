package com.atlassian.jira.workflow;

import javax.annotation.Nonnull;

/**
 * A workflow scheme that can be assigned to a project.
 *
 * @since v5.2
 */
public interface AssignableWorkflowScheme extends WorkflowScheme
{
    /**
     * Return a builder initialised with the state of the current workflow scheme.
     *
     * @return a new builder initialised with the state of the current workflow scheme.
     */
    @Nonnull
    Builder builder();

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
     * A builder that can be used to change an AssignableWorkflowScheme.
     */
    public interface Builder extends WorkflowScheme.Builder<Builder>
    {
        /**
         * Set the name of the worklflow scheme.
         *
         * @param name the name of the workflow scheme.
         *
         * @return the builder.
         */
        @Nonnull
        Builder setName(@Nonnull String name);

        /**
         * Set the description of the workflow scheme.
         *
         * @param description the description of the workflow scheme.
         * @return the builder.
         */
        @Nonnull
        Builder setDescription(String description);

        @Nonnull
        AssignableWorkflowScheme build();
    }
}
