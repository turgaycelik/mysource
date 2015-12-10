package com.atlassian.jira.dev.reference.plugin.renderer;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.renderer.v2.components.PluggableRendererComponentFactory;
import com.atlassian.renderer.v2.components.RendererComponent;
import com.atlassian.renderer.v2.components.phrase.PhraseRendererComponent;

/**
 * Upgraded version of reference renderer component factory.
 * Provides simple {@link com.atlassian.renderer.v2.components.phrase.PhraseRendererComponent}
 * that makes the phrase enclosed by '=' green.
 *
 * @since v4.4
 */
public class ReferenceRendererComponentFactory implements PluggableRendererComponentFactory
{
    private RendererComponent component;

    public void init(ModuleDescriptor moduleDescriptor) throws PluginParseException
    {
        component = new PhraseRendererComponent("=", "=", "<span title='!UPGRADED!' style='color:green;'>", "</span>");
    }

    public RendererComponent getRendererComponent()
    {
        return component;
    }
}
