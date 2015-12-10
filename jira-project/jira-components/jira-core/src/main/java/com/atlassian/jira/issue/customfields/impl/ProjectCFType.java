package com.atlassian.jira.issue.customfields.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.imports.project.customfield.ProjectPickerCustomFieldImporter;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comparator.OfBizComparators;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.config.item.ProjectOptionsConfigItem;
import com.atlassian.jira.issue.customfields.converters.ProjectConverter;
import com.atlassian.jira.issue.customfields.impl.rest.ProjectCustomFieldOperationsHandler;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
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
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.ProjectJsonBean;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

import org.ofbiz.core.entity.GenericValue;

/**
 * Custom Field Type to select a {@link com.atlassian.jira.project.Project} on this JIRA instance.
 * <dl>
 * <dt><strong>Transport Object Type</strong></dt>
 * <dd>{@link GenericValue}</dd>
 * <dt><Strong>Database Storage Type</Strong></dt>
 * <dd>{@link Long} of Project ID</dd>
 * </dl>
 */
public class ProjectCFType extends AbstractSingleFieldType<GenericValue> implements SortableCustomField<GenericValue>, ProjectImportableCustomField, RestAwareCustomFieldType, RestCustomFieldTypeOperations
{
    private final ProjectConverter projectConverter;
    private final PermissionManager permissionManager;
    private final ProjectManager projectManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final ProjectCustomFieldImporter projectCustomFieldImporter;
    private final JiraBaseUrls jiraBaseUrls;

    public ProjectCFType(CustomFieldValuePersister customFieldValuePersister, ProjectConverter projectConverter, PermissionManager permissionManager,
            JiraAuthenticationContext jiraAuthenticationContext, GenericConfigManager genericConfigManager, JiraBaseUrls jiraBaseUrls, ProjectManager projectManager)
    {
        super(customFieldValuePersister, genericConfigManager);
        this.projectConverter = projectConverter;
        this.permissionManager = permissionManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.jiraBaseUrls = jiraBaseUrls;
        this.projectManager = projectManager;
        this.projectCustomFieldImporter = new ProjectPickerCustomFieldImporter();
    }

    public int compare(@Nonnull GenericValue customFieldObjectValue1, @Nonnull GenericValue customFieldObjectValue2, FieldConfig fieldConfig)
    {
        return OfBizComparators.NAME_COMPARATOR.compare(customFieldObjectValue1, customFieldObjectValue2);
    }

    @Nonnull
    protected PersistenceFieldType getDatabaseType()
    {
        return PersistenceFieldType.TYPE_DECIMAL;
    }

    protected Object getDbValueFromObject(GenericValue customFieldObject)
    {
        if (customFieldObject == null)
            return null;

        return new Double(customFieldObject.getLong("id").longValue());
    }

    protected GenericValue getObjectFromDbValue(@Nonnull Object databaseValue) throws FieldValidationException
    {
        Double projectId = (Double) databaseValue;
        return projectConverter.getProject(new Long(projectId.intValue()));
    }

    public String getStringFromSingularObject(GenericValue customFieldObject)
    {
        return projectConverter.getString(customFieldObject);
    }

    public GenericValue getSingularObjectFromString(String string) throws FieldValidationException
    {
        return projectConverter.getProject(string);
    }

    @Nonnull
    public List<FieldConfigItemType> getConfigurationItemTypes()
    {
        final List<FieldConfigItemType> configurationItemTypes = super.getConfigurationItemTypes();
        configurationItemTypes.add(new ProjectOptionsConfigItem(projectConverter, permissionManager, jiraAuthenticationContext));
        return configurationItemTypes;
    }

    public String getChangelogString(CustomField field, GenericValue value)
    {
        if(value == null)
            return null;
        else
            return value.getString("name");
    }

    @Nonnull
    public Map<String, Object> getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem)
    {
        final Map<String, Object> params = super.getVelocityParameters(issue, field, fieldLayoutItem);
        if (issue != null)
        {
            final GenericValue project = getValueFromIssue(field, issue);
            if (project != null)
            {
                boolean hasPermission = permissionManager.hasPermission(Permissions.BROWSE, project, jiraAuthenticationContext.getLoggedInUser());
                params.put("isProjectVisible", hasPermission ? Boolean.TRUE : Boolean.FALSE);
            }
        }
        return params;
    }

    public ProjectCustomFieldImporter getProjectImporter()
    {
        return this.projectCustomFieldImporter;
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitProject(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitProject(ProjectCFType projectCustomFieldType);
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        // Get visible projects
        Collection<Project> projects = permissionManager.getProjectObjects(Permissions.BROWSE, jiraAuthenticationContext.getLoggedInUser());
        return new FieldTypeInfo(ProjectJsonBean.shortBeans(projects, jiraBaseUrls), null);
    }

    @Override
    public JsonType getJsonSchema(CustomField customField)
    {
        return JsonTypeBuilder.custom(JsonType.PROJECT_TYPE, getKey(), customField.getIdAsLong());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(CustomField field, Issue issue, boolean renderedVersionRequested, @Nullable FieldLayoutItem fieldLayoutItem)
    {
        GenericValue valueFromIssue = getValueFromIssue(field, issue);
        if (valueFromIssue == null)
        {
            return new FieldJsonRepresentation(new JsonData(null));
        }
        Project project = projectConverter.getProjectObject(valueFromIssue.getLong("id"));
        return new FieldJsonRepresentation(new JsonData(ProjectJsonBean.shortBean(project, jiraBaseUrls)));
    }

    @Override
    public RestFieldOperationsHandler getRestFieldOperation(CustomField field)
    {
        return new ProjectCustomFieldOperationsHandler(projectManager, field, getI18nBean());
    }
}
