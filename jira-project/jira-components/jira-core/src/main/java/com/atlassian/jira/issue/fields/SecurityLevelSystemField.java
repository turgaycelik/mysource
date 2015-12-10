package com.atlassian.jira.issue.fields;

import com.atlassian.core.util.map.EasyMap;
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
import com.atlassian.jira.issue.fields.rest.RestAwareField;
import com.atlassian.jira.issue.fields.rest.RestFieldOperations;
import com.atlassian.jira.issue.fields.rest.RestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.SecurityLevelRestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.SecurityLevelJsonBean;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.issue.statistics.SecurityLevelStatisticsMapper;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SecurityLevelSystemField extends AbstractOrderableNavigableFieldImpl
        implements HideableField, RequirableField, RestAwareField, RestFieldOperations
{

    private static final String SECURITY_LEVEL_NAME_KEY = "issue.field.securitylevel";
    private static final Long NO_SECURITY_LEVEL_ID = -1L;

    private final IssueSecurityLevelManager issueSecurityLevelManager;
    private final IssueSecuritySchemeManager issueSecuritySchemeManager;
    private final SecurityLevelStatisticsMapper securityLevelStatisticsMapper;
    private final JiraBaseUrls jiraBaseUrls;

    public SecurityLevelSystemField(VelocityTemplatingEngine templatingEngine, PermissionManager permissionManager,
            ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext,
            IssueSecurityLevelManager issueSecurityLevelManager, IssueSecuritySchemeManager issueSecuritySchemeManager,
            SecurityLevelStatisticsMapper securityLevelStatisticsMapper, JiraBaseUrls jiraBaseUrls)
    {
        super(IssueFieldConstants.SECURITY, SECURITY_LEVEL_NAME_KEY, templatingEngine, applicationProperties, authenticationContext, permissionManager, null);
        this.issueSecurityLevelManager = issueSecurityLevelManager;
        this.issueSecuritySchemeManager = issueSecuritySchemeManager;
        this.securityLevelStatisticsMapper = securityLevelStatisticsMapper;
        this.jiraBaseUrls = jiraBaseUrls;
    }

    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        return getEditHtml(fieldLayoutItem, operationContext, action, issue, displayParameters);
    }

    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);

        if (issue.isSubTask())
        {
            // Users can't set the Security Level of a subtask directly, show a read-only messsage.
            velocityParams.put("infoMessageKey", "bulk.edit.security.level.subtask.message");
            return renderTemplate("securitylevel-subtask.vm", velocityParams);
        }

        String securityLevelId = (String) operationContext.getFieldValuesHolder().get(getId());
        if (securityLevelId != null)
        {
            velocityParams.put(getId(), Long.valueOf(securityLevelId));
        }
        velocityParams.put("securityLevels", getUserSecurityLevels(issue));
        velocityParams.put("noneLevelId", NO_SECURITY_LEVEL_ID);
        return renderTemplate("securitylevel-edit.vm", velocityParams);
    }

    public String getBulkEditHtml(OperationContext operationContext, Action action, BulkEditBean bulkEditBean, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(null, action, null, displayParameters);
        //JRA-13990 Subtasks must take the parent's Security Level, so show a message instead of an editable field.
        if (bulkEditBean.isSubTaskOnly())
        {
            velocityParams.put("infoMessageKey", "bulk.edit.security.level.subtask.message");
            return renderTemplate("securitylevel-subtask.vm", velocityParams);
        }

        String securityLevelId = (String) operationContext.getFieldValuesHolder().get(getId());
        if (securityLevelId != null)
        {
            velocityParams.put(getId(), Long.valueOf(securityLevelId));
        }
        /* A bulk move will use the target project for getting the possible security levels
         whlie a bulk transition will use the selected issues.*/
        if (bulkEditBean.getTargetProjectGV() != null)
        {
            velocityParams.put("securityLevels", getUserSecurityLevelsForProject(bulkEditBean.getTargetProjectGV()));
        }
        else
        {
            velocityParams.put("securityLevels", getUserSecurityLevels(bulkEditBean.getSelectedIssues()));
        }
        velocityParams.put("noneLevelId", NO_SECURITY_LEVEL_ID);
        return renderTemplate("securitylevel-edit.vm", velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        velocityParams.put("security", issue.getSecurityLevel());
        return getViewHtml(velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Object value, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, null, displayParameters);
        velocityParams.put("security", value);
        return getViewHtml(velocityParams);
    }

    private String getViewHtml(Map velocityParams)
    {
        return renderTemplate("securitylevel-view.vm", velocityParams);
    }

    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        // Ensure that this field is not hidden in any Field Layouts the selected issues belong to
        if (isHidden(bulkEditBean.getFieldLayouts()))
        {
            return "bulk.edit.unavailable.hidden";
        }

        // Ensure all selected projects are assigned the same issue level security scheme
        Iterator iterator = bulkEditBean.getProjects().iterator();
        GenericValue securityScheme = getSecurityScheme((GenericValue) iterator.next());
        if (securityScheme == null)
        {
            return "bulk.edit.unavailable.issuesecurity.noscheme";
        }

        while (iterator.hasNext())
        {
            GenericValue securityScheme2 = getSecurityScheme((GenericValue) iterator.next());
            if (securityScheme2 == null)
            {
                return "bulk.edit.unavailable.issuesecurity.noscheme";
            }
            else if (!securityScheme.equals(securityScheme2))
            {
                return "bulk.edit.unavailable.issuesecurity.diffschemes";
            }
        }

        // Have to look through all the issues in case permission has been given to current assignee/reporter (i.e. role based)
        for (Issue issue : bulkEditBean.getSelectedIssues())
        {
            // If we got here then the field is visible in all field layouts
            // So check for permission
            if (!hasBulkUpdatePermission(bulkEditBean, issue) || !isShown(issue))
            {
                return "bulk.edit.unavailable.issuesecurity.permission";
            }
        }

        // This field is available for bulk-editing, return null (i.e no unavailble message)
        return null;
    }

    private List<GenericValue> getUserSecurityLevels(Collection<Issue> issues)
    {
        // Need to loop through all the issues as security levels could have role based permission types such as
        // assignee or reporter which could be different for each issue.
        Iterator<Issue> iterator = issues.iterator();
        List<GenericValue> securityLevels = new ArrayList<GenericValue>(getUserSecurityLevels(iterator.next()));

        while (iterator.hasNext())
        {
            securityLevels.retainAll(getUserSecurityLevels(iterator.next()));
        }

        return securityLevels;
    }

    private List<IssueSecurityLevel> getSecurityLevelsForLoggedInUser(Issue issue)
    {
        if (issue.getGenericValue() == null)
        {
            return issueSecurityLevelManager.getUsersSecurityLevels(issue.getProjectObject(), getAuthenticationContext().getLoggedInUser());
        }
        else
        {
            return issueSecurityLevelManager.getUsersSecurityLevels(issue, getAuthenticationContext().getLoggedInUser());
        }
    }

    /**
     * @deprecated Use {@link #getSecurityLevelsForLoggedInUser(com.atlassian.jira.issue.Issue)} instead. Since v5.2. (Need to convert the Velocity Template)
     */
    private List<GenericValue> getUserSecurityLevels(Issue issue)
    {
        try
        {
            if (issue.getGenericValue() == null)
            {
                return issueSecurityLevelManager.getUsersSecurityLevels(issue.getProject(), getAuthenticationContext().getLoggedInUser());
            }
            else
            {
                return issueSecurityLevelManager.getUsersSecurityLevels(issue.getGenericValue(), getAuthenticationContext().getLoggedInUser());
            }
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Error occurred while retrieving security levels.", e);
        }
    }

    private List getUserSecurityLevelsForProject(GenericValue project)
    {
        try
        {
            return issueSecurityLevelManager.getUsersSecurityLevels(project, getAuthenticationContext().getLoggedInUser());
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Error occurred while retrieving security levels for project", e);
        }
    }

    public void validateParams(OperationContext operationContext, ErrorCollection errorCollectionToAddTo, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        Map fieldValuesHolder = operationContext.getFieldValuesHolder();
        // if user does NOT have permission to set security level, and it IS set to a value then generate an error
        try
        {
            // Ensure the security level id is valid
            String securityLevelIdParam = (String) fieldValuesHolder.get(getId());
            if (TextUtils.stringSet(securityLevelIdParam))
            {
                long securityLevelId = Long.parseLong(securityLevelIdParam);
                if (securityLevelId > NO_SECURITY_LEVEL_ID)
                {
                    try
                    {
                        if (issueSecurityLevelManager.getIssueSecurityLevel(securityLevelId) == null)
                        {
                            errorCollectionToAddTo.addError(getId(), i18n.getText("createissue.error.security.level.invalid"));
                        }
                    }
                    catch (GenericEntityException e)
                    {
                        throw new DataAccessException("Error occurred while retrieving security level with id '" + securityLevelId + "'.", e);
                    }
                }
                else
                if (fieldScreenRenderLayoutItem.isRequired() && securityLevelId == NO_SECURITY_LEVEL_ID && !issue.isSubTask())
                {
                    errorCollectionToAddTo.addError(getId(), i18n.getText("issue.field.required", i18n.getText(getNameKey())));
                }
            }
            else if (fieldScreenRenderLayoutItem.isRequired() && !issue.isSubTask())
            {
                errorCollectionToAddTo.addError(getId(), i18n.getText("issue.field.required", i18n.getText(getNameKey())));
            }
        }
        catch (NumberFormatException e)
        {
            errorCollectionToAddTo.addError(getId(), "Invalid security level id '" + fieldValuesHolder.get(getId()) + "'.");
        }
    }

    public Object getDefaultValue(Issue issue)
    {
        return getDefaultSecurityLevel(issue);
    }

    private GenericValue getDefaultSecurityLevel(IssueContext issueCtx)
    {
        try
        {
            Long defaultSecurityLevelId = issueSecurityLevelManager.getSchemeDefaultSecurityLevel(issueCtx.getProject());
            if (defaultSecurityLevelId != null)
            {
                return issueSecurityLevelManager.getIssueSecurityLevel(defaultSecurityLevelId);
            }
            else
            {
                return null;
            }
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Error occurred while retrieving default security level.", e);
        }
    }

    /**
     * Update the issue
     */
    public void updateValue(FieldLayoutItem fieldLayoutItem, Issue issue, ModifiedValue modifiedValue, IssueChangeHolder issueChangeHolder)
    {
        // Sub-task's security level cannot be changed
        if (!issue.isSubTask())
        {
            Object currentValue = modifiedValue.getOldValue();
            Object value = modifiedValue.getNewValue();
            ChangeItemBean cib = null;

            if (currentValue == null)
            {
                if (value != null)
                {
                    GenericValue securityGV = (GenericValue) value; // TODO: if this cast is safe, then why not do it  in the first place?
                    cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "security", null, null, String.valueOf(securityGV.getLong("id")), securityGV.getString("name"));
                }
            }
            else
            {
                if (!valuesEqual(value, currentValue))
                {
                    GenericValue currentSecurityGV = (GenericValue) currentValue;  // TODO: if this cast is safe, then why not do it  in the first place?
                    if (value != null)
                    {
                        GenericValue securityGV = (GenericValue) value; // TODO: if this cast is safe, then why not do it  in the first place?
                        cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "security", String.valueOf(currentSecurityGV.getLong("id")), currentSecurityGV.getString("name"), String.valueOf(securityGV.getLong("id")), securityGV.getString("name"));
                    }
                    else
                    {
                        cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "security", String.valueOf(currentSecurityGV.getLong("id")), currentSecurityGV.getString("name"), null, null);
                    }
                }
            }

            if (cib != null)
            {
                // Change the value of the security level for all sub-tasks
                Collection<GenericValue> subTasks = issue.getSubTasks();
                if (!subTasks.isEmpty())
                {
                    for (final GenericValue subTask : subTasks)
                    {
                        subTask.set(getId(), value == null ? null : ((GenericValue) value).getLong("id"));
                        try
                        {
                            subTask.store();
                        }
                        catch (GenericEntityException e)
                        {
                            throw new DataAccessException("Could not update security level of sub-task.", e);
                        }
                    }
                    // We have changed the subtasks of this issue, so we need to notify this in the change holder.
                    issueChangeHolder.setSubtasksUpdated(true);
                }
                issueChangeHolder.addChangeItem(cib);
            }
        }
    }

    public void createValue(Issue issue, Object value)
    {
        // Do nothing as the value is recorded on the issue itself.
    }

    public Object getValueFromParams(Map params)
    {
        if (params.containsKey(getId()) && params.get(getId()) != null)
        {
            Long securityLevelId = Long.valueOf((String) params.get(getId()));
            if (NO_SECURITY_LEVEL_ID.equals(securityLevelId))
            {
                return null;
            }
            else
            {
                return getSecurityLevel(securityLevelId);
            }
        }
        else
        {
            // No parameter could be found so need to set the default security level
            return null;
        }
    }

    public void populateParamsFromString(Map<String, Object> fieldValuesHolder, String stringValue, Issue issue)
            throws FieldValidationException
    {
        Long securityLevelId;
        try
        {
            // Check if the issue type is a number
            securityLevelId = Long.valueOf(stringValue);
        }
        catch (NumberFormatException e)
        {
            // If not, try to convert to a number
            securityLevelId = getSecurityLevelIdByName(issue, stringValue);
        }

        fieldValuesHolder.put(getId(), securityLevelId.toString());
    }

    public List<IssueSearcher<?>> getAssociatedSearchers()
    {
        // @todo should make this possible
        return Collections.emptyList();
    }

    private Long getSecurityLevelIdByName(Issue issue, String securityLevelName) throws FieldValidationException
    {
        List<IssueSecurityLevel> userSecurityLevels = getSecurityLevelsForLoggedInUser(issue);
        for (IssueSecurityLevel userSecurityLevel : userSecurityLevels)
        {
            if (securityLevelName.equalsIgnoreCase(userSecurityLevel.getName()))
            {
                return userSecurityLevel.getId();
            }
        }

        throw new FieldValidationException("Invalid security level name '" + securityLevelName + "'.");
    }

    private GenericValue getSecurityLevel(Long securityLevelId)
    {
        if (securityLevelId == null)
        {
            return null;
        }

        try
        {
            return issueSecurityLevelManager.getIssueSecurityLevel(securityLevelId);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Error occurred while retrieving security level with id '" + securityLevelId + "'.", e);
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
        GenericValue securityLevel = issue.getSecurityLevel();
        if (securityLevel != null)
        {
            fieldValuesHolder.put(getId(), securityLevel.getLong("id").toString());
        }
        else
        {
            fieldValuesHolder.put(getId(), null);
        }
    }

    public void populateDefaults(Map<String, Object> fieldValuesHolder, Issue issue)
    {
        // setup default security level
        GenericValue defaultSecurityLevel = (GenericValue) getDefaultValue(issue);
        if (defaultSecurityLevel != null)
        {
            fieldValuesHolder.put(getId(), defaultSecurityLevel.getLong("id").toString());
        }
        else
        {
            fieldValuesHolder.put(getId(), null);
        }
    }

    public boolean isShown(Issue issue)
    {
        return hasPermission(issue, Permissions.SET_ISSUE_SECURITY) && hasSecurityScheme(issue.getProject());
    }

    private boolean hasSecurityScheme(GenericValue project)
    {
        // Test of the project has issue level security scheme
        return getSecurityScheme(project) != null;
    }

    protected GenericValue getSecurityScheme(GenericValue project)
    {
        try
        {
            List schemes = issueSecuritySchemeManager.getSchemes(project);
            return schemes == null || schemes.isEmpty() ? null : (GenericValue) schemes.iterator().next();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        if (issue.isSubTask())
        {
            final Issue parentObject = issue.getParentObject();
            // on subtask create its possible that the parent is known on the issue yet
            if (parentObject != null)
            {
                // Set the security field to be the same as the parent issue
                issue.setSecurityLevel(parentObject.getSecurityLevel());
            }
        }
        else
        {
            if (fieldValueHolder.containsKey(getId()))
            {
                issue.setSecurityLevel((GenericValue) getValueFromParams(fieldValueHolder));
            }
        }
    }

    /**
     * We want to prompt the user for a change if the Security Level scheme is changing due to a change in project,
     * or if the issue used to have no security level, and the Project it is being moved to has a non-null default
     * security level.  This will also return true, if the original issue has an issue level set, but the user
     * does not have permission to see that security level in the target project. Finally if no security level has
     * been set in the source issue, and the target project requires a security level, this will also return true.
     *
     * @param originalIssues
     * @param targetIssue
     * @param targetFieldLayoutItem
     * @return
     */
    public MessagedResult needsMove(Collection originalIssues, Issue targetIssue, FieldLayoutItem targetFieldLayoutItem)
    {
        // Note that there is a problem with how to deal with subtasks here.
        // In theory, we should never prompt the user for a Security value on a subtask, as it should just take the
        // same security value as its parent. However, on Bulk Move project, we change an issue and its subtasks.
        // When the parent issue is updated, we update the security of the subtask, then when we update the subtask we
        // stomp on this security value UNLESS the subtask replied true to needsMove(). In this case, it will get the
        // correct level from the parent. Therefore, correct behaviour currently relies on the subtask always returning
        // true if the parent returns true. Unfortunatley this method does not have enough information to always do that.
        // eg: if the parent needs to get a security level because it is moving to a new project and Security is now a
        // required field.

        // See JRA-14350. In order to fix this, we have a workaround in BulkMoveOperation, whereby we don't call this
        // method with subtasks, we check if the parent needed to move instead. However, other pieces of code may be
        // calling this method with subtasks, and getting incorrect results in some cases.

        Long defaultSecurityLevel = issueSecurityLevelManager.getDefaultSecurityLevel(targetIssue.getProjectObject());

        for (final Object originalIssue1 : originalIssues)
        {
            Issue originalIssue = (Issue) originalIssue1;

            if (originalIssue.getSecurityLevelId() == null)
            {
                // If the security level is null only need to update if the target project requires issue level security to be set
                if (targetFieldLayoutItem.isRequired())
                {
                    return new MessagedResult(true);
                }
                // If we are moving Project and the new Project has a Default Security Level, we will prompt.
                if (defaultSecurityLevel != null && issueMovedProject(originalIssue, targetIssue))
                {
                    return new MessagedResult(true);
                }
            }
            else
            {
                // Otherwise, only need to update the field if the security level is not applicable to the target project.
                IssueSecurityLevel securityLevel = issueSecurityLevelManager.getSecurityLevel(originalIssue.getSecurityLevelId());
                if (!(getSecurityLevelsForLoggedInUser(targetIssue).contains(securityLevel)))
                {
                    return new MessagedResult(true);
                }
            }
        }
        return new MessagedResult(false);
    }

    /**
     * Returns true if the original Issue has a different Project than the given target Project ID.
     * @param originalIssue The original Issue, before we do a move/migrate like operation.
     * @param targetIssue The target Issue, after we do a move/migrate like operation.
     * @return true if the issue is moving to a different project from where it is now.
     */
    private boolean issueMovedProject(final Issue originalIssue, final Issue targetIssue)
    {
        if (originalIssue.getProjectObject() == null)
        {
            throw new IllegalArgumentException("Null project for originalIssue '" + originalIssue.getKey() + "'");
        }
        if (targetIssue.getProjectObject() == null)
        {
            throw new IllegalArgumentException("Null project for targetIssue '" + targetIssue.getKey() + "'");
        }
        return !originalIssue.getProjectObject().getId().equals(targetIssue.getProjectObject().getId());
    }

    public void populateForMove(Map<String, Object> fieldValuesHolder, Issue originalIssue, Issue targetIssue)
    {
        populateDefaults(fieldValuesHolder, targetIssue);
    }

    public void removeValueFromIssueObject(MutableIssue issue)
    {
        if (issue.isSubTask())
        {
            // Subtask must always take Security Level of its parent - don't allow someone to try to remove it.
            return;
        }
        else
        {
            // Top Level (parent) Issue.
            issue.setSecurityLevel(null);
        }
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        // Subtask must always take Security Level of its parent - don't allow someone to try to remove it.
        return !issue.isSubTask();
    }

    public boolean hasValue(Issue issue)
    {
        return (issue.getSecurityLevel() != null);
    }

    ////////////////////////////////////////////NavigableField implementation ////////////////////////////////////////
    public String getColumnHeadingKey()
    {
        return "issue.column.heading.securitylevel";
    }

    public String getDefaultSortOrder()
    {
        return NavigableField.ORDER_ASCENDING;
    }

    public LuceneFieldSorter getSorter()
    {
        return securityLevelStatisticsMapper;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map<String, Object> velocityParams = getVelocityParams(getAuthenticationContext().getI18nHelper(), displayParams, issue);
        velocityParams.put(getId(), issue.getSecurityLevel());
        return renderTemplate("securitylevel-columnview.vm", velocityParams);
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
       // Get all the security levels
        Collection issueSecurityLevels = issueSecurityLevelManager.getUsersSecurityLevels(fieldTypeInfoContext.getIssueContext().getProjectObject(), getAuthenticationContext().getLoggedInUser());
        Collection<SecurityLevelJsonBean> allowedValues = SecurityLevelJsonBean.shortBeans(issueSecurityLevels, jiraBaseUrls);

        return new FieldTypeInfo(allowedValues, null);
    }

    @Override
    public JsonType getJsonSchema()
    {
        return getSecurityLevelJsonSchema();
    }
    public static JsonType getSecurityLevelJsonSchema()
    {
        return JsonTypeBuilder.system(JsonType.SECURITY_LEVEL_TYPE, IssueFieldConstants.SECURITY);
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(Issue issue, boolean renderedVersionRequired, FieldLayoutItem fieldLayoutItem)
    {
        Long securityLevelId = issue.getSecurityLevelId();
        if (securityLevelId == null)
        {
            return new FieldJsonRepresentation(null);
        }
        IssueSecurityLevel securityLevel = issueSecurityLevelManager.getSecurityLevel(securityLevelId);
        return new FieldJsonRepresentation(new JsonData(SecurityLevelJsonBean.shortBean(securityLevel, jiraBaseUrls)));
    }

    @Override
    public RestFieldOperationsHandler getRestFieldOperation()
    {
        return new SecurityLevelRestFieldOperationsHandler(issueSecurityLevelManager, authenticationContext, authenticationContext.getI18nHelper());
    }

    @Override
    public JsonData getJsonDefaultValue(IssueContext issueCtx)
    {
        GenericValue gv = getDefaultSecurityLevel(issueCtx);
        if (gv != null)
        {
            // All Json Ids in JIRA are strings so ........
            return new JsonData(EasyMap.build("id", gv.get("id").toString()));
        }
        return null;
    }
}
