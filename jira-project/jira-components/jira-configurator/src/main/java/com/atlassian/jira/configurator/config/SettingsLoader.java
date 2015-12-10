package com.atlassian.jira.configurator.config;

import com.atlassian.jira.config.database.DatabaseConfig;
import com.atlassian.jira.config.database.DatabaseConfigurationLoader;
import com.atlassian.jira.config.database.DatabaseType;
import com.atlassian.jira.config.database.Datasource;
import com.atlassian.jira.config.database.JdbcDatasource;
import com.atlassian.jira.config.database.JndiDatasource;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.security.xml.SecureXmlParserFactory;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.config.ConnectionPoolInfo;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Loads current settings from the relevant files.
 *
 * @since v4.0
 */
public class SettingsLoader
{
    private static final String DEV_MODE_SERVER_XML = "target/tomcat7/work/conf/server.xml";
    static final String SECURITY_CONSTRAINT_NAME = "HTTP to HTTPs Redirection";

    private static final boolean devMode = new File(DEV_MODE_SERVER_XML).exists();

    public static Settings loadCurrentSettings()
            throws ParserConfigurationException, IOException, SAXException, ParseException
    {
        Settings settings = new Settings();
        // Load jira-home first - it now tells us where to find the DB settings.
        loadApplicationProperties(settings);
        // load DB config if we can find it.
        loadDbConfig(settings);
        loadServerXmlSettings(settings);
        loadWebXml(settings);
        return settings;
    }

    private static void loadWebXml(@Nonnull final Settings settings) throws IOException, ParseException
    {
        final String webXmlPath = getWebXmlPath();
        final Document doc = parseDocument(webXmlPath);
        checkExistingSecurityConstraints(doc, settings);
    }

    private static void checkExistingSecurityConstraints(@Nonnull final Document doc, @Nonnull final Settings settings) throws ParseException
    {
        final Node webAppNode = getOnlyChildNode(doc, "web-app");
        final int securityConstraintNodeCount = countSecurityConstraints(webAppNode);
        if (securityConstraintNodeCount == 1)
        {
            final Node redirectionSecurityConstraint = findSecurityConstraint(webAppNode);
            if (redirectionSecurityConstraint == null)
            {
                settings.addComplexConfigurationReason(ComplexConfigurationReason.AnotherSecurityConstraintExisting);
            }
        }
        else if (securityConstraintNodeCount > 1)
        {
            settings.addComplexConfigurationReason(ComplexConfigurationReason.AnotherSecurityConstraintExisting);
        }
    }

    /**
     * Saves the user's new settings back to the required files
     *
     * @param newSettings The new settings to save
     * @throws java.io.IOException If any errors occur.
     * @throws ParseException if processing of the xml file failed
     */
    public static void saveSettings(final Settings newSettings)
            throws IOException, ParseException
    {
        // Save the jira-home settings to the jira-application.properties file
        saveSettingsToApplicationProperties(newSettings);
        // Save the network settings to the server.xml file
        saveSettingsToServerXml(newSettings);
        // Save the database config settings to dbconfig.xml
        saveDbConfig(newSettings);
        updateWebXml(newSettings);
    }

