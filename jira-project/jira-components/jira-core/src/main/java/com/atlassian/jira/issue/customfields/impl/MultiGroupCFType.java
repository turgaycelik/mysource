package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.imports.project.customfield.GroupCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.GroupSelectorField;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.converters.MultiGroupConverter;
import com.atlassian.jira.issue.customfields.impl.rest.GroupCustomFieldOperationsHandler;
import com.atlassian.jira.issue.customfields.impl.rest.MultiGroupCustomFieldOperationsHandler;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.UserField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.RestAwareCustomFieldType;
import com.atlassian.jira.issue.fields.rest.RestCustomFieldTypeOperations;
import com.atlassian.jira.issue.fields.rest.RestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.GroupJsonBeanBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.AbstractCustomFieldIndexer;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.util.GroupNameComparator;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;
import javax.annotation.Nonnull;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.google.common.collect.Lists;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * <p>Multiple User Group Select Type</p>
 * <dl>
 * <dt><strong>Transport Object Type</strong></dt>
 * <dd>{@link java.util.Collection} of {@link Group}s</dd>
 *  <dt><strong>Singular Object Type</strong></dt>
 * <dd>{@link Group}</dd>
 * <dt><Strong>Database Storage Type</Strong></dt>
 * <dd>{@link String} of group name</dd>
 * </dl>
 */
public class MultiGroupCFType extends AbstractMultiCFType<Group> implements GroupSelectorField, ProjectImportableCustomField, UserField, SortableCustomField<String>, RestAwareCustomFieldType, RestCustomFieldTypeOperations
{
    protected final MultiGroupConverter multiGroupConverter;
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authenticationContext;
    private final FieldVisibilityManager fieldVisibilityManager;
    private static final Comparator<Group> NAME_COMPARATOR = new GroupNameComparator();

    private static final String MULTIPLE_PARAM_KEY = "multiple";
    private final GroupCustomFieldImporter groupCustomFieldImporter;
    private final JiraBaseUrls jiraBaseUrls;


    public MultiGroupCFType(final CustomFieldValuePersister customFieldValuePersister, final GenericConfigManager genericConfigManager, final MultiGroupConverter multiGroupConverter, final PermissionManager permissionManager, final JiraAuthenticationContext authenticationContext, final GroupManager groupManager, final FieldVisibilityManager fieldVisibilityManager, JiraBaseUrls jiraBaseUrls)

    {
        super(customFieldValuePersister, genericConfigManager);
        this.multiGroupConverter = multiGroupConverter;
        this.permissionManager = permissionManager;
        this.authenticationContext = authenticationContext;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.jiraBaseUrls = jiraBaseUrls;
        groupCustomFieldImporter = new GroupCustomFieldImporter(groupManager);
    }

    @Override
    protected Comparator<Group> getTypeComparator()
    {
        return NAME_COMPARATOR;
    }

    @Override
    public Collection<Group> getValueFromCustomFieldParams(final CustomFieldParams parameters) throws FieldValidationException
    {
        final Collection<Group> groupSet = new HashSet<Group>();
        final Collection values = parameters.getValuesForNullKey();
        if ((values == null) || values.isEmpty())
        {
            return null;
        }
        for (final Iterator i = values.iterator(); i.hasNext();)
        {
            Collection groupNames;
            if (isMultiple())
            {
                groupNames = multiGroupConverter.extractGroupStringsFromString((String) i.next());
            }
            else
            {
                groupNames = Lists.newArrayList(i.next());
            }

            groupSet.addAll(convertDbObjectToTypes(groupNames));
        }
        final List<Group> l = new ArrayList<Group>(groupSet);
        Collections.sort(l, NAME_COMPARATOR);
        return l;
    }

    @Override
    public Object getStringValueFromCustomFieldParams(final CustomFieldParams parameters)
    {
        final Collection groups = parameters.getValuesForNullKey();
        if ((groups == null) || groups.isEmpty())
        {
            return null;
        }

        return putInvalidGroupsAtFront(groups);
    }

