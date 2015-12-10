package com.atlassian.configurable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.ListOrderedMap;

/**
 * This interface is a property of an Object Configuration and contains a name, description, default value and the type
 * (ObjectConfigurationTypes) of this property. It can also contain a list of values if the type has values specified
 * for it e.g. select list.
 */
public class ObjectConfigurationPropertyImpl implements ObjectConfigurationProperty
{
    private final String name;
    private final String description;
    private final String defaultValue;
    private final int type;
    private final EnabledCondition enabled;

    private final Map internalValues = Collections.synchronizedMap(new ListOrderedMap());
    private final Map userParams = Collections.synchronizedMap(new HashMap());

    private String cascadeFrom;
    private boolean i18nValues = true;

    /**
     * Creates a new ObjectConfigurationPropertyImpl object.
     *
     * @param name         Name of this property
     * @param description  Description of this property
     * @param defaultValue Default value for this property
     * @param type         This property type as specified in {@link ObjectConfigurationTypes}
     */
    ObjectConfigurationPropertyImpl(String name, String description, String defaultValue, int type)
    {
        this(name, description, defaultValue, type, EnabledCondition.TRUE);
    }

    /**
     * Constructor with enabledCondition class name.
     *
     * @param name                  Name of this property
     * @param description           Description of this property
     * @param defaultValue          Default value for this property
     * @param type                  This property type as specified in {@link ObjectConfigurationTypes}
     * @param enabledConditionClass the name of a class that implements {@link EnabledCondition}
     * @since 29 August 2007 for JIRA 3.11
     */
    ObjectConfigurationPropertyImpl(String name, String description, String defaultValue, int type, String enabledConditionClass)
    {
        this(name, description, defaultValue, type, EnabledConditionFactory.create(enabledConditionClass));
    }
    
    /**
     * Constructor with enabledCondition class name.
     *
     * @param name                  Name of this property
     * @param description           Description of this property
     * @param defaultValue          Default value for this property
     * @param type                  This property type as specifiet in {@link ObjectConfigurationTypes}
     * @param enabledConditionClass the name of a class that implements {@link EnabledCondition}
     * @since 29 August 2007 for JIRA 3.11
     */
    ObjectConfigurationPropertyImpl(String name, String description, String defaultValue, int type, String enabledConditionClass, ClassLoader classLoader)
    {
        this(name, description, defaultValue, type, EnabledConditionFactory.create(enabledConditionClass));
    }

    /**
     * Constructs a property with the given field values.
     *
     * @param name             the name of the property.
     * @param description      the description of the property.
     * @param defaultValue     the property's default value.
     * @param type             the property's type as specified in {@link com.atlassian.configurable.ObjectConfigurationTypes}
     * @param enabledCondition <code>null</code> indicating always enabled, or an implementation which determines when the property is enabled.
     * @since 29 August 2007 for JIRA 3.11
     */
    ObjectConfigurationPropertyImpl(String name, String description, String defaultValue, int type, EnabledCondition enabledCondition)
    {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.type = type;
        this.enabled = (enabledCondition == null) ? EnabledCondition.TRUE : enabledCondition;
    }

    /**
     * Creates a new ObjectConfigurationPropertyImpl object with a set of values.
     *
     * @param name           Name of this property
     * @param description    Description of this property
     * @param defaultValue   Default value for this property
     * @param type           This property type as specifiet in {@link ObjectConfigurationTypes}
     * @param propertyValues List of available key,values for this property
     */
    ObjectConfigurationPropertyImpl(String name, String description, String defaultValue, int type, Map propertyValues)
    {
        this(name, description, defaultValue, type);
        this.internalValues.putAll(propertyValues);
    }

    /**
     * This is a list of user parameters that may be required to generate a list of values and/or a description for this
     * Property. e.g. a User may be required to generate a list of values they can see but other users can not.
     *
     * @param userParams Parameters used to generate a list of values and/or a description
     */
    public void init(Map userParams)
    {
        if (userParams != null)
        {
            this.userParams.clear();
            this.userParams.putAll(userParams);
        }
    }

