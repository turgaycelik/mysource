package com.atlassian.jira.plugin.renderercomponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.SubRenderer;
import com.atlassian.renderer.v2.components.RendererComponent;
import com.atlassian.renderer.v2.components.block.BlockRenderer;
import com.atlassian.renderer.v2.components.block.BlockRendererComponent;
import com.atlassian.renderer.v2.components.block.LineWalker;

import com.mockobjects.dynamic.Mock;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/** @since v3.12 */
public class TestBlockRendererComponentFactory
{
    @Test
    public void testInitMissingParam()
    {
        MockControl mockRendererComponentFactoryDescriptorControl = MockClassControl
                .createControl(RendererComponentFactoryDescriptor.class);
        RendererComponentFactoryDescriptor mockRendererComponentFactoryDescriptor = (RendererComponentFactoryDescriptor) mockRendererComponentFactoryDescriptorControl
                .getMock();

        mockRendererComponentFactoryDescriptor.getListParams();
        mockRendererComponentFactoryDescriptorControl.setReturnValue(new HashMap());
        mockRendererComponentFactoryDescriptor.getCompleteKey();
        mockRendererComponentFactoryDescriptorControl.setReturnValue("pluginkey");
        mockRendererComponentFactoryDescriptorControl.replay();

        BlockRendererComponentFactory blockRendererComponentFactory = new BlockRendererComponentFactory(null);
        try
        {
            blockRendererComponentFactory.init(mockRendererComponentFactoryDescriptor);
            fail("Expected exception");
        }
        catch (PluginParseException e)
        {
            assertTrue(e.getMessage().indexOf("Missing required list-param") > 0);
            assertTrue(e.getMessage().indexOf("pluginkey") > 0);
        }
    }

    @Test
    public void testInitInvalidClass()
    {
        MockControl mockPluginControl = MockClassControl.createControl(Plugin.class);
        Plugin mockPlugin = (Plugin) mockPluginControl.getMock();

        MockControl mockRendererComponentFactoryDescriptorControl = MockClassControl
                .createControl(RendererComponentFactoryDescriptor.class);
        RendererComponentFactoryDescriptor mockRendererComponentFactoryDescriptor = (RendererComponentFactoryDescriptor) mockRendererComponentFactoryDescriptorControl
                .getMock();
        Map listParams = new HashMap();
        List blockRenderers = new ArrayList();
        blockRenderers.add("com.test.invalid.class");
        listParams.put("blockrenderers", blockRenderers);

        mockRendererComponentFactoryDescriptor.getListParams();
        mockRendererComponentFactoryDescriptorControl.setReturnValue(listParams);
        mockRendererComponentFactoryDescriptor.getPlugin();
        mockRendererComponentFactoryDescriptorControl.setReturnValue(mockPlugin);
        mockRendererComponentFactoryDescriptor.getCompleteKey();
        mockRendererComponentFactoryDescriptorControl.setReturnValue("pluginkey");
        mockRendererComponentFactoryDescriptorControl.replay();

        BlockRendererComponentFactory blockRendererComponentFactory = new BlockRendererComponentFactory(null)
        {
            BlockRenderer loadBlockRenderer(String rendererComponentClass) throws ClassNotFoundException
            {
                return null;
            }
        };

        try
        {
            mockPlugin.loadClass("com.test.invalid.class", blockRendererComponentFactory.getClass());
            mockPluginControl.setThrowable(new ClassNotFoundException());
            mockPluginControl.replay();
        }
        catch (ClassNotFoundException e)
        {
            // the mock will never throw this exception but we need to catch it
        }

        try
        {
            blockRendererComponentFactory.init(mockRendererComponentFactoryDescriptor);
            fail("Expected exception");
        }
        catch (PluginParseException e)
        {
            assertTrue(e.getMessage().indexOf("Could not load block renderer class") >= 0);
            assertTrue(e.getMessage().indexOf("pluginkey") > 0);
            assertTrue(e.getMessage().indexOf("com.test.invalid.class") > 0);
        }
    }

    @Test
    public void testInitSuccessful() throws PluginParseException, ClassNotFoundException
    {
        MockControl mockPluginControl = MockClassControl.createControl(Plugin.class);
        Plugin mockPlugin = (Plugin) mockPluginControl.getMock();
        
        MockControl mockRendererComponentFactoryDescriptorControl = MockClassControl
                .createControl(RendererComponentFactoryDescriptor.class);
        RendererComponentFactoryDescriptor mockRendererComponentFactoryDescriptor = (RendererComponentFactoryDescriptor) mockRendererComponentFactoryDescriptorControl
                .getMock();

        final Mock mockRenderer = new Mock(BlockRenderer.class);
        final BlockRenderer originalRenderer = (BlockRenderer) mockRenderer.proxy();

        Map listParams = new HashMap();
        List blockRenderers = new ArrayList();
        blockRenderers.add(TestBlockRenderer.class.getName());
        listParams.put("blockrenderers", blockRenderers);

        mockRendererComponentFactoryDescriptor.getListParams();
        mockRendererComponentFactoryDescriptorControl.setReturnValue(listParams);
        mockRendererComponentFactoryDescriptor.getPlugin();
        mockRendererComponentFactoryDescriptorControl.setReturnValue(mockPlugin);
        mockRendererComponentFactoryDescriptor.getCompleteKey();
        mockRendererComponentFactoryDescriptorControl.setReturnValue("pluginkey");
        mockRendererComponentFactoryDescriptorControl.replay();

        BlockRendererComponentFactory blockRendererComponentFactory = new BlockRendererComponentFactory(null)
        {
            BlockRenderer loadBlockRenderer(String rendererComponentClass) throws ClassNotFoundException
            {
                return originalRenderer;
            }
        };

        mockPlugin.loadClass(TestBlockRenderer.class.getName(), blockRendererComponentFactory.getClass());
        mockPluginControl.setReturnValue(TestBlockRenderer.class);
        mockPluginControl.replay();

        blockRendererComponentFactory.init(mockRendererComponentFactoryDescriptor);
        RendererComponent rendererComponent = blockRendererComponentFactory.getRendererComponent();
        assertNotNull(rendererComponent);
        assertTrue(rendererComponent instanceof BlockRendererComponent);
    }

    public static final class TestBlockRenderer implements BlockRenderer
    {
        public String renderNextBlock(String arg0, LineWalker arg1, RenderContext arg2, SubRenderer arg3)
        {
            return null;
        }
    }
}