    @Override
    public void validateFromParams(final CustomFieldParams relevantParams, final ErrorCollection errorCollectionToAddTo, final FieldConfig config)
    {
        final StringBuilder errors = new StringBuilder();
        Collection<String> groupStrings;
        String singleParam;
        for (final Object o : relevantParams.getValuesForNullKey())
        {
            singleParam = (String) o;

            if (isMultiple())
            {
                groupStrings = multiGroupConverter.extractGroupStringsFromString(singleParam);
            }
            else
            {
                groupStrings = Lists.newArrayList(singleParam);
            }

            if (groupStrings == null)
            {
                return;
            }
            for (String groupString : groupStrings)
            {
                try
                {
                    multiGroupConverter.getGroup(groupString);
                }
                catch (final FieldValidationException e)
                {
                    if (errors.length() > 0)
                    {
                        errors.append(", ");
                    }

                    errors.append(groupString);
                }
            }

            if ((errors != null) && (errors.length() > 0))
            {
                String message;
                if (isMultiple())
                {
                    message = getI18nBean().getText("admin.errors.could.not.find.groupnames", errors);
                }
                else
                {
                    message = getI18nBean().getText("admin.errors.could.not.find.groupname", errors);
                }

                errorCollectionToAddTo.addError(config.getCustomField().getId(), message, Reason.VALIDATION_FAILED);
            }
        }
    }

    @Override
    public String getStringFromSingularObject(final Group o)
    {
        return multiGroupConverter.getString(o);
    }

    @Override
    public Group getSingularObjectFromString(final String s) throws FieldValidationException
    {
        return multiGroupConverter.getGroup(s);
    }

    @Override
    public List<FieldIndexer> getRelatedIndexers(final CustomField customField)
    {
        return Lists.<FieldIndexer>newArrayList(new MultiGroupCustomFieldIndexer(fieldVisibilityManager, customField, multiGroupConverter));
    }

