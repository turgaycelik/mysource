package com.atlassian.jira.dev.functest.renderer;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.components.PluggableRendererComponentFactory;
import com.atlassian.renderer.v2.components.RendererComponent;

/**
 * Renderer component factory that uses RenderContext.addRenderedContent
 *
 * @since v4.4
 */
public class FuncTestRendererComponentFactory implements PluggableRendererComponentFactory
{
    private RendererComponent component;

    public void init(ModuleDescriptor moduleDescriptor) throws PluginParseException
    {
        component = new RendererComponent()
        {
            public boolean shouldRender(RenderMode renderMode)
            {
                return true;
            }
            
            public String render(String s, RenderContext renderContext)
            {
                String s1 = s;
                // Replace "Yo stop" with a block replacement
                if (s1.contains("Yo stop"))
                {
                    String token = renderContext.getRenderedContentStore().addBlock("<h1>Yo stop</h1><h2>Collaborate and listen</h2>");
                    s1 = s1.replaceAll("Yo stop", token);
                }
                // Replace "Ice is back" with an inline replacement
                if (s1.contains("Ice is back"))
                {
                    String token = renderContext.getRenderedContentStore().addInline("Ice is back with a brand new invention");
                    s1 = s1.replaceAll("Ice is back", token);
                }
                return s1;
            }
        };
    }

    public RendererComponent getRendererComponent()
    {
        return component;
    }
}
