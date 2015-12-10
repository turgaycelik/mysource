package com.atlassian.jira.imports.project.mapper;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.external.beans.ExternalCustomField;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldConfiguration;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldOption;
import com.atlassian.jira.imports.project.util.IssueTypeImportHelper;
import com.atlassian.jira.imports.project.validation.CustomFieldMapperValidator;
import com.atlassian.jira.imports.project.validation.StatusMapperValidator;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.context.ProjectContext;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.link.MockIssueLinkType;
import com.atlassian.jira.issue.priority.MockPriority;
import com.atlassian.jira.issue.resolution.MockResolution;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.issue.status.MockStatus;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

/**
 * @since v3.13
 */
public class TestAutomaticDataMapperImpl
{

    @Test
    public void testGetIssueSecuritySchemeIdHappyPath() throws Exception
    {
        final GenericValue projectGV = new MockGenericValue("Project");

        final MockControl mockProjectManagerControl = MockControl.createStrictControl(ProjectManager.class);
        final ProjectManager mockProjectManager = (ProjectManager) mockProjectManagerControl.getMock();
        mockProjectManager.getProjectByKey("TST");
        mockProjectManagerControl.setReturnValue(projectGV);
        mockProjectManagerControl.replay();

        final MockControl mockIssueSecuritySchemeManagerControl = MockControl.createStrictControl(IssueSecuritySchemeManager.class);
        final IssueSecuritySchemeManager mockIssueSecuritySchemeManager = (IssueSecuritySchemeManager) mockIssueSecuritySchemeManagerControl.getMock();
        mockIssueSecuritySchemeManager.getSchemes(projectGV);
        mockIssueSecuritySchemeManagerControl.setReturnValue(EasyList.build(new MockGenericValue("SchemeThing", EasyMap.build("id", new Long(5),
            "name", "my scheme"))));
        mockIssueSecuritySchemeManagerControl.replay();

        final AutomaticDataMapperImpl automaticDataMapper = new AutomaticDataMapperImpl(null, null, null, mockProjectManager, null, null, null, null,
            null, null, mockIssueSecuritySchemeManager, null);

        assertEquals(new Long(5), automaticDataMapper.getIssueSecuritySchemeId("TST"));

        mockIssueSecuritySchemeManagerControl.verify();
        mockProjectManagerControl.verify();
    }

    @Test
    public void testGetIssueSecuritySchemeIdNoProject() throws Exception
    {
        final MockControl mockProjectManagerControl = MockControl.createStrictControl(ProjectManager.class);
        final ProjectManager mockProjectManager = (ProjectManager) mockProjectManagerControl.getMock();
        mockProjectManager.getProjectByKey("TST");
        mockProjectManagerControl.setReturnValue(null);
        mockProjectManagerControl.replay();

        final AutomaticDataMapperImpl automaticDataMapper = new AutomaticDataMapperImpl(null, null, null, mockProjectManager, null, null, null, null,
            null, null, null, null);

        assertNull(automaticDataMapper.getIssueSecuritySchemeId("TST"));

        mockProjectManagerControl.verify();
    }

    @Test
    public void testGetIssueSecuritySchemeIdProjectExistsWithEmptyScheme() throws Exception
    {
        final GenericValue projectGV = new MockGenericValue("Project");

        final MockControl mockProjectManagerControl = MockControl.createStrictControl(ProjectManager.class);
        final ProjectManager mockProjectManager = (ProjectManager) mockProjectManagerControl.getMock();
        mockProjectManager.getProjectByKey("TST");
        mockProjectManagerControl.setReturnValue(projectGV);
        mockProjectManagerControl.replay();

        final MockControl mockIssueSecuritySchemeManagerControl = MockControl.createStrictControl(IssueSecuritySchemeManager.class);
        final IssueSecuritySchemeManager mockIssueSecuritySchemeManager = (IssueSecuritySchemeManager) mockIssueSecuritySchemeManagerControl.getMock();
        mockIssueSecuritySchemeManager.getSchemes(projectGV);
        mockIssueSecuritySchemeManagerControl.setReturnValue(EasyList.build());
        mockIssueSecuritySchemeManagerControl.replay();

        final AutomaticDataMapperImpl automaticDataMapper = new AutomaticDataMapperImpl(null, null, null, mockProjectManager, null, null, null, null,
            null, null, mockIssueSecuritySchemeManager, null);

        assertNull(automaticDataMapper.getIssueSecuritySchemeId("TST"));

        mockIssueSecuritySchemeManagerControl.verify();
        mockProjectManagerControl.verify();
    }

    @Test
    public void testMapIssueSecurityLevelsHappyPath() throws Exception
    {
        final MockControl mockIssueSecurityLevelManagerControl = MockControl.createStrictControl(IssueSecurityLevelManager.class);
        final IssueSecurityLevelManager mockIssueSecurityLevelManager = (IssueSecurityLevelManager) mockIssueSecurityLevelManagerControl.getMock();
        mockIssueSecurityLevelManager.getSchemeIssueSecurityLevels(new Long(5));
        mockIssueSecurityLevelManagerControl.setReturnValue(EasyList.build(new MockGenericValue("BSThing", EasyMap.build("name", "level1", "id",
            new Long(4))), new MockGenericValue("BSThing", EasyMap.build("name", "level2", "id", new Long(3)))));
        mockIssueSecurityLevelManagerControl.replay();

        final SimpleProjectImportIdMapperImpl issueSecurityLevelMapper = new SimpleProjectImportIdMapperImpl();

        issueSecurityLevelMapper.registerOldValue("1", "level1");
        issueSecurityLevelMapper.registerOldValue("2", "level2");
        issueSecurityLevelMapper.registerOldValue("3", "level3");
        final AutomaticDataMapperImpl automaticDataMapper = new AutomaticDataMapperImpl(null, null, null, null, null, null, null, null, null,
            mockIssueSecurityLevelManager, null, null)
        {
            @Override
            Long getIssueSecuritySchemeId(final String projectKey)
            {
                return new Long(5);
            }
        };

        automaticDataMapper.mapIssueSecurityLevels("TST", issueSecurityLevelMapper);
        assertEquals("4", issueSecurityLevelMapper.getMappedId("1"));
        assertEquals("3", issueSecurityLevelMapper.getMappedId("2"));
        assertNull(issueSecurityLevelMapper.getMappedId("3"));
        mockIssueSecurityLevelManagerControl.verify();
    }

