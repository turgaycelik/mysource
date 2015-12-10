package com.atlassian.jira.issue.customfields.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.imports.project.customfield.VersionCustomFieldImporter;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comparator.OfBizComparators;
import com.atlassian.jira.issue.comparator.VersionComparator;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.customfields.RequiresProjectSelectedMarker;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.config.item.VersionOptionsConfigItem;
import com.atlassian.jira.issue.customfields.impl.rest.MultiVersionCustomFieldOperationsHandler;
import com.atlassian.jira.issue.customfields.impl.rest.SingleVersionCustomFieldOperationsHandler;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.LongIdsValueHolder;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.renderer.HackyRendererType;
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
import com.atlassian.jira.issue.fields.rest.json.beans.VersionJsonBean;
import com.atlassian.jira.issue.fields.util.VersionHelperBean;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.bean.BulkEditBean;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

/**
 * Custom Field Type to select multiple {@link Version}s.
 * <dl>
 * <dt><strong>Transport Object Type</strong></dt>
 * <dd>{@link java.util.Collection} of {@link Version}s</dd>
 *  <dt><strong>Singular Object Type</strong></dt>
 * <dd>{@link Version}</dd>
 * <dt><Strong>Database Storage Type</Strong></dt>
 * <dd>{@link Long} of the Version's id</dd>
 * </dl> */
public class VersionCFType extends AbstractMultiCFType<Version> implements RequiresProjectSelectedMarker, SortableCustomField, ProjectImportableCustomField, RestAwareCustomFieldType, RestCustomFieldTypeOperations
{
    private static final Logger LOGGER = Logger.getLogger(VersionCFType.class);

    // ------------------------------------------------------------------------------------------------------- Constants
    private static final String NO_VERSION_STRING = "-1";
    private static final PersistenceFieldType DB_TYPE = PersistenceFieldType.TYPE_DECIMAL;
    private static final String MULTIPLE_PARAM_KEY = "multiple";

    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authenticationContext;
    private final VersionManager versionManager;
    private final VersionHelperBean versionHelperBean;
    private final VersionCustomFieldImporter versionCustomFieldImporter;
    private final JiraBaseUrls jiraBaseUrls;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public VersionCFType(final PermissionManager permissionManager, final JiraAuthenticationContext jiraAuthenticationContext, final VersionManager versionManager, final CustomFieldValuePersister customFieldValuePersister, final GenericConfigManager genericConfigManager, final VersionHelperBean versionHelperBean, JiraBaseUrls jiraBaseUrls)
    {
        super(customFieldValuePersister, genericConfigManager);
        this.permissionManager = permissionManager;
        authenticationContext = jiraAuthenticationContext;
        this.versionManager = versionManager;
        this.versionHelperBean = versionHelperBean;
        this.jiraBaseUrls = jiraBaseUrls;
        versionCustomFieldImporter = new VersionCustomFieldImporter();
    }

    // -------------------------------------------------------------------------------------------------- Public Methods

    public void validateFromParams(final CustomFieldParams relevantParams, final ErrorCollection errorCollectionToAddTo, final FieldConfig config)
    {
        final LongIdsValueHolder versionIds = new LongIdsValueHolder(relevantParams.getValuesForNullKey());
        if ((versionIds != null) && !versionIds.isEmpty())
        {
            versionHelperBean.validateVersionIds(versionIds, errorCollectionToAddTo, authenticationContext.getI18nHelper(), config.getFieldId());
        }

        if (versionIds != null) {
            String bad = versionIds.getInputText();
            if (!StringUtils.isEmpty(bad))
            {
                errorCollectionToAddTo.addError(config.getFieldId(), authenticationContext.getI18nHelper().getText("issue.field.versions.invalid.version.id", bad), ErrorCollection.Reason.VALIDATION_FAILED);
            }
        }
    }

    public Collection<Version> getValueFromCustomFieldParams(final CustomFieldParams parameters) throws FieldValidationException
    {
        final Collection allValues = parameters.getValuesForNullKey();
        final Collection collection = CollectionUtils.collect(allValues, new Transformer()
        {
            public Object transform(final Object input)
            {
                final String versionIdString = (String) input;
                final Long versionId = getLongFromString(versionIdString);
                return versionManager.getVersion(versionId);
            }
        });

        if (CustomFieldUtils.isCollectionNotEmpty(collection))
        {
            return collection;
        }
        else
        {
            return null;
        }
    }

