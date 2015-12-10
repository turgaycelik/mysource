package com.atlassian.jira.imports.project.validation;

import java.util.Collections;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.external.beans.ExternalComponent;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.external.beans.ExternalVersion;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldConfiguration;
import com.atlassian.jira.imports.project.mapper.IssueTypeMapper;
import com.atlassian.jira.imports.project.util.IssueTypeImportHelper;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.mock.Strict;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.MockI18nBean;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static com.atlassian.jira.util.MessageSetAssert.assert1ErrorNoWarnings;
import static com.atlassian.jira.util.MessageSetAssert.assertErrorMessages;
import static com.atlassian.jira.util.MessageSetAssert.assertNoMessages;
import static com.atlassian.jira.util.MessageSetAssert.assertNoWarnings;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @since v3.13
 */
public class TestIssueTypeValidatorImpl
{
    @Test
    public void testIssueTypeDoesNotExist()
    {
        final IssueTypeImportHelper issueTypeImportHelper = mock(IssueTypeImportHelper.class);

        IssueTypeMapperValidatorImpl issueTypeValidator = new IssueTypeMapperValidatorImpl(null, issueTypeImportHelper, null);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Task", false);

        BackupProject backupProject = backupProject();

        MockI18nBean mockI18nBean = new MockI18nBean();

        final MessageSet messageSet = issueTypeValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper);
        assert1ErrorNoWarnings(messageSet, "The issue type 'Task' is required for the import but does not exist in the current JIRA instance.");