    private static void saveSettingsToApplicationProperties(final Settings newSettings) throws IOException
    {
        // Check the new jira-home is valid
        if (newSettings.getJiraHome() == null || newSettings.getJiraHome().trim().length() == 0)
        {
            throw new IOException("Please set a value for jira-home.");
        }
        File jiraHomeDirectory = new File(newSettings.getJiraHome());
        if (jiraHomeDirectory.exists())
        {
            if (!jiraHomeDirectory.isDirectory())
            {
                throw new IOException("jira-home '" + newSettings.getJiraHome() + "' is not a directory.");
            }
        }
        else
        {
            // Doesn't exist yet - try to create it as it will have to exist in order to write dbconfig.xml
            boolean created = jiraHomeDirectory.mkdirs();
            if (!created)
            {
                throw new IOException("Unable to create jira-home '" + newSettings.getJiraHome() + "'");
            }
        }

        // Load the current file into memory, and set jira-home as we find it.
        BufferedReader reader = new BufferedReader(new FileReader(getJiraApplicationProperties()));
        StringBuilder sb = new StringBuilder();
        try
        {
            String line;
            boolean jiraHomeSet = false;
            while ((line = reader.readLine()) != null)
            {
                if (isJiraHomeProperty(line))
                {
                    sb.append("jira.home = ").append(encodeForPropertiesFile(newSettings.getJiraHome())).append("\n");
                    jiraHomeSet = true;
                }
                else
                {
                    sb.append(line).append("\n");
                }
            }

            if (!jiraHomeSet)
            {
                throw new IOException("Unable to find the jira.home property to replace in the jira.application.properties file (" + getJiraApplicationProperties() + ").");
            }
        }
        finally
        {
            reader.close();
        }

        // Now save the updated contents back
        FileWriter writer = new FileWriter(getJiraApplicationProperties());
        try
        {
            writer.write(sb.toString());
        }
        finally
        {
            writer.close();
        }
    }

    /**
     * Encodes the jira-home path into a value that is suitable for the rubbish Java properties file.
     * That is, if it includes backslashes (Windows path), these will be encoded to double-backslash
     *
     * @param value The raw value
     * @return The encoded value
     */
    private static String encodeForPropertiesFile(final String value)
    {
        // Replace a single \ with \\  (but it has to be encoded for this java file)
        return value.replace("\\", "\\\\");
    }

    private static boolean isJiraHomeProperty(final String line)
    {
        if (line.startsWith("jira.home"))
        {
            // Strip the jira.home
            String value = line.substring("jira.home".length());
            // You are allowed arbitrary whitespace before the equals sign
            value = value.trim();
            // ensure that we have an equals sign
            if (value.startsWith("="))
            {
                // OK - this is our jira.home line
                return true;
            }
        }
        return false;
    }

    private static void saveSettingsToServerXml(final Settings newSettings) throws IOException
    {
        Document doc;
        try
        {
            doc = parseDocument(getServerXmlFile());
            saveNetworkingSettingsToServerXml(newSettings, doc);
        }
        catch (Exception ex)
        {
            String message = "An error occured while trying to save settings. Your settings may be in an invalid state.";
            if (ex.getMessage() != null)
            {
                message = message + " " + ex.getMessage();
            }
            throw new IOException(message);
        }

        // Now we need to write the in-memory Document back to file
        writeXmlFile(doc, getServerXmlFile());
    }

    private static void updateWebXml(@Nonnull final Settings newSettings) throws IOException, ParseException
    {
        final String webXmlPath = getWebXmlPath();
        final Document doc = parseDocument(webXmlPath);
        final boolean isDirty = updateSecurityConstraint(newSettings, doc);
        if (isDirty)
        {
            writeXmlFile(doc, webXmlPath);
        }
    }

    static void saveNetworkingSettingsToServerXml(final Settings newSettings, final Document doc) throws ParseException
    {
        //<Server port="8095" shutdown="SHUTDOWN">
        //  <Service name="Catalina">
        //    <Connector port="8090" protocol="HTTP/1.1" redirectPort="8443" useBodyEncodingForURI="true"/>
        //            ...
        //    <Connector URIEncoding="UTF-8" enableLookups="false" port="8009" protocol="AJP/1.3" redirectPort="8443"/>

        final Node serverNode = getOnlyChildNode(doc, "Server");

        // Server Control Port
        addAttributeToNode(serverNode, "port", newSettings.getControlPort());

        final Node serviceNode = getOnlyChildNode(serverNode, "Service");
        updateHttpConnector(newSettings, serviceNode);
        updateHttpsConnector(newSettings, serviceNode);
    }

