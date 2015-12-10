package com.atlassian.jira.workflow;

import com.atlassian.jira.user.ApplicationUser;

import javax.annotation.Nonnull;
import java.util.Date;

/**
 * @since v5.2
 */
public interface DraftWorkflowScheme extends WorkflowScheme
{
    /**
     * The user that last modified the scheme.
     *
     * @return the user that last modified the scheme. Can be null.
     */
    public ApplicationUser getLastModifiedUser();

    /**
     * The date the scheme was last updated (or created).
     *
     * @return the date the scheme was last updated or created.
     */
    @Nonnull
    public Date getLastModifiedDate();

    /**
     * Return a reference to the parent of the draft scheme.
     *
     * @return a reference to the parent of the draft scheme.
     */
    public AssignableWorkflowScheme getParentScheme();

    /**
     * Return a builder initialised with the state of the current workflow scheme.
     *
     * @return a new builder initialised with the state of the current workflow scheme.
     */
    Builder builder();

    /**
     * A builder that can be used to change the state of the workflow scheme.
     */
    public interface Builder extends WorkflowScheme.Builder<Builder>
    {
        ApplicationUser getLastModifiedUser();
        Date getLastModifiedDate();
        AssignableWorkflowScheme getParentScheme();
        DraftWorkflowScheme build();
    }
}