    public Object getStringValueFromCustomFieldParams(final CustomFieldParams parameters)
    {
        return parameters.getValuesForNullKey();
    }

    @Override
    public String getChangelogString(final CustomField field, final Collection<Version> versions)
    {
        if (versions != null)
        {
            final StringBuilder sb = new StringBuilder();
            for (final Iterator iterator = versions.iterator(); iterator.hasNext();)
            {
                final Version version = (Version) iterator.next();
                if (version != null)
                {
                    sb.append(version.getName());

                    if (iterator.hasNext())
                    {
                        sb.append(", ");
                    }
                }
            }
            return sb.toString();
        }
        else
        {
            return null;
        }
    }

    // ----------------------------------------------------------------------------------------------------- Old Methods
    @Override
    public String getStringFromSingularObject(final Version version)
    {
        if (version == null)
        {
            return NO_VERSION_STRING;
        }
        else
        {
            return String.valueOf(version.getId());
        }
    }

    @Override
    public Version getSingularObjectFromString(final String string) throws FieldValidationException
    {
        if (StringUtils.isEmpty(string) || NO_VERSION_STRING.equals(string))
        {
            return null;
        }
        else
        {
            final Long versionId = getLongFromString(string);

            return versionManager.getVersion(versionId);
        }
    }

    public int compare(@Nonnull final Object customFieldObjectValue1, @Nonnull final Object customFieldObjectValue2, final FieldConfig fieldConfig)
    {
        // A bit, actually a lot, of a hack, but to ensure backwards compatibility test what object we have been given.
        if ((customFieldObjectValue1 instanceof GenericValue) && (customFieldObjectValue2 instanceof GenericValue))
        {
            LOGGER.debug("Comparing generic values instead of versions!");
            return OfBizComparators.NAME_COMPARATOR.compare((GenericValue) customFieldObjectValue1, (GenericValue) customFieldObjectValue2);
        }
        else if ((customFieldObjectValue1 instanceof Version) && (customFieldObjectValue2 instanceof Version))
        {
            return new VersionComparator().compare((Version) customFieldObjectValue1, (Version) customFieldObjectValue2);
        }
        else
        {
            throw new IllegalArgumentException("The objects are not of the expected type.");
        }
    }

    @Nonnull
    @Override
    public List<FieldConfigItemType> getConfigurationItemTypes()
    {
        final List<FieldConfigItemType> configurationItemTypes = super.getConfigurationItemTypes();
        configurationItemTypes.add(new VersionOptionsConfigItem(versionManager));
        return configurationItemTypes;
    }

    @Override
    public String availableForBulkEdit(final BulkEditBean bulkEditBean)
    {
        // Can bulk-edit this field only if all selected issue belong to one project
        if (bulkEditBean.isMultipleProjects())
        {
            // Let the user know that selected issues belong to more than one project so the action is not available
            return "bulk.edit.unavailable.multipleprojects";
        }

        // Ensure that the project has versions
        if (versionManager.getVersions(bulkEditBean.getSingleProject()).isEmpty())
        {
            return "bulk.edit.unavailable.noversions";
        }

        // Field specific check complete - return available for bulk edit
        // The CustomFieldImpl will perform further checks
        return null;
    }

