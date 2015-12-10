package com.atlassian.jira.workflow;

import com.atlassian.jira.user.ApplicationUser;

import java.util.Date;

/**
 * @since v5.2
 */
public class DraftWorkflowSchemeBuilder extends WorkflowSchemeBuilderTemplate<DraftWorkflowScheme.Builder>
    implements DraftWorkflowScheme.Builder
{
    private final ApplicationUser lastModifiedUser;
    private final Date lastModifiedTime;
    private final AssignableWorkflowScheme parent;

    DraftWorkflowSchemeBuilder(DraftWorkflowScheme draftWorkflowScheme)
    {
        super(draftWorkflowScheme);
        this.lastModifiedUser = draftWorkflowScheme.getLastModifiedUser();
        this.lastModifiedTime = draftWorkflowScheme.getLastModifiedDate();
        this.parent = draftWorkflowScheme.getParentScheme();
    }

    DraftWorkflowSchemeBuilder(AssignableWorkflowScheme parent)
    {
        super(null, parent.getMappings());
        this.lastModifiedUser = null;
        this.lastModifiedTime = null;
        this.parent = parent;
    }

    @Override
    DraftWorkflowScheme.Builder builder()
    {
        return this;
    }

    @Override
    public ApplicationUser getLastModifiedUser()
    {
        return lastModifiedUser;
    }

    @Override
    public Date getLastModifiedDate()
    {
        return lastModifiedTime;
    }

    @Override
    public AssignableWorkflowScheme getParentScheme()
    {
        return parent;
    }

    @Override
    public DraftWorkflowScheme build()
    {
        return new DraftWorkflowSchemeImpl(getId(), getMappings(), lastModifiedUser,
                lastModifiedTime == null ? new Date() : lastModifiedTime, parent);
    }

    @Override
    public boolean isDraft()
    {
        return true;
    }

    @Override
    public boolean isDefault()
    {
        return false;
    }

    @Override
    public String getDescription()
    {
        return parent.getDescription();
    }

    @Override
    public String getName()
    {
        return parent.getName();
    }
}
