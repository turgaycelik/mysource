/*
 * Created by IntelliJ IDEA.
 * User: owen
 * Date: Jan 20, 2003
 * Time: 2:05:35 PM
 * CVS Revision: $Revision: 1.1 $
 * Last CVS Commit: $Date: 2003/09/30 07:11:38 $
 * Author of last CVS Commit: $Author: mcannon $
 * To change this template use Options | File Templates.
 */
package com.atlassian.core.ofbiz;

import java.util.Map;

import org.junit.Test;
import org.ofbiz.core.util.UtilMisc;

import static org.junit.Assert.assertEquals;

public class TestUtilMisc
{
    @Test
    public void testSimpleMap()
    {
        Map subFields;
        subFields = UtilMisc.toMap("repeatCount",new Long(7),
                "repeatInterval",new Long(2),
                "timesTriggered",new Long(3),"trigger",new Long(-1));
        assertEquals(4, subFields.size());
        assertEquals(new Long(-1),subFields.get("trigger"));
        subFields.put("trigger",new Long(1));
        assertEquals(4, subFields.size());
        assertEquals(new Long(1),subFields.get("trigger"));
        assertEquals(new Long(7),subFields.get("repeatCount"));
        assertEquals(new Long(2),subFields.get("repeatInterval"));
        assertEquals(new Long(3),subFields.get("timesTriggered"));
    }
}
