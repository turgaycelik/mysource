/*
 * Created by IntelliJ IDEA.
 * User: owen
 * Date: Jan 23, 2003
 * Time: 1:21:57 PM
 * CVS Revision: $Revision: 1.6 $
 * Last CVS Commit: $Date: 2004/08/31 05:38:55 $
 * Author of last CVS Commit: $Author: amazkovoi $
 * To change this template use Options | File Templates.
 */
package com.atlassian.configurable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TestObjectConfigurationImpl
{
    ObjectConfiguration impl;

    @Before
    public void setUp() throws Exception
    {
        HashMap values = new HashMap();
        for (int i = 0; i < 6; i++)
        {
            Map map = new HashMap();
            for (int j = 0; j < 6; j++)
            {
                map.put("key" + i + " " + j, "value" + i + " " + j);
            }
            values.put("field" + i, new ObjectConfigurationPropertyImpl("fieldNameValue" + i, "fieldDescriptionValue" + i, "default" + i, i, map));
        }

        impl = new ObjectConfigurationImpl(values, new StringObjectDescription("object description"));
    }

    @Test
    public void testInit()
    {
        impl.init(null);
        impl.init(new HashMap());
        Map map = new HashMap();
        map.put("", "");
        impl.init(map);
    }

    @Test
    public void testGetFieldName() throws ObjectConfigurationException
    {
        assertEquals("fieldNameValue0", impl.getFieldName("field0"));
        assertEquals("fieldNameValue1", impl.getFieldName("field1"));
        assertEquals("fieldNameValue2", impl.getFieldName("field2"));
        assertEquals("fieldNameValue3", impl.getFieldName("field3"));
        assertEquals("fieldNameValue4", impl.getFieldName("field4"));
        assertEquals("fieldNameValue5", impl.getFieldName("field5"));
    }

    @Test
    public void testGetFieldDescription() throws ObjectConfigurationException
    {
        assertEquals("fieldDescriptionValue0", impl.getFieldDescription("field0"));
        assertEquals("fieldDescriptionValue1", impl.getFieldDescription("field1"));
        assertEquals("fieldDescriptionValue2", impl.getFieldDescription("field2"));
        assertEquals("fieldDescriptionValue3", impl.getFieldDescription("field3"));
        assertEquals("fieldDescriptionValue4", impl.getFieldDescription("field4"));
        assertEquals("fieldDescriptionValue5", impl.getFieldDescription("field5"));
    }

    @Test
    public void testGetFieldType() throws ObjectConfigurationException
    {
        assertEquals(0, impl.getFieldType("field0"));
        assertEquals(1, impl.getFieldType("field1"));
        assertEquals(2, impl.getFieldType("field2"));
        assertEquals(3, impl.getFieldType("field3"));
        assertEquals(4, impl.getFieldType("field4"));
        assertEquals(5, impl.getFieldType("field5"));
    }

    @Test
    public void testGetFieldValues() throws ObjectConfigurationException
    {
        for (int i = 0; i < 6; i++)
        {
            assertTrue(impl.getFieldValues("field" + i) instanceof Map);
        }
    }

    @Test
    public void testGetFieldValuesHtmlEncoded() throws ObjectConfigurationException
    {
        for (int i = 0; i < 6; i++)
        {
            assertTrue(impl.getFieldValuesHtmlEncoded("field" + i) instanceof Map);
        }
    }

    @Test
    public void testGetFieldNames()
    {
        String[] fieldNames = impl.getFieldKeys();
        assertEquals(6, fieldNames.length);
        for (int i = 0; i < fieldNames.length; i++)
        {
            boolean found = false;
            for (int j = 0; j < fieldNames.length; j++)
            {
                String fieldName = fieldNames[j];
                if (("field" + i).equals(fieldName))
                    found = true;
            }
            assertTrue(found);
        }
    }

    @Test
    public void testAllFieldsHidden() throws ObjectConfigurationException
    {
        assertFalse(impl.allFieldsHidden());

        // Test configuration with no fields
        ObjectConfigurationImpl oc = new ObjectConfigurationImpl(Collections.EMPTY_MAP, new StringObjectDescription("object description2"));
        assertTrue(oc.allFieldsHidden());

        // Test configuration with only hidden fields
        HashMap values = new HashMap();
        for (int i = 0; i < 6; i++)
        {
            Map map = new HashMap();
            for (int j = 0; j < 6; j++)
            {
                map.put("key" + i + " " + j, "value" + i + " " + j);
            }
            values.put("field" + i, new ObjectConfigurationPropertyImpl("fieldNameValue" + i, "fieldDescriptionValue" + i, "default" + i, ObjectConfigurationTypes.HIDDEN, map));
        }

        oc = new ObjectConfigurationImpl(values, new StringObjectDescription("object description2"));
        assertTrue(oc.allFieldsHidden());

    }

    @Test
    public void testGetFieldValuesHtmlEncodedEncodes() throws ObjectConfigurationException
    {
        HashMap values = new HashMap();
        for (int i = 0; i < 6; i++)
        {
            Map map = new HashMap();
            map.put("<script id='key'>alert(3)</script>", "<script id='value'>alert(3)</script>");
            values.put("customfield_" + i, new ObjectConfigurationPropertyImpl("fieldNameValue" + i, "fieldDescriptionValue" + i, "default" + i, i, map));
        }
        ObjectConfigurationImpl oc = new ObjectConfigurationImpl(values, new StringObjectDescription("object description3"));

        for (int i = 0; i < 6; i++)
        {
            Map <String, String> map = oc.getFieldValuesHtmlEncoded("customfield_" + i);
            for (Map.Entry entry : map.entrySet() )
            {
                assertThat(entry.getValue().toString(), is("&lt;script id=&#39;value&#39;&gt;alert(3)&lt;/script&gt;") );
                assertThat(entry.getKey().toString(), is("&lt;script id=&#39;key&#39;&gt;alert(3)&lt;/script&gt;") );
            }

        }
    }

}
