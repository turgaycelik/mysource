package com.atlassian.jira.functest.config.dashboard;

import org.dom4j.Document;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class to deal with loading and syncing JIRA configuration. 
 *
 * @since v4.2
 */
public abstract class ConfigGadgetSupport<T>
{
    private final Document document;

    protected ConfigGadgetSupport(final Document document)
    {
        this.document = document;
    }

    public boolean sync(final Collection<? extends T> gadgets)
    {
        final List<T> oldGadgets = loadAll();
        final Map<Long, T> gadgetMap = new HashMap<Long, T>();

        for (final T oldGadget : oldGadgets)
        {
            gadgetMap.put(getId(oldGadget), oldGadget);
        }

        boolean returnValue = false;
        for (final T gadget : gadgets)
        {
            final T oldGadget = gadgetMap.get(getId(gadget));
            if (oldGadget == null)
            {
                returnValue = create(gadget) | returnValue;
            }
            else
            {
                returnValue = update(oldGadget, gadget) | returnValue;
                gadgetMap.remove(getId(gadget));
            }
        }

        for (final T gadget : gadgetMap.values())
        {
            returnValue = delete(gadget) | returnValue;
        }

        return returnValue;
    }

    Document getDocument()
    {
        return document;
    }

    public abstract List<T> loadAll();
    public abstract boolean create(T object);
    public abstract boolean update(T oldObj, T newObj);
    public abstract boolean delete(T obj);
    public abstract Long getId(T obj);
}
