package com.atlassian.jira.imports.project;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.action.admin.export.DefaultSaxEntitiesExporter;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.external.ExternalException;
import com.atlassian.jira.external.beans.ExternalComponent;
import com.atlassian.jira.external.beans.ExternalCustomField;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.external.beans.ExternalProjectRoleActor;
import com.atlassian.jira.external.beans.ExternalUser;
import com.atlassian.jira.external.beans.ExternalVersion;
import com.atlassian.jira.imports.project.core.BackupOverview;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.MappingResult;
import com.atlassian.jira.imports.project.core.ProjectImportData;
import com.atlassian.jira.imports.project.core.ProjectImportDataImpl;
import com.atlassian.jira.imports.project.core.ProjectImportOptions;
import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldConfiguration;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomFieldParser;
import com.atlassian.jira.imports.project.handler.AbortImportException;
import com.atlassian.jira.imports.project.handler.AttachmentFileValidatorHandler;
import com.atlassian.jira.imports.project.handler.AttachmentPersisterHandler;
import com.atlassian.jira.imports.project.handler.BackupOverviewHandler;
import com.atlassian.jira.imports.project.handler.ChainedSaxHandler;
import com.atlassian.jira.imports.project.handler.ChangeGroupPersisterHandler;
import com.atlassian.jira.imports.project.handler.ChangeItemPersisterHandler;
import com.atlassian.jira.imports.project.handler.CommentPersisterHandler;
import com.atlassian.jira.imports.project.handler.ComponentPersisterHandler;
import com.atlassian.jira.imports.project.handler.CustomFieldMapperHandler;
import com.atlassian.jira.imports.project.handler.CustomFieldOptionsMapperHandler;
import com.atlassian.jira.imports.project.handler.CustomFieldValuePersisterHandler;
import com.atlassian.jira.imports.project.handler.CustomFieldValueValidatorHandler;
import com.atlassian.jira.imports.project.handler.EntityPropertiesPersisterHandler;
import com.atlassian.jira.imports.project.handler.GroupMapperHandler;
import com.atlassian.jira.imports.project.handler.ImportEntityHandler;
import com.atlassian.jira.imports.project.handler.IssueComponentMapperHandler;
import com.atlassian.jira.imports.project.handler.IssueLinkMapperHandler;
import com.atlassian.jira.imports.project.handler.IssueLinkPersisterHandler;
import com.atlassian.jira.imports.project.handler.IssueMapperHandler;
import com.atlassian.jira.imports.project.handler.IssuePartitonHandler;
import com.atlassian.jira.imports.project.handler.IssuePersisterHandler;
import com.atlassian.jira.imports.project.handler.IssueRelatedEntitiesPartionHandler;
import com.atlassian.jira.imports.project.handler.IssueTypeMapperHandler;
import com.atlassian.jira.imports.project.handler.IssueVersionMapperHandler;
import com.atlassian.jira.imports.project.handler.LabelsPersisterHandler;
import com.atlassian.jira.imports.project.handler.ProjectIssueSecurityLevelMapperHandler;
import com.atlassian.jira.imports.project.handler.ProjectMapperHandler;
import com.atlassian.jira.imports.project.handler.ProjectRoleActorMapperHandler;
import com.atlassian.jira.imports.project.handler.RegisterUserMapperHandler;
import com.atlassian.jira.imports.project.handler.RequiredProjectRolesMapperHandler;
import com.atlassian.jira.imports.project.handler.SimpleEntityMapperHandler;
import com.atlassian.jira.imports.project.handler.UserAssociationPersisterHandler;
import com.atlassian.jira.imports.project.handler.UserMapperHandler;
import com.atlassian.jira.imports.project.handler.VersionPersisterHandler;
import com.atlassian.jira.imports.project.handler.WorklogPersisterHandler;
import com.atlassian.jira.imports.project.mapper.AutomaticDataMapper;
import com.atlassian.jira.imports.project.mapper.CustomFieldMapper;
import com.atlassian.jira.imports.project.mapper.IssueTypeMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;
import com.atlassian.jira.imports.project.mapper.UserMapper;
import com.atlassian.jira.imports.project.parser.AttachmentParser;
import com.atlassian.jira.imports.project.parser.ChangeGroupParser;
import com.atlassian.jira.imports.project.parser.ChangeItemParser;
import com.atlassian.jira.imports.project.parser.CommentParser;
import com.atlassian.jira.imports.project.parser.CustomFieldValueParser;
import com.atlassian.jira.imports.project.parser.CustomFieldValueParserImpl;
import com.atlassian.jira.imports.project.parser.EntityPropertyParser;
import com.atlassian.jira.imports.project.parser.IssueLinkParser;
import com.atlassian.jira.imports.project.parser.IssueParser;
import com.atlassian.jira.imports.project.parser.NodeAssociationParser;
import com.atlassian.jira.imports.project.parser.ProjectRoleActorParser;
import com.atlassian.jira.imports.project.parser.ProjectRoleActorParserImpl;
import com.atlassian.jira.imports.project.parser.UserAssociationParser;
import com.atlassian.jira.imports.project.taskprogress.EntityCountTaskProgressProcessor;
import com.atlassian.jira.imports.project.taskprogress.EntityTypeTaskProgressProcessor;
import com.atlassian.jira.imports.project.taskprogress.TaskProgressInterval;
import com.atlassian.jira.imports.project.taskprogress.TaskProgressProcessor;
import com.atlassian.jira.imports.project.util.ProjectImportTemporaryFiles;
import com.atlassian.jira.imports.project.util.ProjectImportTemporaryFilesImpl;
import com.atlassian.jira.imports.project.validation.ProjectImportValidators;
import com.atlassian.jira.imports.xml.BackupXmlParser;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.attachment.AttachmentStore;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.label.OfBizLabelStore;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.worklog.OfBizWorklogStore;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.CachingProjectRoleAndActorStore;
import com.atlassian.jira.security.roles.ProjectRoleAndActorStore;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.ImportUtils;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.concurrent.BoundedExecutor;
import com.atlassian.jira.util.dbc.Null;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelReader;
import org.ofbiz.core.entity.model.ModelViewEntity;
import org.xml.sax.SAXException;

/**
 * @since v3.13
 */
public class DefaultProjectImportManager implements ProjectImportManager
{
    private static final Logger log = Logger.getLogger(DefaultProjectImportManager.class);

    private final BackupXmlParser backupXmlParser;
    private final DelegatorInterface genericDelegator;
    private final ApplicationProperties applicationProperties;
    private final IssueManager issueManager;
    private final IssueLinkManager issueLinkManager;
    private final CustomFieldManager customFieldManager;
    private final AutomaticDataMapper automaticDataMapper;
    private final ProjectImportValidators projectImportValidators;
    private final ProjectImportPersister projectImportPersister;
    private final UserUtil userUtil;
    private final GroupManager groupManager;
    private final ProjectRoleManager projectRoleManager;
    private final ProjectManager projectManager;
    private final ProjectRoleAndActorStore projectRoleAndActorStore;
    private final AttachmentStore attachmentStore;
    private static final int DEFAULT_ENTITY_COUNT = 100;
    private static final int THREAD_POOL_SIZE = 10;
    private static final int THREAD_POOL_QUEUE_SIZE = 20;

    public DefaultProjectImportManager(final BackupXmlParser backupXmlParser, final DelegatorInterface genericDelegator, final ApplicationProperties applicationProperties,
            final IssueManager issueManager, final IssueLinkManager issueLinkManager, final CustomFieldManager customFieldManager,
            final AutomaticDataMapper automaticDataMapper, final ProjectImportValidators projectImportValidators, final ProjectImportPersister projectImportPersister,
            final UserUtil userUtil, final GroupManager groupManager, final ProjectRoleManager projectRoleManager,
            final ProjectManager projectManager, final ProjectRoleAndActorStore projectRoleAndActorStore,
            final AttachmentStore attachmentStore)
    {
        this.backupXmlParser = backupXmlParser;
        this.genericDelegator = genericDelegator;
        this.applicationProperties = applicationProperties;
        this.issueManager = issueManager;
        this.issueLinkManager = issueLinkManager;
        this.customFieldManager = customFieldManager;
        this.automaticDataMapper = automaticDataMapper;
        this.projectImportValidators = projectImportValidators;
        this.projectImportPersister = projectImportPersister;
        this.userUtil = userUtil;
        this.groupManager = groupManager;
        this.projectRoleManager = projectRoleManager;
        this.projectManager = projectManager;
        this.projectRoleAndActorStore = projectRoleAndActorStore;
        this.attachmentStore = attachmentStore;
    }

    public BackupOverview getBackupOverview(final String pathToBackupXml, final TaskProgressSink taskProgressSink, final I18nHelper i18n) throws IOException, SAXException
    {
        Null.not("pathToBackupXml", pathToBackupXml);

        final ChainedSaxHandler handler = getChainedHandler(new EntityTypeTaskProgressProcessor(getTotalEntitiesCount(), taskProgressSink, i18n));
        final BackupOverviewHandler backupOverviewHandler = new BackupOverviewHandler();
        handler.registerHandler(backupOverviewHandler);
        backupXmlParser.parseBackupXml(pathToBackupXml, handler);
        return backupOverviewHandler.getBackupOverview();
    }

