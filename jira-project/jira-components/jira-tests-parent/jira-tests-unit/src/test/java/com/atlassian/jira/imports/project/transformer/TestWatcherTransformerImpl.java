package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.external.beans.ExternalWatcher;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.user.util.UserUtil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

/**
 * @since v3.13
 */
public class TestWatcherTransformerImpl
{
    @Test
    public void testTransform() throws Exception
    {
        final UserUtil userUtil = mock(UserUtil.class);
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(userUtil, null);
        projectImportMapper.getIssueMapper().mapValue("12", "13");

        ExternalWatcher externalWatcher = new ExternalWatcher();
        externalWatcher.setWatcher("admin");
        externalWatcher.setIssueId("12");

        WatcherTransformerImpl watcherTransformer = new WatcherTransformerImpl();
        final ExternalWatcher transformedWatcher = watcherTransformer.transform(projectImportMapper, externalWatcher);
        assertEquals("13", transformedWatcher.getIssueId());
        assertEquals("admin", externalWatcher.getWatcher());
    }

    @Test
    public void testTransformNoMappedIssueId() throws Exception
    {
        final UserUtil userUtil = mock(UserUtil.class);
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(userUtil, null);

        ExternalWatcher externalWatcher = new ExternalWatcher();
        externalWatcher.setWatcher("admin");
        externalWatcher.setIssueId("12");

        WatcherTransformerImpl watcherTransformer = new WatcherTransformerImpl();
        assertNull(watcherTransformer.transform(projectImportMapper, externalWatcher).getIssueId());
    }
}
