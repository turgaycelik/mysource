package com.atlassian.jira.plugin.jql.function;

import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.plugin.webwork.TestDefaultAutowireCapableWebworkActionRegistry;
import com.atlassian.plugin.module.ModuleFactory;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestJqlFunctionModuleDescriptorImpl
{
    @Test
    public void testInitHappyPath() throws Exception
    {
        final JqlFunctionModuleDescriptor moduleDescriptor = new JqlFunctionModuleDescriptorImpl(new MockSimpleAuthenticationContext(null), ModuleFactory.LEGACY_MODULE_FACTORY);

        Document document = DocumentHelper.parseText(
                "<jql-function key=\"now-jql-function\" i18n-name-key=\"jql.function.plugin.now.name\" name=\"Now Function\"\n"
                + "            class=\"com.atlassian.jira.plugin.jql.function.NowFunction\" system=\"true\">\n"
                + "        <description key=\"jql.function.plugin.now.desc\">Returns the current system time.</description>\n"
                + "        <fname>now</fname>\n"
                + "        <list>true</list>\n"
                + "    </jql-function>");

        moduleDescriptor.init(new TestDefaultAutowireCapableWebworkActionRegistry.MockPlugin("now-jql-function"), document.getRootElement());

        assertEquals("now", moduleDescriptor.getFunctionName());
        assertTrue(moduleDescriptor.isList());
    }

    @Test
    public void testInitDefaultIsList() throws Exception
    {
        final JqlFunctionModuleDescriptor moduleDescriptor = new JqlFunctionModuleDescriptorImpl(new MockSimpleAuthenticationContext(null), ModuleFactory.LEGACY_MODULE_FACTORY);

        Document document = DocumentHelper.parseText(
                "<jql-function key=\"now-jql-function\" i18n-name-key=\"jql.function.plugin.now.name\" name=\"Now Function\"\n"
                + "            class=\"com.atlassian.jira.plugin.jql.function.NowFunction\" system=\"true\">\n"
                + "        <description key=\"jql.function.plugin.now.desc\">Returns the current system time.</description>\n"
                + "        <fname>howNow</fname>\n"
                + "    </jql-function>");

        moduleDescriptor.init(new TestDefaultAutowireCapableWebworkActionRegistry.MockPlugin("now-jql-function"), document.getRootElement());

        assertEquals("howNow", moduleDescriptor.getFunctionName());
        assertFalse(moduleDescriptor.isList());
    }

    @Test
    public void testInitNoFname() throws Exception
    {
        final JqlFunctionModuleDescriptor moduleDescriptor = new JqlFunctionModuleDescriptorImpl(new MockSimpleAuthenticationContext(null), ModuleFactory.LEGACY_MODULE_FACTORY);

        Document document = DocumentHelper.parseText(
                "<jql-function key=\"now-jql-function\" i18n-name-key=\"jql.function.plugin.now.name\" name=\"Now Function\"\n"
                + "            class=\"com.atlassian.jira.plugin.jql.function.NowFunction\" system=\"true\">\n"
                + "        <description key=\"jql.function.plugin.now.desc\">Returns the current system time.</description>\n"
                + "    </jql-function>");

        moduleDescriptor.init(new TestDefaultAutowireCapableWebworkActionRegistry.MockPlugin("now-jql-function"), document.getRootElement());
        assertNull(moduleDescriptor.getFunctionName());
        assertFalse(moduleDescriptor.isList());
    }

    @Test
    public void testInitBlankFnameThrowsException() throws Exception
    {
        final JqlFunctionModuleDescriptor moduleDescriptor = new JqlFunctionModuleDescriptorImpl(new MockSimpleAuthenticationContext(null), ModuleFactory.LEGACY_MODULE_FACTORY);

        Document document = DocumentHelper.parseText(
                "<jql-function key=\"now-jql-function\" i18n-name-key=\"jql.function.plugin.now.name\" name=\"Now Function\"\n"
                + "            class=\"com.atlassian.jira.plugin.jql.function.NowFunction\" system=\"true\">\n"
                + "        <description key=\"jql.function.plugin.now.desc\">Returns the current system time.</description>\n"
                + "        <fname>     </fname>\n"
                + "    </jql-function>");

        moduleDescriptor.init(new TestDefaultAutowireCapableWebworkActionRegistry.MockPlugin("now-jql-function"), document.getRootElement());
        assertNull(moduleDescriptor.getFunctionName());
        assertFalse(moduleDescriptor.isList());
    }
}