    public ProjectImportData getProjectImportData(final ProjectImportOptions projectImportOptions, final BackupProject backupProject, final BackupSystemInformation backupSystemInformation, final TaskProgressProcessor taskProgressProcessor) throws IOException, SAXException
    {
        Null.not("backupProject", backupProject);
        Null.not("projectImportOptions", projectImportOptions);
        Null.not("pathToBackupXml", projectImportOptions.getPathToBackupXml());
        Null.not("backupSystemInformation", backupSystemInformation);

        final ChainedSaxHandler handler = getChainedHandler(taskProgressProcessor);
        final String encoding = getApplicationEncoding();

        PrintWriter issueFileWriter = null;
        PrintWriter customFieldValuesWriter = null;
        PrintWriter issueRelatedEntitiesWriter = null;
        PrintWriter changeItemEntitiesWriter = null;
        PrintWriter fileAttachmentEntitiesWriter = null;

        // Creates a temporary directory to store our temp XML in, and create the temp files
        final ProjectImportTemporaryFiles projectImportTemporaryFiles = new ProjectImportTemporaryFilesImpl(backupProject.getProject().getKey());

        try
        {
            // First we create some Import handlers for creating the "partitioned" files.
            // Create an issue partition handler and register it with the chained handler
            final File issueXml = projectImportTemporaryFiles.getIssuesXmlFile();
            issueXml.deleteOnExit();
            issueFileWriter = getWriter(issueXml, encoding);
            final IssuePartitonHandler issuePartitonHandler = getIssuePartitioner(issueFileWriter, backupProject, encoding);
            handler.registerHandler(issuePartitonHandler);

            // Create a custom field partition handler and register it with the chained handler
            final File customFieldValuesXml = projectImportTemporaryFiles.getCustomFieldValuesXmlFile();
            customFieldValuesXml.deleteOnExit();
            customFieldValuesWriter = getWriter(customFieldValuesXml, encoding);
            final IssueRelatedEntitiesPartionHandler customFieldPartionHandler = getCustomFieldValuesHandler(customFieldValuesWriter, backupProject,
                encoding);
            handler.registerHandler(customFieldPartionHandler);

            // Create a partition handler for the rest of the related issue entities
            final File issueRelatedEntitiesXml = projectImportTemporaryFiles.getIssueRelatedEntitiesXmlFile();
            issueRelatedEntitiesXml.deleteOnExit();
            issueRelatedEntitiesWriter = getWriter(issueRelatedEntitiesXml, encoding);
            // Create a partition handler for the change item entities
            final File changeItemEntitiesXml = projectImportTemporaryFiles.getChangeItemEntitiesXmlFile();
            changeItemEntitiesXml.deleteOnExit();
            changeItemEntitiesWriter = getWriter(changeItemEntitiesXml, encoding);
            final IssueRelatedEntitiesPartionHandler relatedEntitiesPartionHandler = getIssueRelatedEntitesHandler(issueRelatedEntitiesWriter,
                changeItemEntitiesWriter, backupProject, encoding);
            handler.registerHandler(relatedEntitiesPartionHandler);

            // If we are importing attachments create an attachment partition handler and register it with the chained handler
            IssueRelatedEntitiesPartionHandler fileAttachmentHandler = null;
            if (StringUtils.isNotEmpty(projectImportOptions.getAttachmentPath()))
            {
                // Create a partition handler for the file attachment related issue entities
                final File fileAttachmentEntitiesXml = projectImportTemporaryFiles.getFileAttachmentEntitiesXmlFile();
                fileAttachmentEntitiesXml.deleteOnExit();
                fileAttachmentEntitiesWriter = getWriter(fileAttachmentEntitiesXml, encoding);
                fileAttachmentHandler = getFileAttachmentHandler(fileAttachmentEntitiesWriter, backupProject, encoding);
                handler.registerHandler(fileAttachmentHandler);
            }

            // Now we create Import Handlers for populating mappers.
            // This is the "Daddy" mapper that holds all the specific mappers.
            final ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(userUtil, groupManager);

            // We already know all the old custom field values that exist for this backup project. We need to populate these in the mapper
            populateCustomFieldMapperOldValues(backupProject, projectImportMapper.getCustomFieldMapper());

            // We already know all the old version values that exist for this backup project. We need to populate these in the mapper
            populateVersionMapperOldValues(backupProject, projectImportMapper.getVersionMapper());

            // We already know all the old component values that exist for this backup project. We need to populate these in the mapper
            populateComponentMapperOldValues(backupProject, projectImportMapper.getComponentMapper());

            // Add all the mapper handlers to the chained handler
            handler.registerHandler(getUserMapperHandler(projectImportOptions, backupProject, projectImportMapper));
            handler.registerHandler(getGroupMapperHandler(backupProject, projectImportMapper));
            handler.registerHandler(getIssueMapperHandler(backupProject, projectImportMapper));
            handler.registerHandler(getProjectIssueSecurityLevelMapperHandler(backupProject, projectImportMapper));
            handler.registerHandler(getIssueTypeMapperHandler(projectImportMapper));
            handler.registerHandler(getPriorityMapperHandler(projectImportMapper));
            handler.registerHandler(getResolutionMapperHandler(projectImportMapper));
            handler.registerHandler(getStatusMapperHandler(projectImportMapper));
            handler.registerHandler(getCustomFieldMapperHandler(backupProject, projectImportMapper));
            handler.registerHandler(getProjectMapperHandler(projectImportMapper));
            handler.registerHandler(getCustomFieldOptionMapperHandler(projectImportMapper));
            handler.registerHandler(getProjectRoleRegistrationHandler(projectImportMapper));
            handler.registerHandler(getRequiredProjectRolesMapperHandler(backupProject, projectImportMapper));
            handler.registerHandler(getIssueVersionMapperHandler(backupProject, projectImportMapper));
            handler.registerHandler(getIssueComponentMapperHandler(backupProject, projectImportMapper));
            handler.registerHandler(getIssueLinkMapperHandler(backupProject, backupSystemInformation, projectImportMapper));
            handler.registerHandler(getRegisterUserMapperHandler(projectImportMapper));
            handler.registerHandler(getProjectRoleActorMapperHandler(backupProject, projectImportMapper));

            // Kick-off the second pass parsing now that we have registered all our handlers.
            backupXmlParser.parseBackupXml(projectImportOptions.getPathToBackupXml(), handler);

            //once we've done the whole parse, the customfield mapper needs to map custom fields to issue types
            projectImportMapper.getCustomFieldMapper().registerIssueTypesInUse();
            
            int fileAttachmentCount = 0;
            if (fileAttachmentHandler != null)
            {
                fileAttachmentCount = fileAttachmentHandler.getEntityCount();
            }
            return new ProjectImportDataImpl(projectImportMapper, projectImportTemporaryFiles, issuePartitonHandler.getEntityCount(),
                customFieldPartionHandler.getEntityCount(), relatedEntitiesPartionHandler.getEntityCount(), fileAttachmentCount,
                relatedEntitiesPartionHandler.getSecondDegreeEntityCount());
        }
        finally
        {
            if (issueFileWriter != null)
            {
                issueFileWriter.close();
            }
            if (customFieldValuesWriter != null)
            {
                customFieldValuesWriter.close();
            }
            if (issueRelatedEntitiesWriter != null)
            {
                issueRelatedEntitiesWriter.close();
            }
            if (changeItemEntitiesWriter != null)
            {
                changeItemEntitiesWriter.close();
            }
            if (fileAttachmentEntitiesWriter != null)
            {
                fileAttachmentEntitiesWriter.close();
            }
        }
    }

    public void createMissingUsers(final UserMapper userMapper, final ProjectImportResults projectImportResults, final TaskProgressInterval taskProgressInterval) throws AbortImportException
    {
        final I18nHelper i18n = projectImportResults.getI18n();
        final Collection<ExternalUser> users = userMapper.getUsersToAutoCreate();
        final EntityCountTaskProgressProcessor taskProgressProcessor = new EntityCountTaskProgressProcessor(taskProgressInterval,
            i18n.getText("admin.message.project.import.manager.do.import.importing.users"), users.size(), i18n);
        final AtomicInteger count = new AtomicInteger(0);

        final BoundedExecutor executor = createExecutor("ProjectImport: CreateUsers");

        try
        {
            // Create the missing mandatory users
            for (final ExternalUser user : users)
            {
                // First thing to do is to make sure we do not have too many errors
                if (projectImportResults.abortImport())
                {
                    break;
                }

                // We want this multi threaded
                executor.execute(new Runnable()
                {
                    public void run()
                    {
                        boolean userCreated = projectImportPersister.createUser(userMapper, user);
                        if (!userCreated)
                        {
                            projectImportResults.addError(i18n.getText("admin.errors.project.import.could.not.create.user", user.getName()));
                        }
                        else
                        {
                            projectImportResults.incrementUsersCreatedCount();
                        }
                        final int currentCount = count.incrementAndGet();
                        taskProgressProcessor.processTaskProgress(i18n.getText("admin.common.words.users"), currentCount);
                    }
                });
            }
        }
        finally
        {
            if (projectImportResults.abortImport())
            {
                executor.shutdownAndIgnoreQueue();
                throw new AbortImportException();
            }
            else
            {
                executor.shutdownAndWait();
            }
        }
    }

