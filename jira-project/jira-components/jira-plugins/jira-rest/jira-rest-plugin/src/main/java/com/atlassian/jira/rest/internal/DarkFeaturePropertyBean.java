package com.atlassian.jira.rest.internal;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Transport for dark feature enablement
 *
 * @since v5.2
 */

@XmlRootElement(name = "darkFeatureProperty")
public class DarkFeaturePropertyBean
{
    @XmlElement
    private boolean enabled;

    public DarkFeaturePropertyBean()
    {}

    public DarkFeaturePropertyBean(final boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isEnabled()
    {
        return enabled;
    }
}
