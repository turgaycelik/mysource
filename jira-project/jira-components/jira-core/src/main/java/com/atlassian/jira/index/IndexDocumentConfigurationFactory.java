package com.atlassian.jira.index;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import com.atlassian.fugue.Either;
import com.atlassian.fugue.Option;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.dom.DOMAttribute;
import org.dom4j.dom.DOMDocument;
import org.dom4j.dom.DOMElement;

import static com.atlassian.jira.index.IndexDocumentConfiguration.ExtractConfiguration;
import static com.atlassian.jira.index.IndexDocumentConfiguration.KeyConfiguration;
import static com.atlassian.jira.index.IndexDocumentConfiguration.Type;

/**
 * Factory for {@link com.atlassian.jira.index.IndexDocumentConfiguration}.
 *
 * @since v6.2
 */
public class IndexDocumentConfigurationFactory
{
    public static final String INDEX_DOCUMENT_CONFIGURATION_ROOT = "index-document-configuration";
    public static final String INDEX_DOCUMENT_KEY_ELEMENT = "key";
    public static final String INDEX_DOCUMENT_ENTITY_KEY_ATTRIBUTE = "entity-key";
    public static final String INDEX_DOCUMENT_KEY_PROPERTY_KEY_ATTRIBUTE = "property-key";
    public static final String INDEX_DOCUMENT_EXTRACT_ELEMENT = "extract";
    public static final String INDEX_DOCUMENT_EXTRACT_PATH_ATTRIBUTE = "path";
    public static final String INDEX_DOCUMENT_EXTRACT_TYPE_ATTRIBUTE = "type";

    public IndexDocumentConfiguration fromXML(final String element) throws IndexDocumentConfigurationParseException
    {
        try
        {
            return fromXML(DocumentHelper.parseText(element).getRootElement());
        }
        catch (final DocumentException e)
        {
            throw new IndexDocumentConfigurationParseException("Cannot parse document ", e);
        }
    }

    public IndexDocumentConfiguration fromXML(Element element) throws IndexDocumentConfigurationParseException
    {
        if (!INDEX_DOCUMENT_CONFIGURATION_ROOT.equals(element.getName()))
        {
            throw new IndexDocumentConfigurationParseException(MessageFormat.format("Root element for configuration should be {0}", INDEX_DOCUMENT_CONFIGURATION_ROOT));
        }
        final String entityKey = getRequiredAttribute(element, INDEX_DOCUMENT_CONFIGURATION_ROOT, INDEX_DOCUMENT_ENTITY_KEY_ATTRIBUTE);
        return new IndexDocumentConfiguration(entityKey, transformRequiredElements(element, INDEX_DOCUMENT_KEY_ELEMENT, new Function<Element, Either<KeyConfiguration, IndexDocumentConfigurationParseException>>()
        {
            @Override
            public Either<KeyConfiguration, IndexDocumentConfigurationParseException> apply(final Element input)
            {
                return keyConfigurationFromXml(input);
            }
        }));
    }

    public String toXML(final IndexDocumentConfiguration indexDocumentConfiguration)
    {
        final DOMDocument document = new DOMDocument();
        final Element configurationElement = new DOMElement(INDEX_DOCUMENT_CONFIGURATION_ROOT);
        configurationElement.add(new DOMAttribute(QName.get(INDEX_DOCUMENT_ENTITY_KEY_ATTRIBUTE), indexDocumentConfiguration.getEntityKey()));
        document.setRootElement(configurationElement);
        for (final KeyConfiguration keyConfiguration : indexDocumentConfiguration.getKeyConfigurations())
        {
            final Element keyConfigurationElement = new DOMElement(INDEX_DOCUMENT_KEY_ELEMENT);
            keyConfigurationElement.add(new DOMAttribute(QName.get(INDEX_DOCUMENT_KEY_PROPERTY_KEY_ATTRIBUTE), keyConfiguration.getPropertyKey()));

            for (final ExtractConfiguration extractConfiguration : keyConfiguration.getExtractorConfigurations())
            {
                final Element extractConfigurationElement = new DOMElement(INDEX_DOCUMENT_EXTRACT_ELEMENT);
                extractConfigurationElement.add(new DOMAttribute(QName.get(INDEX_DOCUMENT_EXTRACT_PATH_ATTRIBUTE), extractConfiguration.getPath()));
                extractConfigurationElement.add(new DOMAttribute(QName.get(INDEX_DOCUMENT_EXTRACT_TYPE_ATTRIBUTE), extractConfiguration.getType().name()));
                keyConfigurationElement.add(extractConfigurationElement);
            }
            configurationElement.add(keyConfigurationElement);
        }
        return document.asXML();
    }