    public void importProjectRoleMembers(final Project project, final ProjectImportMapper projectImportMapper, final ProjectImportResults projectImportResults, final TaskProgressInterval taskProgressInterval) throws AbortImportException
    {
        final I18nHelper i18n = projectImportResults.getI18n();

        // Always nuke the existing role membership
        projectRoleManager.removeAllRoleActorsByProject(project);


        final ProjectRoleActorParser projectRoleActorParser = getProjectRoleActorParser();
        final Collection projectRoleActors = projectImportMapper.getProjectRoleActorMapper().getAllProjectRoleActors();

        final EntityCountTaskProgressProcessor taskProgressProcessor = new EntityCountTaskProgressProcessor(taskProgressInterval,
            i18n.getText("admin.common.words.projectrole"), projectRoleActors.size(), i18n);
        final AtomicInteger count = new AtomicInteger(0);

        final BoundedExecutor executor = createExecutor("ProjectImport: CreateRoleMemebers");

        try
        {
            // Loop through all the role actors
            for (final Object projectRoleActor1 : projectRoleActors)
            {
                // Check to see that we have not had too many errors
                if (projectImportResults.abortImport())
                {
                    break;
                }

                final ExternalProjectRoleActor projectRoleActor = (ExternalProjectRoleActor) projectRoleActor1;

                executor.execute(new Runnable()
                {
                    public void run()
                    {

                        // Find if this actor is a user or group
                        if (projectRoleActor.isUserActor())
                        {
                            createUserRoleActor(projectImportMapper, projectImportResults, projectRoleActorParser, projectRoleActor);
                        }
                        else if (projectRoleActor.isGroupActor())
                        {
                            createGroupRoleActor(projectImportMapper, projectImportResults, projectRoleActorParser, projectRoleActor);
                        }
                        else
                        {
                            // Some weirdo role type that we cannot support.
                            final String projectRoleName = projectImportMapper.getProjectRoleMapper().getDisplayName(projectRoleActor.getRoleId());
                            log.warn("Project role '" + projectRoleName + "' contains a project role actor '" + projectRoleActor.getRoleActor() + "' with unknown role type '" + projectRoleActor.getRoleType() + "', we cannot add this actor to the project role.");
                        }
                        final int currentCount = count.incrementAndGet();
                        taskProgressProcessor.processTaskProgress(i18n.getText("admin.common.words.projectrole"), currentCount);
                    }
                });
            }
        }
        finally
        {
            // wait() for all the executors to finish here and then clear the cache
            executor.shutdownAndWait();
            if (projectRoleAndActorStore instanceof CachingProjectRoleAndActorStore)
            {
                ((CachingProjectRoleAndActorStore)projectRoleAndActorStore).clearCaches();
            }
            if (projectImportResults.abortImport())
            {
                throw new AbortImportException();
            }
        }
    }

    public void importProject(final ProjectImportOptions projectImportOptions, final ProjectImportMapper projectImportMapper, final BackupProject backupProject, final ProjectImportResults projectImportResults, final TaskProgressInterval taskProgressInterval) throws AbortImportException
    {
        // create/update the project
        final SimpleProjectImportIdMapper projectMapper = projectImportMapper.getProjectMapper();
        final ExternalProject externalProject = backupProject.getProject();
        final ExternalProject transformedProject = transformProject(externalProject, projectImportMapper);
        Project project;
        if (projectMapper.getMappedId(externalProject.getId()) == null)
        {
            try
            {
                project = projectImportPersister.createProject(transformedProject);
            }
            catch (final ExternalException e)
            {
                log.error("An error occurred while trying to create the project '" + externalProject.getKey() + "' for project import. The import will be aborted.");
                throw new AbortImportException();
            }
            // Map the new ID
            projectMapper.mapValue(externalProject.getId(), project.getId().toString());
        }
        else if (projectImportOptions.overwriteProjectDetails())
        {
            // Project already exists and user chose to "Overwrite the Project Details".
            project = projectImportPersister.updateProjectDetails(transformedProject);
        }
        else
        {
            // Just lookup the project so that we can keep a reference to it
            project = projectManager.getProjectObjByKey(externalProject.getKey());
        }

        // Record the project in the results
        projectImportResults.setImportedProject(project);

        // Set the project counter to the max so that we won't conflict with any issues we try to create
        try
        {
            projectImportPersister.updateProjectIssueCounter(backupProject, Long.parseLong(externalProject.getCounter()));
        }
        catch (final NumberFormatException e)
        {
            log.warn("The backup project '" + externalProject.getKey() + "' has an invalid issue counter '" + externalProject.getCounter() + "'. The import will continue, but issue creation may produce warnings in the log.");
        }

        // Next create versions
        final Map<String, Version> newVersions;
        try
        {
            newVersions = projectImportPersister.createVersions(backupProject);
        }
        catch (final DataAccessException e)
        {
            throw new AbortImportException();
        }
        // Map the new versions into the version mapper
        populateVersionMapper(projectImportMapper.getVersionMapper(), newVersions);

        // Next create components
        final Map<String, ProjectComponent> newComponents;
        try
        {
            newComponents = projectImportPersister.createComponents(backupProject, projectImportMapper);
        }
        catch (final DataAccessException e)
        {
            throw new AbortImportException();
        }
        // Map the new components into the component mapper
        populateComponentMapper(projectImportMapper.getComponentMapper(), newComponents);

        // If the user has specified to overwrite project details (NOTE: we always set this flag to true for projects we create)
        if (projectImportOptions.overwriteProjectDetails())
        {
            // Update project role membership
            importProjectRoleMembers(project, projectImportMapper, projectImportResults, taskProgressInterval);
        }
    }

    private ExternalProject transformProject(ExternalProject externalProject, ProjectImportMapper projectImportMapper)
    {
        ExternalProject transformedProject = new ExternalProject();

        transformedProject.setId(externalProject.getId());
        transformedProject.setKey(externalProject.getKey());
        transformedProject.setOriginalKey(externalProject.getOriginalKey());
        transformedProject.setName(externalProject.getName());
        transformedProject.setAssigneeType(externalProject.getAssigneeType());
        transformedProject.setCounter(externalProject.getCounter());
        transformedProject.setDescription(externalProject.getDescription());
        transformedProject.setEmailSender(externalProject.getEmailSender());
        // Map Project Lead user key
        transformedProject.setLead(projectImportMapper.getUserMapper().getMappedUserKey(externalProject.getLead()));
        transformedProject.setProjectCategoryName(externalProject.getProjectCategoryName());
        transformedProject.setProjectGV(externalProject.getProjectGV());
        transformedProject.setUrl(externalProject.getUrl());

        return transformedProject;
    }

    public void doImport(final ProjectImportOptions projectImportOptions, final ProjectImportData projectImportData, final BackupProject backupProject, final BackupSystemInformation backupSystemInformation, final ProjectImportResults projectImportResults, final TaskProgressInterval taskProgressInterval, final I18nHelper i18n, final User importAuthor) throws IOException, SAXException, IndexException
    {
        final ProjectImportMapper projectImportMapper = projectImportData.getProjectImportMapper();

        try
        {
            subvertSecurityIndexingNotifications();

            final TaskProgressInterval issuesSubInterval = getSubInterval(taskProgressInterval, 0, 20);
            final long largestIssueKeyNumber = importIssues(projectImportData, projectImportResults, i18n, importAuthor, projectImportMapper,
                issuesSubInterval);

            final TaskProgressInterval issuesRelatedSubInterval = getSubInterval(taskProgressInterval, 20, 35);
            importIssueRelatedData(projectImportData, backupSystemInformation, projectImportResults, i18n, importAuthor, projectImportMapper,
                issuesRelatedSubInterval);

            final TaskProgressInterval changeItemInterval = getSubInterval(taskProgressInterval, 35, 40);
            importIssueSecondDegreeEntities(projectImportData, projectImportResults, i18n, projectImportMapper, changeItemInterval);

            final TaskProgressInterval attachmentSubInterval = getSubInterval(taskProgressInterval, 40, 60);
            importAttachments(projectImportOptions, projectImportData, backupProject, backupSystemInformation, projectImportResults, i18n,
                attachmentSubInterval);

            final TaskProgressInterval custFieldValueSubInterval = getSubInterval(taskProgressInterval, 60, 80);
            importCustomFieldValues(projectImportData, backupProject, backupSystemInformation, projectImportResults, i18n, projectImportMapper,
                custFieldValueSubInterval);

            // Reset the pcounter to the max as determined by the issues that we have stored
            projectImportPersister.updateProjectIssueCounter(backupProject, largestIssueKeyNumber);

        }
        finally
        {
            restoreSecurityIndexingNotifications();
            // NOTE: There is no need to clear the component or version caches since we are using the API to create those entities
            // Clear the Issue Link Manager's cache of links.
            issueLinkManager.clearCache();
            // Lastly lets re-index
            log.info("Re-indexing the Project.");
            final TaskProgressInterval reIndexSubInterval = getSubInterval(taskProgressInterval, 80, 100);
            projectImportPersister.reIndexProject(projectImportMapper, reIndexSubInterval, i18n);
            log.info("Finished re-indexing the Project.");
        }
    }

