package com.atlassian.jira.chartpopup.model;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Gadget DTO used for REST calls.
 *
 * @since v4.0
 */
@XmlRootElement
public class Gadget
{
    @XmlElement
    private final Long portalId;

    @XmlElement
    private final String gadgetUri;

    @XmlElement
    private final Long filterId;

    @XmlElement
    private final String filterName;

    @XmlElement
    private final String jql;

    @XmlElement
    private final Map<String, String> userPrefs;

    @SuppressWarnings({"UnusedDeclaration", "unused"})
    private Gadget()
    {
        portalId = null;
        gadgetUri = null;
        filterId = null;
        filterName = null;
        jql = null;
        userPrefs = new HashMap<String, String>();
    }

    public Gadget(final Long filterId, final String filterName, final String gadgetUri, final String jql, final Long portalId, final Map<String, String> userPrefs)
    {
        this.filterId = filterId;
        this.filterName = filterName;
        this.gadgetUri = gadgetUri;
        this.jql = jql;
        this.portalId = portalId;
        this.userPrefs = userPrefs;
    }

    public Long getFilterId()
    {
        return filterId;
    }

    public String getFilterName()
    {
        return filterName;
    }

    public URI getGadgetUri()
    {
        return URI.create(gadgetUri);
    }

    public String getJql()
    {
        return jql;
    }

    public Long getPortalId()
    {
        return portalId;
    }

    public Map<String, String> getUserPrefs()
    {
        return userPrefs;
    }
}
