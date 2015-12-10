package com.atlassian.jira.issue.fields;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
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
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.ReporterRestFieldOperationsHandler;
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
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.handlers.ReporterSearchHandlerFactory;
import com.atlassian.jira.issue.statistics.ReporterStatisticsMapper;
import com.atlassian.jira.issue.util.IssueChangeHolder;
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
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;
import webwork.action.Action;

import java.util.Collection;
import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class ReporterSystemField extends AbstractUserFieldImpl implements HideableField, RequirableField, RestAwareField, RestFieldOperations
{
    private static final Logger log = Logger.getLogger(ReporterSystemField.class);
    private static final String REPORTER_NAME_KEY = "issue.field.reporter";

    private final ReporterStatisticsMapper reporterStatisticsMapper;
    private UserPickerSearchService searchService;
    private ApplicationProperties applicationProperties;
    private final JiraBaseUrls jiraBaseUrls;
    private final FeatureManager featureManager;
    private final AvatarService avatarService;
    private final UserManager userManager;
    private final EmailFormatter emailFormatter;

    public ReporterSystemField(VelocityTemplatingEngine templatingEngine, PermissionManager permissionManager,
            ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext,
            ReporterStatisticsMapper reporterStatisticsMapper, UserPickerSearchService searchService,
            ReporterSearchHandlerFactory reporterSearchHandlerFactory, JiraBaseUrls jiraBaseUrls,
            FeatureManager featureManager, AvatarService avatarService, UserManager userManager,
            UserHistoryManager userHistoryManager, final EmailFormatter emailFormatter)
    {
        super(IssueFieldConstants.REPORTER, REPORTER_NAME_KEY, templatingEngine, applicationProperties, authenticationContext, permissionManager, reporterSearchHandlerFactory, userHistoryManager);
        this.reporterStatisticsMapper = reporterStatisticsMapper;
        this.searchService = searchService;
        this.applicationProperties = applicationProperties;
        this.jiraBaseUrls = jiraBaseUrls;
        this.featureManager = featureManager;
        this.avatarService = avatarService;
        this.userManager = userManager;
        this.emailFormatter = emailFormatter;
    }

    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        return getEditHtml(fieldLayoutItem, operationContext, action, issue, displayParameters);
    }

    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        String reporterName = (String) operationContext.getFieldValuesHolder().get(getId());
        velocityParams.put(getId(), reporterName);
        velocityParams.put("hasPermissionPickUsers", Boolean.valueOf(getPermissionManager().hasPermission(Permissions.USER_PICKER, getAuthenticationContext().getLoggedInUser())));

        final User reporterUser = userManager.getUser(reporterName);
        final User loggedInUser = authenticationContext.getLoggedInUser();
        if (reporterUser != null)
        {
            final String avatarUrl =  avatarService.getAvatarURL(loggedInUser, reporterName, Avatar.Size.SMALL).toString();
            velocityParams.put("reporter", reporterUser.getName());
            velocityParams.put("reporterDisplayName", reporterUser.getDisplayName());
            velocityParams.put("reporterAvatarUrl", avatarUrl);
        }

        JiraServiceContext ctx = new JiraServiceContextImpl(loggedInUser);

        boolean canPerformAjaxSearch = searchService.canPerformAjaxSearch(ctx);
        if (canPerformAjaxSearch)
        {
            velocityParams.put("canPerformAjaxSearch", "true");
            velocityParams.put("ajaxLimit", applicationProperties.getDefaultBackedString(APKeys.JIRA_AJAX_AUTOCOMPLETE_LIMIT));
        }
        WebResourceManager webResourceManager = ComponentAccessor.getWebResourceManager();
        webResourceManager.requireResource("jira.webresources:autocomplete");

        String template = useFrotherControl() ? "reporter-edit-frother.vm" : "reporter-edit.vm";
        return renderTemplate(template, velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        velocityParams.put("reporter", issue.getReporter());
        return getViewHtml(velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Object value, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, null, displayParameters);
        velocityParams.put("reporter", value);
        return getViewHtml(velocityParams);
    }

    private boolean useFrotherControl()
    {
        return featureManager.isEnabled("jira.frother.reporter.field") && !featureManager.isEnabled("jira.no.frother.reporter.field");
    }

    private String getViewHtml(Map velocityParams)
    {
        return renderTemplate("reporter-view.vm", velocityParams);
    }

    public boolean isShown(Issue issue)
    {
        return hasPermission(issue, Permissions.MODIFY_REPORTER);
    }

    public void validateParams(OperationContext operationContext, ErrorCollection errorCollectionToAddTo, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        Map fieldValuesHolder = operationContext.getFieldValuesHolder();
        // If the value has been entered into the parameters the user must have permission to edit the reporter
        String returnedReporter = (String) fieldValuesHolder.get(getId());
        if (TextUtils.stringSet(returnedReporter))
        {
            // If the username has actually been given then check that the user actually exists
            // Should we check that the user has CREATE ISSUE permission in the project?
            User newReporter = UserUtils.getUser(returnedReporter);
            if (newReporter == null)
            {
                errorCollectionToAddTo.addError(getId(), i18n.getText("createissue.error.reporter.does.not.exist"));
                return;
            }
            // User exists, but are they active?
            if (!newReporter.isActive())
            {
                // Inactive user - allow this only if the value is not being changed
                if (!returnedReporter.equals(issue.getReporterId()))
                {
                    // I am just re-reusing the "not a user" message which is logically correct and doesn't leak information.
                    errorCollectionToAddTo.addError(getId(), i18n.getText("createissue.error.reporter.does.not.exist"));
                    return;
                }
            }
        }

        if (fieldScreenRenderLayoutItem.isRequired() && !TextUtils.stringSet(returnedReporter))
        {
            errorCollectionToAddTo.addError(getId(), i18n.getText("issue.field.required", i18n.getText(getNameKey())));
        }
    }

    public Object getDefaultValue(Issue issue)
    {
        // The default value that should be set if the user cannot modify this field is the remote user's name
        return getAuthenticationContext().getLoggedInUser();
    }

    public void createValue(Issue issue, Object value)
    {
        // The field is recorded on the issue itself so there is nothing to do
    }

    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        if (fieldValueHolder.containsKey(getId()))
        {
            User reporter = (User) getValueFromParams(fieldValueHolder);
            issue.setReporter(reporter);
            addToUsedUserHistoryIfValueChanged(issue);
        }
    }

    public MessagedResult needsMove(Collection originalIssues, Issue targetIssue, FieldLayoutItem targetFieldLayoutItem)
    {
        // JRA-9604: We never want to force update the Reporter field on a Bulk Move because this will overwrite all the
        // issues, even the "good" ones.
        return new MessagedResult(false);
    }

    public void populateForMove(Map<String, Object> fieldValuesHolder, Issue originalIssue, Issue targetIssue)
    {
        // Default to the reporter of the original issue  - JRA-7152
        User originalReporter = originalIssue.getReporterUser();
        if (originalReporter != null)
            fieldValuesHolder.put(getId(), originalReporter.getName());
    }

    public void removeValueFromIssueObject(MutableIssue issue)
    {
        issue.setReporter(null);
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return true;
    }

    public boolean hasValue(Issue issue)
    {
        return (issue.getReporter() != null);
    }

    public void updateValue(FieldLayoutItem fieldLayoutItem, Issue issue, ModifiedValue modifiedValue, IssueChangeHolder issueChangeHolder)
    {
        Object currentValue = modifiedValue.getOldValue();
        Object value = modifiedValue.getNewValue();
        ChangeItemBean cib = null;

        if (currentValue == null)
        {
            if (value != null)
            {
                User reporter = (User) value;
                cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getId(), null, null, getUserKey(reporter), reporter.getDisplayName());
            }
        }
        else
        {
            if (!valuesEqual(value, currentValue))
            {
                User currentReporter = (User) currentValue;
                if (value != null)
                {
                    User reporter = (User) value;
                    cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getId(), getUserKey(currentReporter), currentReporter.getDisplayName(), getUserKey(reporter), reporter.getDisplayName());
                }
                else
                {
                    cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getId(), getUserKey(currentReporter), currentReporter.getDisplayName(), null, null);
                }
            }
        }

        if (cib != null)
            issueChangeHolder.addChangeItem(cib);
    }

    public Object getValueFromParams(Map params)
    {
        String username = (String) params.get(getId());

        if (TextUtils.stringSet(username))
        {
            // Retrieve the selected user
            return UserUtils.getUser(username);
        }
        else
        {
            // If no username has been given resort to no reporter
            return null;
        }
    }

    public void populateParamsFromString(Map<String, Object> fieldValuesHolder, String stringValue, Issue issue) throws FieldValidationException
    {
        fieldValuesHolder.put(getId(), stringValue);
    }

    public void populateFromIssue(Map<String, Object> fieldValuesHolder, Issue issue)
    {
        final User reporter = issue.getReporterUser();
        if (reporter != null)
        {
            fieldValuesHolder.put(getId(), reporter.getName());
        }
    }

    public void populateDefaults(Map<String, Object> fieldValuesHolder, Issue issue)
    {
        User remoteUser = (User) getDefaultValue(issue);
        if (remoteUser != null)
            fieldValuesHolder.put(getId(), remoteUser.getName());
    }

    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        // Ensure that this field is not hidden in any Field Layouts the selected issues belong to
        if (isHidden(bulkEditBean.getFieldLayouts()))
        {
            return "bulk.edit.unavailable.hidden";
        }

        // Have to look through all the issues in case permission has been given to current assignee/reporter (i.e. role based)
        for (Issue issue : bulkEditBean.getSelectedIssues())
        {
            // If we got here then the field is visible in all field layouts
            // So check for permission
            if (!hasBulkUpdatePermission(bulkEditBean, issue) || !isShown(issue))
            {
                return "bulk.edit.multiproject.unavailable.permission";
            }
        }

        // This field is available for bulk-editing, return null (i.e no unavailble message)
        return null;
    }


    public String getColumnHeadingKey()
    {
        return "issue.column.heading.reporter";
    }

    public String getDefaultSortOrder()
    {
        return NavigableField.ORDER_ASCENDING;
    }

    public LuceneFieldSorter getSorter()
    {
        return reporterStatisticsMapper;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        final Map<String, Object> velocityParams = getVelocityParams(getAuthenticationContext().getI18nHelper(), displayParams, issue);
        try
        {
            final String reporterUserId = issue.getReporterId();
            if (reporterUserId != null)
            {
                velocityParams.put("reporterUserkey", reporterUserId);
            }
        }
        catch (DataAccessException e)
        {
            log.debug("Error occurred retrieving reporter", e);
        }
        return renderTemplate("reporter-columnview.vm", velocityParams);
    }

    protected Object getRelevantParams(Map<String, String[]> params)
    {
        String[] value = params.get(getId());
        if (value != null && value.length > 0)
        {
            return value[0];
        }
        else
        {
            return null;
        }
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        String autoCompleteUrl = String.format("%s/rest/api/latest/user/search?username=", jiraBaseUrls.baseUrl());
        return new FieldTypeInfo(null, autoCompleteUrl);
    }

    @Override
    public JsonType getJsonSchema()
    {
        return JsonTypeBuilder.system(JsonType.USER_TYPE, IssueFieldConstants.REPORTER);
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(Issue issue, boolean renderedVersionRequired, FieldLayoutItem fieldLayoutItem)
    {
        return new FieldJsonRepresentation(new JsonData(UserJsonBean.shortBean(issue.getReporter(), jiraBaseUrls, authenticationContext.getUser(), emailFormatter)));
    }

    @Override
    public RestFieldOperationsHandler getRestFieldOperation()
    {
        return new ReporterRestFieldOperationsHandler(authenticationContext.getI18nHelper());
    }

    @Override
    public JsonData getJsonDefaultValue(IssueContext issueCtx)
    {
        User user = getAuthenticationContext().getLoggedInUser();
        if (user == null)
        {
            return new JsonData(null);
        }
        return new JsonData(UserJsonBean.shortBean(user, jiraBaseUrls, authenticationContext.getUser(), emailFormatter));
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
