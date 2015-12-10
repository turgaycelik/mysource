package com.atlassian.jira.functest.config;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Text;

import java.util.List;
import java.util.ListIterator;

/**
 * Some helper methods for reading in JIRA XML manipulation.
 */
public class ConfigXmlUtils
{
    private ConfigXmlUtils()
    {
    }

    /**
     * Add a new child element to the passed parent. The element will be place after other elements of the same name, or
     * if no other elements exist, as the first child of the parent.
     *
     * @param parent the node to add the element to.
     * @param elementName the name of the element to add.
     * @return the added element.
     */
    public static Element createNewElement(final Element parent, String elementName)
    {
        @SuppressWarnings ({ "unchecked" }) final List<Node> list = parent.content();
        int lastPos = -1;

        for (ListIterator<Node> it = list.listIterator(); it.hasNext();)
        {
            Node current = it.next();
            if (current instanceof Element)
            {
                if (current.getName().equals(elementName))
                {
                    lastPos = it.previousIndex();
                }
            }
        }

        final DocumentFactory factory = DocumentFactory.getInstance();
        list.add(lastPos + 1, factory.createText("\n    "));

        final Element element = factory.createElement(elementName);
        list.add(lastPos + 2 , element);
        return element;
    }

    /**
     * Sets the attribute of the passed element.
     *
     * @param element the element to set the attribute on.
     * @param attributeName the name of the attribute to set.
     * @param attributeValue the value of the attribute.
     */
    public static void setAttribute(Element element, String attributeName, Object attributeValue)
    {
        setAttribute(element, attributeName, asString(attributeValue));
    }

    /**
     * Sets the attribute of the passed element. The attribute will be added if necessary. In JIRA XML backups, an
     * attribute can be stored as either an XML attribute or as an element if necessary.
     *
     * @param element the element to set the attribute on.
     * @param attributeName the name of the attribute to set.
     * @param attributeValue the value of the attribute.
     */
    private static void setAttribute(Element element, String attributeName, String attributeValue)
    {
        if (attributeValue == null)
        {
            removeAttribute(element, attributeName);
            return;
        }

        boolean shouldElement = attributeValue.indexOf('\n') >= 0 || attributeValue.indexOf('\r') >= 0;

        if (shouldElement)
        {
            //Remove the attribute if it currently exists.
            final Attribute attrib = element.attribute(attributeName);
            if (attrib != null)
            {
                element.remove(attrib);
            }

            //Create a new sub-element.
            Element sub = element.element(attributeName);
            if (sub == null)
            {
                sub = element.addElement(attributeName);
            }
            sub.setText(attributeValue);
        }
        else
        {
            //Remove the sub-element if it exists.
            Element sub = element.element(attributeName);
            if (sub != null)
            {
                element.remove(sub);
            }

            //Reset the attribute if necessary.
            Attribute attribute = element.attribute(attributeName);
            if (attribute == null)
            {
                element.addAttribute(attributeName, attributeValue);
            }
            else
            {
                attribute.setValue(attributeValue);
            }
        }
    }

    /**
     * Remove the attribute from the passed element. In JIRA XML backups, an
     * attribute can be stored as either an XML attribute or as an element if necessary.
     *
     * @param element the element to remove the attribute from.
     * @param attributeName the name of the attribute to remove.
     */
    public static void removeAttribute(final Element element, final String attributeName)
    {
        final Attribute attrib = element.attribute(attributeName);
        if (attrib != null)
        {
            element.remove(attrib);
        }

        final Element sub = element.element(attributeName);
        if (sub != null)
        {
            element.remove(sub);
        }
    }

    /**
     * Get the text value of the passed attribute off the passed element. In JIRA XML backups, an
     * attribute can be stored as either an XML attribute or as an element if necessary.
     *
     * @param element the element to get the attribute off.
     * @param attribute the name of the attribute to find.
     *
     * @return the text value of the attribute.
     */
    public static String getTextValue(Element element, String attribute)
    {
        String attribValue = element.attributeValue(attribute);
        if (attribValue != null)
        {
            return attribValue;
        }
        else
        {
            final Element valueElement = element.element(attribute);
            if (valueElement != null)
            {
                return valueElement.getText();
            }
        }
        return null;
    }

