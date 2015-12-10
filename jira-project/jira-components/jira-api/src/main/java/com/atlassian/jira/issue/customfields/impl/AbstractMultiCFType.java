package com.atlassian.jira.issue.customfields.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;

/**
 * Abstract class for Multi-select Custom field types.
 * Note that there is also another multi-select Custom Field for arbitrary {@link com.atlassian.jira.issue.customfields.option.Option}s - (MultiSelectCFType which lives in jira-core).
 * <p>
 * The <em>Transport Object</em> for this Custom Field type is a Collection of S, where S is defined by the subclass
 * Data is stored in the database a representations of a single S. E.g. A Custom Field that stores multiple Users,
 * will have each user saved seperately as an Object defined by {@link #convertTypeToDbValue}
 *
 * See the javadoc of {@link #updateValue(com.atlassian.jira.issue.fields.CustomField,com.atlassian.jira.issue.Issue,Object)},
 * {@link #createValue(com.atlassian.jira.issue.fields.CustomField,com.atlassian.jira.issue.Issue,Object)}, and
 * {@link #setDefaultValue(com.atlassian.jira.issue.fields.config.FieldConfig,Object)} for instance.
 * </p>
 *
 * @param <S> Single object contained within the Collection Transport Object
 * @see com.atlassian.jira.issue.customfields.CustomFieldType
 */
@PublicSpi
public abstract class AbstractMultiCFType<S> extends AbstractCustomFieldType<Collection<S>, S>
{
    protected final CustomFieldValuePersister customFieldValuePersister;
    protected final GenericConfigManager genericConfigManager;

    protected AbstractMultiCFType(CustomFieldValuePersister customFieldValuePersister, GenericConfigManager genericConfigManager)
    {
        this.genericConfigManager = genericConfigManager;
        this.customFieldValuePersister = customFieldValuePersister;
    }

    /**
     * Returns a comparator for underlying type of this custom field.  Used e.g. for sorting values retrieved from the Database
     *
     * @return a comparator, null if can't be compared without extra context
     */
    @Nullable
    abstract protected Comparator<S> getTypeComparator();

    /**
     * Converts a given underlying type to its db storage value.  Must be compatable with {@link PersistenceFieldType} returned by {@link #getDatabaseType()}
     *
     * @param value Single form of Transport Object
     * @return database representation of given Transport Object.
     */
    @Nullable
    abstract protected Object convertTypeToDbValue(@Nullable S value);

    /**
     * Converts a given db value to Single form of Transport Object
     *
     * @param dbValue db representation as returned by {@link #convertTypeToDbValue(Object)}
     * @return Single form of Transport Object
     */
    @Nullable
    abstract protected S convertDbValueToType(@Nullable Object dbValue);

    /**
     * Type of database field needed to store this field.
     * @return One of the predefined {@link PersistenceFieldType} types.
     */
    @Nonnull
    abstract protected PersistenceFieldType getDatabaseType();

    /** @see CustomFieldType#getDefaultValue(com.atlassian.jira.issue.fields.config.FieldConfig) */
    @Override
    public Collection<S> getDefaultValue(final FieldConfig fieldConfig)
    {
        final Object o = genericConfigManager.retrieve(CustomFieldType.DEFAULT_VALUE_TYPE, fieldConfig.getId().toString());

        if (o == null)
        {
            return null;
        }
        else if (o instanceof Collection<?>)
        {
            //noinspection unchecked
            return convertDbObjectToTypes((Collection<Object>) o);
        }
        else
        {
            throw new IllegalArgumentException("Value: " + o + " must be a collection. Type not allowed: " + o.getClass());
        }
    }

    /**
     * Sets the default value for a Custom Field.
     *
     * @param fieldConfig CustomField for which the default is being stored
     * @param value       <em>Transport Object</em> representing the value instance of the CustomField.
     * @see CustomFieldType#setDefaultValue(com.atlassian.jira.issue.fields.config.FieldConfig,Object)
     */
    @Override
    public void setDefaultValue(final FieldConfig fieldConfig, final Collection<S> value)
    {
        final Collection names = convertTypesToDbObjects(value);
        if ((names == null) || names.isEmpty())
        {
            genericConfigManager.update(CustomFieldType.DEFAULT_VALUE_TYPE, fieldConfig.getId().toString(), null);
        }
        else
        {
            genericConfigManager.update(CustomFieldType.DEFAULT_VALUE_TYPE, fieldConfig.getId().toString(), names);
        }
    }

