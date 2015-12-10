/*
 * Created by IntelliJ IDEA.
 * User: owen
 * Date: Jan 23, 2003
 * Time: 3:47:49 PM
 * CVS Revision: $Revision: 1.4 $
 * Last CVS Commit: $Date: 2005/11/22 00:50:07 $
 * Author of last CVS Commit: $Author: detkin $
 * To change this template use Options | File Templates.
 */
package com.atlassian.configurable;

import java.util.HashMap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestXMLObjectConfigurationFactory
{
    @Test
    public void testGet()
    {
        boolean exceptionThrown = false;
        try
        {
            ObjectConfigurationFactory ocf = new XMLObjectConfigurationFactory();
            ocf.getObjectConfiguration("NONE", null);
            fail("ObjectConfigurationException expected");
        }
        catch (ObjectConfigurationException yay)
        {
            // expected
        }
    }

    @Test
    public void testLoad() throws ObjectConfigurationException
    {
        XMLObjectConfigurationFactory ocf = new XMLObjectConfigurationFactory();
        ocf.loadObjectConfiguration("octest.xml", "octest");
        ObjectConfiguration oc = ocf.getObjectConfiguration("octest", null);
        HashMap map = new HashMap();
        map.put("selectkey1", "selectvalue1");
        map.put("selectkey2", "selectvalue2");
        map.put("selectkey3", "selectvalue3");
        assertNotNull(oc);
        assertEquals("field1", oc.getFieldName("key1"));
        assertEquals("description1", oc.getFieldDescription("key1"));
        assertEquals(1, oc.getFieldType("key1"));
        assertEquals("selectvalue1", oc.getFieldValues("key1").get("selectkey1"));
        assertEquals("selectvalue2", oc.getFieldValues("key1").get("selectkey2"));
        assertEquals("selectvalue3", oc.getFieldValues("key1").get("selectkey3"));
        assertNull(oc.getFieldValues("key1").get("selectkey4"));

        // Test the i18nValues boolean
        assertTrue(oc.isI18NValues("key1"));
        assertFalse(oc.isI18NValues("key2"));

        assertTrue(oc.isEnabled("key1"));
        assertFalse(oc.isEnabled("key2"));
        assertFalse(oc.isEnabled("key3"));
        assertEquals(1, oc.getEnabledFieldKeys().length);
    }
}
