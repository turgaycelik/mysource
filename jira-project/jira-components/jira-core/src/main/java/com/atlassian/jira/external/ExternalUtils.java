package com.atlassian.jira.external;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.ProjectCreatedEvent;
import com.atlassian.jira.external.beans.ExternalCustomFieldValue;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.external.beans.ExternalUser;
import com.atlassian.jira.external.beans.ExternalVersion;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.admin.customfields.CreateCustomField;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.workflow.loader.StepDescriptor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

public class ExternalUtils
{
    private final ProjectManager projectManager;
    private final PermissionSchemeManager permissionSchemeManager;
    private final IssueManager issueManager;
    private final JiraAuthenticationContext authenticationContext;
    private final VersionManager versionManager;
    private final CustomFieldManager customFieldManager;
    private final GenericDelegator genericDelegator;
    private final ConstantsManager constantsManager;
    private final WorkflowManager workflowManager;
    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private final FieldScreenManager fieldScreenManager;
    private final ApplicationProperties applicationProperties;
    private final UserUtil userUtil;
    private final EventPublisher eventPublisher;

    private static final Logger log = Logger.getLogger(ExternalUtils.class);

    public static final String TYPE_SEPERATOR = ":";
    public static final String CF_PREFIX = "customfield_";

    private static final String SEARCHER = "searcher";

