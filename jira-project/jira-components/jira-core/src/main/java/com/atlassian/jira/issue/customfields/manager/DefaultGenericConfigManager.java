package com.atlassian.jira.issue.customfields.manager;

import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.map.NotNullHashMap;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class DefaultGenericConfigManager implements GenericConfigManager
{

    private static final Logger log = Logger.getLogger(DefaultGenericConfigManager.class);

    private final OfBizDelegator delegator;

    public DefaultGenericConfigManager(OfBizDelegator delegator)
    {
        this.delegator = delegator;
    }

    public void create(String dataType, String key, @Nullable Object obj)
    {
        if (obj != null)
        {
            String xml = toXml(obj);
            log.debug(obj + " stored as " + xml);
            Map fields = toFieldsMap(dataType, key, xml);

            EntityUtils.createValue(ENTITY_TABLE_NAME, fields);
        }
    }

    public void update(String dataType, String key, @Nullable Object obj)
    {
        if (obj != null)
        {
            Map fields = toFieldsMap(dataType, key, null);

            try
            {
                final List gvs = delegator.findByAnd(ENTITY_TABLE_NAME, fields);
                String xml = toXml(obj);

                if (gvs != null && !gvs.isEmpty())
                {
                    // @TODO Probably should do something when more than 1 is returned
                    final GenericValue gv = (GenericValue) gvs.iterator().next();
                    gv.setString(ENTITY_XML_VALUE, xml);
                    gv.store();
                }
                else
                {
                    fields.put(ENTITY_XML_VALUE, xml);
                    EntityUtils.createValue(ENTITY_TABLE_NAME, fields);
                }
            }
            catch (GenericEntityException e)
            {
                throw new DataAccessException(e);
            }
        }
        else
        {
            remove(dataType, key);
        }
    }

    public Object retrieve(String dataType, String key)
    {
        Map fields = toFieldsMap(dataType, key, null);


        final List gvs = delegator.findByAnd(ENTITY_TABLE_NAME, fields);

        if (gvs != null && !gvs.isEmpty())
        {
            // @TODO Probably should do something when more than 1 is returned
            final GenericValue gv = (GenericValue) gvs.iterator().next();
            String xml = gv.getString(ENTITY_XML_VALUE);
            return fromXml(xml);
        }
        else
        {
            return null;
        }

    }


    public void remove(String dataType, String key)
    {
        Map fields = toFieldsMap(dataType, key, null);
        delegator.removeByAnd(ENTITY_TABLE_NAME, fields);
    }


    private Map toFieldsMap(String dataType, String key, String xml)
    {
        Map fields = new NotNullHashMap();
        fields.put(ENTITY_DATA_TYPE, dataType);
        fields.put(ENTITY_DATA_KEY, key);
        fields.put(ENTITY_XML_VALUE, xml);
        return fields;
    }

    private String toXml(Object obj)
    {
        if (obj != null)
        {
            XStream xStream = new XStream();
            String xml = xStream.toXML(obj);
            return xml;
        }
        else
        {
            return null;
        }
    }

    private Object fromXml(String xml)
    {
        if (StringUtils.isNotEmpty(xml))
        {
            XStream xStream = new XStream();
            return xStream.fromXML(xml);
        }
        else
        {
            return null;
        }
    }


}