    static boolean updateSecurityConstraint(@Nonnull final Settings newSettings, @Nonnull final Document doc) throws ParseException
    {
        final Node webAppNode = getOnlyChildNode(doc, "web-app");
        final int securityConstraintNodeNumber = countSecurityConstraints(webAppNode);
        final Node securityConstraintNode = findSecurityConstraint(webAppNode);
        final WebServerProfile webServerProfile = newSettings.getWebServerProfile();
        if (webServerProfile == WebServerProfile.HttpRedirectedToHttps)
        {
            if (securityConstraintNode == null && securityConstraintNodeNumber == 0)
            {
                webAppNode.appendChild(createSecurityConstraintNode(doc));
                return true;
            }
        }
        else
        {
            if (securityConstraintNode != null)
            {
                webAppNode.removeChild(securityConstraintNode);
                return true;
            }
        }
        return false;
    }

    private static int countSecurityConstraints(@Nullable final Node webAppNode)
    {
        if (webAppNode != null)
        {
            return getChildNodes(webAppNode, "security-constraint").size();
        }
        return 0;
    }

    @Nullable
    private static Node findSecurityConstraint(@Nullable final Node webAppNode) throws ParseException
    {
        if (webAppNode == null)
        {
            return null;
        }

        final List<Node> securityConstraintNodes = getChildNodes(webAppNode, "security-constraint");
        for (final Node securityConstraintNode : securityConstraintNodes)
        {
            final Node displayNameNode = getOnlyChildNode(securityConstraintNode, "display-name");
            if (displayNameNode != null)
            {
                final String displayNameText = Strings.nullToEmpty(displayNameNode.getTextContent()).trim();
                if (SECURITY_CONSTRAINT_NAME.equals(displayNameText))
                {
                    return securityConstraintNode;
                }
            }
        }
        return null;
    }

    private static Node createSecurityConstraintNode(@Nonnull final Document doc)
    {
        return addToNode(doc.createElement("security-constraint"),
                addToNode(
                        createElementWithTextContent(doc, "display-name", SECURITY_CONSTRAINT_NAME)
                ),
                addToNode(doc.createElement("web-resource-collection"),
                        createElementWithTextContent(doc, "web-resource-name", "all-except-attachments"),
                        createElementWithTextContent(doc, "url-pattern", "*.jsp"),
                        createElementWithTextContent(doc, "url-pattern", "*.jspa"),
                        createElementWithTextContent(doc, "url-pattern", "/browse/*")
                ),
                addToNode(doc.createElement("user-data-constraint"),
                        createElementWithTextContent(doc, "transport-guarantee", "CONFIDENTIAL")
                )
        );
    }

    private static Node addToNode(@Nonnull final Node parent, @Nonnull final Node...nodes)
    {
        for (final Node node : nodes)
        {
            parent.appendChild(node);
        }
        return parent;
    }

    private static Element createElementWithTextContent(@Nonnull final Document doc, @Nonnull final String tagName, @Nonnull final String textContent)
    {
        final Element element = doc.createElement(tagName);
        element.setTextContent(textContent);
        return element;
    }

    private static void updateHttpConnector(@Nonnull final Settings newSettings, @Nonnull final Node serviceNode)
    {
        final WebServerProfile webServerProfile = newSettings.getWebServerProfile();
        final List<Node> httpConnectorNodes = findHttpConnectorNodes(serviceNode);
        if (httpConnectorNodes.size() <= 1)
        {
            Node httpConnectorNode = httpConnectorNodes.isEmpty() ? null : httpConnectorNodes.get(0);
            if (webServerProfile.isHttpEnabled())
            {
                final String httpPort = newSettings.getHttpPort();
                if (httpConnectorNode == null)
                {
                    final Document doc = serviceNode.getOwnerDocument();
                    httpConnectorNode = serviceNode.appendChild(doc.createElement("Connector"));
                    addAttributeToNode(httpConnectorNode, "protocol", "HTTP/1.1");

                    addDefaultConnectorDefaultSettings(httpConnectorNode);
                }
                addAttributeToNode(httpConnectorNode, "port", httpPort);
                final SslSettings sslSettings = newSettings.getSslSettings();
                if (webServerProfile.isHttpsEnabled() && sslSettings != null)
                {
                    addAttributeToNode(httpConnectorNode, "redirectPort", sslSettings.getHttpsPort());
                }
            }
            else
            {
                if (httpConnectorNode != null)
                {
                    serviceNode.removeChild(httpConnectorNode);
                }
            }
        }
    }

