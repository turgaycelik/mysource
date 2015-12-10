package com.atlassian.jira.issue.fields;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.bulkedit.operation.BulkMigrateOperation;
import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.option.FieldConfigSchemeOption;
import com.atlassian.jira.issue.fields.option.IssueConstantOption;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.issue.fields.option.TextOption;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.RestAwareField;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueTypeJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.handlers.IssueTypeSearchHandlerFactory;
import com.atlassian.jira.issue.statistics.IssueTypeStatisticsMapper;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.ofbiz.GenericValueUtils;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.opensymphony.util.TextUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class IssueTypeSystemField extends AbstractOrderableNavigableFieldImpl implements IssueTypeField, RestAwareField
{
    private static final Logger log = Logger.getLogger(IssueTypeSystemField.class);

    private static final String ISSUE_TYPE_NAME_KEY = "issue.field.issuetype";

    private final ConstantsManager constantsManager;
    private final WorkflowManager workflowManager;
    private final IssueTypeStatisticsMapper issueTypeStatisticsMapper;
    private final OptionSetManager optionSetManager;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final JiraBaseUrls jiraBaseUrls;

    public IssueTypeSystemField(VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext,
            ConstantsManager constantsManager, WorkflowManager workflowManager, PermissionManager permissionManager, IssueTypeStatisticsMapper issueTypeStatisticsMapper,
            OptionSetManager optionSetManager, IssueTypeSchemeManager issueTypeSchemeManager,
            IssueTypeSearchHandlerFactory searchHandlerFactory, JiraBaseUrls jiraBaseUrls)
    {
        super(IssueFieldConstants.ISSUE_TYPE, ISSUE_TYPE_NAME_KEY, templatingEngine, applicationProperties, authenticationContext, permissionManager, searchHandlerFactory);
        this.constantsManager = constantsManager;
        this.workflowManager = workflowManager;
        this.issueTypeStatisticsMapper = issueTypeStatisticsMapper;
        this.optionSetManager = optionSetManager;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.jiraBaseUrls = jiraBaseUrls;
    }

    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        final Map<String, Object> velocityParams = prepareVelocityParams(fieldLayoutItem, action, issue, displayParameters, operationContext);

        populateOptionsForProjects(velocityParams, issue, displayParameters, issue.isSubTask());

        velocityParams.put("createOperation", Boolean.TRUE);

        return renderTemplate("issuetype-edit.vm", velocityParams);
    }

    private List populateOptionsForProjects(Map velocityParams, Issue issue, Map displayParameters, boolean isSubTask)
    {
        // Get the available projects
        ProjectSystemField projectField = (ProjectSystemField) getParentField();
        Collection<Project> allowedProjects;
        if (isSubTask && issue != null)
        {
            // For sub tasks, only allow the current project
            allowedProjects = Arrays.asList(issue.getProjectObject());
        }
        else
        {
            allowedProjects = projectField.getAllowedProjects();
        }

        Map projectToConfig = new ListOrderedMap();
        Set<FieldConfigScheme> releventConfigs = new HashSet<FieldConfigScheme>();
        for (final Project project : allowedProjects)
        {
            FieldConfigScheme configScheme = issueTypeSchemeManager.getConfigScheme(project);
            FieldConfig relevantConfig = configScheme.getOneAndOnlyConfig();

            releventConfigs.add(configScheme);
            projectToConfig.put(project.getId(), relevantConfig.getId());
        }
        velocityParams.put("projectToConfig", projectToConfig);


        // Get all options for the config
        List configOptions = new ArrayList();
        for (final FieldConfigScheme configScheme : releventConfigs)
        {
            FieldConfig config = configScheme.getOneAndOnlyConfig();
            List options = new ArrayList(getOptionsForConfig(config, issue, displayParameters, isSubTask));

            if (!options.isEmpty())
            {
                if (!isSubTask && issueTypeSchemeManager.getDefaultValue(config) == null)
                {
                    // If no default then add a please select
                    TextOption pleaseSelect = new TextOption("", authenticationContext.getI18nHelper().getText("common.words.pleaseselect"));
                    options.add(0, pleaseSelect);
                }

                configOptions.add(new FieldConfigSchemeOption(configScheme, options));
            }
        }
        velocityParams.put("configOptions", configOptions);


        // Set all the different defaults
        if (!isSubTask)
        {
            Map configToDefaultOption = new ListOrderedMap();
            for (final FieldConfigScheme configScheme : releventConfigs)
            {
                FieldConfig config = configScheme.getOneAndOnlyConfig();
                IssueType defaultValue = issueTypeSchemeManager.getDefaultValue(config);
                configToDefaultOption.put(config.getId(), defaultValue != null ? defaultValue.getId() : "");
            }

            if (configToDefaultOption.size() > 1)
            {
                velocityParams.put("configToDefaultOption", configToDefaultOption);
            }
            else
            {
                // There's only one config, so set the value as an issue type, if nothing's been selected before
                if (velocityParams.get(getId()) == null)
                {
                    velocityParams.put(getId(), configToDefaultOption.values().iterator().next());
                }
            }
        }
        return configOptions;
    }


    private boolean isMoveIssue(Map displayParameters, IssueContext issue)
    {
        return displayParameters.containsKey(MOVE_ISSUE_PARAM_KEY) && Boolean.TRUE.equals(displayParameters.get(MOVE_ISSUE_PARAM_KEY)) && issue != null;
    }

    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = prepareVelocityParams(fieldLayoutItem, action, issue, displayParameters, operationContext);

        // Get all options for the config
        List configOptions = new ArrayList();
        FieldConfigScheme configScheme = issueTypeSchemeManager.getConfigScheme(issue.getProject());
        FieldConfig relevantConfig = configScheme.getOneAndOnlyConfig();
        Collection options = getOptionsForConfig(relevantConfig, issue, displayParameters);
        options = CollectionUtils.select(options, new ValidForEditIssueTypes(issue, displayParameters));

        if (options.size() > 1)
        {
            configOptions.add(new FieldConfigSchemeOption(configScheme, options));
            velocityParams.put("configOptions", configOptions);
            return renderTemplate("issuetype-edit.vm", velocityParams);

        }
        else
        {
            velocityParams.put("noAllowedIssueTypes", Boolean.TRUE);
            velocityParams.put("hasMovePermission", Boolean.valueOf(userHasMovePermission(issue)));
            velocityParams.put("issue", issue);
            return renderTemplate("issuetype-edit-not-allowed.vm", velocityParams);
        }
    }

    @VisibleForTesting
    boolean userHasMovePermission(@Nonnull Issue issue)
    {
        return getPermissionManager().hasPermission(Permissions.MOVE_ISSUE, issue, getAuthenticationContext().getUser());
    }

    public String getBulkEditHtml(OperationContext operationContext, Action action, BulkEditBean bulkEditBean, Map displayParameters)
    {
        Map velocityParams = prepareVelocityParams(null, action, null, displayParameters, operationContext);

        // Bulk Move - determine collection of possible target issue types
        if (BulkMoveOperation.NAME.equals(bulkEditBean.getOperationName()) ||
            BulkMigrateOperation.OPERATION_NAME.equals(bulkEditBean.getOperationName()))
        {
            populateOptionsForProjects(velocityParams, null, displayParameters, bulkEditBean.isSubTaskCollection());
        }
        else
        {
            // Bulk editing
            Collection options = getAllowedIssueTypeOptionsForEdit(bulkEditBean.getSelectedIssues(), displayParameters);
            velocityParams.put("configOptions", EasyList.build(new FieldConfigSchemeOption(null, options)));
        }

        return renderTemplate("issuetype-edit.vm", velocityParams);
    }

    public String getEditHtml(OperationContext operationContext, Action action, List options)
    {
        Map velocityParams = prepareVelocityParams(null, action, null, EasyMap.build(), operationContext);
        velocityParams.put("configOptions", EasyList.build(new FieldConfigSchemeOption(null, options)));
        return renderTemplate("issuetype-edit.vm", velocityParams);
    }


    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        // Create issue type object
        IssueType issueType = issue.getIssueTypeObject();
        velocityParams.put("issueTypeObject", issueType);
        return getViewHtml(velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Object value, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, null, displayParameters);
        // Create issue type object
        GenericValue issueTypeGV = (GenericValue) value;
        IssueType issueType = constantsManager.getIssueTypeObject(issueTypeGV.getString("id"));
        velocityParams.put("issueTypeObject", issueType);
        return getViewHtml(velocityParams);
    }

    private String getViewHtml(Map velocityParams)
    {
        return renderTemplate("issuetype-view.vm", velocityParams);
    }

    public boolean isShown(Issue issue)
    {
        return true;
    }

    public void populateDefaults(Map<String, Object> fieldValuesHolder, Issue issue)
    {
        IssueType issueType = (IssueType) getDefaultValue(issue);
        if (issueType != null)
        {
            fieldValuesHolder.put(getId(), issueType.getId());
        }
    }

    public Object getDefaultValue(Issue issue)
    {
        return issueTypeSchemeManager.getDefaultValue(issue);
    }

    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        issue.setIssueType((GenericValue) getValueFromParams(fieldValueHolder));
    }

    public MessagedResult needsMove(Collection originalIssues, Issue targetIssue, FieldLayoutItem targetFieldLayoutItem)
    {
        return new MessagedResult(true);
    }

    public void populateForMove(Map<String, Object> fieldValuesHolder, Issue originalIssue, Issue targetIssue)
    {
        // Issue type should be updated on the first screen of the Move Issue wizard
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
        return (issue.getIssueTypeObject() != null);
    }

    /**
     * validate the field value
     *
     * @param operationContext
     * @param errorCollectionToAddTo
     * @param fieldScreenRenderLayoutItem
     */
    public void validateParams(OperationContext operationContext, ErrorCollection errorCollectionToAddTo, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        Map fieldValuesHolder = operationContext.getFieldValuesHolder();
        String issueTypeId = (String) fieldValuesHolder.get(getId());

        // Check that the issue type with the given id exists.
        if (issueTypeId == null)
        {
            errorCollectionToAddTo.addError(getId(), i18n.getText("createissue.noissuetype"), Reason.VALIDATION_FAILED);
        }
        else if (getValueFromParams(fieldValuesHolder) == null)
        {
            errorCollectionToAddTo.addError(getId(), i18n.getText("createissue.invalidissuetype"), Reason.VALIDATION_FAILED);
        }
        else
        {
            IssueType issueTypeObject = constantsManager.getIssueTypeObject(issueTypeId);
            // Check if the issue type is a subTask type then a parent must be specified & vice-versa
            if (issueTypeObject.isSubTask() && !issue.isSubTask())
            {
                errorCollectionToAddTo.addError(getId(), i18n.getText("createsubtask.error.noparentissuekey"), Reason.VALIDATION_FAILED);
            }
            else if (!issueTypeObject.isSubTask() && issue.isSubTask())
            {
                errorCollectionToAddTo.addError(getId(), i18n.getText("createsubtask.error.issuetypenotsubtask", issueTypeId), Reason.VALIDATION_FAILED);
            }
            // "issue.getParentObject() != null" is dirty hack because of:
            // com.atlassian.jira.bulkedit.operation.BulkMoveOperationImpl.chooseContext(BulkEditBean, ApplicationUser, I18nHelper, ErrorCollection)()
            //            // Set the parent id of subtask object in order to validate issue type selection
            //            if (bulkEditBean.isSubTaskCollection())
            //            {
            //                // This is actually some BS hack that happens to work... Basically trick the issue object into thinking it's a subTask
            //                issueObject.setParentId(new Long(0));
            //            }
            else if (issueTypeObject.isSubTask() && issue.getParentObject() != null && issue.getParentObject().isSubTask())
            {
                errorCollectionToAddTo.addError(getId(), i18n.getText("convert.issue.to.subtask.error.parentissubtask",
                        String.format("ID: '%s' / Key: '%s'", issue.getParentObject().getId(), issue.getParentObject().getKey())));
            }
            else
            {
                // Validate that the issue type is a valid option for the project that we are in.
                Collection allowedIssueTypeOptions = getOptionsForIssue(issue);
                if (!allowedIssueTypeOptions.contains(new IssueConstantOption(issueTypeObject)))
                {
                    errorCollectionToAddTo.addError(getId(), i18n.getText("createissue.invalidissuetype"), Reason.VALIDATION_FAILED);
                }
            }
        }
    }


    public Object getValueFromParams(Map params)
    {
        String issueTypeId = (String) params.get(getId());

        if (TextUtils.stringSet(issueTypeId))
        {
            return constantsManager.getIssueType(issueTypeId);
        }
        else
        {
            return null;
        }
    }

    public void populateParamsFromString(Map<String, Object> fieldValuesHolder, String stringValue, Issue issue) throws FieldValidationException
    {
        Long issuetypeId = null;
        try
        {
            // Check if the issue type is a number
            issuetypeId = Long.valueOf(stringValue);
        }
        catch (NumberFormatException e)
        {
            // If not, try to convert to a number
            issuetypeId = getIssueTypeIdByName(stringValue);
        }

        // Yes, issue type id is a String, even though it is actually a number.
        // Ahh, the joy of backwards compatibility
        fieldValuesHolder.put(getId(), issuetypeId.toString());
    }

    private Long getIssueTypeIdByName(String stringValue) throws FieldValidationException
    {
        for (GenericValue issueTypeGV : constantsManager.getAllIssueTypes())
        {
            if (stringValue.equalsIgnoreCase(issueTypeGV.getString("name")))
            {
                return Long.valueOf(issueTypeGV.getString("id"));
            }
        }

        throw new FieldValidationException("Invalid issue type name '" + stringValue + "'.");
    }

    public void createValue(Issue issue, Object value)
    {
        // The field is recorded on the issue itself so there is nothing to do
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
                GenericValue issueType = (GenericValue) value;
                cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getId(), null, null, issueType.getString("id"), issueType.getString("name"));
            }
        }
        else
        {
            if (!valuesEqual(value, currentValue))
            {
                GenericValue currentIssueType = (GenericValue) currentValue;
                if (value != null)
                {
                    GenericValue issueType = (GenericValue) value;
                    cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getId(), currentIssueType.getString("id"), currentIssueType.getString("name"), issueType.getString("id"), issueType.getString("name"));
                }
                else
                {
                    cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getId(), currentIssueType.getString("id"), currentIssueType.getString("name"), null, null);
                }
            }
        }

        if (cib != null)
        {
            issueChangeHolder.addChangeItem(cib);
        }
    }

    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        // Issue type is not possible to hide as it is always required. So no need to check the field layouts

        // Have to look through all the issues in case permission has been given to current assignee/reporter (i.e. role based)
        List<Issue> selectedIssues = bulkEditBean.getSelectedIssues();
        for (final Issue issue : selectedIssues)
        {
            // If we got here then the field is visible in all field layouts
            // So check for permission
            if (!hasBulkUpdatePermission(bulkEditBean, issue) || !isShown(issue))
            {
                return "bulk.edit.multiproject.unavailable.permission";
            }
        }

        // Need to ensure that the list of available issue is not empty
        if (isHasCommonIssueTypes(selectedIssues))
        {
            return "bulk.edit.issuetype.noissuetypes";
        }

        return null;
    }

    /**
     * This method will determine if there are ANY issue type that the selectedIssues
     * have in common. This takes into account the possible difference in workflow or
     * field configuration for each issue type.
     * @param selectedIssues
     * @return true if there are issue types in common, false otherwise
     */
    public boolean isHasCommonIssueTypes(Collection selectedIssues)
    {
        return getAllowedIssueTypeOptionsForEdit(selectedIssues, new HashMap()).isEmpty();
    }

    private Collection getAllowedIssueTypeOptionsForEdit(Collection issues, Map displayParameters)
    {
        Iterator iterator = issues.iterator();
        Issue issue = (Issue) iterator.next();
        Collection availableIssueTypes = CollectionUtils.select(new ArrayList(getOptionsForIssue(issue)),
                new ValidForEditIssueTypes(issue, displayParameters));

        while (!availableIssueTypes.isEmpty() && iterator.hasNext())
        {
            issue = (Issue) iterator.next();
            Collection newOptions = getOptionsForIssue(issue);

            // Cull the list by removing any options that do not match the original workflow or field config
            newOptions = CollectionUtils.select(newOptions,
                    new ValidForEditIssueTypes(issue, displayParameters));

            // Work out the intersection between the two collections
            availableIssueTypes.retainAll(newOptions);
        }
        return availableIssueTypes;
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

    public void populateFromIssue(Map<String, Object> fieldValuesHolder, Issue issue)
    {
        fieldValuesHolder.put(getId(), issue.getIssueTypeObject().getId());
    }

    private FieldLayoutManager getFieldLayoutManager()
    {
        return ComponentAccessor.getFieldLayoutManager();
    }

    public String getColumnHeadingKey()
    {
        return "issue.column.heading.issuetype";
    }

    public String getDefaultSortOrder()
    {
        return ORDER_DESCENDING;
    }

    public LuceneFieldSorter getSorter()
    {
        return issueTypeStatisticsMapper;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map<String, Object> velocityParams = getVelocityParams(getAuthenticationContext().getI18nHelper(), displayParams, issue);
        velocityParams.put("issue", issue);
        // Create issue type object
        IssueType issueType = issue.getIssueTypeObject();
        velocityParams.put(getId(), issueType);
        return renderTemplate("issuetype-columnview.vm", velocityParams);
    }

    public Collection<Option> getOptionsForIssue(Issue issue, boolean isSubTask)
    {
        FieldConfig relevantConfig = getRelevantConfig(issue);
        return getOptionsForConfig(relevantConfig, issue, EasyMap.build(), isSubTask);
    }
    // -------------------------------------------------------------------------------------------------- Config methods


    public List getConfigurationItemTypes()
    {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<GenericValue> getAssociatedProjects()
    {
        return ComponentAccessor.getComponent(FieldConfigSchemeManager.class).getAssociatedProjects(this);
    }

    @Override
    public List<Project> getAssociatedProjectObjects()
    {
        return ComponentAccessor.getComponent(FieldConfigSchemeManager.class).getAssociatedProjectObjects(this);
    }

    public FieldConfig getRelevantConfig(IssueContext issueContext)
    {
        return ComponentAccessor.getComponent(FieldConfigSchemeManager.class).getRelevantConfig(issueContext, this);
    }

    public Field getParentField()
    {
        return ComponentAccessor.getFieldManager().getField(IssueFieldConstants.PROJECT);
    }

    private Collection getOptionsForIssue(Issue issue)
    {
        FieldConfig relevantConfig = getRelevantConfig(issue);
        return getOptionsForConfig(relevantConfig, issue, EasyMap.build());
    }

    private Collection getOptionsForConfig(FieldConfig fieldConfig, Issue issue, Map displayParameters)
    {
        return getOptionsForConfig(fieldConfig, issue, displayParameters, issue.isSubTask());
    }

    private Collection getOptionsForConfig(FieldConfig fieldConfig, IssueContext issue, Map displayParameters, boolean isSubTask)
    {
        Collection options = optionSetManager.getOptionsForConfig(fieldConfig).getOptions();
        options = CollectionUtils.select(options, new ValidIssueTypePredicate(issue, displayParameters, isSubTask));
        return options;
    }

    private Map prepareVelocityParams(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters, OperationContext operationContext)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        String issueTypeId = (String) operationContext.getFieldValuesHolder().get(getId());
        velocityParams.put(getId(), issueTypeId);
        if (issueTypeId != null && constantsManager.getIssueType(issueTypeId) != null)
        {
            velocityParams.put("issueTypeObject", constantsManager.getIssueTypeObject(issueTypeId));
        }
        return velocityParams;
    }

    public Collection getIssueConstants()
    {
        return constantsManager.getAllIssueTypeObjects();
    }


    private class ValidIssueTypePredicate implements Predicate
    {
        private final IssueConstantOption currentIssuesOption;
        private final IssueContext issue;
        private final Map displayParameters;
        private final Collection subTaskIds = GenericValueUtils.transformToStringIdsList(constantsManager.getSubTaskIssueTypes());
        private boolean subTaskOnly = false;

        public ValidIssueTypePredicate(IssueContext issue, Map displayParameters)
        {
            this.issue = issue;
            this.displayParameters = displayParameters;
            currentIssuesOption = issue != null ? new IssueConstantOption(issue.getIssueTypeObject()) : null;
        }

        public ValidIssueTypePredicate(IssueContext issue, Map displayParameters, boolean isSubTask)
        {
            this(issue, displayParameters);
            this.subTaskOnly = isSubTask;
        }


        public boolean evaluate(Object object)
        {
            Option option = (Option) object;
            if (!isCorrectType(option))
            {
                return false;
            }
            if (isMoveIssue(displayParameters, issue) && currentIssuesOption.equals(option))
            {
                return false;
            }
            else
            {
                // If issue has not been created or we are not using the enterprise edition then simply return the possible issue types
                return true;
            }
        }

        private boolean isCorrectType(Option option)
        {
            if (subTaskOnly)
            {
                return subTaskIds.contains(option.getId());
            }
            else
            {
                return !subTaskIds.contains(option.getId());
            }
        }
    }

    /**
     * This predicate is used as a filter on a list of com.atlassian.jira.issue.fields.option.Option objects.
     * The issue provided in the constructor is used as the base value (the value before the change). We use
     * the project and issue type from the original issue and then we find the field layout and the workflow
     * for the project from the issue and the issue type specified by the option. If the field layout or
     * the workflow are different from the original then we will not include that issue type option in the
     * list (we filter it out).
     */
    private class ValidForEditIssueTypes implements Predicate
    {
        FieldLayout currentFieldLayout;
        JiraWorkflow currentWorkflow;
        private final IssueContext issue;
        private final Map displayParameters;

        public ValidForEditIssueTypes(IssueContext issue, Map displayParameters)
        {
            this.issue = issue;
            this.displayParameters = displayParameters;
        }


        public boolean evaluate(Object object)
        {
            Option option = (Option) object;

            try
            {
                if (currentFieldLayout == null || currentWorkflow == null)
                {
                    currentFieldLayout = getFieldLayoutManager().getFieldLayout(issue.getProjectObject(),
                                                                                issue.getIssueTypeObject() != null ? issue.getIssueTypeObject().getId() : null);
                    currentWorkflow = workflowManager.getWorkflow(issue.getProjectObject().getId(),
                                                                  issue.getIssueTypeObject() != null ? issue.getIssueTypeObject().getId() : null);

                }

                // Otherwise, return all issue types that an issue can change to without causing 'problems', that is all issue types that have the same:
                // 1. Field Layout
                // 2. Workflow
                // as the original issue type.

                FieldLayout fieldLayoutScheme = getFieldLayoutManager().getFieldLayout(issue.getProjectObject(), option.getId());
                if (!currentFieldLayout.equals(fieldLayoutScheme))
                {
                    // This parameter is used by the view - issuetype-edit.vm which will display a message to the user
                    // that an incompatable issue type has been discarded.
                    displayParameters.put("restrictedSelection", Boolean.TRUE);
                    return false;
                }

                // No need to check current issue type workflow
                if (!issue.getIssueTypeObject().getId().equals(option.getId()))
                {
                    JiraWorkflow workflow = workflowManager.getWorkflow(issue.getProjectObject().getId(), option.getId());
                    if (!currentWorkflow.equals(workflow))
                    {
                        // This parameter is used by the view - issuetype-edit.vm which will display a message to the user
                        // that an incompatable issue type has been discarded.
                        displayParameters.put("restrictedSelection", Boolean.TRUE);
                        return false;
                    }
                }

                return true;
            }
            catch (WorkflowException e)
            {
                throw new DataAccessException(e);
            }
        }
    }

    /**
     * Return an internationalized value for the changeHistory item - an issue type name in this case.
     *
     * @param changeHistory     name of issue type
     * @param i18nHelper        used to translate the issue type name
     * @return String
     */
    public String prettyPrintChangeHistory(String changeHistory, I18nHelper i18nHelper)
    {
        if (TextUtils.stringSet(changeHistory))
        {
            Long issueTypeId = getIssueTypeIdByName(changeHistory);

            if (issueTypeId != null)
            {
                IssueType issueType = constantsManager.getIssueTypeObject(issueTypeId.toString());
                if (issueType != null)
                {
                    return issueType.getNameTranslation(i18nHelper);
                }
            }
        }
        // Otherwise - return the original string
        return changeHistory;
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        // Get all options for the config
        FieldConfigScheme configScheme = issueTypeSchemeManager.getConfigScheme(fieldTypeInfoContext.getIssueContext().getProject());
        FieldConfig relevantConfig = configScheme.getOneAndOnlyConfig();
        Collection<IssueType> allowedTypes = new ArrayList<IssueType>();
        if (fieldTypeInfoContext.getOperationContext().getIssueOperation().equals(IssueOperations.CREATE_ISSUE_OPERATION))
        {
            allowedTypes.add(fieldTypeInfoContext.getIssueContext().getIssueTypeObject());
        }
        else
        {
            Collection options;
            options = getOptionsForConfig(relevantConfig, fieldTypeInfoContext.getIssueContext(), new HashMap(), false);
            options = CollectionUtils.select(options, new ValidForEditIssueTypes(fieldTypeInfoContext.getIssueContext(), new HashMap()));
            allowedTypes = Collections2.transform(options, new Function<Object, IssueType>()
            {
                @Override
                public IssueType apply(@Nullable Object from)
                {
                    return constantsManager.getIssueTypeObject(((Option) from).getId());
                }
            });
        }

        return new FieldTypeInfo(allowedTypes, null);
    }

    @Override
    public JsonType getJsonSchema()
    {
        return getIssueTypeJsonSchema();
    }

    static public JsonType getIssueTypeJsonSchema()
    {
        return JsonTypeBuilder.system(JsonType.ISSUETYPE_TYPE, IssueFieldConstants.ISSUE_TYPE);
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(Issue issue, boolean renderedVersionRequired, FieldLayoutItem fieldLayoutItem)
    {
        return new FieldJsonRepresentation(new JsonData(IssueTypeJsonBean.shortBean(issue.getIssueTypeObject(), jiraBaseUrls)));
    }
}
