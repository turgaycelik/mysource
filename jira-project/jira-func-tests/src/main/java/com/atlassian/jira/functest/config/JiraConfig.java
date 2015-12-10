package com.atlassian.jira.functest.config;

import com.atlassian.jira.functest.config.crowd.ConfigCrowdApplication;
import com.atlassian.jira.functest.config.crowd.ConfigCrowdApplicationManager;
import com.atlassian.jira.functest.config.crowd.DefaultConfigCrowdApplicationManager;
import com.atlassian.jira.functest.config.dashboard.ConfigDashboard;
import com.atlassian.jira.functest.config.dashboard.ConfigDashboardManager;
import com.atlassian.jira.functest.config.dashboard.ConfigExternalGadget;
import com.atlassian.jira.functest.config.dashboard.ConfigGadgetManager;
import com.atlassian.jira.functest.config.dashboard.DefaultConfigDashboardManager;
import com.atlassian.jira.functest.config.dashboard.DefaultConfigGadgetManager;
import com.atlassian.jira.functest.config.mail.ConfigMailServer;
import com.atlassian.jira.functest.config.mail.ConfigMailServerManager;
import com.atlassian.jira.functest.config.mail.DefaultConfigMailServerManager;
import com.atlassian.jira.functest.config.ps.ConfigPropertySet;
import com.atlassian.jira.functest.config.ps.ConfigPropertySetManager;
import com.atlassian.jira.functest.config.ps.DefaultConfigPropertySetManager;
import com.atlassian.jira.functest.config.service.ConfigService;
import com.atlassian.jira.functest.config.service.ConfigServiceManager;
import com.atlassian.jira.functest.config.service.DefaultConfigServiceManager;
import com.atlassian.jira.functest.config.sharing.DefaultConfigSharedEntityCleaner;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * An object that represents JIRA's configuration. Mainly kept around for efficiency.
 *
 * @since v4.0
 */
public class JiraConfig
{
    private static final String APP_PROPS_NAME = "jira.properties";
    private static final long APP_PROPS_ID = 1;
    private static final String ROOT_ELEMENT = "entity-engine-xml";

    private static final String KEY_BUILD_NUMBER = "jira.version.patched";

    private final Document document;
    private final File file;
    private final ConfigMailServerManager mailServerManager;
    private final ConfigPropertySetManager propertySetManager;
    private final ConfigServiceManager serviceManager;
    private final ConfigSequence configSequence;
    private final ConfigDashboardManager dashboardManager;
    private final ConfigGadgetManager gadgetManager;
    private final ConfigAdminLocator adminLocator;
    private final ConfigCrowdApplicationManager crowdApplicationManager;

    private ConfigPropertySet applicationProperties;
    private List<ConfigService> services;
    private List<ConfigMailServer> mailServers;
    private List<ConfigDashboard> dashboards;
    private List<ConfigExternalGadget> externalGadgets;
    private Set<String> admins;
    private List<ConfigCrowdApplication> crowdApplications;
    private boolean dirty;

    public JiraConfig(Document document, File file)
    {
        if (!isJiraXml(document))
        {
            throw new IllegalArgumentException("The passed document does not seem to contain JIRA backup XML data.");
        }

        this.document = document;
        this.file = file;
        this.configSequence = new DefaultConfigSequence(document);
        this.mailServerManager = new DefaultConfigMailServerManager(document, configSequence);
        this.propertySetManager = new DefaultConfigPropertySetManager(document, configSequence);
        this.serviceManager = new DefaultConfigServiceManager(document, propertySetManager, configSequence);
        this.gadgetManager = new DefaultConfigGadgetManager(document);
        this.dashboardManager = new DefaultConfigDashboardManager(document, gadgetManager,
                new DefaultConfigSharedEntityCleaner(document));
        this.adminLocator = new ConfigAdminLocator(document);
        this.crowdApplicationManager = new DefaultConfigCrowdApplicationManager(document, configSequence);
    }

    public JiraConfig(Document document, File file, ConfigSequence configSequence, ConfigMailServerManager configMailServerManager,
            ConfigPropertySetManager configPropertySetManager, ConfigServiceManager configServiceManager,
            ConfigDashboardManager dashboardManager, ConfigGadgetManager gadgetManager, ConfigAdminLocator adminLocator,
            ConfigCrowdApplicationManager crowdApplicationManager)
    {
        if (!isJiraXml(document))
        {
            throw new IllegalArgumentException("The passed document does not seem to contain JIRA backup XML data.");
        }

        this.document = document;
        this.file = file;
        this.configSequence = configSequence;
        this.mailServerManager = configMailServerManager;
        this.propertySetManager = configPropertySetManager;
        this.serviceManager = configServiceManager;
        this.gadgetManager = gadgetManager;
        this.dashboardManager = dashboardManager;
        this.adminLocator = adminLocator;
        this.crowdApplicationManager = crowdApplicationManager;
    }