    @Test
    public void testMapIssueSecurityLevelsNoScheme() throws Exception
    {
        final MockControl mockIssueSecurityLevelManagerControl = MockControl.createStrictControl(IssueSecurityLevelManager.class);
        final IssueSecurityLevelManager mockIssueSecurityLevelManager = (IssueSecurityLevelManager) mockIssueSecurityLevelManagerControl.getMock();
        mockIssueSecurityLevelManagerControl.replay();

        final SimpleProjectImportIdMapperImpl issueSecurityLevelMapper = new SimpleProjectImportIdMapperImpl();

        issueSecurityLevelMapper.registerOldValue("1", "level1");
        issueSecurityLevelMapper.registerOldValue("2", "level2");
        issueSecurityLevelMapper.registerOldValue("3", "level3");
        final AutomaticDataMapperImpl automaticDataMapper = new AutomaticDataMapperImpl(null, null, null, null, null, null, null, null, null,
            mockIssueSecurityLevelManager, null, null)
        {
            @Override
            Long getIssueSecuritySchemeId(final String projectKey)
            {
                return null;
            }
        };

        automaticDataMapper.mapIssueSecurityLevels("TST", issueSecurityLevelMapper);
        assertNull(issueSecurityLevelMapper.getMappedId("1"));
        assertNull(issueSecurityLevelMapper.getMappedId("2"));
        assertNull(issueSecurityLevelMapper.getMappedId("3"));
        mockIssueSecurityLevelManagerControl.verify();
    }

    @Test
    public void testMapIssueTypes()
    {
        // Create our issueTypeMapper and add the Issue Types found in the import file.
        final IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.registerOldValue("600", "Task", true);
        issueTypeMapper.registerOldValue("601", "WasteTime", true);
        issueTypeMapper.registerOldValue("602", "Bug", false);
        issueTypeMapper.registerOldValue("603", "New Feature", true);

        final MockControl mockIssueTypeImportHelperControl = MockClassControl.createControl(IssueTypeImportHelper.class);
        final IssueTypeImportHelper mockIssueTypeImportHelper = (IssueTypeImportHelper) mockIssueTypeImportHelperControl.getMock();

        mockIssueTypeImportHelper.getIssueTypeForName("Task");
        final MockIssueType task = new MockIssueType("12", "Task");
        mockIssueTypeImportHelperControl.setReturnValue(task);

        mockIssueTypeImportHelper.getIssueTypeForName("WasteTime");
        mockIssueTypeImportHelperControl.setReturnValue(null);

        mockIssueTypeImportHelper.getIssueTypeForName("Bug");
        final MockIssueType bug = new MockIssueType("10", "Bug");
        mockIssueTypeImportHelperControl.setReturnValue(bug);

        mockIssueTypeImportHelper.getIssueTypeForName("New Feature");
        final MockIssueType newFeature = new MockIssueType("11", "New Feature");
        mockIssueTypeImportHelperControl.setReturnValue(newFeature);

        mockIssueTypeImportHelper.isMappingValid(bug, "TST", false);
        mockIssueTypeImportHelperControl.setReturnValue(true);

        mockIssueTypeImportHelper.isMappingValid(task, "TST", true);
        mockIssueTypeImportHelperControl.setReturnValue(true);

        mockIssueTypeImportHelper.isMappingValid(newFeature, "TST", true);
        mockIssueTypeImportHelperControl.setReturnValue(false);

        mockIssueTypeImportHelperControl.replay();

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        final BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
            EasyList.build(new Long(12), new Long(14)));

        final AutomaticDataMapper automaticDataMapper = new AutomaticDataMapperImpl(null, null, null, null, mockIssueTypeImportHelper, null, null,
            null, null, null, null, null);
        automaticDataMapper.mapIssueTypes(backupProject, issueTypeMapper);

