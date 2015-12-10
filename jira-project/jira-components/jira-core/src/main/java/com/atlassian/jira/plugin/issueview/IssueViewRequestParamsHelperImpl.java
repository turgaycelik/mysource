package com.atlassian.jira.plugin.issueview;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.web.RequestParameterKeys;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Purpose of this class is to encapsulate view field definition parsing. If no parameters are defined issue view is
 * backward compatible. If parameters are defined and and no one parameter is valid class throws exception causing HTTP 400 error.
 */
public class IssueViewRequestParamsHelperImpl implements IssueViewRequestParamsHelper
{
    private static final Logger log = Logger.getLogger(IssueViewRequestParamsHelperImpl.class);

    /*
        Field names mapping. Allows to define mapping between request parameters and names used internally in JIRA.
        Mapping adds additional name of internally defined names - both names are valid.
     */
    private Map<String, String> fieldNamesMapping;

    /*
        XML Field names that not map into JIRA fields (as defied in IssueFieldConstants.
     */
    private Set<String> nonIssueFields;

    /*
        Used to retrieve field information.
     */
    private FieldManager fieldManager;

    public IssueViewRequestParamsHelperImpl(FieldManager fieldManager)
    {
        this.fieldManager = fieldManager;

        Map<String, String> fieldsMapping = new HashMap<String,  String>();
        fieldsMapping.put("pid", IssueFieldConstants.PROJECT);
        fieldsMapping.put("comments", IssueFieldConstants.COMMENT);
        fieldsMapping.put("component", IssueFieldConstants.COMPONENTS);
        fieldsMapping.put("due", IssueFieldConstants.DUE_DATE);
        fieldsMapping.put("type", IssueFieldConstants.ISSUE_TYPE);
        fieldsMapping.put("version", IssueFieldConstants.AFFECTED_VERSIONS);
        fieldsMapping.put("fixfor", IssueFieldConstants.FIX_FOR_VERSIONS);
        fieldsMapping.put("subtask", IssueFieldConstants.SUBTASKS);
        fieldsMapping.put("attachments", IssueFieldConstants.ATTACHMENT);
        fieldsMapping.put("resolved", IssueFieldConstants.RESOLUTION_DATE);
        fieldsMapping.put("aggregatetimeremainingestimate", IssueFieldConstants.AGGREGATE_TIME_ESTIMATE);
        fieldNamesMapping = Collections.unmodifiableMap(fieldsMapping);

        Set<String> nonIssue = new HashSet<String>();
        nonIssue.add("parent");
        nonIssue.add("link");
        nonIssue.add("title");
        nonIssueFields = Collections.unmodifiableSet(nonIssue);
    }

    /**
     * Method checks defined field parameters.
     *
     * Fields are added to @{link IssueViewFieldParams} based on following rules:
     * - field name in param matches to field id as defined in @{link IssueFieldConstants}
     * - field name in param matches to field mapping as defined in #fieldNamesMapping
     * - field name in param matches to #nonIssueFields
     * - allcustom field param means no field ids will be added to @{link IssueViewFieldParams#getCustomFieldIds} but @{link IssueViewFieldParams#isAllCustomFields()} flag will be set
     * - field is ignored if does not match to any of above criteria
     *
     * If no one of above criteria will pass method returns @{link IssueViewFieldParams} with empty fields collection
     * and @{link IssueViewFieldParams#isCustomViewRequested()} flag set to true.
     *
     * @{link IssueViewFieldParams#isCustomViewRequested()} will be set to false only when no field parameters were set.
     *
     * @param  requestParameters HttpServletRequest parameters
     * @return @{link IssueViewFieldParams} containing requested field set and or empty instance if not field parameters were defined
     */
    public IssueViewFieldParams getIssueViewFieldParams(Map requestParameters)
    {
        final Set<String> fields = new HashSet<String>();
        final Set<String> customFields = new HashSet<String>();
        boolean allCustomFields = false;
        boolean customViewRequested = false;

        final Set<String> orderableFieldIds = getOrderableFieldIds();

        // so far only field parameter is checked, but could be extended to other parameters
        if (requestParameters != null && requestParameters.containsKey(RequestParameterKeys.JIRA_ISSUE_VIEW_FIELDS))
        {
            customViewRequested = true;
            String[] fieldNames = (String[]) requestParameters.get(RequestParameterKeys.JIRA_ISSUE_VIEW_FIELDS);
            if (fieldNames != null)
            {
                for (String fieldName : fieldNames)
                {
                    if ("allcustom".equals(fieldName))
                    {
                        allCustomFields = true;
                    }
                    else
                    {
                        if (fieldName.startsWith("customfield_"))
                        {
                            CustomField customField = null;
                            try
                            {
                                customField = fieldManager.getCustomField(fieldName);
                            }
                            catch (IllegalArgumentException ex)
                            {
                                log.debug("Invalid field specified for custom issue XML view.", ex);
                            }
                            if (customField != null)
                            {
                                customFields.add(fieldName);
                            }
                        }
                        else
                        {
                            String queryName = fieldName;
                            if (fieldNamesMapping.containsKey(fieldName))
                            {
                                queryName = fieldNamesMapping.get(fieldName);
                            }
                            if (IssueFieldConstants.TIMETRACKING.equals(queryName))
                            {
                                processField(IssueFieldConstants.TIME_ORIGINAL_ESTIMATE, fields);
                                processField(IssueFieldConstants.TIME_ESTIMATE, fields);
                                processField(IssueFieldConstants.TIME_SPENT, fields);
                                processField(IssueFieldConstants.AGGREGATE_TIME_ORIGINAL_ESTIMATE, fields);
                                processField(IssueFieldConstants.AGGREGATE_TIME_ESTIMATE, fields);
                                processField(IssueFieldConstants.AGGREGATE_TIME_SPENT, fields);
                            }
                            else
                            {
                                Field field = processField(queryName, fields);
                                if (field == null)
                                {
                                    log.debug("Invalid field specified for custom issue XML view: " + fieldName);
                                }
                            }
                        }
                    }
                }
            }
        }

        return new IssueViewFieldParamsImpl(customViewRequested, fields, orderableFieldIds, customFields, allCustomFields);
    }

    private Set<String> getOrderableFieldIds()
    {
        Set<String> orderableFieldIds = new HashSet<String>();
        Set<OrderableField> orderableFields = fieldManager.getOrderableFields();
        for (Field field : orderableFields)
        {
            orderableFieldIds.add(field.getId());
        }
        return orderableFieldIds;
    }

    private Field processField(final String queryName, final Set<String> fields)
    {
        Field field = fieldManager.getField(queryName);
        if (field != null)
        {
            fields.add(queryName);
        }
        else
        {
            if (nonIssueFields.contains(queryName))
            {
                fields.add(queryName);
            }
        }
        return field;
    }
}