    public void validateCustomFieldValues(final ProjectImportData projectImportData, final MappingResult mappingResult, final BackupProject backupProject, final TaskProgressProcessor taskProgressProcessor, final I18nHelper i18n) throws IOException, SAXException
    {
        Null.not("backupProject", backupProject);
        Null.not("mappingResult", mappingResult);
        Null.not("projectImportData", projectImportData);
        Null.not("projectImportMapper", projectImportData.getProjectImportMapper());
        Null.not("pathToCustomFieldValuesXml", projectImportData.getPathToCustomFieldValuesXml());

        final ChainedSaxHandler handler = getChainedHandler(taskProgressProcessor);

        final CustomFieldValueValidatorHandler valueValidatorHandler = getCustomFieldValueValidatorHandler(backupProject,
            projectImportData.getProjectImportMapper());
        handler.registerHandler(valueValidatorHandler);

        backupXmlParser.parseBackupXml(projectImportData.getPathToCustomFieldValuesXml(), handler);

        final Map<String, MessageSet> customFieldValueMessageSets = valueValidatorHandler.getValidationResults();

        // Validate CustomField Options
        projectImportValidators.getCustomFieldOptionMapperValidator().validateMappings(i18n, backupProject,
            projectImportData.getProjectImportMapper().getCustomFieldOptionMapper(),
            projectImportData.getProjectImportMapper().getCustomFieldMapper(), customFieldValueMessageSets);

        mappingResult.setCustomFieldValueMessageSets(customFieldValueMessageSets);
    }

    public void validateFileAttachments(final ProjectImportOptions projectImportOptions, final ProjectImportData projectImportData, final MappingResult mappingResult, final BackupProject backupProject, final BackupSystemInformation backupSystemInformation, final TaskProgressProcessor taskProgressProcessor, final I18nHelper i18n) throws IOException, SAXException
    {
        Null.not("projectImportOptions", projectImportOptions);

        // We only need to do this validation if the user has prompted us to restore attachments
        if (StringUtils.isNotEmpty(projectImportOptions.getAttachmentPath()))
        {
            Null.not("backupProject", backupProject);
            Null.not("backupSystemInformation", backupSystemInformation);
            Null.not("mappingResult", mappingResult);
            Null.not("projectImportData", projectImportData);
            Null.not("pathToFileAttachmentXml", projectImportData.getPathToFileAttachmentXml());

            final ChainedSaxHandler handler = getChainedHandler(taskProgressProcessor);

            final AttachmentFileValidatorHandler attachmentFileValidatorHandler = getAttachmentFileValidatorHandler(backupProject,
                projectImportOptions, backupSystemInformation, i18n);
            handler.registerHandler(attachmentFileValidatorHandler);

            backupXmlParser.parseBackupXml(projectImportData.getPathToFileAttachmentXml(), handler);
            projectImportData.setValidAttachmentsCount(attachmentFileValidatorHandler.getValidAttachmentCount());

            final MessageSet fileAttachmentsMessageSet = attachmentFileValidatorHandler.getValidationResults();

            mappingResult.setFileAttachmentMessageSet(fileAttachmentsMessageSet);
        }
        else
        {
            mappingResult.setFileAttachmentMessageSet(new MessageSetImpl());
        }
    }

    public void autoMapAndValidateIssueTypes(final ProjectImportData projectImportData, final MappingResult mappingResult, final BackupProject backupProject, final I18nHelper i18nBean)
    {
        final IssueTypeMapper issueTypeMapper = projectImportData.getProjectImportMapper().getIssueTypeMapper();
        automaticDataMapper.mapIssueTypes(backupProject, issueTypeMapper);
        final MessageSet messageSet = projectImportValidators.getIssueTypeMapperValidator().validateMappings(i18nBean, backupProject, issueTypeMapper);
        mappingResult.setIssueTypeMessageSet(messageSet);
    }

    public void autoMapAndValidateCustomFields(final ProjectImportData projectImportData, final MappingResult mappingResult, final BackupProject backupProject, final I18nHelper i18nBean)
    {
        final CustomFieldMapper customFieldMapper = projectImportData.getProjectImportMapper().getCustomFieldMapper();
        final IssueTypeMapper issueTypeMapper = projectImportData.getProjectImportMapper().getIssueTypeMapper();
        automaticDataMapper.mapCustomFields(backupProject, customFieldMapper, issueTypeMapper);
        final MessageSet messageSet = projectImportValidators.getCustomFieldMapperValidator().validateMappings(i18nBean, backupProject,
            issueTypeMapper, customFieldMapper);
        mappingResult.setCustomFieldMessageSet(messageSet);
    }

    public void autoMapCustomFieldOptions(final ProjectImportData projectImportData, final BackupProject backupProject)
    {
        final ProjectImportMapper projectImportMapper = projectImportData.getProjectImportMapper();
        automaticDataMapper.mapCustomFieldOptions(backupProject, projectImportMapper.getCustomFieldOptionMapper(),
            projectImportMapper.getCustomFieldMapper(), projectImportMapper.getIssueTypeMapper());
    }

    public void autoMapProjectRoles(final ProjectImportData projectImportData)
    {
        final ProjectImportMapper projectImportMapper = projectImportData.getProjectImportMapper();
        automaticDataMapper.mapProjectRoles(projectImportMapper.getProjectRoleMapper());
    }

    public void autoMapSystemFields(final ProjectImportData projectImportData, final BackupProject backupProject)
    {
        final ProjectImportMapper projectImportMapper = projectImportData.getProjectImportMapper();
        automaticDataMapper.mapPriorities(projectImportMapper.getPriorityMapper());
        automaticDataMapper.mapProjects(projectImportMapper.getProjectMapper());
        automaticDataMapper.mapResolutions(projectImportMapper.getResolutionMapper());
        automaticDataMapper.mapStatuses(backupProject, projectImportMapper.getStatusMapper(), projectImportMapper.getIssueTypeMapper());
        automaticDataMapper.mapIssueLinkTypes(projectImportMapper.getIssueLinkTypeMapper());
        automaticDataMapper.mapIssueSecurityLevels(backupProject.getProject().getKey(), projectImportMapper.getIssueSecurityLevelMapper());
    }

    public void validateSystemFields(final ProjectImportData projectImportData, final MappingResult mappingResult, final ProjectImportOptions projectImportOptions, final BackupProject backupProject, final TaskProgressInterval taskProgressInterval, final I18nHelper i18nBean)
    {
        // Create a simple process
        final EntityCountTaskProgressProcessor taskProgressProcessor = new EntityCountTaskProgressProcessor(taskProgressInterval,
            i18nBean.getText("admin.message.project.import.manager.do.mapping.validate.system.fields"), 9, i18nBean);

        final ProjectImportMapper projectImportMapper = projectImportData.getProjectImportMapper();
        MessageSet messageSet;
        // Validate Priority
        updateTaskProgress(taskProgressProcessor, i18nBean.getText("issue.field.priority"), 1);
        log.debug("Validating priorities.");
        messageSet = projectImportValidators.getPriorityMapperValidator().validateMappings(i18nBean, projectImportMapper.getPriorityMapper());
        mappingResult.setPriorityMessageSet(messageSet);
        // Validate Resolution
        updateTaskProgress(taskProgressProcessor, i18nBean.getText("issue.field.resolution"), 2);
        log.debug("Validating resolutions.");
        messageSet = projectImportValidators.getResolutionMapperValidator().validateMappings(i18nBean, projectImportMapper.getResolutionMapper());
        mappingResult.setResolutionMessageSet(messageSet);
        // Validate Status
        updateTaskProgress(taskProgressProcessor, i18nBean.getText("issue.field.status"), 3);
        log.debug("Validating statuses.");
        messageSet = projectImportValidators.getStatusMapperValidator().validateMappings(i18nBean, backupProject,
            projectImportMapper.getIssueTypeMapper(), projectImportMapper.getStatusMapper());
        mappingResult.setStatusMessageSet(messageSet);
        // Validate ProjectRoles
        updateTaskProgress(taskProgressProcessor, i18nBean.getText("admin.common.words.projectrole"), 4);
        log.debug("Validating project roles.");
        messageSet = projectImportValidators.getProjectRoleMapperValidator().validateMappings(i18nBean, projectImportMapper.getProjectRoleMapper());
        mappingResult.setProjectRoleMessageSet(messageSet);
        // Validate ProjectRoleActors
        updateTaskProgress(taskProgressProcessor, i18nBean.getText("admin.common.words.projectrole.membership"), 5);
        log.debug("Validating project role actors.");
        messageSet = projectImportValidators.getProjectRoleActorMapperValidator().validateProjectRoleActors(i18nBean, projectImportMapper,
            projectImportOptions);
        mappingResult.setProjectRoleActorMessageSet(messageSet);
        // Validate Users
        updateTaskProgress(taskProgressProcessor, i18nBean.getText("admin.common.words.users"), 6);
        log.debug("Validating users.");
        messageSet = projectImportValidators.getUserMapperValidator().validateMappings(i18nBean, projectImportMapper.getUserMapper());
        mappingResult.setUserMessageSet(messageSet);
        // Validate Groups
        updateTaskProgress(taskProgressProcessor, i18nBean.getText("admin.common.words.group"), 7);
        log.debug("Validating groups.");
        messageSet = projectImportValidators.getGroupMapperValidator().validateMappings(i18nBean, projectImportMapper.getGroupMapper());
        mappingResult.setGroupMessageSet(messageSet);
        // Validate IssueLinkTypes
        updateTaskProgress(taskProgressProcessor, i18nBean.getText("common.concepts.issuelinktype"), 8);
        log.debug("Validating issue link types.");
        messageSet = projectImportValidators.getIssueLinkTypeMapperValidator().validateMappings(i18nBean, backupProject,
            projectImportMapper.getIssueLinkTypeMapper());
        mappingResult.setIssueLinkTypeMessageSet(messageSet);
        // Validate IssueSecurityLevels
        updateTaskProgress(taskProgressProcessor, i18nBean.getText("admin.common.words.issue.security.level"), 9);
        log.debug("Validating issue security levels.");
        messageSet = projectImportValidators.getIssueSecurityLevelValidator().validateMappings(projectImportMapper.getIssueSecurityLevelMapper(),
            backupProject, i18nBean);
        mappingResult.setIssueSecurityLevelMessageSet(messageSet);
    }

