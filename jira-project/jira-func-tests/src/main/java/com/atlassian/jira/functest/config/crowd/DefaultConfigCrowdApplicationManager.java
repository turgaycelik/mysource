package com.atlassian.jira.functest.config.crowd;

import com.atlassian.jira.functest.config.ConfigCrudHelper;
import com.atlassian.jira.functest.config.ConfigException;
import com.atlassian.jira.functest.config.ConfigSequence;
import com.atlassian.jira.functest.config.ConfigXmlUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.List;

/**
 * @since v4.3
 */
public class DefaultConfigCrowdApplicationManager implements ConfigCrowdApplicationManager
{
    private final ConfigCrudHelper<ConfigCrowdApplication> helper;

    public DefaultConfigCrowdApplicationManager(Document document, ConfigSequence sequence)
    {
        this.helper = new Helper(document, sequence);
    }

    @Override
    public List<ConfigCrowdApplication> loadApplications()
    {
        return helper.load();
    }

    @Override
    public boolean saveApplications(List<ConfigCrowdApplication> applications)
    {
        return helper.save(applications);
    }

    static class Helper extends ConfigCrudHelper<ConfigCrowdApplication>
    {
        private static final String ELEMENT_APPLICATION = "Application";

        private static final String ATTRIBUTE_ID = "id";
        private static final String ATTRIBUTE_NAME = "name";
        private static final String ATTRIBUTE_LOWER_NAME = "lowerName";
        private static final String ATTRIBUTE_ACTIVE = "active";
        private static final String ATTRIBUTE_APPLICATION_TYPE = "applicationType";

        Helper(final Document document, final ConfigSequence configSeqence)
        {
            super(document, configSeqence, ELEMENT_APPLICATION);
        }

        @Override
        protected ConfigCrowdApplication elementToObject(Element element)
        {
            Long id = ConfigXmlUtils.getLongValue(element, ATTRIBUTE_ID);
            if (id == null)
            {
                throw new ConfigException("Trying to read application without an ID.");
            }

            ConfigCrowdApplication currentApplication = new ConfigCrowdApplication();
            currentApplication.setId(id);
            currentApplication.setName(ConfigXmlUtils.getTextValue(element, ATTRIBUTE_NAME));
            currentApplication.setLowerName(ConfigXmlUtils.getTextValue(element, ATTRIBUTE_LOWER_NAME));
            currentApplication.setActive(getBoolean(element, ATTRIBUTE_ACTIVE));
            currentApplication.setApplicationType(ConfigXmlUtils.getTextValue(element, ATTRIBUTE_APPLICATION_TYPE));

            return currentApplication;
        }

        @Override
        protected void updateObject(Element element, ConfigCrowdApplication updateObject, ConfigCrowdApplication oldObject)
        {
            setAttributes(element, updateObject);
        }

        @Override
        protected void newObject(Element element, ConfigCrowdApplication newObject, Long newId)
        {
            setAttributes(element, newObject);
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_ID, newId);
        }

        @Override
        protected void deleteObject(Element element, ConfigCrowdApplication deleteObject)
        {
        }

        private void setAttributes(Element element, ConfigCrowdApplication newObject)
        {
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_NAME, newObject.getName());
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_LOWER_NAME, newObject.getLowerName());
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_ACTIVE, newObject.isActive() == null ? null : newObject.isActive() ? "1" : "0");
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_APPLICATION_TYPE, newObject.getApplicationType());
        }

        private static Boolean getBoolean(Element element, String attribute)
        {
            Integer value = ConfigXmlUtils.getIntegerValue(element, attribute);
            if (value == null)
            {
                return null;
            }
            else
            {
                return value == 1;
            }
        }
    }
}