    private static void updateHttpsConnector(@Nonnull final Settings newSettings, @Nonnull final Node serviceNode)
    {
        final WebServerProfile webServerProfile = newSettings.getWebServerProfile();
        final SslSettings sslSettings = newSettings.getSslSettings();
        final List<Node> httpsConnectorNodes = findHttpsConnectorNodes(serviceNode);
        if (httpsConnectorNodes.size() <= 1)
        {
            Node httpsConnectorNode;
            if (httpsConnectorNodes.isEmpty())
            {
                httpsConnectorNode = null;
            }
            else
            {
                httpsConnectorNode = httpsConnectorNodes.get(0);
                if (isAprEnabledConnector(httpsConnectorNode))
                {
                    return;
                }
            }

            if (webServerProfile.isHttpsEnabled() && sslSettings != null)
            {
                if (httpsConnectorNode == null)
                {
                    final Document doc = serviceNode.getOwnerDocument();
                    httpsConnectorNode = serviceNode.appendChild(doc.createElement("Connector"));
                }
                addAttributeToNode(httpsConnectorNode, "port", sslSettings.getHttpsPort());
                addAttributeToNode(httpsConnectorNode, "scheme", "https");
                addAttributeToNode(httpsConnectorNode, "secure", "true");
                final String keystoreFile = sslSettings.getKeystoreFile();
                if (keystoreFile != null)
                {
                    addAttributeToNode(httpsConnectorNode, "keystoreFile", keystoreFile);
                }
                addAttributeToNode(httpsConnectorNode, "keystorePass", sslSettings.getKeystorePass());
                addAttributeToNode(httpsConnectorNode, "keystoreType", sslSettings.getKeystoreType());
                final String keyAlias = sslSettings.getKeyAlias();
                if (keyAlias != null)
                {
                    addAttributeToNode(httpsConnectorNode, "keyAlias", keyAlias);
                }
                addAttributeToNode(httpsConnectorNode, "clientAuth", "false");
                addAttributeToNode(httpsConnectorNode, "sslProtocol", "TLS");
                // TODO support for tomcat5
                addAttributeToNode(httpsConnectorNode, "SSLEnabled", "true");
                addAttributeToNode(httpsConnectorNode, "protocol", "org.apache.coyote.http11.Http11Protocol");

                addDefaultConnectorDefaultSettings(httpsConnectorNode);
            }
            else
            {
                if (httpsConnectorNode != null)
                {
                    serviceNode.removeChild(httpsConnectorNode);
                }
            }
        }
    }

    private static List<Node> findHttpsConnectorNodes(@Nonnull final Node serviceNode)
    {
        return Lists.newArrayList(Iterables.filter(getChildNodes(serviceNode, "Connector"), new Predicate<Node>()
        {
            @Override
            public boolean apply(@Nullable final Node connectorNode)
            {
                if (connectorNode == null)
                {
                    return false;
                }

                final NamedNodeMap attributes = connectorNode.getAttributes();
                if (attributes == null)
                {
                    return false;
                }

                final String sslEnabledAttributeValue = Strings.nullToEmpty(getAttributeValue(connectorNode, "SSLEnabled"));
                if (sslEnabledAttributeValue.equalsIgnoreCase("true"))
                {
                    return true;
                }

                final String secureAttributeValue = Strings.nullToEmpty(getAttributeValue(connectorNode, "secure"));
                return secureAttributeValue.equalsIgnoreCase("true");
            }
        }));
    }