    void importCustomFieldValues(final ProjectImportData projectImportData, final BackupProject backupProject, final BackupSystemInformation backupSystemInformation, final ProjectImportResults projectImportResults, final I18nHelper i18n, final ProjectImportMapper projectImportMapper, final TaskProgressInterval custFieldValueSubInterval) throws IOException, SAXException
    {
        final BoundedExecutor customFieldValuesExecutor = createExecutor("ProjectImport: CreateCustomFieldValues");
        // Now that everything else has been saved, create custom field values
        log.info("Creating custom field values.");
        final ChainedSaxHandler customFieldValuesHandler = getChainedHandler(new EntityCountTaskProgressProcessor(custFieldValueSubInterval,
            i18n.getText("admin.message.project.import.manager.do.import.importing.custom.field.values"),
            projectImportData.getCustomFieldValuesEntityCount(), i18n));
        final String mappedProjectId = projectImportMapper.getProjectMapper().getMappedId(backupProject.getProject().getId());
        customFieldValuesHandler.registerHandler(getCustomFieldValuePersisterHandler(projectImportMapper, mappedProjectId, projectImportResults,
            backupSystemInformation, customFieldValuesExecutor));
        try
        {
            // Kick-off the issue related persistence parsing now.
            backupXmlParser.parseBackupXml(projectImportData.getPathToCustomFieldValuesXml(), customFieldValuesHandler);
            log.info("Finished creating custom field values.");
        }
        finally
        {
            if (projectImportResults.abortImport())
            {
                customFieldValuesExecutor.shutdownAndIgnoreQueue();
            }
            else
            {
                customFieldValuesExecutor.shutdownAndWait();
            }
        }
    }

    void importAttachments(final ProjectImportOptions projectImportOptions, final ProjectImportData projectImportData, final BackupProject backupProject, final BackupSystemInformation backupSystemInformation, final ProjectImportResults projectImportResults, final I18nHelper i18n, final TaskProgressInterval attachmentSubInterval) throws IOException, SAXException
    {
        // Create attachments only if the user has provided an attachment directory
        final boolean isImportingAttachments = StringUtils.isNotEmpty(projectImportOptions.getAttachmentPath()) && (projectImportData.getPathToFileAttachmentXml() != null);
        if (isImportingAttachments)
        {
            final BoundedExecutor attachmentExecutor = createExecutor("ProjectImport: CreateAttachments");

            log.info("Creating the attachments.");
            final ChainedSaxHandler attachmentPersistenceHandler = getChainedHandler(new EntityCountTaskProgressProcessor(attachmentSubInterval,
                i18n.getText("admin.message.project.import.manager.do.import.importing.attachments"),
                projectImportData.getFileAttachmentEntityCount(), i18n));
            final AttachmentPersisterHandler attachmentPersisterHandler = new AttachmentPersisterHandler(projectImportPersister,
                projectImportOptions, projectImportData.getProjectImportMapper(), backupProject, backupSystemInformation, projectImportResults,
                attachmentExecutor, attachmentStore);
            attachmentPersistenceHandler.registerHandler(attachmentPersisterHandler);
            try
            {
                backupXmlParser.parseBackupXml(projectImportData.getPathToFileAttachmentXml(), attachmentPersistenceHandler);
                log.info("Finished creating the attachments.");
            }
            finally
            {
                if (projectImportResults.abortImport())
                {
                    attachmentExecutor.shutdownAndIgnoreQueue();
                }
                else
                {
                    attachmentExecutor.shutdownAndWait();
                }
            }
        }
    }

    void importIssueRelatedData(final ProjectImportData projectImportData, final BackupSystemInformation backupSystemInformation, final ProjectImportResults projectImportResults, final I18nHelper i18n, final User importAuthor, final ProjectImportMapper projectImportMapper, final TaskProgressInterval issuesRelatedSubInterval) throws IOException, SAXException
    {
        final BoundedExecutor issueRelatedExecutor = createExecutor("ProjectImport: CreateIssueRelatedData");
        // Create issue related entities
        log.info("Creating the issue-related data.");
        final ChainedSaxHandler issueRelatedEntitiesHandler = getChainedHandler(new EntityCountTaskProgressProcessor(issuesRelatedSubInterval,
            i18n.getText("admin.message.project.import.manager.do.import.importing.issue.related"), projectImportData.getIssueRelatedEntityCount(),
            i18n));
        issueRelatedEntitiesHandler.registerHandler(getCommentPersisterHandler(projectImportMapper, projectImportResults, backupSystemInformation,
                issueRelatedExecutor));
        issueRelatedEntitiesHandler.registerHandler(getWorklogPersisterHandler(projectImportMapper, projectImportResults, backupSystemInformation,
                issueRelatedExecutor));
        issueRelatedEntitiesHandler.registerHandler(getVersionPersisterHandler(projectImportMapper, projectImportResults, backupSystemInformation,
                issueRelatedExecutor));
        issueRelatedEntitiesHandler.registerHandler(getComponentPersisterHandler(projectImportMapper, projectImportResults, backupSystemInformation,
                issueRelatedExecutor));
        issueRelatedEntitiesHandler.registerHandler(getIssueLinkPersisterHandler(projectImportMapper, projectImportResults, backupSystemInformation,
                issueRelatedExecutor, importAuthor));
        issueRelatedEntitiesHandler.registerHandler(getUserAssociationPersisterHandler(projectImportMapper, projectImportResults,
                backupSystemInformation, issueRelatedExecutor));
        issueRelatedEntitiesHandler.registerHandler(getChangeGroupPersisterHandler(projectImportMapper, projectImportResults,
                backupSystemInformation, issueRelatedExecutor));
        issueRelatedEntitiesHandler.registerHandler(getChangeItemPersisterHandler(projectImportMapper, projectImportResults, issueRelatedExecutor));
        issueRelatedEntitiesHandler.registerHandler(getLabelPersisterHandler(projectImportMapper, projectImportResults, backupSystemInformation, issueRelatedExecutor));
        issueRelatedEntitiesHandler.registerHandler(getIssuePropertiesPersisterHandler(projectImportMapper, projectImportResults, issueRelatedExecutor));
        try
        {
            // Kick-off the issue related persistence parsing now.
            backupXmlParser.parseBackupXml(projectImportData.getPathToIssueRelatedEntitiesXml(), issueRelatedEntitiesHandler);
            log.info("Finished creating the issue-related data.");
        }
        finally
        {
            if (projectImportResults.abortImport())
            {
                issueRelatedExecutor.shutdownAndIgnoreQueue();
            }
            else
            {
                issueRelatedExecutor.shutdownAndWait();
            }
        }
    }

