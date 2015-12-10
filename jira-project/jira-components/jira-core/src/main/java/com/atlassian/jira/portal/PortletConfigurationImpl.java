package com.atlassian.jira.portal;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.gadgets.dashboard.Color;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the configuration for a portlet. This is the state that is saved to the database.
 * This implmentation uses a passed in PropertySet to store the configurable properties.  It is recommended that
 * an in memory PropertySet is used as the store now persists this manually.
 *
 * @since ??
 */
public class PortletConfigurationImpl implements PortletConfiguration
{
    private final Long id;
    private Long dashboardPageId;
    private Integer column;
    private Integer row;
    private Color color;
    private Map<String,String> userPrefs;
    private final URI gadgetUri;

    public PortletConfigurationImpl(final Long id, final Long dashboardPageId, final Integer column, final Integer row,
            final URI gadgetUri, final Color color, final Map<String,String> userPrefs)
    {
        this.id = id;
        this.dashboardPageId = dashboardPageId;
        this.column = column;
        this.row = row;
        this.gadgetUri = gadgetUri;
        //color1 will be the default color if none was specified!
        this.color = color == null ? Color.color1 : color;
        this.userPrefs = Collections.unmodifiableMap(new HashMap<String,String>(userPrefs));
    }

    public Long getId()
    {
        return id;
    }

    public Integer getColumn()
    {
        return column;
    }

    public void setColumn(final Integer column)
    {
        this.column = column;
    }

    public Integer getRow()
    {
        return row;
    }

    public Long getDashboardPageId()
    {
        return dashboardPageId;
    }

    public void setDashboardPageId(final Long dashboardPageId)
    {
        this.dashboardPageId = dashboardPageId;
    }

    public void setRow(final Integer row)
    {
        this.row = row;
    }

    public URI getGadgetURI()
    {
        return gadgetUri;
    }

    public Map<String, String> getUserPrefs()
    {
        return userPrefs;
    }

    public void setUserPrefs(final Map<String, String> userPrefs)
    {
        this.userPrefs = new HashMap<String,String>(userPrefs);
    }

    public Color getColor()
    {
        return color;
    }

    public void setColor(Color color)
    {
        this.color = color;
    }

    public int compareTo(final PortletConfiguration that)
    {
        return getRow().compareTo(that.getRow());
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
