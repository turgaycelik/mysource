package com.atlassian.jira.plugin.renderercomponent;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.renderer.v2.components.PluggableRendererComponentFactory;
import com.atlassian.renderer.v2.components.RendererComponent;
import com.atlassian.renderer.v2.components.phrase.PhraseRendererComponent;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Simple component factory that loads a renderer component specified as a parameter in the plugin descriptor.
 *
 * @since v3.12
 */
public class PhraseRendererComponentFactory implements PluggableRendererComponentFactory
{
    private static final String PHRASE_PARAM = "phrase";
    private RendererComponent rendererComponent;

    public void init(ModuleDescriptor moduleDescriptor) throws PluginParseException
    {
        final RendererComponentFactoryDescriptor descriptor = (RendererComponentFactoryDescriptor) moduleDescriptor;
        final Map params = descriptor.getParams();
        if (params == null || StringUtils.isEmpty((String) params.get(PHRASE_PARAM)))
        {
            throw new PluginParseException("The plugin with key '" + descriptor.getCompleteKey() + "' is missing the required '"
                                       + PHRASE_PARAM + "' parameter.");
        }
        else
        {
            String phrase = (String) params.get(PHRASE_PARAM);
            rendererComponent = PhraseRendererComponent.getDefaultRenderer(phrase);
        }
    }

    public RendererComponent getRendererComponent()
    {
        return rendererComponent;
    }
}
