package com.atlassian.jira.project.util;

import com.atlassian.annotations.Internal;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 *
 * @since v6.1
 */
@Internal
public interface ProjectKeyStore
{
    @Nullable
    Long getProjectId(String key);

    void addProjectKey(Long projectId, String projectKey);

    void deleteProjectKeys(Long projectId);

    @Nonnull
    Map<String, Long> getAllProjectKeys();

    @Nullable
    Long getProjectIdByKeyIgnoreCase(String projectKey);

    @Nonnull
    Set<String> getProjectKeys(Long projectId);

    @Internal
    void refresh();
}
