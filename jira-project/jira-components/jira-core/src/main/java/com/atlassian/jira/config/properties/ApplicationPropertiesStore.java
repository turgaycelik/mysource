package com.atlassian.jira.config.properties;

import com.atlassian.jira.bc.admin.ApplicationPropertiesService;
import com.atlassian.jira.bc.admin.ApplicationProperty;
import com.atlassian.jira.bc.admin.ApplicationPropertyMetadata;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.util.concurrent.LazyReference;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.opensymphony.module.propertyset.InvalidPropertyTypeException;
import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This class is responsible for loading jira's configuration properties.
 *
 * @since v4.4
 */
public class ApplicationPropertiesStore
{
    private static final Logger log = Logger.getLogger(ApplicationPropertiesStore.class);

    public static final String JIRA_CONFIG_PROPERTIES = "jira-config.properties";

    private static final String METADATA_XML = "jpm.xml";

    /**
     * Map of property key to {@link com.atlassian.jira.bc.admin.ApplicationPropertyMetadata}
     */
    @ClusterSafe
    private static final LazyReference<LinkedHashMap<String, ApplicationPropertyMetadata>> METADATA_CACHE
            = new LazyReference<LinkedHashMap<String, ApplicationPropertyMetadata>>()
    {
        @Override
        protected LinkedHashMap<String, ApplicationPropertyMetadata> create() throws Exception
        {
            return new MetadataLoader().loadMetadata(METADATA_XML);
        }
    };

    private final PropertiesManager propertiesManager;

    private final JiraHome jiraHome;

    @ClusterSafe
    private final ResettableLazyReference<Map<String, String>> defaultWithOverlays = new ResettableLazyReference<Map<String, String>>()
    {
        protected Map<String, String> create() throws Exception
        {
            Properties properties = loadOverlays();
            // We want to turn the Properties object into an immutable HashMap for scalability.
            // (Properties is backed by a HashTable which is synchronised).
            Map<String, String> defaultPropertyMap = new HashMap<String, String>(properties.size());
            for (Map.Entry<String, ApplicationPropertyMetadata> metadata : METADATA_CACHE.get().entrySet())
            {
                defaultPropertyMap.put(metadata.getKey(), metadata.getValue().getDefaultValue());
            }
            for (Object keyObj : properties.keySet())
            {
                if (keyObj instanceof String)
                {
                    // this should always be the case because of the contract of Properties
                    String key = (String) keyObj;
                    defaultPropertyMap.put(key, properties.getProperty(key));
                }
            }

            return Collections.unmodifiableMap(defaultPropertyMap);
        }

        private Properties loadOverlays()
        {
            Properties overlays = new Properties();
            InputStream in = null;
            File overlayFile = null;
            try
            {
                overlayFile = new File(jiraHome.getLocalHomePath(), JIRA_CONFIG_PROPERTIES);
                if (overlayFile.exists())
                {
                    in = new FileInputStream(overlayFile);
                    overlays.load(in);
                }
            }
            catch (final IOException e)
            {
                log.warn("Could not load config properties from '" + overlayFile + "'.");
            }
            finally
            {
                IOUtils.closeQuietly(in);
            }

            return overlays;
        }
    };

    public ApplicationPropertiesStore(PropertiesManager propertiesManager, JiraHome jiraHome)
    {
        this.propertiesManager = propertiesManager;
        this.jiraHome = jiraHome;
    }

    public List<ApplicationProperty> getEditableApplicationProperties(ApplicationPropertiesService.EditPermissionLevel permissionLevel, String keyFilter)
    {
        ArrayList<ApplicationProperty> props = new ArrayList<ApplicationProperty>();
        for (ApplicationPropertyMetadata value : METADATA_CACHE.get().values())
        {
            // we do not currently support editing values that require a restart
            if (propertyIsAtLevel(value,permissionLevel) && !value.isRequiresRestart() && propertyIsInFilter(value, keyFilter))
            {
                props.add(toApplicationProperty(value));
            }
        }
        return props;
    }

    private boolean propertyIsInFilter(ApplicationPropertyMetadata value, String keyFilter)
    {
        if (StringUtils.isBlank(keyFilter))
        {
            return true;
        }
        return value.getKey().matches(keyFilter);
    }

    private boolean propertyIsAtLevel(ApplicationPropertyMetadata propertyMetadata, ApplicationPropertiesService.EditPermissionLevel permissionLevel)
    {
        switch(permissionLevel)
        {
            case SYSADMIN_ONLY:
                return propertyMetadata.isSysadminEditable() && !propertyMetadata.isAdminEditable();
            case SYSADMIN:
                return propertyMetadata.isSysadminEditable();
            case ADMIN:
                return propertyMetadata.isAdminEditable();
            default:
                return false;
        }
    }

