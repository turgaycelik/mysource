package com.atlassian.jira.plugin;

import com.atlassian.jira.mock.plugin.MockPlugin;
import com.atlassian.jira.mock.plugin.MockPluginAccessor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;

import java.io.BufferedInputStream;
import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 */
public class PluginVelocityResourceBundleMockBundleLoaderTest
{
    private static final InputStream EXPECTED_STREAM = new BufferedInputStream(null);
    private static final String NEVER_FOUND_PLUGINKEY = "NeverMore";
    private static final String NEVER_FOUND_MODULEKEY = ":QuothTheRaven";

    PluginAccessor pluginAccessor;
    private PluginVelocityResourceLoader pluginVelocityResourceLoader;


    @Before
    public void setUp() throws Exception
    {
        pluginVelocityResourceLoader = new PluginVelocityResourceLoader()
        {
            @Override
            PluginAccessor pluginAccessor()
            {
                return pluginAccessor;
            }
        };
    }

    @org.junit.Test
    public void testFullyQualifiedGetResourceStream() throws Exception
    {
        assertResource("good looking template name",
                "pluginKey:module//template/template.vm", "pluginKey", "template/template.vm");

        assertResource("valid names with dots",
                "plugin.Key:module.Key/template.templateName", "plugin.Key", "template.templateName");

        assertResource("removes preceding slashes on the name",
                "pluginKey:module//template/template.vm", "pluginKey", "template/template.vm");

        assertResource("removes preceding slashes on the name",
                "pluginKey:module/////template/template.vm", "pluginKey", "template/template.vm");


        assertResource("old school no pluginKey involved",
                "template/template.vm", "template/template.vm");

        assertResource("old school - removes preceding slashes",
                "/template/template.vm", "template/template.vm");

        assertResource("old school - removes preceding slashes",
                "////template/template.vm", "template/template.vm");


        assertResource("weird looking names fall through to old school ways",
                "pluginKey/moduleName:/template/template.vm", "pluginKey/moduleName:/template/template.vm");

        assertResource("weird looking names fall through to old school ways",
                "pluginKey:moduleName:/template/template.vm", "pluginKey:moduleName:/template/template.vm");
    }

    @org.junit.Test
    public void testResourceNotFoundFallsThrough() throws Exception
    {
        assertResource("", NEVER_FOUND_PLUGINKEY + NEVER_FOUND_MODULEKEY + "/template/templatename.vm", "", "template/templatename.vm");
    }

    private void assertResource(final String reason, final String fullResourceName, final String expectedResourceName)
    {
        assertResource(reason, fullResourceName, null, expectedResourceName);
    }

    private void assertResource(final String reason, final String fullResourceName, final String expectedPluginKey, final String expectedResourceName)
    {
        AssertingPlugin plugin = new AssertingPlugin(reason, expectedResourceName);
        if (StringUtils.isNotEmpty(expectedPluginKey))
        {
            pluginAccessor = new AssertingPluginAccessor(reason, expectedPluginKey, plugin);
        }
        else
        {
            pluginAccessor = new AssertingPluginAccessor(reason, expectedResourceName);
        }
        InputStream actualStream = pluginVelocityResourceLoader.getResourceStream(fullResourceName);
        assertThat(reason, actualStream, equalTo(EXPECTED_STREAM));
    }

    private class AssertingPluginAccessor extends MockPluginAccessor
    {
        private final String reason;
        private final String expectedPluginKey;
        private final String dynamicResourcePath;
        private final AssertingPlugin plugin;

        private AssertingPluginAccessor(final String reason, final String expectedPluginKey, final AssertingPlugin plugin)
        {
            this.reason = reason;
            this.expectedPluginKey = expectedPluginKey;
            this.plugin = plugin;
            this.dynamicResourcePath = null;
        }

        private AssertingPluginAccessor(final String reason, final String dynamicResourcePath)
        {
            this.reason = reason;
            this.expectedPluginKey = null;
            this.plugin = null;
            this.dynamicResourcePath = dynamicResourcePath;
        }

        @Override
        public Plugin getPlugin(final String key) throws IllegalArgumentException
        {
            if (NEVER_FOUND_PLUGINKEY.equals(key))
            {
                return null;
            }
            assertThat(reason, key, equalTo(expectedPluginKey));
            return plugin;
        }

        @Override
        public InputStream getDynamicResourceAsStream(final String resourcePath)
        {
            assertThat(reason, resourcePath, equalTo(dynamicResourcePath));
            return EXPECTED_STREAM;
        }
    }

    private class AssertingPlugin extends MockPlugin
    {
        private final String reason;
        private final String resourceName;

        public AssertingPlugin(final String reason, final String resourceName)
        {
            super(null, null, null);
            this.reason = reason;
            this.resourceName = resourceName;
        }

        @Override
        public InputStream getResourceAsStream(final String name)
        {
            assertThat(reason, name, equalTo(resourceName));
            return EXPECTED_STREAM;
        }
    }
}
