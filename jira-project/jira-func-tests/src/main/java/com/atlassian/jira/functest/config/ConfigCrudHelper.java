package com.atlassian.jira.functest.config;

import org.dom4j.Document;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class that can help implementing CRUD operations on the XML backup.
 *
 * @since v4.3
 */
public abstract class ConfigCrudHelper<T extends ConfigObjectWithId>
{
    private final Document document;
    private final ConfigSequence configSeqence;
    private final String elementName;

    public ConfigCrudHelper(final Document document, final ConfigSequence configSeqence, final String elementName)
    {
        this.document = document;
        this.configSeqence = configSeqence;
        this.elementName = elementName;
    }

    public List<T> load()
    {
        final List<Element> configNodes = ConfigXmlUtils.getTopElementsByName(document, elementName);
        final List<T> returnList = new ArrayList<T>(configNodes.size());

        for (Element element : configNodes)
        {
            T t = elementToObject(element);
            if (t.getId() == null)
            {
                throw new ConfigException("Trying to read config object with no ID.");
            }
            returnList.add(t);
        }
        return returnList;

    }

    public boolean save(List<T> newList)
    {
        final List<T> oldList = load();
        final Map<Long, T> oldMap = new HashMap<Long, T>();
        for (T old : oldList)
        {
            oldMap.put(old.getId(), old);
        }

        boolean returnValue = false;
        for (T newObject : newList)
        {
            //newService.getId can be null, but we don't care in this case because oldService will be null.
            final T oldObject = oldMap.get(newObject.getId());
            if (oldObject == null)
            {
                final Long newId = configSeqence.getNextId(elementName);
                final Element element = ConfigXmlUtils.createNewElement(document.getRootElement(), elementName);
                newObject(element, newObject, newId);
                returnValue = true;
            }
            else
            {
                if (!newObject.equals(oldObject))
                {
                    Element element = findObjectElement(oldObject);
                    updateObject(element, newObject, oldObject);
                    returnValue = true;
                }
                oldMap.remove(newObject.getId());
            }
        }

        for (T deleteObject : oldMap.values())
        {
            Element element = findObjectElement(deleteObject);

            deleteObject(element, deleteObject);
            ConfigXmlUtils.removeElement(element);
            returnValue = true;
        }

        return returnValue;
    }

    protected abstract T elementToObject(Element element);
    protected abstract void updateObject(Element element, T updateObject, T oldObject);
    protected abstract void newObject(Element element, T newObject, Long newId);
    protected abstract void deleteObject(Element element, T deleteObject);

    private Element findObjectElement(final T object)
    {
        Element element = ConfigXmlUtils.getElementByXpath(document,
                String.format("/*/%s[@id='%d']", elementName, object.getId()));
        if (element == null)
        {
            throw new ConfigException("Could not find element for object with id '" + object.getId() + "'");
        }
        return element;
    }
}
