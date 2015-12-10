package com.atlassian.jira.util.system.check;

import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraPropertiesImpl;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.config.properties.SystemPropertiesAccessor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @since v4.0
 */
public class TestJRA18659Check
{
    private JRA18659Check check;
    private final JiraProperties jiraProperties = new JiraPropertiesImpl(new SystemPropertiesAccessor());

    @Before
    public void setUp() throws Exception
    {
        check = new JRA18659Check(jiraProperties);
    }

    @After
    public void tearDown()
    {
        JiraSystemProperties.resetReferences();
    }

    @Test
    public void testJVM150_18() throws Exception
    {
        System.setProperty("java.vm.version", "1.5.0_18");
        System.setProperty("java.vm.vendor", "Sun Microsystems Inc.");

        final I18nMessage message = check.getWarningMessage();
        assertEquals("admin.warning.jra_18659", message.getKey());
    }

    @Test
    public void testJVM150_18_2() throws Exception
    {
        System.setProperty("java.vm.version", "1.5.0_18-b02");
        System.setProperty("java.vm.vendor", "Sun Microsystems Inc.");

        final I18nMessage message = check.getWarningMessage();
        assertEquals("admin.warning.jra_18659", message.getKey());
    }

    @Test
    public void testJVM150_15() throws Exception
    {
        System.setProperty("java.vm.version", "1.5.0_15");
        System.setProperty("java.vm.vendor", "Sun Microsystems Inc.");

        final I18nMessage message = check.getWarningMessage();
        assertEquals("admin.warning.jra_18659", message.getKey());
    }

    @Test
    public void testJVM150_15_2() throws Exception
    {
        System.setProperty("java.vm.version", "1.5.0_15-b02");
        System.setProperty("java.vm.vendor", "Sun Microsystems Inc.");

        final I18nMessage message = check.getWarningMessage();
        assertEquals("admin.warning.jra_18659", message.getKey());
    }

    @Test
    public void testJVM150_18_3() throws Exception
    {
        System.setProperty("java.vm.version", "1.5.0_18-b03");
        System.setProperty("java.vm.vendor", "Sun Microsystems Inc.");

        final I18nMessage message = check.getWarningMessage();
        assertNull(message);
    }

    @Test
    public void testJVM150_19() throws Exception
    {
        System.setProperty("java.vm.version", "1.5.0_19");
        System.setProperty("java.vm.vendor", "Sun Microsystems Inc.");

        final I18nMessage message = check.getWarningMessage();
        assertNull(message);
    }
    
    @Test
    public void testJVM160_5() throws Exception
    {
        System.setProperty("java.vm.version", "1.6.0_5");
        System.setProperty("java.vm.vendor", "Sun Microsystems Inc.");

        final I18nMessage message = check.getWarningMessage();
        assertNull(message);
    }
}
