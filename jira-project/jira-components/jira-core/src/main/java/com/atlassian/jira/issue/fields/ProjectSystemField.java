package com.atlassian.jira.issue.fields;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.RestAwareField;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.ProjectJsonBean;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.handlers.ProjectSearchHandlerFactory;
import com.atlassian.jira.issue.search.parameters.lucene.sort.StringSortComparator;
import com.atlassian.jira.issue.statistics.ProjectStatisticsMapper;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBean;
import org.apache.lucene.search.SortField;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * JIRA's project system field.
 */
public class ProjectSystemField extends AbstractOrderableNavigableFieldImpl implements ProjectField, RestAwareField
{
    public static final String PROJECT_NAME_KEY = "issue.field.project";
    private static final String FIELD_PARAMETER_NAME = "pid";

    private final ProjectManager projectManager;
    private final PermissionManager permissionManager;
    private final ProjectStatisticsMapper projectStatisticsMapper;
    private final JiraBaseUrls jiraBaseUrls;
    private final UserProjectHistoryManager projectHistoryManager;

    public ProjectSystemField(VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties,
            JiraAuthenticationContext authenticationContext, ProjectManager projectManager,
            PermissionManager permissionManager, ProjectStatisticsMapper projectStatisticsMapper,
            final ProjectSearchHandlerFactory searchHandlerFactory, JiraBaseUrls jiraBaseUrls, final UserProjectHistoryManager projectHistoryManager)
    {
        super(IssueFieldConstants.PROJECT, PROJECT_NAME_KEY, templatingEngine, applicationProperties, authenticationContext, permissionManager, searchHandlerFactory);
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
        this.projectStatisticsMapper = projectStatisticsMapper;
        this.jiraBaseUrls = jiraBaseUrls;
        this.projectHistoryManager = projectHistoryManager;
    }

    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        final Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        Long projectId = (Long) operationContext.getFieldValuesHolder().get(getId());
        velocityParams.put(getId(), projectId);

        final User user = authenticationContext.getLoggedInUser();
        final Collection<Project> projects = permissionManager.getProjectObjects(Permissions.CREATE_ISSUE, user);
        List<Project> recentProjects = projectHistoryManager.getProjectHistoryWithPermissionChecks(Permissions.CREATE_ISSUE, user);
        recentProjects = recentProjects.subList(0, Math.min(6, recentProjects.size()));