    private static void addDefaultConnectorDefaultSettings(@Nonnull final Node httpsConnectorNode)
    {
        updateAttribute(httpsConnectorNode, "maxThreads", "150");
        updateAttribute(httpsConnectorNode, "minSpareThreads", "25");
        updateAttribute(httpsConnectorNode, "maxSpareThreads", "75");
        updateAttribute(httpsConnectorNode, "connectionTimeout", "20000");

        updateAttribute(httpsConnectorNode, "enableLookups", "false");
        updateAttribute(httpsConnectorNode, "maxHttpHeaderSize", "8192");
        updateAttribute(httpsConnectorNode, "useBodyEncodingForURI", "true");

        updateAttribute(httpsConnectorNode, "acceptCount", "100");
        updateAttribute(httpsConnectorNode, "disableUploadTimeout", "true");
    }

    private static void saveDbConfig(Settings newSettings)
    {
        final DatabaseConfigurationLoader databaseConfigurationLoader = new JiraHomeDatabaseConfigurationLoader(newSettings.getJiraHome());
        final DatabaseType databaseType = newSettings.initDatabaseType(false);
        newSettings.applyDefaultAdvancedSettings();

        final ConnectionPoolInfo connectionPoolInfo = newSettings.getConnectionPoolInfoBuilder().build();
        final JdbcDatasource.Builder datasourceBuilder = newSettings.getJdbcDatasourceBuilder();
        try
        {
            final Datasource datasource = datasourceBuilder.setConnectionPoolInfo(connectionPoolInfo).build();
            final DatabaseConfig databaseConfig = new DatabaseConfig(databaseType.getTypeName(), newSettings.getSchemaName(), datasource);
            databaseConfigurationLoader.saveDatabaseConfiguration(databaseConfig);
        }
        finally
        {
            // Throw away the connection pool info because we'll keep its settings in
            // its own builder and don't want it to interfere when we compare settings objects
            datasourceBuilder.setConnectionPoolInfo(null);
        }
    }

    /**
     * Writes a DOM document to a file.
     *
     * @param doc The DOM Document
     * @param filename The filename
     * @throws java.io.IOException if any errors occur in writing this file.
     */
    private static void writeXmlFile(Document doc, String filename) throws IOException
    {
        try
        {
            // Prepare the DOM document for writing
            Source source = new DOMSource(doc);

            // JRADEV-6478 Use the filename, not the File Object because of a bug in Xalan Transformer.
            Result result = new StreamResult(filename);

            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
        }
        catch (Exception ex)
        {
            String errorMessage = "An error occurred while writing XML file '" + filename + "'.";
            System.err.println(errorMessage);
            System.err.println(ex.getMessage());
            ex.printStackTrace(System.err);
            throw new IOException(errorMessage + "\n" + ex.getMessage());
        }
    }

    private static Document parseDocument(String filename) throws IOException
    {
        try
        {
            return SecureXmlParserFactory.newDocumentBuilder().parse(filename);
        }
        catch (RuntimeException ex)
        {
            throw new IOException("Error occurred trying to parse the XML file '" + filename + "'. " + ex.getMessage());
        }
        catch (SAXException ex)
        {
            throw new IOException("Error occurred trying to parse the XML file '" + filename + "'. " + ex.getMessage());
        }
    }

    private static IOException asIOException(final RuntimeException re)
    {
        Throwable cause = re.getCause();
        if (cause == null)
        {
            cause = re;
        }
        else if (cause instanceof IOException)
        {
            return (IOException)cause;
        }
        final IOException ioe = new IOException("Unable to load database configuration: " + cause);
        ioe.initCause(cause);
        return ioe;
    }

