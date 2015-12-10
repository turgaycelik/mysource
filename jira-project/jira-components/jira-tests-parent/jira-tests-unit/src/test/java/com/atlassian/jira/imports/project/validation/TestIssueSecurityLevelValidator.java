package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.external.beans.ExternalComponent;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.external.beans.ExternalVersion;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldConfiguration;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapperImpl;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.MockI18nBean;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static com.atlassian.jira.mock.Strict.strict;
import static com.atlassian.jira.util.MessageSetAssert.assert1ErrorNoWarnings;
import static com.atlassian.jira.util.MessageSetAssert.assert1WarningNoErrors;
import static com.atlassian.jira.util.MessageSetAssert.assertNoMessages;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @since v3.13
 */
public class TestIssueSecurityLevelValidator
{
    @Test
    public void testValidateMappingsNoProject()
    {
        final ProjectManager projectManager = mock(ProjectManager.class, strict());
        doReturn(null).when(projectManager).getProjectObjByKey("TST");

        final IssueSecurityLevelValidator issueSecurityLevelValidator = new IssueSecurityLevelValidator(projectManager);
        SimpleProjectImportIdMapper simpleProjectImportIdMapper = new SimpleProjectImportIdMapperImpl();
        simpleProjectImportIdMapper.flagValueAsRequired("12");
        simpleProjectImportIdMapper.flagValueAsRequired("14");
        simpleProjectImportIdMapper.registerOldValue("12", "Real Important");
        simpleProjectImportIdMapper.registerOldValue("14", "Who cares");
        simpleProjectImportIdMapper.mapValue("12", "623");

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        final BackupProject backupProject = backupProject(project);
        final MessageSet messageSet = issueSecurityLevelValidator.validateMappings(simpleProjectImportIdMapper, backupProject, new MockI18nBean());
        assert1ErrorNoWarnings(messageSet, "The issue security level 'Who cares' is required for the import. Please create a project with key 'TST', and configure its issue security scheme.");
    }

    @Test
    public void testValidateMappingsProjectExists()
    {
        final ProjectManager projectManager = mock(ProjectManager.class, strict());
        doReturn(new MockProject(12, "TST")).when(projectManager).getProjectObjByKey("TST");

        IssueSecurityLevelValidator issueSecurityLevelValidator = new IssueSecurityLevelValidator(projectManager);

        SimpleProjectImportIdMapper simpleProjectImportIdMapper = new SimpleProjectImportIdMapperImpl();
        simpleProjectImportIdMapper.flagValueAsRequired("12");
        simpleProjectImportIdMapper.flagValueAsRequired("14");
        simpleProjectImportIdMapper.registerOldValue("12", "Real Important");
        simpleProjectImportIdMapper.registerOldValue("14", "Who cares");
        simpleProjectImportIdMapper.mapValue("12", "623");

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        final BackupProject backupProject = backupProject(project);
        MessageSet messageSet = issueSecurityLevelValidator.validateMappings(simpleProjectImportIdMapper, backupProject, new MockI18nBean());
        assert1ErrorNoWarnings(messageSet, "The issue security level 'Who cares' is required for the import but does not exist in the configured issue security scheme for this project.");
    }

    @Test
    public void testHappyPath()
    {
        IssueSecurityLevelValidator issueSecurityLevelValidator = new IssueSecurityLevelValidator(null);

        SimpleProjectImportIdMapper simpleProjectImportIdMapper = new SimpleProjectImportIdMapperImpl();
        simpleProjectImportIdMapper.flagValueAsRequired("12");
        simpleProjectImportIdMapper.flagValueAsRequired("14");
        simpleProjectImportIdMapper.registerOldValue("12", "Real Important");
        simpleProjectImportIdMapper.registerOldValue("14", "Who cares");
        simpleProjectImportIdMapper.mapValue("12", "623");
        simpleProjectImportIdMapper.mapValue("14", "626");

        MessageSet messageSet = issueSecurityLevelValidator.validateMappings(simpleProjectImportIdMapper, null, new MockI18nBean());
        assertNoMessages(messageSet);
    }

    @Test
    public void testOrphanSecurityLevel() throws Exception
    {
        IssueSecurityLevelValidator issueSecurityLevelValidator = new IssueSecurityLevelValidator(null);

        SimpleProjectImportIdMapper simpleProjectImportIdMapper = new SimpleProjectImportIdMapperImpl();
        simpleProjectImportIdMapper.flagValueAsRequired("12");

        MessageSet messageSet = issueSecurityLevelValidator.validateMappings(simpleProjectImportIdMapper, null, new MockI18nBean());
        assert1WarningNoErrors(messageSet, "The issue security level with id '12' can not be resolved into an actual security level in the backup file. Any issues that were protected by this security level will no longer have an issue security level. After performing the import see the logs for details of which issues were affected.");
    }

    private static BackupProject backupProject(ExternalProject project)
    {
        return new BackupProjectImpl(project,
                ImmutableList.<ExternalVersion>of(),
                ImmutableList.<ExternalComponent>of(),
                ImmutableList.<ExternalCustomFieldConfiguration>of(),
                ImmutableList.<Long>of());
    }
}
