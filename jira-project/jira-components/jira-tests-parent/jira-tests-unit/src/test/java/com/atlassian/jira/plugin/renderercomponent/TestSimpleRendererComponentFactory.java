package com.atlassian.jira.plugin.renderercomponent;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.renderer.v2.components.RendererComponent;

import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v3.12
 */
public class TestSimpleRendererComponentFactory
{
    @Test
    public void testInitMissingParam()
    {
        MockControl mockRendererComponentFactoryDescriptorControl = MockClassControl
                .createControl(RendererComponentFactoryDescriptor.class);
        RendererComponentFactoryDescriptor mockRendererComponentFactoryDescriptor = (RendererComponentFactoryDescriptor) mockRendererComponentFactoryDescriptorControl
                .getMock();
        mockRendererComponentFactoryDescriptor.getPlugin();
        mockRendererComponentFactoryDescriptorControl.setReturnValue(null);
        mockRendererComponentFactoryDescriptor.getParams();
        mockRendererComponentFactoryDescriptorControl.setReturnValue(new HashMap());
        mockRendererComponentFactoryDescriptor.getCompleteKey();
        mockRendererComponentFactoryDescriptorControl.setReturnValue("pluginkey");
        mockRendererComponentFactoryDescriptorControl.replay();

        SimpleRendererComponentFactory simpleRendererComponentFactory = new SimpleRendererComponentFactory();
        try
        {
            simpleRendererComponentFactory.init(mockRendererComponentFactoryDescriptor);
            fail("Expected exception");
        }
        catch (PluginParseException e)
        {
            assertTrue(e.getMessage().indexOf("is missing the required") > 0);
            assertTrue(e.getMessage().indexOf("pluginkey") > 0);
        }
    }

    @Test
    public void testInitInvalidClass()
    {
        MockControl mockRendererComponentFactoryDescriptorControl = MockClassControl.createControl(RendererComponentFactoryDescriptor.class);
        RendererComponentFactoryDescriptor mockRendererComponentFactoryDescriptor = (RendererComponentFactoryDescriptor) mockRendererComponentFactoryDescriptorControl.getMock();
        Mock mockPlugin = new Mock(Plugin.class);

        mockRendererComponentFactoryDescriptor.getPlugin();
        mockRendererComponentFactoryDescriptorControl.setReturnValue(null);
        Map params = new HashMap();
        params.put("rendererComponentClass", "com.test.invalid.class");
        mockRendererComponentFactoryDescriptor.getParams();
        mockRendererComponentFactoryDescriptorControl.setReturnValue(params);
        mockRendererComponentFactoryDescriptor.getCompleteKey();
        mockRendererComponentFactoryDescriptorControl.setReturnValue("pluginkey");
        mockRendererComponentFactoryDescriptor.getPlugin();
        mockRendererComponentFactoryDescriptorControl.setReturnValue(mockPlugin.proxy());
        mockPlugin.expectAndThrow("loadClass", new Constraint[] { new IsEqual("com.test.invalid.class"), new IsAnything() }, new ClassNotFoundException());
        mockRendererComponentFactoryDescriptorControl.replay();

        SimpleRendererComponentFactory simpleRendererComponentFactory = new SimpleRendererComponentFactory()
        {
            RendererComponent loadRendererComponent(Class rendererComponentClass) throws ClassNotFoundException
            {
                throw new ClassNotFoundException();
            }
        };

        try
        {
            simpleRendererComponentFactory.init(mockRendererComponentFactoryDescriptor);
            fail("Expected exception");
        }
        catch (PluginParseException e)
        {
            assertTrue(e.getMessage().indexOf("Could not load renderer") >= 0);
            assertTrue(e.getMessage().indexOf("pluginkey") > 0);
            assertTrue(e.getMessage().indexOf("com.test.invalid.class") > 0);
        }
    }

    @Test
    public void testInitSuccessful() throws PluginParseException
    {
        MockControl mockRendererComponentFactoryDescriptorControl = MockClassControl.createControl(RendererComponentFactoryDescriptor.class);
        RendererComponentFactoryDescriptor mockRendererComponentFactoryDescriptor = (RendererComponentFactoryDescriptor) mockRendererComponentFactoryDescriptorControl.getMock();
        Mock mockPlugin = new Mock(Plugin.class);

        final Mock mockRendererComponent = new Mock(RendererComponent.class);
        final RendererComponent originalRendererComponent = (RendererComponent) mockRendererComponent.proxy();
        mockRendererComponentFactoryDescriptor.getPlugin();
        mockRendererComponentFactoryDescriptorControl.setReturnValue(null);
        Map params = new HashMap();
        params.put("rendererComponentClass", "java.lang.String");
        mockRendererComponentFactoryDescriptor.getParams();
        mockRendererComponentFactoryDescriptorControl.setReturnValue(params);
        mockRendererComponentFactoryDescriptor.getCompleteKey();
        mockRendererComponentFactoryDescriptorControl.setReturnValue("pluginkey");
        mockRendererComponentFactoryDescriptor.getPlugin();
        mockRendererComponentFactoryDescriptorControl.setReturnValue(mockPlugin.proxy());
        mockRendererComponentFactoryDescriptorControl.replay();

        SimpleRendererComponentFactory simpleRendererComponentFactory = new SimpleRendererComponentFactory()
        {
            RendererComponent loadRendererComponent(Class rendererComponentClass) throws ClassNotFoundException
            {
                return originalRendererComponent;
            }
        };

        simpleRendererComponentFactory.init(mockRendererComponentFactoryDescriptor);
        RendererComponent rendererComponent = simpleRendererComponentFactory.getRendererComponent();
        assertNotNull(rendererComponent);
        assertEquals(originalRendererComponent, rendererComponent);
    }

}