    @Nonnull
    @Override
    public Map<String, Object> getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem)
    {
        final Map<String, Object> velocityParameters = super.getVelocityParameters(issue, field, fieldLayoutItem);

        if (issue != null)
        {
            // JRA-15007: released versions must always be reversed (descending order)
            final Collection releasedversion = versionManager.getVersionsReleasedDesc(issue.getProjectObject().getId(), false);
            final Collection unreleasedversion = versionManager.getVersionsUnreleased(issue.getProjectObject().getId(), false);
            final Collection currentlySelectedArchivedVersions = getCurrentlySelectedArchivedVersions(issue, field);

            velocityParameters.put("unknownVersionId", -1L);
            velocityParameters.put("releasedVersion", releasedversion);
            velocityParameters.put("unreleasedVersion", unreleasedversion);
            velocityParameters.put("archivedVersions", currentlySelectedArchivedVersions);
            if (fieldLayoutItem != null)
            {
                velocityParameters.put("isFrotherControl", HackyRendererType.fromKey(fieldLayoutItem.getRendererType()) == HackyRendererType.FROTHER_CONTROL);
            }
        }

        velocityParameters.put("collection", new CollectionUtils());
        velocityParameters.put("multiple", Boolean.valueOf(getDescriptor().getParams().get("multiple")));

        return velocityParameters;
    }

    public ProjectCustomFieldImporter getProjectImporter()
    {
        return versionCustomFieldImporter;
    }

    public boolean isMultiple()
    {
        return Boolean.valueOf(getDescriptor().getParams().get(MULTIPLE_PARAM_KEY)).booleanValue();
    }

    private Collection getCurrentlySelectedArchivedVersions(final Issue issue, final CustomField field)
    {
        final Collection<Version> selectedVersions = getValueFromIssue(field, issue);
        if ((selectedVersions != null) && !selectedVersions.isEmpty())
        {
            return CollectionUtils.select(selectedVersions, new Predicate()
            {
                public boolean evaluate(final Object object)
                {
                    final Version version = (Version) object;
                    return version.isArchived();
                }
            });
        }

        return Collections.EMPTY_LIST;
    }

    @Override
    protected Comparator<Version> getTypeComparator()
    {
       return new VersionComparator();
    }

    @Override
    protected Object convertTypeToDbValue(Version value)
    {
        if (value != null)
        {
            return new Double(value.getId().longValue());
        }
        else
        {
            return null;
        }
    }

    @Override
    protected Version convertDbValueToType(Object input)
    {
        if (input != null)
        {
            final Double versionIdDouble = (Double) input;
            final Long versionId = new Long(versionIdDouble.longValue());
            return versionManager.getVersion(versionId);
        }
        else
        {
            return null;
        }
    }

    @Nonnull
    @Override
    protected PersistenceFieldType getDatabaseType()
    {
        return DB_TYPE;
    }

    private Long getLongFromString(final String stringValue) throws FieldValidationException
    {
        try
        {
            return Long.valueOf(stringValue);
        }
        catch (final NumberFormatException e)
        {
            LOGGER.error(e.getMessage(), e);
            throw new FieldValidationException("Version Id is not a number '" + stringValue + "'");
        }
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitVersion(this);
        }

        return super.accept(visitor);
    }

    @Override
    public RestFieldOperationsHandler getRestFieldOperation(CustomField field)
    {
        if (isMultiple())
        {
            return new MultiVersionCustomFieldOperationsHandler(field, versionManager, authenticationContext.getI18nHelper());
        }
        return new SingleVersionCustomFieldOperationsHandler(field, versionManager, authenticationContext.getI18nHelper());
    }

    @Override
    public JsonData getJsonDefaultValue(IssueContext issueCtx, CustomField field)
    {
        Collection<Version> projects = versionManager.getVersions(issueCtx.getProjectObject());
        return new JsonData(VersionJsonBean.shortBeans(projects, jiraBaseUrls));
    }

    public interface Visitor<X> extends VisitorBase<X>
    {
        X visitVersion(VersionCFType versionCustomFieldType);
    }
    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        // Get visible projects
        Collection<Version> projects = versionManager.getVersions(fieldTypeInfoContext.getIssueContext().getProjectObject());
        return new FieldTypeInfo(VersionJsonBean.shortBeans(projects, jiraBaseUrls), null);
    }

    @Override
    public JsonType getJsonSchema(CustomField customField)
    {
        if (isMultiple())
        {
            return JsonTypeBuilder.customArray(JsonType.VERSION_TYPE, getKey(), customField.getIdAsLong());
        }
        else
        {
            return JsonTypeBuilder.custom(JsonType.VERSION_TYPE, getKey(), customField.getIdAsLong());
        }
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(CustomField field, Issue issue, boolean renderedVersionRequested, @Nullable FieldLayoutItem fieldLayoutItem)
    {
        Collection<Version> versions = getValueFromIssue(field, issue);
        if (versions == null)
        {
            return new FieldJsonRepresentation(new JsonData(null));
        }
        if (isMultiple())
        {
            return new FieldJsonRepresentation(new JsonData(VersionJsonBean.shortBeans(versions, jiraBaseUrls)));
        }
        else
        {
            if (versions.isEmpty())
            {
                return new FieldJsonRepresentation(new JsonData(null));
            }
            else
            {
                return new FieldJsonRepresentation(new JsonData(VersionJsonBean.shortBean(versions.iterator().next(), jiraBaseUrls)));
            }
        }
    }
}