        verify(issueTypeImportHelper).getIssueTypeForName("Task");
        verifyNoMoreInteractions(issueTypeImportHelper);
    }

    @Test
    public void testSubTaskIssueTypeDoesNotExist()
    {
        final IssueTypeImportHelper issueTypeImportHelper = mock(IssueTypeImportHelper.class);
        final SubTaskManager mockSubTaskManager = mock(SubTaskManager.class, new Strict());
        doReturn(true).when(mockSubTaskManager).isSubTasksEnabled();

        IssueTypeMapperValidatorImpl issueTypeValidator = new IssueTypeMapperValidatorImpl(null, issueTypeImportHelper, mockSubTaskManager);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Sub-Task", true);

        BackupProject backupProject = backupProject();

        MockI18nBean mockI18nBean = new MockI18nBean();

        final MessageSet messageSet = issueTypeValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper);
        assert1ErrorNoWarnings(messageSet, "The sub-task issue type 'Sub-Task' is required for the import but does not exist in the current JIRA instance.");

        verify(issueTypeImportHelper).getIssueTypeForName("Sub-Task");
        verifyNoMoreInteractions(issueTypeImportHelper);
    }

    @Test
    public void testSubTaskIssueTypeDoesNotExistSubTasksNotEnabled()
    {
        final IssueTypeImportHelper issueTypeImportHelper = mock(IssueTypeImportHelper.class);
        final SubTaskManager subTaskManager = mock(SubTaskManager.class, new Strict());
        doReturn(false).when(subTaskManager).isSubTasksEnabled();

        IssueTypeMapperValidatorImpl issueTypeValidator = new IssueTypeMapperValidatorImpl(null, issueTypeImportHelper, subTaskManager);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Sub-Task", true);

        BackupProject backupProject = backupProject();

        MockI18nBean mockI18nBean = new MockI18nBean();

        final MessageSet messageSet = issueTypeValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper);
        assert1ErrorNoWarnings(messageSet, "Sub-tasks are currently disabled in JIRA, please enable sub-tasks. The sub-task issue type 'Sub-Task' is required for the import but does not exist in the current JIRA instance.");

        verify(issueTypeImportHelper).getIssueTypeForName("Sub-Task");
        verifyNoMoreInteractions(issueTypeImportHelper);
    }

    @Test
    public void testIssueTypeNotInSchemeNotMapped()
    {
        final IssueTypeImportHelper issueTypeImportHelper = mock(IssueTypeImportHelper.class);
        when(issueTypeImportHelper.getIssueTypeForName("Task")).thenReturn(new MockIssueType("987", "Task"));

        IssueTypeMapperValidatorImpl issueTypeValidator = new IssueTypeMapperValidatorImpl(null, issueTypeImportHelper, null);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Task", false);

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = backupProject(project);

        MockI18nBean mockI18nBean = new MockI18nBean();
        final MessageSet messageSet = issueTypeValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper);
        assert1ErrorNoWarnings(messageSet, "The issue type 'Task' exists in the system but is not valid for the projects issue type scheme.");

        verify(issueTypeImportHelper).isIssueTypeValidForProject("TST", "987");
    }

    @Test
    public void testIssueTypeNotInScheme()
    {
        final IssueTypeImportHelper issueTypeImportHelper = mock(IssueTypeImportHelper.class);

        final ConstantsManager constantsManager = mock(ConstantsManager.class, new Strict());
        doReturn(new MockIssueType("987", "Task")).when(constantsManager).getIssueTypeObject("987");

        IssueTypeMapperValidatorImpl issueTypeValidator = new IssueTypeMapperValidatorImpl(constantsManager, issueTypeImportHelper, null);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Task", false);
        issueTypeMapper.mapValue("678", "987");

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = backupProject(project);

        MockI18nBean mockI18nBean = new MockI18nBean();
        final MessageSet messageSet = issueTypeValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper);
        assert1ErrorNoWarnings(messageSet, "The issue type 'Task' exists in the system but is not valid for the projects issue type scheme.");
    }

    @Test
    public void testIssueTypeWasSubtaskNotNowNotMapped()
    {
        final IssueTypeImportHelper issueTypeImportHelper = mock(IssueTypeImportHelper.class, new Strict());
        doReturn(new MockIssueType("987", "Bug", false)).when(issueTypeImportHelper).getIssueTypeForName("Bug");
        doReturn(true).when(issueTypeImportHelper).isIssueTypeValidForProject("TST", "987");

        IssueTypeMapperValidatorImpl issueTypeValidator = new IssueTypeMapperValidatorImpl(null, issueTypeImportHelper, null);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Bug", true);

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = backupProject(project);

        MockI18nBean mockI18nBean = new MockI18nBean();

        final MessageSet messageSet = issueTypeValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper);
        assert1ErrorNoWarnings(messageSet, "The issue type 'Bug' is defined as a sub-task in the backup project, but it is a normal issue type in the current JIRA instance.");
    }

    @Test
    public void testIssueTypeWasSubtaskNotNow()
    {
        final IssueTypeImportHelper issueTypeImportHelper = mock(IssueTypeImportHelper.class, new Strict());
        doReturn(true).when(issueTypeImportHelper).isIssueTypeValidForProject("TST", "987");
        final ConstantsManager constantsManager = mock(ConstantsManager.class, new Strict());
        doReturn(new MockIssueType("987", "Bug", false)).when(constantsManager).getIssueTypeObject("987");

        IssueTypeMapperValidatorImpl issueTypeValidator = new IssueTypeMapperValidatorImpl(constantsManager, issueTypeImportHelper, null);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Bug", true);
        issueTypeMapper.mapValue("678", "987");

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = backupProject(project);

        MockI18nBean mockI18nBean = new MockI18nBean();

        final MessageSet messageSet = issueTypeValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper);
        assert1ErrorNoWarnings(messageSet, "The issue type 'Bug' is defined as a sub-task in the backup project, but it is a normal issue type in the current JIRA instance.");
    }

    @Test
    public void testIssueTypeWasSubtaskNotNowAndNotRelevantForProject()
    {
        final IssueTypeImportHelper issueTypeImportHelper = mock(IssueTypeImportHelper.class);

        final ConstantsManager constantsManager = mock(ConstantsManager.class, new Strict());
        doReturn(new MockIssueType("987", "Bug", false)).when(constantsManager).getIssueTypeObject("987");

        IssueTypeMapperValidatorImpl issueTypeValidator = new IssueTypeMapperValidatorImpl(constantsManager, issueTypeImportHelper, null);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Bug", true);
        issueTypeMapper.mapValue("678", "987");

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = backupProject(project);

        MockI18nBean mockI18nBean = new MockI18nBean();

        final MessageSet messageSet = issueTypeValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper);
        assertNoWarnings(messageSet);
        assertErrorMessages(messageSet,
                "The issue type 'Bug' is defined as a sub-task in the backup project, but it is a normal issue type in the current JIRA instance.",
                "The issue type 'Bug' exists in the system but is not valid for the projects issue type scheme.");

        verify(issueTypeImportHelper).isIssueTypeValidForProject("TST", "987");
        verifyNoMoreInteractions(issueTypeImportHelper);
    }


    @Test
    public void testIssueTypeNormalNowSubtaskNotMapped()
    {
        final IssueTypeImportHelper issueTypeImportHelper = mock(IssueTypeImportHelper.class, new Strict());
        doReturn(new MockIssueType("987", "Bug", true)).when(issueTypeImportHelper).getIssueTypeForName("Bug");
        doReturn(true).when(issueTypeImportHelper).isIssueTypeValidForProject("TST", "987");

        IssueTypeMapperValidatorImpl issueTypeValidator = new IssueTypeMapperValidatorImpl(null, issueTypeImportHelper, null);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Bug", false);

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = backupProject(project);

        MockI18nBean mockI18nBean = new MockI18nBean();

        final MessageSet messageSet = issueTypeValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper);
        assert1ErrorNoWarnings(messageSet, "The issue type 'Bug' is defined as a normal issue type in the backup project, but it is a sub-task issue type in the current JIRA instance.");
    }

    @Test
    public void testIssueTypeNormalNowSubtask()
    {
        final IssueTypeImportHelper issueTypeImportHelper = mock(IssueTypeImportHelper.class, new Strict());
        doReturn(true).when(issueTypeImportHelper).isIssueTypeValidForProject("TST", "987");
        final ConstantsManager constantsManager = mock(ConstantsManager.class, new Strict());
        doReturn(new MockIssueType("987", "Bug", true)).when(constantsManager).getIssueTypeObject("987");

        IssueTypeMapperValidatorImpl issueTypeValidator = new IssueTypeMapperValidatorImpl(constantsManager, issueTypeImportHelper, null);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Bug", false);
        issueTypeMapper.mapValue("678", "987");

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = backupProject(project);

        MockI18nBean mockI18nBean = new MockI18nBean();

        final MessageSet messageSet = issueTypeValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper);
        assert1ErrorNoWarnings(messageSet, "The issue type 'Bug' is defined as a normal issue type in the backup project, but it is a sub-task issue type in the current JIRA instance.");
    }

    @Test
    public void testIssueTypeNotMappedButSeemsFineError()
    {
        final IssueTypeImportHelper issueTypeImportHelper = mock(IssueTypeImportHelper.class, new Strict());
        doReturn(new MockIssueType("987", "Bug", false)).when(issueTypeImportHelper).getIssueTypeForName("Bug");
        doReturn(true).when(issueTypeImportHelper).isIssueTypeValidForProject("TST", "987");

        IssueTypeMapperValidatorImpl issueTypeValidator = new IssueTypeMapperValidatorImpl(null, issueTypeImportHelper, null);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Bug", false);

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = backupProject(project);

        MockI18nBean mockI18nBean = new MockI18nBean();

        final MessageSet messageSet = issueTypeValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper);
        assert1ErrorNoWarnings(messageSet, "The issue type 'Bug' is required for the import but it is not mapped.");
    }

    @Test
    public void testNoProjectFoundUsingDefaultIssueTypeSchemeAndHappyPath()
    {
        final IssueTypeImportHelper issueTypeImportHelper = mock(IssueTypeImportHelper.class, new Strict());
        doReturn(true).when(issueTypeImportHelper).isIssueTypeValidForProject("TST", "987");
        final ConstantsManager constantsManager = mock(ConstantsManager.class, new Strict());
        doReturn(new MockIssueType("987", "Bug", false)).when(constantsManager).getIssueTypeObject("987");

        IssueTypeMapperValidatorImpl issueTypeValidator = new IssueTypeMapperValidatorImpl(constantsManager, issueTypeImportHelper, null);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Bug", false);
        issueTypeMapper.mapValue("678", "987");

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = backupProject(project);

        MockI18nBean mockI18nBean = new MockI18nBean();

        final MessageSet messageSet = issueTypeValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper);
        assertNoMessages(messageSet);
    }

    private static BackupProject backupProject()
    {
        return backupProject(new ExternalProject());
    }

    private static BackupProject backupProject(final ExternalProject project)
    {
        return new BackupProjectImpl(project,
                Collections.<ExternalVersion>emptyList(),
                Collections.<ExternalComponent>emptyList(),
                Collections.<ExternalCustomFieldConfiguration>emptyList(),
                ImmutableList.of(12L, 14L));
    }
}
