package com.atlassian.jira.workflow;

import java.util.Date;

/**
 * @since v5.2
 */
public interface DraftWorkflowSchemeStore extends WorkflowSchemeStore<DraftWorkflowSchemeStore.DraftState>
{
    boolean deleteByParentId(long parentId);

    boolean hasDraftForParent(long parentId);
    DraftState getDraftForParent(long parentId);
    Long getParentId(long id);

    DraftState.Builder builder(long parentId);

    interface DraftState extends WorkflowSchemeStore.State
    {
        long getParentSchemeId();
        Date getLastModifiedDate();
        String getLastModifiedUser();

        Builder builder();

        interface Builder extends State.Builder<Builder>
        {
            long getParentSchemeId();
            String getLastModifiedUser();
            Builder setLastModifiedUser(String user);
            Date getLastModifiedDate();

            DraftState build();
        }
    }
}
