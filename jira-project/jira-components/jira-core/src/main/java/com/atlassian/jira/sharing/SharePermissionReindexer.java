package com.atlassian.jira.sharing;

/**
 * Responsible for re-indexing SharedEntities when a {@link SharePermission} gets removed.
 */
public interface SharePermissionReindexer
{
    /**
     * Given a permission, re-index all entities that currently have that permission in the index.
     * 
     * @param permission
     */
    void reindex(SharePermission permission);
}
