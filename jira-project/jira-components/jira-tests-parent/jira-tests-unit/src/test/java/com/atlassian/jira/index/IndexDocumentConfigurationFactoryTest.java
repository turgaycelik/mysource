package com.atlassian.jira.index;

import java.util.List;

import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.dom.DOMAttribute;
import org.dom4j.dom.DOMDocument;
import org.dom4j.dom.DOMElement;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.atlassian.jira.index.IndexDocumentConfiguration.ExtractConfiguration;
import static com.atlassian.jira.index.IndexDocumentConfiguration.KeyConfiguration;
import static com.atlassian.jira.index.IndexDocumentConfigurationFactory.IndexDocumentConfigurationParseException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @since v6.2
 */
public class IndexDocumentConfigurationFactoryTest
{
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private final IndexDocumentConfigurationFactory indexDocumentConfigurationFactory = new IndexDocumentConfigurationFactory();

    @Test
    public void shouldParseXmlWithMultipleXmlElement()
            throws IndexDocumentConfigurationParseException
    {
        final Element element = getDocument("issue");
        element.add(getKey("issue",
                getExtract("foo.bar1", IndexDocumentConfiguration.Type.STRING.name()),
                getExtract("foo.bar2", IndexDocumentConfiguration.Type.NUMBER.name()),
                getExtract("foo.bar3", IndexDocumentConfiguration.Type.DATE.name()),
                getExtract("foo.bar4", IndexDocumentConfiguration.Type.TEXT.name())
        ));
        element.add(getKey("issue",
                getExtract("foo.baz", IndexDocumentConfiguration.Type.STRING.name())
        ));

        final IndexDocumentConfiguration indexDocumentConfiguration = indexDocumentConfigurationFactory.fromXML(element);

        final List<KeyConfiguration> keyConfigurations = indexDocumentConfiguration.getKeyConfigurations();
        assertThat("issue", Matchers.equalTo(indexDocumentConfiguration.getEntityKey()));
        //noinspection unchecked
        assertThat(keyConfigurations, Matchers.<KeyConfiguration>contains(
                matcherForConfiguration("issue"),
                matcherForConfiguration("issue")
        ));

        //noinspection unchecked
        assertThat(keyConfigurations.get(0).getExtractorConfigurations(), Matchers.<ExtractConfiguration>contains(
                matcherForExtract("foo.bar1", IndexDocumentConfiguration.Type.STRING),
                matcherForExtract("foo.bar2", IndexDocumentConfiguration.Type.NUMBER),
                matcherForExtract("foo.bar3", IndexDocumentConfiguration.Type.DATE),
                matcherForExtract("foo.bar4", IndexDocumentConfiguration.Type.TEXT)
        ));
        assertThat(keyConfigurations.get(1).getExtractorConfigurations(), Matchers.contains(
                matcherForExtract("foo.baz", IndexDocumentConfiguration.Type.STRING
                )));
    }

    @Test
    public void shouldThrowExceptionWhenInvalidRoot()
            throws IndexDocumentConfigurationParseException
    {

        final Element element = new DOMElement("test");
        expectedException.expect(IndexDocumentConfigurationParseException.class);
        expectedException.expectMessage("Root element for configuration should be index-document-configuration");

        indexDocumentConfigurationFactory.fromXML(element);

    }

    @Test
    public void shouldThrowExceptionWhenNoKeys()
            throws IndexDocumentConfigurationParseException
    {

        final Element element = getDocument("issue");
        expectedException.expect(IndexDocumentConfigurationParseException.class);
        expectedException.expectMessage("Expected at least one");

        indexDocumentConfigurationFactory.fromXML(element);

    }

    @Test
    public void shouldThrowExceptionWhenNoExtractDeffined()
            throws IndexDocumentConfigurationParseException
    {

        final Element element = getDocument("issue");
        element.add(getKey("issue"));
        expectedException.expect(IndexDocumentConfigurationParseException.class);
        expectedException.expectMessage("Expected at least one");

        indexDocumentConfigurationFactory.fromXML(element);

    }

