package com.atlassian.jira.imports.project;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.external.beans.ExternalComponent;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.external.beans.ExternalVersion;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.BackupSystemInformationImpl;
import com.atlassian.jira.imports.project.core.ProjectImportData;
import com.atlassian.jira.imports.project.core.ProjectImportOptions;
import com.atlassian.jira.imports.project.core.ProjectImportOptionsImpl;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldConfiguration;
import com.atlassian.jira.imports.project.handler.ChainedSaxHandler;
import com.atlassian.jira.imports.project.handler.CustomFieldMapperHandler;
import com.atlassian.jira.imports.project.handler.CustomFieldOptionsMapperHandler;
import com.atlassian.jira.imports.project.handler.GroupMapperHandler;
import com.atlassian.jira.imports.project.handler.IssueComponentMapperHandler;
import com.atlassian.jira.imports.project.handler.IssueLinkMapperHandler;
import com.atlassian.jira.imports.project.handler.IssueMapperHandler;
import com.atlassian.jira.imports.project.handler.IssuePartitonHandler;
import com.atlassian.jira.imports.project.handler.IssueRelatedEntitiesPartionHandler;
import com.atlassian.jira.imports.project.handler.IssueTypeMapperHandler;
import com.atlassian.jira.imports.project.handler.IssueVersionMapperHandler;
import com.atlassian.jira.imports.project.handler.ProjectIssueSecurityLevelMapperHandler;
import com.atlassian.jira.imports.project.handler.ProjectMapperHandler;
import com.atlassian.jira.imports.project.handler.ProjectRoleActorMapperHandler;
import com.atlassian.jira.imports.project.handler.RegisterUserMapperHandler;
import com.atlassian.jira.imports.project.handler.RequiredProjectRolesMapperHandler;
import com.atlassian.jira.imports.project.handler.SimpleEntityMapperHandler;
import com.atlassian.jira.imports.project.handler.UserMapperHandler;
import com.atlassian.jira.imports.project.mapper.CustomFieldMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;
import com.atlassian.jira.imports.project.parser.CustomFieldValueParser;
import com.atlassian.jira.imports.project.parser.CustomFieldValueParserImpl;
import com.atlassian.jira.imports.project.parser.IssueParser;
import com.atlassian.jira.imports.project.taskprogress.TaskProgressProcessor;
import com.atlassian.jira.imports.xml.BackupXmlParser;
import com.atlassian.jira.util.collect.MapBuilder;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.model.MockModelEntity;
import org.xml.sax.SAXException;

import static com.atlassian.jira.imports.project.handler.SimpleEntityMapperHandler.PRIORITY_ENTITY_NAME;
import static com.atlassian.jira.imports.project.handler.SimpleEntityMapperHandler.RESOLUTION_ENTITY_NAME;
import static com.atlassian.jira.imports.project.handler.SimpleEntityMapperHandler.STATUS_ENTITY_NAME;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @since v3.13
 */
public class TestDefaultProjectImportManager2
{
    private static final List<ExternalComponent> NO_COMPONENTS = emptyList();
    private static final List<ExternalCustomFieldConfiguration> NO_CUSTOM_FIELD_CONFIGURATIONS = emptyList();
    private static final List<ExternalVersion> NO_VERSIONS = emptyList();

