package com.atlassian.jira.issue.customfields.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;

import com.google.common.collect.Lists;
import com.opensymphony.util.TextUtils;

import org.apache.log4j.Logger;

/**
 * A abstract class that simplifies creating a CustomField that stores
 * a single values via ofbiz.  Possible values to store are:
 * <ul>
 * <li>Short text (< 255 chars) {@link #FIELD_TYPE_STRING}
 * <li>Long text (unlimited) {@link #FIELD_TYPE_TEXT}
 * <li>Floating point values {@link #FIELD_TYPE_NUMBER}
 * <li>Date values {@link #FIELD_TYPE_DATE}
 * </ul>
 * A custom field value has 3 states that it can be represented as.
 * <dl>
 * <dt>In Memory Object (aka Transport Object)</dt>
 * <dd>This is used as the primary version of the custom field value.  This
 * is what is passed to the view layer.  An example would be a <code>User</code>
 * or a <code>GenericValue</code> representing a project.</dd>
 * <dt>String representation</dt>
 * <dd>When editing a custom field value, it needs to be send to the web browser
 * and received in text format.  The in Memory Object mentioned above must have
 * a text representation of itself, and be able to parse it.  This is the value
 * passed to the edit template.  An example would be a username, or project id.</dd>
 * <dt>Database representation (required if storing in a database)</dt>
 * <dd>A custom field can be stored in a database, and there needs to be a way of getting
 * from the object value to the database value. An example of this would be the project
 * id.
 * </dl>
 * <p/>
 * <p/>
 */
@PublicSpi
public abstract class AbstractSingleFieldType<T> extends AbstractCustomFieldType<T, T>
{
    protected static final Logger log = Logger.getLogger(AbstractSingleFieldType.class);

    protected static final String FIELD_TYPE_STRING = "stringvalue"; //typically 255 characters
    protected static final String FIELD_TYPE_TEXT = "textvalue";     //4k or unlimited characters
    protected static final String FIELD_TYPE_DATE = "datevalue";     //a timestamp
    protected static final String FIELD_TYPE_NUMBER = "numbervalue"; //a double

    protected final CustomFieldValuePersister customFieldValuePersister;
    protected final GenericConfigManager genericConfigManager;

    protected AbstractSingleFieldType(CustomFieldValuePersister customFieldValuePersister, GenericConfigManager genericConfigManager)
    {
        this.customFieldValuePersister = customFieldValuePersister;
        this.genericConfigManager = genericConfigManager;
    }

    @Nullable
    public T getValueFromIssue(CustomField field, Issue issue)
    {
        return getValueFromIssue(field, issue.getId(), issue.getKey());
    }

    /**
     * Retrieve the current value of the customfield in the DB based on issue id.
     * <p/>
     * Could be used directly in sub-classes when we don't have the full issue object.
     *
     * @param field the customfield
     * @param issueId the id of the issue
     * @param issueKey the issue key. optional, only used for logging purpose
     * @return the value object
     */
    protected T getValueFromIssue(@Nonnull CustomField field, @Nullable Long issueId, @Nullable String issueKey)
    {
        final List<Object> values = customFieldValuePersister.getValues(field, issueId, getDatabaseType());

        final Object databaseValue;
        if (values.isEmpty())
        {
            return null;
        }
        else if (values.size() > 1)
        {
            // The data is corrupt - presumably because of concurrent update bug in customFieldValuePersister
            // Best we can do is pick one value as the winner
            databaseValue = values.get(0);
            log.warn("More than one value stored for custom field id '" + field.getId() + "' for issue '" + issueKey +
                    "'. Keeping '" + databaseValue +  "' and deleting other values. Original values:" + values);
            customFieldValuePersister.updateValues(field, issueId, getDatabaseType(), Arrays.asList(databaseValue));
        }
        else
        {
            databaseValue = values.get(0);
        }

        if (databaseValue == null)
        {
            return null;
        }
        try
        {
            return getObjectFromDbValue(databaseValue);
        }
        catch (FieldValidationException e)
        {
            // Create the log message
            String message = "Issue " + issueKey + " has an invalid value '" + databaseValue + "' stored in the field '" + field.getName() + "'.";
            if (log.isDebugEnabled())
            {
                // Still log a stacktrace at the debug level just in case we REALLY want it.
                log.debug(message, e);
            }
            else
            {
                // Simple warning with no stack trace, because this can happen easily in JIRA. eg see JRA-15424.
                log.warn(message + ' ' + e.getMessage());
            }
            return null;
        }

    }

