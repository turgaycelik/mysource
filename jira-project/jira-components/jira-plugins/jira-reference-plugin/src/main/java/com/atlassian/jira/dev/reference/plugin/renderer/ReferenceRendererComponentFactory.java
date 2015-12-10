package com.atlassian.jira.dev.reference.plugin.renderer;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.renderer.v2.components.PluggableRendererComponentFactory;
import com.atlassian.renderer.v2.components.RendererComponent;
import com.atlassian.renderer.v2.components.phrase.PhraseRendererComponent;

/**
 * Reference renderer component factory. Provides simple {@link com.atlassian.renderer.v2.components.phrase.PhraseRendererComponent}
 * that makes the phrase enclosed by '=' red.
 *
 * @since v4.4
 */
public class ReferenceRendererComponentFactory implements PluggableRendererComponentFactory
{
    private RendererComponent component;

    public void init(ModuleDescriptor moduleDescriptor) throws PluginParseException
    {
        component = new PhraseRendererComponent("=", "=", "<span style='color:red;'>", "</span>");
    }

    public RendererComponent getRendererComponent()
    {
        return component;
    }
}
