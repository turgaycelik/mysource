package com.atlassian.jira.dev.reference.plugin.template;

import com.atlassian.plugin.elements.ResourceLocation;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.plugin.webresource.transformer.CharSequenceDownloadableResource;
import com.atlassian.plugin.webresource.transformer.WebResourceTransformer;
import org.dom4j.Element;

/**
 */
public class ReferenceWebResourceTransformer implements WebResourceTransformer
{
    /**
     * Transforms the downloadable resource by returning a new one.
     *
     * @param configElement The element where it was used.  This is provided to allow the transformer to
     * take additional configuration in the form of custom attributes or sub-elements.
     * @param location The original resource location
     * @param filePath Extra path information.  Cannot be null, but can be an empty string if no extra path information
     * @param nextResource The original resource
     * @return The new resource representing the transformed resource
     */
    public DownloadableResource transform(Element configElement, ResourceLocation location, String filePath, DownloadableResource nextResource)
    {
        return new CharSequenceDownloadableResource(nextResource)
        {
            protected String transform(CharSequence originalContent)
            {
                return originalContent.toString().replace("webresource", "transformed webresource");
            }
        };
    }

}