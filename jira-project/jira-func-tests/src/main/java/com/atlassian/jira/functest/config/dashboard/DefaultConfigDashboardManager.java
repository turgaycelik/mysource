package com.atlassian.jira.functest.config.dashboard;

import com.atlassian.jira.functest.config.ConfigException;
import com.atlassian.jira.functest.config.ConfigXmlUtils;
import com.atlassian.jira.functest.config.sharing.ConfigSharedEntityCleaner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link DefaultConfigDashboardManager}.
 *
 * @since v4.2
 */
public class DefaultConfigDashboardManager implements ConfigDashboardManager
{
    private static final String ELEMENT_DASHBOARD = "PortalPage";

    private final ConfigSharedEntityCleaner cleaner;
    private final ConfigGadgetManager gadgetManager;
    private final Document document;

    public DefaultConfigDashboardManager(final Document document, final ConfigGadgetManager gadgetManager, final ConfigSharedEntityCleaner cleaner)
    {
        this.document = document;
        this.gadgetManager = gadgetManager;
        this.cleaner = cleaner;
    }

    public List<ConfigDashboard> loadDashboards()
    {
        final List<Element> elements = getDashboardElements();
        final List<ConfigDashboard> dashboards = new ArrayList<ConfigDashboard>(elements.size());
        for (Element element : elements)
        {
            final ConfigDashboard dashboard = new ConfigDashboard(element);
            if (dashboard.getId() == null)
            {
                throw new ConfigException("Trying to read in dashboard with no ID.");
            }
            dashboards.add(dashboard);
        }

        final List<ConfigGadget> configGadgetList = gadgetManager.loadGadgets();
        final Multimap<Long, ConfigGadget> map = ArrayListMultimap.create();
        for (ConfigGadget configGadget : configGadgetList)
        {
            map.put(configGadget.getDashboardId(), configGadget);
        }

        for (ConfigDashboard dashboard : dashboards)
        {
            dashboard.setGadgets(map.get(dashboard.getId()));
        }

        return dashboards;
    }

    public boolean saveDashboards(final Collection<? extends ConfigDashboard> dashboards)
    {
        final List<Element> oldDashboards = getDashboardElements();
        final Map<Long, Element> dashboardMap = new HashMap<Long, Element>();

        for (final Element oldDashboard : oldDashboards)
        {
            final Long id = ConfigDashboard.parseId(oldDashboard);
            if (id == null)
            {
                throw new ConfigException("Trying to read in dashboard with no ID.");
            }
            dashboardMap.put(id, oldDashboard);
        }

        List<ConfigGadget> gadgets = new ArrayList<ConfigGadget>();
        boolean returnValue = false;
        for (final ConfigDashboard dashboard : dashboards)
        {
            final Element oldDashboard = dashboardMap.get(dashboard.getId());
            if (oldDashboard == null)
            {
                throw new ConfigException("Trying to add new dashboard. I was too lazy to implement this.");
            }
            else
            {
                returnValue = dashboard.save(oldDashboard) | returnValue;
                dashboardMap.remove(dashboard.getId());
            }
            for (final ConfigGadget gadget : dashboard.getGadgets())
            {
                gadget.setDashboard(dashboard.getId());
                gadgets.add(gadget);
            }
        }

        returnValue = gadgetManager.saveGadgets(gadgets) | returnValue;

        for (final Element oldDashboard : dashboardMap.values())
        {
            deleteDashboard(new ConfigDashboard(oldDashboard));
            returnValue = true;
        }

        return returnValue;
    }

    private List<Element> getDashboardElements()
    {
        return ConfigXmlUtils.getTopElementsByName(document, ELEMENT_DASHBOARD);
    }

    private void deleteDashboard(final ConfigDashboard dashboard)
    {
        cleaner.clean(dashboard);
        ConfigXmlUtils.removeElement(findElementForDashboard(dashboard.getId()));
    }

    private Element findElementForDashboard(final long id)
    {
        Element element = ConfigXmlUtils.getElementByXpath(document, String.format("/entity-engine-xml/PortalPage[@id='%d']", id));
        if (element == null)
        {
            throw new ConfigException("Could not find dashboard with id '" + id + "'.");
        }
        return element;
    }
}
