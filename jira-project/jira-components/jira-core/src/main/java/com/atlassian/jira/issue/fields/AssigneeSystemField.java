package com.atlassian.jira.issue.fields;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.option.AssigneeOptions;
import com.atlassian.jira.issue.fields.rest.AssigneeRestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.RestAwareField;
import com.atlassian.jira.issue.fields.rest.RestFieldOperations;
import com.atlassian.jira.issue.fields.rest.RestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.UserJsonBean;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.WorkflowIssueOperation;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.handlers.AssigneeSearchHandlerFactory;
import com.atlassian.jira.issue.statistics.AssigneeStatisticsMapper;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.plugin.assignee.AssigneeResolver;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.issue.IssueUtils.AUTOMATIC_ASSIGNEE;
import static com.atlassian.jira.issue.IssueUtils.SEPERATOR_ASSIGNEE;

/**
 * Copyright (c) 2002-2004 All rights reserved.
 */
public class AssigneeSystemField extends AbstractUserFieldImpl implements HideableField, RestAwareField, RestFieldOperations
{
    private static final Logger log = Logger.getLogger(AssigneeSystemField.class);

    private static final String ASSIGNEE_NAME_KEY = "issue.field.assignee";
    public static final String AUTOMATIC_ASSIGNEE_STRING = "-automatic-";

    private final AssigneeStatisticsMapper assigneeStatisticsMapper;
    private final AssigneeResolver assigneeResolver;
    private final UserManager userManager;
    private final JiraBaseUrls jiraBaseUrls;
    private final Assignees assignees;
    private final WebResourceManager webResourceManager;
    private final FeatureManager featureManager;
    private final EmailFormatter emailFormatter;

    public AssigneeSystemField(VelocityTemplatingEngine templatingEngine, PermissionManager permissionManager,
            ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext,
            AssigneeStatisticsMapper assigneeStatisticsMapper, AssigneeResolver assigneeResolver,
            AssigneeSearchHandlerFactory assigneeSearchHandlerFactory, UserManager userManager,
            WebResourceManager webResourceManager, FeatureManager featureManager,
            JiraBaseUrls jiraBaseUrls, Assignees assignees, UserHistoryManager userHistoryManager, EmailFormatter emailFormatter)

    {
        super(IssueFieldConstants.ASSIGNEE, ASSIGNEE_NAME_KEY, templatingEngine, applicationProperties, authenticationContext, permissionManager, assigneeSearchHandlerFactory, userHistoryManager);
        this.assigneeStatisticsMapper = assigneeStatisticsMapper;
        this.assigneeResolver = assigneeResolver;
        this.userManager = userManager;
        this.featureManager = featureManager;
        this.jiraBaseUrls = jiraBaseUrls;
        this.webResourceManager = webResourceManager;
        this.assignees = assignees;
        this.emailFormatter = emailFormatter;
    }