    /**
     * called when removing a field.
     * return issue IDs affected.
     * <p>
     * Subclasses should override this if they have specific cleanup that they need to do
     * (such as removing select list values)
     */
    public Set<Long> remove(CustomField field)
    {
        return customFieldValuePersister.removeAllValues(field.getId());
    }


    /**
     * At this point we know that the value doesn't exist, and that <value>
     * is not null.
     */
    public void createValue(CustomField field, Issue issue, @Nonnull T value)
    {
        customFieldValuePersister.createValues(field, issue.getId(), getDatabaseType(), Lists.newArrayList(getDbValueFromObject(value)));
    }

    /**
     * the value does exist, and the new value is different than the
     * existing one.
     */
    public void updateValue(CustomField customField, Issue issue, T value)
    {
        customFieldValuePersister.updateValues(customField, issue.getId(), getDatabaseType(), Lists.newArrayList(getDbValueFromObject(value)));
    }

    public void setDefaultValue(FieldConfig fieldConfig, T value)
    {
        genericConfigManager.update(DEFAULT_VALUE_TYPE, fieldConfig.getId().toString(), getDbValueFromObject(value));
    }

    public T getDefaultValue(FieldConfig fieldConfig)
    {
        try
        {
            //JRA-30590: NPEs thrown for fields that don't have configs in the target project
            if (fieldConfig == null)
            {
                return null;
            }
            Object databaseValue = genericConfigManager.retrieve(DEFAULT_VALUE_TYPE, fieldConfig.getId().toString());
            if (databaseValue != null)
            {
                return getObjectFromDbValue(databaseValue);
            }
            else
            {
                return null;
            }
        }
        catch (FieldValidationException e)
        {
            log.error("Incorrect formatted custom field stored as default", e);
            return null;
        }
    }

    public void validateFromParams(CustomFieldParams relevantParams, ErrorCollection errorCollectionToAddTo, FieldConfig config)
    {
        try
        {
            getValueFromCustomFieldParams(relevantParams);
        }
        catch (FieldValidationException e)
        {
            errorCollectionToAddTo.addError(config.getCustomField().getId(), e.getMessage(), Reason.VALIDATION_FAILED);
        }
    }

    public T getValueFromCustomFieldParams(CustomFieldParams relevantParams) throws FieldValidationException
    {
        if (relevantParams == null)
        {
            return null;
        }

        Collection normalParams = relevantParams.getValuesForKey(null); //single field types should not scope their parameters
        if (normalParams == null || normalParams.isEmpty())
            return null;

        String singleParam = (String) normalParams.iterator().next();
        if (TextUtils.stringSet(singleParam)) //only validate if the value is set
        {
            return getSingularObjectFromString(singleParam);
        }
        else
        {
            return null;
        }
    }

    public Object getStringValueFromCustomFieldParams(CustomFieldParams parameters)
    {
        return parameters.getFirstValueForNullKey();
    }


    public String getChangelogValue(CustomField field, T value)
    {
        if (value == null)
            return "";
        else
            return getStringFromSingularObject(value);
    }


    /** Type of database field needed to store this field.
     * @return One of the predefined {@link PersistenceFieldType} types.
     */
    @Nonnull
    protected abstract PersistenceFieldType getDatabaseType();

    /**
     * Returns the database representation of the Java object as stored for that CustomField.
     * (eg. ProjectId if the Value represents a project). Must be compatable with type returned by {@link #getDatabaseType()}
     *
     * @param customFieldObject the Transport Object
     * @return String, Double or Date
     */
    @Nullable
    protected abstract Object getDbValueFromObject(T customFieldObject);

    /**
     * Returns the Transport Object for the given Custom Field value as represented by the value
     * stored in the database
     *
     * @param databaseValue - String, Double or Date objects as returned from {@link #getDbValueFromObject(Object)}
     * @return Domain object or GenericValue
     * @throws FieldValidationException if field validation fails.
     */
    @Nullable
    protected abstract T getObjectFromDbValue(@Nonnull Object databaseValue) throws FieldValidationException;

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitSingleField(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<X> extends VisitorBase<X>
    {
        X visitSingleField(AbstractSingleFieldType singleCustomFieldType);
    }

    public JsonData getJsonDefaultValue(IssueContext issueCtx, CustomField field)
    {
        FieldConfig config = field.getRelevantConfig(issueCtx);
        Object defaultValue = field.getCustomFieldType().getDefaultValue(config);
        return defaultValue == null ? null : new JsonData(defaultValue);
    }

}
