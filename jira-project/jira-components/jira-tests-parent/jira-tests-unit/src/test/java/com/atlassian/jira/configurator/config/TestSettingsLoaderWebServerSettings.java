package com.atlassian.jira.configurator.config;

import java.io.ByteArrayInputStream;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.atlassian.security.xml.SecureXmlParserFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestSettingsLoaderWebServerSettings
{
    private static final String CONTROL_PORT = "8090";
    private static final String HTTP_PORT = "8080";

    private static final String HTTPS_PORT = "8443";
    private static final String HTTPS_SCHEME = "https";
    private static final String KEY_STORE_FILE_NAME = "/some/file";
    private static final String KEY_STORE_PASSWORD = "secret";
    private static final String KEY_STORE_TYPE = "JKS";
    private static final String KEY_ALIAS = "alias";

    private static final String ALTERNATE_CONTROL_PORT = "18090";
    private static final String ALTERNATE_HTTP_PORT = "18080";

    private static final String ALTERNATE_HTTPS_PORT = "18443";

    private static final String DEFAULT_PROTOCOL = "HTTP/1.1";
    private static final String APR_PROTOCOL = "org.apache.coyote.http11.Http11AprProtocol";
    private static final String SECURITY_CONSTRAINT_NAME = SettingsLoader.SECURITY_CONSTRAINT_NAME;

    private final Settings settings = new Settings();

    @Test
    public void testLoadHttpConnector() throws Exception
    {
        final Document doc = createDocument(
                "<Server port=\"" + CONTROL_PORT + "\">" +
                "   <Service>" +
                "       <Connector port=\"" + HTTP_PORT + "\" protocol=\"" + DEFAULT_PROTOCOL + "\" />" +
                "   </Service>" +
                "</Server>"
        );
        SettingsLoader.loadWebServerSettings(doc, settings);

        assertEquals(HTTP_PORT, settings.getHttpPort());
    }

    @Test
    public void testLoadAprEnabledHttpConnector() throws Exception
    {
        final Document doc = createDocument(
                "<Server port=\"" + CONTROL_PORT + "\">" +
                "   <Service>" +
                "       <Connector port=\"" + HTTP_PORT + "\" protocol=\"" + APR_PROTOCOL + "\" />" +
                "   </Service>" +
                "</Server>"
        );
        SettingsLoader.loadWebServerSettings(doc, settings);

        assertTrue(settings.getComplexConfigurationReasons().isEmpty());
        assertEquals(HTTP_PORT, settings.getHttpPort());
    }

    @Test
    public void testMultipleHttpConnectorsExistWarning() throws Exception
    {
        final Document doc = createDocumentWithMultipleHttpConnectors();
        SettingsLoader.loadWebServerSettings(doc, settings);

        assertThat(settings.getComplexConfigurationReasons(), hasItem(ComplexConfigurationReason.MultipleHttpConnectors));
        assertNull(settings.getHttpPort());
    }

    @Test
    public void testLoadHttpsConnector() throws Exception
    {
        final Document doc = createDocument(
                "<Server port=\"" + CONTROL_PORT + "\">" +
                        "   <Service>" +
                        "       <Connector port=\"" + HTTPS_PORT + "\" scheme=\"" + HTTPS_SCHEME + "\" secure=\"true\" keystoreFile=\"" + KEY_STORE_FILE_NAME + "\" keystorePass=\"" + KEY_STORE_PASSWORD + "\" keyAlias=\"" + KEY_ALIAS + "\" />" +
                        "   </Service>" +
                        "</Server>"
        );
        SettingsLoader.loadWebServerSettings(doc, settings);

        final SslSettings sslSettings = settings.getSslSettings();
        assertNotNull(sslSettings);
        assertEquals(HTTPS_PORT, sslSettings.getHttpsPort());
        assertEquals(KEY_STORE_FILE_NAME, sslSettings.getKeystoreFile());
        assertEquals(KEY_STORE_PASSWORD, sslSettings.getKeystorePass());
        assertEquals(KEY_ALIAS, sslSettings.getKeyAlias());
    }

    @Test
    public void testMultipleHttpsConnectorsExistWarning() throws Exception
    {
        final Document doc = createDocumentWithMultipleHttpsConnectors();
        SettingsLoader.loadWebServerSettings(doc, settings);

        assertThat(settings.getComplexConfigurationReasons(), hasItem(ComplexConfigurationReason.MultipleHttpsConnectors));
        assertNull(settings.getSslSettings());
    }

    @Test
    public void testAprEnabledForSslConnectorIsUnsupported() throws Exception
    {
        final Document doc = createDocumentWithAprEnabledHttpsConnector();
        SettingsLoader.loadWebServerSettings(doc, settings);

        assertThat(settings.getComplexConfigurationReasons(), hasItem(ComplexConfigurationReason.SslEnabledAprConnector));
        assertNull(settings.getSslSettings());
    }

    @Test
    public void testPreserveAdditionalAttributes() throws Exception
    {
        final Document doc = createDocument(
                "<Server port=\"" + CONTROL_PORT + "\">" +
                "   <Service>" +
                "       <Connector port=\"" + HTTP_PORT + "\" protocol=\"" + DEFAULT_PROTOCOL + "\" foo=\"bar\"/>" +
                "   </Service>" +
                "</Server>"
        );
        SettingsLoader.loadWebServerSettings(doc, settings);
        SettingsLoader.saveNetworkingSettingsToServerXml(settings, doc);

        assertEquals("bar", xpath(doc, "/Server/Service/Connector/@foo"));
    }

    @Test
    public void testPreserveAdditionalElements() throws Exception
    {
        final Document doc = createDocument(
                "<Server port=\"" + CONTROL_PORT + "\">" +
                "   <Service>" +
                "       <foo />" +
                "       <Connector port=\"" + HTTP_PORT + "\" protocol=\"" + DEFAULT_PROTOCOL + "\"/>" +
                "   </Service>" +
                "   <bar>text</bar>" +
                "</Server>"
        );
        SettingsLoader.loadWebServerSettings(doc, settings);
        SettingsLoader.saveNetworkingSettingsToServerXml(settings, doc);

        assertEquals("1", xpath(doc, "count(/Server/Service/foo)"));
        assertEquals("text", xpath(doc, "/Server/bar/text()"));
    }

    @Test
    public void testPreserveComments() throws Exception
    {
        final Document doc = createDocument(
                "<Server port=\"" + CONTROL_PORT + "\">" +
                "   <Service>" +
                "       <!-- a comment -->" +
                "       <Connector port=\"" + HTTP_PORT + "\" protocol=\"" + DEFAULT_PROTOCOL + "\"/>" +
                "   </Service>" +
                "</Server>"
        );
        SettingsLoader.loadWebServerSettings(doc, settings);
        SettingsLoader.saveNetworkingSettingsToServerXml(settings, doc);

        assertHasComment(doc);
    }

    @Test
    public void testLoadMinimalHttpOnlyConfiguration() throws Exception
    {
        final Document doc = createDocument(
                "<Server port=\"" + CONTROL_PORT + "\">" +
                "   <Service>" +
                "       <Connector port=\"" + HTTP_PORT + "\" protocol=\"" + DEFAULT_PROTOCOL + "\"/>" +
                "   </Service>" +
                "</Server>"
        );
        SettingsLoader.loadWebServerSettings(doc, settings);
        assertEquals(CONTROL_PORT, settings.getControlPort());
        assertEquals(HTTP_PORT, settings.getHttpPort());
        assertNull(settings.getSslSettings());
    }

    @Test
    public void testLoadMinimalHttpAndHttpsConfiguration() throws Exception
    {
        final Document doc = createDocument(
                "<Server port=\"" + CONTROL_PORT + "\">" +
                "   <Service>" +
                "       <Connector port=\"" + HTTP_PORT + "\" protocol=\"" + DEFAULT_PROTOCOL + "\"/>" +
                "       <Connector port=\"" + HTTPS_PORT + "\" scheme=\"" + HTTPS_SCHEME + "\" secure=\"true\" keystoreFile=\"" + KEY_STORE_FILE_NAME + "\" keystorePass=\"" + KEY_STORE_PASSWORD + "\" keyAlias=\"" + KEY_ALIAS + "\" />" +
                "   </Service>" +
                "</Server>"
        );
        SettingsLoader.loadWebServerSettings(doc, settings);
        assertEquals(CONTROL_PORT, settings.getControlPort());
        assertEquals(HTTP_PORT, settings.getHttpPort());

        final SslSettings sslSettings = settings.getSslSettings();
        assertNotNull(sslSettings);
        assertEquals(HTTPS_PORT, sslSettings.getHttpsPort());
        assertEquals(KEY_STORE_FILE_NAME, sslSettings.getKeystoreFile());
        assertEquals(KEY_STORE_PASSWORD, sslSettings.getKeystorePass());
        assertEquals(KEY_STORE_TYPE, sslSettings.getKeystoreType());
        assertEquals(KEY_ALIAS, sslSettings.getKeyAlias());
    }

    @Test
    public void testLoadMinimalHttpsOnlyConfiguration() throws Exception
    {
        final Document doc = createDocument(
                "<Server port=\"" + CONTROL_PORT + "\">" +
                "   <Service>" +
                "       <Connector port=\"" + HTTPS_PORT  +"\" scheme=\"" + HTTPS_SCHEME + "\" secure=\"true\" keystoreFile=\"" + KEY_STORE_FILE_NAME + "\" keystorePass=\"" + KEY_STORE_PASSWORD + "\" keyAlias=\"" + KEY_ALIAS + "\" />" +
                "   </Service>" +
                "</Server>"
        );
        SettingsLoader.loadWebServerSettings(doc, settings);
        assertEquals(CONTROL_PORT, settings.getControlPort());
        assertEquals(null, settings.getHttpPort());

        final SslSettings sslSettings = settings.getSslSettings();
        assertNotNull(sslSettings);
        assertEquals(HTTPS_PORT, sslSettings.getHttpsPort());
        assertEquals(KEY_STORE_FILE_NAME, sslSettings.getKeystoreFile());
        assertEquals(KEY_STORE_PASSWORD, sslSettings.getKeystorePass());
        assertEquals(KEY_STORE_TYPE, sslSettings.getKeystoreType());
        assertEquals(KEY_ALIAS, sslSettings.getKeyAlias());
    }

    @Test
    public void testNoUpdateWhenMultipleHttpConnectorsExist() throws Exception
    {
        final Document doc = createDocumentWithMultipleHttpConnectors();

        settings.setHttpPort("1");

        SettingsLoader.saveNetworkingSettingsToServerXml(settings, doc);
        final NodeList nodeList = selectAllConnectorNodes(doc);
        assertEquals(2, nodeList.getLength());
        assertEquals(HTTP_PORT, xpath(nodeList.item(0), "@port"));
        assertEquals(ALTERNATE_HTTP_PORT, xpath(nodeList.item(1), "@port"));
    }

    @Test
    public void testUpdateMinimalHttpOnlyConfiguration() throws Exception
    {
        settings.setControlPort(ALTERNATE_CONTROL_PORT);
        settings.setHttpPort(ALTERNATE_HTTP_PORT);

        final Document doc = createDocumentFromSettings(settings);

        settings.setControlPort(CONTROL_PORT);
        settings.setHttpPort(HTTP_PORT);

        SettingsLoader.saveNetworkingSettingsToServerXml(settings, doc);
        assertEquals(1, numberOfConnectors(doc));
        assertDefaultControlPort(doc);
        assertExistingHttpConnector(doc, DEFAULT_PROTOCOL);
    }

    @Test
    public void testUpdateAprEnabledHttpConnector() throws Exception
    {
        settings.setControlPort(ALTERNATE_CONTROL_PORT);
        settings.setHttpPort(ALTERNATE_HTTP_PORT);

        final Document doc = createDocumentFromSettingsWithProtocol(settings, APR_PROTOCOL);

        settings.setControlPort(CONTROL_PORT);
        settings.setHttpPort(HTTP_PORT);

        SettingsLoader.saveNetworkingSettingsToServerXml(settings, doc);
        assertEquals(1, numberOfConnectors(doc));
        assertDefaultControlPort(doc);
        assertExistingHttpConnector(doc, APR_PROTOCOL);
    }

    @Test
    public void testUpdateHttpOnlyConfigurationWithHttpAndHttpsConfiguration() throws Exception
    {
        settings.setControlPort(ALTERNATE_CONTROL_PORT);
        settings.setHttpPort(ALTERNATE_HTTP_PORT);

        final Document doc = createDocumentFromSettings(settings);

        settings.setControlPort(CONTROL_PORT);
        settings.setHttpPort(HTTP_PORT);
        settings.setSslSettings(createDefaultSslSettings());

        SettingsLoader.saveNetworkingSettingsToServerXml(settings, doc);
        assertEquals(2, numberOfConnectors(doc));
        assertDefaultControlPort(doc);
        assertExistingHttpConnector(doc, DEFAULT_PROTOCOL);
        assertDefaultHttpsConnector(doc);
    }

    @Test
    public void testRedirectPortAttributeUpdatedProperly() throws Exception
    {
        settings.setControlPort(ALTERNATE_CONTROL_PORT);
        settings.setHttpPort(ALTERNATE_HTTP_PORT);

        final Document doc = createDocumentFromSettings(settings);

        final SslSettings sslSettings = createSslSettings(ALTERNATE_HTTPS_PORT);
        settings.setControlPort(CONTROL_PORT);
        settings.setHttpPort(HTTP_PORT);
        settings.setSslSettings(sslSettings);

        SettingsLoader.saveNetworkingSettingsToServerXml(settings, doc);
        final Element connectorElement = selectElement(doc, "/Server/Service/Connector[@redirectPort]");
        assertNotNull(connectorElement);
        assertEquals(sslSettings.getHttpsPort(), xpath(connectorElement, "@redirectPort"));
    }

    @Test
    public void testUpdateHttpAndHttpsConfiguration() throws Exception
    {
        settings.setControlPort(ALTERNATE_CONTROL_PORT);
        settings.setHttpPort(ALTERNATE_HTTP_PORT);
        settings.setSslSettings(createSslSettings(ALTERNATE_HTTPS_PORT));

        final Document doc = createDocumentFromSettings(settings);

        settings.setControlPort(CONTROL_PORT);
        settings.setHttpPort(HTTP_PORT);
        settings.setSslSettings(createDefaultSslSettings());

        SettingsLoader.saveNetworkingSettingsToServerXml(settings, doc);
        assertEquals(2, numberOfConnectors(doc));
        assertDefaultControlPort(doc);
        assertExistingHttpConnector(doc, DEFAULT_PROTOCOL);
        assertDefaultHttpsConnector(doc);
    }

    @Test
    public void testUpdateHttpOnlyConfigurationWithHttpsOnlyConfiguration() throws Exception
    {
        settings.setControlPort(ALTERNATE_CONTROL_PORT);
        settings.setHttpPort(ALTERNATE_HTTP_PORT);
        settings.setSslSettings(createSslSettings(ALTERNATE_HTTPS_PORT));

        final Document doc = createDocumentFromSettings(settings);

        settings.setControlPort(CONTROL_PORT);
        settings.updateWebServerConfiguration(null, createDefaultSslSettings());

        SettingsLoader.saveNetworkingSettingsToServerXml(settings, doc);
        assertEquals(1, numberOfConnectors(doc));
        assertDefaultControlPort(doc);
        assertDefaultHttpsConnector(doc);
    }

    @Test
    public void testUpdateHttpAndHttpsConfigurationWithHttpOnlyConfiguration() throws Exception
    {
        settings.setControlPort(ALTERNATE_CONTROL_PORT);
        settings.updateWebServerConfiguration(ALTERNATE_HTTP_PORT, createSslSettings(ALTERNATE_HTTPS_PORT));

        final Document doc = createDocumentFromSettings(settings);

        settings.setControlPort(CONTROL_PORT);
        settings.updateWebServerConfiguration(HTTP_PORT, null);

        SettingsLoader.saveNetworkingSettingsToServerXml(settings, doc);
        assertEquals(1, numberOfConnectors(doc));
        assertDefaultControlPort(doc);
        assertExistingHttpConnector(doc, DEFAULT_PROTOCOL);
    }

    @Test
    public void testNoUpdateWhenMultipleHttpsConnectorsExist() throws Exception
    {
        final Document doc = createDocumentWithMultipleHttpsConnectors();

        settings.updateWebServerConfiguration(null, createSslSettings("1"));

        SettingsLoader.saveNetworkingSettingsToServerXml(settings, doc);
        final NodeList connectorNodes = selectAllConnectorNodes(doc);
        assertEquals(2, connectorNodes.getLength());
        assertEquals(HTTPS_PORT, xpath(connectorNodes.item(0), "@port"));
        assertEquals(ALTERNATE_HTTPS_PORT, xpath(connectorNodes.item(1), "@port"));
    }

    @Test
    public void testNoUpdateWhenAprEnabledSslConnectorExists() throws Exception
    {
        final Document doc = createDocumentWithAprEnabledHttpsConnector();

        settings.updateWebServerConfiguration(null, createSslSettings(ALTERNATE_HTTPS_PORT));

        SettingsLoader.saveNetworkingSettingsToServerXml(settings, doc);
        final NodeList connectorNodes = selectAllConnectorNodes(doc);
        assertEquals(1, connectorNodes.getLength());
        assertEquals(HTTPS_PORT, xpath(connectorNodes.item(0), "@port"));
    }

    @Test
    public void testUpdateHttpsOnly() throws Exception
    {
        settings.setControlPort(ALTERNATE_CONTROL_PORT);
        settings.updateWebServerConfiguration(null, createSslSettings(ALTERNATE_HTTPS_PORT));

        final Document doc = createDocumentFromSettings(settings);

        settings.setControlPort(CONTROL_PORT);
        settings.updateWebServerConfiguration(null, createDefaultSslSettings());

        SettingsLoader.saveNetworkingSettingsToServerXml(settings, doc);
        assertEquals(1, numberOfConnectors(doc));
        assertDefaultControlPort(doc);
        assertDefaultHttpsConnector(doc);
    }

    @Test
    public void testUpdateHttpsOnlyConfigurationWithHttpOnlyConfiguration() throws Exception
    {
        settings.setControlPort(ALTERNATE_CONTROL_PORT);
        settings.updateWebServerConfiguration(null, createSslSettings(ALTERNATE_HTTPS_PORT));

        final Document doc = createDocumentFromSettings(settings);

        settings.setControlPort(CONTROL_PORT);
        settings.updateWebServerConfiguration(HTTP_PORT, null);

        SettingsLoader.saveNetworkingSettingsToServerXml(settings, doc);
        assertEquals(1, numberOfConnectors(doc));
        assertDefaultControlPort(doc);
        assertDefaultHttpConnector(doc, DEFAULT_PROTOCOL);
    }

    @Test
    public void testInsertSecurityConstraint() throws Exception
    {
        settings.updateWebServerConfiguration(HTTP_PORT, createDefaultSslSettings());

        final Document doc = createDocument("<web-app/>");

        SettingsLoader.updateSecurityConstraint(settings, doc);
        assertEquals(1, countSecurityConstraints(doc));
        assertHasSecurityConstraint(doc);
    }

    @Test
    public void testNoUpdateAnotherSecurityConstraintIsPresent() throws Exception
    {
        settings.updateWebServerConfiguration(HTTP_PORT, createDefaultSslSettings());

        final Document doc = createDocWithSecurityConstraint("another-constraint");

        SettingsLoader.updateSecurityConstraint(settings, doc);
        assertEquals(1, countSecurityConstraints(doc));
        assertNotHasSecurityConstraint(doc);
    }

    @Test
    public void testSecurityConstraintNotDuplicatedOnUpdate() throws Exception
    {
        settings.updateWebServerConfiguration(HTTP_PORT, createDefaultSslSettings());

        final Document doc = createDocWithSecurityConstraint(SECURITY_CONSTRAINT_NAME);

        SettingsLoader.updateSecurityConstraint(settings, doc);
        assertEquals(1, countSecurityConstraints(doc));
        assertHasSecurityConstraint(doc);
    }

    @Test
    public void testRemoveSecurityConstraintWhenHttpOnly() throws Exception
    {
        settings.updateWebServerConfiguration(HTTP_PORT, null);

        final Document doc = createDocWithSecurityConstraint(SECURITY_CONSTRAINT_NAME);

        SettingsLoader.updateSecurityConstraint(settings, doc);
        assertEquals(0, countSecurityConstraints(doc));
        assertNotHasSecurityConstraint(doc);
    }

    @Test
    public void testRemoveSecurityConstraintWhenHttpsOnly() throws Exception
    {
        settings.updateWebServerConfiguration(null, createDefaultSslSettings());

        final Document doc = createDocWithSecurityConstraint(SECURITY_CONSTRAINT_NAME);

        SettingsLoader.updateSecurityConstraint(settings, doc);
        assertEquals(0, countSecurityConstraints(doc));
        assertNotHasSecurityConstraint(doc);
    }

    private void assertHasComment(@Nonnull final Document doc) throws XPathExpressionException
    {
        final Element serviceElement = selectElement(doc, "/Server/Service");
        final NodeList childNodes = serviceElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++)
        {
            final Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.COMMENT_NODE)
            {
                return;
            }
        }
        fail("failed to find a comment node within the service element.");
    }

    private void assertNotHasSecurityConstraint(@Nonnull final Document doc) throws XPathExpressionException
    {
        final NodeList displayNameNodeList = selectElements(doc, "/web-app/security-constraint/display-name");
        for (int i = 0; i < displayNameNodeList.getLength(); i++)
        {
            Node item = displayNameNodeList.item(i);
            if (item.getTextContent().contains(SECURITY_CONSTRAINT_NAME))
            {
                fail("Didn't expected to find a security-constraint node with the display-name '" + SECURITY_CONSTRAINT_NAME + '\'');
            }
        }
    }

    private int countSecurityConstraints(@Nonnull final Document doc) throws XPathExpressionException
    {
        return Integer.parseInt(xpath(doc, "count(/web-app/security-constraint)"));
    }

    private void assertHasSecurityConstraint(@Nonnull final Document doc) throws XPathExpressionException
    {
        final NodeList displayNameNodeList = selectElements(doc, "/web-app/security-constraint/display-name");
        for (int i = 0; i < displayNameNodeList.getLength(); i++)
        {
            Node item = displayNameNodeList.item(i);
            if (item.getTextContent().contains(SECURITY_CONSTRAINT_NAME))
            {
                return;
            }
        }
        fail("Expected to find one security-constraint node with the display-name '" + SECURITY_CONSTRAINT_NAME + '\'');
    }

    @Nonnull
    private Document createDocument(@Nonnull final String xmlText) throws Exception
    {
        final DocumentBuilder builder = SecureXmlParserFactory.newNamespaceAwareDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xmlText.getBytes()));
    }

    private Document createDocumentWithMultipleHttpConnectors() throws Exception
    {
        return createDocument(
                "<Server port=\"" + CONTROL_PORT + "\">" +
                        "   <Service>" +
                        "       <Connector port=\"" + HTTP_PORT + "\" protocol=\"" + DEFAULT_PROTOCOL + "\" />" +
                        "       <Connector port=\"" + ALTERNATE_HTTP_PORT + "\" protocol=\"" + DEFAULT_PROTOCOL + "\" />" +
                        "   </Service>" +
                        "</Server>"
        );
    }

    private Document createDocumentWithMultipleHttpsConnectors() throws Exception
    {
        return createDocument(
                "<Server port=\"" + CONTROL_PORT + "\">" +
                        "   <Service>" +
                        "       <Connector port=\"" + HTTPS_PORT + "\" scheme=\"" + HTTPS_SCHEME + "\" secure=\"true\" keystoreFile=\"" + KEY_STORE_FILE_NAME + "\" keystorePass=\"" + KEY_STORE_PASSWORD + "\" keyAlias=\"" + KEY_ALIAS + "\" />" +
                        "       <Connector port=\"" + ALTERNATE_HTTPS_PORT + "\" scheme=\"" + HTTPS_SCHEME + "\" secure=\"true\" keystoreFile=\"" + KEY_STORE_FILE_NAME + "\" keystorePass=\"" + KEY_STORE_PASSWORD + "\" keyAlias=\"" + KEY_ALIAS + "\" />" +
                        "   </Service>" +
                        "</Server>"
        );
    }

    private Document createDocumentWithAprEnabledHttpsConnector() throws Exception
    {
        return createDocument(
                "<Server port=\"" + CONTROL_PORT + "\">" +
                "   <Service>" +
                "       <Connector protocol=\"" + APR_PROTOCOL + "\" port=\"" + HTTPS_PORT + "\" scheme=\"" + HTTPS_SCHEME + "\" secure=\"true\" keystoreFile=\"" + KEY_STORE_FILE_NAME + "\" keystorePass=\"" + KEY_STORE_PASSWORD + "\" keyAlias=\"" + KEY_ALIAS + "\" />" +
                "   </Service>" +
                "</Server>"
        );
    }

    private Document createDocumentFromSettings(@Nonnull final Settings settings) throws Exception
    {
        return createDocumentFromSettingsWithProtocol(settings, DEFAULT_PROTOCOL);
    }

    private Document createDocumentFromSettingsWithProtocol(@Nonnull final Settings settings, @Nonnull final String protocol) throws Exception
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(String.format("<Server port=\"%s\">", settings.getControlPort()));
        builder.append("<Service>");

        final WebServerProfile webServerProfile = settings.getWebServerProfile();
        if (webServerProfile.isHttpEnabled())
        {
            final String httpPort = settings.getHttpPort();
            builder.append(String.format("<Connector port=\"%s\" protocol=\"" + protocol + "\" redirectPort=\"8443\"/>", httpPort));
        }

        final SslSettings sslSettings = settings.getSslSettings();
        if (webServerProfile.isHttpsEnabled() && sslSettings != null)
        {
            builder.append("<Connector")
                    .append(String.format(" port=\"%s\"", sslSettings.getHttpsPort()))
                    .append(" scheme=\"" + HTTPS_SCHEME + "\" secure=\"true\"")
                    .append(String.format(" keystoreFile=\"%s\"", sslSettings.getKeystoreFile()))
                    .append(String.format(" keystorePass=\"%s\"", sslSettings.getKeystorePass()))
                    .append(String.format(" keyAlias=\"%s\"", sslSettings.getHttpsPort()))
                    .append(" />");
        }

        builder.append("</Service>");
        builder.append("</Server>");

        return createDocument(builder.toString());
    }

    private SslSettings createDefaultSslSettings()
    {
        return new SslSettings(HTTPS_PORT, KEY_STORE_FILE_NAME, KEY_STORE_PASSWORD, KEY_STORE_TYPE, KEY_ALIAS);
    }

    private SslSettings createSslSettings(@Nonnull final String httpsPort)
    {
        return new SslSettings(httpsPort, "/another/file", "obvious", "foo", "different-alias");
    }

    private void assertDefaultControlPort(@Nonnull final Document doc) throws XPathExpressionException
    {
        assertEquals(CONTROL_PORT, xpath(doc, "/Server/@port"));
    }

    private Document createDocWithSecurityConstraint(@Nonnull final String displayName) throws Exception
    {
        return createDocument(
                "<web-app>" +
                "   <security-constraint>" +
                "       <display-name>" + displayName + "</display-name>" +
                "       <web-resource-collection>" +
                "           <web-resource-name>my web resources</web-resource-name>" +
                "       </web-resource-collection>" +
                "   </security-constraint>" +
                "</web-app>"
        );
    }

    private void assertExistingHttpConnector(@Nonnull final Document doc, @Nonnull final String protocol) throws XPathExpressionException
    {
        final Element connectorElement = selectElement(doc, "/Server/Service/Connector[not(@SSLEnabled) or @SSLEnabled = 'false']");
        assertNotNull(connectorElement);
        assertEquals(HTTP_PORT, xpath(connectorElement, "@port"));
        assertEquals(protocol, xpath(connectorElement, "@protocol"));
        assertEquals(3, numberOfAttributes(connectorElement));
    }

    private void assertDefaultHttpConnector(@Nonnull final Document doc, @Nonnull final String protocol) throws XPathExpressionException
    {
        final Element connectorElement = selectElement(doc, "/Server/Service/Connector[not(@SSLEnabled) or @SSLEnabled = 'false']");
        assertNotNull(connectorElement);
        assertEquals(HTTP_PORT, xpath(connectorElement, "@port"));
        assertEquals(protocol, xpath(connectorElement, "@protocol"));
        assertDefaultConnectorAttributes(connectorElement);
        assertEquals(11, numberOfAttributes(connectorElement));
    }

    private void assertDefaultHttpsConnector(@Nonnull final Document doc) throws XPathExpressionException
    {
        final Element connectorElement = selectElement(doc, "/Server/Service/Connector[@SSLEnabled = 'true']");
        assertNotNull(connectorElement);
        assertEquals(HTTPS_PORT, xpath(connectorElement, "@port"));
        assertEquals(HTTPS_SCHEME, xpath(connectorElement, "@scheme"));
        assertEquals(KEY_STORE_FILE_NAME, xpath(connectorElement, "@keystoreFile"));
        assertEquals(KEY_STORE_PASSWORD, xpath(connectorElement, "@keystorePass"));
        assertEquals(KEY_STORE_TYPE, xpath(connectorElement, "@keystoreType"));
        assertEquals(KEY_ALIAS, xpath(connectorElement, "@keyAlias"));
        assertEquals("true", xpath(connectorElement, "@SSLEnabled"));
        assertEquals("org.apache.coyote.http11.Http11Protocol", xpath(connectorElement, "@protocol"));

        assertDefaultConnectorAttributes(connectorElement);

        assertEquals(20, numberOfAttributes(connectorElement));
    }

    private void assertDefaultConnectorAttributes(Element connectorElement) throws XPathExpressionException
    {
        assertEquals("150", xpath(connectorElement, "@maxThreads"));
        assertEquals("25", xpath(connectorElement, "@minSpareThreads"));
        assertEquals("75", xpath(connectorElement, "@maxSpareThreads"));
        assertEquals("20000", xpath(connectorElement, "@connectionTimeout"));
        assertEquals("false", xpath(connectorElement, "@enableLookups"));
        assertEquals("8192", xpath(connectorElement, "@maxHttpHeaderSize"));
        assertEquals("true", xpath(connectorElement, "@useBodyEncodingForURI"));
        assertEquals("100", xpath(connectorElement, "@acceptCount"));
        assertEquals("true", xpath(connectorElement, "@disableUploadTimeout"));
    }

    private int numberOfConnectors(@Nonnull final Document doc) throws XPathExpressionException
    {
        return selectAllConnectorNodes(doc).getLength();
    }

    private int numberOfAttributes(@Nonnull final Node node) throws XPathExpressionException
    {
        return Integer.parseInt(xpath(node, "count(@*)"));
    }

    private String xpath(@Nonnull final Node node, @Nonnull final String xPathExpression) throws XPathExpressionException
    {
        return createXPath().evaluate(xPathExpression, node);
    }

    private NodeList selectAllConnectorNodes(@Nonnull final Document doc) throws XPathExpressionException
    {
        return selectElements(doc, "/Server/Service/Connector");
    }

    private Element selectElement(@Nonnull final Node node, @Nonnull final String xPathExpression) throws XPathExpressionException
    {
        return (Element) createXPath().evaluate(xPathExpression, node, XPathConstants.NODE);
    }

    private NodeList selectElements(@Nonnull final Node node, @Nonnull final String xPathExpression) throws XPathExpressionException
    {
        return (NodeList) createXPath().evaluate(xPathExpression, node, XPathConstants.NODESET);
    }

    private XPath createXPath()
    {
        final XPathFactory xPathFactory = XPathFactory.newInstance();
        return xPathFactory.newXPath();
    }

}
