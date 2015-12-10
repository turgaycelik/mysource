package com.atlassian.jira.functest.config;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import java.util.Arrays;
import java.util.List;

/**
 * XML utils for configuration tests.
 *
 * @since v4.2
 */
public class XmlUtils
{
    private static final String OS_PROPERTY_ENTRY = "OSPropertyEntry";
    private static final String ROOT_ELEMENT = "entity-engine-xml";

    public static Document createInvalidDocument()
    {
        final DocumentFactory instance = DocumentFactory.getInstance();
        final Document rootDoc = instance.createDocument();
        rootDoc.setRootElement(instance.createElement(ROOT_ELEMENT));
        Element invalidProp = instance.createElement(OS_PROPERTY_ENTRY);
        invalidProp.setAttributes(cretePropAttributesList(instance, invalidProp));
        rootDoc.getRootElement().add(invalidProp);
        return rootDoc;
    }

    private static List<Attribute> cretePropAttributesList(DocumentFactory factory, Element parent)
    {
        return Arrays.asList(
                factory.createAttribute(parent, "id", "10005"),
                factory.createAttribute(parent, "entityName", "jira.properties"),
                factory.createAttribute(parent, "entityId", "1"),
                factory.createAttribute(parent, "propertyKey", "jira.path.index"),
                factory.createAttribute(parent, "type", "5")
        );
    }
}
