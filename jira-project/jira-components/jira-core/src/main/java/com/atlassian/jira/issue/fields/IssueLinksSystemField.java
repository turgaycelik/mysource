package com.atlassian.jira.issue.fields;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.bc.issue.link.IssueLinkService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
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
import com.atlassian.jira.issue.fields.rest.IssueFinder;
import com.atlassian.jira.issue.fields.rest.IssueLinkTypeFinder;
import com.atlassian.jira.issue.fields.rest.IssueLinksRestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.RestAwareField;
import com.atlassian.jira.issue.fields.rest.RestFieldOperations;
import com.atlassian.jira.issue.fields.rest.RestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinksBeanBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinksBeanBuilderFactory;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.link.IssueLinkDisplayHelper;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.LinkCollection;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.parameters.lucene.sort.MappedSortComparator;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.I18nBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.SortField;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.security.Permissions.LINK_ISSUE;

public class IssueLinksSystemField extends AbstractOrderableField
        implements HideableField, RequirableField, NavigableField, RestAwareField, RestFieldOperations
{
    public static final String PARAMS_ISCREATEISSUE = "isCreateIssue";
    public static final String PARAMS_LINK_TYPE = "issuelinks-linktype";
    public static final String PARAMS_ISSUE_KEYS = "issuelinks-issues";

    private static final String NAME_KEY = "issue.field.issuelinks";

    private final IssueLinkService issueLinkService;
    private final IssueLinkManager issueLinkManager;
    private final UserHistoryManager userHistoryManager;
    private final IssueLinksBeanBuilderFactory issueLinkBeanBuilderFactory;
    private final I18nBean.BeanFactory i18nFactory;
    private final IssueLinkTypeFinder issueLinkTypeFinder;
    private final JiraBaseUrls jiraBaseUrls;

    public IssueLinksSystemField(
            final VelocityTemplatingEngine templatingEngine,
            final ApplicationProperties applicationProperties,
            final JiraAuthenticationContext authenticationContext,
            final PermissionManager permissionManager,
            final IssueLinkService issueLinkService,
            final UserHistoryManager userHistoryManager,
            final IssueLinkManager issueLinkManager,
            final IssueLinksBeanBuilderFactory issueLinkBeanBuilderFactory,
            final I18nBean.BeanFactory i18nFactory,
            final IssueLinkTypeFinder issueLinkTypeFinder,
            final JiraBaseUrls jiraBaseUrls)
    {
        super(IssueFieldConstants.ISSUE_LINKS, NAME_KEY, templatingEngine, applicationProperties, authenticationContext, permissionManager, null);
        this.issueLinkService = issueLinkService;
        this.userHistoryManager = userHistoryManager;
        this.issueLinkManager = issueLinkManager;
        this.issueLinkBeanBuilderFactory = issueLinkBeanBuilderFactory;
        this.i18nFactory = i18nFactory;
        this.issueLinkTypeFinder = issueLinkTypeFinder;
        this.jiraBaseUrls = jiraBaseUrls;
    }

    public LuceneFieldSorter getSorter()
    {
        return null;
    }

    @Override
    public List<SortField> getSortFields(boolean sortOrder)
    {
        return Collections.emptyList();
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, null, issue, displayParams);
        LinkCollection linkCollection = issueLinkManager.getLinkCollection(issue, authenticationContext.getLoggedInUser());
        velocityParams.put("linkedIssues", linkCollection.getAllIssues());
        velocityParams.put("applicationProperties", getApplicationProperties());
        return renderTemplate("issuelinks-columnview.vm", velocityParams);
    }

    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        return getCreateOrEditHtml(fieldLayoutItem, operationContext, action, issue, displayParameters, Boolean.TRUE);
    }

    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        return getCreateOrEditHtml(fieldLayoutItem, operationContext, action, issue, displayParameters, Boolean.FALSE);
    }

    private String getCreateOrEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters, final Boolean create)
    {
        IssueLinkDisplayHelper issueLinkDisplayHelper = new IssueLinkDisplayHelper(userHistoryManager, authenticationContext.getLoggedInUser());

        Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        velocityParams.put(PARAMS_ISCREATEISSUE, create);
        final Object value = operationContext.getFieldValuesHolder().get(getId());
        velocityParams.put("value", value);
        velocityParams.put("linkTypes", issueLinkDisplayHelper.getSortedIssueLinkTypes(issueLinkService.getIssueLinkTypes()));

        velocityParams.put("selectedLinkType", issueLinkDisplayHelper.getLastUsedLinkType());
        if (value != null && value instanceof IssueLinkingValue)
        {
            //JRADEV-7325: if the fieldvaluesholder contains a value and it contains a linkdescription set that as the
            //currently selected value!
            final IssueLinkingValue ilv = (IssueLinkingValue) value;
            if (StringUtils.isNotBlank(ilv.getLinkDescription()))
            {
                velocityParams.put("selectedLinkType", ilv.getLinkDescription());
            }
        }

        return renderTemplate("issuelinks-edit.vm", velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters)
    {
        Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        populateFromIssue(velocityParams, issue);
        return getViewVelocityTemplate(velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Object value, Map displayParameters)
    {
        Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, action, null, displayParameters);
        velocityParams.put(getId(), value);
        return getViewVelocityTemplate(velocityParams);
    }

    private String getViewVelocityTemplate(Map velocityParams)
    {
        return renderTemplate("issuelinking-view.vm", velocityParams);
    }

    @Override
    public String getBulkEditHtml(final OperationContext operationContext, final Action action, final BulkEditBean bulkEditBean, final Map displayParameters)
    {
        return "TODO";
    }

    public void createValue(Issue issue, Object value)
    {
        if (isIssueLinkingEnabled())
        {
            IssueLinkingValue issueLinkingValue = (IssueLinkingValue) value;
            IssueLinkService.AddIssueLinkValidationResult validationResult = issueLinkingValue.getValidationResult();
            if (validationResult != null && validationResult.isValid())
            {
                issueLinkService.addIssueLinks(authenticationContext.getLoggedInUser(), validationResult);
            }
        }
    }


    public Object getDefaultValue(Issue issue)
    {
        return null;
    }

    /**
     * We don't return any default for the field.
     *
     * @param fieldValuesHolder The fieldValuesHolder Map to be populated.
     * @param issue The issue in play.
     */
    public void populateDefaults(Map<String, Object> fieldValuesHolder, Issue issue)
    {
        //noinspection unchecked
        fieldValuesHolder.put(getId(), new IssueLinkingValue.Builder().setIssueLinkingEnabled(isIssueLinkingEnabled()).build());
    }


    /**
     * This is called by Jelly code to map a value into a field values holder.
     *
     * @param fieldValuesHolder Map of field Values.
     * @param stringValue user friendly string value.
     * @param issue the issue in play.
     */
    public void populateParamsFromString(Map<String, Object> fieldValuesHolder, String stringValue, Issue issue)
            throws FieldValidationException
    {
    }


    public void populateForMove(Map<String, Object> fieldValuesHolder, Issue originalIssue, Issue targetIssue)
    {
        // If the field need to be moved then it does not have a current value, so populate the default value
        // which is null in our case
    }

    /**
     * This is implemented in response to use being an AbstractOrderableField.  It is actually called via
     * populateFromParams so that we can place our relevant value object into the field values holder map.  See above
     * for the code entry point.
     *
     * @param inputParameters the input parameters.
     * @return the object to be placed into a field values holder map under our id.
     */
    protected Object getRelevantParams(Map<String, String[]> inputParameters)
    {
        IssueLinkingValue.Builder builder = new IssueLinkingValue.Builder();
        builder.setIssueLinkingEnabled(isIssueLinkingEnabled());
        builder.setCreateIssue(inputParameters.get(PARAMS_ISCREATEISSUE));
        builder.setLinkDescription(inputParameters.get(PARAMS_LINK_TYPE));
        builder.setLinkedIssues(inputParameters.get(PARAMS_ISSUE_KEYS));
        return builder.build();
    }

    /**
     * This is called to populate the field values holder with the current state of the Issue object.  For example this
     * will be called when the issue is edited.
     *
     * @param fieldValuesHolder The fieldValuesHolder Map to be populated.
     * @param issue The issue in play.
     */
    public void populateFromIssue(Map<String, Object> fieldValuesHolder, Issue issue)
    {
        IssueLinkingValue.Builder valueBuilder = new IssueLinkingValue.Builder();
        valueBuilder.setIssueLinkingEnabled(isIssueLinkingEnabled());
        //noinspection unchecked
        fieldValuesHolder.put(getId(), valueBuilder.build());
    }

    public void validateParams(OperationContext operationContext, ErrorCollection errorCollection, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        IssueLinkingValue value = (IssueLinkingValue) operationContext.getFieldValuesHolder().get(getId());

        if (isIssueLinkingEnabled() && value != null && !value.getLinkedIssues().isEmpty())
        {
            IssueLinkService.AddIssueLinkValidationResult issueLinkValidationResult = issueLinkService.validateAddIssueLinks(authenticationContext.getLoggedInUser(), issue, value.getLinkDescription(), value.getLinkedIssues());

            // if we were returned an updated value, that signifies that we must update the TimeTrackingValue in the FieldValuesHolder
            if (issueLinkValidationResult.isValid())
            {
                //noinspection unchecked
                operationContext.getFieldValuesHolder().put(getId(), new IssueLinkingValue.Builder(issueLinkValidationResult).build());
            }
            else
            {
                transferErrorMessages(errorCollection, issueLinkValidationResult.getErrorCollection().getErrorMessages());
                transferErrorMessages(errorCollection, issueLinkValidationResult.getErrorCollection().getErrors().values());
            }
        }
    }

    private void transferErrorMessages(ErrorCollection errorCollection, Collection<String> errorMessages)
    {
        for (String errMsg : errorMessages)
        {
            errorCollection.addError(getId(), errMsg);
        }
    }


    /**
     * This is called from BulkEdit/BulkWorkflowTransition to get an value object from a input set of values.
     *
     * @param fieldValueHolder the map of parameters.
     * @return a parsed long or null if not in the input.
     */
    public Object getValueFromParams(Map fieldValueHolder)
    {
        return fieldValueHolder.get(getId());
    }

    /**
     * <p> This is called to back update the MutableIssue with the value object we previously stuffed into the field
     * values holder. <p/> <p>This is called prior to the issue being stored on disk.</p>
     *
     * @param fieldLayoutItem FieldLayoutItem in play.
     * @param issue MutableIssue in play.
     * @param fieldValueHolder Field Value Holder Map.
     */
    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        IssueLinkingValue newValue = (IssueLinkingValue) getValueFromParams(fieldValueHolder);
        if (newValue == null)
        {
            return; // belts and braces.  We don't ever expect this
        }
        if (isIssueLinkingEnabled())
        {
            issue.setExternalFieldValue(getId(), null, newValue);
        }
    }


    /**
     * This is called after the issue has been stored on disk and allows us a chance to create change records for the
     * update.
     *
     * @param fieldLayoutItem for this field within this context.
     * @param issue Issue this field is part of.
     * @param modifiedValue new value to set field to. Cannot be null.
     * @param issueChangeHolder an object to record any changes made to the issue by this method.
     */
    public void updateValue(FieldLayoutItem fieldLayoutItem, Issue issue, ModifiedValue modifiedValue, IssueChangeHolder issueChangeHolder)
    {
        Object newValue = modifiedValue.getNewValue();
        if (newValue != null)
        {
            if (isIssueLinkingEnabled())
            {
                IssueLinkingValue issueLinkingValue = (IssueLinkingValue) newValue;
                IssueLinkService.AddIssueLinkValidationResult validationResult = issueLinkingValue.getValidationResult();
                if (validationResult != null && validationResult.isValid())
                {
                    issueLinkService.addIssueLinks(authenticationContext.getLoggedInUser(), validationResult);
                }
            }
        }
    }


    public MessagedResult needsMove(Collection originalIssues, Issue targetIssue, FieldLayoutItem targetFieldLayoutItem)
    {
        return new MessagedResult(false);
    }

    public void removeValueFromIssueObject(MutableIssue issue)
    {
    }

    public boolean isShown(Issue issue)
    {
        return isIssueLinkingEnabled() && hasPermission(issue, LINK_ISSUE);
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return true;
    }

    public boolean hasValue(Issue issue)
    {
        return false;
    }

    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        return "bulk.edit.unavailable";
    }


    /* ===========================
     * Simple implemenation methods of NavigableField since we are no longer derived from NavigableFieldImpl
     * =========================== */

    @Override
    public String getColumnHeadingKey()
    {
        return "issue.column.heading.issuelinks";
    }

    @Override
    public String getColumnCssClass()
    {
        return getId();
    }

    @Override
    public String getDefaultSortOrder()
    {
        return null;
    }

    @Override
    public FieldComparatorSource getSortComparatorSource()
    {
        final LuceneFieldSorter sorter = getSorter();
        if (sorter == null)
        {
            return null;
        }
        else
        {
            return new MappedSortComparator(sorter);
        }
    }

    @Override
    public String getHiddenFieldId()
    {
        return null;
    }

    @Override
    public String prettyPrintChangeHistory(final String changeHistory)
    {
        return changeHistory;
    }

    @Override
    public String prettyPrintChangeHistory(final String changeHistory, final I18nHelper i18nHelper)
    {
        return changeHistory;
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        String issueLinksAutoCompleteUrl;

        if (fieldTypeInfoContext.getIssue() == null)
        {
            issueLinksAutoCompleteUrl = String.format("%s/rest/api/1.0/issues/picker?currentProjectId=%s&showSubTaskParent=true&showSubTasks=true&currentIssueKey=&query=", jiraBaseUrls.baseUrl(), fieldTypeInfoContext.getIssueContext().getProjectObject().getId());
        }
        else
        {
            issueLinksAutoCompleteUrl = String.format("%s/rest/api/1.0/issues/picker?currentProjectId=&showSubTaskParent=true&showSubTasks=true&currentIssueKey=%s&query=", jiraBaseUrls.baseUrl(), fieldTypeInfoContext.getIssue().getKey());
        }
        return new FieldTypeInfo(null, issueLinksAutoCompleteUrl);
    }

    @Override
    public JsonType getJsonSchema()
    {
        return JsonTypeBuilder.systemArray(JsonType.ISSUELINKS_TYPE, IssueFieldConstants.ISSUE_LINKS);
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(Issue issue, boolean renderedVersionRequired, FieldLayoutItem fieldLayoutItem)
    {
        if (!issueLinkManager.isLinkingEnabled())
        {
            return null;
        }

        IssueLinksBeanBuilder builder = issueLinkBeanBuilderFactory.newIssueLinksBeanBuilder(issue);

        return new FieldJsonRepresentation(new JsonData(builder.buildIssueLinks()));
    }

    @Override
    public RestFieldOperationsHandler getRestFieldOperation()
    {
        // this guy needs the IssueService and the IssueService needs the IssueLinksSystemField. so let's break up
        // the circular dependency...
        IssueFinder issueFinder = ComponentAccessor.getComponent(IssueFinder.class);

        return new IssueLinksRestFieldOperationsHandler(i18nFactory.getInstance(authenticationContext.getLoggedInUser()), issueLinkTypeFinder, issueFinder);
    }

    @Override
    public JsonData getJsonDefaultValue(IssueContext issueCtx)
    {
        return null;
    }

    private boolean isIssueLinkingEnabled()
    {
        return getApplicationProperties().getOption(APKeys.JIRA_OPTION_ISSUELINKING);
    }


    /**
     * <p>This interface is used as a value object for IssueLinking information.<p/> <p> It lurks around inside the
     * field values holder maps while JIRA does its thang.  It's referenced by the velocity views and also by the
     * IssueLinksSystemField itself. <p/> <p> While the class is PUBLIC, it is only so that the Velocity template can
     * get to it.  Please do not consider this part of the JIRA API.  It's for the IssueLinksSystemField only.  You have
     * been warned :) <p/>
     */
    @Internal
    public static interface IssueLinkingValue
    {
        boolean isCreateIssue();

        boolean isIssueLinkingActivated();

        public String getLinkDescription();

        public List<String> getLinkedIssues();

        public IssueLinkService.AddIssueLinkValidationResult getValidationResult();

        public static class Builder
        {
            private String linkDesc = null;
            private List<String> linkedIssues;
            private boolean isIssueLinkingEnabled;
            private boolean isCreateIssue = false;
            private IssueLinkService.AddIssueLinkValidationResult validationResult;

            Builder()
            {
            }

            Builder(IssueLinkService.AddIssueLinkValidationResult validationResult)
            {
                this.validationResult = validationResult;
            }

            Builder setCreateIssue(String[] createIssue)
            {
                final String s = fromArray(createIssue);
                this.isCreateIssue = StringUtils.isNotBlank(s) ? Boolean.valueOf(s) : false;
                return this;
            }

            Builder setIssueLinkingEnabled(boolean enabled)
            {
                this.isIssueLinkingEnabled = enabled;
                return this;
            }

            Builder setLinkDescription(String[] value)
            {
                linkDesc = fromArray(value);
                return this;
            }


            Builder setLinkedIssues(String[] value)
            {
                if (value != null)
                {
                    linkedIssues = new ArrayList<String>();
                    linkedIssues.addAll(Arrays.asList(value));
                }
                return this;
            }


            private String fromArray(final String[] value)
            {
                return value != null && value.length > 0 ? value[0] : null;
            }

            IssueLinkingValue build()
            {
                final boolean isCreateIssue = this.isCreateIssue;
                final boolean isIssueLinkingEnabled = this.isIssueLinkingEnabled;
                final String linkDesc = this.linkDesc;
                final List<String> linkedIssues = this.linkedIssues == null ? Collections.<String>emptyList() : this.linkedIssues;

                return new IssueLinkingValue()
                {
                    public boolean isCreateIssue()
                    {
                        return isCreateIssue;
                    }

                    public boolean isIssueLinkingActivated()
                    {
                        return isIssueLinkingEnabled;
                    }

                    public String getLinkDescription()
                    {
                        return linkDesc;
                    }

                    public List<String> getLinkedIssues()
                    {
                        return linkedIssues;
                    }

                    @Override
                    public IssueLinkService.AddIssueLinkValidationResult getValidationResult()
                    {
                        return validationResult;
                    }

                    @Override
                    public boolean equals(final Object obj)
                    {
                        if (this == obj)
                        {
                            return true;
                        }

                        if (!(obj instanceof IssueLinkingValue))
                        {
                            return false;
                        }

                        IssueLinkingValue rhs = (IssueLinkingValue) obj;

                        return new EqualsBuilder().
                                append(isCreateIssue, rhs.isCreateIssue()).
                                append(isIssueLinkingEnabled, rhs.isIssueLinkingActivated()).
                                append(linkDesc, rhs.getLinkDescription()).
                                append(linkedIssues, rhs.getLinkDescription()).
                                isEquals();
                    }

                    @Override
                    public int hashCode()
                    {
                        return new HashCodeBuilder(17, 31).
                                append(isIssueLinkingEnabled).
                                append(isCreateIssue).
                                append(linkDesc).
                                append(linkedIssues).
                                toHashCode();
                    }

                    @Override
                    public String toString()
                    {
                        return new ToStringBuilder(this).
                                append("isIssueLinkingEnabled", isIssueLinkingEnabled).
                                append("isCreateIssue", isCreateIssue).
                                append("linkDesc", linkDesc).
                                append("linkedIssues", linkedIssues).
                                toString();
                    }
                };
            }
        }

    }

}
