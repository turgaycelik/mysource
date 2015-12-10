package com.atlassian.jira.workflow;

import com.atlassian.jira.user.ApplicationUser;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v5.2
 */
class DraftWorkflowSchemeImpl extends AbstractWorkflowScheme implements DraftWorkflowScheme
{
    private final ApplicationUser lastModifiedUser;
    private final Date lastModifiedDate;
    private final AssignableWorkflowScheme parent;

    DraftWorkflowSchemeImpl(Long id, Map<String, String> workflowMap,
            ApplicationUser lastModifiedUser, Date lastModifiedDate, AssignableWorkflowScheme parent)
    {
        super(id, workflowMap);
        this.lastModifiedUser = lastModifiedUser;
        this.lastModifiedDate = notNull("lastModifiedDate", lastModifiedDate);
        this.parent = parent;
    }

    DraftWorkflowSchemeImpl(DraftWorkflowSchemeStore.DraftState state,
            ApplicationUser lastModifiedUser, AssignableWorkflowScheme parent)
    {
        super(state.getId(), state.getMappings());
        this.lastModifiedUser = lastModifiedUser;
        this.lastModifiedDate = state.getLastModifiedDate();
        this.parent = parent;
    }

    @Override
    public ApplicationUser getLastModifiedUser()
    {
        return lastModifiedUser;
    }

    @Nonnull
    @Override
    public Date getLastModifiedDate()
    {
        return lastModifiedDate;
    }

    @Override
    public AssignableWorkflowScheme getParentScheme()
    {
        return parent;
    }

    @Override
    public DraftWorkflowScheme.Builder builder()
    {
        return new DraftWorkflowSchemeBuilder(this);
    }

    @Override
    public String getName()
    {
        return parent == null ? null : parent.getName();
    }

    @Override
    public String getDescription()
    {
        return parent == null ? null : parent.getDescription();
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
}