    private static Either<KeyConfiguration, IndexDocumentConfigurationParseException> keyConfigurationFromXml(final Element keyElement)
    {
        assert INDEX_DOCUMENT_KEY_ELEMENT.equals(keyElement.getName()) : "Unexpected name for element " + keyElement.getName();
        try
        {
            final String propertyKey = getRequiredAttribute(keyElement, INDEX_DOCUMENT_KEY_ELEMENT, INDEX_DOCUMENT_KEY_PROPERTY_KEY_ATTRIBUTE);
            return Either.left(new KeyConfiguration(propertyKey, transformRequiredElements(keyElement, INDEX_DOCUMENT_EXTRACT_ELEMENT, new Function<Element, Either<ExtractConfiguration, IndexDocumentConfigurationParseException>>()
            {
                @Override
                public Either<ExtractConfiguration, IndexDocumentConfigurationParseException> apply(final Element input)
                {
                    return extractConfigurationFromXML(input);
                }
            })));
        }
        catch (final IndexDocumentConfigurationParseException e)
        {
            return Either.right(e);
        }
    }

    private static Either<ExtractConfiguration, IndexDocumentConfigurationParseException> extractConfigurationFromXML(final Element extractElement)
    {
        assert INDEX_DOCUMENT_EXTRACT_ELEMENT.equals(extractElement.getName()) : "Unexpected name for element " + extractElement.getName();
        try
        {
            final String path = getRequiredAttribute(extractElement, INDEX_DOCUMENT_EXTRACT_ELEMENT, INDEX_DOCUMENT_EXTRACT_PATH_ATTRIBUTE);
            final String typeStr = getRequiredAttribute(extractElement, INDEX_DOCUMENT_EXTRACT_ELEMENT, INDEX_DOCUMENT_EXTRACT_TYPE_ATTRIBUTE);

            final Option<Type> type = Type.getValue(typeStr);
            if (type.isEmpty())
            {
                return Either.right(new IndexDocumentConfigurationParseException(MessageFormat.format("Illegal value {0} for type argument expected one of {1}", typeStr, Arrays.toString(Type.values()))));
            }

            return Either.left(new ExtractConfiguration(path, type.get()));
        }
        catch (final IndexDocumentConfigurationParseException e)
        {
            return Either.right(e);
        }
    }

    private static String getRequiredAttribute(final Element element, final String elementName, final String attributeName)
            throws IndexDocumentConfigurationParseException
    {
        final Attribute entityKeyAttribute = element.attribute(attributeName);
        if (entityKeyAttribute == null)
        {
            throw new IndexDocumentConfigurationParseException(MessageFormat.format("Element {0} must have attribute {1} deffined", elementName, attributeName));
        }
        return entityKeyAttribute.getValue();
    }

    private static <T> List<T> transformRequiredElements(Element element, String elementName, final Function<Element, Either<T, IndexDocumentConfigurationParseException>> function)
            throws IndexDocumentConfigurationParseException

    {
        @SuppressWarnings ("unchecked") final List<Element> childElements = element.elements(elementName);
        if (childElements.size() < 1)
        {
            throw new IndexDocumentConfigurationParseException(MessageFormat.format("Expected at least one {0} element", elementName));
        }
        final ImmutableList.Builder<T> listBuilder = ImmutableList.builder();
        for (final Element childElement : childElements)
        {
            final Either<T, IndexDocumentConfigurationParseException> output = function.apply(childElement);
            if (output.isLeft())
            {
                listBuilder.add(output.left().get());
            }
            else
            {
                throw output.right().get();
            }
        }
        return listBuilder.build();
    }

    public static class IndexDocumentConfigurationParseException extends Exception
    {
        private IndexDocumentConfigurationParseException(final String message)
        {
            super(message);
        }

        public IndexDocumentConfigurationParseException(final String message, final Throwable cause)
        {
            super(message, cause);
        }
    }
}