    @Nonnull
    @Override
    public Map<String, Object> getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem)
    {
        Map<String, Object> params = super.getVelocityParameters(issue, field, fieldLayoutItem);
        if (params == null)
        {
            params = new HashMap<String, Object>();
        }

        params.put("hasAdminPermission", Boolean.valueOf(permissionManager.hasPermission(Permissions.ADMINISTER, authenticationContext.getUser())));
        params.put("multiple", Boolean.valueOf(isMultiple()));
        return params;
    }

    @Override
    protected Object convertTypeToDbValue(Group value)
    {
        return multiGroupConverter.getString(value);
    }

    @Override
    protected Group convertDbValueToType(Object dbValue)
    {
        return multiGroupConverter.getGroup((String) dbValue);
    }

    @Nonnull
    @Override
    protected PersistenceFieldType getDatabaseType()
    {
        return PersistenceFieldType.TYPE_LIMITED_TEXT;
    }

    private Collection<String> putInvalidGroupsAtFront(final Collection<String> groups)
    {
        final Set<String> retSet = new HashSet<String>();
        final StringBuffer errorString = new StringBuffer();
        if (groups != null)
        {
            for (final String groupList : groups)
            {
                if (isMultiple())
                {
                    for (final String groupString : multiGroupConverter.extractGroupStringsFromString(groupList))
                    {
                        populateGroupString(groupString, retSet, errorString);
                    }
                }
                else
                {
                    populateGroupString(groupList, retSet, errorString);
                }
            }
        }

        final List<String> l = new ArrayList<String>(retSet);
        Collections.sort(l);
        if (errorString.length() > 0)
        {
            l.add(0, errorString.toString());
        }
        return l;
    }

    private void populateGroupString(final String groupString, final Set<String> retSet, final StringBuffer errorString)
    {
        try
        {
            multiGroupConverter.getGroup(groupString);
            retSet.add(groupString);
        }
        catch (final FieldValidationException e)
        {
            if (errorString.length() > 0)
            {
                errorString.append(", ");
            }

            errorString.append(groupString);
        }
    }

    public boolean isMultiple()
    {
        return Boolean.valueOf(getDescriptor().getParams().get(MULTIPLE_PARAM_KEY)).booleanValue();
    }

    //------------------------------------------------------------------------------------------------------------------
    //  Implementation of ProjectImportableCustomField
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public ProjectCustomFieldImporter getProjectImporter()
    {
        return groupCustomFieldImporter;
    }

    public Query getQueryForGroup(final String fieldName, String groupName)
    {
        return new TermQuery(new Term(fieldName,groupName));
    }

    public int compare(@Nonnull final String customFieldObjectValue1, @Nonnull final String customFieldObjectValue2, final FieldConfig fieldConfig)
    {
        return customFieldObjectValue1.compareTo(customFieldObjectValue2);
    }
    //------------------------------------------------------------------------------------------------------------------

    static class MultiGroupCustomFieldIndexer extends AbstractCustomFieldIndexer
    {
        private final CustomField customField;
        private final MultiGroupConverter multiGroupConverter;

        public MultiGroupCustomFieldIndexer(final FieldVisibilityManager fieldVisibilityManager, final CustomField customField, final MultiGroupConverter multiGroupConverter)
        {
            super(fieldVisibilityManager, customField);
            this.customField = customField;
            this.multiGroupConverter = multiGroupConverter;
        }

        @Override
        public void addDocumentFieldsSearchable(final Document doc, final Issue issue)
        {
            addDocumentFields(doc, issue, Field.Index.NOT_ANALYZED_NO_NORMS);
        }

        @Override
        public void addDocumentFieldsNotSearchable(final Document doc, final Issue issue)
        {
            addDocumentFields(doc, issue, Field.Index.NO);
        }

        void addDocumentFields(final Document doc, final Issue issue, final Field.Index indexType)
        {
            @SuppressWarnings("unchecked")
            final List<Group> o = (List) customField.getValue(issue);
            if (o != null)
            {
                for (final Group group : o)
                {
                    doc.add(new Field(getDocumentFieldId(), multiGroupConverter.getString(group), Field.Store.YES, indexType));
                }
            }
        }
    }


    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitMultiGroup(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitMultiGroup(MultiGroupCFType multiGroupCustomFieldType);
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        return new FieldTypeInfo(null, null);
    }

    @Override
    public JsonType getJsonSchema(CustomField customField)
    {
        if (isMultiple())
        {
            return JsonTypeBuilder.customArray(JsonType.GROUP_TYPE, getKey(), customField.getIdAsLong());
        }
        else
        {
            return JsonTypeBuilder.custom(JsonType.GROUP_TYPE, getKey(), customField.getIdAsLong());
        }
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(CustomField field, Issue issue, boolean renderedVersionRequested, @Nullable FieldLayoutItem fieldLayoutItem)
    {
        Collection<Group> groups = getValueFromIssue(field, issue);
        return new FieldJsonRepresentation(groupsToJsonData(groups));
    }

    private JsonData groupsToJsonData(Collection<Group> groups)
    {
        if (groups == null)
        {
            return new JsonData(null);
        }
        if (isMultiple())
        {
            return new JsonData(GroupJsonBeanBuilder.buildBeans(groups, jiraBaseUrls));
        }
        else
        {
            if (groups.isEmpty())
            {
                return new JsonData(null);
            }
            else
            {
                return new JsonData(new GroupJsonBeanBuilder(jiraBaseUrls).group(groups.iterator().next()).build());
            }
        }
    }

    @Override
    public RestFieldOperationsHandler getRestFieldOperation(CustomField field)
    {
        return isMultiple() ? new MultiGroupCustomFieldOperationsHandler(field, getI18nBean()) : new GroupCustomFieldOperationsHandler(field, getI18nBean());
    }

    public JsonData getJsonDefaultValue(IssueContext issueCtx, CustomField field)
    {
        FieldConfig config = field.getRelevantConfig(issueCtx);
        Collection<Group> groups = getDefaultValue(config);
        return groupsToJsonData(groups);
    }

}