    ApplicationProperty toApplicationProperty(ApplicationPropertyMetadata meta)
    {
        String key = meta.getKey();
        if (meta.getType().equalsIgnoreCase("boolean"))
        {
            return new ApplicationProperty(meta, getOption(key).toString());
        }
        else
        {
            return new ApplicationProperty(meta, getString(key));
        }
    }

    public Boolean getOption(String key)
    {
        final PropertySet propertySet = propertiesManager.getPropertySet();
        if (propertySet.exists(key)) // user has customized the value
        {
            try
            {
                return propertySet.getBoolean(key);
            }
            catch (final Exception e)
            {
                log.error("Exception getting option '" + key + "' from database. Using default");
            }
        }
        return getOverlayedOption(key);
    }

    private Boolean getOverlayedOption(String key)
    {
        return Boolean.valueOf(defaultWithOverlays.get().get(key));
    }

    public ApplicationProperty getApplicationPropertyFromKey(String key)
    {
        ApplicationPropertyMetadata applicationPropertyMetadata = METADATA_CACHE.get().get(key);
        if (applicationPropertyMetadata == null)
        {
            return null;
        }
        else
        {
            return toApplicationProperty(applicationPropertyMetadata);
        }
    }

    public ApplicationProperty setApplicationProperty(String key, String value)
    {
        ApplicationPropertyMetadata applicationPropertyMetadata = METADATA_CACHE.get().get(key);

        String type = applicationPropertyMetadata.getType();
        if ("boolean".equalsIgnoreCase(type))
        {
            setOption(key, Boolean.parseBoolean(value));
        }
        else
        {
            setString(key, value);
        }
        return getApplicationPropertyFromKey(key);
    }

    public void setString(String key, String value)
    {
        final PropertySet propertySet = propertiesManager.getPropertySet();
        if (value == null)
        {
            if (propertySet.exists(key))
            {
                propertySet.remove(key);
            }
        }
        else
        {
            propertySet.setString(key, value);
        }

    }

    public void setOption(String key, Boolean value)
    {
        propertiesManager.getPropertySet().setBoolean(key, value);
    }

    public String getTextFromDb(String key)
    {
        return propertiesManager.getPropertySet().getText(key);
    }

    public void setText(String key, String value)
    {
        final PropertySet propertySet = propertiesManager.getPropertySet();
        if (value == null)
        {
            if (propertySet.exists(key))
            {
                propertySet.remove(key);
            }
        }
        else
        {
            propertySet.setText(key, value);
        }
    }

    public String getStringFromDb(String key)
    {
        try
        {
            return propertiesManager.getPropertySet().getString(key);
        }
        catch (final InvalidPropertyTypeException e)
        {
            //do nothing.  This is a problem because velocity loops over all the properties, even the non-string ones.
            //Yes this is stupid.  It is because they use Apache Commons - enough said.
            return "";
        }
    }

    public boolean existsInDb(String key)
    {
        return propertiesManager.getPropertySet().exists(key);
    }

    public Collection<String> getKeysStoredInDb()
    {
        //noinspection unchecked
        return (Collection<String>) propertiesManager.getPropertySet().getKeys();
    }

    public Map<String, Object> getPropertiesAsMap()
    {
        final Map<String, String> defaultProperties = getDefaultsWithOverlays();
        final PropertySet propertySet = propertiesManager.getPropertySet();

        final Set<String> smooshedKeys = new HashSet<String>();
        smooshedKeys.addAll(getKeysStoredInDb());
        smooshedKeys.addAll(defaultProperties.keySet());

        final Map<String, Object> allProperties = new HashMap<String, Object>(smooshedKeys.size());
        for (String key : smooshedKeys)
        {
            Object value = null;
            try
            {
                value = propertySet.getAsActualType(key);
            }
            catch (PropertyException ignored)
            {
                // if a key cannot be found it will throw an exception.  Brilliant!
            }
            if (value == null)
            {
                value = defaultProperties.get(key);
            }
            allProperties.put(key, value);
        }
        return allProperties;
    }

    public void refreshDbProperties()
    {
        propertiesManager.refresh();
    }

    public void refresh()
    {
        defaultWithOverlays.reset();
        refreshDbProperties();
    }

    public Map<String, String> getDefaultsWithOverlays()
    {
        return defaultWithOverlays.get();
    }

    public Collection<String> getStringsWithPrefixFromDb(String prefix)
    {
        //noinspection unchecked
        return (Collection<String>) propertiesManager.getPropertySet().getKeys(prefix, PropertySet.STRING);
    }

    public String getString(String key)
    {
        String value = null;
        try
        {
            value = getStringFromDb(key);
        }
        catch (final Exception e)
        {
            log.warn("Exception getting property '" + key + "' from database. Using default");
        }
        if (value == null)
        {
            value = getOverlayedString(key);
        }
        return value;
    }

    public String getOverlayedString(String key)
    {
        return defaultWithOverlays.get().get(key);
    }
}
