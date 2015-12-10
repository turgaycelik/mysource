package com.atlassian.jira.functest.config.dashboard;

import com.atlassian.jira.functest.config.ConfigXmlUtils;
import com.atlassian.jira.functest.config.sharing.ConfigSharedEntity;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * <p>Represents a PortalPage from JIRA's configuration (aka. dashboards).
 * <p/>
 * <p>Example:
 * <pre>
 *   &lt;PortalPage id="10000" pagename="System Dashboard" sequence="0" favCount="0" layout="AA" version="0"/&gt;
 * </pre>
 *
 * @since v4.2
 */
public class ConfigDashboard implements ConfigSharedEntity
{
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_USERNAME = "username";
    private static final String ATTRIBUTE_PAGENAME = "pagename";
    private static final String ATTRIBUTE_DESCRIPTION = "description";
    private static final String ATTRIBUTE_SEQUENCE = "sequence";
    private static final String ATTRIBUTE_LAYOUT = "layout";
    private static final String ATTRIBUTE_VERSION = "version";
    private static final String ATTRIBUTE_FAV_COUNT = "favCount";

    private static final String ENTITY_TYPE = "PortalPage";

    private Long id;
    private Long sequence;
    private String owner;
    private String name;
    private String description;
    private Long favouriteCount;
    private Long version;
    private String layout;
    private List<ConfigGadget> gadgets = new ArrayList<ConfigGadget>();

    public ConfigDashboard()
    {
    }

    static Long parseId(final Element element)
    {
        return ConfigXmlUtils.getLongValue(element, ATTRIBUTE_ID);
    }

    public ConfigDashboard(final ConfigDashboard other)
    {
        id = other.id;
        owner = other.owner;
        name = other.name;
        description = other.description;
        sequence = other.sequence;
        layout = other.layout;
        version = other.version;
        favouriteCount = other.favouriteCount;

        gadgets = new ArrayList<ConfigGadget>(other.gadgets.size());
        for (ConfigGadget gadget : other.gadgets)
        {
            gadgets.add(new ConfigGadget(gadget));
        }
    }

    public ConfigDashboard(Element element)
    {
        id = parseId(element);
        owner = ConfigXmlUtils.getTextValue(element, ATTRIBUTE_USERNAME);
        name = ConfigXmlUtils.getTextValue(element, ATTRIBUTE_PAGENAME);
        description = ConfigXmlUtils.getTextValue(element, ATTRIBUTE_DESCRIPTION);
        sequence = ConfigXmlUtils.getLongValue(element, ATTRIBUTE_SEQUENCE);
        layout = ConfigXmlUtils.getTextValue(element, ATTRIBUTE_LAYOUT);
        version = ConfigXmlUtils.getLongValue(element, ATTRIBUTE_VERSION);
        favouriteCount = ConfigXmlUtils.getLongValue(element, ATTRIBUTE_FAV_COUNT);
    }