    // this method imports issue-related entities, which are not directly linked to issue, such as comment properties (linked to comment)
    // and change items + change history properties (linked to changegroup).
    void importIssueSecondDegreeEntities(final ProjectImportData projectImportData, final ProjectImportResults projectImportResults,
            final I18nHelper i18n, final ProjectImportMapper projectImportMapper, final TaskProgressInterval changeItemSubInterval) throws IOException, SAXException
    {
        final BoundedExecutor executor = createExecutor("ProjectImport: IssueSecondDegreeEntities");
        // Create issue related entities
        log.info("Creating the change item data.");
        final ChainedSaxHandler changeItemEntityHandler = getChainedHandler(new EntityCountTaskProgressProcessor(changeItemSubInterval,
            i18n.getText("admin.message.project.import.manager.do.import.importing.change.items"), projectImportData.getChangeItemEntityCount(), i18n));
        changeItemEntityHandler.registerHandler(getChangeItemPersisterHandler(projectImportMapper, projectImportResults, executor));
        changeItemEntityHandler.registerHandler(getCommentPropertiesPersisterHandler(projectImportMapper, projectImportResults, executor));
        changeItemEntityHandler.registerHandler(getChangeHistoryPropertiesPersisterHandler(projectImportMapper, projectImportResults, executor));
        try
        {
            // Kick-off the issue related persistence parsing now.
            backupXmlParser.parseBackupXml(projectImportData.getPathToChangeItemXml(), changeItemEntityHandler);
            log.info("Finished creating the change item data.");
        }
        finally
        {
            if (projectImportResults.abortImport())
            {
                executor.shutdownAndIgnoreQueue();
            }
            else
            {
                executor.shutdownAndWait();
            }
        }
    }

    long importIssues(final ProjectImportData projectImportData, final ProjectImportResults projectImportResults, final I18nHelper i18n, final User importAuthor, final ProjectImportMapper projectImportMapper, final TaskProgressInterval issuesSubInterval) throws IOException, SAXException
    {
        // Create a thread pool to let the handlers do persistence with
        final BoundedExecutor issueExecutor = createExecutor("ProjectImport: CreateIssues");
        // Create issues
        log.info("Creating the issues.");
        final EntityCountTaskProgressProcessor issuesTaskProgressProcessor = new EntityCountTaskProgressProcessor(issuesSubInterval,
            i18n.getText("admin.message.project.import.manager.do.import.importing.issues"), projectImportData.getIssueEntityCount(), i18n);
        final ChainedSaxHandler issuePersistenceHandler = getChainedHandler(issuesTaskProgressProcessor);
        final IssuePersisterHandler issuePersisterHandler = getIssuePersisterHandler(importAuthor, projectImportMapper, projectImportResults,
            issueExecutor);
        issuePersistenceHandler.registerHandler(issuePersisterHandler);
        try
        {
            // Kick-off the issue persistence parsing now.
            backupXmlParser.parseBackupXml(projectImportData.getPathToIssuesXml(), issuePersistenceHandler);
            log.info("Finished creating the issues.");
        }
        finally
        {
            if (projectImportResults.abortImport())
            {
                issueExecutor.shutdownAndIgnoreQueue();
            }
            else
            {
                issueExecutor.shutdownAndWait();
            }
        }
        return issuePersisterHandler.getLargestIssueKeyNumber();
    }

    void createGroupRoleActor(final ProjectImportMapper projectImportMapper, final ProjectImportResults projectImportResults, final ProjectRoleActorParser projectRoleActorParser, final ExternalProjectRoleActor projectRoleActor)
    {
        final String newProjectId = projectImportMapper.getProjectMapper().getMappedId(projectRoleActor.getProjectId());
        final String newProjectRoleId = projectImportMapper.getProjectRoleMapper().getMappedId(projectRoleActor.getRoleId());
        final String groupName = projectRoleActor.getRoleActor();
        final String projectRoleName = projectImportMapper.getProjectRoleMapper().getDisplayName(projectRoleActor.getRoleId());
        // Check if the group exists in the system (they may have just been created automatically by the import)
        if (groupManager.groupExists(groupName))
        {
            // Group exists - we can add the membership. First transform to new IDs.
            final ExternalProjectRoleActor newProjectRoleActor = new ExternalProjectRoleActor(null, newProjectId, newProjectRoleId,
                projectRoleActor.getRoleType(), groupName);
            // Persist it.
            final Long entityId = projectImportPersister.createEntity(projectRoleActorParser.getEntityRepresentation(newProjectRoleActor));
            if (entityId == null)
            {
                projectImportResults.addError("There was an error adding group '" + groupName + "' to the Project Role '" + projectRoleName + "'.");
            }
            else
            {
                projectImportResults.incrementRoleGroupCreatedCount(projectRoleName);
            }
        }
        else
        {
            log.warn("Group '" + groupName + "' does not exist, so we are not adding them to the Project Role '" + projectRoleName + "' in the Project Import.");
        }
    }

    void createUserRoleActor(final ProjectImportMapper projectImportMapper, final ProjectImportResults projectImportResults, final ProjectRoleActorParser projectRoleActorParser, final ExternalProjectRoleActor projectRoleActor)
    {
        final String newProjectId = projectImportMapper.getProjectMapper().getMappedId(projectRoleActor.getProjectId());
        final String newProjectRoleId = projectImportMapper.getProjectRoleMapper().getMappedId(projectRoleActor.getRoleId());
        final String userKey = projectRoleActor.getRoleActor();
        final String projectRoleName = projectImportMapper.getProjectRoleMapper().getDisplayName(projectRoleActor.getRoleId());
        // Check if the user exists in the system (they may have just been created automatically by the import)
        if (projectImportMapper.getUserMapper().userExists(userKey))
        {
            // User exists - we can add the membership. First transform to new IDs.
            final String mappedUserKey = projectImportMapper.getUserMapper().getMappedUserKey(userKey);
            final ExternalProjectRoleActor newProjectRoleActor = new ExternalProjectRoleActor(null, newProjectId, newProjectRoleId,
                projectRoleActor.getRoleType(), mappedUserKey);
            // Persist it.
            final Long entityId = projectImportPersister.createEntity(projectRoleActorParser.getEntityRepresentation(newProjectRoleActor));
            if (entityId == null)
            {
                projectImportResults.addError("There was an error adding user '" + projectImportMapper.getUserMapper().getDisplayName(userKey) + "' to the Project Role '" + projectRoleName + "'.");
            }
            else
            {
                projectImportResults.incrementRoleUserCreatedCount(projectRoleName);
            }
        }
        else
        {
            log.warn("User '" + projectImportMapper.getUserMapper().getDisplayName(userKey) + "' does not exist, so we are not adding them to the Project Role '" + projectRoleName + "' in the Project Import.");
        }
    }

    void updateTaskProgress(final EntityCountTaskProgressProcessor taskProgressProcessor, final String message, final int currentCount)
    {
        if (taskProgressProcessor != null)
        {
            taskProgressProcessor.processTaskProgress(message, currentCount);
        }
    }

    void populateVersionMapper(final SimpleProjectImportIdMapper versionMapper, final Map<String, Version> newVersions)
    {
        for (final Map.Entry<String, Version> entry : newVersions.entrySet())
        {
            final String oldId = entry.getKey();
            final Version version = entry.getValue();
            versionMapper.mapValue(oldId, version.getId().toString());
        }
    }

    void populateComponentMapper(final SimpleProjectImportIdMapper componentMapper, final Map<String, ProjectComponent> newComponents)
    {
        for (final Map.Entry<String, ProjectComponent> entry : newComponents.entrySet())
        {
            final String oldId = entry.getKey();
            final ProjectComponent component = entry.getValue();
            componentMapper.mapValue(oldId, component.getId().toString());
        }
    }

    ImportEntityHandler getCustomFieldValuePersisterHandler(final ProjectImportMapper projectImportMapper, final String newProjectId, final ProjectImportResults projectImportResults, final BackupSystemInformation backupSystemInformation, final Executor executor)
    {
        final Map<String, CustomFieldValueParser> parsers = getCustomFieldParsers();
        return new CustomFieldValuePersisterHandler(projectImportPersister, projectImportMapper, customFieldManager, new Long(newProjectId),
            projectImportResults, backupSystemInformation, executor, parsers);
    }

    ImportEntityHandler getCommentPersisterHandler(final ProjectImportMapper projectImportMapper, final ProjectImportResults projectImportResults, final BackupSystemInformation backupSystemInformation, final Executor executor)
    {
        return new CommentPersisterHandler(projectImportPersister, projectImportMapper, projectImportResults, backupSystemInformation, executor);
    }

    ImportEntityHandler getWorklogPersisterHandler(final ProjectImportMapper projectImportMapper, final ProjectImportResults projectImportResults, final BackupSystemInformation backupSystemInformation, final Executor executor)
    {
        return new WorklogPersisterHandler(projectImportPersister, projectImportMapper, projectImportResults, backupSystemInformation, executor);
    }

    ImportEntityHandler getLabelPersisterHandler(final ProjectImportMapper projectImportMapper, final ProjectImportResults projectImportResults, final BackupSystemInformation backupSystemInformation, final Executor executor)
    {
        return new LabelsPersisterHandler(projectImportPersister, projectImportMapper, projectImportResults, backupSystemInformation, executor);
    }