    /**
     * Create a multi-select value for an issue.
     *
     * @param customField {@link com.atlassian.jira.issue.fields.CustomField} for which the value is being stored
     * @param issue       The {@link com.atlassian.jira.issue.Issue}.
     * @param value       <em>Transport Object</em> representing the value instance of the CustomField.
     * @see CustomFieldType#createValue(com.atlassian.jira.issue.fields.CustomField,com.atlassian.jira.issue.Issue,Object)
     */
    @Override
    public void createValue(final CustomField customField, final Issue issue, @Nonnull final Collection<S> value)
    {
        customFieldValuePersister.createValues(customField, issue.getId(), getDatabaseType(),
            convertTypesToDbObjects(value));
    }

    /**
     * Update a multi-select value for an issue.
     *
     * @param customField {@link com.atlassian.jira.issue.fields.CustomField} for which the value is being stored
     * @param issue       The {@link com.atlassian.jira.issue.Issue}.
     * @param value       <em>Transport Object</em> representing the value instance of the CustomField.
     * @see CustomFieldType#updateValue(com.atlassian.jira.issue.fields.CustomField,com.atlassian.jira.issue.Issue,Object)
     */
    @Override
    public void updateValue(final CustomField customField, final Issue issue, final Collection<S> value)
    {
        customFieldValuePersister.updateValues(customField, issue.getId(), getDatabaseType(),
            convertTypesToDbObjects(value));
    }

    /** @see CustomFieldType#getValueFromIssue(com.atlassian.jira.issue.fields.CustomField,com.atlassian.jira.issue.Issue) */
    @Override
    public Collection<S> getValueFromIssue(final CustomField field, final Issue issue)
    {
        final List<Object> textValues = customFieldValuePersister.getValues(field, issue.getId(), getDatabaseType());
        return ((textValues == null) || textValues.isEmpty()) ? null : convertDbObjectToTypes(textValues);
    }

    /**
     * Returns a string representation of the value if not null.
     *
     * @param field not used
     * @param values value to create a change log for
     * @return string representaion of value if not null, empty string otherwise
     */
    @Override
    public String getChangelogValue(final CustomField field, final Collection<S> values)
    {
        if (values == null || values.isEmpty())
        {
            return "";
        }
        // The old OSUser implementation simply returned value.toString() and relied on the toString of User/Group.
        // We replicate the same outcome here
        ArrayList<String> changeLogValues = new ArrayList<String>(values.size());
        for (S singleValue : values)
        {
            changeLogValues.add(getStringFromSingularObject(singleValue));
        }
        return changeLogValues.toString();
    }

    /**
     * Converts a collection of underlying types to a collection of db representations of underlying type.
     * <p>
     * If a Collection of String is passed, then a new Collection is still created, containing the original String values.
     * </p>
     *
     * @param typedList a collection of underlying types
     * @return a collection of string representations of underlying type
     */
    final protected Collection<Object> convertTypesToDbObjects(final Collection<S> typedList)
    {
        if (typedList == null)
        {
            return Collections.emptyList();
        }
        List<Object> dbObjects = new ArrayList<Object>();
        for (S item : typedList)
        {
            Object dbObject = convertTypeToDbValue(item);
            if (dbObject != null)
            {
                dbObjects.add(dbObject);
            }
        }
        return dbObjects;
    }

    /**
     * Converts a collection of objects representing the underlying type to a collection of underlying types. Returns
     * empty list when given strings collection is null.
     *
     * @param dbObjects collection of db representations of types.
     * @return a collection of underlying types
     */
    final protected Collection<S> convertDbObjectToTypes(final Collection<Object> dbObjects)
    {
        if (dbObjects == null)
        {
            return null;
        }
        final Set<S> retSet = new HashSet<S>();
        for (final Object element : dbObjects)
        {
            try
            {
                final S value = convertDbValueToType(element);
                if (value != null)
                {
                    retSet.add(value);
                }
            }
            catch (final FieldValidationException ignore)
            {}
        }
        final List<S> list = new ArrayList<S>(retSet);

        Comparator<S> typeComparator = getTypeComparator();
        if (typeComparator != null)
        {
            Collections.sort(list, typeComparator);
        }
        return list;
    }

    /**
     * called when removing a field. return issue IDs affected.
     * <p/>
     * Subclasses should override this if they have specific cleanup that they need to do (such as removing select list
     * values)
     */
    @Override
    public Set<Long> remove(CustomField field)
    {
        return customFieldValuePersister.removeAllValues(field.getId());
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitMultiField(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<X> extends VisitorBase<X>
    {
        X visitMultiField(AbstractMultiCFType multiCustomFieldType);
    }
}
