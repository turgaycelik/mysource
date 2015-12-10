package com.atlassian.jira.issue.fields;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.action.issue.customfields.MockCustomFieldType;
import com.atlassian.jira.admin.RenderableProperty;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.RestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchHandler;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.memory.MemoryPropertySet;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.SortField;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple implementation of {@link com.atlassian.jira.issue.fields.CustomField} for.
 *
 * @since v4.1
 */
public class MockCustomField implements CustomField
{
    private String id;
    private String name;
    private String description;
    private CustomFieldType type;

    public MockCustomField()
    {
    }

    public MockCustomField(String id, String name, CustomFieldType customFieldType)
    {
        this.id = id;
        this.name = name;
        this.type = customFieldType;
    }

    public boolean isInScope(final Project project, final List<String> issueTypeIds)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isInScopeForSearch(final Project project, final List<String> issueTypeIds)
    {
        throw new UnsupportedOperationException();
    }

    public boolean isInScope(final GenericValue project, final List<String> issueTypeIds)
    {
        throw new UnsupportedOperationException();
    }

    public boolean isInScope(final SearchContext searchContext)
    {
        throw new UnsupportedOperationException();
    }

    public boolean isInScope(final User user, final SearchContext searchContext)
    {
        throw new UnsupportedOperationException();
    }

    public GenericValue getGenericValue()
    {
        throw new UnsupportedOperationException();
    }

    public int compare(final Issue issue1, final Issue issue2) throws IllegalArgumentException
    {
        throw new UnsupportedOperationException();
    }

    public CustomFieldParams getCustomFieldValues(final Map customFieldValuesHolder)
    {
        throw new UnsupportedOperationException();
    }