    ImportEntityHandler getVersionPersisterHandler(final ProjectImportMapper projectImportMapper, final ProjectImportResults projectImportResults, final BackupSystemInformation backupSystemInformation, final Executor executor)
    {
        return new VersionPersisterHandler(projectImportPersister, projectImportMapper, projectImportResults, backupSystemInformation, executor);
    }

    ImportEntityHandler getComponentPersisterHandler(final ProjectImportMapper projectImportMapper, final ProjectImportResults projectImportResults, final BackupSystemInformation backupSystemInformation, final Executor executor)
    {
        return new ComponentPersisterHandler(projectImportPersister, projectImportMapper, projectImportResults, backupSystemInformation, executor);
    }

    ImportEntityHandler getUserAssociationPersisterHandler(final ProjectImportMapper projectImportMapper, final ProjectImportResults projectImportResults, final BackupSystemInformation backupSystemInformation, final Executor executor)
    {
        return new UserAssociationPersisterHandler(projectImportPersister, projectImportMapper, projectImportResults, backupSystemInformation,
            executor);
    }
    
    ImportEntityHandler getIssuePropertiesPersisterHandler(final ProjectImportMapper projectImportMapper, final ProjectImportResults projectImportResults, final Executor executor)
    {
        return new EntityPropertiesPersisterHandler(executor, projectImportResults, projectImportPersister, EntityPropertyType.ISSUE_PROPERTY, projectImportMapper.getIssueMapper());
    }

    ImportEntityHandler getCommentPropertiesPersisterHandler(final ProjectImportMapper projectImportMapper, final ProjectImportResults projectImportResults, final Executor executor)
    {
        return new EntityPropertiesPersisterHandler(executor, projectImportResults, projectImportPersister, EntityPropertyType.COMMENT_PROPERTY, projectImportMapper.getCommentMapper());
    }

    ImportEntityHandler getChangeHistoryPropertiesPersisterHandler(final ProjectImportMapper projectImportMapper, final ProjectImportResults projectImportResults, final BoundedExecutor executor)
    {
        return new EntityPropertiesPersisterHandler(executor, projectImportResults, projectImportPersister, EntityPropertyType.CHANGE_HISTORY_PROPERTY, projectImportMapper.getChangeGroupMapper());
    }

    ImportEntityHandler getChangeGroupPersisterHandler(final ProjectImportMapper projectImportMapper, final ProjectImportResults projectImportResults, final BackupSystemInformation backupSystemInformation, final Executor executor)
    {
        return new ChangeGroupPersisterHandler(projectImportPersister, projectImportMapper, projectImportResults, backupSystemInformation, executor);
    }

    ImportEntityHandler getChangeItemPersisterHandler(final ProjectImportMapper projectImportMapper, final ProjectImportResults projectImportResults, final Executor executor)
    {
        return new ChangeItemPersisterHandler(projectImportPersister, projectImportMapper, projectImportResults, executor);
    }

    IssuePersisterHandler getIssuePersisterHandler(final User importAuthor, final ProjectImportMapper projectImportMapper, final ProjectImportResults projectImportResults, final Executor executor)
    {
        return new IssuePersisterHandler(projectImportPersister, projectImportMapper, importAuthor, projectImportResults, executor);
    }

    ImportEntityHandler getIssueLinkPersisterHandler(final ProjectImportMapper projectImportMapper, final ProjectImportResults projectImportResults, final BackupSystemInformation backupSystemInformation, final Executor executor, final User importAuthor)
    {
        return new IssueLinkPersisterHandler(projectImportPersister, projectImportMapper, issueManager, projectImportResults,
            backupSystemInformation, executor, importAuthor);
    }

    void populateCustomFieldMapperOldValues(final BackupProject backupProject, final CustomFieldMapper customFieldMapper)
    {
        for (final ExternalCustomFieldConfiguration customFieldConfiguration : backupProject.getCustomFields())
        {
            final ExternalCustomField externalCustomField = customFieldConfiguration.getCustomField();
            customFieldMapper.registerOldValue(externalCustomField.getId(), externalCustomField.getName());
        }
    }

    void populateVersionMapperOldValues(final BackupProject backupProject, final SimpleProjectImportIdMapper versionMapper)
    {
        for (final ExternalVersion externalVersion : backupProject.getProjectVersions())
        {
            versionMapper.registerOldValue(externalVersion.getId(), externalVersion.getName());
        }
    }

    void populateComponentMapperOldValues(final BackupProject backupProject, final SimpleProjectImportIdMapper componentMapper)
    {
        for (final ExternalComponent externalComponent : backupProject.getProjectComponents())
        {
            componentMapper.registerOldValue(externalComponent.getId(), externalComponent.getName());
        }
    }

    CustomFieldValueValidatorHandler getCustomFieldValueValidatorHandler(final BackupProject backupProject, final ProjectImportMapper projectImportMapper)
    {
        final Map<String, CustomFieldValueParser> parsers = getCustomFieldParsers();
        return new CustomFieldValueValidatorHandler(backupProject, projectImportMapper, customFieldManager, parsers);
    }

    CustomFieldMapperHandler getCustomFieldMapperHandler(final BackupProject backupProject, final ProjectImportMapper projectImportMapper)
    {
        final Map<String, CustomFieldValueParser> parsers = getCustomFieldParsers();
        return new CustomFieldMapperHandler(backupProject, projectImportMapper.getCustomFieldMapper(), parsers);
    }

    SimpleEntityMapperHandler getStatusMapperHandler(final ProjectImportMapper projectImportMapper)
    {
        return new SimpleEntityMapperHandler(SimpleEntityMapperHandler.STATUS_ENTITY_NAME, projectImportMapper.getStatusMapper());
    }

    SimpleEntityMapperHandler getResolutionMapperHandler(final ProjectImportMapper projectImportMapper)
    {
        return new SimpleEntityMapperHandler(SimpleEntityMapperHandler.RESOLUTION_ENTITY_NAME, projectImportMapper.getResolutionMapper());
    }

    SimpleEntityMapperHandler getPriorityMapperHandler(final ProjectImportMapper projectImportMapper)
    {
        return new SimpleEntityMapperHandler(SimpleEntityMapperHandler.PRIORITY_ENTITY_NAME, projectImportMapper.getPriorityMapper());
    }

    ProjectMapperHandler getProjectMapperHandler(final ProjectImportMapper projectImportMapper)
    {
        return new ProjectMapperHandler(projectImportMapper.getProjectMapper());
    }

    IssueTypeMapperHandler getIssueTypeMapperHandler(final ProjectImportMapper projectImportMapper)
    {
        return new IssueTypeMapperHandler(projectImportMapper.getIssueTypeMapper());
    }

    ProjectIssueSecurityLevelMapperHandler getProjectIssueSecurityLevelMapperHandler(final BackupProject backupProject, final ProjectImportMapper projectImportMapper)
    {
        return new ProjectIssueSecurityLevelMapperHandler(backupProject, projectImportMapper.getIssueSecurityLevelMapper());
    }

    IssueMapperHandler getIssueMapperHandler(final BackupProject backupProject, final ProjectImportMapper projectImportMapper)
    {
        return new IssueMapperHandler(backupProject, projectImportMapper);
    }

    UserMapperHandler getUserMapperHandler(final ProjectImportOptions projectImportOptions, final BackupProject backupProject, final ProjectImportMapper projectImportMapper)
    {
        return new UserMapperHandler(projectImportOptions, backupProject, projectImportMapper.getUserMapper(), attachmentStore);
    }

    GroupMapperHandler getGroupMapperHandler(final BackupProject backupProject, final ProjectImportMapper projectImportMapper)
    {
        return new GroupMapperHandler(backupProject, projectImportMapper.getGroupMapper());
    }

    ChainedSaxHandler getChainedHandler(final TaskProgressProcessor taskProgressProcessor)
    {
        return new ChainedSaxHandler(taskProgressProcessor);
    }

    CustomFieldOptionsMapperHandler getCustomFieldOptionMapperHandler(final ProjectImportMapper projectImportMapper)
    {
        return new CustomFieldOptionsMapperHandler(projectImportMapper.getCustomFieldOptionMapper());
    }

    SimpleEntityMapperHandler getProjectRoleRegistrationHandler(final ProjectImportMapper projectImportMapper)
    {
        return new SimpleEntityMapperHandler(SimpleEntityMapperHandler.PROJECT_ROLE_ENTITY_NAME, projectImportMapper.getProjectRoleMapper());
    }

    RequiredProjectRolesMapperHandler getRequiredProjectRolesMapperHandler(final BackupProject backupProject, final ProjectImportMapper projectImportMapper)
    {
        return new RequiredProjectRolesMapperHandler(backupProject, projectImportMapper.getProjectRoleMapper());
    }

    RegisterUserMapperHandler getRegisterUserMapperHandler(final ProjectImportMapper projectImportMapper)
    {
        return new RegisterUserMapperHandler(projectImportMapper.getUserMapper());
    }

