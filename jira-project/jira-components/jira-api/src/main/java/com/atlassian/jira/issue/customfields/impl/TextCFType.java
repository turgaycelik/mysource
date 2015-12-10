package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.annotations.PublicApi;
import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.imports.project.customfield.NoTransformationCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.converters.StringConverter;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import javax.annotation.Nonnull;
import org.apache.commons.lang.StringUtils;

/**
 * @deprecated Use {@link GenericTextCFType} instead. Since v5.0.
 */
@Deprecated
@PublicApi
@PublicSpi
public class TextCFType extends StringCFType implements SortableCustomField<String>, ProjectImportableCustomField
{
    private final ProjectCustomFieldImporter projectCustomFieldImporter;

    public TextCFType(final CustomFieldValuePersister customFieldValuePersister, final GenericConfigManager genericConfigManager)
    {
        super(customFieldValuePersister, genericConfigManager);
        projectCustomFieldImporter = new NoTransformationCustomFieldImporter();
    }

    /**
     * This constructor is deprecated and is left only for backward compatibility in 3rd party plugins.
     * It will be removed from a future version of JIRA.
     *
     * @param customFieldValuePersister CustomFieldValuePersister
     * @param stringConverter StringConverter
     * @param genericConfigManager GenericConfigManager
     *
     * @deprecated - We no longer require an instance of StringConverter. Use TextCFType(CustomFieldValuePersister, GenericConfigManager). Since v4.0.
     * @see #TextCFType(CustomFieldValuePersister, GenericConfigManager)
     */
    @SuppressWarnings ({ "UnusedDeclaration" })
    public TextCFType(final CustomFieldValuePersister customFieldValuePersister, final StringConverter stringConverter, final GenericConfigManager genericConfigManager)
    {
        super(customFieldValuePersister, genericConfigManager);
        projectCustomFieldImporter = new NoTransformationCustomFieldImporter();
    }

    public String getStringFromSingularObject(final Object value)
    {
        assertObjectImplementsType(String.class, value);
        // convert null to empty string
        return StringUtils.defaultString((String) value);
    }

    public Object getSingularObjectFromString(final String string) throws FieldValidationException
    {
        return string;
    }

    public int compare(@Nonnull final String customFieldObjectValue1, @Nonnull final String customFieldObjectValue2, final FieldConfig fieldConfig)
    {
        return customFieldObjectValue1.compareTo(customFieldObjectValue2);
    }

    @Nonnull
    @Override
    protected PersistenceFieldType getDatabaseType()
    {
        return PersistenceFieldType.TYPE_LIMITED_TEXT;
    }

    /**
     * This method will return a {@link NoTransformationCustomFieldImporter}, be mindful that if you are extending
     * this class you need to have a good hard think about whether this is the right field importer for your custom
     * field values.
     *
     * @return a {@link NoTransformationCustomFieldImporter}
     * @see com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField#getProjectImporter()
     */
    public ProjectCustomFieldImporter getProjectImporter()
    {
        return projectCustomFieldImporter;
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitText(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitText(TextCFType textCustomFieldType);
    }
}