    public Object getValue(final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public Set<Long> remove()
    {
        throw new UnsupportedOperationException();
    }

    public Options getOptions(final String key, final JiraContextNode jiraContextNode)
    {
        throw new UnsupportedOperationException();
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    @Nonnull
    @Override
    public RenderableProperty getDescriptionProperty()
    {
        return new RenderableProperty()
        {
            @Override
            public String getViewHtml()
            {
                return "view-" + description;
            }

            @Override
            public String getEditHtml(String fieldName)
            {
                return "edit-" + description;
            }

            @Override
            public String getDescriptionHtml()
            {
                return "desc-" + description;
            }
        };
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    public CustomFieldSearcher getCustomFieldSearcher()
    {
        throw new UnsupportedOperationException();
    }

    public void setCustomFieldSearcher(final CustomFieldSearcher searcher)
    {
        throw new UnsupportedOperationException();
    }

    public void store()
    {
        throw new UnsupportedOperationException();
    }

    public boolean isEditable()
    {
        throw new UnsupportedOperationException();
    }

    public Long getIdAsLong()
    {
        return new Long(id);
    }

    public List<FieldConfigScheme> getConfigurationSchemes()
    {
        throw new UnsupportedOperationException();
    }

    public Options getOptions(final String key, final FieldConfig config, final JiraContextNode contextNode)
    {
        throw new UnsupportedOperationException();
    }

    public FieldConfig getRelevantConfig(final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public void validateFromActionParams(final Map actionParameters, final ErrorCollection errorCollection, final FieldConfig config)
    {
        throw new UnsupportedOperationException();
    }

    public List<GenericValue> getAssociatedProjectCategories()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ProjectCategory> getAssociatedProjectCategoryObjects()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<FieldConfigItemType> getConfigurationItemTypes()
    {
        throw new UnsupportedOperationException();
    }

    public List<GenericValue> getAssociatedProjects()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Project> getAssociatedProjectObjects()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<GenericValue> getAssociatedIssueTypes()
    {
        throw new UnsupportedOperationException();
    }

    public boolean isGlobal()
    {
        throw new UnsupportedOperationException();
    }

    public boolean isAllProjects()
    {
        throw new UnsupportedOperationException();
    }

    public boolean isAllIssueTypes()
    {
        throw new UnsupportedOperationException();
    }

    public boolean isEnabled()
    {
        throw new UnsupportedOperationException();
    }

    public CustomFieldType getCustomFieldType()
    {
        return type;
    }

    public MockCustomField setCustomFieldType(CustomFieldType type)
    {
        this.type = type;
        return this;
    }

    public MockCustomFieldType createCustomFieldType()
    {
        final MockCustomFieldType customFieldType = new MockCustomFieldType();
        this.type = customFieldType;
        return customFieldType;
    }

    public FieldConfig getRelevantConfig(final IssueContext issueContext)
    {
        throw new UnsupportedOperationException();
    }

    public FieldConfig getReleventConfig(final SearchContext searchContext)
    {
        throw new UnsupportedOperationException();
    }

    public ClauseNames getClauseNames()
    {
        throw new UnsupportedOperationException();
    }

    public String getColumnHeadingKey()
    {
        throw new UnsupportedOperationException();
    }

    public String getColumnCssClass()
    {
        throw new UnsupportedOperationException();
    }

    public String getDefaultSortOrder()
    {
        throw new UnsupportedOperationException();
    }

    public FieldComparatorSource getSortComparatorSource()
    {
        throw new UnsupportedOperationException();
    }

    public LuceneFieldSorter getSorter()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<SortField> getSortFields(boolean sortOrder)
    {
        return Collections.emptyList();
    }

    public String getColumnViewHtml(final FieldLayoutItem fieldLayoutItem, final Map displayParams, final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public String getHiddenFieldId()
    {
        throw new UnsupportedOperationException();
    }

    public String prettyPrintChangeHistory(final String changeHistory)
    {
        throw new UnsupportedOperationException();
    }

    public String prettyPrintChangeHistory(final String changeHistory, final I18nHelper i18nHelper)
    {
        throw new UnsupportedOperationException();
    }

    public String getValueFromIssue(final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public boolean isRenderable()
    {
        throw new UnsupportedOperationException();
    }

    public String getCreateHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public String getCreateHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue, final Map displayParameters)
    {
        throw new UnsupportedOperationException();
    }

    public String getEditHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public String getEditHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue, final Map displayParameters)
    {
        throw new UnsupportedOperationException();
    }

    public String getBulkEditHtml(final OperationContext operationContext, final Action action, final BulkEditBean bulkEditBean, final Map displayParameters)
    {
        throw new UnsupportedOperationException();
    }

    public String getViewHtml(final FieldLayoutItem fieldLayoutItem, final Action action, final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public String getViewHtml(final FieldLayoutItem fieldLayoutItem, final Action action, final Issue issue, final Map displayParameters)
    {
        throw new UnsupportedOperationException();
    }

    public String getViewHtml(final FieldLayoutItem fieldLayoutItem, final Action action, final Issue issue, final Object value, final Map displayParameters)
    {
        throw new UnsupportedOperationException();
    }

    public boolean isShown(final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public void populateDefaults(final Map fieldValuesHolder, final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public boolean hasParam(Map parameters)
    {
        return false;
    }

    public void populateFromParams(final Map fieldValuesHolder, final Map parameters)
    {
        throw new UnsupportedOperationException();
    }

    public void populateFromIssue(final Map fieldValuesHolder, final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public void validateParams(final OperationContext operationContext, final ErrorCollection errorCollectionToAddTo, final I18nHelper i18n, final Issue issue, final FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        throw new UnsupportedOperationException();
    }

    public Object getDefaultValue(final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public void createValue(final Issue issue, final Object value)
    {
        throw new UnsupportedOperationException();
    }

    public void updateValue(final FieldLayoutItem fieldLayoutItem, final Issue issue, final ModifiedValue modifiedValue, final IssueChangeHolder issueChangeHolder)
    {
        throw new UnsupportedOperationException();
    }

    public void updateIssue(final FieldLayoutItem fieldLayoutItem, final MutableIssue issue, final Map fieldValueHolder)
    {
        throw new UnsupportedOperationException();
    }

    public void removeValueFromIssueObject(final MutableIssue issue)
    {
        throw new UnsupportedOperationException();
    }

    public boolean canRemoveValueFromIssueObject(final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public MessagedResult needsMove(final Collection originalIssues, final Issue targetIssue, final FieldLayoutItem targetFieldLayoutItem)
    {
        throw new UnsupportedOperationException();
    }

    public void populateForMove(final Map<String, Object> fieldValuesHolder, final Issue originalIssue, final Issue targetIssue)
    {
        throw new UnsupportedOperationException();
    }

    public boolean hasValue(final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public String availableForBulkEdit(final BulkEditBean bulkEditBean)
    {
        throw new UnsupportedOperationException();
    }

    public Object getValueFromParams(final Map params) throws FieldValidationException
    {
        throw new UnsupportedOperationException();
    }

    public void populateParamsFromString(final Map fieldValuesHolder, final String stringValue, final Issue issue)
            throws FieldValidationException
    {
        throw new UnsupportedOperationException();
    }

    public SearchHandler createAssociatedSearchHandler()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonType getJsonSchema()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(Issue issue, boolean renderedVersionRequested, @Nullable FieldLayoutItem fieldLayoutItem)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public RestFieldOperationsHandler getRestFieldOperation()
    {
        return null;
    }

    @Override
    public JsonData getJsonDefaultValue(IssueContext issueCtx)
    {
        return null;
    }

    public String getId()
    {
        return id;
    }

    public MockCustomField setId(String id)
    {
        this.id = id;
        return this;
    }

    public String getNameKey()
    {
        throw new UnsupportedOperationException();
    }

    public String getName()
    {
        return name;
    }

    public String getFieldName()
    {
        return name;
    }

    public int compareTo(final Object o)
    {
        CustomField field = (CustomField) o;
        return id.compareTo(field.getId());
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public PropertySet getPropertySet()
    {
        return new MemoryPropertySet();
    }

    @Override
    public String getUntranslatedDescription()
    {
        return getDescription();
    }

    @Override
    public String getUntranslatedName()
    {
        return getName();
    }

    @Nonnull
    @Override
    public RenderableProperty getUntranslatedDescriptionProperty()
    {
        return getDescriptionProperty();
    }
}