    private static void loadDbConfig(final Settings settings) throws IOException
    {
        // First check if jira-home is set
        final String jiraHome = settings.getJiraHome();
        if (jiraHome == null || jiraHome.length() == 0)
        {
            System.out.println("jira-home not configured - no database settings can be loaded.");
            return;
        }

        // home is set - does the config file exist?
        File dbConfigFile = new File(settings.getJiraHome(), "dbconfig.xml");
        if (!dbConfigFile.exists())
        {
            System.out.println("DB config file '" + dbConfigFile.getAbsolutePath() + "' is not created yet - loading database settings as blank.");
            return;
        }

        // Load the config
        final DatabaseConfig databaseConfig;
        try
        {
            databaseConfig = new JiraHomeDatabaseConfigurationLoader(jiraHome).loadDatabaseConfiguration();
        }
        catch (RuntimeException re)
        {
            throw asIOException(re);
        }

        loadDbConfig(databaseConfig, settings);
    }

    private static void loadDbConfig(DatabaseConfig databaseConfig, final Settings settings) throws IOException
    {
        final Datasource datasource = databaseConfig.getDatasource();
        if (datasource instanceof JndiDatasource)
        {
            throw new IOException("You current database configuration uses JNDI, and this configuration tool does not support that");
        }
        if (!(datasource instanceof JdbcDatasource))
        {
            throw new IOException("Unrecognized datasource configuration " + datasource);
        }
        final JdbcDatasource jdbcDatasource = (JdbcDatasource)datasource;
        settings.setSchemaName(databaseConfig.getSchemaName());

        // Throw away the connection pool info because we'll keep its settings in its
        // own builder and don't want it to interfere when we compare settings objects
        settings.setJdbcDatasourceBuilder(jdbcDatasource.toBuilder().setConnectionPoolInfo(null));
        settings.setConnectionPoolInfoBuilder(jdbcDatasource.getConnectionPoolInfo().toBuilder());
        try
        {
            settings.initDatabaseType(true);
        }
        catch (IllegalArgumentException iae)
        {
            throw new IOException(iae.getMessage());
        }
    }

    private static void loadServerXmlSettings(final Settings settings) throws IOException
    {
        final Document doc = parseDocument(getServerXmlFile());
        try
        {
            loadWebServerSettings(doc, settings);
        }
        catch (ParseException ex)
        {
            throw new IOException("Unable to parse the config file '" + getServerXmlFile() + "'. " + ex.getMessage());
        }
    }

    static void loadWebServerSettings(Document doc, final Settings settings) throws ParseException
    {
        //<Service name="Catalina">
        //  <Connector port="8090" protocol="HTTP/1.1" redirectPort="8443" useBodyEncodingForURI="true"/>
        //  ...
        //  <Connector URIEncoding="UTF-8" enableLookups="false" port="8009" protocol="AJP/1.3" redirectPort="8443"/>

        // Get the Control Port from the <Server> tag
        final Node serverNode = getOnlyChildNode(doc, "Server");
        settings.setControlPort(getAttributeValue(serverNode, "port"));

        // Get the HTTP Port from the HTTP <Connector>
        final Node childNode = getOnlyChildNode(serverNode, "Service");
        final String httpPort = parseHttpPort(childNode, settings);
        final SslSettings sslSettings = parseSslSettings(childNode, settings);
        settings.updateWebServerConfiguration(httpPort, sslSettings);
    }

    private static String parseHttpPort(@Nonnull final Node serviceNode, @Nonnull final Settings settings)
    {
        final List<Node> httpConnectorNodes = findHttpConnectorNodes(serviceNode);
        if (httpConnectorNodes.isEmpty())
        {
            return null;
        }
        else if (httpConnectorNodes.size() > 1)
        {
            settings.addComplexConfigurationReason(ComplexConfigurationReason.MultipleHttpConnectors);
            return null;
        }

        final Node httpConnectorNode = httpConnectorNodes.get(0);
        return getAttributeValue(httpConnectorNode, "port");
    }