    /**
     * Retrieves the name of this Property e.g. City
     *
     * @return Name of this Property
     */
    public String getName()
    {
        return name;
    }

    /**
     * Retrieves the description of this Property e.g. Please enter you current location
     *
     * @return Description of the Property
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Retrieves the default value of this Property e.g. Sydney
     *
     * @return Default value of Property
     */
    public String getDefault()
    {
        return defaultValue;
    }

    /**
     * Retrieves the type of the Property e.g. Text
     *
     * @return Type of property from {@link ObjectConfigurationTypes}
     */
    public int getType()
    {
        return type;
    }


    public boolean isEnabled()
    {
        return enabled.isEnabled();
    }

    /**
     * If this Property has a list of choices e.g. a select list then this function returns how many there are. If there
     * are no choices it returns 0
     *
     * @return Number of choices for this properties value
     */
    public int size()
    {
        return getInternalValues(userParams).size();
    }

    /**
     * Does this property have a number of possible choices
     *
     * @return Does this property have a number of possible values
     */
    public boolean isEmpty()
    {
        return getInternalValues(userParams).isEmpty();
    }

    /**
     * Does this property have a choice with key of the specified value
     *
     * @param key Does this Property have a choice with key, key
     * @return Has Key
     */
    public boolean containsKey(Object key)
    {
        return getInternalValues(userParams).containsKey(key);
    }

    /**
     * Does this property have a choice with value of the specified value
     *
     * @param value Does this Property have a choice with value, value
     * @return Has Value
     */
    public boolean containsValue(Object value)
    {
        return getInternalValues(userParams).containsValue(value);
    }

    /**
     * Returns the value of the choice that matches key
     *
     * @param key Key look for
     * @return value for key, key
     */
    public Object get(Object key)
    {
        return getInternalValues(userParams).get(key);
    }

    /**
     * Object Configuration Properties are immutable so this function throws an exception
     */
    public Object put(Object key, Object value)
    {
        throw new UnsupportedOperationException("ObjectConfigurationProperty object is immutable");
    }

    /**
     * Object Configuration Properties are immutable so this function throws an exception
     */
    public Object remove(Object key)
    {
        throw new UnsupportedOperationException("ObjectConfigurationProperty object is immutable");
    }

    /**
     * Object Configuration Properties are immutable so this function throws an exception
     */
    public void putAll(Map t)
    {
        throw new UnsupportedOperationException("ObjectConfigurationProperty object is immutable");
    }

    /**
     * Object Configuration Properties are immutable so this function throws an exception
     */
    public void clear()
    {
        throw new UnsupportedOperationException("ObjectConfigurationProperty object is immutable");
    }

    /**
     * Returns a set of the key for the choices for this property
     *
     * @return Set of keys
     */
    public Set keySet()
    {
        return getInternalValues(userParams).keySet();
    }

    /**
     * Returns all the values for the choices of this property
     *
     * @return Collection of values
     */
    public Collection values()
    {
        return getInternalValues(userParams).values();
    }

    /**
     * Entry set of the choices for this property
     *
     * @return Entry Set
     */
    public Set entrySet()
    {
        return getInternalValues(userParams).entrySet();
    }

    public boolean isI18nValues()
    {
        return i18nValues;
    }

    public void setI18nValues(boolean i18nValues)
    {
        this.i18nValues = i18nValues;
    }

    public String getCascadeFrom()
    {
        return cascadeFrom;
    }

    public void setCascadeFrom(String cascadeFrom)
    {
        this.cascadeFrom = cascadeFrom;
    }

    /**
     * Returns the map of values stored internally for the choices of this property.
     *
     * @param userParams Not used in this implementation.
     * @return Map of choices
     */
    protected Map getInternalValues(Map userParams)
    {
        return Collections.unmodifiableMap(internalValues);
    }

    /**
     * @return the map of values stored internally for the choices of this property as a mutable Map.
     * @since 29 August 2007 for JIRA 3.11
     */
    protected Map getMutableInternalValues()
    {
        return internalValues;
    }
}
