package com.atlassian.jira.imports.project.validation;

import java.util.Collections;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.help.HelpUrls;
import com.atlassian.jira.help.MockHelpUrls;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.mapper.IssueTypeMapper;
import com.atlassian.jira.imports.project.mapper.StatusMapper;
import com.atlassian.jira.issue.status.MockStatus;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetAssert;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestStatusMapperValidatorImpl
{
    @Rule
    public RuleChain container = MockitoMocksInContainer.forTest(this);

    @AvailableInContainer
    private HelpUrls urls = new MockHelpUrls();

    @Test
    public void testStatusNotInWorkflowSchemeNotMappedEnterpriseCustomWorkflowProjectExists()
    {

        MockProject mockProject = new MockProject(1234, "TST");
        Mock mockProjectManager = new Mock(ProjectManager.class);
        mockProjectManager.setStrict(true);
        mockProjectManager.expectAndReturn("getProjectObjByKey", P.ANY_ARGS, mockProject);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Task", false);
        issueTypeMapper.mapValue("678", "987");
        issueTypeMapper.flagValueAsRequired("111");
        issueTypeMapper.registerOldValue("111", "Bug", false);
        issueTypeMapper.mapValue("111", "222");

        // Create a status mapper with an status that is not above
        StatusMapper statusMapper = new StatusMapper();
        statusMapper.flagValueAsRequired("123", "111");
        statusMapper.flagValueAsRequired("123", "678");
        statusMapper.registerOldValue("123", "Open");

        Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.setStrict(true);

        Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
        mockJiraWorkflow.setStrict(true);
        mockJiraWorkflow.expectAndReturn("getLinkedStatusObjects", Collections.EMPTY_LIST);
        mockJiraWorkflow.expectAndReturn("getName", "My Workflow");
        mockWorkflowManager.expectAndReturn("getWorkflow", P.ANY_ARGS, mockJiraWorkflow.proxy());


        Mock mockConstantsManager = new Mock(ConstantsManager.class);
        mockConstantsManager.setStrict(true);
        mockConstantsManager.expectAndReturn("getStatusByName", P.args(P.eq("Open")), new MockStatus("222", "Open"));

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12), new Long(14)));


        StatusMapperValidatorImpl statusMapperValidator = new StatusMapperValidatorImpl((ProjectManager) mockProjectManager.proxy(), (ConstantsManager) mockConstantsManager.proxy(), (WorkflowManager) mockWorkflowManager.proxy())
        {
            @Override
            boolean isUsingDefaultWorkflow(final JiraWorkflow workflow)
            {
                return false;
            }
        };
        MockI18nBean mockI18nBean = new MockI18nBean();

        final MessageSet messageSet = statusMapperValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper, statusMapper);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(2, messageSet.getErrorMessages().size());
        assertTrue(messageSet.getWarningMessages().isEmpty());
        assertTrue(messageSet.getErrorMessages().contains("The status 'Open' is in use by an issue of type 'Bug' in the backup file. The workflow 'My Workflow', which is associated with issue type 'Bug', does not use this status. You must either edit the workflow to use the status or associate a workflow with issue type 'Bug' that uses the status."));
        assertTrue(messageSet.getErrorMessages().contains("The status 'Open' is in use by an issue of type 'Task' in the backup file. The workflow 'My Workflow', which is associated with issue type 'Task', does not use this status. You must either edit the workflow to use the status or associate a workflow with issue type 'Task' that uses the status."));
        // Verify Mocks
        mockProjectManager.verify();
        mockWorkflowManager.verify();
        mockJiraWorkflow.verify();
        mockConstantsManager.verify();
    }

    @Test
    public void testStatusNotInWorkflowSchemeNotMappedEnterpriseDefaultWorkflowProjectExists()
    {
        MockProject mockProject = new MockProject(1234, "TST");
        Mock mockProjectManager = new Mock(ProjectManager.class);
        mockProjectManager.setStrict(true);
        mockProjectManager.expectAndReturn("getProjectObjByKey", P.ANY_ARGS, mockProject);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Task", false);
        issueTypeMapper.mapValue("678", "987");
        issueTypeMapper.flagValueAsRequired("111");
        issueTypeMapper.registerOldValue("111", "Bug", false);
        issueTypeMapper.mapValue("111", "222");

        // Create a status mapper with an status that is not above
        StatusMapper statusMapper = new StatusMapper();
        statusMapper.flagValueAsRequired("123", "111");
        statusMapper.flagValueAsRequired("123", "678");
        statusMapper.registerOldValue("123", "Open");

        Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.setStrict(true);

        Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
        mockJiraWorkflow.setStrict(true);
        mockJiraWorkflow.expectAndReturn("getLinkedStatusObjects", Collections.EMPTY_LIST);
        mockJiraWorkflow.expectAndReturn("getName", "My Workflow");
        mockWorkflowManager.expectAndReturn("getWorkflow", P.ANY_ARGS, mockJiraWorkflow.proxy());


        Mock mockConstantsManager = new Mock(ConstantsManager.class);
        mockConstantsManager.setStrict(true);
        mockConstantsManager.expectAndReturn("getStatusByName", P.args(P.eq("Open")), new MockStatus("222", "Open"));

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12), new Long(14)));


        StatusMapperValidatorImpl statusMapperValidator = new StatusMapperValidatorImpl((ProjectManager) mockProjectManager.proxy(), (ConstantsManager) mockConstantsManager.proxy(), (WorkflowManager) mockWorkflowManager.proxy())
        {
            boolean isUsingDefaultWorkflow(final JiraWorkflow workflow)
            {
                return true;
            }
        };
        MockI18nBean mockI18nBean = new MockI18nBean();

        final MessageSet messageSet = statusMapperValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper, statusMapper);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(2, messageSet.getErrorMessages().size());
        assertTrue(messageSet.getWarningMessages().isEmpty());
        assertTrue(messageSet.getErrorMessages().contains("The status 'Open' is in use by an issue of type 'Bug' in the backup file. The default workflow 'My Workflow', which is associated with issue type 'Bug', does not use this status. This workflow is not editable. You must associate a workflow with issue type 'Bug' that uses the status. To do this you will need to use a workflow scheme."));
        assertTrue(messageSet.getErrorMessages().contains("The status 'Open' is in use by an issue of type 'Task' in the backup file. The default workflow 'My Workflow', which is associated with issue type 'Task', does not use this status. This workflow is not editable. You must associate a workflow with issue type 'Task' that uses the status. To do this you will need to use a workflow scheme."));
        // Verify Mocks
        mockProjectManager.verify();
        mockWorkflowManager.verify();
        mockJiraWorkflow.verify();
        mockConstantsManager.verify();
    }

    @Test
    public void testStatusNotInWorkflowSchemeNotMappedEnterpriseDefaultWorkflowProjectDoesntExist()
    {
        Mock mockProjectManager = new Mock(ProjectManager.class);
        mockProjectManager.setStrict(true);
        mockProjectManager.expectAndReturn("getProjectObjByKey", P.ANY_ARGS, null);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Task", false);
        issueTypeMapper.mapValue("678", "987");
        issueTypeMapper.flagValueAsRequired("111");
        issueTypeMapper.registerOldValue("111", "Bug", false);
        issueTypeMapper.mapValue("111", "222");

        // Create a status mapper with an status that is not above
        StatusMapper statusMapper = new StatusMapper();
        statusMapper.flagValueAsRequired("123", "111");
        statusMapper.flagValueAsRequired("123", "678");
        statusMapper.registerOldValue("123", "Open");

        Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.setStrict(true);

        Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
        mockJiraWorkflow.setStrict(true);
        mockJiraWorkflow.expectAndReturn("getLinkedStatusObjects", Collections.EMPTY_LIST);
        mockJiraWorkflow.expectAndReturn("getName", "My Workflow");
        mockWorkflowManager.expectAndReturn("getDefaultWorkflow", P.ANY_ARGS, mockJiraWorkflow.proxy());


        Mock mockConstantsManager = new Mock(ConstantsManager.class);
        mockConstantsManager.setStrict(true);
        mockConstantsManager.expectAndReturn("getStatusByName", P.args(P.eq("Open")), new MockStatus("222", "Open"));

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12), new Long(14)));


        StatusMapperValidatorImpl statusMapperValidator = new StatusMapperValidatorImpl((ProjectManager) mockProjectManager.proxy(), (ConstantsManager) mockConstantsManager.proxy(), (WorkflowManager) mockWorkflowManager.proxy())
        {
            boolean isUsingDefaultWorkflow(final JiraWorkflow workflow)
            {
                return true;
            }
        };
        MockI18nBean mockI18nBean = new MockI18nBean();

        final MessageSet messageSet = statusMapperValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper, statusMapper);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(2, messageSet.getErrorMessages().size());
        assertTrue(messageSet.getWarningMessages().isEmpty());
        assertTrue(messageSet.getErrorMessages().contains("The status 'Open' is in use by an issue of type 'Bug' in the backup file. The default workflow 'My Workflow', which is associated with issue type 'Bug', does not use this status. This workflow is not editable. You must create a project with key 'TST', instead of letting the import create it for you, and associate a workflow with issue type 'Bug' that uses the status. To do this you will need to use a workflow scheme."));
        assertTrue(messageSet.getErrorMessages().contains("The status 'Open' is in use by an issue of type 'Task' in the backup file. The default workflow 'My Workflow', which is associated with issue type 'Task', does not use this status. This workflow is not editable. You must create a project with key 'TST', instead of letting the import create it for you, and associate a workflow with issue type 'Task' that uses the status. To do this you will need to use a workflow scheme."));
        // Verify Mocks
        mockProjectManager.verify();
        mockWorkflowManager.verify();
        mockJiraWorkflow.verify();
        mockConstantsManager.verify();
    }

    @Test
    public void testNoProjectFoundUsingDefaultIssueTypeSchemeAndHappyPath()
    {
        Mock mockProjectManager = new Mock(ProjectManager.class);
        mockProjectManager.setStrict(true);
        mockProjectManager.expectAndReturn("getProjectObjByKey", P.ANY_ARGS, null);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Task", false);
        issueTypeMapper.mapValue("678", "987");
        issueTypeMapper.flagValueAsRequired("111");
        issueTypeMapper.registerOldValue("111", "Bug", false);
        issueTypeMapper.mapValue("111", "222");

        // Create a status mapper with an status that is not above
        StatusMapper statusMapper = new StatusMapper();
        statusMapper.flagValueAsRequired("123", "111");
        statusMapper.flagValueAsRequired("123", "678");
        statusMapper.registerOldValue("123", "Open");
        statusMapper.mapValue("123", "222");

        Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.setStrict(true);

        Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
        mockJiraWorkflow.setStrict(true);
        final MockStatus mockStatus = new MockStatus("222", "Open");
        mockJiraWorkflow.expectAndReturn("getLinkedStatusObjects", EasyList.build(mockStatus));
        mockWorkflowManager.expectAndReturn("getDefaultWorkflow", P.ANY_ARGS, mockJiraWorkflow.proxy());

        Mock mockConstantsManager = new Mock(ConstantsManager.class);
        mockConstantsManager.setStrict(true);
        mockConstantsManager.expectAndReturn("getStatusObject", P.args(P.eq("222")), mockStatus);

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12), new Long(14)));
        StatusMapperValidatorImpl statusMapperValidator = new StatusMapperValidatorImpl((ProjectManager) mockProjectManager.proxy(), (ConstantsManager) mockConstantsManager.proxy(), (WorkflowManager) mockWorkflowManager.proxy());
        MockI18nBean mockI18nBean = new MockI18nBean();

        final MessageSet messageSet = statusMapperValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper, statusMapper);

        assertFalse(messageSet.hasAnyErrors());
        mockProjectManager.verify();
        mockWorkflowManager.verify();
        mockJiraWorkflow.verify();
        mockConstantsManager.verify();
    }

    @Test
    public void testNoProjectFoundUsingIsStatusValidHappyPath()
    {
        Mock mockProjectManager = new Mock(ProjectManager.class);
        mockProjectManager.setStrict(true);
        mockProjectManager.expectAndReturn("getProjectObjByKey", P.ANY_ARGS, null);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Task", false);
        issueTypeMapper.mapValue("678", "987");
        issueTypeMapper.flagValueAsRequired("111");
        issueTypeMapper.registerOldValue("111", "Bug", false);
        issueTypeMapper.mapValue("111", "222");

        // Create a status mapper with an status that is not above
        StatusMapper statusMapper = new StatusMapper();
        statusMapper.flagValueAsRequired("123", "111");
        statusMapper.flagValueAsRequired("123", "678");
        statusMapper.registerOldValue("123", "Open");
        statusMapper.mapValue("123", "222");

        Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.setStrict(true);

        Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
        mockJiraWorkflow.setStrict(true);
        final MockStatus mockStatus = new MockStatus("222", "Open");
        mockJiraWorkflow.expectAndReturn("getLinkedStatusObjects", EasyList.build(mockStatus));
        mockWorkflowManager.expectAndReturn("getDefaultWorkflow", P.ANY_ARGS, mockJiraWorkflow.proxy());

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12), new Long(14)));
        StatusMapperValidatorImpl statusMapperValidator = new StatusMapperValidatorImpl((ProjectManager) mockProjectManager.proxy(), null, (WorkflowManager) mockWorkflowManager.proxy());

        assertTrue(statusMapperValidator.isStatusValid("123", mockStatus, statusMapper, issueTypeMapper, backupProject.getProject().getKey()));

        mockProjectManager.verify();
        mockWorkflowManager.verify();
        mockJiraWorkflow.verify();
    }

    @Test
    public void testInvalidImportFileRequiredStatusNotRegistered()
    {
        // This test simulates the situation where an import file has inconsistent data such that a "required" status
        // Does not have a Status Name configured.

        MockProject mockProject = new MockProject(1234, "TST");
        Mock mockProjectManager = new Mock(ProjectManager.class);
        mockProjectManager.setStrict(true);
        mockProjectManager.expectAndReturn("getProjectObjByKey", P.ANY_ARGS, mockProject);

        StatusMapper statusMapper = new StatusMapper();
        // We flag Status 123 as required, but don't "register" it.
        statusMapper.flagValueAsRequired("123", "456");

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12), new Long(14)));

        MockI18nBean mockI18nBean = new MockI18nBean();

        StatusMapperValidatorImpl statusMapperValidator = new StatusMapperValidatorImpl((ProjectManager) mockProjectManager.proxy(), null, null);
        final MessageSet messageSet = statusMapperValidator.validateMappings(mockI18nBean, backupProject, new IssueTypeMapper(), statusMapper);
        MessageSetAssert.assert1ErrorNoWarnings(messageSet, "The status with ID '123' is required for the import but could not find this Status configured in the import.");
    }

    @Test
    public void testValidStatusIsNotMapped()
    {
        // This test simulates the situation where the auto-mapper did not map a Status, but the validator can map and it
        // seems valid. This should not happen and is included as a safety net against futurr modifications of mapping.

        MockProject mockProject = new MockProject(1234, "TST");
        Mock mockProjectManager = new Mock(ProjectManager.class);
        mockProjectManager.setStrict(true);
        mockProjectManager.expectAndReturn("getProjectObjByKey", P.ANY_ARGS, null);

        final MockStatus mockStatus = new MockStatus("222", "Open");
        Mock mockConstantsManager = new Mock(ConstantsManager.class);
        mockConstantsManager.setStrict(true);
        mockConstantsManager.expectAndReturn("getStatusByName", P.args(P.eq("Open")), mockStatus);

        // JiraWorkflow
        Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
        mockJiraWorkflow.setStrict(true);
        mockJiraWorkflow.expectAndReturn("getLinkedStatusObjects", EasyList.build(mockStatus));

        // WorkflowManager
        Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.setStrict(true);
        mockWorkflowManager.expectAndReturn("getDefaultWorkflow", P.ANY_ARGS, mockJiraWorkflow.proxy());

        StatusMapper statusMapper = new StatusMapper();
        statusMapper.flagValueAsRequired("123", "1012");
        statusMapper.registerOldValue("123", "Open");

        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.mapValue("1012", "1022");

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12), new Long(14)));

        MockI18nBean mockI18nBean = new MockI18nBean();

        StatusMapperValidatorImpl statusMapperValidator = new StatusMapperValidatorImpl((ProjectManager) mockProjectManager.proxy(),
                (ConstantsManager) mockConstantsManager.proxy(), (WorkflowManager) mockWorkflowManager.proxy());
        final MessageSet messageSet = statusMapperValidator.validateMappings(mockI18nBean, backupProject, new IssueTypeMapper(), statusMapper);
        MessageSetAssert.assert1ErrorNoWarnings(messageSet, "The status 'Open' is required for the import but was not mapped.");

        mockConstantsManager.verify();
        mockJiraWorkflow.verify();
        mockProjectManager.verify();
        mockWorkflowManager.verify();
    }
}
