package com.atlassian.jira.functest.config.dashboard;

import com.atlassian.jira.functest.config.ConfigException;
import com.atlassian.jira.functest.config.ConfigXmlUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Default implementation of {@link ConfigGadgetManager}.
 *
 * @since v4.2
 */
public class DefaultConfigGadgetManager implements ConfigGadgetManager
{
    private final ConfigGadgetSupport<ConfigGadget> gadgetSupport;
    private final ConfigGadgetSupport<ConfigExternalGadget> externalSupport;

    public DefaultConfigGadgetManager(final Document document)
    {
        this(new GadgetHelper(document), new ExternalHelper(document));
    }

    public DefaultConfigGadgetManager(final ConfigGadgetSupport<ConfigGadget> gadgetSupport,
            final ConfigGadgetSupport<ConfigExternalGadget> externalSupport)
    {
        this.gadgetSupport = gadgetSupport;
        this.externalSupport = externalSupport;
    }

    public List<ConfigGadget> loadGadgets()
    {
        return gadgetSupport.loadAll();
    }

    public boolean saveGadgets(final Collection<? extends ConfigGadget> gadgets)
    {
        return gadgetSupport.sync(gadgets);
    }

    public List<ConfigExternalGadget> loadExternalGadgets()
    {
        return externalSupport.loadAll();
    }

    public boolean saveExternalGadgets(final Collection<? extends ConfigExternalGadget> gadgets)
    {
        return externalSupport.sync(gadgets);
    }

    public static class GadgetHelper extends ConfigGadgetSupport<ConfigGadget>
    {
        private static final String ELEMENT_GADGETS = "PortletConfiguration";

        public GadgetHelper(final Document document)
        {
            super(document);
        }

        @Override
        public List<ConfigGadget> loadAll()
        {
            final List<Element> configNodes = getGadetElements();
            final List<ConfigGadget> returnList = new ArrayList<ConfigGadget>(configNodes.size());

            for (Element configNode : configNodes)
            {
                final ConfigGadget configGadget = new ConfigGadget(configNode);
                if (configGadget.getId() == null)
                {
                    throw new ConfigException("Trying to load a gadget without any ID.");
                }
                returnList.add(configGadget);
            }
            return returnList;
        }

        @Override
        public boolean create(final ConfigGadget object)
        {
            throw new ConfigException("Trying create a new gadget. I was too lazy to implement this.");
        }

        @Override
        public boolean update(final ConfigGadget oldGadget, final ConfigGadget newGadget)
        {
            if (!oldGadget.equals(newGadget))
            {
                newGadget.save(findElementForGadget(oldGadget));
                return true;
            }
            else
            {
                return false;
            }
        }

        @Override
        public boolean delete(final ConfigGadget obj)
        {
            ConfigXmlUtils.removeElement(findElementForGadget(obj));
            for (final Element configElement : getConfigElements(obj))
            {
                ConfigXmlUtils.removeElement(configElement);
            }
            return true;
        }

        @Override
        public Long getId(final ConfigGadget obj)
        {
            return obj.getId();
        }

        private List<Element> getGadetElements()
        {
            return ConfigXmlUtils.getTopElementsByName(getDocument(), ELEMENT_GADGETS);
        }

        private Element findElementForGadget(final ConfigGadget gadget)
        {
            final Element element = ConfigXmlUtils.getElementByXpath(getDocument(), 
                    String.format("/entity-engine-xml/PortletConfiguration[@id='%d']", gadget.getId()));
            if (element == null)
            {
                throw new ConfigException("Could not find gadget with id '" + gadget.getId() + "'.");
            }
            return element;
        }

        private List<Element> getConfigElements(final ConfigGadget gadget)
        {
            return ConfigXmlUtils.getElementsByXpath(getDocument(),
                    String.format("/entity-engine-xml/GadgetUserPreference[@portletconfiguration='%d']", gadget.getId()));
        }
    }

    public static class ExternalHelper extends ConfigGadgetSupport<ConfigExternalGadget>
    {
        private static final String ELEMENT_EXTERNAL_GADGET = "ExternalGadget";

        public ExternalHelper(final Document document)
        {
            super(document);
        }

        @Override
        public List<ConfigExternalGadget> loadAll()
        {
            final List<Element> elements = getElements();
            final List<ConfigExternalGadget> gadgets = new ArrayList<ConfigExternalGadget>(elements.size());
            for (Element element : elements)
            {
                ConfigExternalGadget gadget = new ConfigExternalGadget(element);
                if (gadget.getId() == null)
                {
                    throw new ConfigException("Trying to load a external gadget without any ID.");
                }
                gadgets.add(gadget);
            }
            return gadgets;
        }

        @Override
        public boolean create(final ConfigExternalGadget object)
        {
            throw new ConfigException("Trying create a new external gadget. I was too lazy to implement this.");
        }

        @Override
        public boolean update(final ConfigExternalGadget oldObj, final ConfigExternalGadget newObj)
        {
            if (!oldObj.equals(newObj))
            {
                newObj.save(findElementForGadget(oldObj));
                return true;
            }
            else
            {
                return false;
            }
        }

        @Override
        public boolean delete(final ConfigExternalGadget obj)
        {
            ConfigXmlUtils.removeElement(findElementForGadget(obj));
            return true;
        }

        @Override
        public Long getId(final ConfigExternalGadget obj)
        {
            return obj.getId();
        }

        private List<Element> getElements()
        {
            return ConfigXmlUtils.getTopElementsByName(getDocument(), ELEMENT_EXTERNAL_GADGET);
        }

        private Element findElementForGadget(final ConfigExternalGadget gadget)
        {
            Element element = ConfigXmlUtils.getElementByXpath(getDocument(),
                    String.format("/entity-engine-xml/ExternalGadget[@id='%d']", gadget.getId()));
            if (element == null)
            {
                throw new ConfigException("Could not find external gadget with id '" + gadget.getId() + "'.");
            }
            return element;
        }
    }
}
