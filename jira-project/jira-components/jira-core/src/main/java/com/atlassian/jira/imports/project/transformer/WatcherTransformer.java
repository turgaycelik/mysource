package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.external.beans.ExternalWatcher;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;

/**
 * Used to transform an ExternalWatcher based on the project import mapper that is provided. This should only be
 * used with a fully mapped and validated ProjectImportMapper.
 *
 * @since v3.13
 */
public interface WatcherTransformer
{
    /**
     * Transforms an ExternalWatcher based on the project import mapper that is provided. This should only be
     * used with a fully mapped and validated ProjectImportMapper.
     *
     * @param projectImportMapper a fully mapped and validated ProjectImportMapper
     * @param watcher the external watcher that contains all the old values that need to be transformed and other values
     * that should be stored that need no transformation.
     * @return a new ExternalWatcher that contains the transformed values based on the projectImportMapper, null if the
     * issue id has not been mapped.
     */
    ExternalWatcher transform(ProjectImportMapper projectImportMapper, ExternalWatcher watcher);
}
