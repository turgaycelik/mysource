package com.atlassian.jira.issue.fields;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.DueDateRestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.RestAwareField;
import com.atlassian.jira.issue.fields.rest.RestFieldOperations;
import com.atlassian.jira.issue.fields.rest.RestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.history.DateTimeFieldChangeLogHelper;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.handlers.DueDateSearchHandlerFactory;
import com.atlassian.jira.issue.search.parameters.lucene.sort.StringSortComparator;
import com.atlassian.jira.issue.statistics.DateFieldSorter;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.rest.Dates;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.DateFieldFormat;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.search.SortField;
import webwork.action.Action;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DueDateSystemField extends AbstractOrderableNavigableFieldImpl implements HideableField, RequirableField, DateField, RestAwareField, RestFieldOperations
{
    private static final Logger log = Logger.getLogger(DueDateSystemField.class);

    private static final String DUE_DATE_NAME_KEY = "issue.field.duedate";

    private final DateFieldFormat dateFieldFormat;
    private final DateTimeFieldChangeLogHelper dateTimeFieldChangeLogHelper;

    public DueDateSystemField
            (
                    final VelocityTemplatingEngine templatingEngine,
                    final ApplicationProperties applicationProperties,
                    final PermissionManager permissionManager,
                    final JiraAuthenticationContext authenticationContext,
                    final DueDateSearchHandlerFactory factory,
                    final DateFieldFormat dateFieldFormat,
                    final DateTimeFieldChangeLogHelper dateTimeFieldChangeLogHelper)
    {
        super(IssueFieldConstants.DUE_DATE, DUE_DATE_NAME_KEY, templatingEngine, applicationProperties, authenticationContext, permissionManager, factory);
        this.dateTimeFieldChangeLogHelper = dateTimeFieldChangeLogHelper;
        this.dateFieldFormat = dateFieldFormat;
    }

    /**
     * @deprecated since v4.4. Use {@link #} instead.
     */
    public DueDateSystemField
    (
                    final VelocityTemplatingEngine templatingEngine,
                    final ApplicationProperties applicationProperties,
                    final PermissionManager permissionManager,
                    final JiraAuthenticationContext authenticationContext,
                    final DueDateSearchHandlerFactory factory,
                    final DateFieldFormat dateFieldFormat)
    {
        this(templatingEngine, applicationProperties, permissionManager, authenticationContext, factory, dateFieldFormat, ComponentAccessor.getComponentOfType(DateTimeFieldChangeLogHelper.class));
    }

    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map dispayParameters)
    {
        return getEditHtml(fieldLayoutItem, operationContext, action, issue, dispayParameters);
    }

    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        velocityParams.put(getId(), operationContext.getFieldValuesHolder().get(getId()));
        velocityParams.put("dateFormat", CustomFieldUtils.getDateFormat());
        velocityParams.put("dateTimeFormat", CustomFieldUtils.getDateTimeFormat());
        velocityParams.put("timeFormat", CustomFieldUtils.getTimeFormat());
        return renderTemplate("duedate-edit.vm", velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        if (issue.getDueDate() != null)
        {
            velocityParams.put("duedate", this.dateFieldFormat.formatDatePicker(issue.getDueDate()));
        }
        return getViewHtml(velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Object value, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, null, displayParameters);
        if (value != null)
        {
            velocityParams.put("duedate", this.dateFieldFormat.formatDatePicker((Date) value));
        }
        return getViewHtml(velocityParams);
    }

    private String getViewHtml(Map velocityParams)
    {
        return renderTemplate("duedate-view.vm", velocityParams);
    }

    public boolean isShown(Issue issue)
    {
        return hasPermission(issue, Permissions.SCHEDULE_ISSUE);
    }

    public void validateParams(OperationContext operationContext, ErrorCollection errorCollection, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        Map fieldValuesHolder = operationContext.getFieldValuesHolder();
        // If the value has been entered into the parameters the user must have permission to set the due date
        String dueDate = (String) fieldValuesHolder.get(getId());
        if (TextUtils.stringSet(dueDate))
        {
            try
            {
                this.dateFieldFormat.parseDatePicker(dueDate);
            }
            catch (IllegalArgumentException e)
            {
                errorCollection.addError(getId(), i18n.getText("createissue.error.invalid.due.date",
                        getApplicationProperties().getDefaultBackedString(APKeys.JIRA_DATE_PICKER_JAVA_FORMAT),
                        this.dateFieldFormat.formatDatePicker(new Date())
                ));

                return;
            }
        }

        if (fieldScreenRenderLayoutItem.isRequired() && !TextUtils.stringSet(dueDate))
        {
            if (isShown(issue))
            {
                errorCollection.addError(getId(), i18n.getText("issue.field.required", i18n.getText(getNameKey())));
            }
            else
            {
                errorCollection.addErrorMessage(i18n.getText("createissue.error.due.date.required", i18n.getText(getNameKey()), issue.getProjectObject().getName()));
            }
        }
    }

    public void createValue(Issue issue, Object value)
    {
        // The field is recorded on the issue itself so there is nothing to do
    }

    /**
     * Update the issue
     *


     @param fieldLayoutItem
      * @param issue
     * @param issueChangeHolder
     */
    public void updateValue(FieldLayoutItem fieldLayoutItem, Issue issue, ModifiedValue modifiedValue, IssueChangeHolder issueChangeHolder)
    {
        //NOTE: No need to update issue in the data store as the value is stored on the issue itself

        Object currentValue = modifiedValue.getOldValue();
        Object value = modifiedValue.getNewValue();
        ChangeItemBean cib = null;

        if (currentValue == null)
        {
            if (value != null)
            {
                cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getId(), null, null, getChangelogValue(value), getChangelogString(value));
            }
        }
        else
        {
            if (!valuesEqual(value, currentValue))
            {
                cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getId(), getChangelogValue(currentValue), getChangelogString(currentValue), getChangelogValue(value), getChangelogString(value));
            }
        }

        if (cib != null)
            issueChangeHolder.addChangeItem(cib);
    }

    private String getChangelogString(Object value)
    {
        if (value != null)
        {
            return value.toString();
        }
        else
        {
            return null;
        }
    }

    private String getChangelogValue(Object value)
    {
        if (value != null && value instanceof Date)
        {
            Date date = (Date) value;
            return dateTimeFieldChangeLogHelper.createChangelogValueForDateField(date);
        }
        return null;
    }

    public Object getValueFromParams(Map params)
    {
        if (params.containsKey(getId()))
        {
            String dueDate = (String) params.get(getId());
            if (TextUtils.stringSet(dueDate))
            {
                return new Timestamp(this.dateFieldFormat.parseDatePicker(dueDate).getTime());
                }
            else
            {
                // If no due date has been given resort to null
                return null;
            }
        }
        else
        {
            // If the due date has not been shown then just record null
            return null;
        }
    }

    public void populateParamsFromString(Map<String, Object> fieldValuesHolder, String stringValue, Issue issue) throws FieldValidationException
    {
        fieldValuesHolder.put(getId(), stringValue);
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
        Timestamp duedate = issue.getDueDate();
        if (duedate != null)
            fieldValuesHolder.put(getId(), this.dateFieldFormat.formatDatePicker(duedate));
        else
            fieldValuesHolder.put(getId(), null);
    }

    public void populateDefaults(Map<String, Object> fieldValuesHolder, Issue issue)
    {
    }

    public Object getDefaultValue(Issue issue)
    {
        return null;
    }

    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        if (fieldValueHolder.containsKey(getId()))
        {
            issue.setDueDate((Timestamp) getValueFromParams(fieldValueHolder));
        }
    }

    public MessagedResult needsMove(Collection originalIssues, Issue targetIssue, FieldLayoutItem targetFieldLayoutItem)
    {
        for (final Object originalIssue1 : originalIssues)
        {
            Issue originalIssue = (Issue) originalIssue1;

            if (originalIssue.getDueDate() == null && targetFieldLayoutItem.isRequired())
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
        issue.setDueDate(null);
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return true;
    }

    public boolean hasValue(Issue issue)
    {
        return (issue.getDueDate() != null);
    }

    /////////////////////////////////////////// NavigableField Implementation /////////////////////////////////////////
    public String getColumnHeadingKey()
    {
        return "issue.column.heading.duedate";
    }

    public String getDefaultSortOrder()
    {
        return NavigableField.ORDER_DESCENDING;
    }

    public LuceneFieldSorter getSorter()
    {
        return DateFieldSorter.ISSUE_DUEDATE_STATSMAPPER;
    }

    @Override
    public List<SortField> getSortFields(final boolean sortOrder)
    {
        return Collections.singletonList(new SortField(DocumentConstants.ISSUE_SORT_DUEDATE, new StringSortComparator(), sortOrder));
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map<String, Object> velocityParams = getVelocityParams(getAuthenticationContext().getI18nHelper(), displayParams, issue);
        velocityParams.put("dueDateFormatter", dateFieldFormat);
        Timestamp dueDate = issue.getDueDate();
        if (dueDate != null)
        {
            velocityParams.put(getId(), dueDate);
        }
        return renderTemplate("duedate-columnview.vm", velocityParams);
    }

    public String prettyPrintChangeHistory(String changeHistory)
    {
        if (StringUtils.isNotBlank(changeHistory))
        {
            try
            {
                Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(changeHistory);

                return dateFieldFormat.format(date);
            }
            catch (ParseException e)
            {
                log.warn("unable to parse duedate change history, falling back to unparsed date.");
            }
        }
        return super.prettyPrintChangeHistory(changeHistory);
    }

    public String prettyPrintChangeHistory(String changeHistory, I18nHelper i18nHelper)
    {
        if (StringUtils.isNotBlank(changeHistory))
        {
            try
            {
                Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(changeHistory);

                return this.dateFieldFormat.formatDatePicker(date);
            }
            catch (ParseException e)
            {
                log.warn("unable to parse duedate change history, falling back to unparsed date.");
            }
        }
        return super.prettyPrintChangeHistory(changeHistory);
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        return new FieldTypeInfo(null, null);
    }

    @Override
    public JsonType getJsonSchema()
    {
        return JsonTypeBuilder.system(JsonType.DATE_TYPE, getId());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(Issue issue, boolean renderedVersionRequired, FieldLayoutItem fieldLayoutItem)
    {
        Timestamp dueDate = issue.getDueDate();
        FieldJsonRepresentation fieldJsonRepresentation = new FieldJsonRepresentation(new JsonData(Dates.asDateString(dueDate)));
        if (renderedVersionRequired && dueDate != null)
        {
            fieldJsonRepresentation.setRenderedData(new JsonData(dateFieldFormat.format(dueDate)));
        }
        return fieldJsonRepresentation;
    }

    @Override
    public RestFieldOperationsHandler getRestFieldOperation()
    {
        return new DueDateRestFieldOperationsHandler(dateFieldFormat, authenticationContext.getI18nHelper());
    }

    @Override
    public JsonData getJsonDefaultValue(IssueContext issueCtx)
    {
        return null;
    }
}
