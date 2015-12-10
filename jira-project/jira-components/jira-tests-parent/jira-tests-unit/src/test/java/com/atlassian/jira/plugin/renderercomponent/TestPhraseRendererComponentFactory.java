package com.atlassian.jira.plugin.renderercomponent;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.renderer.v2.components.phrase.PhraseRendererComponent;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/** @since v3.12 */
public class TestPhraseRendererComponentFactory
{
    @Test
    public void testInitMissingParam()
    {
        MockControl mockRendererComponentFactoryDescriptorControl = MockClassControl
                .createControl(RendererComponentFactoryDescriptor.class);
        RendererComponentFactoryDescriptor mockRendererComponentFactoryDescriptor = (RendererComponentFactoryDescriptor) mockRendererComponentFactoryDescriptorControl
                .getMock();

        mockRendererComponentFactoryDescriptor.getParams();
        mockRendererComponentFactoryDescriptorControl.setReturnValue(new HashMap());
        mockRendererComponentFactoryDescriptor.getCompleteKey();
        mockRendererComponentFactoryDescriptorControl.setReturnValue("pluginkey");
        mockRendererComponentFactoryDescriptorControl.replay();

        PhraseRendererComponentFactory phraseRendererComponentFactory = new PhraseRendererComponentFactory();
        try
        {
            phraseRendererComponentFactory.init(mockRendererComponentFactoryDescriptor);
            fail("Expected exception");
        }
        catch (PluginParseException e)
        {
            assertTrue(e.getMessage().indexOf("missing the required 'phrase' parameter") > 0);
            assertTrue(e.getMessage().indexOf("pluginkey") > 0);
        }
    }

    @Test
    public void testInitInvalidPhrase() throws PluginParseException
    {
        MockControl mockRendererComponentFactoryDescriptorControl = MockClassControl
                .createControl(RendererComponentFactoryDescriptor.class);
        RendererComponentFactoryDescriptor mockRendererComponentFactoryDescriptor = (RendererComponentFactoryDescriptor) mockRendererComponentFactoryDescriptorControl
                .getMock();
        Map params = new HashMap();
        params.put("phrase", "somephrase");

        mockRendererComponentFactoryDescriptor.getParams();
        mockRendererComponentFactoryDescriptorControl.setReturnValue(params);
        mockRendererComponentFactoryDescriptor.getCompleteKey();
        mockRendererComponentFactoryDescriptorControl.setReturnValue("pluginkey");
        mockRendererComponentFactoryDescriptorControl.replay();

        PhraseRendererComponentFactory phraseRendererComponentFactory = new PhraseRendererComponentFactory();

        phraseRendererComponentFactory.init(mockRendererComponentFactoryDescriptor);

        assertNull(phraseRendererComponentFactory.getRendererComponent());
    }

    @Test
    public void testInitSuccessful() throws PluginParseException
    {
        MockControl mockRendererComponentFactoryDescriptorControl = MockClassControl
                .createControl(RendererComponentFactoryDescriptor.class);
        RendererComponentFactoryDescriptor mockRendererComponentFactoryDescriptor = (RendererComponentFactoryDescriptor) mockRendererComponentFactoryDescriptorControl
                .getMock();
        Map params = new HashMap();
        params.put("phrase", "strong");

        mockRendererComponentFactoryDescriptor.getParams();
        mockRendererComponentFactoryDescriptorControl.setReturnValue(params);
        mockRendererComponentFactoryDescriptor.getCompleteKey();
        mockRendererComponentFactoryDescriptorControl.setReturnValue("pluginkey");
        mockRendererComponentFactoryDescriptorControl.replay();

        PhraseRendererComponentFactory phraseRendererComponentFactory = new PhraseRendererComponentFactory();

        phraseRendererComponentFactory.init(mockRendererComponentFactoryDescriptor);

        final PhraseRendererComponent rendererComponent =
                (PhraseRendererComponent) phraseRendererComponentFactory.getRendererComponent();
        assertNotNull(rendererComponent);
    }

}
