package com.atlassian.jira.upgrade.tasks;

import com.atlassian.crowd.directory.DelegatedAuthenticationDirectory;
import com.atlassian.crowd.directory.GenericLDAP;
import com.atlassian.crowd.directory.InternalDirectory;
import com.atlassian.crowd.directory.RemoteCrowdDirectory;
import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import com.atlassian.crowd.directory.loader.LDAPDirectoryInstanceLoader;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.crowd.embedded.ofbiz.OfBizDirectoryDao;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.exception.DirectoryInstantiationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.security.xml.SecureXmlParserFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.removeEnd;

/**
 * Migrate the OSUser.xml configuration over to the new Crowd Embedded Directories.
 *
 * This is also a setup task, to create an initial directory configuration.
 *
 * @since v4.3
 */
public class UpgradeTask_Build601 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build601.class);

    /** This property is not used these days,  but is present in the pre crowd embedded world. */
    private static final String JIRA_OPTION_USER_PASSWORD_EXTERNALMGT = "jira.option.user.externalpasswordmanagement";
    private static final String DEFAULT_DIRECTORY = "JIRA Internal Directory";
    private static final String DELEGATING_DIRECTORY = "JIRA Delegated Authentication Directory";
    private static final String CROWD_DIRECTORY = "Remote Crowd Directory";
    private static final String APPLICATION_ENTITY_NAME = "Application";
    private static final String DIRECTORY_ENTITY_NAME = "Directory";
    private static final int INTERNAL_DIRECTORY_ID = 1;
    private static final int REMOTE_CROWD_DIRECTORY_ID = 2;
    private static final int DELEGATED_LDAP_DIRECTORY_ID = 3;

    private static final String DIRECTORY_OPERATION_ENTITY_NAME = "DirectoryOperation";
    private static final String DIRECTORY_ATTRIBUTE_ENTITY_NAME = "DirectoryAttribute";

    private final CrowdDirectoryService crowdDirectoryService;
    private final LDAPDirectoryInstanceLoader ldapDirectoryInstanceLoader;
    private final ApplicationProperties applicationProperties;

    private final String upgradeGuideUrl;
    private final String upgradeGuideTitle;

    private int ofbizProviderCount;
    private int crowdProviderCount;
    private int ldapProviderCount;
    private int unknownProviderCount;
    private List<String> providerList;
    private GenericDelegator genericDelegator;

    private static final Set<String> ofbizProviders = CollectionBuilder.newBuilder(
            // Jira extended providers
            "com.atlassian.core.ofbiz.osuser.CoreOFBizCredentialsProvider",
            "com.atlassian.jira.user.osuser.JiraOFBizProfileProvider",
            "com.atlassian.jira.user.osuser.JiraOFBizAccessProvider",
            // Original providers
            "com.opensymphony.user.provider.ofbiz.OFBizAccessProvider",
            "com.opensymphony.user.provider.ofbiz.OFBizProfileProvider",
            "com.opensymphony.user.provider.ofbiz.OFBizCredentialsProvider"
            ).asSet();

    private static final Set<String> crowdProviders = CollectionBuilder.newBuilder(
            "com.atlassian.crowd.integration.osuser.CrowdCredentialsProvider",
            "com.atlassian.crowd.integration.osuser.CrowdAccessProvider",
            "com.atlassian.crowd.integration.osuser.DelegatingProfileProvider"
            ).asSet();

    private static final Set<String> ldapProviders = CollectionBuilder.newBuilder(
            "com.opensymphony.user.provider.ldap.LDAPCredentialsProvider"
            ).asSet();
    private static final String CROWD_EMBEDDED_APPLICATION = "crowd-embedded";

    public UpgradeTask_Build601(GenericDelegator genericDelegator, CrowdDirectoryService crowdDirectoryService, LDAPDirectoryInstanceLoader ldapDirectoryInstanceLoader, ApplicationProperties applicationProperties)
    {
        super(false);
        this.genericDelegator = genericDelegator;
        this.crowdDirectoryService = crowdDirectoryService;
        this.ldapDirectoryInstanceLoader = ldapDirectoryInstanceLoader;
        this.applicationProperties = applicationProperties;

        HelpUtil.HelpPath helpPath = HelpUtil.getInstance().getHelpPath("upgrading");
        upgradeGuideUrl = helpPath.getUrl();
        upgradeGuideTitle = helpPath.getTitle();
    }

    public String getBuildNumber()
    {
        return "601";
    }

    @Override
    public String getShortDescription()
    {
        return "Migrate User Directory configuration";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        addJiraApplication();

        // Need to initialise these here because upgrade tasks are singletons and may be run multiple times in
        // the functional testing environments or when restoring backups from XML.
        providerList = new ArrayList<String>();
        ofbizProviderCount = 0;
        crowdProviderCount = 0;
        ldapProviderCount = 0;

        InputStream osuserStream = getOSUserXmlStream();

        if (!validatePresentConfiguration(osuserStream))
        {
            // Validation failed.
            return;
        }

        // load the osuser.xml if it exists and examine it to see what types of configuration it contains.
        Document document = null;

        boolean internalDirectory = false;
        boolean remoteCrowdDirectory = false;
        boolean ldapDirectory = false;

        if (osuserStream != null)
        {
            DocumentBuilder builder = SecureXmlParserFactory.newDocumentBuilder();
            document = builder.parse(osuserStream);

            extractConfigurationTypes(document);

            log.info("The following OSUser providers have been detected in the osuser.xml file.");
            for (String providerClassName : providerList)
            {
                log.info(providerClassName);
            }

            // Check we have a sensible configuration to migrate
            if (unknownProviderCount > 0)
            {
                addError(getI18nBean().getText("admin.errors.upgrade.601.error.bad.providers", upgradeGuideUrl, upgradeGuideTitle));
                return;
            }
            boolean oldConfigOK = false;
            if (ofbizProviderCount == 3 && crowdProviderCount == 0 && ldapProviderCount == 0)
            {
                oldConfigOK = true;
                internalDirectory = true;
            }
            else if (ofbizProviderCount == 0 && crowdProviderCount == 3 && ldapProviderCount == 0)
            {
                oldConfigOK = true;
                remoteCrowdDirectory = true;
            }
            else if (ofbizProviderCount == 3 && crowdProviderCount == 0 && ldapProviderCount >= 1)
            {
                oldConfigOK = true;
                ldapDirectory = true;
            }

            if (!oldConfigOK)
            {
                addError(getI18nBean().getText("admin.errors.upgrade.601.error.bad.osuser.config", upgradeGuideUrl, upgradeGuideTitle));
                return;
            }
            else
            {
                log.info("Migrating a valid User Directory configuration found in osuser.xml");
            }

            // log message

        }
        else
        {
            internalDirectory = true;
        }

        if (internalDirectory)
        {
            // Don't delete any directories.
            // Setup may have built a directory.
            // This will only create if it doesn't exist.  
            createInternalDirectoryConfiguration(0);
        }
        if (remoteCrowdDirectory)
        {
            removeAllDirectories();
            createRemoteCrowdDirectoryConfiguration(0);
            createInternalDirectoryConfiguration(1);
        }
        if (ldapDirectory)
        {
            removeAllDirectories();
            createDelegatingLdpaDirectoryConfiguration(document, 0);
            createInternalDirectoryConfiguration(ldapProviderCount);
        }

        // Flush directory cache
        DirectoryDao directoryDao = ComponentAccessor.getComponentOfType(DirectoryDao.class);
        if (directoryDao instanceof OfBizDirectoryDao)
        {
            ((OfBizDirectoryDao) directoryDao).flushCache();
        }
        else
        {
            log.error("Expected to find an OfBizDirectoryDao, but got " + directoryDao.getClass().getName());
        }

        // If we have an ldap directory make sure it is accessible before we try and go on to the next upgrade task.
        if (getErrors().isEmpty() && ldapDirectory)
        {
            testLdapConnections();
        }
    }

    /**
     * Create the default internal directory configuration.
     * @param position
     */
    private void createInternalDirectoryConfiguration(final int position) throws GenericEntityException
    {
        // Check if the default directory already exists
        if (!genericDelegator.findByAnd(DIRECTORY_ENTITY_NAME, FieldMap.build("directoryName", DEFAULT_DIRECTORY)).isEmpty())
        {
            // already created
            return;
        }

        addDirectory(INTERNAL_DIRECTORY_ID, DEFAULT_DIRECTORY, true, "JIRA default internal directory", InternalDirectory.class.getName(), DirectoryType.INTERNAL, position);
        // Add the allowed operations
        addDirectoryOperations(INTERNAL_DIRECTORY_ID, Sets.newHashSet(OperationType.values()));
        // Add the attributes
        addDirectoryAttribute(INTERNAL_DIRECTORY_ID, InternalDirectory.ATTRIBUTE_USER_ENCRYPTION_METHOD, PasswordEncoderFactory.ATLASSIAN_SECURITY_ENCODER);
    }


    protected void addJiraApplication() throws GenericEntityException
    {
        // Check if the default application already exists
        if (!genericDelegator.findByAnd(APPLICATION_ENTITY_NAME, FieldMap.build("name", CROWD_EMBEDDED_APPLICATION)).isEmpty())
        {
            // already created
            return;
        }

        FieldMap applicationFields = FieldMap.build("id", 1)
                .add("name", CROWD_EMBEDDED_APPLICATION)
                .add("lowerName", CROWD_EMBEDDED_APPLICATION.toLowerCase())
                .add("active", 1)
                .add("description", "")
                .add("applicationType", "CROWD")
                .add("credential", PasswordCredential.NONE.getCredential())
                .add("createdDate", new Timestamp(new Date().getTime()))
                .add("updatedDate", new Timestamp(new Date().getTime()));
        try
        {
            genericDelegator.create(APPLICATION_ENTITY_NAME, applicationFields);
        }
        catch (GenericEntityException e)
        {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void addDirectory(final int directoryId, final String directory, final boolean active, final String description, final String implementationClassName, final DirectoryType directoryType, final int position)
    {
        FieldMap directoryFields = FieldMap.build("id", directoryId)
                .add("directoryName", directory)
                .add("lowerDirectoryName", directory.toLowerCase())
                .add("active", active ? 1 : 0)
                .add("description", description)
                .add("type", directoryType.name())
                .add("position", position)
                .add("implementationClass", implementationClassName)
                .add("lowerImplementationClass", implementationClassName.toLowerCase())
                .add("createdDate", new Timestamp(new Date().getTime()))
                .add("updatedDate", new Timestamp(new Date().getTime()));
        try
        {
            genericDelegator.create(DIRECTORY_ENTITY_NAME, directoryFields);
        }
        catch (GenericEntityException e)
        {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void addDirectoryOperations(final int directoryId, final HashSet<OperationType> operationTypes)
    {
        for (final OperationType operationType : operationTypes)
        {
            FieldMap directoryOperationFields = FieldMap.build("directoryId", directoryId)
                    .add("operationType", operationType.getName());
            try
            {
                genericDelegator.create(DIRECTORY_OPERATION_ENTITY_NAME, directoryOperationFields);
            }
            catch (GenericEntityException e)
            {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }

    private void addDirectoryAttribute(final int directoryId, final String attributeKey, final String value)
    {
        FieldMap directoryAttributeFields = FieldMap.build("directoryId", directoryId)
                .add("name", attributeKey)
                .add("value", value);
        try
        {
            genericDelegator.create(DIRECTORY_ATTRIBUTE_ENTITY_NAME, directoryAttributeFields);
        }
        catch (GenericEntityException e)
        {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Create the remote crowd directory configuration.
     * Migrate the LDAP delgating directory configuration.
     * The parameters for the configuration are containded in the the crowd.properties file
     * <ul>
     * <li>application.name</li> 	 
     * <li>application.password/li>
     * <li>crowd.server.url/li>
     * </ul>
     *
     * @param position
     */
    private void createRemoteCrowdDirectoryConfiguration(final int position)
    {

        // For crowd the properties com from the crowd.properties file which should be on the classpath
        Properties crowdProperties = new Properties();
        InputStream crowdPropertyInputStream = this.getClass().getResourceAsStream("/crowd.properties");

        if (crowdPropertyInputStream == null)
        {
            throw new IllegalStateException("We found a Crowd Provider in the osuser.xml file to be migrated, but can't locate the 'crowd.properties' file.");
        }

        try
        {
            crowdProperties.load(crowdPropertyInputStream);
        }
        catch (IOException ex)
        {
            throw new RuntimeException("An error occurred while loading the 'crowd.properties' file for migrating the Crowd connection properties.", ex);
        }

        // Check we have the attributes we need to build a valid configuration
        boolean paramsOK = checkForRequiredCrowdParams(crowdProperties, "application.name")
                     && checkForRequiredCrowdParams(crowdProperties, "application.password")
                     && checkForRequiredCrowdParams(crowdProperties, "crowd.server.url");

        if (!paramsOK)
        {
            return;
        }

        addDirectory(REMOTE_CROWD_DIRECTORY_ID, CROWD_DIRECTORY, true, "Remote crowd directory", RemoteCrowdDirectory.class.getName(), DirectoryType.CROWD, position);
        // Add the allowed operations
        addDirectoryOperations(REMOTE_CROWD_DIRECTORY_ID, Sets.newHashSet(OperationType.values()));
        // Add the attributes

        final String crowdServiceUrl =
                new CrowdServiceUrlBuilder().
                        setPropertiesUrlTo((String) crowdProperties.get("crowd.server.url")).
                        build();

        addDirectoryAttribute(REMOTE_CROWD_DIRECTORY_ID, "application.name", (String) crowdProperties.get("application.name"));
        addDirectoryAttribute(REMOTE_CROWD_DIRECTORY_ID, "application.password", (String) crowdProperties.get("application.password"));

        if (crowdServiceUrl != null)
        {
            addDirectoryAttribute(REMOTE_CROWD_DIRECTORY_ID, "crowd.server.url", crowdServiceUrl);
        }

        addDirectoryAttribute(REMOTE_CROWD_DIRECTORY_ID, DirectoryImpl.ATTRIBUTE_KEY_USE_NESTED_GROUPS, "true");

        log.warn("Added migrated directory to JIRA:" + "Remote crowd directory");

    }

    /**
     * Migrate the LDAP delgating directory configuration.
     * The parameters for the configuration are containde din the OSUser config as parameter elements.
     * &lt;provider class="com.opensymphony.user.provider.ldap.LDAPCredentialsProvider"&gt;
     *   &lt;property name="java.naming.factory.initial"&gt;com.sun.jndi.ldap.LdapCtxFactory&lt;/property&gt;
     *   &lt;property name="java.naming.provider.url"&gt;ldap://localhost:389&lt;/property&gt;
     *   &lt;property name="searchBase"&gt;ou=People,dc=atlassian,dc=com&lt;/property&gt;
     *   &lt;property name="uidSearchName"&gt;uid&lt;/property&gt;
     *   &lt;property name="java.naming.security.principal"&gt;cn=admin,dc=atlassian,dc=com&lt;/property&gt;
     *   &lt;property name="java.naming.security.credentials"&gt;secret&lt;/property&gt;
     *   &lt;property name="exclusive-access"&gt;true&lt;/property&gt;
     * &lt;/provider&gt;
     *
     * @param document The DOM document containing the osuser.xml based configuration
     * @param position
     */
    private void createDelegatingLdpaDirectoryConfiguration(Document document, final int position) throws GenericEntityException
    {
        // Gather the properties from the osuser.xml

        int ldapIndex = 0;
        // Find the ldap provider element in the configuration
        Element ldapProviderElement = null;
        NodeList providerElements = document.getElementsByTagName("provider");
        for (int i = 0; i < providerElements.getLength(); i++)
        {
            Element providerElement = (Element) providerElements.item(i);
            String className = providerElement.getAttribute("class");
            if (className.contains("com.opensymphony.user.provider.ldap.LDAPCredentialsProvider"))
            {
                ldapProviderElement = providerElement;
                addDelegatingLdapDirectory(DELEGATED_LDAP_DIRECTORY_ID + ldapIndex, position + ldapIndex, ldapProviderElement);
                ldapIndex++;
            }
        }

        if (ldapProviderElement == null)
        {
            throw new RuntimeException("We found an LDAP Provider, but now it isn't there anymore.  That just shouldn't happen");
        }


    }

    private void addDelegatingLdapDirectory(int directoryId, int position, Element ldapProviderElement)
    {
        Map<String, String> oldParams = new HashMap<String, String>();

        // Collect the ldap configuration parameters
        NodeList paramAttributes = ldapProviderElement.getElementsByTagName("property");
        for (int i = 0; i < paramAttributes.getLength(); i++)
        {
            Element param = (Element) paramAttributes.item(i);
            oldParams.put(param.getAttribute("name"), param.getTextContent());
        }

        // Check we have the attributes we need to build a valid configuration
        boolean paramsOK = checkForRequiredLdapParams(oldParams, "java.naming.provider.url")
                     && checkForRequiredLdapParams(oldParams, "searchBase")
                     && checkForRequiredLdapParams(oldParams, "uidSearchName");

        if (!paramsOK)
        {
            return;
        }

        addDirectory(directoryId, DELEGATING_DIRECTORY, true, "JIRA delegating internal directory", DelegatedAuthenticationDirectory.class.getName(), DirectoryType.DELEGATING, position);
        // Add the alllowed operations
        addDirectoryOperations(directoryId, Sets.newHashSet(OperationType.values()));
        // Add the attributes
        addDirectoryAttribute(directoryId, LDAPPropertiesMapper.LDAP_URL_KEY, oldParams.get("java.naming.provider.url"));
        addDirectoryAttribute(directoryId, LDAPPropertiesMapper.LDAP_BASEDN_KEY, oldParams.get("searchBase"));
        if (oldParams.containsKey("java.naming.security.principal"))
        {
            addDirectoryAttribute(directoryId, LDAPPropertiesMapper.LDAP_USERDN_KEY, oldParams.get("java.naming.security.principal"));
        }
        if (oldParams.containsKey("java.naming.security.credentials"))
        {
            addDirectoryAttribute(directoryId, LDAPPropertiesMapper.LDAP_PASSWORD_KEY, oldParams.get("java.naming.security.credentials"));
        }
        addDirectoryAttribute(directoryId, LDAPPropertiesMapper.USER_USERNAME_KEY, oldParams.get("uidSearchName"));
        addDirectoryAttribute(directoryId, DelegatedAuthenticationDirectory.ATTRIBUTE_LDAP_DIRECTORY_CLASS, GenericLDAP.class.getName());
        addDirectoryAttribute(directoryId, DelegatedAuthenticationDirectory.ATTRIBUTE_CREATE_USER_ON_AUTH, String.valueOf(false));
        if (oldParams.containsKey("java.naming.referral"))
        {
            if ("follow".equals(oldParams.get("java.naming.referral")))
            {
                 addDirectoryAttribute(directoryId, LDAPPropertiesMapper.LDAP_REFERRAL_KEY, String.valueOf(true));
            }
        }
        log.warn("Added directory to JIRA:" + "JIRA delegating internal directory");
    }

    private void testLdapConnections() throws DirectoryInstantiationException, SQLException
    {
        Connection connection = getDatabaseConnection();
        try
        {
            String selectSql = "select id, directory_type from " + convertToSchemaTableName("cwd_directory") + " order by directory_position";
            PreparedStatement selectStmt = connection.prepareStatement(selectSql);

            ResultSet rs = selectStmt.executeQuery();
            while (rs.next())
            {
                String directoryType = rs.getString("directory_type");
                if (directoryType.equals(DirectoryType.DELEGATING.toString()))
                {
                    Directory delegatingDirectory = crowdDirectoryService.findDirectoryById(rs.getLong("id"));
                    Directory ldap = getLdapVersionOfDirectory(delegatingDirectory);

                    RemoteDirectory ldapAuthenticatingDirectory = ldapDirectoryInstanceLoader.getDirectory(ldap);
                    // Make sure we can authenticate and run a find, because we can't do anything if not.
                    try
                    {
                        ldapAuthenticatingDirectory.findUserByName("dummy-name");
                    }
                    catch (UserNotFoundException e)
                    {
                    }
                    catch (OperationFailedException e)
                    {
                        String message = e.getMessage();
                        // Look for authentication like words in the message text and give better message
                        if (message.toLowerCase().contains("authenticat"))
                        {
                            addError(getI18nBean().getText("admin.errors.upgrade.602.ldap.authentication.failed", e.getMessage()));
                        }
                        else
                        {
                            addError(getI18nBean().getText("admin.errors.upgrade.602.ldap.connection.failed", e.getMessage()));
                        }
                    }
                }
            }
        }
        finally
        {
            connection.close();
        }
    }

    private Directory getLdapVersionOfDirectory(Directory directory)
    {
        DirectoryImpl ldap = new DirectoryImpl(directory);

        String ldapClass = directory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_LDAP_DIRECTORY_CLASS);
        ldap.setImplementationClass(ldapClass);

        return ldap;
    }

    /**
     * Remove all current directories.
     */
    private void removeAllDirectories() throws GenericEntityException
    {
        genericDelegator.removeByAnd(DIRECTORY_ATTRIBUTE_ENTITY_NAME, new HashMap());
        genericDelegator.removeByAnd(DIRECTORY_OPERATION_ENTITY_NAME, new HashMap());
        genericDelegator.removeByAnd(DIRECTORY_ENTITY_NAME, new HashMap());
    }

    private boolean checkForRequiredCrowdParams(final Properties oldParams, final String s)
    {
        if (!oldParams.containsKey(s))
        {
            addError(getI18nBean().getText("admin.errors.upgrade.601.error.missing.crowd.param", s, upgradeGuideUrl, upgradeGuideTitle));
            return false;
        }
        return true;
    }

    private boolean checkForRequiredLdapParams(final Map<String, String> oldParams, final String s)
    {
        if (!oldParams.containsKey(s))
        {
            addError(getI18nBean().getText("admin.errors.upgrade.601.error.missing.ldap.param", s, upgradeGuideUrl, upgradeGuideTitle));
            return false;
        }
        return true;
    }

    private void extractConfigurationTypes(final Document document)
    {
        NodeList providerElements = document.getElementsByTagName("provider");
        for (int i = 0; i < providerElements.getLength(); i++)
        {
            Element providerElement = (Element) providerElements.item(i);
            String className = providerElement.getAttribute("class");
            if (className.contains("com.atlassian.jira.user.osuser"))
            {
                this.ofbizProviderCount++;
            }
            else if (className.contains("com.atlassian.core.ofbiz.osuser"))
            {
                this.ofbizProviderCount++;
            }
            else if (className.contains("com.opensymphony.user.provider.ofbiz"))
            {
                this.ofbizProviderCount++;
            }
            else if (ofbizProviders.contains(className))
            {
                this.ofbizProviderCount++;
            }
            else if (crowdProviders.contains(className))
            {
                this.crowdProviderCount++;
            }
            else if (ldapProviders.contains(className))
            {
                this.ldapProviderCount++;
            }
            else
            {
                log.error("OSUser config file 'osuser.xml' contains an unknown provider '" + className + "'.");
                this.unknownProviderCount++;
            }
            providerList.add(className);
        }
    }

    private boolean validatePresentConfiguration(final InputStream osuserStream) throws SQLException
    {
        final HelpUtil.HelpPath userManagementChangesHelpPath =
                HelpUtil.getInstance().getHelpPath("upgrade.note.43.usermanagement.changes");

        if (osuserStream != null)
        {
            // osuser.xml exists - we can migrate
            return true;
        }

        if (areExternalUsersPresent())
        {
            // External users exist in the DB, but we don't have an osuser.xml to migrate the config.
            addError(
                    getI18nBean().getText
                            (
                                    "admin.errors.upgrade.601.error.missing.osuser.xml",
                                    userManagementChangesHelpPath.getUrl(), userManagementChangesHelpPath.getTitle()
                            )
            );
            return false;
        }
        else if (isExternalUserManagement())
        {
            // External user management, but we don't have an osuser.xml to migrate the config.
            addError(
                    getI18nBean().getText
                            (
                                    "admin.errors.upgrade.601.error.missing.extrenal.user.management",
                                    userManagementChangesHelpPath.getUrl(), userManagementChangesHelpPath.getTitle()
                            )
            );
            return false;
        }
        else if (isExternalPasswordManagement())
        {
            // External password management, but we don't have an osuser.xml to migrate the config.
            addError(
                    getI18nBean().getText
                            (
                                    "admin.errors.upgrade.601.error.missing.extrenal.password.management",
                                    userManagementChangesHelpPath.getUrl(), userManagementChangesHelpPath.getTitle()
                            )
            );
            return false;
        }
        else
        {
            // There are only internal users so we can always migrate.
            return true;
        }
    }

    private boolean isExternalUserManagement()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
    }

    private boolean isExternalPasswordManagement()
    {
        return applicationProperties.getOption(JIRA_OPTION_USER_PASSWORD_EXTERNALMGT);
    }

    /**
     * See if there are external users.
     * This method is protected so that it can be mocked out in unit tests.
     * @return
     */
    protected boolean areExternalUsersPresent() throws SQLException
    {
        Connection connection = getDatabaseConnection();
        try
        {
            String sql = "select count(*) from " + convertToSchemaTableName("external_entities");
            PreparedStatement ps = connection.prepareStatement(sql);
            try
            {
                ResultSet rs = ps.executeQuery();
                // Count(*) will always return 1 row, because the administrator must login to do an import and so a row will be created automatically in
                // External entity for him.
                rs.next();
                return rs.getInt(1) > 1;
            }
            finally
            {
                ps.close();
            }
        }
        finally
        {
            connection.close();
        }
    }

    /**
     * Get the osuser.xml.
     * This method is protected so that it can be mocked out in unit tests.
     * @return input stream
     */
    protected InputStream getOSUserXmlStream()
    {
        return UpgradeTask_Build601.class.getResourceAsStream("/osuser.xml");
    }

    /**
     * Builds the url to the crowd service from the url specified in the &quot;crowd.properties&quot; file.
     *
     * Removes &quot;/services/&quot; or &quot;/services&quot; from the url path if it is present. This was necessary
     * to connect to crowd in the past because we used the SOAP API at this path, in the embedded
     * crowd world we only need the path to the crowd instance.
     */
    @VisibleForTesting
    static class CrowdServiceUrlBuilder
    {
        private String serviceUrlFromProperties;

        /**
         * Sets the service url found in the properties file to the specified string.
         *
         * @param propertiesUrl the service url found in the properties file.
         * @return this builder.
         */
        CrowdServiceUrlBuilder setPropertiesUrlTo(String propertiesUrl)
        {
            this.serviceUrlFromProperties = propertiesUrl;
            return this;
        }

        String build()
        {
            if (serviceUrlFromProperties.endsWith("/services/"))
            {
                return removeEnd(serviceUrlFromProperties, "/services/");
            }
            else
            {
                return removeEnd(serviceUrlFromProperties, "/services");
            }
        }
    }
}
