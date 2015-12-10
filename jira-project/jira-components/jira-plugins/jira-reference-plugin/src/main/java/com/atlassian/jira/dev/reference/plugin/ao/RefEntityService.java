package com.atlassian.jira.dev.reference.plugin.ao;

import com.atlassian.activeobjects.tx.Transactional;

import java.util.List;

/**
 * Simple interface to add RefEntity transactionally
 *
 * @since v5.0
 */
@Transactional
public interface RefEntityService
{
    RefEntity add(String description);

    List<RefEntity> allEntities();
}
