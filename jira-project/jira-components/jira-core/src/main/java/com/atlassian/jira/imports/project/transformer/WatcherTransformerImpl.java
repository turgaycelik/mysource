package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.external.beans.ExternalWatcher;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;

/**
 * @since v3.13
 */
public class WatcherTransformerImpl implements WatcherTransformer
{
    public ExternalWatcher transform(final ProjectImportMapper projectImportMapper, final ExternalWatcher watcher)
    {
        final String mappedId = projectImportMapper.getIssueMapper().getMappedId(watcher.getIssueId());
        final String mappedUserKey = projectImportMapper.getUserMapper().getMappedUserKey(watcher.getWatcher());

        final ExternalWatcher transformedWatcher = new ExternalWatcher();
        transformedWatcher.setWatcher(mappedUserKey);
        transformedWatcher.setIssueId(mappedId);
        return transformedWatcher;
    }
}
