package com.atlassian.jira.issue.customfields;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.annotations.PublicApi;
import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.bean.BulkEditBean;

/**
 * <p>This interface represents a particular type of {@link CustomField}. It encapsulates all logic dealing with values
 * of a Custom Field, such as creation, update and removal of values, storage & retrieval of defaults and validation of
 * values. </p>
 *
 * <p>A value instance of a custom field is represented as an {@link Object}, from hereon in referred to as a
 * <em><strong>Transport Object</strong></em>. These may be of singular types (eg. {@link Number}, {@link String}) or
 * Multi-Dimensional (eg. {@link Collection} of Strings, {@link Map} of Date Objects, {@link
 * CustomFieldParams} of {@link Option}). Most methods in the interface expect/returns Transfer Objects (e.g.
 * Persistence Methods ({@link #createValue}, {@link #updateValue}) and Transfer Object Getters
 * {@link #getValueFromIssue} and {@link #getValueFromCustomFieldParams}.</p>
 *
 * <p> However, two special conversion methods ({@link #getSingularObjectFromString} & {@link
 * #getStringFromSingularObject}) act on the <strong>Singular Object</strong> level. Thus, even when the
 * Transfer Object type is a Collection of Number, getSingularObjectFromString would still return a single Number
 * object. </p>
 *
 * <p>Implementing classes should <strong>clearly document</strong> the exact structure of the Transport Object in the
 * Class javadoc header. If the Transport Object is Multi-Dimensional, the type of the Singular Object should also be
 * specified. This is especially relevant for complex custom field types (CascadingSelectCFType for
 * example)</p>
 *
 * @see CustomField
 * @see CustomFieldParams
 * @since JIRA 3.0
 * @param <T> Transport Object  For a single Transport Objects this will be the same as <Strong>S</Strong>.
 * Otherwise, the transport Object currently supports {@code Collection&lt;S>}, {@code Map&lt;String,S>} or {@code Map&lt;String, Collection&lt;S>>}.
 * N.B. Support for {@link CustomFieldParams} as the Transport Object has been deprecated since 5.0.
 * @param <S> Singular Form of the Transport object <Strong>T</Strong>.
 *
 *
 */
@PublicApi
@PublicSpi
public interface CustomFieldType <T, S>
{
    String DEFAULT_VALUE_TYPE = "DefaultValue";

    /**
     * Name of the resource that can be used as a preview for the CustomField when rendering in the UI.
     */
    String RESOURCE_PREVIEW = "customfieldpreview.png";
    // ---------------------------------------------------------------------------------------------  Descriptor Methods

    /**
     * Initialises the CustomFieldType with the given descriptor.
     *
     * @param customFieldTypeModuleDescriptor CustomFieldTypeModuleDescriptor
     */
    void init(CustomFieldTypeModuleDescriptor customFieldTypeModuleDescriptor);

    /**
     * Returns the full key of the CustomFieldType. Typically, this will be prefixed with
     * "com.atlassian.jira.plugin.system.customfieldtypes:"
     *
     * @return CustomFieldType Key prefixed with the Package
     */
    public String getKey();

    public String getName();
    public String getDescription();
    public CustomFieldTypeModuleDescriptor getDescriptor();



    // ---------------------------------------------------------------------------------- Single Value Object Converters

    /**
     * Returns the {@link String} representation of a single value within the CustomFieldType. This is the value that
     * is passed to the presentation tier for editing. For single CustomFieldTypes the <em>Singular Object</em> is
     * the same as a <em>Transport Object</em>. However, for multi-dimensional CustomFieldTypes, the Singular Object is
     * the Object contained within the {@link Collection} or {@link Map}
     *
     * @param singularObject the object
     * @return String representation of the Object
     */
    public String getStringFromSingularObject(S singularObject);

    /**
     * Returns a Singular Object, given the string value as passed by the presentation tier.
     * Throws FieldValidationException if the string is an invalid representation of the Object.
     *
     * @param string the String
     * @return singularObject instance
     * @throws FieldValidationException if the string is an invalid representation of the Object.
     */
    public S getSingularObjectFromString(String string) throws FieldValidationException;



    // -------------------------------------------------------------------------------- Custom Field Persistence Methods

    /**
     * Performs additional tasks when an entire CustomField of this type is being removed {@link CustomField#remove}.
     * This includes removal of values & options.
     *
     * @param field The custom field that is being removed, so any data stored for
     * any issues for that field can be deleted.
     * @return {@link Set<Long>} of issue ids that has been affected
     */
    public Set<Long> remove(CustomField field);



    // -----------------------------------------------------------------------------------------------------  Validation

    /**
     * Ensures that the {@link CustomFieldParams} of Strings is a valid representation of the Custom Field values.
     * Any errors should be added to the {@link ErrorCollection} under the appropriate key as required.
     *
     * @param relevantParams parameter object of Strings
     * @param errorCollectionToAddTo errorCollection to which any erros should be added (never null)
     * @param config FieldConfig
     */
    public void validateFromParams(CustomFieldParams relevantParams, ErrorCollection errorCollectionToAddTo, FieldConfig config);



    // --------------------------------------------------------------------------------------------- Persistance Methods


    /**
     * Save the value for this Custom Field in a particular issue to the database.
     * @param field {@link com.atlassian.jira.issue.fields.CustomField} for which the value is being stored
     * @param issue The {@link com.atlassian.jira.issue.Issue} to be stored against.
     * @param value <em>Transport Object</em> representing the value instance of the CustomField.
     * Can not be {@code null}.
     */
    public void createValue(CustomField field, Issue issue, @Nonnull T value);

