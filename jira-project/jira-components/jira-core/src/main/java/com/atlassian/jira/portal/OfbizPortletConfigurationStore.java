package com.atlassian.jira.portal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.DatabaseIterable;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.util.Resolver;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.jira.util.collect.MapBuilder;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class OfbizPortletConfigurationStore implements PortletConfigurationStore
{
    private static final Logger log = Logger.getLogger(OfbizPortletConfigurationStore.class);

    public static final String TABLE = "PortletConfiguration";
    public static final String USER_PREFERENCES_TABLE = "GadgetUserPreference";

    public static final class Columns
    {
        public static final String COLOR = "color";
        public static final String GADGET_XML = "gadgetXml";
        public static final String PORTALPAGE = "portalpage";
        public static final String PORTLETKEY = "portletId";
        public static final String COLUMN = "columnNumber";
        public static final String ROW = "position";
        public static final String ID = "id";
    }

    public static final class UserPreferenceColumns
    {
        public static final String KEY = "userprefkey";
        public static final String VALUE = "userprefvalue";
        public static final String PORTLETID = "portletconfiguration";
    }

    private final OfBizDelegator delegator;

    public OfbizPortletConfigurationStore(final OfBizDelegator delegator)
    {
        notNull("delegator", delegator);

        this.delegator = delegator;
    }

    public List<PortletConfiguration> getByPortalPage(final Long portalPageId)
    {
        notNull("portalPageId", portalPageId);

        final List<GenericValue> portletConfigsListGVs = delegator.findByAnd(TABLE, EasyMap.build(Columns.PORTALPAGE, portalPageId));
        final List<PortletConfiguration> portletConfigs = new ArrayList<PortletConfiguration>(portletConfigsListGVs.size());

        for (final GenericValue portletConfigGV : portletConfigsListGVs)
        {
            portletConfigs.add(createConfigurationFromGv(portletConfigGV));
        }

        return portletConfigs;
    }  

    public PortletConfiguration getByPortletId(final Long portletId)
    {
        notNull("portletId", portletId);

        final List<GenericValue> portletConfigsListGVs = delegator.findByAnd(TABLE, MapBuilder.singletonMap(Columns.ID, portletId));
        if (!portletConfigsListGVs.isEmpty())
        {
            return createConfigurationFromGv(portletConfigsListGVs.get(0));
        }
        else
        {
            return null;
        }
    }

    public void delete(final PortletConfiguration pc)
    {
        notNull("pc", pc);
        notNull("pc.id", pc.getId());

        final GenericValue gv = getGenericValue(pc);

        if (gv != null)
        {
            OFBizPropertyUtils.removePropertySet(gv);
            delegator.removeValue(gv);
            //also delete any user preferences that may be linked against this portlet config.
            delegator.removeByAnd(USER_PREFERENCES_TABLE, EasyMap.build(UserPreferenceColumns.PORTLETID, pc.getId()));
        }
    }

    public void updateGadgetPosition(final Long gadgetId, final int row, final int column, final Long dashboardId)
    {
        notNull("gadgetId", gadgetId);
        notNull("dashboardId", dashboardId);

        final int rowsUpdated = delegator.bulkUpdateByPrimaryKey(TABLE, MapBuilder.<String, Object>newBuilder().
                add(Columns.ROW, row).
                add(Columns.COLUMN, column).
                add(Columns.PORTALPAGE, dashboardId).toMap(),
                CollectionBuilder.newBuilder(gadgetId).asList());
        if(rowsUpdated != 1)
        {
            throw new DataAccessException("Gadget position for gadget with id '" + gadgetId + "' not updated correctly.");
        }
    }

    public void updateGadgetColor(final Long gadgetId, final Color color)
    {
        notNull("gadgetId", gadgetId);
        notNull("color", color);

        final int rowsUpdated = delegator.bulkUpdateByPrimaryKey(TABLE, MapBuilder.singletonMap(Columns.COLOR, color.toString()), CollectionBuilder.list(gadgetId));
        if(rowsUpdated != 1)
        {
            throw new DataAccessException("Gadget color for gadget with id '" + gadgetId + "' not updated correctly.");
        }
    }

    public void updateUserPrefs(final Long gadgetId, final Map<String, String> userPrefs)
    {
        notNull("gadgetId", gadgetId);
        notNull("userPrefs", userPrefs);

        delegator.removeByAnd(USER_PREFERENCES_TABLE, EasyMap.build(UserPreferenceColumns.PORTLETID, gadgetId));

        for (Map.Entry<String, String> userPref : userPrefs.entrySet())
        {
            EntityUtils.createValue(USER_PREFERENCES_TABLE, MapBuilder.<String, Object>build(UserPreferenceColumns.KEY, userPref.getKey(),
                    UserPreferenceColumns.VALUE, userPref.getValue(), UserPreferenceColumns.PORTLETID, gadgetId));
        }
    }

    public void store(final PortletConfiguration pc)
    {
        notNull("pc", pc);
        notNull("pc.id", pc.getId());

        final GenericValue gv = getGenericValue(pc);
        if (gv == null)
        {
            throw new IllegalArgumentException("Can't store a portlet configuration that has no database entry.  Must add.");
        }

        gv.set(Columns.PORTALPAGE, pc.getDashboardPageId());
        gv.set(Columns.COLUMN, pc.getColumn());
        gv.set(Columns.ROW, pc.getRow());
        //legacy portlets may not have a gadget URI.
        if (pc.getGadgetURI() != null)
        {
            gv.set(Columns.GADGET_XML, pc.getGadgetURI().toASCIIString());
        }
        else
        {
            gv.set(Columns.GADGET_XML, null);
        }
        gv.set(Columns.COLOR, pc.getColor().toString());
        updateUserPrefs(pc.getId(), pc.getUserPrefs());

        delegator.store(gv);
    }

    public PortletConfiguration addGadget(final Long portalPageId, final Long portletConfigurationId, final Integer column, final Integer row,
            final URI gadgetXml, final Color color, final Map<String, String> userPreferences)
    {
        notNull("portalPageId", portalPageId);
        notNull("column", column);
        notNull("row", row);
        notNull("gadgetXml", gadgetXml);
        notNull("color", color);
        notNull("userPreferences", userPreferences);

        final GenericValue gv = EntityUtils.createValue(TABLE, MapBuilder.<String, Object>newBuilder()
                .add(Columns.PORTALPAGE, portalPageId)
                .add(Columns.ID, portletConfigurationId)
                .add(Columns.COLUMN, column)
                .add(Columns.ROW, row)
                .add(Columns.GADGET_XML, gadgetXml.toASCIIString())
                .add(Columns.COLOR, color.name()).toMap());

        updateUserPrefs(gv.getLong("id"), userPreferences);

        return createConfigurationFromGv(gv);
    }

    public EnclosedIterable<PortletConfiguration> getAllPortletConfigurations()
    {
        Resolver<GenericValue, PortletConfiguration> resolver = new Resolver<GenericValue, PortletConfiguration>()
        {
            public PortletConfiguration get(final GenericValue input)
            {
                return createConfigurationFromGv(input);
            }
        };

        return new DatabaseIterable<PortletConfiguration>(-1, resolver)
        {
            protected OfBizListIterator createListIterator()
            {
                return delegator.findListIteratorByCondition(TABLE, null);
            }
        };
    }

    private GenericValue getGenericValue(final PortletConfiguration pc)
    {
        return delegator.findById(TABLE, pc.getId());
    }

    private PortletConfiguration createConfigurationFromGv(final GenericValue portletConfigGV)
    {
        if (portletConfigGV == null)
        {
            return null;
        }

        final Long id = portletConfigGV.getLong(Columns.ID);
        final URI gadgetUri = getGadgetXmlURI(portletConfigGV.getString(Columns.GADGET_XML), id);
        final String colorString = portletConfigGV.getString(Columns.COLOR);
        final Map<String, String> userPrefs = getUserPreferences(id);

        //legacy portlets may not have a value for these fields
        final Color gadgetColor = StringUtils.isEmpty(colorString) ? null : Color.valueOf(colorString);

        return new PortletConfigurationImpl(id, portletConfigGV.getLong(Columns.PORTALPAGE),
                portletConfigGV.getInteger(Columns.COLUMN), portletConfigGV.getInteger(Columns.ROW), gadgetUri, gadgetColor, userPrefs);
    }

    Map<String, String> getUserPreferences(final Long id)
    {
        final Map<String, String> ret = new HashMap<String, String>();
        final List<GenericValue> list = delegator.findByAnd(USER_PREFERENCES_TABLE, MapBuilder.<String, Object>newBuilder().
                add(UserPreferenceColumns.PORTLETID, id).toMap());
        for (GenericValue userPrefGv : list)
        {
            String value = userPrefGv.getString(UserPreferenceColumns.VALUE);
            //JRA-18125: Oracle/Sybase store empty strings as null.  When reading these we need to convert them back
            //to empty strings!  Userprefs will only ever be strings and should not be null!
            if(value == null)
            {
                value = "";
            }
            ret.put(userPrefGv.getString(UserPreferenceColumns.KEY), value);
        }
        return ret;
    }

    private URI getGadgetXmlURI(final String gadgetXmlString, final Long portletId)
    {
        if (StringUtils.isNotEmpty(gadgetXmlString))
        {
            try
            {
                return new URI(gadgetXmlString);
            }
            catch (URISyntaxException e)
            {
                throw new DataAccessException("Invalid gadget XML URI stored for portlet with id '" + portletId + "': '" + gadgetXmlString + "'", e);
            }
        }
        return null;
    }

}
