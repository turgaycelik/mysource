package com.atlassian.jira.mock.ofbiz;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericPK;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelField;
import org.ofbiz.core.util.UtilMisc;
import org.ofbiz.core.util.UtilValidate;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.google.common.collect.ImmutableMap;

/**
 * This was taken from atlassian-ofbiz and placed into its now rightful home of JIRA.
 *
 * @since 4.3
 */
public class MockGenericValue extends GenericValue
{
    protected boolean created = false;
    protected boolean stored = false;
    protected boolean refreshed = false;
    protected boolean removed = false;
    Map<String,List<GenericValue>> related = newHashMap();

    GenericDelegator gd;
    OfBizDelegator ofBizDelegator = null;

    public MockGenericValue(final GenericValue value)
    {
        this(value.getEntityName());
        this.fields = value.getFields(value.getAllKeys());
    }

    public MockGenericValue(final String entityName)
    {
        super(new ModelEntity(), null);
        this.entityName = entityName;
        this.modelEntity = new MockModelEntity(this);
        this.fields = newHashMap();
    }

    public MockGenericValue(final String entityName, final Map fields)
    {
        this(entityName);

        if (fields != null)
        {
            this.fields = newHashMap(fields);
        }
    }

    public MockGenericValue(final String entityName, final Long id)
    {
        this(entityName, ImmutableMap.<String, Object>of("id", id));
    }

    public MockGenericValue(final String entityName, final ModelEntity modelEntity, final Map<String, Object> fields)
    {
        super(modelEntity, null);
        this.entityName = entityName;
        this.fields = fields != null ? fields : new HashMap<String, Object>();
    }

    @Override
    public Object get(final String name)
    {
        return fields.get(name);
    }

    @Override
    public void set(final String name, final Object value)
    {
        fields.put(name, value);
    }

    @Override
    public Collection<String> getAllKeys()
    {
        return fields.keySet();
    }

    @Override
    public Map<String,Object> getFields(final Collection collection)
    {
        final Map selectedFields = new HashMap();
        for (final Object aCollection : collection)
        {
            final String key = (String) aCollection;
            selectedFields.put(key, fields.get(key));
        }
        return selectedFields;
    }

    @Override
    public synchronized void setFields(final Map<? extends String, ?> keyValuePairs)
    {
        if(keyValuePairs == null)
        {
            this.fields = newHashMap();
        }
        else
        {
            // preserve existing id to facilitate updates over mocked GVs:
            final Object id = get("id");
            this.fields = newHashMap(keyValuePairs);
            if(!keyValuePairs.containsKey("id") && id != null)
            {
                set("id", id);
            }
        }
    }

    @Override
    public Map<String,Object> getAllFields()
    {
        return fields;
    }

    @Override
    public List<GenericValue> getRelated(final String s) throws GenericEntityException
    {
        final List<GenericValue> related = this.related.get(s);
        return related != null ? related : Collections.<GenericValue>emptyList();
    }

    @Override
    public GenericValue getRelatedOne(final String s) throws GenericEntityException {
        final List<GenericValue> related = this.related.get(s);
        return related != null && related.size() > 0 ? related.get(0) : null;
    }

    @Override
    public List<GenericValue> getRelated(final String s, final Map<String,?> map, final List<String> order) throws GenericEntityException
    {
        return CoreFactory.getGenericDelegator().getRelated(s, map, order, this);
    }

    public void setRelated(final String s, final List relatedGVs)
    {
        related.put(s, relatedGVs);
    }

    @Override
    public GenericValue create() throws GenericEntityException
    {
        if (ofBizDelegator != null)
            ofBizDelegator.createValue(this.entityName, this.fields);
        created = true;
        return this;
    }

    public boolean isCreated()
    {
        return created;
    }

    public boolean isStored()
    {
        return stored;
    }

    public boolean isRemoved()
    {
        return removed;
    }

    public boolean isRefreshed()
    {
        return refreshed;
    }

    @Override
    public ModelEntity getModelEntity()
    {
        final ModelEntity result = super.getModelEntity();
        return result != null ? result : new MockModelEntity(this);
    }