        velocityParams.put("projects", projects);
        velocityParams.put("recentProjects", recentProjects);
        return renderTemplate("project-edit.vm", velocityParams);
    }

    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        throw new UnsupportedOperationException("Project field cannot be edited.");
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Object value, Map displayParameters)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public String getBulkEditHtml(OperationContext operationContext, Action action, BulkEditBean bulkEditBean, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(null, action, null, displayParameters);
        Long projectId = (Long) operationContext.getFieldValuesHolder().get(getId());
        velocityParams.put(getId(), projectId);
        Collection<Project> allowedProjects = getAllowedProjects();

        velocityParams.put("projects", allowedProjects);
        return renderTemplate("project-edit.vm", velocityParams);
    }

    public void validateParams(OperationContext operationContext, ErrorCollection errorCollectionToAddTo, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        try
        {
            Map fieldValuesHolder = operationContext.getFieldValuesHolder();
            Long projectId = (Long) fieldValuesHolder.get(getId());
            if (projectId != null)
            {
                Project project = getProject(projectId);
                if (project == null)
                {
                    errorCollectionToAddTo.addError(FIELD_PARAMETER_NAME, i18n.getText("createissue.invalidproject"), Reason.VALIDATION_FAILED);
                }
                else if (!getAllowedProjects().contains(project))
                {
                    if (authenticationContext.getLoggedInUser() != null)
                    {
                        errorCollectionToAddTo.addError(FIELD_PARAMETER_NAME, i18n.getText("createissue.projectnopermission"), Reason.FORBIDDEN);
                    }
                    else
                    {
                        errorCollectionToAddTo.addError(FIELD_PARAMETER_NAME, i18n.getText("createissue.projectnopermission.notloggedin"), Reason.NOT_LOGGED_IN);
                    }
                }
                else
                {
                    // Check when creating a subtask that the project on the parent matches.
                    if (issue.getParentObject() != null)
                    {
                        if (!issue.getParentObject().getProjectObject().getId().equals(projectId))
                        {
                            errorCollectionToAddTo.addError(FIELD_PARAMETER_NAME, i18n.getText("createsubtask.error.notsameproject"), Reason.VALIDATION_FAILED);
                        }
                    }
                }
            }
            else
            {
                errorCollectionToAddTo.addError(FIELD_PARAMETER_NAME, i18n.getText("createissue.noproject"), Reason.VALIDATION_FAILED);
            }
        }
        catch (NumberFormatException e)
        {
            errorCollectionToAddTo.addError(getId(), i18n.getText("createissue.invalidproject"), Reason.VALIDATION_FAILED);
        }

    }

    protected Object getRelevantParams(Map<String, String[]> params)
    {
        String[] value = params.get(FIELD_PARAMETER_NAME);
        if (value != null && value.length > 0)
        {
            return new Long(value[0]);
        }
        else
        {
            return null;
        }
    }

    protected Project getProject(Long projectId)
    {
        return projectManager.getProjectObj(projectId);
    }

    @Override
    public Project getValueFromParams(Map params)
    {
        return getProject((Long) params.get(getId()));
    }

    public void populateParamsFromString(Map<String, Object> fieldValuesHolder, String stringValue, Issue issue) throws FieldValidationException
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public Collection<Project> getAllowedProjects()
    {
        return getPermissionManager().getProjects(Permissions.CREATE_ISSUE, getAuthenticationContext().getUser());
    }

    public boolean isShown(Issue issue)
    {
        return true;
    }

    public void populateDefaults(Map<String, Object> fieldValuesHolder, Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public void populateFromIssue(Map<String, Object> fieldValuesHolder, Issue issue)
    {
        fieldValuesHolder.put(getId(), issue.getProjectObject().getId());
    }

    public Object getDefaultValue(Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public void createValue(Issue issue, Object value)
    {
        // Do not do anything the value is recorded on the issue itself
    }

    public void updateValue(FieldLayoutItem fieldLayoutItem, Issue issue, ModifiedValue modifiedValue, IssueChangeHolder issueChangeHolder)
    {
        throw new UnsupportedOperationException("Project field cannot be changed.");
    }

    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        issue.setProjectObject(getValueFromParams(fieldValueHolder));
    }

    public MessagedResult needsMove(Collection originalIssues, Issue targetIssue, FieldLayoutItem targetFieldLayoutItem)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public void populateForMove(Map<String, Object> fieldValuesHolder, Issue originalIssue, Issue targetIssue)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public void removeValueFromIssueObject(MutableIssue issue)
    {
        throw new UnsupportedOperationException("This method should never be called.");
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return false;
    }

    public boolean hasValue(Issue issue)
    {
        return (issue.getProject() != null);
    }

    ///////////////////////////////////////////// NavigableField implementation //////////////////////////////////////
    public String getColumnHeadingKey()
    {
        return "issue.column.heading.project";
    }

    public String getDefaultSortOrder()
    {
        return NavigableField.ORDER_ASCENDING;
    }

    public LuceneFieldSorter getSorter()
    {
        return projectStatisticsMapper;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map<String, Object> velocityParams = getVelocityParams(getAuthenticationContext().getI18nHelper(), displayParams, issue);
        velocityParams.put("project", issue.getProject());
        return renderTemplate("project-columnview.vm", velocityParams);
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        // not really used, there is special-case code in the REST plugin to deal with project createmeta
        return new FieldTypeInfo(null, null);
    }

    @Override
    public JsonType getJsonSchema()
    {
        return getJsonType();
    }
    public static JsonType getJsonType()
    {
        return JsonTypeBuilder.system(JsonType.PROJECT_TYPE, IssueFieldConstants.PROJECT);
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(Issue issue, boolean renderedVersionRequested, @Nullable FieldLayoutItem fieldLayoutItem)
    {
        return new FieldJsonRepresentation(new JsonData(ProjectJsonBean.shortBean(issue.getProjectObject(), jiraBaseUrls)));
    }

}
