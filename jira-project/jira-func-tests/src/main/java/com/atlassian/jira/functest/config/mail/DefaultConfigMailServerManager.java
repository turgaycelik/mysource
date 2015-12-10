package com.atlassian.jira.functest.config.mail;

import com.atlassian.jira.functest.config.ConfigCrudHelper;
import com.atlassian.jira.functest.config.ConfigException;
import com.atlassian.jira.functest.config.ConfigSequence;
import com.atlassian.jira.functest.config.ConfigXmlUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.List;

/**
* A class related to the CRUD operations for the {@link ConfigMailServer}.
*
* @since v4.1
*/
public class DefaultConfigMailServerManager implements ConfigMailServerManager
{
    private final ConfigCrudHelper<ConfigMailServer> helper;

    public DefaultConfigMailServerManager(final Document document, final ConfigSequence sequence)
    {
        helper = new Helper(document, sequence);
    }

    public List<ConfigMailServer> loadServers()
    {
        return helper.load();
    }

    public boolean saveServers(List<ConfigMailServer> newList)
    {
        return helper.save(newList);
    }

    private static class Helper extends ConfigCrudHelper<ConfigMailServer>
    {
        private static final String ATTRIBUTE_ID = "id";
        private static final String ATTRIBUTE_NAME = "name";
        private static final String ATTRIBUTE_DESCRIPTION = "description";
        private static final String ATTRIBUTE_FROM = "from";
        private static final String ATTRIBUTE_PREFIX = "prefix";
        private static final String ATTRIBUTE_SMTP_PORT = "smtpPort";
        private static final String ATTRIBUTE_TYPE = "type";
        private static final String ATTRIBUTE_SERVERNAME = "servername";
        private static final String ATTRIBUTE_JNDILOCATION = "jndilocation";
        private static final String ATTRIBUTE_USERNAME = "username";
        private static final String ATTRIBUTE_PASSWORD = "password";

        private static final String ELEMENT_NAME = "MailServer";

        private Helper(final Document document, final ConfigSequence configSeqence)
        {
            super(document, configSeqence, ELEMENT_NAME);
        }

        @Override
        protected ConfigMailServer elementToObject(Element element)
        {
            Long id = ConfigXmlUtils.getLongValue(element, ATTRIBUTE_ID);
            if (id == null)
            {
                throw new ConfigException("Trying to read mail server without an ID.");
            }

            ConfigMailServer currentServer = new ConfigMailServer();
            currentServer.setId(id);
            currentServer.setName(ConfigXmlUtils.getTextValue(element, ATTRIBUTE_NAME));
            currentServer.setDescription(ConfigXmlUtils.getTextValue(element, ATTRIBUTE_DESCRIPTION));
            currentServer.setFrom(ConfigXmlUtils.getTextValue(element, ATTRIBUTE_FROM));
            currentServer.setPrefix(ConfigXmlUtils.getTextValue(element, ATTRIBUTE_PREFIX));
            currentServer.setPort(ConfigXmlUtils.getTextValue(element, ATTRIBUTE_SMTP_PORT));
            currentServer.setType(ConfigXmlUtils.getTextValue(element, ATTRIBUTE_TYPE));
            currentServer.setServerName(ConfigXmlUtils.getTextValue(element, ATTRIBUTE_SERVERNAME));
            currentServer.setJndiLocation(ConfigXmlUtils.getTextValue(element, ATTRIBUTE_JNDILOCATION));
            currentServer.setUserName(ConfigXmlUtils.getTextValue(element, ATTRIBUTE_USERNAME));
            currentServer.setPassword(ConfigXmlUtils.getTextValue(element, ATTRIBUTE_PASSWORD));

            return currentServer;
        }

        @Override
        protected void updateObject(Element element, ConfigMailServer updateObject, ConfigMailServer oldObject)
        {
            throw new ConfigException("Trying to modify old server configuration.");
        }

        @Override
        protected void newObject(Element element, ConfigMailServer newObject, Long newId)
        {
            throw new ConfigException("Trying to add new mail server to configuration.");
        }

        @Override
        protected void deleteObject(Element element, ConfigMailServer deleteObject)
        {
        }
    }
}