    @Override
    public boolean matchesFields(final Map keyValuePairs)
    {
        if (fields == null) return true;
        if (keyValuePairs == null || keyValuePairs.isEmpty()) return true;
        final Iterator entries = keyValuePairs.entrySet().iterator();
        while (entries.hasNext())
        {
            final Map.Entry anEntry = (Map.Entry) entries.next();
            if (!UtilValidate.areEqual(anEntry.getValue(), this.fields.get(anEntry.getKey())))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public GenericPK getPrimaryKey()
    {
        return new GenericPK(getModelEntity(), UtilMisc.toMap("id", fields.get("id")));
    }


    @Override
    public void setDelegator(final GenericDelegator internalDelegator)
    {
        this.gd = internalDelegator;
    }

    @Override
    public GenericDelegator getDelegator()
    {
        return gd;
    }

    @Override
    public void store() throws GenericEntityException
    {
        stored = true;
        if (ofBizDelegator instanceof MockOfBizDelegator)
        {
            ofBizDelegator.store(this);
        }
        else
        {
            CoreFactory.getGenericDelegator().store(this);
        }
    }

    @Override
    public void remove() throws GenericEntityException
    {
        removed = true;
        if (ofBizDelegator instanceof MockOfBizDelegator)
        {
            ofBizDelegator.removeValue(this);
        }
        else
        {
            CoreFactory.getGenericDelegator().removeValue(this);
        }
    }

    @Override
    public void removeRelated(final String relationName) throws GenericEntityException
    {
        related.remove(relationName);
    }

    @Override
    public void refresh() throws GenericEntityException
    {
        refreshed = true;
        CoreFactory.getGenericDelegator().refresh(this);
    }

    @Override
    public String toString()
    {
        final StringBuilder theString = new StringBuilder();
        theString.append("[GenericEntity:");
        theString.append(getEntityName());
        theString.append(']');

        final Iterator entries = fields.entrySet().iterator();
        Map.Entry anEntry;
        while (entries.hasNext())
        {
            anEntry = (Map.Entry) entries.next();
            theString.append('[');
            theString.append(anEntry.getKey());
            theString.append(',');
            theString.append(anEntry.getValue());
            theString.append(']');
        }
        return theString.toString();
    }

    @Override
    public Object dangerousGetNoCheckButFast(final ModelField modelField)
    {
        if (modelField == null) throw new IllegalArgumentException("Cannot get field with a null modelField");
        return fields.get(modelField.getName());
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (!(o instanceof MockGenericValue)) return false;
        if (!super.equals(o)) return false;

        final MockGenericValue mockGenericValue = (MockGenericValue) o;

        if (fields != null ? !fields.equals(mockGenericValue.fields) : mockGenericValue.fields != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 29 * result + (fields != null ? fields.hashCode() : 0);
        result = 29 * result + (created ? 1 : 0);
        return result;
    }

    @Override
    public void setString(final String s, final String s1)
    {
        this.set(s, s1);
    }

    @Override
    public List<GenericValue> getRelatedOrderBy(final String relationName, final List orderBy) throws GenericEntityException
    {
        return CoreFactory.getGenericDelegator().getRelatedOrderBy(relationName, orderBy, this);
    }

    @Override
    public List<GenericValue> getRelatedByAnd(final String relationName, final Map fields) throws GenericEntityException
    {
        return CoreFactory.getGenericDelegator().getRelatedByAnd(relationName, fields, this);
    }

    public class MockModelEntity extends ModelEntity
    {
        GenericValue value;

        public MockModelEntity(){}

        public MockModelEntity(final GenericValue value)
        {
            this.value = value;
            this.setEntityName(value.getEntityName());
        }

        @Override
        public List<String> getAllFieldNames()
        {
            return newArrayList(value.getAllKeys());
        }

        @Override
        public ModelField getField(final String fieldName)
        {
            ModelField field = super.getField(fieldName);

            if (field == null && value.getAllKeys().contains(fieldName))
            {
                field = new ModelField();
                field.setName(fieldName);
            }

            return field;
        }
    }

    @Override
    public Set entrySet()
    {
        return fields.entrySet();
    }

    @Override
    public Set keySet()
    {
        return fields.keySet();
    }

    @Override
    public int size()
    {
        return fields.size();
    }

    @Override
    public boolean isEmpty()
    {
        return fields.isEmpty();
    }

    @Override
    public Collection values()
    {
        return fields.values();
    }

    @Override
    public Object clone()
    {
        final MockGenericValue result = new MockGenericValue(entityName, newHashMap(fields));
        result.related.putAll(this.related);
        result.setOfBizDelegator(ofBizDelegator);
        return result;
    }

    public void setOfBizDelegator(final OfBizDelegator ofBizDelegator)
    {
        this.ofBizDelegator = ofBizDelegator;
    }
}