    @Test
    public void shouldThrowExceptionWhenInvalidTypeAttrbute()
            throws IndexDocumentConfigurationParseException
    {

        final Element element = getDocument("issue");
        element.add(getKey("issue",
                getExtract("aaa", "thingy")));
        expectedException.expect(IndexDocumentConfigurationParseException.class);
        expectedException.expectMessage("Illegal value thingy for type argument expected one of");

        indexDocumentConfigurationFactory.fromXML(element);

    }

    @Test
    public void shouldConvertToAndFromXmlWithoutLoosingInformation()
            throws IndexDocumentConfigurationParseException
    {
        final DOMElement element = getDocument("issue");
        element.add(getKey("issue",
                getExtract("foo.bar1", IndexDocumentConfiguration.Type.STRING.name())));

        final IndexDocumentConfiguration indexDocumentConfiguration = indexDocumentConfigurationFactory.fromXML(element);

        final String xmlRepresentationIn = new DOMDocument(element).asXML();
        final String xmlRepresentationOut = indexDocumentConfigurationFactory.toXML(indexDocumentConfiguration);
        assertThat(xmlRepresentationOut, Matchers.containsString("index-document-configuration"));
        assertEquals(xmlRepresentationIn, xmlRepresentationOut);
    }

    @Test
    public void shoudConvertToAndFromXmlWithoutLoosingInformationFromString()
            throws IndexDocumentConfigurationParseException
    {
        final DOMElement element = getDocument("issue");
        element.add(getKey("issue",
                getExtract("foo.bar1", IndexDocumentConfiguration.Type.STRING.name()),
                getExtract("foo.bar2", IndexDocumentConfiguration.Type.TEXT.name())));
        element.add(getKey("issue",
                getExtract("foo.bar21", IndexDocumentConfiguration.Type.STRING.name()),
                getExtract("foo.bar22", IndexDocumentConfiguration.Type.TEXT.name())));


        final String xmlRepresentationIn = new DOMDocument(element).asXML();

        final IndexDocumentConfiguration indexDocumentConfiguration = indexDocumentConfigurationFactory.fromXML(xmlRepresentationIn);
        final String xmlRepresentationOut = indexDocumentConfigurationFactory.toXML(indexDocumentConfiguration);

        assertThat(xmlRepresentationOut, Matchers.containsString("index-document-configuration"));
        assertEquals(xmlRepresentationIn, xmlRepresentationOut);
    }

    private Matcher<ExtractConfiguration> matcherForExtract(final String path, final IndexDocumentConfiguration.Type type)
    {
        return new BaseMatcher<ExtractConfiguration>()
        {
            @Override
            public boolean matches(final Object item)
            {
                final ExtractConfiguration extract = (ExtractConfiguration) item;
                return path.equals(extract.getPath()) && type == extract.getType();
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("ExtractConfiguration.path=" + path + " ExtractConfiguration.type=" + type);

            }
        };
    }

    private Matcher<KeyConfiguration> matcherForConfiguration(final String propertyKey)
    {
        return new BaseMatcher<KeyConfiguration>()
        {
            @Override
            public boolean matches(final Object item)
            {
                final KeyConfiguration keyConfiguration = (KeyConfiguration) item;
                return propertyKey.equals(keyConfiguration.getPropertyKey());
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("KeyConfiguration.propertyKey=" + propertyKey);
            }
        };
    }

    private Element getKey(final String propertyKey, final Element... extracts)
    {
        final Element key = new DOMElement("key");
        key.add(new DOMAttribute(new QName("property-key"), propertyKey));
        for (final Element extract : extracts)
        {
            key.add(extract);
        }
        return key;
    }

    private Element getExtract(final String path, final String type)
    {
        final Element extract = new DOMElement("extract");
        extract.add(new DOMAttribute(new QName("path"), path));
        extract.add(new DOMAttribute(new QName("type"), type));
        return extract;
    }

    private DOMElement getDocument(final String entityKey)
    {
        final DOMElement element = new DOMElement("index-document-configuration");
        element.add(new DOMAttribute(new QName("entity-key"), entityKey));
        return element;
    }
}
