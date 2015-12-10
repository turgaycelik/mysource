package com.atlassian.jira.crowd.embedded.ofbiz.db;

import com.google.common.collect.Maps;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.ModelEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;

import com.atlassian.jira.util.dbc.Assertions;

/**
 * Left over from the OFBiz Crowd SPI implementation.
 */
public class OfBizHelper
{
    /**
     * Converts a java.sql.Timestamp to a java.util.Date.
     * @param timestamp The java.sql.Timestamp
     * @return the java.util.Date.
     */
    public static java.util.Date convertToUtilDate(final Timestamp timestamp)
    {
        if (timestamp == null)
        {
            return null;
        }
        return new java.util.Date(timestamp.getTime());
    }

    /**
     * Converts a java.util.Date to a java.sql.Timestamp.
     * @param date The java.util.Date
     * @return the java.sql.Timestamp.
     */
    public static Timestamp convertToSqlTimestamp(final java.util.Date date)
    {
        if (date == null)
        {
            return null;
        }
        return new Timestamp(date.getTime());
    }
}
