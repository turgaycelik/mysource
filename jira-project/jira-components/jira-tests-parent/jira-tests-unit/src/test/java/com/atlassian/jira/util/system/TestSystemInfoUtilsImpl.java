package com.atlassian.jira.util.system;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the SystemInfoUtilsImpl class
 */
public class TestSystemInfoUtilsImpl
{


    @Test
    public void testMaskDatabaseURL()
    {
        assertEquals("jdbc:hsqldb:/database/hsqldb", SystemInfoUtilsImpl.MaskedUrlDatabaseMetaData.maskDatabaseUrl("jdbc:hsqldb:/database/hsqldb"));
        assertEquals("jdbc:hsqldb:/database/hsqldb&password=****", SystemInfoUtilsImpl.MaskedUrlDatabaseMetaData.maskDatabaseUrl("jdbc:hsqldb:/database/hsqldb&password=password"));
        assertEquals("jdbc:hsqldb:/database/password&password=****", SystemInfoUtilsImpl.MaskedUrlDatabaseMetaData.maskDatabaseUrl("jdbc:hsqldb:/database/password&password=password"));
        assertEquals("jdbc:hsqldb:/database/h=s=q=l=d=b&type=db&password=****&username=bob;", SystemInfoUtilsImpl.MaskedUrlDatabaseMetaData.maskDatabaseUrl("jdbc:hsqldb:/database/h=s=q=l=d=b&type=db&password=p1aps1p&username=bob;"));
        assertEquals("jdbc:hsqldb:/database/h=s=q=l=d=b&type=db&password=****&username=bob;", SystemInfoUtilsImpl.MaskedUrlDatabaseMetaData.maskDatabaseUrl("jdbc:hsqldb:/database/h=s=q=l=d=b&type=db&password=&username=bob;"));
        assertEquals("jdbc:hsqldb:/database/hbab&type=db&password=****&username=bob&password=****&anotherUser=50;", SystemInfoUtilsImpl.MaskedUrlDatabaseMetaData.maskDatabaseUrl("jdbc:hsqldb:/database/hbab&type=db&password=&username=bob&password=ANOTHER $#%^*#$@AND#*SLAKDF-=__++__ PASSWORD??  &anotherUser=50;"));
        assertEquals("jdbc:hsqldb:/database/hsqldb&password=****", SystemInfoUtilsImpl.MaskedUrlDatabaseMetaData.maskDatabaseUrl("jdbc:hsqldb:/database/hsqldb&PASSWORD=password"));
        assertEquals("jdbc:hsqldb:/database/hsqldb&password=****", SystemInfoUtilsImpl.MaskedUrlDatabaseMetaData.maskDatabaseUrl("jdbc:hsqldb:/database/hsqldb&pAsSWorD=PASSWORD"));
        assertEquals("", SystemInfoUtilsImpl.MaskedUrlDatabaseMetaData.maskDatabaseUrl(""));
        assertNull(SystemInfoUtilsImpl.MaskedUrlDatabaseMetaData.maskDatabaseUrl(null));
    }

    @Test
    public void testJVMMemoryFunctions()
    {
        SystemInfoUtilsImpl sysInfoUtils = new SystemInfoUtilsImpl();
        // Test that the free + used memory = total memory.
        // Need to check difference is < 2 because of rounding when values are truncated to Megabytes.
        assertTrue(Math.abs(sysInfoUtils.getTotalMemory() - (sysInfoUtils.getFreeMemory() + sysInfoUtils.getUsedMemory())) < 2);
        assertTrue(Math.abs(sysInfoUtils.getTotalPermGenMemory() - (sysInfoUtils.getFreePermGenMemory() + sysInfoUtils.getUsedPermGenMemory())) < 2);
        assertTrue(Math.abs(sysInfoUtils.getTotalNonHeapMemory() - (sysInfoUtils.getFreeNonHeapMemory() + sysInfoUtils.getUsedNonHeapMemory())) < 2);
    }
}