    /**
     * Update the value for this Custom Field in a particular issue currently stored in the database.
     *
     * @param field {@link com.atlassian.jira.issue.fields.CustomField} for which the value is being stored
     * @param issue The {@link com.atlassian.jira.issue.Issue} to be stored against.
     * @param value <em>Transport Object</em> representing the value instance of the CustomField.
     */
    public void updateValue(CustomField field, Issue issue, T value);



    // ---------------------------------------------------------------------------------- Transfer Object Getter methods

    /**
     * Retrieves the Transport Object representing the CustomField value instance from the CustomFieldParams of Strings.
     *
     * @param parameters CustomFieldParams of <b>String</b> objects. Will contain one value for Singular field types.
     * @return <i>Transport Object</i> matching the Object parameter of {@link #createValue}, {@link #updateValue}
     * @throws FieldValidationException if the String value fails to convert into Objects
     * @see #createValue(com.atlassian.jira.issue.fields.CustomField, com.atlassian.jira.issue.Issue, Object)
     * @see #updateValue(com.atlassian.jira.issue.fields.CustomField, com.atlassian.jira.issue.Issue, Object)
     * @see #getValueFromIssue(com.atlassian.jira.issue.fields.CustomField, com.atlassian.jira.issue.Issue)
     */
    public T getValueFromCustomFieldParams(CustomFieldParams parameters) throws FieldValidationException;

    /**
     * Return the String value object from the CustomFieldParams. The object may be a single String (e.g. TextCFType,
     * List of Strings (e.g. MultiSelectCFType) or CustomFieldParams of Strings (e.g. CascadingSelectCFType).  Among other things
     * these values are passed to Velocity for rendering edit screens.
     *
     * @param parameters - CustomFieldParams containing String values
     * @return String value object from the CustomFieldParams
     */
    public Object getStringValueFromCustomFieldParams(CustomFieldParams parameters);

    /**
     * Retrieves the Transport Object representing the <strong>current</strong> CustomField value for the given issue.
     *
     * @param field Custom field for which to retrieve the value
     * @param issue Issue from which to retrieve the value
     * @return <i>Transport Object</i> matching the Object parameter of {@link #createValue}, {@link #updateValue}
     */
    @Nullable
    public T getValueFromIssue(CustomField field, Issue issue);

    // -------------------------------------------------------------------------------------------------- Default Values

    /**
     * Retrieves the Object representing the <strong>default</strong> CustomField value for the Custom Field.
     *
     * @param fieldConfig CustomField for default value
     * @return <i>Transport Object</i> of the Default Value
     */
    public T getDefaultValue(FieldConfig fieldConfig);

    /**
     * Sets the default value for a Custom Field
     *
     * @param fieldConfig CustomField for which the default is being stored
     * @param value <i>Transport Object</i> representing the value instance of the CustomField
     */
    public void setDefaultValue(FieldConfig fieldConfig, T value);

    // --------------------------------------------------------------------------------------------------  Miscellaneous

    /**
     * Returns a values to be stored in the change log, example is the id of the changed item.
     *
     * @since 3.1 Implementations can return {@code null}. This should only occur when no change log is desired.
     *
     * @param field CustomField that the value belongs to
     * @param value <i>Transport Object</i> representing the value instance of the CustomField
     * @return Change log value.
     */
    @Nullable
    public String getChangelogValue(CustomField field, T value);

    /**
     * Returns a String of representing values to be stored in the change log, an example is the name of a version
     * field that a version id will resolve to within JIRA.
     *
     * @since 3.4 Implementations can return {@code null}. This should only occur when no change log is desired or when the
     * value returned from the getChangelogValue method is an accurate representation of the data's value.
     *
     * @param field CustomField that the value belongs to
     * @param value <i>Transport Object</i> representing the value instance of the CustomField
     * @return Change log string.
     */
    @Nullable
    public String getChangelogString(CustomField field, T value);

    /**
     * The custom field may wish to pass parameters to the velocity context beyond the getValueFromIssue methods
     * (eg managers).
     * <p />
     * The values are added to the context for all velocity views (edit, search, view, xml)
     * <p />
     *
     * @param issue The issue currently in context (Note: this will be null in cases like 'default value')
     * @param field CustomField
     * @param fieldLayoutItem FieldLayoutItem
     * @return  A {@link Map} of parameters to add to the velocity context, or an empty Map otherwise (never null)
     */
    @Nonnull
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem);

    /**
     * Returns a List of {@link FieldConfigItemType} objects. Can not be immutable.
     * This opens up possibilities for configurable custom fields.
     *
     * @return List of {@link FieldConfigItemType}
     */
    @Nonnull
    public List<FieldConfigItemType> getConfigurationItemTypes();

    /**
     * Returns a list of indexers that will be used for the field.
     *
     * @param customField the custom field to get the related indexers of.
     * @return List of instantiated and initialised {@link FieldIndexer} objects. Null if no related indexers.
     */
    @Nullable
    List<FieldIndexer> getRelatedIndexers(CustomField customField);

    /**
     * This is a mirror of the method from the RenderableField interface and is needed to bridge the gap between
     * CustomFields and CustomFieldTypes.
     * @return true if the field is configurable for use with the renderers, a text based field, false otherwise.
     */
    public boolean isRenderable();

    /**
     * Used to compare 2 field values and work out whether a change item should be generated
     * @param v1 current value
     * @param v2 new value
     * @return true if the change item should be generated, false otherwise
     */
    boolean valuesEqual(T v1, T v2);

    /**
     * Allow the custom field type perform a specific check as to its availability for bulk editing.
     * @param bulkEditBean BulkEditBean
     * @return null if available for bulk edit or appropriate unavailable message
     */
    @Nullable
    String availableForBulkEdit(BulkEditBean bulkEditBean);
}