    /**
     * Get the long value of the passed attribute off the passed element. In JIRA XML backups, an
     * attribute can be stored as either an XML attribute or as an element if necessary.
     *
     * @param element the element to get the attribute off.
     * @param attribute the name of the attribute to find.
     *
     * @return the long value of the attribute. Null will be returned if the attribute does not represent a long.
     */
    public static Long getLongValue(Element element, String attribute)
    {
        String text = getTextValue(element, attribute);
        if (text != null)
        {
            try
            {
                return new Long(text);
            }
            catch (NumberFormatException e)
            {
                //fall through
            }
        }
        return null;
    }

    /**
     * Get the integer value of the passed attribute off the passed element. In JIRA XML backups, an
     * attribute can be stored as either an XML attribute or as an element if necessary.
     *
     * @param element the element to get the attribute off.
     * @param attribute the name of the attribute to find.
     *
     * @return the integer value of the attribute. Null will be returned if the attribute does not represent a long.
     */
    public static Integer getIntegerValue(Element element, String attribute)
    {
        String text = getTextValue(element, attribute);
        if (text != null)
        {
            try
            {
                return new Integer(text);
            }
            catch (NumberFormatException e)
            {
                //fall through
            }
        }
        return null;
    }

    /**
     * Remove the passed element from dom tree.
     *
     * @param element the element to remove.
     * @return true if the element was remove, false otherwise.
     */
    public static boolean removeElement(Element element)
    {
        final Element parent = element.getParent();
        if (parent == null)
        {
            return false;
        }

        @SuppressWarnings ({ "unchecked" }) final List<Node> list = parent.content();
        int indexOf = list.indexOf(element);
        if (indexOf >= 0)
        {
            if (indexOf > 0)
            {
                Node textNode = list.get(indexOf - 1);
                if (textNode instanceof Text && StringUtils.isBlank(textNode.getText()))
                {
                    indexOf = indexOf - 1;
                    list.remove(indexOf);
                }
            }
            list.remove(indexOf);
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Find all the elements returned by the passed xpath. Will throw an {@link IllegalArgumentException} if the passed
     * xpath returns anything by {@link Element}s.
     *
     * @param document the document to run the xpath on.
     * @param xpath the xpath to run.
     * @return the elements returned from the xpath.
     * @throws IllegalArgumentException if the xpath returns non-element nodes.
     */
    @SuppressWarnings ({ "unchecked" })
    public static List<Element> getElementsByXpath(final Document document, final String xpath)
    {
        final List<?> list = document.selectNodes(xpath);
        for (Object o : list)
        {
            if (!(o instanceof Element))
            {
                throw new IllegalArgumentException(String.format("xpath '%s' returned a '%s' which is not an Element.",
                        xpath, o.getClass().getSimpleName()));
            }
        }
        return (List<Element>) list;
    }

    /**
     * Return the element matched by the passed XPath. If the xpath matches more than one Node or matches a non-element,
     * a {@link IllegalArgumentException} will be thrown. A null value will be returned if the xpath matches nothing.
     *
     * @param document the document to run the xpath over.
     * @param xpath the xpath to run.
     * @return the element matched or null if the xpath matched nothing.
     */
    public static Element getElementByXpath(final Document document, final String xpath)
    {
        @SuppressWarnings ({ "unchecked" })
        final List<Node> nodes = document.selectNodes(xpath);
        if (nodes == null || nodes.isEmpty())
        {
            return null;
        }
        else if (nodes.size() == 1)
        {
            Node node = nodes.get(0);
            if (node instanceof Element)
            {
                return (Element) node;
            }
            else
            {
                throw new IllegalArgumentException(String.format("xpath '%s' returned a '%s' which is not an Element.",
                        xpath, node.getClass().getSimpleName()));
            }
        }
        else
        {
            throw new IllegalArgumentException(String.format("xpath '%s' returned more than one Node.", xpath));
        }
    }

    /**
     * Return all the elements directly under the root of the passed document with the passed name. Returns an
     * empty list if no such elements exist.
     *
     * @param document the document to look through.
     * @param name the name of the elements to return.
     * @return the list of elements with the given name located under the root of the passed document. Returns an
     * empty list if no such elements exist.
     */
    @SuppressWarnings ({ "unchecked" })
    public static List<Element> getTopElementsByName(final Document document, final String name)
    {
        return document.getRootElement().elements(name);
    }

    private static String asString(Object object)
    {
        if (object == null)
        {
            return null;
        }
        else
        {
            return String.valueOf(object);
        }
    }
}
