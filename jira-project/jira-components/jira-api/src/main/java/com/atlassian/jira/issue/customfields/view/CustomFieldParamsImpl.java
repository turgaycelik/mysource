package com.atlassian.jira.issue.customfields.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.CustomField;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntity;

public class CustomFieldParamsImpl implements CustomFieldParams
{
    private static final Logger log = Logger.getLogger(CustomFieldParamsImpl.class);
    private final Map<String, Collection<String>> parameterMap = new HashMap<String, Collection<String>>(); //this needs to be a hashmap, as we store null keys in it
    private CustomField customField;

    public CustomFieldParamsImpl()
    {}

    public CustomFieldParamsImpl(final CustomField customField)
    {
        this(customField, Collections.EMPTY_LIST);
    }

    public CustomFieldParamsImpl(final CustomField customField, final Object paramsObject)
    {
        if (paramsObject instanceof CustomFieldParams)
        {
            final CustomFieldParams params = (CustomFieldParams) paramsObject;
            deepCopy(params.getKeysAndValues());
        }
        else if ((paramsObject instanceof Map) && !(paramsObject instanceof GenericEntity))
        {
            final Map params = (Map) paramsObject;
            deepCopy(params);
        }
        else if (paramsObject instanceof Collection)
        {
            parameterMap.put(null, new ArrayList((Collection) paramsObject));
        }
        else if (paramsObject != null)
        {
            final ArrayList list = new ArrayList(); //This is not generic because we write to that map
                                                    //not only Strings...
            list.add(paramsObject);
            parameterMap.put(null, list);
        }
        else
        {
            log.debug("CustomFieldParamsImpl received a null object in constructor");
        }

        this.customField = customField;
    }

    private void deepCopy(final Map params)
    {
        final Set entries = params.entrySet();
        for (final Object entry1 : entries)
        {
            final Map.Entry entry = (Map.Entry) entry1;
            final String key = (String) entry.getKey();
            Object values = entry.getValue();
            if (values instanceof Collection)
            {
                parameterMap.put(key, new ArrayList((Collection) values));
            }
            else
            {
                final ArrayList list = new ArrayList(); //This is not generic because we write to that map
                                                        //not only Strings...
                list.add(values);
                parameterMap.put(key, list);
            }
        }
    }

    public Set<String> getAllKeys()
    {
        return parameterMap.keySet();
    }

    public CustomField getCustomField()
    {
        return customField;
    }

    public void setCustomField(final CustomField customField)
    {
        this.customField = customField;
    }

    public Collection<String> getValuesForKey(@Nullable final String key)
    {
        return parameterMap.get(key);
    }

    public Object getFirstValueForKey(@Nullable final String key)
    {
        final Collection c = getValuesForKey(key);

        if ((c != null) && !c.isEmpty())
        {
            return c.iterator().next();
        }
        else
        {
            return null;
        }
    }

    public Collection getValuesForNullKey()
    {
        return getValuesForKey(null);
    }

    public Collection getAllValues()
    {
        final Collection<String> allValues = new ArrayList<String>();
        for (final Collection<String> values : parameterMap.values())
        {
            allValues.addAll(values);
        }
        return allValues;
    }

    public String getQueryString()
    {
        final String customFieldId = getCustomField().getId();
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<String, Collection<String>> entry : parameterMap.entrySet())
        {
            final String key = entry.getKey();
            final Collection<String> values = entry.getValue();
            if (values != null)
            {
                for (final String value : values)
                {
                    if (sb.length() > 0)
                    {
                        sb.append("&");
                    }

                    if (key != null)
                    {
                        sb.append(customFieldId).append(":").append( key);
                    }
                    else
                    {
                        sb.append(customFieldId);
                    }

                    sb.append("=").append(value);
                }
            }

        }
        return sb.toString();
    }

    public void addValue(final String key, final Collection<String> values)
    {
        parameterMap.put(key, values);
    }

    public void addValue(final Collection<String> values)
    {
        parameterMap.put(null, values);
    }

    public void put(final String key, final Collection<String> value)
    {
        addValue(key, value);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof CustomFieldParams))
        {
            return false;
        }

        final CustomFieldParams customFieldParams = (CustomFieldParams) o;

        if (customField == null && customFieldParams.getCustomField() != null)
        {
            return false;
        }
        if (customField != null && !customField.equals(customFieldParams.getCustomField()))
        {
            return false;
        }
        if (!parameterMap.equals(customFieldParams.getKeysAndValues()))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result;
        result = parameterMap.hashCode();
        result = 29 * result + customField.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "CustomFieldParams: " + customField + ".  Params: " + parameterMap + ".";
    }

    public boolean isEmpty()
    {
        if (!parameterMap.isEmpty())
        {
            for (final Collection<String> element : parameterMap.values())
            {
                if (!element.isEmpty())
                {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean contains(final String key, final String value)
    {
        final Collection c = getValuesForKey(key);
        if ((c != null) && !c.isEmpty())
        {
            return c.contains(value);
        }
        else
        {
            return false;
        }
    }

    public void transformObjectsToStrings()
    {
        final Transformer toStringTransformer = new Transformer()
        {
            public Object transform(final Object input)
            {
                return getCustomField().getCustomFieldType().getStringFromSingularObject(input);
            }
        };

        transformMultiMap(toStringTransformer, parameterMap);
    }

    public void transformStringsToObjects()
    {
        final Transformer toObjectTransformer = new Transformer()
        {
            public Object transform(final Object input)
            {
                try
                {
                    return getCustomField().getCustomFieldType().getSingularObjectFromString((String) input);
                }
                catch (final FieldValidationException e)
                {
                    log.info("Failed to convert from string to singular object", e);
                    return null;
                }
            }
        };

        transformMultiMap(toObjectTransformer, parameterMap);
    }

    public static void transformMultiMap(final Transformer transformer, final Map parameterMap)
    {
        final Collection values = parameterMap.values();
        for (final Object value : values)
        {
            try
            {
                final Collection collection = (Collection) value;
                CollectionUtils.transform(collection, transformer);
            }
            catch (final RuntimeException e)
            {
                log.error("Exception occurred during transformation of Params. State may be inconsistent", e);
            }
        }
    }

    public void transform(final Transformer transformer)
    {
        transformMultiMap(transformer, parameterMap);
    }

    public Object getFirstValueForNullKey()
    {
        return getFirstValueForKey(null);
    }

    public Map getKeysAndValues()
    {
        return new HashMap<String, Collection<String>>(parameterMap);
    }

    public void remove(final String key)
    {
        parameterMap.remove(key);
    }

    public boolean containsKey(final String key)
    {
        return parameterMap.containsKey(key);
    }

}
