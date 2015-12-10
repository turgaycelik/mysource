package com.atlassian.jira.portal;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.gadgets.dashboard.Color;

import com.opensymphony.module.propertyset.PropertySet;

import org.ofbiz.core.entity.GenericValue;

/**
 * Simple portlet configuration for tests.
 *
 * @since v3.13
 */

public class MockPortletConfiguration implements PortletConfiguration
{
    private final Long id;
    private final Integer suggestedCol;
    private Long portalPageId;
    private final Integer suggestRow;
    private final String portletKey;
    private final PropertySet propertySet;
    private URI gadgetXml;

    public MockPortletConfiguration(final long id, final long portalPageId, final String portletKey)
    {
        this(new Long(id), new Integer(0), new Integer(0), new Long(portalPageId), portletKey);
    }

    public MockPortletConfiguration(final Long id, final Integer suggestedCol, final Integer suggestRow, final Long portalPageId, final String portletKey)
    {
        this(id, suggestedCol, suggestRow, portalPageId, portletKey, null);
    }

    public MockPortletConfiguration(final Long id, final Integer suggestedCol, final Integer suggestRow, final Long portalPageId, final String portletKey, final PropertySet propertySet)
    {
        this(id, suggestedCol, suggestRow, portalPageId, portletKey, propertySet, null);
    }

    public MockPortletConfiguration(final Long id, final Integer suggestedCol, final Integer suggestRow, final Long portalPageId, final String portletKey, final PropertySet propertySet, final URI gadgetXml)
    {
        this.id = id;
        this.suggestedCol = suggestedCol;
        this.suggestRow = suggestRow;
        this.portalPageId = portalPageId;
        this.portletKey = portletKey;
        this.propertySet = propertySet;
        this.gadgetXml = gadgetXml;
    }

    public GenericValue getGenericValue()
    {
        return null;
    }

    public Long getId()
    {
        return id;
    }

    public Integer getColumn()
    {
        return suggestedCol;
    }

    public void setColumn(final Integer column)
    {}

    public Integer getRow()
    {
        return suggestRow;
    }

    public void setRow(final Integer row)
    {}

    public Long getDashboardPageId()
    {
        return portalPageId;
    }

    public void setDashboardPageId(final Long portalPageId)
    {
        this.portalPageId = portalPageId;
    }

    public boolean isResourcesProvided()
    {
        return false;
    }

    public URI getGadgetURI()
    {
        return gadgetXml;
    }

    public Map<String, String> getUserPrefs()
    {
        return new HashMap<String, String>();
    }

    public void setUserPrefs(final Map<String, String> userPrefs)
    {
    }

    public Color getColor()
    {
        return Color.color1;
    }

    public void setColor(final Color color)
    {
        
    }

    public int compareTo(final PortletConfiguration o)
    {
        return 0;
    }

    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
    {
        return null;
    }

    public boolean hasProperty(final String s) throws ObjectConfigurationException
    {
        return false;
    }

    public String getProperty(final String s) throws ObjectConfigurationException
    {
        return null;
    }

    public String getTextProperty(final String s) throws ObjectConfigurationException
    {
        return null;
    }

    public Long getLongProperty(final String s) throws ObjectConfigurationException
    {
        return null;
    }

    public String getDefaultProperty(final String s) throws ObjectConfigurationException
    {
        return null;
    }

    public PropertySet getProperties() throws ObjectConfigurationException
    {
        return propertySet;
    }

    public String getKey()
    {
        return portletKey;
    }

    @Override
    public String toString()
    {
        return "MockPortletConfiguration{" +
                "id=" + id +
                ", suggestedCol=" + suggestedCol +
                ", portalPageId=" + portalPageId +
                ", suggestRow=" + suggestRow +
                ", portletKey='" + portletKey + '\'' +
                ", propertySet=" + propertySet +
                ", gadgetXml=" + gadgetXml +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        MockPortletConfiguration that = (MockPortletConfiguration) o;

        if (gadgetXml != null ? !gadgetXml.equals(that.gadgetXml) : that.gadgetXml != null)
        {
            return false;
        }
        if (!id.equals(that.id))
        {
            return false;
        }
        if (portalPageId != null ? !portalPageId.equals(that.portalPageId) : that.portalPageId != null)
        {
            return false;
        }

        if (portletKey != null ? !portletKey.equals(that.portletKey) : that.portletKey != null)
        {
            return false;
        }
        if (propertySet != null ? !propertySet.equals(that.propertySet) : that.propertySet != null)
        {
            return false;
        }
        if (suggestRow != null ? !suggestRow.equals(that.suggestRow) : that.suggestRow != null)
        {
            return false;
        }
        if (suggestedCol != null ? !suggestedCol.equals(that.suggestedCol) : that.suggestedCol != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id.hashCode();
        result = 31 * result + (suggestedCol != null ? suggestedCol.hashCode() : 0);
        result = 31 * result + (portalPageId != null ? portalPageId.hashCode() : 0);
        result = 31 * result + (suggestRow != null ? suggestRow.hashCode() : 0);
        result = 31 * result + (portletKey != null ? portletKey.hashCode() : 0);
        result = 31 * result + (propertySet != null ? propertySet.hashCode() : 0);
        result = 31 * result + (gadgetXml != null ? gadgetXml.hashCode() : 0);
        return result;
    }
}