    ProjectRoleActorMapperHandler getProjectRoleActorMapperHandler(final BackupProject backupProject, final ProjectImportMapper projectImportMapper)
    {
        return new ProjectRoleActorMapperHandler(backupProject, projectImportMapper.getProjectRoleActorMapper());
    }

    IssueRelatedEntitiesPartionHandler getIssueRelatedEntitesHandler(final PrintWriter issueRelatedEntitiesWriter, final PrintWriter changeItemEntitiesWriter, final BackupProject backupProject, final String encoding)
    {
        final List<ModelEntity> relatedModelEntities = new ArrayList<ModelEntity>();
        relatedModelEntities.add(getModelEntity(OfBizWorklogStore.WORKLOG_ENTITY));
        relatedModelEntities.add(getModelEntity(OfBizLabelStore.TABLE));
        relatedModelEntities.add(getModelEntity(NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME));
        relatedModelEntities.add(getModelEntity(IssueLinkParser.ISSUE_LINK_ENTITY_NAME));
        relatedModelEntities.add(getModelEntity(CommentParser.COMMENT_ENTITY_NAME));
        relatedModelEntities.add(getModelEntity(ChangeGroupParser.CHANGE_GROUP_ENTITY_NAME));
        relatedModelEntities.add(getModelEntity(ChangeItemParser.CHANGE_ITEM_ENTITY_NAME));
        relatedModelEntities.add(getModelEntity(UserAssociationParser.USER_ASSOCIATION_ENTITY_NAME));
        relatedModelEntities.add(getModelEntity(EntityPropertyParser.ENTITY_PROPERTY_ENTITY_NAME));
        return new IssueRelatedEntitiesPartionHandler(backupProject, issueRelatedEntitiesWriter,
                changeItemEntitiesWriter, relatedModelEntities, encoding, genericDelegator);
    }

    IssueRelatedEntitiesPartionHandler getFileAttachmentHandler(final PrintWriter attachmentValuesWriter, final BackupProject backupProject, final String encoding)
    {
        final ModelEntity attachmentModelEntity = getModelEntity(AttachmentParser.ATTACHMENT_ENTITY_NAME);
        // This does not need a writer for changeItems
        return new IssueRelatedEntitiesPartionHandler(backupProject, attachmentValuesWriter, null,
                EasyList.build(attachmentModelEntity), encoding, genericDelegator);
    }

    IssueRelatedEntitiesPartionHandler getCustomFieldValuesHandler(final PrintWriter customFieldValuesWriter, final BackupProject backupProject, final String encoding)
    {
        final Set<String> entities = getCustomFieldParsers().keySet();
        final List<ModelEntity> model = new ArrayList<ModelEntity>();
        for (String entity : entities)
        {
            model.add(getModelEntity(entity));
        }
        // This does not need a writer for changeItems
        return new IssueRelatedEntitiesPartionHandler(
                backupProject, customFieldValuesWriter, null, model, encoding, genericDelegator);
    }

    private Map<String, CustomFieldValueParser> getCustomFieldParsers()
    {
        final MapBuilder<String, CustomFieldValueParser> entities = MapBuilder.newBuilder();
        entities.add(CustomFieldValueParser.CUSTOM_FIELD_VALUE_ENTITY_NAME, new CustomFieldValueParserImpl());

        //check if there's any custom field types that store entities in separate tables to the custom field value table
        final List customFieldTypes = customFieldManager.getCustomFieldTypes();
        for (Object customFieldType : customFieldTypes)
        {
            if(customFieldType instanceof ProjectImportableCustomFieldParser)
            {
                final String entity = ((ProjectImportableCustomFieldParser) customFieldType).getEntityName();
                entities.add(entity, (ProjectImportableCustomFieldParser) customFieldType);
            }
        }
        return entities.toMap();
    }

    IssuePartitonHandler getIssuePartitioner(final PrintWriter issueFileWriter, final BackupProject backupProject, final String encoding)
    {
        final ModelEntity issueModelEntity = getModelEntity(IssueParser.ISSUE_ENTITY_NAME);
        return new IssuePartitonHandler(backupProject, issueFileWriter, issueModelEntity, encoding, genericDelegator);
    }

    IssueVersionMapperHandler getIssueVersionMapperHandler(final BackupProject backupProject, final ProjectImportMapper projectImportMapper)
    {
        return new IssueVersionMapperHandler(backupProject, projectImportMapper.getVersionMapper());
    }

    IssueComponentMapperHandler getIssueComponentMapperHandler(final BackupProject backupProject, final ProjectImportMapper projectImportMapper)
    {
        return new IssueComponentMapperHandler(backupProject, projectImportMapper.getComponentMapper());
    }

    AttachmentFileValidatorHandler getAttachmentFileValidatorHandler(final BackupProject backupProject, final ProjectImportOptions projectImportOptions, final BackupSystemInformation backupSystemInformation, final I18nHelper i18nHelper)
    {
        return new AttachmentFileValidatorHandler(backupProject, projectImportOptions, backupSystemInformation, i18nHelper, attachmentStore);
    }

    IssueLinkMapperHandler getIssueLinkMapperHandler(final BackupProject backupProject, final BackupSystemInformation backupSystemInformation, final ProjectImportMapper projectImportMapper)
    {
        return new IssueLinkMapperHandler(backupProject, backupSystemInformation, issueManager, projectImportMapper.getIssueLinkTypeMapper());
    }

    PrintWriter getWriter(final File file, final String encoding) throws UnsupportedEncodingException, FileNotFoundException
    {
        final FileOutputStream fos = new FileOutputStream(file);
        final OutputStreamWriter out = new OutputStreamWriter(fos, encoding);
        final BufferedWriter bufferedWriter = new BufferedWriter(out, DefaultSaxEntitiesExporter.DEFAULT_BUFFER_SIZE);
        return new PrintWriter(bufferedWriter);
    }

    ///CLOVER:OFF
    void subvertSecurityIndexingNotifications()
    {
        ImportUtils.setSubvertSecurityScheme(true);
        ImportUtils.setIndexIssues(false);
        ImportUtils.setEnableNotifications(false);
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    void restoreSecurityIndexingNotifications()
    {
        ImportUtils.setSubvertSecurityScheme(false);
        ImportUtils.setIndexIssues(true);
        ImportUtils.setEnableNotifications(true);
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    TaskProgressInterval getSubInterval(final TaskProgressInterval taskProgressInterval, final int subIntervalStart, final int subIntervalEnd)
    {
        if (taskProgressInterval == null)
        {
            return null;
        }
        else
        {
            return taskProgressInterval.getSubInterval(subIntervalStart, subIntervalEnd);
        }
    }

    ///CLOVER:ON

    ///CLOVER:OFF - this is just a call-through to ofBiz which we mock out in tests
    /**
     * Gets the total number of non-view entities that OfBiz knows about.
     *
     * @return the total number of non-view entities that OfBiz knows about, or 100 if something goes wrong.
     */
    int getTotalEntitiesCount()
    {
        // NOTE: this is not tested since we will need to have OfBiz configured for the unit test and if we don't
        // want to have to change the test every time we add a table we would need to use this exact same code
        // to determine if this code is correct, sounds a bit silly
        int count = 0;
        try
        {
            final ModelReader reader = genericDelegator.getModelReader();
            for (final Iterator iterator = reader.getEntityNamesIterator(); iterator.hasNext();)
            {
                final String entityName = (String) iterator.next();
                final ModelEntity modelEntity = genericDelegator.getModelReader().getModelEntity(entityName);

                // We don't want to include the views in our entity count
                if (!(modelEntity instanceof ModelViewEntity))
                {
                    count++;
                }
            }
            return count;
        }
        catch (final GenericEntityException e)
        {
            // Lets just fall back to 100
            return DEFAULT_ENTITY_COUNT;
        }
    }

    ///CLOVER:ON

    ///CLOVER:OFF - this is just a call-through to ofBiz which we mock out in tests
    ModelEntity getModelEntity(final String entityName)
    {
        try
        {
            return genericDelegator.getModelReader().getModelEntity(entityName);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    ///CLOVER:ON

    ///CLOVER:OFF - always mocked out and delegates to appProps
    String getApplicationEncoding()
    {
        return applicationProperties.getEncoding();
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    ProjectRoleActorParser getProjectRoleActorParser()
    {
        return new ProjectRoleActorParserImpl();
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    BoundedExecutor createExecutor(final String threadName)
    {
        return new BoundedExecutor(Executors.newFixedThreadPool(THREAD_POOL_SIZE, new ProjectImportThreadFactory(threadName)), THREAD_POOL_QUEUE_SIZE);
    }

    ///CLOVER:ON

    /** Internal thread factory class for threads created when executing tasks. */
    private static class ProjectImportThreadFactory implements ThreadFactory
    {
        private final AtomicLong threadId = new AtomicLong(0);
        private final String threadName;

        private ProjectImportThreadFactory(final String threadName)
        {
            this.threadName = threadName;
        }

        public Thread newThread(final Runnable runnable)
        {
            return new Thread(runnable, threadName + "-" + threadId.incrementAndGet());
        }
    }

}
