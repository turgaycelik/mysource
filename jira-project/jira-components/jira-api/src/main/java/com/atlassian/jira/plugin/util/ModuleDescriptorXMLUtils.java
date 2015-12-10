package com.atlassian.jira.plugin.util;

import org.apache.log4j.Logger;
import org.dom4j.Element;

public class ModuleDescriptorXMLUtils
{
    private static final Logger log = Logger.getLogger(ModuleDescriptorXMLUtils.class);
    public static final int DEFAULT_ORDER = Integer.MAX_VALUE; // if we don't specify an order, then it should be the highest order

    private ModuleDescriptorXMLUtils()
    {
    }

    /**
     * Given an XML Element, find a child element called 'order', and get the value of it.  eg:
     * <pre><code>
     * &lt;project-tabpanel ...&gt;
     *  &lt;order&gt;10&lt;/order&gt;
     * &lt;/project-tabpanel&gt;
     * </code></pre>
     * @param element   The parent element (in this case 'project-tabpanel')
     * @return  The order.  In the example above, this would be '10'.  If no order is specified in the XML, then
     * {@link #DEFAULT_ORDER} is returned. 
     */
    public static int getOrder(Element element)
    {
        int order = DEFAULT_ORDER;
        if (element.element("order") != null)
        {
            try
            {
                order = Integer.parseInt(element.element("order").getTextTrim());
            }
            catch (NumberFormatException e)
            {
                log.warn("Invalid order specified: " + element.element("order").getTextTrim() + ". Should be an integer.", e);
            }
        }
        return order;
    }
}