    private Map<String, Object> makeSoyRenderData(String currentAssigneeName, AssigneeOptions assigneeOptions, List<Project> projects, Issue issue, ActionDescriptor actionDescriptor)
    {
        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, Object> field = new HashMap<String, Object>();
        field.put("id", getId());
        field.put("name", getId());

        data.put("field", field);
        if (actionDescriptor != null)
        {
            data.put("actionDescriptorId", actionDescriptor.getId());
        }

        if (issue != null && issue.getKey() != null)
        {
            data.put("issueKey", issue.getKey());
        }

        if (currentAssigneeName != null && assigneeOptions.isInvalidAssigneeSelected()) {
            data.put("editValue", currentAssigneeName);
        }

        data.put("assigneeOptions", assigneeOptions);
        data.put("isLoggedInUserAssignable", assigneeOptions.isLoggedInUserAssignable());

        if (projects != null)
        {
            String projectKeys = StringUtils.join(Lists.transform(projects, new Function<Project, String>()
            {
                @Override
                public String apply(Project project)
                {
                    return project.getKey();
                }
            }), ",");
            data.put("projectKeys", projectKeys);
        }

        return data;
    }

    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        return getEditHtml(fieldLayoutItem, operationContext, action, issue, displayParameters);
    }

    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);

        String currentAssigneeName = (String) operationContext.getFieldValuesHolder().get(getId());
        velocityParams.put("currentAssignee", currentAssigneeName);
        if (hasContext(operationContext, issue))
        {
            if (useFrotherControl())
            {
                AssigneeOptions assigneeOptions = assignees.optionsForFrotherControl(issue, extractActionDescriptor(operationContext), currentAssigneeName);
                List<Project> projects = Arrays.asList(issue.getProjectObject());
                velocityParams.put("soyRenderData", makeSoyRenderData(currentAssigneeName, assigneeOptions, projects, issue, extractActionDescriptor(operationContext)));
            }
            else
            {
                velocityParams.put("assigneeOptions", assignees.optionsForHtmlSelect(issue, extractActionDescriptor(operationContext)));
            }

            Map<String, String> auiparams = (Map<String, String>) velocityParams.get("auiparams");
            auiparams.put("controlHeaderId", "assignee-container");

            return renderTemplate("assignee-edit.vm", velocityParams);
        }
        else
        {
            webResourceManager.requireResource("jira.webresources:autocomplete");

            velocityParams.put("allowUnassigned", isUnassignedIssuesEnabled());
            return renderTemplate("assignee-edit-no-context.vm", velocityParams);
        }
    }

    public String getBulkEditHtml(OperationContext operationContext, Action action, BulkEditBean bulkEditBean, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(null, action, null, displayParameters);

        String currentAssigneeName = (String) operationContext.getFieldValuesHolder().get(getId());
        velocityParams.put("currentAssignee", currentAssigneeName);

        ActionDescriptor actionDescriptor = extractActionDescriptor(bulkEditBean);
        final Collection<Issue> issues = getBulkEditIssues(bulkEditBean);

        boolean frotherAssignee = useFrotherControl();
        if (frotherAssignee)
        {
            AssigneeOptions assigneeOptions = assignees.bulkOptionsForFrotherControl(issues, extractActionDescriptor(operationContext));
            List<Project> projects = new ArrayList<Project>(getIssueProjects(issues));
            velocityParams.put("soyRenderData", makeSoyRenderData(currentAssigneeName, assigneeOptions, projects, null, extractActionDescriptor(operationContext)));
        }
        else
        {
            velocityParams.put("assigneeOptions", assignees.bulkOptionsForHtmlSelect(issues, actionDescriptor));
        }

        return renderTemplate("assignee-edit.vm", velocityParams);
    }

    private boolean useFrotherControl()
    {
        // The Frother Assignee field breaks some old tests expecting the select element to exist with all user
        // options - allow these tests to run without it in the short term by setting the 'off' flag.
        boolean on = featureManager.isEnabled("frother.assignee.field");
        boolean off = featureManager.isEnabled("no.frother.assignee.field");
        return on && !off;
    }

    private Set<Project> getIssueProjects(Collection<Issue> issues)
    {
        Set<Project> projects = new HashSet<Project>();
        for (Issue issue : issues)
        {
            projects.add(issue.getProjectObject());
        }
        return projects;
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        User assignee = issue.getAssignee();
        if (assignee != null)
        {
            velocityParams.put("assignee", assignee.getName());
        }
        else
        {
            velocityParams.put("assignee", null);
        }
        return getViewHtml(velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Object value, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, null, displayParameters);
        velocityParams.put("assignee", value);
        return getViewHtml(velocityParams);
    }

    private String getViewHtml(Map velocityParams)
    {
        velocityParams.put("userutils", new UserUtils());
        return renderTemplate("assignee-view.vm", velocityParams);
    }

    /**
     * Validate from parameters given an existing issue (usually invoked during some sort of edit stage)
     */
    public void validateParams(OperationContext operationContext, ErrorCollection errorCollectionToAddTo, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        Map fieldValuesHolder = operationContext.getFieldValuesHolder();
        String assigneeUsername = (String) fieldValuesHolder.get(getId());

        if (SEPERATOR_ASSIGNEE.equals(assigneeUsername))
        {
            errorCollectionToAddTo.addError(getId(), i18n.getText("assign.error.invaliduser"), Reason.VALIDATION_FAILED);
            return;
        }
        else if (AUTOMATIC_ASSIGNEE.equals(assigneeUsername))
        {
            errorCollectionToAddTo.addErrorCollection(assigneeResolver.validateDefaultAssignee(issue, fieldValuesHolder));
            return;
        }
        else
        {
            // The user must have 'assign' permission - as otherwise 'automatic' should be chosen, or the field should not
            // be presented at all
            if (!hasPermission(issue, Permissions.ASSIGN_ISSUE))
            {
                errorCollectionToAddTo.addErrorMessage(i18n.getText("assign.error.no.permission"), Reason.FORBIDDEN);
                return;
            }
        }

        // Check that the assignee is valid
        if (assigneeUsername != null)
        {
            final ApplicationUser assigneeUser = userManager.getUserByName(assigneeUsername);
            if (assigneeUser == null)
            {
                errorCollectionToAddTo.addError(getId(), i18n.getText("admin.errors.issues.user.does.not.exit", "'" + assigneeUsername + "'"), Reason.VALIDATION_FAILED);
            }
            else
            {
                // Check that the assignee has the assignable permission
                // But if they are already the current assignee, we will let them remain
                if (!assigneeUsername.equals(issue.getAssigneeId()) &&
                        !ComponentAccessor.getPermissionManager().hasPermission(Permissions.ASSIGNABLE_USER, issue, assigneeUser))
                {
                   errorCollectionToAddTo.addError("assignee", i18n.getText("assign.error.user.cannot.be.assigned", "'" + assigneeUsername + "'"), Reason.VALIDATION_FAILED);
                }
            }
        }
        else
        {
            // check whether assigning to null is allowed
            if (!isUnassignedIssuesEnabled())
            {
                log.info("Validation error: Issues must be assigned");
                errorCollectionToAddTo.addError("assignee", i18n.getText("assign.error.issues.unassigned"), Reason.VALIDATION_FAILED);
            }
        }
    }

    private boolean isUnassignedIssuesEnabled()
    {
        return getApplicationProperties().getOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED);
    }

    public Object getDefaultValue(Issue issue)
    {
        return null;
    }

    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        if (fieldValueHolder.containsKey(getId()))
        {
            User assignee;
            String assigneeUsername = (String) getValueFromParams(fieldValueHolder);
            boolean addToHistory = true;
            if (AUTOMATIC_ASSIGNEE.equals(assigneeUsername))
            {
                assignee = assigneeResolver.getDefaultAssigneeObject(issue, fieldValueHolder);
                addToHistory = false;
            }
            else
            {
                assignee = getUser(assigneeUsername);
            }

            issue.setAssignee(assignee);
            if (addToHistory)
            {
                addToUsedUserHistoryIfValueChanged(issue);
            }
        }
    }

    public MessagedResult needsMove(Collection originalIssues, Issue targetIssue, FieldLayoutItem targetFieldLayoutItem)
    {
        for (final Object originalIssue1 : originalIssues)
        {
            Issue originalIssue = (Issue) originalIssue1;

            if (hasValue(originalIssue))
            {
                // See if the assignee is assignable in the target project
                if (!getPermissionManager().hasPermission(Permissions.ASSIGNABLE_USER, targetIssue.getProjectObject(), originalIssue.getAssigneeUser()))
                {
                    return new MessagedResult(true);
                }
            }
            else
            {
                // See unassigned issues are allowed - if not, then we need to set the value
                if (!isUnassignedIssuesEnabled())
                {
                    return new MessagedResult(true);
                }
            }
        }
        return new MessagedResult(false);
    }

    public void populateForMove(Map<String, Object> fieldValuesHolder, Issue originalIssue, Issue targetIssue)
    {
        // If we are displaying the field then the current assignee is not assinable, so populate with default value.
        populateDefaults(fieldValuesHolder, targetIssue);
    }

    public void removeValueFromIssueObject(MutableIssue issue)
    {
        issue.setAssignee(null);
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return true;
    }

    public boolean hasValue(Issue issue)
    {
        return issue.getAssignee() != null;
    }

    public void createValue(Issue issue, Object value)
    {
        // The field is recorded on the issue itself so there is nothing to do
    }

    /**
     * Update the issue
     */
    public void updateValue(FieldLayoutItem fieldLayoutItem, Issue issue, ModifiedValue modifiedValue, IssueChangeHolder issueChangeHolder)
    {
        //NOTE: No need to update issue in the data store as the value is stored on the issue record itself

        Object currentValue = modifiedValue.getOldValue();
        Object value = modifiedValue.getNewValue();
        ChangeItemBean cib = null;

        if (currentValue == null)
        {
            if (value != null)
            {
                User assignee = (User) value;
                cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getId(), null, null, getUserKey(assignee), assignee.getDisplayName());
            }
        }
        else
        {
            if (!valuesEqual(value, currentValue))
            {
                User currentAssignee = (User) currentValue;
                if (value != null)
                {
                    User assignee = (User) value;
                    cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getId(), getUserKey(currentAssignee), currentAssignee.getDisplayName(), getUserKey(assignee), assignee.getDisplayName());
                }
                else
                {
                    cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getId(), getUserKey(currentAssignee), currentAssignee.getDisplayName(), null, null);
                }
            }
        }

        if (cib != null)
        {
            issueChangeHolder.addChangeItem(cib);
        }
    }

    private User getUser(String assigneeUsername)
    {
        if (assigneeUsername != null)
        {
            ApplicationUser user = userManager.getUserByName(assigneeUsername);
            if (user == null)
            {
                throw new DataAccessException("Error while retrieving user with name '" + assigneeUsername + "'.");
            }
            return user.getDirectoryUser();
        }
        else
        {
            return null;
        }
    }

    private boolean hasContext(OperationContext operationContext, Issue issue)
    {
        return issue != null;
    }

    private Collection<Issue> getBulkEditIssues(BulkEditBean bulkEditBean)
    {
        final Collection<Issue> issues;
        if (BulkMoveOperation.NAME.equals(bulkEditBean.getOperationName()))
        {
            //JRA-17011: When moving we wish to find the asignees in the target project not the source.
            issues = bulkEditBean.getTargetIssueObjects().values();
        }
        else
        {
            issues = bulkEditBean.getSelectedIssues();
        }
        return issues;
    }

    private ActionDescriptor extractActionDescriptor(OperationContext operationContext)
    {
        IssueOperation issueOperation = operationContext.getIssueOperation();
        if (issueOperation instanceof WorkflowIssueOperation)
        {
            return ((WorkflowIssueOperation) issueOperation).getActionDescriptor();
        }
        return null;
    }


    public boolean isShown(Issue issue)
    {
        return hasPermission(issue, Permissions.ASSIGN_ISSUE);
    }

    public void populateDefaults(Map<String, Object> fieldValuesHolder, Issue issue)
    {
        fieldValuesHolder.put(getId(), AUTOMATIC_ASSIGNEE);
    }

    public void populateFromIssue(Map<String, Object> fieldValuesHolder, Issue issue)
    {
        User assignee = issue.getAssignee();
        String assigneeName = (assignee != null) ? assignee.getName() : null;
        fieldValuesHolder.put(getId(), assigneeName);
    }

    protected Object getRelevantParams(Map<String, String[]> params)
    {
        String[] value = params.get(getId());
        if (value != null && value.length > 0 && TextUtils.stringSet(value[0]))
        {
            return value[0];
        }
        else
        {
            return null;
        }
    }

    /////////////////////////////////////////// Bulk Edit //////////////////////////////////////////////////////////
    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        // Ensure that this field is not hidden in any Field Layouts the selected issues belong to
        if (isHidden(bulkEditBean.getFieldLayouts()))
        {
            return "bulk.edit.unavailable.hidden";
        }

        // NOTE: Not checking the available assignees for the selected issues as if the user has the assign permission
        // the 'Automatic' option is always available.

        // Have to look through all the issues in case permission has been given to current assignee/reporter (i.e. role based)
        for (Issue issue : bulkEditBean.getSelectedIssues())
        {
            // If we got here then the field is visible in all field layouts
            // So check for permission
            if (!isShown(issue))
            {
                return "bulk.edit.unavailable.permission";
            }
        }

        // This field is available for bulk-editing, return null (i.e no unavailble message)
        return null;
    }

    public Object getValueFromParams(Map params)
    {
        return params.get(getId());
    }

    public void populateParamsFromString(Map<String, Object> fieldValuesHolder, String stringValue, Issue issue)
            throws FieldValidationException
    {
        String assignee = AUTOMATIC_ASSIGNEE_STRING.equals(stringValue) ? AUTOMATIC_ASSIGNEE : stringValue;
        fieldValuesHolder.put(getId(), assignee);
    }

    ///////////////////////// Navigable field implementation //////////////////////////////////////////////////////////
    public String getColumnHeadingKey()
    {
        return "issue.column.heading.assignee";
    }

    public String getDefaultSortOrder()
    {
        return NavigableField.ORDER_ASCENDING;
    }

    public LuceneFieldSorter getSorter()
    {
        return assigneeStatisticsMapper;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        String assigneeUserId = null;
        try
        {
            assigneeUserId = issue.getAssigneeId();
        }
        catch (DataAccessException e)
        {
            log.debug("Error occurred retrieving assignee", e);
        }

        final Map<String, Object> velocityParams = getVelocityParams(getAuthenticationContext().getI18nHelper(), displayParams, issue);
        if (assigneeUserId != null)
        {
            velocityParams.put("assigneeUserkey", assigneeUserId);
        }
        return renderTemplate("assignee-columnview.vm", velocityParams);
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        String autoCompleteUrl;
        if (fieldTypeInfoContext.getIssue() == null)
        {
            autoCompleteUrl = String.format("%s" + "/rest/api/latest/user/assignable/search" + "?project=%s&username=", jiraBaseUrls.baseUrl(), fieldTypeInfoContext.getIssueContext().getProjectObject().getKey());
        }
        else
        {
            autoCompleteUrl = String.format("%s" + "/rest/api/latest/user/assignable/search" + "?issueKey=%s&username=", jiraBaseUrls.baseUrl(), fieldTypeInfoContext.getIssue().getKey());
        }
        return new FieldTypeInfo(null, autoCompleteUrl);
    }

    @Override
    public JsonType getJsonSchema()
    {
        return JsonTypeBuilder.system(JsonType.USER_TYPE, IssueFieldConstants.ASSIGNEE);
    }

    @Override
    public RestFieldOperationsHandler getRestFieldOperation()
    {
        return new AssigneeRestFieldOperationsHandler(authenticationContext.getI18nHelper());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(Issue issue, boolean renderedVersionRequired, FieldLayoutItem fieldLayoutItem)
    {
        return new FieldJsonRepresentation(new JsonData(UserJsonBean.shortBean(issue.getAssignee(), jiraBaseUrls, authenticationContext.getUser(), emailFormatter)));
    }

    @Override
    public JsonData getJsonDefaultValue(IssueContext issueCtx)
    {
        return new JsonData(EasyMap.build("name", AUTOMATIC_ASSIGNEE));
    }

    private String getUserKey(User user)
    {
        if (user == null)
        {
            return "";
        }
        ApplicationUser applicationUser = userManager.getUserByName(user.getName());
        if (applicationUser == null)
        {
            return "";
        }
        return applicationUser.getKey();
    }
}