        assertEquals("12", issueTypeMapper.getMappedId("600"));
        assertEquals("10", issueTypeMapper.getMappedId("602"));
        // We expect the others to not map
        assertEquals(null, issueTypeMapper.getMappedId("601"));
        assertEquals(null, issueTypeMapper.getMappedId("603"));
        mockIssueTypeImportHelperControl.verify();
    }

    @Test
    public void testMapIssueLinkTypesSubtasksNotEnabled()
    {
        // Create and populate our IssueLinkTypeMapper
        final IssueLinkTypeMapper issueLinkTypeMapper = new IssueLinkTypeMapper();
        issueLinkTypeMapper.registerOldValue("2", "xxx", "jira_subtask");

        final MockControl mockIssueLinkTypeManagerControl = MockControl.createStrictControl(IssueLinkTypeManager.class);
        final IssueLinkTypeManager mockIssueLinkTypeManager = (IssueLinkTypeManager) mockIssueLinkTypeManagerControl.getMock();
        mockIssueLinkTypeManager.getIssueLinkTypesByName("xxx");
        mockIssueLinkTypeManagerControl.setReturnValue(EasyList.build(new MockIssueLinkType(102, "xxx", "A", "B", "jira_subtask")));
        mockIssueLinkTypeManagerControl.replay();

        // Mock SubTaskManager
        final MockControl mockSubTaskManagerControl = MockControl.createStrictControl(SubTaskManager.class);
        final SubTaskManager mockSubTaskManager = (SubTaskManager) mockSubTaskManagerControl.getMock();
        mockSubTaskManager.isSubTasksEnabled();
        mockSubTaskManagerControl.setReturnValue(false);
        mockSubTaskManagerControl.replay();

        final AutomaticDataMapper automaticDataMapper = new AutomaticDataMapperImpl(null, null, null, null, null, null, null,
            mockIssueLinkTypeManager, mockSubTaskManager, null, null, null);
        automaticDataMapper.mapIssueLinkTypes(issueLinkTypeMapper);
        // Now verify expected results:
        assertEquals(null, issueLinkTypeMapper.getMappedId("2"));

        // Verify Mock objects
        mockIssueLinkTypeManagerControl.verify();
        mockSubTaskManagerControl.verify();
    }

    @Test
    public void testMapIssueLinkTypes()
    {
        // Create and populate our IssueLinkTypeMapper
        final IssueLinkTypeMapper issueLinkTypeMapper = new IssueLinkTypeMapper();
        issueLinkTypeMapper.registerOldValue("1", "Duplicate", null);
        issueLinkTypeMapper.registerOldValue("2", "xxx", "jira_subtask");
        issueLinkTypeMapper.registerOldValue("3", "yyy", "green");
        issueLinkTypeMapper.registerOldValue("4", "zzz", null);
        issueLinkTypeMapper.registerOldValue("5", "ImportOnly", null);

        final MockControl mockIssueLinkTypeManagerControl = MockControl.createControl(IssueLinkTypeManager.class);
        final IssueLinkTypeManager mockIssueLinkTypeManager = (IssueLinkTypeManager) mockIssueLinkTypeManagerControl.getMock();
        mockIssueLinkTypeManager.getIssueLinkTypesByName("Duplicate");
        mockIssueLinkTypeManagerControl.setReturnValue(EasyList.build(new MockIssueLinkType(101, "Duplicate", "duplicates", "is duplicated by", null)));
        mockIssueLinkTypeManager.getIssueLinkTypesByName("xxx");
        mockIssueLinkTypeManagerControl.setReturnValue(EasyList.build(new MockIssueLinkType(102, "xxx", "A", "B", "jira_subtask")));
        mockIssueLinkTypeManager.getIssueLinkTypesByName("yyy");
        mockIssueLinkTypeManagerControl.setReturnValue(EasyList.build(new MockIssueLinkType(103, "yyy", "A", "B", "jira_subtask")));
        mockIssueLinkTypeManager.getIssueLinkTypesByName("zzz");
        mockIssueLinkTypeManagerControl.setReturnValue(EasyList.build(new MockIssueLinkType(104, "zzz", "A", "B", "jira_subtask")));
        mockIssueLinkTypeManager.getIssueLinkTypesByName("ImportOnly");
        mockIssueLinkTypeManagerControl.setReturnValue(Collections.EMPTY_LIST);
        mockIssueLinkTypeManagerControl.replay();

        final MockControl mockSubTaskManagerControl = MockControl.createControl(SubTaskManager.class);
        final SubTaskManager mockSubTaskManager = (SubTaskManager) mockSubTaskManagerControl.getMock();
        mockSubTaskManager.isSubTasksEnabled();
        mockSubTaskManagerControl.setReturnValue(true);
        mockSubTaskManagerControl.replay();

        final AutomaticDataMapper automaticDataMapper = new AutomaticDataMapperImpl(null, null, null, null, null, null, null,
            mockIssueLinkTypeManager, mockSubTaskManager, null, null, null);
        automaticDataMapper.mapIssueLinkTypes(issueLinkTypeMapper);
        // Now verify expected results:
        assertEquals("101", issueLinkTypeMapper.getMappedId("1"));
        assertEquals("102", issueLinkTypeMapper.getMappedId("2"));
        // Not Mapped - wrong style
        assertEquals(null, issueLinkTypeMapper.getMappedId("3"));
        // Not Mapped - wrong style
        assertEquals(null, issueLinkTypeMapper.getMappedId("4"));
        // Not Mapped - missing in current system
        assertEquals(null, issueLinkTypeMapper.getMappedId("5"));

        // Verify Mock objects
        mockIssueLinkTypeManagerControl.verify();
        mockSubTaskManagerControl.verify();
    }

    @Test
    public void testMapPriorities()
    {
        // Set up a mock ConstantsManager
        final Mock mockConstantsManager = new Mock(ConstantsManager.class);
        mockConstantsManager.setStrict(true);
        mockConstantsManager.expectAndReturn("getPriorityObjects", P.ANY_ARGS, EasyList.build(new MockPriority("10", "P1"), new MockPriority("11",
            "P2"), new MockPriority("12", "P3")));
        final ConstantsManager constantsManager = (ConstantsManager) mockConstantsManager.proxy();

        // Create our mapper and add the stuff found in the import file.
        final SimpleProjectImportIdMapper mapper = new SimpleProjectImportIdMapperImpl();
        mapper.registerOldValue("41", "P3");
        mapper.registerOldValue("42", "P4");
        mapper.registerOldValue("43", "P1");
        mapper.registerOldValue("44", "P2");

        final AutomaticDataMapper automaticDataMapper = new AutomaticDataMapperImpl(constantsManager, null, null, null, null, null, null, null, null,
            null, null, null);
        automaticDataMapper.mapPriorities(mapper);

        // Check the Old ID to new ID mapping
        assertEquals("12", mapper.getMappedId("41"));
        assertEquals("10", mapper.getMappedId("43"));
        assertEquals("11", mapper.getMappedId("44"));
        // P4 could not be mapped.
        assertEquals(null, mapper.getMappedId("42"));
        mockConstantsManager.verify();
    }

    @Test
    public void testMapResolutions()
    {
        // Set up a mock ConstantsManager
        final Mock mockConstantsManager = new Mock(ConstantsManager.class);
        mockConstantsManager.setStrict(true);
        mockConstantsManager.expectAndReturn("getResolutionObjects", P.ANY_ARGS, EasyList.build(new MockResolution("27", "Fixed"),
            new MockResolution("28", "Duplicate"), new MockResolution("29", "Ignored")));
        final ConstantsManager constantsManager = (ConstantsManager) mockConstantsManager.proxy();

        // Create our mapper and add the stuff found in the import file.
        final SimpleProjectImportIdMapper mapper = new SimpleProjectImportIdMapperImpl();
        mapper.registerOldValue("41", "Duplicate");
        mapper.registerOldValue("42", "Fixed");
        mapper.registerOldValue("43", "Whatever");

        final AutomaticDataMapper automaticDataMapper = new AutomaticDataMapperImpl(constantsManager, null, null, null, null, null, null, null, null,
            null, null, null);
        automaticDataMapper.mapResolutions(mapper);

        // Check the Old ID to new ID mapping
        assertEquals("28", mapper.getMappedId("41"));
        assertEquals("27", mapper.getMappedId("42"));
        // "Whatever" could not be mapped.
        assertEquals(null, mapper.getMappedId("43"));
        mockConstantsManager.verify();
    }

    @Test
    public void testMapStatuses()
    {
        // Set up a mock ConstantsManager
        final MockControl mockConstantsManagerControl = MockControl.createControl(ConstantsManager.class);
        final ConstantsManager mockConstantsManager = (ConstantsManager) mockConstantsManagerControl.getMock();
        mockConstantsManager.getStatusByName("Closed");
        final MockStatus closedStatus = new MockStatus("30", "Closed");
        mockConstantsManagerControl.setReturnValue(closedStatus);
        mockConstantsManager.getStatusByName("In Progress");
        mockConstantsManagerControl.setReturnValue(null);
        mockConstantsManager.getStatusByName("Open");
        final MockStatus openStatus = new MockStatus("27", "Open");
        mockConstantsManagerControl.setReturnValue(openStatus);
        mockConstantsManagerControl.replay();

        // Create our mapper and add the stuff found in the import file.
        final StatusMapper mapper = new StatusMapper();
        mapper.registerOldValue("41", "Closed");
        mapper.registerOldValue("42", "Open");
        mapper.registerOldValue("43", "In Progress");
        mapper.flagValueAsRequired("41", "111");
        mapper.flagValueAsRequired("42", "222");

        final IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.mapValue("111", "333");
        issueTypeMapper.mapValue("222", "444");

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        final BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
            EasyList.build(new Long(12), new Long(14)));

        final MockControl mockStatusMapperValidatorControl = MockControl.createControl(StatusMapperValidator.class);
        final StatusMapperValidator mockStatusMapperValidator = (StatusMapperValidator) mockStatusMapperValidatorControl.getMock();
        mockStatusMapperValidator.isStatusValid("41", closedStatus, mapper, issueTypeMapper, "TST");
        mockStatusMapperValidatorControl.setReturnValue(true);
        mockStatusMapperValidator.isStatusValid("42", openStatus, mapper, issueTypeMapper, "TST");
        mockStatusMapperValidatorControl.setReturnValue(false);
        mockStatusMapperValidatorControl.replay();

        final AutomaticDataMapper automaticDataMapper = new AutomaticDataMapperImpl(mockConstantsManager, null, null, null, null,
            mockStatusMapperValidator, null, null, null, null, null, null);
        automaticDataMapper.mapStatuses(backupProject, mapper, issueTypeMapper);

        // Mapped status
        assertEquals("30", mapper.getMappedId("41"));
        // "Whatever" could not be mapped.
        assertNull(mapper.getMappedId("43"));
        assertNull(mapper.getMappedId("42"));
        mockConstantsManagerControl.verify();
        mockStatusMapperValidatorControl.verify();
    }

    @Test
    public void testMapCustomFieldsFieldTypeNotImportable()
    {
        // Create a mock CustomFieldMapperValidator
        final Mock mockCustomFieldMapperValidator = new Mock(CustomFieldMapperValidator.class);
        mockCustomFieldMapperValidator.setStrict(true);
        mockCustomFieldMapperValidator.expectAndReturn("customFieldTypeIsImportable", P.args(P.eq("a.b.c:Colour")), Boolean.FALSE);
        final CustomFieldMapperValidator customFieldMapperValidator = (CustomFieldMapperValidator) mockCustomFieldMapperValidator.proxy();

        final ExternalProject project = new ExternalProject();
        project.setId("2");
        project.setKey("MKY");
        final List customFieldConfigurations = EasyList.build(new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("13", "John",
            "a.b.c:Colour"), "12"));
        final BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST, customFieldConfigurations,
            Collections.EMPTY_LIST);

        final AutomaticDataMapperImpl automaticDataMapper = new AutomaticDataMapperImpl(null, null, customFieldMapperValidator, null, null, null,
            null, null, null, null, null, null);
        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        automaticDataMapper.mapCustomFields(backupProject, customFieldMapper, null);
        // This should have not mapped any fields
        assertNull(customFieldMapper.getMappedId("13"));

        mockCustomFieldMapperValidator.verify();
    }

    @Test
    public void testMapCustomFieldsFieldMissing()
    {
        // Create a mock CustomFieldMapperValidator
        final Mock mockCustomFieldMapperValidator = new Mock(CustomFieldMapperValidator.class);
        mockCustomFieldMapperValidator.setStrict(true);
        mockCustomFieldMapperValidator.expectAndReturn("customFieldTypeIsImportable", P.args(P.eq("a.b.c:Colour")), Boolean.TRUE);
        final CustomFieldMapperValidator customFieldMapperValidator = (CustomFieldMapperValidator) mockCustomFieldMapperValidator.proxy();

        final ExternalProject project = new ExternalProject();
        project.setId("2");
        project.setKey("MKY");
        final List customFieldConfigurations = EasyList.build(new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("13", "John",
            "a.b.c:Colour"), "12"));
        final BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST, customFieldConfigurations,
            Collections.EMPTY_LIST);

        // Make a mock custom field manager
        final Mock mockCustomFieldManager = new Mock(CustomFieldManager.class);
        mockCustomFieldManager.setStrict(true);
        // We want to test when no custom fields with the given name exist
        mockCustomFieldManager.expectAndReturn("getCustomFieldObjectsByName", P.ANY_ARGS, Collections.EMPTY_LIST);

        final AutomaticDataMapperImpl automaticDataMapper = new AutomaticDataMapperImpl(null, (CustomFieldManager) mockCustomFieldManager.proxy(),
            customFieldMapperValidator, null, null, null, null, null, null, null, null, null);
        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        automaticDataMapper.mapCustomFields(backupProject, customFieldMapper, null);
        // This should have not mapped any fields
        assertNull(customFieldMapper.getMappedId("13"));

        mockCustomFieldManager.verify();
        mockCustomFieldMapperValidator.verify();
    }

    @Test
    public void testMapCustomFieldsOrphanRequiredValueGetsIgnored()
    {
        // Create a mock CustomFieldMapperValidator
        final Mock mockCustomFieldMapperValidator = new Mock(CustomFieldMapperValidator.class);
        mockCustomFieldMapperValidator.setStrict(true);
        mockCustomFieldMapperValidator.expectAndReturn("customFieldTypeIsImportable", P.args(P.eq("a.b.c:Colour")), Boolean.TRUE);
        final CustomFieldMapperValidator customFieldMapperValidator = (CustomFieldMapperValidator) mockCustomFieldMapperValidator.proxy();

        final ExternalProject project = new ExternalProject();
        project.setId("2");
        project.setKey("MKY");
        final List customFieldConfigurations = EasyList.build(new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("13", "John",
            "a.b.c:Colour"), "12"));
        final BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST, customFieldConfigurations,
            Collections.EMPTY_LIST);

        // Make a mock custom field manager
        final Mock mockCustomFieldManager = new Mock(CustomFieldManager.class);
        mockCustomFieldManager.setStrict(true);
        // We want to test when no custom fields with the given name exist
        mockCustomFieldManager.expectAndReturn("getCustomFieldObjectsByName", P.ANY_ARGS, Collections.EMPTY_LIST);

        final AutomaticDataMapperImpl automaticDataMapper = new AutomaticDataMapperImpl(null, (CustomFieldManager) mockCustomFieldManager.proxy(),
            customFieldMapperValidator, null, null, null, null, null, null, null, null, null);
        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        // Flag custom field 12 as required but don't register it
        customFieldMapper.flagValueAsRequired("12", "12");

        automaticDataMapper.mapCustomFields(backupProject, customFieldMapper, null);
        // This should have not mapped any fields
        assertNull(customFieldMapper.getMappedId("13"));
        // This should also have flagged the field as ignored
        assertTrue(customFieldMapper.isIgnoredCustomField("12"));

        mockCustomFieldManager.verify();
        mockCustomFieldMapperValidator.verify();
    }

    @Test
    public void testMapCustomFieldsFieldWrongType()
    {
        // Create a mock CustomFieldMapperValidator
        final Mock mockCustomFieldMapperValidator = new Mock(CustomFieldMapperValidator.class);
        mockCustomFieldMapperValidator.setStrict(true);
        mockCustomFieldMapperValidator.expectAndReturn("customFieldTypeIsImportable", P.args(P.eq("a.b.c:Colour")), Boolean.TRUE);
        final CustomFieldMapperValidator customFieldMapperValidator = (CustomFieldMapperValidator) mockCustomFieldMapperValidator.proxy();

        final MockProjectManager mockProjectManager = new MockProjectManager();
        mockProjectManager.addProject(new MockGenericValue("Project", EasyMap.build("id", new Long(2), "key", "MKY")));

        final ExternalProject project = new ExternalProject();
        project.setId("2");
        project.setKey("MKY");
        final List customFieldConfigurations = EasyList.build(new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("13", "John",
            "a.b.c:Colour"), "12"));
        final BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST, customFieldConfigurations,
            Collections.EMPTY_LIST);

        // Make a mock custom field manager
        final Mock mockCustomFieldManager = new Mock(CustomFieldManager.class);
        mockCustomFieldManager.setStrict(true);

        // Make a mock Custom Field Type
        final Mock mockCustomFieldType = new Mock(CustomFieldType.class);
        mockCustomFieldType.setStrict(true);
        mockCustomFieldType.expectAndReturn("getKey", "a.b.c:WRONG");

        // Make a mock Custom Field
        final Mock mockCustomField = new Mock(CustomField.class);
        mockCustomField.setStrict(true);
        mockCustomField.expectAndReturn("getCustomFieldType", P.ANY_ARGS, mockCustomFieldType.proxy());

        mockCustomFieldManager.expectAndReturn("getCustomFieldObjectsByName", P.args(P.eq("John")), EasyList.build(mockCustomField.proxy()));

        final AutomaticDataMapperImpl automaticDataMapper = new AutomaticDataMapperImpl(null, (CustomFieldManager) mockCustomFieldManager.proxy(),
            customFieldMapperValidator, null, null, null, null, null, null, null, null, null);
        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        automaticDataMapper.mapCustomFields(backupProject, customFieldMapper, null);
        // This should have not mapped any fields
        assertNull(customFieldMapper.getMappedId("13"));

        mockCustomFieldManager.verify();
        mockCustomField.verify();
        mockCustomFieldType.verify();
    }

    @Test
    public void testMapCustomFieldsFieldInvalidForIssueType()
    {
        final Mock mockCustomFieldMapperValidator = new Mock(CustomFieldMapperValidator.class);
        mockCustomFieldMapperValidator.setStrict(true);
        mockCustomFieldMapperValidator.expectAndReturn("customFieldIsValidForRequiredContexts", P.ANY_ARGS, Boolean.FALSE);
        mockCustomFieldMapperValidator.expectAndReturn("customFieldTypeIsImportable", P.args(P.eq("a.b.c:Colour")), Boolean.TRUE);

        final ExternalProject project = new ExternalProject();
        project.setId("2");
        project.setKey("MKY");
        final List customFieldConfigurations = EasyList.build(new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("13", "John",
            "a.b.c:Colour"), "12"));
        final BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST, customFieldConfigurations,
            Collections.EMPTY_LIST);

        // Make a mock custom field manager
        final Mock mockCustomFieldManager = new Mock(CustomFieldManager.class);
        mockCustomFieldManager.setStrict(true);

        // Make a mock Custom Field Type
        final Mock mockCustomFieldType = new Mock(CustomFieldType.class);
        mockCustomFieldType.setStrict(true);
        mockCustomFieldType.expectAndReturn("getKey", "a.b.c:Colour");

        // Make a mock Custom Field
        final Mock mockCustomField = new Mock(CustomField.class);
        mockCustomField.setStrict(true);
        mockCustomField.expectAndReturn("getCustomFieldType", P.ANY_ARGS, mockCustomFieldType.proxy());

        mockCustomFieldManager.expectAndReturn("getCustomFieldObjectsByName", P.args(P.eq("John")), EasyList.build(mockCustomField.proxy()));

        // Set up the Mapper with some values.
        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.flagValueAsRequired("13", "2001");
        customFieldMapper.flagIssueTypeInUse("2001", "1");

        final AutomaticDataMapperImpl automaticDataMapper = new AutomaticDataMapperImpl(null, (CustomFieldManager) mockCustomFieldManager.proxy(),
            (CustomFieldMapperValidator) mockCustomFieldMapperValidator.proxy(), null, null, null, null, null, null, null, null, null);
        automaticDataMapper.mapCustomFields(backupProject, customFieldMapper, null);
        // This should have not mapped any fields
        assertNull(customFieldMapper.getMappedId("13"));

        mockCustomFieldManager.verify();
        mockCustomField.verify();
        mockCustomFieldType.verify();
        mockCustomFieldMapperValidator.verify();
    }

    @Test
    public void testMapCustomFieldsFieldSimpleHappyPath()
    {
        final Mock mockCustomFieldMapperValidator = new Mock(CustomFieldMapperValidator.class);
        mockCustomFieldMapperValidator.setStrict(true);
        mockCustomFieldMapperValidator.expectAndReturn("customFieldIsValidForRequiredContexts", P.ANY_ARGS, Boolean.TRUE);
        mockCustomFieldMapperValidator.expectAndReturn("customFieldTypeIsImportable", P.args(P.eq("a.b.c:Colour")), Boolean.TRUE);

        final ExternalProject project = new ExternalProject();
        project.setId("2");
        project.setKey("MKY");
        final List customFieldConfigurations = EasyList.build(new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("13", "John",
            "a.b.c:Colour"), "12"));
        final BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST, customFieldConfigurations,
            Collections.EMPTY_LIST);

        // Make a mock custom field manager
        final Mock mockCustomFieldManager = new Mock(CustomFieldManager.class);
        mockCustomFieldManager.setStrict(true);

        // Make a mock Custom Field Type
        final Mock mockCustomFieldType = new Mock(CustomFieldType.class);
        mockCustomFieldType.setStrict(true);
        mockCustomFieldType.expectAndReturn("getKey", "a.b.c:Colour");

        // Make a mock Custom Field
        final Mock mockCustomField = new Mock(CustomField.class);
        mockCustomField.setStrict(true);
        mockCustomField.expectAndReturn("getIdAsLong", new Long(1000000));
        mockCustomField.expectAndReturn("getCustomFieldType", P.ANY_ARGS, mockCustomFieldType.proxy());
        // !!! We want to return NOT null because we want the Custom Field to no be valid for the reuired Issue Type.

        mockCustomFieldManager.expectAndReturn("getCustomFieldObjectsByName", P.args(P.eq("John")), EasyList.build(mockCustomField.proxy()));

        // Set up the Mapper with some values.
        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.registerOldValue("13", "Test Custom Field");
        customFieldMapper.flagValueAsRequired("13", "2001");
        customFieldMapper.flagIssueTypeInUse("2001", "1");

        final AutomaticDataMapperImpl automaticDataMapper = new AutomaticDataMapperImpl(null, (CustomFieldManager) mockCustomFieldManager.proxy(),
            (CustomFieldMapperValidator) mockCustomFieldMapperValidator.proxy(), null, null, null, null, null, null, null, null, null);
        automaticDataMapper.mapCustomFields(backupProject, customFieldMapper, null);
        // This should have just one mapped field
        assertEquals("1000000", customFieldMapper.getMappedId("13"));
        assertNull(customFieldMapper.getMappedId("14"));

        mockCustomFieldManager.verify();
        mockCustomField.verify();
        mockCustomFieldType.verify();
    }

    @Test
    public void testMapProjectSimpleHappyPath()
    {
        final Mock mockProjectManager = new Mock(ProjectManager.class);
        mockProjectManager.setStrict(true);
        mockProjectManager.expectAndReturn("getProjects", EasyList.build(new MockGenericValue("Project", EasyMap.build("id", new Long(1), "key",
            "TST")), new MockGenericValue("Project", EasyMap.build("id", new Long(2), "key", "QQQ"))));

        final AutomaticDataMapperImpl automaticDataMapper = new AutomaticDataMapperImpl(null, null, null,
            (ProjectManager) mockProjectManager.proxy(), null, null, null, null, null, null, null, null);
        final SimpleProjectImportIdMapper projectMapper = new SimpleProjectImportIdMapperImpl();
        projectMapper.registerOldValue("432", "QQQ");
        projectMapper.registerOldValue("433", "UWM");
        automaticDataMapper.mapProjects(projectMapper);
        assertEquals("2", projectMapper.getMappedId("432"));
        assertNull(projectMapper.getMappedId("433"));
        assertNull(projectMapper.getMappedId("1"));
        assertNull(projectMapper.getMappedId("2"));

        mockProjectManager.verify();
    }

    @Test
    public void testMapOptionsForParent() throws Exception
    {
        final ExternalCustomFieldOption oldParentOption = new ExternalCustomFieldOption("111", "222", "333", null, "Choc");
        final ExternalCustomFieldOption oldParentOption2 = new ExternalCustomFieldOption("1", "2", "3", null, "lkjasdf");

        final MockControl mockOptionsControl = MockControl.createStrictControl(Options.class);
        final Options mockOptions = (Options) mockOptionsControl.getMock();

        mockOptions.getOptionForValue("Choc", null);
        mockOptionsControl.setReturnValue(new MockOption(null, null, null, "Choc", null, new Long(555)));

        mockOptions.getOptionForValue("lkjasdf", null);
        mockOptionsControl.setReturnValue(null);

        mockOptionsControl.replay();

        final CustomFieldOptionMapper customFieldOptionsMapper = new CustomFieldOptionMapper();
        customFieldOptionsMapper.registerOldValue(oldParentOption);
        customFieldOptionsMapper.registerOldValue(oldParentOption2);

        final AutomaticDataMapperImpl automaticDataMapper = new AutomaticDataMapperImpl(null, null, null, null, null, null, null, null, null, null,
            null, null);

        automaticDataMapper.mapOptions(mockOptions, customFieldOptionsMapper, EasyList.build(oldParentOption, oldParentOption2));

        assertEquals("555", customFieldOptionsMapper.getMappedId("111"));
        assertEquals(null, customFieldOptionsMapper.getMappedId("1"));
        mockOptionsControl.verify();
    }

    @Test
    public void testMapOptionsForParentAndChildren() throws Exception
    {
        final ExternalCustomFieldOption oldParentOption = new ExternalCustomFieldOption("111", "222", "333", null, "Choc");
        final ExternalCustomFieldOption oldChildOption = new ExternalCustomFieldOption("666", "222", "333", "111", "Straw");
        final ExternalCustomFieldOption oldChildOption2 = new ExternalCustomFieldOption("6", "2", "3", "111", "Van");

        final MockControl mockOptionsControl = MockControl.createStrictControl(Options.class);
        final Options mockOptions = (Options) mockOptionsControl.getMock();

        mockOptions.getOptionForValue("Choc", null);
        mockOptionsControl.setReturnValue(new MockOption(null, null, null, "Choc", null, new Long(555)));

        mockOptions.getOptionForValue("Straw", new Long(555));
        mockOptionsControl.setReturnValue(new MockOption(null, null, null, "Straw", null, new Long(777)));

        mockOptions.getOptionForValue("Van", new Long(555));
        mockOptionsControl.setReturnValue(null);

        mockOptionsControl.replay();

        final CustomFieldOptionMapper customFieldOptionsMapper = new CustomFieldOptionMapper();
        customFieldOptionsMapper.registerOldValue(oldParentOption);
        customFieldOptionsMapper.registerOldValue(oldChildOption);
        customFieldOptionsMapper.registerOldValue(oldChildOption2);

        final AutomaticDataMapperImpl automaticDataMapper = new AutomaticDataMapperImpl(null, null, null, null, null, null, null, null, null, null,
            null, null);

        automaticDataMapper.mapOptions(mockOptions, customFieldOptionsMapper, EasyList.build(oldParentOption));

        assertEquals("555", customFieldOptionsMapper.getMappedId("111"));
        assertEquals("777", customFieldOptionsMapper.getMappedId("666"));
        assertEquals(null, customFieldOptionsMapper.getMappedId("6"));

        mockOptionsControl.verify();
    }

    @Test
    public void testGetNewOptionsNoMapping() throws Exception
    {
        final AutomaticDataMapperImpl automaticDataMapper = new AutomaticDataMapperImpl(null, null, null, null, null, null, null, null, null, null,
            null, null);
        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();

        final ExternalCustomFieldConfiguration externalCustomFieldConfiguration = new ExternalCustomFieldConfiguration(null, null,
            new ExternalCustomField("12", "", ""), "432");
        assertNull(automaticDataMapper.getNewOptions(null, customFieldMapper, null, externalCustomFieldConfiguration));
    }

    @Test
    public void testGetNewOptions() throws Exception
    {
        final ExternalCustomFieldConfiguration externalCustomFieldConfiguration = new ExternalCustomFieldConfiguration(null, null,
            new ExternalCustomField("1", "CustoField", "some.key"), "2");

        final ProjectContext mockProjectContext = Mockito.mock(ProjectContext.class);

        final MockControl mockCustomFieldControl = MockControl.createStrictControl(CustomField.class);
        final CustomField mockCustomField = (CustomField) mockCustomFieldControl.getMock();
        mockCustomField.getOptions(null, null, mockProjectContext);
        mockCustomFieldControl.setReturnValue(null);
        mockCustomFieldControl.replay();

        final MockControl mockCustomFieldManagerControl = MockControl.createStrictControl(CustomFieldManager.class);
        final CustomFieldManager mockCustomFieldManager = (CustomFieldManager) mockCustomFieldManagerControl.getMock();
        mockCustomFieldManager.getCustomFieldObject(new Long(2));
        mockCustomFieldManagerControl.setReturnValue(mockCustomField);
        mockCustomFieldManagerControl.replay();

        final MockControl mockProjectManagerControl = MockControl.createStrictControl(ProjectManager.class);
        final ProjectManager mockProjectManager = (ProjectManager) mockProjectManagerControl.getMock();
        mockProjectManager.getProjectObjByKey("TST");
        mockProjectManagerControl.setReturnValue(null);
        mockProjectManagerControl.replay();

        final MockControl mockFieldConfigSchemeManagerControl = MockControl.createControl(FieldConfigSchemeManager.class);
        final FieldConfigSchemeManager mockFieldConfigSchemeManager = (FieldConfigSchemeManager) mockFieldConfigSchemeManagerControl.getMock();
        mockFieldConfigSchemeManager.getRelevantConfigScheme(mockProjectContext, mockCustomField);
        mockFieldConfigSchemeManagerControl.setReturnValue(null);
        mockFieldConfigSchemeManagerControl.replay();

        final AutomaticDataMapperImpl automaticDataMapper = new AutomaticDataMapperImpl(null, mockCustomFieldManager, null, mockProjectManager, null,
            null, null, null, null, null, null, mockFieldConfigSchemeManager)
        {
            JiraContextNode getProjectContext(final Long newProjectId)
            {
                return mockProjectContext;
            }
        };

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        final BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
            Collections.EMPTY_LIST);

        final IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.mapValue("3", "4");

        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.mapValue("1", "2");

        assertNull(automaticDataMapper.getNewOptions(backupProject, customFieldMapper, issueTypeMapper, externalCustomFieldConfiguration));

        mockProjectManagerControl.verify();
        mockCustomFieldManagerControl.verify();
        mockCustomFieldControl.verify();
    }

    @Test
    public void testGetNewOptionsRelevantConfigFound() throws Exception
    {
        final ExternalCustomFieldConfiguration externalCustomFieldConfiguration = new ExternalCustomFieldConfiguration(null, null,
            new ExternalCustomField("1", "CustoField", "some.key"), "2");

        final ProjectContext mockProjectContext = Mockito.mock(ProjectContext.class);

        CustomField mockCustomField = Mockito.mock(CustomField.class);
        when(mockCustomField.getOptions(null, null, mockProjectContext)).thenReturn(null);

        CustomFieldManager mockCustomFieldManager = Mockito.mock(CustomFieldManager.class);
        when(mockCustomFieldManager.getCustomFieldObject(new Long(2))).thenReturn(mockCustomField);

        ProjectManager mockProjectManager = Mockito.mock(ProjectManager.class);
        when(mockProjectManager.getProjectObjByKey("TST")).thenReturn(null);

        FieldConfigSchemeManager mockFieldConfigSchemeManager = Mockito.mock(FieldConfigSchemeManager.class);
        FieldConfigScheme mockfieldConfigSheme = Mockito.mock(FieldConfigScheme.class);
        FieldConfig mockFieldConfig = Mockito.mock(FieldConfig.class);
        when(mockFieldConfigSchemeManager.getRelevantConfigScheme(mockProjectContext, mockCustomField)).thenReturn(mockfieldConfigSheme);
        when(mockfieldConfigSheme.getOneAndOnlyConfig()).thenReturn(mockFieldConfig);

        final AutomaticDataMapperImpl automaticDataMapper = new AutomaticDataMapperImpl(null, mockCustomFieldManager, null, mockProjectManager, null,
            null, null, null, null, null, null, mockFieldConfigSchemeManager)
        {
            JiraContextNode getProjectContext(final Long newProjectId)
            {
                return mockProjectContext;
            }
        };

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        final BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
            Collections.EMPTY_LIST);

        final IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.mapValue("3", "4");

        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.mapValue("1", "2");

        automaticDataMapper.getNewOptions(backupProject, customFieldMapper, issueTypeMapper, externalCustomFieldConfiguration);

        ArgumentCaptor<FieldConfig> fieldConfigCaptor = ArgumentCaptor.forClass(FieldConfig.class);
        verify(mockCustomField).getOptions(Mockito.anyString(), fieldConfigCaptor.capture(), Mockito.eq(mockProjectContext));

        assertThat(fieldConfigCaptor.getValue(), sameInstance(mockFieldConfig));
    }

    @Test
    public void testMapCustomFieldOptions() throws Exception
    {
        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        final ExternalCustomFieldConfiguration externalCustomFieldConfiguration1 = new ExternalCustomFieldConfiguration(null, null,
            new ExternalCustomField("1", "Friend", "123"), "12");
        final ExternalCustomFieldConfiguration externalCustomFieldConfiguration2 = new ExternalCustomFieldConfiguration(null, null,
            new ExternalCustomField("2", "Schmoo", "4312"), "24");
        final List customFieldConfigurations = EasyList.build(externalCustomFieldConfiguration1, externalCustomFieldConfiguration2);
        final BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST, customFieldConfigurations,
            Collections.EMPTY_LIST);

        final CustomFieldOptionMapper customFieldOptionMapper = new CustomFieldOptionMapper();
        final ExternalCustomFieldOption customFieldOption = new ExternalCustomFieldOption("1", "40", "12", null, "Rum n Raisin");
        customFieldOptionMapper.registerOldValue(customFieldOption);

        final Mock mockOptions = new Mock(Options.class);
        final AtomicInteger optionsCalled = new AtomicInteger(0);
        final AtomicInteger mapOptionsCalled = new AtomicInteger(0);
        final AutomaticDataMapperImpl automaticDataMapper = new AutomaticDataMapperImpl(null, null, null, null, null, null, null, null, null, null,
            null, null)
        {
            Options getNewOptions(final BackupProject backupProject, final CustomFieldMapper customFieldMapper, final IssueTypeMapper issueTypeMapper, final ExternalCustomFieldConfiguration externalCustomFieldConfiguration)
            {
                optionsCalled.incrementAndGet();
                return (Options) mockOptions.proxy();
            }

            void mapOptions(final Options options, final CustomFieldOptionMapper customFieldOptionMapper, final Collection parentOptions)
            {
                assertEquals(1, parentOptions.size());
                assertEquals(customFieldOption, parentOptions.iterator().next());
                mapOptionsCalled.incrementAndGet();
                assertEquals(options, mockOptions);
            }
        };

        automaticDataMapper.mapCustomFieldOptions(backupProject, customFieldOptionMapper, null, null);

        assertEquals(1, optionsCalled.get());
        assertEquals(1, mapOptionsCalled.get());
    }

    @Test
    public void testMapCustomFieldOptionsCustomFieldNotMapped() throws Exception
    {
        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        final ExternalCustomFieldConfiguration externalCustomFieldConfiguration1 = new ExternalCustomFieldConfiguration(null, null,
            new ExternalCustomField("1", "Friend", "123"), "12");
        final List customFieldConfigurations = EasyList.build(externalCustomFieldConfiguration1);
        final BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST, customFieldConfigurations,
            Collections.EMPTY_LIST);

        final CustomFieldOptionMapper customFieldOptionMapper = new CustomFieldOptionMapper();
        final ExternalCustomFieldOption customFieldOption = new ExternalCustomFieldOption("1", "40", "12", null, "Rum n Raisin");
        customFieldOptionMapper.registerOldValue(customFieldOption);

        final AtomicInteger optionsCalled = new AtomicInteger(0);
        final AutomaticDataMapperImpl automaticDataMapper = new AutomaticDataMapperImpl(null, null, null, null, null, null, null, null, null, null,
            null, null)
        {
            Options getNewOptions(final BackupProject backupProject, final CustomFieldMapper customFieldMapper, final IssueTypeMapper issueTypeMapper, final ExternalCustomFieldConfiguration externalCustomFieldConfiguration)
            {
                optionsCalled.incrementAndGet();
                return null;
            }

            void mapOptions(final Options options, final CustomFieldOptionMapper customFieldOptionMapper, final Collection parentOptions)
            {
                fail("Should not be called.");
            }
        };

        automaticDataMapper.mapCustomFieldOptions(backupProject, customFieldOptionMapper, null, null);

        assertEquals(1, optionsCalled.get());
    }

    @Test
    public void testMapProjectRoles()
    {
        // Set up a mock ProjectRoleManager
        final MockControl mockProjectRoleManagerControl = MockControl.createControl(ProjectRoleManager.class);
        final ProjectRoleManager mockProjectRoleManager = (ProjectRoleManager) mockProjectRoleManagerControl.getMock();
        mockProjectRoleManager.getProjectRole("Administrators");
        mockProjectRoleManagerControl.setReturnValue(new MockProjectRoleManager.MockProjectRole(12, "Administrators", ""));
        mockProjectRoleManager.getProjectRole("Developers");
        mockProjectRoleManagerControl.setReturnValue(new MockProjectRoleManager.MockProjectRole(13, "Developers", ""));
        mockProjectRoleManager.getProjectRole("Users");
        mockProjectRoleManagerControl.setReturnValue(null);
        mockProjectRoleManagerControl.replay();

        // Create our mapper and add the stuff found in the import file.
        final SimpleProjectImportIdMapper mapper = new SimpleProjectImportIdMapperImpl();
        mapper.registerOldValue("41", "Administrators");
        mapper.registerOldValue("42", "Developers");
        mapper.registerOldValue("43", "Users");

        final AutomaticDataMapper automaticDataMapper = new AutomaticDataMapperImpl(null, null, null, null, null, null, mockProjectRoleManager, null,
            null, null, null, null);
        automaticDataMapper.mapProjectRoles(mapper);

        // Check the Old ID to new ID mapping
        assertEquals("12", mapper.getMappedId("41"));
        assertEquals("13", mapper.getMappedId("42"));
        // "Users" could not be mapped.
        assertEquals(null, mapper.getMappedId("43"));
        mockProjectRoleManagerControl.verify();
    }

}
