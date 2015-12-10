package com.atlassian.jira.util.system.check;

import com.atlassian.jira.config.properties.JiraPropertiesImpl;
import com.atlassian.jira.config.properties.SystemPropertiesAccessor;
import com.atlassian.jira.help.HelpUrls;
import com.atlassian.jira.help.MockHelpUrls;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * A JUNIT Test for the JVMCheck
 *
 * @since v4.0
 */
public class TestJVMCheck
{
    @Rule
    public RuleChain container = MockitoMocksInContainer.forTest(this);

    @AvailableInContainer
    private HelpUrls urls = new MockHelpUrls();

    private final JVMCheck check = new JVMCheck(new JiraPropertiesImpl(new SystemPropertiesAccessor()));

    @Test
    public void testJVM16_0_07()
    {
        System.setProperty("java.vm.vendor", "Sun Microsystems Inc.");
        System.setProperty("java.version", "1.6.0_07");
        // beware - Sun started using a different version number system for the "vm version" sometime between 1.6.0 and 1.6.0_07
        System.setProperty("java.vm.version", "10.0-b23");

        final I18nMessage warningMessage = check.getWarningMessage();
        assertNotNull(warningMessage);
        assertEquals("admin.warning.jvmversion6", warningMessage.getKey());
    }


    @Test
    public void testJVM16_0_18()
    {
        System.setProperty("java.vm.vendor", "Sun Microsystems Inc.");
        System.setProperty("java.version", "1.6.0_18");
        // beware - Sun started using a different version number system for the "vm version" sometime between 1.6.0 and 1.6.0_07
        System.setProperty("java.vm.version", "11.0-b15");

        final I18nMessage warningMessage = check.getWarningMessage();
        assertNull(warningMessage);
    }


    @Test
    public void testJVM16_0_17()
    {
        System.setProperty("java.vm.vendor", "Sun Microsystems Inc.");
        System.setProperty("java.version", "1.6.0_17");
        // beware - Sun started using a different version number system for the "vm version" sometime between 1.6.0 and 1.6.0_07
        System.setProperty("java.vm.version", "11.0-b15");

        final I18nMessage warningMessage = check.getWarningMessage();
        assertNotNull(warningMessage);
        assertEquals("admin.warning.jvmversion6", warningMessage.getKey());
    }

    @Test
    public void testJVM16_0()
    {
        System.setProperty("java.vm.vendor", "Sun Microsystems Inc.");
        System.setProperty("java.version", "1.6.0");
        // beware - Sun started using a different version number system for the "vm version" sometime between 1.6.0 and 1.6.0_07
        System.setProperty("java.vm.version", "1.6.0-b105");

        final I18nMessage warningMessage = check.getWarningMessage();
        assertNotNull(warningMessage);
        assertEquals("admin.warning.jvmversion6", warningMessage.getKey());
    }

    @Test
    public void testInvalidJVMString()
    {
        System.setProperty("java.vm.vendor", "Sun Microsystems Inc.");
        System.setProperty("java.version", "1.6.0_BLAH");
        // beware - Sun started using a different version number system for the "vm version" sometime between 1.6.0 and 1.6.0_07
        System.setProperty("java.vm.version", "10.0-b23");

        final I18nMessage warningMessage = check.getWarningMessage();
        // No message to show, we are really just testing that it doesn't blow up.
        assertNull(warningMessage);
    }

}
