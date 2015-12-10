package com.atlassian.jira.imports.project.customfield;

import com.atlassian.annotations.PublicApi;
import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;

/**
 * Does the custom field specific, "actual work", of importing custom field values during a project import.
 *
 * @since v3.13
 * @see ProjectImportableCustomField
 */
@PublicSpi
public interface ProjectCustomFieldImporter
{
    /**
     * The custom field needs to determine if the provided custom field value, in the context of the config and
     * project import mapper, is a valid value that can be imported.
     * <p>
     * The implementation has two ways to report dependencies:
     * <ul>
     * <li>It can use the flagAsRequired() method on the appropriate System object Mapper if it simply wants to report that it requires a particular object from the import file.<br>
     *   If this object cannot be mapped into the new System, then the Project Import will report an erro against that Object type (eg Version).
     * </li>
     * <li>It can create its own error messages and/or warnings and add them to the returned MessageSet.<br>
     *   These messages will be reported in the context of the particular Custom Field.</li>
     * </ul>
     * Because these two ways of creating messages will be shown in different contexts, it is valid for a Custom Field developer
     * to choose to report a single problem both ways.
     * <p>
     * If this method does not return any errors in the MessageSet, and any objects marked as required can be mapped in
     * the new system, then it is safe to do the import and call the {@link #getMappedImportValue} method.
     * </p>
     * <p>
     * It is valid for implementors of this method to return <code>null</code>, and this will be treated the same as an empty <code>MessageSet</code>.
     * </p>
     *
     * @param projectImportMapper contains a fully populated and validated ProjectImportMapper which contains
     *          system mappings that the custom fields can use to determine if the value is one that they can map.
     * @param customFieldValue the Object representation of the stored custom field value.
     * @param fieldConfig the relevant FieldConfig that the custom field value is in use with. This will allow the
     * custom field to lookup any relevant options that it may have configured.
     * @param i18n an i18nHelper setup from the custom fields configuration that can be used to i18n error and
     * warning messages. 
     * @return a MessageSet that will contain any errors or warnings that the custom field wants to report, in an
     * i18n'ed form. Errors returned in the MessageSet will cause the project import to stop.
     */
    MessageSet canMapImportValue(ProjectImportMapper projectImportMapper, ExternalCustomFieldValue customFieldValue, FieldConfig fieldConfig, final I18nHelper i18n);

    /**
     * The custom field needs to determine what the "mapped" value will be for the provided custom field value and return
     * this new string representation of the value. This value may come from the passed in project import mapper, it
     * may come from mapping custom field options, or may come from an external system.
     *
     * @param projectImportMapper contains a fully populated and validated ProjectImportMapper which contains
     * system mappings that the custom fields can use to determine if the value is one that they can map.
     * @param customFieldValue the Object representation of the stored custom field value.
     * @param fieldConfig the relevant FieldConfig that the custom field value is in use with. This will allow the
     * custom field to lookup any relevant options that it may have configured.
     * @return the Object representation of the valid mapped value (and possibly parentKey value) for the passed in custom field value.
     */
    MappedCustomFieldValue getMappedImportValue(ProjectImportMapper projectImportMapper, ExternalCustomFieldValue customFieldValue, FieldConfig fieldConfig);

    /**
     * A simple class that is used to return a value/parentKey pair of new "mapped" values for a custom field.
     */
    @PublicApi
    public class MappedCustomFieldValue
    {
        private final String parentKey;
        private final String value;

        public MappedCustomFieldValue(final String value)
        {
            this(value, null);
        }

        public MappedCustomFieldValue(final String value, final String parentKey)
        {
            this.value = value;
            this.parentKey = parentKey;
        }

        public String getParentKey()
        {
            return parentKey;
        }

        public String getValue()
        {
            return value;
        }
    }
}
