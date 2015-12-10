package com.atlassian.jira.util.ofbiz;

import java.util.Collections;
import java.util.Map;

import com.atlassian.core.util.map.EasyMap;

import org.junit.Test;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelField;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class TestImmutableGenericValue
{
    private final ImmutableGenericValue gv = new ImmutableGenericValue(new GenericDelegator()
    {
        public ModelEntity getModelEntity(String entityName)
        {
            return new ModelEntity();
        }
    }, "name", Collections.EMPTY_MAP);

    @Test
    public final void testSetStringObjectBoolean()
    {
        try
        {
            gv.set("name", new Object(), true);
            fail("should have thrown");
        }
        catch (UnsupportedOperationException yay)
        {}
    }

    @Test
    public final void testSetPKFieldsMapBoolean()
    {
        try
        {
            gv.setPKFields(Collections.EMPTY_MAP, true);
            fail("should have thrown");
        }
        catch (UnsupportedOperationException yay)
        {}
    }

    @Test
    public final void testSetFields()
    {
        try
        {
            gv.setFields(Collections.EMPTY_MAP);
            fail("should have thrown");
        }
        catch (UnsupportedOperationException yay)
        {}
    }

    @Test
    public final void testRemoveObject()
    {
        try
        {
            gv.remove(new Object());
            fail("should have thrown");
        }
        catch (UnsupportedOperationException yay)
        {}
    }

    @Test
    public final void testPut()
    {
        try
        {
            gv.put("name", new Object());
            fail("should have thrown");
        }
        catch (UnsupportedOperationException yay)
        {}
    }

    @Test
    public final void testStore() throws GenericEntityException
    {
        try
        {
            gv.store();
            fail("should have thrown");
        }
        catch (UnsupportedOperationException yay)
        {}
    }

    @Test
    public final void testRemove() throws GenericEntityException
    {
        try
        {
            gv.remove();
            fail("should have thrown");
        }
        catch (UnsupportedOperationException yay)
        {}
    }

    @Test
    public final void testRefresh() throws GenericEntityException
    {
        try
        {
            gv.refresh();
            fail("should have thrown");
        }
        catch (UnsupportedOperationException yay)
        {}
    }

    @Test
    public final void testRemoveRelated() throws GenericEntityException
    {
        try
        {
            gv.removeRelated("stuff");
            fail("should have thrown");
        }
        catch (UnsupportedOperationException yay)
        {}
    }

    @Test
    public final void testPutAllMap()
    {
        try
        {
            gv.putAll(Collections.EMPTY_MAP);
            fail("should have thrown");
        }
        catch (UnsupportedOperationException yay)
        {}
    }

    @Test
    public final void testSetDelegator()
    {
        try
        {
            gv.setDelegator(new GenericDelegator()
            {});
            fail("should have thrown");
        }
        catch (UnsupportedOperationException yay)
        {}
    }

    @Test
    public final void testSetString()
    {
        try
        {
            gv.setString("name", "value");
            fail("should have thrown");
        }
        catch (UnsupportedOperationException yay)
        {}
    }

    @Test
    public final void testSetBytes()
    {
        try
        {
            gv.setBytes("name", new byte[16]);
            fail("should have thrown");
        }
        catch (UnsupportedOperationException yay)
        {}
    }

    @Test
    public final void testClear()
    {
        try
        {
            gv.clear();
            fail("should have thrown");
        }
        catch (UnsupportedOperationException yay)
        {}
    }

    @Test
    public void testConstructorWithMapValues() throws Exception
    {
        final ModelEntity modelEntity = new ModelEntity();
        modelEntity.addField(getModelField("key"));
        modelEntity.addField(getModelField("name"));
        modelEntity.addField(getModelField("id"));
        assertNotNull(modelEntity.getField("key"));
        assertNotNull(modelEntity.getField("name"));
        assertNotNull(modelEntity.getField("id"));
        assertNull(modelEntity.getField("description"));

        Map values = EasyMap.build("name", "the.name", "key", "the.key", "id", "the.id");
        GenericValue value = new ImmutableGenericValue(new GenericDelegator()
        {
            public ModelEntity getModelEntity(String entityName)
            {
                return modelEntity;
            }
        }, "name", values);

        assertEquals("the.id", value.get("id"));
        assertEquals("the.name", value.get("name"));
        assertEquals("the.key", value.get("key"));
        try
        {
            assertNull(value.get("description"));
            fail("should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException yay)
        {}
    }

    private ModelField getModelField(String name)
    {
        ModelField field = new ModelField();
        field.setName(name);
        field.setColName(name);
        return field;
    }
}
