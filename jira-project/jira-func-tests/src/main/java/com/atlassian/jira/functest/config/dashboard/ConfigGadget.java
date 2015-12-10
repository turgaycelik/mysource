package com.atlassian.jira.functest.config.dashboard;

import com.atlassian.jira.functest.config.ConfigXmlUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.dom4j.Element;

/**
 * Represents a gadet or portal from a JIRA backup file.
 *
 * Example:
 * <pre>
 *     &lt;PortletConfiguration id="10000" portalpage="10000" columnNumber="0" position="0" gadgetXml="rest/gadgets/1.0/g/com.atlassian.jira.gadgets:introduction-gadget/gadgets/introduction-gadget.xml"/&gt;
 * </pre>
 *
 * @since v4.2
 */
public class ConfigGadget
{
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_PORTALPAGE = "portalpage";
    private static final String ATTRIBUTE_COLUMN_NUMBER = "columnNumber";
    private static final String ATTRIBUTE_POSITION = "position";
    private static final String ATTRIBUTE_GADGET_XML = "gadgetXml";
    private static final String ATTRIBUTE_PORTLET_ID = "portletId";

    private Long id;
    private Long dashboardId;
    private Integer columnNumber;
    private Integer rowNumber;
    private String gadgetXml;
    private String portletId;

    public ConfigGadget()
    {
    }

    public ConfigGadget(Element element)
    {
        //New ->
        //<PortletConfiguration id="10003" portalpage="10000" columnNumber="1" position="1"
        //   gadgetXml="rest/gadgets/1.0/g/com.atlassian.jira.gadgets:assigned-to-me-gadget/gadgets/assigned-to-me-gadget.xml"/>

        //Old ->
        //<PortletConfiguration id="10000" portalpage="10000" portletId="com.atlassian.jira.plugin.system.portlets:introduction"
        //   columnNumber="0" position="0"/>        

        id = ConfigXmlUtils.getLongValue(element, ATTRIBUTE_ID);
        dashboardId = ConfigXmlUtils.getLongValue(element, ATTRIBUTE_PORTALPAGE);
        columnNumber = ConfigXmlUtils.getIntegerValue(element, ATTRIBUTE_COLUMN_NUMBER);
        rowNumber = ConfigXmlUtils.getIntegerValue(element, ATTRIBUTE_POSITION);
        gadgetXml = ConfigXmlUtils.getTextValue(element, ATTRIBUTE_GADGET_XML);
        portletId = ConfigXmlUtils.getTextValue(element, ATTRIBUTE_PORTLET_ID);
    }

    public ConfigGadget(ConfigGadget gadget)
    {
        //New ->
        //<PortletConfiguration id="10003" portalpage="10000" columnNumber="1" position="1"
        //   gadgetXml="rest/gadgets/1.0/g/com.atlassian.jira.gadgets:assigned-to-me-gadget/gadgets/assigned-to-me-gadget.xml"/>

        //Old ->
        //<PortletConfiguration id="10000" portalpage="10000" portletId="com.atlassian.jira.plugin.system.portlets:introduction"
        //   columnNumber="0" position="0"/>

        id = gadget.id;
        dashboardId = gadget.dashboardId;
        columnNumber = gadget.columnNumber;
        rowNumber = gadget.rowNumber;
        gadgetXml = gadget.gadgetXml;
        portletId = gadget.portletId;
    }

    public Long getId()
    {
        return id;
    }

    public ConfigGadget setId(final Long id)
    {
        this.id = id;
        return this;
    }

    public Long getDashboardId()
    {
        return dashboardId;
    }

    public ConfigGadget setDashboard(final Long dashboardId)
    {
        this.dashboardId = dashboardId;
        return this;
    }

    public Integer getColumnNumber()
    {
        return columnNumber;
    }

    public ConfigGadget setColumnNumber(final Integer columnNumber)
    {
        this.columnNumber = columnNumber;
        return this;
    }

    public Integer getRowNumber()
    {
        return rowNumber;
    }

    public ConfigGadget setRowNumber(final Integer rowNumber)
    {
        this.rowNumber = rowNumber;
        return this;
    }

    public String getGadgetXml()
    {
        return gadgetXml;
    }

    public ConfigGadget setGadgetXml(final String gadgetXml)
    {
        this.gadgetXml = gadgetXml;
        return this;
    }

    public String getPortletId()
    {
        return portletId;
    }

    public ConfigGadget setPortletId(final String portletId)
    {
        this.portletId = portletId;
        return this;
    }

    public void save(Element element)
    {
        //New ->
        //<PortletConfiguration id="10003" portalpage="10000" columnNumber="1" position="1"
        //   gadgetXml="rest/gadgets/1.0/g/com.atlassian.jira.gadgets:assigned-to-me-gadget/gadgets/assigned-to-me-gadget.xml"/>

        //Old ->
        //<PortletConfiguration id="10000" portalpage="10000" portletId="com.atlassian.jira.plugin.system.portlets:introduction"
        //   columnNumber="0" position="0"/>

        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_ID, getId());
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_PORTALPAGE, getDashboardId());
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_COLUMN_NUMBER, getColumnNumber());
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_POSITION, getRowNumber());
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_GADGET_XML, getGadgetXml());
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_PORTLET_ID, getPortletId());        
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final ConfigGadget that = (ConfigGadget) o;

        if (columnNumber != null ? !columnNumber.equals(that.columnNumber) : that.columnNumber != null)
        {
            return false;
        }
        if (dashboardId != null ? !dashboardId.equals(that.dashboardId) : that.dashboardId != null)
        {
            return false;
        }
        if (gadgetXml != null ? !gadgetXml.equals(that.gadgetXml) : that.gadgetXml != null)
        {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null)
        {
            return false;
        }
        if (portletId != null ? !portletId.equals(that.portletId) : that.portletId != null)
        {
            return false;
        }
        if (rowNumber != null ? !rowNumber.equals(that.rowNumber) : that.rowNumber != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (dashboardId != null ? dashboardId.hashCode() : 0);
        result = 31 * result + (columnNumber != null ? columnNumber.hashCode() : 0);
        result = 31 * result + (rowNumber != null ? rowNumber.hashCode() : 0);
        result = 31 * result + (gadgetXml != null ? gadgetXml.hashCode() : 0);
        result = 31 * result + (portletId != null ? portletId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
