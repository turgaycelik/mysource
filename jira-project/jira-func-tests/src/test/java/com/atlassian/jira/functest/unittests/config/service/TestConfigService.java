package com.atlassian.jira.functest.unittests.config.service;

import com.atlassian.jira.functest.config.ps.ConfigPropertySet;
import com.atlassian.jira.functest.config.service.ConfigService;
import junit.framework.TestCase;

/**
 * Test for {@link com.atlassian.jira.functest.config.service.ConfigService}.
 *
 * @since v4.1
 */
public class TestConfigService extends TestCase
{
    public void testCotr() throws Exception
    {
        ConfigPropertySet set = new ConfigPropertySet(null, null);
        set.setStringProperty("test", "property");

        ConfigService expectedService = new ConfigService();
        assertNull(expectedService.getId());
        assertNull(expectedService.getClazz());
        assertNull(expectedService.getName());
        assertNull(expectedService.getPropertySet());
        assertNull(expectedService.getTimeout());

        expectedService = new ConfigService(null, null, null, null, null);
        assertNull(expectedService.getId());
        assertNull(expectedService.getClazz());
        assertNull(expectedService.getName());
        assertNull(expectedService.getPropertySet());
        assertNull(expectedService.getTimeout());        

        expectedService = new ConfigService(10L, 101L, "klass", "name", set);
        assertEquals(10L, (long)expectedService.getId());
        assertEquals(101L, (long)expectedService.getTimeout());
        assertEquals("name", expectedService.getName());
        assertEquals("klass", expectedService.getClazz());
        assertSame(set, expectedService.getPropertySet());
    }
}