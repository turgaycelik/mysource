package com.atlassian.jira.mock.plugin.elements;

import java.util.Collections;

import com.atlassian.plugin.elements.ResourceDescriptor;

import org.dom4j.Element;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/**
 * Builder for mock instances of {@link com.atlassian.plugin.elements.ResourceDescriptor}.
 *
 * @since v4.3
 */
public final class MockResourceDescriptorBuilder
{
    public static ResourceDescriptor i18n(String name, String location)
    {
        return new MockResourceDescriptorBuilder().type("i18n").name(name).location(location).build();
    }

    public static ResourceDescriptor velocity(String name, String location)
    {
        return new MockResourceDescriptorBuilder().type("velocity").name(name).location(location).build();
    }

    public static ResourceDescriptor feature(String name, String location)
    {
        return new MockResourceDescriptorBuilder().type("feature").name(name).location(location).build();
    }

    private String type;
    private String name;
    private String location;

    public MockResourceDescriptorBuilder type(String type)
    {
        this.type = type;
        return this;
    }

    public MockResourceDescriptorBuilder name(String name)
    {
        this.name = name;
        return this;
    }

    public MockResourceDescriptorBuilder location(String location)
    {
        this.location = location;
        return this;
    }

    public ResourceDescriptor build()
    {
        return new ResourceDescriptor(buildElement());
    }

    private Element buildElement()
    {
        Element element = createNiceMock(Element.class);
        expect(element.attributeValue("type")).andReturn(type).anyTimes();
        expect(element.attributeValue("name")).andReturn(name).anyTimes();
        expect(element.attributeValue("location")).andReturn(location).anyTimes();
        expect(element.elements("param")).andReturn(Collections.emptyList()).anyTimes();
        replay(element);
        return element;
    }
}