    public ExternalUtils(final ProjectManager projectManager, final PermissionSchemeManager permissionSchemeManager, final IssueManager issueManager,
            final JiraAuthenticationContext authenticationContext, final VersionManager versionManager, final CustomFieldManager customFieldManager,
            final GenericDelegator genericDelegator, final ConstantsManager constantsManager, final WorkflowManager workflowManager, final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager,
            final FieldScreenManager fieldScreenManager, final ApplicationProperties applicationProperties, final UserUtil userUtil,
            final EventPublisher eventPublisher)
    {
        this.projectManager = projectManager;
        this.permissionSchemeManager = permissionSchemeManager;
        this.issueManager = issueManager;
        this.authenticationContext = authenticationContext;
        this.versionManager = versionManager;
        this.customFieldManager = customFieldManager;
        this.genericDelegator = genericDelegator;
        this.constantsManager = constantsManager;
        this.workflowManager = workflowManager;
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
        this.fieldScreenManager = fieldScreenManager;
        this.applicationProperties = applicationProperties;
        this.userUtil = userUtil;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Tries to find an existing Project based on the values in the given ExternalProject.
     * @param externalProject the ExternalProject.
     * @return the project or null if none exist
     */
    public Project getProjectObject(final ExternalProject externalProject)
    {
        Project project = null;

        if (StringUtils.isNotEmpty(externalProject.getKey()))
        {
            project = projectManager.getProjectObjByKey(externalProject.getKey());
        }

        if ((project == null) && StringUtils.isNotEmpty(externalProject.getName()))
        {
            project = projectManager.getProjectObjByName(externalProject.getName());
        }

        return project;
    }

    /**
     * Create a project in JIRA from the given ExternalProject.
     * @param externalProject the ExternalProject definition
     * @return The newly created Project
     * @throws ExternalException if anything goes wrong
     */
    public Project createProject(final ExternalProject externalProject) throws ExternalException
    {
        try
        {
            // Set lead to current user if none exists
            if (externalProject.getLead() == null)
            {
                externalProject.setLead(authenticationContext.getUser().getName());
            }

            // JRA-19699: if there is no assignee type - set it to either UNASSIGNED or PROJECT LEAD
            if (externalProject.getAssigneeType() == null)
            {
                if (isUnassignedIssuesAllowed())
                {
                    externalProject.setAssigneeType(String.valueOf(AssigneeTypes.UNASSIGNED));
                }
                else
                {
                    externalProject.setAssigneeType(String.valueOf(AssigneeTypes.PROJECT_LEAD));
                }
            }

            final Project project = projectManager.createProject(
                    externalProject.getName(),
                    externalProject.getKey(),
                    externalProject.getDescription(),
                    externalProject.getLead(),
                    externalProject.getUrl(),
                    new Long(externalProject.getAssigneeType())
                    );

            eventPublisher.publish(new ProjectCreatedEvent(null, project));

            // Add the default schemes for this project
            permissionSchemeManager.addDefaultSchemeToProject(project);
            issueTypeScreenSchemeManager.associateWithDefaultScheme(project);

            return project;
        }
        catch (final Exception e)
        {
            throw new ExternalException("Unable to create project: " + externalProject, e);
        }
    }

    private boolean isUnassignedIssuesAllowed()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED);
    }

    public Version createVersion(final ExternalProject externalProject, final ExternalVersion externalVersion)
    {
        Version jiraVersion = null;
        try
        {
            final String versionName = externalVersion.getName();
            jiraVersion = versionManager.createVersion(versionName, externalVersion.getReleaseDate(), externalVersion.getDescription(),
                    externalProject.getProjectGV().getLong("id"), null);

            if (externalVersion.isArchived())
            {
                versionManager.archiveVersion(jiraVersion, true);
            }
        }
        catch (final Exception e)
        {
            log.warn("Problems encoutered while creating Version " + externalVersion, e);
        }
        return jiraVersion;
    }

    public Version getVersion(final ExternalProject externalProject, final ExternalVersion externalVersion)
    {
        Version jiraVersion = null;
        try
        {
            final String versionName = externalVersion.getName();
            jiraVersion = versionManager.getVersion(externalProject.getProjectGV().getLong("id"), versionName);
        }
        catch (final Exception e)
        {
            log.warn("Problems encoutered while retrieving Version " + externalVersion, e);
        }
        return jiraVersion;
    }

    public User createUser(final ExternalUser externalUser)
    {
        try
        {
            return userUtil.createUserNoNotification(externalUser.getName(), externalUser.getPassword(), externalUser.getEmail(), externalUser.getFullname());
        }
        catch (final Exception e)
        {
            log.warn("Problems encoutered while creating User " + externalUser, e);
            return null;
        }
    }

    public GenericValue createIssue(final Issue issue, final String status, final String resolution) throws ExternalException
    {
        try
        {
            if (StringUtils.isNotBlank(status))
            {
                // Validate status and that it has a linked step in the workflow. This method will throw exception
                // if some data is invalid.
                checkStatus(issue, status);
            }

            final GenericValue issueGV = issueManager.createIssue(authenticationContext.getLoggedInUser(), issue);

            if (StringUtils.isNotBlank(status))
            {
                setCurrentWorkflowStep(issueGV, status, resolution);
            }

            return issueGV;
        }
        catch (final Exception e)
        {
            throw new ExternalException("Unable to create issue: " + issue, e);
        }
    }

    protected void checkStatus(final Issue issue, final String statusId) throws WorkflowException, ExternalException
    {
        // Check that the status is OK
        if (issue != null)
        {
            final Status status = constantsManager.getStatusObject(statusId);

            if (status != null)
            {
                final JiraWorkflow workflow = workflowManager.getWorkflow(issue.getProjectId(), issue.getIssueTypeId());
                final StepDescriptor linkedStep = workflow.getLinkedStep(status);

                if (linkedStep == null)
                {
                    throw new ExternalException(
                        "Status '" + status.getName() + "' does not have a linked step in the '" + workflow.getName() + "' workflow. Please map to a different status.");
                }
            }
            else
            {
                throw new ExternalException("Cannot find status with id '" + statusId + "'.");
            }
        }
    }

    private void setCurrentWorkflowStep(final GenericValue issue, final String statusId, final String resolution) throws GenericEntityException, WorkflowException
    {
        // retrieve the wfCurrentStep for this issue and change it
        if (issue != null)
        {
            final Status status = constantsManager.getStatusObject(statusId);

            if (status != null)
            {
                final JiraWorkflow workflow = workflowManager.getWorkflow(issue);
                final StepDescriptor linkedStep = workflow.getLinkedStep(status);

                final Collection<GenericValue> wfCurrentStepCollection = genericDelegator.findByAnd("OSCurrentStep",
                        MapBuilder.build("entryId", issue.getLong("workflowId")));
                if ((wfCurrentStepCollection != null) && !wfCurrentStepCollection.isEmpty())
                {
                    final GenericValue wfCurrentStep = wfCurrentStepCollection.iterator().next();
                    if (linkedStep != null)
                    {
                        wfCurrentStep.set("stepId", linkedStep.getId());
                        wfCurrentStep.store();
                    }
                    else
                    {
                        // This should never occur as the status had to be checked before this
                        log.error("Workflow '" + workflow.getName() + "' does not have a step for status '" + status.getName() + "'.");
                    }
                }
                else
                {
                    log.warn("Workflow Id not found");
                }

                // Set the resolution & statuses nicely
                issue.set("status", statusId);
                issue.set("resolution", resolution);
                issue.store();
            }
            else
            {
                log.warn("Status' GV for '" + statusId + "' was null. Issue not updated. " + issue);
            }
        }
    }

    public CustomField getCustomField(final ExternalCustomFieldValue customFieldValue)
    {
        final String customfieldId = customFieldValue.getKey();
        return getCustomField(customfieldId);

    }

    public CustomField getCustomField(final String customfieldId)
    {
        CustomField customFieldObject = null;
        try
        {
            try
            {
                customFieldObject = customFieldManager.getCustomFieldObject(customfieldId);
            }
            catch (final NumberFormatException e)
            {
                // Don't do anything, expected for new stuff
            }
            catch (final Exception e)
            {
                log.warn(e.getMessage(), e);
            }

            if (customFieldObject == null)
            {
                final String fieldName = extractCustomFieldId(customfieldId);
                customFieldObject = customFieldManager.getCustomFieldObjectByName(fieldName);
            }

        }
        catch (final Exception e)
        {
            // Can't get the custom field
            log.warn(e.getMessage(), e);
        }

        return customFieldObject;
    }

    public CustomField createCustomField(final ExternalCustomFieldValue customFieldValue)
    {
        CustomField customFieldObject = null;

        final String customfieldId = customFieldValue.getKey();
        final String fieldName = extractCustomFieldId(customfieldId);
        final String fieldType = extractCustomFieldType(customfieldId);

        try
        {
            // Create a new custom field
            customFieldObject = createCustomField(fieldName, fieldType);
        }
        catch (final ExternalException e)
        {
            log.warn("Unable to create custom field " + customFieldValue, e);
        }

        return customFieldObject;
    }

    public String extractCustomFieldType(final String customfieldId)
    {
        return StringUtils.substringAfter(customfieldId, TYPE_SEPERATOR);
    }

    public String extractCustomFieldId(final String customfieldId)
    {
        String fieldId;
        if (StringUtils.contains(customfieldId, TYPE_SEPERATOR))
        {
            fieldId = StringUtils.substringBetween(customfieldId, CF_PREFIX, TYPE_SEPERATOR);
        }
        else
        {
            fieldId = StringUtils.substringAfter(customfieldId, CF_PREFIX);
        }
        return fieldId;
    }

    private static final String TEXT_FIELD_TYPE = "textfield";
    private static final String TEXT_FIELD_SEARCHER = "textsearcher";

    private static final String DATE_FIELD_TYPE = "datepicker";
    private static final String DATE_FIELD_SEARCHER = "daterange";

    private CustomField createCustomField(final String customFieldName, final String type) throws ExternalException
    {
        try
        {
            // Create cf of the correct type
            CustomFieldType cfType;
            CustomFieldSearcher searcher;

            // @TODO this is surely unescessary? Should just always match the field name
            if ("select".equals(type) || "userpicker".equals(type) || "multiselect".equals(type))
            {
                cfType = customFieldManager.getCustomFieldType(CreateCustomField.FIELD_TYPE_PREFIX + type);
                searcher = customFieldManager.getCustomFieldSearcher(CreateCustomField.FIELD_TYPE_PREFIX + type + SEARCHER);
            }
            else if ("date".equals(type) || DATE_FIELD_TYPE.equals(type))
            {
                cfType = customFieldManager.getCustomFieldType(CreateCustomField.FIELD_TYPE_PREFIX + DATE_FIELD_TYPE);
                searcher = customFieldManager.getCustomFieldSearcher(CreateCustomField.FIELD_TYPE_PREFIX + DATE_FIELD_SEARCHER);
            }
            else
            {
                cfType = customFieldManager.getCustomFieldType(CreateCustomField.FIELD_TYPE_PREFIX + TEXT_FIELD_TYPE);
                searcher = customFieldManager.getCustomFieldSearcher(CreateCustomField.FIELD_TYPE_PREFIX + TEXT_FIELD_SEARCHER);
            }

            final CustomField customField = customFieldManager.createCustomField(customFieldName, customFieldName, cfType, searcher,
                EasyList.build(GlobalIssueContext.getInstance()), EasyList.buildNull());

            associateCustomFieldWithScreen(customField, null);

            return customField;

        }
        catch (final GenericEntityException e)
        {
            throw new ExternalException(e);
        }
    }

    public void associateCustomFieldWithScreen(final CustomField customField, FieldScreen screen)
    {
        if (screen == null)
        {
            screen = fieldScreenManager.getFieldScreen(FieldScreen.DEFAULT_SCREEN_ID);
        }

        if ((screen != null) && (screen.getTabs() != null) && !screen.getTabs().isEmpty())
        {
            final FieldScreenTab tab = screen.getTab(0);
            tab.addFieldScreenLayoutItem(customField.getId());
        }
    }

}