    private MapBuilder<String, CustomFieldValueParser> entities = MapBuilder.newBuilder();
    @org.mockito.Mock private DelegatorInterface mockDelegator;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        when(mockDelegator.getDelegatorName()).thenReturn("default");
        entities.add(CustomFieldValueParser.CUSTOM_FIELD_VALUE_ENTITY_NAME, new CustomFieldValueParserImpl());
    }

    @Test
    public void testXmlPartitioningHappyPath() throws IOException, SAXException
    {
        final BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), NO_VERSIONS, NO_COMPONENTS,
                NO_CUSTOM_FIELD_CONFIGURATIONS, Collections.<Long>emptyList());
        final String fileName = "/test/path";
        final ProjectImportOptionsImpl projectImportOptions = new ProjectImportOptionsImpl(fileName, fileName);
        final BackupSystemInformation backupSystemInformation =
                new BackupSystemInformationImpl("123", "Prof", Collections.EMPTY_LIST, true, Collections.EMPTY_MAP, 0);

        final IssuePartitonHandler issuePartitonHandler = new IssuePartitonHandler(
                null, null, new MockModelEntity(IssueParser.ISSUE_ENTITY_NAME), null, mockDelegator);
        final IssueRelatedEntitiesPartionHandler issueRelatedEntitiesPartionHandler =
                new IssueRelatedEntitiesPartionHandler(null, null, null, Collections.EMPTY_LIST, null, mockDelegator);
        final IssueRelatedEntitiesPartionHandler customFieldValuesPartitionHandler =
                new IssueRelatedEntitiesPartionHandler(null, null, null, Collections.EMPTY_LIST, null, mockDelegator);
        final IssueRelatedEntitiesPartionHandler fileAttachmentPartitionHandler =
                new IssueRelatedEntitiesPartionHandler(null, null, null, Collections.EMPTY_LIST, null, mockDelegator);

        final MockControl mockChainedSaxHandlerControl = MockClassControl.createControl(ChainedSaxHandler.class);
        final ChainedSaxHandler mockChainedSaxHandler = (ChainedSaxHandler) mockChainedSaxHandlerControl.getMock();
        mockChainedSaxHandler.registerHandler(issuePartitonHandler);
        mockChainedSaxHandler.registerHandler(customFieldValuesPartitionHandler);
        mockChainedSaxHandler.registerHandler(issueRelatedEntitiesPartionHandler);
        mockChainedSaxHandler.registerHandler(fileAttachmentPartitionHandler);

        final ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        final UserMapperHandler userMapperHandler =
                new UserMapperHandler(projectImportOptions, backupProject, projectImportMapper.getUserMapper(), null);
        final GroupMapperHandler groupMapperHandler =
                new GroupMapperHandler(backupProject, projectImportMapper.getGroupMapper());
        final IssueMapperHandler issueMapperHandler = new IssueMapperHandler(backupProject, projectImportMapper);
        final ProjectIssueSecurityLevelMapperHandler securityLevelMapperHandler =
                new ProjectIssueSecurityLevelMapperHandler(backupProject, projectImportMapper.getIssueSecurityLevelMapper());
        final IssueTypeMapperHandler issueTypeHandler = new IssueTypeMapperHandler(projectImportMapper.getIssueTypeMapper());
        final SimpleEntityMapperHandler priorityHandler = new SimpleEntityMapperHandler(PRIORITY_ENTITY_NAME,
                projectImportMapper.getPriorityMapper());
        final SimpleEntityMapperHandler resolutionHandler =
                new SimpleEntityMapperHandler(RESOLUTION_ENTITY_NAME, projectImportMapper.getResolutionMapper());
        final SimpleEntityMapperHandler statusHandler =
                new SimpleEntityMapperHandler(STATUS_ENTITY_NAME, projectImportMapper.getStatusMapper());
        final CustomFieldMapperHandler customFieldHandler =
                new CustomFieldMapperHandler(backupProject, projectImportMapper.getCustomFieldMapper(), entities.toMap());
        final ProjectMapperHandler projectMapperHandler = new ProjectMapperHandler(projectImportMapper.getProjectMapper());
        final CustomFieldOptionsMapperHandler customFieldOptionMapperHandler = new CustomFieldOptionsMapperHandler(
                projectImportMapper.getCustomFieldOptionMapper());
        final SimpleEntityMapperHandler projectRoleRegistrationMapperHandler = new SimpleEntityMapperHandler(
                SimpleEntityMapperHandler.PROJECT_ROLE_ENTITY_NAME, projectImportMapper.getProjectRoleMapper());
        final RequiredProjectRolesMapperHandler requiredProjectRolesMapperHandler =
                new RequiredProjectRolesMapperHandler(backupProject, projectImportMapper.getProjectMapper());
        final IssueVersionMapperHandler issueVersionMapperHandler = new IssueVersionMapperHandler(backupProject,
                projectImportMapper.getVersionMapper());
        final IssueComponentMapperHandler issueComponentMapperHandler =
                new IssueComponentMapperHandler(backupProject, projectImportMapper.getComponentMapper());
        final IssueLinkMapperHandler issueLinkMapperHandler = new IssueLinkMapperHandler(
                backupProject, backupSystemInformation, null, projectImportMapper.getIssueLinkTypeMapper());
        final RegisterUserMapperHandler registerUserMapperHandler =
                new RegisterUserMapperHandler(projectImportMapper.getUserMapper());
        final ProjectRoleActorMapperHandler projectRoleActorMapperHandler =
                new ProjectRoleActorMapperHandler(backupProject, projectImportMapper.getProjectRoleActorMapper());
        mockChainedSaxHandler.registerHandler(userMapperHandler);
        mockChainedSaxHandler.registerHandler(groupMapperHandler);
        mockChainedSaxHandler.registerHandler(issueMapperHandler);
        mockChainedSaxHandler.registerHandler(securityLevelMapperHandler);
        mockChainedSaxHandler.registerHandler(issueTypeHandler);
        mockChainedSaxHandler.registerHandler(priorityHandler);
        mockChainedSaxHandler.registerHandler(resolutionHandler);
        mockChainedSaxHandler.registerHandler(statusHandler);
        mockChainedSaxHandler.registerHandler(customFieldHandler);
        mockChainedSaxHandler.registerHandler(projectMapperHandler);
        mockChainedSaxHandler.registerHandler(customFieldOptionMapperHandler);
        mockChainedSaxHandler.registerHandler(projectRoleRegistrationMapperHandler);
        mockChainedSaxHandler.registerHandler(requiredProjectRolesMapperHandler);
        mockChainedSaxHandler.registerHandler(issueVersionMapperHandler);
        mockChainedSaxHandler.registerHandler(issueComponentMapperHandler);
        mockChainedSaxHandler.registerHandler(issueLinkMapperHandler);
        mockChainedSaxHandler.registerHandler(registerUserMapperHandler);
        mockChainedSaxHandler.registerHandler(projectRoleActorMapperHandler);

        mockChainedSaxHandlerControl.replay();

        final Mock mockBackupXmlParser = new Mock(BackupXmlParser.class);
        mockBackupXmlParser.setStrict(true);
        mockBackupXmlParser.expectVoid("parseBackupXml", P.args(P.eq(fileName), P.IS_ANYTHING));

        final DefaultProjectImportManager defaultProjectImportManager = new DefaultProjectImportManager(
                (BackupXmlParser) mockBackupXmlParser.proxy(), null, null, null, null, null, null, null, null, null, null, null, null, null, null)
        {
            @Override
            String getApplicationEncoding()
            {
                return "UTF-8";
            }

            @Override
            IssuePartitonHandler getIssuePartitioner(final PrintWriter issueFileWriter, final BackupProject backupProject, final String encoding)
            {
                return issuePartitonHandler;
            }

            @Override
            IssueRelatedEntitiesPartionHandler getIssueRelatedEntitesHandler(final PrintWriter issueRelatedEntitiesWriter, final PrintWriter changeItemEntitiesWriter, final BackupProject backupProject, final String encoding)
            {
                return issueRelatedEntitiesPartionHandler;
            }

            @Override
            IssueRelatedEntitiesPartionHandler getCustomFieldValuesHandler(final PrintWriter customFieldValuesWriter, final BackupProject backupProject, final String encoding)
            {
                return customFieldValuesPartitionHandler;
            }

            @Override
            IssueRelatedEntitiesPartionHandler getFileAttachmentHandler(final PrintWriter customFieldValuesWriter, final BackupProject backupProject, final String encoding)
            {
                return fileAttachmentPartitionHandler;
            }

            @Override
            ChainedSaxHandler getChainedHandler(final TaskProgressProcessor taskProgressProcessor)
            {
                return mockChainedSaxHandler;
            }

            @Override
            IssueMapperHandler getIssueMapperHandler(final BackupProject backupProject, final ProjectImportMapper projectImportMapper)
            {
                return issueMapperHandler;
            }

            @Override
            IssueTypeMapperHandler getIssueTypeMapperHandler(final ProjectImportMapper projectImportMapper)
            {
                return issueTypeHandler;
            }

            @Override
            SimpleEntityMapperHandler getPriorityMapperHandler(final ProjectImportMapper projectImportMapper)
            {
                return priorityHandler;
            }

            @Override
            ProjectIssueSecurityLevelMapperHandler getProjectIssueSecurityLevelMapperHandler(final BackupProject backupProject, final ProjectImportMapper projectImportMapper)
            {
                return securityLevelMapperHandler;
            }

            @Override
            SimpleEntityMapperHandler getResolutionMapperHandler(final ProjectImportMapper projectImportMapper)
            {
                return resolutionHandler;
            }

            @Override
            SimpleEntityMapperHandler getStatusMapperHandler(final ProjectImportMapper projectImportMapper)
            {
                return statusHandler;
            }

            @Override
            UserMapperHandler getUserMapperHandler(final ProjectImportOptions projectImportOptions, final BackupProject backupProject, final ProjectImportMapper projectImportMapper)
            {
                return userMapperHandler;
            }

            @Override
            GroupMapperHandler getGroupMapperHandler(final BackupProject backupProject, final ProjectImportMapper projectImportMapper)
            {
                return groupMapperHandler;
            }

            @Override
            CustomFieldMapperHandler getCustomFieldMapperHandler(final BackupProject backupProject, final ProjectImportMapper projectImportMapper)
            {
                return customFieldHandler;
            }

            @Override
            ProjectMapperHandler getProjectMapperHandler(final ProjectImportMapper projectImportMapper)
            {
                return projectMapperHandler;
            }

            @Override
            CustomFieldOptionsMapperHandler getCustomFieldOptionMapperHandler(final ProjectImportMapper projectImportMapper)
            {
                return customFieldOptionMapperHandler;
            }

            @Override
            SimpleEntityMapperHandler getProjectRoleRegistrationHandler(final ProjectImportMapper projectImportMapper)
            {
                return projectRoleRegistrationMapperHandler;
            }

            @Override
            RequiredProjectRolesMapperHandler getRequiredProjectRolesMapperHandler(final BackupProject backupProject, final ProjectImportMapper projectImportMapper)
            {
                return requiredProjectRolesMapperHandler;
            }

            @Override
            IssueVersionMapperHandler getIssueVersionMapperHandler(final BackupProject backupProject, final ProjectImportMapper projectImportMapper)
            {
                return issueVersionMapperHandler;
            }

            @Override
            IssueComponentMapperHandler getIssueComponentMapperHandler(final BackupProject backupProject, final ProjectImportMapper projectImportMapper)
            {
                return issueComponentMapperHandler;
            }

            @Override
            IssueLinkMapperHandler getIssueLinkMapperHandler(final BackupProject backupProject, final BackupSystemInformation backupSystemInformation, final ProjectImportMapper projectImportMapper)
            {
                return issueLinkMapperHandler;
            }

            @Override
            RegisterUserMapperHandler getRegisterUserMapperHandler(final ProjectImportMapper projectImportMapper)
            {
                return registerUserMapperHandler;
            }

            @Override
            void populateCustomFieldMapperOldValues(final BackupProject backupProject, final CustomFieldMapper customFieldMapper)
            {
                // do nothing
            }

            @Override
            void populateVersionMapper(final SimpleProjectImportIdMapper versionMapper, final Map newVersions)
            {
                // do nothing
            }

            @Override
            void populateComponentMapper(final SimpleProjectImportIdMapper componentMapper, final Map newComponents)
            {
                // do nothing
            }

            @Override
            ProjectRoleActorMapperHandler getProjectRoleActorMapperHandler(final BackupProject backupProject, final ProjectImportMapper projectImportMapper)
            {
                return projectRoleActorMapperHandler;
            }
        };
        final ProjectImportData backupParsingResult = defaultProjectImportManager.getProjectImportData(projectImportOptions, backupProject,
                backupSystemInformation, null);

        // Test that the files exist
        assertTrue(new File(backupParsingResult.getPathToCustomFieldValuesXml()).exists());
        assertTrue(new File(backupParsingResult.getPathToIssueRelatedEntitiesXml()).exists());
        assertTrue(new File(backupParsingResult.getPathToIssuesXml()).exists());
        assertTrue(new File(backupParsingResult.getPathToFileAttachmentXml()).exists());

        mockChainedSaxHandlerControl.verify();
    }
}
