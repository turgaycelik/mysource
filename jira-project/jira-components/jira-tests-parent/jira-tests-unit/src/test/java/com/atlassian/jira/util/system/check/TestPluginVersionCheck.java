package com.atlassian.jira.util.system.check;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.ServletContext;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginInformation;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestPluginVersionCheck extends MockControllerTestCase
{
    private BuildUtilsInfo buildUtilsInfo;
    private PluginAccessor pluginAccessor;

    @Before
    public void setUp() throws Exception
    {
        buildUtilsInfo = getMock(BuildUtilsInfo.class);
        expect(buildUtilsInfo.getVersion()).andStubReturn("3.3.1");

        pluginAccessor = getMock(PluginAccessor.class);
    }

    @Test
    public void testCheckWithNoPlugin()
    {
        final AtomicBoolean addErrorsCalled = new AtomicBoolean(false);
        PluginVersionCheck pluginVersionCheck = new PluginVersionCheck(pluginAccessor, buildUtilsInfo)
        {
            @Override
            void addErrors(final ServletContext context, final Set<Plugin> outdatedPlugins)
            {
                addErrorsCalled.set(true);
            }
        };

        expect(pluginAccessor.getPlugin("com.atlassian.jira.ext.charting")).andReturn(null);
        expect(pluginAccessor.getPlugin("com.atlassian.jira.plugin.labels")).andReturn(null);

        mockController.replay();

        pluginVersionCheck.check(null);
        assertFalse(addErrorsCalled.get());
    }

    @Test
    public void testCheckWithMinVersionGood()
    {
        final AtomicBoolean addErrorsCalled = new AtomicBoolean(false);
        PluginVersionCheck pluginVersionCheck = new PluginVersionCheck(pluginAccessor, buildUtilsInfo)
        {
            @Override
            void addErrors(final ServletContext context, final Set<Plugin> outdatedPlugins)
            {
                addErrorsCalled.set(true);
            }
        };

        MockControl mockPluginInformationControl = MockClassControl.createControl(PluginInformation.class);
        PluginInformation mockPluginInformation = (PluginInformation) mockPluginInformationControl.getMock();
        expect(mockPluginInformation.getMinVersion()).andReturn(2.8f);
        mockPluginInformationControl.replay();

        final Plugin mockPlugin = mockController.getMock(Plugin.class);
        expect(mockPlugin.getPluginInformation()).andReturn(mockPluginInformation);

        expect(pluginAccessor.getPlugin("com.atlassian.jira.ext.charting")).andReturn(mockPlugin);
        expect(pluginAccessor.getPlugin("com.atlassian.jira.plugin.labels")).andReturn(null);

        mockController.replay();

        pluginVersionCheck.check(null);
        assertFalse(addErrorsCalled.get());

        mockPluginInformationControl.verify();
    }

    @Test
    public void testCheckWithError()
    {
        final AtomicBoolean addErrorsCalled = new AtomicBoolean(false);

        PluginVersionCheck pluginVersionCheck = new PluginVersionCheck(pluginAccessor, buildUtilsInfo)
        {
            @Override
            void addErrors(final ServletContext context, final Set<Plugin> outdatedPlugins)
            {
                addErrorsCalled.set(true);
            }
        };

        MockControl mockPluginInformationControl = MockClassControl.createControl(PluginInformation.class);
        PluginInformation mockPluginInformation = (PluginInformation) mockPluginInformationControl.getMock();
        //minVersion is above the current app version
        expect(mockPluginInformation.getMinVersion()).andReturn(100.1f);
        mockPluginInformationControl.replay();

        final Plugin mockPlugin = mockController.getMock(Plugin.class);
        expect(mockPlugin.getPluginInformation()).andReturn(mockPluginInformation);

        expect(pluginAccessor.getPlugin("com.atlassian.jira.ext.charting")).andReturn(mockPlugin);
        expect(pluginAccessor.getPlugin("com.atlassian.jira.plugin.labels")).andReturn(null);

        mockController.replay();

        pluginVersionCheck.check(null);
        assertTrue(addErrorsCalled.get());

        mockPluginInformationControl.verify();
    }

    @Test
    public void testBlackListedPlugin()
    {
        final AtomicBoolean addErrorsCalled = new AtomicBoolean(false);

        PluginVersionCheck pluginVersionCheck = new PluginVersionCheck(pluginAccessor, buildUtilsInfo)
        {
            @Override
            void addErrors(final ServletContext context, final Set<Plugin> outdatedPlugins)
            {
                addErrorsCalled.set(true);
            }
        };

        final Plugin mockPlugin = mockController.getMock(Plugin.class);

        expect(pluginAccessor.getPlugin("com.atlassian.jira.ext.charting")).andReturn(null);
        expect(pluginAccessor.getPlugin("com.atlassian.jira.plugin.labels")).andReturn(mockPlugin);

        mockController.replay();

        pluginVersionCheck.check(null);
        assertTrue(addErrorsCalled.get());
    }
}