    private static List<Node> findHttpConnectorNodes(@Nonnull final Node serviceNode)
    {
        return Lists.newArrayList(Iterables.filter(getChildNodes(serviceNode, "Connector"), new Predicate<Node>()
        {
            @Override
            public boolean apply(@Nullable final Node connectorNode)
            {
                if (connectorNode == null)
                {
                    return false;
                }

                final NamedNodeMap attributes = connectorNode.getAttributes();
                if (attributes == null)
                {
                    return false;
                }

                final String sslEnabledAttributeValue = Strings.nullToEmpty(getAttributeValue(connectorNode, "SSLEnabled"));
                if (sslEnabledAttributeValue.equalsIgnoreCase("true"))
                {
                    return false;
                }

                final String secureAttributeValue = getAttributeValue(connectorNode, "secure");
                return secureAttributeValue == null || secureAttributeValue.equalsIgnoreCase("false");
            }
        }));
    }

    @Nullable
    private static SslSettings parseSslSettings(@Nonnull final Node serviceNode, @Nonnull final Settings settings)
    {
        final List<Node> sslConnectorNodes = getChildNodesWithAttribute(serviceNode, "Connector", "secure", "true");
        if (sslConnectorNodes.isEmpty())
        {
            return null;
        }
        else if (sslConnectorNodes.size() > 1)
        {
            settings.addComplexConfigurationReason(ComplexConfigurationReason.MultipleHttpsConnectors);
            return null;
        }

        final Node sslConnectorNode = sslConnectorNodes.get(0);
        if (isAprEnabledConnector(sslConnectorNode))
        {
            settings.addComplexConfigurationReason(ComplexConfigurationReason.SslEnabledAprConnector);
            return null;
        }
        // TODO: add warning if the following protocol is in use and the tomcat native library is found
//        if ("HTTP/1.1".equals(protocol) && pathContainsTomcatNativeLibrary())
//        {
//            settings.addComplexConfigurationReason(ComplexConfigurationReason.SslEnabledAprConnector);
//            return null;
//        }
        final String port = getAttributeValue(sslConnectorNode, "port");
        final String keystoreFile = getAttributeValue(sslConnectorNode, "keystoreFile");
        final String keystorePass = StringUtils.defaultString(getAttributeValue(sslConnectorNode, "keystorePass"), "changeit");
        final String keystoreType = StringUtils.defaultString(getAttributeValue(sslConnectorNode, "keystoreType"), "JKS");
        final String keyAlias = getAttributeValue(sslConnectorNode, "keyAlias");
        return new SslSettings(port, keystoreFile, keystorePass, keystoreType, keyAlias);
    }

    private static boolean isAprEnabledConnector(Node connectorNode)
    {
        final String protocol = Strings.nullToEmpty(getAttributeValue(connectorNode, "protocol"));
        return "org.apache.coyote.http11.Http11AprProtocol".equals(protocol);
    }

    private static void loadApplicationProperties(final Settings settings) throws IOException
    {
        final File jiraApplicationProperties = new File(getJiraApplicationProperties());
        logInfo("Loading application properties from " + jiraApplicationProperties.getCanonicalPath());
        InputStream propertiesInputStream = new FileInputStream(jiraApplicationProperties);
        try
        {
            String jiraHome = getJiraHomeValue(propertiesInputStream);
            // IF we are on Windoze, then display backslashes instead of forward slashes
            if (File.separatorChar == '\\')
            {
                jiraHome = jiraHome.replace("/", "\\");
            }
            settings.setJiraHome(jiraHome);
        }
        catch (ParseException ex)
        {
            throw new IOException("Error parsing " + jiraApplicationProperties + ". " + ex.getMessage());
        }
        finally
        {
            propertiesInputStream.close();
        }
    }

    private static void logInfo(String message)
    {
        System.out.println(message);
    }

    static String getJiraHomeValue(InputStream propertiesInputStream) throws ParseException, IOException
    {
        Properties applicationProperties = new Properties();
        applicationProperties.load(propertiesInputStream);
        String jiraHome = applicationProperties.getProperty("jira.home");
        if (jiraHome == null)
        {
            // We didn't find the jira.home property :(
            throw new ParseException("Unable to find the jira.home property.");
        }

        return jiraHome;
    }

