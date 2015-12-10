package com.atlassian.jira.functest.config.dashboard;

import com.atlassian.jira.functest.config.ConfigXmlUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.dom4j.Element;

/**
 * Represents an "&lt;ExternalGadget&gt; from the JIRA configuration. 
 *
 * @since v4.2
 */
public class ConfigExternalGadget
{
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_GADGET_XML = "gadgetXml";

    private Long id;
    private String gadgetXml;

    public ConfigExternalGadget()
    {
    }

    public ConfigExternalGadget(final ConfigExternalGadget other)
    {
        this.id = other.id;
        this.gadgetXml = other.gadgetXml;
    }

    public ConfigExternalGadget(final Element element)
    {
        id = ConfigXmlUtils.getLongValue(element, ATTRIBUTE_ID);
        gadgetXml = ConfigXmlUtils.getTextValue(element, ATTRIBUTE_GADGET_XML);
    }

    public Long getId()
    {
        return id;
    }

    public ConfigExternalGadget setId(final Long id)
    {
        this.id = id;
        return this;
    }

    public String getGadgetXml()
    {
        return gadgetXml;
    }

    public ConfigExternalGadget setGadgetXml(final String gadgetXml)
    {
        this.gadgetXml = gadgetXml;
        return this;
    }

    public void save(Element element)
    {
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_ID, id);
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_GADGET_XML, gadgetXml);
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

        final ConfigExternalGadget that = (ConfigExternalGadget) o;

        if (gadgetXml != null ? !gadgetXml.equals(that.gadgetXml) : that.gadgetXml != null)
        {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (gadgetXml != null ? gadgetXml.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
