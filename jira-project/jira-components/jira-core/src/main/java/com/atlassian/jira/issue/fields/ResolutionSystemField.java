package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.ResolutionRestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.RestAwareField;
import com.atlassian.jira.issue.fields.rest.RestFieldOperations;
import com.atlassian.jira.issue.fields.rest.RestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.ResolutionJsonBean;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.operation.WorkflowIssueOperation;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.handlers.ResolutionSearchHandlerFactory;
import com.atlassian.jira.issue.statistics.ResolutionStatisticsMapper;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResolutionSystemField extends AbstractOrderableNavigableFieldImpl
        implements HideableField, IssueConstantsField, RestAwareField, RestFieldOperations
{
    public static final Long UNRESOLVED_VALUE = -1L;
    public static final String UNRESOLVED_OPERAND = "Unresolved";

    private static final String RESOLUTION_NAME_KEY = "issue.field.resolution";

    private final ConstantsManager constantsManager;
    private final ResolutionStatisticsMapper resolutionStatisticsMapper;
    private final UserHistoryManager userHistoryManager;
    private final JiraBaseUrls jiraBaseUrls;

    public ResolutionSystemField(
            final VelocityTemplatingEngine templatingEngine,
            final ApplicationProperties applicationProperties,
            final ConstantsManager constantsManager,
            final JiraAuthenticationContext authenticationContext,
            final ResolutionStatisticsMapper resolutionStatisticsMapper,
            final PermissionManager permissionManager,
            final ResolutionSearchHandlerFactory searchHandlerFactory,
            final UserHistoryManager userHistoryManager, JiraBaseUrls jiraBaseUrls)
    {
        super(IssueFieldConstants.RESOLUTION, RESOLUTION_NAME_KEY, templatingEngine, applicationProperties, authenticationContext, permissionManager, searchHandlerFactory);
        this.constantsManager = constantsManager;
        this.resolutionStatisticsMapper = resolutionStatisticsMapper;
        this.userHistoryManager = userHistoryManager;
        this.jiraBaseUrls = jiraBaseUrls;
    }

    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        return getEditHtml(fieldLayoutItem, operationContext, action, issue, displayParameters);
    }

    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        if (operationContext != null)
        {
            velocityParams.put(getId(), operationContext.getFieldValuesHolder().get(getId()));
        }

        // Get the resolutions we should show
        velocityParams.put("resolutions", retrieveResolutions(operationContext, issue, velocityParams));
        return renderTemplate("resolution-edit.vm", velocityParams);
    }

    Collection<Resolution> retrieveResolutions(final OperationContext operationContext, final IssueContext issue, final Map<String, Object> velocityParams)
    {
        Predicate resolutionIncluder = new Predicate()
        {
            // Create a collection of resolution objects
            final Collection excludeResolutionIds = getIncludeResolutionIds(operationContext, JiraWorkflow.JIRA_META_ATTRIBUTE_EXCLUDE_RESOLUTION);
            final Collection includeResolutionIds = getIncludeResolutionIds(operationContext, JiraWorkflow.JIRA_META_ATTRIBUTE_INCLUDE_RESOLUTION);

            public boolean evaluate(Object arg)
            {
                Resolution resolution = (Resolution) arg;
                // Include resolution if
                //     exclude resolution list is defined, and resolution *is not* part of the list
                //     include resolution list is defined, and resolution *is* part of the list
                //     none of the lists are defined
                if (!excludeResolutionIds.equals(Collections.EMPTY_LIST))
                {
                    return (!excludeResolutionIds.contains(resolution.getId()));
                }
                else if (!includeResolutionIds.equals(Collections.EMPTY_LIST))
                {
                    return (includeResolutionIds.contains(resolution.getId()));
                }
                return true;
            }
        };

        List<Resolution> resolutions = new ArrayList<Resolution>(constantsManager.getResolutionObjects());
        CollectionUtils.filter(resolutions, resolutionIncluder);
        return Collections.unmodifiableCollection(resolutions);
    }

    Collection getIncludeResolutionIds(OperationContext operationContext, String resolutionType)
    {
        if (operationContext != null && (operationContext.getIssueOperation() instanceof WorkflowIssueOperation))
        {
            WorkflowIssueOperation issueOperation = (WorkflowIssueOperation) operationContext.getIssueOperation();
            // Retrieve the ids of resolutions to exclude on this transition. The ids are stored as meta attributes
            // on the transition in the workflow.
            ActionDescriptor wfAction = issueOperation.getActionDescriptor();
            if (wfAction != null)
            {
                Map metaAttributes = wfAction.getMetaAttributes();
                String resolutionIdString = (String) metaAttributes.get(resolutionType);
                if (!StringUtils.isBlank(resolutionIdString))
                {
                    // If the meta attribute is set and is not empty retrieve the ids of resolutions to exclude
                    String[] resolutionIds = StringUtils.split(resolutionIdString, ",");
                    if (resolutionIds != null)
                    {
                        return Arrays.asList(resolutionIds);
                    }
                }
            }
        }

        return Collections.EMPTY_LIST;
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters)
    {
        Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        // Create resolution object
        Resolution resolution = issue.getResolutionObject();
        velocityParams.put("resolution", resolution);
        return getViewHtml(velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Object value, Map displayParameters)
    {
        Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, action, null, displayParameters);
        // Create resolution object
        GenericValue resolutionGV = (GenericValue) value;
        Resolution resolution = constantsManager.getResolutionObject(resolutionGV.getString("id"));
        velocityParams.put("resolution", resolution);
        return getViewHtml(velocityParams);
    }

    private String getViewHtml(Map<String, Object> velocityParams)
    {
        return renderTemplate("resolution-view.vm", velocityParams);
    }

    public void validateParams(OperationContext operationContext, ErrorCollection errorCollectionToAddTo, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        Map fieldValuesHolder = operationContext.getFieldValuesHolder();
        String resolutionId = (String) fieldValuesHolder.get(getId());


        if (TextUtils.stringSet(resolutionId))
        {
            if (getValueFromParams(fieldValuesHolder) == null)
            {
                // Check that the resolution with the given id exists.
                errorCollectionToAddTo.addError(getId(), i18n.getText("field.error.invalidresolution"));

            }
            // Ensure that the selected resolution is not part of the exclude list
            else if (getIncludeResolutionIds(operationContext, JiraWorkflow.JIRA_META_ATTRIBUTE_EXCLUDE_RESOLUTION).contains(resolutionId))
            {
                errorCollectionToAddTo.addError(getId(), i18n.getText("field.error.excludedresolution"));
            }
            // Ensure that the selected resolution is part of the include list
            else if (!getIncludeResolutionIds(operationContext, JiraWorkflow.JIRA_META_ATTRIBUTE_INCLUDE_RESOLUTION).equals(Collections.EMPTY_LIST))
            {
                if (!getIncludeResolutionIds(operationContext, JiraWorkflow.JIRA_META_ATTRIBUTE_INCLUDE_RESOLUTION).contains(resolutionId))
                {
                    errorCollectionToAddTo.addError(getId(), i18n.getText("field.error.excludedresolution"));
                }
            }

            // Do not do any other checks
            return;
        }

        if (fieldScreenRenderLayoutItem.isRequired() && !TextUtils.stringSet(resolutionId))
        {
            errorCollectionToAddTo.addError(getId(), i18n.getText("issue.field.required", i18n.getText(getNameKey())));
        }
    }

    public Object getValueFromParams(Map params)
    {
        String resolutionId = (String) params.get(getId());
        if (TextUtils.stringSet(resolutionId))
        {
            return constantsManager.getResolution(resolutionId);
        }
        else
        {

            return null;
        }
    }

    public void populateParamsFromString(Map<String, Object> fieldValuesHolder, String stringValue, Issue issue)
            throws FieldValidationException
    {
        Long resolutionId;
        try
        {
            // Check if the issue type is a number
            resolutionId = Long.valueOf(stringValue);
        }
        catch (NumberFormatException e)
        {
            // If not, try to convert to a number
            resolutionId = getResolutionIdByName(stringValue);
        }

        // Yes, resolution id is a String, even though it is actually a number.
        // Ahh, the joy of backwards compatibility
        //noinspection unchecked
        fieldValuesHolder.put(getId(), resolutionId.toString());
    }

    private Long getResolutionIdByName(String stringValue) throws FieldValidationException
    {
        for (GenericValue resolutionGV : constantsManager.getResolutions())
        {
            if (stringValue.equalsIgnoreCase(resolutionGV.getString("name")))
            {
                return Long.valueOf(resolutionGV.getString("id"));
            }
        }

        throw new FieldValidationException("Invalid resolution name '" + stringValue + "'.");
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
                GenericValue resolution = (GenericValue) value;
                cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getId(), null, null, resolution.getString("id"), resolution.getString("name"));
            }
        }
        else
        {
            if (!valuesEqual(value, currentValue))
            {
                GenericValue currentResolution = (GenericValue) currentValue;
                if (value != null)
                {
                    GenericValue resolution = (GenericValue) value;
                    cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getId(), currentResolution.getString("id"), currentResolution.getString("name"), resolution.getString("id"), resolution.getString("name"));
                }
                else
                {
                    cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getId(), currentResolution.getString("id"), currentResolution.getString("name"), null, null);
                }
            }
        }

        if (cib != null)
        {
            issueChangeHolder.addChangeItem(cib);
        }
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
        String resolutionId = issue.getString(getId());

        // This is very dodgy! This is only here, so that the UI gets pre-populated with the default resolution
        // selected when resolving an issue that doesn't have a resolution set yet.  We should really remove this
        // here and move this bit of logic into the UI layer.
        //
        // A side effect of this is that if you transition an issue via SOAP or jelly and the issue doesn't have
        // a resolution set, then the issue will be resolved with the default resolution (unless of course the user
        // provides a resolution in the SOAP/Jelly request).
        if (resolutionId == null)
        {
            resolutionId = getApplicationProperties().getString(APKeys.JIRA_CONSTANT_DEFAULT_RESOLUTION);
        }

        //noinspection unchecked
        fieldValuesHolder.put(getId(), resolutionId);
    }

    public boolean isShown(Issue issue)
    {
        return true;
    }

    public void populateDefaults(Map<String, Object> fieldValuesHolder, Issue issue)
    {
        //noinspection unchecked
        fieldValuesHolder.put(getId(), getApplicationProperties().getString(APKeys.JIRA_CONSTANT_DEFAULT_RESOLUTION));
    }

    public Object getDefaultValue(Issue issue)
    {
        return null;
    }

    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        if (fieldValueHolder.containsKey(getId()))
        {
            GenericValue resolution = (GenericValue) getValueFromParams(fieldValueHolder);
            issue.setResolution(resolution);
            if (resolution != null)
            {
                userHistoryManager.addItemToHistory(UserHistoryItem.RESOLUTION, authenticationContext.getLoggedInUser(), resolution.getString("id"), resolution.getString("name"));
            }
        }
    }

    public MessagedResult needsMove(Collection originalIssues, Issue targetIssue, FieldLayoutItem targetFieldLayoutItem)
    {
        for (Object originalIssue1 : originalIssues)
        {
            Issue originalIssue = (Issue) originalIssue1;

            if (originalIssue.getResolution() == null && targetFieldLayoutItem.isRequired())
            {
                return new MessagedResult(true);
            }
        }
        return new MessagedResult(false);
    }

    public void populateForMove(Map<String, Object> fieldValuesHolder, Issue originalIssue, Issue targetIssue)
    {
        // If the field need to be moved then it does not have a current value, so populate the default
        populateDefaults(fieldValuesHolder, targetIssue);
    }

    public void removeValueFromIssueObject(MutableIssue issue)
    {
        issue.setResolution(null);
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return true;
    }

    public boolean hasValue(Issue issue)
    {
        return issue.getResolution() != null;
    }

    //////////////////////////////////////////// NavigableField implementation //////////////////////////////////////
    public String getColumnHeadingKey()
    {
        return "issue.column.heading.resolution";
    }

    public String getDefaultSortOrder()
    {
        return ORDER_ASCENDING;
    }

    public LuceneFieldSorter getSorter()
    {
        return resolutionStatisticsMapper;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map<String, Object> velocityParams = getVelocityParams(getAuthenticationContext().getI18nHelper(), displayParams, issue);
        GenericValue resolutionGV = issue.getResolution();
        if (resolutionGV != null)
        {
            // Create resolution object
            Resolution resolution = issue.getResolutionObject();
            velocityParams.put(getId(), resolution);
        }
        return renderTemplate("resolution-columnview.vm", velocityParams);
    }

    /////////////////////////////////////////// Bulk Edit //////////////////////////////////////////////////////////
    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        // Ensure that the project has resolutions
        if (constantsManager.getResolutionObjects().isEmpty())
        {
            return "bulk.edit.unavailable.noresolutions";
        }

        // Ensure that this field is not hidden in any Field Layouts the selected issues belong to
        for (FieldLayout fieldLayout : bulkEditBean.getFieldLayouts())
        {
            if (fieldLayout.isFieldHidden(getId()))
            {
                return "bulk.edit.unavailable.hidden";
            }
        }

        // If we got here then the field is visible in all field layouts
        // So check for permissions
        // Need to check for RESOLVE permission here rather than in the BulkEdit itself, as a user does not need the EDIT permission to edit the ASSIGNEE field,
        // just the ASSIGNEE permission, so the permissions to check depend on the field
        // hAv eto loop through all the issues incase the permission has been granted to current assignee/reporter (i.e. assigned ot a role)
        for (Issue issue : bulkEditBean.getSelectedIssues())
        {
            if (!hasBulkUpdatePermission(bulkEditBean, issue) || !isShown(issue))
            {
                return "bulk.edit.unavailable.permission";
            }
        }

        // This field is available for bulk-editing, return null (i.e no unavailble message)
        return null;
    }

    public Collection getIssueConstants()
    {
        return constantsManager.getResolutionObjects();
    }

    /**
     * Return an internationalized value for the changeHistory item - a resolution name in this case.
     *
     * @param changeHistory name of resolution
     * @param i18nHelper used to translate the resolution name
     * @return String
     */
    public String prettyPrintChangeHistory(String changeHistory, I18nHelper i18nHelper)
    {
        if (TextUtils.stringSet(changeHistory))
        {
            Long resolutionId = getResolutionIdByName(changeHistory);

            if (resolutionId != null)
            {
                Resolution resolution = constantsManager.getResolutionObject(resolutionId.toString());
                if (resolution != null)
                {
                    return resolution.getNameTranslation(i18nHelper);
                }
            }
        }
        // Otherwise return the original string
        return changeHistory;
    }


    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        final Map<String, Object> velocityParams = new HashMap<String, Object>();
        Collection<Resolution> allowedResolutions = retrieveResolutions(fieldTypeInfoContext.getOperationContext(), fieldTypeInfoContext.getIssueContext(), velocityParams);
        return new FieldTypeInfo(allowedResolutions, null);
    }

    @Override
    public JsonType getJsonSchema()
    {
        return getResolutionJsonSchema(true);
    }

    public static JsonType getResolutionJsonSchema(boolean nullable)
    {
        return JsonTypeBuilder.system(JsonType.RESOLUTION_TYPE, IssueFieldConstants.RESOLUTION);
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(Issue issue, boolean renderedVersionRequired, FieldLayoutItem fieldLayoutItem)
    {
        return new FieldJsonRepresentation(new JsonData(ResolutionJsonBean.shortBean(issue.getResolutionObject(), jiraBaseUrls)));
    }

    @Override
    public RestFieldOperationsHandler getRestFieldOperation()
    {
        return new ResolutionRestFieldOperationsHandler(constantsManager, authenticationContext.getI18nHelper());
    }

    @Override
    public JsonData getJsonDefaultValue(IssueContext issueCtx)
    {
        return null;
    }
}