    @Nonnull
    private static List<Node> getChildNodesWithAttribute(final Node parentNode, final String tagName, final String attributeName, final String attributeValue)
    {
        final List<Node> nodes = getChildNodes(parentNode, tagName);
        final List<Node> result = new ArrayList<Node>();
        for (Node node : nodes)
        {
            String value = getAttributeValue(node, attributeName);
            if (attributeValue.equals(value))
            {
                result.add(node);
            }
        }
        return result;
    }

    private static String getAttributeValue(final Node node, final String attributeName)
    {
        NamedNodeMap attributes = node.getAttributes();
        if (attributes == null)
        {
            return null;
        }
        Node attrNode = attributes.getNamedItem(attributeName);
        if (attrNode == null)
        {
            return null;
        }
        return attrNode.getNodeValue();
    }

    private static Node getOnlyChildNode(final Node parentNode, final String tagName) throws ParseException
    {
        List<Node> childNodes = getChildNodes(parentNode, tagName);
        if (childNodes.size() == 1)
        {
            return childNodes.get(0);
        }
        else if (childNodes.size() == 0)
        {
            return null;
        }
        else
        {
            throw new ParseException("Expected to find one child <" + tagName + "> in <" + parentNode.getNodeName() + "> but found " + childNodes.size());
        }
    }

    private static List<Node> getChildNodes(final Node parentNode, final String tagName)
    {
        NodeList childNodes = parentNode.getChildNodes();
        final List<Node> list = new ArrayList<Node>();
        for (int i = 0; i < childNodes.getLength(); i++)
        {
            Node child = childNodes.item(i);
            if (child.getNodeName().equals(tagName))
            {
                list.add(child);
            }
        }
        return list;
    }

    private static void addAttributeToNode(@Nonnull final Node targetNode, @Nonnull final String attributeName, @Nonnull final String attributeValue)
    {
        final NamedNodeMap nodeAttributes = targetNode.getAttributes();

        final Document doc = targetNode.getOwnerDocument();
        final Attr attribute = doc.createAttribute(attributeName);
        attribute.setNodeValue(attributeValue);
        nodeAttributes.setNamedItem(attribute);
    }

    private static void updateAttribute(@Nonnull final Node targetNode, @Nonnull final String attributeName, @Nonnull final String attributeValue)
    {
        final NamedNodeMap nodeAttributes = targetNode.getAttributes();
        if (nodeAttributes.getNamedItem(attributeName) == null)
        {
            final Document doc = targetNode.getOwnerDocument();
            final Attr attribute = doc.createAttribute(attributeName);
            attribute.setNodeValue(attributeValue);
            nodeAttributes.setNamedItem(attribute);
        }
    }

    private static String getServerXmlFile()
    {
        if (devMode)
        {
            return DEV_MODE_SERVER_XML;
        }
        else
        {
            return "../conf/server.xml";
        }
    }

    private static String getWebXmlPath()
    {
        if (devMode)
        {
            return "jira-components/jira-webapp/src/main/webapp/WEB-INF/web.xml";
        }
        else
        {
            return "../atlassian-jira/WEB-INF/web.xml";
        }
    }
    
    private static String getJiraApplicationProperties()
    {
        if (devMode)
        {
            return "jira-components/jira-core/src/main/resources/jira-application.properties";
        }
        else
        {
            return "../atlassian-jira/WEB-INF/classes/jira-application.properties";
        }
    }

    /**
     * Used to reload DB Config after the user changes the jira-home.
     *
     * @param jiraHome the new jira-home
     *
     * @return the new DB Settings (other settings will be blank)
     *
     * @throws IOException if an error occurrs while reading or parsing the dbconfig file.
     */
    public static Settings reloadDbConfig(String jiraHome) throws IOException
    {
        Settings settings = new Settings();
        settings.setJiraHome(jiraHome);
        loadDbConfig(settings);
        return settings;
    }
}