    public JiraConfig()
    {
        this(createEmptyDocument(), null);
    }

    public long getBuildNumber()
    {
        return getApplicationProperties().getLongPropertyDefault(KEY_BUILD_NUMBER, 0L);
    }

    public JiraConfig setApplicationProperties(final ConfigPropertySet applicationProperties)
    {
        this.applicationProperties = applicationProperties;
        return this;
    }

    public Set<String> getSystemAdmins()
    {
        if (admins == null)
        {
            admins = adminLocator.locateSystemAdmins();
        }
        return admins;
    }

    public ConfigPropertySet getApplicationProperties()
    {
        if (applicationProperties == null)
        {
            applicationProperties = propertySetManager.loadPropertySet(APP_PROPS_NAME, APP_PROPS_ID);
        }
        return applicationProperties;
    }

    public List<ConfigService> getServices()
    {
        if (services == null)
        {
            services = copy(serviceManager.loadServices());
        }

        return services;
    }

    public JiraConfig setServices(final List<ConfigService> services)
    {
        this.services = services;
        return this;
    }

    public List<ConfigMailServer> getMailServers()
    {
        if (mailServers == null)
        {
            mailServers = copy(mailServerManager.loadServers());
        }
        return mailServers;
    }

    public JiraConfig setMailServers(final List<ConfigMailServer> mailServers)
    {
        this.mailServers = mailServers;
        return this;
    }

    public List<ConfigDashboard> getDashboards()
    {
        if (dashboards == null)
        {
            dashboards = copy(dashboardManager.loadDashboards());
        }
        return dashboards;
    }

    public JiraConfig setDashboards(final List<ConfigDashboard> dashboards)
    {
        this.dashboards = dashboards;
        return this;
    }

    public List<ConfigExternalGadget> getExternalGadgets()
    {
        if (externalGadgets == null)
        {
            externalGadgets = copy(gadgetManager.loadExternalGadgets());
        }
        return externalGadgets;
    }

    public JiraConfig setExternalGadgets(final List<ConfigExternalGadget> externalGadgets)
    {
        this.externalGadgets = externalGadgets;
        return this;
    }

    public List<ConfigCrowdApplication> getCrowdApplications()
    {
        if (crowdApplications == null)
        {
            crowdApplications = copy(crowdApplicationManager.loadApplications());
        }
        return crowdApplications;
    }

    public JiraConfig setCrowdApplications(List<ConfigCrowdApplication> crowdApplications)
    {
        this.crowdApplications = crowdApplications;
        return this;
    }

    public void markDirty()
    {
        this.dirty = true;
    }

    public boolean save()
    {
        boolean returnValue = dirty;

        if (applicationProperties != null)
        {
            returnValue = propertySetManager.savePropertySet(applicationProperties.copyForEntity(APP_PROPS_NAME, APP_PROPS_ID)) || returnValue;
        }

        if (services != null)
        {
            returnValue = serviceManager.saveServices(services) || returnValue;
        }

        if (mailServers != null)
        {
            returnValue = mailServerManager.saveServers(mailServers) || returnValue;
        }

        if (dashboards != null)
        {
            returnValue = dashboardManager.saveDashboards(dashboards) || returnValue;
        }

        if (externalGadgets != null)
        {
            returnValue = gadgetManager.saveExternalGadgets(externalGadgets) || returnValue;
        }

        if (crowdApplications != null)
        {
            returnValue = crowdApplicationManager.saveApplications(crowdApplications) || returnValue;
        }

        return configSequence.save() || returnValue;
    }

    public Document getDocument()
    {
        return document;
    }

    public File getFile()
    {
        return file;
    }

    public static boolean isJiraXml(Document document)
    {
        if (document == null)
        {
            return false;
        }
        final Element rootElem = document.getRootElement();
        return rootElem != null && ROOT_ELEMENT.equalsIgnoreCase(rootElem.getName());
    }

    private static Document createEmptyDocument()
    {
        final DocumentFactory instance = DocumentFactory.getInstance();
        final Document rootDoc = instance.createDocument();
        rootDoc.setRootElement(instance.createElement(ROOT_ELEMENT));
        return rootDoc;
    }

    private static <T> List<T> copy(Collection<? extends T> source)
    {
        if (source == null)
        {
            return new ArrayList<T>();
        }
        else
        {
            return new ArrayList<T>(source);
        }
    }
}
