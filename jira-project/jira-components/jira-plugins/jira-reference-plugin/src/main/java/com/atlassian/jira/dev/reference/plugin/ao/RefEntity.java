package com.atlassian.jira.dev.reference.plugin.ao;

import net.java.ao.Entity;
import net.java.ao.Preload;

/**
 * Simple entity for testing AO backup/restore
 *
 * @since v5.0
 */
@Preload
public interface RefEntity extends Entity
{
    String getDescription();

    void setDescription(String description);
}
