package com.atlassian.jira.functest.config.service;

import com.atlassian.jira.functest.config.ConfigCrudHelper;
import com.atlassian.jira.functest.config.ConfigException;
import com.atlassian.jira.functest.config.ConfigSequence;
import com.atlassian.jira.functest.config.ConfigXmlUtils;
import com.atlassian.jira.functest.config.ps.ConfigPropertySet;
import com.atlassian.jira.functest.config.ps.ConfigPropertySetManager;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.List;

/**
 * Class for CRUD operations related to the ConfigService.
 *
 * @since v4.1
 */
public class DefaultConfigServiceManager implements ConfigServiceManager
{
    private final ConfigCrudHelper<ConfigService> helper;

    public DefaultConfigServiceManager(final Document document, final ConfigPropertySetManager propertySetManager, final ConfigSequence configSeqence)
    {
        helper = new Helper(document, configSeqence, propertySetManager);
    }

    public List<ConfigService> loadServices()
    {
        return helper.load();
    }

    public boolean saveServices(List<ConfigService> newList)
    {
        return helper.save(newList);
    }

    private static class Helper extends ConfigCrudHelper<ConfigService>
    {
        private static final String ELEMENT_SERVICE_CONFIG = "ServiceConfig";
        private static final String ATTRIBUTE_ID = "id";
        private static final String ATTRIBUTE_TIME = "time";
        private static final String ATTRIBUTE_CLAZZ = "clazz";
        private static final String ATTRIBUTE_NAME = "name";

        private final ConfigPropertySetManager psm;

        private Helper(final Document document, final ConfigSequence configSequence, ConfigPropertySetManager psm)
        {
            super(document, configSequence, ELEMENT_SERVICE_CONFIG);
            this.psm = psm;
        }

        @Override
        protected ConfigService elementToObject(Element element)
        {
            //<ServiceConfig id="10000" time="60000" clazz="com.atlassian.jira.service.services.mail.MailQueueService" name="Mail Queue Service"/>
            Long id = ConfigXmlUtils.getLongValue(element, ATTRIBUTE_ID);
            if (id == null)
            {
                throw new ConfigException("Trying to read service without an ID.");
            }

            Long time = ConfigXmlUtils.getLongValue(element, ATTRIBUTE_TIME);
            String clazz = ConfigXmlUtils.getTextValue(element, ATTRIBUTE_CLAZZ);
            String name = ConfigXmlUtils.getTextValue(element, ATTRIBUTE_NAME);

            return new ConfigService(id, time, clazz, name, psm.loadPropertySet(ELEMENT_SERVICE_CONFIG, id));

        }

        @Override
        protected void updateObject(Element element, ConfigService updateObject, ConfigService oldObject)
        {
            setAttributes(element, updateObject);
            final ConfigPropertySet propertySet = updateObject.getPropertySet();
            if (propertySet != null)
            {
                psm.savePropertySet(propertySet.copyForEntity(ELEMENT_SERVICE_CONFIG, updateObject.getId()));
            }
            else if (oldObject.getPropertySet() != null)
            {
                psm.deletePropertySet(oldObject.getPropertySet());
            }
        }

        @Override
        protected void newObject(Element element, ConfigService newObject, Long newId)
        {
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_ID, newId);

            setAttributes(element, newObject);

            final ConfigPropertySet propertySet = newObject.getPropertySet();
            if (propertySet != null)
            {
                psm.savePropertySet(propertySet.copyForEntity(ELEMENT_SERVICE_CONFIG, newId));
            }
        }

        private void setAttributes(Element element, ConfigService newObject)
        {
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_TIME, newObject.getTimeout());
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_NAME, newObject.getName());
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_CLAZZ, newObject.getClazz());
        }

        @Override
        protected void deleteObject(Element element, ConfigService deleteObject)
        {
            psm.deletePropertySet(deleteObject.getPropertySet());
        }
    }
}
