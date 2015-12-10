package com.atlassian.jira.mock.plugin;

import com.atlassian.plugin.Resourced;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.elements.ResourceLocation;
import com.google.common.collect.Lists;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import java.util.List;

/**
 * @since v6.2.3
 */
class MockResources implements Resourced
{
    private final List<ResourceDescriptor> descriptors = Lists.newArrayList();

    ResourceDescriptor createI18nResource(String name, String location)
    {
        return createResource("i18n", name, location);
    }

    ResourceDescriptor createHelpResource(String name, String location)
    {
        return createResource("helpPaths", name, location);
    }

    ResourceDescriptor createResource(String type, String name, String location)
    {
        final Element resource = DocumentFactory.getInstance().createElement("resource");
        resource.addAttribute("type", type);
        resource.addAttribute("name", name);
        resource.addAttribute("location", location);

        final ResourceDescriptor resourceDescriptor = new ResourceDescriptor(resource);
        descriptors.add(resourceDescriptor);
        return resourceDescriptor;
    }

    List<ResourceDescriptor> getAll()
    {
        return descriptors;
    }

    List<ResourceDescriptor> getAllByType(final String type)
    {
        final List<ResourceDescriptor> result = Lists.newArrayList();
        for (ResourceDescriptor resource : descriptors)
        {
            if (resource.getType().equals(type))
            {
                result.add(resource);
            }
        }
        return result;
    }

    @Override
    public List<ResourceDescriptor> getResourceDescriptors()
    {
        return getAll();
    }

    @Override
    public List<ResourceDescriptor> getResourceDescriptors(final String type)
    {
        return getAllByType(type);
    }

    @Override
    public ResourceDescriptor getResourceDescriptor(final String s, final String s2)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ResourceLocation getResourceLocation(final String s, final String s2)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    void add(final ResourceDescriptor resourceDescriptor)
    {
        descriptors.add(resourceDescriptor);
    }
}