    public boolean save(Element element)
    {
        ConfigDashboard oldAttributes = new ConfigDashboard(element);
        if (oldAttributes.isSameSavedState(this))
        {
            return false;
        }
        else
        {
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_ID, id);
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_USERNAME, owner);
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_PAGENAME, name);
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_DESCRIPTION, description);
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_SEQUENCE, sequence);
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_LAYOUT, layout);
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_VERSION, version);
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_FAV_COUNT, favouriteCount);
            return true;
        }
    }

    public String getEntityType()
    {
        return ENTITY_TYPE;
    }

    public Long getId()
    {
        return id;
    }

    public ConfigDashboard setId(final Long id)
    {
        this.id = id;
        return this;
    }

    public Long getSequence()
    {
        return sequence;
    }

    public ConfigDashboard setSequence(final Long sequence)
    {
        this.sequence = sequence;
        return this;
    }

    public String getOwner()
    {
        return owner;
    }

    public ConfigDashboard setOwner(final String owner)
    {
        this.owner = owner;
        return this;
    }

    public String getName()
    {
        return name;
    }

    public ConfigDashboard setName(final String name)
    {
        this.name = name;
        return this;
    }

    public String getDescription()
    {
        return description;
    }

    public ConfigDashboard setDescription(final String description)
    {
        this.description = description;
        return this;
    }

    public Long getFavouriteCount()
    {
        return favouriteCount;
    }

    public ConfigDashboard setFavouriteCount(final Long favouriteCount)
    {
        this.favouriteCount = favouriteCount;
        return this;
    }

    public Long getVersion()
    {
        return version;
    }

    public ConfigDashboard setVersion(final Long version)
    {
        this.version = version;
        return this;
    }

    public String getLayout()
    {
        return layout;
    }

    public ConfigDashboard setLayout(final String layout)
    {
        this.layout = layout;
        return this;
    }

    public boolean isSystem()
    {
        return owner == null;
    }

    public ConfigDashboard setGadgets(final Collection<? extends ConfigGadget> gadgets)
    {
        this.gadgets = gadgets == null ? new ArrayList<ConfigGadget>() : new ArrayList<ConfigGadget>(gadgets);
        return this;
    }

    public List<ConfigGadget> getGadgets()
    {
        return gadgets;
    }

    public ConfigDashboard reorderGadgets()
    {
        if (gadgets.isEmpty())
        {
            return this;
        }

        final List<ConfigGadget> sortedGadgets = new ArrayList<ConfigGadget>(gadgets);
        Collections.sort(sortedGadgets, GadgetPositionComparator.INSTANCE);

        int actualColumn = 0;
        int actualRow = 0;
        int lastColumn = getNullAsZero(sortedGadgets.get(0).getColumnNumber());
        for (ConfigGadget gadget : sortedGadgets)
        {
            int currentColumn = getNullAsZero(gadget.getColumnNumber());
            if (currentColumn != lastColumn)
            {
                actualRow = 0;
                actualColumn++;
                lastColumn = currentColumn;
            }
            gadget.setColumnNumber(actualColumn).setRowNumber(actualRow);
            actualRow++;
        }

        return this;
    }

    private static int getNullAsZero(Integer integer)
    {
        return integer == null ? 0 : integer;
    }

    /**
     * Return true if the saved dashboard state of the passed dashboard is the same as this dashboard. This method only
     * compares the state that is directly saved by the dashboard. Other state (e.g. gadgets) is not considered.
     *
     * @param dashboard the dashboard to compare.
     * @return true if the saved state of the passed dashboard is the same as the saved state of this dashboard.
     */
    private boolean isSameSavedState(ConfigDashboard dashboard)
    {
        if (description != null ? !description.equals(dashboard.description) : dashboard.description != null)
        {
            return false;
        }
        if (favouriteCount != null ? !favouriteCount.equals(dashboard.favouriteCount) : dashboard.favouriteCount != null)
        {
            return false;
        }
        if (id != null ? !id.equals(dashboard.id) : dashboard.id != null)
        {
            return false;
        }
        if (layout != null ? !layout.equals(dashboard.layout) : dashboard.layout != null)
        {
            return false;
        }
        if (name != null ? !name.equals(dashboard.name) : dashboard.name != null)
        {
            return false;
        }
        if (owner != null ? !owner.equals(dashboard.owner) : dashboard.owner != null)
        {
            return false;
        }
        if (sequence != null ? !sequence.equals(dashboard.sequence) : dashboard.sequence != null)
        {
            return false;
        }
        if (version != null ? !version.equals(dashboard.version) : dashboard.version != null)
        {
            return false;
        }
        return true;
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

        final ConfigDashboard dashboard = (ConfigDashboard) o;
        return isSameSavedState(dashboard) && gadgets.equals(dashboard.getGadgets());
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (sequence != null ? sequence.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (favouriteCount != null ? favouriteCount.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (layout != null ? layout.hashCode() : 0);
        result = 31 * result + gadgets.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    private static class GadgetPositionComparator implements Comparator<ConfigGadget>
    {
        private static final Comparator<ConfigGadget> INSTANCE = new GadgetPositionComparator();

        public int compare(final ConfigGadget o1, final ConfigGadget o2)
        {
            int col1 = getIntWithDefault(o1.getColumnNumber(), 0);
            int col2 = getIntWithDefault(o2.getColumnNumber(), 0);
            int row1 = getIntWithDefault(o1.getRowNumber(), Integer.MAX_VALUE);
            int row2 = getIntWithDefault(o2.getRowNumber(), Integer.MAX_VALUE);

            if (col1 == col2)
            {
                if (row1 == row2)
                {
                    return 0;
                }
                else if (row1 < row2)
                {
                    return -1;
                }
                else
                {
                    return 1;
                }
            }
            else if (col1 < col2)
            {
                return -1;
            }
            else
            {
                return 1;
            }
        }

        private static int getIntWithDefault(Integer intObj, int defaultValue)
        {
            return intObj == null ? defaultValue : intObj;
        }
    }
}
